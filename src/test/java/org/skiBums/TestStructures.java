package org.skiBums;

import org.junit.jupiter.api.Test;
import org.skiBums.Observers.ObservableGameController;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestStructures {

    @Test
    void testRandomizedStartingSettlements(){
        List<Player> players = new ArrayList<>();
        players.add(new Player("Charlie"));
        players.add(new Player("Agustin"));
        TestObserver testObserver = new TestObserver();
        Board board = new Board();
        ObservableGameController game = new ObservableGameController(players, board);
        game.attach(testObserver);
        game.initializeRandomBoard();
        game.initializeStartingSettlementsRandom();
        assertTrue(testObserver.hasMessage("Starting Settlements Placed"));
    }

    @Test
    void testPlaceStartingSettlement() {
        String playerName = "Charlie";
        TestObserver testObserver = new TestObserver();
        Board board = new Board();
        board.setup();
        Vertex start = board.getVertices().getFirst();
        int vertexToPlace = start.id();
        int edgeToPlace = board.getConnectedEdges(vertexToPlace).getFirst();
        Player player = new Player(playerName);
        ObservableGameController game = new ObservableGameController(List.of(player), board);
        game.attach(testObserver);
        assertTrue(game.placeSelectedSettlement(player, edgeToPlace, vertexToPlace));
        assertTrue(testObserver.hasMessage("Starting Settlement successfully placed by " + playerName + " at " + vertexToPlace + " vertex and " + edgeToPlace + " edge"));
        game.detach(testObserver);
    }

    @Test
    void testRoadConnectsWithRoad() {
        Board board = new Board();
        board.setup();
        Edge firstEdge = board.getEdges().getFirst();
        int v1 = firstEdge.vertexIdA();
        int v2 = firstEdge.vertexIdB();
        Player player = new Player("p");
        board.buildSettlement(v1, player);
        board.buildRoad(firstEdge.id(), player);
        int continuationEdge = -1;
        for (int edgeId : board.getConnectedEdges(v2)) {
            if (edgeId != firstEdge.id()) {
                continuationEdge = edgeId;
                break;
            }
        }
        assertTrue(continuationEdge >= 0);
        assertTrue(board.roadConnectsWithRoad(continuationEdge, player));
    }

    @Test
    void testBuildRoadIfPossible_() {
        Board board = new Board();
        board.setup();
        Player player = new Player("p");
        Edge edge = board.getEdges().get(0);
        assertFalse(board.buildRoadIfPossible(edge.id(), player));

        board.buildSettlement(edge.vertexIdA(), player);
        assertTrue(board.buildRoadIfPossible(edge.id(), player));
        assertFalse(board.buildRoadIfPossible(edge.id(), player));
    }
}
