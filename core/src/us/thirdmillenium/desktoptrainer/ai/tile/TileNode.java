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


package us.thirdmillenium.desktoptrainer.ai.tile;

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
