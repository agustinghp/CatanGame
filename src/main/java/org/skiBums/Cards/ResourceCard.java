package org.skiBums.Cards;

import org.skiBums.ResourceType;

public class ResourceCard extends Card {
    ResourceType resourceType;

    private ResourceCard(ResourceType resourceType) {
        this.resourceType = resourceType;
    }



    public static ResourceCard newResourceCard(ResourceType resourceType) {
        return new ResourceCard(resourceType);
    }
}
