package games.azul;

import core.AbstractGameState;
import core.AbstractParameters;
import evaluation.optimisation.TunableParameters;

import java.util.Arrays;
import java.util.Objects;

/**
 * <p>This class should hold a series of variables representing game parameters (e.g. number of cards dealt to players,
 * maximum number of rounds in the game etc.). These parameters should be used everywhere in the code instead of
 * local variables or hard-coded numbers, by accessing these parameters from the game state via {@link AbstractGameState#getGameParameters()}.</p>
 *
 * <p>It should then implement appropriate {@link #_copy()}, {@link #_equals(Object)} and {@link #hashCode()} functions.</p>
 *
 * <p>The class can optionally extend from {@link TunableParameters} instead, which allows to use
 * automatic game parameter optimisation tools in the framework.</p>
 */
public class AzulParameters extends AbstractParameters {

    // Board and factory parameters
    int playerBoardSize = 5;
    int maxTilesInFloorLine = 7;
    int tilesInScoreTrack = 101;
    int factoryBoardHeight = 1;
    int factoryBoardWidth;
    int nTilesperFactory = 4;
    int nFactories;
    int totalColours = 5;

    // Round parameters
    int maxRounds = 5;
    int maxPoints = 240;

    // Scoring parameters
    int rowBonusPoints = 2;
    int columnBonusPoints = 7;
    int colorSetBonusPoints = 10;
    int adjacencyBasePoints = 1;

    // Penalty parameters
    int[] floorPenalties = {1, 1, 2, 2, 2, 3, 3};

    public int getBoardSize() { return playerBoardSize; }
    public int getNTilesPerFactory() { return nTilesperFactory; }
    public int getTotalTilesInFactories() { return getNTilesPerFactory() * getNFactories(); }
    public int getNFactories() { return nFactories; }
    public int getMaxRounds() { return maxRounds; }

    // Points
    public int getMaxPoints() { return maxPoints; }
    public int getRowBonusPoints() { return rowBonusPoints; }
    public int getColumnBonusPoints() { return columnBonusPoints; }
    public int getColorSetBonusPoints() { return colorSetBonusPoints; }
    public int getAdjacencyBasePoints() { return adjacencyBasePoints; }

    public int[] getFloorPenalties() { return floorPenalties; }
    public int getScoreTrackLength() { return tilesInScoreTrack; }
    public int getFloorLineLength() { return maxTilesInFloorLine; }

    /**
     * Function initializes the parameters based on the number of players.
     * @param nPlayers - Number of players in the game.
     */
    public void initialise(int nPlayers){
        this.nFactories = ((nPlayers)*2) + 1;
        this.factoryBoardWidth = this.nFactories * this.nTilesperFactory;
    }

    @Override
    protected AbstractParameters _copy() {
        return new AzulParameters();
    }

    @Override
    protected boolean _equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AzulParameters)) return false;
        AzulParameters that = (AzulParameters) o;
        return playerBoardSize == that.playerBoardSize &&
                maxTilesInFloorLine == that.maxTilesInFloorLine &&
                tilesInScoreTrack == that.tilesInScoreTrack &&
                factoryBoardHeight == that.factoryBoardHeight &&
                factoryBoardWidth == that.factoryBoardWidth &&
                nTilesperFactory == that.nTilesperFactory &&
                nFactories == that.nFactories &&
                totalColours == that.totalColours &&
                maxRounds == that.maxRounds &&
                maxPoints == that.maxPoints &&
                rowBonusPoints == that.rowBonusPoints &&
                columnBonusPoints == that.columnBonusPoints &&
                colorSetBonusPoints == that.colorSetBonusPoints &&
                adjacencyBasePoints == that.adjacencyBasePoints &&
                Arrays.equals(floorPenalties, that.floorPenalties);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(playerBoardSize, maxTilesInFloorLine, tilesInScoreTrack, factoryBoardHeight,
                factoryBoardWidth, nTilesperFactory, nFactories, totalColours, maxRounds, maxPoints,
                rowBonusPoints, columnBonusPoints, colorSetBonusPoints, adjacencyBasePoints);
        result = 31 * result + Arrays.hashCode(floorPenalties);
        return result;
    }
}
