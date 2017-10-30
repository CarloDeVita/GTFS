package gtfs.parser;

import gtfs.entities.Calendar;
import gtfs.entities.Route;
import gtfs.entities.Shape;
import gtfs.entities.Trip;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses the trip contained in trips file.
 */
public class TripParser extends GTFSParser<Trip> {
    // associates each id with the corresponding Route
    private Map<String, Route> routes = new HashMap<>();
    // associates each id with the corresponding calendar
    private Map<String, Calendar> calendars = new HashMap<>();
    // associates each id with the corresponding shape
    private Map<String, Shape> shapes = new HashMap<>();
    
    /**
     * 
     * @return true if at least one route and one calendar has been added, false otherwise.
     */
    @Override
    public boolean isReady(){
        return !(routes.isEmpty()|| calendars.isEmpty() || shapes.isEmpty());
    }        
    /**
     * Adds the routes to the parser.
     * These routes are used to check the references in route_id field.
     * 
     * @param rCollection The collection of routes to add. Must be not null.
     */
    public void addRoutes(Collection<Route> rCollection){
        if(rCollection==null) return;
        for(Route r : rCollection)
            routes.put(r.getId(), r);
    }
    
    /**
     * Adds the calendars to the parser.
     * These calendars are used to check the references in service_id field.
     * 
     * @param cCollection The collection of calendars to add. Must be not null.
     */
    public void addCalendars(Collection<Calendar> cCollection){
        if(cCollection==null) return;
        for(Calendar c : cCollection)
            calendars.put(c.getId(), c);
    }
    
    /**
     * Adds the shapes to the parser.
     * These shapes are used to check the references in shape_id field.
     * Shapes are not necessary for the parsing.
     * 
     * @param sCollection The collection of shapes to add. Must be not null.
     */
    public void addShapes(Collection<Shape> sCollection){
        if(sCollection==null) return;
        for(Shape s : sCollection)
            shapes.put(s.getId(), s);
    }
    
    @Override
    public void clear(){
        super.clear();
        routes.clear();
        calendars.clear();
        shapes.clear();
    }
    
    @Override
    public String getFileName() {
        return "trips.txt";
    }

    @Override
    protected int columnToParameter(String name) {
        switch(name){
            case "route_id" : return 0;
            case "service_id" : return 1;
            case "trip_id" : return 2;
            case "trip_headsign" : return 3;
            case "trip_short_name" : return 4;
            case "direction_id" : return 5;
            case "block_id" : return 6;
            case "shape_id" : return 7;
            case "wheelchair_accessible" : return 8;
            case "bikes_allowed" : return 9;
        }
        return -1;
    }

    @Override
    protected boolean checkRequired(String[] firstRow) {
        boolean route = false;
        boolean service = false;
        boolean id = false;
        for(String s : firstRow){
            switch(s){
                case "route_id" : route = true; break;
                case "service_id" : service = true; break;
                case "trip_id" : id = true; break;
            }
        }
        return (route && service && id);
    }

    @Override
    protected void processRow(String[] parameters, Collection<Trip> result) {
        String routeString = parameters[0];
        String calendarId = parameters[1];
        String id = parameters[2];
        String headSign = parameters[3];
        String shortName = parameters[4];
        String directionString = parameters[5];
        String blockString = parameters[6]; //TODO unused
        String shapeId = parameters[7];
        String wheelchair = parameters[8];
        String bikes = parameters[9];
        
        // check required values
        if(routeString==null || calendarId==null || id==null)
            throw new GTFSParsingException("Missing required value");
        
        // get and check the route
        Route route = routes.get(routeString);
        if(route==null) throw new GTFSParsingException("Missing route : " + routeString);
        // get and check the calendar
        Calendar calendar = calendars.get(calendarId);
        if(calendar==null) throw new GTFSParsingException("Missing calendar : " + calendarId);
        
        // get and check the shape
        Shape shape = null;
        if(shapeId!=null){
            shape = shapes.get(shapeId);
            if(shape==null) throw new GTFSParsingException("Missing shape : " + shapeId);
        }
        
        // get and check wheelchair allowed value
        Boolean wheelchairAllowed = null;
        if(wheelchair!=null){
            if(wheelchair.equals("1"))
                wheelchairAllowed = true;
            else if(wheelchair.equals("2"))
                wheelchairAllowed = false;
            else
                throw new GTFSParsingException("Bad wheelchair allowed value : "+wheelchair);
        }
        
        // get and check bikes allowed value
        Boolean bikesAllowed = null;
        if(bikes!=null){
            if(bikes.equals("1"))
                bikesAllowed = true;
            else if(bikes.equals("2"))
                bikesAllowed = false;
            else
                throw new GTFSParsingException("Bad bikes allowed value : "+bikes);        
        }
        
        // get and check direction value
        int direction = -1;
        if(directionString!=null){
            try{
                direction = Integer.parseInt(directionString);
            } catch(NumberFormatException e){
                throw new GTFSParsingException("Bad direction value : "+directionString);
            }
            if(!Trip.isValidDirection(direction))
                throw new GTFSParsingException("Bad direction value : "+direction);
        }
        
        // create and add the trip
        Trip trip = new Trip(route, calendar, id, headSign, shortName, direction, wheelchairAllowed, bikesAllowed, shape);
        result.add(trip);
    }

    @Override
    public int numberOfParameters() {
        return 10;
    }
    
}
