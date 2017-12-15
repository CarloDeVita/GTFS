/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demo.maputils;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Path2D;
import java.util.LinkedList;
import java.util.List;
import org.openstreetmap.gui.jmapviewer.MapPolygonImpl;
import org.openstreetmap.gui.jmapviewer.interfaces.ICoordinate;

public class MapLine extends MapPolygonImpl {
    Graphics2D g2d;
    Path2D path2d;
    public MapLine(List<? extends ICoordinate> points) {
        super(points);
    }
    
    
    
    public void repaint(Color c){
        g2d.setColor(c);
        g2d.draw(path2d);
    }
    
    @Override
    public void paint(Graphics g, List<Point> points) {
        if (g2d==null){ 
            g2d = (Graphics2D) g.create();
            g2d.setColor(Color.BLUE);
        }
        g2d.setStroke(getStroke());
        path2d = buildPath(points);
        g2d.draw(path2d);
    }

    private Path2D buildPath(List<Point> points) {
        Path2D path = new Path2D.Double();
        if (points != null && points.size() > 0) {
            Point firstPoint = points.get(0);
            path.moveTo(firstPoint.getX(), firstPoint.getY());
            for (Point p : points) {
                path.lineTo(p.getX(), p.getY());
            }
        }
        return path;
    }
}    

