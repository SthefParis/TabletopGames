package games.azul.stats;

import core.interfaces.IGameEvent;
import evaluation.listeners.MetricsGameListener;
import evaluation.metrics.AbstractMetric;
import evaluation.metrics.Event;
import evaluation.metrics.IMetricsCollection;
import games.azul.AzulGameState;
import games.azul.AzulParameters;
import games.azul.actions.PickUpTilesAction;

import java.util.*;

import static evaluation.metrics.Event.GameEvent.*;

@SuppressWarnings("unused")
public class AzulMetrics implements IMetricsCollection {

    public static class FirstColourPickedPerRound extends AbstractMetric {

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

    public static class PatternLineCompletionRatePerRound extends AbstractMetric {
        private final Map<Integer, Map<Integer, Integer>> rowsCompletedPerRound = new HashMap<>();
        private final Map<Integer, Map<Integer, Integer>> totalTurnsPerPlayer = new HashMap<>();

        @Override
        protected boolean _run(MetricsGameListener listener, Event e, Map<String, Object> records) {
            AzulGameState ags = (AzulGameState) e.state;

            if (e.type == ROUND_OVER) {
                int round = e.state.getRoundCounter();

                // For each player, calculate the completion rate for the current round
                for (int player = 0; player < ags.getNPlayers(); player++) {
                    int completed = rowsCompletedPerRound.getOrDefault(player, new HashMap<>()).getOrDefault(round, 0);
                    int roundsPlayed = totalTurnsPerPlayer.getOrDefault(player, new HashMap<>()).getOrDefault(round, 0);

                    double rate = roundsPlayed > 0 ? (completed / (double) roundsPlayed) : 0;

//                    System.out.printf("Player %d completed pattern line with a rate of %.2f this round%n", player, rate);

                    String key = String.format("P%d_R%d", player, round);
                    records.put(key, rate);
                }

                return true;
            }

            if (e.type == TURN_OVER) {
                int playerId = e.playerID;
                int round = e.state.getRoundCounter();

                // Initialize per-round data if it's the player's first turn in this round
                rowsCompletedPerRound.putIfAbsent(playerId, new HashMap<>());
                totalTurnsPerPlayer.putIfAbsent(playerId, new HashMap<>());

                // Count how many rows are completed this turn
                int completedThisTurn = 0;
                for (int row = 0; row < 5; row++) {
                    if (ags.getPlayerBoard(playerId).isPatternLineRowFull(row)) {
                        completedThisTurn++;
                    }
                }

                // Update the number of rows completed for the current round
                Map<Integer, Integer> playerRowsCompleted = rowsCompletedPerRound.get(playerId);
                playerRowsCompleted.put(round, playerRowsCompleted.getOrDefault(round, 0) + completedThisTurn);

                // Increment the number of rounds for this player
                Map<Integer, Integer> playerRounds = totalTurnsPerPlayer.get(playerId);
                playerRounds.put(round, playerRounds.getOrDefault(round, 0) + 1);

                return true;
            }

            return false;
        }

        @Override
        public Set<IGameEvent> getDefaultEventTypes() {
            return Set.of(TURN_OVER, ROUND_OVER);
        }

        @Override
        public Map<String, Class<?>> getColumns(int nPlayersPerGame, Set<String> playerNames) {
            Map<String, Class<?>> columns = new HashMap<>();
            for (int playerId = 0; playerId < nPlayersPerGame; playerId++) {
                for (int r = 0; r < 10; r++) {
                    columns.put(String.format("P%d_R%d", playerId, r), Double.class);
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
//                System.out.println("Round: " + round);

                for (int player = 0; player < ags.getNPlayers(); player++) {
                    int penalty = Arrays.stream(ags.getFloorLineAsIndex(player)).sum();

//                    System.out.printf("Player %d got %d penalties \n", player, penalty);
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
            if (e.type == TURN_OVER) {
                AzulGameState ags = (AzulGameState) e.state;
                int player = e.playerID;

                patternLineUsage.putIfAbsent(player, new int[5]);
                int[] usage = patternLineUsage.get(player);

                for (int row = 0; row < 5; row++) {
                    if (!ags.getPlayerBoard(player).isPatternLineEmpty(row)) {
                        usage[row]++;
                    }
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
                return true;
            }
            return false;
        }

        @Override
        public Set<IGameEvent> getDefaultEventTypes() {
            return Set.of(TURN_OVER, GAME_OVER);
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

}
