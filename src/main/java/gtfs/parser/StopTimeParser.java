package gtfs.parser;

import gtfs.entities.Stop;
import gtfs.entities.StopTime;
import gtfs.entities.Trip;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses StopTimes from stoptimes.txt .
 */
public class StopTimeParser extends GTFSParser<StopTime>{    
    // associates each id with the corresponding trip
    private Map<String, Trip> trips;
    // associates each id with the corresponding stop
    private Map<String, Stop> stops;
    // last sequence number found in file
    private int lastSequence = -1;
    // last trip found in file
    private String lastTrip;
    
    /**
     * Adds the trips to the parser.
     * These trips are used to check the reference in trip_id field.
     * 
     * @param tCollection The collection of trips to add. Must be not null.
     */
    public void addTrips(Collection<Trip> tCollection){
        if(tCollection.isEmpty()) return;
        if(trips==null) trips = new HashMap<>();
        for(Trip t : tCollection)
            trips.put(t.getId(), t);
    }
    
    /**
     * Adds the stop to the parser.
     * These stops are used to check the references in stop_id field.
     * 
     * @param sCollection The collection of stops to add. Must be not null.
     */
    public void addStops(Collection<Stop> sCollection){
        if(sCollection.isEmpty()) return;
        if(stops==null) stops = new HashMap<>();
        for(Stop s : sCollection)
            stops.put(s.getId(), s);
    }
    
    @Override
    public void clear(){
        super.clear();
        lastSequence = -1;
        lastTrip = null;
        trips = null;
        stops = null;
    }
    
    @Override
    public boolean isReady(){
        return (trips!=null && stops!=null && !trips.isEmpty() && !stops.isEmpty());
    }
    
    @Override
    public String getFileName() {
        return "stop_times.txt";
    }

    @Override
    protected int columnToParameter(String name) {
        switch(name){
            case "trip_id" : return 0;
            case "arrival_time" : return 1;
            case "departure_time" : return 2;
            case "stop_id" : return 3;
            case "stop_sequence" : return 4;
            case "stop_headsign" : return 5;
            case "pickup_type" : return 6;
            case "drop_off_type" : return 7;
            case "shape_dist_traveled" : return 8;
            case "timepoint" : return 9;
        }
        return -1;
    }

    @Override
    protected boolean checkRequired(String[] firstRow) {
        boolean trip = false;
        boolean arrival = false;
        boolean departure = false;
        boolean stop = false;
        boolean sequence = false;
        boolean timepoint = false;
        for(String s : firstRow){
            switch(s){
                case "trip_id" : trip = true; break;
                case "arrival_time" : arrival = true; break;
                case "departure_time" : departure = true; break;
                case "stop_id" : stop = true; break;
                case "stop_sequence" : sequence = true; break;
                case "timepoint" : timepoint = true; break;
            }
        }
        
        return (trip && stop && sequence && ((arrival && departure) || timepoint));
    }

    @Override
    protected void processRow(String[] parameters, Collection<StopTime> result) {
        String tripId = parameters[0];
        String arrivalString = parameters[1];
        String departureString = parameters[2];
        String stopId = parameters[3];
        String sequenceString = parameters[4];
        String headsign = parameters[5];
        String pickupString = parameters[6];
        String dropoffString = parameters[7];
        String distString = parameters[8];
        String timepointString = parameters[9];
        
        // check required values
        if(tripId==null || stopId==null || sequenceString==null)
            throw new RuntimeException("Missing required value");
        
        if(lastTrip!=null && !tripId.equals(lastTrip)) // reset sequence number
            lastSequence = -1;
        
        // get and check trip and stop
        Trip trip = trips.get(tripId);
        if(trip==null) throw new RuntimeException("Missing trip "+tripId);
        Stop stop = stops.get(stopId);
        if(stop==null) throw new RuntimeException("Missing stop "+stopId);
        
        
        // get and check sequence number value
        // check if it is bigger than last sequence number of the same trip
        int sequence;
        try{
            sequence = Integer.parseInt(sequenceString);
            if(sequence<0) throw new RuntimeException("Bad sequence value");
        }catch(NumberFormatException e){
            throw new RuntimeException("Bad sequence value");
        }
        if(lastSequence!=-1 && sequence<=lastSequence)
            throw new RuntimeException("Sequence number must increase");
        
        // get and check pickup type value
        int pickup = 0;
        if(pickupString!=null){
            try{
                pickup = Integer.parseInt(pickupString);
                if(!StopTime.isValidPickupType(pickup))
                    throw new RuntimeException("Pickup value "+pickup+" not in range");
            }catch(NumberFormatException e){
                throw new RuntimeException("Bad pickup type value" + pickupString);
            }
        }
        
        // get and check dropoff type value
        int dropoff = 0;
        if(dropoffString!=null){
            try{
                dropoff = Integer.parseInt(dropoffString);
                if(!StopTime.isValidDropoffType(dropoff))
                    throw new RuntimeException("Dropoff value "+pickup+" not in range");
            }catch(NumberFormatException e){
                throw new RuntimeException("Bad dropoff type value" + pickupString);
            }
        }
        
        // get and check timepoint value
        // if it is a timepoint check arrival and departure
        boolean timepoint = true;
        if(timepointString!=null){
            if(timepointString.equals("0"))
                timepoint = false;
            else if(!timepointString.equals("1"))
                throw new RuntimeException("Bad timepoint value : "+ timepointString);
        }
        if(timepoint && (arrivalString==null || departureString==null))
            throw new RuntimeException("Timepoint missing arrival or departure time");
        
        // check arrival and departure values
        if(arrivalString!=null && !StopTime.isValidTime(arrivalString))
            throw new RuntimeException("Bad arrival time value : "+arrivalString);
        if(departureString!=null && !StopTime.isValidTime(departureString))
            throw new RuntimeException("Bad departure time value : "+departureString);
        
        // get and check distance value
        Double distance = null;
        if(distString!=null){
            try{
                distance = Double.parseDouble(distString);
                if(distance<0.)
                    throw new RuntimeException("Bad distance value : "+ distance);
            }catch(NumberFormatException e){
                throw new RuntimeException("Bad distance value : "+ distString);
            }
        }
        
        // create the stoptime
        StopTime s = new StopTime(trip, stop, sequence, arrivalString, departureString, headsign, pickup, dropoff, timepoint, distance);
        // save last sequence
        lastSequence = sequence;
        lastTrip = tripId;
        // add the stoptime to the result
        result.add(s);
    }

    @Override
    public int numberOfParameters() {
        return 10;
    }
}
