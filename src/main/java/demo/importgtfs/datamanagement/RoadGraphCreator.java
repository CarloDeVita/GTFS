package demo.importgtfs.datamanagement;

import demo.hibernate.HibernateUtil;
import java.sql.SQLException;
import org.hibernate.Session;
import org.hibernate.procedure.ProcedureCall;

/**
 * Divides the streets into segments that form a graph.
 * 
 */
public class RoadGraphCreator {
    
    public boolean createStreetGraph() throws SQLException{
        
        HibernateUtil hibernateUtil = HibernateUtil.getInstance();
        Session session = hibernateUtil.openSession();
        ProcedureCall procedure = session.createStoredProcedureCall("PopulateSegments");
        procedure.setTimeout(0); // indeterminate time
        procedure.execute();
        String result = (String) procedure.getSingleResult();
        session.close();
        return result.equals("OK");
    }
}
