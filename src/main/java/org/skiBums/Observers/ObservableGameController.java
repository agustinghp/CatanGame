package org.skiBums.Observers;

import org.skiBums.Board;
import org.skiBums.GameController;
import org.skiBums.Player;

import java.util.List;

public class ObservableGameController extends GameController implements IGame {
    public ObservableGameController(List<Player> players, Board board) {
        super(players, board);
    }

    @Override
    public void attach(IGameObserver observer) {
        EventBus.getInstance().attach(observer);
    }

    private void postMessage(String message) {
        EventBus.getInstance().postMessage(message);
    }

    @Override
    public void initializeRandomBoard() {
        super.initializeRandomBoard();
        this.postMessage("Board Initialized");
    }

    @Override
    public void initializeStartingSettlementsRandom() {
        super.initializeStartingSettlementsRandom();
        this.postMessage("Starting Settlements Placed");
    }

    @Override
    public void playerPlacesRoad(int edgeID, Player player){
        if (board.buildRoadIfPossible(edgeID, player)) {
            postMessage("Road was successfully placed by " + player.getName() + " at " + edgeID + " edge");
            return;
        }
        postMessage("Road was unsuccessfully placed by " + player.getName() + " at " + edgeID + " edge");
    }

    @Override
    public void playerPlacesSettlement(int vertexID, Player player){
        if (board.buildSettlementIfPossible(vertexID, player)) {
            postMessage("Settlement was successfully placed by " + player.getName() + " at " + vertexID + " vertex");
            return;
        }
        postMessage("Settlement was unsuccessfully placed by " + player.getName() + " at " + vertexID + " vertex");
    }

    @Override
    public void playerPlacesCity(int vertexID, Player player){
        if (board.buildCityIfPossible(vertexID, player)) {
            postMessage("City was successfully placed by " + player.getName() + " at " + vertexID + " vertex");
            return;
        }
        postMessage("City was unsuccessfully placed by " + player.getName() + " at " + vertexID + " vertex");
    }

    @Override
    public boolean placeSelectedSettlement(Player player, int edgeID, int vertexID) {
        if (super.placeSelectedSettlement(player, edgeID, vertexID)) {
            postMessage("Starting Settlement successfully placed by " + player.getName() + " at " + vertexID + " vertex and " + edgeID + " edge");
            return true;
        }
        postMessage("Starting Settlement unsuccessfully placed by " + player.getName() + " at " + vertexID + " vertex and " + edgeID + " edge");
        return false;
    }


}
