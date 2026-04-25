package org.skiBums;

import org.junit.jupiter.api.Test;
import org.skiBums.Observers.ObservableGameController;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestStructures {

    @Test
    void testRandomizedStartingSettlements(){
        List<Player> players = new ArrayList<>();
        players.add(Player.getNewBuilder("Charlie").build());
        players.add(Player.getNewBuilder("Agustin").build());
        TestObserver testObserver = new TestObserver();
        Board board = new Board();
        ObservableGameController game = new ObservableGameController(players, board);
        game.attach(testObserver);
        game.initializeRandomBoard();
        game.initializeStartingSettlements();
        assertTrue(testObserver.hasMessage("All Starting Settlements Placed"));
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
        Player player = Player.getNewBuilder(playerName).build();
        ObservableGameController game = new ObservableGameController(List.of(player), board);
        game.attach(testObserver);
        assertTrue(game.placeSelectedSettlement(player, edgeToPlace, vertexToPlace));
        assertTrue(testObserver.hasMessage(playerName + " has placed a starting settlement and road"));
        game.detach(testObserver);
    }

    @Test
    void testRoadConnectsWithRoad() {
        Board board = new Board();
        board.setup();
        Edge firstEdge = board.getEdges().getFirst();
        int v1 = firstEdge.vertexIdA();
        int v2 = firstEdge.vertexIdB();
        Player player = Player.getNewBuilder("p").build();
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
        Player player = Player.getNewBuilder("p").build();
        Edge edge = board.getEdges().getFirst();
        assertFalse(board.buildRoadIfPossible(edge.id(), player));

        board.buildSettlement(edge.vertexIdA(), player);
        assertTrue(board.buildRoadIfPossible(edge.id(), player));
        assertFalse(board.buildRoadIfPossible(edge.id(), player));
    }

    @Test
    void testFirstMainTurnStartsWithFirstSetupPlayer() {
        List<Player> players = new ArrayList<>();
        players.add(Player.getNewBuilder("Charlie").build());
        players.add(Player.getNewBuilder("Agustin").build());
        Board board = new Board();
        ObservableGameController game = new ObservableGameController(players, board);
        game.initializeRandomBoard();
        game.initializeStartingSettlements();

        assertTrue(game.hasMainGameStarted());
        Player currentTurn = game.getCurrentTurnPlayer();
        assertNotNull(currentTurn);
        assertTrue(game.turnNumber >= 1);
    }

    @Test
    void testPlayerCanAffordCityWithBankTrades() {
        Player player = Player.getNewBuilder("Trader").build();
        for (int i = 0; i < 20; i++) {
            player.giveResourceCard(ResourceType.BRICK);
        }

        assertFalse(player.hasResourcesFor(GameConstants.CITY_COST));
        assertTrue(player.canAffordWithBankTrades(GameConstants.CITY_COST, GameConstants.BANK_TRADE_RATE));
    }

    @Test
    void testBankTradePostsLogMessage() {
        String playerName = "Banker";
        Player player = Player.getNewBuilder(playerName).build();
        for (int i = 0; i < GameConstants.BANK_TRADE_RATE; i++) {
            player.giveResourceCard(ResourceType.BRICK);
        }

        Board board = new Board();
        board.setup();
        ObservableGameController game = new ObservableGameController(List.of(player), board);
        TestObserver testObserver = new TestObserver();
        game.attach(testObserver);

        assertTrue(game.playerTradesWithBank(player, ResourceType.BRICK, ResourceType.WHEAT));
        assertTrue(testObserver.hasMessage(playerName + " traded 4 BRICK with the bank for 1 WHEAT"));
        assertEquals(0, player.getResourceCardCounts().getOrDefault(ResourceType.BRICK, 0));
        assertEquals(1, player.getResourceCardCounts().getOrDefault(ResourceType.WHEAT, 0));
    }
}
