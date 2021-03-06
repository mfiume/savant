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
package savant.data.sources;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import savant.api.adapter.RangeAdapter;
import savant.api.adapter.RecordFilterAdapter;
import savant.api.data.DataFormat;
import savant.api.util.Resolution;
import savant.data.types.GenericContinuousRecord;
import savant.file.*;
import savant.format.ContinuousFormatterHelper;
import savant.format.ContinuousFormatterHelper.Level;
import savant.util.MiscUtils;
import savant.util.Range;
import savant.util.SavantFileUtils;


/**
 * A data track containing ContinuousRecords. Depending on the range requested, data
 * may be drawn from one of the lower-resolution levels within the file.
 *
 * @author vwilliams
 */
public class GenericContinuousDataSource extends DataSource<GenericContinuousRecord> {

    private static final Log LOG = LogFactory.getLog(GenericContinuousDataSource.class);

    private SavantROFile savantFile;

    private int recordSize;

    private Map<String,List<Level>> refnameToLevelsIndex;

    public GenericContinuousDataSource(URI uri) throws IOException, SavantFileNotFormattedException {

        this.savantFile = new SavantROFile(uri, FileType.CONTINUOUS_GENERIC);
        this.refnameToLevelsIndex = ContinuousFormatterHelper.readLevelHeaders(savantFile);

        printLevelsMap(refnameToLevelsIndex);

        setRecordSize();
    }

    /**
     * Get the level which is best for displaying the given range.
     * @param levels   list of levels to be checked
     * @param r     the range to be displayed
     * @return
     */
    private Level getBestLevel(List<Level> levels, Range r) {
        for (int i = levels.size() - 1; i > 0; i--) {
            Level lev = levels.get(i);
            if (r.getLength() > lev.resolution * ContinuousFormatterHelper.NOTIONAL_SCREEN_SIZE / 2) {
                return lev;
            }
        }
        return levels.get(0);
    }

    /**
     * Get the records representing the data to be displayed for the given range.
     *
     * @param ref       e.g. "chr1"
     * @param r         the range of bases to be displayed
     * @param ignored   historical only; no longer used
     * @return  List of records to be displayed for the given range
     * @throws IOException
     */
    @Override
    public List<GenericContinuousRecord> getRecords(String ref, RangeAdapter r, Resolution ignored, RecordFilterAdapter filt) throws IOException {

        List<GenericContinuousRecord> data = new ArrayList<GenericContinuousRecord>();

        if (!savantFile.containsDataForReference(ref)) {
            ref = MiscUtils.homogenizeSequence(ref);
            if (!savantFile.containsDataForReference(ref)) {
                return data;
            }
        }

        Level lev = getBestLevel(refnameToLevelsIndex.get(ref), (Range)r);
        LOG.debug("Chose " + lev.resolution + " as the best for range (" + r.getFrom() + "-" + r.getTo() + ")");
        int rangeEnd = r.getTo() + 1;

        long seekPos = lev.offset + (r.getFrom() - 1) / lev.resolution * recordSize;

        if (savantFile.seek(ref, seekPos) >= 0) {
            LOG.debug("Sought to " + seekPos + " to find data for " + r.getFrom());
            for (int pos = r.getFrom(); pos <= rangeEnd; pos += lev.resolution) {

                data.add(GenericContinuousRecord.valueOf(ref, pos, savantFile.readFloat()));

                if (savantFile.getFilePointer() >= savantFile.getHeaderOffset() + savantFile.getReferenceOffset(ref) + lev.offset + lev.size) {
                    // We've read all the data available for this level.  The rest of the
                    // range will have no data.
                    LOG.debug("File position " + savantFile.getFilePointer() + " was past end of level (" + (savantFile.getHeaderOffset() + savantFile.getReferenceOffset(ref) + lev.offset + lev.size) + ").");
                    break;
                }
            }
        }

        return data;
    }

    @Override
    public void close() {
        try {
            if (savantFile != null) savantFile.close();
        } catch (IOException ignore) { }
    }

    public int getRecordSize() {
        return recordSize;
    }

    public final void setRecordSize() throws IOException {
        this.recordSize = SavantFileUtils.getRecordSize(savantFile);
        LOG.debug("Setting record size to " + this.recordSize);
    }

    @Override
    public Set<String> getReferenceNames() {
        return savantFile.getReferenceMap().keySet();
    }

    private void printLevelsMap(Map<String, List<Level>> refnameToLevelsIndex) {
        if (LOG.isDebugEnabled()) {
            for (String refname : refnameToLevelsIndex.keySet()) {
                LOG.debug("Level header for reference " + refname);
                LOG.debug("Levels list " + refnameToLevelsIndex.get(refname));
                LOG.debug("Number of levels " + refnameToLevelsIndex.get(refname).size());
                for (Level l : refnameToLevelsIndex.get(refname)) {
                    LOG.debug("Offset: " + l.offset);
                    LOG.debug("Size: " + l.size);
                    LOG.debug("Record size: " + l.recordSize);
                    LOG.debug("Type: " + l.mode.type);
                }
            }
        }
    }

    @Override
    public URI getURI() {
        return savantFile.getURI();
    }

    @Override
    public final DataFormat getDataFormat() {
        return DataFormat.CONTINUOUS;
    }

    @Override
    public final String[] getColumnNames() {
        return GenericContinuousRecord.COLUMN_NAMES;
    }
}
