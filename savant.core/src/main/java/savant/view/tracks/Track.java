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
package savant.view.tracks;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.text.NumberFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import savant.api.adapter.DataSourceAdapter;
import savant.api.adapter.FrameAdapter;
import savant.api.adapter.RecordFilterAdapter;
import savant.api.adapter.TrackAdapter;
import savant.api.data.DataFormat;
import savant.api.data.Record;
import savant.api.event.DataRetrievalEvent;
import savant.api.util.DialogUtils;
import savant.api.util.Resolution;
import savant.controller.TrackController;
import savant.exception.RenderingException;
import savant.exception.SavantTrackCreationCancelledException;
import savant.plugin.SavantPanelPlugin;
import savant.selection.SelectionController;
import savant.util.*;

/**
 * Class to handle the preparation for rendering of a track. Handles colour
 * schemes and drawing instructions, getting and filtering of data, setting of
 * vertical axis, etc. The ranges associated with various resolutions are also
 * handled here, and the drawing modes are defined.
 *
 * @author mfiume
 */
public abstract class Track extends Controller<DataRetrievalEvent> implements TrackAdapter {

    private static final Log LOG = LogFactory.getLog(Track.class);
    protected static final RenderingException ZOOM_MESSAGE = new RenderingException(MiscUtils.MAC ? "Zoom in to see data\nTo view data at this range, change Preferences > Track Resolutions" : "Zoom in to see data\nTo view data at this range, change Edit > Preferences > Track Resolutions", RenderingException.LOWEST_PRIORITY);
    private final String name;
    private ColourScheme colourScheme;
    private List<Record> dataInRange;
    protected DrawingMode drawingMode = DrawingMode.STANDARD;
    protected final TrackRenderer renderer;
    private final DataSourceAdapter dataSource;
    private DataRetriever retriever;
    protected RecordFilterAdapter filter;
    /**
     * In practice this will be a JIDE DockableFrame, but we could conceivably
     * have a stub implementation for headless or web-based operation.
     */
    private FrameAdapter frame;

    /**
     * Constructor a new track with the given renderer.
     *
     * @param dataSource track data source; name, type, and will be derived from
     * this
     * @param renderer the <code>TrackRenderer</code> to be used for this track
     */
    protected Track(DataSourceAdapter dataSource, TrackRenderer renderer) throws SavantTrackCreationCancelledException {

        this.dataSource = dataSource;
        this.renderer = renderer;

        String n = getUniqueName(dataSource.getName());

        if (n == null) {
            throw new SavantTrackCreationCancelledException();
        }

        name = n;

        renderer.setTrackName(name);
        addListener(renderer);
    }

    @Override
    public String toString() {
        return name;
    }

    private String getUniqueName(String name) {
        String result = name;
        while (TrackController.getInstance().containsTrack(result)) {
            result = DialogUtils.displayInputMessage("Duplicate Track",
                    "A track with that name already exists. Please enter a new name:",
                    result);
            if (result == null) {
                return null;
            }
        }
        return result;
    }

    /**
     * Get the current colour scheme.
     *
     * @return ColourScheme
     */
    public ColourScheme getColourScheme() {
        if (colourScheme == null) {
            colourScheme = getDefaultColourScheme();
        }
        return colourScheme;
    }

    /**
     * Set individual colour.
     *
     * @param key one of Savant's standard colour keys
     * @param color new color
     */
    public void setColor(ColourKey key, Color color) {
        getColourScheme().setColor(key, color);
    }

    public abstract ColourScheme getDefaultColourScheme();

    /**
     * Get the name of this track. Usually constructed from the file name.
     *
     * @return track name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Get the data currently being displayed (or ready to be displayed)
     *
     * @return List of data objects
     */
    @Override
    public List<Record> getDataInRange() {
        return dataInRange;
    }

    @Override
    public List<Record> getSelectedDataInRange() {
        return SelectionController.getInstance().getSelections(getName());
    }

    /**
     * Get current draw mode
     *
     * @return draw mode as Mode
     */
    @Override
    public DrawingMode getDrawingMode() {
        return drawingMode;
    }

    /**
     * Get all valid draw modes for this track.
     *
     * @return List of draw Modes
     */
    @Override
    public DrawingMode[] getValidDrawingModes() {
        return new DrawingMode[]{DrawingMode.STANDARD};
    }

    /**
     * Set the current draw mode.
     *
     * @param mode
     */
    @Override
    public void setDrawingMode(DrawingMode mode) {
        drawingMode = mode;
        frame.drawModeChanged(this);
    }

    /**
     * Get the record (data) track associated with this view track (if any.)
     *
     * @return Record Track or null (in the case of a genome.)
     */
    @Override
    public DataSourceAdapter getDataSource() {
        return dataSource;
    }

    /**
     * Convenience method to get a track's data format.
     *
     * @return the DataFormat of the track's DataSource
     */
    @Override
    public DataFormat getDataFormat() {
        return dataSource.getDataFormat();
    }

    /**
     * Utility method to determine whether this track has data for the given
     * reference.
     *
     * @param ref the reference to be checked
     * @return true if the track has data for ref
     */
    public boolean containsReference(String ref) {
        return dataSource.getReferenceNames().contains(ref) || dataSource.getReferenceNames().contains(MiscUtils.homogenizeSequence(ref));
    }

    /**
     * Retrieve a JPanel for the layer which plugins can use to draw on top of
     * the track, creating one if necessary.
     *
     * @return component to draw onto (guaranteed to be non-null if called after <code>TrackEvent.OPENED<code> notification has been received)
     * @since 1.6.0
     */
    @Override
    public JPanel getLayerCanvas(SavantPanelPlugin plugin) {
        return frame.getLayerCanvas(plugin, true);
    }

    /**
     * Create a JPanel for the layer which a plugin can use to draw on top of
     * the track.
     *
     * @return component to draw onto or null if frame not initialized yet
     * @deprecated Renamed to <code>createLayerCanvas()</code>.
     */
    @Override
    public JPanel getLayerCanvas() {
        return getLayerCanvas(null);
    }

    /**
     * For use by plugins. Scale a pixel position along the x-axis into a base
     * position.
     *
     * @since 1.6.0
     */
    @Override
    public int transformXPixel(double pix) {
        return frame.getGraphPane().transformXPixel(pix);
    }

    /**
     * For use by plugins. Scale a position in bases into a pixel position along
     * the x-axis.
     *
     * @since 1.6.0
     */
    @Override
    public double transformXPos(int pos) {
        return frame.getGraphPane().transformXPos(pos);
    }

    /**
     * For use by plugins. Scale a pixel position along the y-axis into a
     * logical vertical position.
     *
     * @since 1.6.0
     */
    @Override
    public double transformYPixel(double pix) {
        return frame.getGraphPane().transformYPixel(pix);
    }

    /**
     * For use by plugins. Scale a logical vertical position into a pixel
     * position along the y-axis.
     *
     * @since 1.6.0
     */
    @Override
    public double transformYPos(double pos) {
        return frame.getGraphPane().transformYPos(pos);
    }

    /**
     * Given a record, determine the bounds which would be used for displaying
     * that record.
     *
     * @param rec the record whose bounds we're interested in
     * @return the record's bounds in pixels, relative to the track's bounds (or
     * null
     */
    @Override
    public Rectangle getRecordBounds(Record rec) {
        Shape s = renderer.recordToShapeMap.get(rec);
        if (s != null) {
            return s.getBounds();
        }
        return null;
    }

    /**
     * Given a location within a track window, determine the record which lies
     * at that location. If multiple records overlap at the given position, only
     * the first one will be returned.
     *
     * @param pt the point we're interested in
     * @return the record at that position, or <code>null</code> if no record is
     * there
     */
    @Override
    public Record getRecordAtPos(Point pt) {
        for (Record r : renderer.recordToShapeMap.keySet()) {
            Shape s = renderer.recordToShapeMap.get(r);
            if (s.contains(new Point2D.Double(pt.x, pt.y))) {
                return r;
            }
        }
        return null;
    }

    public FrameAdapter getFrame() {
        return frame;
    }

    public void setFrame(FrameAdapter f, DrawingMode initialMode) {
        frame = f;
        if (initialMode != null) {
            drawingMode = initialMode;
        }
        addListener(frame);
    }

    /**
     * Retrieve the renderer associated with this track.
     *
     * @return the track's renderer
     */
    public TrackRenderer getRenderer() {
        return renderer;
    }

    /**
     * Prepare this track to render the given range. Since the actual
     * data-retrieval is now done on a separate thread, preparing to render
     * should not throw any exceptions.
     *
     * @param ref the reference to be rendered
     * @param r the range to be rendered
     */
    public abstract void prepareForRendering(String ref, Range r);

    /**
     * Method which plugins can use to force the Track to repaint itself.
     */
    @Override
    public void repaint() {
        frame.getGraphPane().setRenderForced();
        frame.getGraphPane().repaint();
    }

    /**
     * Like repaint(), but doesn't force a re-render. Intended for updating the
     * track's selection.
     */
    public void repaintSelection() {
        frame.getGraphPane().repaint();
    }

    @Override
    public boolean isSelectionAllowed() {
        return renderer.selectionAllowed(false);
    }

    /**
     * All ordinary tracks have integer markings along their x axes.
     *
     * @param res the resolution to be considered (ignored)
     * @return <code>AxisType.INTEGER</code>
     */
    @Override
    public AxisType getXAxisType(Resolution res) {
        return AxisType.INTEGER;
    }

    /**
     * A number of common track types (Sequence, Point, RichInterval) have no
     * y-axis, so they all share this implementation.
     *
     * @param res the resolution to be considered (ignored)
     * @return <code>AxisType.NONE</code>
     */
    @Override
    public AxisType getYAxisType(Resolution res) {
        return AxisType.NONE;
    }

    /**
     * Request data from the underlying data track at the current resolution. A
     * new thread will be started.
     *
     * @param reference The reference within which to retrieve objects
     * @param range The range within which to retrieve objects
     */
    public void requestData(String reference, Range range) {
        if (retriever != null) {
            if (retriever.reference.equals(reference) && retriever.range.equals(range)) {
                LOG.debug("Nothing to request, already busy retrieving " + reference + ":" + range);
                return;
            } else {
                LOG.debug("You're wasting your time on " + reference + ":" + range);
            }
        }
        dataInRange = null;
        fireEvent(new DataRetrievalEvent(this, range));

        retriever = new DataRetriever(reference, range, filter);
        retriever.start();



        try {
            if (retriever != null) {
                retriever.join(1000);
                if (retriever != null && retriever.isAlive()) {
                    // Join timed out, but we are still waiting for data to arrive.
                    LOG.trace("Join timed out, putting up progress-bar.");
                }
            }
        } catch (InterruptedException ix) {
            LOG.error("DataRetriever interrupted during join.", ix);
            retriever = null;
        }
    }

    /**
     * Fires a DataSource successful completion event. It will be posted to the
     * AWT event-queue thread, so that UI code can function properly.
     */
    private void fireDataRetrievalCompleted(final Range r) {
        MiscUtils.invokeLaterIfNecessary(new Runnable() {
            @Override
            public void run() {
                fireEvent(new DataRetrievalEvent(Track.this, dataInRange, r));
            }
        });
    }

    /**
     * Fires a DataSource error event. It will be posted to the AWT event-queue
     * thread, so that UI code can function properly.
     */
    private void fireDataRetrievalFailed(final Throwable x, final Range r) {
        MiscUtils.invokeLaterIfNecessary(new Runnable() {
            @Override
            public void run() {
                fireEvent(new DataRetrievalEvent(Track.this, x, r));
            }
        });
    }

    /**
     * Cancel an in-progress request to retrieve data.
     */
    public void cancelDataRequest() {
        if (retriever != null) {
            retriever.interrupt();  // Will fire fireDataRetrievalFailed when InterruptedException is caught.
        }
    }

    /**
     * Store null to dataInRange. This implicitly means that data-retrieval is
     * considered to have completed without error.
     *
     * @throws Exception
     */
    public void saveNullData(Range r) {
        dataInRange = null;
        fireDataRetrievalCompleted(r);
    }

    /**
     * Retrieve data from the underlying data source. The default behaviour is
     * just to call getRecords on the track's data source.
     *
     * @param r The range within which to retrieve objects
     * @param res The resolution at which to get data
     * @return a List of data objects from the given range and resolution
     * @throws Exception
     */
    protected synchronized List<Record> retrieveData(String ref, Range r, Resolution res, RecordFilterAdapter filter) throws Exception {
        return getDataSource().getRecords(ref, r, res, filter);
    }

    private static class MemoryMonitor extends Thread {

        private final Thread thread;
        private int MEMORY_LIMIT = 20; // in mb
        private boolean warned;

        public MemoryMonitor(DataRetriever r) {
            this.thread = r;
        }

        public boolean didWarn() {
            return warned;
        }

        @Override
        public void run() {

            Runtime runtime = Runtime.getRuntime();
            NumberFormat format = NumberFormat.getInstance();

            while (true) {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    return;
                }

                long freeMemory = runtime.freeMemory() / 1024*1024; // in mb
                if (freeMemory < MEMORY_LIMIT) {
                    warned = true;
                    thread.interrupt();
                    return;
                }
            }
        }
    }

    private class DataRetriever extends Thread {

        String reference;
        Range range;
        RecordFilterAdapter filter;
        private final MemoryMonitor memoryMonitor;

        DataRetriever(String ref, Range r, RecordFilterAdapter filt) {
            super("DataRetriever-" + ref + ":" + r);
            reference = ref;
            range = r;
            filter = filt;
            memoryMonitor = new MemoryMonitor(this);
        }

        @Override
        public void run() {

            try {
                memoryMonitor.start();
                LOG.debug("Retrieving data for " + name + "(" + reference + ":" + range + ")");
                dataInRange = retrieveData(reference, range, getResolution(range), filter);
                if (isInterrupted()) {
                    LOG.info(name + " was interrupted.");
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Retrieved " + (dataInRange != null ? Integer.toString(dataInRange.size()) : "no") + " records for " + name + "(" + reference + ":" + range + ")");
                    }
                    fireDataRetrievalCompleted(range);
                }
            } catch (InterruptedException x) {
                if (memoryMonitor.didWarn()) {
                    fireDataRetrievalFailed(new Exception("Retrieval stopped due to memory warning"), range);
                } else {
                    fireDataRetrievalFailed(new Exception("Data retrieval cancelled"), range);
                }
            } catch (Throwable x) {
                if (NetworkUtils.isStreamCached(dataSource.getURI())) {
                    LOG.info("Cached read failed for " + getName() + " with " + MiscUtils.getMessage(x) + "; deleting cache file and retrying.");
                    try {
                        RemoteFileCache.removeCacheEntry(dataSource.getURI().toString());
                        dataInRange = retrieveData(reference, range, getResolution(range), filter);
                        fireDataRetrievalCompleted(range);
                    } catch (Throwable x2) {
                        LOG.error("Data retrieval failed twice.", x2);
                        fireDataRetrievalFailed(x2, range);
                    }
                } else {
                    LOG.error("Data retrieval failed.", x);
                    fireDataRetrievalFailed(x, range);
                }
            }
            memoryMonitor.interrupt();

            retriever = null;
        }
    }
}
