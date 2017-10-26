package gtfs.entities;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import java.util.SortedSet;
import java.util.Collections;
import java.util.TreeSet;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * The physical path the vehicle takes.
 * 
 * @see <a href="https://developers.google.com/transit/gtfs/reference/#shapestxt">GTFS Overview - Shape</a>
 */
@Entity
@Table(name="shapes", schema="gtfs", catalog="postgis_test")
public class Shape extends GTFS{
    private String id;
    private SortedSet<Point> points = new TreeSet<>((Point o1, Point o2) -> (o2.sequenceNumber-o1.sequenceNumber));
    
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
    
    public void setId(String id) {
        this.id = id;
    }

    public void setPoints(SortedSet<Point> points) {
        this.points = points;
    }

    /**
     * 
     * @return a read-only ordered view of the point that form the shape.
     */
    @OneToMany(targetEntity=Shape.Point.class)
    @JoinColumn(name="points", nullable=false)
    public SortedSet<Point> getPoints(){
        return Collections.unmodifiableSortedSet(points);
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
            if(points.last().getSequenceNumber()>=point.getSequenceNumber())
                return false;
        
        return points.add(point);
    }
    
    /**
     * A point of the shape with WGS84 coordinates.
     */
    @Entity
    @Table(name="shape_points", schema="gtfs", catalog="postgis_test")
    public static class Point{
        private static GeometryFactory factory;
        private com.vividsolutions.jts.geom.Point coordinate; 
        /**
         * The sequence number of the point.
         */
        private int sequenceNumber;
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
            
            if(factory==null){
                PrecisionModel precision = new PrecisionModel(PrecisionModel.FLOATING);
                int srid = 4326; //WGS 84
                factory = new GeometryFactory(precision, srid);
            }
            
            this.coordinate = factory.createPoint(new Coordinate(lon, lat));
            this.sequenceNumber = sequence;
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
        
        public int getSequenceNumber(){
            return this.sequenceNumber;
        }
        
        @Transient
        public double getLon(){
            return coordinate.getX();
        }
        
        @Transient
        public double getLat(){
            return coordinate.getY();
        }
        
        public com.vividsolutions.jts.geom.Point getCoordinate(){
            return coordinate;
        } 
        
        @Override
        public String toString(){
            return String.format("(%.3f, %.3f, %d)",getLat(), getLon(), sequenceNumber);
        }

        public void setCoordinate(com.vividsolutions.jts.geom.Point coordinate) {
            this.coordinate = coordinate;
        }

        public void setSequenceNumber(int sequenceNumber) {
            this.sequenceNumber = sequenceNumber;
        }

        public void setDistTraveled(double distTraveled) {
            this.distTraveled = distTraveled;
        }

        public double getDistTraveled() {
            return distTraveled;
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
    
    @Id
    public String getId(){
        return id;
    }
    
    @Override
    public String toString(){
        return "ShapeId : " + id + ", Points : " + points.toString();
    }
}
