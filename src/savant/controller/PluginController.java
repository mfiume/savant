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
package savant.controller;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPanel;

import com.jidesoft.docking.DockableFrame;
import com.jidesoft.docking.DockingManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.java.plugin.JpfException;
import org.java.plugin.ObjectFactory;
import org.java.plugin.Plugin;
import org.java.plugin.PluginManager;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;
import org.java.plugin.registry.PluginDescriptor;
import org.java.plugin.standard.StandardPluginLocation;

import savant.experimental.PluginTool;
import savant.plugin.GUIPlugin;
import savant.plugin.PluginAdapter;
import savant.util.MiscUtils;
import savant.view.swing.DockableFrameFactory;
import savant.view.swing.Savant;
import savant.view.tools.ToolsModule;

/**
 *
 * @author mfiume
 */
public class PluginController {
    private static final Log LOG = LogFactory.getLog(PluginController.class);
    private final String FILENAME = ".uninstall_plugins";
    private Set<String> pluginsToUnInstall = new HashSet<String>();

    /** VARIABLES **/

    private static PluginController instance;
    private static PluginManager pluginManager;

    //private Map<SuperPluginDescriptor,Plugin> pluginMap = new HashMap<SuperPluginDescriptor,Plugin>();

    private Map<String,PluginDescriptor> pluginIdToDescriptorMap = new HashMap<String,PluginDescriptor>();
    private Map<String,Extension> pluginIdToExtensionMap = new HashMap<String,Extension>();
    private Map<String,Plugin> pluginIdToPluginMap = new HashMap<String,Plugin>();
    //private Map<String,String> pluginIdToPathMap = new HashMap<String,String>();

    private ExtensionPoint coreExtPt;
    private final String PLUGINS_DIR = "plugins";

    /** SINGLETON **/

    public static synchronized PluginController getInstance() {
        if (instance == null) {
            instance = new PluginController();
        }
        return instance;
    }
    private File uninstallFile;

    /** CONSTRUCTOR **/

    public PluginController() {
        try {

            pluginManager = ObjectFactory.newInstance().createManager();
            uninstallFile = new File(FILENAME);
            if (uninstallFile.exists()) {
                uninstallPlugins(uninstallFile);
            }
            loadCorePlugin();
            loadPlugins(new File(PLUGINS_DIR));
        } catch (Exception ex) {
            LOG.error("Error loading plugins.", ex);
        }
    }

    /** PLUGIN LOADING **/

    private void loadPlugins(File pluginsDir) throws MalformedURLException, JpfException, InstantiationException, IllegalAccessException {
        File[] plugins = pluginsDir.listFiles(new FilenameFilter() {

            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".jar") || name.toLowerCase().endsWith(".zip") || name.toLowerCase().endsWith(".gz");
            }
        });
        for (int i = 0; i < plugins.length; i++) {
            loadPlugin(plugins[i]);
        }
    }

    private void loadPlugin(File pluginLocation) throws JpfException, MalformedURLException, InstantiationException, IllegalAccessException {
        PluginManager.PluginLocation[] locs = new PluginManager.PluginLocation[1];
        locs[0] = StandardPluginLocation.create(pluginLocation);
        if (locs[0] != null) {
            pluginManager.publishPlugins(locs);
            activateNewPlugins();
        } else {
            LOG.warn("Unable to load plugin: " + pluginLocation);
        }
    }

    /** PLUGIN ACTIVATION **/

    private void deactivatePlugin(String id) {
        pluginManager.deactivatePlugin(id);
    }


    public void queuePluginForUnInstallation(String pluginid) {
        pluginsToUnInstall.add(pluginid);
        FileWriter fstream = null;
        try {
            if (!uninstallFile.exists()) {
                uninstallFile.createNewFile();
            }
            fstream = new FileWriter(uninstallFile, true);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(this.getPluginPath(pluginid) + "\n");
            out.close();
        } catch (IOException ex) {
            LOG.error("Error uninstalling plugin: " + uninstallFile, ex);
        } finally {
            try {
                fstream.close();
            } catch (IOException ignored) {
            }
        }
    }

    private boolean activatePlugin(PluginDescriptor d, Extension e) {

            try {
                pluginManager.activatePlugin(d.getId());
                ClassLoader classLoader = pluginManager.getPluginClassLoader(d);

               /* if (d.getId().equals("savant.pivot")) {
                    StandardPathResolver spr = new StandardPathResolver();
                    Library l = d.getLibrary("excel");
                    System.out.println(l.getPath());
                }
                * 
                */
                
                Plugin plugininstance = (Plugin) (
                        classLoader.loadClass(
                            e.getParameter("class").valueAsString()
                        )).newInstance();

                // init the plugin based on its type
                if (plugininstance instanceof GUIPlugin) {
                    initGUIPlugin(plugininstance);
                } else if (plugininstance instanceof PluginTool) {
                    initPluginTool(plugininstance);
                }

                addToPluginMaps(d.getUniqueId(), d, e, plugininstance);

                return true;

            } catch (Exception ex) {}

            return false;
        }

    /** ACTIVATE NEW **/
        private void activateNewPlugins() {
        Iterator it = coreExtPt.getConnectedExtensions().iterator();
        while (it.hasNext()) {
            Extension ext = (Extension) it.next();
            PluginDescriptor descr = ext.getDeclaringPluginDescriptor();
            this.activatePlugin(descr, ext);
        }
    }

    /** ADD AND REMOVE PLUGINS **/

    public void installPlugin(String path) throws JpfException, MalformedURLException, InstantiationException, IllegalAccessException {
        loadPlugin(new File(path));
    }

    private void addToPluginMaps(String id, PluginDescriptor d, Extension e, Plugin p) {
        this.pluginIdToDescriptorMap.put(id, d);
        this.pluginIdToExtensionMap.put(id, e);
        this.pluginIdToPluginMap.put(id, p);
    }

    private void removeFromPluginMaps(String id) {
        this.pluginIdToDescriptorMap.remove(id);
        this.pluginIdToExtensionMap.remove(id);
        this.pluginIdToPluginMap.remove(id);
    }

    public String getPluginPath(String pluginId) {
        String rawLocation = this.pluginIdToDescriptorMap.get(pluginId).getLocation().getPath();
        rawLocation = rawLocation.replaceAll("!/plugin.xml", "");
        rawLocation = rawLocation.replaceAll("file:/", "");
        return rawLocation;
    }

    /** CORE PLUGIN **/

    private void loadCorePlugin() throws MalformedURLException, JpfException {
        PluginManager.PluginLocation[] locs = new PluginManager.PluginLocation[1];
        java.net.URL[] mans = new java.net.URL[1];

        File location = new File(PLUGINS_DIR + System.getProperty("file.separator") + "SavantCore.jar");

        if (!location.exists()) {
            LOG.error("Loading of core plugin failed.");
            return;
        }

        locs[0] = StandardPluginLocation.create(location);

        pluginManager.publishPlugins(locs);

        PluginDescriptor core = pluginManager.getRegistry().getPluginDescriptor("savant.core");
        ExtensionPoint corePt = (ExtensionPoint) (core.getExtensionPoints().toArray()[0]);
        coreExtPt = pluginManager.getRegistry().getExtensionPoint(core.getId(), corePt.getId());
    }


    /** GETTERS **/
    public List<PluginDescriptor> getPluginDescriptors() {
        return new ArrayList<PluginDescriptor>(this.pluginIdToDescriptorMap.values());
    }

    public List<Extension> getExtensions() {
        return new ArrayList<Extension>(this.pluginIdToExtensionMap.values());
    }

    public List<Plugin> getPlugins() {
        return new ArrayList<Plugin>(this.pluginIdToPluginMap.values());
    }

    /** INIT PLUGIN TYPES **/

    private void initPluginTool(Object plugininstance) {
        PluginTool p = (PluginTool) plugininstance;
        p.init(new PluginAdapter());
        ToolsModule.addTool(p);
    }

    private void initGUIPlugin(Object plugininstance) {
        GUIPlugin plugin = (GUIPlugin) plugininstance;
        final DockableFrame f = DockableFrameFactory.createGUIPluginFrame(plugin.getTitle());
        JPanel p = (JPanel) f.getContentPane();
        p.setLayout(new BorderLayout());
        JPanel canvas = new JPanel();
        p.add(canvas, BorderLayout.CENTER);
        canvas.setLayout(new BorderLayout());
        plugin.init(canvas, new PluginAdapter());
        Savant.getInstance().getAuxDockingManager().addFrame(f);
        boolean isIntiallyVisible = false;
        MiscUtils.setFrameVisibility(f.getTitle(), isIntiallyVisible, Savant.getInstance().getAuxDockingManager());
        final JCheckBoxMenuItem cb = new JCheckBoxMenuItem(plugin.getTitle());
        cb.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DockingManager m = Savant.getInstance().getAuxDockingManager();
                String frameKey = f.getTitle();
                boolean isVisible = m.getFrame(frameKey).isHidden();
                MiscUtils.setFrameVisibility(frameKey, isVisible, m);
                cb.setSelected(isVisible);
            }
        });
        cb.setSelected(!Savant.getInstance().getAuxDockingManager().getFrame(f.getTitle()).isHidden());
        // move this to a menu controller!
        Savant.getInstance().addPluginToMenu(cb);
    }

    private void uninstallPlugins(File f) {
        BufferedReader br = null;
        String line = "";
        try {
            br = new BufferedReader(new FileReader(f));
            
            while ((line = br.readLine()) != null) {
                LOG.info("Uninstalling " + line);
                new File(line).delete();
            }
        } catch (IOException ex) {
            LOG.error("Problem uninstalling " + line, ex);
        } finally {
            try {
                br.close();
            } catch (IOException ex) {
            }
        }
        f.delete();
    }

    public boolean isPluginQueuedForDeletion(String id) {
        return this.pluginsToUnInstall.contains(id);
    }

    public String getPluginName(String id) {
        Plugin p = this.pluginIdToPluginMap.get(id);
        if (p instanceof GUIPlugin) {
            GUIPlugin pp = (GUIPlugin) p;
            return pp.getTitle();
        } else if (p instanceof PluginTool) {
            PluginTool pp = (PluginTool) p;
            return pp.getToolInformation().getName();
        }
        return id;
    }
}
