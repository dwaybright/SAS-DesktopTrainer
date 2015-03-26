/*
 Copyright (C) 2015 Daniel Waybright, daniel.waybright@gmail.com

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License along
 with this program (located in root of GitHub folder); if not, visit:

    http://www.apache.org/licenses/LICENSE-2.0


 **** Special Thanks ****

 This project makes extensive use of the LibGDX library
 http://libgdx.badlogicgames.com/index.html

 */

package us.thirdmillenium.desktoptrainer.environment;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.ai.pfa.indexed.DefaultIndexedGraph;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

import java.util.Collections;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.neuroph.core.NeuralNetwork;

import us.thirdmillenium.desktoptrainer.agents.TrainingAgent;
import us.thirdmillenium.desktoptrainer.agents.TrainingShooter;
import us.thirdmillenium.desktoptrainer.ai.TileNode;
import us.thirdmillenium.desktoptrainer.TrainingParams;


public class TestEnvironment implements InputProcessor {
    // Debug Flag
    private boolean DEBUG = false;
    private boolean DRAW = false;
    //private float lastAngle = 0;
    
    // Bullet Tracker
 	private Set<GreenBullet> BulletTracker;
 	
 	// Agent Trackers
 	private Set<TrainingShooter> shooters;
 	private Set<TrainingAgent> trainees; 

    // OpenGL Camera for Orientation
    private OrthographicCamera Camera;

    // The Tiled Map Assets for this Environment
    private TiledMap TiledMap;
    private TiledMapRenderer TiledMapRenderer;
    private float width;
    private float height;

    // A mapping of all Nodes that are traversible, and a ShapeRenderer to plot them
	private IndexedGraph<TileNode> TileNodeGraph;
    private ConcurrentHashMap<Integer, TileNode> TraverseNodes;
    
    // Shape Renderer
    private ShapeRenderer MapNodeSR;

    // A Renderer for the Agents
    private SpriteBatch SpriteBatchRenderer;

    // An A* Debug Renderer
    private ShapeRenderer LineRenderer;
    

    /**
     * The constructor takes in a "*.tmx" file, and converts to TileMap.
     * Also prepares LibGDX req'd graphical stuff.
     *
     */
    public TestEnvironment(@SuppressWarnings("rawtypes") NeuralNetwork nnet, Random random, int testLevelID, int startX, int startY) {
        // Screen width and height
        this.width = 800;	//Gdx.graphics.getWidth();
        this.height = 1216;	//Gdx.graphics.getHeight();
        //Gdx.graphics.setDisplayMode((int)width, (int)height, false);

        // Setup camera
        this.Camera = new OrthographicCamera();
        this.Camera.setToOrtho(false, width, height);
        this.Camera.update();

        // Setup map asset
        this.TiledMap = new TmxMapLoader().load(TrainingParams.TileMapsPath + "TestLevel" + testLevelID + ".tmx");
        this.TiledMapRenderer = new OrthogonalTiledMapRenderer(this.TiledMap);

        // Setup input (motion grabbing) processing
        //Gdx.input.setInputProcessor(this);

        // Setup Rendering Objects
        //this.MapNodeSR = new ShapeRenderer();
        //this.SpriteBatchRenderer = new SpriteBatch();
        //this.LineRenderer = new ShapeRenderer();
        
        // Setup Trackers
        this.BulletTracker = Collections.newSetFromMap(new ConcurrentHashMap<GreenBullet, Boolean>());
        this.trainees      = Collections.newSetFromMap(new ConcurrentHashMap<TrainingAgent, Boolean>());
        this.shooters      = Collections.newSetFromMap(new ConcurrentHashMap<TrainingShooter, Boolean>());
        
        // Generate TileMap Objects
        createGraphFromTileMap();
        
        // Add a Training Shooter
        //this.shooters.add(new TrainingShooter(190,  630, this.trainees, this.shooters, this.BulletTracker, random));
        //this.shooters.add(new TrainingShooter(540,  700, this.trainees, this.shooters, this.BulletTracker, random));
        //this.shooters.add(new TrainingShooter(510,   90, this.trainees, this.shooters, this.BulletTracker, random));
        //this.shooters.add(new TrainingShooter( 65, 1090, this.trainees, this.shooters, this.BulletTracker, random));
        //this.shooters.add(new TrainingShooter(740, 1090, this.trainees, this.shooters, this.BulletTracker, random));
        //this.shooters.add(new TrainingShooter(410,  960, this.trainees, this.shooters, this.BulletTracker, random));
        
        // Add the Trainee
        this.trainees.add(new TrainingAgent(testLevelID, nnet, startX, startY, this.TraverseNodes, random, this.TiledMap,
        				  this.trainees, this.shooters, this.BulletTracker));
    }


    /**
     * Called from the Android "render", this method updates everything that
     * needs to be drawn.
     */
    public long simulate() {
    	float deltaTime = (float) (1 / TrainingParams.FramesPerSecond);
    	
    	for(int p = 0; p < TrainingParams.SimulationTimeSteps; p++ ) {
    	
	    	// Compute time delta (max of frame speed)
	    	//deltaTime = (float) Math.min(deltaTime, 1 / TrainingParams.FramesPerSecond);
	    	
	    	
	    	/*if( DRAW ) {
		    	// Clear Background
		        Gdx.gl.glClearColor(1, 0, 0, 1);
		        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		        // Draw Map
		        this.Camera.update();
		        this.TiledMapRenderer.setView(this.Camera);
		        this.TiledMapRenderer.render();
	    	}*/
	
	
	        // Draw DEBUG information
	        /*if( DEBUG && DRAW) {
	            // Draw Map Nodes
	            this.MapNodeSR.setProjectionMatrix(this.Camera.combined);
	            this.MapNodeSR.setColor(Color.OLIVE);
	            this.MapNodeSR.begin(ShapeRenderer.ShapeType.Filled);
	
	            if (this.TraverseNodes != null) {
	                for (Integer key : this.TraverseNodes.keySet()) {
	                    this.MapNodeSR.circle(this.TraverseNodes.get(key).getPixelX(), this.TraverseNodes.get(key).getPixelY(), 10);
	                }
	            }
	
	            this.MapNodeSR.end();
	            
	            // Draw Overlay Lines
	            this.LineRenderer.setProjectionMatrix(this.Camera.combined);
	            this.LineRenderer.begin(ShapeRenderer.ShapeType.Filled);
	
	            // For each Agent.  Different Colors?
	            this.LineRenderer.setColor(Color.BLACK);
	            
	            try{
	    		    Iterator<TrainingAgent> agentItr = this.trainees.iterator();
	    		    
	    		    while(agentItr.hasNext()) {
	    		    	TrainingAgent currAgent = agentItr.next();
	    		    	currAgent.drawPreferredPath(this.LineRenderer);
	    		    }
	            } catch( Exception ex) {  Do nothing  }
	
	            this.LineRenderer.end();
	        }*/
	        
	
	        // Draw Agent Sprites
	        //this.SpriteBatchRenderer.setProjectionMatrix(this.Camera.combined);
	        //this.SpriteBatchRenderer.begin();
	
	        Iterator<TrainingShooter> shootItr = this.shooters.iterator();
	        
	        while(shootItr.hasNext()) {
	        	TrainingShooter currShooter = shootItr.next();
	        	
	        	currShooter.updateAgent(deltaTime);
	        	if( DRAW ) { currShooter.drawAgent(this.SpriteBatchRenderer); }
	        }
	        
	        try{
			    Iterator<TrainingAgent> agentItr = this.trainees.iterator();
			    
			    while(agentItr.hasNext()) {
			    	TrainingAgent currAgent = agentItr.next();
			    	
			    	currAgent.updateAgent(deltaTime);
			    	if( DRAW ) { currAgent.drawAgent(this.SpriteBatchRenderer); }
			    }
	        } catch( Exception ex) { /* Do nothing */ }
	        
	
	        //this.SpriteBatchRenderer.end();
	
	
	        // Draw Bullet every 5 seconds (Debug help)
	        /*if( this.BulletTracker.size() == 0 ) {
	        	Vector2 tempLoc = new Vector2(190,630);
	
	        	this.BulletTracker.add(new GreenBullet(tempLoc, this.lastAngle++));
	        	
	        	if( this.lastAngle == 360) {
	        		this.lastAngle = 0;
	        	}
	        }*/
	        
	        
	        // Test if Bullets Intersected with Anything
	        MapObjects wallMapObjects = this.TiledMap.getLayers().get(2).getObjects();
	        //ShapeRenderer anotherShapeRenderer = new ShapeRenderer();
	        Iterator<GreenBullet> bullets = this.BulletTracker.iterator();
	        //this.SpriteBatchRenderer.begin();
	        
	        while(bullets.hasNext()) {
	        	// Collect a Bullet to consider
	        	GreenBullet currentBullet = bullets.next();
	        	
	        	if( DRAW ) { currentBullet.drawSprite(this.SpriteBatchRenderer); }
	        	
	        	currentBullet.updateBullet(deltaTime);
	        	
	        	// If bullet is off-screen, remove it.
	        	if( currentBullet.getBulletVector().x < 0 || 
	        		currentBullet.getBulletVector().x > this.width || 
	        		currentBullet.getBulletVector().y < 0 ||
	        		currentBullet.getBulletVector().y > this.height) 
	        	{
	        		this.BulletTracker.remove(currentBullet);
	        	} else {
		        	// Compare with all Agents
		        	
	        		
		        	
		        	// Compare with all Wall Boundaries
		        	for( int i = 0; i < wallMapObjects.getCount(); i++) {
		        		Object rectangleMapObject = wallMapObjects.get(i);
			        	
		        		// Make sure this is a Rectangle from Tiled describing a wall.
			        	if( rectangleMapObject.getClass() == RectangleMapObject.class ) {
			        		Rectangle wallRectangle = ((RectangleMapObject)rectangleMapObject).getRectangle();
			        		Polygon polyBound = GraphicsHelpers.convertRectangleToPolygon(wallRectangle);	        		
			        		
			        		// Terminate when hitting a wall
			        		if( Intersector.overlapConvexPolygons(polyBound, currentBullet.getBulletPath())) {
			        			this.BulletTracker.remove(currentBullet);
			        		}
			        	}
		        	}
	        	}
	        }
	        //anotherShapeRenderer.end();
	        //this.SpriteBatchRenderer.end();
	        
	        // Test Draw the Collision Boxes
	        /*if( DEBUG && DRAW ) {
		        //anotherShapeRenderer.setProjectionMatrix(this.Camera.combined);
		        //anotherShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
		        
		        bullets = this.BulletTracker.iterator();
		        
		        while(bullets.hasNext()) {
		        	GreenBullet currentBullet = bullets.next();
		        	anotherShapeRenderer.polygon(currentBullet.getBulletPath().getTransformedVertices());
		        }
		        
		        for(int i = 0; i < wallMapObjects.getCount(); i++ ){
		        	Object obj = wallMapObjects.get(i);
		        	
		        	if( obj.getClass() == RectangleMapObject.class ) {
		        		Rectangle boundary = ((RectangleMapObject)obj).getRectangle();
		        		anotherShapeRenderer.rect(boundary.x, boundary.y, boundary.width, boundary.height);
		        		
		        		float[] vertices = { 
		        			boundary.x, boundary.y, 
		        			boundary.x + boundary.width, boundary.y,
		        			boundary.x + boundary.width, boundary.y + boundary.height,
		        			boundary.x, boundary.y + boundary.height
		        		};
		        		
		        		//Polygon polyBound = new Polygon(vertices);
		        		anotherShapeRenderer.setColor(Color.BLUE);
		        		anotherShapeRenderer.polygon(vertices);
		        	}
		        }
		        
		        anotherShapeRenderer.end();
	        }*/
    	}
        
    	
    	// Collect Scores from the Training Agent(s) and return
	    Iterator<TrainingAgent> agentItr = this.trainees.iterator();
	    long score = 0;
	    
	    while(agentItr.hasNext()) {
	    	TrainingAgent currAgent = agentItr.next();
	    	score += currAgent.getScore();
	    }        
	    
	    return score;
    }


    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
    	//System.out.println((screenX*2) + "," + ((38*32)-(screenY*2)));
    	
    	System.out.println((screenX*2) + "," + (screenY*2));
    	
    	System.out.println("GDX H: " + Gdx.graphics.getHeight());
    	
    	//TrainingAgent ta = new TrainingAgent(null, null, (screenX*2), ((38*32)-(screenY*2)), this.trainees, this.shooters, this.BulletTracker);
    	//this.trainees.add(ta);
    	
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }


    /**
     * Generates a mapping of all tiles that are traversible by an Agent.
     * 
     * Pixel (16,16) actually matched Cell (0,0) now.
     * 
     * @return an IndexedGraph for an A* implementation
     */
    private void createGraphFromTileMap() {
        try {
            this.TraverseNodes = new ConcurrentHashMap<Integer, TileNode>();
            Array<TileNode> myNodes = new Array<TileNode>();
            TileNode temp;

            int tilePixel = TrainingParams.MapTileSize;
            int halfTilePixel = TrainingParams.MapTileSize / 2;
            int numCellY = TrainingParams.NumCellsY;
            int numCellX = TrainingParams.NumCellsX;

            TiledMapTileLayer wallLayer = (TiledMapTileLayer) this.TiledMap.getLayers().get(1);

            // Step 1 : Collect all open tiles, create a TileNode for it, and add

            for (int cellX = 0; cellX < numCellX; cellX++) {

                // Calculate the pixel point for this X
                int x = halfTilePixel + (tilePixel * cellX);

                // Need to reverse Y to match (0,0) being bottom left corner in LibGDX
                for (int cellY = 37; cellY >= 0; cellY--) {

                    // Calculate the pixel point for this Y
                    int y = halfTilePixel + (tilePixel * cellY);

                    if (!wallLayer.getCell(cellX, cellY).getTile().getProperties().containsKey("blocked")) {
                        // Generate a unique (sequential start at 0) ID for key, then generate TileNode
                        Integer key = new Integer( (cellX * numCellY) + cellY );
                        temp = new TileNode(x, y, cellX, cellY, key);

                        // Add node to the Array<> and the HashMap
                        myNodes.add(temp);
                        this.TraverseNodes.put(key, temp);
                    }
                }
            }

            // Step 2 : Build TileNode Connections

            TileNode node;
            int idTag;

            for( int cellX = 0; cellX < numCellX; cellX++) {

                for( int cellY = 0; cellY < numCellY; cellY++) {

                    // This cell
                    idTag = (cellX * numCellY) + cellY;
                    node = findIndex(idTag);

                    /*
                     *      Check eight surrounding cells
                     */

                    if( node != null ) {

                        // Top Left
                        int topLeftX = cellX - 1;
                        int topLeftY = cellY + 1;
                        int topLeftID = (topLeftX * numCellY) + topLeftY;

                        if (topLeftX >= 0 && topLeftY < numCellY) {
                            temp = findIndex(topLeftID);

                            // If it exists, add to connection list for the TileNode being considered.
                            if (temp != null) {
                                node.addConnection(temp);
                            }
                        }

                        // Left
                        int leftX = cellX - 1;
                        int leftY = cellY;
                        int leftID = (leftX * numCellY) + leftY;

                        if (leftX >= 0) {
                            temp = findIndex(leftID);

                            // If it exists, add to connection list for the TileNode being considered.
                            if (temp != null) {
                                node.addConnection(temp);
                            }
                        }

                        // Bottom Left
                        int botLeftX = cellX - 1;
                        int botLeftY = cellY - 1;
                        int botLeftID = (botLeftX * numCellY) + botLeftY;

                        if (botLeftX >= 0 && botLeftY >= 0) {
                            temp = findIndex(botLeftID);

                            // If it exists, add to connection list for the TileNode being considered.
                            if (temp != null) {
                                node.addConnection(temp);
                            }
                        }


                        // Top
                        int topX = cellX;
                        int topY = cellY + 1;
                        int topID = (topX * numCellY) + topY;

                        if (topY < numCellY) {
                            temp = findIndex(topID);

                            // If it exists, add to connection list for the TileNode being considered.
                            if (temp != null) {
                                node.addConnection(temp);
                            }
                        }

                        // Bottom
                        int botX = cellX;
                        int botY = cellY - 1;
                        int botID = (botX * numCellY) + botY;

                        if (botY >= 0) {
                            temp = findIndex(botID);

                            // If it exists, add to connection list for the TileNode being considered.
                            if (temp != null) {
                                node.addConnection(temp);
                            }
                        }

                        // Top Right
                        int topRightX = cellX + 1;
                        int topRightY = cellY + 1;
                        int topRightID = (topRightX * numCellY) + topRightY;

                        if (topRightX < numCellX && topRightY < numCellY) {
                            temp = findIndex(topRightID);

                            // If it exists, add to connection list for the TileNode being considered.
                            if (temp != null) {
                                node.addConnection(temp);
                            }
                        }

                        // Right
                        int rightX = cellX + 1;
                        int rightY = cellY;
                        int rightID = (rightX * numCellY) + rightY;

                        if (rightX < numCellX) {
                            temp = findIndex(rightID);

                            // If it exists, add to connection list for the TileNode being considered.
                            if (temp != null) {
                                node.addConnection(temp);
                            }
                        }

                        // Bottom Right
                        int botRightX = cellX + 1;
                        int botRightY = cellY - 1;
                        int botRightID = (botRightX * numCellY) + botRightY;

                        if (botRightX < numCellX && botRightY >= 0) {
                            temp = findIndex(botRightID);

                            // If it exists, add to connection list for the TileNode being considered.
                            if (temp != null) {
                                node.addConnection(temp);
                            }
                        }
                    }

                }
            }

            this.TileNodeGraph = new DefaultIndexedGraph<TileNode>(myNodes);

        } catch(Exception ex)
        {
        	System.out.println(ex.getMessage());
        }
    }


    /**
     * A helper function that searches for the TileNode index provided.
     * @param index
     */
    private TileNode findIndex(Integer index) {
        TileNode temp = null;

        if(this.TraverseNodes.containsKey(index)) {
            temp = this.TraverseNodes.get(index);
        }

        return temp;
    }

}
