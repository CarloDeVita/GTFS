package gtfs.entities;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * An operator of a public transit network, often a public authority.
 * 
 * @see <a href="https://developers.google.com/transit/gtfs/reference/#agencytxt">GTFS Overview - Agency</a>
 */
public class Agency extends GTFS{
    private String id;
    private String name; //required
    private URL url; // required
    private String timezone; //required
    private String lang;
    private String phone;
    private String fareUrl;
    private String email;
    private Set<Route> routes;
    
    /**
     * The Agency constructor with all the required parameters.
     * All the other fields will be null.
     * 
     * @param name the name of the agency. Must be not null.
     * @param url the url of the agency. Must be not null.
     * @param timezone the timezone of the agency. Must be not null.
     */
    public Agency(String name, URL url,String timezone){
        if(name==null || url==null || timezone==null) throw new IllegalArgumentException();
        //TODO check timezone value
        this.name = name;
        this.url = url;
        this.timezone = timezone;
    }
    
    /**
     * The Agency constructor with all the possible parameters.
     * 
     * @param id
     * @param name the name of the agency. Must be not null.
     * @param url the url of the agency. Must be not null.
     * @param timezone the timezone of the agency. Must be not null.
     * @param lang
     * @param phone
     * @param fareUrl
     * @param email 
     */
    public Agency(String id, String name, URL url, String timezone, String lang, String phone ,String fareUrl, String email){
        this(name, url, timezone);
        this.id = id;
        this.lang = lang;
        this.phone = phone;
        this.fareUrl = fareUrl;
        this.email = email;
    }
    
    /**
     * Adds a new route to the agency.
     * 
     * @param r the route to add.
     * @return false if the route is not associated with this agency or if the route
     * is already present in the agency, true otherwise.
     */
    public boolean addRoute(Route r){
        if(routes==null) routes = new HashSet<>();
        if(!equals(r.getAgency())) return false; //TODO eccezione? 
        return routes.add(r);
    }
    
    @Override
    public String toString(){
        StringBuilder buffer = new StringBuilder();
        if(id!=null){
            buffer.append("ID = ");
            buffer.append(id);
            buffer.append(", ");
        }
        buffer.append("Name = ");
        buffer.append(name);
        buffer.append(", Url = ");
        buffer.append(url);
        buffer.append(", Timezone = ");
        buffer.append(timezone);
        if(lang!=null){
            buffer.append(", Language = ");
            buffer.append(lang);
        }
        if(phone!=null){
            buffer.append(", Phone = ");
            buffer.append(phone);
        }
        if(fareUrl!=null){
            buffer.append(", Fare Url = ");
            buffer.append(fareUrl);
        }
        if(email!=null){
            buffer.append(", Email = ");
            buffer.append(email);
        }
        return buffer.toString();
    }
    
    public String getId(){
        return id;
    }
    
    public String getName(){
        return name;
    }

    public URL getUrl() {
        return url;
    }

    public String getTimezone() {
        return timezone;
    }

    public String getLang() {
        return lang;
    }

    public String getPhone() {
        return phone;
    }

    public String getFareUrl() {
        return fareUrl;
    }

    public String getEmail() {
        return email;
    }

    /**
     * 
     * @return a read-only view of the routes associated with the agency.
     */
    public Collection<Route> getRoutes() {
        return Collections.unmodifiableCollection(routes);
    }
}
