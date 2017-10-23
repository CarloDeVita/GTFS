package demo;

import gtfs.entities.Agency;
import gtfs.entities.Calendar;
import gtfs.entities.Frequency;
import gtfs.entities.Route;
import gtfs.entities.Shape;
import gtfs.entities.Stop;
import gtfs.entities.StopTime;
import gtfs.entities.Trip;
import gtfs.parser.AgencyParser;
import gtfs.parser.CalendarFileParser;
import gtfs.parser.CalendarParser;
import gtfs.parser.FrequencyParser;
import gtfs.parser.RouteParser;
import gtfs.parser.ShapeParser;
import gtfs.parser.StopParser;
import gtfs.parser.StopTimeParser;
import gtfs.parser.TripParser;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Riccardo
 */
public class Test {
    public static void main(String args[]) throws IOException{
        
        AgencyParser agencyParser = new AgencyParser();
        Collection<Agency> agencies =null;
        String directory = "./gtfs_feeds/gtfs_ANM/";
        try {
            agencies = agencyParser.parse(directory);
        } catch (IOException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(agencies==null) return;
        
        for(Agency a : agencies)
            System.out.println(a);
            
        RouteParser routeParser = new RouteParser();
        routeParser.addAgencies(agencies);
        
        Collection<Route> routes = null;
        try {
            routes = routeParser.parse(directory);
        } catch (IOException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(routes==null) return;
        
        //for(Route r : routes) System.out.println(r);
    
        CalendarParser calendarParser = new CalendarParser();
        Collection<Calendar> calendars = null;
        try {
            calendars = calendarParser.read(directory);
        } catch (IOException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(calendars == null) return;
        for(Calendar c : calendars) System.out.println(c);
        
        
        Collection<Shape> shapes = null;
        ShapeParser shapeParser = new ShapeParser();
        shapes = shapeParser.parse(directory);
        
        Collection<Trip> trips= null;
        TripParser tripParser = new TripParser();
        tripParser.addCalendars(calendars);
        tripParser.addRoutes(routes);
        tripParser.addShapes(shapes);
        trips = tripParser.parse(directory);
        if(trips==null) return;
        //for(Trip t : trips)  System.out.println(t);
        
        Collection<Frequency> frequencies = null;
        FrequencyParser frequencyParser = new FrequencyParser();
        frequencyParser.addTrips(trips);
        frequencies = frequencyParser.parse(directory);
        
        for(Frequency f : frequencies)
            System.out.println(f);
        
        if(true) return;
        Collection<Stop> stops = null;
        StopParser stopParser = new StopParser();
        stops = stopParser.parse(directory);
        if(stops==null) return;
        for(Stop s : stops)
            System.out.println(s);
        
        Collection<StopTime> stopTimes = null;
        StopTimeParser stopTimeParser = new StopTimeParser();
        stopTimeParser.addStops(stops);
        stopTimeParser.addTrips(trips);
        stopTimes = stopTimeParser.parse(directory);
        if(stops==null) return;
        /*for(StopTime s : stopTimes)
            System.out.println(s);
        */
}
}
