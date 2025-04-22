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

        return switch (color.getRGB()) {
            case 0xFFFFFFFF -> "White";   // Color.WHITE
            case 0xFF000000 -> "Black";   // Color.BLACK
            case 0xFFFF0000 -> "Red";     // Color.RED
            case 0xFFFFA500 -> "Orange";  // Color.ORANGE
            case 0xFF0000FF -> "Blue";    // Color.BLUE
            case 0xFFFF00FF -> "Magenta"; // Color.MAGENTA
            default -> "";
        };
    }
}
