package datamanagement;

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
        ProcedureCall procedure = session.createStoredProcedureCall("matchseg");
        procedure.setTimeout(0); // indeterminate time
        procedure.execute();
        session.close();
        return true;
    }
}
