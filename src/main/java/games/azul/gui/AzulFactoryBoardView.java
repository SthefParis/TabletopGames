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

public class AzulFactoryBoardView extends ComponentView implements IScreenHighlight {

    AzulGameState gs;

    Rectangle[] rects; // Use for highlights
    ArrayList<Rectangle> highlight;

    AzulFactoryBoard azulFactory;

    Color colorName;

    int offsetX = 10;
    int offsetY = 10;

    private Map<Rectangle, AzulTile> rectToColorMap = new HashMap();

//    private String selectedTile;
//    private final int selectedFactory;

    public AzulFactoryBoardView(AzulFactoryBoard azulFactory, AzulGameState gs, int selectedFactory) {
        super(null, 0, 0);
        this.gs = gs;
//        this.selectedFactory = selectedFactory;
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
//                                selectedTile = colorName;
                                System.out.println("Clicked cell color: " + colorName);
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

    @Override
    protected void paintComponent(Graphics g1) {
        Graphics2D g = (Graphics2D) g1;

        drawFactoryBoard(g, this.azulFactory, offsetX, offsetY);

        if (highlight.size() > 0) {
            g.setColor(Color.green);
            Stroke s = g.getStroke();
            g.setStroke(new BasicStroke(3));

            Rectangle r = highlight.get(0);
            g.drawRect(r.x, r.y, r.width, r.height);
            g.setStroke(s);
        }
    }

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

    // Helper function to get colors for Azul tiles
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

    public ArrayList<Rectangle> getHighlight() {
        return highlight;
    }

    @Override
    public void clearHighlights() {
        highlight.clear();
    }
}
