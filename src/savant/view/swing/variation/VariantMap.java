/*
 *    Copyright 2012 University of Toronto
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
package savant.view.swing.variation;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import savant.api.adapter.PopupHostingAdapter;
import savant.api.data.Record;
import savant.api.data.VariantRecord;
import savant.api.data.VariantType;
import savant.api.event.PopupEvent;
import savant.api.util.Listener;
import savant.data.types.ParticipantRecord;
import savant.selection.PopupPanel;
import savant.settings.BrowserSettings;
import savant.settings.ColourSettings;
import savant.util.ColourAccumulator;
import savant.util.ColourKey;
import savant.util.ColourScheme;
import savant.util.Hoverer;
import savant.util.MiscUtils;
import savant.view.tracks.Track;
import savant.view.tracks.VariantTrackRenderer;


/**
 * Map-like overview of all variant tracks.
 *
 * @author tarkvara
 */
public class VariantMap extends JPanel implements PopupHostingAdapter {
    private static final Log LOG = LogFactory.getLog(VariantMap.class);

    /** Height in pixels of gap between blocks (when we have enough space to draw gap sizes). */
    private static final double GAP_HEIGHT = 9.0;

    private VariationSheet owner;
    
    private double unitHeight;
    private double unitWidth;

    VariantMap(VariationSheet p) {
        this.owner = p;
        setFont(BrowserSettings.getTrackFont());

        MouseAdapter listener = new Hoverer() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                ParticipantRecord partRec = pointToParticipantRecord(hoverPos);
                if (partRec != null) {
                    PopupPanel.hidePopup();
                    VariantType[] partVars = partRec.getVariants();
                    
                    // Only display a popup if this participant actually has variation here.
                    if (partVars[0] != VariantType.NONE || (partVars.length > 1 && partVars[1] != VariantType.NONE)) {
                        Point globalPt = SwingUtilities.convertPoint(VariantMap.this, hoverPos, null);
                        PopupPanel.showPopup(VariantMap.this, globalPt, owner.tracks.get(0), partRec);
                    }
                }
                hoverPos = null;
            }
            
            @Override
            public void mouseMoved(MouseEvent evt) {
                owner.updateStatusBar(pointToVariantRecord(evt.getPoint().y));
                Point oldHover = hoverPos;
                super.mouseMoved(evt);
                if (oldHover != null && !isHoverable(oldHover)) {
                    PopupPanel.hidePopup();
                }
            }

            @Override
            public void mouseClicked(MouseEvent evt) {
                owner.navigateToRecord(pointToVariantRecord(evt.getY()));
            }
        };
        addMouseListener(listener);
        addMouseMotionListener(listener);
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Paint a gradient from top to bottom
        int h = getHeight();
        int w = getWidth();
        GradientPaint gp0 = new GradientPaint(0, 0, ColourSettings.getColor(ColourKey.GRAPH_PANE_BACKGROUND_TOP), 0, h, ColourSettings.getColor(ColourKey.GRAPH_PANE_BACKGROUND_BOTTOM));
        g2.setPaint(gp0);
        g2.fillRect(0, 0, w, h);


        List<VariantRecord> data = owner.getData();
        if (data != null && !data.isEmpty()) {
            int participantCount = owner.getParticipantCount();
            unitHeight = (double)h / data.size();
            unitWidth = (double)w / participantCount;

            boolean gappable = unitHeight > GAP_HEIGHT * 2.0;

            ColourScheme cs = new ColourScheme(ColourKey.A, ColourKey.C, ColourKey.G, ColourKey.T, ColourKey.INSERTED_BASE, ColourKey.DELETED_BASE);
            ColourAccumulator accumulator = new ColourAccumulator(cs);


            double y = 0.0;
            double topGap = 0.0;
            for (int i = 0; i < data.size(); i++) {
                VariantRecord varRec = data.get(i);

                double bottomGap = 0.0;
                if (gappable && i + 1 < data.size()) {
                    VariantRecord nextRec = data.get(i + 1);
                    if (nextRec.getPosition() - varRec.getPosition() > 1) {
                        bottomGap = GAP_HEIGHT * 0.5;
                    }
                }

                double x = 0.0;
                for (int j = 0; j < participantCount; j++) {
                    VariantTrackRenderer.accumulateZygoteShapes(varRec.getVariantsForParticipant(j), accumulator, new Rectangle2D.Double(x, y + topGap, unitWidth, unitHeight - topGap - bottomGap));
                    x += unitWidth;
                }
                topGap = bottomGap;
                y += unitHeight;
            }
            accumulator.fill(g2);

            if (gappable) {
                drawGapSizes(g2);
            } else {
                // Not enough room to draw gaps, so just label the axes.
                labelAxis(g2);
            }
        }
    }

    /**
     * Given a point on this panel, figure out which record it corresponds to.
     * @param pt the point we're interested in
     * @return 
     */
    private VariantRecord pointToVariantRecord(int y) {
        int logicalY = (int)(y / unitHeight);
        List<VariantRecord> data = owner.getData();
        if (data != null && logicalY >= 0 && logicalY < data.size()) {
            return data.get(logicalY);
        }
        return null;
    }

    /**
     * Given a point on this panel, figure out which record it corresponds to.
     * @param pt the point we're interested in
     * @return 
     */
    private ParticipantRecord pointToParticipantRecord(Point pt) {
        VariantRecord varRec = pointToVariantRecord(pt.y);
        if (varRec != null) {
            int logicalX = (int)(pt.x / unitWidth);
            return new ParticipantRecord(varRec, logicalX, owner.participantNames.get(logicalX));
        }
        return null;
    }

    /**
     * At low resolutions we draw the axis lines to indicate the non-linearity.
     */
    private void labelAxis(Graphics2D g2) {
        List<VariantRecord> data = owner.getData();

        if (data.size() > 0) {
            int[] ticks = MiscUtils.getTickPositions(owner.getVisibleRange());

            Color gridColor = ColourSettings.getColor(ColourKey.AXIS_GRID);

            // Smallish font for tick labels.
            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 9));
            g2.setColor(gridColor);
            FontMetrics fm = g2.getFontMetrics();

            // We don't want the axes stomping on our labels, so make sure the clip excludes them.
            Area clipArea = new Area(new Rectangle(0, 0, getWidth(), getHeight()));
            double[] ys = new double[ticks.length];

            int index = 0;
            float labelX = 0.0F;    // Labels may get stacked up.
            for (int i = 0; i < ticks.length && index < data.size(); i++) {
                int t = ticks[i];
                while (index < data.size() && t > data.get(index).getPosition()) {
                    index++;
                    labelX = 0.0F;
                }
                double y = index * unitHeight;
                ys[i] = y;

                String s = Integer.toString(t);
                Rectangle2D labelRect = fm.getStringBounds(s, g2);
                double baseline = y + fm.getAscent() - fm.getHeight() * 0.5;
                g2.drawString(s, labelX + 4.0F, (float)baseline);
                clipArea.subtract(new Area(new Rectangle2D.Double(labelX + 3.0, baseline - labelRect.getHeight() - 1.0, labelRect.getWidth() + 2.0, labelRect.getHeight() + 2.0)));
                labelX += labelRect.getWidth() + 2.0F;
            }
            g2.setClip(clipArea);
            for (int i = 0; i < ticks.length; i++) {
                double y = ys[i];
                g2.draw(new Line2D.Double(0.0, y, getWidth(), y));
            }
           g2.setClip(null);
        }
    }
    
    /**
     * If we have enough room, we draw the gap sizes between the variants.
     * @param g2 
     */
    private void drawGapSizes(Graphics2D g2) {
        List<VariantRecord> data = owner.getData();
        if (data.size() > 1) {
            Color gridColor = ColourSettings.getColor(ColourKey.AXIS_GRID);

            // Tiny font for gap labels.
            g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 8));
            FontMetrics fm = g2.getFontMetrics();
            g2.setColor(gridColor);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double y = unitHeight;
            double w = getWidth();
            for (int i = 1; i < data.size(); i++) {
                int gapSize = data.get(i).getPosition() - data.get(i - 1).getPosition() - 1;
                if (gapSize > 0) {
                    String s = gapSize > 1 ? String.format("%d bases", gapSize) : "1 base";

                    Rectangle2D labelRect = fm.getStringBounds(s, g2);
                    double baseline = y + fm.getAscent() - fm.getHeight() * 0.5;
                    g2.drawString(s, (float)((w - labelRect.getWidth()) * 0.5), (float)baseline);
                    g2.draw(new Line2D.Double(0.0, y - GAP_HEIGHT * 0.5, w, y - GAP_HEIGHT * 0.5));
                    g2.draw(new Line2D.Double(0.0, y + GAP_HEIGHT * 0.5, w, y + GAP_HEIGHT * 0.5));
                }
                y += unitHeight;
            }
        }
    }

    @Override
    public void addPopupListener(Listener<PopupEvent> l) {
    }

    @Override
    public void removePopupListener(Listener<PopupEvent> l) {
    }

    @Override
    public void firePopupEvent(PopupPanel panel) {
    }

    @Override
    public void popupHidden() {
    }

    /**
     * Invoked when user chooses Select/Deselect from the popup menu.
     * @param rec the Participant record which should be selected
     */
    @Override
    public void recordSelected(Record rec) {
        rec = ((ParticipantRecord)rec).getVariantRecord();
        for (Track t: owner.tracks) {
            boolean sel = false;
            if (rec instanceof MergedVariantRecord) {
                for (VariantRecord rec2: ((MergedVariantRecord)rec).getConstituents()) {
                    t.getRenderer().addToSelected(rec2);
                    sel = true;
                }
            } else {
                t.getRenderer().addToSelected(rec);
                sel = true;
            }
            if (sel) {
                t.repaintSelection();
            }
        }
    }
}
