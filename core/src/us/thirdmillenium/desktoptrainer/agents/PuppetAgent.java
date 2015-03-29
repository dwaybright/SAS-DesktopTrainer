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
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import us.thirdmillenium.desktoptrainer.Params;
import us.thirdmillenium.desktoptrainer.ai.tile.TileAStarPathFinder;
import us.thirdmillenium.desktoptrainer.ai.tile.TileHeuristic;
import us.thirdmillenium.desktoptrainer.ai.tile.TileNode;
import us.thirdmillenium.desktoptrainer.brains.Brain;
import us.thirdmillenium.desktoptrainer.brains.PuppetBrain;
import us.thirdmillenium.desktoptrainer.graphics.GraphicsHelpers;
import us.thirdmillenium.desktoptrainer.environment.GreenBullet;


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
    private Vector2 position;
    private int Cell_X;
    private int Cell_Y;
    private float Angle;
    private float deltaAngle;

    // Agent Physical Attributes
    private int Health;
    private float Eyesight;
    private float Hearing;

    // Agent Equipment
    //private WeaponModel MyWeapon;
    //private ArmorModel MyArmor;

    // Agent Mental Model
    private Brain Control;
    private int agentShoot;
    
    
    // Output Path
    private String csvOutputPath = Params.PathToCSV;
    private File csvOutputFile;
    private ArrayList<double[]> trainingData;


    public PuppetAgent(TiledMap myTiledMap, ConcurrentHashMap<Integer, TileNode> mapNodes,
                       TileAStarPathFinder pathFinder, int pixelX, int pixelY, Set<GreenBullet> bullets, Set<TrainingShooter> shooters)
    {
        // Setup game world parameters
        this.MyTiledMap = myTiledMap;
        this.TileSize = Params.MapTileSize;
        this.MapNodes = mapNodes;
        this.PathFinder = pathFinder;
        this.bullets = bullets;
        this.shooters = shooters;

        // Setup Graphics
        this.Alive = new Texture(Params.TrainingAgentLivePNG);
        this.Dead = new Texture(Params.DeadAgentPNG);
        this.Sprite = new Sprite(Alive);

        // Setup Start Location
        this.CurrentPathIndex = -1;
        this.position = new Vector2(pixelX, pixelY);
        this.Angle = 270;

        // Set Basic Values
        this.Health = 50;
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
    public void updateAgent(float deltaTime) {
        // Bounds Check - Do nothing
        if( this.CurrentPath == null) {
            return;
        } else if (this.CurrentPath.getCount() < 1 || this.CurrentPathIndex < 0) {
        	writeTrainingData();
        	this.CurrentPath = null;
            return;
        }
        
        this.agentShoot = 0;
        
        // First, calculate inputs
        double[] timeStepData = calculateTrainingInputs();  
        
        // Collect next intermediate node to move to
        TileNode tempTile = this.CurrentPath.get(this.CurrentPathIndex);

        // Calculate pixel distance between positions
        Vector2 currentPosition = this.position.cpy();
        Vector2 nextPosition = tempTile.getPixelVector2();

        float distance = currentPosition.dst2(nextPosition);

        // Make sure to move as far as possible
        if( distance < (Params.AgentMaxMovement * Params.AgentMaxMovement) ) {

            if( this.CurrentPathIndex + 1 < this.CurrentPath.getCount() ) {
                this.CurrentPathIndex++;
                tempTile = this.CurrentPath.get(this.CurrentPathIndex);
                nextPosition = tempTile.getPixelVector2();
            } else {
                // We have arrived!
                this.position.set(nextPosition.x, nextPosition.y);
                this.Sprite.setCenter(nextPosition.x, nextPosition.y);

                // Clear Path
                this.CurrentPath = null;
                this.CurrentPathIndex = -1;
                
                // Write Data
                writeTrainingData();

                return;
            }
        }

        
        // Collect angle information from direction
        Vector2 direction = nextPosition.cpy().sub(currentPosition).nor();

        float desiredAngle = direction.angle() - 90;                                // The angle the sprite should be pointing (90 deg shift)
        desiredAngle = (desiredAngle < 0) ? 360 + desiredAngle : desiredAngle;      // Bring back to 0 - 360

        float wantedAngleChange = desiredAngle - this.Angle;                        // How much angle needs to be added between current angle and desired angle.

        // Rotate in shortest direction
        if( wantedAngleChange >= 180 ) {
            wantedAngleChange -= 360;
        } else if( wantedAngleChange <= -180) {
            wantedAngleChange += 360;
        }

        this.deltaAngle = wantedAngleChange;

        // Check that turn is legal (i.e. within Max Turn Per Frame)
        if( Math.abs(this.deltaAngle) > Params.AgentMaxTurnAngle ) {
            this.deltaAngle = this.deltaAngle > 0 ? Params.AgentMaxTurnAngle : -Params.AgentMaxTurnAngle;
        }


        // Update Position
        this.position.x += direction.x * Params.AgentMaxMovement;
        this.position.y += direction.y * Params.AgentMaxMovement;
        this.Sprite.setCenter(this.position.x, this.position.y);

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
    public void drawPath(ShapeRenderer sr) {

        // Draws the CurrentPath.
        if( this.CurrentPath != null) {
            for (int i = 1; i < this.CurrentPath.getCount(); i++) {
                sr.rectLine(this.CurrentPath.get(i - 1).getPixelX(), this.CurrentPath.get(i - 1).getPixelY(),
                        this.CurrentPath.get(i).getPixelX(), this.CurrentPath.get(i).getPixelY(), 5);
            }
        }
    }

    @Override
    public void drawVision(ShapeRenderer sr) {

    }

    @Override
    public Vector2 getPosition() {
        return null;
    }


    private void calculateTrainingOutputs(double[] timeStepData, int startIndex) {
        float normAngle = this.deltaAngle / Params.AgentMaxTurnAngle;

        // Compute Rotation Change
        timeStepData[startIndex + 0] = Math.max(-1f, Math.min(1f, normAngle * 0.10f * Params.AgentRotationModArray[0]));    // 100% Counter-Clockwise
        timeStepData[startIndex + 1] = Math.max(-1f, Math.min(1f, normAngle * 0.15f * Params.AgentRotationModArray[1]));    //  66% Counter-Clockwise
        timeStepData[startIndex + 2] = Math.max(-1f, Math.min(1f, normAngle * 0.25f * Params.AgentRotationModArray[2]));    //  33% Counter-Clockwise
        timeStepData[startIndex + 3] = Math.max(-1f, Math.min(1f, normAngle * 0.90f * Params.AgentRotationModArray[3]));    //   No Rotation
        timeStepData[startIndex + 4] = Math.max(-1f, Math.min(1f, normAngle * 0.80f * Params.AgentRotationModArray[4]));    //  33% Clockwise
        timeStepData[startIndex + 5] = Math.max(-1f, Math.min(1f, normAngle * 0.70f * Params.AgentRotationModArray[5]));    //  66% Clockwise
        timeStepData[startIndex + 6] = Math.max(-1f, Math.min(1f, normAngle * 0.50f * Params.AgentRotationModArray[6]));    // 100% Clockwise

        // Compute Velocity Change
        timeStepData[startIndex + 14] = Math.max(-1f, Math.min(1f, (0.9f - Math.abs(normAngle)) * Params.AgentVelocityModArray[0] * 0.40f));	// -80% Run Backwards
        timeStepData[startIndex + 15] = Math.max(-1f, Math.min(1f, (0.9f - Math.abs(normAngle)) * Params.AgentVelocityModArray[1] * 0.50f));	// -50% Jog Backwards
        timeStepData[startIndex + 16] = Math.max(-1f, Math.min(1f, (0.9f - Math.abs(normAngle)) * Params.AgentVelocityModArray[2] * 0.60f));	// -10% Creep Backwards
        timeStepData[startIndex + 17] = Math.max(-1f, Math.min(1f, (0.9f - Math.abs(normAngle)) * Params.AgentVelocityModArray[3] * 0.85f));	//   No Movement
        timeStepData[startIndex + 18] = Math.max(-1f, Math.min(1f, (0.9f - Math.abs(normAngle)) * Params.AgentVelocityModArray[4] * 0.95f));	//  20% Creep Forward
        timeStepData[startIndex + 19] = Math.max(-1f, Math.min(1f, (0.9f - Math.abs(normAngle)) * Params.AgentVelocityModArray[5] * 1.00f));	//  60% Jog Forward
        timeStepData[startIndex + 20] = Math.max(-1f, Math.min(1f, (0.9f - Math.abs(normAngle)) * Params.AgentVelocityModArray[6] * 1.00f));	// 100% Run Forward

//        timeStepData[startIndex + 14] = 2 * Math.min(1, 1.05 - Math.abs(normAngle)) * TrainingParams.AgentVelocityModArray[0];	// -80% Run Backwards
//        timeStepData[startIndex + 15] = 2 * Math.min(1, 1.05 - Math.abs(normAngle)) * TrainingParams.AgentVelocityModArray[1];	// -50% Jog Backwards
//        timeStepData[startIndex + 16] = 2 * Math.min(1, 1.05 - Math.abs(normAngle)) * TrainingParams.AgentVelocityModArray[2];	// -10% Creep Backwards
//        timeStepData[startIndex + 17] = 2 * Math.min(1, 1.05 - Math.abs(normAngle)) * TrainingParams.AgentVelocityModArray[3];	//   No Movement
//        timeStepData[startIndex + 18] = 2 * Math.min(1, 1.05 - Math.abs(normAngle)) * TrainingParams.AgentVelocityModArray[4];	//  20% Creep Forward
//        timeStepData[startIndex + 19] = 2 * Math.min(1, 1.05 - Math.abs(normAngle)) * TrainingParams.AgentVelocityModArray[5];	//  60% Jog Forward
//        timeStepData[startIndex + 20] = 2 * Math.min(1, 1.05 - Math.abs(normAngle)) * TrainingParams.AgentVelocityModArray[6];	// 100% Run Forward
    	
    	// Compute Gun Movement?  Hard!!!
    	
    	
    	// Shoot?
    	timeStepData[startIndex + 21] = this.agentShoot;
    }
    
    
    private double[] calculateTrainingInputs() {
    	double[] timeStepData = new double[74];
        
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
 			
 			if( position.dst(currBullet.getBulletVector()) < (Params.MapTileSize * 3) ) {
 				Vector2 direction = currBullet.getBulletVector().cpy().sub(position).nor();
 				Vector2 unitVec = new Vector2(0,1);
 				
 				timeStepData[count++] = (double) (unitVec.angle(direction) / 360);
 			}
 		}
 		
 		// When no bullets, its just 0.
 		timeStepData[1] = (count > 1) ? timeStepData[1] : 1;
 		timeStepData[2] = (count > 2) ? timeStepData[2] : 1;
 		
 		// Feed in the 7x7 array of values
 		int cellX = (int)(this.position.x / Params.MapTileSize);
     	int cellY = (int)(this.position.y / Params.MapTileSize);
     	
 		int currentCellIndex = (cellX * Params.NumCellsY) + cellY;
     	int gridYCount = 0;
     	int gridXCount = 0;    	
     	
 		// Compute from Bottom Left to Top Right - note that it is (Col,Row)
 		for( int gridY = cellY - 3; gridY <= cellY + 3; gridY++ ) {
 						
 			if( gridY >= 0 && gridY < Params.NumCellsY ) {
 				gridXCount = 0;
 				
 				for( int gridX = cellX - 3; gridX <= cellX + 3; gridX++ ) {
 					
 					if( gridX >= 0 && gridX < Params.NumCellsX ) {
 						// Compute indexes
 						int tileIndex = (gridX * Params.NumCellsY) + gridY;
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
        this.CurrentPathIndex = 1;

        // Start and Goal node
        TileNode startNode = GraphicsHelpers.findTileNodeByPixelLocation((int)this.position.x, (int)this.position.y, this.MapNodes);
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
