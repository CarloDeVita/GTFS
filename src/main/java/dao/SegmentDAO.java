/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import com.vividsolutions.jts.geom.Geometry;
import demo.hibernate.HibernateUtil;
import org.hibernate.Session;

/**
 *
 * @author cdevi
 */
public class SegmentDAO {
    public Geometry getBound(){
        String query = "select extent(s.segment) from Segment s";
        Session s = HibernateUtil.getInstance().openSession();
        s.beginTransaction();
        Geometry g = s.createQuery(query,Geometry.class).getSingleResult();
        s.close();
        return g;
    }
}
