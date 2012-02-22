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
package savant.format;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import savant.file.SavantROFile;
import savant.util.SavantFileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mfiume, tarkvara
 */
public class ContinuousFormatterHelper {
    private static final Log LOG = LogFactory.getLog(ContinuousFormatterHelper.class);
    
    /** No point in lower resolutions which would generate less than 1000 pixels of data. */
    public static final int NOTIONAL_SCREEN_SIZE = 1000;

    public static Map<String,List<Level>> readLevelHeaders(SavantROFile savantFile) throws IOException {

        // read the refname -> index position map
        Map<String, long[]> refMap = SavantFileUtils.readReferenceMap(savantFile);

        // change the offset
        savantFile.setHeaderOffset(savantFile.getFilePointer());

        Map<String,List<Level>> headers = new HashMap<String,List<Level>>();

        int headerNum = 0;

        LOG.debug("Number of headers to get: " + refMap.keySet().size());

        // keep track of the maximum end of tree position
        // (IMPORTANT NOTE: order of elements returned by keySet() is not guaranteed!!!)
        long maxend = Long.MIN_VALUE;
        for (String refname : refMap.keySet()) {
            long[] v = refMap.get(refname);

            savantFile.seek(v[0] + savantFile.getHeaderOffset());

            int numLevels = savantFile.readInt();
            List<Level> levels =  new ArrayList<Level>(numLevels);
            for (int i = 0; i < numLevels; i++) {
                levels.add(new Level(savantFile));
            }

            maxend = Math.max(maxend, savantFile.getFilePointer());

            headers.put(refname, levels);
            headerNum++;
        }

        // set the header offset appropriately
        savantFile.setHeaderOffset(maxend);

        return headers;
    }


    public static class Level {

        public long offset;
        public long size;
        public long recordSize;
        public long resolution;
        public Mode mode;

        public Level(long offset, long size, long recordSize, long resolution, Mode m) {
            this.offset = offset;
            this.size = size;
            this.recordSize = recordSize;
            this.resolution = resolution;
            this.mode = m;
        }

        public Level(SavantROFile savantFile) throws IOException {
            // FIXME: need to use readBinaryRecord?
            offset = savantFile.readLong();
            size = savantFile.readLong();
            recordSize = savantFile.readLong();
            resolution = savantFile.readLong();
            mode = Mode.ModeType.class.getEnumConstants()[savantFile.readInt()] == Mode.ModeType.FIXED ? new FixedMode(savantFile) : new VariableStepMode(savantFile);
        }

        public void writeHeader(DataOutputStream o) throws IOException {
            o.writeLong(offset);
            o.writeLong(size);
            o.writeLong(recordSize);
            o.writeLong(resolution);

            mode.write(o);
        }
    }

    public static class Mode {

        public enum ModeType { FIXED, VARIABLESTEP };

        public ModeType type;

        public Mode(ModeType t) {
            this.type = t;
        }

        public void write(DataOutputStream o) throws IOException {
            o.writeInt(type.ordinal());
        }
    }


    /**
     * Corresponds to fixedStep mode, as described in Wiggle format.
     * This is currently the only mode supported by Savant.
     */
    public static class FixedMode extends Mode {

        public int start;
        public int step;
        public int span;

        public FixedMode(int start, int step, int span) {
            super(ModeType.FIXED);
            this.start = start;
            this.step = step;
            this.span = span;
        }

        public FixedMode(SavantROFile savantFile) throws IOException {
            super(ModeType.FIXED);
            start = savantFile.readInt();
            step = savantFile.readInt();
            span = savantFile.readInt();
        }

        @Override
        public void write(DataOutputStream o) throws IOException {
            super.write(o);
            o.writeInt(start);
            o.writeInt(step);
            o.writeInt(span);
        }
    }

    /**
     * Corresponds to variableStep mode, as described in Wiggle format.
     * Not currently supported by Savant.
     */
    public static class VariableStepMode extends Mode {

        public int start;
        public int span;

        public VariableStepMode(int start, int span) {
            super(ModeType.VARIABLESTEP);
            this.start = start;
            this.span = span;
        }

        public VariableStepMode(SavantROFile savantFile) {
            super(ModeType.VARIABLESTEP);
            throw new UnsupportedOperationException("Reading variable-step mode not implemented.");
        }

        @Override
        public void write(DataOutputStream o) {
            throw new UnsupportedOperationException("Writing variable-step mode not supported.");
        }
    }
}
