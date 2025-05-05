package games.azul.gui;

import games.azul.components.AzulCenter;
import games.azul.tiles.AzulTile;
import gui.views.ComponentView;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Visual component responsible for rendering the tiles in the center.
 * This component draws a grid of colored squares representing Azul tiles.
 */
public class AzulCenterView extends ComponentView {

    private final AzulCenter center;

    private final int tileSize;
    private final int spacing;
    private final int tilesPerRow;

    private final int offsetX = 10;
    private final int offsetY = 10;


    /**
     * Constructs the view for displaying the Azul center tiles.
     *
     * @param center - The AzulCenter model containing the tiles.
     * @param tileSize - The size of each tile (width and height).
     * @param tilesPerRow - The maximum number of tiles per row in the layout.
     */
    public AzulCenterView(AzulCenter center, int tileSize, int spacing, int tilesPerRow) {
        super(center, 0, 0);
        this.center = center;
        this.tileSize = tileSize;
        this.spacing = spacing;
        this.tilesPerRow = tilesPerRow;
    }

    /**
     * Draws all Azul tiles in a grid format with their corresponding colours.
     *
     * @param g1 - The <code>Graphics</code> object to protect
     */
    @Override
    protected void paintComponent(Graphics g1) {
        Graphics2D g = (Graphics2D) g1;

        drawTilesGrid(g, center.getTiles(), offsetX, offsetY);
    }

    /**
     * Renders the tiles as a grid of colored rectangles.
     *
     * @param g - The Graphics2D object used to draw.
     * @param tiles - The list of tiles to render.
     * @param offsetX - Horizontal offset from the component origin.
     * @param offsetY - Vertical offset from the component origin.
     */
    private void drawTilesGrid(Graphics2D g, List<AzulTile> tiles, int offsetX, int offsetY) {
        g.setFont(new Font("Arial", Font.PLAIN, 12));

        List<TilePosition> positions = AzulTileLayout.computeLayout(tiles, tileSize, spacing, tilesPerRow);
        for (TilePosition pos : positions) {
            int drawX = offsetX + pos.x();
            int drawY = offsetY + pos.y();
            Color tileColor = (pos.tile() != null && pos.tile() != AzulTile.Empty)
                    ? getTileColor(pos.tile())
                    : Color.LIGHT_GRAY;

            g.setColor(tileColor);
            g.fillRect(drawX, drawY, tileSize, tileSize);

            g.setColor(Color.BLACK);
            g.drawRect(drawX, drawY, tileSize, tileSize);
        }
    }

    /**
     * Maps on AzulTile enum to its corresponding display colour.
     *
     * @param tile - The AzulTile to covert.
     * @return the colour associated with the tile.
     */
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

    /**
     * Represents a tile's position in the layout grid.
     *
     * @param x - X position.
     * @param y - Y position.
     * @param tile - Tile to place in position (x, y).
     */
    private record TilePosition(int x, int y, AzulTile tile) {}

    /**
     * Helper class that computes the x/y positions for each tile in a grid.
     */
    private static class AzulTileLayout {

        /**
         * Calculates layout positions for tiles arranged in a grid.
         *
         * @param tiles - The list of tiles to lay out.
         * @param tileSize - Size of each tile.
         * @param spacing - Spacing between tiles.
         * @param tilesPerRow - Maximum tiles per row.
         * @return a list of TilePosition objects containing coordinates and tile references.
         */
        public static List<TilePosition> computeLayout(List<AzulTile> tiles, int tileSize, int spacing, int tilesPerRow) {
            List<TilePosition> positions = new ArrayList<>();
            int row = 0, col = 0;
            for (AzulTile tile : tiles) {
                int x = col * (tileSize + spacing);
                int y = row * (tileSize + spacing);
                positions.add(new TilePosition(x, y, tile));
                col++;
                if (col >= tilesPerRow) {
                    col = 0;
                    row++;
                }
            }
            return positions;
        }
    }
}
