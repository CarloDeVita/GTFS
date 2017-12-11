package gtfs.parser;

import gtfs.entities.Calendar;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

/**
 * A parser for Calendar file.
 */
public class CalendarFileParser extends GTFSParser<Calendar>{
    
    @Override
    public String getFileName() {
        return "calendar.txt";
    }

    @Override
    protected int columnToParameter(String name) {
        switch(name){
            case "service_id": return 0; 
            case "monday": return 1;
            case "tuesday": return 2;
            case "wednesday": return 3;
            case "thursday": return 4;
            case "friday": return 5;
            case "saturday": return 6;
            case "sunday": return 7;
            case "start_date": return 8;
            case "end_date": return 9;
        }
        return -1;
    }

    @Override
    protected boolean checkRequired(String[] firstRow) {
        return (firstRow.length == numberOfParameters());
        
    }

    @Override
    protected void processRow(String[] parameters, Collection<Calendar> result) {
        // All parameters are required
        for(String s : parameters)
            if(s==null)
                throw new GTFSParsingException("Missing required value");
        
        String id = parameters[0];
        String monday = parameters[1];
        String tuesday = parameters[2];
        String wednesday = parameters[3];
        String thursday = parameters[4];
        String friday = parameters[5];
        String saturday = parameters[6];
        String sunday = parameters[7];
        String sDate = parameters[8];
        String eDate = parameters[9];
        
        // get and checks the dates
        LocalDate startDate = createDateFromGTFS(sDate);
        LocalDate endDate = createDateFromGTFS(eDate);
        if(startDate.isAfter(endDate))
            throw new GTFSParsingException("Start date comes after the end date");
        
        // get the association day -> activity status
        Map<DayOfWeek,Boolean> days = new EnumMap<>(DayOfWeek.class); 
        for(int i = 1; i <= 7; i++){
            try{
                int activeValue = Integer.parseInt(parameters[i]);
                if(activeValue < 0 && activeValue > 1)
                    throw new GTFSParsingException("Invalid value \""+parameters[i+1]+"\" for "+DayOfWeek.of(i));
                DayOfWeek day = DayOfWeek.of(i);
                Boolean active = (activeValue==1);
                days.put(DayOfWeek.of(i), active);
            }
            catch(NumberFormatException ex){
                throw new GTFSParsingException("Invalid value \""+parameters[i+1]+"\" for "+DayOfWeek.of(i));
            }
        }
        
        // creates and add the calendar
        Calendar calendar = new Calendar(id, startDate, endDate, days);
        result.add(calendar);
    }

    @Override
    public int numberOfParameters() {
        return 10;
    }
    
    /**
     * Parses a date from the calendar dates file column
     * 
     * @param dateString The string from the date column. Must be not null. If the string is an invalid date value, throws a GTFSParsingException.
     * @return The date parsed.
     */
    static LocalDate createDateFromGTFS(String dateString){
        LocalDate date = null;
        try{
            int day,month,year;
            year = Integer.parseInt(dateString.substring(0,4));
            month = Integer.parseInt(dateString.substring(4,6));
            day = Integer.parseInt(dateString.substring(6,8));
            date = LocalDate.of(year, month, day);
        }
        catch(NumberFormatException | DateTimeException ex){
            throw new GTFSParsingException("Invalid date "+dateString);
        }
        return date;
    }
    
}
