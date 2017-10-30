package gtfs;

import gtfs.entities.*;
import gtfs.parser.*;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.io.IOException;
import java.util.ArrayList;

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
    public Feed read(final String directory) throws IOException, FileNotFoundException{
        final Thread threads[] = new Thread[3];
        final Exception exceptions[] = new Exception[3];
        // result containers for threads
        ArrayList<Collection<Calendar>> calendarsContainer = new ArrayList<>(1);
        ArrayList<Collection<Shape>> shapesContainer = new ArrayList<>(1);
        ArrayList<Collection<Stop>> stopsContainer = new ArrayList<>(1);
        ArrayList<Collection<Frequency>> frequenciesContainer = new ArrayList<>(1);
        
        // read the calendars
        threads[0] = new Thread(){
            @Override
            public void run(){
                CalendarParser calendarParser = new CalendarParser();
                try{
                    Collection<Calendar> calendars = calendarParser.read(directory);
                    calendarsContainer.add(calendars);
                }catch(Exception e){
                        exceptions[0] = e;
                }
            }
        };

        // read the shapes
        threads[1] = new Thread(){
            @Override
            public void run(){
                ShapeParser shapeParser = new ShapeParser();
                try{
                    Collection<Shape> shapes = shapeParser.parse(directory);
                    shapesContainer.add(shapes);
                }catch(Exception e){
                    exceptions[1] = e;
                }
            }
        };
        
        // read the stops
        threads[2] = new Thread(){
            @Override
            public void run(){
                StopParser stopParser = new StopParser();
                try{
                    Collection<Stop> stops = stopParser.parse(directory);
                    stopsContainer.add(stops);
                }catch(Exception e){
                    exceptions[2] = e;
                }
            }
        };
        
        for(Thread t : threads)
            t.start();
        
        // read the agencies
        AgencyParser agencyParser = new AgencyParser();
        Collection<Agency> agencies = agencyParser.parse(directory);
        
        // read the routes
        Collection<Route> routes = null;
        RouteParser routeParser = new RouteParser();
        routeParser.addAgencies(agencies);
        routes = routeParser.parse(directory);

        for(int i=0 ; i<3 ; i++){
            try {
                threads[i].join();
                threads[i] = null;
            } catch (InterruptedException ex) {
                return null;
            }
            Exception e = exceptions[i];
            if(e!=null){
                if((e instanceof FileNotFoundException)){
                    if(i!=1)throw (FileNotFoundException) e;
                }
                if(e instanceof IOException) throw (IOException) e;
                if(e instanceof GTFSParsingException) throw (GTFSParsingException) e;
                throw (RuntimeException) e;
            }
        }

        // read the trips
        Collection<Shape> shapes = shapesContainer.isEmpty() ? null : shapesContainer.get(0);
        final Collection<Stop> stops = stopsContainer.get(0);
        Collection<Calendar> calendars = calendarsContainer.get(0);
        TripParser tripParser = new TripParser();
        tripParser.addCalendars(calendars);
        tripParser.addRoutes(routes);
        if(shapes!=null)
            tripParser.addShapes(shapes);
        final Collection<Trip> trips = tripParser.parse(directory);

        // read the frequencies
        threads[0] = new Thread(){
            @Override
            public void run(){
                FrequencyParser frequencyParser = new FrequencyParser();
                try{
                    frequencyParser.addTrips(trips);
                    Collection<Frequency> frequencies = frequencyParser.parse(directory);
                    frequenciesContainer.add(frequencies);
                } catch (Exception e) {
                    exceptions[0] = e;
                }
            }
        };
        threads[0].start();
        
        // read the stop times
        StopTimeParser stopTimeParser = new StopTimeParser();
        stopTimeParser.addStops(stops);
        stopTimeParser.addTrips(trips);
        Collection<StopTime> stopTimes = stopTimeParser.parse(directory);
        
        try{
            threads[0].join();
        }catch(InterruptedException e){
            return null;
        }
        Exception e = exceptions[0];
        if(e!=null && !(e instanceof FileNotFoundException)){
            if(e instanceof IOException) throw (IOException) e;
            if(e instanceof GTFSParsingException) throw (GTFSParsingException) e;
            throw (RuntimeException) e;
        }
        
        Collection<Frequency> frequencies = frequenciesContainer.isEmpty() ? null : frequenciesContainer.get(0);
        
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
