package games.azul.actions;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.interfaces.IPrintable;
import games.azul.AzulGameState;
import games.azul.components.AzulCentre;
import games.azul.components.AzulFactoryBoard;
import games.azul.tiles.AzulTile;

import java.util.Objects;

/**
 * Action representing picking up tiles from a factory or the centre.
 */
public class PickUpTilesAction extends AbstractAction implements IPrintable {

    private final int playerID;
    private final AzulTile tile;
    private final int factoryId;

    public PickUpTilesAction(int playerID, AzulTile tile, int factoryId) {
        this.playerID = playerID;
        this.tile = tile;
        this.factoryId = factoryId;
    }

    /**
     * Executes the action of picking up tiles from either a factory or the centre.
     *
     * @param gs - The current game state.
     * @return true if the action was successful, false otherwise.
     */
    @Override
    public boolean execute(AbstractGameState gs) {
        AzulGameState ags = (AzulGameState) gs;
        AzulCentre center = ags.getCentre();

        if (factoryId == -1) {
            // Pick from centre
            //System.out.println("Player " + playerID + " picks up from centre: " + tile);
            return pickUpTilesFromCenter(ags, center);
        } else {
            AzulFactoryBoard factory = ags.getFactory(factoryId);
            if (factory == null) {
                //System.out.println("Factory " + factoryId + " not found.");
                return false;
            }
            //System.out.println("Player " + playerID + " picks up from factory " + factoryId + ": " + tile);
            return pickUpTilesFromFactory(ags, factory);
        }
    }

    /**
     * Handles the logic for picking up tiles from a factory.
     *
     * @param ags - The game state.
     * @param fb  - The factory board.
     */
    public boolean pickUpTilesFromFactory(AzulGameState ags, AzulFactoryBoard fb) {
        int selectedFactory = fb.getFactoryNum();

        if (selectedFactory == -1 || tile == AzulTile.Empty || tile == null) return false;

        boolean tileRemoved = ags.getFactory(selectedFactory).removeTile(ags, tile);

        if (!tileRemoved) return false;

        ags.setPickedTile(tile);

        //System.out.println(tile + " tile removed from factory " + selectedFactory + ": " +Arrays.toString(fb.factoryBoard));

        // Move remaining tiles to centre
        AzulTile[] remainingTiles = fb.clearTiles();
        ags.getCentre().addTiles(remainingTiles);
        //System.out.println("Remaining tiles moved to centre: " + Arrays.toString(remainingTiles));
        return true;
    }

    /**
     * Handles the logic for picking up tiles from the centre.
     *
     * @param ags    - The game state.
     * @param center - The shared centre.
     */
    public boolean pickUpTilesFromCenter(AzulGameState ags, AzulCentre center) {
        // Check if the player is the first to pick from the centre
        if (!ags.hasPickedFromCenter()) {
            // If this is the first player to pick from the centre, give them the first player tile
            //System.out.println("Giving first player tile to player " + playerID);

            AzulTile[] floorLine = ags.getPlayerBoard(playerID).playerFloorLine;

            // Place the first player tile in the first available spot on the player's floor line
            for (int i = 0; i < floorLine.length; i++) {
                if (floorLine[i] == AzulTile.Empty || floorLine[i] == null) {
                    floorLine[i] = AzulTile.FirstPlayer;
                    //System.out.println("Placed first player tile in floor line at position " + i);
                    break;
                }
            }

            // Set the flag to indicate that the first player tile has been picked up
            ags.setHasPickedFromCenter(true);
        }

        // Remove the first player tile from the centre, if it's still there
        boolean firstPlayerTileRemoved = center.removeTile(ags, AzulTile.FirstPlayer);
        if (firstPlayerTileRemoved) { // TESTING!!
            //System.out.println("First player tile removed from centre.");
        }

        // Proceed with picking up the tile from the centre
        boolean tileRemoved = center.removeTile(ags, tile);
        //System.out.println("Player " + playerID + " picks tile: " + tile);

        // If tile removal was unsuccessful, log an error
        if (!tileRemoved) return false;

        ags.setPickedTile(tile);
        return true;
    }


    /**
     * Gets the color of the tile associated with this action.
     *
     * @return The tile's color.
     */
    public String getTileColour() {
        return this.tile.getColourAsString(this.tile.getColor());
    }

    /**
     * Determines if the tile was picked up from a factory.
     *
     * @return True if the tile was picked from a factory, false if picked from the centre.
     */
    public boolean isFromFactory() {
        return factoryId != -1;
    }

//    /**
//     * Returns how many tiles were picked up in this action.
//     *
//     * @param ags - The Game state.
//     * @return Number of tiles picked.
//     */
//    public Integer getTileCount(AzulGameState ags) {
//        return ags.getNumOfTilesPicked();
//    }

    @Override
    public AbstractAction copy() {
        return new PickUpTilesAction(playerID, tile, factoryId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PickUpTilesAction other)) return false;
        return playerID == other.playerID &&
                tile == other.tile &&
                factoryId == other.factoryId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerID, tile, factoryId);
    }

    /**
     * A readable string representation of this action.
     *
     * @param gameState - The current game state.
     * @return String description of the action.
     */
    @Override
    public String getString(AbstractGameState gameState) {
        return (factoryId == -1)
                ? String.format("Picked up %s tiles from centre.", tile.getTileType())
                : String.format("Picked up %s tiles from factory %d.", tile.getTileType(), factoryId);
    }
}

