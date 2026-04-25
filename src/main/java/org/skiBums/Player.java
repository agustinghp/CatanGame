package org.skiBums;

import org.skiBums.Cards.ResourceCard;
import org.skiBums.Strategies.AIStrategy;
import org.skiBums.Strategies.HumanStrategy;
import org.skiBums.Strategies.IStrategy;
import org.skiBums.Structures.Building;
import org.skiBums.Structures.BuildingType;
import org.skiBums.Structures.Road;
import java.awt.Color;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.skiBums.Structures.BuildingType.CITY;

public class Player {
    private final String name;
    private List<ResourceCard> resourceCards = new ArrayList<>();
    int victoryPoints = 0;
    private Map<Integer, Building> buildings = new HashMap<>();
    private Map<Integer, Road> roads = new HashMap<>();
    private Dice dice;
    private final Color color;
    private final boolean isAI;
    private final IStrategy strategy;


    private Player(String name, Color color, Dice dice, boolean isAI, IStrategy strategy) {
        this.name = name;
        this.color = color;
        this.dice = dice;
        this.isAI = isAI;
        this.strategy = strategy;
    }



    public static Builder getNewBuilder(String name) {
        return new Builder(name);
    }

    public static class Builder {
        private boolean isAI = true;
        private Color color = Color.GRAY;
        private final String name;
        private IStrategy strategy = new AIStrategy();
        private Dice dice = new Dice(6);

        private Builder(String name) {
            this.name = name;
        }

        public Builder isAI(boolean isAI) {
            if (isAI) {
                this.strategy = new AIStrategy();
            }
            else {
                this.strategy = new HumanStrategy();
            }
            this.isAI = isAI;
            return this;
        }

        public Builder setColor(Color color) {
            this.color = color;
            return this;
        }

        public Builder setDice(Dice dice) {
            this.dice = dice;
            return this;
        }

        public Player build() {
            return new Player(name, color, dice, isAI, strategy);
        }
    }

    public Color getColor() {
        return color;
    }

    public boolean isAI() {
        return isAI;
    }



    public void addRoad(Integer edgeID, Road road){
        this.roads.put(edgeID, road);
    }

    public void addSettlement(Integer vertexID, Building building) {
        if (building.type() != BuildingType.SETTLEMENT) {
            return;
        }
        this.buildings.put(vertexID, building);
    }

    public void addCity(Integer vertexID, Building building) {
        if (building.type() != CITY) {
            return;
        }
        // This replaces the settlement that was already there.
        this.buildings.put(vertexID, building);
    }
    public String getName(){
        return this.name;
    }

    public IStrategy getStrategy() {
        return strategy;
    }

    public int roll() {
        return this.dice.doubleRoll();
    }

    public void setCards(ArrayList<ResourceCard> resourceCards){
        this.resourceCards = new ArrayList<>();
        for (ResourceCard card : resourceCards) {
            if (card == null || card.getQuantity() <= 0) {
                continue;
            }
            mergeResourceCard(card.getResourceType(), card.getQuantity());
        }
    }

    public void giveResourceCard(ResourceType resourceType){
        if (resourceType == ResourceType.DESERT) {
            return;
        }
        mergeResourceCard(resourceType, 1);
    }

    public void getResourcesFromBuilding(Building building, ResourceType resourceType) {
        if (building.owner() != this) {
            throw new RuntimeException("Someone tried to steal resources from a building he doesn't own");
        }
        this.giveResourceCard(resourceType);
        if (building.type() == CITY) {
            this.giveResourceCard(resourceType);
        }
    }

    public List<ResourceCard> getResourceCards() {
        return Collections.unmodifiableList(resourceCards);
    }

    public Map<ResourceType, Integer> getResourceCardCounts() {
        Map<ResourceType, Integer> counts = new EnumMap<>(ResourceType.class);
        for (ResourceCard card : resourceCards) {
            ResourceType type = card.getResourceType();
            counts.put(type, counts.getOrDefault(type, 0) + card.getQuantity());
        }
        return Collections.unmodifiableMap(counts);
    }

    public boolean hasResourcesFor(Map<ResourceType, Integer> cost) {
        Map<ResourceType, Integer> current = getResourceCardCounts();
        for (Map.Entry<ResourceType, Integer> required : cost.entrySet()) {
            int available = current.getOrDefault(required.getKey(), 0);
            if (available < required.getValue()) {
                return false;
            }
        }
        return true;
    }

    public int countResource(ResourceType resourceType) {
        return getResourceCardCounts().getOrDefault(resourceType, 0);
    }

    public boolean canAffordWithBankTrades(Map<ResourceType, Integer> cost, int tradeRate) {
        if (tradeRate <= 0) {
            return false;
        }
        Map<ResourceType, Integer> current = getResourceCardCounts();
        int missingCards = 0;
        int excessCards = 0;
        for (ResourceType type : ResourceType.values()) {
            if (type == ResourceType.DESERT) {
                continue;
            }
            int available = current.getOrDefault(type, 0);
            int required = cost.getOrDefault(type, 0);
            if (available < required) {
                missingCards += (required - available);
            } else {
                excessCards += (available - required);
            }
        }
        return excessCards >= missingCards * tradeRate;
    }

    public boolean tradeWithBank(ResourceType giveResourceType, ResourceType receiveResourceType, int tradeRate) {
        if (giveResourceType == null || receiveResourceType == null) {
            return false;
        }
        if (tradeRate <= 0 || giveResourceType == receiveResourceType) {
            return false;
        }
        if (countResource(giveResourceType) < tradeRate) {
            return false;
        }
        if (!removeResourceQuantity(giveResourceType, tradeRate)) {
            return false;
        }
        giveResourceCard(receiveResourceType);
        return true;
    }

    public boolean spendResources(Map<ResourceType, Integer> cost) {
        if (!hasResourcesFor(cost)) {
            return false;
        }
        for (Map.Entry<ResourceType, Integer> required : cost.entrySet()) {
            removeResourceQuantity(required.getKey(), required.getValue());
        }
        return true;
    }

    private void mergeResourceCard(ResourceType resourceType, int quantity) {
        if (resourceType == null || quantity <= 0) {
            return;
        }
        for (ResourceCard card : resourceCards) {
            if (card.getResourceType() == resourceType) {
                card.addQuantity(quantity);
                return;
            }
        }
        resourceCards.add(ResourceCard.newResourceCard(resourceType, quantity));
    }

    private boolean removeResourceQuantity(ResourceType resourceType, int amount) {
        if (resourceType == null) {
            return false;
        }
        if (amount == 0) {
            return true;
        }
        if (amount < 0) {
            return false;
        }
        for (int i = 0; i < resourceCards.size(); i++) {
            ResourceCard card = resourceCards.get(i);
            if (card.getResourceType() != resourceType) {
                continue;
            }
            boolean removed = card.removeQuantity(amount);
            if (!removed) {
                return false;
            }
            if (card.getQuantity() == 0) {
                resourceCards.remove(i);
            }
            return true;
        }
        return false;
    }

    public int getVictoryPoints() {
        int points = 0;
        for (Building building : buildings.values()) {
            points += building.type() == CITY ? 2 : 1;
        }
        return points;
    }


}
