package org.skiBums.Strategies;

import org.skiBums.Board;
import org.skiBums.Commands.CommandFactory;
import org.skiBums.Edge;
import org.skiBums.GameController;
import org.skiBums.GameConstants;
import org.skiBums.Player;
import org.skiBums.ResourceType;
import org.skiBums.Vertex;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import java.util.Random;

public class AIStrategy implements IStrategy {

    @Override
    public boolean placeInitialSettlementAndRoad(Board board, Player self, Random random) {
        board.randomStartingSettlement(self, random);
        return true;
    }

    @Override
    public void takeTurn(GameController gameController, Player self, Random random) {
        Board board = gameController.getBoard();
        while (true) {
            if (gameController.isGameOver()) {
                return;
            }
            List<Integer> cityCandidates = collectCityCandidates(board, self);
            if (!cityCandidates.isEmpty()) {
                int chosen = cityCandidates.get(random.nextInt(cityCandidates.size()));
                if (!prepareResourcesForCostViaBank(gameController, self, GameConstants.CITY_COST)) {
                    CommandFactory.endTurn(gameController).execute();
                    return;
                }
                boolean built = CommandFactory.buildCity(gameController, self, chosen).execute();
                if (!built) {
                    CommandFactory.endTurn(gameController).execute();
                    return;
                }
                continue;
            }

            List<Integer> settlementCandidates = collectSettlementCandidates(board, self);
            if (!settlementCandidates.isEmpty()) {
                int chosen = settlementCandidates.get(random.nextInt(settlementCandidates.size()));
                if (!prepareResourcesForCostViaBank(gameController, self, GameConstants.SETTLEMENT_COST)) {
                    CommandFactory.endTurn(gameController).execute();
                    return;
                }
                boolean built = CommandFactory.buildSettlement(gameController, self, chosen).execute();
                if (!built) {
                    CommandFactory.endTurn(gameController).execute();
                    return;
                }
                continue;
            }

            // If there are settlement spots available on the map, do not fall back to roads.
            // This prevents AI from preferring cheap road spam over future expansion.
            if (hasAnySettlementOpportunityIgnoringResources(board, self)) {
                CommandFactory.endTurn(gameController).execute();
                return;
            }

            List<Integer> roadCandidates = collectRoadCandidates(board, self);
            if (!roadCandidates.isEmpty()) {
                int chosen = roadCandidates.get(random.nextInt(roadCandidates.size()));
                if (!prepareResourcesForCostViaBank(gameController, self, GameConstants.ROAD_COST)) {
                    CommandFactory.endTurn(gameController).execute();
                    return;
                }
                boolean built = CommandFactory.buildRoad(gameController, self, chosen).execute();
                if (!built) {
                    CommandFactory.endTurn(gameController).execute();
                    return;
                }
                continue;
            }

            CommandFactory.endTurn(gameController).execute();
            return;
        }
    }

    private static List<Integer> collectCityCandidates(Board board, Player self) {
        List<Integer> cityCandidates = new ArrayList<>();
        if (!self.canAffordWithBankTrades(GameConstants.CITY_COST, GameConstants.BANK_TRADE_RATE)) {
            return cityCandidates;
        }
        for (Vertex vertex : board.getVertices()) {
            int vertexId = vertex.id();
            if (board.canBuildCity(vertexId, self)) {
                cityCandidates.add(vertexId);
            }
        }
        return cityCandidates;
    }

    private static List<Integer> collectSettlementCandidates(Board board, Player self) {
        List<Integer> settlementCandidates = new ArrayList<>();
        if (!self.canAffordWithBankTrades(GameConstants.SETTLEMENT_COST, GameConstants.BANK_TRADE_RATE)) {
            return settlementCandidates;
        }
        for (Vertex vertex : board.getVertices()) {
            int vertexId = vertex.id();
            if (board.canBuildSettlement(vertexId, self)) {
                settlementCandidates.add(vertexId);
            }
        }
        return settlementCandidates;
    }

    private static boolean hasAnySettlementOpportunityIgnoringResources(Board board, Player self) {
        for (Vertex vertex : board.getVertices()) {
            if (board.canBuildSettlement(vertex.id(), self)) {
                return true;
            }
        }
        return false;
    }

    private static List<Integer> collectRoadCandidates(Board board, Player self) {
        List<Integer> roadCandidates = new ArrayList<>();
        if (!self.canAffordWithBankTrades(GameConstants.ROAD_COST, GameConstants.BANK_TRADE_RATE)) {
            return roadCandidates;
        }
        for (Edge edge : board.getEdges()) {
            int edgeId = edge.id();
            if (board.canBuildRoad(edgeId, self)) {
                roadCandidates.add(edgeId);
            }
        }
        return roadCandidates;
    }

    private static boolean prepareResourcesForCostViaBank(GameController gameController, Player self,
                                                          Map<ResourceType, Integer> cost) {
        if (self.hasResourcesFor(cost)) {
            return true;
        }
        if (!self.canAffordWithBankTrades(cost, GameConstants.BANK_TRADE_RATE)) {
            return false;
        }
        Map<ResourceType, Integer> counts = new EnumMap<>(ResourceType.class);
        counts.putAll(self.getResourceCardCounts());

        while (!coversCost(counts, cost)) {
            ResourceType neededType = pickMissingResource(counts, cost);
            ResourceType giveType = pickTradeAwayResource(counts, cost, GameConstants.BANK_TRADE_RATE);
            if (neededType == null || giveType == null) {
                return false;
            }
            boolean traded = gameController.playerTradesWithBank(self, giveType, neededType);
            if (!traded) {
                return false;
            }
            counts.put(giveType, counts.getOrDefault(giveType, 0) - GameConstants.BANK_TRADE_RATE);
            counts.put(neededType, counts.getOrDefault(neededType, 0) + 1);
        }
        return true;
    }

    private static boolean coversCost(Map<ResourceType, Integer> counts, Map<ResourceType, Integer> cost) {
        for (Map.Entry<ResourceType, Integer> required : cost.entrySet()) {
            if (counts.getOrDefault(required.getKey(), 0) < required.getValue()) {
                return false;
            }
        }
        return true;
    }

    private static ResourceType pickMissingResource(Map<ResourceType, Integer> counts, Map<ResourceType, Integer> cost) {
        ResourceType best = null;
        int largestDeficit = 0;
        for (Map.Entry<ResourceType, Integer> required : cost.entrySet()) {
            int deficit = required.getValue() - counts.getOrDefault(required.getKey(), 0);
            if (deficit > largestDeficit) {
                largestDeficit = deficit;
                best = required.getKey();
            }
        }
        return best;
    }

    private static ResourceType pickTradeAwayResource(Map<ResourceType, Integer> counts, Map<ResourceType, Integer> cost,
                                                      int tradeRate) {
        ResourceType best = null;
        int largestExcess = 0;
        for (Map.Entry<ResourceType, Integer> entry : counts.entrySet()) {
            ResourceType type = entry.getKey();
            int required = cost.getOrDefault(type, 0);
            int excess = entry.getValue() - required;
            if (excess >= tradeRate && excess > largestExcess) {
                largestExcess = excess;
                best = type;
            }
        }
        return best;
    }
}
