package gtfs;

import java.util.Collection;
import gtfs.entities.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

/**
 * A GTFS feed.
 */
public class Feed {
    private Collection<Agency> agencies;
    private Collection<Route> routes;
    private Collection<Trip> trips;
    private Collection<Stop> stops;
    private Collection<Shape> shapes;
    private Collection<StopTime> stopTimes;
    private Collection<Calendar> calendars;
    private Collection<Frequency> frequencies;
    
    public void setAgencies(Collection<Agency> agencies) {
        this.agencies = agencies;
    }

    public void setRoutes(Collection<Route> routes) {
        this.routes = routes;
    }

    public void setTrips(Collection<Trip> trips) {
        this.trips = trips;
    }

    public void setStops(Collection<Stop> stops) {
        this.stops = stops;
    }

    public void setStopTimes(Collection<StopTime> stopTimes) {
        this.stopTimes = stopTimes;
    }

    public void setCalendars(Collection<Calendar> calendars) {
        this.calendars = calendars;
    }
    
    public void setShapes(Collection<Shape> shapes){
        this.shapes = shapes;
    }
    
    public void setFrequencies(Collection<Frequency> frequencies) {
        this.frequencies = frequencies;
    }
    
    public Collection<Shape> getShapes(){
        return shapes;
    }

    public Collection<Agency> getAgencies() {
        return agencies;
    }

    /**
     * 
     * @return a read-only view of all the routes of the feed.
     */
    public Collection<Route> getRoutes(){
        return Collections.unmodifiableCollection(routes);
    }
    
    /**
     * 
     * @param type the type of the route, if -1 return all the routes.
     * @return  a read-only view of the routes with the specified type.
     */
    public Collection<Route> getRoutes(int type) {
        ArrayList<Route> routes = new ArrayList<>(this.routes.size());
        for(Route r : this.routes){
            if(type==-1 || r.getType()!=type) continue;
            routes.add(r);
        }
        return Collections.unmodifiableCollection(routes);
    }

    /**
     * 
     * @return a read-only view of all the trips.
     */
    public Collection<Trip> getTrips() {
        return Collections.unmodifiableCollection(trips);
    }
    
    /**
     * 
     * @return a read-only view of the stops in the feed.
     */
    public Collection<Stop> getStops() {
        return Collections.unmodifiableCollection(stops);
    }
    
    /**
     * 
     * @return a read-only view of the stop times in the feed.
     */
    public Collection<StopTime> getStopTimes() {
        return Collections.unmodifiableCollection(stopTimes);
    }

    /**
     * 
     * @return a read-only view of the calendars of the feed.
     */
    public Collection<Calendar> getCalendars() {
        return calendars;
    }
    
    /**
     * 
     * @param id The id of the route. Null returns null.
     * @return the route with the specified id if found, null otherwise.
     */
    public Route getRouteById(String id){
        if(routes==null) return null;
        for(Route r : routes)
            if(id.equals(r.getId()))
                return r;
        return null;
    }
    
    /**
     * Finds a route by short name.
     * If a route does not have a short name, it is used its long name.
     * 
     * @param name The name of the route. Null returns null.
     * @return the route in the feed with the specified name, null if not found.
     */
    public Route getRouteByName(String name){
        if(routes==null) return null;
        for(Route r : routes)
            if(name.equals(r.getName()))
                return r;
        return null;
    }
    
    /**
     * @param id  the id of the route.
     * @return  a read-only view of the trips associated with the route.
     */
    public Collection<Trip> getTripsByRoute(String id){
        LinkedList<Trip> routeTrips = new LinkedList<>();
        for(Trip t : trips)
            if(id.equals(t.getRoute().getId()))
                routeTrips.add(t);
        return Collections.unmodifiableCollection(routeTrips);
    }
    
    /**
     * @param route the route.
     * @return  a read-only view of the trips associated with the route.
     */
    public Collection<Trip> getTripsByRoute(Route route){
        LinkedList<Trip> routeTrips = new LinkedList<>();
        for(Trip t : trips)
            if(route.equals(t.getRoute()))
                routeTrips.add(t);
        return routeTrips;
    }
    
    /**
     * 
     * @param id The id of the agency. Null is replaced with "".
     * @return the agency in the feed with the specified id if found, null otherwise.
     */
    public Agency getAgencyById(String id){
        if(id==null) id = "";
        for(Agency a : agencies)
            if(id.equals(a.getId()))
                return a;
        return null;
    }
    
    /**
     * 
     * @param name The name of the agency. Null returns null.
     * @return  the agency in the feed with the specified name if found, null otherwise.
     */
    public Agency getAgencyByName(String name){
        if(name==null) return null;
        for(Agency a : agencies)
            if(name.equals(a.getId()))
                return a;
        return null;
    }
    
    /**
     * 
     * @param id The id of the stop. Null returns null.
     * @return the stop in the feed with the specified id if found, null otherwise.
     */
    public Stop getStop(String id){
        if(id==null) return null;
        for(Stop s : stops)
            if(id.equals(s.getId()))
                return s;
        return null;
    }
    
    /**
     * 
     * @param lat The WGS84 latitude of the stop. If it is not a valid WGS84 value returns null.
     * @param lon The WGS84 longitude of the stop. If it is not a valid WGS84 value returns null.
     * @return  the stop with the specified coordinates if found, null otherwise.
     */
    public Stop getStop(double lat, double lon){
        if(lat<-90. || lat>90.)
            return null;
        if(lon<-180. || lon>180.)
            return null;
        for(Stop s : stops)
            if(s.getLat()==lat && s.getLon()==lon)
                return s;
        return null;
    }
    
    /**
     * 
     * @return a read-only view of the frequencies of the feed.
     */
    public Collection<Frequency> getFrequencies(){
        return Collections.unmodifiableCollection(frequencies);
    }
}
