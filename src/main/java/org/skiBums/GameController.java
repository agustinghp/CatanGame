package org.skiBums;

import org.skiBums.Structures.Building;
import org.skiBums.Tiles.Tile;
import org.skiBums.geometry.HexCoord;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameController {
    public GameController (List<Player> players, Board board) {
        this.players = players;
        this.board = board;
    }

    List<Player> players;
    protected Board board;
    private boolean gameOver;
    private final Random random = new Random();

    public void initializeRandomBoard() {
        board.setup();
    }

    public void initializeStartingSettlementsRandom() {
        Collections.shuffle(players);
        for (Player player : players) {
            board.randomStartingSettlement(player, random);
        }
        Collections.reverse(players);
        for (Player player : players) {
            board.randomStartingSettlement(player, random);
        }
        Collections.reverse(players);
    }

    public boolean placeSelectedSettlement(Player player, int edgeID, int vertexID) {
        return board.selectedStartingSettlement(player, edgeID, vertexID);
    }

    public void playerPlacesRoad(int edgeID, Player player){
        board.buildRoadIfPossible(edgeID, player);
    }

    public void playerPlacesSettlement(int vertexID, Player player){
        board.buildSettlementIfPossible(vertexID, player);
    }

    public void playerPlacesCity(int vertexID, Player player){
        board.buildCityIfPossible(vertexID, player);
    }

    public void gameLoop() {
        int turnCount = 0;
        while (!gameOver) {
            for (Player currentPlayer : players) {
                // TODO player rolls 7 logic
                // First player rolls dice and everyone gets their resources
                int playerRoll = currentPlayer.roll();
                board.playerRoll(playerRoll);
            }
        }
    }


}
