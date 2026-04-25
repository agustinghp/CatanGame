package org.skiBums;

import javax.swing.Timer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GameController {
    public GameController (List<Player> players, Board board) {
        this.players = players;
        this.board = board;
    }

    List<Player> players;
    protected Board board;
    private boolean gameOver;
    protected final Random random = new Random();
    protected int currentTurnIndex = -1;
    protected int turnNumber = 0;
    protected boolean turnsStarted = false;
    protected boolean setupCompletionHandled = false;
    private boolean rolledDiceThisTurn;

    public void initializeRandomBoard() {
        board.setup();
    }

    // SETUP PHASE
    protected enum SetupPhase { INACTIVE, PASS_ONE, PASS_TWO, COMPLETE }
    protected SetupPhase setupPhase = SetupPhase.INACTIVE;
    protected List<Player> shuffledOrder = List.of();
    protected int setupIndex;

    public void initializeStartingSettlements() {
        Collections.shuffle(players);
        shuffledOrder = new ArrayList<>(players);
        setupPhase = SetupPhase.PASS_ONE;
        setupIndex = 0;
        turnsStarted = false;
        currentTurnIndex = -1;
        turnNumber = 0;
        setupCompletionHandled = false;
        rolledDiceThisTurn = false;
        advanceInitialPlacement();
    }

    protected boolean isInitialPlacementActive() {
        return setupPhase == SetupPhase.PASS_ONE || setupPhase == SetupPhase.PASS_TWO;
    }

    protected void advanceInitialPlacement() {
        while (isInitialPlacementActive()) {
            List<Player> order = currentPassOrder();
            if (setupIndex >= order.size()) {
                if (setupPhase == SetupPhase.PASS_ONE) {
                    setupPhase = SetupPhase.PASS_TWO;
                    setupIndex = 0;
                } else {
                    markSetupCompleteIfNeeded();
                }
                continue;
            }
            Player current = order.get(setupIndex);
            boolean placed = current.getStrategy().placeInitialSettlementAndRoad(board, current, random);
            if (placed) {
                onInitialPlacementAttempt(current, true);
                setupIndex++;
            } else {
                return; // We have to wait for UI
            }
        }
    }

    protected void markSetupCompleteIfNeeded() {
        if (setupCompletionHandled) {
            return;
        }
        setupCompletionHandled = true;
        setupPhase = SetupPhase.COMPLETE;
        onInitialPlacementComplete();
    }

    protected List<Player> currentPassOrder() {
        if (setupPhase == SetupPhase.PASS_ONE) {
            return shuffledOrder;
        }
        if (setupPhase == SetupPhase.PASS_TWO) {
            List<Player> rev = new ArrayList<>(shuffledOrder);
            Collections.reverse(rev);
            return rev;
        }
        return List.of();
    }

    public Player getCurrentSetupPlayer() {
        if (setupPhase != SetupPhase.PASS_ONE && setupPhase != SetupPhase.PASS_TWO) {
            return null;
        }
        List<Player> order = currentPassOrder();
        if (setupIndex >= order.size()) {
            return null;
        }
        return order.get(setupIndex);
    }

    public boolean isWaitingForHumanSetup() {
        Player c = getCurrentSetupPlayer();
        return c != null && !c.isAI();
    }


    public boolean placeSelectedSettlement(Player player, int edgeID, int vertexID) {
        if (isInitialPlacementActive()) {
            Player expected = getCurrentSetupPlayer();
            if (expected == null || expected != player) {
                onInitialPlacementAttempt(player, false);
                return false;
            }
            int previousSetupIndex = setupIndex;
            SetupPhase previousSetupPhase = setupPhase;
            player.getStrategy().queueInitialPlacement(edgeID, vertexID);
            advanceInitialPlacement();
            boolean ok = setupIndex != previousSetupIndex || setupPhase != previousSetupPhase;
            if (!ok) {
                onInitialPlacementAttempt(player, false);
            }
            if (!isInitialPlacementActive()) {
                markSetupCompleteIfNeeded();
            }
            if (ok) {
                checkWinCondition(player);
            }
            return ok;
        }
        boolean ok = board.selectedStartingSettlement(player, edgeID, vertexID);
        onInitialPlacementAttempt(player, ok);
        if (ok) {
            checkWinCondition(player);
        }
        return ok;
    }

    protected void onInitialPlacementAttempt(Player player, boolean success) {
    }

    protected void onInitialPlacementComplete() {
        startTurnsIfNeeded();
    }

    // This sets up the variables for the turns
    protected void startTurnsIfNeeded() {
        if (turnsStarted || shuffledOrder.isEmpty()) {
            return;
        }
        // Turn order begins with the same player who placed the very first starting settlement.
        currentTurnIndex = 0;
        turnNumber = 1;
        turnsStarted = true;
    }

    public boolean hasMainGameStarted() {
        return turnsStarted;
    }

    public Player getCurrentTurnPlayer() {
        if (!turnsStarted || currentTurnIndex < 0 || currentTurnIndex >= shuffledOrder.size()) {
            return null;
        }
        return shuffledOrder.get(currentTurnIndex);
    }

    public boolean endTurn() {
        if (!turnsStarted || gameOver || shuffledOrder.isEmpty()) {
            return false;
        }
        Player endingPlayer = shuffledOrder.get(currentTurnIndex);
        onTurnEnded(endingPlayer, turnNumber);
        currentTurnIndex = (currentTurnIndex + 1) % shuffledOrder.size();
        if (currentTurnIndex == 0) {
            turnNumber++;
        }
        rolledDiceThisTurn = false;
        return true;
    }

    protected void scheduleAiTurn(Player expectedPlayer) {
        // Timer for delay
        Timer timer = new Timer(GameConstants.AI_TURN_DELAY_MS, e -> {
            ((Timer) e.getSource()).stop();
            if (gameOver) {
                return;
            }
            Player current = getCurrentTurnPlayer();
            if (current != expectedPlayer || current == null || !current.isAI()) {
                return;
            }
            current.getStrategy().takeTurn(this, current, random);
        });
        timer.setRepeats(false);
        timer.start();
    }

    protected void onTurnStarted(Player player, int turn) {
        int rolledNumber = player.roll();
        Map<Player, Map<ResourceType, Integer>> payouts = board.playerRoll(rolledNumber);
        onRollResolved(player, turn, rolledNumber, payouts);
    }

    protected void onRollResolved(Player roller, int turn, int rolledNumber,
                                  Map<Player, Map<ResourceType, Integer>> payouts) {
    }

    protected void onTurnEnded(Player player, int turn) {
    }

    protected void onBankTrade(Player player, ResourceType giveType, int giveAmount,
                               ResourceType receiveType, int receiveAmount) {
    }

    protected void onRoadPlacementResolved(Player player, int edgeId, boolean success) {
    }

    protected void onSettlementPlacementResolved(Player player, int vertexId, boolean success) {
    }

    protected void onCityPlacementResolved(Player player, int vertexId, boolean success) {
    }

    public boolean playerTradesWithBank(Player player, ResourceType giveType, ResourceType receiveType) {
        if (player == null || giveType == null || receiveType == null) {
            return false;
        }
        boolean traded = player.tradeWithBank(giveType, receiveType, GameConstants.BANK_TRADE_RATE);
        if (traded) {
            onBankTrade(player, giveType, GameConstants.BANK_TRADE_RATE, receiveType, 1);
        }
        return traded;
    }

    public boolean playerPlacesRoad(int edgeID, Player player){
        if (gameOver || player == null) {
            return false;
        }
        boolean success = player.hasResourcesFor(GameConstants.ROAD_COST)
                && board.buildRoadIfPossible(edgeID, player)
                && player.spendResources(GameConstants.ROAD_COST);
        onRoadPlacementResolved(player, edgeID, success);
        return success;
    }

    public boolean playerPlacesSettlement(int vertexID, Player player){
        if (gameOver || player == null) {
            return false;
        }
        boolean success = player.hasResourcesFor(GameConstants.SETTLEMENT_COST)
                && board.buildSettlementIfPossible(vertexID, player)
                && player.spendResources(GameConstants.SETTLEMENT_COST);
        onSettlementPlacementResolved(player, vertexID, success);
        if (success) {
            checkWinCondition(player);
        }
        return success;
    }

    public boolean playerPlacesCity(int vertexID, Player player){
        if (gameOver || player == null) {
            return false;
        }
        boolean success = player.hasResourcesFor(GameConstants.CITY_COST)
                && board.buildCityIfPossible(vertexID, player)
                && player.spendResources(GameConstants.CITY_COST);
        onCityPlacementResolved(player, vertexID, success);
        if (success) {
            checkWinCondition(player);
        }
        return success;
    }

    public Board getBoard() {
        return board;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    protected void checkWinCondition(Player player) {
        if (gameOver || player == null) {
            return;
        }
        int vp = player.getVictoryPoints();
        if (vp >= GameConstants.WINNING_VICTORY_POINTS) {
            gameOver = true;
            onGameWon(player, vp);
        }
    }

    protected void onGameWon(Player winner, int victoryPoints) {
    }

    // Drives the whole game loop
    public void gameLoop() {
        while (!gameOver) {
            if (isInitialPlacementActive()) {
                int previousSetupIndex = setupIndex;
                SetupPhase previousPhase = setupPhase;
                advanceInitialPlacement();
                // Human setup needs a click to continue; stop driving for now.
                if (isWaitingForHumanSetup()) {
                    break;
                }
                boolean setupProgressed = setupIndex != previousSetupIndex || setupPhase != previousPhase;
                if (!setupProgressed) {
                    break;
                }
                continue;
            }
            if (!turnsStarted || shuffledOrder.isEmpty()) {
                break;
            }
            Player currentTurn = getCurrentTurnPlayer();
            if (currentTurn == null) {
                break;
            }
            if (rolledDiceThisTurn) {
                // Waiting for endTurn() (human or AI callback).
                break;
            }
            onTurnStarted(currentTurn, turnNumber);
            rolledDiceThisTurn = true;
            if (currentTurn.isAI()) {
                // AI actions run after delay via timer callback.
                scheduleAiTurn(currentTurn);
            }
            break;
        }
    }


}
