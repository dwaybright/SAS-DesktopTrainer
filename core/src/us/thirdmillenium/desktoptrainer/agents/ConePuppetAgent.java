/*
 Copyright (C) 2015 Daniel Waybright, daniel.waybright@gmail.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package us.thirdmillenium.desktoptrainer.agents;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import us.thirdmillenium.desktoptrainer.Params;
import us.thirdmillenium.desktoptrainer.ai.tile.TileAStarPathFinder;
import us.thirdmillenium.desktoptrainer.ai.tile.TileHeuristic;
import us.thirdmillenium.desktoptrainer.ai.tile.TileNode;
import us.thirdmillenium.desktoptrainer.brains.Brain;
import us.thirdmillenium.desktoptrainer.brains.NeuralNetworkBrain;
import us.thirdmillenium.desktoptrainer.environment.GreenBullet;
import us.thirdmillenium.desktoptrainer.graphics.GraphicsHelpers;
import us.thirdmillenium.desktoptrainer.graphics.Line;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class ConePuppetAgent extends AgentModel {
    // Environment Trackers
    private TiledMap gameMap;
    private Set<GreenBullet> bulletTracker;
    private Set<AgentModel> enemyTracker;
    private Set<AgentModel> teamTracker;
    private Set<Line> collisionLines;

    // Agent Position Information
    private Sprite sprite;
    private Vector2 position;
    private float rotation;

    // Agent Preferred Path
    private TileAStarPathFinder pathFinder;
    private ConcurrentHashMap<Integer, TileNode> mapNodes;
    private GraphPath<TileNode> preferredPath;
    private HashSet<TileNode> preferredPathNodeTracker;
    private int preferredPathIndex;

    // Agent Vision
    private int degreesOfView;      // How many degrees I can see ahead of me
    private int visionDepth;        // How far I can see

    // Agent Health
    private boolean ALIVE;
    private int health;

    // Last Computed Information
    private File csvFile;
    private boolean writeDataToFile;
    private Brain brain;
    private double[] input;
    private double[] output;
    private float[] visionPolygonVertices;

    // Other Information
    private Random random;


    public ConePuppetAgent(Vector2 startPosition, float startAngle, int degreesOfView, int visionDepth, int health, String spritePNG, boolean writeDataToFile,
                     Random random, Set<Line> collisionLines, String nnetPath, GraphPath<TileNode> prefPath, ConcurrentHashMap<Integer, TileNode> mapNodes,
                     TiledMap gameMap, Set<AgentModel> team, Set<AgentModel> enemies, Set<GreenBullet> bullets) {

        // Agent Position and Movement Config
        this.brain = new NeuralNetworkBrain(nnetPath);
        this.position = startPosition;
        this.rotation = startAngle;

        // Agent Preferred Path
        this.pathFinder = new TileAStarPathFinder();
        this.mapNodes = mapNodes;
        this.preferredPath = prefPath;
        this.preferredPathIndex = 0;

        // Agent Vision Config
        this.visionPolygonVertices = new float[(1 + this.degreesOfView) * 2];
        this.degreesOfView = degreesOfView;
        this.visionDepth = visionDepth;

        // Agent Health
        this.ALIVE = true;
        this.health = health;

        // Agent Sprite Config
        this.sprite = new Sprite(new Texture(spritePNG));
        this.sprite.setCenter(startPosition.x, startPosition.y);
        this.sprite.rotate(startAngle);

        // Environment Trackers
        this.gameMap = gameMap;
        this.bulletTracker = bullets;
        this.enemyTracker = enemies;
        this.teamTracker = team;
        this.collisionLines = collisionLines;

        // Writing Training Data
        this.writeDataToFile = writeDataToFile;
        if( writeDataToFile ) { this.csvFile = new File(Params.PathToCSV); }

        // Other information
        this.random = random;
    }



    @Override
    public void agentHit() {
        if( (--this.health) < 1 ) {
            this.ALIVE = false;
            this.sprite = new Sprite(new Texture(Params.DeadAgentPNG));
        }
    }


    @Override
    public void updateAgent(float deltaTime) {
        if( this.ALIVE ) {
            // Compute the distance and item seen
            this.input = computeVision();

            // Brain Crunch!
            this.output = this.brain.brainCrunch(this.input);

            // Update Agent Position and Rotation
            updatePosition(output);

            // Write out training data
            if(this.writeDataToFile) { writeTrainingData(); }
        }
    }

    /**
     *  Write out a Frame of training data.
     */
    private void writeTrainingData() {
        PrintWriter csvWriter = null;

        try{
            // Use if currently exists or create fresh file
            if( !this.csvFile.exists() ) {
                this.csvFile.createNewFile();
            }

            // Write out to file
            csvWriter = new PrintWriter(new FileOutputStream(this.csvFile), true);

            for(int i = 0; i < this.input.length; i++ ) {
                csvWriter.print( this.input[i] );
                csvWriter.print( "," );
            }

            for(int i = 0; i < this.output.length; i++) {
                csvWriter.print( this.output[i] );
                csvWriter.print( "," );
            }

            csvWriter.println();

        } catch (Exception ex) {

        } finally {
            if( csvWriter != null ) { csvWriter.close(); }
        }
    }

    @Override
    public void setPathToGoal(float goalX, float goalY) {
        // Reset Index Tracker
        this.preferredPathIndex = 1;

        // Start and Goal node
        TileNode startNode = GraphicsHelpers.findTileNodeByPixelLocation((int)this.position.x, (int)this.position.y, this.mapNodes);
        TileNode endNode   = GraphicsHelpers.findTileNodeByPixelLocation((int)goalX, (int)goalY, this.mapNodes);

        // The returned path once computed
        this.preferredPath = new DefaultGraphPath<TileNode>();

        // Compute Path!
        this.pathFinder.searchNodePath(startNode, endNode, new TileHeuristic(), this.preferredPath);

        this.preferredPath.reverse();

        // Node Tracker
        Iterator<TileNode> itr = this.preferredPath.iterator();
        this.preferredPathNodeTracker = new HashSet<TileNode>(300);

        while(itr.hasNext()) {
            TileNode tile = itr.next();

            if( this.preferredPathNodeTracker.contains(tile) ) {
                itr.remove();
            } else {
                this.preferredPathNodeTracker.add(tile);
            }
        }
    }

    @Override
    public int getTraverseNodeIndex() {
        return GraphicsHelpers.getCurrentCellIndex((int) this.position.x, (int) this.position.y);
    }


    /**
     * Computes distance and what is seen for each degree of viewing angle.
     *
     */
    private double[] computeVision() {
        double[] item = new double[this.degreesOfView];
        double[] distance = new double[this.degreesOfView];
        float startDeg = this.rotation + (this.degreesOfView / 2);
        float degree = 0;

        // Store the first point for the vision Polygon
        this.visionPolygonVertices[0] = this.position.x;
        this.visionPolygonVertices[1] = this.position.y;

        // Consider every degree angle coming from Agent
        for(int i = 0; i < this.degreesOfView; i++ ) {
            // Set values for this degree and arrays
            degree      = startDeg - i;
            item[i]     = Params.ConeVisEmpty;
            distance[i] = this.visionDepth;

            // Variables to hold intermediate calcs
            Vector2 intersection = new Vector2();
            float distToObject;
            boolean seenAgent = false;

            // Calculate the direction for the Agent at this angle degree
            Vector2 direction = new Vector2(0,1);
            direction.rotate(degree);

            // Calculate the furthest point Agent can see for this degree
            Vector2 endPoint = this.position.cpy();
            endPoint.mulAdd(direction, this.visionDepth);


            // The boundRect is used for Sprite collision calcs
            Rectangle agentBoundRect = new Rectangle(0, 0, Params.AgentTileSize, Params.AgentTileSize);
            Rectangle bulletBoundRect = new Rectangle(0, 0, 5, 8);   // Hard coded!!! Gah!!


            // Detect Bullets?


            // Detect Enemy Agents
            Iterator<AgentModel> enemyItr = this.enemyTracker.iterator();
            AgentModel enemy;

            while(enemyItr.hasNext()) {
                enemy = enemyItr.next();

                // If segment intersects circle
                if( Intersector.intersectSegmentCircle(this.position, endPoint, enemy.getPosition(), Params.AgentRadiusSquared) ) {
                    distToObject = this.position.dst(enemy.getPosition()) - (Params.AgentTileSize / 2.2f);

                    // Check if Agents are within Vision Depth
                    if (distToObject < distance[i]) {
                        item[i] = Params.ConeVisEnemy;
                        distance[i] = distToObject;
                        seenAgent = true;
                    }
                }
            }

            // Detect Friendly Agents
            Iterator<AgentModel> teamItr = this.teamTracker.iterator();
            AgentModel team;

            while(teamItr.hasNext()) {
                team = teamItr.next();

                // If segment intersects circle
                if( Intersector.intersectSegmentCircle(this.position, endPoint, team.getPosition(), Params.AgentRadiusSquared) ) {
                    distToObject = this.position.dst(team.getPosition()) - (Params.AgentTileSize / 2);

                    // Check if Agents are within Vision Depth
                    if (distToObject < distance[i]) {
                        item[i] = Params.ConeVisTeam;
                        distance[i] = distToObject;
                        seenAgent = true;
                    }
                }
            }

            // Detect Collision with Walls or Boundary
            Iterator<Line> lineItr = this.collisionLines.iterator();
            Line line;

            while(lineItr.hasNext()) {
                line = lineItr.next();

                if( Intersector.intersectLines(this.position, endPoint, line.start, line.end, intersection) ) {
                    distToObject = intersection.dst(this.position);

                    if( distToObject < distance[i] ) {
                        item[i] = Params.ConeVisWall;
                        distance[i] = distToObject;
                        seenAgent = false;                  // if true, then Agent on other side of wall
                    }
                }
            }

            // Detect Path ONLY when an Agent hasn't been seen on this degree line.
            if( !seenAgent ) {
                TileNode node;

                for(int index = 0; index < this.preferredPath.getCount(); index++) {
                    node = this.preferredPath.get(index);

                    // Place radius 5 circle over preferred path node
                    if( Intersector.intersectSegmentCircle(this.position, endPoint, node.getPixelVector2(), 25) ) {
                        distToObject = this.position.dst(node.getPixelVector2());

                        // Check if Agents are within Vision Depth
                        if (distToObject < distance[i]) {
                            item[i] = Params.ConeVisPath;
                            distance[i] = distToObject;
                        }
                    }
                }
            }

            // Store the x,y point for this degree line
            this.visionPolygonVertices[2 + (i * 2)] = this.position.x + (direction.x * (float)distance[i]);
            this.visionPolygonVertices[3 + (i * 2)] = this.position.y + (direction.y * (float)distance[i]);

            // Normalize distance to (agent edge -> 1)
            if( distance[i] < Params.AgentCircleRadius ) { distance[i] = Params.AgentCircleRadius; }
            distance[i] = distance[i] / (float)this.visionDepth;
        }

        return GraphicsHelpers.interleaveDoubleArrays(item, distance);
    }


    /**
     * This method takes the output from the Brain, and moves Agent / shoots weapon.
     *
     * @param output from a Brain
     */
    private void updatePosition(double[] output) {
        // Compute Angle Change  ( -1 Hard Counter Clockwise, +1 Hard Clockwise, 0 is no rotation )
        float angleChange = (float)(2 * (0.5 - output[0]) * Params.AgentMaxTurnAngle);
        this.rotation += angleChange;

        // Compute Movement Length in Pixels  ( -0.25 Backward, +1 Forward )
        float movement = (float)(1.25 * (output[1] - 0.2) * Params.AgentMaxMovement);

        // Compute new Agent position
        Vector2 newPosition = this.position.cpy().mulAdd((new Vector2(0,1)).rotate(this.rotation), movement);

        // Bound Check, keep Agent from running over walls or off map
        this.position = boundaryCheckNewPosition(newPosition);

        // Finally, update Sprite position
        this.sprite.setCenter(this.position.x, this.position.y);
    }


    @Override
    public void drawAgent(SpriteBatch sb) {
        this.sprite.draw(sb);
    }

    @Override
    public void drawPath(ShapeRenderer sr) {
        // Draws the CurrentPath.
        if( this.preferredPath != null) {
            for (int i = 1; i < this.preferredPath.getCount(); i++) {
                sr.rectLine(this.preferredPath.get(i - 1).getPixelX(), this.preferredPath.get(i - 1).getPixelY(),
                        this.preferredPath.get(i).getPixelX(), this.preferredPath.get(i).getPixelY(), 5);
            }
        }
    }

    @Override
    public void drawVision(ShapeRenderer sr) {
        sr.polygon(this.visionPolygonVertices);
    }

    @Override
    public Vector2 getPosition() {
        return this.position;
    }

    @Override
    public Rectangle getBoundingRectangle() {
        return this.sprite.getBoundingRectangle();
    }

    @Override
    public long getScore() {
        return 0;
    }


    /**
     * Need to check that the Agent doesn't run through walls or off the map.
     *
     * @param newPosition
     */
    private Vector2 boundaryCheckNewPosition(Vector2 newPosition) {
        Rectangle boundRect = this.sprite.getBoundingRectangle();
        Polygon boundPoly = GraphicsHelpers.convertRectangleToPolygon(boundRect);

        /*
         *  Check World Map Boundaries First, just fudge back in if needed
         */

        if( boundRect.x < 0 ) {
            newPosition.x += Math.abs((int)boundRect.x);
        } else if( boundRect.x > (Params.MapTileSize * Params.NumCellsX) - (Params.AgentTileSize) ) {
            newPosition.x = (float) ((Params.MapTileSize * Params.NumCellsX) - (Params.AgentTileSize));
        }

        if( boundRect.y < 0 ) {
            newPosition.y += Math.abs((int)boundRect.y);
        } else if( boundRect.y > (Params.MapTileSize * Params.NumCellsY) - (Params.AgentTileSize) ) {
            newPosition.y = (float) ((Params.MapTileSize * Params.NumCellsY) - (Params.AgentTileSize));
        }

        /*
         *  Check Walls Second
         */
        MapObjects wallMapObjects = this.gameMap.getLayers().get(2).getObjects();

        for( int i = 0; i < wallMapObjects.getCount(); i++) {
            Object rectangleMapObject = wallMapObjects.get(i);
            Intersector.MinimumTranslationVector mtv = new Intersector.MinimumTranslationVector();

            // Make sure this is a Rectangle from Tiled describing a wall.
            if( rectangleMapObject.getClass() == RectangleMapObject.class ) {
                Rectangle wallRectangle = ((RectangleMapObject)rectangleMapObject).getRectangle();
                Polygon polyBound = GraphicsHelpers.convertRectangleToPolygon(wallRectangle);

                // If hitting a wall, kick back to be off wall
                if( Intersector.overlapConvexPolygons(boundPoly, polyBound, mtv)) {
                    newPosition.x += mtv.depth * mtv.normal.x;
                    newPosition.y += mtv.depth * mtv.normal.y;
                }
            }
        }

        /*
         *  Check against other Agents
         */
        Rectangle intersection = new Rectangle();

        // Check Enemy Agents
        Iterator<AgentModel> enemyItr = this.enemyTracker.iterator();
        AgentModel enemy;

        while(enemyItr.hasNext()) {
            enemy = enemyItr.next();

            // If bounding rectangles overlap, shuffle Agent in X to keep from overlapping
            if( Intersector.intersectRectangles(boundRect, enemy.getBoundingRectangle(), intersection) ) {
                if( boundRect.x >= intersection.x ) {
                    newPosition.x += intersection.x;
                } else {
                    newPosition.x -= intersection.x;
                }
            }
        }

        // Check Friendly Agents
        Iterator<AgentModel> teamItr = this.teamTracker.iterator();
        AgentModel team;

        while(teamItr.hasNext()) {
            team = teamItr.next();

            // If bounding rectangles overlap, shuffle Agent
            if( Intersector.intersectRectangles(boundRect, team.getBoundingRectangle(), intersection) ) {
                if( boundRect.x >= intersection.x ) {
                    newPosition.x += intersection.x;
                } else {
                    newPosition.x -= intersection.x;
                }
            }
        }


        return newPosition;
    }
}