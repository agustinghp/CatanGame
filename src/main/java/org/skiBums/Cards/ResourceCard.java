package org.skiBums.Cards;

import org.skiBums.ResourceType;

public class ResourceCard extends Card {
    private final ResourceType resourceType;
    private int quantity;

    private ResourceCard(ResourceType resourceType, int quantity) {
        this.resourceType = resourceType;
        this.quantity = Math.max(0, quantity);
    }



    public static ResourceCard newResourceCard(ResourceType resourceType) {
        return new ResourceCard(resourceType, 1);
    }

    public static ResourceCard newResourceCard(ResourceType resourceType, int quantity) {
        return new ResourceCard(resourceType, quantity);
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void addQuantity(int amount) {
        if (amount <= 0) {
            return;
        }
        quantity += amount;
    }

    public boolean removeQuantity(int amount) {
        if (amount <= 0 || quantity < amount) {
            return false;
        }
        quantity -= amount;
        return true;
    }
}
