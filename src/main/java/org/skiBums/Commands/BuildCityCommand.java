package org.skiBums.Commands;

import org.skiBums.GameController;
import org.skiBums.Player;

public class BuildCityCommand implements ICommand {
    private final GameController gameController;
    private final Player player;
    private final int vertexId;

    public BuildCityCommand(GameController gameController, Player player, int vertexId) {
        this.gameController = gameController;
        this.player = player;
        this.vertexId = vertexId;
    }

    @Override
    public boolean execute() {
        return gameController.playerPlacesCity(vertexId, player);
    }
}
