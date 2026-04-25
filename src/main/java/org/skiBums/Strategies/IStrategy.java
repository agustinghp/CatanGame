package org.skiBums.Strategies;

import org.skiBums.Board;
import org.skiBums.GameController;
import org.skiBums.Player;

import java.util.Random;

public interface IStrategy {
    boolean placeInitialSettlementAndRoad(Board board, Player self, Random random);

    default void queueInitialPlacement(int edgeId, int vertexId) {
    }

    default void takeTurn(GameController gameController, Player self, Random random) {
    }

}
