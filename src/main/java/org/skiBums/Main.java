package org.skiBums;

import org.skiBums.Observers.ObservableGameController;

import javax.swing.JFrame;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.CardLayout;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Catan");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            CardLayout cards = new CardLayout();
            JPanel root = new JPanel(cards);

            root.add(new MainMenuPanel(cards, root), "MENU");
            BoardPanel boardPanel = new BoardPanel();
            PlayerHandPanel handPanel = new PlayerHandPanel();
            JTextArea gameLog = new JTextArea();
            gameLog.setEditable(false);
            gameLog.setLineWrap(true);
            gameLog.setWrapStyleWord(true);

            JScrollPane logScrollPane = new JScrollPane(gameLog);
            logScrollPane.setPreferredSize(new Dimension(280, 170));

            JPanel rightSide = new JPanel(new BorderLayout());
            rightSide.add(logScrollPane, BorderLayout.NORTH);
            JPanel turnActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
            JButton endTurnButton = new JButton("End Turn");
            JButton tradeButton = new JButton("Trade 4:1");
            JButton buildRoadButton = new JButton("Build Road");
            JButton buildSettlementButton = new JButton("Build Settlement");
            JButton buildCityButton = new JButton("Build City");
            turnActions.add(new JLabel("Your Turn:"));
            turnActions.add(endTurnButton);
            turnActions.add(tradeButton);
            turnActions.add(buildRoadButton);
            turnActions.add(buildSettlementButton);
            turnActions.add(buildCityButton);

            JPanel gameScreen = new JPanel(new BorderLayout());
            gameScreen.add(boardPanel, BorderLayout.CENTER);
            gameScreen.add(rightSide, BorderLayout.EAST);
            JPanel bottomArea = new JPanel(new BorderLayout());
            bottomArea.add(turnActions, BorderLayout.NORTH);
            bottomArea.add(handPanel, BorderLayout.CENTER);
            gameScreen.add(bottomArea, BorderLayout.SOUTH);
            root.add(gameScreen, "GAME");

            PlayerSetupPanel setup = new PlayerSetupPanel(cards, root, players -> {
                Board board = new Board();
                Player firstCreatedPlayer = players.isEmpty() ? null : players.getFirst();
                ObservableGameController game = new ObservableGameController(players, board);
                Runnable refreshTurnControls = () -> {
                    Player current = game.getCurrentTurnPlayer();
                    boolean humanTurn = current != null && !current.isAI() && !game.isGameOver() && game.hasMainGameStarted();
                    endTurnButton.setEnabled(humanTurn);
                    boolean canTrade = humanTurn && canTradeWithBank(current);
                    boolean canBuildRoad = humanTurn && current.hasResourcesFor(GameConstants.ROAD_COST);
                    boolean canBuildSettlement = humanTurn && current.hasResourcesFor(GameConstants.SETTLEMENT_COST);
                    boolean canBuildCity = humanTurn && current.hasResourcesFor(GameConstants.CITY_COST);
                    tradeButton.setEnabled(canTrade);
                    buildRoadButton.setEnabled(canBuildRoad);
                    buildSettlementButton.setEnabled(canBuildSettlement);
                    buildCityButton.setEnabled(canBuildCity);
                    if (!humanTurn) {
                        boardPanel.setPendingBuildSelection(BoardPanel.BuildSelection.NONE);
                        return;
                    }
                    BoardPanel.BuildSelection selection = boardPanel.getPendingBuildSelection();
                    boolean selectionInvalid = (selection == BoardPanel.BuildSelection.ROAD && !canBuildRoad)
                            || (selection == BoardPanel.BuildSelection.SETTLEMENT && !canBuildSettlement)
                            || (selection == BoardPanel.BuildSelection.CITY && !canBuildCity);
                    if (selectionInvalid) {
                        boardPanel.setPendingBuildSelection(BoardPanel.BuildSelection.NONE);
                    }
                };

                gameLog.setText("");
                game.attach(message -> SwingUtilities.invokeLater(() -> {
                    gameLog.append(message + System.lineSeparator());
                    gameLog.setCaretPosition(gameLog.getDocument().getLength());
                    // Keep showing Player 1's live hand as resources change.
                    handPanel.setPlayer(firstCreatedPlayer);
                    // Ensure board visuals stay in sync with AI and turn-driven builds.
                    boardPanel.repaint();
                    refreshTurnControls.run();
                    game.gameLoop();
                }));

                clearActionListeners(endTurnButton);
                clearActionListeners(buildRoadButton);
                clearActionListeners(buildSettlementButton);
                clearActionListeners(buildCityButton);
                clearActionListeners(tradeButton);
                endTurnButton.addActionListener(e -> {
                    if (game.endTurn()) {
                        boardPanel.setPendingBuildSelection(BoardPanel.BuildSelection.NONE);
                        boardPanel.repaint();
                    }
                    refreshTurnControls.run();
                    game.gameLoop();
                });
                buildRoadButton.addActionListener(e ->
                        boardPanel.setPendingBuildSelection(BoardPanel.BuildSelection.ROAD));
                buildSettlementButton.addActionListener(e ->
                        boardPanel.setPendingBuildSelection(BoardPanel.BuildSelection.SETTLEMENT));
                buildCityButton.addActionListener(e ->
                        boardPanel.setPendingBuildSelection(BoardPanel.BuildSelection.CITY));
                tradeButton.addActionListener(e -> showBankTradeDialog(frame, game, refreshTurnControls, boardPanel));

                game.initializeRandomBoard();
                game.initializeStartingSettlements();
                game.gameLoop();
                handPanel.setPlayer(firstCreatedPlayer);
                boardPanel.setBoard(board);
                boardPanel.setGameController(game);
                boardPanel.repaint();
                refreshTurnControls.run();
                cards.show(root, "GAME");
            });
            root.add(setup, "SETUP");

            cards.show(root, "MENU");

            frame.setContentPane(root);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private static void clearActionListeners(JButton button) {
        for (ActionListener listener : button.getActionListeners()) {
            button.removeActionListener(listener);
        }
    }

    private static void showBankTradeDialog(JFrame frame, ObservableGameController game,
                                            Runnable refreshTurnControls, BoardPanel boardPanel) {
        Player current = game.getCurrentTurnPlayer();
        if (current == null || current.isAI() || game.isGameOver()) {
            return;
        }
        List<ResourceType> canGive = new ArrayList<>();
        for (ResourceType type : new ResourceType[]{
                ResourceType.WOOD, ResourceType.BRICK, ResourceType.SHEEP, ResourceType.WHEAT, ResourceType.ROCK
        }) {
            if (current.countResource(type) >= GameConstants.BANK_TRADE_RATE) {
                canGive.add(type);
            }
        }
        if (canGive.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "You need at least 4 of one resource to trade with the bank.");
            return;
        }
        JComboBox<ResourceType> giveBox = new JComboBox<>(canGive.toArray(new ResourceType[0]));
        JComboBox<ResourceType> receiveBox = new JComboBox<>(new ResourceType[]{
                ResourceType.WOOD, ResourceType.BRICK, ResourceType.SHEEP, ResourceType.WHEAT, ResourceType.ROCK
        });
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        panel.add(new JLabel("Give:"));
        panel.add(giveBox);
        panel.add(new JLabel("Receive:"));
        panel.add(receiveBox);
        int choice = JOptionPane.showConfirmDialog(frame, panel, "Trade 4:1 with Bank",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) {
            return;
        }
        ResourceType give = (ResourceType) giveBox.getSelectedItem();
        ResourceType receive = (ResourceType) receiveBox.getSelectedItem();
        if (give == null || receive == null) {
            return;
        }
        if (give == receive) {
            JOptionPane.showMessageDialog(frame, "Give and receive resource must be different.");
            return;
        }
        game.playerTradesWithBank(current, give, receive);
        boardPanel.repaint();
        refreshTurnControls.run();
    }

    private static boolean canTradeWithBank(Player player) {
        for (ResourceType type : new ResourceType[]{
                ResourceType.WOOD, ResourceType.BRICK, ResourceType.SHEEP, ResourceType.WHEAT, ResourceType.ROCK
        }) {
            if (player.countResource(type) >= GameConstants.BANK_TRADE_RATE) {
                return true;
            }
        }
        return false;
    }
}