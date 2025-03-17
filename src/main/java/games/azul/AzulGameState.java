package games.azul;

import core.AbstractGameState;
import core.AbstractParameters;
import core.components.Component;
import core.interfaces.IGamePhase;
import games.GameType;
import games.azul.components.AzulCenter;
import games.azul.components.AzulFactoryBoard;
import games.azul.components.AzulPlayerBoard;
import games.azul.components.AzulWallPattern;
import games.azul.tiles.AzulTile;

import java.util.*;

/**
 * <p>The game state encapsulates all game information. It is a data-only class, with game functionality present
 * in the Forward Model or actions modifying the state of the game.</p>
 * <p>Most variables held here should be {@link Component} subclasses as much as possible.</p>
 * <p>No initialisation or game logic should be included here (not in the constructor either). This is all handled externally.</p>
 * <p>Computation may be included in functions here for ease of access, but only if this is querying the game state information.
 * Functions on the game state should never <b>change</b> the state of the game.</p>
 */
public class AzulGameState extends AbstractGameState {

    enum AzulPhase implements IGamePhase {
        FactoryOffer,
        PlaceTile,
        WallTiling,
        PrepNextRnd
    }

    List<AzulFactoryBoard> factoryBoards;
    List<AzulPlayerBoard> playerBoards;
    AzulCenter center;
    HashMap<AzulTile, Integer> tileCounts;
    HashMap<AzulTile, Integer> lid;
    AzulWallPattern wall;

    private AzulTile pickedTile;
    private int numOfTilesPicked;
    private boolean hasPickedFromCenter = false;
    int[] playerScore;

    /**
     * @param gameParameters - game parameters.
     * @param nPlayers       - number of players in the game
     */
    public AzulGameState(AbstractParameters gameParameters, int nPlayers) {
        super(gameParameters, nPlayers);
    }

    public List<AzulFactoryBoard> getAllFactoryBoards(){ return factoryBoards; }
    public AzulFactoryBoard getFactory(int factoryID){ return factoryBoards.get(factoryID); }

    public List<AzulPlayerBoard> getAllPlayerBoards(){ return playerBoards; }
    public AzulPlayerBoard getPlayerBoard(int playerID){ return playerBoards.get(playerID); }

    public void setPickedTile(AzulTile pickedTile) { this.pickedTile = pickedTile; }
    public AzulTile getPickedTile() { return pickedTile; }
    public void setNumOfTilesPicked(int numOfTilesPicked) { this.numOfTilesPicked = numOfTilesPicked; }
    public int getNumOfTilesPicked() { return numOfTilesPicked; }

    public AzulCenter getCenter(){ return center; }

    public AzulTile[][] getWallPattern() { return wall.patternPlayerBoard; }
    public AzulWallPattern getWall() { return wall; }
    public void setWall(AzulWallPattern wall) { this.wall = wall; }
    public AzulTile getTileInWall(int row, int col) { return wall.patternPlayerBoard[row][col]; }

    public void updateTileCount(AzulTile tile, int count) {
        tileCounts.put(tile, tileCounts.getOrDefault(tile, 0) + count);
    }
    public void updateAllTileCounts(HashMap<AzulTile, Integer> tileCounts) { this.tileCounts = tileCounts; }
    public int getTileCount(AzulTile tile) { return tileCounts.getOrDefault(tile, 0); }
    public HashMap<AzulTile, Integer> getAllTileCounts() { return tileCounts; }

    public void updateLid(AzulTile tile, int count) {
        lid.put(tile, lid.getOrDefault(tile, 0) + count);
    }
    public HashMap<AzulTile, Integer> getLid() { return lid; }

    public void EmptyLid() {
        lid.put(AzulTile.White, 0);
        lid.put(AzulTile.Orange, 0);
        lid.put(AzulTile.Red, 0);
        lid.put(AzulTile.Black, 0);
        lid.put(AzulTile.Blue, 0);
    }

    public void setHasPickedFromCenter(boolean hasPickedFromCenter) { this.hasPickedFromCenter = hasPickedFromCenter; }
    public boolean hasPickedFromCenter() { return hasPickedFromCenter; }

    public void setPlayerScore(int points, int playerID) { playerScore[playerID] = points; }
    public void addPlayerPoint(int playerID, int points) { playerScore[playerID] += points; }
    public void subtractPlayerPoint(int playerID, int points) {
        playerScore[playerID] -= points;

        if (playerScore[playerID] < 0) playerScore[playerID] = 0;
    }

    public double getCompletedRows(AbstractGameState gs, int playerId) {
        AzulParameters params = (AzulParameters) gs.getGameParameters();
        int completedRows = 0;
        int rowSize = params.getBoardSize();

        for (int row = 0; row < rowSize; row++) {
            if (isRowComplete(playerId, row)) {
                completedRows++;
            }
        }
        return completedRows;
    }

    public double getCompletedCols(AbstractGameState gs, int playerId) {
        AzulParameters params = (AzulParameters) gs.getGameParameters();
        int completedCols = 0;
        int colSize = params.getBoardSize();

        for (int col=0; col < colSize; col++){
            if (isColComplete(playerId, col)){
                completedCols++;
            }
        }
        return completedCols;
    }

    public boolean isRowComplete(int playerId, int row) {
        AzulPlayerBoard playerBoard = getPlayerBoard(playerId);
        int wallLength = playerBoard.getPlayerWall().length;
        for (int col = 0; col < wallLength; col++) {
            if (playerBoard.getPlayerWall()[row][col] == AzulTile.Empty || playerBoard.getPlayerWall()[row][col] == null) {
                return false;
            }
        }
        return true;
    }

    public boolean isColComplete(int playerId, int col) {
        AzulPlayerBoard playerBoard = getPlayerBoard(playerId);
        int wallLength = playerBoard.getPlayerWall().length;

        for (int row = 0; row < wallLength; row++) {
            if (playerBoard.getPlayerWall()[row][col] == AzulTile.Empty || playerBoard.getPlayerWall()[row][col] == null) {
                return false;
            }
        }
        return true;
    }

    public int getCompletedColorSets(AbstractGameState gs, int playerID) {
        AzulParameters params = (AzulParameters) gs.getGameParameters();
        int wallSize = params.getBoardSize();

        boolean[] completedColors = new boolean[params.totalColours];

        AzulTile[][] playerWall = getPlayerBoard(playerID).playerWall;

        for (int col = 0; col < wallSize; col++) {
            boolean isComplete = true;
            for (int row = 0; row < wallSize; row++) {
                if (playerWall[row][col] == AzulTile.Empty || playerWall[row][col] == null) {
                    isComplete = false;
                    break;
                }
            }
            completedColors[col] = isComplete;
        }
        int completedSetCount = 0;
        for (boolean completed: completedColors) {
            if (completed) completedSetCount++;
        }

        return completedSetCount;
    }

    public int getNegativePointsInRound(AbstractGameState gs, int playerId) {
        AzulParameters params = (AzulParameters) gs.getGameParameters();

        int[] penalties = params.getFloorPenalties();
        AzulTile[] floorLine = getPlayerBoard(playerId).playerFloorLine;
        int penalty = 0;

        for (int col = 0; col < floorLine.length; col++) {
            if (floorLine[col] != AzulTile.Empty || floorLine[col] != null) {
                penalty += penalties[Math.min(col, penalties.length-1)];
            }
        }

        return penalty;
    }

    /**
     * @return the enum value corresponding to this game, declared in {@link GameType}.
     */
    @Override
    protected GameType _getGameType() {
        return GameType.Azul;
    }

    /**
     * Returns all Components used in the game and referred to by componentId from actions or rules.
     * This method is called after initialising the game state, so all components will be initialised already.
     *
     * @return - List of Components in the game.
     */
    @Override
    protected List<Component> _getAllComponents() {
        return new ArrayList<Component>() {{
            addAll(factoryBoards);
            addAll(playerBoards);
            add(center);
        }};
    }

    /**
     * <p>Create a deep copy of the game state containing only those components the given player can observe.</p>
     * <p>If the playerID is NOT -1 and If any components are not visible to the given player (e.g. cards in the hands
     * of other players or a face-down deck), then these components should instead be randomized (in the previous examples,
     * the cards in other players' hands would be combined with the face-down deck, shuffled together, and then new cards drawn
     * for the other players). This process is also called 'redeterminisation'.</p>
     * <p>There are some utilities to assist with this in utilities.DeterminisationUtilities. One firm is guideline is
     * that the standard random number generator from getRnd() should not be used in this method. A separate Random is provided
     * for this purpose - redeterminisationRnd.
     *  This is to avoid this RNG stream being distorted by the number of player actions taken (where those actions are not themselves inherently random)</p>
     * <p>If the playerID passed is -1, then full observability is assumed and the state should be faithfully deep-copied.</p>
     *
     * <p>Make sure the return type matches the class type, and is not AbstractGameState.</p>
     *
     *
     * @param playerId - player observing this game state.
     */
    @Override
    protected AzulGameState _copy(int playerId) {
        AzulGameState copy = new AzulGameState(gameParameters, getNPlayers());

        copy.factoryBoards = new ArrayList<>();
        for (AzulFactoryBoard fb : factoryBoards) {
            copy.factoryBoards.add((AzulFactoryBoard) fb.copy());
        }

        copy.playerBoards = new ArrayList<>();
        for (AzulPlayerBoard pb : playerBoards) {
            copy.playerBoards.add((AzulPlayerBoard) pb.copy());
        }

        copy.center = (AzulCenter) center.copy();

        copy.numOfTilesPicked = numOfTilesPicked;
        copy.pickedTile = pickedTile;
        copy.wall = wall;

        copy.tileCounts = new HashMap<>(tileCounts);
        copy.lid = new HashMap<>(lid);
        copy.playerScore = playerScore.clone();

        return copy;
    }

    /**
     * @param playerId - player observing the state.
     * @return a score for the given player approximating how well they are doing (e.g. how close they are to winning
     * the game); a value between 0 and 1 is preferred, where 0 means the game was lost, and 1 means the game was won.
     */
    @Override
    protected double _getHeuristicScore(int playerId) {
        return new AzulHeuristic().evaluateState(this, playerId);
    }

    /**
     * @param playerId - player observing the state.
     * @return the true score for the player, according to the game rules. May be 0 if there is no score in the game.
     */
    @Override
    public double getGameScore(int playerId) {
        return playerScore[playerId];
    }

    @Override
    protected boolean _equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AzulGameState)) return false;
        AzulGameState that = (AzulGameState) o;

        return numOfTilesPicked == that.numOfTilesPicked &&
                hasPickedFromCenter == that.hasPickedFromCenter &&
                Objects.equals(pickedTile, that.pickedTile) &&
                Objects.equals(factoryBoards, that.factoryBoards) &&
                Objects.equals(center, that.center) &&
                Objects.equals(tileCounts, that.tileCounts) &&
                Objects.equals(lid, that.lid) &&
                Objects.equals(wall, that.wall) &&
                Arrays.equals(playerScore, that.playerScore);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(numOfTilesPicked, hasPickedFromCenter, pickedTile,
                factoryBoards, playerBoards, center, tileCounts, lid, wall);
        result = 31 * result + Arrays.hashCode(playerScore);
        return result;
    }

    // TODO: Consider the methods below for possible implementation
    // TODO: These all have default implementations in AbstractGameState, so are not required to be implemented here.
    // TODO: If the game has 'teams' that win/lose together, then implement the next two nethods.
    /**
     * Returns the number of teams in the game. The default is to have one team per player.
     * If the game does not have 'teams' that win/lose together, then ignore these two methods.
     */
   // public int getNTeams();
    /**
     * Returns the team number the specified player is on.
     */
    //public int getTeam(int player);

    // TODO: If your game has multiple special tiebreak options, then implement the next two methods.
    // TODO: The default is to tie-break on the game score (if this is the case, ignore these)
    // public double getTiebreak(int playerId, int tier);
    // public int getTiebreakLevels();


    // TODO: If your game does not have a score of any type, and is an 'insta-win' type game which ends
    // TODO: as soon as a player achieves a winning condition, and has some bespoke method for determining 1st, 2nd, 3rd etc.
    // TODO: Then you *may* want to implement:.
    //public int getOrdinalPosition(int playerId);

    // TODO: Review the methods below...these are all supported by the default implementation in AbstractGameState
    // TODO: So you do not (and generally should not) implement your own versions - take advantage of the framework!
    // public Random getRnd() returns a Random number generator for the game. This will be derived from the seed
    // in game parameters, and will be updated correctly on a reset

    // Ths following provide access to the id of the current player; the first player in the Round (if that is relevant to a game)
    // and the current Turn and Round numbers.
    // public int getCurrentPlayer()
    // public int getFirstPlayer()
    // public int getRoundCounter()
    // public int getTurnCounter()
    // also make sure you check out the standard endPlayerTurn() and endRound() methods in StandardForwardModel

    // This method can be used to log a game event (e.g. for something game-specific that you want to include in the metrics)
    // public void logEvent(IGameEvent...)
}
