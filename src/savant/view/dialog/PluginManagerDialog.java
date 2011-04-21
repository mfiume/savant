/*
 * PluginManagerDialog.java
 *
 * Created on Mar 9, 2010, 10:11:36 AM
 *
 *
 *    Copyright 2010 University of Toronto
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

import java.awt.BorderLayout;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.JDOMException;

import savant.api.util.DialogUtils;
import savant.settings.BrowserSettings;
import savant.settings.DirectorySettings;
import savant.util.DownloadFile;
import savant.view.dialog.tree.PluginRepositoryBrowser;
import savant.view.swing.Savant;

/**
 *
 * @author mfiume
 */
public class PluginManagerDialog extends JDialog {

    private static Log LOG = LogFactory.getLog(PluginManagerDialog.class);
    private PluginBrowser panel;

    public static PluginManagerDialog instance;

    public static PluginManagerDialog getInstance() {
        if (instance == null) {
            instance = new PluginManagerDialog(Savant.getInstance());
        }
        return instance;
    }

    /** Creates new form PluginManager */
    private PluginManagerDialog(java.awt.Frame parent) {
        super(parent,"Plugin Manager");
        initComponents();
        this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setResizable(false);
        panel = new PluginBrowser();
        refresh();
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
        button_add_from_file = new javax.swing.JButton();
        button_add_from_url = new javax.swing.JButton();
        panel_plugincanvas = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setIconImage(null);
        setIconImages(null);

        jLabel1.setText("Installed Plugins");

        button_add_from_file.setText("Install from File");
        button_add_from_file.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_add_from_fileActionPerformed(evt);
            }
        });

        button_add_from_url.setText("Install from Repository");
        button_add_from_url.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                button_add_from_urlActionPerformed(evt);
            }
        });

        panel_plugincanvas.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout panel_plugincanvasLayout = new javax.swing.GroupLayout(panel_plugincanvas);
        panel_plugincanvas.setLayout(panel_plugincanvasLayout);
        panel_plugincanvasLayout.setHorizontalGroup(
            panel_plugincanvasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 515, Short.MAX_VALUE)
        );
        panel_plugincanvasLayout.setVerticalGroup(
            panel_plugincanvasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 243, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panel_plugincanvas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 302, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(button_add_from_url)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                        .addComponent(button_add_from_file)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panel_plugincanvas, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(button_add_from_file)
                    .addComponent(button_add_from_url))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void button_add_from_fileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_add_from_fileActionPerformed
        addPlugin();
    }//GEN-LAST:event_button_add_from_fileActionPerformed

    PluginRepositoryBrowser browser = null;

    private void button_add_from_urlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_button_add_from_urlActionPerformed
        try {
            File file = DownloadFile.downloadFile(new URL(BrowserSettings.PLUGIN_URL), new File(System.getProperty("java.io.tmpdir")));
            if (file == null) {
                JOptionPane.showMessageDialog(this, "Problem downloading file: " + BrowserSettings.PLUGIN_URL);
                return;
            }
            if (browser == null) {
                browser = new PluginRepositoryBrowser(Savant.getInstance(), false, "Install Plugins", "Install", file, DirectorySettings.getPluginsDirectory());
            }
            this.setVisible(false);
            //browser.setAlwaysOnTop(true);
            browser.setVisible(true);

        } catch (JDOMException ex) {
            JOptionPane.showMessageDialog(this, "Problem downloading file: " + BrowserSettings.PLUGIN_URL);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Problem downloading file: " + BrowserSettings.PLUGIN_URL);
        }
    }//GEN-LAST:event_button_add_from_urlActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton button_add_from_file;
    private javax.swing.JButton button_add_from_url;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel panel_plugincanvas;
    // End of variables declaration//GEN-END:variables

    private void addPlugin() {

        File selectedFile = DialogUtils.chooseFileForOpen("Select Plugin JAR", null, null);

        // copy the plugin
        if (selectedFile != null) {
            try {
                copyFile(selectedFile, new File(DirectorySettings.getPluginsDirectory(), selectedFile.getName()));

                refresh();
                JOptionPane.showMessageDialog(this, "Plugin successfully installed. Restart Savant \n" +
                    "for changes to take effect.");

            // error copying file
            } catch (Exception ex) {
                LOG.error("Error installing plugin.", ex);
                JOptionPane.showMessageDialog(this, "Error installing plugin." +
                        "\nYou can manually install it by adding the appropriate \n" +
                        ".jar file to the plugins directory.");
            }

        }
    }

    public static void copyFile(File in, File out) throws Exception {

        if (in.getAbsolutePath().equals(out.getAbsolutePath())) { return; }

        FileInputStream fis  = new FileInputStream(in);
        FileOutputStream fos = new FileOutputStream(out);
        try {
            byte[] buf = new byte[1024];
            int i = 0;
            while ((i = fis.read(buf)) != -1) {
                fos.write(buf, 0, i);
            }
        }
        catch (Exception e) {
            throw e;
        }
        finally {
            if (fis != null) fis.close();
            if (fos != null) fos.close();
        }
    }

    private void refresh() {
        this.panel_plugincanvas.setLayout(new BorderLayout());
        this.panel_plugincanvas.removeAll();
        this.panel_plugincanvas.add(panel.getPluginListPanel(), BorderLayout.CENTER);
    }
}
