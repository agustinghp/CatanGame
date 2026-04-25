package org.skiBums;

import org.skiBums.geometry.HexCoord;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class GameConstants {
    private GameConstants() {}

    public static final int TILES_AMOUNT = 19;
    public static final int ROCK_TILE_AMOUNT = 3;
    public static final int WHEAT_TILE_AMOUNT = 4;
    public static final int SHEEP_TILE_AMOUNT = 4;
    public static final int BRICK_TILE_AMOUNT = 3;
    public static final int WOOD_TILE_AMOUNT = 4;
    public static final int DESERT_TILE_AMOUNT = 1;
    public static final List<Integer> LIST_OF_ROLL_NUMS =
            List.of(5, 2, 6, 3, 8, 10, 9, 12, 11, 4, 8, 10, 9, 4, 5, 6, 3, 11);


    public static final List<HexCoord> LAND_HEX_ORDER =
            List.of(
                    new HexCoord(0, -2),
                    new HexCoord(1, -2),
                    new HexCoord(2, -2),
                    new HexCoord(-1, -1),
                    new HexCoord(0, -1),
                    new HexCoord(1, -1),
                    new HexCoord(2, -1),
                    new HexCoord(-2, 0),
                    new HexCoord(-1, 0),
                    new HexCoord(0, 0),
                    new HexCoord(1, 0),
                    new HexCoord(2, 0),
                    new HexCoord(-2, 1),
                    new HexCoord(-1, 1),
                    new HexCoord(0, 1),
                    new HexCoord(1, 1),
                    new HexCoord(-2, 2),
                    new HexCoord(-1, 2),
                    new HexCoord(0, 2));


    public static final List<HexCoord> LAND_HEX_NUMBER_SPIRAL_ORDER =
            List.of(
                    new HexCoord(2, -2),
                    new HexCoord(2, -1),
                    new HexCoord(2, 0),
                    new HexCoord(1, 1),
                    new HexCoord(0, 2),
                    new HexCoord(-1, 2),
                    new HexCoord(-2, 2),
                    new HexCoord(-2, 1),
                    new HexCoord(-2, 0),
                    new HexCoord(-1, -1),
                    new HexCoord(0, -2),
                    new HexCoord(1, -2),
                    new HexCoord(1, -1),
                    new HexCoord(1, 0),
                    new HexCoord(0, 1),
                    new HexCoord(-1, 1),
                    new HexCoord(-1, 0),
                    new HexCoord(0, -1),
                    new HexCoord(0, 0));

    public static final Set<HexCoord> ALL_LAND_HEXES = Set.copyOf(LAND_HEX_ORDER);
    public static final Map<ResourceType, Integer> ROAD_COST = Map.of(
            ResourceType.BRICK, 1,
            ResourceType.WOOD, 1
    );
    public static final Map<ResourceType, Integer> SETTLEMENT_COST = Map.of(
            ResourceType.BRICK, 1,
            ResourceType.WOOD, 1,
            ResourceType.WHEAT, 1,
            ResourceType.SHEEP, 1
    );
    public static final Map<ResourceType, Integer> CITY_COST = Map.of(
            ResourceType.ROCK, 3,
            ResourceType.WHEAT, 2
    );
    public static final int BANK_TRADE_RATE = 4;
    public static final int AI_TURN_DELAY_MS = 50;
    public static final int WINNING_VICTORY_POINTS = 10;

    public static Color colorForResource(ResourceType resourceType) {
        return switch (resourceType) {
            case WOOD -> new Color(34, 139, 34);
            case BRICK -> new Color(178, 34, 34);
            case SHEEP -> new Color(144, 238, 144);
            case WHEAT -> new Color(255, 215, 0);
            case ROCK -> new Color(128, 128, 128);
            case DESERT -> new Color(238, 203, 173);
        };
    }

}