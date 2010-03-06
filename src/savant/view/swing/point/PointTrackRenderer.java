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

package savant.view.swing.point;

import savant.model.Point;
import savant.model.PointRecord;
import savant.model.Resolution;
import savant.model.view.ColorScheme;
import savant.model.view.DrawingInstructions;
import savant.util.Range;
import savant.view.swing.GraphPane;
import savant.view.swing.TrackRenderer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.util.List;

/**
 *
 * @author mfiume
 */
public class PointTrackRenderer extends TrackRenderer {

    public PointTrackRenderer() { this(new DrawingInstructions()); }

    public PointTrackRenderer(
            DrawingInstructions drawingInstructions) {
        super(drawingInstructions);
    }

    @Override
    public void render(Graphics g, GraphPane gp) {

        Graphics2D g2 = (Graphics2D) g;

        gp.setIsOrdinal(true);

        DrawingInstructions di = this.getDrawingInstructions();


        Resolution r = (Resolution) di.getInstruction(DrawingInstructions.InstructionName.RESOLUTION.toString());

        List<Object> data = this.getData();
        if (data == null) return;
        int numdata = this.getData().size();


        if (r == Resolution.VERY_HIGH) {
            
            ColorScheme cs = (ColorScheme) di.getInstruction(DrawingInstructions.InstructionName.COLOR_SCHEME.toString());
            Color bgcolor = cs.getColor("BACKGROUND");
            Color linecolor = cs.getColor("LINE");

            float pointiness = 0.1F;

            for (int i = 0; i < numdata; i++) {

                Polygon p = new Polygon();

                double width = gp.getUnitWidth();
                double height = gp.getUnitHeight();

                PointRecord record = (PointRecord)data.get(i);
                Point sp = record.getPoint();

                Point2D.Double p1 = new Point2D.Double(gp.transformXPos(sp.getPosition()),0);
                Point2D.Double p2 = new Point2D.Double(gp.transformXPos(sp.getPosition()+1),0);
                Point2D.Double p3 = new Point2D.Double(gp.transformXPos(sp.getPosition()+1),gp.getHeight());
                Point2D.Double p4 = new Point2D.Double(gp.transformXPos(sp.getPosition()),gp.getHeight());

                p.addPoint((int)p1.x, (int)p1.y);
                p.addPoint((int)p2.x, (int)p2.y);
                p.addPoint((int)p3.x, (int)p3.y);
                p.addPoint((int)p4.x, (int)p4.y);

                g2.setColor(bgcolor);
                g2.fillPolygon(p);

                if (width > 5) {
                    g2.setColor(linecolor);
                    g2.drawPolygon(p);
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
}
