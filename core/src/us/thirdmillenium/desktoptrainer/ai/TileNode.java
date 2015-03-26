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
import com.badlogic.gdx.ai.pfa.indexed.IndexedNode;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;


/**
 * TileNode is used by the A* routine.  Tracks Cell and OpenGL x, y coords.
 */
public class TileNode implements IndexedNode<TileNode>, Comparable<TileNode>  {
    private Array<Connection<TileNode>> links;
    private float Pixel_x;
    private float Pixel_y;
    private int Cell_x;
    private int Cell_y;
    private int id;

    /**
     * Constructor for TileNode
     * @param pix_X
     * @param pix_Y
     * @param cell_x
     * @param cell_y
     * @param id
     */
    public TileNode(float pix_X, float pix_Y, int cell_x, int cell_y, int id)
    {
        this.links = new Array<Connection<TileNode>>();

        this.Pixel_x = pix_X;
        this.Pixel_y = pix_Y;
        this.Cell_x = cell_x;
        this.Cell_y = cell_y;

        this.id = id;
    }

    public int getCellX() {
        return this.Cell_x;
    }

    public int getCellY() {
        return this.Cell_y;
    }

    public float getPixelX() {
        return this.Pixel_x;
    }

    public float getPixelY() {
        return this.Pixel_y;
    }

    @Override
    public int getIndex() {
        return this.id;
    }

    @Override
    public Array<Connection<TileNode>> getConnections() {
        return this.links;
    }

    /**
     * Adds TileNode Connection for this Node
     * @param connect
     */
    public void addConnection(TileNode connect) {
        this.links.add(new TileConnect(this, connect));
    }


    public boolean equals(TileNode o) {
        if( this.Cell_x == o.getCellX() && this.Cell_y == o.getCellY() ) {
            return true;
        }

        return false;
    }

    @Override
    public int compareTo(TileNode o) {

        if( this.equals(o)) {
            return 0;
        }

        return 1;
    }
    
    public Vector2 getPixelVector2(){ return new Vector2(this.Pixel_x, this.Pixel_y); }
}
