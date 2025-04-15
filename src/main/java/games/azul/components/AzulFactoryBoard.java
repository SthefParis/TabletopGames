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
     * @param ags
     */
    public void refill(AzulGameState ags) {
        AzulParameters parameters = (AzulParameters) ags.getGameParameters();
        HashMap<AzulTile, Integer> tileCounts = ags.getAllTileCounts();
        //System.out.println("Tile counts in refill: " + tileCounts);
        int totalTilesInBag = tileCounts.values().stream().mapToInt(Integer::intValue).sum();

        //System.out.println("Total tiles in bag: " + totalTilesInBag);
        //System.out.println("Factory size: " +factorySize);
        if (totalTilesInBag < factorySize) {
            //System.out.println("Bag has less than 4 tiles. Bag: " + tileCounts + " Lid: " + ags.getLid());

            if (!ags.getLid().isEmpty()){
                //System.out.println("Lid isnt empty");

                tileCounts.clear();
                tileCounts.putAll(ags.getLid());
                ags.updateAllTileCounts(tileCounts);
                ags.EmptyLid();

                //System.out.println("Lid after transfer: " + ags.getLid());
                //System.out.println("Bag after transfer: " + ags.getAllTileCounts());
            }

            totalTilesInBag = tileCounts.values().stream().mapToInt(Integer::intValue).sum();

            // TESTING
            if (totalTilesInBag < 4) {
                //System.out.println("Even after transfer, not enough tiles to refill.");
                return;
            }
        }

        // Reset factory board
        Arrays.fill(factoryBoard, AzulTile.Empty);

        // List to store selected tiles
//        List<AzulTile> availableTiles = new ArrayList<>();
//        tileCounts.forEach((tile, count) -> {
//            for (int i = 0; i < count; i++) {
//                availableTiles.add(tile);
//            }
//        });
        List<AzulTile> availableTiles = new ArrayList<>();
        for (Map.Entry<AzulTile, Integer> entry : tileCounts.entrySet()) {
            AzulTile tile = entry.getKey();
//            System.out.println("Tile added to available tiles: " + tile);
            int count = entry.getValue();
//            System.out.println("Number of times tile is added: " + count);
            // Add tiles as many times as its count
            for (int i = 0; i < count; i++){
                availableTiles.add(tile);

//                System.out.println("Available tiles after adding tile: "  +availableTiles);
            }
        }


        //System.out.println("Available tiles: " + availableTiles);

        // Randomly shuffle and select 4 tiles
        Collections.shuffle(availableTiles);
//        System.out.println("Available tiles have been shuffled: " + availableTiles);
        for (int j = 0; j < 4; j++) {
//            System.out.println("Step 1");
            AzulTile drawnTile = availableTiles.get(j);
//            System.out.println("Step 2");
            factoryBoard[j] = drawnTile;
//            System.out.println("Drawn tile: " + drawnTile + " tile in factory board: " + factoryBoard[j]);
            tileCounts.put(drawnTile, tileCounts.get(drawnTile) - 1);
        }

        //System.out.println("Factory board right after refill: " + Arrays.toString(factoryBoard));
        //System.out.println("Lid at end of refill: " + ags.getLid());
        //System.out.println("Bag at end of refill: " + ags.getAllTileCounts());

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
        //System.out.println("Factory board before: " + Arrays.toString(factoryBoard));
        //System.out.println("Selected: " + tile.getTileType());

        int numTilesRemoved = 0;

        for (int i = 0; i < factoryBoard.length; i++) {
            if (factoryBoard[i] == tile) {
                factoryBoard[i] = AzulTile.Empty;
                numTilesRemoved++;
            }
        }

        ags.setNumOfTilesPicked(numTilesRemoved);

        //System.out.println("Factory board after: " + Arrays.toString(factoryBoard));
        //System.out.println("Number of tiles removed: " + numTilesRemoved);
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
