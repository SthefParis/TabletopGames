package games.azul.gui;

import games.azul.AzulGameState;
import games.azul.AzulParameters;
import games.azul.components.AzulPlayerBoard;
import games.azul.tiles.AzulTile;
import gui.GUI;
import gui.IScreenHighlight;
import gui.views.ComponentView;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class AzulPlayerBoardView extends ComponentView implements IScreenHighlight {

    AzulGameState ags;

    Rectangle[] rects;
    ArrayList<Rectangle> highlight;

    AzulPlayerBoard azulPlayerBoard;

    final static int offsetX = 50;
    final static int offsetY = 50;

    final static int marginBetweenBoards = 50; // Space between the boards

    final static int colorFontSize = 8;
    final static int defaultFontSize = 12;
    final static int tileSize = 40;

    int defaultWidth = GUI.defaultItemSize/2;
    int defaultHeight = GUI.defaultItemSize/2;


    public AzulPlayerBoardView(AzulPlayerBoard azulPlayerBoard, AzulGameState ags) {
        super(null, 0, 0); //What goes in c??
        this.ags = ags;

        rects = new Rectangle[azulPlayerBoard.playerWall.length*azulPlayerBoard.playerPatternWall.length];
        highlight = new ArrayList<>();
        this.azulPlayerBoard = azulPlayerBoard;


        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    // Left click, highlight cell
                    for (Rectangle r : rects) {
                        if (r != null && r.contains(e.getPoint())) {
                            highlight.clear();
                            highlight.add(r);

                            repaint();
                            break;
                        }
                    }
                } else {
                    highlight.clear(); // Remove highlight
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g1) {
        Graphics2D g = (Graphics2D) g1;

        // Calculate dimensions
        int tempBoardHeight = azulPlayerBoard.playerPatternWall.length * tileSize;
        int tempBoardWidth = azulPlayerBoard.playerPatternWall.length * tileSize;
        int playerBoardHeight = azulPlayerBoard.playerWall.length * tileSize;
        int totalBoardHeight = Math.max(tempBoardHeight, playerBoardHeight);

        int scoreTrackHeight = azulPlayerBoard.playerScoreTrack.length * GUI.defaultItemSize / 2;

        // Calculate the x-offset for the player board (to the right of the temp board)
        int playerBoardOffsetX = offsetX + tempBoardWidth + marginBetweenBoards;

        // Calculate the x-offset for the floor line (below player and temp board)
        int floorLineOffsetY = offsetY + totalBoardHeight + marginBetweenBoards*2;

        // Calculates the y-offset for the score track (above player and temp board)
        int scoreTrackOffsetY = GUI.defaultItemSize/2;

        // Calculate the y-offset for the player board (below the score track)
        int playerBoardOffsetY = offsetY + scoreTrackOffsetY*2 + 10;

        // Draw the temporary board (left)
        drawPlayerTempBoard(g, this.azulPlayerBoard, offsetX, playerBoardOffsetY);

        // Draw the mosaic playerWall (right)
        drawPlayerBoard(g, this.azulPlayerBoard, playerBoardOffsetX, playerBoardOffsetY);

        // Draw the floor line (above temp board and playerWall)
        drawFloorLine(g, this.azulPlayerBoard, offsetX, floorLineOffsetY);

        drawScoreTrack(g, this.azulPlayerBoard, offsetX, scoreTrackOffsetY);

        if (!highlight.isEmpty()){
            g.setColor(Color.green);
            Stroke s = g.getStroke();
            g.setStroke(new BasicStroke(3));

            Rectangle r = highlight.get(0);
            g.drawRect(r.x, r.y, r.width, r.height);
            g.setStroke(s);
        }
    }

    // Draws Wall
    private void drawPlayerBoard(Graphics2D g, AzulPlayerBoard playerBoard, int x, int y) {

        // Iterate over the playerWall to draw each tile
        for (int i = 0; i < playerBoard.playerWall.length; i++) {
            for (int j = 0; j < playerBoard.playerWall[i].length; j++) {
                int xC = x + j * tileSize;
                int yC = y + i * tileSize;

                AzulTile tile = playerBoard.playerWall[i][j];
//                System.out.println("Tile in player wall: " + tile.getTileType());

                drawCell(g, tile, xC, yC, true, i, j);
            }
        }
    }

    // Draws Pattern Line from right to left
    private void drawPlayerTempBoard(Graphics2D g, AzulPlayerBoard playerBoard, int x, int y) {
//        System.out.println("Printing temp board");
        int spacing = 0;

        g.setFont(new Font("Arial", Font.PLAIN, defaultFontSize));

        for (int i = 0; i < playerBoard.playerPatternWall.length; i++) {
            // Calculate the starting x-coordinate for the current row (right-aligned)
            int startX = x + (5 - 1 - i) * (tileSize);

            for (int j = 0; j <= i; j++) {
                int xC = startX + j * (tileSize + spacing); // Position cells from right to left
                int yC = y + i * (tileSize + spacing);      // Row positioning

                AzulTile tile = playerBoard.playerPatternWall[i][j];

                // Draw background color for the tile (empty slots should be grey)
                drawCell(g, tile, xC, yC, false, -1, -1);

                // Save rect position
                int idx = i * playerBoard.playerPatternWall.length + j;
                if (rects[idx] == null) {
                    rects[idx] = new Rectangle(xC, yC, tileSize, tileSize);
                }
            }
        }
    }

    private void drawFloorLine(Graphics2D g, AzulPlayerBoard playerFloorLine,int x, int y) {
        AzulParameters params =(AzulParameters) ags.getGameParameters();
        int[] floorPenalties = params.getFloorPenalties();

        for (int i = 0; i < playerFloorLine.playerFloorLine.length; i++) {
            int startX = x + i * (tileSize + 5);

            // Get tile at current floor line index
            AzulTile tile = azulPlayerBoard.playerFloorLine[i];

            // Determine tile color (empty slots should be grey)
            drawCell(g, tile, startX, y, false, -1, -1);

            // Draw floor penalty points inside each tile with a white outline
            g.setFont(new Font("Arial", Font.BOLD, defaultFontSize));
            String text = String.valueOf(floorPenalties[i]);

            FontMetrics metrics = g.getFontMetrics();
            int textWidth = metrics.stringWidth(text);
            int textHeight = metrics.getAscent();
            int textX = startX + (tileSize - textWidth) / 2;
            int textY = y + (tileSize + textHeight) / 2 - 3;

            // White outline
            g.setColor(Color.WHITE);
            g.drawString(text, textX - 1, textY);
            g.drawString(text, textX + 1, textY);
            g.drawString(text, textX, textY - 1);
            g.drawString(text, textX, textY + 1);

            // Actual text in black
            g.setColor(Color.BLACK);
            g.drawString(text, textX, textY);

        }
    }

    private void drawScoreTrack(Graphics2D g, AzulPlayerBoard playerBoard, int x, int y) {
        int cellsPerRow = 20;
        int cellWidth = defaultWidth/2;
        int cellHeight = defaultHeight/2;

        if (playerBoard == null || playerBoard.playerScoreTrack == null) return;

        int playerID = playerBoard.playerID;
        if (playerID < 0 || playerID >= ags.getNPlayers()) return;

        int playerScore = (int) this.ags.getGameScore(playerID);
        int trackLength = playerBoard.playerScoreTrack.length;

        // Draw cell 0
        boolean isOrange = (0 % 5 == 0);
        drawScoreTrackCell(g, x, y, cellWidth, cellHeight, playerScore == 0 ? Color.BLACK : Color.ORANGE, isOrange ? 0 : -1);

        for (int i = 1; i < trackLength; i++) {
            // Calculate row and column based on index and cells per row
            int row = (i - 1) / cellsPerRow + 1;
            int col = (i - 1) % cellsPerRow;

            // Calculate position of cells without spacing
            int startX = x + col * cellWidth;
            int startY = y + row * cellHeight;

            // Determine cell color
            Color cellColor = (i == playerScore) ? Color.BLACK
                    : (i % 5 == 0) ? Color.ORANGE
                    : Color.WHITE;

            drawScoreTrackCell(g, startX, startY, cellWidth, cellHeight, cellColor, (cellColor == Color.ORANGE) ? i : -1);
        }
    }

    private void drawScoreTrackCell(Graphics2D g, int x, int y, int width, int height, Color bgColor, int number){
        // Paint cell background
        g.setColor(bgColor);
        g.fillRect(x, y, width, height);

        g.setColor(Color.black);
        g.drawRect(x, y, width, height);

        // Draw number if the cell is orange
        if (number >= 0) {
            g.setFont(new Font("Arial", Font.BOLD, colorFontSize));
            String text = String.valueOf(number);
            FontMetrics metrics = g.getFontMetrics();
            int textX = x + (width - metrics.stringWidth(text)) / 2;
            int textY = y + ((height - metrics.getHeight()) / 2) + metrics.getAscent();
            g.drawString(text, textX, textY);
        }
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

    private void drawCell(Graphics2D g, AzulTile tile, int xC, int yC, boolean isWallCell, int row, int col) {
        Color tileColor;

        AzulTile[] tileOrder = {
                AzulTile.Blue, AzulTile.Orange, AzulTile.Red, AzulTile.Black, AzulTile.White
        };

        if (tile != null && tile != AzulTile.Empty) {
            tileColor = getTileColor(tile);
        } else if (isWallCell) {
            int index = (col - row + 5) % 5;  // Proper Azul pattern
            Color baseColor = tileOrder[index].getColor();
            tileColor = new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 50); // semi-transparent
        } else {
            tileColor = Color.LIGHT_GRAY;
        }

        g.setColor(tileColor);
        g.fillRect(xC, yC, tileSize, tileSize);

        // Draw tile border
        g.setColor(Color.BLACK);
        g.drawRect(xC, yC, tileSize, tileSize);
    }

    public void updateComponent(AzulPlayerBoard updatedPlayerBoard) {
        if (updatedPlayerBoard == null) return;

        // Update the playerboard
        this.azulPlayerBoard = updatedPlayerBoard;

        // Clear and recreate rectangles for highlights
        rects = new Rectangle[azulPlayerBoard.playerPatternWall.length];
        highlight.clear();

        revalidate();
        repaint();
    }

    public ArrayList<Rectangle> getHighlight() { return highlight; }

    @Override
    public void clearHighlights() { highlight.clear(); }

}