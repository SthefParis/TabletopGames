package games.azul.tiles;

import java.awt.*;

public enum AzulTile {
    Empty(Color.GRAY),
    White(Color.WHITE),
    Black(Color.BLACK, "data/azul/images/tiles/Black.jpg"),
    Red(Color.RED, "data/azul/images/tiles/Red.jpg"),
    Orange(Color.ORANGE, "data/azul/images/tiles/Orange.jpg"),
    Blue(Color.BLUE, "data/azul/images/tiles/Blue.jpg"),
    FirstPlayer(Color.MAGENTA);

    // Fields for color code and image path
    private final Color colorCode;
    private final String imagePath;

    // Constructor for tiles without an image path
    AzulTile(Color color) {
        this.colorCode = color;
        this.imagePath = null;
    }

    // Constructor for tiles with an image path
    AzulTile(Color color, String imagePath) {
        this.colorCode = color;
        this.imagePath = imagePath;
    }

    public Color getColor() {
        return colorCode;
    }

    public String getImagePath() {
        return imagePath;
    }

    // Returns the tile type
    public AzulTile getTileType() {
        return this;
    }

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
