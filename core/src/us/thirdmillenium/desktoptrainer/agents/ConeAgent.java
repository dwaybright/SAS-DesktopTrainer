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

import com.badlogic.gdx.ai.pfa.GraphPath;
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
import us.thirdmillenium.desktoptrainer.ai.tile.TileNode;
import us.thirdmillenium.desktoptrainer.brains.Brain;
import us.thirdmillenium.desktoptrainer.brains.NeuralNetworkBrain;
import us.thirdmillenium.desktoptrainer.graphics.GraphicsHelpers;
import us.thirdmillenium.desktoptrainer.environment.GreenBullet;
import us.thirdmillenium.desktoptrainer.graphics.Line;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;


public class ConeAgent extends AgentModel {
    // Environment Trackers
    private TiledMap gameMap;
    private Set<GreenBullet> bulletTracker;
    private Set<AgentModel> enemyTracker;
    private Set<AgentModel> teamTracker;
    private Set<Line> collisionLines;

    // Agent Information
    private Sprite sprite;
    private GraphPath<TileNode> preferredPath;
    private Vector2 position;
    private float rotation;
    private int degreesOfView;      // How many degrees I can see ahead of me
    private int visionDepth;        // How far I can see

    // Last Computed Information
    private Brain brain;
    private double[] input;
    private double[] output;

    // Other Information
    private Random random;


    public ConeAgent(Vector2 startPosition, float startAngle, int degreesOfView, int visionDepth,
                     Random random, Set<Line> collisionLines, String nnetPath, GraphPath<TileNode> prefPath,
                     TiledMap gameMap, Set<AgentModel> team, Set<AgentModel> enemies, Set<GreenBullet> bullets) {

        // Agent Attribs
        this.brain = new NeuralNetworkBrain(nnetPath);
        this.preferredPath = prefPath;
        this.position = startPosition;
        this.rotation = startAngle;
        this.degreesOfView = degreesOfView;
        this.visionDepth = visionDepth;

        // Environment Trackers
        this.gameMap = gameMap;
        this.bulletTracker = bullets;
        this.enemyTracker = enemies;
        this.teamTracker = team;
        this.collisionLines = collisionLines;

        // Other information
        this.random = random;

        // Agent Sprite Config
        this.sprite = new Sprite();
        this.sprite.setCenter(startPosition.x, startPosition.y);
        this.sprite.rotate(startAngle);
    }



    @Override
    public void agentHit() {

    }


    @Override
    public void updateAgent(float deltaTime) {
        // Compute the distance and item seen
        this.input = computeVision();

        // Brain Crunch!
        this.output = this.brain.brainCrunch(this.input);

        // Update Agent Position and Rotation
        updatePosition(output);
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

            // Detect Path only when an Agent hasn't been seen on this degree line.
            if( !seenAgent ) {
                TileNode node;

                for(int index = 0; index < this.preferredPath.getCount(); index++) {
                    node = this.preferredPath.get(index);

                    // Place radius 3 circle over preferred path node
                    if( Intersector.intersectSegmentCircle(this.position, endPoint, node.getPixelVector2(), 9) ) {
                        distToObject = this.position.dst(node.getPixelVector2());

                        // Check if Agents are within Vision Depth
                        if (distToObject < distance[i]) {
                            item[i] = Params.ConeVisPath;
                            distance[i] = distToObject;
                        }
                    }
                }
            }


            // Normalize distance to (agent edge -> 1)
            if( distance[i] < Params.AgentCircleRadius ) { distance[i] = Params.AgentCircleRadius; }
            distance[i] = distance[i] / (float)this.visionDepth;
        }

        return GraphicsHelpers.interleaveDoubleArrays(item, distance);
    }


    private void updatePosition(double[] output) {
        // Compute Angle Change  ( -1 Hard Counter Clockwise, +1 Hard Clockwise, 0 is no rotation )
        float angleChange = (float)(2 * (0.5 - output[0]) * Params.AgentMaxTurnAngle);
        this.rotation += angleChange;

        // Compute Movement Length in Pixels  ( -0.2 Backward, +0.8 Forward )
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

    }

    @Override
    public void drawVision(ShapeRenderer sr) {

    }

    @Override
    public Vector2 getPosition() {
        return this.position;
    }


    /**
     * Need to check that the Agent doesn't run through walls or off the map.
     *
     * @param newPosition
     */
    private Vector2 boundaryCheckNewPosition(Vector2 newPosition) {
        Rectangle boundRect = this.sprite.getBoundingRectangle();
        Polygon   boundPoly = GraphicsHelpers.convertRectangleToPolygon(boundRect);

        // Check World Map Boundaries First, just fudge back in if needed
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

        // Check Walls Second
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

        // Check with other Agents, make sure not overlapping


        return newPosition;
    }
}
