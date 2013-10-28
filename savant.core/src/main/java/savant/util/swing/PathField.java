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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package savant.util.swing;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import savant.view.icon.SavantIconFactory;
import savant.util.swing.DocumentViewer;

/**
 *
 * @author mfiume
 */
public class PathField extends JPanel {

    JTextField f;
    JButton b;
    JFileChooser fc;

     public PathField(final int JFileChooserDialogType) {
         this(JFileChooserDialogType,false,false);
     }

    public PathField(final int JFileChooserDialogType, boolean showFileButton, boolean directoriesOnly) {
        f = new JTextField();
        b = new JButton("...");
        fc = new JFileChooser();
        f.setMaximumSize(new Dimension(9999,22));
        if (JFileChooserDialogType == JFileChooser.SAVE_DIALOG) {
            fc.setDialogTitle("Save File");
            f.setToolTipText("Path to output file");
            b.setToolTipText("Set output file");
        } else {
            fc.setDialogTitle("Open File");
            f.setToolTipText("Path to input file");
            b.setToolTipText("Choose input file");
        }
        if(directoriesOnly){
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.add(f);
        this.add(b);

        b.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                fc.setDialogType(JFileChooserDialogType);
                int result = fc.showDialog(null, null);
                if (result == JFileChooser.CANCEL_OPTION || result == JFileChooser.ERROR_OPTION) {
                    return;
                }
                setPath(fc.getSelectedFile().getAbsolutePath());
            }
        });

        if (showFileButton) {
            JButton showButt = new JButton();
            showButt.setToolTipText("Show file");
            showButt.setIcon(SavantIconFactory.getInstance().getIcon(SavantIconFactory.StandardIcon.VIEW));
            showButt.addActionListener(new ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    DocumentViewer v = DocumentViewer.getInstance();
                    v.addDocument(getPath());
                }
            });
            this.add(showButt);
        }
    }

    public String getPath() {
        return this.f.getText();
    }

    public void setPath(String s) {
        this.f.setText(s);
    }

    public JFileChooser getFileChooser() {
        return this.fc;
    }
}
