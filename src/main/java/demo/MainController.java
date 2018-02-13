/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demo;

import dao.SegmentDAO;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openstreetmap.gui.jmapviewer.DefaultMapController;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerCircle;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.openstreetmap.gui.jmapviewer.interfaces.MapMarker;
import demo.maputils.MapLine;
import gtfs.dao.FeedDAO;
import java.awt.Cursor;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingWorker;

import org.openstreetmap.gui.jmapviewer.Coordinate;
/**
 *
 * @author cdevi
 */
public class MainController {
    
    private JMapViewer map;
    private MainFrame frame;
    private DefaultMapController mapCtl;
    private ICoordinate coordinateCenter;
    private int status;
    private double radius;
    public static final int SETCENTER = 0;
    public static final int FINDSEGMENT = 1;
    GeometryFactory gf = new GeometryFactory();
    
    public static void main(String args[]){
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        MainController mainC = new MainController();
        try {
            mainC.start();
        } catch (IOException | SQLException ex) {
            Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }

    //Per ora fa partire statistics, poi cambierà per far partire anche l'altro controller
    private void start() throws IOException, SQLException {
        map = new JMapViewer();
        frame = new MainFrame(map, this);
        frame.setVisible(true);
        frame.setEnabled(false);
        Cursor c = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
        frame.setCursor(c);
        
        new SwingWorker<Void,Void>(){
            @Override
            protected Void doInBackground() throws Exception {
                //FeedDAO fd = new FeedDAO();
                SegmentDAO sd = new SegmentDAO();
                Geometry bound = sd.getBound();
                setDisplayPositionToBoundCenter(bound);
                return null;
            }
            
            @Override
            protected void done(){
                frame.setCursor(Cursor.getDefaultCursor());
                frame.setEnabled(true);
            }
            
        }.execute();
        
        
        mapCtl = new DefaultMapController(map){
            @Override
            //Override method mouseClicked to find nearest segment from click position.
            public void mouseClicked(MouseEvent e) {
                if(status==SETCENTER)
                    setCenterRadius(e.getPoint());
                else if(status == FINDSEGMENT)
                    findSegment(e.getPoint());
            }

            private void findSegment(Point point) {
                
            }

            private void setCenterRadius(Point point) {
                coordinateCenter = map.getPosition(point);
                double km = Double.parseDouble(frame.getRadiusText());
                radius = km/111.325;//convert km to angle
                frame.setCenterText(coordinateCenter.getLat(),coordinateCenter.getLon());
                MapMarker circle = new MapMarkerCircle((org.openstreetmap.gui.jmapviewer.Coordinate)coordinateCenter, radius);
                map.addMapMarker(circle);
            }
        };
    }
/*
    public void searchSegments() {
        map.removeAllMapMarkers();
        SegmentDAO sDao = new SegmentDAO();
        List<Segment> segments=sDao.searchSegments(coordinateCenter, radius);
        showSegments(segments);
        status = FINDSEGMENT;
    }
*/
    private void setDisplayPositionToBoundCenter(Geometry bound) {
        com.vividsolutions.jts.geom.Point center = bound.getCentroid();
        setDisplayPosition(center);
    }

    private void setDisplayPosition(com.vividsolutions.jts.geom.Point center) {
        Coordinate c = new Coordinate(center.getCoordinate().y,center.getCoordinate().x);
        map.setDisplayPosition(c, 10);
    }
/*
    private void showSegments(List<Segment> segments) {
        //map.removeAllMapMarkers();
        
        Coordinate c=null;
        
        for(Segment s : segments){
            List<Coordinate> coords = new LinkedList<>();
            for(com.vividsolutions.jts.geom.Coordinate coord : s.getSegment().getCoordinates() ){
                c = new Coordinate(coord.y,coord.x);
                coords.add(c);
            }
            if(coords.size() <=2) coords.add(c);
            MapLine ml = new MapLine(coords);
            map.addMapPolygon(ml);
        }
        
    }
    */
}
