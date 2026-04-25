package org.skiBums.Observers;

import org.skiBums.Board;
import org.skiBums.GameController;
import org.skiBums.Player;
import org.skiBums.ResourceType;

import java.util.Map;
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
    protected void onInitialPlacementComplete() {
        postMessage("All Starting Settlements Placed");
        super.onInitialPlacementComplete();
    }

    @Override
    protected void onInitialPlacementAttempt(Player player, boolean success) {
        if (success) {
            postMessage(player.getName() + " has placed a starting settlement and road");
            return;
        }
        postMessage("Starting Settlement unsuccessfully placed by " + player.getName());
    }

    @Override
    protected void onTurnStarted(Player player, int turn) {
        postMessage("Turn " + turn + " begins: " + player.getName());
        super.onTurnStarted(player, turn);
    }

    @Override
    protected void onRollResolved(Player roller, int turn, int rolledNumber,
                                  Map<Player, Map<ResourceType, Integer>> payouts) {
        postMessage(roller.getName() + " rolled a " + rolledNumber);
        if (payouts.isEmpty()) {
            postMessage("No resources distributed.");
            return;
        }
        for (Map.Entry<Player, Map<ResourceType, Integer>> byPlayer : payouts.entrySet()) {
            postMessage(byPlayer.getKey().getName() + " receives " + formatResourceMap(byPlayer.getValue()));
        }
    }

    @Override
    protected void onTurnEnded(Player player, int turn) {
        postMessage("Turn " + turn + " ends: " + player.getName());
    }

    @Override
    protected void onBankTrade(Player player, ResourceType giveType, int giveAmount,
                               ResourceType receiveType, int receiveAmount) {
        postMessage(player.getName() + " traded " + giveAmount + " " + giveType
                + " with the bank for " + receiveAmount + " " + receiveType);
    }

    @Override
    protected void onGameWon(Player winner, int victoryPoints) {
        postMessage("Game Over: " + winner.getName() + " wins with " + victoryPoints + " victory points");
    }

    @Override
    protected void onRoadPlacementResolved(Player player, int edgeID, boolean success) {
        if (success) {
            postMessage("Road was successfully placed by " + player.getName() + " at " + edgeID + " edge");
            return;
        }
        postMessage("Road was unsuccessfully placed by " + player.getName() + " at " + edgeID + " edge");
    }

    @Override
    protected void onSettlementPlacementResolved(Player player, int vertexID, boolean success) {
        if (success) {
            postMessage("Settlement was successfully placed by " + player.getName() + " at " + vertexID + " vertex");
            return;
        }
        postMessage("Settlement was unsuccessfully placed by " + player.getName() + " at " + vertexID + " vertex");
    }

    @Override
    protected void onCityPlacementResolved(Player player, int vertexID, boolean success) {
        if (success) {
            postMessage("City was successfully placed by " + player.getName() + " at " + vertexID + " vertex");
            return;
        }
        postMessage("City was unsuccessfully placed by " + player.getName() + " at " + vertexID + " vertex");
    }

    private static String formatResourceMap(Map<ResourceType, Integer> resources) {
        StringBuilder sb = new StringBuilder();
        appendResource(sb, resources, ResourceType.BRICK);
        appendResource(sb, resources, ResourceType.WOOD);
        appendResource(sb, resources, ResourceType.SHEEP);
        appendResource(sb, resources, ResourceType.WHEAT);
        appendResource(sb, resources, ResourceType.ROCK);
        return sb.toString();
    }

    private static void appendResource(StringBuilder sb, Map<ResourceType, Integer> resources, ResourceType type) {
        int amount = resources.getOrDefault(type, 0);
        if (amount <= 0) {
            return;
        }
        if (sb.length() > 0) {
            sb.append(", ");
        }
        sb.append(amount).append(' ').append(type.name());
    }

}
