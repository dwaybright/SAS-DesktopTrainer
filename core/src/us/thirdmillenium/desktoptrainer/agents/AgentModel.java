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
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import us.thirdmillenium.desktoptrainer.ai.tile.TileNode;

/**
 * The AgentModel class is extended by all actual Agents to be made.
 */
public abstract class AgentModel {


    /**
     * This method is called if your agent bounces into something in the world.
     */
    public abstract void agentHit();


    /**
     * This method adjusts Agent rotation and velocity in time.
     *
     * @param deltaTime
     */
    public abstract void updateAgent(float deltaTime);


    /**
     * Set the preferred path for an Agent.
     *
     * @param goalX
     * @param goalY
     */
    public abstract void setPathToGoal(float goalX, float goalY);


    /**
     * Collect what Tile Cell the Agent is on.
     *
     * @return
     */
    public abstract int getTraverseNodeIndex();


    /**
     * Return the score this Agent has achieved.
     *
     * @return
     */
    public abstract long getScore();


    /**
     * This method draws the Agent sprite.
     *
     * @param sb
     */
    public abstract void drawAgent(SpriteBatch sb);


    /**
     * This method draws this Agent's current preferred path.
     *
     * @param sr
     */
    public abstract void drawPath(ShapeRenderer sr);


    /**
     * This method draws a translucent box around what the Agent is viewing.
     *
     * @param sr
     */
    public abstract void drawVision(ShapeRenderer sr);


    /**
     * Returns a Vector2 for the position Agent is centered on.
     *
     * @return
     */
    public abstract Vector2 getPosition();


    /**
     * Returns the Bounding Rectangle for this Agent.
     *
     * @return
     */
    public abstract Rectangle getBoundingRectangle();

}
