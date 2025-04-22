package games.azul;

import core.AbstractGameState;
import core.AbstractPlayer;
import core.Game;
import games.azul.gui.AzulCentreView;
import games.azul.gui.AzulFactoryBoardView;
import games.azul.gui.AzulPlayerBoardView;
import gui.AbstractGUIManager;
import gui.GamePanel;
import gui.IScreenHighlight;
import players.human.ActionController;
import utilities.ImageIO;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * <p>This class allows the visualisation of the game. The game components (accessible through {@link Game#getGameState()}
 * should be added into {@link JComponent} subclasses (e.g. {@link JLabel},
 * {@link JPanel}, {@link JScrollPane}; or custom subclasses such as those in {@link gui} package).
 * These JComponents should then be added to the <code>`parent`</code> object received in the class constructor.</p>
 *
 * <p>An appropriate layout should be set for the parent GamePanel as well, e.g. {@link BoxLayout} or
 * {@link BorderLayout} or {@link GridBagLayout}.</p>
 *
 * <p>Check the super class for methods that can be overwritten for a more custom look, or
 * {@link games.terraformingmars.gui.TMGUI} for an advanced game visualisation example.</p>
 *
 * <p>A simple implementation example can be found in {@link games.tictactoe.gui.TicTacToeGUIManager}.</p>
 */
public class AzulGUIManager extends AbstractGUIManager {

    // Settings for display areas
    final static int playerAreaWidth = 200;
    final static int playerAreaHeight = 450;

    // Width and height of total window
    int width, height;

    // List of Factory Boards
    private List<AzulFactoryBoardView> factoryBoards;
    private List<AzulPlayerBoardView> playerBoards;

    // Current active player
    private int activePlayer = -1;

    // Stores the player tabs
    private JTabbedPane playerBoardTabs;
    private List<JPanel> playerPanels = new ArrayList<>();

    public AzulGUIManager(GamePanel parent, Game game, ActionController ac, Set<Integer> human) {
        super(parent, game, ac, human);

        UIManager.put("TabbedPane.contentOpaque", false);
        UIManager.put("TabbedPane.opaque", false);
        UIManager.put("TabbedPane.tabsOpaque", false);

        if (game == null) return;
        AbstractGameState gs = game.getGameState();
        if (gs == null) return;

        // Create main and rules window
        JTabbedPane pane = new JTabbedPane();
        JPanel main = new JPanel();
        main.setOpaque(false);
        main.setLayout(new BorderLayout());
        JPanel rules = new JPanel();
        rules.setOpaque(false);
        pane.add("Main", main);
        pane.add("Rules", rules);
        JLabel ruleText = new JLabel(getRuleText());
        rules.add(ruleText);

        parent.setBackground(ImageIO.GetInstance().getImage("data/azul/bg.jpg"));

        // Initialise active player
        activePlayer = gs.getCurrentPlayer();

        // Find required size of window
        int nPlayers = gs.getNPlayers();
        int nHorizAreas = 1 + (nPlayers <= 3 ? 2 : 3);
        double nVertAreas = 5;
        this.width = playerAreaWidth * nHorizAreas;
        this.height = (int) (playerAreaHeight * nVertAreas) + 20;
        ruleText.setPreferredSize(new Dimension(width*2/3+60, height/3));

        AzulGameState ags = (AzulGameState) gs;
        AzulParameters params = (AzulParameters) gs.getGameParameters();

        // Main game area that will hold all game views
        factoryBoards = new ArrayList<>();
        playerBoards = new ArrayList<>();
        AzulCentreView centreView = new AzulCentreView(ags.centre, 40, 10, 4);
        centreView.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 2), "Centre"));

        JPanel mainGameArea = new JPanel();
        mainGameArea.setLayout(new BorderLayout());
        mainGameArea.setOpaque(false);

        // Tabbed pane for player boards
        playerBoardTabs = new JTabbedPane();
        playerBoardTabs.setPreferredSize(new Dimension(playerAreaWidth, playerAreaHeight));

        for (int i = 0; i < nPlayers; i++) {
            AzulPlayerBoardView playerBoard = new AzulPlayerBoardView(ags.getPlayerBoard(i), ags);

            // Get agent name
            String[] split = game.getPlayers().get(i).getClass().toString().split("\\.");
            String agentName = split[split.length - 1];

            // Create border and set preferred size
            Border title = BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(Color.BLACK, 1),
                    "Player " + i + " [" + agentName + "]",
                    TitledBorder.CENTER,
                    TitledBorder.BELOW_BOTTOM
            );

            playerBoard.setBorder(title);
            playerBoard.setPreferredSize(new Dimension(playerAreaWidth, playerAreaHeight));

            // Wrap player board in a JPanel for spacing
            JPanel playerPanel = new JPanel();
            playerPanels.add(playerPanel);
            playerPanel.setPreferredSize(new Dimension(playerAreaWidth, playerAreaHeight));
            playerPanel.setLayout(new BorderLayout());
            playerPanel.add(playerBoard, BorderLayout.CENTER);

            // Add tab for each player
            playerBoardTabs.addTab("Player " + i, playerPanel);

            playerBoards.add(playerBoard);
        }

        // Add tabbed pane to main game area at the top
        mainGameArea.add(playerBoardTabs, BorderLayout.NORTH);

        // Centre panel which holds factories and tile disposal
        JPanel centreArea = new JPanel();
        int factoriesPerRow = 5;
        int numRows = (int) Math.ceil((double) params.getNFactories() / factoriesPerRow);
        centreArea.setLayout(new GridLayout(numRows, factoriesPerRow));

        centreArea.setPreferredSize(new Dimension(100,400));
        centreArea.setOpaque(true);

        for (int j = 0; j < params.getNFactories(); j++) {
            AzulFactoryBoardView factoryBoard = new AzulFactoryBoardView(ags.getFactory(j), ags, j);
            factoryBoards.add(factoryBoard);
            centreArea.add(factoryBoard);
        }

        centreArea.add(centreView);
        mainGameArea.add(centreArea, BorderLayout.CENTER);

        // Top area will show state information
        JPanel infoPanel = createGameStateInfoPanel("Azul", gs, width, defaultInfoPanelHeight);
        infoPanel.setOpaque(false);

//      Bottom area will show actions available
//        JComponent actionPanel = createActionPanel(new IScreenHighlight[0], width, defaultActionPanelHeight, false, true, null, null, null);
//        actionPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(mainGameArea);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16); // smoother scroll

        main.add(scrollPane, BorderLayout.CENTER);
        main.add(infoPanel, BorderLayout.NORTH);
//        main.add(actionPanel, BorderLayout.SOUTH);

        // Add all views to frame
        parent.setLayout(new BorderLayout());
        parent.add(pane, BorderLayout.CENTER);
        parent.setPreferredSize(new Dimension(width, height + defaultActionPanelHeight + defaultInfoPanelHeight + defaultCardHeight + 20));
        parent.revalidate();
        parent.setVisible(true);
        parent.repaint();
    }

    /**
     * Defines how many action button objects will be created and cached for usage if needed. Less is better, but
     * should not be smaller than the number of actions available to players in any game state.
     *
     * @return maximum size of the action space (maximum actions available to a player for any decision point in the game)
     */
    @Override
    public int getMaxActionSpace() {
        return 10;
    }

    /**
     * Updates all GUI elements given current game state and player that is currently acting.
     *
     * @param player    - current player acting.
     * @param gs - current game state to be used in updating visuals.
     */
    @Override
    protected void _update(AbstractPlayer player, AbstractGameState gs) {
        if (gs == null) return;
        AzulGameState ags = (AzulGameState) gs;

        int newActivePlayer = gs.getCurrentPlayer();
        if (newActivePlayer != activePlayer) {
            activePlayer = gs.getCurrentPlayer();
            playerBoardTabs.setSelectedIndex(activePlayer);
        }

       // Update factory boards
        for (int i=0; i<factoryBoards.size(); i++){
            factoryBoards.get(i).updateComponent(ags.getFactory(i));
        }

        // Update borders to reflect active player
        for (int i = 0; i < playerPanels.size(); i++) {
            JPanel panel = playerPanels.get(i);

            Border lineBorder = BorderFactory.createLineBorder(
                    i == activePlayer ? Color.ORANGE : Color.BLACK,
                    i == activePlayer ? 3 : 1
            );
            String[] split = game.getPlayers().get(i).getClass().toString().split("\\.");
            String agentName = split[split.length - 1];
            TitledBorder title = BorderFactory.createTitledBorder(
                    lineBorder,
                    "Player " + i + " [" + agentName + "]",
                    TitledBorder.CENTER,
                    TitledBorder.BELOW_BOTTOM
            );
            // Apply border to the internal AzulPlayerBoardView
            playerBoards.get(i).setBorder(title);
        }

        parent.revalidate();
        parent.repaint();
    }

    private String getRuleText() {
        String rules = "<html><centre><h1>Azul</h1></centre><br/><hr><br/>";
        rules += "<p>Azul is a competitive tile-drafting game where players take turns selecting and placing colorful tiles to decorate their personal board. The goal is to create beautiful patterns while maximizing points and outmaneuvering opponents. The player with the most points at the end of the game wins.</p><br/>";

        rules += "<p><b>Game Setup:</b></p>";
        rules += "<ul><li>Each player receives a personal board.</li>";
        rules += "<li>Factory displays (tile supply piles) are placed in the centre.</li>";
        rules += "<li>Tiles are randomly drawn from a bag to fill the factories.</li></ul><br/>";

        rules += "<p><b>Gameplay:</b></p>";
        rules += "<ul><li><b>Tile Selection:</b> On your turn, pick all tiles of one color from a factory display or the centre. Remaining tiles will be moved to the centre.</li>";
        rules += "<li><b>Pattern Line Placement:</b> Place chosen tiles in a row on your personal board. Rows must be completely filled before moving tiles to the playerWall.</li>";
        rules += "<li><b>Wall Tiling:</b> At the end of the round, one tile from each completed row moves to the playerWall, scoring points based on adjacency.</li>";
        rules += "<li><b>Penalties:</b> Unused tiles fall to the floor line, deducting points.</li></ul><br/>";

        rules += "<p><b>Game End & Scoring:</b></p>";
        rules += "<ul><li>The game ends when a player completes a full horizontal row on their mosaic.</li>";
        rules += "<li>Points are awarded for connected tiles, completed rows, columns, and full sets of a color.</li>";
        rules += "<li>The player with the highest score wins.</li></ul><br/>";

        rules += "<hr><p><b>INTERFACE:</b> Find available actions at the bottom of the screen. Each player has two areas: their pattern lines (left) and their mosaic board (right). Click on tiles to select them, place them, or view possible moves.</p>";

        rules += "</html>";

        return rules;
    }
}
