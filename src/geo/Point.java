package geo;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Objects;

public class Point {

    public final double x;
    public final double y;

    public Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Point)) return false;
        Point point = (Point) o;
        return point.x == x && point.y == y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }

    public double distance(Point p) {
        return Math.sqrt((p.x - x) * (p.x - x) +
                         (p.y - y) * (p.y - y));
    }

    /**
     * Figure out if the point is inside a simple polygon by counting intersections between an infinite ray from the
     * point and the boundary of the polygon
     */
    public boolean isInside(SimplePolygon polygon) {
        if (isOnBoundary(polygon)) {
            return false;
        }

        int numIntersections = 0;
        double xmax = Arrays.stream(polygon.outerBoundary).max(Comparator.comparingDouble(p -> p.x)).get().x;
        Edge ray = new Edge(this, new Point(xmax+1, this.y));

        HashSet<Integer> verticesConsidered = new HashSet<>(); // vertices that directly intersect the ray

        for (int i = 0; i < polygon.outerBoundary.length; i++) {
            Edge edge = new Edge(polygon.outerBoundary[i], polygon.outerBoundary[(i + 1) % polygon.outerBoundary.length]);

            Point intersectionPoint = edge.intersect(ray, false);
            if (intersectionPoint != null) {
                numIntersections++;
            }

            // the ray may be parallel to some edges of the polygon. The vertices at the ednpoints of such
            // parallel edges must effectively be considered to be one vertex
            if (ray.contains(polygon.outerBoundary[i]) && !verticesConsidered.contains(i)) {
                int nexti = i + 1;
                int previ = i - 1;
                verticesConsidered.add(i);
                Point prev = polygon.outerBoundary[(previ + polygon.outerBoundary.length) % polygon.outerBoundary.length];
                Point next = polygon.outerBoundary[(nexti) % polygon.outerBoundary.length];
                while (polygon.outerBoundary[i].y == next.y) {
                    verticesConsidered.add((nexti) % polygon.outerBoundary.length);
                    nexti++;
                    next = polygon.outerBoundary[(nexti) % polygon.outerBoundary.length];
                }
                while (polygon.outerBoundary[i].y == prev.y) {
                    verticesConsidered.add((previ + polygon.outerBoundary.length) % polygon.outerBoundary.length);
                    previ--;
                    prev = polygon.outerBoundary[(previ + polygon.outerBoundary.length) % polygon.outerBoundary.length];
                }
                if ((polygon.outerBoundary[i].y - next.y) * (polygon.outerBoundary[i].y- prev.y) < 0) {
                    numIntersections++;
                }
            }
        }

        return numIntersections % 2 == 1;
    }


    public boolean isOnBoundary(SimplePolygon polygon) {
        for (int i = 0; i < polygon.outerBoundary.length; i++) {
            Edge edge = new Edge(polygon.outerBoundary[i], polygon.outerBoundary[(i + 1) % polygon.outerBoundary.length]);
            if (polygon.outerBoundary[i].equals(this) || edge.contains(this)) {
                return true;
            }
        }
        return false;
    }
}
