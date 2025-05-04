package games.azul;

import core.AbstractGameState;
import core.AbstractParameters;
import evaluation.optimisation.TunableParameters;
import games.azul.tiles.AzulTile;

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
    final int playerBoardSize = 5;
    final int maxTilesInFloorLine = 7;
    final int tilesInScoreTrack = 101;
    final int factoryBoardHeight = 1;
    int factoryBoardWidth;
    final int nTilesPerFactory = 4;
    int nFactories;
    final int totalColours = 5;

    // Scoring parameters
    private final int rowBonusPoints = 2;
    private final int columnBonusPoints = 7;
    private final int colorSetBonusPoints = 10;
    private final int placingTilePoints = 1;

    // Penalty parameters
    int[] floorPenalties = {1, 1, 2, 2, 2, 3, 3};

    // Wall Pattern Positions
    public AzulTile[][] wallPattern;

    /**
     * Function initializes the parameters based on the number of players. And initializes the correct pattern on the wall.
     * @param nPlayers - Number of players in the game.
     */
    public void initialise(int nPlayers){
        this.nFactories = ((nPlayers)*2) + 1;
        this.factoryBoardWidth = this.nFactories * this.nTilesPerFactory;

        int boardSize = getBoardSize();
        this.wallPattern = new AzulTile[boardSize][boardSize];

        AzulTile[] tileOrder = {
                AzulTile.Blue, AzulTile.Orange, AzulTile.Red, AzulTile.Black, AzulTile.White
        };

        // Fill the board according to the Azul pattern
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                wallPattern[row][col] = tileOrder[(col - row + tileOrder.length) % tileOrder.length];
            }
        }
    }

    public int getBoardSize() { return playerBoardSize; }
    public int getNTilesPerFactory() { return nTilesPerFactory; }
    public int getNFactories() { return nFactories; }

    // Points
    public int getRowBonusPoints() { return rowBonusPoints; }
    public int getColumnBonusPoints() { return columnBonusPoints; }
    public int getColorSetBonusPoints() { return colorSetBonusPoints; }
    public int getPlacingTilePoints() { return placingTilePoints; }

    public int[] getFloorPenalties() { return floorPenalties; }
    public int getScoreTrackLength() { return tilesInScoreTrack; }
    public int getFloorLineLength() { return maxTilesInFloorLine; }

    public int getTileColPositionInRow(int row, AzulTile tile) {
        if (row < 0 || row >= wallPattern.length) {
            return -1;
        }

        for (int col = 0; col < wallPattern[row].length; col++) {
            if (wallPattern[row][col] == tile) {
                return col;
            }
        }

        return -1;
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
        return factoryBoardWidth == that.factoryBoardWidth &&
                nFactories == that.nFactories &&
                Arrays.equals(floorPenalties, that.floorPenalties);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(playerBoardSize, maxTilesInFloorLine, tilesInScoreTrack, factoryBoardHeight,
                factoryBoardWidth, nTilesPerFactory, nFactories, totalColours,
                rowBonusPoints, columnBonusPoints, colorSetBonusPoints, placingTilePoints);
        result = 31 * result + Arrays.hashCode(floorPenalties);
        return result;
    }
}
