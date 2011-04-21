/*
 *    Copyright 2011 University of Toronto
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

package savant.settings;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mfiume
 */
public class TrackResolutionSettings {

    // The defaults
    private static int SEQUENCE_LOW_TO_HIGH = 10000;
    private static int INTERVAL_LOW_TO_HIGH = 1000000;
    private static int BAM_DEFAULT_LOW_TO_HIGH = 20000;
    private static int BAM_ARC_LOW_TO_HIGH = 100000;

    public static int getSequenceLowToHighThresh() {
        return Integer.parseInt(PersistentSettings.getInstance().getProperty("SEQUENCE_LOW_TO_HIGH", SEQUENCE_LOW_TO_HIGH + ""));
    }

    public static int getIntervalLowToHighThresh() {
        return Integer.parseInt(PersistentSettings.getInstance().getProperty("INTERVAL_LOW_TO_HIGH", INTERVAL_LOW_TO_HIGH + ""));
    }

    public static int getBAMDefaultModeLowToHighThresh() {
        return Integer.parseInt(PersistentSettings.getInstance().getProperty("BAM_DEFAULT_LOW_TO_HIGH", BAM_DEFAULT_LOW_TO_HIGH + ""));
    }

    public static int getBAMArcModeLowToHighThresh() {
        return Integer.parseInt(PersistentSettings.getInstance().getProperty("BAM_ARC_LOW_TO_HIGH", BAM_ARC_LOW_TO_HIGH + ""));
    }


    public static void setBAMArcModeLowToHighThresh(int BAM_ARC_LOW_TO_HIGH) {
        TrackResolutionSettings.BAM_ARC_LOW_TO_HIGH = BAM_ARC_LOW_TO_HIGH;
        PersistentSettings.getInstance().setProperty("BAM_ARC_LOW_TO_HIGH", BAM_ARC_LOW_TO_HIGH + "");
        try {
            PersistentSettings.getInstance().store();
        } catch (IOException ex) {
            Logger.getLogger(TrackResolutionSettings.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void setBAMDefaultModeLowToHighThresh(int BAM_DEFAULT_LOW_TO_HIGH) {
        TrackResolutionSettings.BAM_DEFAULT_LOW_TO_HIGH = BAM_DEFAULT_LOW_TO_HIGH;
        PersistentSettings.getInstance().setProperty("BAM_DEFAULT_LOW_TO_HIGH", BAM_DEFAULT_LOW_TO_HIGH + "");
        try {
            PersistentSettings.getInstance().store();
        } catch (IOException ex) {
            Logger.getLogger(TrackResolutionSettings.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void setIntervalLowToHighThresh(int INTERVAL_LOW_TO_HIGH) {
        TrackResolutionSettings.INTERVAL_LOW_TO_HIGH = INTERVAL_LOW_TO_HIGH;
        PersistentSettings.getInstance().setProperty("INTERVAL_LOW_TO_HIGH", INTERVAL_LOW_TO_HIGH + "");
        try {
            PersistentSettings.getInstance().store();
        } catch (IOException ex) {
            Logger.getLogger(TrackResolutionSettings.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void setSequenceLowToHighThresh(int SEQUENCE_LOW_TO_HIGH) {
        TrackResolutionSettings.SEQUENCE_LOW_TO_HIGH = SEQUENCE_LOW_TO_HIGH;
        PersistentSettings.getInstance().setProperty("SEQUENCE_LOW_TO_HIGH", SEQUENCE_LOW_TO_HIGH + "");
        try {
            PersistentSettings.getInstance().store();
        } catch (IOException ex) {
            Logger.getLogger(TrackResolutionSettings.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


}
