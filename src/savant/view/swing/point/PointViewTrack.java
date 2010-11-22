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

import savant.data.sources.GenericPointDataSource;
import savant.file.FileFormat;
import savant.util.*;
import savant.util.ColorScheme;
import savant.util.DrawingInstructions;
import savant.view.swing.TrackRenderer;
import savant.view.swing.ViewTrack;

import java.util.ArrayList;
import java.util.List;
import savant.api.adapter.RangeAdapter;
import savant.settings.ColourSettings;

/**
 *
 * @author mfiume
 */
public class PointViewTrack extends ViewTrack {

    public List<Object> savedList = null;

    public PointViewTrack(String name, GenericPointDataSource pointTrack) {
        super(name, FileFormat.POINT_GENERIC, pointTrack);
        setColorScheme(getDefaultColorScheme());
        this.notifyViewTrackControllerOfCreation();
    }

    private ColorScheme getDefaultColorScheme() {
        ColorScheme c = new ColorScheme();

        /* add settings here */
        c.addColorSetting("Background", ColourSettings.getPointFill());
        c.addColorSetting("Line", ColourSettings.getPointLine());

        return c; 
    }

    @Override
    public void resetColorScheme() {
        setColorScheme(getDefaultColorScheme());
    }

    public Resolution getResolution(RangeAdapter range)
    {
        long length = range.getLength();

        if (length > 100000) { return Resolution.VERY_LOW; }
        return Resolution.VERY_HIGH;
    }

    @Override
    public List<Object> retrieveData(String reference, RangeAdapter range, Resolution resolution) throws Exception {
        return new ArrayList<Object>(getDataSource().getRecords(reference, range, resolution));
    }

    public void prepareForRendering(String reference, Range range) throws Throwable {
        Resolution r = getResolution(range);

        List<Object> data = null;

        switch (r)
        {
            case VERY_HIGH:
                data = this.retrieveAndSaveData(reference, range);
                break;
            default:
                this.saveNullData();
                break;
        }

        for (TrackRenderer renderer : getTrackRenderers()) {
            boolean contains = (this.getDataSource().getReferenceNames().contains(reference) || this.getDataSource().getReferenceNames().contains(MiscUtils.homogenizeSequence(reference)));
            renderer.getDrawingInstructions().addInstruction(DrawingInstructions.InstructionName.RESOLUTION, r);
            renderer.getDrawingInstructions().addInstruction(DrawingInstructions.InstructionName.COLOR_SCHEME, this.getColorScheme());
            renderer.getDrawingInstructions().addInstruction(DrawingInstructions.InstructionName.AXIS_RANGE, AxisRange.initWithRanges(range, getDefaultYRange()));
            renderer.getDrawingInstructions().addInstruction(DrawingInstructions.InstructionName.REFERENCE_EXISTS, contains);
            renderer.getDrawingInstructions().addInstruction(DrawingInstructions.InstructionName.SELECTION_ALLOWED, true);

            renderer.setData(data);
        }
    }

    private Range getDefaultYRange() {
        return new Range(0, 1);
    }

}
