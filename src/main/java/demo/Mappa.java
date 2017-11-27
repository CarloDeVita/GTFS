package demo;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.MouseInputListener;
import net.sf.dynamicreports.report.exception.DRException;
import org.openstreetmap.gui.jmapviewer.DefaultMapController;
import org.openstreetmap.gui.jmapviewer.JMapViewer;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author cdevi
 */
public class Mappa extends javax.swing.JFrame {
    JMapViewer map;
    MapController controller;
    
    public Mappa(JMapViewer map,MapController controller) {
        this.map = map;
        this.controller = controller;
        initComponents();
        
        
    }

    public void setRouteList(String[] routes){
        namesList.setModel(new javax.swing.DefaultComboBoxModel<>(routes));
    }
    
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jDialog1 = new javax.swing.JDialog();
        jDialog2 = new javax.swing.JDialog();
        jDialog3 = new javax.swing.JDialog();
        mapPanel = map;
        selectionPanel = new javax.swing.JPanel();
        show = new javax.swing.JButton();
        namesList = new javax.swing.JComboBox<>();
        clearButton = new javax.swing.JButton();
        routeLabel = new javax.swing.JLabel();
        chooser = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        timeFromLabel = new javax.swing.JLabel();
        timeToLabel = new javax.swing.JLabel();
        availabilityButton = new javax.swing.JButton();
        timeFrom = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        dateFrom = new org.jdesktop.swingx.JXDatePicker();
        dateTo = new org.jdesktop.swingx.JXDatePicker();
        dateFromLabel = new javax.swing.JLabel();
        dateToLabel = new javax.swing.JLabel();
        timeTo = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        snap = new javax.swing.JButton();

        javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(jDialog1.getContentPane());
        jDialog1.getContentPane().setLayout(jDialog1Layout);
        jDialog1Layout.setHorizontalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jDialog1Layout.setVerticalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jDialog2Layout = new javax.swing.GroupLayout(jDialog2.getContentPane());
        jDialog2.getContentPane().setLayout(jDialog2Layout);
        jDialog2Layout.setHorizontalGroup(
            jDialog2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jDialog2Layout.setVerticalGroup(
            jDialog2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jDialog3Layout = new javax.swing.GroupLayout(jDialog3.getContentPane());
        jDialog3.getContentPane().setLayout(jDialog3Layout);
        jDialog3Layout.setHorizontalGroup(
            jDialog3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jDialog3Layout.setVerticalGroup(
            jDialog3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        mapPanel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));

        javax.swing.GroupLayout mapPanelLayout = new javax.swing.GroupLayout(mapPanel);
        mapPanel.setLayout(mapPanelLayout);
        mapPanelLayout.setHorizontalGroup(
            mapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 450, Short.MAX_VALUE)
        );
        mapPanelLayout.setVerticalGroup(
            mapPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        selectionPanel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));

        show.setText("Show route");
        show.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showActionPerformed(evt);
            }
        });

        namesList.setModel(new javax.swing.DefaultComboBoxModel<>());
        namesList.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                namesListItemStateChanged(evt);
            }
        });

        clearButton.setText("Clear");
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        routeLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        routeLabel.setText("Route");

        chooser.setText("Choose GTFS");
        chooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chooserActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel3.setText("Time");

        timeFromLabel.setText("From: ");

        timeToLabel.setText("To: ");

        availabilityButton.setText("Availability");
        availabilityButton.setEnabled(false);
        availabilityButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                availabilityButtonActionPerformed(evt);
            }
        });

        String[] times = new String[24];
        for(int i = 0;i<24;i++){
            times[i] = i+":00";
        }
        timeFrom.setModel(new javax.swing.DefaultComboBoxModel<>(times));
        timeFrom.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                timeFromItemStateChanged(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel6.setText("Date");

        dateFrom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dateFromActionPerformed(evt);
            }
        });

        dateTo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dateToActionPerformed(evt);
            }
        });

        dateFromLabel.setText("From:");

        dateToLabel.setText("To:");

        timeTo.setModel(new javax.swing.DefaultComboBoxModel<>(times));

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel1.setText("GTFS");

        snap.setText("Snap");
        snap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                snapActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout selectionPanelLayout = new javax.swing.GroupLayout(selectionPanel);
        selectionPanel.setLayout(selectionPanelLayout);
        selectionPanelLayout.setHorizontalGroup(
            selectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(selectionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(selectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(selectionPanelLayout.createSequentialGroup()
                        .addGap(35, 35, 35)
                        .addGroup(selectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(chooser, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(selectionPanelLayout.createSequentialGroup()
                                .addGap(2, 2, 2)
                                .addGroup(selectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(namesList, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(selectionPanelLayout.createSequentialGroup()
                                        .addComponent(show, javax.swing.GroupLayout.PREFERRED_SIZE, 181, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(37, 37, 37)
                                        .addComponent(clearButton, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE))))))
                    .addComponent(jLabel1)
                    .addComponent(routeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(selectionPanelLayout.createSequentialGroup()
                        .addGroup(selectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(selectionPanelLayout.createSequentialGroup()
                                .addGap(28, 28, 28)
                                .addGroup(selectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, selectionPanelLayout.createSequentialGroup()
                                        .addComponent(timeFromLabel)
                                        .addGap(18, 18, 18)
                                        .addComponent(timeFrom, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGap(64, 64, 64))
                                    .addGroup(selectionPanelLayout.createSequentialGroup()
                                        .addComponent(dateFromLabel)
                                        .addGap(18, 18, 18)
                                        .addComponent(dateFrom, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGap(18, 18, 18))))
                            .addGroup(selectionPanelLayout.createSequentialGroup()
                                .addGap(10, 10, 10)
                                .addComponent(snap)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                        .addGroup(selectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(selectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(selectionPanelLayout.createSequentialGroup()
                                    .addComponent(timeToLabel)
                                    .addGap(18, 18, 18)
                                    .addComponent(timeTo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, selectionPanelLayout.createSequentialGroup()
                                    .addComponent(dateToLabel)
                                    .addGap(18, 18, 18)
                                    .addComponent(dateTo, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 3, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(selectionPanelLayout.createSequentialGroup()
                                .addGap(37, 37, 37)
                                .addComponent(availabilityButton, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jLabel6)
                    .addComponent(jLabel3))
                .addContainerGap(34, Short.MAX_VALUE))
        );

        selectionPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {dateFrom, dateTo, timeFrom, timeTo});

        selectionPanelLayout.setVerticalGroup(
            selectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(selectionPanelLayout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(chooser)
                .addGap(18, 18, 18)
                .addComponent(routeLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(namesList, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(selectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(show)
                    .addComponent(clearButton))
                .addGap(79, 79, 79)
                .addComponent(jLabel6)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(selectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dateFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dateTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dateFromLabel)
                    .addComponent(dateToLabel))
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addGap(15, 15, 15)
                .addGroup(selectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(timeToLabel)
                    .addComponent(timeFromLabel)
                    .addComponent(timeFrom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(timeTo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 63, Short.MAX_VALUE)
                .addGroup(selectionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(availabilityButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(snap))
                .addContainerGap())
        );

        selectionPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {dateFrom, dateTo, timeFrom, timeTo});

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mapPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(selectionPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(mapPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(selectionPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void timeFromItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_timeFromItemStateChanged
        int startTime = timeFrom.getSelectedIndex();
        String[] times = new String[24-startTime];
        int j=0;
        for(int i = startTime+1;i<25;i++){
            times[j++] = i+":00";
        }
        timeTo.setModel(new javax.swing.DefaultComboBoxModel<>(times));
    }//GEN-LAST:event_timeFromItemStateChanged

    
    /**
     * 
     * @param d d==0 from, d==1 to
     * @return 
     */
    public LocalDate getDate(int d){
        Date date = (d==0)? dateFrom.getDate() : dateTo.getDate();
        Instant instant = Instant.ofEpochMilli(date.getTime());
        LocalDate localDate = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate();
        return localDate;
    }
    
    public String getTime(int t) {
        String time = (t==0)? (String)timeFrom.getSelectedItem() : (String)timeTo.getSelectedItem();
        return time;
    }
    
    private void availabilityButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_availabilityButtonActionPerformed
        String nameRoute = (String)namesList.getSelectedItem();
        controller.statistic(nameRoute);
        
        
        /*Date date = dateFrom.getDate();
        Instant instant = Instant.ofEpochMilli(date.getTime());
        LocalDate localDate = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate();
        DayOfWeek day = localDate.getDayOfWeek();
        String nameRoute = (String)namesList.getSelectedItem();
        Route r = feed.getRouteByName(nameRoute);
        HashSet<Stop> stops = new HashSet<>();
        Image im=null;
        try {
            im = ImageIO.read(getClass().getClassLoader().getResourceAsStream("station.png"));
        } catch (IOException ex) {
            Logger.getLogger(Mappa.class.getName()).log(Level.SEVERE, null, ex);
        }
        for(Trip t : r.getTrips()){
            Calendar cal = t.getCalendar();
            Map<DayOfWeek,Boolean> days = cal.getServiceDays();
            Map<LocalDate,Boolean> exc = cal.getExceptions();
            Boolean active = null;

            if ((active = exc.get(localDate))==null || active != true )
            if((active=days.get(day))==null || active!=true )
            continue;

            Shape s = t.getShape();
            List<Coordinate> shapel = new LinkedList<>();
            for(Shape.Point p : s.getPoints()){
                Coordinate c = new Coordinate( p.getLat(),p.getLon()) ;
                shapel.add(new Coordinate(p.getLat(),p.getLon()));
            }
            map.addMapPolygon(new MapLine(shapel));
            for(StopTime stopT : t.getStopTimes()){
                stops.add(stopT.getStop());
            }
        }
        for(Stop stop : stops){
            map.addMapMarker(new IconMarker(new Coordinate(stop.getLat(),stop.getLon()), im));
        }*/
    }//GEN-LAST:event_availabilityButtonActionPerformed

    private void chooserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chooserActionPerformed
        controller.chooseGtfs();
    }//GEN-LAST:event_chooserActionPerformed

    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        map.removeAllMapPolygons();
        map.removeAllMapMarkers();
    }//GEN-LAST:event_clearButtonActionPerformed

    private void showActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showActionPerformed
        String selected = (String)namesList.getSelectedItem();
        if(selected != null && !selected.equals(""))
            controller.showRoute((String)namesList.getSelectedItem());
    }//GEN-LAST:event_showActionPerformed

    private void snapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_snapActionPerformed
       String name = (String)namesList.getSelectedItem();
       controller.snap(name);
    }//GEN-LAST:event_snapActionPerformed

    private void dateFromActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dateFromActionPerformed
        Date d = dateFrom.getDate();
        dateTo.getMonthView().setLowerBound(d);
        Date d2 = dateTo.getDate();
        if(d == null) availabilityButton.setEnabled(false);
        else{
            if(d2 == null) dateTo.setDate(d);
            availabilityButton.setEnabled(true);
        }
        controller.clearStatistic();
    }//GEN-LAST:event_dateFromActionPerformed

    private void dateToActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dateToActionPerformed
        Date d = dateFrom.getDate();
        Date d2 = dateTo.getDate();
        if(d!=null && d2!=null) availabilityButton.setEnabled(true);
        else availabilityButton.setEnabled(false);
        controller.clearStatistic();
    }//GEN-LAST:event_dateToActionPerformed

    private void namesListItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_namesListItemStateChanged
        controller.clearStatistic();
    }//GEN-LAST:event_namesListItemStateChanged

    public String getSelectedRoute(){
        return (String)namesList.getSelectedItem();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton availabilityButton;
    private javax.swing.JButton chooser;
    private javax.swing.JButton clearButton;
    private org.jdesktop.swingx.JXDatePicker dateFrom;
    private javax.swing.JLabel dateFromLabel;
    private org.jdesktop.swingx.JXDatePicker dateTo;
    private javax.swing.JLabel dateToLabel;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JDialog jDialog2;
    private javax.swing.JDialog jDialog3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel mapPanel;
    private javax.swing.JComboBox<String> namesList;
    private javax.swing.JLabel routeLabel;
    private javax.swing.JPanel selectionPanel;
    private javax.swing.JButton show;
    private javax.swing.JButton snap;
    private javax.swing.JComboBox<String> timeFrom;
    private javax.swing.JLabel timeFromLabel;
    private javax.swing.JComboBox<String> timeTo;
    private javax.swing.JLabel timeToLabel;
    // End of variables declaration//GEN-END:variables

    public void disableInput() {
        for(Component c : selectionPanel.getComponents()){
            if(c!=availabilityButton) c.setEnabled(false);    
        }
    }
    
    public void enableInput(){
        for(Component c : selectionPanel.getComponents()){
            if(c!=availabilityButton) c.setEnabled(true);    
        }
    }

   
    
    
}
