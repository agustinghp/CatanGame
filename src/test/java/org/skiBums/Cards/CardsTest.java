package org.skiBums.Cards;

import org.junit.jupiter.api.Test;
import org.skiBums.GameConstants;
import org.skiBums.Player;
import org.skiBums.ResourceType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CardsTest {
    @Test
    void settlementBuildTest() {
        Player player1 = Player.getNewBuilder("Bob").build();

        // Give player resources for a settlement
        player1.giveResourceCard(ResourceType.BRICK);
        player1.giveResourceCard(ResourceType.WHEAT);
        player1.giveResourceCard(ResourceType.SHEEP);
        player1.giveResourceCard(ResourceType.WOOD);
        assertTrue(player1.hasResourcesFor(GameConstants.SETTLEMENT_COST));
    }
    @Test
    void CityBuildTest() {
        Player player1 = Player.getNewBuilder("Bob").build();
        // Give player resources for a city
        player1.giveResourceCard(ResourceType.ROCK);
        player1.giveResourceCard(ResourceType.ROCK);
        player1.giveResourceCard(ResourceType.ROCK);
        player1.giveResourceCard(ResourceType.WHEAT);
        player1.giveResourceCard(ResourceType.WHEAT);
        assertTrue(player1.hasResourcesFor(GameConstants.CITY_COST));
    }
    @Test
    void roadBuildTest() {
        Player player1 = Player.getNewBuilder("Bob").build();

        // Give player resources for a road
        player1.giveResourceCard(ResourceType.BRICK);
        player1.giveResourceCard(ResourceType.WOOD);
        assertTrue(player1.hasResourcesFor(GameConstants.ROAD_COST));
    }
    @Test
    void buildingWithInteractionBank() {
        Player player1 = Player.getNewBuilder("Bob").build();

        // Give player resources for a road, but has to trade with bank
        player1.giveResourceCard(ResourceType.BRICK);
        player1.giveResourceCard(ResourceType.BRICK);
        player1.giveResourceCard(ResourceType.BRICK);
        player1.giveResourceCard(ResourceType.BRICK);
        assertFalse(player1.canAffordWithBankTrades(GameConstants.ROAD_COST, GameConstants.BANK_TRADE_RATE));
        player1.giveResourceCard(ResourceType.BRICK);
        assertTrue(player1.canAffordWithBankTrades(GameConstants.ROAD_COST, GameConstants.BANK_TRADE_RATE));
    }

}

