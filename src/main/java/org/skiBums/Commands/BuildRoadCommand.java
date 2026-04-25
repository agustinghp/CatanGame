package org.skiBums.Commands;

import org.skiBums.GameController;
import org.skiBums.Player;

public class BuildRoadCommand implements ICommand {
    private final GameController gameController;
    private final Player player;
    private final int edgeId;

    public BuildRoadCommand(GameController gameController, Player player, int edgeId) {
        this.gameController = gameController;
        this.player = player;
        this.edgeId = edgeId;
    }

    @Override
    public boolean execute() {
        return gameController.playerPlacesRoad(edgeId, player);
    }
}
