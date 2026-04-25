package org.skiBums.Commands;

import org.skiBums.GameController;
import org.skiBums.Player;

public class  PlaceStartingSettlementCommand implements ICommand {
    private final GameController gameController;
    private final Player player;
    private final int edgeId;
    private final int vertexId;

    public PlaceStartingSettlementCommand(GameController gameController, Player player, int edgeId, int vertexId) {
        this.gameController = gameController;
        this.player = player;
        this.edgeId = edgeId;
        this.vertexId = vertexId;
    }

    @Override
    public boolean execute() {
        return gameController.placeSelectedSettlement(player, edgeId, vertexId);
    }
}
