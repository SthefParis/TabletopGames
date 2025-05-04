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
     *
     * @param ags - Game state, used to get the game parameters.
     * @param playerID - ID of the player this board belongs to.
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

        for (int i = 0; i < boardSize; i++) {
            playerPatternWall[i] = new AzulTile[i + 1];
            for (int j = 0; j <= i; j++) {
                playerPatternWall[i][j] = AzulTile.Empty;
            }
        }
    }

    /**
     * Gets the player's Wall.
     *
     * @return the player's wall.
     */
    public AzulTile[][] getPlayerWall(){
        return playerWall;
    }

    /**
     * Places a tile in the players wall.
     *
     * @param ags - Game state.
     * @param tile - Tile that needs to be placed.
     * @param row - Row that tile needs to be placed on.
     * @param col - Column that the tile must be placed in according to the pattern.
     * @return boolean - Returns true if the tile was placed successfully, false otherwise.
     */
    public boolean placeTileInWall(AzulGameState ags, AzulTile tile, int row, int col) {
        if (row < 0 || row >= playerWall.length) return false;

        if (isPositionEmpty(row, col)) {
            playerWall[row][col] = tile;
            clearRowOnPatternLine(ags, row, col);
            return true;
        }

        return false;
    }

    /**
     * Checks if a specific position in the wall is empty.
     *
     * @param row - Row to check.
     * @param col - Column to check.
     * @return boolean - Returns true if the position is empty, false otherwise.
     */
    public boolean isPositionEmpty(int row, int col) {
        return playerWall[row][col] == AzulTile.Empty || playerWall[row][col] == null;
    }

    /**
     * Places a tile in the pattern wall if the row is valid for the tile type.
     *
     * @param ags - Game state.
     * @param tile - Tile that needs to be placed.
     * @param row - Row in which the tile needs to be placed.
     * @return boolean - Returns true if the tile was placed successfully, false otherwise.
     */
    public boolean placeTileInPatternLine(AzulGameState ags, AzulTile tile, int row){
        if (row < 0 || row >= playerPatternWall.length) return false;

        if (hasBeenTiled(tile, row)) {
            // Find an alternative row
            row = findValidRow(tile);
            if (row == -1) {
                placeTileInFloorLine(ags, tile);
                return true;
            }}

        if (!isRowValid(row, tile)) {
            // Find alternative row
            row = findValidRow(tile);
            if (row == -1) {
                placeTileInFloorLine(ags, tile);
                return true;
            }
        }

        // Place tile in the lowest available slot in the selected row
        for (int i = playerPatternWall[row].length - 1; i >= 0; i--){
            if (playerPatternWall[row][i] == null || playerPatternWall[row][i] == AzulTile.Empty){
                playerPatternWall[row][i] = tile;
                ags.setLastPlacedRow(row);
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if a given row is valid for placing a specific tile.
     *
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
     *
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
     *
     * @param ags - Game state.
     * @param tile - Tile type that needs to be placed.
     */
    public void placeTileInFloorLine(AzulGameState ags, AzulTile tile) {
        // Check if floor line is full
        for (int i = 0; i < playerFloorLine.length; i++) {
            if (playerFloorLine[i] == null || playerFloorLine[i] == AzulTile.Empty) {
                // If there's space, place the tile in the floor line
                playerFloorLine[i] = tile;
                return;
            }
        }

        // If floor line is full, place the tile in the lid
        ags.updateLid(tile, 1);  // Add tile to the lid
        ags.setLastPlacedRow(-1);
    }

    /**
     * Checks if a given pattern line is full.
     *
     * @param row - Row it will check.
     * @return true if the pattern line is full, false otherwise.
     */
    public boolean isPatternLineRowFull(int row) {
        // Check if row is full
        for (AzulTile tile: playerPatternWall[row]){
            if (tile == AzulTile.Empty || tile == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the given tile has already been placed in the wall at a specific row.
     *
     * @param tile - Tile that will be checked.
     * @param row - Row where tile is being placed.
     * @return true if tile has been placed, false otherwise.
     */
    public boolean hasBeenTiled(AzulTile tile, int row) {
        for (int col = 0; col < playerWall[row].length; col++) {
            if (playerWall[row][col] == tile) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the tile type placed in a row (used to identify row colour).
     *
     * @param row - Row where tile colour is checked.
     * @return AzulTile of the tile that is on that row.
     */
    public AzulTile getTileAt(int row) {
        if (row >= 0 && row < playerPatternWall.length && playerPatternWall[row].length > 0) {
            // Since all tiles in this row are the same, return the tile in the first column.
            return playerPatternWall[row][0];
        }
        return AzulTile.Empty;
    }

    /**
     * Function clears row in Pattern Line once row is full and tile has been moved to the wall.
     *
     * @param ags - Game state.
     * @param row - Row that is full on pattern line
     */
    public void clearRowOnPatternLine(AzulGameState ags, int row, int col) {
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
                    break;
                }
            }

            playerWall[row][col] = rightMostTile;


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
            AzulTile tile = playerPatternWall[row][0];
            int numOfTiles = row  + 1;
            ags.updateLid(tile, numOfTiles);

            Arrays.fill(playerPatternWall[row], AzulTile.Empty);
        }

    }

    /**
     * Checks if a specified index in the floor line is occupied.
     *
     * @param i - The cell that should be checked.
     * @return true if that cell is occupied, false otherwise.
     */
    public boolean isFloorLineOccupied(int i) {
        return playerFloorLine[i] != null && playerFloorLine[i] != AzulTile.Empty;
    }

    /**
     * Clears all tiles from the floor  line, moving them to the lid.
     *
     * @param ags - Game state.
     */
    public void clearFloorLine(AzulGameState ags) {
        for (int col = 0; col < playerFloorLine.length; col++) {
            AzulTile tile = playerFloorLine[col];

            if (playerFloorLine[col] != AzulTile.Empty && playerFloorLine[col] != null) {
                if (playerFloorLine[col] == AzulTile.FirstPlayer) {
                    playerFloorLine[col] = AzulTile.Empty;
                    continue;
                }

                ags.updateLid(tile, 1);

                playerFloorLine[col] = AzulTile.Empty;
            }
        }
    }

    /**
     * Checks if a particular row in the player's wall is complete.
     *
     * @param row - The row to check.
     * @return true if the entire row has non-empty tiles, false otherwise.
     */
    public boolean isWallRowComplete(int row) {
        int wallLength = playerWall.length;
        for (int col = 0; col < wallLength; col++) {
            if (playerWall[row][col] == AzulTile.Empty || playerWall[row][col] == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the specified column in the player's wall is completely filled with tiles.
     *
     * @param col - The column to check.
     * @return true if the entire column has non-empty tiles, false otherwise.
     */
    public boolean isWallColComplete(int col) {
        for (int row = 0; row < playerWall.length; row++) {
            if (playerWall[row][col] == AzulTile.Empty || playerWall[row][col] == null) {
                return false;
            }
        }
        return true;
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
}