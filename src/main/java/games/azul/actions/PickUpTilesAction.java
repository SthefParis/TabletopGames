package games.azul.actions;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.interfaces.IPrintable;
import games.azul.AzulGameState;
import games.azul.components.AzulCenter;
import games.azul.components.AzulFactoryBoard;
import games.azul.tiles.AzulTile;

import java.util.Arrays;
import java.util.Objects;

public class PickUpTilesAction extends AbstractAction implements IPrintable {

    protected final int playerID;
    protected final AzulTile tile;
    protected final int factoryId;

    public PickUpTilesAction(int playerID, AzulTile tile, int factoryId) {
        this.playerID = playerID;
        this.tile = tile;
        this.factoryId = factoryId;
    }

    @Override
    public boolean execute(AbstractGameState gs) {
        AzulGameState ags = (AzulGameState) gs;
        AzulCenter center = ags.getCenter();

        // If fb (factory board) is null, player is picking up from the center
        if (factoryId == -1) {
            // Pick from center
            System.out.println("Player " + playerID + " picks up from center: " + tile);
            pickUpTilesFromCenter(ags, center);
        } else {
            // Pick from factory
            AzulFactoryBoard factory = ags.getFactory(factoryId);
            if (factory != null) {
                System.out.println("Player " + playerID + " picks up from factory " + factoryId + ": " + tile);
                pickUpTilesFromFactory(ags, factory);
            } else {
                System.out.println("Factory " + factoryId + " not found.");
                return false;
            }
        }

        return true;
    }

    public void pickUpTilesFromFactory(AzulGameState ags, AzulFactoryBoard fb) {
        int selectedFactory = fb.getFactoryNum();

        if (selectedFactory != - 1 && tile != AzulTile.Empty) {
            // Remove tile from the factory
            boolean tileRemoved = ags.getFactory(selectedFactory).removeTile(ags, tile);
            ags.setPickedTile(tile);

            if (tileRemoved) {
                System.out.println(tile + " tile removed from factory " + selectedFactory + ": " + Arrays.toString(fb.factoryBoard));

                // Move all remaining tiles in factory to center
                AzulTile[] remainingTiles = fb.clearTiles();
                ags.getCenter().addTiles(remainingTiles);
                System.out.println("Remaining tiles moved to center: " + Arrays.toString(remainingTiles));
            } else {
                System.out.println("Failed to remove tile. Check logic.");
            }
        } else {
            System.out.println("No factory or tile selected." + tile);
        }
    }

    public void pickUpTilesFromCenter(AzulGameState ags, AzulCenter center) {
        System.out.println("Center before removing: " + Arrays.toString(center.center.toArray()));

        boolean firstPlayerTileRemoved = ags.getCenter().removeTile(ags, AzulTile.FirstPlayer);

        if (firstPlayerTileRemoved) {
            System.out.println("First player tile removed from center.");
        }

        if (!ags.hasPickedFromCenter()) {
            System.out.println("Giving first player tile to player " + playerID);
            AzulTile[] floorLine = ags.getPlayerBoard(playerID).playerFloorLine;

            for (int i = 0; i < floorLine.length; i++) {
                if (floorLine[i] == AzulTile.Empty || floorLine[i] == null) {
                    floorLine[i] = AzulTile.FirstPlayer;
                    System.out.println("Player " + playerID + " picks up the first player tile. Placed in floor line at position " + i);
                    break;
                }
            }
            ags.setHasPickedFromCenter(true);
        }

        boolean tileRemoved = ags.getCenter().removeTile(ags, tile);
        ags.setPickedTile(tile);
    }

    @Override
    public AbstractAction copy() {
        return new PickUpTilesAction(playerID, tile, factoryId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PickUpTilesAction other = (PickUpTilesAction) obj;
        return playerID == other.playerID &&
                tile == other.tile &&
                factoryId == other.factoryId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerID, tile, factoryId);
    }

    @Override
    public String getString(AbstractGameState gameState) {
        return (factoryId == -1)
                ? String.format("Picked up %s tiles from center.", tile.getTileType())
                : String.format("Picked up %s tiles from factory %d.", tile.getTileType(), factoryId);
    }
}
