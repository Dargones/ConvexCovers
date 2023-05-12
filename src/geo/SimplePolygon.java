package geo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimplePolygon extends PolygonWithHoles {

    public SimplePolygon(Point[] outerBoundary) {
        super(outerBoundary, new Point[0][]);
    }

    /**
     * Ear-clipping triangulation
     */
    public List<Triangle> triangulate(int limit) throws ConvexPolygon.NotConvexException {
        List<Triangle> triangles = new ArrayList<>();
        List<Edge> edges = getAllEdges();
        List<Point> points = new ArrayList<>();
        points.addAll(Arrays.asList(outerBoundary));
        while (points.size() > 3) {
            int i = 0;
            while (i < points.size() && points.size() > 3) {
                Point prev = points.get((i - 1 + points.size()) % points.size());
                Point next = points.get((i + 1) % points.size());
                if (new Edge(prev, next).contains(points.get(i))) {
                    points.remove(i);
                    continue;
                }
                if (isEar(i, points, edges)) {
                    // the check below is necessary due to degeneracies introduced by conversion to a simple polygon
                    if (ConvexPolygon.isConvex(new Point[]{prev, points.get(i), next})) {
                        triangles.add(new Triangle(prev, points.get(i), next));
                    }
                    points.remove(i);
                    limit--;
                    if (limit <= 0) {
                        return triangles;
                    }
                } else {
                    i++;
                }
            }
        }
        // the check below is necessary due to degeneracies introduced by conversion to a simple polygon
        if (ConvexPolygon.isConvex(new Point[]{points.get(0), points.get(1), points.get(2)})) {
            triangles.add(new Triangle(points.get(0), points.get(1), points.get(2)));
        }
        return triangles;
    }

    /**
     * Determine if a given triangle is an ear of the polygon
     */
    private static boolean isEar(int i, List<Point> points, List<Edge> edges) {
        Point prev = points.get((i - 1 + points.size()) % points.size());
        Point next = points.get((i + 1) % points.size());
        Point mid = points.get(i);
        if (crossProduct(prev, mid, next) <= 0) {
            return false;
        }
        int j = (i + 2) % points.size() ;
        while (j != (i - 1 + points.size()) % points.size()) {
            if (isInsideTriangle(prev, mid, next, points.get(j))) {
                return false;
            }
            j = (j + 1) % points.size();
        }
        Edge[] triangle = new Edge[] {new Edge(prev, mid), new Edge(mid, next), new Edge(next, prev)};
        for (Edge edge: edges) {
            for (Edge triangleEdge: triangle) {
                if (edge.intersect(triangleEdge, false) != null) {
                    return false;
                }
            }
        }
        return true;
    }

    private static double crossProduct(Point a, Point b, Point c) {
        return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
    }

    private static boolean isInsideTriangle(Point a, Point b, Point c, Point p) {
        double s1 = crossProduct(a, b, p);
        double s2 = crossProduct(b, c, p);
        double s3 = crossProduct(c, a, p);
        return s1 > 0 && s2 > 0 && s3 > 0 || s1 < 0 && s2 < 0 && s3 < 0;
    }
}
