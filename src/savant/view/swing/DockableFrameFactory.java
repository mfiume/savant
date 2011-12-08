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

package savant.view.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
import javax.swing.JPanel;

import com.jidesoft.docking.DockContext;
import com.jidesoft.docking.DockableFrame;

import savant.api.data.DataFormat;
import savant.controller.FrameController;


/**
 * Factory for creating dockable frames.  This come in three flavours: one for bookmarks,
 * one for tracks (including genome tracks), and one for GUI plugins.
 */
public class DockableFrameFactory {

    /**
     * Factory method used to create the Bookmarks frame.
     *
     * @param name the frame's title
     * @param mode STATE_HIDDEN, STATE_FLOATING, STATE_AUTOHIDE, or STATE_FRAMEDOCKED
     * @param side DOCK_SIDE_EAST, DOCK_SIDE_WEST, DOCK_SIDE_SOUTH, DOCK_SIDE_NORTH, or DOCK_SIDE_CENTER
     * @return 
     */
    public static DockableFrame createFrame(String name, int mode, int side) {
        DockableFrame frame = new DockableFrame(name, null);
        frame.setSlidingAutohide(true);
        frame.setInitMode(mode);
        frame.setInitSide(side);
        frame.add(new JPanel());
        frame.setPreferredSize(new Dimension(400, 400));
        frame.setAutohideWidth(400);
        frame.setAutohideHeight(400);
        return frame;
    }

    public static DockableFrame createGUIPluginFrame(String name) {
        DockableFrame f = createFrame(name, DockContext.STATE_AUTOHIDE, DockContext.DOCK_SIDE_SOUTH);
        f.setAvailableButtons(DockableFrame.BUTTON_AUTOHIDE | DockableFrame.BUTTON_FLOATING | DockableFrame.BUTTON_MAXIMIZE );
        return f;
    }

    public static Frame createTrackFrame(DataFormat df) {

        final Frame frame = new Frame(df);
        
        frame.setAvailableButtons(DockableFrame.BUTTON_AUTOHIDE | DockableFrame.BUTTON_MAXIMIZE | DockableFrame.BUTTON_CLOSE );
        
        if (df == DataFormat.VARIANT) {
//            frame.setSlidingAutohide(true);
            frame.setInitMode(DockContext.STATE_AUTOHIDE);
            frame.setInitSide(DockContext.DOCK_SIDE_EAST);
//            frame.setPreferredSize(new Dimension(400, 400));
//            frame.setAutohideWidth(400);
//            frame.setAutohideHeight(400);
        } else {
            frame.setSlidingAutohide(false);
            frame.setInitMode(DockContext.STATE_FRAMEDOCKED);
            frame.setInitSide(DockContext.DOCK_SIDE_NORTH);
        }
        //frame.setShowTitleBar(false);
        //frame.setShowGripper(true);
        //frame.setPreferredAutohideSide(DockContext.DOCK_SIDE_SOUTH);
        
        frame.add(new JPanel());

        frame.setCloseAction(new Action() {
            private boolean isEnabled = true;
            private Map<String,Object> map = new HashMap<String,Object>();

            @Override
            public void actionPerformed(ActionEvent e) {
                FrameController.getInstance().closeFrame(frame, true);
            }

            @Override
            public Object getValue(String key) {
                if (key.equals(Action.NAME)) { return "Close"; }
                else { return map.get(key); }
            }

            @Override
            public void putValue(String key, Object value) {
                map.put(key, value);
            }

            @Override
            public void setEnabled(boolean b) {
                this.isEnabled = b;
            }

            @Override
            public boolean isEnabled() {
                return isEnabled;
            }

            @Override
            public void addPropertyChangeListener(PropertyChangeListener listener) {}

            @Override
            public void removePropertyChangeListener(PropertyChangeListener listener) {}
        });


        // TODO: this seems cyclical. What's going on here?
        JPanel panel = (JPanel)frame.getContentPane();
        panel.setLayout(new BorderLayout());
        panel.add(frame.getFrameLandscape());
        return frame;
    }
}
