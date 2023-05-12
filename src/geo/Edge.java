package geo;

import java.util.Objects;

public class Edge {
    public final Point p1;
    public final Point p2;

    public Edge(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge)) return false;
        Edge edge = (Edge) o;
        return (p1.equals(edge.p1) && p2.equals(edge.p2)) ||
               (p1.equals(edge.p2) && p2.equals(edge.p1));
    }

    @Override
    public int hashCode() {
        return Objects.hash(p1, p2);
    }

    @Override
    public String toString() {
        return p1 + "--" + p2;
    }

    public Point midPoint() {
        return new Point((p1.x + p2.x)/2, (p1.y + p2.y) /2);
    }

    /**
     * Return the intersection point of two edges, if one exists.
     * @param intersectOnEndPoints if False, two edges that share a vertex
     *                             will not be considered intersecting
     */
    public Point intersect(Edge e, boolean intersectOnEndPoints) {
        double x1 = this.p1.x;
        double y1 = this.p1.y;
        double x2 = this.p2.x;
        double y2 = this.p2.y;

        double x3 = e.p1.x;
        double y3 = e.p1.y;
        double x4 = e.p2.x;
        double y4 = e.p2.y;

        // One could calculate the slopes of the two lines directly but that might
        // lead to division by 0, hence multiplication here
        double den = (x1-x2)*(y3-y4) - (y1-y2)*(x3-x4);
        if (den == 0) {
            return null;
        }

        // The intersection point
        double px = ((x1*y2-y1*x2)*(x3-x4) - (x1-x2)*(x3*y4-y3*x4))/den;
        double py = ((x1*y2-y1*x2)*(y3-y4) - (y1-y2)*(x3*y4-y3*x4))/den;
        Point p = new Point(px, py);

        if ((px < Math.min(x1, x2) || px > Math.max(x1, x2)) ||
                (py < Math.min(y1, y2) || py > Math.max(y1, y2)) ||
                (px < Math.min(x3, x4) || px > Math.max(x3, x4)) ||
                (py < Math.min(y3, y4) || py > Math.max(y3, y4))) {
            return null;
        }

        if (!intersectOnEndPoints && (p.equals(p1) || p.equals(p2) || p.equals(e.p1) || p.equals(e.p2))) {
            return null;
        }
        return p;
    }

    public boolean contains(Point p) {
        double d1 = (p.x - p1.x) * (p2.y - p1.y);
        double d2 = (p2.x - p1.x) * (p.y - p1.y);
        double epsilon = 1e-8; // Introduced to get away from rounding errors
        if (Math.abs(d1 - d2) > epsilon) {
            return false;
        }
        if (p.x < Math.min(p1.x, p2.x) - epsilon || p.x > Math.max(p1.x, p2.x) + epsilon) {
            return false;
        }
        return !(p.y < Math.min(p1.y, p2.y) - epsilon) && !(p.y > Math.max(p1.y, p2.y) + epsilon);
    }
}
