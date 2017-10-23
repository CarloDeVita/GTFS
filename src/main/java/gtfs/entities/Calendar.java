package gtfs.entities;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * A service availability schedule.
 * 
 * @see <a href="https://developers.google.com/transit/gtfs/reference/#calendartxt">GTFS Overview - Calendar</a>
 * @see <a href="https://developers.google.com/transit/gtfs/reference/#calendar_datestxt">GTFS Overview - Calendar Dates</a>
 */
public class Calendar extends GTFS{
    private String id;
    private Map<DayOfWeek,Boolean> serviceDays;
    private LocalDate startDate;
    private LocalDate endDate;
    private Map<LocalDate, Boolean> exceptions;
    
    /**
     * The constructor of a calendar with the id parameter only.
     * Service will be set inactive for all the days of the week.
     * The start date and the end date will be respectively the minimum and the maximum supported LocalDate.
     * 
     * @param id the id of the service. Must be not null.
     */
    public Calendar(String id){
        this(id, null, null);
    }
    
    /**
     * A calendar constructor without the service days.
     * Service will be set inactive for all the days of the week.
     * 
     * @param id
     * @param startDate the first date of the service. Null is replaced with the minimum date supported.
     * @param endDate the last date of the service. Null is replaced with the maximum date supported.
     * @throws IllegalArgumentException if id is null or if the start date comes after the end date.
     */
    public Calendar(String id, LocalDate startDate, LocalDate endDate){
        if(id==null) throw new IllegalArgumentException("Id must be not null");
        this.id = id;
        this.startDate = (startDate!=null ? startDate : LocalDate.MIN);
        this.endDate = (endDate!=null ? endDate : LocalDate.MAX);
        
        if(this.endDate.compareTo(this.startDate)<0)
            throw new IllegalArgumentException("Start date comes after the end date");
        
        this.serviceDays = new EnumMap<>(DayOfWeek.class);
        for(DayOfWeek d : DayOfWeek.values())
           this.serviceDays.put(d, Boolean.FALSE);
        
    }
    
    /**
     * A Calendar constructor with the service days.
     * Acts the same as {@link #Calendar(java.lang.String, java.time.LocalDate, java.time.LocalDate)} but service days are specified.
     * 
     * @param id the id of the service. Must be not null.
     * @param startDate
     * @param endDate
     * @param serviceDays  the service activity status for the days of the week. The service will be considered inactive in the days of the map that miss. Must be not null.
     */
    public Calendar(String id, LocalDate startDate, LocalDate endDate, Map<DayOfWeek,Boolean> serviceDays){
        this(id, startDate, endDate);
        if(serviceDays==null)
            throw new IllegalArgumentException("Service days must be not null");
        
        for(DayOfWeek d : DayOfWeek.values()){
            Boolean active = serviceDays.get(d);
            if(active!=null && active)
                this.serviceDays.put(d, active);
        }
    }

    public Map<DayOfWeek, Boolean> getServiceDays() {
        if(serviceDays == null)
            serviceDays = new EnumMap<>(DayOfWeek.class);
        return Collections.unmodifiableMap(serviceDays);
    }
    
    
    
    /**
     * 
     * @return  a read-only map containing specific dates of activity or inactivity.
     */
    public Map<LocalDate, Boolean> getExceptions(){
        if(exceptions==null)
            exceptions = new HashMap<>();
        return Collections.unmodifiableMap(exceptions);
    }
    
    /**
     * Specifies if the service is active in a certain date.
     * 
     * @param date the specified date. Must be not null.
     * @param active true if the service is active in the specified date, false if inactive.
     * @return the old value associated with the date if present, null otherwise.
     */
    public Boolean setDate(LocalDate date, boolean active){
        if(exceptions==null)
            exceptions = new HashMap<>();
        return exceptions.put(date, active);
    }
    
    public String getId(){
        return id;
    } 
    
    @Override
    public boolean equals(Object o){
        if(!(o instanceof Calendar)) return false;
        Calendar c = (Calendar) o;
        return id.equals(c.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
    
    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("ID : ");
        builder.append(id);
        for(DayOfWeek d : DayOfWeek.values()){
            builder.append(", ");
            builder.append(d.name());
            builder.append(" : ");
            builder.append(serviceDays.get(d));
        }
        if(startDate!=null){
            builder.append(", Start Date : ");
            builder.append(startDate);
        }
        if(endDate!=null){
            builder.append(", End Date : ");
            builder.append(endDate);
        }
        if(exceptions!=null)
            builder.append(exceptions.toString());
        
        return builder.toString();
    }
}
