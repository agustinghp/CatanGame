package org.skiBums.Structures;

import org.skiBums.Player;

import java.awt.*;

public record Building(Player owner, BuildingType type) {
    public Color getColor() {
        return owner.getColor();
    }
}
