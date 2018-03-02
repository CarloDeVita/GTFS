package demo.importgtfs;

import com.vividsolutions.jts.geom.Envelope;
import demo.importgtfs.datamanagement.MapDownloader;
import demo.importgtfs.datamanagement.OSMImporter;
import demo.importgtfs.datamanagement.PassageTimeCalculator;
import demo.importgtfs.datamanagement.RoadGraphCreator;
import demo.importgtfs.datamanagement.ShapeMatcher;
import gtfs.Feed;
import gtfs.FeedParser;
import gtfs.dao.FeedDAO;
import gtfs.entities.Route;
import gtfs.entities.Shape;
import gtfs.entities.Stop;
import gtfs.entities.Trip;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.apache.commons.io.FileUtils;


public class ImportController {
    private ImportFrame frame;
    private Feed feed;
    private HashMap<String,Set<String>> routesToTrips = new HashMap<>();
    
    public void start(){
        JOptionPane.setDefaultLocale(Locale.US);
        frame = new ImportFrame();
        frame.chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int choice = JOptionPane.showConfirmDialog(frame, "Existing data in the database will be erased.\nDo you wish to continue?", "Confirm", JOptionPane.YES_NO_OPTION);
                if(choice==JOptionPane.CANCEL_OPTION || choice==JOptionPane.CLOSED_OPTION || choice==JOptionPane.NO_OPTION){
                    return;
                }
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Select GTFS directory");
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.requestFocus();
                if(fileChooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION){
                    if(getFeed(fileChooser.getSelectedFile().getAbsolutePath()))
                        importGtfs();
                }
            }
        });
        
        frame.downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int choice = JOptionPane.showConfirmDialog(frame, "Existing data in the database will be erased.\nDo you wish to continue?", "Confirm", JOptionPane.YES_NO_OPTION);
                if(choice==JOptionPane.CANCEL_OPTION || choice==JOptionPane.CLOSED_OPTION || choice==JOptionPane.NO_OPTION){
                    return;
                }else if(frame.urlText.getText().isEmpty()){
                    JOptionPane.showMessageDialog(frame, "No URL submitted!", "Missing URL", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                String fileSeparator = System.getProperty("file.separator");
                String tmpDir = System.getProperty("java.io.tmpdir")+fileSeparator+"gtfs";
                
                File dir = new File(tmpDir);
                File file = new File(tmpDir+fileSeparator+"gtfs.zip");
                File dest = null;
                try {
                    if(!dir.exists()) dir.mkdir();
                    else FileUtils.cleanDirectory(dir);
                    
                    URL url = new URL(frame.urlText.getText());
                    FileUtils.copyURLToFile(url, file);
                    ZipFile zip = new ZipFile(file);
                    
                    Enumeration<? extends ZipEntry> entries = zip.entries();
                    while(entries.hasMoreElements()){
                        ZipEntry entry = entries.nextElement();
                        dest = new File(tmpDir+fileSeparator+entry.getName());
                        FileUtils.copyInputStreamToFile(zip.getInputStream(entry), dest);
                    }
                    
                    if(getFeed(tmpDir));
                        importGtfs();
                } catch (ZipException ex){
                    JOptionPane.showMessageDialog(null, "The URL does not contain a zip file!","Invalid zip",JOptionPane.ERROR_MESSAGE);
                } catch (MalformedURLException ex) {
                    JOptionPane.showMessageDialog(null, "The URL submitted is malformed!", "Malformed URL", JOptionPane.ERROR_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "An issue has been experienced during the download, please check the URL and try again.","Downloading error",JOptionPane.ERROR_MESSAGE);
                } finally{
                    FileUtils.deleteQuietly(file);
                    if(dest!=null)
                        FileUtils.deleteQuietly(dest);
                }
            }
        });
        
        frame.computeTimesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e){
                int choice = JOptionPane.showConfirmDialog(frame, "This operation may take a lot, do you want to continue?", "Confirm", JOptionPane.YES_NO_OPTION);
                if(choice==JOptionPane.CANCEL_OPTION || choice==JOptionPane.CLOSED_OPTION || choice==JOptionPane.NO_OPTION){
                    return;
                }
                
                new PassageTimeCalculator().calculate();
            }
        });
        
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
    
    public static void main(String args[]){
        new ImportController().start();
    }
    
    private void importGtfs(){
        String tasks[] = new String[]{
                    "Importing GTFS data",
                    "Downloading the map",
                    "Saving the map into the database",
                    "Creating street graph",
                    "Matching gtfs points with the streets"
                };

        final ProgressDialog progressDialog = new ProgressDialog(frame,"Import Progress",true,tasks);
        progressDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        progressDialog.setLocationRelativeTo(null);
        SwingWorker<String,Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                try{
                    //progressDialog.append("Phase 1 of %f : saving GTFS data into the database");
                    progressDialog.setStatus(1, ProgressDialog.OPERATION_START);
                    FeedDAO feedDAO = new FeedDAO();
                    feedDAO.save(feed);
                    progressDialog.setStatus(1,ProgressDialog.OPERATION_SUCCESS);
                    //progressDialog.append("Phase 1 completed");

                    if(isCancelled()) return null;


                    //progressDialog.append("Phase 2 of %f : Downloading OpenStreetMap data");
                    progressDialog.setStatus(2, ProgressDialog.OPERATION_START);
                    Envelope env = new Envelope();
                    //for(Stop s : feed.getStops())
                        //env.expandToInclude(s.getCoordinate().getCoordinate());

                    HashSet<Shape> shapes = new HashSet<>();

                    Collection<Trip> trips = feed.getTrips();
                    if(trips!=null){
                        for(Trip t : trips){
                            if(t.getRoute().getType()!=3) continue;
                            Shape shape = t.getShape();
                            if(shape!=null && shapes.add(shape))
                                for(Shape.Point sp : shape.getPoints()) 
                                    env.expandToInclude(sp.getCoordinate().getCoordinate());
                        }
                    }

                    if(shapes.isEmpty())
                        return "No shape found in feed.";

                    MapDownloader downloader = new MapDownloader();
                    try {
                        downloader.download(env, System.getProperty("java.io.tmpdir"), "map.osm");
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, "An error occurred during the import of the download of the map","Error",JOptionPane.ERROR_MESSAGE);
                        System.err.println(ex.getMessage());
                        progressDialog.setStatus(2, ProgressDialog.OPERATION_FAILED);
                        return "error";
                    }
                    progressDialog.setStatus(2, ProgressDialog.OPERATION_SUCCESS);

                    if(isCancelled()) return null;

                    progressDialog.setStatus(3, ProgressDialog.OPERATION_START);
                    OSMImporter importer = new OSMImporter();
                    //TODO db parameters!!
                    importer.setDirectory("C:\\Users\\Ricky\\Desktop\\osm2pgsql-bin")
                            .setDatabase("postgisTest").setUsername("postgres").setPassword("postgres").setExecutableName("osm2pgsql.exe");

                    String dataFile = System.getProperty("java.io.tmpdir")+"map.osm";
                    if(importer.importData(dataFile)){
                        FileUtils.deleteQuietly(new File(dataFile));
                        progressDialog.setStatus(3, ProgressDialog.OPERATION_SUCCESS);
                    }
                    else{
                        progressDialog.setStatus(3, ProgressDialog.OPERATION_FAILED);
                        JOptionPane.showMessageDialog(null,"Map import failed", "Error", JOptionPane.ERROR_MESSAGE);
                        return "Map data import failed";
                    }

                    if(isCancelled()) return null;

                    RoadGraphCreator roadGraphCreator = new RoadGraphCreator();
                    progressDialog.setStatus(4, ProgressDialog.OPERATION_START);
                    if(roadGraphCreator.createStreetGraph())
                        progressDialog.setStatus(4, ProgressDialog.OPERATION_SUCCESS);
                    else{
                        progressDialog.setStatus(4, ProgressDialog.OPERATION_FAILED);
                        return "Creation of the street graph failed";
                    }

                    if(isCancelled()) return null;

                    progressDialog.setStatus(5, ProgressDialog.OPERATION_START);
                    ShapeMatcher matcher = new ShapeMatcher();
                    if(matcher.match())
                        progressDialog.setStatus(5, ProgressDialog.OPERATION_SUCCESS);
                    else{
                        progressDialog.setStatus(5, ProgressDialog.OPERATION_FAILED);
                        return "Matching of the shapes failed";
                    }
                    return "OK";

                }catch(Exception e){
                    System.err.println(e.getMessage());
                    return "An error occurred during the import.\nCheck the application log to know more.";
                }finally{
                    progressDialog.dispose();
                }
            }
        };
        worker.execute();

        progressDialog.setVisible(true); // waits for the process to end
        try {
            String result = worker.get();
            if(result==null) return;
            if(result.equals("OK"))
                JOptionPane.showMessageDialog(frame, "Import completed successfully!","Import result",JOptionPane.INFORMATION_MESSAGE);
            else
                JOptionPane.showMessageDialog(frame, result,"Import result",JOptionPane.ERROR_MESSAGE);
            
            feed = null; //help garbage collector
            
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(ImportController.class.getName()).log(Level.SEVERE, null, ex);
        }
        /*Envelope env = new Envelope();
        for(Stop s : feed.getStops())
        env.expandToInclude(s.getCoordinate().getCoordinate());
        Collection<Shape> shapes = feed.getShapes();
        if(shapes!=null)
        for(Shape s : shapes)
        for(Shape.Point sp : s.getPoints())
        env.expandToInclude(sp.getCoordinate().getCoordinate());
        MapDownloader downloader = new MapDownloader();
        try {
        downloader.download(env, System.getProperty("java.io.tmpdir"), "map.osm");
        } catch (IOException ex) {
        JOptionPane.showMessageDialog(null, "An error occurred during the import of the download of the map","Error",JOptionPane.ERROR_MESSAGE);
        System.err.println(ex.getMessage());
        return;
        }
        OSMImporter importer = new OSMImporter();
        //TODO db parameters!!
        importer.setDirectory("C:\\Users\\Ricky\\Desktop\\osm2pgsql-bin")
        .setDatabase("postgisTest").setUsername("postgres").setPassword("postgres").setExecutableName("osm2pgsql.exe");
        String dataFile = System.getProperty("java.io.tmpdir")+System.getProperty("file.separator")+"map.osm";
        if(importer.importData(dataFile)){
        FileUtils.deleteQuietly(new File(dataFile));
        JOptionPane.showMessageDialog(null, "Map successfully imported");
        }
        else{
        JOptionPane.showMessageDialog(null,"Map import failed", "Error", JOptionPane.ERROR_MESSAGE);
        }*/ 
       


            /*Envelope env = new Envelope();
            for(Stop s : feed.getStops())
            env.expandToInclude(s.getCoordinate().getCoordinate());

            Collection<Shape> shapes = feed.getShapes();
            if(shapes!=null)
            for(Shape s : shapes)
            for(Shape.Point sp : s.getPoints())
            env.expandToInclude(sp.getCoordinate().getCoordinate());

            MapDownloader downloader = new MapDownloader();
            try {
            downloader.download(env, System.getProperty("java.io.tmpdir"), "map.osm");
            } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "An error occurred during the import of the download of the map","Error",JOptionPane.ERROR_MESSAGE);
            System.err.println(ex.getMessage());
            return;
            }

            OSMImporter importer = new OSMImporter();
            //TODO db parameters!!
            importer.setDirectory("C:\\Users\\Ricky\\Desktop\\osm2pgsql-bin")
            .setDatabase("postgisTest").setUsername("postgres").setPassword("postgres").setExecutableName("osm2pgsql.exe");

            String dataFile = System.getProperty("java.io.tmpdir")+System.getProperty("file.separator")+"map.osm";
            if(importer.importData(dataFile)){
            FileUtils.deleteQuietly(new File(dataFile));
            JOptionPane.showMessageDialog(null, "Map successfully imported");
            }
            else{
            JOptionPane.showMessageDialog(null,"Map import failed", "Error", JOptionPane.ERROR_MESSAGE);
            }*/

    }
    
    private boolean getFeed(String path){
        try {
            feed = new FeedParser().read(path);
        } catch (FileNotFoundException ex){
            //TODO show missing file's name
            return false;
        } catch (IOException ex) {
           JOptionPane.showMessageDialog(null, "An issue occurred during the analysis of the feed.","Unknown issue",JOptionPane.ERROR_MESSAGE);
           return false;
        } catch(Exception ex){
            return false;
        }
        
        //TODO center the map

        frame.routeCombo.removeAllItems();
        frame.tripCombo.removeAllItems();
        
        routesToTrips.clear();
        
        Collection<Route> routes = feed.getRoutes();
        if(routes!=null){
            HashSet<String> routeNames = new HashSet<>();
            for(Route r : routes){
                if(r.getType()!=3) continue;
                String name = r.getName();
                if(routeNames.add(name))
                    frame.routeCombo.addItem(name);
                
                    HashSet<String> routeTrips = new HashSet<>();
                    Collection<Trip> trips = r.getTrips();
                    if(trips!=null){
                        for(Trip t: trips)
                            if(t.getShape()!=null)
                                routeTrips.add(t.getId()+"-"+t.getHeadSign());
                    }
                
                routesToTrips.put(name, routeTrips);
            }
        }
        

        
        Envelope env = new Envelope();
        for(Stop s : feed.getStops())
            env.expandToInclude(s.getCoordinate().getCoordinate());
        
        Collection<Shape> shapes = feed.getShapes();
        if(shapes!=null) 
            for(Shape s : shapes) 
                for(Shape.Point sp : s.getPoints()) 
                    env.expandToInclude(sp.getCoordinate().getCoordinate());

        double centerX = env.getMaxX()-env.getMinX();
        double centerY = env.getMaxY()-env.getMinY();
        
        /*JMapViewer map=null;
        ICoordinate coord = new org.openstreetmap.gui.jmapviewer.Coordinate(centerX, centerY);
        map.setDisplayPosition(coord, 13);*/
        return true;
    }
}
