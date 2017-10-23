package gtfs.parser;

import gtfs.entities.Calendar;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;

/**
 * A parser for both calendar and caldendar dates files.
 */
public class CalendarParser {
    /**
     * Parses both calendar and calendar dates files.
     * 
     * @param directory The directory of the GTFS feed. Must be not null.
     * @return a collection containing all the calendars parsed.
     * @throws 
     * @throws IOException if the parsing produces an IOException for both files.
     */
    public Collection<Calendar> read(String directory) throws IOException{
        Collection<Calendar> calendars = null, calendars2 = null;
        FileNotFoundException ex = null;
        
        // read calendars from calendar file
        CalendarFileParser calendarFileParser = new CalendarFileParser();
        try{
            calendars = calendarFileParser.parse(directory);
        }catch(FileNotFoundException e){
            ex = e;
        }
        
        // read calendars from calendar dates file
        CalendarDatesParser calendarDatesParser = new CalendarDatesParser();
        if(calendars!=null)
            calendarDatesParser.addCalendars(calendars);
        try{
            calendars2 = calendarDatesParser.parse(directory);
        }catch(FileNotFoundException e){}
        
        if(calendars!=null){
            if(calendars2!=null) // merges the collections
                calendars.addAll(calendars2);
        }
        else
            calendars = calendars2;
      
        // both file produced an exception
        if(calendars==null) throw ex;
        
        return calendars;
    }
}
