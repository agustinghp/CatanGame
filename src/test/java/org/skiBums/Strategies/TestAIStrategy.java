package org.skiBums.Strategies;

import org.junit.jupiter.api.Test;
import org.skiBums.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestAIStrategy {
    @Test
    void willNotBuildRoadIfCanBuildSettlement() {
        Player player1 = Player.getNewBuilder("Sully").build();
        Board board = new Board();
        Random random = new Random();
        board.setup();
        List<Vertex> vertices = board.getVertices();
        Vertex vertex = vertices.get(random.nextInt(vertices.size()));
        board.buildSettlement(vertex.id(),player1);
        List<Edge> edges = new ArrayList<> (board.getEdges());
        edges.removeFirst();
        // Fill up the board with roads except for one
        for (Edge e : edges) {
            board.buildRoad(e.id(), player1);
        }
        player1.giveResourceCard(ResourceType.BRICK);
        player1.giveResourceCard(ResourceType.WOOD);
        GameController game = new GameController(List.of(player1), board);
        player1.getStrategy().takeTurn(game, player1, random);
        assertTrue(player1.hasResourcesFor(GameConstants.ROAD_COST));
    }

}
