package gtfs.entities;

import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import java.net.URL;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * A stop or station.
 * 
 * @see <a href="https://developers.google.com/transit/gtfs/reference/#stopstxt">GTFS Overview - Stop</a>
 */
@Entity
@Table(name="stops", schema="gtfs", catalog="postgis_test")
public class Stop extends GTFS{
    //TODO static or built one every time?
    private static GeometryFactory factory;
    private Point coordinate;
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
        if(factory==null){
            PrecisionModel precision = new PrecisionModel(PrecisionModel.FLOATING);
            int srid = 4326; //WGS 84
            factory = new GeometryFactory(precision, srid);
        }
        
        this.id = id;
        this.name = name;
        this.coordinate = factory.createPoint(new Coordinate(lon, lat));
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

    @OneToOne(targetEntity=Stop.class)
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
        builder.append(getLat());
        builder.append(", Longitude : ");
        builder.append(getLon());
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
    
    @Id
    public String getId() {
        return id;
    }

    @Transient
    public double getLat() {
        return coordinate.getY();
    }

    @Transient
    public double getLon() {
        return coordinate.getX();
    }
    
    public Point getCoordinate(){
        return coordinate;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 
     * @param lat The latitude to set. Must be a valid WGS-84 projection latitude value.
     */
    public void setLat(double lat) {
        //TODO check param
        Coordinate coordinate = new Coordinate();
        coordinate.setOrdinate(Coordinate.X, getLon());
        coordinate.setOrdinate(Coordinate.Y, lat);
        this.coordinate = factory.createPoint(coordinate);
    }

    /**
     * 
     * @param lon The longitude to set. Must be a valid WGS-84 projection longitude value.
     */
    public void setLon(double lon) {
        //TODO check param
        Coordinate coordinate = new Coordinate();
        coordinate.setOrdinate(Coordinate.Y, getLat());
        coordinate.setOrdinate(Coordinate.X, lon);
        this.coordinate = factory.createPoint(coordinate);
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public void setStation(boolean isStation) {
        this.isStation = isStation;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public void setWheelchairBoarding(Boolean wheelchairBoarding) {
        this.wheelchairBoarding = wheelchairBoarding;
    }

    public void setCoordinate(Point coordinate) {
        this.coordinate = coordinate;
    }
    
    
}
