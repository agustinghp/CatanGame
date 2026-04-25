package org.skiBums;

import org.skiBums.Commands.CommandFactory;
import org.skiBums.Structures.Building;
import org.skiBums.Tiles.Tile;
import org.skiBums.geometry.FlatTopHexPixelLayout;
import org.skiBums.geometry.HexCoord;
import org.skiBums.Structures.BuildingType;
import org.skiBums.Structures.Road;

import java.util.List;
import java.util.Map;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.EnumMap;

public class BoardPanel extends JPanel {

    private static final double SETUP_HINT_DIAMETER_FACTOR = 0.5;
    private static final double HEX_LAYOUT_SIZE = 40.0;
    private static final float SETUP_ROAD_HINT_STROKE_MIN = 5f;
    private static final double SETUP_ROAD_HINT_STROKE_FACTOR = 0.24;
    private static final Color BUILD_HINT_COLOR = new Color(70, 120, 255, 170);

    private Board board;
    private GameController gameController;
    private final Map<ResourceType, BufferedImage> tileTextures;
    // This will be null intil someone clicks a valid vertex
    private Integer selectedSetupVertexId;
    private BuildSelection pendingBuildSelection = BuildSelection.NONE;

    public enum BuildSelection {
        NONE,
        ROAD,
        SETTLEMENT,
        CITY
    }

    public BoardPanel() {
        tileTextures = loadTileTextures();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (gameController != null && gameController.isWaitingForHumanSetup()) {
                    handleSetupClick(e.getX(), e.getY());
                    return;
                }
                handleMainTurnClick(e.getX(), e.getY());
            }
        });
    }


    public void setBoard(Board board) {
        this.board = board;
        selectedSetupVertexId = null;
        pendingBuildSelection = BuildSelection.NONE;
    }

    public void setGameController(GameController gameController) {
        this.gameController = gameController;
        selectedSetupVertexId = null;
        pendingBuildSelection = BuildSelection.NONE;
    }

    public void setPendingBuildSelection(BuildSelection selection) {
        pendingBuildSelection = selection == null ? BuildSelection.NONE : selection;
        repaint();
    }

    public BuildSelection getPendingBuildSelection() {
        return pendingBuildSelection;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        if (board == null) {
            return;
        }
        if (gameController == null || !gameController.isWaitingForHumanSetup()) {
            selectedSetupVertexId = null;
        }
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double originX = getWidth() / 2.0;
        double originY = getHeight() / 2.0;
        double size = HEX_LAYOUT_SIZE;
        FlatTopHexPixelLayout layout = new FlatTopHexPixelLayout(size, originX, originY);

        for (Map.Entry<HexCoord, Tile> e : board.getTiles().entrySet()) {
            HexCoord hex = e.getKey();
            Tile tile = e.getValue();
            Color fill = colorForTile(tile);
            drawHex(g2, hex, size, originX, originY, fill, Color.BLACK);
        }
        List<Vertex> vertices = board.getVertices();
        drawRoads(g2, layout, vertices, size);
        for (Map.Entry<Integer, Building> e : board.getBuildings().entrySet()) {
            int vertexId = e.getKey();
            if (vertexId < 0 || vertexId >= vertices.size()) {
                continue;
            }
            Vertex v = vertices.get(vertexId);
            double[] pt = layout.vertexIntersectionToPixel(v);
            drawHouse(g2, pt[0], pt[1], size, e.getValue().type(), e.getValue().owner().getColor());
        }
        if (gameController != null) {
            if (gameController.isWaitingForHumanSetup()) {
                if (selectedSetupVertexId != null) {
                    List<Vertex> verts = board.getVertices();
                    if (selectedSetupVertexId >= 0 && selectedSetupVertexId < verts.size()) {
                        Vertex selected = verts.get(selectedSetupVertexId);
                        if (board.canPlaceSettlement(selected.id())) {
                            drawSettlementHintCircle(g2, layout, selected, size, false);
                            drawSetupRoadHints(g2, layout, verts, size, selected.id());
                        } else {
                            selectedSetupVertexId = null;
                        }
                    } else {
                        selectedSetupVertexId = null;
                    }
                } else {
                    for (Vertex v : board.getVertices()) {
                        if (board.canPlaceSettlement(v.id())) {
                            drawSettlementHintCircle(g2, layout, v, size, true);
                        }
                    }
                }
            } else if (isHumanMainTurnActive()) {
                drawMainTurnHints(g2, layout, vertices, size);
            } else {
                pendingBuildSelection = BuildSelection.NONE;
            }
        }
    }

    private void drawMainTurnHints(Graphics2D g2, FlatTopHexPixelLayout layout, List<Vertex> vertices, double size) {
        Player current = gameController.getCurrentTurnPlayer();
        if (current == null || pendingBuildSelection == BuildSelection.NONE) {
            return;
        }
        if (pendingBuildSelection == BuildSelection.ROAD) {
            drawRoadBuildHints(g2, layout, vertices, size, current);
            return;
        }
        for (Vertex v : vertices) {
            boolean canBuild = pendingBuildSelection == BuildSelection.SETTLEMENT
                    ? board.canBuildSettlement(v.id(), current)
                    : board.canBuildCity(v.id(), current);
            if (!canBuild) {
                continue;
            }
            drawBuildHintCircle(g2, layout, v, size);
        }
    }

    private void drawBuildHintCircle(Graphics2D g2, FlatTopHexPixelLayout layout, Vertex v, double hexSize) {
        double[] pt = layout.vertexIntersectionToPixel(v);
        int d = (int) Math.round(hexSize * SETUP_HINT_DIAMETER_FACTOR);
        int x = (int) Math.round(pt[0] - d / 2.0);
        int y = (int) Math.round(pt[1] - d / 2.0);
        g2.setColor(BUILD_HINT_COLOR);
        g2.fillOval(x, y, d, d);
    }

    private void drawRoadBuildHints(Graphics2D g2, FlatTopHexPixelLayout layout, List<Vertex> vertices, double hexSize,
                                    Player player) {
        Color oldColor = g2.getColor();
        Stroke oldStroke = g2.getStroke();
        float w = (float) Math.max(SETUP_ROAD_HINT_STROKE_MIN, hexSize * SETUP_ROAD_HINT_STROKE_FACTOR);
        g2.setColor(BUILD_HINT_COLOR);
        g2.setStroke(new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        List<Edge> edges = board.getEdges();
        for (Edge edge : edges) {
            if (!board.canBuildRoad(edge.id(), player)) {
                continue;
            }
            int a = edge.vertexIdA();
            int b = edge.vertexIdB();
            if (a < 0 || b < 0 || a >= vertices.size() || b >= vertices.size()) {
                continue;
            }
            double[] p1 = layout.vertexIntersectionToPixel(vertices.get(a));
            double[] p2 = layout.vertexIntersectionToPixel(vertices.get(b));
            g2.draw(new Line2D.Double(p1[0], p1[1], p2[0], p2[1]));
        }
        g2.setColor(oldColor);
        g2.setStroke(oldStroke);
    }

    private static void drawSettlementHintCircle(Graphics2D g2, FlatTopHexPixelLayout layout, Vertex v,
                                                 double hexSize, boolean transparent) {
        double[] pt = layout.vertexIntersectionToPixel(v);
        int d = (int) Math.round(hexSize * SETUP_HINT_DIAMETER_FACTOR);
        int x = (int) Math.round(pt[0] - d / 2.0);
        int y = (int) Math.round(pt[1] - d / 2.0);
        g2.setColor(transparent
                ? new Color(140, 140, 140, 180)
                : new Color(140, 140, 140, 255));
        g2.fillOval(x, y, d, d);
    }

    private void drawSetupRoadHints(Graphics2D g2, FlatTopHexPixelLayout layout, List<Vertex> vertices,
                                    double hexSize, int fromVertexId) {
        Color oldColor = g2.getColor();
        Stroke oldStroke = g2.getStroke();

        float w = (float) Math.max(SETUP_ROAD_HINT_STROKE_MIN, hexSize * SETUP_ROAD_HINT_STROKE_FACTOR);
        g2.setColor(new Color(110, 110, 120, 130)); // Transparent gray
        g2.setStroke(new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        List<Edge> edges = board.getEdges();
        for (int edgeId : board.getConnectedEdges(fromVertexId)) {
            if (board.hasRoadAt(edgeId)) {
                continue;
            }
            if (edgeId < 0 || edgeId >= edges.size()) {
                continue;
            }
            Edge edge = edges.get(edgeId);
            int AVertexId = edge.vertexIdA();
            int BVertexID = edge.vertexIdB();
            if (AVertexId < 0 || BVertexID < 0 || AVertexId >= vertices.size() || BVertexID >= vertices.size()) {
                continue;
            }
            double[] p1 = layout.vertexIntersectionToPixel(vertices.get(AVertexId));
            double[] p2 = layout.vertexIntersectionToPixel(vertices.get(BVertexID));
            g2.draw(new Line2D.Double(p1[0], p1[1], p2[0], p2[1]));
        }

        g2.setColor(oldColor);
        g2.setStroke(oldStroke);
    }

    private void drawRoads(Graphics2D g2, FlatTopHexPixelLayout layout, List<Vertex> vertices, double hexSize) {
        Color oldColor = g2.getColor();
        Stroke oldStroke = g2.getStroke();
        float w = (float) Math.max(4.0, hexSize * 0.11);
        g2.setStroke(new BasicStroke(w, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        List<Edge> edges = board.getEdges();
        for (Map.Entry<Integer, Road> e : board.getRoads().entrySet()) {
            int edgeId = e.getKey();
            if (edgeId < 0 || edgeId >= edges.size()) {
                continue;
            }
            Edge edge = edges.get(edgeId);
            int a = edge.vertexIdA();
            int b = edge.vertexIdB();
            if (a < 0 || b < 0 || a >= vertices.size() || b >= vertices.size()) {
                continue;
            }
            double[] p1 = layout.vertexIntersectionToPixel(vertices.get(a));
            double[] p2 = layout.vertexIntersectionToPixel(vertices.get(b));
            g2.setColor(e.getValue().owner().getColor());
            g2.draw(new Line2D.Double(p1[0], p1[1], p2[0], p2[1]));
        }

        g2.setColor(oldColor);
        g2.setStroke(oldStroke);
    }

    private void handleSetupClick(int mx, int my) {
        if (board == null || gameController == null || !gameController.isWaitingForHumanSetup()) {
            return;
        }
        double originX = getWidth() / 2.0;
        double originY = getHeight() / 2.0;
        double size = HEX_LAYOUT_SIZE;
        FlatTopHexPixelLayout layout = new FlatTopHexPixelLayout(size, originX, originY);
        List<Vertex> vertexList = board.getVertices();

        if (selectedSetupVertexId == null) {
            int d = (int) Math.round(size * SETUP_HINT_DIAMETER_FACTOR);
            double radius = d / 2.0;

            for (Vertex v : vertexList) {
                if (!board.canPlaceSettlement(v.id())) {
                    continue;
                }
                double[] pt = layout.vertexIntersectionToPixel(v);
                double dx = mx - pt[0];
                double dy = my - pt[1];
                if (dx * dx + dy * dy <= radius * radius) {
                    selectedSetupVertexId = v.id();
                    repaint();
                    return;
                }
            }
            return;
        }

        Player player = gameController.getCurrentSetupPlayer();
        if (player == null) {
            return;
        }

        float strokePx = (float) Math.max(SETUP_ROAD_HINT_STROKE_MIN, size * SETUP_ROAD_HINT_STROKE_FACTOR);
        double hitMaxDist = strokePx / 2.0 + 8.0;

        List<Edge> edges = board.getEdges();
        for (int edgeId : board.getConnectedEdges(selectedSetupVertexId)) {
            if (board.hasRoadAt(edgeId)) {
                continue;
            }
            if (edgeId < 0 || edgeId >= edges.size()) {
                continue;
            }
            Edge edge = edges.get(edgeId);
            int a = edge.vertexIdA();
            int b = edge.vertexIdB();
            if (a < 0 || b < 0 || a >= vertexList.size() || b >= vertexList.size()) {
                continue;
            }
            double[] p1 = layout.vertexIntersectionToPixel(vertexList.get(a));
            double[] p2 = layout.vertexIntersectionToPixel(vertexList.get(b));
            Line2D seg = new Line2D.Double(p1[0], p1[1], p2[0], p2[1]);
            if (seg.ptSegDist(mx, my) <= hitMaxDist) {
                boolean ok = CommandFactory.placeStartingSettlement(
                        gameController, player, edgeId, selectedSetupVertexId).execute();
                if (ok) {
                    selectedSetupVertexId = null;
                }
                repaint();
                return;
            }
        }
    }

    private void handleMainTurnClick(int mx, int my) {
        if (board == null || gameController == null || !isHumanMainTurnActive()) {
            return;
        }
        if (pendingBuildSelection == BuildSelection.NONE) {
            return;
        }
        Player player = gameController.getCurrentTurnPlayer();
        if (player == null) {
            return;
        }

        double originX = getWidth() / 2.0;
        double originY = getHeight() / 2.0;
        double size = HEX_LAYOUT_SIZE;
        FlatTopHexPixelLayout layout = new FlatTopHexPixelLayout(size, originX, originY);
        List<Vertex> vertices = board.getVertices();

        boolean executed = switch (pendingBuildSelection) {
            case ROAD -> tryBuildRoadAtPoint(mx, my, size, layout, vertices, player);
            case SETTLEMENT -> tryBuildSettlementAtPoint(mx, my, size, layout, vertices, player);
            case CITY -> tryBuildCityAtPoint(mx, my, size, layout, vertices, player);
            case NONE -> false;
        };
        if (executed) {
            pendingBuildSelection = BuildSelection.NONE;
        }
        repaint();
    }

    private boolean tryBuildSettlementAtPoint(int mx, int my, double size, FlatTopHexPixelLayout layout,
                                              List<Vertex> vertices, Player player) {
        Integer vertexId = findClickedVertex(mx, my, size, layout, vertices);
        if (vertexId == null || !board.canBuildSettlement(vertexId, player)) {
            return false;
        }
        return CommandFactory.buildSettlement(gameController, player, vertexId).execute();
    }

    private boolean tryBuildCityAtPoint(int mx, int my, double size, FlatTopHexPixelLayout layout,
                                        List<Vertex> vertices, Player player) {
        Integer vertexId = findClickedVertex(mx, my, size, layout, vertices);
        if (vertexId == null || !board.canBuildCity(vertexId, player)) {
            return false;
        }
        return CommandFactory.buildCity(gameController, player, vertexId).execute();
    }

    private boolean tryBuildRoadAtPoint(int mx, int my, double size, FlatTopHexPixelLayout layout,
                                        List<Vertex> vertices, Player player) {
        float strokePx = (float) Math.max(SETUP_ROAD_HINT_STROKE_MIN, size * SETUP_ROAD_HINT_STROKE_FACTOR);
        double hitMaxDist = strokePx / 2.0 + 8.0;
        for (Edge edge : board.getEdges()) {
            if (!board.canBuildRoad(edge.id(), player)) {
                continue;
            }
            int a = edge.vertexIdA();
            int b = edge.vertexIdB();
            if (a < 0 || b < 0 || a >= vertices.size() || b >= vertices.size()) {
                continue;
            }
            double[] p1 = layout.vertexIntersectionToPixel(vertices.get(a));
            double[] p2 = layout.vertexIntersectionToPixel(vertices.get(b));
            Line2D seg = new Line2D.Double(p1[0], p1[1], p2[0], p2[1]);
            if (seg.ptSegDist(mx, my) <= hitMaxDist) {
                return CommandFactory.buildRoad(gameController, player, edge.id()).execute();
            }
        }
        return false;
    }

    private Integer findClickedVertex(int mx, int my, double size, FlatTopHexPixelLayout layout, List<Vertex> vertices) {
        int d = (int) Math.round(size * SETUP_HINT_DIAMETER_FACTOR);
        double radius = d / 2.0;
        for (Vertex v : vertices) {
            double[] pt = layout.vertexIntersectionToPixel(v);
            double dx = mx - pt[0];
            double dy = my - pt[1];
            if (dx * dx + dy * dy <= radius * radius) {
                return v.id();
            }
        }
        return null;
    }

    private boolean isHumanMainTurnActive() {
        if (gameController == null || !gameController.hasMainGameStarted() || gameController.isGameOver()) {
            return false;
        }
        Player current = gameController.getCurrentTurnPlayer();
        return current != null && !current.isAI();
    }


    private static Color colorForTile(Tile tile) {
        return GameConstants.colorForResource(tile.getResourceType());
    }

    private Map<ResourceType, BufferedImage> loadTileTextures() {
        Map<ResourceType, BufferedImage> textures = new EnumMap<>(ResourceType.class);
        putTexture(textures, ResourceType.WOOD, "/Images/Wood No background.png");
        putTexture(textures, ResourceType.BRICK, "/Images/Brick No Background.png");
        putTexture(textures, ResourceType.SHEEP, "/Images/Sheep No Background.png");
        putTexture(textures, ResourceType.WHEAT, "/Images/Wheat No Background.png");
        putTexture(textures, ResourceType.ROCK, "/Images/Rock No Background.png");
        putTexture(textures, ResourceType.DESERT, "/Images/Desert No Background.png");
        return textures;
    }

    private static void putTexture(Map<ResourceType, BufferedImage> textures, ResourceType type, String resourcePath) {
        try (InputStream in = BoardPanel.class.getResourceAsStream(resourcePath)) {
            if (in != null) {
                textures.put(type, ImageIO.read(in));
            }
        } catch (IOException ignored) {
        }
    }


    private static void axialToPixelFlatTop(HexCoord hex, double size, double originX, double originY,
                                            double[] outCenter) {
        double q = hex.q();
        double r = hex.r();
        double x = size * (3.0 / 2.0) * q;
        double y = size * Math.sqrt(3.0) * (r + q / 2.0);
        outCenter[0] = originX + x;
        outCenter[1] = originY + y;
    }

    private static Path2D.Double hexOutlineFlatTop(double cx, double cy, double size) {
        Path2D.Double path = new Path2D.Double();
        for (int i = 0; i < 6; i++) {
            double angle = i * Math.PI / 3.0;
            double px = cx + size * Math.cos(angle);
            double py = cy - size * Math.sin(angle);
            if (i == 0) {
                path.moveTo(px, py);
            } else {
                path.lineTo(px, py);
            }
        }
        path.closePath();
        return path;
    }

    private void drawHex(Graphics2D g2, HexCoord hex, double size, double originX, double originY,
                         Color fill, Color stroke) {
        double[] c = new double[2];
        axialToPixelFlatTop(hex, size, originX, originY, c);
        double cx = c[0];
        double cy = c[1];

        Path2D.Double path = hexOutlineFlatTop(cx, cy, size);

        g2.setColor(fill);
        g2.fill(path);

        Tile tile = board.tileAt(hex).orElse(null);
        if (tile != null) {
            BufferedImage texture = tileTextures.get(tile.getResourceType());
            if (texture != null) {
                Shape oldClip = g2.getClip();
                Composite oldComposite = g2.getComposite();
                g2.setClip(path);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.82f));
                Rectangle2D bounds = path.getBounds2D();
                double scale = 1.12;
                double drawW = bounds.getWidth() * scale;
                double drawH = bounds.getHeight() * scale;
                double drawX = bounds.getCenterX() - (drawW / 2.0);
                double drawY = bounds.getCenterY() - (drawH / 2.0);
                g2.drawImage(
                        texture,
                        (int) drawX,
                        (int) drawY,
                        (int) drawW,
                        (int) drawH,
                        null
                );
                g2.setComposite(oldComposite);
                g2.setClip(oldClip);
            }

            if (!tile.isDesert() && tile.getRollNumber() > 0) {
                drawRollNumberChit(g2, cx, cy, tile.getRollNumber(), size);
            }
        }

        g2.setColor(stroke);
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(path);
    }

    private static void drawRollNumberChit(Graphics2D g2, double cx, double cy, int rollNumber, double size) {
        int radius = (int) Math.round(size * 0.28);
        int x = (int) Math.round(cx) - radius;
        int y = (int) Math.round(cy) - radius;
        int d = radius * 2;

        Color oldColor = g2.getColor();
        Font oldFont = g2.getFont();
        Stroke oldStroke = g2.getStroke();

        g2.setColor(new Color(245, 235, 210));
        g2.fillOval(x, y, d, d);
        g2.setColor(new Color(90, 70, 40));
        g2.setStroke(new BasicStroke(1.3f));
        g2.drawOval(x, y, d, d);

        String text = String.valueOf(rollNumber);
        Font font = oldFont.deriveFont(Font.BOLD, (float) Math.max(14, radius * 0.95));
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int textX = (int) Math.round(cx) - (fm.stringWidth(text) / 2);
        int textY = (int) Math.round(cy) + ((fm.getAscent() - fm.getDescent()) / 2);
        g2.setColor((rollNumber == 6 || rollNumber == 8) ? new Color(180, 25, 25) : Color.BLACK);
        g2.drawString(text, textX, textY);

        g2.setColor(oldColor);
        g2.setFont(oldFont);
        g2.setStroke(oldStroke);
    }


    private static void drawHouse(Graphics2D g2, double cx, double cy, double hexSize,
                                  BuildingType type, Color fill) {
        double baseW = hexSize * 0.34;
        if (type == BuildingType.CITY) {
            baseW *= 1.55;
        }
        double bodyH = baseW * 0.52;
        double roofH = baseW * 0.48;
        double left = cx - baseW / 2.0;
        double right = cx + baseW / 2.0;
        double bodyTop = cy - bodyH;
        double peakY = bodyTop - roofH;

        Path2D.Double path = new Path2D.Double();
        path.moveTo(left, cy);
        path.lineTo(right, cy);
        path.lineTo(right, bodyTop);
        path.lineTo(cx, peakY);
        path.lineTo(left, bodyTop);
        path.closePath();

        Color old = g2.getColor();
        Stroke oldStroke = g2.getStroke();

        g2.setColor(fill);
        g2.fill(path);
        g2.setColor(new Color(25, 25, 25));
        g2.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(path);

        g2.setColor(old);
        g2.setStroke(oldStroke);
    }
}
