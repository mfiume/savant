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

import savant.view.tracks.Track;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;

import com.jidesoft.docking.DockableFrame;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import savant.api.util.DialogUtils;
import savant.controller.FrameController;
import savant.controller.GenomeController;
import savant.controller.LocationController;
import savant.controller.TrackController;
import savant.controller.event.GenomeChangedEvent;
import savant.data.event.DataRetrievalEvent;
import savant.data.event.TrackCreationEvent;
import savant.data.event.TrackCreationListener;
import savant.file.DataFormat;
import savant.plugin.SavantPanelPlugin;
import savant.util.DrawingMode;
import savant.util.Listener;
import savant.util.Range;
import savant.util.swing.ProgressPanel;
import savant.view.icon.SavantIconFactory;
import savant.view.tracks.SequenceTrack;


/**
 *
 * @author mfiume, AndrewBrook
 */
public class Frame extends DockableFrame implements Listener<DataRetrievalEvent>, TrackCreationListener {
    private static final Log LOG = LogFactory.getLog(Frame.class);

    /** If true, the frame's construction was halted by an error or by the user cancelling. */
    private boolean aborted;

    /** If true, the frame will be holding a sequence track, so it can be shorter than usual. */
    private boolean sequence;

    private GraphPane graphPane;
    private JLayeredPane frameLandscape;
    private Track[] tracks = new Track[0];

    private JLayeredPane jlp;

    private FrameCommandBar commandBar;
    private JComponent legend;
    private JPanel sidePanel;
    private JLabel yMaxPanel;
    private Map<SavantPanelPlugin, JPanel> pluginLayers = new HashMap<SavantPanelPlugin, JPanel>();

    public JScrollPane scrollPane;

    /**
     * Construct a new Frame for holding a track.
     *
     * @param seq if true, the Frame will be holding a sequence track
     */
    public Frame(boolean seq) {
        super(SavantIconFactory.getInstance().getIcon(SavantIconFactory.StandardIcon.TRACK));
        sequence = seq;

        // Component which displays the legend component.
        legend = new JComponent() {
            @Override
            public Dimension getPreferredSize() {
                for (Track t: tracks) {
                    Dimension d = t.getRenderer().getLegendSize(t.getDrawingMode());
                    if (d != null) {
                        return d;
                    }
                }
                return new Dimension(0, 0);
            }
            
            @Override
            public Dimension getMinimumSize() {
                return getPreferredSize();
            }

            @Override
            public void paintComponent(Graphics g) {
                for (Track t: tracks) {
                    Dimension d = t.getRenderer().getLegendSize(t.getDrawingMode());
                    if (d != null) {
                        Graphics2D g2 = (Graphics2D)g;
                        GradientPaint gp = new GradientPaint(0, 0, Color.WHITE, 0, 60, new Color(230,230,230));
                        g2.setPaint(gp);
                        g2.fillRect(0, 0, d.width, d.height);

                        g2.setColor(Color.BLACK);
                        g2.draw(new Rectangle2D.Double(0, 0, d.width - 1, d.height - 1));
                        t.getRenderer().drawLegend(g2, t.getDrawingMode());
                        return;
                    }
                }
            }
        };
        legend.setVisible(false);

        frameLandscape = new JLayeredPane();
        graphPane = new GraphPane(this);

        //scrollpane
        scrollPane = new JScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setWheelScrollingEnabled(false);
        scrollPane.setBorder(null);

        //add graphPane -> jlp -> scrollPane
        jlp = new JLayeredPane();
        jlp.setLayout(new GridBagLayout());
        GridBagConstraints gbc= new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        jlp.add(graphPane, gbc, 0);

        scrollPane.getViewport().add(jlp);

        //GRID FRAMEWORK AND COMPONENT ADDING...
        frameLandscape.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;

        //add sidepanel
        sidePanel = new JPanel() {
            @Override
            public Dimension getMinimumSize() {
                return new Dimension(0, 0);
            }
        };
        sidePanel.setLayout(new GridBagLayout());
        sidePanel.setOpaque(false);
        sidePanel.setVisible(false);
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        c.gridx = 1;
        c.gridy = 0;
        c.insets = new Insets(0, 0, 0, 16); // Leave 16 pixels so that we don't sit on top of the scroll-bar.
        frameLandscape.add(sidePanel, c);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension dim = getSize();
                if (dim != null) {
                    // TODO: The following shouldn't be necessary, but it seems to be.
                    int expectedWidth = frameLandscape.getWidth();
                    if (expectedWidth != graphPane.getWidth()) {
                        Dimension goodSize = new Dimension(expectedWidth, graphPane.getHeight());
                        graphPane.setPreferredSize(goodSize);
                        graphPane.setSize(goodSize);
                    }
                    
                    // This is also an opportunity to hide/show our legend depending on the available height.
                    setYMaxVisible(true);
                    legend.setVisible(legend.getPreferredSize().height > 0);
                    
                    // We have 24 pixels of slop to allow for margins and title-bars.
                    if (dim.height < sidePanel.getPreferredSize().height + 24) {
                        setYMaxVisible(false);
                        legend.setVisible(false);
                    }
                }
            }
        });

        //add graphPane to all cells
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.insets = new Insets(0, 0, 0, 0);
        frameLandscape.add(scrollPane, c);

        frameLandscape.setLayer(sidePanel, JLayeredPane.PALETTE_LAYER);
        frameLandscape.setLayer(scrollPane, JLayeredPane.DEFAULT_LAYER);

        // Add our progress-panel.  If setTracks is called promptly, it will be cleared
        // away before it ever has a chance to draw.
        getContentPane().add(new ProgressPanel(null));
    }

    public FrameCommandBar getCommandBar() {
        return commandBar;
    }

    public JLayeredPane getFrameLandscape() {
        return frameLandscape;
    }

    public GraphPane getGraphPane() {
        return graphPane;
    }

    public final Track[] getTracks() {
        return tracks;
    }

    /**
     * Set the tracks associated with this frame.  Normally, this should only be done
     * once, since the Frame also uses this opportunity to set up some GUI elements
     * which depend on the presence of loaded tracks.
     *
     * @param newTracks the tracks to be displayed in this frame
     */
    public void setTracks(Track[] newTracks) {
        if (!GenomeController.getInstance().isGenomeLoaded() && newTracks[0].getDataFormat() != DataFormat.SEQUENCE_FASTA) {
            trackCreationFailed(null);
            for (Track track : newTracks) {
                TrackController.getInstance().removeTrack(track);
            }
            DialogUtils.displayError("Sorry", "This does not appear to be a genome track. Please load a genome first.");
            return;
        }
        if (newTracks[0].getDataFormat() == DataFormat.SEQUENCE_FASTA) {
            GenomeController.getInstance().setSequence((SequenceTrack)newTracks[0]);
        }

        tracks = newTracks;
        graphPane.setTracks(tracks);

        commandBar = new FrameCommandBar(this);

        for (Track t: tracks) {
            t.setFrame(this);

            // Adds the track to the TrackController's internal list, and fires a TrackEvent.ADDED event to all listeners.
            TrackController.getInstance().addTrack(t);
        }

        // We get the name and other properties from the zero'th track.
        Track t0 = tracks[0];
        setName(t0.getName());
        setKey(t0.getName());

        DataFormat df = t0.getDataFormat();
        if (df != DataFormat.SEQUENCE_FASTA && df != DataFormat.INTERVAL_RICH) {
            yMaxPanel = new JLabel();
            yMaxPanel.setBorder(BorderFactory.createLineBorder(Color.darkGray));
            yMaxPanel.setBackground(new Color(240,240,240));
            yMaxPanel.setOpaque(true);
            yMaxPanel.setAlignmentX(0.5f);

            if (df == DataFormat.INTERVAL_BAM) {
                // We need to listen to genome changes so that we can redraw mismatches as appropriate.
                GenomeController.getInstance().addListener(new Listener<GenomeChangedEvent>() {
                    @Override
                    public void handleEvent(GenomeChangedEvent event) {
                        // In certain BAM modes, we care about whether the sequence has been set (or unset).
                        if (event.getNewGenome() == event.getOldGenome()) {
                            forceRedraw();
                        }
                    }
                });
            }
        }

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(4, 0, 0, 0);
        sidePanel.add(commandBar, gbc);
        sidePanel.add(legend, gbc);
        if (yMaxPanel != null) {
            sidePanel.add(yMaxPanel, gbc);
        }
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.VERTICAL;
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        sidePanel.add(filler, gbc);

        drawTracksInRange(LocationController.getInstance().getReferenceName(), LocationController.getInstance().getRange());

        JPanel contentPane = (JPanel)getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(frameLandscape);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(100, sequence ? 100 : 200);
    }

    /**
     * Convenience method.  More often than not, we just want the frame to host a single track.
     *
     * @param newTrack the track to be used for this frame.
     */
    public void setTrack(Track newTrack) {
        setTracks(new Track[] { newTrack });
    }


    public void setActiveFrame(boolean value) {
        sidePanel.setVisible(value);
        if (value) {
            // We may have to kick the legend to make it show up.
            legend.setVisible(legend.getPreferredSize().height > 0);
            legend.invalidate();
        }
    }

    public void resetLayers(){
        frameLandscape.moveToBack(graphPane);
    }

    public void redrawSidePanel(){
        sidePanel.repaint();
    }

    public void updateYMax(int value) {
        if (yMaxPanel != null) {
            yMaxPanel.setText(String.format(" ymax=%d ", value));
        }
    }

    /**
     * When the GraphPane's y-axis type has been updated, update the visibility of the yMax value.
     */
    public void setYMaxVisible(boolean flag) {
        if (yMaxPanel != null) {
            yMaxPanel.setVisible(flag);
        }
    }

    /**
     * Force the associated track to redraw.  Used when the colour scheme has been changed by the Preferences dialog.
     */
    public void forceRedraw() {
        graphPane.setRenderForced();
        drawTracksInRange(LocationController.getInstance().getReferenceName(), graphPane.getXRange());
    }

    /**
     * Prepare the data for the tracks in range and fire off a repaint.
     *
     * @param range
     */
    public void drawTracksInRange(String reference, Range range) {
        if (graphPane.isLocked()) { return; }

        graphPane.setXRange(range);

        for (Track t : tracks) {
            t.getRenderer().clearInstructions();
            t.prepareForRendering(reference, range);
        }
        resetLayers();
    }

    public void drawModeChanged(Track t) {
        DrawingMode mode = t.getDrawingMode();
        commandBar.drawModeChanged(mode);
        setYMaxVisible(true);
        legend.setVisible(legend.getPreferredSize().height > 0);
        legend.invalidate();
        validate();
        drawTracksInRange(LocationController.getInstance().getReferenceName(), LocationController.getInstance().getRange());
    }

    /**
     * Get a panel for a plugin to draw on.  If necessary, a new one will be created.
     */
    public JPanel getLayerCanvas(SavantPanelPlugin plugin, boolean mayCreate) {
        JPanel p = pluginLayers.get(plugin);
        if (p == null && mayCreate) {
            p = new JPanel();
            p.setOpaque(false);
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;
            c.weightx = 1.0;
            c.weighty = 1.0;
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 3;
            c.gridheight = 2;
            jlp.add(p,c,2);
            jlp.setLayer(p, 50);
            pluginLayers.put(plugin, p);
            if (plugin != null) {
                p.setVisible(plugin.isVisible());
            }
        }
        return p;
    }

    /**
     * Export this frame as an image.
     */
    public BufferedImage frameToImage(int baseSelected){
        BufferedImage bufferedImage = new BufferedImage(graphPane.getWidth(), graphPane.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bufferedImage.createGraphics();
        graphPane.setRenderForced();
        graphPane.forceFullHeight();
        graphPane.render(g2);
        graphPane.unforceFullHeight();
        g2.setColor(Color.black);
        if(baseSelected > 0){
            double h = (double)graphPane.getHeight();
            double spos = graphPane.transformXPos(baseSelected);
            g2.draw(new Line2D.Double(spos, 0.0, spos, h));
            double rpos = graphPane.transformXPos(baseSelected + 1);
            g2.draw(new Line2D.Double(rpos, 0.0, rpos, h));
        }
        g2.setFont(new Font(null, Font.BOLD, 13));
        g2.drawString(tracks[0].getName(), 2, 15);
        return bufferedImage;
    }

    @Override
    public void handleEvent(DataRetrievalEvent evt) {
        switch (evt.getType()) {
            case COMPLETED:
                LOG.trace("Frame received dataRetrievalCompleted.  Forcing full render.");
                setYMaxVisible(evt.getData() != null && evt.getData().size() > 0);
                graphPane.setRenderForced();
                graphPane.repaint();
                break;
            case FAILED:
                LOG.trace("Frame received dataRetrievalFailed.  Forcing full render.");
                setYMaxVisible(false);
                graphPane.setRenderForced();
                graphPane.repaint();
                break;
        }
    }

    @Override
    public void trackCreationStarted(TrackCreationEvent evt) {
    }

    @Override
    public void trackCreationCompleted(TrackCreationEvent evt) {
        if (!aborted) {
            setTracks(evt.getTracks());
        }
    }

    @Override
    public void trackCreationFailed(TrackCreationEvent evt) {
        aborted = true;
        FrameController.getInstance().closeFrame(this, false);
    }

    public int getIntervalHeight() {
        return commandBar.getIntervalHeight();
    }

    public void setHeightFromSlider() {
        int h = commandBar.getIntervalHeight();
        graphPane.setUnitHeight(h);
        graphPane.setScaledToFit(false);
    }
}
