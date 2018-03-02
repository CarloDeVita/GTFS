package gtfs.parser;

import gtfs.entities.Calendar;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A parser for Calendar Dates file.
 * The collection returned does not contain the calendars added before the parsing.
 */
public class CalendarDatesParser extends GTFSParser<Calendar>{
    // a map that associates each id with the corresponding calendar
    private final Map<String,Calendar> calendars = new HashMap<>();

    public CalendarDatesParser(){
        super("calendar_dates.txt",3);
    }
    
    @Override
    public void clear(){
        calendars.clear();
    }
    
    /**
     * Adds the calendars to prepare the parser.
     * These calendars are used to add the exception to the existing calendars referenced by the service_id column.
     * 
     * @param cCollection The collection of calendars to add. Must be not null.
     */
    public void addCalendars(Collection<Calendar> cCollection){
        for(Calendar c : cCollection)
            calendars.put(c.getId(), c);
    }
    
    @Override
    protected int columnToParameter(String name) {
        switch(name){
            case "service_id": return 0;
            case "date": return 1;
            case "exception_type": return 2;
        }
        return -1;
    }

    @Override
    protected boolean checkRequired(String[] firstRow) {
        return (firstRow.length == numberOfParameters); //all attributes required
    }

    @Override
    protected void processRow(String[] parameters, Collection<Calendar> result) {
        String id = parameters[0];
        String dateString = parameters[1];
        String excType = parameters[2];
        
        if(id == null || dateString == null || excType == null)
            throw new GTFSParsingException("Missing required values");
        
        // get and check the date
        LocalDate date = CalendarFileParser.createDateFromGTFS(dateString);
        
        // get and check exception type value
        int exception = 0;
        try{
            exception = Integer.parseInt(excType);
        }
        catch(NumberFormatException ex){
            throw new GTFSParsingException("Invalid exception type value : "+excType);
        }
        if(exception < 1 || exception > 2)
            throw new GTFSParsingException("Invalid exception type value : "+excType);//todo
        Boolean exceptionType = (exception==1);
        
        // if the calendar specified in the id field
        // is not found, a new empty calendar is created
        // and added to the result.
        Calendar c = calendars.get(id);
        if(c==null){
            c = new Calendar(id);
            calendars.put(id, c);
            result.add(c);
        }
        c.setDate(date, exceptionType);
    }
}
