/**
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package savant.view.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import savant.settings.BrowserSettings;

/**
 *
 * @author mfiume
 */
public class Splash extends javax.swing.JDialog {

    JLabel status;
    String pad = "  ";
    int currentprogress;
    int totalprogress = 5;
    JProgressBar progress;

    /**
     * Creates new form Splash
     *
     * @param parent the parent frame, typically the Savant main form
     * @param modal true if this is being used as a modal dialog (i.e. as an
     * about-box)
     */
    public Splash(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        Image i = new ImageIcon(getClass().getResource("/savant/images/splash2.png")).getImage();
        this.setPreferredSize(new Dimension(i.getWidth(null), i.getHeight(null)));
        this.setMaximumSize(new Dimension(i.getWidth(null), i.getHeight(null)));
        this.setMinimumSize(new Dimension(i.getWidth(null), i.getHeight(null)));
        ImagePanel ip = new ImagePanel(i);
        this.splashPanel.add(ip, BorderLayout.CENTER);

        ip.setLayout(new BorderLayout());

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel website = new JLabel(pad + "Website: " + BrowserSettings.URL);
        formatLabel(website, false);
        textPanel.add(website);

        JLabel authors = new JLabel(pad + "Authors: Computational Biology Lab @ University of Toronto");
        formatLabel(authors, false);
        textPanel.add(authors);

        textPanel.add(getPadding(2));

        JLabel version = new JLabel(pad + "Version: " + BrowserSettings.VERSION + " " + BrowserSettings.BUILD);
        formatLabel(version, false);
        textPanel.add(version);

        textPanel.add(getPadding(2));

        status = new JLabel(modal ? "" : pad + "Initializing ...");
        formatLabel(status, true);
        textPanel.add(status);

        //textPanel.add(getHorizontalPad());

        textPanel.add(getPadding(8));

        /*
         progress = new JProgressBar();
         Dimension d = new Dimension(300,23);
         progress.setMinimumSize(d);
         progress.setMaximumSize(d);
         textPanel.add(progress);
         *
         */

        //textPanel.add(getPadding(3));

        ip.add(textPanel, BorderLayout.SOUTH);

        this.setLocationRelativeTo(null);

    }

    public final JPanel getPadding(int ps) {
        JPanel p = new JPanel();
        Dimension d = new Dimension(ps, ps);
        p.setMaximumSize(d);
        p.setMinimumSize(d);
        p.setPreferredSize(d);
        p.setOpaque(false);
        return p;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        splashPanel = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        setUndecorated(true);

        splashPanel.setBackground(new java.awt.Color(204, 204, 255));
        splashPanel.setPreferredSize(new java.awt.Dimension(450, 150));

        javax.swing.GroupLayout splashPanelLayout = new javax.swing.GroupLayout(splashPanel);
        splashPanel.setLayout(splashPanelLayout);
        splashPanelLayout.setHorizontalGroup(
            splashPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 238, Short.MAX_VALUE)
        );
        splashPanelLayout.setVerticalGroup(
            splashPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 144, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splashPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splashPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                Splash dialog = new Splash(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel splashPanel;
    // End of variables declaration//GEN-END:variables

    void setStatus(String msg) {
        this.status.setText(pad + msg);
    }

    private void formatLabel(JLabel label, boolean isBold) {
        if (isBold) {
            label.setFont(new Font("Arial", Font.BOLD, 11));
        } else {
            label.setFont(new Font("Arial", Font.PLAIN, 11));
        }
    }
}