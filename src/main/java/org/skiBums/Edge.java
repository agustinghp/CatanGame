package org.skiBums;

public record Edge(int id, int vertexIdA, int vertexIdB) {
    public Edge {
        if (vertexIdA == vertexIdB) {
            throw new IllegalArgumentException("An edge must connect two distinct vertices");
        }
    }
}
