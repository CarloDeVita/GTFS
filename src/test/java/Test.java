import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.operation.distance.DistanceOp;
import com.vividsolutions.jts.operation.linemerge.LineMerger;
import com.vividsolutions.jts.operation.overlay.snap.GeometrySnapper;
import com.vividsolutions.jts.operation.overlay.snap.LineStringSnapper;
import java.io.IOException;
import java.util.Collection;
import java.util.TreeMap;

/**
 *
 * @author cdevi
 */
public class Test {
    public static void main(String arg[]) throws IOException{
        /*JFileChooser ch = new JFileChooser();
        ch.setDialogTitle("Choose GTFS directory");
        //ch.setCurrentDirectory(new File(dirpath));
        ch.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if(ch.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
            File currentDir = ch.getCurrentDirectory();
            //pref.put("gtfs_dir", currentDir.getAbsolutePath());
            File file = ch.getSelectedFile();
            String dir = file.getAbsolutePath();
            Feed feed = new FeedParser().read(dir);
            Route r6 = feed.getRouteByName("R6");
            Trip trip = null;
            for(Trip t : r6.getTrips()){
                trip = t; break;
            }
            Shape shape = trip.getShape();
            Iterator<StopTime> it1 = trip.getStopTimes().iterator();
            System.out.print("[");
            while(it1.hasNext()){
                System.out.print(it1.next().getStop().getCoordinate());
                if(it1.hasNext())
                    System.out.print(", ");
            }
            System.out.println("]");;
            Iterator<Shape.Point> it2 = shape.getPoints().iterator();
            System.out.print("[");
            while(it2.hasNext()){
                System.out.print(it2.next().getCoordinate());
                if(it2.hasNext())
                    System.out.print(", ");
            }
            System.out.println("]");      
        }*/
        TreeMap<Double, Point> distances = new TreeMap<>();
        GeometryFactory g = new GeometryFactory();
        Point a = g.createPoint(new Coordinate(2,1));
        Point b = g.createPoint(new Coordinate(2,5));
        Point c = g.createPoint(new Coordinate(2,10));
        
        Coordinate[] cs = new Coordinate[]{a.getCoordinate(),b.getCoordinate(),c.getCoordinate()};
        LineString l1 = g.createLineString(cs);
        Point p = g.createPoint(new Coordinate(1,7));
        
        double distA = p.distance(a);
        double distB = p.distance(b);
        double distC = p.distance(c);
        distances.put(distA,a);
        distances.put(distB,b);
        distances.put(distC,c);
        GeometrySnapper gs = new GeometrySnapper(l1);
        Geometry gnew = gs.snapTo(p, distances.firstKey()+0.0000001);
        System.out.println("ARR:");
        int i = 0;
        for(Coordinate cr : gnew.getCoordinates()){
            System.out.println("C: "+cr);
        }
    }
}
