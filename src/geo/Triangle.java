package geo;

public class Triangle extends ConvexPolygon {

    public Triangle(Point p1, Point p2, Point p3) throws NotConvexException {
        super(new Point[]{p1, p2, p3});
    }
}
