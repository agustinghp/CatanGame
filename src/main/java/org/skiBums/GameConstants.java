package org.skiBums;

import org.skiBums.geometry.HexCoord;

import java.util.List;
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

    /** Shuffle assigns terrain in this order (same island geometry as before, axial only). */
    public static final List<HexCoord> LAND_HEX_ORDER =
            List.of(
                    new HexCoord(-2, 0),
                    new HexCoord(-3, 1),
                    new HexCoord(-3, 2),
                    new HexCoord(-1, -1),
                    new HexCoord(-1, 0),
                    new HexCoord(-2, 1),
                    new HexCoord(-2, 2),
                    new HexCoord(1, -2),
                    new HexCoord(0, -1),
                    new HexCoord(0, 0),
                    new HexCoord(-1, 1),
                    new HexCoord(-1, 2),
                    new HexCoord(2, -2),
                    new HexCoord(1, -1),
                    new HexCoord(1, 0),
                    new HexCoord(0, 1),
                    new HexCoord(3, -2),
                    new HexCoord(2, -1),
                    new HexCoord(2, 0));

    /** Number-chit spiral (axial), same path as the old grid spiral. */
    public static final List<HexCoord> LAND_HEX_NUMBER_SPIRAL_ORDER =
            List.of(
                    new HexCoord(-2, 0),
                    new HexCoord(-3, 1),
                    new HexCoord(-3, 2),
                    new HexCoord(-2, 2),
                    new HexCoord(-1, 2),
                    new HexCoord(0, 1),
                    new HexCoord(2, 0),
                    new HexCoord(2, -1),
                    new HexCoord(3, -2),
                    new HexCoord(2, -2),
                    new HexCoord(1, -2),
                    new HexCoord(-1, -1),
                    new HexCoord(-1, 0),
                    new HexCoord(-2, 1),
                    new HexCoord(-1, 1),
                    new HexCoord(1, 0),
                    new HexCoord(1, -1),
                    new HexCoord(0, -1),
                    new HexCoord(0, 0));

    public static final Set<HexCoord> ALL_LAND_HEXES = Set.copyOf(LAND_HEX_ORDER);
}
