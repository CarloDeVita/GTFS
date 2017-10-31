package demo;

import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.operation.distance.DistanceOp;
import demo.maputils.IconMarker;
import demo.maputils.MapLine;
import gtfs.Feed;
import gtfs.FeedParser;
import gtfs.entities.Route;
import gtfs.entities.Shape;
import gtfs.entities.Stop;
import gtfs.entities.StopTime;
import gtfs.entities.Trip;
import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import net.sf.dynamicreports.jasper.builder.JasperReportBuilder;
import static net.sf.dynamicreports.report.builder.DynamicReports.cht;
import static net.sf.dynamicreports.report.builder.DynamicReports.cmp;
import static net.sf.dynamicreports.report.builder.DynamicReports.col;
import static net.sf.dynamicreports.report.builder.DynamicReports.report;
import static net.sf.dynamicreports.report.builder.DynamicReports.stl;
import static net.sf.dynamicreports.report.builder.DynamicReports.type;
import net.sf.dynamicreports.report.builder.chart.AxisFormatBuilder;
import net.sf.dynamicreports.report.builder.chart.XyAreaChartBuilder;
import net.sf.dynamicreports.report.builder.chart.XyLineChartBuilder;
import net.sf.dynamicreports.report.builder.column.TextColumnBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;
import net.sf.dynamicreports.report.exception.DRException;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;

/**
 *
 * @author cdevi
 */
public class MapController {
    Feed feed;
    Mappa m;
    JMapViewer map;
    
    /**
     * controller starts
     */
    private void start() {
        map = new JMapViewer();
        m = new Mappa(map,this); 
        m.setVisible(true);
        
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
     */
    public void setGTFS(String dir) throws IOException{
        final JDialog dialog = new JDialog(m, "Wait"); 
        dialog.setLocation(m.getWidth()/2, m.getHeight()/2- dialog.getHeight());
        dialog.setUndecorated(false); 
        
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        bar.setStringPainted(true);
        bar.setString("Please wait...");
        bar.setVisible(true);
        dialog.add(bar);
        
        dialog.setSize(300,200);
        dialog.setVisible(true);
        
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
                dialog.dispose();
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
        arr[0] = "";
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
                    shapel.add(new Coordinate(p.getLat(),p.getLon()));                       
                }
                
                map.addMapPolygon(new MapLine(shapel));
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
    
    public int[] calculateFrequency(Collection<Trip> trips, LineString line){
       
        Point stop;
        StopTime correct;
        String timeArrival,time;
        TreeMap<Double,StopTime> distances = new TreeMap<>();
        int[] freqs = new int[24];
        
        
        for(Trip t : trips){
            
            for(StopTime s : t.getStopTimes()){
                stop = s.getStop().getCoordinate();
                DistanceOp d = new DistanceOp(line,stop);
                System.out.println("distanza= "+d.distance());
                distances.put(d.distance(),s);     
                //map.addMapMarker(new MapMarkerDot(new Coordinate(stop.getY(),stop.getX())));
            }
            correct = distances.get(distances.firstKey());
            timeArrival = correct.getArrival();
            time = timeArrival.substring(0,timeArrival.indexOf(":"));
            freqs[Integer.parseInt(time)%24]++;
            distances.clear();
        }
        return freqs;
    }
    
    public void testDistance(String nameRoute){
        com.vividsolutions.jts.geom.Coordinate[] cs = new com.vividsolutions.jts.geom.Coordinate[5];
        for(int i = 40;i<45;i++){
            cs[i-40] = new com.vividsolutions.jts.geom.Coordinate(i,5);
        }
        CoordinateSequence c = new CoordinateArraySequence(cs);
        GeometryFactory gf = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),4326);
        LineString l = new LineString(c, gf);
        
        Route r = feed.getRouteByName(nameRoute);
        int arr[] = calculateFrequency(r.getTrips(),l);
        
        for(int i=0;i<arr.length;i++)
            System.out.println("Array["+i+"]: "+arr[i] );
            
        try {
            showGraphic(arr);
        } catch (DRException ex) {
            Logger.getLogger(MapController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    
    void snap(String name) {
       Route r = feed.getRouteByName(name);
       HashSet<String> idShapes = new HashSet<>();
       for( Trip t : r.getTrips()){
           for(Shape s : t.getShape()){
               if(!idShapes.add(s.getId())) continue;
           }
       }
    }
    
    
    /*Classe fascia*/
    public static class Fascia{
        private int hour;
        private int freq;

        public Fascia(int hour, int freq) {
            this.hour = hour;
            this.freq = freq;
        }
        
        public int getHour() {
            return hour;
        }

        public int getFreq() {
            return freq;
        }
    }
    
    /*Deve esserci una collection di classe FASCIA.*/
    private void showGraphic(int[] freqs) throws DRException {
        Collection<Fascia> coll = new LinkedList<>();//create collection
        for(int i=0;i<freqs.length;i++)
            coll.add(new Fascia(i,freqs[i]));
        /*Set styles*/
        StyleBuilder boldStyle = stl.style().bold();
        StyleBuilder columnStyle = stl.style().setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);
        StyleBuilder boldCenteredStyle = stl.style(boldStyle).setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);
        StyleBuilder columnTitleStyle  = stl.style(boldCenteredStyle)
		                                    .setBorder(stl.pen1Point())
		                                    .setBackgroundColor(Color.LIGHT_GRAY);
        
        TextColumnBuilder<Integer> hourColumn = col.column("Hour","hour",type.integerType());
        TextColumnBuilder<Integer> freqColumn = col.column("Frequency","freq",type.integerType());
        /*Create chart 3D*/
        XyLineChartBuilder graphic = cht.xyLineChart()
                                        .setTitle("Frequency chart")
                                        .setXValue(hourColumn)
                                        .setXAxisFormat(cht.axisFormat().setRangeMaxValueExpression(23).setLabel("Hours"))
                                        .setYAxisFormat(cht.axisFormat().setLabel("Number of bus"))
                                        .series(cht.xySerie(freqColumn));
                                        
        
        JasperReportBuilder jrb = report()
                .setColumnTitleStyle(columnTitleStyle)
                .setColumnStyle(columnStyle)
                .highlightDetailEvenRows()
                .title(cmp.text("Report frequences"))
                .columns( hourColumn,freqColumn)
                .summary(graphic)
                .setDataSource(coll)
                .show(false);
        
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
        controller.start();
    }

   
}
