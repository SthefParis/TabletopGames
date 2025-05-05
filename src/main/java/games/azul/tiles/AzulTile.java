package games.azul.tiles;

import java.awt.*;

public enum AzulTile {
    Empty(Color.GRAY),
    White(Color.WHITE),
    Black(Color.BLACK),
    Red(Color.RED),
    Orange(Color.ORANGE),
    Blue(Color.BLUE),
    FirstPlayer(Color.MAGENTA);

    // Stores the colour associated with the tile
    private final Color color;

    // Constructor for tiles without an image path
    AzulTile(Color color) {
        this.color = color;
    }

    /**
     * Gets the colour object associated with the tile.
     *
     * @return the colour object.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Gets the tile type of the tile.
     *
     * @return the tile type.
     */
    public AzulTile getTileType() {
        return this;
    }

    /**
     * Converts a colour object to its corresponding colour name as a String.
     *
     * @param color - The colour to be converted to a string.
     * @return a String of the colour.
     */
    public String getColourAsString(Color color) {
        String colorAsString = "";

        if (color == Color.WHITE) colorAsString = "White";
        if (color == Color.BLACK) colorAsString = "Black";
        if (color == Color.RED) colorAsString = "Red";
        if (color == Color.ORANGE) colorAsString = "Orange";
        if (color == Color.BLUE) colorAsString = "Blue";
        if (color == Color.MAGENTA) colorAsString = "Magenta";
        
        return colorAsString;
    }
}