package gtfs;

import gtfs.entities.*;
import gtfs.parser.*;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.io.IOException;

/**
 * A parser for an entire GTFS feed.
 */
public class FeedParser {
    /**
     * Retrieves the feed from a GTFS feed directory.
     * 
     * @param directory the path of the feed directory. Must be not null.
     * @return the feed of the directory.
     * @throws FileNotFoundException if one of the required file misses.
     */
    public Feed read(String directory) throws IOException, FileNotFoundException{
        Collection<Agency> agencies = null;
        Collection<Route> routes = null;
        Collection<Calendar> calendars = null;
        Collection<Shape> shapes = null;
        Collection<Trip> trips= null;
        Collection<Stop> stops = null;
        Collection<StopTime> stopTimes = null;
        Collection<Frequency> frequencies = null;
        
        // read the agencies
        AgencyParser agencyParser = new AgencyParser();
        agencies = agencyParser.parse(directory);
        
        // read the routes
        RouteParser routeParser = new RouteParser();
        routeParser.addAgencies(agencies);
        routes = routeParser.parse(directory);

        // read the calendars and the calendar dates
        CalendarParser calendarParser = new CalendarParser();
        calendars = calendarParser.read(directory);
        
        // read the shapes
        ShapeParser shapeParser = new ShapeParser();
        try{
            shapes = shapeParser.parse(directory);
        }catch(FileNotFoundException ex){
            // nothing because not required
        }
        
        // read the trips
        TripParser tripParser = new TripParser();
        tripParser.addCalendars(calendars);
        tripParser.addRoutes(routes);
        if(shapes!=null)
            tripParser.addShapes(shapes);
        trips = tripParser.parse(directory);
        
        // read the stops
        StopParser stopParser = new StopParser();
        stops = stopParser.parse(directory);
        
        // read the stop times
        StopTimeParser stopTimeParser = new StopTimeParser();
        stopTimeParser.addStops(stops);
        stopTimeParser.addTrips(trips);
        stopTimes = stopTimeParser.parse(directory);
        
        // read the frequencies
        FrequencyParser frequencyParser = new FrequencyParser();
        frequencyParser.addTrips(trips);
        try{
            frequencies = frequencyParser.parse(directory);
        }catch(FileNotFoundException ex){
            // nothing because not required
        }
        
        // create the feed
        Feed feed = new Feed();
        feed.setAgencies(agencies);
        feed.setRoutes(routes);
        feed.setTrips(trips);
        feed.setStops(stops);
        feed.setStopTimes(stopTimes);
        feed.setCalendars(calendars);
        feed.setShapes(shapes);
        feed.setFrequencies(frequencies);
        
        return feed;
    }
}
