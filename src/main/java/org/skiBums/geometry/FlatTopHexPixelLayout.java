package org.skiBums.geometry;

import org.skiBums.Board;
import org.skiBums.Vertex;

public class FlatTopHexPixelLayout {
    double size; //Distance from hex center to any corner
    // Origin are center for the whole board
    double originX;
    double originY;

    public FlatTopHexPixelLayout(double size, double originX, double originY) {
        this.size = size;
        this.originX = originX;
        this.originY = originY;
    }

    public double[] hexCenterToPixel(HexCoord hex) {
        return new double[] {
                size * 1.5 * hex.q() + originX,
                size * Math.sqrt(3.0) * (hex.r() + hex.q()/2.0) + originY
        };
    }


    public double[] hexCornerToPixel(HexCoord hex, int visualCornerIndex) {
        double[] pixelCenter = hexCenterToPixel(hex);

        double angle = visualCornerIndex * Math.PI / 3.0;
        double px = pixelCenter[0] + size * Math.cos(angle);
        double py = pixelCenter[1] - size * Math.sin(angle);
        return new double[] {px, py};
    }


    public double[] hexCornerToPixel(Board.HexCornerPlacement hexCornerPlacement) {
        return hexCornerToPixel(hexCornerPlacement.hex(), hexCornerPlacement.cornerIndex());
    }

    private static final double PIXEL_EPS = 1e-4;


    public double[] vertexIntersectionToPixel(Vertex v) {
        var corners = v.corners();
        if (corners.size() != 3) {
            throw new IllegalArgumentException("Expected three hexes at a vertex, got " + corners.size());
        }
        HexCoord h0 = corners.get(0);
        HexCoord h1 = corners.get(1);
        HexCoord h2 = corners.get(2);

        for (int c0 = 0; c0 < 6; c0++) {
            for (int c1 = 0; c1 < 6; c1++) {
                for (int c2 = 0; c2 < 6; c2++) {
                    double[] p0 = hexCornerToPixel(h0, c0);
                    double[] p1 = hexCornerToPixel(h1, c1);
                    double[] p2 = hexCornerToPixel(h2, c2);
                    if (samePixel(p0, p1) && samePixel(p0, p2)) {
                        return new double[] {p0[0], p0[1]};
                    }
                }
            }
        }
        throw new IllegalStateException("Could not align visual corners for vertex id=" + v.id());
    }

    private static boolean samePixel(double[] a, double[] b) {
        return Math.abs(a[0] - b[0]) < PIXEL_EPS && Math.abs(a[1] - b[1]) < PIXEL_EPS;
    }

}
