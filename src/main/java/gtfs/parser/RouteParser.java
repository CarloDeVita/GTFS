package gtfs.parser;

import gtfs.entities.Agency;
import gtfs.entities.Route;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A parser for route file.
 */
public class RouteParser extends GTFSParser<Route>{
    // a map that associates each id with the corresponding agency.
    private final Map<String, Agency> agencies = new HashMap<>();
    // if the agency 
    private Agency uniqueAgency;
    
    /**
     * Adds the agencies to the parser.
     * <p>These agencies are used to check the references in agency_id column
     * and to bind the route with the corresponding Agency.</p>
     * <p>Only one agency without id can be added, otherwise a GTFSParsingException will be thrown.</p>
     * 
     * @param aCollection The agencies to add. Must be not null.
     * @return false if there is an agency
     */
    public boolean addAgencies(Collection<Agency> aCollection){
        if(uniqueAgency!=null && !aCollection.isEmpty()) return false;
        for(Agency a : aCollection){
            String key = a.getId();
            if(key==null && aCollection.size()>0){
                agencies.clear();
                return false;
            }
            
            if(key==null)
                agencies.put("", a);
            else
                agencies.put(key, a);
        }
        return true;
    }
    
    /**
     * 
     * @return true if at least one agency has been added to the parser.
     */
    @Override
    public boolean isReady(){
        return !agencies.isEmpty();
    }
    
    @Override
    public void clear(){
        super.clear();
        agencies.clear();
        uniqueAgency = null;
    }
    
    @Override
    public String getFileName() {
        return "routes.txt";
    }

    @Override
    protected int columnToParameter(String name) {
        switch(name){
            case "route_id" : return 0;
            case "agency_id" : return 1;
            case "route_short_name" : return 2;
            case "route_long_name" : return 3;
            case "route_desc" : return 4;
            case "route_type" : return 5;
            case "route_url" : return 6;
            case "route_color" : return 7;
            case "route_text_color" : return 8;
        }
        return -1;
    }

    @Override
    protected boolean checkRequired(String[] firstRow){
        boolean id = false;
        boolean short_name = false;
        boolean long_name = false;
        boolean type = false;
        boolean agency = false;
        for(String s : firstRow){
            switch(s){
                case "route_id" : id = true; break;
                case "route_short_name" : short_name = true; break;
                case "route_long_name" : long_name = true; break;
                case "route_type" : type = true; break;
                case "agency_id" : agency = true; break;
            }
        }
        if(!agency && agencies.size()!=1) return false;
        return(id && type && (short_name || long_name));
    }

    @Override
    public int numberOfParameters() {
        return 9;
    }

    @Override
    protected void processRow(String[] parameters, Collection<Route> result) {
        String id = parameters[0];
        String agencyId = parameters[1];
        String shortName = parameters[2];
        String longName = parameters[3];
        String desc = parameters[4];
        String typeString = parameters[5];
        String urlString = parameters[6];
        String color = parameters[7];
        String textColor = parameters[8];
        
        // check required field
        if(id==null || (shortName==null && longName==null) || typeString==null)
            throw new GTFSParsingException("Missing required value");
        
        // get and check the agency
        Agency agency = null;
        if(agencyId==null){
            if(uniqueAgency==null)
                throw new GTFSParsingException("Missinng agency id, but more than one agency in the feed");
            agency = uniqueAgency;
        }
        else
            agency = agencies.get(agencyId);
        if(agency==null)
            throw new GTFSParsingException("Missing agency "+ agencyId);
        
        // get and check the type
        int type;
        try{
            type = Integer.parseInt(typeString);
        }catch(NumberFormatException e){
            throw new GTFSParsingException("Invalid type "+typeString);
        }
        if(!Route.isValidType(type))
            throw new GTFSParsingException("Bad type value "+type);
        
        // get and check the url
        URL url = null;
        if(urlString!=null && !urlString.isEmpty()){
            try {
                url = new URL(urlString);
            } catch (MalformedURLException ex) {
                throw new GTFSParsingException("Malformed Url "+urlString);
            }
        }
        
        // get and check the color
        if(color!=null){
            if(color.isEmpty())
                color = null;
            else if(!Route.checkColor(color))
                throw new GTFSParsingException("Invalid color value : "+color);
        }
        
        // get and check the color
        if(textColor!=null){
            if(textColor.isEmpty())
                textColor = null;
            else if(!Route.checkColor(textColor))
                throw new GTFSParsingException("Invalid text color : "+textColor);
        }
        
        // create and add the route
        Route r = new Route(agency, id, type, shortName, longName, desc, url, color, textColor);
        agency.addRoute(r);
        result.add(r);
    }
    
}
