package org.skiBums;

import org.skiBums.geometry.HexCoord;

import java.util.List;

public record Vertex(int id, List<HexCoord> corners) {
}
