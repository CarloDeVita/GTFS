package gtfs.entities;

import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * A group of Trips displayed to user as a single service.
 * 
 * @see <a href="https://developers.google.com/transit/gtfs/reference/#routestxt">GTFS Overview - Route</a>
 */
@Entity
@Table(name="routes", schema="gtfs",  catalog="postgis_test")
public class Route extends GTFS{
    private Agency agency;
    /**
     * The id of the route.
     */
    private String id; //required
    /**
     * The short name of the route, often an abstract identifier.
     */
    private String shortName; // required or longName
    /**
     * The full name of the route, generally more descriptive than the short name.
     */
    private String longName; // required or shortName
    private String description;
    /**
     * Describes the type of transportation used on the route.
     * It must be a value between 0 and 7, as indicated in GTFS specifications.
     */
    private int type; //required
    private URL url;
    /**
     * 
     */
    private String color = "FFFFFF";
    private String textColor = "000000";
    private Set<Trip> trips;
    
    /**
     *
     * @param c the string to check, null returns false.
     * @return true if the string is a six-character hexadecimal number, false otherwise.
     */
    public static boolean checkColor(String c){
        return (c!=null && c.matches("^[0-9A-F]{6}$"));
    }
    
    /**
     * A route constructor with the required fields only.
     * 
     * @param agency The agency the route belongs to. Must be not null.
     * @param id The id of the route. Must be not null.
     * @param type The type of the transportations used on the route. Must be a value between 0 and 7.
     * @param shortName The short name of the route. It can be null only if longName parameter is not.
     * @param longName The long name of the route. It can be null onlt if shortName parameter is not.
     */
    public Route(Agency agency, String id, int type, String shortName, String longName){
        if(agency==null)
            throw new IllegalArgumentException("Agency must be not null");
        if(id==null)
            throw new IllegalArgumentException("Id must be not null");
        if(type<0 || type>7)
            throw new IllegalArgumentException("Type must be between 0 and 7");
        if(shortName==null && longName==null)
            throw new IllegalArgumentException("One of shortName or longName must be not null");
        this.id = id;
        this.type = type;
        this.shortName = shortName;
        this.longName = longName;
        this.agency = agency;
    }
    
    /**
     * A route constructor with all the possible fields.
     * 
     * @param agency The agency the route belongs to. Must be not null.
     * @param id The id of the route. Must be not null.
     * @param type The type of the transportations used on the route. Must be a value between 0 and 7.
     * @param shortName The short name of the route. It can be null only if longName parameter is not.
     * @param longName The long name of the route. It can be null onlt if shortName parameter is not.
     * @param description
     * @param url
     * @param color if null is ignored, otherwise it must be a valid value according to {@link #checkColor(java.lang.String)}.
     * @param textColor if null is ignored, otherwise it must be a valid value according to {@link #checkColor(java.lang.String)}.
     */
    public Route(Agency agency, String id, int type, String shortName, String longName, String description, URL url, String color, String textColor){
        this(agency, id, type, shortName, longName);
        
        if(color!=null && !checkColor(color))
            throw new IllegalArgumentException("Invalid color");
        if(color!=null && !checkColor(textColor))
            throw new IllegalArgumentException("Invalid textColor");
        
        this.description = description;
        this.url = url;
        if(color!=null)
            this.color = color;
        if(textColor!=null)
            this.textColor = textColor;
    }
    
    /**
     * Sets the agency for the route.
     * 
     * @param agency  the agency, not null.
     */
    public void setAgency(Agency agency) {
        if(agency==null) throw new IllegalArgumentException("Agency must be not null");
        this.agency = agency;
    }
    
    
    
    /**
     * Sets the id of the route.
     * The id must be unique in the dataset the route belongs to.
     * 
     * @param id  the id, not null.
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Sets the short name of the route.
     * 
     * @param shortName  the short name.
     * @return true if the shortName is set correctly, false if is null and longName is too.
     */
    public boolean setShortName(String shortName) {
        if(shortName==null && longName==null) return false;
        this.shortName = shortName;
        return true;
    }

    /**
     * Sets the long name of the route.
     * 
     * @param longName  the short name.
     * @return true if the longName is set correctly, false if is null and shortName is too.
     */
    public boolean setLongName(String longName) {
        if(shortName==null && longName==null) return false;
        this.longName = longName;
        return true;
    }
    
    /**
     * Sets the route description.
     * 
     * @param description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Sets the route type.
     * 
     * @param type  The route type, as allowed by the GTFS specification.
     * @return  true if type set correctly, false if it's not a valid value.
     */
    public boolean setType(int type) {
        if(type<0 || type>7) return false;
        this.type = type;
        return true;
    }

    /**
     * Sets the url of the route.
     * 
     * @param url  the route url.
     */
    public void setUrl(URL url) {
        this.url = url;
    }
    
    /**
     * Sets the color associated with the route.
     * 
     * @param color  The color to set, not null.
     * @return  true if the color is set correctly, false if it is not correct according to {@link #checkColor(java.lang.String)}.
     */
    public boolean setColor(String color) {
        if(!checkColor(color)) return false;
        this.color = color;
        return false;
    }

    /**
     * Sets the color associated with the route text.
     * 
     * @param textColor  the color to set, not null.
     * @return  true if the color is set correctly, false if it is not correct according to {@link #checkColor(java.lang.String)}.
     */
    public boolean setTextColor(String textColor) {
        if(!checkColor(textColor)) return false;
        this.textColor = textColor;
        return false;
    }
    
    public void setTrips(Set<Trip> trips) {
        this.trips = trips;
    }

    public String getDescription() {
        return description;
    }

    public int getType() {
        return type;
    }

    public URL getUrl() {
        return url;
    }

    public String getColor() {
        return color;
    }

    public String getTextColor() {
        return textColor;
    }
    
    @Override
    public boolean equals(Object o){
        if(!(o instanceof Route)) return false;
        Route r = (Route) o;
        return id.equals(r.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @ManyToOne(optional=false)
    @JoinColumn(name="agency", nullable=false )
    public Agency getAgency(){
        return agency;
    }
    
    @Id
    @Column(name="id")
    public String getId(){
        return id;
    }

    public String getShortName() {
        return shortName;
    }

    public String getLongName() {
        return longName;
    }

    /**
     * 
     * @return the read-only view of all the trips belonging to the route.
     */
    @Transient
    //@OneToMany(targetEntity=Trip.class)
    public Set<Trip> getTrips() {
        if(trips==null)
            setTrips(new HashSet<>());
        return Collections.unmodifiableSet(trips);
    }
    
    /**
     * @return the short name if not null, the long name otherwise.
     */
    @Transient
    public String getName(){
        return (shortName!=null ? shortName : longName);
    }
    
    /**
     * Adds a trip to the route.
     * 
     * @param trip The trip to add. Must be not null.
     * @return true if the trip is not already present, false otherwise.
     */
    public boolean addTrip(Trip trip){
        if(trips==null)
            setTrips(new HashSet<>());
        return trips.add(trip);
    }
    
    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        
        if(agency!=null){
            builder.append("Agency name = ");
            builder.append(agency.getName());
            builder.append(", ");
        }
        builder.append("ID = ");
        builder.append(id);
        if(shortName!=null){
            builder.append(", Short Name = ");
            builder.append(shortName);
        }
        if(longName!=null){
            builder.append(", Long Name = ");
            builder.append(longName);
        }
        if(description!=null){
            builder.append(", Description = ");
            builder.append(description);
        }
        builder.append(", Type = ");
        builder.append(type);
        if(url!=null){
            builder.append(", Url = ");
            builder.append(url);
        }
        builder.append((", Color = "));
        builder.append(color);
        builder.append(", Text Color = ");
        builder.append(textColor);
        return builder.toString();
    }
}
