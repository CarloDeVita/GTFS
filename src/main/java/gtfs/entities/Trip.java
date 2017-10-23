package gtfs.entities;

import gtfs.entities.Shape.Point;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

/**
 * A sequence stops.
 * 
 * @see <a href="https://developers.google.com/transit/gtfs/reference/#tripstxt">GTFS Overview - Trip</a>
 */
public class Trip extends GTFS{
    /**
     * The route the trip belongs to.
     */
    private Route route; //required
    /**
     * The service schedule of the trip.
     */
    private Calendar calendar; //required
    /**
     * The id of the trip.
     */
    private String id; //required
    private String headSign;
    private String shortName;
    private int direction = -1;
    private Shape shape;
    private Boolean wheelchairAccessible;
    private Boolean bikesAllowed;
    private TreeSet<StopTime> stopTimes;
    private TreeSet<Frequency> frequencies;
    
    /**
     * The Trip constructor with all the required fields.
     * 
     * @param route The route the trip belongs to. Must be not null;
     * @param calendar The service schedule of the trip. Must be not null.
     * @param id The id of the trip. Must be not null.
     */
    public Trip(Route route, Calendar calendar ,String id){
        if(route==null || calendar==null || id==null)
            throw new IllegalArgumentException("Arguments must be not null");
        this.route = route;
        this.calendar = calendar;
        this.id = id;
        route.addTrip(this);
    }
    
    /**
     * The Trip constructor with all possible parameters.
     * 
     * @param route The route the trip belongs to. Must be not null;
     * @param calendar The service schedule of the trip. Must be not null.
     * @param id The id of the trip. Must be not null.
     * @param headSign
     * @param shortName
     * @param direction The direction of the trip. The only values admitted are (-1, 0, 1).
     * @param whAccessible
     * @param bikesAllowed
     * @param shape 
     */
    public Trip(Route route, Calendar calendar, String id, String headSign, String shortName, int direction, Boolean whAccessible, Boolean bikesAllowed, Shape shape){
        this(route, calendar, id);
        if(direction<-1 || direction>1)
            throw new IllegalArgumentException("Direction can only have values (-1, 0, 1)");
        this.headSign = headSign;
        this.shortName = shortName;
        this.direction = direction;
        this.wheelchairAccessible = whAccessible;
        this.bikesAllowed = bikesAllowed;
        this.shape = shape;
    }
    
    /**
     * The Trip constructor without shape.
     * 
     * @param route
     * @param calendar
     * @param id
     * @param headSign
     * @param shortName
     * @param direction
     * @param whAccessible
     * @param bikesAllowed 
     */
    public Trip(Route route, Calendar calendar, String id, String headSign, String shortName, int direction, Boolean whAccessible, Boolean bikesAllowed){
        this(route, calendar, id, headSign, shortName, direction, whAccessible, bikesAllowed, null);
    }
    
    
    /**
     * 
     * @return a read-only ordered view of all the points that form the trip path.
     */
    public Collection<Point> getPoints(){
        //TODO points in order!!
        //TODO missing shape?
        return Collections.unmodifiableCollection(shape.getPoints());
    }
    
    @Override
    public boolean equals(Object o){
        if(!(o instanceof Trip)) return false;
        Trip t = (Trip) o;
        return id.equals(t.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    public String getId(){
        return id;
    }
    
    public Route getRoute(){
        return route;
    }

    public Calendar getCalendar() {
        return calendar;
    }

    public String getHeadSign() {
        return headSign;
    }

    public String getShortName() {
        return shortName;
    }

    public int getDirection() {
        return direction;
    }

    public Shape getShape() {
        return shape;
    }

    /**
     * 
     * @return wether the vehicle can accomodate at least a rider in wheelchair, null if the information is missing. 
     */
    public Boolean getWheelchairAccessible() {
        return wheelchairAccessible;
    }

    /**
     * 
     * @return wether the vehicle can accomodate a bicycle, null if the information is missing.
     */
    public Boolean getBikesAllowed() {
        return bikesAllowed;
    }
   
    /**
     * Sets the shape for the trip.
     * @param shape
     * @return the old shape associated with the trip.
     */
    public Shape setShape(Shape shape){
        Shape oldShape = this.shape;
        this.shape = shape;
        return oldShape;
    }
    
    /**
     * Sets the service schedule for the trip.
     * @param calendar
     * @return the old schedule associated with the trip.
     */
    public Calendar setCalendar(Calendar calendar){
        if(calendar==null) throw new IllegalArgumentException("Calendar can't be null");
        Calendar oldCalendar = this.calendar;
        this.calendar = calendar;
        return oldCalendar;
    }
    
    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("TripId = ");
        builder.append(id);
        builder.append(", ServiceId = ");
        builder.append(calendar.getId());
        
        return builder.toString();
    }
    
    /**
     * 
     * @return a read-only view of the stop times of the trip.
     */
    public Collection<StopTime> getStopTimes(){
        return Collections.unmodifiableCollection(stopTimes);
    }
    
    /**
     * Same as {@link #addStopTime(gtfs.entities.StopTime, boolean)} with append parameter set to false.
     */
    public boolean addStopTime(StopTime stopTime) {
        return addStopTime(stopTime, false);
    }
    
    /**
     * Adds a stop time.
     * 
     * @param stopTime The stop time to add. Must be not null.
     * @param append Specifies wether the stop time should be added at the end of the list or not.
     * @return  false if the stop time could not be added (according to its sequence number), false otherwise.
     */
    public boolean addStopTime(StopTime stopTime, boolean append){
        if(stopTimes==null)
            stopTimes = new TreeSet<>(StopTime.SEQUENCE_COMPARATOR);

        if(append){
            StopTime last = null;
            if(!stopTimes.isEmpty())
                last = stopTimes.last();
            if(last!=null && StopTime.SEQUENCE_COMPARATOR.compare(last, stopTime)<0)
                    return false;
        }
        
        return stopTimes.add(stopTime);
    }
    
    /**
     * Adds a frequency to the trip.
     * 
     * @param frequency The frequency to add. Must be not null. 
     * @return  false if there is already a frequency with the same start time, true otherwise.
     */
    public boolean addFrequency(Frequency frequency) {
        if(frequencies==null)
            frequencies = new TreeSet<>((Frequency o1, Frequency o2) -> StopTime.TIME_COMPARATOR.compare(o1.getStartTime(), o2.getStartTime()));
        return frequencies.add(frequency);
    }
}
