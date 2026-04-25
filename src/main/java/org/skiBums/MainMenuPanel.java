package org.skiBums;

import org.skiBums.geometry.HexCoord;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

public class MainMenuPanel extends JPanel {

    public MainMenuPanel(CardLayout cards, JPanel root) {
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;

        JButton startButton = new JButton("Start game");
        startButton.addActionListener(e -> cards.show(root, "SETUP"));
        add(startButton, gbc);

        gbc.gridy = 1;
        gbc.weighty = 0;
        gbc.insets = new Insets(0, 0, 32, 0);
        gbc.anchor = GridBagConstraints.PAGE_END;

        JButton quitButton = new JButton("Quit");
        quitButton.addActionListener(e -> System.exit(0));
        add(quitButton, gbc);
    }
}