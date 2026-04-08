package org.skiBums;

import org.skiBums.Cards.Card;
import org.skiBums.Cards.DevelopmentCard;
import org.skiBums.Cards.ResourceCard;
import org.skiBums.Structures.Building;
import org.skiBums.Structures.BuildingType;
import org.skiBums.Structures.Road;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.skiBums.Structures.BuildingType.CITY;

public class Player {
    private final String name;
    private List<ResourceCard> resourceCards;
    private List<DevelopmentCard> developmentCards;
    int victoryPoints = 0;
    private Map<Integer, Building> buildings = new HashMap<>();
    private Map<Integer, Road> roads = new HashMap<>();
    private Dice dice;

    public Player(String name) {
        this.name = name;
    }

    public Player(String name, Dice dice) {
        this.name = name;
        this.dice = dice;
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

    public int roll() {
        return this.dice.doubleRoll();
    }

    public void setCards(ArrayList<ResourceCard> resourceCards, ArrayList<DevelopmentCard> developmentCards){
        this.resourceCards = resourceCards;
        this.developmentCards = developmentCards;
    }

    public void giveResourceCard(ResourceType resourceType){
        if (resourceType == ResourceType.DESERT) {
            return;
        }
        ResourceCard.newResourceCard(resourceType);
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


}
