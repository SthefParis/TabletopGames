package games.azul.stats;

import core.interfaces.IGameEvent;
import evaluation.listeners.MetricsGameListener;
import evaluation.metrics.AbstractMetric;
import evaluation.metrics.Event;
import evaluation.metrics.IMetricsCollection;
import games.azul.AzulGameState;
import games.azul.AzulParameters;
import games.azul.actions.PickUpTilesAction;
import games.azul.actions.PlaceTileAction;

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
//                    System.out.println("Player " + playerId + " picked up " + numTilesPickedPerTurn.get(playerId) + " tile(s).");
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

    public static class PatternLineCompletionRate extends AbstractMetric {
        // This map will store the number of completions per row for each player
        private final Map<Integer, int[]> rowsCompleted = new HashMap<>();
        private final Map<Integer, Integer> totalTurns = new HashMap<>();

        @Override
        protected boolean _run(MetricsGameListener listener, Event e, Map<String, Object> records) {
            if (e.type == TURN_OVER) {
                AzulGameState ags = (AzulGameState) e.state;
                int playerId = e.playerID;

                // Initialize the row completion array for the player if not already done
                rowsCompleted.putIfAbsent(playerId, new int[5]);  // 5 rows, indexed from 0 to 4
                int[] playerRowsCompleted = rowsCompleted.get(playerId);

                // Check each row for completion this turn
                for (int row = 0; row < 5; row++) {
                    if (ags.getPlayerBoard(playerId).isPatternLineRowFull(row)) {
                        playerRowsCompleted[row]++;  // Increment completion count for the row
                    }
                }

                // Track the total number of turns for this player
                totalTurns.put(playerId, totalTurns.getOrDefault(playerId, 0) + 1);

                return true;
            }

            if (e.type == GAME_OVER) {
                // At the end of the game, store the completion rate for each row per player
                for (int playerId : rowsCompleted.keySet()) {
                    int[] completedRows = rowsCompleted.getOrDefault(playerId, new int[5]);
                    int turns = totalTurns.getOrDefault(playerId, 1);  // Avoid division by zero

                    // Store completion rate for each row
                    for (int row = 0; row < 5; row++) {
                        double rate = completedRows[row] / (double) turns;
                        records.put("PatternCompletionRate_P" + playerId + "_Row" + row, rate);
                    }
                }
                return true;
            }

            return false;
        }

        @Override
        public Set<IGameEvent> getDefaultEventTypes() {
            return Set.of(TURN_OVER, Event.GameEvent.GAME_OVER);
        }

        @Override
        public Map<String, Class<?>> getColumns(int nPlayersPerGame, Set<String> playerNames) {
            Map<String, Class<?>> columns = new HashMap<>();
            // Add a separate column for each row (0 to 4) for each player
            for (int playerId = 0; playerId < nPlayersPerGame; playerId++) {
                for (int row = 0; row < 5; row++) {
                    columns.put("PatternCompletionRate_P" + playerId + "_Row" + row, Double.class);
                }
            }
            return columns;
        }
    }

    public static class PenaltiesPerRound extends AbstractMetric {

        @Override
        protected boolean _run(MetricsGameListener listener, Event e, Map<String, Object> records) {
            if (e.type == ROUND_OVER) {
                AzulGameState ags = (AzulGameState) e.state;
                AzulParameters params = (AzulParameters) ags.getGameParameters();
                int round = e.state.getRoundCounter();

                for (int player = 0; player < ags.getNPlayers(); player++) {
                    int penalty = Arrays.stream(ags.getFloorLineAsIndex(player)).sum();

//                    System.out.printf("Metrics: Player %d got %d penalties \n", player, penalty);
                    String key = String.format("P%d_R%d", player, round);
                    records.put(key, penalty);
                }

                return true;

            }

            return false;
        }

        @Override
        public Set<IGameEvent> getDefaultEventTypes() {
            return Set.of(ROUND_OVER);
        }

        @Override
        public Map<String, Class<?>> getColumns(int nPlayersPerGame, Set<String> playerNames) {
            Map<String, Class<?>> columns = new LinkedHashMap<>();
            for (int p = 0; p < nPlayersPerGame; p++) {
                for (int r = 0; r < 10; r++) {
                    columns.put(String.format("P%d_R%d", p, r), Integer.class);
                }
            }
            return columns;
        }
    }

    public static class PatternLineUsage extends AbstractMetric {
        HashMap<Integer, int[]> patternLineUsage = new HashMap<>();

        @Override
        protected boolean _run(MetricsGameListener listener, Event e, Map<String, Object> records) {
            if (e.action instanceof PlaceTileAction) {
                PlaceTileAction pta = (PlaceTileAction) e.action;
                AzulGameState ags = (AzulGameState) e.state;
                int player = e.playerID;

                patternLineUsage.putIfAbsent(player, new int[5]);
                int[] usage = patternLineUsage.get(player);
                int lastPlacedRow = ags.getLastPlacedRow();
                if (lastPlacedRow >= 0) {
                    usage[lastPlacedRow]++;
                }

                return true;
            }

            if (e.type == GAME_OVER) {
                for (Map.Entry<Integer, int[]> entry : patternLineUsage.entrySet()) {
                    int playerId = entry.getKey();
                    int[] usage = entry.getValue();
                    for (int row = 0; row < usage.length; row++) {
                        records.put(String.format("P%d_Row%d_Usage", playerId, row), usage[row]);
                    }
                }
                patternLineUsage.clear();
                return true;
            }
            return false;
        }

        @Override
        public Set<IGameEvent> getDefaultEventTypes() {
            return Set.of(ACTION_CHOSEN, GAME_OVER);
        }

        @Override
        public Map<String, Class<?>> getColumns(int nPlayersPerGame, Set<String> playerNames) {
            Map<String, Class<?>> columns = new HashMap<>();
            for (int player = 0; player < nPlayersPerGame; player++) {
                for (int row = 0; row < 5; row++) {
                    columns.put(String.format("P%d_Row%d_Usage", player, row), Integer.class);
                }
            }
            return columns;
        }
    }




    // public static class WallCompletionRate
    // public static class Penalties
    // public static class AverageFinalScore
    // public static class EndgameStrategy
    // public static class TileColourFrequency
    // public static class TilePlacement



}
