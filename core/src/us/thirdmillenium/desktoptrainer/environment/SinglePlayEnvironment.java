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
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.indexed.DefaultIndexedGraph;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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

import us.thirdmillenium.desktoptrainer.agents.PuppetAgent;
import us.thirdmillenium.desktoptrainer.agents.TrainingAgent;
import us.thirdmillenium.desktoptrainer.agents.TrainingShooter;
import us.thirdmillenium.desktoptrainer.ai.TileAStarPathFinder;
import us.thirdmillenium.desktoptrainer.ai.TileNode;
import us.thirdmillenium.desktoptrainer.TrainingParams;


public class SinglePlayEnvironment extends Environment implements InputProcessor {
    // Debug Flag
    private boolean DEBUG = true;
    private boolean DRAW = true;
    
    private boolean PUPPET = false;
    private boolean NNET = true;
    //private float lastAngle = 0;
    
    // Bullet Tracker
 	private Set<GreenBullet> BulletTracker;
 	
 	// Agent Trackers
 	private Set<TrainingShooter> shooters;
 	private Set<TrainingAgent> trainees; 
 	private PuppetAgent puppet;

    // OpenGL Camera for Orientation
    private OrthographicCamera Camera;

    // The Tiled Map Assets for this Environment
    private TiledMap TiledMap;
    private TiledMapRenderer TiledMapRenderer;
    private float width;
    private float height;

    // A mapping of all Nodes that are traversible, and a ShapeRenderer to plot them
	private GraphPath<TileNode> TileNodeGraph;
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
     */
    public SinglePlayEnvironment(String nnetPath, Random random, int testLevelID) {
        // Screen width and height
        this.width = 800;	//Gdx.graphics.getWidth();
        this.height = 1216;	//Gdx.graphics.getHeight();
        //Gdx.graphics.setDisplayMode((int)width, (int)height, false);
        
        String levelPath = TrainingParams.TileMapsPath + "TestLevel" + testLevelID + ".tmx";
        

        // Setup camera
        this.Camera = new OrthographicCamera();
        this.Camera.setToOrtho(false, width, height);
        this.Camera.update();

        // Setup map asset
        this.TiledMap = new TmxMapLoader().load(levelPath);
        this.TiledMapRenderer = new OrthogonalTiledMapRenderer(this.TiledMap);

        // Setup input (motion grabbing) processing
        Gdx.input.setInputProcessor(this);

        // Setup Rendering Objects
        this.MapNodeSR = new ShapeRenderer();
        this.SpriteBatchRenderer = new SpriteBatch();
        this.LineRenderer = new ShapeRenderer();
        
        // Setup Trackers
        this.BulletTracker = Collections.newSetFromMap(new ConcurrentHashMap<GreenBullet, Boolean>());
        this.trainees      = Collections.newSetFromMap(new ConcurrentHashMap<TrainingAgent, Boolean>());
        this.shooters      = Collections.newSetFromMap(new ConcurrentHashMap<TrainingShooter, Boolean>());
        
        // Generate TileMap Objects
        this.TraverseNodes = new ConcurrentHashMap<Integer, TileNode>();
        this.TileNodeGraph = super.createGraphFromTileMap(this.TraverseNodes, (TiledMapTileLayer) this.TiledMap.getLayers().get(1));
        
        // Add a Training Shooter
        this.shooters.add(new TrainingShooter(190,  630, this.trainees, this.shooters, this.BulletTracker, random));
        this.shooters.add(new TrainingShooter(540,  700, this.trainees, this.shooters, this.BulletTracker, random));
        this.shooters.add(new TrainingShooter(510,   90, this.trainees, this.shooters, this.BulletTracker, random));
        this.shooters.add(new TrainingShooter( 65, 1090, this.trainees, this.shooters, this.BulletTracker, random));
        this.shooters.add(new TrainingShooter(740, 1090, this.trainees, this.shooters, this.BulletTracker, random));
        this.shooters.add(new TrainingShooter(410,  960, this.trainees, this.shooters, this.BulletTracker, random));
        
        int startX = 16;
        int startY = 16;
        
        switch(testLevelID) {
        case 1:
        	startX = 16;
            startY = 16;
            break;
        case 2:
        	startX = 784;
            startY = 16;
            break;
        case 3:
        	startX = 784;
            startY = 1200;
            break;
        case 4:
        	startX = 16;
            startY = 1200;
            break;
        case 5:
        	startX = 16;
            startY = 16;
            break;
        }
        
        // Add the Trainee
        if( NNET ) { 
        	this.trainees.add(new TrainingAgent(testLevelID, NeuralNetwork.createFromFile(nnetPath), startX, startY,
                              this.TraverseNodes, random, this.TiledMap, this.trainees, this.shooters, this.BulletTracker));
        }
        
        if( PUPPET ) {
        	this.puppet = new PuppetAgent(this.TiledMap, this.TraverseNodes, new TileAStarPathFinder(), startX, startY, this.BulletTracker, this.shooters);
        }
    }


    @Override
    public void simulate() { /* Do Nothing */ }


    @Override
    public void simulate(float deltaTime) {
    	// Compute time delta (max of frame speed)
    	deltaTime = (float) Math.min(deltaTime, 1 / TrainingParams.FramesPerSecond);
    	
    	if( DRAW ) {
	    	// Clear Background
	        Gdx.gl.glClearColor(1, 0, 0, 1);
	        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
	        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
	
	        // Draw Map
	        this.Camera.update();
	        this.TiledMapRenderer.setView(this.Camera);
	        this.TiledMapRenderer.render();
    	}


        // Draw DEBUG information
        if( DEBUG && DRAW) {
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
            
            if( PUPPET ) {
            	this.puppet.drawLines(this.LineRenderer);
            }
            
            try{
    		    Iterator<TrainingAgent> agentItr = this.trainees.iterator();
    		    
    		    while(agentItr.hasNext()) {
    		    	TrainingAgent currAgent = agentItr.next();
    		    	currAgent.drawPreferredPath(this.LineRenderer);
    		    }
            } catch( Exception ex) { /* Do nothing */ }

            this.LineRenderer.end();
        }
        

        // Draw Agent Sprites
        this.SpriteBatchRenderer.setProjectionMatrix(this.Camera.combined);
        this.SpriteBatchRenderer.begin();
        
        if( PUPPET ) {
        	this.puppet.updateAgentState();
        	this.puppet.drawAgent(this.SpriteBatchRenderer);
        }

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
        
        
        // Test if Bullets Intersected with Anything
        MapObjects wallMapObjects = this.TiledMap.getLayers().get(2).getObjects();
        ShapeRenderer anotherShapeRenderer = new ShapeRenderer();
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
        anotherShapeRenderer.end();
        this.SpriteBatchRenderer.end();
        
        // Test Draw the Collision Boxes
        if( DEBUG && DRAW ) {
	        anotherShapeRenderer.setProjectionMatrix(this.Camera.combined);
	        anotherShapeRenderer.begin(ShapeRenderer.ShapeType.Line);
	        
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
        }
        
        
    }

    @Override
    public long getScore() {
        return 0;
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
    	
    	this.puppet.setPathToGoal((2*screenX), 1216 - (2*screenY));
    	
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

}
