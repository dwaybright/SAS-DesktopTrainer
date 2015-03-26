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


public class TestEnvironment extends Environment implements InputProcessor {
    // Debug Flag
    private boolean DEBUG = false;
    private boolean DRAW = false;
    //private float lastAngle = 0;
    
    // Bullet Tracker
 	private Set<GreenBullet> bulletTracker;
 	
 	// Agent Trackers
 	private Set<TrainingShooter> shooters;
 	private Set<TrainingAgent> trainees; 

    // OpenGL camera for Orientation
    private OrthographicCamera camera;

    // The Tiled Map Assets for this Environment
    private TiledMap tiledMap;
    private TiledMapRenderer tiledMapRenderer;
    private float width;
    private float height;

    // A mapping of all Nodes that are traversible, and a ShapeRenderer to plot them
	private GraphPath<TileNode> tileNodeGraph;
    private ConcurrentHashMap<Integer, TileNode> traverseNodes;
    
    // Shape Renderer
    private ShapeRenderer mapNodeSR;

    // A Renderer for the Agents
    private SpriteBatch spriteBatchRenderer;

    // An A* Debug Renderer
    private ShapeRenderer lineRenderer;
    

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
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, width, height);
        this.camera.update();

        // Setup map asset
        this.tiledMap = new TmxMapLoader().load(TrainingParams.TileMapsPath + "TestLevel" + testLevelID + ".tmx");
        this.tiledMapRenderer = new OrthogonalTiledMapRenderer(this.tiledMap);
        
        // Setup Trackers
        this.bulletTracker = Collections.newSetFromMap(new ConcurrentHashMap<GreenBullet, Boolean>());
        this.trainees      = Collections.newSetFromMap(new ConcurrentHashMap<TrainingAgent, Boolean>());
        this.shooters      = Collections.newSetFromMap(new ConcurrentHashMap<TrainingShooter, Boolean>());

        // Generate TileMap Objects
        this.traverseNodes = new ConcurrentHashMap<Integer, TileNode>();
        this.tileNodeGraph = super.createGraphFromTileMap(this.traverseNodes, (TiledMapTileLayer) this.tiledMap.getLayers().get(1));
        
        // Add a Training Shooter
        //this.shooters.add(new TrainingShooter(190,  630, this.trainees, this.shooters, this.bulletTracker, random));
        //this.shooters.add(new TrainingShooter(540,  700, this.trainees, this.shooters, this.bulletTracker, random));
        //this.shooters.add(new TrainingShooter(510,   90, this.trainees, this.shooters, this.bulletTracker, random));
        //this.shooters.add(new TrainingShooter( 65, 1090, this.trainees, this.shooters, this.bulletTracker, random));
        //this.shooters.add(new TrainingShooter(740, 1090, this.trainees, this.shooters, this.bulletTracker, random));
        //this.shooters.add(new TrainingShooter(410,  960, this.trainees, this.shooters, this.bulletTracker, random));
        
        // Add the Trainee
        this.trainees.add(new TrainingAgent(testLevelID, nnet, startX, startY, this.traverseNodes, random, this.tiledMap,
        				  this.trainees, this.shooters, this.bulletTracker));
    }


    @Override
    public void simulate() {
    	float deltaTime = (float) (1 / TrainingParams.FramesPerSecond);
    	
    	for(int p = 0; p < TrainingParams.SimulationTimeSteps; p++ ) {
	
	        Iterator<TrainingShooter> shootItr = this.shooters.iterator();
	        
	        while(shootItr.hasNext()) {
	        	TrainingShooter currShooter = shootItr.next();
	        	
	        	currShooter.updateAgent(deltaTime);
	        }
	        
	        try{
			    Iterator<TrainingAgent> agentItr = this.trainees.iterator();
			    
			    while(agentItr.hasNext()) {
			    	TrainingAgent currAgent = agentItr.next();
			    	
			    	currAgent.updateAgent(deltaTime);
			    }
	        } catch( Exception ex) { /* Do nothing */ }
	        
	        
	        // Test if Bullets Intersected with Anything
	        MapObjects wallMapObjects = this.tiledMap.getLayers().get(2).getObjects();
	        Iterator<GreenBullet> bullets = this.bulletTracker.iterator();
	        
	        while(bullets.hasNext()) {
	        	// Collect a Bullet to consider
	        	GreenBullet currentBullet = bullets.next();
	        	currentBullet.updateBullet(deltaTime);
	        	
	        	// If bullet is off-screen, remove it.
	        	if( currentBullet.getBulletVector().x < 0 || 
	        		currentBullet.getBulletVector().x > this.width || 
	        		currentBullet.getBulletVector().y < 0 ||
	        		currentBullet.getBulletVector().y > this.height) 
	        	{
	        		this.bulletTracker.remove(currentBullet);
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
			        			this.bulletTracker.remove(currentBullet);
			        		}
			        	}
		        	}
	        	}
	        }
    	}

    }

    @Override
    public void simulate(float deltaTime) {

    }

    @Override
    public long getScore() {
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
    	
    	//TrainingAgent ta = new TrainingAgent(null, null, (screenX*2), ((38*32)-(screenY*2)), this.trainees, this.shooters, this.bulletTracker);
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
     * A helper function that searches for the TileNode index provided.
     * @param index
     */
    private TileNode findIndex(Integer index) {
        TileNode temp = null;

        if(this.traverseNodes.containsKey(index)) {
            temp = this.traverseNodes.get(index);
        }

        return temp;
    }

}
