package games.azul.components;

import games.azul.AzulParameters;
import games.azul.tiles.AzulTile;

public class AzulWallPattern {
    public AzulTile[][] patternPlayerBoard;

    public void initialise(AzulParameters params) {
        int boardSize = params.getBoardSize();
        this.patternPlayerBoard = new AzulTile[boardSize][boardSize];

        AzulTile[] tileOrder = {
                AzulTile.Blue, AzulTile.Orange, AzulTile.Red, AzulTile.Black, AzulTile.White
        };

        // Fill the board according to the Azul pattern
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                patternPlayerBoard[row][col] = tileOrder[(col - row + tileOrder.length) % tileOrder.length];
            }
        }
    }

    public int getTileColPositionInRow(int row, AzulTile tile) {
        if (row < 0 || row >= patternPlayerBoard.length) {
            return -1;
        }

        for (int col = 0; col < patternPlayerBoard[row].length; col++) {
            if (patternPlayerBoard[row][col] == tile) {
                return col;
            }
        }

        return -1;
    }
}
