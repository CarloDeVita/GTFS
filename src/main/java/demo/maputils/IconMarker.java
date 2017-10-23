/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demo.maputils;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.MapMarkerCircle;


public class IconMarker extends MapMarkerCircle {

    private Image image;

    public IconMarker(Coordinate coord, Image image) {
        this(coord, 0.125, image);
    }

    public IconMarker(Coordinate coord, double radius, Image image) {
        super(coord, radius);
        this.image = image;
    }

    @Override
    public void paint(Graphics g, Point position, int radio) {
        
        double r =getRadius();
        int width = (int) (this.image.getWidth(null) * r);
        int height = (int) (this.image.getHeight(null) * r);
        int w2 = width / 2;
        int h2 = height / 2;
        g.drawImage(this.image, position.x -w2, position.y-height, width, height, null);
        this.paintText(g, position);
    }

    public Image getImage() {
        return this.image;
    }

    public void setImage(Image image) {
        this.image = image;
    }
}
