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
