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
import games.azul.components.AzulWallPattern;
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

        // Initialise Wall
        AzulWallPattern wall = new AzulWallPattern();
        wall.initialise(params);

        ags.setWall(wall);

        // Initialise Factory
        //System.out.println("Number of factories: " + params.nFactories);
        ags.factoryBoards = new ArrayList<>();

        for (int i = 0; i < params.nFactories; i++) {
            AzulFactoryBoard factoryBoard = new AzulFactoryBoard();
            factoryBoard.initialise(params, i);
            factoryBoard.refill(ags);

//            System.out.println("Created factory board: " + i + ": " + Arrays.toString(factoryBoard.factoryBoard));
            ags.factoryBoards.add(factoryBoard);
        }

        // Initialise Center
        ags.center = new AzulCenter();
        ags.center.initialise(ags);

        // First thing to do is pick up tile (Factory Offer)
        ags.setGamePhase(AzulGameState.AzulPhase.FactoryOffer);
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
//                    System.out.println("Player " + ags.getCurrentPlayer() + " picked up tiles.");

                    // Move to the next phase where player places tiles
                    ags.setGamePhase(AzulGameState.AzulPhase.PlaceTile);
                }
            } else if (ags.getGamePhase() == AzulGameState.AzulPhase.PlaceTile) {
                if (action instanceof PlaceTileAction) {
//                    System.out.println("Player " + ags.getCurrentPlayer() + " placed tiles.");
                    int nextPlayer = (ags.getCurrentPlayer() + 1) % ags.getNPlayers();
                    ags.setTurnOwner(nextPlayer);
                    endPlayerTurn(ags, nextPlayer);
                    ags.setGamePhase(AzulGameState.AzulPhase.FactoryOffer);
                }
            } else if (ags.getGamePhase() == AzulGameState.AzulPhase.WallTiling) {
                // Check if all players have completed their wall-tiling phase
                boolean allPlayersCompleted = true;
                for (int i = 0; i < ags.getNPlayers(); i++) {
                    if (!isPlayerWallTilingComplete(ags, i)) {
                        allPlayersCompleted = false;
                        break;
                    }
                }

                // If all players have finished, move to the next phase
                if (allPlayersCompleted) {
//                    System.out.println("All players have completed wall tiling. Moving to next phase.");
                    ags.setGamePhase(AzulGameState.AzulPhase.PrepNextRnd);
                }
            }
        }
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
//            System.out.println("In factory offer");
            actions.addAll(pickUpTileActions(ags, playerID));
            actions.addAll(pickUpTileFromCenterAction(ags, playerID));
        } else if (AzulGameState.AzulPhase.PlaceTile.equals(ags.getGamePhase())) {
//            System.out.println("In place tile");
            actions = placeTileActions(ags, playerID);
        } else {
//            System.out.println("No action available");
        }

        if (actions.isEmpty()) {
            throw new IllegalStateException("No valid actions available");
        }

        return actions;
    }


    private boolean isPlayerWallTilingComplete(AzulGameState ags, int playerID) {
        AzulPlayerBoard playerBoard = ags.getPlayerBoard(playerID);

        // Check all rows on the player's wall, and if any row is not fully filled, return false
        for (int row = 0; row < playerBoard.playerPatternWall.length; row++) {
            if (playerBoard.isRowFull(row)) { // && !playerBoard.isRowTiled(row)
                return false;  // The player still has unfinished tiling on this row
            }
        }

        // If all rows are tiled, return true
        return true;
    }

    public boolean checkEndOfRound(AzulGameState ags) {
        if (isFactoriesAndCentreEmpty(ags)){
//            System.out.println("Round is ending: Factories and center are empty");

            // Wall tile
            executeWallTilingPhase(ags);

            for (int playerID = 0; playerID < ags.getNPlayers(); playerID++){
                calculateFloorLinePenalty(ags, ags.getPlayerBoard(playerID), playerID);
                ags.getPlayerBoard(playerID).clearFloorLine(ags);
            }

            endRound(ags);

            // Check if game has ended
            if (checkEndOfGame(ags)) {
                for (int playerID = 0; playerID < ags.getNPlayers(); playerID++) {
                    AzulPlayerBoard playerBoard = ags.getPlayerBoard(playerID);
                    calculateBonusPoints(ags, playerBoard, playerID);
                }
                return true;
            }

            // Prep for next round if game has not ended
            executePrepNxtRound(ags);

            return true;
        }

        return false;
    }

    /**
     *
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
            int tileScore = calculateTileScore(playerBoard, row, col);
            score += tileScore;
//            System.out.println("Tile at (" + row + "," + col + ") added " + tileScore + " points. Total: " + score);
        }

//        System.out.println("PlayerID in scoring: " + playerID);
        ags.setPlayerScore(score, playerID);
//        System.out.println("Score after placing tile in row " + row + " col " + col + " : " + score);
    }

    private int calculateTileScore(AzulPlayerBoard playerBoard, int row, int col) {
        int score = 1;  // Scores one for placing tile down

        // Count connected tiles in the row
        int rowCount = 1 + countTilesInDirection(playerBoard, row, col, 0, -1) // Left
                + countTilesInDirection(playerBoard, row, col, 0, 1); // Right

//        // Count connected tiles in the column
        int colCount = 1 + countTilesInDirection(playerBoard, row, col, -1, 0) // Up
                + countTilesInDirection(playerBoard, row, col, 1, 0); // Down

        // If part of a line, score the entire line
        score += (rowCount > 1 ? rowCount - 1 : 0) + (colCount > 1 ? colCount - 1 : 0);

        // If intersection is formed, tile is scored twice
        if (rowCount > 1 && colCount > 1) score++;

//        System.out.println("rowCount calculated: " + rowCount);
//        System.out.println("colCount calculated: " + colCount);
//        System.out.println("Score: " + score);

        return score;
    }

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
//            System.out.println("new row: " + newRow + " new col: " + newCol);
        }
//        System.out.println("Count tiles in direction: " + count);
        return count;
    }

    private void calculateFloorLinePenalty(AzulGameState ags, AzulPlayerBoard playerBoard, int playerID) {
        AzulParameters params = (AzulParameters) ags.getGameParameters();
        int[] penaltyValues = params.floorPenalties;
        int penalty = 0;

        for (int i = 0; i < playerBoard.playerFloorLine.length; i++) {
            if (playerBoard.isFloorLineOccupied(i)) {
                penalty += penaltyValues[i];
            }
        }
        //System.out.println("penalty for player " + playerID + " : " + penalty);
        int playerScore = (int) ags.getGameScore(playerID);
        //System.out.println("Player score before deducted penalty: " + playerScore);
        playerScore = playerScore - penalty;
        //System.out.println("Player score after deducted penalty: " + playerScore);
//        ags.setPlayerScore(playerScore, playerID);
        ags.subtractPlayerPoint(playerID, penalty);
    }

    private boolean checkEndOfGame(AzulGameState ags){
        for (int playerID = 0; playerID < ags.getNPlayers(); playerID++) {
            AzulPlayerBoard playerBoard = ags.getPlayerBoard(playerID);

            // Check if any row in the player's wall is complete
            for (int row = 0; row < playerBoard.getPlayerWall().length; row++) {
                if (ags.isRowComplete(playerID, row)) {
                    //System.out.println("Game ends: Player" + playerID + " has completed a row: " + Arrays.deepToString(ags.getPlayerBoard(playerID).playerWall));
                    endGame(ags);
                    return true;
                }
            }
        }
        return false;
    }

    private void calculateBonusPoints(AzulGameState ags, AzulPlayerBoard playerBoard, int playerID) {
        //System.out.println("BONUS POINTS!!!");
        //System.out.println("Player " + playerID + " score before bonus: " + ags.getGameScore(playerID));
        // Award 2 points for any completed rows
        for (int row = 0; row < playerBoard.getPlayerWall().length; row++) {
            if (ags.isRowComplete(playerID, row)) {
                ags.addPlayerPoint(playerID, 2);
                //System.out.println("Player " + playerID + " has completed row " + row + " and gained 2 points.");
            }
        }

        // Award 7 points for any completed columns
        for (int col = 0; col < playerBoard.getPlayerWall().length; col++) {
            if (ags.isColComplete(playerID, col)) {
                ags.addPlayerPoint(playerID, 7);
                //System.out.println("Player " + playerID + " has completed col " + col + " and gained 7 points.");
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
                ags.addPlayerPoint(playerID, 10);
                //intln("Player " + playerID + " has placed all 5 tiles of colour " + entry.getKey() + " and gained 10 points.");
            }
        }
        //System.out.println("Player " + playerID + " points after bonus: " + ags.getGameScore(playerID));
    }

    private void executeWallTilingPhase(AzulGameState ags) {

        for (int playerID = 0; playerID < ags.getNPlayers(); playerID++) {
            AzulPlayerBoard playerBoard = ags.getPlayerBoard(playerID);
            AzulWallPattern wall = ags.getWall();

            // Check all rows on the player's wall
            for (int row = 0; row < playerBoard.playerPatternWall.length; row++) {
                AzulTile tile = playerBoard.getTileAt(row);
                int col = wall.getTileColPositionInRow(row, tile);

                if (playerBoard.isRowFull(row) && playerBoard.isPositionEmpty(row, col)) {
                    if (col != -1) {
                        // Automatically place the tile on the wall without an explicit action
                        boolean tilePlaced = playerBoard.placeTileInWall(ags, tile, row, col);
//                        executeScoring(ags, playerID, row, col);

                        if (tilePlaced) {
                            //System.out.println("Player " + playerID + " placed " + tile + " tile on their wall, row: " + row + " col: " + col);
                            executeScoring(ags, playerID, row, col);
                        } else {
                            //System.out.println("Tile could not be placed on player " + playerID + "'s wall.");
                        }
                    } else {
                        //System.out.println("Error: tile colour not found in player " + playerID + "'s wall.");
                    }
                }
                if (playerBoard.isRowFull(row) && !playerBoard.isPositionEmpty(row, col)) {
                    playerBoard.clearRowOnPatternLine(ags, row, -1);
                }
            }
        }

//        ags.setHasPickedFromCenter(false);
        //System.out.println("Tiles remaining in bag: " + ags.getAllTileCounts().keySet() + " " + ags.getAllTileCounts().values());
    }

    private void executePrepNxtRound(AzulGameState ags) {
        //System.out.println("Preparing next round!");
        //System.out.println("Bag: " + ags.tileCounts);
        //System.out.println("Lid: " + ags.getLid());

        for (AzulFactoryBoard factory : ags.getAllFactoryBoards()) {
            factory.refill(ags);
            //intln("Factory after refill: " + Arrays.toString(factory.factoryBoard));
        }

        ags.getCenter().addFirstPlayerTile();
        ags.setHasPickedFromCenter(false);
    }

    private ArrayList<AbstractAction> pickUpTileActions(AzulGameState ags, int playerID) {
        ArrayList<AbstractAction> actions = new ArrayList<>();

        List<AzulFactoryBoard> factoryBoards = ags.getAllFactoryBoards();
        //System.out.println("Factory boards at start of pickUpTileActions: " + factoryBoards.size());

        boolean allEmpty = factoryBoards.stream().allMatch(AzulFactoryBoard::isEmpty);

        if (allEmpty) {
            //System.out.println("Factory boards is empty!");
            return actions;
        } else {
            //System.out.println("Factory boards is not empty!");
        }



        // Create the respective actions for each tile on the factory boards
        for (AzulFactoryBoard factoryBoard : factoryBoards) {
            // Get unique tile types in factory
            Set<AzulTile> availableTileTypes = factoryBoard.getTileTypes();
            //System.out.println("Available tile types: " + availableTileTypes);

            // Add a pick-up action for each tile type present in the factory
            for (AzulTile availableTile : availableTileTypes) {
                if (availableTile != AzulTile.Empty) {
                    int factoryIndex = ags.getAllFactoryBoards().indexOf(factoryBoard);
                    PickUpTilesAction action = new PickUpTilesAction(playerID, availableTile, factoryIndex);
//                    System.out.println("Adding action: " + action);
                    actions.add(action);
                }
            }
        }

        return actions;
    }

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

    private ArrayList<AbstractAction> pickUpTileFromCenterAction(AzulGameState ags, int playerID) {
        ArrayList<AbstractAction> actions = new ArrayList<>();
        AzulCenter center = ags.getCenter();

        Set<AzulTile> availableTileTypes = center.getTileTypes();
        //intln("Available tiles in center: " + availableTileTypes);

        for (AzulTile availableTile : availableTileTypes) {
            if (availableTile != AzulTile.Empty && availableTile != AzulTile.FirstPlayer && availableTile != null) {
                PickUpTilesAction action = new PickUpTilesAction(playerID, availableTile, -1);
                actions.add(action);
            }
        }

        return actions;
    }

    private ArrayList<AbstractAction> placeTileActions(AzulGameState ags, int playerID) {
        ArrayList<AbstractAction> actions = new ArrayList<>();
        AzulPlayerBoard playerBoard = ags.getPlayerBoard(playerID);
        //intln("Player id: " + playerID);
        //System.out.println("Player board: " + Arrays.deepToString(playerBoard.playerPatternWall));

        for (int row = 0; row < playerBoard.playerPatternWall.length; row++) { // Iterate rows
            if (!playerBoard.isRowFull(row)) { // Ensure the row is valid
                PlaceTileAction action = new PlaceTileAction(
                        playerID, ags.getTilesPicked(), ags.getNumOfTilesPicked(), row
                );
                actions.add(action);
            }
        }

        return actions;
    }
}
