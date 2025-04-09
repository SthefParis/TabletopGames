package games.azul.stats;

import core.interfaces.IGameEvent;
import evaluation.listeners.MetricsGameListener;
import evaluation.metrics.AbstractMetric;
import evaluation.metrics.Event;
import evaluation.metrics.IMetricsCollection;
import games.azul.AzulGameState;
import games.azul.actions.PickUpTilesAction;
import org.apache.commons.collections.map.HashedMap;

import java.awt.*;
import java.util.*;
import java.util.List;

import static evaluation.metrics.Event.GameEvent.*;

@SuppressWarnings("unused")
public class AzulMetrics implements IMetricsCollection {

    public static class FirstColourPicked extends AbstractMetric {

        private final Map<Integer, Color> firstColourPerRound = new HashMap<>();
        private int currentRound = -1;

        @Override
        protected boolean _run(MetricsGameListener listener, Event e, Map<String, Object> records) {
            int round = e.state.getRoundCounter();
            int player = e.playerID;

            if (e.action instanceof PickUpTilesAction) {
                PickUpTilesAction pta = (PickUpTilesAction) e.action;

                if (round != currentRound){
                    firstColourPerRound.clear();
                    currentRound = round;
                }

                if (!firstColourPerRound.containsKey(player)) {
                    firstColourPerRound.put(player, pta.getTileColour());
                    records.put("FirstColour_P" + player, pta.getTileColour());
                }
            }
            return true;
        }

        @Override
        public Set<IGameEvent> getDefaultEventTypes() {
            return Collections.singleton(ACTION_CHOSEN);
        }

        @Override
        public Map<String, Class<?>> getColumns(int nPlayersPerGame, Set<String> playerNames) {
            Map<String, Class<?>> columns = new HashMap<>();
            for (int playerId = 0; playerId < nPlayersPerGame; playerId++){
                columns.put("FirstColour_P" + playerId, String.class);
            }
            return columns;
        }
    }

    public static class AverageTilesPickedPerTurn extends AbstractMetric {
        private final Map<Integer, List<Integer>> tilesPicked = new HashMap<>();

        @Override
        protected boolean _run(MetricsGameListener listener, Event e, Map<String, Object> records) {
            int round = e.state.getRoundCounter();
            int player = e.playerID;
            AzulGameState ags = (AzulGameState) e.state;

            if (e.action instanceof PickUpTilesAction) {
                PickUpTilesAction pta = (PickUpTilesAction) e.action;

                tilesPicked.putIfAbsent(player, new ArrayList<>());
                tilesPicked.get(player).add(ags.getNumOfTilesPicked());
            }
            return true;
        }

        @Override
        public Set<IGameEvent> getDefaultEventTypes() {
            return Set.of();
        }

        @Override
        public Map<String, Class<?>> getColumns(int nPlayersPerGame, Set<String> playerNames) {
            return Map.of();
        }
    }

    public static class FactoryVsCentrePicks extends AbstractMetric {

        @Override
        protected boolean _run(MetricsGameListener listener, Event e, Map<String, Object> records) {
            return false;
        }

        @Override
        public Set<IGameEvent> getDefaultEventTypes() {
            return Set.of();
        }

        @Override
        public Map<String, Class<?>> getColumns(int nPlayersPerGame, Set<String> playerNames) {
            return Map.of();
        }
    }

    // public static class PatternLineCompletionRate
    // public static class WallCompletionRate
    // public static class Penalties
    // public static class AverageFinalScore
    // public static class EndgameStrategy
    // public static class TileColourFrequency
    // public static class TilePlacement



}
