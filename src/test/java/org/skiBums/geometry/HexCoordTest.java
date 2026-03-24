package org.skiBums.geometry;

import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class HexCoordTest {

    @Test
    void neighborAndOppositeRoundTrip() {
        HexCoord origin = new HexCoord(0, 0);
        for (int d = 0; d < 6; d++) {
            HexCoord out = origin.neighbor(d);
            HexCoord back = out.neighbor(HexCoord.oppositeDirection(d));
            assertEquals(origin, back, "direction " + d);
        }
    }

    @Test

    void sixNeighborsAreDistinct() {
        HexCoord origin = new HexCoord(2, -1);
        var set = new HashSet<HexCoord>();
        for (HexCoord n : origin.allNeighbors()) {
            set.add(n);
        }
        assertEquals(6, set.size());
        assertEquals(6, origin.allNeighbors().size());
    }

    @Test
    void addMovesByDelta() {
        assertEquals(new HexCoord(3, 0), new HexCoord(1, 0).add(new HexCoord(2, 0)));
    }

    @Test
    void recordsEqualByValue() {
        assertEquals(new HexCoord(-1, 2), new HexCoord(-1, 2));
        assertNotEquals(new HexCoord(0, 0), new HexCoord(0, 1));
    }
}
