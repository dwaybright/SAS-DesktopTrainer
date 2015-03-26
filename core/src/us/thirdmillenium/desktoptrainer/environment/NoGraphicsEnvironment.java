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

import com.badlogic.gdx.ai.pfa.indexed.DefaultIndexedGraph;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.Array;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import org.neuroph.core.NeuralNetwork;

import us.thirdmillenium.desktoptrainer.ai.TileNode;
import us.thirdmillenium.desktoptrainer.TrainingParams;


public class NoGraphicsEnvironment {
	// Bullet Tracker
	private HashSet<GreenBullet> BulletTracker;
	
    // The Tiled Map Assets for this Environment
    private String LevelMapPath;
    private TiledMap MyTiledMap;
    private TiledMapRenderer TiledMapRenderer;
    private int TileSize;
    private Random random = new Random();

    // A mapping of all Nodes that are traversible, and a ShapeRenderer to plot them
    private IndexedGraph<TileNode> TileNodeGraph;
    private HashMap<Integer, TileNode> TraverseNodes;
    
    // NN Agent to test
	@SuppressWarnings("rawtypes")
	private NeuralNetwork trainingNeuralNet;

	
    /**
     * The constructor takes in a "*.tmx" file, and converts to TileMap.
     * Also prepares LibGDX req'd graphical stuff.
     *
     * @param levelMapName
     */
	public NoGraphicsEnvironment(String levelMapPath, int tileSize) {
        // Screen width and height
        float w = 800; //Gdx.graphics.getWidth();
        float h = 1216; //Gdx.graphics.getHeight();
        
        // Init Bullet Tracker
        this.BulletTracker = new HashSet<GreenBullet>(20);

        // Setup map asset
        this.LevelMapPath = levelMapPath;
        this.MyTiledMap = new TmxMapLoader().load(levelMapPath);
        this.TiledMapRenderer = new OrthogonalTiledMapRenderer(this.MyTiledMap);
        this.TileSize = tileSize;

        // Generate TileMap Objects
        createGraphFromTileMap(w, h, tileSize);
    }
    
	
    public void setupSimulation() {
    	// Setup Enemy Shooters
    	
    	
    	// Setup training NN
    	
    }
    
    
    public long simulate() {
    	// Setup NN and Enemy Shooters
    	setupSimulation();
    	long score = 0;
    	double timeDelta = 1 / TrainingParams.FramesPerSecond;
    	
    	// Simulate for a maximum number of time steps
    	for(int t = 0; t < TrainingParams.SimulationTimeSteps; t++) {
    		
    		// Update Bullets
    		
    		
    		// Update Training Agent
    		
    		
    		// Update Training Shooters
    		
    		
    	}
    	
    	return (long)this.random.nextInt(1500);
    }

    
    /**
     * Generates a mapping of all tiles that are traversible by an Agent
     * @return an IndexedGraph for an A* implementation
     */
    private void createGraphFromTileMap(float width, float height, int tilePixel) {
        try {
            this.TraverseNodes = new HashMap<Integer, TileNode>();
            Array<TileNode> myNodes = new Array<TileNode>();
            TileNode temp;

            int halfTilePixel = tilePixel / 2;
            int numCellY = (int) height / 32;
            int numCellX = (int) width / 32;

            TiledMapTileLayer wallLayer = (TiledMapTileLayer) this.MyTiledMap.getLayers().get(1);

            // Step 1 : Collect all open tiles, create a TileNode for it, and add

            for (int cellX = 0; cellX < numCellX; cellX++) {

                // Calculate the pixel point for this X
                int x = halfTilePixel + (tilePixel * cellX);

                for (int cellY = 0; cellY < numCellY; cellY++) {

                    // Calculate the pixel point for this Y
                    // Need to reverse because OpenGL indexes opposite of Tiled
                    //int y = (numCellY * tilePixel) - (halfTilePixel + (tilePixel * cellY));
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

    
    @SuppressWarnings("rawtypes")
	public void setNeuralNetwork(NeuralNetwork nnet) {
    	this.trainingNeuralNet = nnet;
    }
    
}
