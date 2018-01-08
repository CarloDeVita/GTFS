package demo.hibernate;

import java.util.Collection;
import java.util.Properties;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

/**
 * Hibernate Utility class with a convenient method to get Session Factory
 * object.
 * Creates the session factory from the standard (hibernate.cfg.xml) file.
 */
public class HibernateUtil {
    private static HibernateUtil instance;
    private SessionFactory sessionFactory;
    private static String defaultCatalog;
    
    /**
     * Sets the default catalog for the session factory.
     * 
     * @param catalog the default catalog to set. Null is ignored.
     */
    public synchronized static void setDefaultCatalog(String catalog){
        if(catalog==null) return;
        defaultCatalog = catalog;
    }
    
    public static synchronized HibernateUtil getInstance(){
        if(instance==null)
            instance = new HibernateUtil();
        return instance;
    }
    
    private HibernateUtil(){
        try {
            // Create the SessionFactory from standard (hibernate.cfg.xml) 
            // config file.
            Configuration config = new Configuration().configure();
            if(defaultCatalog!=null)
                config.setProperty("hibernate.default_catalog", defaultCatalog);
            sessionFactory = config.buildSessionFactory();
        } catch (Throwable ex) {
            // Log the exception. 
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }
    
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }
    
    public void saveOrUpdateObject(Object object){
        try (Session session = openSession()) {
            Transaction tx = session.beginTransaction();
            session.saveOrUpdate(object);
            tx.commit();
        }
    }
    
    public void saveOrUpdateCollection(Collection<?> c){
        Session session = openSession();
        int count = 0;
        Transaction tx = session.beginTransaction();
        for(Object o : c){
            if(count==100){
                count = 0;
                tx.commit();
                session.close();
                session = openSession();
                tx = session.beginTransaction();
            }
            session.saveOrUpdate(o);
            count++;
        }
        tx.commit();
        session.close();
    }
    
    public void save(Object object){
        try(Session session = openSession()){
            Transaction tx = session.beginTransaction();
            session.save(object);
            tx.commit();
        }
    }
    
    public void saveCollection(Collection<?> objects){
        try(Session session = openSession()){
            int count = 0;
            Transaction tx = session.beginTransaction();
            for(Object o : objects){
                if(count==1000){
                    count = 0;
                    tx.commit();
                    tx = session.beginTransaction();
                }
                session.saveOrUpdate(o);
                count++;
            }
            tx.commit();
        }
    }

    public Session openSession() {
        return sessionFactory.openSession();
    }
    
    public void close(){
        if(sessionFactory!=null)
            sessionFactory.close();
        StandardServiceRegistryBuilder.destroy(sessionFactory.getSessionFactoryOptions().getServiceRegistry());
    }
}
