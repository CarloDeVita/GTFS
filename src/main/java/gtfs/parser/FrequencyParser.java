package gtfs.parser;

import gtfs.entities.Frequency;
import gtfs.entities.StopTime;
import gtfs.entities.Trip;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A parser for frequencies file.
 */
public class FrequencyParser extends GTFSParser<Frequency>{
    // a map that associates each id with the corresponding trip
    private Map<String, Trip> trips;
    // the exact time value of the file (decided by the second row)
    private Boolean fileExactTimes;
    
    public FrequencyParser(){
        super("frequencies.txt", 5);
    }
    
    /**
     * 
     * @return true if at least one trip has been added to the parser, false otherwise.
     * @see GTFSParser#isReady() 
     */
    @Override
    public boolean isReady(){
        return (trips!=null && !trips.isEmpty());
    }
    
    /**
     * Adds the trips to prepare the parser.
     * <p>These trips are used to check the existance of the trips 
     * referenced by the trip_id column and to bind theese trips with the frequencies read</p>
     * 
     * @param tCollection The collection of trips to add.
     */
    public void addTrips(Collection<Trip> tCollection){
        if(trips==null)
            trips = new HashMap<>();
        for(Trip t : tCollection)
            trips.put(t.getId(), t);
    }
    
    @Override
    public void clear(){
        trips = null;
        fileExactTimes = null;
    }
    
    @Override
    protected int columnToParameter(String name) {
        switch(name){
            case "trip_id" : return 0;
            case "start_time" : return 1;
            case "end_time" : return 2;
            case "headway_secs" : return 3;
            case "exact_times" : return 4;
        }
        return -1;
    }

    @Override
    protected boolean checkRequired(String[] firstRow) {
        boolean trip = false;
        boolean start_time = false;
        boolean end_time = false;
        boolean headway_secs = false;
        for(String s : firstRow){
            switch(s){
                case "trip_id" : trip = true; break;
                case "start_time" : start_time = true; break;
                case "end_time" : end_time = true; break;
                case "headway_secs" : headway_secs = true ; break;
            }
        }
        return (trip && start_time && end_time && headway_secs);
    }

    @Override
    protected void processRow(String[] parameters, Collection<Frequency> result) {
        String tripId = parameters[0];
        String start = parameters[1];
        String end = parameters[2];
        String seconds = parameters[3];
        String exactTimes = parameters[4];
        
        // check required values
        if(tripId==null || start==null || end==null || seconds==null)
            throw new GTFSParsingException("Missing required value");
        
        // get and check trip
        Trip trip = trips.get(tripId);
        if(trip==null)
            throw new GTFSParsingException("Missing trip : "+ tripId);
        
        //check times
        if(!StopTime.isValidTime(start))
            throw new GTFSParsingException("Bad start time value : "+start);
        if(!StopTime.isValidTime(end))
            throw new GTFSParsingException("Bad end time value : "+end);
        if(StopTime.TIME_COMPARATOR.compare(start, end)>=0)
            throw new GTFSParsingException("Start time after end time");
        
        // get and check headway_seconds
        int headwaySec = -1;
        try{
            headwaySec = Integer.parseInt(seconds);
            if(headwaySec<=0)
                throw new GTFSParsingException("Bad headway_sec value : "+headwaySec);
        }catch(NumberFormatException e){
            throw new GTFSParsingException("Bad headway_sec value : "+headwaySec);
        }
        
        // get and check exact_times
        boolean exact = false;
        if(exactTimes!=null){
            int exactValue = -1;
            try{
                exactValue = Integer.parseInt(exactTimes);
                if(exactValue!=0 && exactValue!=1)
                    throw new GTFSParsingException("Bad exact_times value : "+exactTimes);
                else
                    exact = (exactValue==1);
            }catch(NumberFormatException e){
                throw new GTFSParsingException("Bad exact_time value : "+exactTimes);
            }
        }
        
        // the exact times column values must be the same for all rows
        if(fileExactTimes==null)
            fileExactTimes = exact;
        else if(exact!=fileExactTimes)
            throw new GTFSParsingException("exact_times must be the same for all rows");
        
        // create and add frequency
        Frequency frequency = new Frequency(trip, start, end, headwaySec, exact);
        result.add(frequency);
    }
}
