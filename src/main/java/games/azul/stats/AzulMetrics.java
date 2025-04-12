package games.azul.stats;

import core.interfaces.IGameEvent;
import evaluation.listeners.MetricsGameListener;
import evaluation.metrics.AbstractMetric;
import evaluation.metrics.Event;
import evaluation.metrics.IMetricsCollection;
import games.azul.AzulGameState;
import games.azul.actions.PickUpTilesAction;

import java.util.*;

import static evaluation.metrics.Event.GameEvent.*;

@SuppressWarnings("unused")
public class AzulMetrics implements IMetricsCollection {

    public static class FirstColourPicked extends AbstractMetric {

        private final Map<Integer, String> firstColourPerRound = new HashMap<>();
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

    public static class TilesPickedPerPlayer extends AbstractMetric {
        Map<Integer, Integer> numTilesPickedPerTurn = new HashMap<>();
        private int currentRound = -1;
        private int currentTurn = -1;

        @Override
        protected boolean _run(MetricsGameListener listener, Event e, Map<String, Object> records) {
            if (e.action instanceof PickUpTilesAction) {
                PickUpTilesAction pta = (PickUpTilesAction) e.action;
                AzulGameState ags = (AzulGameState) e.state;
                int player = e.playerID;
                int round = e.state.getRoundCounter();
                int turn = e.state.getTurnCounter();

                if (round != currentRound){
                    numTilesPickedPerTurn.clear();
                    currentRound = round;
                }

                if (turn != currentTurn) {
                    currentTurn = turn;
                }

                int tilesPicked = ags.getNumOfTilesPicked();
                numTilesPickedPerTurn.put(player, numTilesPickedPerTurn.getOrDefault(player, 0) + tilesPicked);

                for (int playerId : numTilesPickedPerTurn.keySet()) {
                    System.out.println("Player " + playerId + " picked up " + numTilesPickedPerTurn.get(playerId) + " tile(s).");
                    records.put("NumTilesPicked_P" + playerId, numTilesPickedPerTurn.get(playerId));
                }
                return true;
            }
            return false;
        }

        @Override
        public Set<IGameEvent> getDefaultEventTypes() {
            return Collections.singleton(ACTION_CHOSEN);
        }

        @Override
        public Map<String, Class<?>> getColumns(int nPlayersPerGame, Set<String> playerNames) {
            Map<String, Class<?>> columns = new HashMap<>();
            for (int playerId = 0; playerId < nPlayersPerGame; playerId++){
                columns.put("NumTilesPicked_P" + playerId, Integer.class);
            }
            return columns;
        }
    }

    public static class FactoryVsCentrePicks extends AbstractMetric {
        private final Map<Integer, Integer> factoryPicksPerPlayer = new HashMap<>();
        private final Map<Integer, Integer> centerPicksPerPlayer = new HashMap<>();

        @Override
        protected boolean _run(MetricsGameListener listener, Event e, Map<String, Object> records) {
            if (e.action instanceof PickUpTilesAction) {
                PickUpTilesAction pta = (PickUpTilesAction) e.action;
                int player = e.playerID;

                if (pta.isFromFactory()) {
                    factoryPicksPerPlayer.put(player, factoryPicksPerPlayer.getOrDefault(player, 0) + 1);
                } else {
                    centerPicksPerPlayer.put(player, factoryPicksPerPlayer.getOrDefault(player, 0) + 1);
                }

                for (int playerId : factoryPicksPerPlayer.keySet()) {
//                    System.out.println("Player " + playerId + " - Factory Picks: " + factoryPicksPerPlayer.get(playerId) +
//                            ", Center Picks: " + centerPicksPerPlayer.get(playerId));

                    // Store the picks per player in the records
                    records.put("FactoryPicks_P" + playerId, factoryPicksPerPlayer.get(playerId));
                    records.put("CenterPicks_P" + playerId, centerPicksPerPlayer.get(playerId));
                }
                return true;
            }

//            if (e.type == Event.GameEvent.GAME_OVER) {
//                System.out.println("Game over");
//
//                for (int playerId : factoryPicksPerPlayer.keySet()) {
//                    System.out.println("Player " + playerId + " - Factory Picks: " + factoryPicksPerPlayer.get(playerId) +
//                            ", Center Picks: " + centerPicksPerPlayer.get(playerId));
//
//                    // Store the picks per player in the records
//                    records.put("FactoryPicks_P" + playerId, factoryPicksPerPlayer.get(playerId));
//                    records.put("CenterPicks_P" + playerId, centerPicksPerPlayer.get(playerId));
//                }
//                return true;
//            }
            return false;
        }

        @Override
        public Set<IGameEvent> getDefaultEventTypes() {
            return Set.of(ACTION_CHOSEN);
        }

        @Override
        public Map<String, Class<?>> getColumns(int nPlayersPerGame, Set<String> playerNames) {
            // Define columns to store the picks for each player
            Map<String, Class<?>> columns = new HashMap<>();
            for (int playerId = 0; playerId < nPlayersPerGame; playerId++) {
                columns.put("FactoryPicks_P" + playerId, Integer.class);
                columns.put("CenterPicks_P" + playerId, Integer.class);
            }
            return columns;
        }
    }




//     public static class PatternLineCompletionRate
    // public static class WallCompletionRate
    // public static class Penalties
    // public static class AverageFinalScore
    // public static class EndgameStrategy
    // public static class TileColourFrequency
    // public static class TilePlacement



}
