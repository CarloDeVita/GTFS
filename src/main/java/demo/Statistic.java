/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demo;


import com.vividsolutions.jts.geom.Point;
import java.awt.Color;
import java.util.HashSet;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;


public class Statistic {
    private Point p;
    private HashSet<String> pullman;
    private int[] freqs;
    private int start; //inizio tempo
    private int end;
    private MapMarkerDot mmd;//Per ora sono punti, poi saranno linee

    public Statistic(Point p){
        this.p = p;
    }
    
    public Statistic(Point p, MapMarkerDot mmd){
        this.p = p;
        this.mmd = mmd;
    }
    
    public Statistic(HashSet<String> pullman, int[] freqs) {
        this.pullman = pullman;
        this.freqs = freqs;
    }
        
    public Point getPoint() {
        return p;
    }
    
    public void setPullman(HashSet<String> pullman) {
        this.pullman = pullman;
    }

    public void setFreqs(int[] freqs) {
        this.freqs = freqs;
    }
    
    public void setInterval(int start,int end){
        System.out.println("S: "+start+" E: "+end);
        this.start = start;
        this.end = end;
        System.out.println("S: "+start+" E: "+end);
    }

    public HashSet<String> getPullman() {
        return pullman;
    }

    public int[] getFreqs() {
        return freqs;
    }
    
    public boolean addPullman(String route){
        if(pullman == null) pullman = new HashSet<>();
        return pullman.add(route);
    }
    
    public void colorSegment(){
        int sum = 0;
        for(int i=start;i<end;i++){
            sum +=freqs[i];
            System.out.println("freq["+i+"="+freqs[i]);
        }
        if(sum>10) mmd.setColor(Color.blue);
        else if(sum>5 )mmd.setColor(Color.red);
        else mmd.setColor(Color.black);
       
    }
                
    
    @Override
    public int hashCode(){
        return p.hashCode();
    }
    
    @Override
    public boolean equals(Object o){
        if(!(o instanceof Statistic)) return false;
        Statistic s = (Statistic)o;
        return p.equals(s.p);
    }
    
}
