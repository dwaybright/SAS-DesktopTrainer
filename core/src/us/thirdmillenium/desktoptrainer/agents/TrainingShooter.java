package us.thirdmillenium.desktoptrainer.agents;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import us.thirdmillenium.desktoptrainer.environment.GraphicsHelpers;
import us.thirdmillenium.desktoptrainer.environment.GreenBullet;
import us.thirdmillenium.desktoptrainer.TrainingParams;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;



public class TrainingShooter {
	// Training Agents
	private Set<TrainingAgent> trainees;
	private Set<TrainingShooter> shooters;
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
	
	
	public TrainingShooter(int pixelX, int pixelY, Set<TrainingAgent> trainees, Set<TrainingShooter> shooters, Set<GreenBullet> bulletTracker, Random random) {
		this.shooters = shooters;
		this.trainees = trainees;
		this.bulletTracker = bulletTracker;
	
		this.position = new Vector2(pixelX, pixelY);
		
		
		this.alive = true;
		this.hits = TrainingParams.ShootingAgentHitPoints;
		
		this.deadPic = new Texture(TrainingParams.DeadAgentPNG);
		this.sprite = new Sprite(new Texture(TrainingParams.ShootingAgentLivePNG));
		this.sprite.setCenter(pixelX, pixelY);
		
		this.random = random;
		this.timeSinceLastShot = 0;
		this.canShoot = true;
	}
	
	
	/**
	 * If Agent is alive, will scan surrounding for Trainee, and fire at it if not in cooldown.
	 */
	public void updateAgent(float timeDiff) {
		// Check if Agent can shoot now
		if( !this.canShoot ) {
			this.timeSinceLastShot += timeDiff;
			
			if( this.timeSinceLastShot > TrainingParams.AgentFireRate) {
				this.timeSinceLastShot = 0;
				this.canShoot = true;
			} else {
				return;
			}
		}
		
		// If Agent is alive and can shoot, scan for target in range.
		if( this.alive && this.canShoot ) {
			// If a Trainee has wondered within firing range, fire at it.
			Iterator<TrainingAgent> itr = this.trainees.iterator();
			
			while(itr.hasNext()) {
				TrainingAgent trainee = itr.next();
				Vector2 traineePosition = trainee.getPosition();
				
				// Trainee in Range, Shoot it!
				if( this.position.dst(traineePosition) < (TrainingParams.MapTileSize * 3) ) {
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
		float change = TrainingParams.ShootingAgentFireAccuracy - (this.random.nextFloat() * (TrainingParams.ShootingAgentFireAccuracy * 2));
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
}
