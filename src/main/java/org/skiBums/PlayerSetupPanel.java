package org.skiBums;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;


public class PlayerSetupPanel extends JPanel {

    private final Consumer<List<Player>> onContinueSuccess;

    private static final Color[] DEFAULT_PALETTE = {
            Color.RED,
            Color.BLUE,
            Color.GREEN,
            Color.ORANGE,
            new Color(128, 0, 128),
            Color.CYAN
    };

    private static final int SIMILAR_COLOR_RGB_THRESHOLD = 80;

    private final CardLayout cards;
    private final JPanel root;

    private final JSpinner playerCountSpinner;
    private final JPanel rowsPanel;
    private final List<JTextField> nameFields = new ArrayList<>();
    private final List<Color> rowColors = new ArrayList<>();
    private final List<JCheckBox> aiCheckboxes = new ArrayList<>();

    private List<Player> lastConfirmedPlayers = List.of();

    public PlayerSetupPanel(CardLayout cards, JPanel root, Consumer<List<Player>> onContinueSuccess) {
        this.onContinueSuccess = onContinueSuccess;
        this.cards = cards;
        this.root = root;

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Number of players:"));
        playerCountSpinner = new JSpinner(new SpinnerNumberModel(2, 2, 4, 1));
        top.add(playerCountSpinner);

        rowsPanel = new JPanel(new GridBagLayout());
        rowsPanel.setBorder(BorderFactory.createTitledBorder("Players"));

        ChangeListener rebuild = e -> rebuildRows();
        playerCountSpinner.addChangeListener(rebuild);

        add(top, BorderLayout.NORTH);
        add(rowsPanel, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> cards.show(root, "MENU"));
        JButton continueButton = new JButton("Continue");
        continueButton.addActionListener(e -> onContinue());
        bottom.add(backButton);
        bottom.add(continueButton);
        add(bottom, BorderLayout.SOUTH);

        rebuildRows();
    }

    private void rebuildRows() {
        rowsPanel.removeAll();
        nameFields.clear();
        rowColors.clear();
        aiCheckboxes.clear();

        int n = (Integer) playerCountSpinner.getValue();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        for (int i = 0; i < n; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            rowsPanel.add(new JLabel("Player " + (i + 1) + " name:"), gbc);

            gbc.gridx = 1;
            JTextField nameField = new JTextField("Player " + (i + 1), 14);
            nameFields.add(nameField);
            rowsPanel.add(nameField, gbc);

            gbc.gridx = 2;
            JCheckBox aiBox = new JCheckBox("AI");
            if (i == 0) {
                aiBox.setToolTipText("Check for a computer-controlled player; leave unchecked for a human.");
            } else {
                aiBox.setSelected(true);
                aiBox.setEnabled(false);
                aiBox.setToolTipText("Additional players are always AI.");
            }
            aiCheckboxes.add(aiBox);
            rowsPanel.add(aiBox, gbc);

            Color initial = DEFAULT_PALETTE[i % DEFAULT_PALETTE.length];
            rowColors.add(initial);

            gbc.gridx = 3;
            JPanel colorRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));

            JPanel swatch = new JPanel();
            swatch.setPreferredSize(new Dimension(40, 24));
            swatch.setMinimumSize(new Dimension(40, 24));
            swatch.setBackground(initial);
            swatch.setOpaque(true);
            swatch.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));

            JButton chooseColorButton = new JButton("Choose color");
            int rowIndex = i;
            chooseColorButton.addActionListener(e -> {
                Color chosen = JColorChooser.showDialog(
                        PlayerSetupPanel.this,
                        "Choose color for " + nameFields.get(rowIndex).getText().trim(),
                        rowColors.get(rowIndex));
                if (chosen != null) {
                    rowColors.set(rowIndex, chosen);
                    swatch.setBackground(chosen);
                    swatch.repaint();
                }
            });

            colorRow.add(swatch);
            colorRow.add(chooseColorButton);
            rowsPanel.add(colorRow, gbc);
        }

        rowsPanel.revalidate();
        rowsPanel.repaint();
    }

    private void onContinue() {
        int n = (Integer) playerCountSpinner.getValue();

        List<String> names = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            String name = nameFields.get(i).getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Please enter a name for every player.",
                        "Missing name",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            names.add(name);
        }

        if (new HashSet<>(names).size() != names.size()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Player names must be different.",
                    "Duplicate name",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Color> chosenColors = new ArrayList<>(rowColors);
        if (new HashSet<>(chosenColors).size() != chosenColors.size()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Each player must have a different color.",
                    "Duplicate color",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<int[]> similarPairs = getSimilarColorPairs(chosenColors, SIMILAR_COLOR_RGB_THRESHOLD);
        if (!similarPairs.isEmpty()) {
            StringBuilder msg = new StringBuilder(
                    "These player colors are too similar. Please choose colors that are easier to tell apart:");
            for (int[] pair : similarPairs) {
                int a = pair[0];
                int b = pair[1];
                msg.append("\n• ").append(names.get(a)).append(" and ").append(names.get(b));
            }
            JOptionPane.showMessageDialog(
                    this,
                    msg.toString(),
                    "Colors too similar",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Player> players = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            boolean ai = aiCheckboxes.get(i).isSelected();
            players.add(Player.getNewBuilder(names.get(i))
                    .setColor(chosenColors.get(i))
                    .setDice(new Dice(6))
                    .isAI(ai)
                    .build()
            );
        }
        lastConfirmedPlayers = List.copyOf(players);
        onContinueSuccess.accept(players);
    }

    public List<Player> getLastConfirmedPlayers() {
        return lastConfirmedPlayers;
    }

    private List<int[]> getSimilarColorPairs(List<Color> colors, int threshold) {
        List<int[]> similarColorPairs = new ArrayList<>();
        for (int i = 0; i < colors.size(); i++) {
            for (int j = i + 1; j < colors.size(); j++) {
                if (colorsAreSimilar(colors.get(i), colors.get(j), threshold)) {
                    similarColorPairs.add(new int[]{i, j});
                }
            }
        }
        return similarColorPairs;
    }

    private boolean colorsAreSimilar(Color color1, Color color2, int threshold) {
        Double value1 = Math.pow((color1.getRed() - color2.getRed()), 2);
        Double value2 = Math.pow((color1.getGreen() - color2.getGreen()), 2);
        Double value3 = Math.pow((color1.getBlue() - color2.getBlue()), 2);
        return Math.sqrt(value1 + value2 + value3) <= threshold;
    }
}
