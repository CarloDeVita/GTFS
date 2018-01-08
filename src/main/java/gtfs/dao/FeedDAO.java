package gtfs.dao;

import demo.hibernate.HibernateUtil;
import gtfs.Feed;
import gtfs.entities.Stop;
import java.util.LinkedList;

public class FeedDAO {
    public void save(Feed feed){
        HibernateUtil hibernateUtil = HibernateUtil.getInstance();
        hibernateUtil.saveCollection(feed.getAgencies());
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
        System.out.println("|------------ Saved frequencies-----------|");
    }
}
