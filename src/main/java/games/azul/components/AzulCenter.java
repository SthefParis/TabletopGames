package games.azul.components;

import core.CoreConstants;
import core.components.Component;
import games.azul.AzulGameState;
import games.azul.tiles.AzulTile;

import java.util.*;

/**
 * Represents the shared center area where tiles can be drawn.
 */
public class AzulCenter extends Component {

    public List<AzulTile> center;

    public AzulCenter() { super(CoreConstants.ComponentType.BOARD, "AzulCenter"); }

    public AzulCenter(int componentID) {
        super(CoreConstants.ComponentType.BOARD, "AzulCenter", componentID);
    }

    /**
     * Initialises the center with the first player tile.
     *
     * @param ags - The game state.
     */
    public void initialise(AzulGameState ags) {
        this.center = new ArrayList<>();
        this.center.add(AzulTile.FirstPlayer);
    }

    /**
     * Adds the first player tile to the center.
     */
    public void addFirstPlayerTile() { center.add(AzulTile.FirstPlayer); }

    /**
     * Adds an array of tiles to the center.
     *
     * @param tiles - Array of tiles to add to center.
     */
    public void addTiles(AzulTile[] tiles) {
        for (AzulTile tile : tiles) {
            if (tile != AzulTile.Empty) {
                center.add(tile);
            }
        }
    }

    /**
     * Removes all instances of the specified tile type from the center.
     *
     * @param ags - The game state.
     * @param tile - The tile type to remove.
     * @return True of any tiles were removed, false otherwise
     */
    public boolean removeTile(AzulGameState ags, AzulTile tile) {

        int numTilesRemoved = 0;
        Iterator<AzulTile> iterator = center.iterator();

        while (iterator.hasNext()) {
            if (iterator.next() == tile) {
                iterator.remove();
                numTilesRemoved++;
            }
        }

        ags.setNumOfTilesPicked(numTilesRemoved);
//        System.out.println("Center after: " + Arrays.toString(center.toArray()));
//        System.out.println("Number of tiles removed: " + numTilesRemoved);

        return numTilesRemoved > 0;
    }

    /**
     * Gets all tiles in the center.
     *
     * @return list of tiles in the center.
     */
    public List<AzulTile> getTiles() { return new ArrayList<>(center); }

    public boolean isFirstPlayer() {
        boolean b = false;
        for (int i = 0; i < center.size(); i++){
            b = center.get(i) == AzulTile.FirstPlayer;
        }
        return b;
    }

    /**
     * Checks if the center is empty.
     *
     * @return True if the center is empty, false otherwise.
     */
    public boolean isEmpty() { return center.isEmpty(); }

    /**
     * Gets a set of distinct tile types in the center.
     * @return set of tile types.
     */
    public Set<AzulTile> getTileTypes() {
        Set<AzulTile> tiles = new HashSet<>();

        for (AzulTile tile : center) {
            if (tile != AzulTile.Empty) {
                tiles.add(tile);
            }
        }

        return tiles;
    }

    @Override
    public Component copy() {
        AzulCenter copy = new AzulCenter(componentID);
        copyComponentTo(copy);
        copy.center = new ArrayList<>(this.center);
        return copy;
    }
}