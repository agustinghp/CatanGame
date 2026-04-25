package org.skiBums.Commands;

import org.skiBums.GameController;
import org.skiBums.Player;

public final class CommandFactory {
    private CommandFactory() {
    }

    public static ICommand buildCity(GameController gameController, Player player, int vertexId) {
        return new BuildCityCommand(gameController, player, vertexId);
    }

    public static ICommand buildSettlement(GameController gameController, Player player, int vertexId) {
        return new BuildSettlementCommand(gameController, player, vertexId);
    }

    public static ICommand buildRoad(GameController gameController, Player player, int edgeId) {
        return new BuildRoadCommand(gameController, player, edgeId);
    }

    public static ICommand endTurn(GameController gameController) {
        return new EndTurnCommand(gameController);
    }

    public static ICommand placeStartingSettlement(GameController gameController, Player player, int edgeId, int vertexId) {
        return new PlaceStartingSettlementCommand(gameController, player, edgeId, vertexId);
    }
}
