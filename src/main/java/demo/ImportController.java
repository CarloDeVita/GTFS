package demo;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryCollection;
import datamanagement.MapDownloader;
import gtfs.Feed;
import gtfs.FeedParser;
import gtfs.dao.FeedDAO;
import gtfs.entities.Route;
import gtfs.entities.Shape;
import gtfs.entities.Stop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import org.apache.commons.io.FileUtils;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;


public class ImportController {
    private Feed feed;
    private ImportPanel panel;
    public ImportController(){
        panel = new ImportPanel();
        panel.chooseFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Select GTFS directory");
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.requestFocus();
                if(fileChooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION){
                    getFeed(fileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        });
        
        panel.downloadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String tmpDir = System.getProperty("java.io.tmpdir")+"/gtfs";
                File dir = new File(tmpDir);
                File file = new File(tmpDir+"/gtfs.zip");
                URL url = null;
                try {
                    if(!dir.exists()){
                        dir.mkdir();
                    }
                    else {
                        FileUtils.cleanDirectory(dir);
                    }
                    url = new URL(panel.urlText.getText());
                    FileUtils.copyURLToFile(url, file);
                    JOptionPane.showMessageDialog(null, "An issue has been experienced during the download, please check the URL and try again.","Downloading error",JOptionPane.ERROR_MESSAGE);
                    ZipFile zip = new ZipFile(file);
                    
                    Enumeration<? extends ZipEntry> entries = zip.entries();
                    while(entries.hasMoreElements()){
                        ZipEntry entry = entries.nextElement();
                        File dest = new File(tmpDir+"/"+entry.getName());
                        FileUtils.copyInputStreamToFile(zip.getInputStream(entry), dest);
                    }
                    
                    getFeed(tmpDir);
                } catch (ZipException ex){
                    JOptionPane.showMessageDialog(null, "The URL does not contain a zip file!","Invalid zip",JOptionPane.ERROR_MESSAGE);
                } catch (MalformedURLException ex) {
                    JOptionPane.showMessageDialog(null, "The URL submitted is malformed!", "Malformed URL", JOptionPane.ERROR_MESSAGE);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "An issue has been experienced during the download, please check the URL and try again.","Downloading error",JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        
        panel.importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(feed==null){
                    JOptionPane.showMessageDialog(null, "Select a feed first!", "Feed not loaded",JOptionPane.ERROR_MESSAGE);
                    return;
                }
                FeedDAO feedDAO = new FeedDAO();
                feedDAO.save(feed);
                
                //TODO continue
                MapDownloader downloader = new MapDownloader();
                //downloader.download(env, System.getProperty(key), "map");
            }
        });
        
    }
    
    public static void main(String args[]){
        JFrame frame = new JFrame();
        ImportController controller = new ImportController();
        frame.add(controller.panel);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
    
    private void getFeed(String path){
        try {
            feed = new FeedParser().read(path);
        } catch (FileNotFoundException ex){
            //TODO show missing file's name
            System.out.println(ex.getMessage());
            return;
        } catch (IOException ex) {
           JOptionPane.showMessageDialog(null, "An issue occurred during the analysis of the feed.","Unknown issue",JOptionPane.ERROR_MESSAGE);
        }
        
        // center the map
        
        // import the map

        // import the data

        panel.routeCombo.removeAllItems();

        Collection<Route> routes = feed.getRoutes();
        if(routes!=null){
            HashSet<String> routeNames = new HashSet<>();
            for(Route r : routes){
                if(r.getType()!=3) continue;
                String name = r.getName();
                if(routeNames.add(name))
                    panel.routeCombo.addItem(name);
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
        
        JMapViewer map=null;
        ICoordinate coord = new org.openstreetmap.gui.jmapviewer.Coordinate(centerX, centerY);
        map.setDisplayPosition(coord, 13);
    }
}
