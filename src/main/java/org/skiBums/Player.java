package org.skiBums;

import org.skiBums.Cards.DevelopmentCard;
import org.skiBums.Cards.ResourceCard;
import org.skiBums.Structures.Building;
import org.skiBums.Structures.BuildingType;
import org.skiBums.Structures.Road;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Player {
    private final String name;
    private List<ResourceCard> resourceCards;
    private List<DevelopmentCard> developmentCards;
    int victoryPoints = 0;
    private Map<Integer, Building> buildings = new HashMap<>();
    private Map<Integer, Road> roads = new HashMap<>();

    public Player(String name) {
        this.name = name;
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
        if (building.type() != BuildingType.CITY) {
            return;
        }
        // This replaces the settlement that was already there.
        this.buildings.put(vertexID, building);
    }
    public String getName(){
        return this.name;
    }
}
