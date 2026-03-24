package org.skiBums.Tiles;

import org.skiBums.GameConstants;
import org.skiBums.ResourceType;
import org.skiBums.geometry.HexCoord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Tile {

    private HexCoord hexCoord;
    private int rollNumber;

    public abstract boolean isDesert();

    abstract boolean isLand();

    public void setRollNumber(int rollNumber) {
        this.rollNumber = rollNumber;
    }

    public void setHexCoord(HexCoord hexCoord) {
        this.hexCoord = hexCoord;
    }

    public HexCoord getHexCoord() {
        return hexCoord;
    }

    public static Map<HexCoord, Tile> setUpTiles() {
        List<Tile> startTiles = new ArrayList<>();
        for (int i = 0; i < GameConstants.DESERT_TILE_AMOUNT; i++) {
            startTiles.add(new LandTile(ResourceType.DESERT));
        }
        for (int i = 0; i < GameConstants.WOOD_TILE_AMOUNT; i++) {
            startTiles.add(new LandTile(ResourceType.WOOD));
        }
        for (int i = 0; i < GameConstants.WHEAT_TILE_AMOUNT; i++) {
            startTiles.add(new LandTile(ResourceType.WHEAT));
        }
        for (int i = 0; i < GameConstants.SHEEP_TILE_AMOUNT; i++) {
            startTiles.add(new LandTile(ResourceType.SHEEP));
        }
        for (int i = 0; i < GameConstants.ROCK_TILE_AMOUNT; i++) {
            startTiles.add(new LandTile(ResourceType.ROCK));
        }
        for (int i = 0; i < GameConstants.BRICK_TILE_AMOUNT; i++) {
            startTiles.add(new LandTile(ResourceType.BRICK));
        }
        Collections.shuffle(startTiles);

        Map<HexCoord, Tile> map = new HashMap<>();
        List<HexCoord> positions = GameConstants.LAND_HEX_ORDER;
        for (int i = 0; i < positions.size(); i++) {
            HexCoord c = positions.get(i);
            Tile t = startTiles.get(i);
            t.setHexCoord(c);
            map.put(c, t);
        }
        return map;
    }
}
