package org.skiBums.Commands;

import org.skiBums.GameController;
import org.skiBums.Player;

public class BuildSettlementCommand implements ICommand {
    private final GameController gameController;
    private final Player player;
    private final int vertexId;

    public BuildSettlementCommand(GameController gameController, Player player, int vertexId) {
        this.gameController = gameController;
        this.player = player;
        this.vertexId = vertexId;
    }

    @Override
    public boolean execute() {
        return gameController.playerPlacesSettlement(vertexId, player);
    }
}
