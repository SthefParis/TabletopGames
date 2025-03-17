package games.azul.components;

import core.CoreConstants;
import core.components.Component;
import games.azul.AzulGameState;
import games.azul.tiles.AzulTile;

import java.util.*;

public class AzulCenter extends Component {

    public List<AzulTile> center;

    public AzulCenter() { super(CoreConstants.ComponentType.BOARD, "AzulCenter"); }

    public AzulCenter(int componentID) {
        super(CoreConstants.ComponentType.BOARD, "AzulXenter", componentID);
    }

    public void initialise(AzulGameState ags) {
        this.center = new ArrayList<>();
        this.center.add(AzulTile.FirstPlayer);
    }

    public void addTile(AzulTile tile) { center.add(tile); }

    public void addTiles(AzulTile[] tiles) {
        for (AzulTile tile : tiles) {
            if (tile != AzulTile.Empty) {
                center.add(tile);
            }
        }
    }

    public boolean removeTile(AzulGameState ags, AzulTile tile) {

        boolean tileRemoved = false;
        int tilesRemoved = 0;

        Iterator<AzulTile> iterator = center.iterator();
        while (iterator.hasNext()) {
            if (iterator.next() == tile) {
                iterator.remove();
                tilesRemoved++;
                tileRemoved = true;
            }
        }

        ags.setNumOfTilesPicked(tilesRemoved);

        System.out.println("Center after: " + Arrays.toString(center.toArray()));
        System.out.println("Number of tiles removed: " + tilesRemoved);

        return tileRemoved;
    }

    public List<AzulTile> getTiles() { return new ArrayList<>(center); }

    public boolean isEmpty() { return center.isEmpty(); }

    public Set<AzulTile> getTileTypes() {
        Set<AzulTile> tiles = new HashSet<>();

        for (AzulTile tile : center) {
            if (tile != AzulTile.Empty) {
                tiles.add(tile);
            }
        }

        return tiles;
    }

    @Override
    public Component copy() {
        AzulCenter copy = new AzulCenter(componentID);
        copyComponentTo(copy);
        copy.center = new ArrayList<>(this.center);
        return copy;
    }
}
