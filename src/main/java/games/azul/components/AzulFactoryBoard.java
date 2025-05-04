package games.azul.components;

import core.CoreConstants;
import core.components.Component;
import games.azul.AzulGameState;
import games.azul.AzulParameters;
import games.azul.tiles.AzulTile;

import java.util.*;

/**
 * Represents one factory board where tiles are placed and drawn from.
 */
public class AzulFactoryBoard extends Component {

    int tileCount = 0;
    public AzulTile[] factoryBoard;
    int factoryNum = 0;
    private int factorySize;

    public AzulFactoryBoard() { super(CoreConstants.ComponentType.BOARD, "AzulFactoryBoard"); }

    public AzulFactoryBoard(int componentID) {
        super(CoreConstants.ComponentType.BOARD, "AzulFactoryBoard", componentID);
    }

    /**
     * Initialises the factory board with a specific size and factory number.
     *
     * @param params - The game parameters.
     * @param factoryNum - The factory number.
     */
    public void initialise(AzulParameters params, int factoryNum) {
        factorySize = params.getNTilesPerFactory();
        this.factoryBoard = new AzulTile[factorySize];
        this.factoryNum = factoryNum;
    }

    /**
     * Refills the factory with 4 random tiles from the bag (refilling from the lid if needed).
     *
     * @param ags - Game state.
     */
    public void refill(AzulGameState ags) {
        AzulParameters parameters = (AzulParameters) ags.getGameParameters();
        HashMap<AzulTile, Integer> tileCounts = ags.getAllTileCounts();
        int totalTilesInBag = tileCounts.values().stream().mapToInt(Integer::intValue).sum();

        if (totalTilesInBag < factorySize) {
            if (!ags.getLid().isEmpty()){
                tileCounts.clear();
                tileCounts.putAll(ags.getLid());
                ags.updateAllTileCounts(tileCounts);
                ags.EmptyLid();
            }

            totalTilesInBag = tileCounts.values().stream().mapToInt(Integer::intValue).sum();
        }

        // Reset factory board
        Arrays.fill(factoryBoard, AzulTile.Empty);

        List<AzulTile> availableTiles = new ArrayList<>();
        for (Map.Entry<AzulTile, Integer> entry : tileCounts.entrySet()) {
            AzulTile tile = entry.getKey();
            int count = entry.getValue();
            // Add tiles as many times as its count
            for (int i = 0; i < count; i++){
                availableTiles.add(tile);
            }
        }

        // Randomly shuffle and select 4 tiles
        Collections.shuffle(availableTiles);
        for (int j = 0; j < 4; j++) {
            AzulTile drawnTile = availableTiles.get(j);
            factoryBoard[j] = drawnTile;
            tileCounts.put(drawnTile, tileCounts.get(drawnTile) - 1);
        }

        ags.updateAllTileCounts(tileCounts);
    }

    /**
     * Gets all distinct tile types present in the factory.
     *
     * @return A set of tile types.
     */
    public Set<AzulTile> getTileTypes() {
        Set<AzulTile> tileTypes = new HashSet<>();
        for (AzulTile tile : factoryBoard) {
            if (tile != null && tile.getTileType() != AzulTile.Empty) {
                tileTypes.add(tile.getTileType());
            }
        }
        return tileTypes;
    }

    /**
     * Clears all tiles from the factory.
     *
     * @return the removed tiles.
     */
    public AzulTile[] clearTiles() {
        //System.out.println("Clearing tiles");

        // Store remaining tiles before clearing
        AzulTile[] remainingTiles = Arrays.stream(factoryBoard)
                .filter(tile -> tile != AzulTile.Empty)
                .toArray(AzulTile[]::new);

        // Clear factory board
        Arrays.fill(factoryBoard, AzulTile.Empty);
        return remainingTiles;
    }

    /**
     * Removes all tiles of a given type from this factory.
     *
     * @param ags - The game state.
     * @param tile - The tile type to remove.
     * @return True if any tiles were removed.
     */
    public boolean removeTile(AzulGameState ags, AzulTile tile) {
        int numTilesRemoved = 0;

        for (int i = 0; i < factoryBoard.length; i++) {
            if (factoryBoard[i] == tile) {
                factoryBoard[i] = AzulTile.Empty;
                numTilesRemoved++;
            }
        }

        ags.setNumOfTilesPicked(numTilesRemoved);
        return numTilesRemoved > 0;
    }

    /**
     * Gets the factory ID.
     * @return the factory ID.
     */
    public int getFactoryNum() {
        return factoryNum;
    }

    /**
     * Checks if the factory is empty.
     * @return True if the factory is empty, false otherwise.
     */
    public boolean isEmpty() {
        return getTileTypes().isEmpty();
    }

    @Override
    public Component copy() {
        AzulFactoryBoard copy = new AzulFactoryBoard(componentID);
        copyComponentTo(copy);

        copy.tileCount = this.tileCount;
        copy.factoryNum = this.factoryNum;
        copy.factorySize = this.factorySize;
        copy.factoryBoard = Arrays.copyOf(this.factoryBoard, this.factoryBoard.length); // Deep copy
        // copy.factoryBoard = this.factoryBoard.clone();

        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        AzulFactoryBoard that = (AzulFactoryBoard) o;
        return tileCount == that.tileCount &&
                factoryNum == that.factoryNum &&
                Objects.deepEquals(factoryBoard, that.factoryBoard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), tileCount, Arrays.hashCode(factoryBoard), factoryNum);
    }
}