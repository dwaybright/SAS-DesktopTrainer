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

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import us.thirdmillenium.desktoptrainer.Params;
import us.thirdmillenium.desktoptrainer.graphics.GraphicsHelpers;
import us.thirdmillenium.desktoptrainer.environment.GreenBullet;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;



public class TrainingShooter extends AgentModel {
	// Training Agents
	private Set<AgentModel> trainees;
	private Set<AgentModel> shooters;
	private Set<GreenBullet> bulletTracker;
	
	// Shooter Position
	private Vector2 position;
	
	// Shooter State
	private boolean alive;
	private short hits;
	private Random random;
	private double timeSinceLastShot;
	private boolean canShoot;
	
	// Sprite image
	private Sprite sprite;
	private Texture deadPic;	
	
	
	public TrainingShooter(int pixelX, int pixelY, Set<AgentModel> trainees, Set<AgentModel> shooters, Set<GreenBullet> bulletTracker, Random random) {
		this.shooters = shooters;
		this.trainees = trainees;
		this.bulletTracker = bulletTracker;
	
		this.position = new Vector2(pixelX, pixelY);
		
		
		this.alive = true;
		this.hits = Params.EnemyAgentHitPoints;
		
		this.deadPic = new Texture(Params.DeadAgentPNG);
		this.sprite = new Sprite(new Texture(Params.ShootingAgentLivePNG));
		this.sprite.setCenter(pixelX, pixelY);
		
		this.random = random;
		this.timeSinceLastShot = 0;
		this.canShoot = true;
	}


    @Override
    public void agentHit() {

    }

    @Override
    public long getScore() { return 0; }

    /**
	 * If Agent is alive, will scan surrounding for Trainee, and fire at it if not in cooldown.
	 */
	public void updateAgent(float timeDiff) {
		// Check if Agent can shoot now
		if( !this.canShoot ) {
			this.timeSinceLastShot += timeDiff;
			
			if( this.timeSinceLastShot > Params.AgentFireRate) {
				this.timeSinceLastShot = 0;
				this.canShoot = true;
			} else {
				return;
			}
		}
		
		// If Agent is alive and can shoot, scan for target in range.
		if( this.alive && this.canShoot ) {
			// If a Trainee has wondered within firing range, fire at it.
			Iterator<AgentModel> itr = this.trainees.iterator();
			
			while(itr.hasNext()) {
				TrainingAgent trainee = (TrainingAgent)itr.next();
				Vector2 traineePosition = trainee.getPosition();
				
				// Trainee in Range, Shoot it!
				if( this.position.dst(traineePosition) < (Params.MapTileSize * 3) ) {
					Vector2 direction = traineePosition.cpy().sub(this.position).nor();
					Vector2 unitVec = new Vector2(0,1);
					
					float angle = unitVec.angle(direction);
					
					this.sprite.setRotation(angle);
					
		        	this.bulletTracker.add(new GreenBullet(new Vector2(this.position.x, this.position.y), calcFireAngle(angle)));
		        	this.canShoot = false;
		        	this.timeSinceLastShot = 0;
		        	
		        	return;
				}
			}
		}
	}
	
	/**
	 * Calculates a number +/- Accuracy from the actual angle.
	 * @param angle
	 * @return
	 */
	private float calcFireAngle(float angle) {
		float change = Params.ShootingAgentFireAccuracy - (this.random.nextFloat() * (Params.ShootingAgentFireAccuracy * 2));
		float fireAngle = angle + change;
		
		return fireAngle;
	}
	
	
	/**
	 * Draws Agent to supplied SpriteBatch.
	 * @param sb
	 */
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

    @Override
    public void setPathToGoal(float goalX, float goalY) {

    }


    /**
	 * If hit, decrements hit counter.  If dead, changes photo and removes itself from Active Training Shooters hashset.
	 */
	public void hitByBullet() {
		if((--this.hits) < 1 ) {
			this.alive = false;
			this.sprite = new Sprite(this.deadPic);
			this.shooters.remove(this);
		}
	}
	
	
	public int getTraverseNodeIndex() {
		return GraphicsHelpers.getCurrentCellIndex((int)this.position.x, (int)this.position.y);
	}

    @Override
    public Rectangle getBoundingRectangle() {
        return this.sprite.getBoundingRectangle();
    }
}
