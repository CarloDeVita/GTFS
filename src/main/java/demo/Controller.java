package demo;

import com.vividsolutions.jts.geom.Envelope;
import datamanagement.MapDownloader;
import datamanagement.OSMImporter;
import demo.hibernate.HibernateUtil;
import demo.maputils.IconMarker;
import demo.maputils.MapLine;
import gtfs.Feed;
import gtfs.FeedParser;
import gtfs.dao.FeedDAO;
import gtfs.entities.Route;
import gtfs.entities.Shape;
import gtfs.entities.Stop;
import gtfs.entities.Trip;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;
import org.postgis.Geometry;
import org.postgis.PGgeometry;
import org.postgis.Point;

public class Controller {
    public static void main(String args[]) throws IOException, SQLException{
        //showSegments();
        JFileChooser ch = new JFileChooser();
        ch.setDialogTitle("Choose GTFS directory");
        ch.setCurrentDirectory(new File(System.getProperty("user.home")));
        ch.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        ch.requestFocus();
        if(ch.showOpenDialog(null)==JFileChooser.APPROVE_OPTION){
            // load the gtfs feed
            FeedParser parser = new FeedParser();
            final Feed feed = parser.read(ch.getSelectedFile().getAbsolutePath());
            
            Thread th = new Thread(){
                @Override
                public void run(){
                   FeedDAO feedDAO = new FeedDAO();
                   feedDAO.save(feed);
                }
            };
            th.start();
            
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            JMapViewer map = new JMapViewer();
            frame.add(map);
            frame.setVisible(true);
            // create bounding box
            Envelope envelope = new Envelope();
            for(Shape shape : feed.getShapes())
                for(Shape.Point point : shape.getPoints())
                    envelope.expandToInclude(point.getCoordinate().getCoordinate());
            for(Stop stop : feed.getStops())
                envelope.expandToInclude(stop.getCoordinate().getCoordinate());
            
            
            // download the map
            new MapDownloader().download(envelope, null,null );

            // import the map into the database
            OSMImporter importer = new OSMImporter();
            importer.setUsername("postgres").setPassword("postgres").setDatabase("postgisTest")
                .setDirectory(System.getProperty("user.home")+"\\Desktop\\osm2pgsql-bin").setExecutableName("osm2pgsql.exe");
            boolean success = importer.importData(System.getProperty("java.io.tmpdir")+"map.osm");
            
            if(success) System.out.println("Data successfully imported");
            else{ System.out.println("Data could not be imported"); return;}
            //showSegments();
            /*Shape firstShape = null;
            Route r=null;
            for(Trip t : feed.getTrips()){
                
                if(t.getRoute().getShortName().equals("180") && t.getShape()!=null){
                    if(r==null) r=t.getRoute();
                    
                    firstShape = t.getShape();
                    if(r!=t.getRoute()) break;
                    r = t.getRoute();
                }
                
            }
            Shape.Point px = null;
            for(Shape.Point sp : firstShape.getPoints()){
                if(px==null){
                    px = sp;
                    map.addMapMarker(new IconMarker(new Coordinate(px.getLat(),px.getLon()),ImageIO.read(new Controller().getClass().getClassLoader().getResourceAsStream("station.png"))));
                }    
                map.addMapMarker(new MapMarkerDot(sp.getLat(), sp.getLon()));
                
            }
            
            //stampa envelope
            List<Coordinate> envPolygon = new LinkedList<>();
            envPolygon.add(new Coordinate(envelope.getMaxY(), envelope.getMinX()));
            envPolygon.add(new Coordinate(envelope.getMaxY(), envelope.getMaxX()));
            envPolygon.add(new Coordinate(envelope.getMinY(), envelope.getMaxX()));
            envPolygon.add(new Coordinate(envelope.getMinY(), envelope.getMinX()));
            
            map.addMapPolygon(new MapPolygonImpl(envPolygon));*/
            
            
            //stampa matched
            try { 
          /* 
          * Load the JDBC driver and establish a connection. 
          */
                Class.forName("org.postgresql.Driver"); 
                String url = "jdbc:postgresql://localhost:5432/postgisTest"; 
                Connection conn = DriverManager.getConnection(url, "postgres", "postgres"); 
          /* 
          * Add the geometry types to the connection. Note that you 
          * must cast the connection to the pgsql-specific connection 
          * implementation before calling the addDataType() method. 
          */
          
          ((org.postgresql.PGConnection)conn).addDataType("geometry",org.postgis.PGgeometry.class);
          //((org.postgresql.PGConnection)conn).addDataType("box3d","org.postgis.PGbox3d");
          /* 
          * Create a statement and execute a select query. 
          */
            long time;
            Statement st = conn.createStatement();
            time = System.currentTimeMillis();
            st.execute("Select PopulateSegments()");
            time = System.currentTimeMillis() - time;
            System.out.println("NOTICE : Segments table populated in "+time/1000.+" seconds");
            th.join();
           
            st = conn.createStatement();
            time = System.currentTimeMillis();
            st.execute("select matchseg()");
            time = System.currentTimeMillis() - time;
            System.out.println("NOTICE : Shapes matched with the street map in "+time/1000.+" seconds");
            
            /*String query =" select S.segment from matchedsegments M join segments S on M.segment=S.id where shape_id = ?";
            PreparedStatement s = conn.prepareStatement(query);
            s.setString(1, firstShape.getId());
            ResultSet rs = s.executeQuery();
            rs.next();
            while(rs.next()){
                Geometry geom = ((PGgeometry) rs.getObject("segment")).getGeometry();
                LinkedList<ICoordinate> l = new LinkedList<>();
                ICoordinate c = null;
                for(int i = 0 ; i<geom.numPoints();i++){
                    Point p = geom.getPoint(i);
                    c = new Coordinate(p.y,p.x);
                    l.add(c);
                }
                if(l.size()==2) l.add(c);
                MapLine mline = new MapLine(l);
                map.addMapPolygon(mline);
            }
            
            
            /*String query = "select ST_X(sp.coordinate) as lon,ST_Y(sp.coordinate) as lat, r.shortname, r.longname from gtfs.shape_points sp join gtfs.trips t on t.shape=sp.shape_id join gtfs.routes r on t.shape=r.id where not exists (Select id from segments s where ST_Dwithin(s.segment,sp.coordinate,0.0003)) and r.type=3 and r.shortname not in ('L1','F1','F2')";
            PreparedStatement pst = conn.prepareStatement(query);
            ResultSet rs = pst.executeQuery();
            HashSet<String> names = new HashSet<>();
            while(rs.next()){
                ICoordinate c = new Coordinate(rs.getDouble("lat"), rs.getDouble("lon"));
                map.addMapMarker(new MapMarkerDot((Coordinate) c));
                names.add(rs.getString("shortname")+"|"+rs.getString("longname"));
            }
            for(String s : names)
                System.out.println(s);
            */
            }catch( Exception e ) { 
                e.printStackTrace(); 
            }
        }
    }
}