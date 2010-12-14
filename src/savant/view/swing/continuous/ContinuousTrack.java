/*
 * ContinuousTrack.java
 * Created on Jan 19, 2010
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

package savant.view.swing.continuous;

import java.io.IOException;
import java.util.List;

import savant.api.adapter.RangeAdapter;
import savant.data.sources.DataSource;
import savant.data.types.ContinuousRecord;
import savant.data.types.Record;
import savant.exception.SavantTrackCreationCancelledException;
import savant.settings.ColourSettings;
import savant.util.*;
import savant.view.swing.Savant;
import savant.view.swing.Track;


/**
 * A helper class to set up rendering of a ContinuousTrack
 *
 * @author vwilliams
 */
 public class ContinuousTrack extends Track {

    public ContinuousTrack(DataSource track) throws SavantTrackCreationCancelledException {
        super(track, new ContinuousTrackRenderer());
        setColorScheme(getDefaultColorScheme());
        this.notifyControllerOfCreation();
    }

    @Override
    public void prepareForRendering(String reference, Range range) throws IOException {

        Resolution r = getResolution(range);
        List<Record> data = retrieveAndSaveData(reference, range);
        renderer.addInstruction(DrawingInstruction.RANGE, range);
        renderer.addInstruction(DrawingInstruction.RESOLUTION, r);
        renderer.addInstruction(DrawingInstruction.COLOR_SCHEME, getColorScheme());
        renderer.addInstruction(DrawingInstruction.REFERENCE_EXISTS, containsReference(reference));
        renderer.addInstruction(DrawingInstruction.SELECTION_ALLOWED, true);

        int maxDataValue = getMaxValue(data);
        renderer.addInstruction(DrawingInstruction.AXIS_RANGE, AxisRange.initWithRanges(range, new Range(0, maxDataValue)));

        Savant.log("Max Data Value is " + maxDataValue);
        renderer.setData(data);
    }

    @Override
    public List<Record> retrieveData(String reference, RangeAdapter range, Resolution resolution) throws IOException {
        return getDataSource().getRecords(reference, range, resolution);
    }

    /**
     * Continuous tracks no longer use the Resolution enum.
     *
     * @param range
     * @return
     * @deprecated
     */
    @Override
    @Deprecated
    public Resolution getResolution(RangeAdapter range) {
        return Resolution.VERY_HIGH;
    }

    private ColorScheme getDefaultColorScheme() {
        ColorScheme c = new ColorScheme();

        /* add settings here */
        c.addColorSetting("Line", ColourSettings.getContinuousLine());

        return c;
    }

    @Override
    public void resetColorScheme() {
        setColorScheme(getDefaultColorScheme());
    }

    private Range getDefaultYRange() {
        return new Range(0, 1);
    }

    private int getMaxValue(List<Record> data) {
        float max = 0f;
        for (Record r: data) {
            float val = ((ContinuousRecord)r).getValue().getValue();
            if (val > max) max = val;
        }
        return (int)Math.ceil(max);
    }

}
