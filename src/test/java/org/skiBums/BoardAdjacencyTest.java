package org.skiBums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BoardAdjacencyTest {

    @Test
    void adjacencyMatchesEdges() {
        Board board = new Board();
        board.setup();
        for (Vertex v : board.getVertices()) {
            int id = v.id();
            assertEquals(
                    board.getConnectedEdges(id).size(),
                    board.getConnectedVertices(id).size());
        }
        for (Edge e : board.getEdges()) {
            assertTrue(board.getConnectedVertices(e.vertexIdA()).contains(e.vertexIdB()));
            assertTrue(board.getConnectedVertices(e.vertexIdB()).contains(e.vertexIdA()));
            assertTrue(board.getConnectedEdges(e.vertexIdA()).contains(e.id()));
            assertTrue(board.getConnectedEdges(e.vertexIdB()).contains(e.id()));
        }
    }
}
