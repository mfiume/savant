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

import java.awt.Window;
import java.io.File;
import java.net.URI;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;

import savant.api.util.DialogUtils;
import savant.file.FileType;
import savant.format.DataFormatter;
import savant.format.SavantFileFormatterUtils;


/**
 * @author mfiume, tarkvara
 */
public final class DataFormatForm extends JDialog {

    /**
     * Construct new data format form.
     *
     * @param parent typically the Savant main window
     */
    public DataFormatForm(Window parent, URI input) {
        super(parent, ModalityType.APPLICATION_MODAL);
        initComponents();

        formatList.setListData(new FormatDef[] {
                                    new FormatDef("BED", false, false, FileType.INTERVAL_BED , "BED format is an alternative to GFF format for describing co-ordinates of localized features on genomes."),
                                    new FormatDef("GFF", true, false, FileType.INTERVAL_GFF , "GFF (General Feature Format) is a format for locating & describing genes and other localized features associated with DNA, RNA and Protein sequences."),
                                    new FormatDef("GTF", true, false, FileType.INTERVAL_GTF , "GTF (Gene Transfer Format) is a refinement to GFF which stores gene and transcript IDs in a standard way."),
                                    new FormatDef("VCF", false, false, FileType.INTERVAL_VCF , "VCF (Variant Call Format) is a format for storing gene sequence variants."),
                                    new FormatDef("WIG/BedGraph", true, false, FileType.CONTINUOUS_WIG , "WIG format allows display of continuous-valued data in track format. This display type is useful for GC percent, probability scores, and transcriptome data."),
                                    new FormatDef("BAM Coverage", true, false, FileType.INTERVAL_BAM , "SAM format (binary, for BAM) is a generic format for storing large nucleotide sequence alignments. This option generates a coverage graph which will represent the BAM data at low resolutions."),
                });

        if (input != null) {
            inputField.setText(input.getPath());
            if (!guessFileType()) {
                setOutputPath();
            }
        }

        // This feature is temporarily disabled.
        tempOutputCheck.setVisible(false);
        setLocationRelativeTo(parent);

        validateReadyToFormat();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        inputField = new javax.swing.JTextField();
        javax.swing.JScrollPane jScrollPane2 = new javax.swing.JScrollPane();
        descriptionArea = new javax.swing.JTextArea();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        javax.swing.JScrollPane jScrollPane1 = new javax.swing.JScrollPane();
        formatList = new javax.swing.JList();
        inputButton = new javax.swing.JButton();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        javax.swing.JLabel jLabel3 = new javax.swing.JLabel();
        outputButton = new javax.swing.JButton();
        formatButton = new javax.swing.JButton();
        javax.swing.JLabel jLabel4 = new javax.swing.JLabel();
        outputField = new javax.swing.JTextField();
        tempOutputCheck = new javax.swing.JCheckBox();
        zeroBasedCheck = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Format");

        inputField.setEditable(false);
        inputField.setDisabledTextColor(new java.awt.Color(255, 255, 255));

        descriptionArea.setColumns(20);
        descriptionArea.setEditable(false);
        descriptionArea.setFont(new java.awt.Font("Verdana", 0, 10));
        descriptionArea.setLineWrap(true);
        descriptionArea.setRows(3);
        descriptionArea.setWrapStyleWord(true);
        jScrollPane2.setViewportView(descriptionArea);

        jLabel2.setText("Format");

        formatList.setModel(new DefaultListModel());
        formatList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        formatList.setVisibleRowCount(6);
        formatList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                formatListValueChanged(evt);
            }
        });
        jScrollPane1.setViewportView(formatList);

        inputButton.setText("...");
        inputButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inputButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("Input File");

        jLabel3.setText("Output File");

        outputButton.setText("...");
        outputButton.setEnabled(false);
        outputButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputButtonActionPerformed(evt);
            }
        });

        formatButton.setText("Format");
        formatButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                formatButtonActionPerformed(evt);
            }
        });

        jLabel4.setText("Format Description");

        outputField.setEditable(false);
        outputField.setDisabledTextColor(new java.awt.Color(255, 255, 255));
        outputField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                outputFieldActionPerformed(evt);
            }
        });

        tempOutputCheck.setText("Store output temporarily");
        tempOutputCheck.setToolTipText("Store the formatted file just for this session");
        tempOutputCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tempOutputCheckActionPerformed(evt);
            }
        });

        zeroBasedCheck.setText("Input file is 0-based");
        zeroBasedCheck.setToolTipText("Checked for 1-based; unchecked for 0-based.");
        zeroBasedCheck.setEnabled(false);
        zeroBasedCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zeroBasedCheckActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 483, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(inputField, javax.swing.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(inputButton))
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel4)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 483, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(zeroBasedCheck)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tempOutputCheck))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(outputField, javax.swing.GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(outputButton))
                    .addComponent(jLabel3)
                    .addComponent(formatButton, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(2, 2, 2)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(inputField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(inputButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(zeroBasedCheck)
                    .addComponent(tempOutputCheck))
                .addGap(16, 16, 16)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(outputButton)
                    .addComponent(outputField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(formatButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void inputButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inputButtonActionPerformed
        File selectedFile = DialogUtils.chooseFileForOpen("Input File", null, null);
        if (selectedFile != null) {
            inputField.setText(selectedFile.getPath());
            if (guessFileType()) {
                // If we guessed the file type, setOutputPath and validateReadyToFormat will have been called.
                return;
            }
            setOutputPath();
        }
        validateReadyToFormat();
    }//GEN-LAST:event_inputButtonActionPerformed

    private void tempOutputCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tempOutputCheckActionPerformed
        
        setOutputPath();
        
    }//GEN-LAST:event_tempOutputCheckActionPerformed


    private void formatButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_formatButtonActionPerformed

        File infile = new File(inputField.getText());
        File outfile = new File(outputField.getText());
        FileType ft = ((FormatDef)formatList.getSelectedValue()).type;
        boolean isInputOneBased = !zeroBasedCheck.isSelected();
        
        final DataFormatter df = new DataFormatter(infile, outfile, ft, isInputOneBased);

        final FormatProgressDialog fpd = new FormatProgressDialog(this, df);
        fpd.setLocationRelativeTo(this);
        new Thread("Formatter") {
            @Override
            public void run() {
                try {
                    df.format(fpd);
                    fpd.notifyOfTermination(true, null);
                } catch (Throwable ex) {
                    fpd.notifyOfTermination(false, ex);
                }
            }
        }.start();
        fpd.setVisible(true);
        dispose();
    }//GEN-LAST:event_formatButtonActionPerformed

    private void zeroBasedCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zeroBasedCheckActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_zeroBasedCheckActionPerformed

    private void outputFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_outputFieldActionPerformed

    private void formatListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_formatListValueChanged
        if (!evt.getValueIsAdjusting()) {
            if (!formatList.isSelectionEmpty()) {
                FormatDef picked = (FormatDef)formatList.getSelectedValue();
                descriptionArea.setText(picked.description);
                zeroBasedCheck.setSelected(picked.defaultIsZeroBased);
                zeroBasedCheck.setEnabled(picked.canChooseBase);
            }
            setOutputPath();
            validateReadyToFormat();
        }
    }//GEN-LAST:event_formatListValueChanged

    private void outputButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_outputButtonActionPerformed
        File outputFile = new File(outputField.getText());
        outputFile = DialogUtils.chooseFileForSave("Output File", outputFile.getName(), null, outputFile.getParentFile());
        if (outputFile != null) {
            outputField.setText(outputFile.getPath());
        }
        validateReadyToFormat();
}//GEN-LAST:event_outputButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea descriptionArea;
    private javax.swing.JButton formatButton;
    private javax.swing.JList formatList;
    private javax.swing.JButton inputButton;
    private javax.swing.JTextField inputField;
    private javax.swing.JButton outputButton;
    private javax.swing.JTextField outputField;
    private javax.swing.JCheckBox tempOutputCheck;
    private javax.swing.JCheckBox zeroBasedCheck;
    // End of variables declaration//GEN-END:variables

    private void setOutputPath() {
        FormatDef format = (FormatDef)formatList.getSelectedValue();
        if (format != null) {
            FileType ft = format.type;
            String inputPath = inputField.getText();
            boolean bam = (ft == null && inputPath.endsWith(".bam")) || ft == FileType.INTERVAL_BAM;

            outputButton.setEnabled(!bam);
            outputField.setEnabled(!bam);
            if (bam) {
                outputField.setText("Directory of BAM file");
            } else {
                if (inputPath.equals("")) {
                    outputField.setText("No input file selected");
                } else {
                    String outputPath = inputPath;
                    if (outputPath.equals(""))
                    if (tempOutputCheck.isSelected()) {
                        outputPath += ".tmp";
                    }
                    switch (ft) {
                        case CONTINUOUS_WIG:
                            outputPath += ".tdf";
                            break;
                        case INTERVAL_BED:
                        case INTERVAL_BED1:
                        case INTERVAL_GENERIC:
                        case INTERVAL_GFF:
                        case INTERVAL_GTF:
                        case INTERVAL_PSL:
                        case INTERVAL_VCF:
                        case INTERVAL_KNOWNGENE:
                        case INTERVAL_REFGENE:
                            outputPath += ".gz";
                            break;
                        default:
                            outputPath += ".savant";
                            break;
                    }
                    outputField.setText(outputPath);
                }
            }
            validateReadyToFormat();
        }
    }

    private void validateReadyToFormat() {
        if (inputField.getText().equals("") || outputField.getText().equals("") || formatList.isSelectionEmpty()) {
            formatButton.setEnabled(false);
        } else {
            formatButton.setEnabled(true);
            getRootPane().setDefaultButton(formatButton);
        }
    }

    /**
     * Given the file name, try to guess which Savant file-type is appropriate.
     *
     * @return true if we found a match
     */
    private boolean guessFileType() {
        FileType guess = SavantFileFormatterUtils.guessFileTypeFromPath(inputField.getText());
        if (guess != null) {
            int numItems = formatList.getModel().getSize();
            for (int i = 0; i < numItems; i++) {
                if (((FormatDef)formatList.getModel().getElementAt(i)).type == guess) {
                    if (i != formatList.getSelectedIndex()) {
                        formatList.setSelectedIndex(i);
                        return true;
                    }
                    break;
                }
            }
        }
        return false;
    }

    /**
     * Class which is used to populate the format list.
     */
    static class FormatDef {
        final String name;
        final boolean defaultIsZeroBased;
        final boolean canChooseBase;
        final FileType type;
        final String description;

        FormatDef(String name, boolean defaultIsZeroBased, boolean canChooseBase, FileType type, String description) {
            this.name = name;
            this.defaultIsZeroBased = defaultIsZeroBased;
            this.canChooseBase = canChooseBase;
            this.type = type;
            this.description = description;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
