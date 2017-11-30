package gtfs.entities;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.time.Month;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * A service availability schedule.
 * 
 * @see <a href="https://developers.google.com/transit/gtfs/reference/#calendartxt">GTFS Overview - Calendar</a>
 * @see <a href="https://developers.google.com/transit/gtfs/reference/#calendar_datestxt">GTFS Overview - Calendar Dates</a>
 */
@Entity
@Table(name="calendars", schema="gtfs")
public class Calendar extends GTFS{
    /**
     * The maximum date supported by the class, that is 2999-12-01 (YYYY-MM-DD).
     */
    public static final LocalDate MAX_DATE = LocalDate.of(2999, Month.DECEMBER, Month.DECEMBER.maxLength());
    /**
     * The minimum date supported by the class, that is the 00:00:00 UTC Thursday 1, January 1970 (Unix epoch).
     */
    public static final LocalDate MIN_DATE = LocalDate.of(1970, Month.JANUARY, 1);
    private String id;
    private Map<DayOfWeek,Boolean> serviceDays;
    private LocalDate startDate;
    private LocalDate endDate;
    private Map<LocalDate, Boolean> exceptions;
    
    public Calendar(){
        startDate = MIN_DATE;
        endDate = MAX_DATE;
    }
    
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
     * @param id the id of the schedule. Must be not null.
     * @param startDate the first date of the service. Null is replaced with {@link #MIN_DATE}.
     * @param endDate the last date of the service. Null is replaced with {@link #MAX_DATE}.
     * @throws IllegalArgumentException if id is null or if the start date comes after the end date.
     */
    public Calendar(String id, LocalDate startDate, LocalDate endDate){
        if(id==null) throw new IllegalArgumentException("Id must be not null");
        if(startDate==null) startDate = MIN_DATE;
        if(endDate==null) endDate = MIN_DATE;
        
        if(startDate.isAfter(endDate))
            throw new IllegalArgumentException("Start date comes after the end date");
        
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;

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

    @Transient
    public Map<DayOfWeek, Boolean> getServiceDays() {
        if(serviceDays == null)
            setServiceDays(new EnumMap<>(DayOfWeek.class));
        return Collections.unmodifiableMap(serviceDays);
    }
    
    public boolean getMonday(){
        return serviceDays.get(DayOfWeek.MONDAY);
    }
    
    public void setMonday(boolean active){
        serviceDays.put(DayOfWeek.MONDAY, active);
    }
    
    public boolean getTuesday(){
        return serviceDays.get(DayOfWeek.TUESDAY);
    }
    
    public void setTuesday(boolean active){
        serviceDays.put(DayOfWeek.TUESDAY, active);
    }
    
    public boolean getWednesday(){
        return serviceDays.get(DayOfWeek.WEDNESDAY);
    }
    
    public void setWednesday(boolean active){
        serviceDays.put(DayOfWeek.WEDNESDAY, active);
    }
    
    public boolean getThursday(){
        return serviceDays.get(DayOfWeek.THURSDAY);
    }
    
    public void setThursday(boolean active){
        serviceDays.put(DayOfWeek.THURSDAY, active);
    }
    
    public boolean getFriday(){
        return serviceDays.get(DayOfWeek.FRIDAY);
    }
    
    public void setFriday(boolean active){
        serviceDays.put(DayOfWeek.FRIDAY, active);
    }
    
    public boolean getSaturday(){
        return serviceDays.get(DayOfWeek.SATURDAY);
    }
    
    public void setSaturday(boolean active){
        serviceDays.put(DayOfWeek.SATURDAY, active);
    }

    public boolean getSunday(){
        return serviceDays.get(DayOfWeek.SUNDAY);
    }
    
    public void setSunday(boolean active){
        serviceDays.put(DayOfWeek.SUNDAY, active);
    }
    
    public boolean isActive(DayOfWeek day){
        return serviceDays.get(day);
    }
    
    /**
     * 
     * @return  a read-only map containing specific dates of activity or inactivity.
     */
    @ElementCollection
    @CollectionTable(name="calendar_exceptions", schema="gtfs", catalog="postgis_test")
    @MapKeyJoinColumn(name="calendar")
    public Map<LocalDate, Boolean> getExceptions(){
        if(exceptions==null)
            setExceptions(new HashMap<>());
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
            setExceptions(new HashMap<>());
        return exceptions.put(date, active);
    }
    
    @Id
    public String getId(){
        return id;
    } 
    
    @Override
    public boolean equals(Object o){
        if(this==o) return true;
        if(!(o instanceof Calendar)) return false;
        Calendar c = (Calendar) o;
        return id.equals(c.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    /**
     * 
     * @return the start date. Null can be considered {@link LocalDate#MIN}.
     */
    public LocalDate getStartDate() {
        return startDate;
    }

    /**
     * 
     * @return the end date. Null can be considered {@link LocalDate#MAX}.
     */
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setServiceDays(Map<DayOfWeek,Boolean> serviceDays) {
        this.serviceDays = serviceDays;
    }

    /**
     * 
     * @param startDate the start date to set. Null is replaced with {@link #MIN_DATE}.
     * @return true if the start date isn't after the end date and it has been set, false otherwise. 
     */
    public boolean setStartDate(LocalDate startDate) {
        if(startDate==null) startDate = MIN_DATE;
        if(startDate.isAfter(endDate))
            return false;
        this.startDate = startDate;
        return true;
    }

    /**
     * 
     * @param endDate the end date to set. Null is replaced with {@link #MAX_DATE}.
     * @return true if the end date isn't before the start date and it has been set, false otherwise.
     */
    public boolean setEndDate(LocalDate endDate) {
        if(endDate==null) endDate = MAX_DATE;
        if(endDate.isBefore(startDate))
            return false;
        this.endDate = endDate;
        return true;
    }

    public void setExceptions(Map<LocalDate, Boolean> exceptions) {
        this.exceptions = exceptions;
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
