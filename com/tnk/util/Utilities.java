package com.tnk.util;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.tnk.Contract_OperationInput;

import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.abst.feature.detect.interest.ConfigFastHessian;
import boofcv.abst.feature.detect.interest.InterestPointDetector;
import boofcv.abst.feature.detect.line.DetectLineHoughFoot;
import boofcv.abst.feature.detect.line.DetectLineHoughFootSubimage;
import boofcv.abst.feature.detect.line.DetectLineHoughPolar;
import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.misc.ImageStatistics;
import boofcv.alg.shapes.FitData;
import boofcv.alg.shapes.ShapeFittingOps;
import boofcv.factory.feature.detdesc.FactoryDetectDescribe;
import boofcv.factory.feature.detect.interest.FactoryInterestPoint;
import boofcv.factory.feature.detect.line.ConfigHoughFoot;
import boofcv.factory.feature.detect.line.ConfigHoughFootSubimage;
import boofcv.factory.feature.detect.line.ConfigHoughPolar;
import boofcv.factory.feature.detect.line.FactoryDetectLineAlgs;
import boofcv.gui.ListDisplayPanel;
import boofcv.gui.binary.VisualizeBinaryData;
import boofcv.gui.feature.FancyInterestPointRender;
import boofcv.gui.feature.ImageLinePanel;
import boofcv.gui.feature.VisualizeShapes;
import boofcv.gui.image.ShowImages;
import boofcv.io.UtilIO;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.ConnectRule;
import boofcv.struct.feature.BrightFeature;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayS16;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageGray;
import georegression.struct.line.LineParametric2D_F32;
import georegression.struct.point.Point2D_F64;
import georegression.struct.shapes.EllipseRotated_F64;

public class Utilities {

	private static DefaultMutableTreeNode root;
	private static DefaultTreeModel treeModel;
	
	public static Contract_OperationInput runLine;
	public Contract_OperationInput runBlob;
	// private JTree tree;
	

	public static JRadioButton runLineRadio;
	public static JRadioButton runBlobRadio;
	public static JRadioButton runSURFRadio;
	public static JRadioButton runAIPRadio;
	public static JRadioButton runImageDerivRadio;
	public static JRadioButton runDIFRadio;
	public static JRadioButton runColourSegRadio;
	public static JRadioButton runSceneRecogRadio;

	/**
	 * Line Detector
	 * 
	 * @return
	 */

	/**
	 * Adjusts edge threshold for identifying pixels belonging to a line
	 */
	private static final float edgeThreshold = 25;
	/**
	 * Adjust the maximum number of found lines in an image
	 */
	private static final int maxLines = 10;
	private static ListDisplayPanel listPanel = new ListDisplayPanel();

	public static JTree DriveExplorer(FileSystemView fileSystemView) {
		// FileSystemView fileSystemView = FileSystemView.getFileSystemView();
		// File fileRoot = new File("C:/");
		// root = new DefaultMutableTreeNode(new FileNode(fileRoot));
		// treeModel = new DefaultTreeModel(root);

		// the File tree
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		treeModel = new DefaultTreeModel(root);
		// show the file system roots
		File[] roots = fileSystemView.getRoots();
		for (File fileSystemRoot : roots) {
			DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(fileSystemRoot);
			root.add(rootNode);
			File[] files = fileSystemView.getFiles(fileSystemRoot, true);
			for (File file : files) {
				if (file.isDirectory()) {
					rootNode.add(new DefaultMutableTreeNode(file));
				}
			}
		}

		// Lets make the tree variable
		JTree tree = new JTree(treeModel);
		// Change some attributes
		tree.setRootVisible(false);
		// tree.addTreeSelectionListener(tsl);
		tree.setCellRenderer(new FileTreeCellRenderer());
		// tree.setShowsRootHandles(true);
		tree.expandRow(0);
		tree.setVisibleRowCount(15);

		// Create the ChildNode
		// CreateChildNodes ccn = new CreateChildNodes(fileRoot, root);
		// new Thread(ccn).start();

		// Return the finished tree to the caller
		return tree;
	}

	/**
	 * Detects lines inside the image using different types of Hough detectors
	 * 
	 * 
	 * @param image
	 *            Input image
	 * @param imagetype
	 *            type of image processed by line detector
	 * @param derivType
	 *            Type of image derivative
	 * @return
	 */

	public static <T extends ImageGray<T>, D extends ImageGray<D>> 
	ListDisplayPanel IP_DetectLines(BufferedImage image,
			Class<T> imageType, Class<D> derivType) {
		// convert the line into a single band
		T input = ConvertBufferedImage.convertFromSingle(image, null, imageType);

		// Comment/uncomment to try a different type of line detector
		
	
		DetectLineHoughPolar<T, D> detectorPolar = FactoryDetectLineAlgs.houghPolar(
				new ConfigHoughPolar(3, 30, 2, Math.PI / 180, edgeThreshold, maxLines), imageType, derivType);
				List<LineParametric2D_F32> found = detectorPolar.detect(input);
		
		DetectLineHoughFoot<T, D> detectorFoot = FactoryDetectLineAlgs
				.houghFoot(new ConfigHoughFoot(3, 8, 5, edgeThreshold, maxLines), imageType, derivType);
		DetectLineHoughFootSubimage<T, D> detectorFootsubimage = FactoryDetectLineAlgs.houghFootSub(
				new ConfigHoughFootSubimage(3, 8, 5, edgeThreshold, maxLines, 2, 2), imageType, derivType);

		

		// display the results
		ImageLinePanel gui = new ImageLinePanel();
		gui.setBackground(image);
		gui.setLines(found);
		gui.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));

		listPanel.addItem(gui, "Found Lines");
		return listPanel;
	}
	
	
	public static <T extends ImageGray<T>>
	void IP_DetectFeatures( BufferedImage image , Class<T> imageType ) {
		T input = ConvertBufferedImage.convertFromSingle(image, null, imageType);

		// Create a Fast Hessian detector from the SURF paper.
		// Other detectors can be used in this example too.
		InterestPointDetector<T> detector = FactoryInterestPoint.fastHessian(
				new ConfigFastHessian(10, 2, 100, 2, 9, 3, 4));

		// find interest points in the image
		detector.detect(input);

		// Show the features
		displayResults(image, detector);
}
	
	private static <T extends ImageGray<T>>
	void displayResults(BufferedImage image, InterestPointDetector<T> detector)
	{
		Graphics2D g2 = image.createGraphics();
		FancyInterestPointRender render = new FancyInterestPointRender();


		for( int i = 0; i < detector.getNumberOfFeatures(); i++ ) {
			Point2D_F64 pt = detector.getLocation(i);

			// note how it checks the capabilities of the detector
			if( detector.hasScale() ) {
				int radius = (int)(detector.getRadius(i));
				render.addCircle((int)pt.x,(int)pt.y,radius);
			} else {
				render.addPoint((int) pt.x, (int) pt.y);
			}
		}
		// make the circle's thicker
		g2.setStroke(new BasicStroke(3));

		// just draw the features onto the input image
		render.draw(g2);
		ShowImages.showWindow(image, "Detected Features", true);
}
	
	public static JPanel OperationPanel () {
		
		JPanel jp = new JPanel(new GridLayout(5,2));
		jp.add(new JLabel("OPS", JLabel.CENTER));
		
		Contract_OperationInput runLine = new Contract_OperationInput();
		runLineRadio = new JRadioButton("Line Detection");
		Contract_OperationInput runBlob = new Contract_OperationInput();
		runBlobRadio = new JRadioButton("Blob Detection");
		Contract_OperationInput runSURF = new Contract_OperationInput();
		runSURFRadio = new JRadioButton("SURF");
		Contract_OperationInput runAIP = new Contract_OperationInput();
		runAIPRadio = new JRadioButton("Associate Interest Points");
		Contract_OperationInput runImageDeriv = new Contract_OperationInput();
		runImageDerivRadio = new JRadioButton("Derivate Image");
		Contract_OperationInput runDIF = new Contract_OperationInput();
		runDIFRadio = new JRadioButton("Dense Image Features");
		Contract_OperationInput runColourSeg = new Contract_OperationInput();
		runColourSegRadio = new JRadioButton("Segment Colour");
		Contract_OperationInput runSceneRecog = new Contract_OperationInput();
		runSceneRecogRadio = new JRadioButton("Scene Recognition");
		
		
		
		runLineRadio.addItemListener(new ItemListener() {
			@SuppressWarnings("static-access")
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				runLine.setEnabled(true);
	
			}
		});
		
		jp.add(runLineRadio);
		jp.add(runBlobRadio);
		jp.add(runSURFRadio);
		jp.add(runAIPRadio);
		jp.add(runImageDerivRadio);
		jp.add(runDIFRadio);
		jp.add(runColourSegRadio);
		jp.add(runSceneRecogRadio);
		
		return jp;
	}
	
	//ldp = Utilities.IP_DetectLines(input, GrayU8.class, GrayS16.class);
	public static <T extends ImageGray<T>, D extends ImageGray<D>> 
	void IP_LineDetect(String imagePath, Class<T> imageType, Class<D> derivType) {
		
		BufferedImage image = UtilImageIO.loadImage(UtilIO.pathExample(imagePath));
		T input = ConvertBufferedImage.convertFromSingle(image, null, imageType);
		
		DetectLineHoughPolar<T, D> detectorPolar = FactoryDetectLineAlgs.houghPolar(
				new ConfigHoughPolar(3, 30, 2, Math.PI / 180, edgeThreshold, maxLines), imageType, derivType);
				List<LineParametric2D_F32> found = detectorPolar.detect(input);
		
		DetectLineHoughFoot<T, D> detectorFoot = FactoryDetectLineAlgs
				.houghFoot(new ConfigHoughFoot(3, 8, 5, edgeThreshold, maxLines), imageType, derivType);
		DetectLineHoughFootSubimage<T, D> detectorFootsubimage = FactoryDetectLineAlgs.houghFootSub(
				new ConfigHoughFootSubimage(3, 8, 5, edgeThreshold, maxLines, 2, 2), imageType, derivType);

		ImageLinePanel gui = new ImageLinePanel();
		gui.setBackground(image);
		gui.setLines(found);
		gui.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
		//ShowImages.showWindow(VisualizeBinaryData.renderBinary(filtered, false, null),"Binary",true);
		ShowImages.showWindow(gui,"LINEZ@@",true);
		
	}
	
	public static void IP_FitEllipses(String imagePath) {
		// load and convert the image into a usable format
		BufferedImage image = UtilImageIO.loadImage(UtilIO.pathExample(imagePath));
		GrayF32 input = ConvertBufferedImage.convertFromSingle(image, null, GrayF32.class);

		GrayU8 binary = new GrayU8(input.width,input.height);

		// the mean pixel value is often a reasonable threshold when creating a binary image
		double mean = ImageStatistics.mean(input);

		// create a binary image by thresholding
		ThresholdImageOps.threshold(input, binary, (float) mean, true);

		// reduce noise with some filtering
		GrayU8 filtered = BinaryImageOps.erode8(binary, 1, null);
		filtered = BinaryImageOps.dilate8(filtered, 1, null);

		// Find the contour around the shapes
		List<Contour> contours = BinaryImageOps.contour(filtered, ConnectRule.EIGHT,null);

		// Fit an ellipse to each external contour and draw the results
		Graphics2D g2 = image.createGraphics();
		g2.setStroke(new BasicStroke(3));
		g2.setColor(Color.RED);

		for( Contour c : contours ) {
			FitData<EllipseRotated_F64> ellipse = ShapeFittingOps.fitEllipse_I32(c.external,0,false,null);
			VisualizeShapes.drawEllipse(ellipse.shape, g2);
		}

		ShowImages.showWindow(VisualizeBinaryData.renderBinary(filtered, false, null),"Binary",true);
		ShowImages.showWindow(image,"Ellipses",true);
	}
	
	public static void IP_EasySURF( String imagePath ) {
		// create the detector and descriptors
		BufferedImage image = UtilImageIO.loadImage(UtilIO.pathExample(imagePath));
		GrayF32 input = ConvertBufferedImage.convertFromSingle(image, null, GrayF32.class);
		DetectDescribePoint<GrayF32,BrightFeature> surf = FactoryDetectDescribe.
				surfStable(new ConfigFastHessian(0, 2, 200, 2, 9, 4, 4), null, null,GrayF32.class);

		 // specify the image to process
		surf.detect(input);

		System.out.println("Found Features: "+surf.getNumberOfFeatures());
		System.out.println("First descriptor's first value: "+surf.getDescription(0).value[0]);
	}
	

}
