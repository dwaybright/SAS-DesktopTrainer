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

package us.thirdmillenium.desktoptrainer.ai;

import com.badlogic.gdx.ai.pfa.Connection;


/**
 * TileConnect tracks the connections between two TileNodes
 */
public class TileConnect implements Connection<TileNode> {
    private TileNode Me;
    private TileNode Connect;
    private int Cost;


    /**
     * Constructor with default cost of 1.
     * @param me
     * @param connect
     */
    public TileConnect(TileNode me, TileNode connect) {
        this.Me = me;
        this.Connect = connect;
        this.Cost = 1;
    }

    /**
     * Constructor with user set cost.
     * @param cost
     * @param me
     * @param connect
     */
    public TileConnect(int cost, TileNode me, TileNode connect) {
        this.Me = me;
        this.Connect = connect;
        this.Cost = cost;
    }

    @Override
    public float getCost() {
        return this.Cost;
    }

    @Override
    public TileNode getFromNode() {
        return this.Me;
    }

    @Override
    public TileNode getToNode() {
        return this.Connect;
    }
}
