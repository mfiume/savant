/*
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

package savant.view.swing.interval;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import savant.data.types.Interval;
import savant.data.types.IntervalRecord;
import savant.data.types.Record;
import savant.file.DataFormat;
import savant.util.AxisRange;
import savant.util.ColorScheme;
import savant.util.DrawingInstructions;
import savant.util.IntervalPacker;
import savant.util.Mode;
import savant.util.Range;
import savant.util.Resolution;
import savant.view.swing.GraphPane;
import savant.view.swing.TrackRenderer;
import savant.view.swing.util.GlassMessagePane;


/**
 *
 * @author mfiume
 */
public class IntervalTrackRenderer extends TrackRenderer {

    public IntervalTrackRenderer() { this(new DrawingInstructions()); }

    public IntervalTrackRenderer(
            DrawingInstructions drawingInstructions) {
        super(drawingInstructions);
        this.dataType = DataFormat.INTERVAL_GENERIC;
    }

    @Override
    public void render(Graphics g, GraphPane gp) {

        Graphics2D g2 = (Graphics2D) g;
        gp.setIsOrdinal(true);
        this.clearShapes();

        DrawingInstructions di = this.getDrawingInstructions();

        Boolean refexists = (Boolean) di.getInstruction(DrawingInstructions.InstructionName.REFERENCE_EXISTS);
        if (!refexists) {
            GlassMessagePane.draw(g2, gp, "no data for reference", 500);
            return;
        }

        Mode drawMode = (Mode) di.getInstruction(DrawingInstructions.InstructionName.MODE);
        Resolution r = (Resolution) di.getInstruction(DrawingInstructions.InstructionName.RESOLUTION.toString());

        if (drawMode.getName().equals("SQUISH")) {
            renderSquishMode(g2, gp, r);
        }
        else if (drawMode.getName().equals("ARC")) {
            renderArcMode(g2, gp, r);
        }
        else if (drawMode.getName().equals("PACK")) {
            renderPackMode(g2, gp, r);
        }
    }

    private void renderSquishMode(Graphics2D g2, GraphPane gp, Resolution r) {

        List<Record> data = getData();
        int numdata = getData().size();

        ColorScheme cs = (ColorScheme) getDrawingInstructions().getInstruction(DrawingInstructions.InstructionName.COLOR_SCHEME.toString());
        Color bgcolor = cs.getColor("Translucent Graph");
        Color linecolor = cs.getColor("Line");

        AxisRange axisRange = (AxisRange) getDrawingInstructions().getInstruction(DrawingInstructions.InstructionName.AXIS_RANGE);
        
        if (r == Resolution.VERY_HIGH || r == Resolution.HIGH) {

            gp.setIsOrdinal(true);
            gp.setXRange(axisRange.getXRange());
            gp.setYRange(axisRange.getYRange());
 
            double unitWidth;
            double unitHeight;
            unitWidth = gp.getUnitWidth();
            unitHeight = gp.getUnitHeight();

            for (int i = 0; i < numdata; i++) {

                IntervalRecord record = (IntervalRecord)data.get(i);
                Interval inter = record.getInterval();


                double x = gp.transformXPos(inter.getStart());
                double y = 0;
                double w = unitWidth*inter.getLength();
                double h = unitHeight;

                Rectangle2D.Double rect = new Rectangle2D.Double(x, y, w, h);

                g2.setColor(bgcolor);
                g2.fill(rect);

                if (w > 5) {
                    g2.setColor(linecolor);
                    g2.draw(rect);
                }
            }
        }
        else {
            GlassMessagePane.draw(g2, gp, "Zoom in to see intervals", 300);
        }

    }

    private void renderArcMode(Graphics2D g2, GraphPane gp, Resolution r) {


        List<Record> data = getData();
        int numdata = getData().size();

        ColorScheme cs = (ColorScheme) getDrawingInstructions().getInstruction(DrawingInstructions.InstructionName.COLOR_SCHEME.toString());
        Color bgcolor = cs.getColor("Opaque Graph");

        AxisRange axisRange = (AxisRange) getDrawingInstructions().getInstruction(DrawingInstructions.InstructionName.AXIS_RANGE);

        if (r == Resolution.VERY_HIGH || r == Resolution.HIGH) {

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            gp.setIsOrdinal(false);
            gp.setXRange(axisRange.getXRange());
            gp.setYRange(axisRange.getYRange());

            for (int i = 0; i < numdata; i++) {

                IntervalRecord record = (IntervalRecord)data.get(i);
                Interval inter = record.getInterval();

                long arcLength = inter.getLength();
                int arcHeight = (int)(Math.log((double)arcLength));

                int rectWidth = (int)(gp.getWidth(arcLength));
                int rectHeight = (int)(gp.getHeight(arcHeight)*2);

                int xOrigin = (int)(gp.transformXPos(inter.getStart()));
                int yOrigin = (int)(gp.transformYPos(arcHeight));

                g2.setColor(bgcolor);
                g2.drawArc(xOrigin, yOrigin, rectWidth, rectHeight, -180, -180);
            }
        }

    }

    private void renderPackMode(Graphics2D g2, GraphPane gp, Resolution r) {

        List<Record> data = getData();

        ColorScheme cs = (ColorScheme) getDrawingInstructions().getInstruction(DrawingInstructions.InstructionName.COLOR_SCHEME.toString());
        Color bgcolor = cs.getColor("Opaque Graph");
        Color linecolor = cs.getColor("Line");

        AxisRange axisRange = (AxisRange) getDrawingInstructions().getInstruction(DrawingInstructions.InstructionName.AXIS_RANGE);

        if (r == Resolution.VERY_HIGH || r == Resolution.HIGH) {

            IntervalPacker packer = new IntervalPacker(data);
//            Map<Integer, ArrayList<IntervalRecord>> intervals = packer.pack(2);
            ArrayList<List<IntervalRecord>> intervals = packer.pack(2);

            gp.setIsOrdinal(false);
            gp.setXRange(axisRange.getXRange());
            int maxYRange;
            int numIntervals = intervals.size();
            // Set the Y range to the closest value of 10, 20, 50, 100, n*100
            if (numIntervals <= 10) maxYRange = 10;
            else if (numIntervals <= 20) maxYRange = 20;
            else if (numIntervals <=50) maxYRange = 50;
            else if (numIntervals <= 100) maxYRange = 100;
            else maxYRange = numIntervals;
            gp.setYRange(new Range(0,maxYRange));

            double unitHeight;
            unitHeight = gp.getUnitHeight();

            // display only a message if intervals will not be visible at this resolution
            if (unitHeight < 1) {
                GlassMessagePane.draw(g2, gp, "Increase height of window", 300);
                return;
            }

            // scan the map of intervals and draw the intervals for each level
            for (int k=0; k<intervals.size(); k++) {

//                ArrayList<IntervalRecord> intervalsThisLevel = intervals.get(k);
                List<IntervalRecord> intervalsThisLevel = intervals.get(k);

                for (IntervalRecord intervalRecord : intervalsThisLevel) {
                    Interval interval = intervalRecord.getInterval();
                    double x = gp.transformXPos(interval.getStart());
                    double y = gp.transformYPos(k)-unitHeight;
                    double w = gp.getWidth(interval.getLength());
                    if (w < 1) continue; // don't draw intervals less than one pixel wide
                    double h = unitHeight;

                    Rectangle2D.Double intervalRect = new Rectangle2D.Double(x, y, w, h);

                    g2.setColor(bgcolor);
                    g2.fill(intervalRect);
                    this.recordToShapeMap.put(intervalRecord, intervalRect);

                    if (w > 5) {
                        g2.setColor(linecolor);
                        g2.draw(intervalRect);
                    }
                }
            }
        }

    }

    @Override
    public boolean isOrdinal() {
        return true;
    }

    @Override
    public Range getDefaultYRange() {
        return new Range(0,1);
    }

    @Override
    public boolean hasHorizontalGrid() {
        Mode drawMode = (Mode)getDrawingInstructions().getInstruction(DrawingInstructions.InstructionName.MODE);
        String modeName = drawMode.getName();
        if (modeName.equals("ARC")) {
            return true;
        } else {
            return false;
        }
    }
}
