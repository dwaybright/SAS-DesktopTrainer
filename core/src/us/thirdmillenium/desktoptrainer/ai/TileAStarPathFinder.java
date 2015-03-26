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
import com.badlogic.gdx.ai.pfa.Graph;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.ai.pfa.PathFinder;
import com.badlogic.gdx.ai.pfa.PathFinderRequest;
import com.badlogic.gdx.utils.Array;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;


public class TileAStarPathFinder implements PathFinder<TileNode> {

    // A wrapper for the TileNode to be used
    private class AStarTileNode implements Comparable<AStarTileNode> {
        private AStarTileNode cameFrom;
        private TileNode node;
        private float DistanceToGoal;
        private int DistanceFromStart;
        private float PathCost;
        private boolean FirstNode;


        public AStarTileNode(TileNode me, int distToMe, float distToGoal) {
            this.node = me;

            this.DistanceFromStart = distToMe;
            this.DistanceToGoal = distToGoal;

            this.PathCost = this.DistanceFromStart + this.DistanceToGoal;
        }

        public float getPathCost() { return this.PathCost; }

        public int getDistanceFromStart() { return this.DistanceFromStart; }

        public void setDistanceFromStart(int dist) { this.DistanceFromStart = dist; }

        public void setDistanceToGoal(int dist) { this.DistanceToGoal = dist; }

        public TileNode getTileNode() { return this.node; }

        public void setCameFrom(AStarTileNode cFrom) { this.cameFrom = cFrom; }

        public AStarTileNode getCameFrom() { return this.cameFrom; }

        public void setFirstNode() { this.FirstNode = true; }

        public boolean isFirstNode() { return this.FirstNode; }

        public boolean equals(AStarTileNode o) {
            if( o.getTileNode().equals(this.node) ) {
                return true;
            }

            return false;
        }

        @Override
        public int compareTo(AStarTileNode o) {
            if( node.equals(o.getTileNode())) {
                return 0;
            }
            else if( this.PathCost < o.getPathCost() ) {
                return -1;
            }
            else {
                return 1;
            }
        }
    }

    // A way to compare different AStarNodes
    private class AStarTileNodeComparator implements Comparator<AStarTileNode> {

        @Override
        public int compare(AStarTileNode o1, AStarTileNode o2) {
            float dist1 = o1.getPathCost();
            float dist2 = o2.getPathCost();

            if( dist1 > dist2 ) {
                return 1;
            } else if (dist1 < dist2) {
                return -1;
            } else {
                return 0;
            }
        }

    }


    Graph<TileNode> myGraph;


    public TileAStarPathFinder(Graph<TileNode> graph) {
        this.myGraph = graph;
    }
    
    public TileAStarPathFinder() {
    	this.myGraph = null;
    }


    @Override
    public boolean searchConnectionPath(TileNode startNode, TileNode endNode, Heuristic<TileNode> heuristic, GraphPath<Connection<TileNode>> outPath) {
        throw new NotImplementedException();
    }

    @Override
    public boolean searchNodePath(TileNode startNode, TileNode endNode, Heuristic<TileNode> heuristic, GraphPath<TileNode> outPath) {
        PriorityQueue<AStarTileNode> openList = new PriorityQueue<AStarTileNode>(200, new AStarTileNodeComparator());
        HashSet<TileNode> closeList= new HashSet<TileNode>(250);
        HashSet<TileNode> openListTracker = new HashSet<TileNode>(250);
        AStarTileNode current, end, temp;
        TileNode neighbor;


        // Setup A* with StartNode
        current = new AStarTileNode(startNode, 0, heuristic.estimate(startNode, endNode));
        end = new AStarTileNode(endNode, 0, 0);

        openList.add(current);


        // Begin Search
        while (openList.size() > 0) {
            // Collect closest item to the Goal
            current = openList.peek();

            // Path has been found
            if( current.equals(end) ) {
                return returnNodePath(current, outPath);
            }

            // Remove current from openList, add to closeList
            current = openList.poll();
            closeList.add(current.getTileNode());

            // Loop through neighbors, adding to openList
            Array<Connection<TileNode>> adjacentCells = current.getTileNode().getConnections();

            for( int i = 0; i < adjacentCells.size; i++) {
                neighbor = adjacentCells.get(i).getToNode();

                if( !closeList.contains(neighbor) && !openListTracker.contains(neighbor) ) {
                    temp = new AStarTileNode(neighbor, current.getDistanceFromStart() + 1, heuristic.estimate(neighbor, endNode));
                    temp.setCameFrom(current);
                    openList.add(temp);
                    openListTracker.add(neighbor);
                }
            }
        }

        return false;
    }


    private boolean returnNodePath(AStarTileNode lastNode, GraphPath<TileNode> outPath) {
        AStarTileNode walkBack = lastNode;

        while( walkBack != null && !walkBack.isFirstNode() ) {
            outPath.add(walkBack.getTileNode());
            walkBack = walkBack.getCameFrom();
        }

        //outPath.reverse();

        return true;
    }
    
    private boolean returnNodePath2(AStarTileNode lastNode, Array<TileNode> outPath) {
        AStarTileNode walkBack = lastNode;

        while( walkBack != null && !walkBack.isFirstNode() ) {
            outPath.add(walkBack.getTileNode());
            walkBack = walkBack.getCameFrom();
        }

        outPath.reverse();

        return true;
    }
    
    
    public boolean searchNodePath2(TileNode startNode, TileNode endNode, Heuristic<TileNode> heuristic, Array<TileNode> outPath) {
        PriorityQueue<AStarTileNode> openList = new PriorityQueue<AStarTileNode>(200, new AStarTileNodeComparator());
        HashSet<TileNode> closeList= new HashSet<TileNode>(250);
        HashSet<TileNode> openListTracker = new HashSet<TileNode>(250);
        AStarTileNode current, end, temp;
        TileNode neighbor;


        // Setup A* with StartNode
        current = new AStarTileNode(startNode, 0, heuristic.estimate(startNode, endNode));
        end = new AStarTileNode(endNode, 0, 0);

        openList.add(current);


        // Begin Search
        while (openList.size() > 0) {
            // Collect closest item to the Goal
            current = openList.peek();

            // Path has been found
            if( current.equals(end) ) {
            	return returnNodePath2(current, outPath);
            }

            // Remove current from openList, add to closeList
            current = openList.poll();
            closeList.add(current.getTileNode());

            // Loop through neighbors, adding to openList
            Array<Connection<TileNode>> adjacentCells = current.getTileNode().getConnections();

            for( int i = 0; i < adjacentCells.size; i++) {
                neighbor = adjacentCells.get(i).getToNode();

                if( !closeList.contains(neighbor) && !openListTracker.contains(neighbor) ) {
                    temp = new AStarTileNode(neighbor, current.getDistanceFromStart() + 1, heuristic.estimate(neighbor, endNode));
                    temp.setCameFrom(current);
                    openList.add(temp);
                    openListTracker.add(neighbor);
                }
            }
        }

        return false;
    }

    @Override
    public boolean search(PathFinderRequest<TileNode> request, long timeToRun) {
        throw new NotImplementedException();
    }


}
