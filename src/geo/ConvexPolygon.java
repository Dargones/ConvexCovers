package geo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class ConvexPolygon extends SimplePolygon {

    public ConvexPolygon(Point[] outerBoundary) throws NotConvexException {
        super(outerBoundary);
        if (!isConvex(outerBoundary)) {
            throw new NotConvexException();
        }
    }

    public static boolean isConvex(Point[] points) {
        int n = points.length;

        if (n < 3) {
            return false;
        }

        boolean collinear = true;
        for (int i = 2; i < n; i++) {
            double area = (points[i].x - points[0].x) * (points[1].y - points[0].y) -
                          (points[i].y - points[0].y) * (points[1].x - points[0].x);
            if (area != 0) {
                collinear = false;
                break;
            }
        }
        if (collinear) {
            return false;
        }

        for (int i = 0; i < n; i++) {
            double dx1 = points[(i+1)%n].x - points[i].x;
            double dy1 = points[(i+1)%n].y - points[i].y;
            double dx2 = points[(i+2)%n].x - points[(i+1)%n].x;
            double dy2 = points[(i+2)%n].y - points[(i+1)%n].y;
            double cross = dx1 * dy2 - dx2 * dy1;
            if (cross < 0) {
                return false;
            }
        }
        return true;
    }

    static class NotConvexException extends Exception {
        public NotConvexException() {
            super("The polygon is not convex.");
        }
    }

    protected boolean isContainedWithin(SimplePolygon other) {
        for (Point point : outerBoundary) {
            if (!point.isInside(other) && !point.isOnBoundary(other)) {
                return false;
            }
        }
        for (int i = 0; i < other.outerBoundary.length; i++) {
            Edge edge = new Edge(other.outerBoundary[i], other.outerBoundary[(i+1)% other.outerBoundary.length]);
            if (other.outerBoundary[i].isInside(this) || (edge.midPoint().isInside(this))) {
                return false;
            }
        }
        return true;
    }

    public boolean isContainedWithin(PolygonWithHoles polygon) {
        if (!isContainedWithin(new SimplePolygon(polygon.outerBoundary))) {
            return false;
        }
        for (Point[] hole : polygon.holes) {
            for (int i = 0; i < hole.length; i++) {
                Point next = hole[(i+1)%hole.length];
                if (hole[i].isInside(this) || new Edge(hole[i], next).midPoint().isInside(this)) {
                    return false;
                }
            }
        }
        return true;
    }

    public ConvexPolygon mergeWith(ConvexPolygon polygon) throws NotConvexException {
        HashSet<Point> combinedVertices = new HashSet<>();
        combinedVertices.addAll(Arrays.asList(this.outerBoundary));
        combinedVertices.addAll(Arrays.asList(polygon.outerBoundary));
        return new ConvexPolygon(convexHull(combinedVertices.toArray(new Point[0])));
    }

    private enum Orientation {
        Clockwise, Counterclockwise, Collinear;
    }

    private static Orientation orientation(Point p, Point q, Point r) {
        double val = (q.y - p.y) * (r.x - q.x) -
                     (q.x - p.x) * (r.y - q.y);
        if (val == 0) return Orientation.Collinear;
        return (val > 0)? Orientation.Clockwise: Orientation.Counterclockwise;
    }

    /**
     * This method has been copied, with edits, from
     * https://www.geeksforgeeks.org/convex-hull-using-graham-scan/
     */
     private static Point[] convexHull(Point points[]) {
        int n = points.length;
        if (n < 3) return null;
        List<Point> hull = new ArrayList<>();

        // Find the leftmost point
        int l = 0;
        for (int i = 1; i < n; i++)
            if (points[i].x < points[l].x) {
                l = i;
            }

        int p = l, q;
        do {
            hull.add(points[p]);

            q = (p + 1) % n;
            for (int i = 0; i < n; i++) {
                Orientation o = orientation(points[p], points[i], points[q]);
                if ((o == Orientation.Counterclockwise)) { //|| (o == Orientation.Collinear &&
                   q = i;
                }
            }

            p = q;

        } while (p != l);

        return hull.toArray(new Point[0]);
    }

}
