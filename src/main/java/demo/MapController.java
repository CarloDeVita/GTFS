package demo;

import demo.maputils.IconMarker;
import demo.maputils.MapLine;
import gtfs.Feed;
import gtfs.FeedParser;
import gtfs.entities.Route;
import gtfs.entities.Shape;
import gtfs.entities.Stop;
import gtfs.entities.StopTime;
import gtfs.entities.Trip;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;

/**
 *
 * @author cdevi
 */
public class MapController {
    Feed feed;//TODO togliere dagli attributi
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
    
    public void chooseGtfs(){
        Preferences pref = Preferences.userNodeForPackage(Mappa.class);
        String dirpath = pref.get("gtfs_dir", System.getProperty("user.home"));
        JFileChooser ch = new JFileChooser();
        ch.setDialogTitle("Choose directory GTFS");
        ch.setCurrentDirectory(new File(dirpath));
        ch.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if(ch.showOpenDialog(m) == JFileChooser.APPROVE_OPTION){
            File currentDir = ch.getCurrentDirectory();
            pref.put("gtfs_dir", currentDir.getAbsolutePath());
            File file = ch.getSelectedFile();
            String dir = file.getAbsolutePath();
            setGTFS(dir);
        }
    }
    
     /**
     * 
     * @param dir absolute path of GTFS directory.
     */
    public void setGTFS(String dir){
        final JDialog dialog = new JDialog(m, "Wait"); 
        dialog.setLocation(m.getWidth()/2, m.getHeight()/2- dialog.getHeight());
        dialog.setUndecorated(false); 
        JProgressBar bar = new JProgressBar();
        bar.setIndeterminate(true);
        bar.setStringPainted(true);
        bar.setString("Please wait...");
        bar.setVisible(true);
        dialog.add(bar);
        dialog.setSize(300,150);
        dialog.setVisible(true);
        
        SwingWorker<Void,Void> sw = new SwingWorker<Void,Void>(){
            @Override
            protected Void doInBackground() throws Exception {
                try{
                    feed = new FeedParser().read(dir);
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
    
    private void setRouteList(Collection<Route> routes){
        String[] arr = new String[routes.size()];
        int i=0;
        arr[0] = "";
        for(Route r : routes)
            arr[i++] = r.getName();
        
        m.setRouteList(arr);
    }
    
    public void showRoute(String name){
        Coordinate c=null;
        try {
            Image im = ImageIO.read(getClass().getClassLoader().getResourceAsStream("station.png"));
            Route r = feed.getRouteByName(name);
            HashSet<Stop> stops = new HashSet<>();
            
            for(Trip t : r.getTrips()){
                Shape s = t.getShape();

                List<Coordinate> shapel = new LinkedList<>();
                for(Shape.Point p : s.getPoints()){
                    c = new Coordinate( p.getLat(),p.getLon()) ;
                    
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
