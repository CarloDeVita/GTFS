package gtfs.entities;

import java.util.Comparator;

/**
 * The time a vehicle arrives and departs from a stop.
 * 
 * @see <a href="https://developers.google.com/transit/gtfs/reference/#stop_timestxt">GTFS Overview - Stop Time</a>
 */
public class StopTime extends GTFS{
    /**
     * The trip of the stop time.
     */
    private Trip trip;
    /**
     * The stop the vehicle stops in.
     */
    private Stop stop;
    /**
     * The arrival time of the vehicle at the stop.
     */
    private String arrival;
    /**
     * The departure time of the veichle from the stop.
     */
    private String departure;
    /**
     * The sequence number of the stop for the trip.
     */
    private int sequence;
    private String headSign;
    private int pickupType;
    private int dropoffType;
    private Double shapeDistTraveled;
    /**
     * Tells wether the departure time is strictly adhered to by the vehicle.
     */
    private boolean timepoint;
    
    /**
     * Compares two time as represented in a Stop Time.
     */
    public static final Comparator<String> TIME_COMPARATOR = (String o1, String o2) -> {
        if(o1.indexOf(':')==1) o1 = "0"+o1;
        if(o2.indexOf(':')==1) o2 = "0"+o2;
        return o1.compareTo(o2);
    };
    
    /**
     * Compares two StopTimes by their arrival time.
     */
    public static final Comparator<StopTime> ARRIVAL_COMPARATOR = (StopTime o1, StopTime o2) -> TIME_COMPARATOR.compare(o1.arrival, o2.arrival);
    
    /**
     * Compares two StopTimes by their departure time.
     */
    public static final Comparator<StopTime> DEPARTURE_COMPARATOR = (StopTime o1, StopTime o2) -> TIME_COMPARATOR.compare(o1.departure, o2.departure);
    
    /**
     * Compares two StopTimes by their sequence number.
     */
    public static final Comparator<StopTime> SEQUENCE_COMPARATOR = (StopTime o1, StopTime o2) -> Integer.compare(o1.sequence, o2.sequence);
    
    /**
     * Checks if a String is a valid value for arrival and departure time fields.
     * 
     * @param time The time string to check. Null returns false.
     * @return true if time is a time in the format HH:MM:SS or H:MM:SS, false otherwise. HH may also exceed 24 hours as allowed from the GTFS specifications.
     */
    public static boolean isValidTime(String time){
        return time!=null && time.matches("^[0-9]{1,2}:[0-5][0-9]:[0-5][0-9]$");
    }
    
    /**
     * Checks if an int is a valid StopTime pickup type value.
     * 
     * @param type
     * @return true if the parameter is between 0 and 3, false otherwise.
     */
    public static boolean isValidPickupType(int type){
        return (type>=0 && type<=3);
    }
    
     /**
     * Checks if an int is a valid StopTime dropoff type value.
     * 
     * @param type
     * @return true if the parameter is between 0 and 3, false otherwise.
     */
    public static boolean isValidDropoffType(int type){
        return (type>=0 && type<=3);
    }
    
    /**
     * 
     * @param trip The trip of the stop time. Must be not null.
     * @param stop The stop the vehicle stops in. Must be not null.
     * @param sequence The sequence number of the stop for the trip. Must be a non negative number.
     * @param arrival The arrival time of the vehicle at the stop. It can be null only if timepoint parameter is false.
     * @param departure The departure time of the veichle from the stop. It can be null only if timepoint parameter is false. If not null it must come after the arrival time.
     * @param timepoint Tells wether the departure time is strictly adhered to by the vehicle. If it is true arrival and departure parameters must be not null.
     */
    public StopTime(Trip trip, Stop stop, int sequence, String arrival, String departure, boolean timepoint){
        if(timepoint && (arrival==null || departure==null))
            throw new IllegalArgumentException("Timepoint without specified arrival or departure time");
        if(arrival!=null && !isValidTime(arrival))
            throw new IllegalArgumentException("Bad arrival time value");
        if(departure!=null &&!isValidTime(departure))
            throw new IllegalArgumentException("Bad departure time value");
        if(arrival!=null && departure!=null)
            if(TIME_COMPARATOR.compare(arrival, departure)>0)
                throw new IllegalArgumentException("Departure time must come after arrival time");
        if(sequence<0)
            throw new IllegalArgumentException("Sequence value must non negative");
        this.trip = trip;
        this.stop = stop;
        this.sequence = sequence;
        this.arrival = arrival;
        this.departure = departure;
        this.timepoint = timepoint;
        trip.addStopTime(this);
    }
    
    /**
     * The StopTime constructor with all the parameters except the distance traveled.
     * 
     * @param trip The trip of the stop time. Must be not null.
     * @param stop The stop the vehicle stops in. Must be not null.
     * @param sequence The sequence number of the stop for the trip. Must be a non negative number.
     * @param arrival The arrival time of the vehicle at the stop. It can be null only if timepoint parameter is false.
     * @param departure The departure time of the veichle from the stop. It can be null only if timepooint parameter is false.
     * @param timepoint Tells wether the departure time is strictly adhered to by the vehicle. If it is true arrival and departure parameters must be not null.
     * @param headSign
     * @param pickup
     * @param dropoff
     */
    public StopTime(Trip trip, Stop stop, int sequence, String arrival, String departure, String headSign, int pickup, int dropoff, boolean timepoint){
        this(trip, stop, sequence, arrival, departure, headSign, pickup, dropoff, timepoint, null);
    }
    
    /**
     * The StopTime constructor with all possible parameters.
     * 
     * @param trip The trip of the stop time. Must be not null.
     * @param stop The stop the vehicle stops in. Must be not null.
     * @param sequence The sequence number of the stop for the trip. Must be a non negative number.
     * @param arrival The arrival time of the vehicle at the stop. It can be null only if timepoint parameter is false.
     * @param departure The departure time of the veichle from the stop. It can be null only if timepooint parameter is false.
     * @param timepoint Tells wether the departure time is strictly adhered to by the vehicle. If it is true arrival and departure parameters must be not null.
     * @param headSign
     * @param pickup
     * @param dropoff
     * @param shapeDistTraveled
     */
    public StopTime(Trip trip, Stop stop, int sequence, String arrival, String departure, String headSign, int pickup, int dropoff, boolean timepoint, Double shapeDistTraveled){
        this(trip, stop, sequence, arrival, departure, timepoint);
        this.headSign = headSign;
        this.pickupType = pickup;
        this.dropoffType = dropoff;
        this.shapeDistTraveled = shapeDistTraveled;
    }

    public String getArrival() {
        return arrival;
    }

    public String getDeparture() {
        return departure;
    }

    public int getSequence() {
        return sequence;
    }

    public Double getShapeDistTraveled() {
        return shapeDistTraveled;
    }

    public Trip getTrip() {
        return trip;
    }

    public Stop getStop() {
        return stop;
    }

    public String getHeadSign() {
        return headSign;
    }

    public int getPickupType() {
        return pickupType;
    }

    public int getDropoffType() {
        return dropoffType;
    }

    public boolean isTimepoint() {
        return timepoint;
    }

    /**
     * Sets the trip of the stop time.
     * 
     * @param trip
     * @return true if the trip is not null, false otherwise.
     */
    public boolean setTrip(Trip trip) {
        if(trip==null) return false;
        this.trip = trip; //TODO trip binding?
        return true;
    }

    /**
     * Sets the stop.
     * 
     * @param stop
     * @return true if the stop is not null, false otherwise.
     */
    public boolean setStop(Stop stop) {
        if(stop==null) return false;
        this.stop = stop;
        return true;
    }

    /**
     * Sets the arrival and departure times of the vehicle.
     * 
     * @param arrival The arrival time of the vehicle. If null it is replaced with the current arrival time of the instance.
     * @param departure The departure time of the vehicle. If null it is replaced with the current departure time of the instance. Departure time must come after the arrival time.
     * @return true if the parameters respect the specifications, false otherwise.
     */
    public boolean setTimes(String arrival, String departure){
        if(arrival==null) arrival = this.arrival;
        if(departure==null) departure = this.departure;
        if(!isValidTime(arrival) || !isValidTime(departure)) return false;
        if(TIME_COMPARATOR.compare(arrival, departure)>0) return false;
        this.arrival = arrival;
        this.departure = departure;
        return true;
    }

    /**
     * Same as {@link #setTimepoint(boolean, boolean)} with false as second parameter.
     */
    public boolean setTimepoint(boolean timepoint) {
        return setTimepoint(timepoint, false);
    }
    
    /**
     * Tells wether the arrival and departure time must be considered strictly adhered to by the vehicle.
     * 
     * @param timepoint If it is true, arrival and departure times must be not null, otherwise false is returned.
     * @param removeTimes Tells if the arrival and departure times must be set to null. Ignored if timepoint value is true.
     * @return false if timepoint is true and arrival or departure time is null, true otherwise.
     */
    public boolean setTimepoint(boolean timepoint, boolean removeTimes){
        if(timepoint && (arrival==null || departure==null)) return false;
        this.timepoint = timepoint;
        if(!timepoint && removeTimes){
            this.arrival = null;
            this.departure = null;
        }
        return true;
    }

    /**
     * Sets the sequence number of the stop for the trip.
     * 
     * @param sequence
     * @return true if the sequence number is set correctly, false if it is a negative number.
     */
    public boolean setSequence(int sequence) {
        if(sequence<0) return false;
        this.sequence = sequence;
        return true;
    }
    
    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("TripId = ");
        builder.append(trip.getId());
        builder.append(", StopId = ");
        builder.append(stop.getId());
        builder.append(", Sequence = ");
        builder.append(sequence);
        if(arrival!=null){
            builder.append(", Arrival = ");
            builder.append(arrival);
            builder.append(", Departure = ");
            builder.append(departure);
        }
        builder.append(", Timepoint ? ");
        builder.append(timepoint);
        return builder.toString();
    }
}
