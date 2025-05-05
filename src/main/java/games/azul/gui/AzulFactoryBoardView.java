package games.azul.gui;

import games.azul.AzulGameState;
import games.azul.components.AzulFactoryBoard;
import games.azul.tiles.AzulTile;
import gui.IScreenHighlight;
import gui.views.ComponentView;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * View component responsible for rendering the Azul factory board and handling user interactions.
 * Supports tile highlighting and displays tiles with appropriate colours based on type.
 */
public class AzulFactoryBoardView extends ComponentView implements IScreenHighlight {

    AzulGameState gs;

    Rectangle[] rects; // Use for highlights
    ArrayList<Rectangle> highlight;

    AzulFactoryBoard azulFactory;

    Color colorName;

    int offsetX = 10;
    int offsetY = 10;

    private Map<Rectangle, AzulTile> rectToColorMap = new HashMap();

    /**
     * Constructs a view for an Azul factory board.
     * Sets up mouse interactions for highlighting tiles.
     *
     * @param azulFactory - The factory baord to render.
     * @param gs - The current game state.
     */
    public AzulFactoryBoardView(AzulFactoryBoard azulFactory, AzulGameState gs) {
        super(null, 0, 0);
        this.gs = gs;
        rects = new Rectangle[azulFactory.factoryBoard.length];
        highlight = new ArrayList<>();
        this.azulFactory = azulFactory;

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    // Left click, highlight cell
                    for (Rectangle r : rects) {
                        if (r != null && r.contains(e.getPoint())) {
                            highlight.clear();
                            highlight.add(r);

                            // Fetch and print color of the clicked cell
                            AzulTile tile = rectToColorMap.get(r);
                            if (tile != null) {
                                colorName = tile.getColor();
                            }

                            repaint();
                            break;
                        }
                    }
                } else {
                    highlight.clear(); // Remove highlight
                    repaint();
                }
            }
        });
    }

    /**
     * Paints the factory board tiles and any highlights.
     *
     * @param g1 the <code>Graphics</code> object to protect.
     */
    @Override
    protected void paintComponent(Graphics g1) {
        Graphics2D g = (Graphics2D) g1;

        drawFactoryBoard(g, this.azulFactory, offsetX, offsetY);

        if (!highlight.isEmpty()) {
            g.setColor(Color.green);
            Stroke s = g.getStroke();
            g.setStroke(new BasicStroke(3));

            Rectangle r = highlight.get(0);
            g.drawRect(r.x, r.y, r.width, r.height);
            g.setStroke(s);
        }
    }

    /**
     * Renders the Azul factory board tiles in a single row with spacing.
     * Also updates the mapping of rectangles to tile values for interaction.
     *
     * @param g - The Graphics2D context used for drawing.
     * @param azulFactory - The factory board to render.
     * @param x - X-offset for drawing.
     * @param y - Y-offset for drawing.
     */
    private void drawFactoryBoard(Graphics2D g, AzulFactoryBoard azulFactory, int x, int y) {
        // Set up tile size, spacing, and factory length
        int tileSize = 40;
        int spacing = 5;
        int rowCount = 1;
        int colCount = azulFactory.factoryBoard.length;

        g.setFont(new Font("Arial", Font.PLAIN, 12));

        for (int i = 0; i < colCount; i++) {
            int xC = x + (i * (tileSize + spacing));
            int yC = y;

            AzulTile tile = azulFactory.factoryBoard[i];

            // Draw the tile as a border and then add color text
            Color tileColor = (tile != null && tile != AzulTile.Empty) ? getTileColor(tile) : Color.LIGHT_GRAY;
            g.setColor(tileColor);
            g.fillRect(xC, yC, tileSize, tileSize);

            // Draw tile border
            g.setColor(Color.BLACK);
            g.drawRect(xC, yC, tileSize, tileSize);

            rects[i] = new Rectangle(xC, yC, tileSize, tileSize);  // Update the rectangle map for highlights
            rectToColorMap.put(rects[i], tile);
        }
    }

    /**
     * Updates the component to reflect a new or changed factory board.
     * Clears highlights and redraws.
     *
     * @param updatedFactoryBoard - The new AzulFactoryBoard to display.
     */
    public void updateComponent(AzulFactoryBoard updatedFactoryBoard) {
        if (updatedFactoryBoard == null) return;

        this.azulFactory = updatedFactoryBoard;

        // Clear and recreate the rectangles for highlights
        rects = new Rectangle[azulFactory.factoryBoard.length];
        rectToColorMap.clear();
        highlight.clear();

        revalidate();
        repaint();
    }

    /**
     * Returns the display colour corresponding to an AzulTile enum.
     *
     * @param tile - The Azul tile type.
     * @return the associated colour.
     */
    private Color getTileColor(AzulTile tile) {
        return switch (tile) {
            case White -> AzulTile.White.getColor();
            case Black -> AzulTile.Black.getColor();
            case Red -> AzulTile.Red.getColor();
            case Orange -> AzulTile.Orange.getColor();
            case Blue -> AzulTile.Blue.getColor();
            default -> AzulTile.Empty.getColor();
        };
    }

    /**
     * Returns the currently highlighted rectangles (tiles).
     *
     * @return a list of highlighted tile rectangles.
     */
    public ArrayList<Rectangle> getHighlight() {
        return highlight;
    }

    @Override
    public void clearHighlights() {
        highlight.clear();
    }
}