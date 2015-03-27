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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import us.thirdmillenium.desktoptrainer.ai.TileAStarPathFinder;
import us.thirdmillenium.desktoptrainer.ai.TileHeuristic;
import us.thirdmillenium.desktoptrainer.ai.TileNode;
import us.thirdmillenium.desktoptrainer.brains.Brain;
import us.thirdmillenium.desktoptrainer.brains.PuppetBrain;
import us.thirdmillenium.desktoptrainer.environment.GraphicsHelpers;
import us.thirdmillenium.desktoptrainer.environment.GreenBullet;
import us.thirdmillenium.desktoptrainer.TrainingParams;


public class PuppetAgent extends AgentModel {
    // Game World
    private TiledMap MyTiledMap;
    private Sprite Sprite;
    private Texture Alive;
    private Texture Dead;
    private int TileSize;
    private ConcurrentHashMap<Integer, TileNode> MapNodes;
    private TileAStarPathFinder PathFinder;
    private int AgentSize = 10;  // 20 x 20
    private Set<GreenBullet> bullets;
    private Set<TrainingShooter> shooters;

    // Agent location
    private HashSet<TileNode> currPathNodeTracker;
    private GraphPath<TileNode> CurrentPath;
    private int CurrentPathIndex;
    private float Pixel_X;
    private float Pixel_Y;
    private int Cell_X;
    private int Cell_Y;
    private float Angle;
    private float deltaAngle;

    // Agent Physical Attributes
    private int Health;
    private float MovementSpeed;
    private float MovementSpeedScalar;
    private float Eyesight;
    private float Hearing;

    // Agent Equipment
    //private WeaponModel MyWeapon;
    //private ArmorModel MyArmor;

    // Agent Mental Model
    private Brain Control;
    private int agentShoot;
    
    
    // Output Path
    private String csvOutputPath = TrainingParams.PathToCSV;
    private File csvOutputFile;
    private ArrayList<double[]> trainingData;


    public PuppetAgent(TiledMap myTiledMap, ConcurrentHashMap<Integer, TileNode> mapNodes,
                       TileAStarPathFinder pathFinder, int pixelX, int pixelY, Set<GreenBullet> bullets, Set<TrainingShooter> shooters)
    {
        // Setup game world parameters
        this.MyTiledMap = myTiledMap;
        this.TileSize = TrainingParams.MapTileSize;
        this.MapNodes = mapNodes;
        this.PathFinder = pathFinder;
        this.bullets = bullets;
        this.shooters = shooters;

        // Setup Graphics
        this.Alive = new Texture(TrainingParams.TrainingAgentLivePNG);
        this.Dead = new Texture(TrainingParams.DeadAgentPNG);
        this.Sprite = new Sprite(Alive);

        // Setup Start Location
        this.CurrentPathIndex = -1;
        this.Pixel_X = pixelX;
        this.Pixel_Y = pixelY;
        this.Angle = 270;

        // Set Basic Values
        this.Health = 50;
        this.MovementSpeed = 50;
        this.MovementSpeedScalar = (float) (TrainingParams.AgentMaxMovement / TrainingParams.FramesPerSecond);
        this.Eyesight = 10;
        this.Hearing = 20;
        

        // Set Control
        this.Control = new PuppetBrain();
        
        // Finally, set Sprite
        this.Sprite.setCenter(pixelX, pixelY);
        this.Sprite.setRotation(this.Angle);
        
        // Training CSV
        this.csvOutputFile = new File(this.csvOutputPath);
        this.trainingData = new ArrayList<double[]>();
    }

    @Override
    public void agentHit() {
        throw new NotImplementedException();
    }
    
    private void writeTrainingData() {
    	PrintWriter csvWriter = null;
    	
    	try{
	    	// Delete if currently exists, then create fresh file
			if( !this.csvOutputFile.exists() ) { 
				this.csvOutputFile.createNewFile();
			}
			
			// Write out to file
			csvWriter = new PrintWriter(this.csvOutputFile);
			
			for(int i = 0; i < this.trainingData.size(); i++ ) {
				double[] temp = this.trainingData.get(i);
				
				for(int j = 0; j < temp.length; j++ ) {
					csvWriter.print( temp[j] );
					csvWriter.print( "," );
				}
				
				csvWriter.println("");
			}
			
    	} catch (Exception ex) {
    		
    	} finally {
    		if( csvWriter != null ) { csvWriter.close(); }
    	}
    }

    @Override
    public void updateAgentState(float deltaTime) {
        // Bounds Check - Do nothing
        if( this.CurrentPath == null) {
            return;
        } else if (this.CurrentPath.getCount() < 1 || this.CurrentPathIndex < 0) {
        	writeTrainingData();
        	this.CurrentPath = null;
            return;
        }
        
        this.agentShoot = 0;
        
        float oldAngle = this.Angle;
        
        // First, calculate inputs
        double[] timeStepData = calculateTrainingInputs();  
        
        // Collect next intermediate node to move to
        TileNode tempTile = this.CurrentPath.get(this.CurrentPathIndex);

        // Calculate pixel distance between positions
        Vector2 currentPosition = new Vector2(this.Pixel_X, this.Pixel_Y);
        Vector2 nextPosition = tempTile.getPixelVector2();

        float distance = currentPosition.dst2(nextPosition);

        // Make sure to move as far as possible
        if( distance < this.MovementSpeed ) {

            if( this.CurrentPathIndex + 1 < this.CurrentPath.getCount() ) {
                this.CurrentPathIndex++;
                tempTile = this.CurrentPath.get(this.CurrentPathIndex);
                nextPosition = tempTile.getPixelVector2();
            } else {
                // We have arrived!
                this.Pixel_X = nextPosition.x;
                this.Pixel_Y = nextPosition.y;
                this.Sprite.setPosition(this.Pixel_X, this.Pixel_Y);

                // Clear Path
                this.CurrentPath = null;
                this.CurrentPathIndex = -1;
                
                // Write Data
                writeTrainingData();

                return;
            }
        }

        
        // Collect angle information from direction
        Vector2 direction = nextPosition.sub(currentPosition);
        float wantedAngle = (this.Angle + 90) - direction.angle();
        //float wantedAngle = (this.Angle - 90) - direction.angle();


        // Rotate in shortest direction
        if( wantedAngle > 180 ) {
            this.deltaAngle = wantedAngle - 360;
        } else if( wantedAngle < -180) {
            this.deltaAngle = 360 + wantedAngle;
        } else {
            this.deltaAngle = wantedAngle;
        }

        // Check that turn is legal (i.e. within Max Turn Per Frame)
        if( Math.abs(this.deltaAngle) > TrainingParams.AgentMaxTurnAngle ) {
            this.deltaAngle = this.deltaAngle > 0 ? -TrainingParams.AgentMaxTurnAngle : TrainingParams.AgentMaxTurnAngle;
        }


        // Update Position
        this.Pixel_X += direction.x * deltaTime;
        this.Pixel_Y += direction.y * deltaTime;
        this.Sprite.setCenter(this.Pixel_X, this.Pixel_Y);

        // Update Rotation
        this.Angle += this.deltaAngle;

        // Keep between 0 and 360 (sanity purposes)
        if( this.Angle > 360)  { this.Angle -= 360; }
        else if( this.Angle < 0 ) { this.Angle += 360; }

        this.Sprite.setRotation( this.Angle );

        // Tack on Training Outputs from observed change
        calculateTrainingOutputs(timeStepData, 52);
        
        // Finally, store snapshot of this time step for training
        this.trainingData.add( timeStepData);
    }

    @Override
    public void drawAgent(SpriteBatch sb) {
        this.Sprite.draw(sb);
    }

    @Override
    public void drawLines(ShapeRenderer sr) {

        // Draws the CurrentPath.
        if( this.CurrentPath != null) {
            for (int i = 1; i < this.CurrentPath.getCount(); i++) {
                sr.rectLine(this.CurrentPath.get(i - 1).getPixelX(), this.CurrentPath.get(i - 1).getPixelY(),
                        this.CurrentPath.get(i).getPixelX(), this.CurrentPath.get(i).getPixelY(), 5);
            }
        }
    }
    
    
    private void calculateTrainingOutputs(double[] timeStepData, int startIndex) {
        float normAngle = this.deltaAngle / TrainingParams.AgentMaxTurnAngle;

        // Compute Rotation Change
        timeStepData[startIndex + 0] = Math.max(-1, Math.min(1, normAngle * 0.10f * TrainingParams.AgentRotationModArray[0]));    // 100% Counter-Clockwise
        timeStepData[startIndex + 1] = Math.max(-1, Math.min(1, normAngle * 0.15f * TrainingParams.AgentRotationModArray[1]));    //  66% Counter-Clockwise
        timeStepData[startIndex + 2] = Math.max(-1, Math.min(1, normAngle * 0.25f * TrainingParams.AgentRotationModArray[2]));    //  33% Counter-Clockwise
        timeStepData[startIndex + 3] = Math.max(-1, Math.min(1, normAngle * 0.90f * TrainingParams.AgentRotationModArray[3]));    //   No Rotation
        timeStepData[startIndex + 4] = Math.max(-1, Math.min(1, normAngle * 0.80f * TrainingParams.AgentRotationModArray[4]));    //  33% Clockwise
        timeStepData[startIndex + 5] = Math.max(-1, Math.min(1, normAngle * 0.70f * TrainingParams.AgentRotationModArray[5]));    //  66% Clockwise
        timeStepData[startIndex + 6] = Math.max(-1, Math.min(1, normAngle * 0.50f * TrainingParams.AgentRotationModArray[6]));    // 100% Clockwise

        // Compute Velocity Change
        timeStepData[startIndex + 14] = 2 * Math.min(1, 1.05 - Math.abs(normAngle)) * TrainingParams.AgentVelocityModArray[0];	// -80% Run Backwards
        timeStepData[startIndex + 15] = 2 * Math.min(1, 1.05 - Math.abs(normAngle)) * TrainingParams.AgentVelocityModArray[1];	// -50% Jog Backwards
        timeStepData[startIndex + 16] = 2 * Math.min(1, 1.05 - Math.abs(normAngle)) * TrainingParams.AgentVelocityModArray[2];	// -10% Creep Backwards
        timeStepData[startIndex + 17] = 2 * Math.min(1, 1.05 - Math.abs(normAngle)) * TrainingParams.AgentVelocityModArray[3];	//   No Movement
        timeStepData[startIndex + 18] = 2 * Math.min(1, 1.05 - Math.abs(normAngle)) * TrainingParams.AgentVelocityModArray[4];	//  20% Creep Forward
        timeStepData[startIndex + 19] = 2 * Math.min(1, 1.05 - Math.abs(normAngle)) * TrainingParams.AgentVelocityModArray[5];	//  60% Jog Forward
        timeStepData[startIndex + 20] = 2 * Math.min(1, 1.05 - Math.abs(normAngle)) * TrainingParams.AgentVelocityModArray[6];	// 100% Run Forward
    	
    	// Compute Gun Movement?  Hard!!!
    	
    	
    	// Shoot?
    	timeStepData[startIndex + 21] = this.agentShoot;
    }
    
    
    private double[] calculateTrainingInputs() {
    	double[] timeStepData = new double[74];
    	Vector2 position = new Vector2(this.Pixel_X, this.Pixel_Y);
        
        //timeStepData[0] =(double)( Math.abs(this.Angle) > 360 ? (this.Angle / 360) : (this.Angle / 360) );

        if( this.Angle > 360 ) {
            timeStepData[0] = (double) ( (this.Angle - 360) / 360 );
        } else if( this.Angle < -360 ) {
            timeStepData[0] = (double) ( (this.Angle + 720) / 360 );
        } else if( this.Angle < 0 ) {
            timeStepData[0] = (double) ( (this.Angle + 360) / 360 );
        } else {
            timeStepData[0] = (double) ( this.Angle / 360 );
        }

        
        // Angle to Bullets within 3 tiles (Max of 2)
 		int count = 1;
 		
 		Iterator<GreenBullet> bulletITR = this.bullets.iterator();
 		
 		while( bulletITR.hasNext() && count < 3 ) {
 			GreenBullet currBullet = bulletITR.next();
 			
 			if( position.dst(currBullet.getBulletVector()) < (TrainingParams.MapTileSize * 3) ) {
 				Vector2 direction = currBullet.getBulletVector().cpy().sub(position).nor();
 				Vector2 unitVec = new Vector2(0,1);
 				
 				timeStepData[count++] = (double) (unitVec.angle(direction) / 360);
 			}
 		}
 		
 		// When no bullets, its just 0.
 		timeStepData[1] = (count > 1) ? timeStepData[1] : 1;
 		timeStepData[2] = (count > 2) ? timeStepData[2] : 1;
 		
 		// Feed in the 7x7 array of values
 		int cellX = (int)(position.x / TrainingParams.MapTileSize);
     	int cellY = (int)(position.y / TrainingParams.MapTileSize);
     	
 		int currentCellIndex = (cellX * TrainingParams.NumCellsY) + cellY;
     	int gridYCount = 0;
     	int gridXCount = 0;    	
     	
 		// Compute from Bottom Left to Top Right - note that it is (Col,Row)
 		for( int gridY = cellY - 3; gridY <= cellY + 3; gridY++ ) {
 						
 			if( gridY >= 0 && gridY < TrainingParams.NumCellsY ) {
 				gridXCount = 0;
 				
 				for( int gridX = cellX - 3; gridX <= cellX + 3; gridX++ ) {
 					
 					if( gridX >= 0 && gridX < TrainingParams.NumCellsX ) {
 						// Compute indexes
 						int tileIndex = (gridX * TrainingParams.NumCellsY) + gridY;
 						int inputIndex = ((6-gridYCount)*7) + gridXCount + 3;  //((6-colCount)*7)+rowCount+1;
 						
 						// Check if cell is traversable
 						if( this.MapNodes.containsKey(tileIndex)) {
 							//TileNode tester = this.traverseNodes.get(tileIndex);
 							
 							// Its traversable, so at least a 1
 							timeStepData[inputIndex] =  (1 / (double)5);
 							
 							// Check if player is currently located there
 							if( tileIndex == currentCellIndex ) {		
 								timeStepData[inputIndex] =  (2 / (double)5);
 							}	
 							
 							// Check if contains a preferred path node
 							if(this.currPathNodeTracker.contains(this.MapNodes.get(tileIndex)) ) { 
 								timeStepData[inputIndex] =  (3 / (double)5);
 							} 
 							
 							// Check if contains a friendly
 							/*Iterator<TrainingAgent> friendlyITR = this.trainees.iterator();
 							
 							while(friendlyITR.hasNext()) {
 								TrainingAgent tempFriendly = friendlyITR.next();
 								
 								if( tempFriendly != this && tileIndex == tempFriendly.getTraverseNodeIndex() ) {
 									timeStepData[inputIndex] =  (4 / (double)5);
 								}
 							}*/
 							
 							// Check if contains an enemy
 							Iterator<TrainingShooter> shooterITR = this.shooters.iterator();
 							
 							while(shooterITR.hasNext()) {
 								TrainingShooter tempShooter = shooterITR.next();
 								
 								if( tileIndex == tempShooter.getTraverseNodeIndex() ) {
 									timeStepData[inputIndex] =  (5 / (double)5);
 									this.agentShoot = 1;
 								}
 							}
 						}
 					}
 					
 					gridXCount++;
 				}
 			} 
 			
 			gridYCount++;
 		}
        
        return timeStepData;
    }

    /**
     * Updates the Path to where you touch.
     * @param goalX
     * @param goalY
     */
    public void setPathToGoal(float goalX, float goalY) {
        // Reset Index Tracker
        this.CurrentPathIndex = 0;

        // Start and Goal node
        TileNode startNode = GraphicsHelpers.findTileNodeByPixelLocation((int)this.Pixel_X, (int)this.Pixel_Y, this.MapNodes);
        TileNode endNode   = GraphicsHelpers.findTileNodeByPixelLocation((int)goalX, (int)goalY, this.MapNodes);

        // The returned path once computed
        this.CurrentPath = new DefaultGraphPath<TileNode>();

        // Compute Path!
        this.PathFinder.searchNodePath(startNode, endNode, new TileHeuristic(), this.CurrentPath);

        this.CurrentPath.reverse();
        
        // Node Tracker
        Iterator<TileNode> itr = CurrentPath.iterator();
        this.currPathNodeTracker = new HashSet<TileNode>(300);
		
		while(itr.hasNext()) {
			TileNode tile = itr.next();
			
			if( this.currPathNodeTracker.contains(tile) ) {
				itr.remove();
			} else {
				this.currPathNodeTracker.add(tile);
			}
		}
    }
}
