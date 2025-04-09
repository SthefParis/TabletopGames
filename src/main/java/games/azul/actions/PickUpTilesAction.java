package games.azul.actions;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.interfaces.IPrintable;
import games.azul.AzulGameState;
import games.azul.components.AzulCenter;
import games.azul.components.AzulFactoryBoard;
import games.azul.tiles.AzulTile;

import java.awt.*;
import java.util.Arrays;
import java.util.Objects;

/**
 * Action representing picking up tiles from a factory or the center.
 */
public class PickUpTilesAction extends AbstractAction implements IPrintable {

    protected final int playerID;
    protected final AzulTile tile;
    protected final int factoryId;

    public PickUpTilesAction(int playerID, AzulTile tile, int factoryId) {
        this.playerID = playerID;
        this.tile = tile;
        this.factoryId = factoryId;
    }

    /**
     * Executes the action of picking up tiles from either a factory or the center.
     *
     * @param gs - The current game state.
     * @return true if the action was successful, false otherwise.
     */
    @Override
    public boolean execute(AbstractGameState gs) {
        AzulGameState ags = (AzulGameState) gs;
        AzulCenter center = ags.getCenter();

        if (factoryId == -1) {
            // Pick from center
            System.out.println("Player " + playerID + " picks up from center: " + tile);
            pickUpTilesFromCenter(ags, center);
        } else {
            AzulFactoryBoard factory = ags.getFactory(factoryId);
            if (factory == null) {
                System.out.println("Factory " + factoryId + " not found.");
                return false;
            }
            System.out.println("Player " + playerID + " picks up from factory " + factoryId + ": " + tile);
            pickUpTilesFromFactory(ags, factory);
        }

        return true;
    }

    /**
     * Handles the logic for picking up tiles from a factory.
     *
     * @param ags - The game state.
     * @param fb  - The factory board.
     */
    public void pickUpTilesFromFactory(AzulGameState ags, AzulFactoryBoard fb) {
        int selectedFactory = fb.getFactoryNum();

        if (selectedFactory == -1 || tile == AzulTile.Empty) {
            System.out.println("Invalid tile or factory selection: " + tile);
            return;
        }

        boolean tileRemoved = ags.getFactory(selectedFactory).removeTile(ags, tile);
        ags.setPickedTile(tile);

        if (!tileRemoved) {
            System.out.println("Failed to remove tile. Check logic.");
            return;
        }

        System.out.println(tile + " tile removed from factory " + selectedFactory + ": " +
                Arrays.toString(fb.factoryBoard));

        // Move remaining tiles to center
        AzulTile[] remainingTiles = fb.clearTiles();
        ags.getCenter().addTiles(remainingTiles);
        System.out.println("Remaining tiles moved to center: " + Arrays.toString(remainingTiles));
    }

    /**
     * Handles the logic for picking up tiles from the center.
     *
     * @param ags    - The game state.
     * @param center - The shared center.
     */
    public void pickUpTilesFromCenter(AzulGameState ags, AzulCenter center) {
//        System.out.println("Center before removing: " + Arrays.toString(center.center.toArray()));

        // Remove first player tile if present
        boolean firstPlayerTileRemoved = center.removeTile(ags, AzulTile.FirstPlayer);
        if (firstPlayerTileRemoved) {
            System.out.println("First player tile removed from center.");
        }

        // Give first player tile if not already picked
        if (!ags.hasPickedFromCenter()) {
            System.out.println("Giving first player tile to player " + playerID);
            AzulTile[] floorLine = ags.getPlayerBoard(playerID).playerFloorLine;

            for (int i = 0; i < floorLine.length; i++) {
                if (floorLine[i] == AzulTile.Empty || floorLine[i] == null) {
                    floorLine[i] = AzulTile.FirstPlayer;
                    System.out.println("Placed first player tile in floor line at position " + i);
                    break;
                }
            }
            ags.setHasPickedFromCenter(true);
        }

        boolean tileRemoved = center.removeTile(ags, tile);
        ags.setPickedTile(tile);
    }

    /**
     * Gets the color of the tile associated with this action.
     *
     * @return The tile's color.
     */
    public Color getTileColour() {
        return this.tile.getColor();
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
                ? String.format("Picked up %s tiles from center.", tile.getTileType())
                : String.format("Picked up %s tiles from factory %d.", tile.getTileType(), factoryId);
    }
}

