package gtfs.entities;

import java.util.Comparator;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * The frequency of a departure time schedule for a trip.
 * 
 * @see <a href="https://developers.google.com/transit/gtfs/reference/#frequenciestxt">GTFS Overview - Frequencies</a>
 */
@Entity
@Table(name="frequencies", schema="gtfs")
public class Frequency extends GTFS implements java.io.Serializable{
    public static Comparator<Frequency> START_COMPARATOR = new StartComparator();
    public static Comparator<Frequency> END_COMPARATOR = new EndComparator();
    private int id;
    private Trip trip; // required
    /**
     * The time when the frequency starts to be considered.
     */
    private String startTime; // required
    /**
     * The time when the frequency ends to be considered.
     */
    private String endTime; // required
    /**
     * The seconds between departures.
     * The vehicle leaves every headwaySeconds starting from startTime.
     */
    private int headwaySeconds; // required
    
    //TODO javadoc
    private boolean exactTime = false;
    
    public Frequency(){}
    
    /**
     * The Frequency constructor with all the possible parameters.
     * 
     * @param trip the trip associated with frequency. Must be not null.
     * @param startTime  The time when the frequency starts to be considered. Must be not null and respect the format specified in {@link StopTime#isValidTime(java.lang.String)}.
     * @param endTime The time when the frequency ends to be considered. Must be not null, respect the format specified in {@link StopTime#isValidTime(java.lang.String)} and must come after the start time. 
     * @param headwaySec The seconds between departures. Must be a positive int.
     * @param extactTime 
     */
    public Frequency(Trip trip, String startTime, String endTime, int headwaySec, boolean extactTime){
        if(trip==null || headwaySec<=0)
            throw new IllegalArgumentException();
        if(!StopTime.isValidTime(startTime))
            throw new IllegalArgumentException("Invalid start time");
        if(!StopTime.isValidTime(endTime))
            throw new IllegalArgumentException("Invalid end time");
        if(StopTime.TIME_COMPARATOR.compare(startTime, endTime)>=0)
            throw new IllegalArgumentException("End time must come after end time");
        
        this.trip = trip;
        this.startTime = startTime;
        this.endTime = endTime;
        this.headwaySeconds = headwaySec;
        trip.addFrequency(this);
    }
    
    /**
     * The Frequency constructor with all the required values.
     * 
     * @param trip the trip associated with frequency. Must be not null.
     * @param startTime  The time when the frequency starts to be considered. Must be not null and respect the format specified in {@link StopTime#isValidTime(java.lang.String)}.
     * @param endTime The time when the frequency ends to be considered. Must be not null, respect the format specified in {@link StopTime#isValidTime(java.lang.String)} and must come after the start time. 
     * @param headwaySec The seconds between departures. Must be a positive int.
     */
    public Frequency(Trip trip, String startTime, String endTime, int headwaySec){
        this(trip, startTime, endTime, headwaySec, false);
    }

    /**
     * Sets the time the frequency starts to be considered.
     * 
     * @param startTime Must be not null.
     * @return false if the parameter does not respected the format specified in {@link StopTime#isValidTime(java.lang.String)} or if it comes after the end time, true otherwise.
     */
    public boolean setStartTime(String startTime) {
        if(!StopTime.isValidTime(startTime)) return false;
        if(StopTime.TIME_COMPARATOR.compare(startTime, endTime)>=0) return false;
        this.startTime = startTime;
        return true;
    }
    
    /**
     * Sets the time the frequency starts to be considered.
     * 
     * @param endTime  Must be not null.
     * @return false if the parameter does not respected the format specified in {@link StopTime#isValidTime(java.lang.String)} or if it comes before the end time, true otherwise.
     */
    public boolean setEndTime(String endTime) {
        if(!StopTime.isValidTime(endTime)) return false;
        if(StopTime.TIME_COMPARATOR.compare(startTime, endTime)>=0) return false;
        this.endTime = endTime;
        return true;
    }
    
    /**
     * Sets the seconds between each trip departure.
     * 
     * @param headwaySeconds
     * @return true if the parameter is a positive int, false otherwise.
     */
    public boolean setHeadwaySeconds(int headwaySeconds) {
        if(headwaySeconds<=0) return false;
        this.headwaySeconds = headwaySeconds;
        return true;
    }

    public void setExactTime(boolean exactTime) {
        this.exactTime = exactTime;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }
    
    public void setId(int id){
        this.id = id;
    }
    
    @Id
    @GeneratedValue
    public int getId(){
        return this.id;
    }
    
    @Id
    @ManyToOne(optional=false)
    @JoinColumn(name="trip", nullable=false)
    public Trip getTrip() {
        return trip;
    }

    @Id
    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public int getHeadwaySeconds() {
        return headwaySeconds;
    }

    @Column(name="exact_time")
    public boolean isExactTime() {
        return exactTime;
    }
    
    public static class StartComparator implements Comparator<Frequency>{
        @Override
        public int compare(Frequency o1, Frequency o2) {
            return StopTime.TIME_COMPARATOR.compare(o1.startTime, o2.startTime);
        }
    }
    
    public static class EndComparator implements Comparator<Frequency>{
        @Override
        public int compare(Frequency o1, Frequency o2) {
            return StopTime.TIME_COMPARATOR.compare(o1.endTime, o2.endTime);
        }
    }
    
    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("Trip id : ");
        builder.append(trip.getId());
        builder.append(", Frequency Start : ");
        builder.append(startTime);
        builder.append(", Frequency End : ");
        builder.append(endTime);
        builder.append(", Seconds between departures : ");
        builder.append(headwaySeconds);
        builder.append(",Is exact time ? ");
        builder.append(exactTime);
        return builder.toString();
    }
}
