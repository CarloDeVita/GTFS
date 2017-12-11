package gtfs.entities;

import com.vividsolutions.jts.geom.Point;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.hibernate.annotations.SortComparator;

/**
 * The path performed by a vehicle.
 * Contains information about the sequence of stops and 
 * information about the service.
 * 
 * @see <a href="https://developers.google.com/transit/gtfs/reference/#tripstxt">GTFS Overview - Trip</a>
 */
@Entity
@Table(name="trips", schema="gtfs")
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
    private byte direction = -1;
    private Shape shape;
    private Boolean wheelchairAccessible;
    private Boolean bikesAllowed;
    private SortedSet<StopTime> stopTimes;
    private SortedSet<Frequency> frequencies;
    
    /**
     * 
     * @param direction The direction of the trip.
     * @return true if direction is in the range of valid values [0..1], false otherwise.
     */
    public static boolean isValidDirection(int direction){
        return (direction>=-1 && direction<=1);
    }
    
    public Trip(){}
    
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
     * @param direction The direction of the trip. The value must be valid according to {@link #isValidDirection(int)}.
     * @param whAccessible
     * @param bikesAllowed
     * @param shape 
     */
    public Trip(Route route, Calendar calendar, String id, String headSign, String shortName, int direction, Boolean whAccessible, Boolean bikesAllowed, Shape shape){
        this(route, calendar, id);
        if(!isValidDirection(direction))
            throw new IllegalArgumentException("Direction can only have values (-1, 0, 1)");
        this.headSign = headSign;
        this.shortName = shortName;
        this.direction = (byte) direction;
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

    public void setRoute(Route route) {
        this.route = route;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setHeadSign(String headSign) {
        this.headSign = headSign;
    }

    /**
     * 
     * @param direction
     * @return true if the direction is valid according to {@link #isValidDirection(int)}, false otherwise .
     */
    public boolean setDirection(int direction) {
        if(!isValidDirection(direction)) return false;
        this.direction = (byte)direction;
        return true;
    }

    public void setWheelchairAccessible(Boolean wheelchairAccessible) {
        this.wheelchairAccessible = wheelchairAccessible;
    }

    public void setStopTimes(SortedSet<StopTime> stopTimes) {
        this.stopTimes = stopTimes;
    }

    public void setFrequencies(SortedSet<Frequency> frequencies) {
        this.frequencies = frequencies;
    }
    
    /**
     * 
     * @return a read-only ordered view of the points that form the trip path.
     * If the trip has been associated with a shape, then the shape points are returned,
     * otherwise the points used are the stops.
     */
    @Transient
    public Iterable<Point> getPoints(){
        //TODO interleave shape points and stops
         return new Iterable<Point>(){
            @Override
            public Iterator<Point> iterator() {
                return new Iterator<Point>(){
                    Iterator<Shape.Point> shapePoints;
                    Iterator<StopTime> stops;
                    {
                        if(shape!=null)
                            shapePoints = shape.getPoints().iterator();
                        else if(stopTimes!=null)
                            stops = stopTimes.iterator();
                    }

                    @Override
                    public boolean hasNext() {
                        if(shapePoints==null && stops==null) return false;
                        if(shapePoints!=null) return shapePoints.hasNext();
                        else return stops.hasNext();
                    }

                    @Override
                    public Point next() {
                        if(!hasNext()) throw new NoSuchElementException();
                        if(shapePoints!=null) return shapePoints.next().getCoordinate();
                        return stops.next().getStop().getCoordinate();
                    }
                };
            }
        };
    }
    
    @Override
    public boolean equals(Object o){
        if(this==o) return true;
        if(!(o instanceof Trip)) return false;
        Trip t = (Trip) o;
        return id.equals(t.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Id
    public String getId(){
        return id;
    }
       
    @ManyToOne(optional=true)
    @JoinColumn(name="shape", nullable=true)
    public Shape getShape(){
        return shape;
    }
    
    @ManyToOne(optional=false)
    @JoinColumn(name="route", nullable=false)
    public Route getRoute(){
        return route;
    }

    @ManyToOne(optional=false)
    @JoinColumn(name="calendar", nullable=false)
    public Calendar getCalendar() {
        return calendar;
    }

    /**
     * 
     * @return a read-only view of the frequencies of the trip.
     */
    @OneToMany(targetEntity=Frequency.class, mappedBy="trip")
    @SortComparator(Frequency.StartComparator.class)
    public SortedSet<Frequency> getFrequencies(){
        if(frequencies==null)
            setFrequencies(new TreeSet<>(new Frequency.StartComparator()));
        return Collections.unmodifiableSortedSet(frequencies);
    }
    
    /**
     * 
     * @return a read-only view of the stop times of the trip.
     */
    /*@OneToMany(targetEntity=StopTime.class, mappedBy="trip")
    @SortComparator(StopTime.ArrivalComparator.class)*/
    @Transient
    public SortedSet<StopTime> getStopTimes(){
        if(stopTimes==null)
            setStopTimes(new TreeSet<>(StopTime.SEQUENCE_COMPARATOR));
        return Collections.unmodifiableSortedSet(stopTimes);
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

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public void setBikesAllowed(Boolean bikesAllowed) {
        this.bikesAllowed = bikesAllowed;
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
     * Same as {@link #addStopTime(gtfs.entities.StopTime, boolean)} with append parameter set to false.
     * 
     * @param stopTime The stop time to add. Must be not null. 
     * @return true if the insert has been successful, false otherwise.
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
            setStopTimes(new TreeSet<>(StopTime.SEQUENCE_COMPARATOR));

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
            setFrequencies(new TreeSet<>(new Frequency.StartComparator()));
        return frequencies.add(frequency);
    }
}
