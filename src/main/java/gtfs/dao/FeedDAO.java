package gtfs.dao;

import demo.hibernate.HibernateUtil;
import gtfs.Feed;
import gtfs.entities.Frequency;
import gtfs.entities.Shape;
import gtfs.entities.Stop;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FeedDAO {
    public void save(final Feed feed){
        final HibernateUtil hibernateUtil = HibernateUtil.getInstance();
        Thread threads[] = new Thread[4];
        long time = System.currentTimeMillis();
        threads[0] = new Thread(){
                        @Override
                        public void run(){
                            hibernateUtil.saveCollection(feed.getAgencies());
                            System.out.println("|------------ Saved agencies -----------|");
                            hibernateUtil.saveCollection(feed.getRoutes());
                            System.out.println("|------------ Saved routes -----------|");
                        }
        };
        threads[0].start();

        final Collection<Shape> shp = feed.getShapes();
        if(shp!=null){
            threads[1] = new Thread(){
                            @Override
                            public void run(){
                                hibernateUtil.saveCollection(shp);
                                System.out.println("|------------ Saved shapes -----------|");
                            }
                        };
            threads[1].start();
        }
        
        threads[2] = new Thread(){
                        @Override
                        public void run(){
                            hibernateUtil.saveCollection(feed.getCalendars());
                            System.out.println("|------------ Saved calendars -----------|");
                        }
                    };
        threads[2].start();
        
        threads[3] = new Thread(){
                        @Override
                        public void run(){
                            LinkedList<Stop> noParent = new LinkedList<>();
                            LinkedList<Stop> withParent = new LinkedList<>();
                            for(Stop s : feed.getStops()){
                                if(s.getParent()==null)
                                    noParent.add(s);
                                else
                                    withParent.add(s);
                            }
                            hibernateUtil.saveCollection(noParent);
                            hibernateUtil.saveCollection(withParent);
                            System.out.println("|------------ Saved stops -----------|");
                        }
                     };
        threads[3].start();
        
        try{
            for(Thread t : threads)
                if(t!=null) t.join();
        }catch(InterruptedException ex){
            return;
        }
        
        hibernateUtil.saveCollection(feed.getTrips());
        System.out.println("|------------ Saved trips -----------|");
        
        final Collection<Frequency> frequencies = feed.getFrequencies();
        if(frequencies!=null){
            threads[0] = new Thread(){
                            @Override
                            public void run(){
                                hibernateUtil.saveCollection(feed.getStopTimes());
                                System.out.println("|------------ Saved stop times -----------|");
                            }
                    };
            threads[0].start();
            hibernateUtil.saveCollection(frequencies);
            System.out.println("|------------ Saved frequencies-----------|");
            try {
                threads[0].join();
            } catch (InterruptedException ex) {
                return;
            }
        }
        else{
            hibernateUtil.saveCollection(feed.getStopTimes());
            System.out.println("|------------ Saved stop times -----------|");
        }
        time = System.currentTimeMillis()-time;
        
        System.out.println("Feed saved in "+time/1000. +" seconds");
        
        /*hibernateUtil.saveCollection(feed.getAgencies());
        System.out.println("|------------ Saved agencies -----------|");
        hibernateUtil.saveCollection(feed.getRoutes());
        System.out.println("|------------ Saved routes -----------|");
        hibernateUtil.saveCollection(feed.getCalendars());
        System.out.println("|------------ Saved calendars -----------|");
        if(feed.getShapes()!=null)
            hibernateUtil.saveCollection(feed.getShapes());
        System.out.println("|------------ Saved shapes -----------|");
        hibernateUtil.saveCollection(feed.getTrips());
        System.out.println("|------------ Saved trips -----------|");
        LinkedList<Stop> noParent = new LinkedList<>();
        LinkedList<Stop> withParent = new LinkedList<>();
        for(Stop s : feed.getStops()){
            if(s.getParent()==null)
                noParent.add(s);
            else
                withParent.add(s);
        }
        hibernateUtil.saveCollection(noParent);
        hibernateUtil.saveCollection(withParent);
        System.out.println("|------------ Saved stops -----------|");
        hibernateUtil.saveCollection(feed.getStopTimes());
        System.out.println("|------------ Saved stop times -----------|");
        if(feed.getFrequencies()!=null)
            hibernateUtil.saveCollection(feed.getFrequencies());
        System.out.println("|------------ Saved frequencies-----------|");*/
    }
}
