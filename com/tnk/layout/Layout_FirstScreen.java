package com.tnk.layout;

/*
 *
 * Main Screen for the TeethCart Alpha Application
 *  
 *  A foray back into the Computer Vision industry
 * 
 */

import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageConsumer;
import java.io.File;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
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
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.table.TableColumn;

import com.tnk.Item_OperationInput;
import com.tnk.util.FileTableModel;
import com.tnk.util.Utilities;
import com.tnk.util.impro.SuperpixelRunnable;

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
import javax.swing.JTextPane;

public class Layout_FirstScreen {

	private DefaultMutableTreeNode root;
	private JTree tree;
	private JFrame frame;
	private JPanel fileMainDetails;
	private JScrollPane previewWindow;

	/** Main GUI container */
	private JPanel gui;

	/* Interface */
	private JLabel label_se; // SE scroll label; for developer callouts
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

	/*
	 * File controls. private JButton openFile; private JButton printFile; private
	 * JButton editFile;
	 */

	// Directory listing
	private JTable table;
	private JProgressBar progressBar;
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

	// private JTree tree;

	/*
	 * LABEL IMPRO VARS
	 * [I][P] Image Processing Controls
	 * 
	 * Thanks BoofCV!
	 * 
	 */
	public JRadioButton radio_LineDetect;
	public JRadioButton radio_FitEllipses;
	public JRadioButton radio_DetectFeatures;
	public JRadioButton radio_EasySurf;
	public JRadioButton radio_ImageDerivative;
	public JRadioButton radio_ImageFeatures;
	public JRadioButton radio_AIP; // yet to be implemented
	public JRadioButton radio_SceneRecognition; // yet to implement
	public JRadioButton radio_ColorSegementation; // implementation to come
	public JRadioButton radio_CannyEdge;
	public JRadioButton radio_SuperPixel;
	public JRadioButton radio_AlgoAlpha;

	private BufferedImage input;
	private BufferedImage superpixelOutput;
	private BufferedImage derivativeOutput;
	private BufferedImage ellipsesOutput;
	// ellipses
	private ListDisplayPanel ldp;
	private String selectedPath = "";

	public Item_OperationInput runBlob;
	public Item_OperationInput runImageDeriv;
	public Item_OperationInput runImageFeatures;
	public Item_OperationInput runSURF;
	public Item_OperationInput runLine;
	public Item_OperationInput runFitElip;
	public Item_OperationInput runSuperpixel;
	public Item_OperationInput runAlgoAlpha;

	// in the process of implementing
	// public JRadioButton radio_

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
		runLate();
	}

	/**
	 * Run after the interface is loaded
	 */
	public void runLate() {
		// String runimg = new String();
		Utilities.DW_AddColouredText("runImageDeriv -" + String.valueOf(runImageDeriv.getEnabled()), Color.BLACK);
		Utilities.DW_AddColouredText("runImageFeatures -" + String.valueOf(runImageFeatures.getEnabled()), Color.BLACK);
		Utilities.DW_AddColouredText("runFitElip -" + String.valueOf(runFitElip.getEnabled()), Color.BLACK);
		Utilities.DW_AddColouredText("runLine -" + String.valueOf(runLine.getEnabled()), Color.BLACK);
		Utilities.DW_AddColouredText("runSURF -" + String.valueOf(runSURF.getEnabled()), Color.BLACK);
	}

	/**
	 * Initialize the contents of the frame.
	 * 	   _____________  _____________   ___________
	 *   ||  			||  			|| 			|
	 *   ||  			||  			|| 			|
	 *   ||  			||  			|| 			| 
	 *   ||  	tree	||  	table	|| 	debug	|  
	 *   ||  			||  			|| 			| 
	 *   ||  			||  			|| 			| 
	 *   || ____________||______________||__________|
	 *   ||  			||  			|| 			|
	 *   ||  			||  			|| 			|
	 *   ||  	details	||  operations 	|| 	image	|  
	 *   ||  			||  			|| 	preview	| 
	 *   || ____________||______________||__________|
	 * 
	 *  This MiG layout shit is something I am going to have to work at
	 *  
	 * 
	 */
	private void initialize() {
		
		fileSystemView = FileSystemView.getFileSystemView();
		desktop = Desktop.getDesktop();

		frame = new JFrame();
		frame.setBounds(0, 0, 1366, 768); // for my small ass monitor :'(
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(new MigLayout("", "[grow|grow|grow]",
														   "[fill|fill|fill]"));
		
		//this is for the file details
		// TODO make this into the picture preview
		initializeMyViewer();
		initializeMyExplorer();

		label_se = new JLabel();
		label_se.setText("Holy crap, I'm a label!");

		scrollpane_se = new JScrollPane();
		scrollpane_se.setViewportView(ldp);
		scrollpane_se.setBorder(new BevelBorder(BevelBorder.RAISED, Color.ORANGE, null, null, null));
		scrollpane_se.setColumnHeaderView(label_se);

		readable = new JCheckBox();
		writable = new JCheckBox();
		executable = new JCheckBox();
		isDirectory = new JRadioButton();
		isFile = new JRadioButton();
		
		// LABEL Folder View
		// Add the File Explorer tree to the Content Pane
		table = new JTable();
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setAutoCreateRowSorter(true);
		table.setShowVerticalLines(false);

		listSelectionListener = new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent lse) {
				int row = table.getSelectionModel().getLeadSelectionIndex();
				//setFileDetails(((FileTableModel) table.getModel()).getFile(row));
			}
		};

		table.getSelectionModel().addListSelectionListener(listSelectionListener);
		explorerPane = new JScrollPane();
		
		Dimension d = explorerPane.getPreferredSize();
		explorerPane.setPreferredSize(new Dimension((int)d.getWidth(), (int)d.getHeight()));
		// LABEL Folder View END
		explorerPane.setViewportView(tree);

		JScrollPane sp_right = new JScrollPane();

		JViewport vp_lbl_ops = new JViewport();
		JLabel lblNewLabel = new JLabel("Operation Window");
		lblNewLabel.setForeground(Color.BLACK);
		lblNewLabel.setBackground(Color.BLACK);
		vp_lbl_ops.add(OperationPanel());
		// TODO Fixme!!!
		// Utilities.runLineRadio.setActionMap(Utilities.IP_DetectFeatures(input,
		// GrayF32.class));

		sp_right.setViewport(vp_lbl_ops);

		

		JScrollPane scrollpane_center = new JScrollPane();
		scrollpane_center.setViewportView(viewerTable);
		scrollpane_center.setBorder(new BevelBorder(BevelBorder.RAISED, Color.darkGray, null, null, null));

		// This used to be the
		// frame.getContentPane().add(scrollpane_se, "cell 1 1,grow");
		// vp_panel_right.add(scrollpane_right);

		// TODO INvestigate this progress bar!
		progressBar = new JProgressBar();

		// Add the ViewerTablee to the GUI
		// frame.getContentPane().add(viewerTable, "cell 1 0,grow");
		JTextPane dbWindow = Utilities.DebugWindow();
		previewWindow = new JScrollPane();
		
		frame.setBackground(Color.YELLOW);
		explorerPane.setBackground(Color.RED);
		scrollpane_se.setBackground(Color.RED);
		sp_right.setBackground(Color.RED);
		scrollpane_center.setBackground(Color.RED);
		previewWindow.setBackground(Color.RED);
		
		
		frame.getContentPane().add(explorerPane, "cell 0 0,grow");
		frame.getContentPane().add(scrollpane_center, "cell 1 0,growx");
		frame.getContentPane().add(sp_right, "cell 1 1,growx");
		frame.getContentPane().add(dbWindow, "cell 2 0, growy");
		frame.getContentPane().add(fileMainDetails, "cell 0 1, grow");
		frame.getContentPane().add(previewWindow, "cell 2 1, growx, height 200:300:400");
		frame.getContentPane().add(progressBar, "cell 0 2 3 2,growx");

		
		/*
		 * LABEL ProgressBar
		 */
		progressBar.setMinimum(0);
		progressBar.setMaximum(0);
		progressBar.setValue(0);
		progressBar.setString("Welcome");
		progressBar.setStringPainted(true);
	}

	public void imageLoaded(BufferedImage img) {
		this.superpixelOutput = img;
	}
	
	private void Proc_SelectedImage(String imagePath) {
		if (imagePath == null) {
			label_se.setText(imagePath);
			imagePath = "C:\\Users\\Tom\\Pictures\\download.jpg";
		}
		System.out.println(selectedPath + " is a Picture!");
		label_se.setText(selectedPath);
		input = UtilImageIO.loadImage(UtilIO.pathExample(selectedPath));
		
		int currentX = previewWindow.getWidth();
		int currentY = previewWindow.getHeight();
		JLabel displayPreview = new JLabel(new ImageIcon(input.getScaledInstance(currentX, currentY, 0)));
		
		//previewWindow = new JScrollPane(displayPreview);
		//frame.getContentPane().remove();
		previewWindow.setViewportView(new JScrollPane(displayPreview));
		//frame.getContentPane().add(previewWindow, "cell 2 1, grow, width 100:150:200, height 100:150:200");
		
	}

	public class CreateChildNodes implements Runnable {

		private DefaultMutableTreeNode root;

		private File fileRoot;

		public CreateChildNodes(File fileRoot, DefaultMutableTreeNode root) {
			this.fileRoot = fileRoot;
			this.root = root;
		}

		@Override
		public void run() {
			createChildren(fileRoot, root);
		}

		private void createChildren(File fileRoot, DefaultMutableTreeNode node) {
			File[] files = fileRoot.listFiles();
			if (files == null)
				return;

			for (File file : files) {
				DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(new FileNode(file));
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
		 * | rev 00 | 2018-04-29
		 *
		 * Short method to help populate the information of the JTree File Explorer
		 * 
		 * Based on a SO answer for a simple drive explorer
		 * 
		 * TODO [LIST] File Explorer [LIST] FIXME These are for repairs MEMBERRY These
		 * are for nostalgia, or caveats, or pitfalls
		 * 
		 * - Icons? - Just show folders - Last location browsed? -> likely will have to
		 * dabble with a kind of 'User Preferences' for this
		 * 
		 */
		TreeSelectionListener treeSelectionListener = new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent tse) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tse.getPath().getLastPathComponent();
				showChildren(node);
				setFileDetails((File) node.getUserObject());
			}
		};

		fileSystemView = FileSystemView.getFileSystemView();
		
		tree = Utilities.DriveExplorer(fileSystemView);
		tree.addTreeSelectionListener(treeSelectionListener);
		Dimension preferredSize = tree.getPreferredSize();
        Dimension widePreferred = new Dimension(
            200,
            (int)preferredSize.getHeight());
        tree.setPreferredSize( widePreferred );
	}

	private void initializeMyViewer() {

		viewerTable = new JTable();
		viewerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		viewerTable.setAutoCreateRowSorter(true);
		viewerTable.setShowVerticalLines(false);
		listSelectionListener = new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent lse) {
				System.out.println("Selection changed to " + selectedPath);
				int row = viewerTable.getSelectionModel().getLeadSelectionIndex();
				if (selectedPath.endsWith(".jpg") || selectedPath.endsWith(".png")) {
					
					Proc_SelectedImage(selectedPath);
				}
				
				if (((FileTableModel) viewerTable.getModel()).getFile(row) != null) {
				setFileDetails(((FileTableModel) viewerTable.getModel()).getFile(row));
				}
			}
		};
		viewerTable.getSelectionModel().addListSelectionListener(listSelectionListener);

		// Dimension d = explorerPane.getPreferredSize();
		// explorerPane.setPreferredSize(new Dimension((int)d.getWidth(),
		// (int)d.getHeight()/2));
		// Details of a file
		fileMainDetails = new JPanel(new BorderLayout(4, 2));
		fileMainDetails.setBorder(new EmptyBorder(0, 6, 0, 6));

		JPanel fileDetailsLabels = new JPanel(new GridLayout(0, 1, 2, 2));
		fileMainDetails.add(fileDetailsLabels, BorderLayout.WEST);

		JPanel fileDetailsValues = new JPanel(new GridLayout(0, 1, 2, 2));
		fileMainDetails.add(fileDetailsValues, BorderLayout.CENTER);

		

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
		JPanel flags = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 0));
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
		// progressBar.setVisible(true);
		// progressBar.setIndeterminate(true);

		SwingWorker<Void, File> worker = new SwingWorker<Void, File>() {
			@Override
			public Void doInBackground() {
				File file = (File) node.getUserObject();
				if (file.isDirectory()) {
					File[] files = fileSystemView.getFiles(file, true); // This is apparently a very important step or a
																		// commonly missed caveat!
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
				// progressBar.setIndeterminate(false);
				// progressBar.setVisible(false);
				tree.setEnabled(true);
			}
		};
		worker.execute();
	}

	/** Update the table on the EDT */
	private void setTableData(final File[] files) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (fileTableModel == null) {
					fileTableModel = new FileTableModel();
					viewerTable.setModel(fileTableModel);
				}
				viewerTable.getSelectionModel().removeListSelectionListener(listSelectionListener);
				fileTableModel.setFiles(files);
				viewerTable.getSelectionModel().addListSelectionListener(listSelectionListener);
				if (!cellSizesSet) {
					Icon icon = fileSystemView.getSystemIcon(files[0]);

					// size adjustment to better account for icons
					viewerTable.setRowHeight(icon.getIconHeight() + rowIconPadding);

					setColumnWidth(0, -1);
					setColumnWidth(3, 60);
					viewerTable.getColumnModel().getColumn(3).setMaxWidth(120);
					setColumnWidth(4, -1);
					setColumnWidth(5, -1);
					setColumnWidth(6, -1);
					setColumnWidth(7, -1);
					setColumnWidth(8, -1);
					setColumnWidth(9, -1);

					cellSizesSet = true;
				}
			}
		});
	}

	private void setColumnWidth(int column, int width) {
		TableColumn tableColumn = viewerTable.getColumnModel().getColumn(column);
		if (width < 0) {
			// use the preferred width of the header..
			JLabel label = new JLabel((String) tableColumn.getHeaderValue());
			Dimension preferred = label.getPreferredSize();
			// altered 10->14 as per camickr comment.
			width = (int) preferred.getWidth() + 14;
		}
		tableColumn.setPreferredWidth(width);
		tableColumn.setMaxWidth(width);
		tableColumn.setMinWidth(width);
	}

	/** Update the File details view with the details of this File. */
	private void setFileDetails(File file) {
		// currentFile = file;
		Icon icon = fileSystemView.getSystemIcon(file);
		fileName.setIcon(icon);
		fileName.setText(fileSystemView.getSystemDisplayName(file));

		selectedPath = file.getPath();

		path.setText(file.getPath());
		/*
		 * if (selectedPath.endsWith(".jpg") ) { Proc_SelectedImage(selectedPath); }
		 */
		date.setText(new Date(file.lastModified()).toString());
		size.setText(file.length() + " bytes");
		
		if (file != null) {
		
		// TODO I think the checkboxes may be broken?
		readable.setSelected(file.canRead());
		writable.setSelected(file.canWrite());
		executable.setSelected(file.canExecute());
		isDirectory.setSelected(file.isDirectory());

		isFile.setSelected(file.isFile());

		}
		if (gui != null) {
		
		JFrame f = (JFrame) gui.getTopLevelAncestor();
		if (f != null) {
			f.setTitle(

					"Browsing :: " + fileSystemView.getSystemDisplayName(file));
		}
		
		System.out.println("Selected " + selectedPath);
		Utilities.DW_AddColouredText(file.getAbsolutePath(), Color.ORANGE);
		if (selectedPath.endsWith(".png") || selectedPath.endsWith(".jpg"))
		{
			input = UtilImageIO.loadImage(UtilIO.pathExample(file.getPath()));
			
			JLabel displayPreview = new JLabel(new ImageIcon(input));
			
			previewWindow.setViewportView(displayPreview);
		}
		else {
			label_se.setText(file.getPath());
		}
		
		
		

		gui.repaint();
		}
	}

	public JPanel OperationPanel() {

		JPanel jp = new JPanel(new GridLayout(5, 2));
		jp.add(new JLabel("OPS", JLabel.CENTER));

		runImageDeriv = new Item_OperationInput();
		runImageDeriv.setEnabled(false);
		radio_ImageDerivative = new JRadioButton("Derivate Image");
		runImageFeatures = new Item_OperationInput();
		runImageFeatures.setEnabled(false);
		radio_ImageFeatures = new JRadioButton("Image Features");
		runSURF = new Item_OperationInput();
		runSURF.setEnabled(false);
		radio_EasySurf = new JRadioButton("SURF");
		runLine = new Item_OperationInput();
		runLine.setEnabled(false);
		radio_LineDetect = new JRadioButton("Line Detection");
		runFitElip = new Item_OperationInput();
		runFitElip.setEnabled(false);
		radio_FitEllipses = new JRadioButton("Fit Ellipses");
		runSuperpixel = new Item_OperationInput();
		runSuperpixel.setEnabled(false);
		radio_SuperPixel = new JRadioButton("Superpixel Image");
		runAlgoAlpha = new Item_OperationInput();
		runAlgoAlpha.setEnabled(false);
		radio_AlgoAlpha = new JRadioButton("Alpha");
		

		// Contract_OperationInput runBlob = new Contract_OperationInput();
		// runBlobRadio = new JRadioButton("Blob Detection");
		// Contract_OperationInput runAIP = new Contract_OperationInput();
		// runAIPRadio = new JRadioButton("Associate Interest Points");

		JButton runIPButton = new JButton();

		radio_ImageDerivative.addItemListener(new ItemListener() {
			@Override

			public void itemStateChanged(ItemEvent arg0) {
				Utilities.DW_AddColouredText("\n Derivative Radio TOUCHED", Color.BLACK);
				if (runImageDeriv.getEnabled()) {
					runImageDeriv.setEnabled(false);
					
					progressBar.setMaximum(progressBar.getMaximum()-1);
				} else {
					runImageDeriv.setEnabled(true);
					
					progressBar.setMaximum(progressBar.getMaximum()+1);
				}
			}
		});
		radio_ImageFeatures.addItemListener(new ItemListener() {
			@Override

			public void itemStateChanged(ItemEvent arg0) {
				Utilities.DW_AddColouredText("\n Features Radio TOUCHED", Color.BLACK);
				if (runImageFeatures.getEnabled()) {
					runImageFeatures.setEnabled(false);
					
					progressBar.setMaximum(progressBar.getMaximum()-1);
				} else {
					runImageFeatures.setEnabled(true);
					
					progressBar.setMaximum(progressBar.getMaximum()+1);
				}
			}
		});
		radio_EasySurf.addItemListener(new ItemListener() {
			@Override

			public void itemStateChanged(ItemEvent arg0) {
				Utilities.DW_AddColouredText("\n SURF Radio TOUCHED", Color.BLACK);
				if (runSURF.getEnabled()) {
					runSURF.setEnabled(false);
					
					progressBar.setMaximum(progressBar.getMaximum()-1);
				} else {
					runSURF.setEnabled(true);
					
					progressBar.setMaximum(progressBar.getMaximum()+1);
				}
			}
		});
		radio_LineDetect.addItemListener(new ItemListener() {
			@Override

			public void itemStateChanged(ItemEvent arg0) {
				Utilities.DW_AddColouredText("\n Line Radio TOUCHED", Color.BLACK);
				if (runLine.getEnabled()) {
					runLine.setEnabled(false);
					
					progressBar.setMaximum(progressBar.getMaximum()-1);
				} else {
					runLine.setEnabled(true);
					
					progressBar.setMaximum(progressBar.getMaximum()+1);
				}
			}
		});
		radio_FitEllipses.addItemListener(new ItemListener() {
			@Override

			public void itemStateChanged(ItemEvent arg0) {
				Utilities.DW_AddColouredText("\n Ellipses Radio TOUCHED", Color.BLACK);
				if (runFitElip.getEnabled()) {
					runFitElip.setEnabled(false);
					
					progressBar.setMaximum(progressBar.getMaximum()-1);
				} else {
					runFitElip.setEnabled(true);
					
					progressBar.setMaximum(progressBar.getMaximum()+1);
				}
			}
		});

		radio_SuperPixel.addItemListener(new ItemListener() {
			@Override

			public void itemStateChanged(ItemEvent arg0) {
				Utilities.DW_AddColouredText("\n Superpixel Radio TOUCHED", Color.BLACK);
				if (runSuperpixel.getEnabled()) {
					runSuperpixel.setEnabled(false);
					
					progressBar.setMaximum(progressBar.getMaximum()-1);
				} else {
					runSuperpixel.setEnabled(true);
					
					progressBar.setMaximum(progressBar.getMaximum()+1);
				}
			}
		});
		radio_AlgoAlpha.addItemListener(new ItemListener() {
			@Override

			public void itemStateChanged(ItemEvent arg0) {
				Utilities.DW_AddColouredText("\n Algo Alpha Radio TOUCHED", Color.BLACK);
				if (runAlgoAlpha.getEnabled()) {
					runAlgoAlpha.setEnabled(false);
				} else {
					runAlgoAlpha.setEnabled(true);
				}
			}
		});

		jp.add(radio_ImageDerivative);
		jp.add(radio_ImageFeatures);
		jp.add(radio_EasySurf);
		jp.add(radio_LineDetect);
		jp.add(radio_FitEllipses);
		jp.add(radio_SuperPixel);
		jp.add(radio_AlgoAlpha);

		runIPButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				Utilities.DW_AddColouredText("Button PRESSED", Color.BLUE);

				if (runImageDeriv.getEnabled()) {
					Utilities.DW_AddColouredText("Image Derivation Enabled", Color.BLUE);

					Utilities.DW_AddColouredText("Running Image Derivative on:", Color.BLUE);
					
					// LABEL Try Me
					// See if we can make a private variable for the imagePath to label in the progress bar?
					progressBar.setString("Running Derivative " + 
										   progressBar.getValue() +
										   "/" + progressBar.getMaximum() + 
										   "_" + progressBar.getPercentComplete());
					
					Utilities.DW_AddColouredText(selectedPath, Color.black);
					Utilities.IP_ImageDerivative(selectedPath);
					progressBar.setValue(progressBar.getValue() + 1);
				}

				if (runImageFeatures.getEnabled()) {
					Utilities.DW_AddColouredText("Image Features Enabled", Color.BLUE);

					progressBar.setString("Finding Features... " + 
										   progressBar.getValue() +
										   "/" + progressBar.getMaximum() + 
										   "_" + progressBar.getPercentComplete());
					
					
					Utilities.DW_AddColouredText("Finding Image Features in:", Color.BLUE);
					Utilities.DW_AddColouredText(selectedPath, Color.black);
					Utilities.IP_DetectFeatures(input, GrayF32.class);
					
					progressBar.setValue(progressBar.getValue() + 1);
				}
				if (runSURF.getEnabled()) {
					Utilities.DW_AddColouredText("Easy Surf Enabled", Color.BLUE);

					progressBar.setString("SURFing...tubular!  " + 
										   progressBar.getValue() +
										   "/" + progressBar.getMaximum() + 
										   "_" + progressBar.getPercentComplete());
					
					
					Utilities.DW_AddColouredText("Running Easy SURF on:", Color.BLUE);
					Utilities.DW_AddColouredText(selectedPath, Color.black);
					Utilities.IP_EasySURF(selectedPath);
					
					progressBar.setValue(progressBar.getValue() + 1);
				}
				if (runLine.getEnabled()) {
					Utilities.DW_AddColouredText("Line Detection Enabled", Color.BLUE);

					progressBar.setString("Looking for lines... " + 
							   progressBar.getValue() +
							   "/" + progressBar.getMaximum() + 
							   "_" + progressBar.getPercentComplete());
					
					Utilities.DW_AddColouredText("Running Line Detection on:", Color.BLUE);
					Utilities.DW_AddColouredText(selectedPath, Color.black);
					Utilities.IP_LineDetect(selectedPath, GrayU8.class, GrayS16.class, 7);
					
					progressBar.setValue(progressBar.getValue() + 1);
				}
				if (runFitElip.getEnabled()) {
					Utilities.DW_AddColouredText("Fit Ellippses Enabled", Color.BLUE);

					progressBar.setString("Drawing circles... " + 
							   progressBar.getValue() +
							   "/" + progressBar.getMaximum() + 
							   "_" + progressBar.getPercentComplete());
					
					Utilities.DW_AddColouredText("Running Fit Ellipses on:", Color.BLUE);
					Utilities.DW_AddColouredText(selectedPath, Color.black);
					Utilities.IP_FitEllipses(selectedPath);
					
					progressBar.setValue(progressBar.getValue() + 1);
				}
				if (runSuperpixel.getEnabled()) {
					Utilities.DW_AddColouredText("Superpixel Run en Route!", Color.MAGENTA);

					progressBar.setString("Superpixellating the s&^% out of it..." + 
							   progressBar.getValue() +
							   "/" + progressBar.getMaximum() + 
							   "_" + progressBar.getPercentComplete());
					
					Utilities.DW_AddColouredText("Running Superpixel Example on:", Color.BLUE);
					Utilities.DW_AddColouredText(selectedPath, Color.black);
					//Utilities.IP_ImageSegmentation(selectedPath);
					SuperpixelRunnable sr = new SuperpixelRunnable(selectedPath);
					Thread superpix = new Thread(sr);
					superpix.setName(selectedPath);
					superpix.start();
					
					progressBar.setValue(progressBar.getValue() + 1);
				}
				if (runAlgoAlpha.getEnabled()) {
					Utilities.DW_AddColouredText("First algorithm coming up!!", Color.MAGENTA);
					progressBar.setString("Initiating Alpha... " + 
							   progressBar.getValue() +
							   "/" + progressBar.getMaximum() + 
							   "_" + progressBar.getPercentComplete());
					Utilities.DW_AddColouredText("Running Superpixel Example on:", Color.BLUE);
					Utilities.DW_AddColouredText(selectedPath, Color.black);
					Utilities.IP_ImageSegmentation(selectedPath);
					
					progressBar.setValue(progressBar.getValue() + 1);
				}

			}
		});

		jp.add(runIPButton);

		return jp;
	}
	
	public void FirstFinderAlgorithm(String imagePath) {
		Utilities.IP_DetectFeatures(Utilities.IP_Algo_Deriv(imagePath), GrayF32.class);
		progressBar.setValue(1);
		Utilities.IP_Algo_Segmentation(Utilities.IP_Algo_Deriv(imagePath));
		progressBar.setValue(2);
		progressBar.setString("First Finder Algorithm COMPLETE");
		
	}
	
}
