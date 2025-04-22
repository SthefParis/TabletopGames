package games.azul.components;

import core.CoreConstants;
import core.components.Component;
import games.azul.AzulGameState;
import games.azul.AzulParameters;
import games.azul.tiles.AzulTile;

import java.util.Arrays;

/**
 * Represents a player's board in Azul, managing the walls, pattern lines, score track, and floor line.
 * It extends the {@link Component} class and implements the {@link Component#copy()} method.
 */
public class AzulPlayerBoard extends Component {
    public AzulTile[][] playerWall; // Refers to Wall
    public AzulTile[][] playerPatternWall; // Refers to Pattern Lines
    public AzulTile[] playerScoreTrack; // Refers to the score track
    public AzulTile[] playerFloorLine; // Refers to the floor line

    public int playerID;

    public AzulPlayerBoard() {
        super(CoreConstants.ComponentType.BOARD, "AzulPlayerBoard");
    }

    protected AzulPlayerBoard(int componentID) {
        super(CoreConstants.ComponentType.BOARD, "AzulPlayerBoard", componentID);
    }

    /**
     * Initialises the player's board with the appropriate sizes for the walls, patern lines, score track, and floor line.
     * @param ags - Game state, used to get the game parameters.
     */
    public void initialise(AzulGameState ags, int playerID){
        int boardSize = ((AzulParameters) ags.getGameParameters()).getBoardSize();
        int scoreTrackSize = ((AzulParameters) ags.getGameParameters()).getScoreTrackLength();
        int floorLineSize = ((AzulParameters) ags.getGameParameters()).getFloorLineLength();

        this.playerWall = new AzulTile[boardSize][boardSize];
        this.playerPatternWall = new AzulTile[boardSize][];
        this.playerScoreTrack = new AzulTile[scoreTrackSize];
        this.playerFloorLine = new AzulTile[floorLineSize];
        this.playerID = playerID;

        for(int i = 0; i < boardSize; i++){
            playerPatternWall[i] = new AzulTile[i+1];
            for(AzulTile tile : playerPatternWall[i]) {
                tile = AzulTile.Empty;
            }
        }
    }

    /**
     * Places a tile in the players wall.
     * @param ags - Game state.
     * @param tile - Tile that needs to be placed.
     * @param row - Row that tile needs to be placed on.
     * @param col - Column that the tile must be placed in according to the pattern.
     * @return boolean - Returns true if the tile was placed successfully, false otherwise.
     */
    public boolean placeTileInWall(AzulGameState ags, AzulTile tile, int row, int col) {
        //System.out.println("Wall before: " + Arrays.deepToString(playerWall));
        //System.out.println("Tile placing: " + tile.getTileType());

        if (row < 0 || row >= playerWall.length) {
            //System.out.println("Row out of bounds: " + row);
            return false;
        }

        if (isPositionEmpty(row, col)) {
            //System.out.println("Tile can be placed.");
            playerWall[row][col] = tile;
            //System.out.println("Wall after: " + Arrays.deepToString(playerWall));
            clearRowOnPatternLine(ags, row, col);
            //System.out.println("Row has been cleared");
            return true;
        }

        //System.out.println(tile.getTileType() + " tile cannot be placed on playerWall as tile already exists: row " + row + " col " + col);
        return false;
    }

    /**
     * Checks if a specific position in the wall is empty.
     * @param row - Row to check.
     * @param col - Column to check.
     * @return boolean - Returns true if the position is empty, false otherwise.
     */
    public boolean isPositionEmpty(int row, int col) {
        return playerWall[row][col] == AzulTile.Empty || playerWall[row][col] == null;
    }

    /**
     * Places a tile in the pattern wall if the row is valid for the tile type.
     * @param ags - Game state.
     * @param tile - Tile that needs to be placed.
     * @param row - Row in which the tile needs to be placed.
     * @return boolean - Returns true if the tile was placed successfully, false otherwise.
     */
    public boolean placeTileInPatternLine(AzulGameState ags, AzulTile tile, int row){

        if (row < 0 || row >= playerPatternWall.length){
            return false;
        }

        if (hasBeenTiled(tile, row)) {
            // Find an alternative row
            row = findValidRow(tile);
            if (row == -1) {
//                placeTileInFloorLine(ags, tile);
                return false;
            }
        }

        if (!isRowValid(row, tile)) {
            // Find alternative row
            row = findValidRow(tile);
            if (row == -1) {
//                placeTileInFloorLine(ags, tile);
                return false;
            }
        }

        // Place tile in the lowest available slot in the selected row
        for (int i = playerPatternWall[row].length - 1; i >= 0; i--){
            if (playerPatternWall[row][i] == null || playerPatternWall[row][i] == AzulTile.Empty){
                playerPatternWall[row][i] = tile;
                //System.out.println("Placed " + tile.getTileType() + " in row " + row + " at position " + i);
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if a given row is valid for placing a specific tile.
     * @param row - Row to check.
     * @param tile - Tile type to check.
     */
    private boolean isRowValid(int row, AzulTile tile) {
        for (AzulTile t : playerPatternWall[row]) {
            if (t != null && t != AzulTile.Empty && t != tile) {
                return false;
            }
        }
        return true;
    }

    /**
     * Finds a valid row for placing a tile.
     * @param tile - Tile type that needs to be placed.
     * @return integer - Number of valid row.
     */
    private int findValidRow(AzulTile tile) {
        for (int r = 0; r < playerPatternWall.length; r++) {
            if (isRowValid(r, tile) && !hasBeenTiled(tile, r)) {
                return r;
            }
        }
        return -1; // No valid row found
    }

    /**
     * Places a tile in the player's floor line if no valid row is found or if row is full.
     * @param ags - Game state.
     * @param tile - Tile type that needs to be placed.
     */
    public void placeTileInFloorLine(AzulGameState ags, AzulTile tile) {
        // Check if floor line is full
        for (int i = 0; i < playerFloorLine.length; i++) {
            if (playerFloorLine[i] == null || playerFloorLine[i] == AzulTile.Empty) {
                // If there's space, place the tile in the floor line
                playerFloorLine[i] = tile;
                //System.out.println("Placed " + tile.getTileType() + " in floor line at position " + i);
                return;
            }
        }

        // If floor line is full, place the tile in the lid
        ags.updateLid(tile, 1);  // Add tile to the lid
        //System.out.println("Floor line full! Placed 1 " + tile.getTileType() + " in the lid: " + ags.getLid().keySet() + " " + ags.getLid().values());
    }

    /**
     * Checks if tile can be placed in a specific row.
     * @param row
     * @return
     */
    public boolean canPlaceTile(int row) {
        boolean isRowFull = isPatternLineRowFull(row);

        return !isRowFull;
    }

    public AzulTile[][] getPlayerWall(){
        return playerWall;
    }

    public AzulTile[][] getPatternLine() {
        return playerPatternWall;
    }

    public boolean isPatternLineEmpty(int row) {
        for (int i = 0; i < playerPatternWall[row].length; i++) {
            if (playerPatternWall[row][i] != null && playerPatternWall[row][i] != AzulTile.Empty) {
                return false;

            }
        }
        return true;
    }

    public boolean isPatternLineRowFull(int row) {
        // Check if row is full
        for (AzulTile tile: playerPatternWall[row]){
            if (tile == AzulTile.Empty || tile == null) {
                return false;
            }
        }
        return true;
    }

    public boolean hasBeenTiled(AzulTile tile, int row) {
        for (int col = 0; col < playerWall[row].length; col++) {
            if (playerWall[row][col] == tile) {
                return true;
            }
        }
        return false;
    }


    public boolean isRowTiled(int row) {
        // Check if the entire row is filled with a tile (none of the positions are empty)
        for (int col = 0; col < playerPatternWall[row].length; col++) {
            if (playerPatternWall[row][col] == AzulTile.Empty) {
                return false; // If any position in the row is empty, the row is not tiled
            }
        }
        return true; // All positions in the row are filled with a tile, the row is fully tiled
    }


    public AzulTile getTileAt(int row) {
        if (row >= 0 && row < playerPatternWall.length && playerPatternWall[row].length > 0) {
            // Since all tiles in this row are the same, return the tile in the first column.
            //System.out.println("Tile colour returned: " + playerPatternWall[row][0]);
            return playerPatternWall[row][0];
        }
        return AzulTile.Empty;
    }

    /**
     * Function clears row in Pattern Line once row is full and tile has been moved to the wall.
     * @param ags - Game state.
     * @param row - Row that is full on pattern line
     */
    public void clearRowOnPatternLine(AzulGameState ags, int row, int col) {
        AzulParameters params = (AzulParameters) ags.getGameParameters();

        if (col != -1) {
            // Find the right-most tile in the row
            AzulTile rightMostTile = null;
            int rightMostTileIndex = -1;

            for (int col1 = playerPatternWall[row].length - 1; col1 >= 0; col1--) {
                AzulTile tile = playerPatternWall[row][col1];
                if (tile != null && tile != AzulTile.Empty) {
                    rightMostTile = tile;
                    rightMostTileIndex = col1;
                    playerPatternWall[row][col1] = AzulTile.Empty;
                    //System.out.println("Row in pattern wall: " + Arrays.toString(playerPatternWall[row]));
                    break;
                }
            }

            if (rightMostTile != null || rightMostTile != AzulTile.Empty) {
                playerWall[row][col] = rightMostTile;
                //System.out.println("Moved " + rightMostTile.getTileType() + " to wall at position " + row + ", " + col);


            }

            // Now, move all other tiles to the lid (except for the right-most one)
            for (int col2 = 0; col2 < playerPatternWall[row].length; col2++) {
                AzulTile tile = playerPatternWall[row][col2];
                if (tile != null && tile != AzulTile.Empty && col2 != rightMostTileIndex) {
                    // Add tile to the lid
                    ags.updateLid(tile, 1);
                    playerPatternWall[row][col2] = AzulTile.Empty;  // Clear the tile from the pattern line
                }
            }
        }
        // If col = -1, then row is full but tile has already been placed in wall
        else{
            //System.out.println("Adding completed row to lid as tile in wall already exists.");
            AzulTile tile = playerPatternWall[row][0];
            int numOfTiles = row  + 1;
            ags.updateLid(tile, numOfTiles);

            Arrays.fill(playerPatternWall[row], AzulTile.Empty);
        }

        //System.out.println("Lid: Key: " + ags.getLid().keySet() + " values: " + ags.getLid().values());
    }



    /**
     * @return Make sure to return an exact <b>deep</b> copy of the object, including all of its variables.
     * Make sure the return type is this class (e.g. GTComponent) and NOT the super class Component.
     * <p>
     * <b>IMPORTANT</b>: This should have the same componentID
     * (using the protected constructor on the Component super class which takes this as an argument).
     * </p>
     * <p>The function should also call the {@link Component#copyComponentTo(Component)} method, passing in as an
     * argument the new copy you've made.</p>
     * <p>If all variables in this class are final or effectively final, then you can just return <code>`this`</code>.</p>
     */
    @Override
    public AzulPlayerBoard copy() {
        AzulPlayerBoard copy = new AzulPlayerBoard(componentID);
        copyComponentTo(copy);

        int boardSize = playerWall.length;
        copy.playerWall = new AzulTile[boardSize][boardSize];
        copy.playerPatternWall = new AzulTile[boardSize][];
        copy.playerScoreTrack = Arrays.copyOf(playerScoreTrack, playerScoreTrack.length);
        copy.playerFloorLine = Arrays.copyOf(playerFloorLine, playerFloorLine.length);

        for (int i = 0; i < boardSize; i++) {
            copy.playerWall[i] = Arrays.copyOf(playerWall[i], boardSize);

            copy.playerPatternWall[i] = Arrays.copyOf(playerPatternWall[i], playerPatternWall[i].length);
//            copy.playerPatternWall[i] = new AzulTile[playerPatternWall[i].length];
            //System.arraycopy(playerPatternWall[i], 0, copy.playerPatternWall[i], 0, playerPatternWall[i].length);
        }

        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AzulPlayerBoard)) return false;
        AzulPlayerBoard that = (AzulPlayerBoard) o;
        return Arrays.deepEquals(playerWall, that.playerWall) &&
                Arrays.deepEquals(playerPatternWall, that.playerPatternWall) &&
                Arrays.equals(playerScoreTrack, that.playerScoreTrack) &&
                Arrays.equals(playerFloorLine, that.playerFloorLine);
    }

    @Override
    public int hashCode() {
        int result = Arrays.deepHashCode(playerWall);
        result = 31 * result + Arrays.deepHashCode(playerPatternWall);
        result = 31 * result + Arrays.hashCode(playerScoreTrack);
        result = 31 * result + Arrays.hashCode(playerFloorLine);
        return result;
    }

    public boolean isFloorLineOccupied(int i) {
        return playerFloorLine[i] != null && playerFloorLine[i] != AzulTile.Empty;
    }

    public void clearFloorLine(AzulGameState ags) {
        for (int col = 0; col < playerFloorLine.length; col++) {
            AzulTile tile = playerFloorLine[col];

            if (playerFloorLine[col] != AzulTile.Empty && playerFloorLine[col] != null) {
                if (playerFloorLine[col] == AzulTile.FirstPlayer) {
                    playerFloorLine[col] = AzulTile.Empty;
                    continue;
                }

                ags.updateLid(tile, 1);

                //System.out.println(tile + " tile has been removed from floorline. Added to lid: " + ags.getLid().toString());
                playerFloorLine[col] = AzulTile.Empty;
            }
        }
    }
}