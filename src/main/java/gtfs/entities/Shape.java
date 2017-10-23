package gtfs.entities;

import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;

/**
 * The physical path the vehicle takes.
 * 
 * @see <a href="https://developers.google.com/transit/gtfs/reference/#shapestxt">GTFS Overview - Shape</a>
 */
public class Shape extends GTFS{
    private String id;
    private TreeSet<Point> points = new TreeSet<>((Point o1, Point o2) -> (o2.sequence-o1.sequence));
    
    /**
     * Creates a shape with no points.
     * 
     * @param id The id of the shape. Must be not null.
     */
    public Shape(String id){
        if(id==null)
            throw new IllegalArgumentException("Id must be not null");
        this.id = id;
    }
    
    /**
     * 
     * @return a read-only ordered view of the point that form the shape.
     */
    public Collection<Point> getPoints(){
        return Collections.<Point>unmodifiableCollection(points);
    }
    
    /**
     * Appends a point to the shape.
     * 
     * @param point the point to add. Must be not null.
     * @return true if the point is added, false otherwise.
     */
    public boolean addPoint(Point point){
        return addPoint(point, true);
    }
    
    /**
     * Adds a point to the shape.
     * 
     * @param point the point to add. Must be not null.
     * @param addLast true if the point must be appended to the shape, false if it must be add according to its sequence number.
     * @return true if the point is added, false otherwise.
     */
    public boolean addPoint(Point point, boolean addLast){
        if(addLast==true && !points.isEmpty())
            if(points.last().getSequence()>=point.getSequence())
                return false;
        
        return points.add(point);
    }
    
    /**
     * A point of the shape with WGS84 coordinates.
     */
    public static class Point{
        /**
         * The WGS84 latitude of the point.
         */
        private double lat;
        /**
         * The WGS84 longitude of the point.
         */
        private double lon;
        /**
         * The sequence number of the point.
         */
        private int sequence;
        /**
         * The distance traveled from the first point.
         */
        private double distTraveled=-1;
        
        /**
         * A Shape Point constructor without the distance from the starting point.
         * 
         * @param lat The latitude of the point. Must be a valid WGS84 latitude value.
         * @param lon The longitude of the point. Must be a valid WGS84 longitude value.
         * @param sequence The sequence number of the point. Must be a non negative number.
         */
        public Point(double lat, double lon, int sequence){
            if(lat<-90. || lat>90.)
                throw new IllegalArgumentException("Invalid WGS84 latitude value");
            if(lon<-180. || lon>180.)
                throw new IllegalArgumentException("Invalid WGS84 longitude value");
            if(sequence<0)
                throw new IllegalArgumentException("Invalid sequence number");
            this.lat = lat;
            this.lon = lon;
            this.sequence = sequence;
        }
        
        /**
         * The Shape Point constructor with all possible parameters.
         * 
         * @param lat The latitude of the point. Must be a valid WGS84 latitude value.
         * @param lon The longitude of the point. Must be a valid WGS84 longitude value.
         * @param sequence The sequence number of the point. Must be a non negative number.
         * @param dist The distance of the point from the first point. Must be a non negative number.
         */
        public Point(double lat, double lon, int sequence, double dist){
            this(lat, lon, sequence);
            if(dist<0)
                throw new IllegalArgumentException("Invalid distance value");
            this.distTraveled = dist;
        }
        
        public int getSequence(){
            return this.sequence;
        }
        
        public double getLon(){
            return this.lon;
        }
        
        public double getLat(){
            return this.lat;
        }
        @Override
        public String toString(){
            return String.format("(%.3f, %.3f, %d)",lat, lon, sequence);
        }
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
    
    @Override
    public boolean equals(Object o){
        if(!(o instanceof Shape)) return false;
        Shape s = (Shape) o;
        return id.equals(s.id);
    }
    
    public String getId(){
        return id;
    }
    
    @Override
    public String toString(){
        return "ShapeId : " + id + ", Points : " + points.toString();
    }
}
