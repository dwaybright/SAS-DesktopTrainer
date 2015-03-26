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
