package org.skiBums;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;

public class PlayerHandPanel extends JPanel {
    private static final int CARD_ICON_SIZE = 48;
    private static final Color DEFAULT_BG = new Color(245, 245, 245);
    private final JLabel title = new JLabel("Current Player Cards");
    private final JPanel cardsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
    private final Map<ResourceType, BufferedImage> cardImages = loadCardImages();

    public PlayerHandPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(8, 12, 6, 12));
        setPreferredSize(new Dimension(10, 132));
        setOpaque(true);
        setBackground(DEFAULT_BG);
        cardsRow.setOpaque(true);
        cardsRow.setBackground(DEFAULT_BG);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        add(title, BorderLayout.NORTH);
        add(cardsRow, BorderLayout.CENTER);
        renderCards(null);
    }

    public void setPlayer(Player player) {
        renderCards(player);
    }

    private void renderCards(Player player) {
        cardsRow.removeAll();
        if (player == null) {
            title.setText("Current Player Cards");
            setBackground(DEFAULT_BG);
            cardsRow.setBackground(DEFAULT_BG);
            cardsRow.add(new JLabel("Waiting for setup to finish..."));
            revalidate();
            repaint();
            return;
        }

        title.setText("Current Player Cards: " + player.getName());
        setBackground(DEFAULT_BG);
        cardsRow.setBackground(DEFAULT_BG);
        Map<ResourceType, Integer> counts = player.getResourceCardCounts();
        for (ResourceType type : new ResourceType[] {
                ResourceType.WOOD, ResourceType.BRICK, ResourceType.SHEEP, ResourceType.WHEAT, ResourceType.ROCK
        }) {
            cardsRow.add(buildCardSlot(type, counts.getOrDefault(type, 0)));
        }

        revalidate();
        repaint();
    }

    private JPanel buildCardSlot(ResourceType type, int count) {
        JPanel slot = new JPanel(new BorderLayout(4, 2));
        slot.setBorder(BorderFactory.createEtchedBorder());
        slot.setPreferredSize(new Dimension(95, 74));
        slot.setOpaque(true);
        slot.setBackground(colorForResource(type));

        JLabel iconLabel = new JLabel(type.name(), SwingConstants.CENTER);
        BufferedImage img = cardImages.get(type);
        if (img != null) {
            Image scaled = img.getScaledInstance(CARD_ICON_SIZE, CARD_ICON_SIZE, Image.SCALE_SMOOTH);
            iconLabel.setText("");
            iconLabel.setIcon(new ImageIcon(scaled));
        }

        JLabel countLabel = new JLabel("x" + count, SwingConstants.CENTER);
        slot.add(iconLabel, BorderLayout.CENTER);
        slot.add(countLabel, BorderLayout.SOUTH);
        return slot;
    }

    private static Color colorForResource(ResourceType type) {
        return GameConstants.colorForResource(type);
    }

    private Map<ResourceType, BufferedImage> loadCardImages() {
        Map<ResourceType, BufferedImage> images = new EnumMap<>(ResourceType.class);
        putImage(images, ResourceType.WOOD, "/Images/Wood No background.png");
        putImage(images, ResourceType.BRICK, "/Images/Brick No Background.png");
        putImage(images, ResourceType.SHEEP, "/Images/Sheep No Background.png");
        putImage(images, ResourceType.WHEAT, "/Images/Wheat No Background.png");
        putImage(images, ResourceType.ROCK, "/Images/Rock No Background.png");
        return images;
    }

    private static void putImage(Map<ResourceType, BufferedImage> images, ResourceType type, String path) {
        try (InputStream in = PlayerHandPanel.class.getResourceAsStream(path)) {
            if (in != null) {
                images.put(type, ImageIO.read(in));
            }
        } catch (IOException ignored) {
        }
    }
}
