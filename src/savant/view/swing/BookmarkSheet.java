/*
 *    Copyright 2009-2010 University of Toronto
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

import savant.controller.BookmarkController;
import savant.controller.RangeController;
import savant.controller.event.bookmark.BookmarksChangedEvent;
import savant.controller.event.bookmark.BookmarksChangedListener;
//import savant.controller.event.range.RangeChangedEvent;
//import savant.controller.event.range.RangeChangedListener;
import savant.util.Bookmark;
import savant.util.Range;
import savant.view.swing.model.BookmarksTableModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Arrays;

import savant.controller.event.range.RangeChangedListener;
import static java.awt.FileDialog.SAVE;

/**
 *
 * @author mfiume
 */
public class BookmarkSheet implements BookmarksChangedListener /*, RangeChangedListener*/ {

    private JTable table;

    static boolean isRecording = false;
    static JButton recordButton;
    static JButton addButton;
    static boolean confirmDelete = true;

    public BookmarkSheet(Savant parent, Container c) {

        JPanel subpanel = new JPanel();

        // set the layout of the data sheet
        c.setLayout(new BorderLayout());
        subpanel.setLayout(new BoxLayout(subpanel, BoxLayout.Y_AXIS));

        /**
         * Create a toolbar. 
         */
        JPanel toolbar = new JPanel();
        toolbar.setLayout(new BoxLayout(toolbar, BoxLayout.Y_AXIS));
        toolbar.setMinimumSize(new Dimension(22,22));
        toolbar.setPreferredSize(new Dimension(22,22));
        toolbar.setMaximumSize(new Dimension(999999,22));
        c.add(toolbar, BorderLayout.NORTH);

        //bottom toolbar for load and save
        JPanel toolbarBottom = new JPanel();
        toolbarBottom.setLayout(new BoxLayout(toolbarBottom, BoxLayout.Y_AXIS));
        toolbarBottom.setMinimumSize(new Dimension(22,22));
        toolbarBottom.setPreferredSize(new Dimension(22,22));
        toolbarBottom.setMaximumSize(new Dimension(999999,22));
        c.add(toolbarBottom, BorderLayout.SOUTH);

        JPanel topToolRow = new JPanel();
        topToolRow.setLayout(new BoxLayout(topToolRow, BoxLayout.X_AXIS));
        topToolRow.setMinimumSize(new Dimension(22,22));
        topToolRow.setPreferredSize(new Dimension(22,22));
        topToolRow.setMaximumSize(new Dimension(999999,22));
        toolbar.add(topToolRow);

        JPanel bottomToolRow = new JPanel();
        bottomToolRow.setLayout(new BoxLayout(bottomToolRow, BoxLayout.X_AXIS));
        bottomToolRow.setMinimumSize(new Dimension(22,22));
        bottomToolRow.setPreferredSize(new Dimension(22,22));
        bottomToolRow.setMaximumSize(new Dimension(999999,22));
        toolbarBottom.add(bottomToolRow);
        
        Dimension buttonSize = new Dimension(65,22);

        JButton goButton = new JButton("Go");
        goButton.setMinimumSize(buttonSize);
        goButton.setPreferredSize(buttonSize);
        goButton.setMaximumSize(buttonSize);
        goButton.setToolTipText("Go to selected bookmark");
        goButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedRow = table.getSelectedRow();
                if (selectedRow > -1) {
                    RangeController rc = RangeController.getInstance();
                    BookmarksTableModel tableModel = (BookmarksTableModel) table.getModel();
                    Bookmark bookmark = tableModel.getData().get(selectedRow);
                    rc.setRange(bookmark.getReference(),bookmark.getRange());
                }
            }

        });
        topToolRow.add(goButton);

        topToolRow.add(Box.createGlue());

        addButton = new JButton("Add");
        addButton.setMinimumSize(buttonSize);
        addButton.setPreferredSize(buttonSize);
        addButton.setMaximumSize(buttonSize);
        addButton.setToolTipText("Add bookmark for current range");
        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                BookmarkController fc = BookmarkController.getInstance();
                fc.addCurrentRangeToBookmarks();
            }
        });
        topToolRow.add(addButton);

        JButton deleteButton = new JButton("Delete");
        deleteButton.setMinimumSize(buttonSize);
        deleteButton.setPreferredSize(buttonSize);
        deleteButton.setMaximumSize(buttonSize);
        deleteButton.setToolTipText("Delete selected bookmarks");
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                BookmarkController fc = BookmarkController.getInstance();
                int[] selectedRows = table.getSelectedRows();
                Arrays.sort(selectedRows);
                boolean delete = false;

                if(selectedRows.length > 0 && confirmDelete){
                    Object[] options = {"Yes",
                                        "No",
                                        "Yes, don't ask again"};
                    int confirmDeleteDialog = JOptionPane.showOptionDialog(Savant.getInstance(),
                        "Are you sure you want to delete " + selectedRows.length + " item(s)?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]);
                    
                    if(confirmDeleteDialog==0){
                        delete = true;
                    } else if (confirmDeleteDialog==2){
                        delete = true;
                        confirmDelete = false;
                    }
                } else if(selectedRows.length > 0 && !confirmDelete){
                    delete = true;
                }

                if(delete){
                    for(int i = selectedRows.length -1; i >= 0; i--){
                        fc.removeBookmark(selectedRows[i]);
                    }
                }
            }
        });
        topToolRow.add(deleteButton);

        bottomToolRow.add(Box.createGlue());

        JButton loadButton = new JButton("Load");
        loadButton.setMinimumSize(buttonSize);
        loadButton.setPreferredSize(buttonSize);
        loadButton.setMaximumSize(buttonSize);
        loadButton.setToolTipText("Load bookmarks from file");
        loadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadBookmarks(table);
            }
        });
        bottomToolRow.add(loadButton);

        JButton saveButton = new JButton("Save");
        saveButton.setMinimumSize(buttonSize);
        saveButton.setPreferredSize(buttonSize);
        saveButton.setMaximumSize(buttonSize);
        saveButton.setToolTipText("Save bookmarks to file");
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveBookmarks(table);
            }
        });
        bottomToolRow.add(saveButton);


        recordButton = new JButton("Record");
        recordButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                BookmarkSheet.isRecording = !BookmarkSheet.isRecording;
                if (BookmarkSheet.isRecording) {
                    recordButton.setText("Stop Recording");
                    addButton.setEnabled(false);
                } else {
                    recordButton.setText("Record");
                    addButton.setEnabled(true);
                }
            }
        });
        toolbar.add(recordButton);

        // create a table (the most important component)
        table = new JTable(new BookmarksTableModel());
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        //table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);

        /*
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                BookmarkController fc = BookmarkController.getInstance();
                int index = e.getFirstIndex();
                RangeController rc = RangeController.getInstance();
                rc.setRange(fc.getBookmark(index).getRange());
            }

        });
         * 
         */


        // add the table and its header to the subpanel
        c.add(table.getTableHeader());

        subpanel.add(table);

        JScrollPane sp = new JScrollPane(table,
        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        c.add(sp);

        // add glue to fill the remaining space
        subpanel.add(Box.createGlue());

        //RangeController rc = RangeController.getInstance();
        //rc.addRangeChangedListener(this);

        // initContextualMenu();
    }

    public void bookmarksChangeReceived(BookmarksChangedEvent event) {
        this.refreshData(event.favorites());
    }

    private void refreshData(List<Bookmark> favorites) {
        Savant.log("Setting data");
        ((BookmarksTableModel) table.getModel()).setData(favorites);
        Savant.log("Done data");
        ((BookmarksTableModel) table.getModel()).fireTableDataChanged();
    }

    /*
    public void rangeChangeReceived(RangeChangedEvent event) {
        if (isRecording) {
            BookmarkController bc = BookmarkController.getInstance();
            bc.addCurrentRangeToBookmarks();
        }
    }
     */

    private static void loadBookmarks(JTable table) {
        BookmarksTableModel btm = (BookmarksTableModel) table.getModel();
        List<Bookmark> bookmarks = btm.getData();

        if (bookmarks.size() > 0) {
            String message = "Clear existing bookmarks?";
            String title = "Clear Bookmarks";
                // display the JOptionPane showConfirmDialog
            int reply = JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION);
            if (reply == JOptionPane.YES_OPTION)
                bookmarks.clear();
        }

        JFrame jf = new JFrame();
        String selectedFileName;
        if (Savant.mac) {
            FileDialog fd = new FileDialog(jf, "Load Bookmarks", FileDialog.LOAD);
            fd.setVisible(true);
            jf.setAlwaysOnTop(true);
            // get the path (null if none selected)
            selectedFileName = fd.getFile();
            if (selectedFileName != null) {
                selectedFileName = fd.getDirectory() + selectedFileName;
            }
        }
        else {
            JFileChooser fd = new JFileChooser();
            fd.setDialogTitle("Load Bookmards");
            fd.setDialogType(JFileChooser.OPEN_DIALOG);
            int result = fd.showOpenDialog(jf);
            if (result == JFileChooser.CANCEL_OPTION || result == JFileChooser.ERROR_OPTION ) return;
            selectedFileName = fd.getSelectedFile().getPath();
        }

        Savant.log("load 0");

        // set the genome
        if (selectedFileName != null) {
            try {
                loadBookmarks(selectedFileName, bookmarks);
                btm.fireTableDataChanged();
            } catch (IOException ex) {
                String message = "Load unsuccessful";
                String title = "Uh oh...";
                // display the JOptionPane showConfirmDialog
                JOptionPane.showConfirmDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
            }
        }
        //System.out.println(BookmarkController.getInstance().getBookmarks());
    }

    private static void saveBookmarks(JTable table) {
        BookmarksTableModel btm = (BookmarksTableModel) table.getModel();
        List<Bookmark> bookmarks = btm.getData();

        JFrame jf = new JFrame();
        FileDialog fd = new FileDialog(jf, "Save Bookmarks", SAVE);
        fd.setVisible(true);
        jf.setAlwaysOnTop(true);

        // get the path (null if none selected)
        String selectedFileName = fd.getFile();

        // set the genome
        if (selectedFileName != null) {
            try {
                selectedFileName = fd.getDirectory() + selectedFileName;
                saveBookmarks(selectedFileName, bookmarks);
            } catch (IOException ex) {
                String message = "Save unsuccessful";
                String title = "Uh oh...";
                // display the JOptionPane showConfirmDialog
                JOptionPane.showConfirmDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void saveBookmarks(String filename, List<Bookmark> bookmarks) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(filename));

        for (Bookmark bm : bookmarks) {
            bw.write(bm.getReference() + "\t" + bm.getRange().getFrom() + "\t" + bm.getRange().getTo() + "\t" + bm.getAnnotation() + "\n");
        }

        bw.close();
    }

    private static void loadBookmarks(String filename, List<Bookmark> bookmarks) throws FileNotFoundException, IOException {

        Savant.log("load 1");

        BufferedReader br = new BufferedReader(new FileReader(filename));

        BookmarkController bmc = BookmarkController.getInstance();

        String line = "";

        while ((line = br.readLine()) != null) {
            Bookmark bm = parseBookmark(line);
            bookmarks.add(bm);
            bmc.addBookmarkSilent(bm);
        }

        br.close();
    }

    private static Bookmark parseBookmark(String line) {

        Savant.log("load 2");

        StringTokenizer st = new StringTokenizer(line,"\t");

        String ref = st.nextToken();
        int from = Integer.parseInt(st.nextToken());
        int to = Integer.parseInt(st.nextToken());
        String annotation = "";

        while (st.hasMoreElements()) {
            annotation += st.nextToken() + " ";
        }
        annotation.trim();

        return new Bookmark(ref, new Range(from,to), annotation);
    }
}
