package us.thirdmillenium.desktoptrainer.environment;

import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import us.thirdmillenium.desktoptrainer.ai.tile.TileAStarPathFinder;
import us.thirdmillenium.desktoptrainer.ai.tile.TileHeuristic;
import us.thirdmillenium.desktoptrainer.ai.tile.TileNode;
import us.thirdmillenium.desktoptrainer.TrainingParams;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;


public abstract class GraphicsHelpers {
	
	/**
	 * Convert a rectangle into a Polygon.
	 * 
	 * @param rect
	 * @return
	 */
	public static Polygon convertRectangleToPolygon(Rectangle rect) {
		
		float[] vertices = { 
			rect.x, rect.y, 
			rect.x + rect.width, rect.y,
			rect.x + rect.width, rect.y + rect.height,
			rect.x, rect.y + rect.height
    	};
		
		return new Polygon(vertices);
	}
	
	
	
	/**
	 * Builds the Preferred Path for the 5 testing maps.
	 * 
	 * @param testID
	 * @param prefPathNodeTracker
	 * @param traverseNodes
	 * @return
	 */
	public static GraphPath<TileNode> getPrefPathTest(int testID, HashSet<TileNode> prefPathNodeTracker, ConcurrentHashMap<Integer, TileNode> traverseNodes) {
		TileAStarPathFinder pathFinder = new TileAStarPathFinder();
		TileHeuristic heuristic = new TileHeuristic();
		
		Array<TileNode> prefPath = new Array<TileNode>(250);
		Array<TileNode> partialPath;
		
		TileNode start, end;
		
		switch(testID) {
		
		case 1:
			// Start Bottom Left
			int[] x1 = {   16,  336,  16, 50, 752, 752, 338, 174, 626 };
			int[] y1 = { 1198, 1134, 910, 48,  46, 272, 270, 554, 554 };
			
			for(int i = 0; i < x1.length-1; i++ ) {
				start = GraphicsHelpers.findTileNodeByScreenTouch(x1[i], y1[i], traverseNodes);
				end   = GraphicsHelpers.findTileNodeByScreenTouch(x1[i+1], y1[i+1], traverseNodes);
				partialPath = new Array<TileNode>(100);
				pathFinder.searchNodePath2(start, end, heuristic, partialPath);
				prefPath.addAll(partialPath);
			}
			
			break;
			
		case 2:
			// Start Bottom Right
			int[] x2 = {  754,  690,  48,  48, 368 };
			int[] y2 = { 1170,   76,  82, 912, 526 };
			
			for(int i = 0; i < x2.length-1; i++ ) {
				start = GraphicsHelpers.findTileNodeByScreenTouch(x2[i], y2[i], traverseNodes);
				end   = GraphicsHelpers.findTileNodeByScreenTouch(x2[i+1], y2[i+1], traverseNodes);
				partialPath = new Array<TileNode>(100);
				pathFinder.searchNodePath2(start, end, heuristic, partialPath);
				prefPath.addAll(partialPath);
			}
			
			break;
			
		case 3:
			// Start Top Right
			int[] x3 = { 784,  48,  52, 755,  755,  430, 430,  50,  50,   300  };
			int[] y3 = {  20, 140, 490, 490, 1165, 1165, 875, 875, 1160, 1164  };
			
			for(int i = 0; i < x3.length-1; i++ ) {
				start = GraphicsHelpers.findTileNodeByScreenTouch(x3[i], y3[i], traverseNodes);
				end   = GraphicsHelpers.findTileNodeByScreenTouch(x3[i+1], y3[i+1], traverseNodes);
				partialPath = new Array<TileNode>(100);
				pathFinder.searchNodePath2(start, end, heuristic, partialPath);
				prefPath.addAll(partialPath);
			}
			
			break;
			
		case 4:
			// Start Top Left
			int[] x4 = {  16,   80,  750, 720, 210, 400  };
			int[] y4 = {  16, 1165, 1165,  48,  50, 850  };
			
			for(int i = 0; i < x4.length-1; i++ ) {
				start = GraphicsHelpers.findTileNodeByScreenTouch(x4[i], y4[i], traverseNodes);
				end   = GraphicsHelpers.findTileNodeByScreenTouch(x4[i+1], y4[i+1], traverseNodes);
				partialPath = new Array<TileNode>(100);
				pathFinder.searchNodePath2(start, end, heuristic, partialPath);
				prefPath.addAll(partialPath);
			}
			
			break;
			
		case 5:
			// Start Bottom Left
			int[] x5 = {    16,  50, 530,  80 };
			int[] y5 = {  1200, 400, 630, 270  };
			
			for(int i = 0; i < x5.length-1; i++ ) {
				start = GraphicsHelpers.findTileNodeByScreenTouch(x5[i], y5[i], traverseNodes);
				end   = GraphicsHelpers.findTileNodeByScreenTouch(x5[i+1], y5[i+1], traverseNodes);
				partialPath = new Array<TileNode>(100);
				pathFinder.searchNodePath2(start, end, heuristic, partialPath);
				prefPath.addAll(partialPath);
			}
			
			break;
			
			
		default:
			break;
		}
		
		
		// Add all nodes to HashSet tracker for fast look-up later, deduping as we go		
		Iterator<TileNode> itr = prefPath.iterator();
		
		while(itr.hasNext()) {
			TileNode tile = itr.next();
			
			if( prefPathNodeTracker.contains(tile) ) {
				itr.remove();
			} else {
				prefPathNodeTracker.add(tile);
			}
		}
		
		// Return our new Graph Path
		return new DefaultGraphPath<TileNode>(prefPath);
	}
	
	
	/**
     * A  helper function where you give location in cell (x,y), and <br>
     * the method returns the TileNode for that cell.
     * 
     * @param cellX
     * @param cellY
     * @param numCellY
     * @return
     */
    public static TileNode findTileNodeByCellIndex(int cellX, int cellY, ConcurrentHashMap<Integer, TileNode> traverseNodes) {
    	TileNode temp = null;
    	
    	int index = ((cellX) * TrainingParams.NumCellsY) + (38-cellY);
    	
    	if(traverseNodes.containsKey(index)){
    		temp = traverseNodes.get(index);
    	}
    	
    	return temp;
    }
    
    
	/**
     * A helper function where you give location in pixels (x,y), and <br>
     * the method returns the TileNode for the cell containing that point.
     * 
     * @param cellX
     * @param cellY
     * @param numCellY
     * @return
     */
    public static TileNode findTileNodeByPixelLocation(int pixelX, int pixelY, ConcurrentHashMap<Integer, TileNode> traverseNodes) {
    	TileNode temp = null;
    	
    	int index = getCurrentCellIndex(pixelX, pixelY);
    	
    	if(traverseNodes.containsKey(index)){
    		temp = traverseNodes.get(index);
    	}
    	
    	return temp;
    }
    
    
	/**
     * A helper function where you give location in pixels (x,y), and <br>
     * the method returns the TileNode for the cell containing that point.
     * 
     * @param cellX
     * @param cellY
     * @param numCellY
     * @return
     */
    public static TileNode findTileNodeByScreenTouch(int pixelX, int pixelY, ConcurrentHashMap<Integer, TileNode> traverseNodes) {
    	TileNode temp = null;
    	
    	int index = getCurrentCellIndex(pixelX, (1216 - pixelY));
    	
    	if(traverseNodes.containsKey(index)){
    		temp = traverseNodes.get(index);
    	}
    	
    	return temp;
    }
    
    
    /**
     * Get the HashMap index for the TileNode currently on.
     * 
     * @param pixelX
     * @param pixelY
     * @return
     */
    public static int getCurrentCellIndex(int pixelX, int pixelY) {
    	int cellX = (int)(pixelX / TrainingParams.MapTileSize);
    	int cellY = (int)(pixelY / TrainingParams.MapTileSize);
    	
    	int index = (cellX * TrainingParams.NumCellsY) + (cellY);
    	
    	return index;
    }
}
