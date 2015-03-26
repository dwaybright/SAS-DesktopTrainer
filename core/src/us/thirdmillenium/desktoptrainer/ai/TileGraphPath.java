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

import com.badlogic.gdx.ai.pfa.GraphPath;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Contains a Tile path
 */
public class TileGraphPath implements GraphPath<TileConnect> {
    private ArrayList<TileConnect> myNodes;


    public TileGraphPath() { }


    @Override
    public int getCount() {
        return this.myNodes.size();
    }

    @Override
    public TileConnect get(int index) {
        return this.myNodes.get(index);
    }

    @Override
    public void add(TileConnect node) {
        this.myNodes.add(node);
    }

    @Override
    public void clear() {
        this.myNodes.clear();
    }

    @Override
    public void reverse() {
        ArrayList<TileConnect> temp = new ArrayList<TileConnect>(this.myNodes.size());

        for( int i = this.myNodes.size()-1; i > -1; i-- )
        {
            temp.add(this.myNodes.get(i));
        }

        this.myNodes = temp;
    }

    @Override
    public Iterator<TileConnect> iterator() {
        return this.myNodes.iterator();
    }
}
