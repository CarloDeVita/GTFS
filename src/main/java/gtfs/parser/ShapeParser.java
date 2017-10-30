package gtfs.parser;

import gtfs.entities.Shape;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A parser for shape file.
 */
public class ShapeParser extends GTFSParser<Shape>{
    // a map that associates each id with the corresponding shape
    private Map<String, Shape> shapes = new HashMap<>();
    // associates each shape id with the corresponding last sequence number found
    private Map<String, Integer> lastSequences = new HashMap<>();
    // associates each shape id with the corresponding last distance traveled value found
    private Map<String, Double> lastDistances = new HashMap<>();
     
    @Override
    public void clear(){
        super.clear();
        shapes.clear();
        lastSequences.clear();
        lastDistances.clear();
    }        
            
    @Override
    public String getFileName() {
        return "shapes.txt";
    }

    @Override
    protected int columnToParameter(String name) {
        switch(name){
            case "shape_id" : return 0;
            case "shape_pt_lat" : return 1;
            case "shape_pt_lon" : return 2;
            case "shape_pt_sequence" : return 3;
            case "shape_dist_traveled" : return 4;
        }
        return -1;
    }

    @Override
    protected boolean checkRequired(String[] firstRow) {
        boolean id = false;
        boolean lat = false;
        boolean lon = false;
        boolean sequence = false;
        for(String s : firstRow){
            switch(s){
                case "shape_id" : id = true; break;
                case "shape_pt_lat" : lat = true; break;
                case "shape_pt_lon" : lon = true; break;
                case "shape_pt_sequence" : sequence = true; break;
            }
        }
        return (id && lat && lon && sequence);
    }

    @Override
    protected void processRow(String[] parameters, Collection<Shape> result) {
        String shapeId = parameters[0];
        String latString = parameters[1];
        String lonString = parameters[2];
        String sequenceString = parameters[3];
        String distanceString = parameters[4];
        
        // check required field
        if(shapeId==null || latString==null || lonString==null)
            throw new GTFSParsingException("Missing required value");
        
        // check if shape already found before
        boolean newShape = false;
        Shape shape = shapes.get(shapeId);
        if(shape==null){ // create a new shape
            newShape = true;
            shape = new Shape(shapeId);
            shapes.put(shapeId, shape);
        }
        
        // get and check latitude and longitude
        double latitude;
        double longitude;
        try{
            latitude = Double.parseDouble(latString);
            longitude = Double.parseDouble(lonString);
        }catch(NumberFormatException e){
            throw new GTFSParsingException("Invalid latitude or longitude");
        }
        if(latitude>90. || latitude<-90.)
            throw new GTFSParsingException("Invalid WGS84 latitude value "+latString);
        if(longitude>180. || longitude<-180.)
            throw new GTFSParsingException("Invalid WGS84 longitude value "+lonString);
        
        // get and check the sequence number
        int sequence = -1;
        try{
           sequence = Integer.parseInt(sequenceString);
        }catch(NumberFormatException e){
            throw new GTFSParsingException("Invalid sequence value "+sequenceString);
        }
        if(sequence<0)
            throw new GTFSParsingException("Sequence value must be positive");
        
        // check if sequence number increases
        Integer lastSequence = lastSequences.get(shapeId);
        if(lastSequence!=null && sequence<=lastSequence)
            throw new GTFSParsingException("Sequence number must increase");
        
        // get and check the distance traveled
        double distance = -1;
        if(distanceString!=null){
            try{
                distance = Double.parseDouble(distanceString);
            }catch(NumberFormatException e){
                throw new GTFSParsingException("Invalid distance value "+distanceString);
            }
            if(distance<0)
                throw new GTFSParsingException("Distance value must be positive");
        }
        
        // check if distance increases
        Double lastDistance = lastDistances.get(shapeId);
        if(lastDistance!=null && distance<lastDistance)
            throw new GTFSParsingException("Distance must increase");
        
        // create the point
        Shape.Point point;
        if(distance<0)
            point = new Shape.Point(latitude, longitude, sequence);
        else
            point = new Shape.Point(latitude, longitude, sequence, distance);
        
        // add the point to the shape
        shape.addPoint(point);
        
        // add the shape (if new) to the result
        if(newShape)
            result.add(shape);
        
        // updates the last sequence number and distance traveled found for the shape
        lastSequences.put(shapeId, sequence);
        if(distance>=0)
            lastDistances.put(shapeId, distance);    
    }

    @Override
    public int numberOfParameters() {
        return 5;
    }
}
