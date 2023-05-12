package geo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Main {

    public static void main(String[] args) {
        CLO clo = new CLO(args);
        try {
            PolygonWithHoles polygon = PolygonWithHoles.parseInstanceFile(clo.file);
            PolygonWithHoles.drawPolygons(Collections.singletonList(polygon), clo.width, clo.height, new File(clo.file + "_0_original.png"), true);
            System.out.println("Saved the image of the original polygon to " + clo.file + "_0_original.png");
            System.out.println("Reducing to a degenerate simple polygon...");
            SimplePolygon simplePolygon = polygon.toSimple();
            PolygonWithHoles.drawPolygons(Collections.singletonList(simplePolygon), clo.width, clo.height, new File(clo.file + "_1_simple.png"), true);
            System.out.println("Saved the image of the simple polygon obtained from original to " + clo.file + "_1_simple.png");
            System.out.println("Triangulating...");
            List<ConvexPolygon> triangles = new ArrayList<>(simplePolygon.triangulate(1590000));
            PolygonWithHoles.drawPolygons(new ArrayList<>(triangles), clo.width, clo.height, new File(clo.file + "_2_triangulated.png"), false);
            System.out.println("Saved the image of the triangulated polygon to " + clo.file + "_2_triangulated.png");
            System.out.println("Reducing cover size...");
            reduceCoverSize(triangles, polygon, clo);
            if (!clo.gif) {
                PolygonWithHoles.drawPolygons(new ArrayList<>(triangles), clo.width, clo.height, new File(clo.file + "_3_cover_with_"+triangles.size()+"_pieces.png"), false);
                System.out.println("Saved the image of the final cover to " + clo.file + "_3_cover_with_"+triangles.size()+"_pieces.png");
            }
        } catch (IOException e) {
            System.err.println("Could not find the specified file " + clo.file);
        } catch (ConvexPolygon.NotConvexException e) {
            System.err.println("Something went wrong and program created a non-convex triangle");
        }
    }

    /**
     * Reduces the size of the convex cover (initially a triangulation) by randomly merging existing pieces.
     * If pairs of randomly chose pieces cannot be merged @param limit times in a row, stop the procedure.
     */
    public static void reduceCoverSize(List<ConvexPolygon> cover, PolygonWithHoles toCover, CLO clo) throws ConvexPolygon.NotConvexException {
        Random random = new Random();
        int step = 3;
        random.setSeed(clo.seed);
        int triesLeft = clo.searchLimit;
        while (triesLeft > 0 && cover.size() > 1) {
            triesLeft--;
            ConvexPolygon first = null, second = null;
            while (first == second) {
                first = cover.get(random.nextInt(cover.size()));
                second = cover.get(random.nextInt(cover.size() - 1));
            }
            ConvexPolygon union = first.mergeWith(second);
            if (union.isContainedWithin(toCover)) {
                cover.remove(first);
                cover.remove(second);
                cover.add(union);
                triesLeft = clo.searchLimit;
                if (clo.gif) {
                    System.out.println("Saving the image of the cover with " + cover.size() + " pieces to " + clo.file + "_" + step + "_cover_with_" + cover.size() + "_pieces.png");
                    PolygonWithHoles.drawPolygons(new ArrayList<>(cover), clo.width, clo.height, new File(clo.file + "_" + (step++) + "_cover_with_" + cover.size() + "_pieces.png"), false);
                }
            }
        }
    }
}