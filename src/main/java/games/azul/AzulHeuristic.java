package games.azul;

import core.AbstractGameState;
import core.AbstractParameters;
import core.CoreConstants;
import core.interfaces.IStateHeuristic;
import evaluation.optimisation.TunableParameters;
import utilities.Utils;

public class AzulHeuristic extends TunableParameters implements IStateHeuristic {

    double FACTOR_SCORE = 1.0;
    double FACTOR_COMPLETED_ROWS = 0.5;
    double FACTOR_COMPLETED_COLS = 0.5;
    double FACTOR_COMPLETED_SETS = 0.5;
    double FACTOR_NEGATIVE_POINTS = -0.5;

    public AzulHeuristic() {
        addTunableParameter("FACTOR_SCORE", 1.0);
        addTunableParameter("FACTOR_COMPLETED_ROWS", 0.5);
        addTunableParameter("FACTOR_COMPLETED_COLS", 0.5);
        addTunableParameter("FACTOR_COMPLETED_SETS", 0.5);
        addTunableParameter("FACTOR_NEGATIVE_POINTS", -0.5);
    }

    @Override
    protected AbstractParameters _copy() {
        AzulHeuristic copy = new AzulHeuristic();
        copy.FACTOR_SCORE = FACTOR_SCORE;
        copy.FACTOR_COMPLETED_ROWS = FACTOR_COMPLETED_ROWS;
        copy.FACTOR_COMPLETED_COLS = FACTOR_COMPLETED_COLS;
        copy.FACTOR_COMPLETED_SETS = FACTOR_COMPLETED_SETS;
        copy.FACTOR_NEGATIVE_POINTS = FACTOR_NEGATIVE_POINTS;
        return copy;
    }

    @Override
    protected boolean _equals(Object o) {
        if (o instanceof AzulHeuristic) {
            AzulHeuristic other = (AzulHeuristic) o;
            return other.FACTOR_SCORE == FACTOR_SCORE &&
                    other.FACTOR_COMPLETED_ROWS == FACTOR_COMPLETED_ROWS &&
                    other.FACTOR_COMPLETED_COLS == FACTOR_COMPLETED_COLS &&
                    other.FACTOR_COMPLETED_SETS == FACTOR_COMPLETED_SETS &&
                    other.FACTOR_NEGATIVE_POINTS == FACTOR_NEGATIVE_POINTS;
        }
        return false;
    }

    @Override
    public double evaluateState(AbstractGameState gs, int playerId) {
        AzulGameState ags = (AzulGameState) gs;
        CoreConstants.GameResult playerResult = gs.getPlayerResults()[playerId];

        if (!gs.isNotTerminal()) {
            return playerResult.value;
        }

        double score = ags.getGameScore(playerId);
        double completedRows = ags.getCompletedRows(gs, playerId);
        double completedCols = ags.getCompletedCols(gs, playerId);
        double completedSets = ags.getCompletedColorSets(gs, playerId);
        double negativePoints = ags.getNegativePointsInRound(gs, playerId);

        double heuristicValue = FACTOR_SCORE * score +
                FACTOR_COMPLETED_ROWS * completedRows +
                FACTOR_COMPLETED_COLS * completedCols +
                FACTOR_COMPLETED_SETS * completedSets +
                FACTOR_NEGATIVE_POINTS * negativePoints;

        return Utils.clamp(heuristicValue, -1.0, 1.0);
    }

    @Override
    public Object instantiate() { return this._copy(); }

    @Override
    public void _reset() {
        FACTOR_SCORE = (double) getParameterValue("FACTOR_SCORE");
        FACTOR_COMPLETED_ROWS = (double) getParameterValue("FACTOR_COMPLETED_ROWS");
        FACTOR_COMPLETED_COLS = (double) getParameterValue("FACTOR_COMPLETED_COLUMNS");
        FACTOR_COMPLETED_SETS = (double) getParameterValue("FACTOR_COMPLETED_SETS");
        FACTOR_NEGATIVE_POINTS = (double) getParameterValue("FACTOR_NEGATIVE_POINTS");
    }
}
