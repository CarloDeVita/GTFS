/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package demo.importgtfs;

import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;

public class ProgressDialog extends javax.swing.JDialog {

    private JLabel labels[];
    private JProgressBar pBars[];
    //private JScrollPane textScrollPane;
    //private JTextArea textArea;
    private long startTimes[];
    
    
    /**
     * 
     */
    public ProgressDialog(java.awt.Frame parent, String title, boolean modal, String tasks[]) {
        super(parent, title,modal);
        if(tasks==null || tasks.length==0) throw new IllegalArgumentException(); //TODO
        
        //ProgressBarUI ui = new javax.swing.plaf.basic.BasicProgressBarUI();
        pBars = new JProgressBar[tasks.length];
        labels = new JLabel[tasks.length];
        startTimes = new long[tasks.length];
        for(int i=0 ; i<tasks.length ; i++){
           labels[i] = new JLabel(tasks[i]);
           pBars[i] = new JProgressBar();
           //pBars[i].setUI(ui);
        }
        
        
        /*textArea = new JTextArea();
        textArea.setColumns(20);
        textArea.setRows(5);
        textArea.setEditable(false);
        //textScrollPane = new JScrollPane();
        //textScrollPane.setViewportView(textArea);*/
        
        
        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        // create horizontal layout
        ParallelGroup parallelH = layout.createParallelGroup(GroupLayout.Alignment.LEADING);        
        //parallelH.addComponent(textScrollPane, GroupLayout.DEFAULT_SIZE, 421, Short.MAX_VALUE);
        for(int i=0 ; i<tasks.length ; i++) //add the group composed by the label and the progress bar
            parallelH.addGroup(layout.createSequentialGroup()
                    .addComponent(labels[i])
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pBars[i], javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE));
        
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(parallelH)
                .addContainerGap())
        );
        
        SequentialGroup seqV = layout.createSequentialGroup();
        for(int i=0 ; i<tasks.length ; i++){
            if(i==0)
                seqV.addGap(18, 18, 18);
            else
                seqV.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED);
            
            seqV.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(labels[i])
                    .addComponent(pBars[i], javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE));
        }
        
        seqV.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 20/*50*/, Short.MAX_VALUE)
                //.addComponent(textScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap();
            
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(seqV)
        );
        
        pack();
        setPreferredSize(new Dimension(getWidth()+20,getHeight()));
        
        setResizable(false);
    }
    
    public static final int OPERATION_START = 1;
    public static final int OPERATION_SUCCESS = 2;
    public static final int OPERATION_FAILED = 3;
    
    /**
     * Sets the status of a task. The dialog will display the changes in the progress bars.
     * 
     * @param op the 1-based operation number
     * @param status the status of the task
     */
    public void setStatus(int op, int status){
        int hours,minutes;
        double seconds;
        
        op -= 1;
        switch(status){
            case OPERATION_START :
                startTimes[op] = System.currentTimeMillis();
                pBars[op].setIndeterminate(true);
                pBars[op].setStringPainted(true);
                pBars[op].setString("Executing . . .");
                break;
            case OPERATION_SUCCESS :
                try{
                pBars[op].setIndeterminate(false);
                pBars[op].setValue(100);
                pBars[op].setString("COMPLETED");
                seconds = (System.currentTimeMillis()-startTimes[op])/1000.;
                hours = (int) (seconds/3600.);
                seconds -= hours*3600;
                minutes = (int) (seconds/60.);
                seconds -= minutes*60;
                //append(String.format("%s took %d hours, %d minutes and %.2f seconds", labels[op].getText(), hours, minutes, seconds));
                }catch(Exception e ){
                    System.err.println(e.getMessage());
                }
                break;
            case OPERATION_FAILED :
                pBars[op].setIndeterminate(false);
                pBars[op].setValue(0);
                pBars[op].setString("FAILED");
                break;
        }
    }
    
    /*public void append(String message){
        textArea.append(message);
        textArea.append("\n");
    }*/

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ProgressDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ProgressDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ProgressDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ProgressDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                ProgressDialog dialog = new ProgressDialog(new javax.swing.JFrame(),"ciao",true,  new String[]{"finchè la barca va tu lasciala andare che va bene e anche se non va bene ci arrangiamo","PEPPE","Francesco", "mammt"});
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
