package org.skiBums.geometry;

import java.util.ArrayList;
import java.util.List;

public record HexCoord(int q, int r) {

    public static final int[][] AXIAL_DIRECTIONS = {
            {1, 0},
            {1, -1},
            {0, -1},
            {-1, 0},
            {-1, 1},
            {0, 1},
    };

    public static int oppositeDirection(int d) {
        return (d + 3) % 6;
    }

    public HexCoord neighbor(int directionIndex) {
        int[] d = AXIAL_DIRECTIONS[directionIndex];
        return new HexCoord(q + d[0], r + d[1]);
    }

    public List<HexCoord> allNeighbors() {
        List<HexCoord> list = new ArrayList<>(6);
        for (int i = 0; i < 6; i++) {
            list.add(neighbor(i));
        }
        return list;
    }


    public HexCoord add(HexCoord delta) {
        return new HexCoord(q + delta.q, r + delta.r);
    }
}
