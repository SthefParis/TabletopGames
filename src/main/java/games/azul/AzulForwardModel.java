package games.azul;

import core.AbstractGameState;
import core.StandardForwardModel;
import core.actions.AbstractAction;
import core.actions.ActionSpace;
import games.azul.actions.PickUpTilesAction;
import games.azul.actions.PlaceTileAction;
import games.azul.components.AzulCenter;
import games.azul.components.AzulFactoryBoard;
import games.azul.components.AzulPlayerBoard;
import games.azul.tiles.AzulTile;

import java.util.*;

/**
 * <p>The forward model contains all the game rules and logic. It is mainly responsible for declaring rules for:</p>
 * <ol>
 *     <li>Game setup</li>
 *     <li>Actions available to players in a given game state</li>
 *     <li>Game events or rules applied after a player's action</li>
 *     <li>Game end</li>
 * </ol>
 */
public class AzulForwardModel extends StandardForwardModel {

    /**
     * Initializes all variables in the given game state. Performs initial game setup according to game rules, e.g.:
     * <ul>
     *     <li>Sets up decks of cards and shuffles them</li>
     *     <li>Gives player cards</li>
     *     <li>Places tokens on boards</li>
     *     <li>...</li>
     * </ul>
     *
     * @param firstState - the state to be modified to the initial game state.
     */
    @Override
    protected void _setup(AbstractGameState firstState) {
        AzulGameState ags = (AzulGameState) firstState;
        AzulParameters params = (AzulParameters) firstState.getGameParameters();

        // Initialise scores for all players, initially 0
        ags.playerScore = new int[firstState.getNPlayers()];
        ags.playersPenalty = new int[firstState.getNPlayers()];

        // Initialise tile counts (bag)
        ags.tileCounts = new HashMap<>();
        ags.tileCounts.put(AzulTile.White, 20);
        ags.tileCounts.put(AzulTile.Orange, 20);
        ags.tileCounts.put(AzulTile.Red, 20);
        ags.tileCounts.put(AzulTile.Black, 20);
        ags.tileCounts.put(AzulTile.Blue, 20);

        // Initialise lid (location tiles will be placed when they are not being used)
        ags.lid = new HashMap<>();
        ags.lid.put(AzulTile.White, 0);
        ags.lid.put(AzulTile.Orange, 0);
        ags.lid.put(AzulTile.Red, 0);
        ags.lid.put(AzulTile.Black, 0);
        ags.lid.put(AzulTile.Blue, 0);

        // Initialise Player Boards
        ags.playerBoards = new ArrayList<>();
        params.initialise(ags.getNPlayers());

        for (int i = 0; i < ags.getNPlayers(); i++) {
            AzulPlayerBoard playerBoard = new AzulPlayerBoard();
            playerBoard.initialise(ags, i);
            playerBoard.setOwnerId(i);
            ags.playerBoards.add(playerBoard);
        }

        // Initialise Factory
        ags.factoryBoards = new ArrayList<>();

        for (int i = 0; i < params.nFactories; i++) {
            AzulFactoryBoard factoryBoard = new AzulFactoryBoard();
            factoryBoard.initialise(params, i);
            factoryBoard.refill(ags);
            ags.factoryBoards.add(factoryBoard);
        }

        // Initialise Center
        ags.center = new AzulCenter();
        ags.center.initialise(ags);

        // First thing to do is pick up tile (Factory Offer)
        ags.setGamePhase(AzulGameState.AzulPhase.FactoryOffer);
    }

    /**
     * r     * @return - List of AbstractAction objects.
     */
    @Override
    protected List<AbstractAction> _computeAvailableActions(AbstractGameState gameState) {
        return _computeAvailableActions(gameState, ActionSpace.Default);
    }

    /**
     * Calculates the list of currently available actions, possibly depending on the game phase.
     *
     * @return - List of AbstractAction objects.
     */
    @Override
    protected List<AbstractAction> _computeAvailableActions(AbstractGameState gameState, ActionSpace space) {
        AzulGameState ags = (AzulGameState) gameState;
        ArrayList<AbstractAction> actions = new ArrayList<>();
        int playerID = ags.getCurrentPlayer();

        if (AzulGameState.AzulPhase.FactoryOffer.equals(ags.getGamePhase())) {
            actions.addAll(pickUpTileFromFactoryAction(ags, playerID));
            actions.addAll(pickUpTileFromCenterAction(ags, playerID));
        } else if (AzulGameState.AzulPhase.PlaceTile.equals(ags.getGamePhase())) {
            actions.addAll(placeTileActions(ags, playerID));
        }

        if (actions.isEmpty()) {
            throw new IllegalStateException("No valid actions available");
        }

        return actions;
    }

    @Override
    protected void _afterAction(AbstractGameState currentState, AbstractAction action) {
        if (currentState.isActionInProgress()) return;

        // Each turn begins with the player picking up a tile
        // after which those tiles will be place on a player board
        AzulGameState ags = (AzulGameState) currentState;

        if (!checkEndOfRound(ags)) {
            if (ags.getGamePhase() == AzulGameState.AzulPhase.FactoryOffer) {
                if (action instanceof PickUpTilesAction) {

                    // Move to the next phase where player places tiles
                    ags.setGamePhase(AzulGameState.AzulPhase.PlaceTile);
                }
            } else if (ags.getGamePhase() == AzulGameState.AzulPhase.PlaceTile) {
                if (action instanceof PlaceTileAction) {
                    int nextPlayer = (ags.getCurrentPlayer() + 1) % ags.getNPlayers();
                    ags.setTurnOwner(nextPlayer);
                    endPlayerTurn(ags, nextPlayer);
                    ags.setGamePhase(AzulGameState.AzulPhase.FactoryOffer);
                }
            }
        } else {
            ags.setGamePhase(AzulGameState.AzulPhase.FactoryOffer);
        }
    }

    /**
     * Generates a list of all valid pick-up tile actions from factories for the current player.
     * @param ags - Game state.
     * @param playerID - ID of the player whose turn it is.
     * @return list of valid pick-up actions.
     */
    private ArrayList<AbstractAction> pickUpTileFromFactoryAction(AzulGameState ags, int playerID) {
        ArrayList<AbstractAction> actions = new ArrayList<>();

        List<AzulFactoryBoard> factoryBoards = ags.getAllFactoryBoards();

        boolean allEmpty = factoryBoards.stream().allMatch(AzulFactoryBoard::isEmpty);

        if (allEmpty) {
            return actions;
        }

        // Create the respective actions for each tile on the factory boards
        for (AzulFactoryBoard factoryBoard : factoryBoards) {
            // Get unique tile types in factory
            Set<AzulTile> availableTileTypes = factoryBoard.getTileTypes();

            // Add a pick-up action for each tile type present in the factory
            for (AzulTile availableTile : availableTileTypes) {
                if (availableTile != AzulTile.Empty) {
                    int factoryIndex = ags.getAllFactoryBoards().indexOf(factoryBoard);
                    PickUpTilesAction action = new PickUpTilesAction(playerID, availableTile, factoryIndex);
                    actions.add(action);
                }
            }
        }

        return actions;
    }

    /**
     * Generates a list of valid pick-up actions from the center for the current player.
     * @param ags - Game state.
     * @param playerID - ID of the player whose turn it is.
     * @return list of center pick-up actions.
     */
    private ArrayList<AbstractAction> pickUpTileFromCenterAction(AzulGameState ags, int playerID) {
        ArrayList<AbstractAction> actions = new ArrayList<>();
        AzulCenter center = ags.getCenter();

        Set<AzulTile> availableTileTypes = center.getTileTypes();

        for (AzulTile availableTile : availableTileTypes) {
            if (availableTile != AzulTile.Empty && availableTile != AzulTile.FirstPlayer && availableTile != null) {
                PickUpTilesAction action = new PickUpTilesAction(playerID, availableTile, -1);
                actions.add(action);
            }
        }

        return actions;
    }

    /**
     * Generates a list of valid tile placement actions for the current player.
     * @param ags - Game state.
     * @param playerID - ID of the player whose turn it is.
     * @return list of tile placement actions.
     */
    private ArrayList<AbstractAction> placeTileActions(AzulGameState ags, int playerID) {
        ArrayList<AbstractAction> actions = new ArrayList<>();
        AzulPlayerBoard playerBoard = ags.getPlayerBoard(playerID);

        for (int row = 0; row < playerBoard.playerPatternWall.length; row++) { // Iterate rows
            if (!playerBoard.isPatternLineRowFull(row)) { // Ensure the row is valid
                PlaceTileAction action = new PlaceTileAction(
                        playerID, ags.getTilesPicked(), ags.getNumOfTilesPicked(), row
                );
                actions.add(action);
            }
        }

        // All pattern lines are full, so tiles should be placed in floor line
        if (actions.isEmpty()) {
            PlaceTileAction action = new PlaceTileAction(playerID, ags.getTilesPicked(), ags.getNumOfTilesPicked(), -1);
            actions.add(action);
        }

        return actions;
    }

    /**
     * Checks whether the round should end (all factories and center are empty).
     * If so, proceeds to end-round tasks: wall tiling, scoring, penalty and game end check.
     * @param ags - Game state.
     * @return true if the round ends (and possibly the game), false otherwise
     */
    public boolean checkEndOfRound(AzulGameState ags) {
        if (isFactoriesAndCentreEmpty(ags)){

            endRound(ags);
            // Wall tile
            executeWallTilingPhase(ags);

            for (int playerID = 0; playerID < ags.getNPlayers(); playerID++){
                calculateFloorLinePenalty(ags, ags.getPlayerBoard(playerID), playerID);
                ags.getPlayerBoard(playerID).clearFloorLine(ags);
            }

            // Check if game has ended
            if (checkEndOfGame(ags)) {
                for (int playerID = 0; playerID < ags.getNPlayers(); playerID++) {
                    AzulPlayerBoard playerBoard = ags.getPlayerBoard(playerID);
                    calculateBonusPoints(ags, playerBoard, playerID);
                }
                return true;
            }

            // Prep for next round if game has not ended
            executePrepNextRound(ags);

            return true;
        }

        return false;
    }

    /**
     * Checks if the game has ended by checking for any completed wall rows.
     * @param ags - Game state.
     * @return true if the game should end, false otherwise.
     */
    private boolean checkEndOfGame(AzulGameState ags){
        for (int playerID = 0; playerID < ags.getNPlayers(); playerID++) {
            AzulPlayerBoard playerBoard = ags.getPlayerBoard(playerID);

            // Check if any row in the player's wall is complete
            for (int row = 0; row < playerBoard.getPlayerWall().length; row++) {
                if (ags.isWallRowComplete(playerID, row)) {
                    endGame(ags);
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Calculates and applies score after a tile is placed on the wall.
     * @param ags - Game state.
     * @param playerID - ID of player who has just completed a wall tiling action.
     * @param row - Row where tile has been placed.
     * @param col - Column where tile has been placed.
     */
    private void executeScoring(AzulGameState ags, int playerID, int row, int col) {

        AzulPlayerBoard playerBoard = ags.getPlayerBoard(playerID);
        int score = (int) ags.getGameScore(playerID);

        // Find tile that has just been placed and score it
        if (playerBoard.getPlayerWall()[row][col] != AzulTile.Empty && playerBoard.getPlayerWall()[row][col] != null) {
            int tileScore = calculateTileScore(ags, playerBoard, row, col);
            score += tileScore;
        }

        ags.setPlayerScore(score, playerID);
    }

    /**
     * Calculates the score of a tile just placed on the wall based on adjacent tiles.
     * @param ags - Game state.
     * @param playerBoard - The player's board.
     * @param row - Row of the placed tile.
     * @param col - Column of the placed tile.
     * @return the score for the tile.
     */
    private int calculateTileScore(AzulGameState ags, AzulPlayerBoard playerBoard, int row, int col) {
        AzulParameters params = (AzulParameters) ags.getGameParameters();
        int score = params.getPlacingTilePoints();  // Scores one for placing tile down

        // Count connected tiles in the row
        int rowCount = 1 + countTilesInDirection(playerBoard, row, col, 0, -1) // Left
                + countTilesInDirection(playerBoard, row, col, 0, 1); // Right

        // Count connected tiles in the column
        int colCount = 1 + countTilesInDirection(playerBoard, row, col, -1, 0) // Up
                + countTilesInDirection(playerBoard, row, col, 1, 0); // Down

        // If part of a line, score the entire line
        score += (rowCount > 1 ? rowCount - 1 : 0) + (colCount > 1 ? colCount - 1 : 0);

        // If intersection is formed, tile is scored twice
        if (rowCount > 1 && colCount > 1) score++;

        return score;
    }

    /**
     * Recursively counts adjacent tiles in a given direction from a starting tile.
     * @param playerBoard - The player's board.
     * @param row - Starting row.
     * @param col - Starting column.
     * @param dRow - Row direction increment.
     * @param dCol - Column direction increment.
     * @return the number of adjacent tiles in that direction.
     */
    private int countTilesInDirection(AzulPlayerBoard playerBoard, int row, int col, int dRow, int dCol){
        int count = 0;
        int newRow = row + dRow;
        int newCol = col + dCol;

        while (newRow >= 0 && newRow < playerBoard.getPlayerWall().length &&
                newCol >= 0 && newCol < playerBoard.getPlayerWall()[0].length &&
                playerBoard.getPlayerWall()[newRow][newCol] != AzulTile.Empty &&
                playerBoard.getPlayerWall()[newRow][newCol] != null ) {
            count++;
            newRow += dRow;
            newCol += dCol;
        }
        return count;
    }

    /**
     * Calculates and applies floor line penalty points to a player.
     * @param ags - Game state.
     * @param playerBoard - The player's board.
     * @param playerID - ID of the player being penalized.
     */
    private void calculateFloorLinePenalty(AzulGameState ags, AzulPlayerBoard playerBoard, int playerID) {
        AzulParameters params = (AzulParameters) ags.getGameParameters();
        int[] penaltyValues = params.floorPenalties;
        int penalty = 0;

        for (int i = 0; i < playerBoard.playerFloorLine.length; i++) {
            if (playerBoard.isFloorLineOccupied(i)) {
                penalty += penaltyValues[i];
            }
        }

        ags.subtractPlayerPoint(playerID, penalty);
    }

    /**
     * Calculates and adds bonus points to a player at game end.
     * Bonuses include completed rows, columns, and full sets of tile colors.
     * @param ags - Game state.
     * @param playerBoard - The player's board.
     * @param playerID - ID of the player receiving bonuses.
     */
    private void calculateBonusPoints(AzulGameState ags, AzulPlayerBoard playerBoard, int playerID) {
        AzulParameters params = (AzulParameters) ags.getGameParameters();

        // Award 2 points for any completed rows
        for (int row = 0; row < playerBoard.getPlayerWall().length; row++) {
            if (ags.isWallRowComplete(playerID, row)) {
                ags.addPlayerPoint(playerID, params.getRowBonusPoints());
            }
        }

        // Award 7 points for any completed columns
        for (int col = 0; col < playerBoard.getPlayerWall().length; col++) {
            if (ags.isWallColComplete(playerID, col)) {
                ags.addPlayerPoint(playerID, params.getColumnBonusPoints());
            }
        }

        Map<AzulTile, Integer> colorCount = new HashMap<>();

        for (int row = 0; row < playerBoard.getPlayerWall().length; row++) {
            for (int col = 0; col < playerBoard.getPlayerWall().length; col++) {
                AzulTile tile = playerBoard.getPlayerWall()[row][col];
                if (tile != AzulTile.Empty && tile != null) {
                    colorCount.put(tile, colorCount.getOrDefault(tile, 0) + 1);
                }
            }
        }

        // Award 10 points for each colour that appears exactly 5 times
        for (Map.Entry<AzulTile, Integer> entry : colorCount.entrySet()) {
            if (entry.getValue() == 5) {
                ags.addPlayerPoint(playerID, params.getColorSetBonusPoints());
                //intln("Player " + playerID + " has placed all 5 tiles of colour " + entry.getKey() + " and gained 10 points.");
            }
        }
    }

    /**
     * Performs wall tiling phase for all players.
     * Moves completed pattern line tiles to the wall and scores them.
     * @param ags - Game state.
     */
    private void executeWallTilingPhase(AzulGameState ags) {
        AzulParameters params = (AzulParameters) ags.getGameParameters();
        ags.setGamePhase(AzulGameState.AzulPhase.WallTiling);
        for (int playerID = 0; playerID < ags.getNPlayers(); playerID++) {
            AzulPlayerBoard playerBoard = ags.getPlayerBoard(playerID);

            // Check all rows on the player's wall
            for (int row = 0; row < playerBoard.playerPatternWall.length; row++) {
                AzulTile tile = playerBoard.getTileAt(row);
                int col = params.getTileColPositionInRow(row, tile);

                if (playerBoard.isPatternLineRowFull(row) && playerBoard.isPositionEmpty(row, col)) {
                    if (col != -1) {
                        boolean tilePlaced = playerBoard.placeTileInWall(ags, tile, row, col);

                        if (tilePlaced) {
                            executeScoring(ags, playerID, row, col);
                        }
                    }
                }
                if (playerBoard.isPatternLineRowFull(row) && !playerBoard.isPositionEmpty(row, col)) {
                    playerBoard.clearRowOnPatternLine(ags, row, -1);
                }
            }
        }
    }

    /**
     * Prepares the game state for the next round by refilling factories and resetting flags.
     * @param ags - Game state.
     */
    private void executePrepNextRound(AzulGameState ags) {
        ags.setGamePhase(AzulGameState.AzulPhase.PrepNextRnd);
        for (AzulFactoryBoard factory : ags.getAllFactoryBoards()) {
            factory.refill(ags);
        }

        ags.getCenter().addFirstPlayerTile();
        ags.setHasPickedFromCenter(false);
    }

    /**
     * Checks if all factory boards and the center are empty.
     * @param ags - Game state.
     * @return true if all factories are empty, false otherwise.
     */
    private boolean isFactoriesAndCentreEmpty(AzulGameState ags) {
        // Checks if all factory boards are empty
        for (AzulFactoryBoard factory : ags.getAllFactoryBoards()) {
            if (!factory.isEmpty()) {
                return false;
            }
        }

        // Check if center is empty
        return ags.getCenter().isEmpty();
    }
}