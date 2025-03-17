package games.azul.components;

import core.CoreConstants;
import core.components.Component;
import games.azul.AzulGameState;
import games.azul.AzulParameters;
import games.azul.tiles.AzulTile;

import java.util.*;

public class AzulFactoryBoard extends Component {

    int tileCount = 0;
    public AzulTile[] factoryBoard;
    int factoryNum = 0;

    public AzulFactoryBoard() { super(CoreConstants.ComponentType.BOARD, "AzulFactoryBoard"); }

    public AzulFactoryBoard(int componentID) {
        super(CoreConstants.ComponentType.BOARD, "AzulFactoryBoard", componentID);
    }

    public void initialise(AzulParameters params, int factoryNum) {
        int factorySize = params.getNTilesPerFactory();
        this.factoryBoard = new AzulTile[factorySize];
        this.factoryNum = factoryNum;
    }

    public void refill(AzulGameState ags) {
        AzulParameters parameters = (AzulParameters) ags.getGameParameters();

        HashMap<AzulTile, Integer> tileCounts = ags.getAllTileCounts();
        int totalTilesInBag = tileCounts.values().stream().mapToInt(Integer::intValue).sum();

        if (totalTilesInBag < parameters.getNTilesPerFactory()) {
            System.out.println("Bag has less than 4 tiles. Bag: " + tileCounts + " Lid: " + ags.getLid());

            if (!ags.getLid().isEmpty()){
                System.out.println("Lid isnt empty");

                tileCounts.clear();
                tileCounts.putAll(ags.getLid());
                ags.updateAllTileCounts(tileCounts);
                ags.EmptyLid();

                System.out.println("Lid after transfer: " + ags.getLid());
                System.out.println("Bag after transfer: " + ags.getAllTileCounts());
            }

            totalTilesInBag = tileCounts.values().stream().mapToInt(Integer::intValue).sum();

            if (totalTilesInBag < 4) {
                System.out.println("Even after transfer, not enough tiles to refill.");
                return;
            }
        }

        // Reset factory board
        this.factoryBoard = new AzulTile[4];

        // List to store selected tiles
        List<AzulTile> availableTiles = new ArrayList<>();
        for (Map.Entry<AzulTile, Integer> entry : tileCounts.entrySet()) {
            AzulTile tile = entry.getKey();
            int count = entry.getValue();

            // Add tiles as many times as its count
            for (int i=0; i<count; i++){
                availableTiles.add(tile);
            }
        }

        // Randomly shuffle and select 4 tiles
        Collections.shuffle(availableTiles);
        for (int i = 0; i < 4; i++) {
            factoryBoard[i] = availableTiles.get(i);
            tileCounts.put(factoryBoard[i], tileCounts.get(factoryBoard[i]) - 1);
        }

        System.out.println("Lid at end of refill: " + ags.getLid());
        System.out.println("Bag at end of refill: " + ags.getAllTileCounts());

        ags.updateAllTileCounts(tileCounts);
    }


    public Set<AzulTile> getTileTypes() {
        Set<AzulTile> tileTypes = new HashSet<>();
        for (AzulTile tile : factoryBoard) {
            if (tile != null && tile.getTileType() != AzulTile.Empty) {
                tileTypes.add(tile.getTileType());
            }
        }
        return tileTypes;
    }

    public AzulTile[] clearTiles() {
        System.out.println("Clearing tiles");

        // Store remaining tiles before clearing
        AzulTile[] remainingTiles = Arrays.stream(factoryBoard)
                .filter(tile -> tile != AzulTile.Empty)
                .toArray(AzulTile[]::new);

        // Clear factory board
        Arrays.fill(factoryBoard, AzulTile.Empty);

        return remainingTiles;
    }

    public boolean removeTile(AzulGameState ags, AzulTile tile) {
        System.out.println("Factory board before: " + Arrays.toString(factoryBoard));
        System.out.println("Selected: " + tile.getTileType());

        boolean tileRemoved = false;
        int tilesRemoved = 0;

        for (int i=0; i<factoryBoard.length; i++) {
            if (factoryBoard[i] == tile) {
                factoryBoard[i] = AzulTile.Empty;
                tilesRemoved++;
                tileRemoved = true;
            }
        }

        ags.setNumOfTilesPicked(tilesRemoved);

        System.out.println("Factory board after: " + Arrays.toString(factoryBoard));
        System.out.println("Number of tiles removed: " + tilesRemoved);
        return tileRemoved;
    }

    public int getFactoryNum() {
        return factoryNum;
    }

    public boolean isEmpty() {
        return getTileTypes().isEmpty();
    }

    @Override
    public Component copy() {
        AzulFactoryBoard copy = new AzulFactoryBoard(componentID);
        copyComponentTo(copy);

        copy.tileCount = this.tileCount;
        copy.factoryNum = this.factoryNum;

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
