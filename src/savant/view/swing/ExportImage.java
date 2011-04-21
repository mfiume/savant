/*
 *    Copyright 2010-2011 University of Toronto
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
package savant.view.swing;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;

import savant.controller.FrameController;
import savant.controller.RangeController;
import savant.util.MiscUtils;

/**
 *
 * @author AndrewBrook
 */
public class ExportImage {

    public ExportImage(){

        String[] trackNames = Savant.getInstance().getSelectedTracks(true, "Select Tracks to Export");
        if(trackNames == null) return;

        List<Frame> frames = FrameController.getInstance().getFrames();

        List<BufferedImage> images = new ArrayList<BufferedImage>();
        int totalWidth = 0;
        int totalHeight = 45;
        for(int j = 0; j <trackNames.length; j++){
            for(int i = 0; i <frames.size(); i++){
                if(frames.get(i).getName().equals(trackNames[j])){
                    BufferedImage im = frames.get(i).frameToImage();
                    images.add(im);
                    totalWidth = Math.max(totalWidth, im.getWidth());
                    totalHeight += im.getHeight();
                    trackNames[j] = null;
                    break;
                }
            }
        }
        //no frames selected
        if (images.isEmpty()) {
            return;
        }

        BufferedImage out = new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_RGB);


        //write range at top
        RangeController range = RangeController.getInstance();
        int start = range.getRangeStart();
        int end = range.getRangeEnd();
        String toWrite = "Range:  " + start + " - " + end;
        Graphics2D g = out.createGraphics();
        g.setColor(Color.white);
        g.setFont(new Font(null, Font.BOLD, 13));
        g.drawString(toWrite, 2, 17);



        int outX = 0;
        int outY = 25;
        for(int i = 0; i < images.size(); i++){
            BufferedImage current = images.get(i);
            for(int y = 0; y < current.getHeight(); y++){
                for(int x = 0; x < current.getWidth(); x++){
                    int color = current.getRGB(x, y);
                    out.setRGB(outX, outY, color);
                    outX++;
                }
                outX = 0;
                outY++;
            }
        }

        //write message at bottom
        toWrite = "Generated using the Savant Genome Browser - http://savantbrowser.com";
        g.setColor(Color.white);
        g.setFont(new Font(null, Font.BOLD, 10));
        g.drawString(toWrite, 2, outY+14);

        save(out);

    }

    private String save(BufferedImage screen) {

        JFrame jf = new JFrame();
        String selectedFileName;
        // TODO: Switch this to use DialogUtils.chooseFileForSave.
        if (MiscUtils.MAC) {
            FileDialog fd = new FileDialog(jf, "Output File", FileDialog.SAVE);
            fd.setVisible(true);
            jf.setAlwaysOnTop(true);
            // get the path (null if none selected)
            selectedFileName = fd.getFile();
            if (selectedFileName != null) {
                selectedFileName = fd.getDirectory() + selectedFileName;
            }
        } else {
            JFileChooser fd = new JFileChooser();
            fd.setDialogTitle("Output File");
            fd.setDialogType(JFileChooser.SAVE_DIALOG);
            fd.setFileFilter(new FileFilter() {

                @Override
                public boolean accept(File f) {
                        if (f.isDirectory()) {
                            return true;
                        }



                        String path = f.getAbsolutePath();
                        String extension = "";
                        int indexOfDot = path.lastIndexOf(".");
                        if (indexOfDot == -1 || indexOfDot == path.length() - 1) {
                            extension = "";
                        } else {
                            extension = path.substring(indexOfDot + 1);
                        }

                        //String extension = DataFormatUtils.getExtension(f.getAbsolutePath());
                        if (extension != null) {
                            if (extension.equals("png")) {
                                return true;
                            } else {
                                return false;
                            }
                        }

                        return false;
                }

                @Override
                public String getDescription() {
                    return "Image files (*.png)";
                }

            });
            int result = fd.showSaveDialog(jf);
            if (result == JFileChooser.CANCEL_OPTION || result == JFileChooser.ERROR_OPTION ) return null;
            selectedFileName = fd.getSelectedFile().getPath();
        }

        // set the genome
        if (selectedFileName != null) {
            try {
                ImageIO.write(screen, "PNG", new File(selectedFileName));
            } catch (IOException ex) {
                String message = "Screenshot unsuccessful";
                String title = "Uh oh...";
                // display the JOptionPane showConfirmDialog
                JOptionPane.showConfirmDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
            }
        }

        return null;
    }

}
