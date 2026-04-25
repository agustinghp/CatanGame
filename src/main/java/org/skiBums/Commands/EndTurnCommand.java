package org.skiBums.Commands;

import org.skiBums.GameController;

public class EndTurnCommand implements ICommand {
    private final GameController gameController;

    public EndTurnCommand(GameController gameController) {
        this.gameController = gameController;
    }

    @Override
    public boolean execute() {
        return gameController.endTurn();
    }
}
