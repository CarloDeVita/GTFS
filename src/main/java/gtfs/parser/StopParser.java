package gtfs.parser;

import gtfs.entities.Stop;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A parser for stop file.
 * <p>If a stop is found in the file before the parent station,
 * it is put into a queue and completed later when the station is found.
 * If the station is not found in the entire file, the stop is not returned.</p>
 */
public class StopParser extends GTFSParser<Stop> {
    // associates each id with the corresponding station
    Map<String, Stop> stations = new HashMap<>();
    // associates a Stop waiting for its parent to be found to its parent station id
    Map<Stop, String> pendingForParent = new HashMap<>();
    
    @Override
    public void clear(){
        super.clear();
        stations.clear();
        pendingForParent.clear();
    }
    
    @Override
    public String getFileName() {
        return "stops.txt";
    }

    @Override
    protected int columnToParameter(String name) {
        switch(name){
            case "stop_id" : return 0;
            case "stop_code" : return 1;
            case "stop_name" : return 2;
            case "stop_desc" : return 3;
            case "stop_lat" : return 4;
            case "stop_lon" : return 5;
            case "zone_id" : return 6;
            case "stop_url" : return 7;
            case "location_type" : return 8;
            case "parent_station" : return 9;
            case "stop_timezone" : return 10;
            case "wheelchair_boarding" : return 11;
        }
        return -1;
    }

    @Override
    protected boolean checkRequired(String[] firstRow) {
        boolean id = false;
        boolean name = false;
        boolean lat = false;
        boolean lon = false;
        for(String s : firstRow){
            switch(s){
                case "stop_id" : id = true; break;
                case "stop_name" : name = true; break;
                case "stop_lat" : lat = true; break;
                case "stop_lon" : lon = true; break;
            }
        }
        
        return (id && name && lat && lon);
    }

    @Override
    protected void processRow(String[] parameters, Collection<Stop> result) {
        String id = parameters[0];
        String code = parameters[1];
        String name = parameters[2];
        String desc = parameters[3];
        String latString = parameters[4];
        String lonString = parameters[5];
        String zone = parameters[6];
        String urlString = parameters[7];
        String location = parameters[8];
        String parentString = parameters[9];
        String timezone = parameters[10];
        String wheelchairString = parameters[11];
    
        // check required fields
        if(id==null || name==null || latString==null || lonString==null)
            throw new RuntimeException("Required value missing");
        
        // get and check latitude and longitude
        double latitude;
        double longitude;
        try{
            latitude = Double.parseDouble(latString);
            longitude = Double.parseDouble(lonString);
        }catch(NumberFormatException e){
            throw new RuntimeException("Invalid value");
        }
        if(latitude<-90. || latitude>90.)
            throw new RuntimeException("Invalid WGS84 latitude");
        if(longitude<-180. || longitude>180.)
            throw new RuntimeException("Invalid WGS84 longitude");
    
        // get and check the url
        URL url = null;
        if(urlString!=null){    
            try {
                url = new URL(urlString);
            } catch (MalformedURLException ex) {
                throw new RuntimeException("Malformed URL");
            }
        }
        
        // get and check wheelchair boarding value
        Boolean wheelchair = null;
        if(wheelchairString!=null){
            int wheelStringValue;
            try{
                wheelStringValue = Integer.parseInt(wheelchairString);
            }catch(NumberFormatException e){
                throw new RuntimeException("Invalid wheelchair boarding value");
            }
            if(wheelStringValue<0 || wheelStringValue>2)
                throw new RuntimeException("Wheelchair boarding values can be only 0 or 1");
            if(wheelStringValue==1)
                wheelchair = true;
            else if(wheelStringValue==2)
                wheelchair = false;
        }
        
        // get and check the location type
        int locationType = 0;
        if(location!=null){
            try{
                locationType = Integer.parseInt(location);
                if(locationType<0 || locationType>1)
                    throw new RuntimeException("Location type can have values 0 or 1");
            }catch(NumberFormatException e){
                throw new RuntimeException("Invalid location type");
            }
        }
        
        // get and check the parent station
        Stop parent = null;
        if(locationType==0 && parentString!=null)
            parent = stations.get(parentString);
        
        // create the stop
        Stop stop = new Stop(id, code, name, desc, latitude, longitude, url, locationType==1, parent, timezone, wheelchair);
        
        if(stop.isStation()){
            // add the station to the list
            stations.put(id, stop);
            // complete stops that have this station as parent
            if(!pendingForParent.isEmpty()){
                for(Stop s : pendingForParent.keySet()){
                    String pendingParent = pendingForParent.get(s);
                    // complete the pending stop
                    if(pendingParent.equals(id)){
                        s.setParent(stop);
                        result.add(s);
                    }
                }
            }
        }
        else if(parentString!=null && parent==null)
            pendingForParent.put(stop, parentString);
        
        // add the stop to the result
        if(stop.isStation() || parentString==null || parent!=null)
            result.add(stop);
    }
    
    @Override
    public int numberOfParameters() {
        return 12;
    }
}
