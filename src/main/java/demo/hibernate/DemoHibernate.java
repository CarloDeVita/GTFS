package demo.hibernate;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import gtfs.FeedParser;
import gtfs.entities.Trip;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;
import gtfs.Feed;
import gtfs.entities.Route;
import gtfs.entities.Stop;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

/**
 *
 */
public class DemoHibernate {
    public static void main(String[] args) throws IOException {
        HibernateUtil hibernateUtil = null;
        try{
            HibernateUtil.setDefaultCatalog("postgis_test");
            hibernateUtil = HibernateUtil.getInstance();
            //if(true) return;
            Preferences pref = Preferences.userNodeForPackage(DemoHibernate.class);
            String dirpath = pref.get("gtfs_dir", System.getProperty("user.home"));
            JFileChooser ch = new JFileChooser();
            ch.setDialogTitle("Choose directory GTFS");
            ch.setCurrentDirectory(new File(dirpath));
            ch.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            ch.requestFocus();
            if(ch.showOpenDialog(null)==JFileChooser.APPROVE_OPTION){
                File currentDir = ch.getCurrentDirectory();
                pref.put("gtfs_dir", currentDir.getAbsolutePath());
                File file = ch.getSelectedFile();
                dirpath = file.getAbsolutePath();
                FeedParser feedParser = new FeedParser();
                long start = System.currentTimeMillis();
                Feed feed = feedParser.read(dirpath);
                if(false)
                    throw new FileNotFoundException();
                long end = System.currentTimeMillis();
                System.out.println("Ottenuto feed in tempo : "+ ((end-start)/1000.)+" secondi");
                hibernateUtil.saveCollection(feed.getAgencies());
                System.out.println("|------------ Saved agencies-----------|");
                hibernateUtil.saveCollection(feed.getRoutes());
                System.out.println("|------------ Saved routes-----------|");
                hibernateUtil.saveCollection(feed.getCalendars());
                System.out.println("|------------ Saved calendars-----------|");
                if(feed.getShapes()!=null)
                    hibernateUtil.saveCollection(feed.getShapes());
                System.out.println("|------------ Saved shapes-----------|");
                hibernateUtil.saveCollection(feed.getTrips());
                System.out.println("|------------ Saved trips-----------|");
                LinkedList<Stop> noParent = new LinkedList<>();
                LinkedList<Stop> withParent = new LinkedList<>();
                for(Stop s : feed.getStops()){
                    if(s.getParent()==null)
                        noParent.add(s);
                    else
                        withParent.add(s);
                }
                hibernateUtil.saveCollection(noParent);
                hibernateUtil.saveCollection(withParent);
                System.out.println("|------------ Saved stops-----------|");
                hibernateUtil.saveCollection(feed.getStopTimes());
                System.out.println("|------------ Saved stop times-----------|");
                if(feed.getFrequencies()!=null)
                    hibernateUtil.saveCollection(feed.getFrequencies());
                System.out.println("|------------ Saved frequencies-----------|");
            }
            GeometryFactory factory;
                factory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);

        }catch(FileNotFoundException e){
            System.err.println("ERRORE : "+e.getMessage());
            throw e;
        }
        finally{
            if(hibernateUtil!=null)
                hibernateUtil.close();
        }
        return;
    }
}
