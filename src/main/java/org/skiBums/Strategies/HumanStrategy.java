package org.skiBums.Strategies;

import org.skiBums.Board;
import org.skiBums.Player;

import java.util.Random;

public class HumanStrategy implements IStrategy {
    private Integer pendingEdgeId;
    private Integer pendingVertexId;

    @Override
    public void queueInitialPlacement(int edgeId, int vertexId) {
        pendingEdgeId = edgeId;
        pendingVertexId = vertexId;
    }

    @Override
    public boolean placeInitialSettlementAndRoad(Board board, Player self, Random random) {
        if (pendingEdgeId == null || pendingVertexId == null) {
            return false;
        }
        boolean ok = board.selectedStartingSettlement(self, pendingEdgeId, pendingVertexId);
        if (ok) {
            pendingEdgeId = null;
            pendingVertexId = null;
        }
        return ok;
    }
}
