/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * BatchAnalysisForm.java
 *
 * Created on Feb 15, 2010, 12:51:07 PM
 */

package savant.analysis;

import savant.analysis.BatchAnalysis.FIREONEVENT;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.swing.DefaultListModel;

/**
 *
 * @author mfiume
 */
public class BatchAnalysisForm extends javax.swing.JFrame {

    BatchAnalysis batchAnalysis = null;

    List<String> runWhenEvents = new ArrayList<String>();
    HashMap<String,FIREONEVENT> runWhenEventToEnum = new HashMap<String,FIREONEVENT>();

    List<String> analyses = new ArrayList<String>();
    HashMap<String,Analysis> analysisNameToInstance = new HashMap<String,Analysis>();

    /** Creates new form BatchAnalysisForm */
    public BatchAnalysisForm() {

        initComponents();
        initRunWhenEvents();
        initAnalyses();

        populateLists();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        list_runWhen = new javax.swing.JList(new DefaultListModel());
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        list_analyses = new javax.swing.JList(new DefaultListModel());
        button_addBatchAnalysis = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Create Batch Analysis");

        jLabel1.setText("Run when:");

        list_runWhen.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(list_runWhen);

        jLabel2.setText("Analyses to run (Ctrl+click to select multiple):");

        jScrollPane2.setViewportView(list_analyses);

        button_addBatchAnalysis.setText("Add");
        button_addBatchAnalysis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_addBatchAnalysisActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(button_addBatchAnalysis, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(button_addBatchAnalysis)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void button_addBatchAnalysisActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_addBatchAnalysisActionPerformed

        String eventName = (String) this.list_runWhen.getSelectedValue();

        List<String> analysesNames = new ArrayList<String>();
        for (Object o : this.list_analyses.getSelectedValues()) {
            analysesNames.add(o.toString());
        }

        FIREONEVENT foe = this.runWhenEventToEnum.get(eventName);

        List<AnalyzeEventListener> runAnalysisListeners = new ArrayList<AnalyzeEventListener>();

        for (String anName : analysesNames) {
            runAnalysisListeners.add(this.analysisNameToInstance.get(anName));
        }

        this.batchAnalysis = new BatchAnalysis(foe, runAnalysisListeners);

        this.dispose();;
    }//GEN-LAST:event_button_addBatchAnalysisActionPerformed

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new BatchAnalysisForm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton button_addBatchAnalysis;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList list_analyses;
    private javax.swing.JList list_runWhen;
    // End of variables declaration//GEN-END:variables

    private void initRunWhenEvents() {
        this.addRunWhenEvent("On demand", FIREONEVENT.ON_DEMAND);
        this.addRunWhenEvent("Range changes", FIREONEVENT.RANGE_CHANGE);
        this.addRunWhenEvent("Bookmark added", FIREONEVENT.BOOKMARK_CHANGE);
    }

    private void addRunWhenEvent(String name, FIREONEVENT foe) {
        this.runWhenEvents.add(name);
        this.runWhenEventToEnum.put(name, foe);
    }

    private void initAnalyses() {
        this.addAnalyses("Screen shot", new ScreenShotAnalysis());
    }

    private void addAnalyses(String name, Analysis a) {
        this.analyses.add(name);
        this.analysisNameToInstance.put(name, a);
    }

    private void populateLists() {
        populateRunWhenList();
        populateAnalysesList();
    }

    private void populateRunWhenList() {
        DefaultListModel model = (DefaultListModel) this.list_runWhen.getModel();
        for (int i = 0; i < this.runWhenEvents.size(); i++) {
            model.add(i, runWhenEvents.get(i));
        }
    }

    private void populateAnalysesList() {
        DefaultListModel model = (DefaultListModel) this.list_analyses.getModel();
        for (int i = 0; i < this.analyses.size(); i++) {
            model.add(i, analyses.get(i));
        }
    }

    public BatchAnalysis getBatchAnalysis() {
        return this.batchAnalysis;
    }

   
}
