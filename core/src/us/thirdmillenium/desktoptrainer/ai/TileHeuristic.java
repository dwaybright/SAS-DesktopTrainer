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

import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.math.Vector2;


public class TileHeuristic implements Heuristic<TileNode> {

    @Override
    public float estimate(TileNode node, TileNode endNode) {
        // Straight Line
        Vector2 start = new Vector2(node.getCellX(), node.getCellY());
        Vector2 end = new Vector2(endNode.getCellX(), node.getCellY());

        return start.dst(end);
    }
}
