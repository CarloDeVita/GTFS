package demo.importgtfs.datamanagement;

import demo.hibernate.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.procedure.ProcedureCall;

/**
 * 
 * Matches the shapes of a GTFS feed with the street graph from OpenStreetMap.
 */
public class ShapeMatcher {
    public boolean match(){
        HibernateUtil hibernateUtil = HibernateUtil.getInstance();
        Session session = hibernateUtil.openSession();
        ProcedureCall procedure = session.createStoredProcedureCall("match_gtfs_shapes");
        procedure.setTimeout(0); // indeterminate time
        procedure.execute();
        session.close();
        return true;
    }
    
    public static void main(String args[]){
        new ShapeMatcher().match();
    }
}
