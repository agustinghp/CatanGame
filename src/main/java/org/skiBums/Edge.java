package org.skiBums;

/**
 * One side between two {@link Vertex} intersections (where roads are placed).
 * Endpoints are referenced by the same ids used in {@link Vertex}.
 */
public record Edge(int id, int vertexIdA, int vertexIdB) {
    public Edge {
        if (vertexIdA == vertexIdB) {
            throw new IllegalArgumentException("An edge must connect two distinct vertices");
        }
    }
}
