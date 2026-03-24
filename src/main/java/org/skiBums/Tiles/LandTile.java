package org.skiBums.Tiles;

import org.skiBums.ResourceType;

import static org.skiBums.ResourceType.DESERT;

public class LandTile extends Tile{
    private final ResourceType resourceType;

    public LandTile(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public boolean isDesert() {
        return resourceType == DESERT;
    }

    public boolean isLand() {
        return true;
    }

}
