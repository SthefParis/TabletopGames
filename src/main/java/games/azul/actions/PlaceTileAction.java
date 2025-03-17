package games.azul.actions;

import core.AbstractGameState;
import core.actions.AbstractAction;
import core.interfaces.IPrintable;
import games.azul.AzulGameState;
import games.azul.components.AzulPlayerBoard;
import games.azul.tiles.AzulTile;

import java.util.Objects;

public class PlaceTileAction extends AbstractAction implements IPrintable {

    private final int playerID;
    private final AzulTile tile;
    private final int numOfTiles;
    private final int row;
    private boolean inFloorLine;

    public PlaceTileAction(int playerID, AzulTile tile, int numOfTiles, int row) {
        this.playerID = playerID;
        this.tile = tile;
        this.numOfTiles = numOfTiles;
        this.row = row;
        this.inFloorLine = false;
    }

    @Override
    public boolean execute(AbstractGameState gs) {
        AzulGameState ags = (AzulGameState) gs;
        System.out.println("Executing PlaceTileAction for Player "  + playerID);
        System.out.println("Placing on Temp Board");
        placeTileOnTempBoard(ags);

        return true;
    }

    public void placeTileOnTempBoard(AzulGameState ags){
        int placedTiles = 0;
        AzulPlayerBoard playerBoard = ags.getPlayerBoard(playerID);
        for (int i=0; i < numOfTiles; i++) {
            boolean tilePlaced = playerBoard.placeTileInPatternLine(ags, tile, row);

            if (tilePlaced) {
                placedTiles++;
                System.out.println("Successfully placed " + tile.getTileType() + " tile " + (i + 1) + " out of " + numOfTiles + " in row " + row);
            }
            else {
                inFloorLine = true;
                System.out.println("Failed to place " + tile.getTileType() + " tile " + (i + 1) + "/" + numOfTiles + " in row " + row + ". Moving to floor line.");
                playerBoard.placeTileInFloorLine(ags, tile);
                System.out.println("Floor Line main");
            }
        }

        if (placedTiles == numOfTiles) {
            System.out.println("All " + numOfTiles + " tiles successfully placed in row " + row);
        } else {
            System.out.println((numOfTiles - placedTiles) + " tiles moved to the floor line.");
        }
    }

    @Override
    public AbstractAction copy() {
        return new PlaceTileAction(playerID, tile, numOfTiles, row);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PlaceTileAction other = (PlaceTileAction) obj;
        return playerID == other.playerID &&
                row == other.row &&
                tile == other.tile &&
                numOfTiles == other.numOfTiles;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerID, tile, numOfTiles, row);
    }

    @Override
    public String getString(AbstractGameState gameState) {
        return inFloorLine
                ? String.format("Placed %s on the floor line.", tile.getTileType())
                : String.format("Placed %d %s tile(s) on row %d.", numOfTiles, tile.getTileType(), row);
    }
}
