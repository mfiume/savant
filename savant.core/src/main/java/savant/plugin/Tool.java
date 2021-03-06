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
package savant.plugin;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URI;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sf.samtools.SAMSequenceDictionary;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import savant.api.adapter.BAMDataSourceAdapter;
import savant.api.adapter.RangeAdapter;
import savant.api.util.DialogUtils;
import savant.api.util.Listener;
import savant.api.util.TrackUtils;
import savant.controller.FrameController;
import savant.file.FileType;
import savant.format.SavantFileFormatter;
import savant.format.SavantFileFormatterUtils;
import savant.settings.DirectorySettings;
import savant.util.*;
import savant.util.export.FastaExporter;
import savant.util.export.TrackExporter;
import savant.view.dialog.FormatProgressDialog;


/**
 * 
 * @author tarkvara
 */
public class Tool extends SavantPanelPlugin {
    static final Log LOG = LogFactory.getLog(Tool.class);

    /** Portion of tool execution which is devoted to preparing files. */
    private static final double PREP_PORTION = 0.25;

    /** Portion of tool execution which is devoted to actual execution. */
    private static final double WORK_PORTION = 0.75;

    private String baseCommand;
    private Pattern progressRegex;
    private Pattern errorRegex;
    private JTextArea console;

    List<ToolArgument> arguments = new ArrayList<ToolArgument>();

    private JPanel mainPanel;
    
    // The wait panel
    private JProgressBar progressBar;
    private JLabel progressInfo;
    private JButton cancelButton;
    
    private String workingRef;
    private RangeAdapter workingRange;
    boolean useHomoRefs;
    boolean loadUponCompletion = true;
    private Process toolProc;

    @Override
    public void init(JPanel panel) {
        mainPanel = panel;
        panel.setLayout(new CardLayout());
        
        JPanel settingsPanel = new ToolSettingsPanel(this);
        panel.add(new JScrollPane(settingsPanel), "Settings");
        
        JPanel waitCard = new JPanel();
        waitCard.setLayout(new GridBagLayout());

        // Left side filler.
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 100, 0, 100);
        waitCard.add(new JLabel(getDescriptor().getName()), gbc);
        
        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(240, progressBar.getPreferredSize().height));
        waitCard.add(progressBar, gbc);
        
        progressInfo = new JLabel();
        progressInfo.setAlignmentX(1.0f);
        Font f = progressInfo.getFont();
        f = f.deriveFont(f.getSize() - 2.0f);
        progressInfo.setFont(f);
        waitCard.add(progressInfo, gbc);
        
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (toolProc != null) {
                    Process p = toolProc;
                    toolProc = null;
                    p.destroy();
                }
                showCard("Settings");
            }
        });
        gbc.fill = GridBagConstraints.NONE;
        waitCard.add(cancelButton, gbc);

        // Console output at the bottom.
        console = new JTextArea();
        console.setFont(f);
        console.setLineWrap(false);  
        console.setEditable(false);

        JScrollPane consolePane = new JScrollPane(console);
        consolePane.setPreferredSize(new Dimension(800, 200));
        gbc.weighty = 1.0;
        gbc.insets = new Insets(30, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;
        waitCard.add(consolePane, gbc);

        panel.add(waitCard, "Progress");
    }
    
    /**
     * The tool's arguments are contained in the associated plugin.xml file.
     */
    void parseDescriptor() throws XMLStreamException, FileNotFoundException {
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new FileInputStream(getDescriptor().getFile()));
        do {
            switch (reader.next()) {
                case XMLStreamConstants.START_ELEMENT:
                    String elemName = reader.getLocalName().toLowerCase();
                    if (elemName.equals("tool")) {
                        baseCommand = reader.getElementText();
                    } else if (elemName.equals("arg")) {
                        // There's lots of crud in the XML file; we're just interested in the <arg> elements.
                        arguments.add(new ToolArgument(reader));
                    } else if (elemName.equals("progress")) {
                        progressRegex = Pattern.compile(reader.getElementText());
                    } else if (elemName.equals("error")) {
                        errorRegex = Pattern.compile(reader.getElementText());
                    }
                    break;
                case XMLStreamConstants.END_DOCUMENT:
                    reader.close();
                    reader = null;
                    break;
            }
        } while (reader != null);
    }
    
    /**
     * Displays the command line in the given label.  Similar to, but somewhat prettier than
     * the command line generated by <code>buildCommandLine</code>.
     */
    void displayCommandLine(JLabel l) {
        String command = "<html>";
        command += baseCommand;
        for (ToolArgument a: arguments) {
            if (a.value == null) {
                if (a.required) {
                    command += "<font color=\"red\"> " + a.flag + "</font>";
                }
            } else if (a.enabled) {
                if (a.type == ToolArgument.Type.MULTI) {
                    String[] values = a.value.split(",");
                    for (String val: values) {
                        command += " " + a.flag + " " + val;
                    }
                } else {
                    try {
                        command += " " + a.flag + " " + getStringValue(a);
                    } catch (ParseException px) {
                        // An invalid range specification.
                        command += "<font color=\"red\"> " + a.flag + " " + a.value + "</font>";
                    }
                }
            }
        }
        command += "</html>";
        l.setText(command);
    }

    void checkCommandLine() {
        for (ToolArgument a: arguments) {
            if (a.value == null) {
                if (a.required) {
                    throw new IllegalArgumentException(String.format("Required argument %s (%s) does not have a value.", a.flag, a.name));
                }
            } else if (a.enabled) {
                switch (a.type) {
                    case RANGE:
                        // We assume that a tool will only have a single RANGE argument.
                        try {
                            // This sets workingRef and workingRange, so buildCommandLine can count on those being set.
                            getStringValue(a);
                        } catch (ParseException px) {
                            throw new IllegalArgumentException(String.format("Unable to parse \"%s\" as a valid range.", a.value));
                        }
                        break;
                }
            }
        }
    }

    List<String> buildCommandLine() throws IOException {
        List<String> commandLine = new ArrayList<String>();
        commandLine.addAll(Arrays.asList(baseCommand.split("\\s")));
        
        // If we're launching a .jar, we may want to look for it in the plugins directory.
        if (commandLine.get(0).equals("java")) {
            for (int i = 2; i < commandLine.size(); i++) {
                String arg = commandLine.get(i);
                if (arg.endsWith(".jar")) {
                    // If it's just a .jar name (i.e. no path slashes), assume it's in the plugins directory.
                    if (!arg.contains("/")) {
                        commandLine.set(i, new File(DirectorySettings.getPluginsDirectory(), arg).getAbsolutePath());
                    }
                    break;
                }
            }
        }

        for (ToolArgument a: arguments) {
            if (a.value == null) {
                if (a.required) {
                    throw new IllegalArgumentException(String.format("Required argument %s (%s) does not have a value.", a.flag, a.name));
                }
            } else if (a.enabled) {
                if (a.type == ToolArgument.Type.MULTI) {
                    String[] values = a.value.split(",");
                    for (String val: values) {
                        commandLine.add(a.flag);
                        commandLine.add(val);
                    }
                } else {
                    commandLine.add(a.flag);
                    try {
                        switch (a.type) {
                            case BAM_INPUT_FILE:
                                commandLine.add(getLocalFile(a.value, true).getAbsolutePath());
                                break;
                            case FASTA_INPUT_FILE:
                                commandLine.add(getLocalFile(a.value, false).getAbsolutePath());
                                break;
                            default:
                                commandLine.add(getStringValue(a));
                                break;
                        }
                    } catch (ParseException ignored) {
                        // Shouldn't happen because we've already successfully passed checkCommandLine.
                    }
                }
            }
        }
        return commandLine;
    }

        
    /**
     * Interpret this argument's value in a form suitable for appearing on a command line.
     */
    public String getStringValue(ToolArgument a) throws ParseException {
        switch (a.type) {
            case BAM_INPUT_FILE:
                return a.value;
            case RANGE:
                parseWorkingRange(a.value);
                if (workingRef != null) {
                    String ref = useHomoRefs ? MiscUtils.homogenizeSequence(workingRef) : workingRef;
                    if (workingRange != null) {
                        return String.format("%s:%d-%d", ref, workingRange.getFrom(), workingRange.getTo());
                    }
                    return ref;
                } 
                break;
        }
        return a.value;
    }

    private void parseWorkingRange(String val) throws ParseException {
        if (val == null || val.length() == 0) {
            // Empty string means no range restriction.
            workingRef = null;
            workingRange = null;
        } else {
            int colonPos = val.indexOf(':');
            if (colonPos > 0) {
                Bookmark b = new Bookmark(val);
                workingRef = b.getReference();
                workingRange = b.getRange();
            } else {
                // Just a chromosome name.
                workingRef = val;
                workingRange = null;
            }
        }
    }

    /**
     * Get the local file which contains the data for the given argument.
     *
     * @param t URI of the track which is providing the data, or a local file
     * @param loc string specifying ref and range to be processed
     * @param canUseDirectly for bam files, Savant uses them natively, so we may be able to use a local file directly
     */
    private File getLocalFile(String fileOrURI, boolean canUseDirectly) throws IOException {
        
        URI uri = NetworkUtils.getURIFromPath(fileOrURI);

        // If the data source is a local bam file, we can just use it.
        if (canUseDirectly) {
            if (uri.getScheme().equals("file")) {
                return new File(uri);
            }
        }
        
        // Track is remote.  We'll need to download it.
        StringBuilder source = new StringBuilder(uri.toString());
        int savantExt = source.lastIndexOf(".savant");
        if (savantExt > 0) {
            source.setLength(savantExt);
        }
        
        // File may represent only a partial track.  We may need to fetch it afresh,
        // or it may already be in our cache.
        if (RemoteFileCache.findCacheEntry(source.toString()) == null) {
            // Couldn't find exported file for full genome.  Perhaps just for the current chromosome?
            if (workingRef != null) {
                int lastDot = source.lastIndexOf(".");
                source.insert(lastDot, "-" + workingRef);
                if (RemoteFileCache.findCacheEntry(source.toString()) == null) {
                    if (workingRange != null) {
                        // No existing chromosome file, so just request the subrange of interest.
                        lastDot = source.lastIndexOf(".");
                        source.insert(lastDot, String.format(":%d-%d", workingRange.getFrom(), workingRange.getTo()));
                    }
                }
            }
        }

        return RemoteFileCache.getCacheFile(uri.toURL(), source.toString(), 0, 0);
    }

    void execute() {
        // Before we do anything else, make sure all the required parameters have been specified.
        try {
            checkCommandLine();
            showCard("Progress");
            new ToolWorker().execute();
        } catch (IllegalArgumentException x) {
            DialogUtils.displayMessage(x.getMessage());
        }
    }
    
    private void showCard(String card) {
        ((CardLayout)mainPanel.getLayout()).show(mainPanel, card);
    }

    private class ToolWorker extends BackgroundWorker<File> {
        List<ToolArgument> missingFiles = new ArrayList<ToolArgument>();
        int inputIndex;
        private String errorMessage;
        private File destFile;

        @Override
        protected void showProgress(double fraction) {
            progressBar.setIndeterminate(fraction < 0.0);
            progressBar.setValue((int)(fraction * 100.0));
        }

        @Override
        protected File doInBackground() throws Exception {
            showProgress(0.0);
            cancelButton.setText("Cancel");
            console.setText("");

            progressInfo.setText("Preparing input files\u2026");
            prepareInputs();

            progressInfo.setText("Running tool\u2026");
            runTool();

            if (loadUponCompletion) {
                String destPath = destFile.getAbsolutePath();
                FileType guess = SavantFileFormatterUtils.guessFileTypeFromPath(destPath);
                if (guess == FileType.INTERVAL_BAM) {
                    // BAM files we open directly, without having to format.
                    FrameController.getInstance().addTrackFromPath(destPath, null, null);
                } else {
                    File formattedFile = SavantFileFormatterUtils.getFormattedFile(destPath, guess);

                    SavantFileFormatter sff = SavantFileFormatter.getFormatter(destFile, formattedFile, guess);
                    if (sff != null) {
                        FormatProgressDialog fpd = new FormatProgressDialog(DialogUtils.getMainWindow(), sff, true);
                        fpd.setLocationRelativeTo(DialogUtils.getMainWindow());
                        fpd.setVisible(true);
                    }
                }
            }

            progressInfo.setText("");
            return destFile;
        }

        @Override
        protected void showSuccess(File result) {
            cancelButton.setText("Done");
        }

        /**
         * The first stage of the process may involve copying the track data into
         * local files so that the tool can operate on it.
         * 
         * Once the files have been set up, we have an extra step of bullshit, which
         * involves generating fake .fai and .dict files for our sequence.
         */
        private void prepareInputs() throws IOException, InterruptedException {
            ToolArgument bamArg = null;
            for (ToolArgument a: arguments) {
                if (a.enabled) {
                    switch (a.type) {
                        case BAM_INPUT_FILE:
                            // Remote URLs will need to be downloaded.
                            if (!NetworkUtils.getURIFromPath(a.value).getScheme().equals("file")) {
                                missingFiles.add(a);
                            }
                            bamArg = a;
                            break;
                        case FASTA_INPUT_FILE:
                            // Remote URLs and formatted FASTA files will need to be downloaded.
                            // Actually, all Fasta files will need to be downloaded, since GATK is so finicky about sequence dictionaries.
                            missingFiles.add(a);
                            break;
                        case OUTPUT_FILE:
                            destFile = new File(a.value);
                            break;
                    }
                }
            }

            inputIndex = 0;
            
            FastaExporter fastaExp = null;
            for (ToolArgument a: missingFiles) {
                File f = getLocalFile(a.value, false);
                if (!f.exists()) {
                    LOG.info(f + " not found, exporting.");
                    TrackExporter exp = TrackExporter.getExporter(a.value, f);
                    exp.addListener(new Listener<DownloadEvent>() {
                        @Override
                        public void handleEvent(DownloadEvent event) {
                            switch (event.getType()) {
                                case PROGRESS:
                                    double prog = event.getProgress();
                                    if (prog >= 0.0) {
                                        showProgress(PREP_PORTION * (inputIndex + event.getProgress()) / missingFiles.size());
                                    } else {
                                        showProgress(-1.0);
                                    }
                                    break;
                                case COMPLETED:
                                    try {
                                        RemoteFileCache.updateCacheEntry(event.getFile());
                                    } catch (Exception x) {
                                        LOG.error("Unable to update cache entry for " + event.getFile(), x);
                                    }
                                    break;
                            }
                        }
                    });
                    exp.export(workingRef, workingRange);
                    if (exp instanceof FastaExporter) {
                        fastaExp = (FastaExporter)exp;
                    }
                }
                showProgress(++inputIndex * PREP_PORTION / missingFiles.size());
            }
            
            // If we did a fasta export, we have to create the index and dictionary based, not on the contents of the
            // FASTA file, but on the sequence dictionary from the header of the BAM file.
            if (bamArg != null && fastaExp != null) {
                SAMSequenceDictionary samDict = ((BAMDataSourceAdapter)TrackUtils.getTrackDataSource(bamArg.value)).getHeader().getSequenceDictionary();
                fastaExp.createFakeIndex(samDict, workingRef == null);
                fastaExp.createFakeSequenceDictionary(samDict);
            }
        }

        private void runTool() throws IOException {
            List<String> commandLine = buildCommandLine();
            ProcessBuilder builder = new ProcessBuilder(commandLine);
            builder.redirectErrorStream(true);
            toolProc = builder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(toolProc.getInputStream()));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    line += "\n";
                    console.append(line);
                    if (errorRegex != null) {
                        Matcher m = errorRegex.matcher(line);
                        if (m.find()) {
                            errorMessage = m.group(1);
                            LOG.info("Retrieved error message \"" + errorMessage + "\".");
                            continue;
                        }
                    }

                    if (progressRegex != null) {
                        Matcher m = progressRegex.matcher(line);
                        if (m.find()) {
                            String progress = m.group(1);
                            try {
                                showProgress(PREP_PORTION + Double.valueOf(progress) * WORK_PORTION * 0.01);
                            } catch (NumberFormatException ignored) {
                                // So it's not a valid number.  Unfortunate, but no disaster.
                                LOG.info("Unable to interpret \"" + progress + "\" as a percentage.");
                            }
                        }
                    }
                }
                toolProc = null;
            } catch (IOException x) {
                // If user cancelled the process, we'll get a harmless IOException trying to read its output.
                if (toolProc != null) {
                    throw x;
                }
            }

            // We're done.  We may have picked up an error message along the way.
            if (errorMessage != null) {
                throw new IOException(errorMessage);
            }
        }
    }
}
