package gtfs.entities;

import java.net.URL;

/**
 * A stop or station.
 * 
 * @see <a href="https://developers.google.com/transit/gtfs/reference/#stopstxt">GTFS Overview - Stop</a>
 */
public class Stop extends GTFS{
    /**
     * The id of the stop.
     */
    private String id; //required
    private String code;
    /**
     * The name of the stop. Must be not null.
     */
    private String name; //required
    private String description;
    /**
     * The WGS84 latitude of the stop.
     */
    private double lat; //required
    /**
     * The WGS84 longitude of the stop.
     */
    private double lon; //required
    private URL url;
    private boolean isStation;
    private String timezone;
    private Boolean wheelchairBoarding;
    private Stop parent;
    
    /**
     * The Stop constructor with all the possibile parameters.
     * 
     * @param id The id of the stop. Must be not null.
     * @param name The name of the stop. Must be not null.
     * @param lat The latitude of the stop. Must be a WGS84 valid latitude value.
     * @param lon The longitude of the stop. Must be a WGS84 valid longitude value.
     * @param code
     * @param description
     * @param url
     * @param isStation
     * @param parent
     * @param timezone
     * @param whBoarding Tells wether wheelchair can board on the vehcile or not. Null if there are no information about that.
     */
    public Stop(String id, String code, String name, String description, double lat, double lon, URL url, boolean isStation, Stop parent, String timezone, Boolean whBoarding){
        this(id, name, lat, lon);
        this.code = code;
        this.description = description;
        this.url = url;
        this.isStation = isStation;
        this.wheelchairBoarding = whBoarding;
        this.parent = parent;
        this.timezone = timezone;
    }
    
    /**
     * Stop constructor with required values only.
     * 
     * @param id The id of the stop. Must be not null.
     * @param name The name of the stop. Must be not null.
     * @param lat The latitude of the stop. Must be a WGS84 valid latitude value.
     * @param lon The longitude of the stop. Must be a WGS84 valid longitude value.
     */
    public Stop(String id, String name, double lat, double lon){
        if(id==null)
            throw new IllegalArgumentException("Id can't be null");
        if(name==null)
            throw new IllegalArgumentException("Name can't be null");
        if(lat<-90. || lat>90.)
            throw new IllegalArgumentException("Invalid WGS84 latitude value");
        if(lon<-180. || lon>180.)
            throw new IllegalArgumentException("Invalid WGS84 longitude value");
        
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }
    
    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public URL getUrl() {
        return url;
    }
    
    /**
     * 
     * @return null if there are no information about the wheelchair boarding, otherwise if wheelchairs can board on the vehicle.
     */
    public Boolean getWheelchairBoarding() {
        return wheelchairBoarding;
    }

    public Stop getParent() {
        return parent;
    }
    
    /**
     * 
     * @return true if this stop is a station, false otherwise.
     */
    public boolean isStation(){
        return isStation;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Stop)) return false;
        Stop s = (Stop) o;
        return id.equals(s.id);
    }
    
    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append("StopId : ");
        builder.append(id);
        if(code!=null){
            builder.append(", Code : ");
            builder.append(code);
        }
        builder.append(", Name : ");
        builder.append(name);
        if(description!=null){
            builder.append("Description : ");
            builder.append(description);
        }
        builder.append(", Latitude : ");
        builder.append(lat);
        builder.append(", Longitude : ");
        builder.append(lon);
        if(url!=null){
            builder.append(", URL : ");
            builder.append(url);
        }
        builder.append(", Station ? ");
        builder.append(isStation);
        
        if(parent!=null){
            builder.append(", ParentId : ");
            builder.append(parent.id);
        }
        if(getTimezone()!=null){
            builder.append(", Timezone : ");
            builder.append(getTimezone());
        }
        if(wheelchairBoarding!=null){
            builder.append(", Wheelchair boarding ? ");
            builder.append(wheelchairBoarding);
        }
        return builder.toString();
    }
    
    /**
     * 
     * @return the timezone associated with the stop or the station it is in.
     */
    public String getTimezone(){
        if(timezone!=null) return timezone;
        if(parent!=null) return parent.timezone;
        return null;
    }
    
    /**
     * Sets the parent of this stop.
     * 
     * @param parent The parent to set.
     * @return true if the this stop is not a station, false otherwhise.
     */
    public boolean setParent(Stop parent){
        if(isStation()) return false;
        this.parent = parent;
        return true;
    }
    
    public String getId() {
        return id;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }
}
