/*
 *    Copyright 2010-2012 University of Toronto
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

package savant.view.dialog;

import java.io.File;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import savant.api.util.DialogUtils;
import savant.api.util.Listener;
import savant.controller.FrameController;
import savant.controller.GenomeController;
import savant.format.BAMToCoverage;
import savant.format.FormatEvent;
import savant.format.SavantFileFormatter;
import savant.view.icon.SavantIconFactory;


/**
 * Form which displays progress during the formatting process.
 *
 * @author mfiume
 */
public class FormatProgressDialog extends JDialog implements Listener<FormatEvent> {

    final SavantFileFormatter formatter;

    /** Creates new form FormatFrame */
    public FormatProgressDialog(DataFormatForm parent, SavantFileFormatter sff) {
        super(parent, ModalityType.APPLICATION_MODAL);
        initComponents();
        formatter = sff;
        setIconImage(SavantIconFactory.getInstance().getIcon(SavantIconFactory.StandardIcon.LOGO).getImage());

        srcLabel.setText(shorten(formatter.getInputFile().getPath()));
        destLabel.setText(shorten(formatter.getOutputFile().getPath()));
    }

    @Override
    public void setVisible(boolean flag) {
        if (flag) {
            formatter.addListener(this);
            formatter.run();
        }
        super.setVisible(flag);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.JLabel destCaption = new javax.swing.JLabel();
        srcLabel = new javax.swing.JLabel();
        javax.swing.JLabel srcCaption = new javax.swing.JLabel();
        statusLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        javax.swing.JButton cancelButton = new javax.swing.JButton();
        destLabel = new javax.swing.JLabel();
        javax.swing.JLabel statusCaption = new javax.swing.JLabel();
        javax.swing.JSeparator jSeparator1 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Formatting ...");
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        setResizable(false);

        destCaption.setText("Destination: ");

        srcLabel.setText("filename1");

        srcCaption.setText("Formatting: ");

        statusLabel.setText("status ...");

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        destLabel.setText("filename2");

        statusCaption.setText("Status: ");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(destCaption)
                            .addComponent(srcCaption))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(srcLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
                            .addComponent(destLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(statusCaption)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 644, Short.MAX_VALUE))
                    .addComponent(cancelButton, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(progressBar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 699, Short.MAX_VALUE)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 699, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(srcCaption)
                    .addComponent(srcLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(destCaption)
                    .addComponent(destLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 13, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusCaption)
                    .addComponent(statusLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        if (DialogUtils.askYesNo("Confirm Cancel", "Are you sure you want to cancel?") == DialogUtils.YES) {
            formatter.cancel();
            dispose();
        }
}//GEN-LAST:event_cancelButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel destLabel;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel srcLabel;
    private javax.swing.JLabel statusLabel;
    // End of variables declaration//GEN-END:variables

    @Override
    public void handleEvent(FormatEvent event) {
        switch (event.getType()) {
            case PROGRESS:
                progressBar.setValue((int)(event.getProgress() * 100.0));
                if (event.getSubTask() != null) {
                    statusLabel.setText(event.getSubTask());
                }
                break;
            case COMPLETED:
                setVisible(false);
                if (GenomeController.getInstance().isGenomeLoaded()) {
                    if (formatter instanceof BAMToCoverage) {
                        // For coverage tracks, we report success, but don't offer to open the track.
                        DialogUtils.displayMessage("Format Successful", String.format("<HTML>Format successful.<BR>Coverage will be available the next time you open <i>%s</i>.</HTML>", formatter.getInputFile().getName()));
                    } else {
                        if (((DataFormatForm)getParent()).loadingTrack || DialogUtils.askYesNo("Format Successful", "Format successful. Open track now?") == DialogUtils.YES) {
                            try {
                                FrameController.getInstance().addTrackFromPath(formatter.getOutputFile().getAbsolutePath(), null, null);
                            } catch (Exception ex) {
                            }
                        }
                    }
                } else {
                    DialogUtils.displayMessage("Format Successful", "<HTML>Format successful. <BR>A genome must be loaded before you can open this track.</HTML>");
                }
                break;
            case FAILED:
                setVisible(false);
                ((DataFormatForm)getParent()).handleFormattingError(event.getError());
                break;
        }
    }

    private String shorten(String path) {
        int maxLen = 80;
        if (path.length() > maxLen) {
            String file = new File(path).getName();
            do {
                String parent = new File(path).getParent();
                if (parent == null) {
                    break;
                }
                path = parent;
            } while (path.length() > 0 && path.length() + 2 + file.length() > maxLen);
            return path + "…" + File.separator + file;
        } else {
            return path;
        }
    }
}
