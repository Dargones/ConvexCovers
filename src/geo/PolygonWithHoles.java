package geo;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.*;

import javax.imageio.ImageIO;

public class PolygonWithHoles {

    protected final Point[] outerBoundary;
    protected final Point[][] holes;

    public PolygonWithHoles(Point[] outerBoundary, Point[][] holes) {
        this.outerBoundary = outerBoundary;
        this.holes = holes;
    }

    public static PolygonWithHoles parseInstanceFile(String instanceFile) throws IOException {
        Path path = FileSystems.getDefault().getPath(instanceFile);
        String jsonString = String.join("\n", Files.readAllLines(path));
        JSONObject json = new JSONObject(jsonString);

        JSONArray jsonOuterBoundary = json.getJSONArray("outer_boundary");
        Point[] outerBoundary = parsePointArray(jsonOuterBoundary);

        JSONArray jsonHoles = json.getJSONArray("holes");
        Point[][] holes = new Point[jsonHoles.length()][];
        for (int i = 0; i < jsonHoles.length(); i++) {
            JSONArray jsonHole = jsonHoles.getJSONArray(i);
            holes[i] = parsePointArray(jsonHole);
        }
        return new PolygonWithHoles(outerBoundary, holes);
    }

    /**
     * Create a nice to look at .png picture
     */
    public static void drawPolygons(List<PolygonWithHoles> polygons, int width, int height, File outputFile, boolean monochrome) {
        Rectangle bounds = null;
        for (PolygonWithHoles polygon : polygons) {
            for (Point point : polygon.outerBoundary) {
                if (bounds == null) {
                    bounds = new Rectangle((int) point.x, (int) point.y, 0, 0);
                }
                bounds.add(point.x, point.y);
            }
            for (Point[] hole : polygon.holes) {
                for (Point point : hole) {
                    bounds.add(point.x, point.y);
                }
            }
        }

        // Determine the scaling factor to fit the polygons on the image
        double scaleX = (double) width / bounds.width;
        double scaleY = (double) height / bounds.height;
        double scale = Math.min(scaleX, scaleY);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.white);
        g.fillRect(0, 0, width, height);

        for (PolygonWithHoles polygon : polygons) {

            // Get a pseudo-random color based on polygon's hashcode
            Color color;
            if (!monochrome) {
                int colorCode = polygon.hashCode();
                int R = (colorCode & 0xFF0000) >> 16;
                int G = (colorCode & 0x00FF00) >> 8;
                int B = colorCode & 0x0000FF;
                color = new Color(R, G, B, 128);
            } else {
                color = new Color(125, 125, 125, 128);
            }

            int[] xPoints = new int[polygon.outerBoundary.length];
            int[] yPoints = new int[polygon.outerBoundary.length];
            for (int i = 0; i < polygon.outerBoundary.length; i++) {
                xPoints[i] = (int) ((polygon.outerBoundary[i].x - bounds.x) * scale);
                yPoints[i] = (int) ((polygon.outerBoundary[i].y - bounds.y) * scale);
            }
            g.setColor(color);
            g.fillPolygon(xPoints, yPoints, xPoints.length);
            g.setColor(Color.BLACK);
            g.drawPolygon(xPoints, yPoints, xPoints.length);

            for (Point[] hole : polygon.holes) {
                xPoints = new int[hole.length];
                yPoints = new int[hole.length];
                for (int i = 0; i < hole.length; i++) {
                    xPoints[i] = (int) ((hole[i].x - bounds.x) * scale);
                    yPoints[i] = (int) ((hole[i].y - bounds.y) * scale);
                }
                g.setColor(Color.white);
                g.fillPolygon(xPoints, yPoints, xPoints.length);
                g.setColor(Color.BLACK);
                g.drawPolygon(xPoints, yPoints, xPoints.length);
            }
        }

        try {
            ImageIO.write(image, "png", outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Point[] parsePointArray(JSONArray jsonArray) {
        List<Point> points = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonPoint = jsonArray.getJSONObject(i);
            double x = jsonPoint.getDouble("x");
            double y = jsonPoint.getDouble("y");
            points.add(new Point(x, y));
        }
        return points.toArray(new Point[0]);
    }

    protected List<Edge> getAllEdges() {
        List<Edge> result = new ArrayList<>();
        for (int i = 0; i < outerBoundary.length; i++) {
            result.add(new Edge(outerBoundary[i], outerBoundary[(i+1)%outerBoundary.length]));
        }
        for (Point[] hole: holes) {
            for (int i = 0; i < hole.length; i++) {
                result.add(new Edge(hole[i], hole[(i+1)%hole.length]));
            }
        }
        return result;
    }

    /**
     * Convert a polygon with holes to a degenerate simple polygon (that can have duplicate vertices).
     * For each hole, the method looks for a vertex visible from the boundary of the hole that the hole can be
     * connected to.
     */
    public SimplePolygon toSimple() {

        List<Point> points = new ArrayList<>(Arrays.asList(outerBoundary));
        List<Edge> edges = getAllEdges();
        List<Point[]> holes = new ArrayList<>(Arrays.asList(this.holes));


        while (holes.size() != 0) {
            int holeId = 0;
            while (holeId < holes.size()) {
                Point[] hole = holes.get(holeId);
                boolean foundEdge = false;
                Edge attempt = null;
                int connectTo = -1;
                for (int i = 0; i < hole.length; i++) {
                    for (int j = 0; j < points.size(); j++) {
                        attempt = new Edge(hole[i], points.get(j));
                        boolean intersects = false;
                        for (Edge edge : edges) {
                            // can intersect at endpoints though
                            if (attempt.intersect(edge, false) != null) {
                                intersects = true;
                                break;
                            }
                        }
                        Point prev = points.get((j - 1 + points.size()) % points.size());
                        Point next = points.get((j + 1 + points.size()) % points.size());

                        // the second condition is necessary if the vertex has already been used to connect a hole
                        if ((!intersects) && (computeAngle(prev, points.get(j), next) < computeAngle(prev, points.get(j), hole[i]))){
                            foundEdge = true;
                            connectTo = j;
                            break;
                        }
                    }
                    if (foundEdge) {
                        for (int k = 0; k < hole.length + 1; k++) {
                            points.add(connectTo + 1 + k, hole[(i + k) % hole.length]);
                        }
                        points.add(connectTo + 1 + hole.length + 1, points.get(connectTo));
                        edges.add(attempt);
                        break;
                    }
                }
                if (foundEdge) {
                    holes.remove(holeId);
                } else {
                    holeId++;
                }
            }
        }

        Point[] newOuterBoundary = new Point[points.size()];
        for (int i = 0; i < points.size(); i++) {
            newOuterBoundary[i] = points.get(i);
        }
        return new SimplePolygon(newOuterBoundary);
    }

    public static double computeAngle(Point p1, Point p2, Point p3) {
        double dx1 = p1.x - p2.x;
        double dy1 = p1.y - p2.y;
        double dx2 = p3.x - p2.x;
        double dy2 = p3.y - p2.y;
        double crossProduct = dx1 * dy2 - dx2 * dy1;
        double dotProduct = dx1 * dx2 + dy1 * dy2;
        double angle = Math.atan2(crossProduct, dotProduct);
        if (angle < 0) {
            angle += 2 * Math.PI;
        }
        return angle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PolygonWithHoles)) return false;
        PolygonWithHoles polygon = (PolygonWithHoles) o;
        return Arrays.equals(outerBoundary, polygon.outerBoundary) && Arrays.equals(holes, polygon.holes);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(outerBoundary);
        result = 31 * result + Arrays.deepHashCode(holes);
        return result;
    }
}
