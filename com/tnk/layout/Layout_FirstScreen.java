package com.tnk.layout;

/*
 *
 * Main Screen for the TeethCart Alpha
 *  
 * 
 * 
 */

import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import net.miginfocom.swing.MigLayout;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;

import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import com.tnk.util.FileTableModel;
import com.tnk.util.FileTreeCellRenderer;
import com.tnk.util.Utilities;

import boofcv.gui.ListDisplayPanel;
import boofcv.io.UtilIO;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayS16;
import boofcv.struct.image.GrayU8;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.JTextField;

public class Layout_FirstScreen {

	private DefaultMutableTreeNode root;
    private JTree tree;
	private JFrame frame;
	
	/** Main GUI container */
    private JPanel gui;
    
    /* Interface */
    private JLabel label_se;			// SE scroll label; for developer callouts
    private JScrollPane scrollpane_se;
    
    /** Table model for File[]. */
    private FileTableModel fileTableModel;
    private ListSelectionListener listSelectionListener;
    private boolean cellSizesSet = false;
    private int rowIconPadding = 6;
    /** Used to open/edit/print files. */
    private Desktop desktop;
    /** Provides nice icons and names for files. */
    private FileSystemView fileSystemView;
    private JTable viewerTable;
    
    /* File controls. */
    private JButton openFile;
    private JButton printFile;
    private JButton editFile;

    /* File details. */
    private JScrollPane explorerPane;
    private JLabel fileName;
    private JTextField path;
    private JLabel date;
    private JLabel size;
    private JCheckBox readable;
    private JCheckBox writable;
    private JCheckBox executable;
    private JRadioButton isDirectory;
    private JRadioButton isFile;
    
    /* Image Processing */
    /* Thanks BoofCV!   */
    private BufferedImage input;
	private ListDisplayPanel ldp;
	private String selectedPath = "";
    
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Layout_FirstScreen window = new Layout_FirstScreen();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Layout_FirstScreen() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		
		fileSystemView = FileSystemView.getFileSystemView();
        desktop = Desktop.getDesktop();
        
        
		frame = new JFrame();
		frame.setBounds(0, 0, 1366, 1024);		// for my small ass monitor   :'(
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new MigLayout("", "[grow][][grow][grow]", "[grow][][grow]"));
		
		initializeMyViewer();
		initializeMyExplorer();
		
		label_se = new JLabel();
		label_se.setText("Holy crap, I'm a label!");
		
		scrollpane_se = new JScrollPane();
		scrollpane_se.setViewportView(ldp);
		scrollpane_se.setBorder(new BevelBorder(BevelBorder.RAISED, Color.darkGray, null, null, null));
		scrollpane_se.setBackground(Color.GRAY);
		scrollpane_se.setColumnHeaderView(label_se);
		
		// Add the File Explorer tree to the Content Pane
		explorerPane = new JScrollPane();
		frame.getContentPane().add(explorerPane, "cell 0 0,grow");
		
		explorerPane.setViewportView(tree);
		
		JScrollPane sp_right = new JScrollPane();		

		JViewport vp_lbl_ops = new JViewport();
		JLabel lblNewLabel = new JLabel("Operation Window");
		lblNewLabel.setForeground(Color.BLACK);
		lblNewLabel.setBackground(Color.BLACK);
		vp_lbl_ops.add(Utilities.OperationPanel());
		//TODO Fixme!!!
		//Utilities.runLineRadio.setActionMap(Utilities.IP_DetectFeatures(input, GrayF32.class));
	
		sp_right.setViewport(vp_lbl_ops);
		
		
		frame.getContentPane().add(sp_right, "cell 1 1,growx");
				
		JScrollPane scrollpane_center = new JScrollPane();
		scrollpane_center.setViewportView(viewerTable);
		scrollpane_center.setBorder(new BevelBorder(BevelBorder.RAISED, Color.darkGray, null, null, null));
		scrollpane_center.setBackground(Color.GRAY);
		frame.getContentPane().add(scrollpane_center, "cell 1 0,grow");
		
		// This used to be the
		//frame.getContentPane().add(scrollpane_se, "cell 1 1,grow");
		//vp_panel_right.add(scrollpane_right);
		
		// TODO INvestigate this progress bar!
		JProgressBar progressBar = new JProgressBar();
		frame.getContentPane().add(progressBar, "cell 0 2 3 2,growx");
		
		// Add the ViewerTable to the GUI
		//frame.getContentPane().add(viewerTable, "cell 1 0,grow");
	
		
		
	}
	
	private void Proc_SelectedImage(String imagePath) {
		// TODO Auto-generated method stub
		if (imagePath == null) {
			label_se.setText(imagePath);
			imagePath = "C:\\Users\\Tom\\Pictures\\download.jpg";
		}
		label_se.setText(imagePath);
		input = UtilImageIO.loadImage(UtilIO.pathExample(imagePath));
		Utilities.IP_LineDetect(selectedPath, GrayU8.class, GrayS16.class);
		//Utilities.IP_DetectFeatures(input, GrayF32.class);
		//Utilities.IP_FitEllipses(selectedPath);
		//Utilities.IP_EasySURF(selectedPath);
		scrollpane_se.setViewportView(ldp);
	}

	public class CreateChildNodes implements Runnable {

        private DefaultMutableTreeNode root;

        private File fileRoot;

        public CreateChildNodes(File fileRoot, 
                DefaultMutableTreeNode root) {
            this.fileRoot = fileRoot;
            this.root = root;
        }

        @Override
        public void run() {
            createChildren(fileRoot, root);
        }

        private void createChildren(File fileRoot, 
                DefaultMutableTreeNode node) {
            File[] files = fileRoot.listFiles();
            if (files == null) return;

            for (File file : files) {
                DefaultMutableTreeNode childNode = 
                        new DefaultMutableTreeNode(new FileNode(file));
                node.add(childNode);
                if (file.isDirectory()) {
                    createChildren(file, childNode);
                }
            }
        }

    }

    public class FileNode {

        private File file;

        public FileNode(File file) {
            this.file = file;
        }

        @Override
        public String toString() {
            String name = file.getName();
            if (name.equals("")) {
                return file.getAbsolutePath();
            } else {
                return name;
            }
        }
    }
    
    public void initializeMyExplorer() {
    	/**
    	 * Initialize My Explorer
    	 * 
    	 *	| rev 00 |	2018-04-29
    	 *
    	 * Short method to help populate the information of the JTree File Explorer
    	 * 
    	 * Based on a SO answer for a simple drive explorer
    	 * 
    	 * TODO [LIST]  File Explorer  [LIST]
    	 * - Icons?
    	 * - Just show folders
    	 * - Last location browsed?
    	 * 		-> likely will have to dabble with a kind of 'User Preferences' for this
    	 * 
    	 */
    	TreeSelectionListener treeSelectionListener = new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent tse){
                DefaultMutableTreeNode node =
                  (DefaultMutableTreeNode)tse.getPath().getLastPathComponent();
               showChildren(node);
               setFileDetails((File)node.getUserObject());
            }
        };
        
 
    
        fileSystemView = FileSystemView.getFileSystemView();
        tree = Utilities.DriveExplorer(fileSystemView);
        tree.addTreeSelectionListener(treeSelectionListener);
                
    }
                  	
    
    private void initializeMyViewer() {
    	
    	viewerTable = new JTable();
    	viewerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    	viewerTable.setAutoCreateRowSorter(true);
    	viewerTable.setShowVerticalLines(false);
    	listSelectionListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent lse) {
                int row = viewerTable.getSelectionModel().getLeadSelectionIndex();
                if (selectedPath.endsWith(".jpg") ) {
                	//Utilities.runLineRadio.
                	Proc_SelectedImage(selectedPath);
                }
                setFileDetails( ((FileTableModel)viewerTable.getModel()).getFile(row) );
                
            }
        };
        viewerTable.getSelectionModel().addListSelectionListener(listSelectionListener);
    	
        //Dimension d = explorerPane.getPreferredSize();
        //explorerPane.setPreferredSize(new Dimension((int)d.getWidth(), (int)d.getHeight()/2));
      //Details of a file
      		JPanel fileMainDetails = new JPanel(new BorderLayout(4,2));
      		fileMainDetails.setBorder(new EmptyBorder(0,6,0,6));
      		
      		JPanel fileDetailsLabels = new JPanel(new GridLayout(0,1,2,2));
      		fileMainDetails.add(fileDetailsLabels, BorderLayout.WEST);
      		
      		JPanel fileDetailsValues = new JPanel(new GridLayout(0,1,2,2));
      		fileMainDetails.add(fileDetailsValues, BorderLayout.CENTER);
      		
      		frame.getContentPane().add(fileMainDetails, "cell 0 1, grow");
      		
      		fileDetailsLabels.add(new JLabel("File", JLabel.TRAILING));
      		fileName = new JLabel();
      		fileDetailsValues.add(fileName);
      		fileDetailsLabels.add(new JLabel("Path/name", JLabel.TRAILING));
      		path = new JTextField(5);
      		path.setEditable(false);
      		fileDetailsValues.add(path);
      		fileDetailsLabels.add(new JLabel("Last Modified", JLabel.TRAILING));
      		date = new JLabel();
      		fileDetailsValues.add(date);
      		fileDetailsLabels.add(new JLabel("File Size", JLabel.TRAILING));
      		size = new JLabel();
      		fileDetailsValues.add(size);
      		fileDetailsLabels.add(new JLabel("Type", JLabel.TRAILING));
      		// Create and populate the FLAGS portion of the File Browser
    		JPanel flags = new JPanel(new FlowLayout(FlowLayout.LEADING,4,0));
    		isDirectory = new JRadioButton("Directory");
    		flags.add(isDirectory);
    		
    		isFile = new JRadioButton("File");
    		flags.add(isFile);
    		fileDetailsValues.add(flags);
    }

    
    public void showChildren(final DefaultMutableTreeNode node) {
		tree.setEnabled(false);
		// TODO look into this progressBar thing
		// SOUNDS NIFTY!!!
		//progressBar.setVisible(true);
		//progressBar.setIndeterminate(true);
		
		SwingWorker<Void, File> worker = new SwingWorker<Void, File>() {
			@Override
			public Void doInBackground() {
				File file = (File) node.getUserObject();
				if (file.isDirectory()) {
					File[] files = fileSystemView.getFiles(file, true);  //  This is apparently a very important step or a commonly missed caveat!
					if (node.isLeaf()) {
						for (File child : files) {
						if (child.isDirectory()) {
							publish(child);
						}
                        }
                    }
                    setTableData(files);
                }
                return null;
			}
			
			@Override
			protected void process(List<File> chunks) {
				for (File child : chunks) {
					node.add(new DefaultMutableTreeNode(child));
				}
			}
			
			@Override
			protected void done() {
				//progressBar.setIndeterminate(false);
				//progressBar.setVisible(false);
				tree.setEnabled(true);
			}
		};
		worker.execute();
    }
    
    
    /** Update the table on the EDT */
    private void setTableData(final File[] files) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (fileTableModel==null) {
                    fileTableModel = new FileTableModel();
                    viewerTable.setModel(fileTableModel);
                }
                viewerTable.getSelectionModel().removeListSelectionListener(listSelectionListener);
                fileTableModel.setFiles(files);
                viewerTable.getSelectionModel().addListSelectionListener(listSelectionListener);
                if (!cellSizesSet) {
                    Icon icon = fileSystemView.getSystemIcon(files[0]);

                    // size adjustment to better account for icons
                    viewerTable.setRowHeight( icon.getIconHeight()+rowIconPadding );

                    setColumnWidth(0,-1);
                    setColumnWidth(3,60);
                    viewerTable.getColumnModel().getColumn(3).setMaxWidth(120);
                    setColumnWidth(4,-1);
                    setColumnWidth(5,-1);
                    setColumnWidth(6,-1);
                    setColumnWidth(7,-1);
                    setColumnWidth(8,-1);
                    setColumnWidth(9,-1);

                    cellSizesSet = true;
                }
            }
        });
    }
    
    private void setColumnWidth(int column, int width) {
        TableColumn tableColumn = viewerTable.getColumnModel().getColumn(column);
        if (width<0) {
            // use the preferred width of the header..
            JLabel label = new JLabel( (String)tableColumn.getHeaderValue() );
            Dimension preferred = label.getPreferredSize();
            // altered 10->14 as per camickr comment.
            width = (int)preferred.getWidth()+14;
        }
        tableColumn.setPreferredWidth(width);
        tableColumn.setMaxWidth(width);
        tableColumn.setMinWidth(width);
    }
    
    /** Update the File details view with the details of this File. */
    private void setFileDetails(File file) {
        //currentFile = file;
        Icon icon = fileSystemView.getSystemIcon(file);
        fileName.setIcon(icon);
        fileName.setText(fileSystemView.getSystemDisplayName(file));
        
        selectedPath = file.getPath();
        
        path.setText(file.getPath());
        /*if (selectedPath.endsWith(".jpg") ) {
        	Proc_SelectedImage(selectedPath);
        }*/
        date.setText(new Date(file.lastModified()).toString());
        size.setText(file.length() + " bytes");
        readable.setSelected(file.canRead());
        writable.setSelected(file.canWrite());
        executable.setSelected(file.canExecute());
        isDirectory.setSelected(file.isDirectory());

        isFile.setSelected(file.isFile());

        JFrame f = (JFrame)gui.getTopLevelAncestor();
        if (f!=null) {
            f.setTitle(
                
                "Browsing :: " +
                fileSystemView.getSystemDisplayName(file) );
        }

        gui.repaint();
    }
    
    
}
			
    
  

