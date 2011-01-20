/*
 *    Copyright 2009-2010 University of Toronto
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

/*
 * BAMParametersDialog.java
 *
 * Created on Apr 20, 2010, 11:20:40 AM
 */

package savant.view.dialog;

import java.awt.*;
import javax.swing.ImageIcon;
import savant.util.SAMReadUtils;
import savant.view.swing.ImagePanel;

/**
 *
 * @author vwilliam
 */
public class BAMParametersDialog extends javax.swing.JDialog {

    private int discordantMin;
    private int discordantMax;
    private double arcLengthThreshold;
    private SAMReadUtils.PairedSequencingProtocol prot;

    private boolean cancelled = false;
    private boolean accepted  = true;

    /** Creates new form BAMParametersDialog */
    public BAMParametersDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        Image i = new ImageIcon(getClass().getResource("/savant/images/reads_opposite.png")).getImage();
        this.setPreferredSize(new Dimension(i.getWidth(null),i.getHeight(null)));
        this.setMaximumSize(new Dimension(i.getWidth(null),i.getHeight(null)));
        this.setMinimumSize(new Dimension(i.getWidth(null),i.getHeight(null)));
        ImagePanel ip = new ImagePanel(i);

        this.reads_opposite_container.setLayout(new BorderLayout());
        this.reads_opposite_container.add(ip, BorderLayout.CENTER);

        i = new ImageIcon(getClass().getResource("/savant/images/reads_same.png")).getImage();
        this.setPreferredSize(new Dimension(i.getWidth(null),i.getHeight(null)));
        this.setMaximumSize(new Dimension(i.getWidth(null),i.getHeight(null)));
        this.setMinimumSize(new Dimension(i.getWidth(null),i.getHeight(null)));
        ip = new ImagePanel(i);

        this.reads_same_container.setLayout(new BorderLayout());
        this.reads_same_container.add(ip, BorderLayout.CENTER);


        this.setModal(modal);
        this.getRootPane().setDefaultButton(buttonOK);
        this.setLocationRelativeTo(parent);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        textDiscordantMin = new javax.swing.JTextField();
        textDiscordantMax = new javax.swing.JTextField();
        textArcThreshold = new javax.swing.JTextField();
        buttonCancel = new javax.swing.JButton();
        buttonOK = new javax.swing.JButton();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        pairedend_button = new javax.swing.JRadioButton();
        matepair_button = new javax.swing.JRadioButton();
        reads_opposite_container = new javax.swing.JPanel();
        reads_same_container = new javax.swing.JPanel();

        buttonGroup1.add(pairedend_button);
        buttonGroup1.add(matepair_button);

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Read Pair Settings");

        jLabel1.setText("Min normal insert size:");

        jLabel2.setText("Max normal insert size:");

        jLabel3.setText("Ignore sizes smaller than:");

        textDiscordantMin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textDiscordantMinActionPerformed(evt);
            }
        });

        textDiscordantMax.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textDiscordantMaxActionPerformed(evt);
            }
        });

        textArcThreshold.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textArcThresholdActionPerformed(evt);
            }
        });

        buttonCancel.setText("Cancel");
        buttonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCancelActionPerformed(evt);
            }
        });

        buttonOK.setText("OK");
        buttonOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonOKActionPerformed(evt);
            }
        });

        jLabel4.setText("eg. 100 or 10%");

        jLabel5.setText("Pairs are sequenced from:");

        pairedend_button.setText("same strand");

        matepair_button.setSelected(true);
        matepair_button.setText("opposite strands");

        reads_opposite_container.setBackground(new java.awt.Color(255, 204, 204));
        reads_opposite_container.setPreferredSize(new java.awt.Dimension(144, 14));

        javax.swing.GroupLayout reads_opposite_containerLayout = new javax.swing.GroupLayout(reads_opposite_container);
        reads_opposite_container.setLayout(reads_opposite_containerLayout);
        reads_opposite_containerLayout.setHorizontalGroup(
            reads_opposite_containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 130, Short.MAX_VALUE)
        );
        reads_opposite_containerLayout.setVerticalGroup(
            reads_opposite_containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        reads_same_container.setBackground(new java.awt.Color(255, 204, 204));
        reads_same_container.setPreferredSize(new java.awt.Dimension(144, 14));

        javax.swing.GroupLayout reads_same_containerLayout = new javax.swing.GroupLayout(reads_same_container);
        reads_same_container.setLayout(reads_same_containerLayout);
        reads_same_containerLayout.setHorizontalGroup(
            reads_same_containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 130, Short.MAX_VALUE)
        );
        reads_same_containerLayout.setVerticalGroup(
            reads_same_containerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 19, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(buttonOK)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(textArcThreshold, javax.swing.GroupLayout.DEFAULT_SIZE, 162, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 89, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(matepair_button)
                                    .addComponent(pairedend_button))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(reads_opposite_container, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                                    .addComponent(reads_same_container, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)))
                            .addComponent(textDiscordantMin, javax.swing.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE)
                            .addComponent(textDiscordantMax, javax.swing.GroupLayout.DEFAULT_SIZE, 256, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(14, 14, 14))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(reads_opposite_container, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(8, 8, 8)))
                        .addComponent(reads_same_container, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(matepair_button)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pairedend_button)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textDiscordantMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textDiscordantMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(textArcThreshold, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonCancel)
                    .addComponent(buttonOK))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void textDiscordantMinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textDiscordantMinActionPerformed
//        parseDiscordantMin();
    }//GEN-LAST:event_textDiscordantMinActionPerformed

    private void textDiscordantMaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textDiscordantMaxActionPerformed
//        parseDiscordantMax();
    }//GEN-LAST:event_textDiscordantMaxActionPerformed

    private void textArcThresholdActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textArcThresholdActionPerformed
//        parseArcThreshold();
    }//GEN-LAST:event_textArcThresholdActionPerformed

    private void buttonCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonCancelActionPerformed
        setCancelled(true);
        setAccepted(false);
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_buttonCancelActionPerformed

    private void buttonOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonOKActionPerformed
        if (parseProtocol() && parseDiscordantMin() && parseDiscordantMax() && parseArcThreshold()) {
            setAccepted(true);
            setCancelled(false);
            this.setVisible(false);
        }
    }//GEN-LAST:event_buttonOKActionPerformed

    private boolean parseDiscordantMin() {

        boolean result = false;
        String minStr = textDiscordantMin.getText();
        if (minStr == null || minStr.equals("")) {
            setDiscordantMin(Integer.MIN_VALUE);
            result = true;
        }
        else {
            try {
                setDiscordantMin(Integer.parseInt(minStr));
                result = true;
            } catch (NumberFormatException e) {
                Toolkit.getDefaultToolkit().beep();
                textDiscordantMin.setText("");
                textDiscordantMin.grabFocus();
            }
        }
        return result;
    }

    private boolean parseDiscordantMax() {

        boolean result = false;
        String maxStr = textDiscordantMax.getText();
        if (maxStr == null || maxStr.equals("")) {
            setDiscordantMax(Integer.MAX_VALUE);
            result = true;
        }
        else {
            try {
                setDiscordantMax(Integer.parseInt(maxStr));
                result = true;
            } catch (NumberFormatException e) {
                Toolkit.getDefaultToolkit().beep();
                textDiscordantMax.setText("");
                textDiscordantMax.grabFocus();
            }
        }
        return result;
    }

    private boolean parseProtocol() {
        if (pairedend_button.isSelected()) {
            prot = SAMReadUtils.PairedSequencingProtocol.PAIREDEND;
        } else if (matepair_button.isSelected()) {
            prot = SAMReadUtils.PairedSequencingProtocol.MATEPAIR;
        }
        return true;
    }

    private boolean parseArcThreshold() {

        boolean result = false;
        String threshStr = textArcThreshold.getText();
        if (threshStr == null || threshStr.equals("")) {
            setArcLengthThreshold(Integer.MIN_VALUE);
            result = true;
        }
        else {
            boolean percent = false;
            if (threshStr.endsWith("%")) {
                threshStr = threshStr.substring(0, threshStr.length()-1);
                percent = true;
            }
            try {
                double tempThresh = Double.parseDouble(threshStr);
                if (percent) {
                    setArcLengthThreshold(tempThresh/100.0d);
                }
                else {
                    setArcLengthThreshold(tempThresh);
                }
                result = true;
            } catch (NumberFormatException e) {
                Toolkit.getDefaultToolkit().beep();
                textArcThreshold.setText("");
                textArcThreshold.grabFocus();
            }
        }
        return result;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonCancel;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton buttonOK;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JRadioButton matepair_button;
    private javax.swing.JRadioButton pairedend_button;
    private javax.swing.JPanel reads_opposite_container;
    private javax.swing.JPanel reads_same_container;
    private javax.swing.JTextField textArcThreshold;
    private javax.swing.JTextField textDiscordantMax;
    private javax.swing.JTextField textDiscordantMin;
    // End of variables declaration//GEN-END:variables


    public int getDiscordantMin() {
        return discordantMin;
    }

    public void setDiscordantMin(int discordantMin) {
        this.discordantMin = discordantMin;
    }

    public int getDiscordantMax() {
        return discordantMax;
    }

    public void setDiscordantMax(int discordantMax) {
        this.discordantMax = discordantMax;
    }

    public SAMReadUtils.PairedSequencingProtocol getSequencingProtocol() {
        return prot;
    }

    public double getArcLengthThreshold() {
        return arcLengthThreshold;
    }

    public void setArcLengthThreshold(double arcLengthThreshold) {
        this.arcLengthThreshold = arcLengthThreshold;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }


}
