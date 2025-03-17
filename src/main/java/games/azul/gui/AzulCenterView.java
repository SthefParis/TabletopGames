package games.azul.gui;

import games.azul.components.AzulCenter;
import games.azul.tiles.AzulTile;
import gui.views.ComponentView;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AzulCenterView extends ComponentView {

    private AzulCenter center;

    int offsetX = 10;
    int offsetY = 10;

    public AzulCenterView(AzulCenter center) {
        super(center, 0, 0);
        this.center = center;
    }

    @Override
    protected void paintComponent(Graphics g1) {
        Graphics2D g = (Graphics2D) g1;

        drawCenter(g, this.center, offsetX, offsetY);
    }

    private void drawCenter(Graphics2D g, AzulCenter center, int x, int y) {
        // Set up tile size and spacing
        int tileSize = 40;  // You can adjust this value
        int spacing = 10;  // Space between tiles

        List<AzulTile> tiles = center.getTiles();
        int rowCount = 0;
        int colCount = 0;

        // Set the font for the tile color text
        g.setFont(new Font("Arial", Font.PLAIN, 12));

        // Loop through each tile and draw the text representing its color
        for (AzulTile tile : tiles) {
            // Calculate the position of the tile
            int tileX = x + (colCount * (tileSize + spacing));
            int tileY = y + (rowCount * (tileSize + spacing));

            Color tileColor = (tile != null && tile != AzulTile.Empty) ? getTileColor(tile) : Color.LIGHT_GRAY;
            g.setColor(tileColor);
            g.fillRect(tileX, tileY, tileSize, tileSize);

            // Draw the tile as an empty rectangle (no fill)
            g.setColor(Color.BLACK);  // Set the color for the rectangle's border
            g.drawRect(tileX, tileY, tileSize, tileSize);

            // Move to the next column
            colCount++;

            // If we have reached the end of the row, move to the next row
            if (colCount >= 5) {  // Assuming a max of 5 tiles per row (adjust if needed)
                colCount = 0;
                rowCount++;
            }
        }

    }

    public void updateComponent() {
        this.removeAll();
        List<AzulTile> tiles = center.getTiles();

        for (AzulTile tile : tiles) {
            JLabel tileLabel = new JLabel(tile.toString());
            tileLabel.setOpaque(true);
            tileLabel.setBackground(Color.LIGHT_GRAY);
            tileLabel.setPreferredSize(new Dimension(40,40));
            this.add(tileLabel);
        }

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
            case FirstPlayer -> AzulTile.FirstPlayer.getColor();
            default -> AzulTile.Empty.getColor();
        };
    }
}
