package gtfs.parser;

import gtfs.entities.Agency;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

/**
 * A parser for the agency file.
 */
public class AgencyParser extends GTFSParser<Agency> {
    private boolean noAgencyId; // tells wheter "agency_id" column has been declared
    private int records; // number or records found
    
    public AgencyParser(){
        super("agency.txt", 8);
    }

    @Override
    protected void processRow(String parameters[], Collection<Agency> result) {
        String id = parameters[0];
        String name = parameters[1];
        String urlString = parameters[2];
        String timezone = parameters[3];
        String lang = parameters[4];
        String phone = parameters[5];
        String fareUrl = parameters[6];
        String email = parameters[7];
        
        if(name==null || urlString==null || timezone==null)
            throw new GTFSParsingException("Missing required value");
        
        // get and check the url
        URL url = null;
        try{
            url = new URL(urlString);
        }catch(MalformedURLException e){
            throw new GTFSParsingException("Malformed URL : "+urlString);
        }
        
        if(records>0 && (noAgencyId || id==null))
            throw new GTFSParsingException("Id can be optional only if the file contains a single agency");
        
        // create and add the agency
        Agency a = new Agency(id, name, url, timezone, lang, phone, fareUrl, email);
        if(!result.add(a))
            throw new GTFSParsingException("Multiple equal agencies");
        records++;
    }

    @Override
    protected int columnToParameter(String name) {
        switch(name){
            case "agency_id" : return 0;
            case "agency_name" : return 1;
            case "agency_url" : return 2;
            case "agency_timezone" : return 3;
            case "agency_lang" : return 4;
            case "agency_phone" : return 5;
            case "agency_fare_url" : return 6;
            case "agency_email" : return 7;
        }
        return -1;
    }

    @Override
    protected boolean checkRequired(String firstRow[]){
        boolean id = false;
        boolean name = false;
        boolean url = false;
        boolean timezone = false;
        for(String s : firstRow){
            switch(s){
                case "agency_id" : id = true; break;
                case "agency_name" : name = true; break;
                case "agency_url" : url = true; break;
                case "agency_timezone" : timezone = true; break;
            }
        }
        noAgencyId = !id;
        return(name && url && timezone);
    }
    
    @Override
    public void clear(){
        super.clear();
        noAgencyId = false;
        records = 0;
    }
}