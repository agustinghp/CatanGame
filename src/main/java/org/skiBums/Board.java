package org.skiBums;

import org.skiBums.Structures.Building;
import org.skiBums.Structures.BuildingType;
import org.skiBums.Structures.Road;
import org.skiBums.Tiles.Tile;
import org.skiBums.geometry.HexCoord;

import java.util.*;

public class Board {
    private Map<HexCoord, Tile> landTiles = new HashMap<>();
    private List<Vertex> vertices = List.of();
    private List<Edge> edges = List.of();
    private Map<Integer, List<Integer>> adjacentEdgeIdsByVertexId = Map.of();
    private Map<Integer, List<Integer>> adjacentVertexIdsByVertexId = Map.of();
    private Map<Integer, Building> buildings = new HashMap<>();
    private Map<Integer, Road> roads = new HashMap<>();

    void addTile(Tile tile, HexCoord hex) {
        tile.setHexCoord(hex);
        landTiles.put(hex, tile);
    }

    public void setup() {
        landTiles = new HashMap<>(Tile.setUpTiles());
        vertices = buildVertices(GameConstants.ALL_LAND_HEXES);
        edges = buildEdges(vertices, GameConstants.ALL_LAND_HEXES);


        VertexAdjacency adj = buildVertexAdjacency(edges, vertices.size());
        adjacentEdgeIdsByVertexId = adj.incidentEdgeIdsByVertexId();
        adjacentVertexIdsByVertexId = adj.adjacentVertexIdsByVertexId();
        setNumbers();
    }

    public void randomStartingSettlement(Player player, Random random) {
        // For this function we will first choose a random vertex and then a random edge from that vertex.
        int vertexID = vertices.get(random.nextInt(vertices.size())).id();
        while (!settlementProperlySpaced(vertexID)){
            vertexID = vertices.get(random.nextInt(vertices.size())).id();
        }
        buildSettlement(vertexID, player);
        List<Integer> incidentEdges = this.getConnectedEdges(vertexID);
        Integer roadEdgeID = incidentEdges.get(random.nextInt(incidentEdges.size()));
        buildRoad(roadEdgeID, player);
    }

    public boolean selectedStartingSettlement(Player player, int edgeID, int vertexID) {
        if (!settlementProperlySpaced(vertexID)) {
            return false;
        }
        List<Integer> incidentEdges = this.getConnectedEdges(vertexID);
        if (!incidentEdges.contains(edgeID)) {
            return false;
        }
        buildSettlement(vertexID, player);
        buildRoad(edgeID, player);
        return true;
    }

    private record VertexAdjacency(
            Map<Integer, List<Integer>> incidentEdgeIdsByVertexId,
            Map<Integer, List<Integer>> adjacentVertexIdsByVertexId) {}

    private static VertexAdjacency buildVertexAdjacency(List<Edge> edges, int vertexCount) {
        Map<Integer, List<Integer>> incidentMutable = new HashMap<>();
        Map<Integer, List<Integer>> adjacentMutable = new HashMap<>();
        for (int i = 0; i < vertexCount; i++) {
            incidentMutable.put(i, new ArrayList<>());
            adjacentMutable.put(i, new ArrayList<>());
        }
        for (Edge e : edges) {
            int a = e.vertexIdA();
            int b = e.vertexIdB();
            incidentMutable.get(a).add(e.id());
            incidentMutable.get(b).add(e.id());
            adjacentMutable.get(a).add(b);
            adjacentMutable.get(b).add(a);
        }
        Map<Integer, List<Integer>> incidentFrozen = new LinkedHashMap<>();
        Map<Integer, List<Integer>> adjacentFrozen = new LinkedHashMap<>();
        for (int i = 0; i < vertexCount; i++) {
            List<Integer> inc = incidentMutable.get(i);
            List<Integer> adj = adjacentMutable.get(i);
            Collections.sort(inc);
            Collections.sort(adj);
            incidentFrozen.put(i, List.copyOf(inc));
            adjacentFrozen.put(i, List.copyOf(adj));
        }
        return new VertexAdjacency(
                Collections.unmodifiableMap(incidentFrozen), Collections.unmodifiableMap(adjacentFrozen));
    }



    public List<Integer> getConnectedEdges(int vertexId) {
        return adjacentEdgeIdsByVertexId.getOrDefault(vertexId, List.of());
    }


    public List<Integer> getConnectedVertices(int vertexId) {
        return adjacentVertexIdsByVertexId.getOrDefault(vertexId, List.of());
    }


    private static List<Edge> buildEdges(List<Vertex> vertices, Set<HexCoord> land) {
        Map<String, Vertex> byTripleKey = new HashMap<>();
        for (Vertex v : vertices) {
            byTripleKey.put(vertexKey(v.corners()), v);
        }
        Map<String, Edge> seen = new LinkedHashMap<>();
        for (HexCoord h : land) {
            for (int d = 0; d < 6; d++) {
                Vertex va = vertexAtCorner(byTripleKey, h, d);
                Vertex vb = vertexAtCorner(byTripleKey, h, (d + 1) % 6);
                if (va == null || vb == null) {
                    continue;
                }
                int a = Math.min(va.id(), vb.id());
                int b = Math.max(va.id(), vb.id());
                String key = a + "|" + b;
                if (!seen.containsKey(key)) {
                    seen.put(key, new Edge(seen.size(), a, b));
                }
            }
        }
        return List.copyOf(seen.values());
    }

    private static Vertex vertexAtCorner(Map<String, Vertex> byTripleKey, HexCoord h, int corner) {
        int prev = (corner + 5) % 6;
        List<HexCoord> tri = List.of(h, h.neighbor(prev), h.neighbor(corner));
        return byTripleKey.get(vertexKey(tri));
    }

    public List<Edge> getEdges() {
        return edges;
    }

    private static List<Vertex> buildVertices(Set<HexCoord> land) {
        Map<String, Vertex> seen = new LinkedHashMap<>();
        for (HexCoord h : land) {
            for (int i = 0; i < 6; i++) {
                int prev = (i + 5) % 6;
                List<HexCoord> tri = List.of(h, h.neighbor(prev), h.neighbor(i));
                String key = vertexKey(tri);
                seen.putIfAbsent(key, new Vertex(seen.size(), sortTriple(tri)));
            }
        }
        return List.copyOf(seen.values());
    }

    private static List<HexCoord> sortTriple(List<HexCoord> tri) {
        var copy = new ArrayList<>(tri);
        copy.sort(Comparator.comparingInt(HexCoord::q).thenComparingInt(HexCoord::r));
        return List.copyOf(copy);
    }

    private static String vertexKey(List<HexCoord> tri) {
        List<HexCoord> s = sortTriple(tri);
        return s.get(0).q() + "," + s.get(0).r() + "|" + s.get(1).q() + "," + s.get(1).r() + "|" + s.get(2).q() + "," + s.get(2).r();
    }

    private void setNumbers() {
        int numbersPlaced = 0;
        for (HexCoord hex : GameConstants.LAND_HEX_NUMBER_SPIRAL_ORDER) {
            Tile tile = tileAt(hex).orElseThrow(() -> new IllegalStateException("no tile at " + hex));
            if (!tile.isDesert()) {
                tile.setRollNumber(GameConstants.LIST_OF_ROLL_NUMS.get(numbersPlaced));
                numbersPlaced++;
            }
        }
    }

    public Optional<Tile> tileAt(HexCoord hex) {
        return Optional.ofNullable(landTiles.get(hex));
    }

    public Map<HexCoord, Tile> getLandTiles() {
        return Collections.unmodifiableMap(landTiles);
    }

    public List<Vertex> getVertices() {
        return vertices;
    }


    public boolean buildSettlementIfPossible(int vertexID, Player player) {
        if (!this.canBuildSettlement(vertexID, player)) {
            return false;
        }
        this.buildSettlement(vertexID, player);
        return true;
    }

    public void buildSettlement(int vertexID, Player player) {
        Building settlement = new Building(player, BuildingType.SETTLEMENT);
        buildings.putIfAbsent(vertexID, settlement);
        player.addSettlement(vertexID, settlement);

    }


    private boolean canBuildSettlement(int vertexID, Player player) {
        return settlementConnectsWithRoad(vertexID, player) &&
                settlementProperlySpaced(vertexID);
    }

    private boolean settlementProperlySpaced(int vertexID){
        // If there is already another settlement in place, return false
        if (hasSettlement(vertexID)) {
            return false;
        }
        boolean canBuildSettlement = true;
        List<Integer> verticesConnectingToVertex = this.getConnectedVertices(vertexID);
        for (Integer adjacentVertexID : verticesConnectingToVertex) {
            if (hasSettlement(adjacentVertexID)) {
                canBuildSettlement = false;
            }
        }
        return canBuildSettlement;
    }

    private boolean settlementConnectsWithRoad(int vertexID, Player player) {
        List<Integer> edgesConnectingToVertex = this.getConnectedEdges(vertexID);
        for (int edgeID : edgesConnectingToVertex) {
            if (hasPlayersRoad(edgeID, player)) {
                return true;
            }
        }
        return false;
    }

    public boolean buildCityIfPossible(int vertexID, Player player) {
        if (!this.canBuildCity(vertexID, player)) {
            return false;
        }
        this.buildCity(vertexID, player);
        return true;
    }

    public void buildCity(int vertexID, Player player) {
        Building city = new Building(player,BuildingType.CITY);
        buildings.put(vertexID, city);
        player.addCity(vertexID, city);
    }



    public boolean buildRoadIfPossible(int edgeID, Player player) {
        if (!this.canBuildRoad(edgeID, player)){
            return false;
        }
        this.buildRoad(edgeID, player);
        return true;
    }

    public void buildRoad(int edgeID, Player player) {
        Road road = new Road(player);
        roads.putIfAbsent(edgeID, road);
        player.addRoad(edgeID, road);
    }


    private boolean canBuildCity(int vertexID, Player player) {
        return this.hasSettlement(vertexID) && hasPlayersBuilding(vertexID, player);
    }

    private boolean canBuildRoad(int edgeID, Player player) {
        if (hasRoad(edgeID)) {
            return false;
        }
        return roadConnectsToBuilding(edgeID, player) || roadConnectsWithRoad(edgeID, player);
    }

    private boolean hasSettlement(int vertexID) {
        if (!this.hasBuilding(vertexID)) {
            return false;
        }
        return buildings.get(vertexID).type().equals(BuildingType.SETTLEMENT);
    }


    private boolean hasCity(int vertexID) {
        if (!this.hasBuilding(vertexID)) {
            return false;
        }
        return buildings.get(vertexID).type().equals(BuildingType.CITY);
    }

    private boolean hasBuilding(int vertexID) {
        return buildings.containsKey(vertexID);
    }

    private boolean hasPlayersBuilding(int vertexID, Player player) {
        if (!this.hasBuilding(vertexID)) {
            return false;
        }
        return buildings.get(vertexID).owner().equals(player);
    }

    private boolean roadConnectsToBuilding(int edgeID, Player player) {
        Edge edge = edges.get(edgeID);
        int vertexIDA = edge.vertexIdA();
        int vertexIDB = edge.vertexIdB();
        return (this.hasPlayersBuilding(vertexIDA, player) || this.hasPlayersBuilding(vertexIDB, player));
    }

    boolean roadConnectsWithRoad(int edgeID, Player player) {
        Edge edge = edges.get(edgeID);
        int vertexIDA = edge.vertexIdA();
        int vertexIDB = edge.vertexIdB();
        List<Integer> adjacentEdgeVertices = new ArrayList<>(List.copyOf(this.getConnectedEdges(vertexIDA)));
        adjacentEdgeVertices.addAll(adjacentEdgeIdsByVertexId.get(vertexIDB));
        for (Integer vertexEdgeID : adjacentEdgeVertices) {
            if (vertexEdgeID != edgeID) {
                if (hasPlayersRoad(vertexEdgeID, player)) {
                    return true;
                }
            }
        }
        return false;
    }



    private boolean hasPlayersRoad(int edgeID, Player player) {
        if (!this.hasRoad(edgeID)) {
            return false;
        }
        return roads.get(edgeID).owner().equals(player);
    }

    private boolean hasRoad(int edgeID) {
        return roads.containsKey(edgeID);
    }


}
