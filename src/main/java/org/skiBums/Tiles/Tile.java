package org.skiBums.Tiles;

import org.skiBums.GameConstants;
import org.skiBums.ResourceType;
import org.skiBums.geometry.HexCoord;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.skiBums.ResourceType.DESERT;

public class Tile {

    private HexCoord hexCoord;
    private int rollNumber;
    private final ResourceType resourceType;


    public Tile(ResourceType resourceType) {
        this.resourceType = resourceType;
    }
    public void setRollNumber(int rollNumber) {
        this.rollNumber = rollNumber;
    }

    public int getRollNumber() {
        return this.rollNumber;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public boolean isDesert() {
        return resourceType == DESERT;
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
            startTiles.add(new Tile(ResourceType.DESERT));
        }
        for (int i = 0; i < GameConstants.WOOD_TILE_AMOUNT; i++) {
            startTiles.add(new Tile(ResourceType.WOOD));
        }
        for (int i = 0; i < GameConstants.WHEAT_TILE_AMOUNT; i++) {
            startTiles.add(new Tile(ResourceType.WHEAT));
        }
        for (int i = 0; i < GameConstants.SHEEP_TILE_AMOUNT; i++) {
            startTiles.add(new Tile(ResourceType.SHEEP));
        }
        for (int i = 0; i < GameConstants.ROCK_TILE_AMOUNT; i++) {
            startTiles.add(new Tile(ResourceType.ROCK));
        }
        for (int i = 0; i < GameConstants.BRICK_TILE_AMOUNT; i++) {
            startTiles.add(new Tile(ResourceType.BRICK));
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
