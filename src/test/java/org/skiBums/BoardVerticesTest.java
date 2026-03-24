package org.skiBums;

import org.junit.jupiter.api.Test;
import org.skiBums.geometry.HexCoord;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardVerticesTest {

    @Test
    void setupBuildsIntersections() {
        Board board = new Board();
        board.setup();
        assertTrue(board.getVertices().size() >= 19);
        for (Vertex v : board.getVertices()) {
            assertEquals(3, v.corners().size());
        }
    }
}
