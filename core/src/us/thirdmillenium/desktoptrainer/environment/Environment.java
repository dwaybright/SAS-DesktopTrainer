package us.thirdmillenium.desktoptrainer.environment;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.utils.Array;

import us.thirdmillenium.desktoptrainer.TrainingParams;
import us.thirdmillenium.desktoptrainer.ai.TileNode;

import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by daniel on 3/26/2015.
 */
public abstract class Environment {

    /**
     * This method runs a simulation without a time parameter.
     */
    public abstract void simulate();

    /**
     * This method runs a simulation for one time step.
     *
     * @param deltaTime
     */
    public abstract void simulate(float deltaTime);

    /**
     * This method will return a computed score.
     *
     * @return The score achieved for this Environment.
     */
    public abstract long getScore();


    /**
     * Generates a mapping of all tiles that are traversable by an Agent.
     *
     * Pixel (16,16) actually matched Cell (0,0) now.
     *
     * @return an IndexedGraph for an A* implementation
     */
    public static GraphPath<TileNode> createGraphFromTileMap(ConcurrentHashMap<Integer, TileNode> traverseNodes, TiledMapTileLayer wallLayer) {
        GraphPath<TileNode> graphPath = null;

        try {
            Array<TileNode> myNodes = new Array<TileNode>();
            TileNode temp;

            int tilePixel       = TrainingParams.MapTileSize;
            int halfTilePixel   = TrainingParams.MapTileSize / 2;
            int numCellY        = TrainingParams.NumCellsY;
            int numCellX        = TrainingParams.NumCellsX;

            // Step 1 : Collect all open tiles, create a TileNode for it, and add

            for (int cellX = 0; cellX < numCellX; cellX++) {

                // Calculate the pixel point for this X
                int x = halfTilePixel + (tilePixel * cellX);

                // Need to reverse Y to match (0,0) being bottom left corner in LibGDX
                for (int cellY = 37; cellY >= 0; cellY--) {

                    // Calculate the pixel point for this Y
                    int y = halfTilePixel + (tilePixel * cellY);

                    if (!wallLayer.getCell(cellX, cellY).getTile().getProperties().containsKey("blocked")) {
                        // Generate a unique (sequential start at 0) ID for key, then generate TileNode
                        Integer key = new Integer( (cellX * numCellY) + cellY );
                        temp = new TileNode(x, y, cellX, cellY, key);

                        // Add node to the Array<> and the HashMap
                        myNodes.add(temp);
                        traverseNodes.put(key, temp);
                    }
                }
            }

            // Step 2 : Build TileNode Connections

            TileNode node;
            int idTag;

            for( int cellX = 0; cellX < numCellX; cellX++) {

                for( int cellY = 0; cellY < numCellY; cellY++) {

                    // This cell
                    idTag = (cellX * numCellY) + cellY;
                    node = findIndex(idTag, traverseNodes);

                    /*
                     *      Check eight surrounding cells
                     */

                    if( node != null ) {

                        // Top Left
                        int topLeftX = cellX - 1;
                        int topLeftY = cellY + 1;
                        int topLeftID = (topLeftX * numCellY) + topLeftY;

                        if (topLeftX >= 0 && topLeftY < numCellY) {
                            temp = findIndex(topLeftID, traverseNodes);

                            // If it exists, add to connection list for the TileNode being considered.
                            if (temp != null) {
                                node.addConnection(temp);
                            }
                        }

                        // Left
                        int leftX = cellX - 1;
                        int leftY = cellY;
                        int leftID = (leftX * numCellY) + leftY;

                        if (leftX >= 0) {
                            temp = findIndex(leftID, traverseNodes);

                            // If it exists, add to connection list for the TileNode being considered.
                            if (temp != null) {
                                node.addConnection(temp);
                            }
                        }

                        // Bottom Left
                        int botLeftX = cellX - 1;
                        int botLeftY = cellY - 1;
                        int botLeftID = (botLeftX * numCellY) + botLeftY;

                        if (botLeftX >= 0 && botLeftY >= 0) {
                            temp = findIndex(botLeftID, traverseNodes);

                            // If it exists, add to connection list for the TileNode being considered.
                            if (temp != null) {
                                node.addConnection(temp);
                            }
                        }


                        // Top
                        int topX = cellX;
                        int topY = cellY + 1;
                        int topID = (topX * numCellY) + topY;

                        if (topY < numCellY) {
                            temp = findIndex(topID, traverseNodes);

                            // If it exists, add to connection list for the TileNode being considered.
                            if (temp != null) {
                                node.addConnection(temp);
                            }
                        }

                        // Bottom
                        int botX = cellX;
                        int botY = cellY - 1;
                        int botID = (botX * numCellY) + botY;

                        if (botY >= 0) {
                            temp = findIndex(botID, traverseNodes);

                            // If it exists, add to connection list for the TileNode being considered.
                            if (temp != null) {
                                node.addConnection(temp);
                            }
                        }

                        // Top Right
                        int topRightX = cellX + 1;
                        int topRightY = cellY + 1;
                        int topRightID = (topRightX * numCellY) + topRightY;

                        if (topRightX < numCellX && topRightY < numCellY) {
                            temp = findIndex(topRightID, traverseNodes);

                            // If it exists, add to connection list for the TileNode being considered.
                            if (temp != null) {
                                node.addConnection(temp);
                            }
                        }

                        // Right
                        int rightX = cellX + 1;
                        int rightY = cellY;
                        int rightID = (rightX * numCellY) + rightY;

                        if (rightX < numCellX) {
                            temp = findIndex(rightID, traverseNodes);

                            // If it exists, add to connection list for the TileNode being considered.
                            if (temp != null) {
                                node.addConnection(temp);
                            }
                        }

                        // Bottom Right
                        int botRightX = cellX + 1;
                        int botRightY = cellY - 1;
                        int botRightID = (botRightX * numCellY) + botRightY;

                        if (botRightX < numCellX && botRightY >= 0) {
                            temp = findIndex(botRightID, traverseNodes);

                            // If it exists, add to connection list for the TileNode being considered.
                            if (temp != null) {
                                node.addConnection(temp);
                            }
                        }
                    }

                }
            }

            graphPath = new DefaultGraphPath<TileNode>(myNodes);

        } catch(Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
            return graphPath;
        }
    }


    /**
     * A helper function that searches for the TileNode index provided in the traverse nodes hash map.
     * @param index
     */
    public static TileNode findIndex(Integer index, ConcurrentHashMap<Integer, TileNode> traverseNodes) {
        TileNode temp = null;

        if(traverseNodes.containsKey(index)) {
            temp = traverseNodes.get(index);
        }

        return temp;
    }
}
