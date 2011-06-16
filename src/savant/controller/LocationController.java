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


package savant.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import savant.api.util.DialogUtils;
import savant.controller.event.GenomeChangedEvent;
import savant.data.types.Genome;
import savant.util.MiscUtils;
import savant.view.swing.sequence.SequenceTrack;
import savant.settings.BrowserSettings;
import savant.util.Range;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import savant.controller.event.LocationChangeCompletedListener;
import savant.controller.event.LocationChangedEvent;
import savant.controller.event.LocationChangedListener;



/**
 * Controller object to manage changes to current chromosome and viewed range.
 *
 * @author AndrewBrook, vwilliams
 */
public class LocationController {

    private static LocationController instance;
    private static final Log LOG = LogFactory.getLog(LocationController.class);

    // Current genome and reference
    private Genome loadedGenome;
    private String currentReference;

    // Undo/Redo Stack
    private Stack<History> undoStack;
    private Stack<History> redoStack;
    private int maxUndoStackSize = 50;
    private boolean shouldClearRedoStack = true;

    // The maximum and current viewable range
    private Range maximumViewableRange;
    private Range currentViewableRange;

    // Location Changed Listeners
    private List<LocationChangedListener> locationChangedListeners;
    private List<LocationChangeCompletedListener> locationChangeCompletedListeners;



    public static synchronized LocationController getInstance() {
        if (instance == null) {
            instance = new LocationController();
        }
        return instance;
    }

    private LocationController() {
        locationChangedListeners = new ArrayList();
        locationChangeCompletedListeners = new ArrayList();
        undoStack = new Stack<History>();
        redoStack = new Stack<History>();
    }

    //LOCATION//////////////////////////////////////////////////////////////////

    public void setLocation(String ref){
        setLocation(ref, false);
    }

    public void setLocation(String ref, boolean forceEvent){
        if (isValidAndNewReference(ref) || forceEvent){
            updateHistory();
            setReference(ref);
            fireLocationChangedEvent(true);
        }
    }

    public void setLocation(Range range){
        updateHistory();
        setRange(range);
        fireLocationChangedEvent(false);
    }

    public void setLocation(int from, int to){
        setLocation(new Range(from, to));
    }

    public void setLocation(String ref, int from, int to){
        setLocation(ref, new Range(from, to));
    }

    /**
     * This is the version of setLocation() which does the actual work.  All the other
     * overloads should be calling this one.
     */
    public void setLocation(String ref, Range range) {
        if (loadedGenome != null) {
            updateHistory();
            boolean newRef = false;
            if (isValidAndNewReference(ref)) {
                setReference(ref);
                newRef = true;
            }
            setRange(range);
            fireLocationChangedEvent(newRef);
        }
    }

    private void setLocation(History history){
        setLocation(history.reference, history.range);
    }

    //GENOME////////////////////////////////////////////////////////////////////

    /**
     * Get the loaded genome.
     * @return The loaded genome
     */
    public Genome getGenome() {
        return loadedGenome;
    }

    /**
     * Get whether or not a genome has been loaded.
     * @return True iff a genome has been loaded
     */
    public boolean isGenomeLoaded() {
        return loadedGenome != null;
    }

    public synchronized void setGenome(Genome genome) {
        if (genome == null) {
            // Sometimes we need to clear out the current genome in preparation for loading a new one.
            loadedGenome = null;
        } else {
            Genome oldGenome = loadedGenome;
            if (!genome.equals(oldGenome)) {
                loadedGenome = genome;
                fireGenomeChangedEvent(oldGenome);

                // Auto-select the first reference on the new genome.
                String ref = MiscUtils.set2List(loadedGenome.getReferenceNames()).get(0);
                setLocation(ref, true);
            }
        }
    }

    public synchronized void setSequence(SequenceTrack t) {
        if (loadedGenome == null) {
            setGenome(Genome.createFromTrack(t));
        } else if (!loadedGenome.isSequenceSet()) {
            // We have a loaded genome, but no sequence yet.  Plug it in.  Listeners can recognise this
            // event because the oldGenome and the newGenome on the GenomeChangedEvent will be the same.
            loadedGenome.setSequenceTrack(t);
            fireGenomeChangedEvent(loadedGenome);
        }
    }

    //REFERENCE/////////////////////////////////////////////////////////////////

    /**
     * Set the reference. Always check isValidAndNewReference before doing this.
     */
    private void setReference(String ref){
        currentReference = ref;
        setDefaultRange();
    }

    /**
     * Check if reference exists and is not the current reference
     *
     * @param ref           the new reference
     * @return True iff reference can be changed
     */
    private boolean isValidAndNewReference(String ref){
        if (getAllReferenceNames().contains(ref)) {
            if (!ref.equals(currentReference)) {
                return true;
            }
        } else {
            if (DataSourceController.getInstance().getDataSources().size() > 0) {
                DialogUtils.displayMessage("Reference " + ref + " not found in loaded tracks.");
            }
        }
        return false;
    }

    public String getReferenceName() {
        return this.currentReference;
    }

    public Set<String> getAllReferenceNames() {
        Set<String> all = new HashSet<String>();
        all.addAll(loadedGenome.getReferenceNames());
        all.addAll(getNonGenomicReferenceNames());
        return all;
    }

    public Set<String> getReferenceNames() {
        return loadedGenome.getReferenceNames();
    }

    public int getReferenceLength(String refname) {
        return loadedGenome.getLength(refname);
    }

    public Set<String> getNonGenomicReferenceNames() {
        return new HashSet<String>();
    }


    //RANGE/////////////////////////////////////////////////////////////////////

    /*
     * Should only be called before event is to be fired
     */
    private void setDefaultRange(){
        setMaxRange(new Range(1, loadedGenome.getLength()));
        setRange(1, Math.min(1000, loadedGenome.getLength()));
    }

    private void setRange(int from, int to){
        setRange(new Range(from, to));
    }

    private void setRange(Range r){
        LOG.debug("Setting range to " + r);        

        int from = r.getFrom();
        int to = r.getTo();

        /*
         * Make sure the current viewable range
         * stays within the maximum viewable range
         */
        if (from < getMaxRangeStart()) {
            int diff = getMaxRangeStart() - from;
            from = getMaxRangeStart();
            to += diff;
        }

        if (to > getMaxRangeEnd()) {
            int diff = to - getMaxRangeEnd();
            to = getMaxRangeEnd();
            from -= diff;
        }

        if (from < getMaxRangeStart()) {
            from = getMaxRangeStart();
            to = getMaxRangeEnd();
        }

        r = new Range(from, to);

        // set the current viewable range
        currentViewableRange = r;

        System.gc();
    }

    /**
     * Set the maximum viewable range (usually 0 to genome size).
     * @param r The range to set as max
     */
    public void setMaxRange(Range r) {
        maximumViewableRange = r;
        LOG.debug("Setting maximum range to " + r);
    }

    /**
     * Set the maximum viewable range (usually 0 to genome size).
     * @param from Lowest value of the maximum viewable range
     * @param to Highest value of the maximum viewable range
     */
    public void setMaxRange(int from, int to) {
        setMaxRange(new Range(from, to));
    }

    /**
     * Get the maximumViewableRange.
     * @return The maximumViewableRange
     */
    public Range getMaxRange() {
        return maximumViewableRange;
    }

    /**
     * Get the lower bound on the maximumViewableRange.
     * @return The lower bound on the maximumViewableRange
     */
    public int getMaxRangeStart() {
        return maximumViewableRange.getFrom();
    }

    /**
     * Get the upper bound on the maximumViewableRange.
     * @return The upper bound on the maximumViewableRange
     */
    public int getMaxRangeEnd() {
        return maximumViewableRange.getTo();
    }

    /**
     * Get the currentViewableRange.
     * @return The currentViewableRange
     */
    public Range getRange() {
        return currentViewableRange;
    }

    /**
     * Get the lower bound on the currentViewableRange.
     * @return The lower bound on the currentViewableRange
     */
    public int getRangeStart() {
        return currentViewableRange.getFrom();
    }

    /**
     * Get the upper bound on the currentViewableRange.
     * @return The upper bound on the currentViewableRange
     */
    public int getRangeEnd() {
        return currentViewableRange.getTo();
    }
    /**
     * == [[ SHIFTS ]] ==
     *  Shifts move the current viewable range left
     *  or right.
     */
    /**
     * Shift the currentViewableRange to the left
     */
    public void shiftRangeLeft() {
        shiftRange(false, 0.5);
    }

    /**
     * Shift the currentViewableRange to the right
     */
    public void shiftRangeRight() {
        shiftRange(true, 0.5);
    }

    /**
     * Shift the range left or right a specified percentage of the current
     * viewable range
     * @param shiftRight True if shift is to the right, false for left
     * @param percentwindow How much of the current window to be visible after
     * the shift
     */
    public void shiftRange(boolean shiftRight, double percentwindow) {
        Range r = getRange();
        int length = r.getLength();
        int direction = 1;
        if (!shiftRight) {
            direction = -1;
        }
        //int shift = (int) Math.ceil(direction * (percentwindow * length)) - ( (direction == 1) ? 1 : 0);
        int shift = (int)Math.ceil(direction * (percentwindow * length)) - ( (direction == -1 && length == 1) ? 1 : 0);

        r = new Range(r.getFrom() + shift, r.getTo() + shift);
        setLocation(r);
    }

    /**
     * Shift the currentViewableRange all the way to the left
     */
    public void shiftRangeFarLeft() {
        Range r = new Range(1, getRange().getLength());
        setLocation(r);
    }

    /**
     * Shift the currentViewableRange all the way to the right
     */
    public void shiftRangeFarRight() {
        Range r = new Range(getMaxRangeEnd() - getRange().getLength() + 1, getMaxRangeEnd());
        setLocation(r);
    }

    /**
     * == [[ Zoom ]] ==
     *  Zoom in makes the viewable range smaller
     *  Zoom out makes the viewable range larger
     */
    /**
     * Zoom to the specified length.
     * @param length The length to which to zoom
     */
    public void zoomToLength(int length) {
        zoomToLength(length, (getRangeEnd()+ 1 + getRangeStart()) / 2); // + 1 because ranges are inclusive
    }

    public void zoomToLength(int length, int center) {

        if(length > maximumViewableRange.getLength()){
            zoomToLength(maximumViewableRange.getLength());
        }

        length = Math.max(length, 1);
        LOG.debug("Zooming to length " + length);

        if (length > getMaxRangeEnd()) {
            return; // can't go any further out, stay at same range.
        }

        int half = Math.max(length / 2, 1);

        Range rg = new Range(center-half,center-half+length-1);
        setLocation(rg);
    }

    /**
     * Zoom out one level
     */
    public void zoomOut() {
        int length = Math.min(maximumViewableRange.getLength(), currentViewableRange.getLength() * BrowserSettings.zoomAmount);
        zoomToLength(length);
    }

    /**
     * Zoom in one level
     */
    public void zoomIn() {
        zoomToLength(currentViewableRange.getLength() / BrowserSettings.zoomAmount);
    }

    public void zoomInOnMouse() {
        int center = GraphPaneController.getInstance().getMouseXPosition();
        zoomToLength(currentViewableRange.getLength() / BrowserSettings.zoomAmount,center);
    }

    public void zoomOutFromMouse() {
        int center = GraphPaneController.getInstance().getMouseXPosition();
        zoomToLength(currentViewableRange.getLength() * BrowserSettings.zoomAmount,center);
    }
    
    //HISTORY///////////////////////////////////////////////////////////////////

    private void updateHistory(){
        if (shouldClearRedoStack && currentViewableRange != null && currentReference != null) {
            redoStack.clear();
            undoStack.push(new History(currentReference, currentViewableRange));
            while (undoStack.size() > maxUndoStackSize) {
                undoStack.remove(0);
            }
        }
    }

    public void undoLocationChange(){
        if (undoStack.size() > 0) {
            shouldClearRedoStack = false;
            redoStack.push(new History(currentReference, currentViewableRange));
            setLocation(undoStack.pop());
            shouldClearRedoStack = true;
        }
    }

    public void redoLocationChange(){
        if (redoStack.size() > 0) {
            shouldClearRedoStack = false;
            undoStack.push(new History(currentReference, currentViewableRange));
            setLocation(redoStack.pop());
            shouldClearRedoStack = true;
        }
    }

    private class History {
        public Range range;
        public String reference;
        public History(String ref, Range range){
            this.range = range;
            this.reference = ref;
        }
    }

    //EVENTS////////////////////////////////////////////////////////////////////

    private void fireGenomeChangedEvent(Genome oldGenome) {
        GenomeChangedEvent evt = new GenomeChangedEvent(oldGenome, loadedGenome);
        for (LocationChangedListener l: locationChangedListeners) {
            l.genomeChanged(evt);
        }
    }

    private synchronized void fireLocationChangedEvent(boolean newRef) {
        LocationChangedEvent evt = new LocationChangedEvent(newRef, currentReference, currentViewableRange);
        for (LocationChangedListener l: locationChangedListeners) {
            l.locationChanged(evt);
        }
    }

    public synchronized void fireLocationChangeCompletedEvent() {
        LocationChangedEvent evt = new LocationChangedEvent(false, currentReference, currentViewableRange);
        for (LocationChangeCompletedListener l : locationChangeCompletedListeners) {
            l.locationChangeCompleted(evt);
        }
    }

    public synchronized void addLocationChangedListener(LocationChangedListener l) {
        locationChangedListeners.add(l);
    }

    public synchronized void removeLocationChangedListener(LocationChangedListener l) {
        locationChangedListeners.remove(l);
    }

    public synchronized void addLocationChangeCompletedListener(LocationChangeCompletedListener l) {
        locationChangeCompletedListeners.add(l);
    }

    public synchronized void removeLocationChangeCompletedListener(LocationChangeCompletedListener l) {
        locationChangeCompletedListeners.remove(l);
    }

}
