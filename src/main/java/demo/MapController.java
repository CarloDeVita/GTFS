package demo;


import com.vividsolutions.jts.operation.distance.DistanceOp;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.operation.overlay.snap.GeometrySnapper;
import demo.maputils.IconMarker;
import demo.maputils.MapLine;
import gtfs.Feed;
import gtfs.FeedParser;
import gtfs.entities.Calendar;
import gtfs.entities.Route;
import gtfs.entities.Shape;
import gtfs.entities.Stop;
import gtfs.entities.StopTime;
import gtfs.entities.Trip;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.SwingWorker;
import net.sf.dynamicreports.report.exception.DRException;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.DefaultMapController;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;

/**
 *
 * @author cdevi
 */
public class MapController {
    Feed feed;
    Mappa m;
    JMapViewer map;
    /*Control*/
    HashMap<Point,Statistic> stats;
    DefaultMapController contPos;
    /**
     * controller starts
     */
    private void start() throws IOException {
        map = new JMapViewer();
        m = new Mappa(map,this);
        Image im = ImageIO.read(getClass().getClassLoader().getResourceAsStream("bus.png"));
        m.setIconImage(im);

        m.setVisible(true);
        stats = new HashMap<>();
        contPos = new DefaultMapController(map){
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                   findSegment(e.getPoint(),m.getSelectedRoute());
                } catch (DRException ex) {
                    Logger.getLogger(Mappa.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        
    }
    
    /**
     * choose GTFS to analyze
     */
    public void chooseGtfs(){
        Preferences pref = Preferences.userNodeForPackage(Mappa.class);
        String dirpath = pref.get("gtfs_dir", System.getProperty("user.home"));
        JFileChooser ch = new JFileChooser();
        ch.setDialogTitle("Choose GTFS directory");
        ch.setCurrentDirectory(new File(dirpath));
        ch.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if(ch.showOpenDialog(m) == JFileChooser.APPROVE_OPTION){
            File currentDir = ch.getCurrentDirectory();
            pref.put("gtfs_dir", currentDir.getAbsolutePath());
            File file = ch.getSelectedFile();
            String dir = file.getAbsolutePath();
            try {
                setGTFS(dir);
               
            } catch (IOException ex) {
                Logger.getLogger(MapController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
     /**
     * 
     * @param dir absolute path of GTFS directory.
     * @throws java.io.IOException
     */
    public void setGTFS(String dir) throws IOException{
        m.disableInput();
        Cursor c = new Cursor(Cursor.WAIT_CURSOR);
        m.setCursor(c);
        SwingWorker<Void,Void> sw = new SwingWorker<Void,Void>(){
            @Override
            protected Void doInBackground() throws Exception {
                try{
                    long start = System.currentTimeMillis();
                    feed = new FeedParser().read(dir);
                    long end = System.currentTimeMillis();
                    System.out.println("Ci ho messo: "+(end-start)/1000+" sec");
                }catch (IOException ex) {
                    Logger.getLogger(Mappa.class.getName()).log(Level.SEVERE, null, ex);
                }
                Collection<Route> routes = feed.getRoutes(3);
                setRouteList(routes);
                
                return null;
            }
            
            @Override
            protected void done(){
                m.enableInput();
                m.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        };
        sw.execute();
    }
    
    /**
     * set names of routes in combobox 
     * @param routes collection of routs
     */
    private void setRouteList(Collection<Route> routes){
        String[] arr = new String[routes.size()];
        int i=0;
        for(Route r : routes)
            arr[i++] = r.getName();
        
        m.setRouteList(arr);
    }
    
    /**
     * draw lines on the map
     * @param name name of route
     */
    public void showRoute(String name){
        HashSet<String> idShapes = new HashSet<>();
        Coordinate c=null;
        HashSet<Statistic> lines = new HashSet<>();
        try {
            Image im = ImageIO.read(getClass().getClassLoader().getResourceAsStream("station.png"));
            Route r = feed.getRouteByName(name);
            HashSet<Stop> stops = new HashSet<>();
            for(Trip t : r.getTrips()){
                Shape s = t.getShape();
                if(!idShapes.add(s.getId())) continue;
                List<Coordinate> shapel = new LinkedList<>();
                for(Shape.Point p : s.getPoints()){
                    c = new Coordinate( p.getLat(),p.getLon());
                    MapMarkerDot mmd = new MapMarkerDot(c);
                    map.addMapMarker(mmd);
                    stats.put(p.getCoordinate(),new Statistic(p.getCoordinate(),mmd));
                    shapel.add(c);                       
                }
                MapLine ml = new MapLine(shapel);
                map.addMapPolygon(ml);
                map.setDisplayPosition(c, 13);
                for(StopTime stopT : t.getStopTimes()){
                    stops.add(stopT.getStop());
                }
                
            }
            for(Stop stop : stops){
                map.addMapMarker(new IconMarker(new Coordinate(stop.getLat(),stop.getLon()), im));
            }
        }
       
        catch(IOException ex){
                System.err.println(ex.getMessage());
        }
    }


    /*TODO: Togliere name, per ora è per un controllo*/ 
    public void findSegment(java.awt.Point point,String name) throws DRException {
        GeometryFactory g;
        PrecisionModel precision = new PrecisionModel(PrecisionModel.FLOATING);
        int srid = 4326; //WGS 84
        g = new GeometryFactory(precision, srid);
        ICoordinate c = map.getPosition(point);
        Coordinate coord; 
        Point pnt = g.createPoint(new com.vividsolutions.jts.geom.Coordinate(c.getLon(),c.getLat()));
        m.disableInput();
        m.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        SwingWorker<Void,Void> sw = new SwingWorker<Void,Void>(){
            @Override
            protected Void doInBackground() throws Exception {

                HashSet<String> idShapes = new HashSet<>();
                Route r = feed.getRouteByName(name);
                com.vividsolutions.jts.geom.Coordinate[] coordinates;
                MultiPoint path;
                double dist,distMin;
                distMin = Double.MAX_VALUE;
                Point min = null;
                Coordinate coord;
                for(Trip t : r.getTrips()){
                    Shape s = t.getShape();
                    if(!idShapes.add(s.getId())) continue;
                    coordinates = new com.vividsolutions.jts.geom.Coordinate[t.getStopTimes().size()];
                    for(Shape.Point sp : s.getPoints()){
                        Point p = sp.getCoordinate();
                        dist = p.distance(pnt);
                        if(dist < distMin){
                            distMin = dist;
                            min = p;
                        }
                    }
                }
                coord = new Coordinate(min.getY(),min.getX());
                MapMarkerDot mmd = new MapMarkerDot(coord);
                mmd.setColor(Color.MAGENTA);
                map.addMapMarker(mmd);
                //filterByTime(min);
                Report report = new Report();
                Statistic stat = stats.get(min);
                report.showGraphic(stat);
                //report.showGraphic(r,timeF,s.getPullman());*/
                return null;
            }
            
            @Override
            protected void done(){
                m.enableInput();
                m.setCursor(Cursor.getDefaultCursor());
            }
                
        };
        sw.execute();
        
    }
    
     private void filterByTime(Point p) throws DRException {

        Comparator<String> comp = StopTime.TIME_COMPARATOR;
        String timeFrom = m.getTime(0);
        String timeTo = m.getTime(1);
        int timeF = Integer.parseInt(timeFrom.substring(0, timeFrom.indexOf(":")));
        int timeT = Integer.parseInt(timeTo.substring(0, timeTo.indexOf(":")));
        Statistic s = stats.get(p);
        s.setInterval(timeF, timeT);
        int[] arr = s.getFreqs();
        int k=0;
        int[] r = new int[timeT-timeF+1];
        for(int i = 0;i<arr.length;i++){
                if(i>=timeF && i<timeT)
                    r[k++] = arr[i];
        }
        
     }
    
    /*Nome route per controlli, Per ora sono punti per verif correttezza*/
    
    public void statistic(String name){
        m.disableInput();
        m.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingWorker<Void,Void> sw = new SwingWorker<Void,Void>(){
            @Override
            protected Void doInBackground() throws Exception {
                //if(stats == null) stats = new HashMap<>();
                Collection<Trip> trips = feed.getRouteByName(name).getTrips();
                HashSet<Shape> shapes = new HashSet<>();
                for(Trip t : trips){
                    if(!shapes.add(t.getShape())) continue;
                        statisticSegment(t.getShape().getPoints(),trips);
                }
                System.out.println("Dimensione struttura: "+stats.size());
                return null;
            }
            
            @Override
            protected void done(){
                m.enableInput();
                m.setCursor(Cursor.getDefaultCursor());
            }
        };
        sw.execute();
    }
    
    
    
    public void statisticSegment(Collection<Shape.Point> segments,Collection<Trip> trips) throws DRException{
        
        for(Shape.Point ls : segments){
            Point p = ls.getCoordinate();
            Statistic s = calculateFrequency(trips,p);
            filterByTime(p);
            s.colorSegment();
        }
    }
    
    
    
    /* Per ora considero il punto e non un segmento per testare la correttezza.*/    
    public Statistic calculateFrequency(Collection<Trip> trips, Point line){
        Point stop;
        StopTime correct;
        String timeArrival,time;
        TreeMap<Double,StopTime> distances = new TreeMap<>();
        int[] freqs = new int[25];
        LocalDate from = m.getDate(0);
        LocalDate to = m.getDate(1);
        Statistic stat;
        LocalDate localDate = from.plusDays(0);
        DayOfWeek day;
        stat = stats.get(line);
        do{
            for(Trip t : trips){
                stat.addPullman(t.getRoute().getName());
                Calendar cal = t.getCalendar();
                /*C'è per ora per fare controlli su NA*/
                //if( (cal.getStartDate().isAfter(localDate) || (cal.getEndDate().isBefore(localDate)))) continue;
                Map<DayOfWeek,Boolean> days = cal.getServiceDays();
                Map<LocalDate,Boolean> exc = cal.getExceptions();
                day = localDate.getDayOfWeek();
                Boolean activeE,activeD;
                /*Controllo se quel giorno è attivo*/
                if( (activeE = exc.get(localDate) == null && ( (activeD=days.get(day))==null || activeD!=true)) || activeE == true ) 
                    continue;
                
                for(StopTime s : t.getStopTimes()){
                    stop = s.getStop().getCoordinate();
                    DistanceOp d = new DistanceOp(line,stop);
                    distances.put(d.distance(),s);     
                    //map.addMapMarker(new MapMarkerDot(new Coordinate(stop.getY(),stop.getX())));
                }
                if(distances.isEmpty()) continue;
                correct = distances.get(distances.firstKey());
                timeArrival = correct.getArrival();
                time = timeArrival.substring(0,timeArrival.indexOf(":"));
                freqs[Integer.parseInt(time)%25]++;
                distances.clear();
            }
            localDate = localDate.plusDays(1);
        }while(!localDate.isAfter(to));
        stat.setFreqs(freqs);
        return stat;
    }

    void clearStatistic() {
        if(stats!=null && !stats.isEmpty()) stats.clear(); 
   }
   
   
    public static void main(String arg[]){
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Mappa.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        MapController controller = new MapController();
        try {
            controller.start();
        } catch (IOException ex) {
            Logger.getLogger(MapController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

 
    
    
    
    
    
    
    
        
   
    //TODO-FINIRE.
    public void snap(String name) {
        GeometryFactory g = new GeometryFactory();
        Route r = feed.getRouteByName(name);
        HashSet<String> idShapes = new HashSet<>();
        com.vividsolutions.jts.geom.Coordinate[] coordinates;
        double tolerance;
        GeometrySnapper gs;
        MultiPoint path;
        for( Trip t : r.getTrips()){
            Shape s = t.getShape();
            if(!idShapes.add(s.getId())) continue;
            coordinates = new com.vividsolutions.jts.geom.Coordinate[s.getPoints().size()];
            int i = 0;
            for(Shape.Point p: s.getPoints()){
                Point jp = p.getCoordinate();
                coordinates[i++]=jp.getCoordinate();
            }
            path = g.createMultiPoint(coordinates);
            for(StopTime st : t.getStopTimes()){
                Point p = st.getStop().getCoordinate();
                tolerance = DistanceOp.distance(path, p);
                gs = new GeometrySnapper(path); 
                path = (MultiPoint)gs.snapTo(p,tolerance+0.000000001 );
            } 
            showSnappedRoute(path);
            break;
       }
    }
    /**
     * draw lines on the map
     * @param path multipoint of all the path
     */
    public void showSnappedRoute(MultiPoint path){
        map.removeAllMapPolygons();
        HashSet<String> idShapes = new HashSet<>();
        try {
            Image im = ImageIO.read(getClass().getClassLoader().getResourceAsStream("station.png"));
            List<Coordinate> shapel = new LinkedList<>();
            for( com.vividsolutions.jts.geom.Coordinate c : path.getCoordinates()){
                shapel.add(new Coordinate( c.y,c.x));
            }        
            map.addMapPolygon(new MapLine(shapel));
            for(Coordinate c : shapel){
                map.addMapMarker(new MapMarkerDot(c));

            }
        }
        catch(IOException ex){
                System.err.println(ex.getMessage());
        }
    }
}
