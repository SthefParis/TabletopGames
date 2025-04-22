package games.azul.components;

import core.CoreConstants;
import core.components.Component;
import games.azul.AzulGameState;
import games.azul.tiles.AzulTile;

import java.util.*;

/**
 * Represents the shared centre area where tiles can be drawn.
 */
public class AzulCentre extends Component {

    public List<AzulTile> centre;

    public AzulCentre() { super(CoreConstants.ComponentType.BOARD, "AzulCentre"); }

    public AzulCentre(int componentID) {
        super(CoreConstants.ComponentType.BOARD, "AzulCentre", componentID);
    }

    /**
     * Initialises the centre with the first player tile.
     *
     * @param ags - The game state.
     */
    public void initialise(AzulGameState ags) {
        this.centre = new ArrayList<>();
        this.centre.add(AzulTile.FirstPlayer);
    }

    /**
     * Adds the first player tile to the centre.
     */
    public void addFirstPlayerTile() { centre.add(AzulTile.FirstPlayer); }

    /**
     * Adds an array of tiles to the centre.
     *
     * @param tiles - Array of tiles to add to centre.
     */
    public void addTiles(AzulTile[] tiles) {
        for (AzulTile tile : tiles) {
            if (tile != AzulTile.Empty) {
                centre.add(tile);
            }
        }
    }

    /**
     * Removes all instances of the specified tile type from the centre.
     *
     * @param ags - The game state.
     * @param tile - The tile type to remove.
     * @return True of any tiles were removed, false otherwise
     */
    public boolean removeTile(AzulGameState ags, AzulTile tile) {

        int numTilesRemoved = 0;
        Iterator<AzulTile> iterator = centre.iterator();

        while (iterator.hasNext()) {
            if (iterator.next() == tile) {
                iterator.remove();
                numTilesRemoved++;
            }
        }

        ags.setNumOfTilesPicked(numTilesRemoved);
//        System.out.println("Center after: " + Arrays.toString(centre.toArray()));
//        System.out.println("Number of tiles removed: " + numTilesRemoved);

        return numTilesRemoved > 0;
    }

    /**
     * Gets all tiles in the centre.
     *
     * @return list of tiles in the centre.
     */
    public List<AzulTile> getTiles() { return new ArrayList<>(centre); }

    public boolean isFirstPlayer() {
        boolean b = false;
        for (int i = 0; i < centre.size(); i++){
            b = centre.get(i) == AzulTile.FirstPlayer;
        }
        return b;
    }

    /**
     * Checks if the centre is empty.
     *
     * @return True if the centre is empty, false otherwise.
     */
    public boolean isEmpty() { return centre.isEmpty(); }

    /**
     * Gets a set of distinct tile types in the centre.
     * @return set of tile types.
     */
    public Set<AzulTile> getTileTypes() {
        Set<AzulTile> tiles = new HashSet<>();

        for (AzulTile tile : centre) {
            if (tile != AzulTile.Empty) {
                tiles.add(tile);
            }
        }

        return tiles;
    }

    @Override
    public Component copy() {
        AzulCentre copy = new AzulCentre(componentID);
        copyComponentTo(copy);
        copy.centre = new ArrayList<>(this.centre);
        return copy;
    }
}