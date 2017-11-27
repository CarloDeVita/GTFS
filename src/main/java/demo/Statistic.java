/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demo;

import java.util.HashSet;


public class Statistic {
    private HashSet<String> pullman;
    private int[] freqs;
    
    public Statistic(){}
    
    public Statistic(HashSet<String> pullman, int[] freqs) {
        this.pullman = pullman;
        this.freqs = freqs;
    }

    public void setPullman(HashSet<String> pullman) {
        this.pullman = pullman;
    }

    public void setFreqs(int[] freqs) {
        this.freqs = freqs;
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
    
}
