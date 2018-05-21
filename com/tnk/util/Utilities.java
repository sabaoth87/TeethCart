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

import javax.imageio.ImageIO;
import javax.jws.soap.SOAPBinding.Style;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import com.tnk.Item_OperationInput;

import boofcv.abst.feature.detdesc.DetectDescribePoint;
import boofcv.abst.feature.detect.interest.ConfigFastHessian;
import boofcv.abst.feature.detect.interest.InterestPointDetector;
import boofcv.abst.feature.detect.line.DetectLineHoughFoot;
import boofcv.abst.feature.detect.line.DetectLineHoughFootSubimage;
import boofcv.abst.feature.detect.line.DetectLineHoughPolar;
import boofcv.abst.filter.derivative.AnyImageDerivative;
import boofcv.alg.filter.binary.BinaryImageOps;
import boofcv.alg.filter.binary.Contour;
import boofcv.alg.filter.binary.ThresholdImageOps;
import boofcv.alg.filter.derivative.DerivativeType;
import boofcv.alg.filter.derivative.GImageDerivativeOps;
import boofcv.alg.misc.ImageStatistics;
import boofcv.alg.shapes.FitData;
import boofcv.alg.shapes.ShapeFittingOps;
import boofcv.core.image.border.BorderType;
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
import boofcv.gui.image.VisualizeImageData;
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
import net.miginfocom.swing.MigLayout;

/*
 * LABEL [UTILITIES]
 * 
 * This is my grab bag of utilities for the project
 */

public class Utilities {

	private static DefaultMutableTreeNode root;
	private static DefaultTreeModel treeModel;

	public static Item_OperationInput runLine;
	public Item_OperationInput runBlob;
	// private JTree tree;

	public static JRadioButton runLineRadio;
	public static JRadioButton runBlobRadio;
	public static JRadioButton runSURFRadio;
	public static JRadioButton runAIPRadio;
	public static JRadioButton runImageDerivRadio;
	public static JRadioButton runDIFRadio;
	public static JRadioButton runColourSegRadio;
	public static JRadioButton runSceneRecogRadio;

	public static JTextPane _window;

	/**
	 * Adjusts edge threshold for identifying pixels belonging to a line
	 */
	private static final float edgeThreshold = 25;
	/**
	 * Adjust the maximum number of found lines in an image
	 */
	private static final int maxLines = 10;
	private static ListDisplayPanel listPanel = new ListDisplayPanel();

	public Utilities() {
		// TODO Do I fill this with anything?
	}
	
	/**
	 * LABEL [DriveExplorer] 
	 * Frankenstein'd version of a File Browser; SO:Java FTW !!
	 * 
	 * @param fileSystemView
	 * @return
	 */
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
		//tree.setShowsRootHandles(true);
		tree.setToolTipText("My Tree!");
		tree.expandRow(0);
		tree.setVisibleRowCount(15);
		return tree;
	}

	/**
	 * LABEL [IP - Detect Features]
	 * 
	 * Detects features
	 * 
	 * Outputs a .PNG of the found data
	 * "FeatureDetection_output");
	 * 
	 * @param image
	 * @param imageType
	 */
	
	public static <T extends ImageGray<T>> void IP_DetectFeatures(BufferedImage image, Class<T> imageType) {
		T input = ConvertBufferedImage.convertFromSingle(image, null, imageType);

		// Create a Fast Hessian detector from the SURF paper.
		// Other detectors can be used in this example too.
		InterestPointDetector<T> detector = FactoryInterestPoint
				.fastHessian(new ConfigFastHessian(10, 2, 100, 2, 9, 3, 4));

		// find interest points in the image
		detector.detect(input);

		// Show the features
		displayResults(image, detector);
	}

	private static <T extends ImageGray<T>> void displayResults(BufferedImage image,
			InterestPointDetector<T> detector) {
		Graphics2D g2 = image.createGraphics();
		FancyInterestPointRender render = new FancyInterestPointRender();

		for (int i = 0; i < detector.getNumberOfFeatures(); i++) {
			Point2D_F64 pt = detector.getLocation(i);

			// note how it checks the capabilities of the detector
			if (detector.hasScale()) {
				int radius = (int) (detector.getRadius(i));
				render.addCircle((int) pt.x, (int) pt.y, radius);
			} else {
				render.addPoint((int) pt.x, (int) pt.y);
			}
		}
		// make the circle's thicker
		g2.setStroke(new BasicStroke(3));

		// just draw the features onto the input image
		render.draw(g2);
		
		IP_Util_SaveBI(image,"FeatureDetection_output");
		
		ShowImages.showWindow(image, "Detected Features", true);
	}
	
<<<<<<< HEAD

	/**
	 * LABEL [IP_Util_SaveBI]
	 * 
	 * Simple routine to simplify the output from all of these Image Processes
	 * during the testing phases(s)
	 * 
	 * @param image
	 * @param fileName
	 */
	public static void IP_Util_SaveBI(BufferedImage image, String fileName) {
		String TAG = "IP_Util_SaveBI ";
		System.out.println(TAG + "Saving an image to disk...");
		String outputPath = new String("C:\\test\\output\\"+fileName+".png");
		File f = new File(outputPath);
		UtilImageIO.saveImage(image, outputPath);
		System.out.println(TAG + "Saved " + outputPath);
	}

	/**
=======

	/**
	 * LABEL [IP_Util_SaveBI]
	 * 
	 * Simple routine to simplify the output from all of these Image Processes
	 * during the testing phases(s)
	 * 
	 * @param image
	 * @param fileName
	 */
	public static void IP_Util_SaveBI(BufferedImage image, String fileName) {
		String TAG = "IP_Util_SaveBI ";
		System.out.println(TAG + "Saving an image to disk...");
		String outputPath = new String("C:\\test\\output\\"+fileName+".png");
		File f = new File(outputPath);
		UtilImageIO.saveImage(image, outputPath);
		System.out.println(TAG + "Saved " + outputPath);
	}

	/**
>>>>>>> origin/BreadBanjo
	 * LABEL [IP_LineDetect] 
	 * Find lines example from BoofCV - thanks!
	 * 
	 * Outputs 
	 * "LineDetection_output.PNG"
	 * 
	 * @param imagePath
	 * @param imageType
	 * @param derivType
	 * @param numberOfLines
	 */
	public static <T extends ImageGray<T>, D extends ImageGray<D>> void IP_LineDetect(String imagePath,
			Class<T> imageType, Class<D> derivType, int numberOfLines) {

		BufferedImage image = UtilImageIO.loadImage(UtilIO.pathExample(imagePath));
		T input = ConvertBufferedImage.convertFromSingle(image, null, imageType);

		DetectLineHoughPolar<T, D> detectorPolar = FactoryDetectLineAlgs.houghPolar(
				new ConfigHoughPolar(3, 30, 2, Math.PI / 180, edgeThreshold, numberOfLines), imageType, derivType);
		List<LineParametric2D_F32> found = detectorPolar.detect(input);

		DetectLineHoughFoot<T, D> detectorFoot = FactoryDetectLineAlgs
				.houghFoot(new ConfigHoughFoot(3, 8, 5, edgeThreshold, maxLines), imageType, derivType);
		DetectLineHoughFootSubimage<T, D> detectorFootsubimage = FactoryDetectLineAlgs.houghFootSub(
				new ConfigHoughFootSubimage(3, 8, 5, edgeThreshold, maxLines, 2, 2), imageType, derivType);

		ImageLinePanel gui = new ImageLinePanel();
		gui.setBackground(image);
		gui.setLines(found);
		gui.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
		// ShowImages.showWindow(VisualizeBinaryData.renderBinary(filtered, false,
		// null),"Binary",true);
		
		IP_Util_SaveBI(image,"LineDetection_output");
		
		ShowImages.showWindow(gui, "LINEZ@@", true);

	}

	/**
	 * LABEL [IP_FitEllipses] 
	 * Fit Ellipses example from BoofCV - thanks!!
	 * 
	 * Outputs
	 * "FitEllipses_output"
	 * 
	 * @param imagePath
	 */
	public static void IP_FitEllipses(String imagePath) {
		// load and convert the image into a usable format
		BufferedImage image = UtilImageIO.loadImage(UtilIO.pathExample(imagePath));
		GrayF32 input = ConvertBufferedImage.convertFromSingle(image, null, GrayF32.class);

		GrayU8 binary = new GrayU8(input.width, input.height);

		// the mean pixel value is often a reasonable threshold when creating a binary
		// image
		double mean = ImageStatistics.mean(input);

		// create a binary image by thresholding
		ThresholdImageOps.threshold(input, binary, (float) mean, true);

		// reduce noise with some filtering
		GrayU8 filtered = BinaryImageOps.erode8(binary, 1, null);
		filtered = BinaryImageOps.dilate8(filtered, 1, null);

		// Find the contour around the shapes
		List<Contour> contours = BinaryImageOps.contour(filtered, ConnectRule.EIGHT, null);

		// Fit an ellipse to each external contour and draw the results
		Graphics2D g2 = image.createGraphics();
		g2.setStroke(new BasicStroke(3));
		g2.setColor(Color.RED);

		for (Contour c : contours) {
			FitData<EllipseRotated_F64> ellipse = ShapeFittingOps.fitEllipse_I32(c.external, 0, false, null);
			VisualizeShapes.drawEllipse(ellipse.shape, g2);
		}

		IP_Util_SaveBI(ConvertBufferedImage.convertTo(filtered, null),"FitEllipses_output");
		
		ShowImages.showWindow(VisualizeBinaryData.renderBinary(filtered, false, null), "Binary", true);
		ShowImages.showWindow(image, "Ellipses", true);
	}

	/**
	 * LABEL [IP_EasySurf] 
	 * Easy SURF example from BoofCV - thanks!!
	 * 
	 * @param imagePath
	 *            string path to the image
	 */
	public static void IP_EasySURF(String imagePath) {
		// create the detector and descriptors
		BufferedImage image = UtilImageIO.loadImage(UtilIO.pathExample(imagePath));
		GrayF32 input = ConvertBufferedImage.convertFromSingle(image, null, GrayF32.class);
		DetectDescribePoint<GrayF32, BrightFeature> surf = FactoryDetectDescribe
				.surfStable(new ConfigFastHessian(0, 2, 200, 2, 9, 4, 4), null, null, GrayF32.class);

		// specify the image to process
		surf.detect(input);

		
		
		BrightFeature feature00 = surf.getDescription(0);
		//feature00.
		
		
		System.out.println("Found Features: " + surf.getNumberOfFeatures());
		System.out.println("First descriptor's first value: " + surf.getDescription(0).value[0]);
		//System.out.println("First descriptor's first value: " + surf.getDescription(0).);
	}

	/*
	 * Image Derivative
	 * 
	 * The gradient (1st order derivative) is probably the most important image
	 * derivative and is used as a first step when extracting many tyes of image
	 * features
	 * 
	 * @param String imagePath points to the image location
	 */
	public static void IP_ImageDerivative(String imagePath) {

		JFrame frame = new JFrame();
		frame.setBounds(0,0,300,300);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(new GridLayout(0, 1));
		
		BufferedImage input = UtilImageIO.loadImage(imagePath);

		// We will use floating point images here, but GrayU8 with GrayS16 for
		// derivatives also works
		GrayF32 grey = new GrayF32(input.getWidth(), input.getHeight());
		ConvertBufferedImage.convertFrom(input, grey);

		// First order derivative, also known as the gradient
		GrayF32 derivX = new GrayF32(grey.width, grey.height);
		GrayF32 derivY = new GrayF32(grey.width, grey.height);

		GImageDerivativeOps.gradient(DerivativeType.SOBEL, grey, derivX, derivY, BorderType.EXTENDED);

		// Second order derivative, also known as the Hessian
		GrayF32 derivXX = new GrayF32(grey.width, grey.height);
		GrayF32 derivXY = new GrayF32(grey.width, grey.height);
		GrayF32 derivYY = new GrayF32(grey.width, grey.height);

		GImageDerivativeOps.hessian(DerivativeType.SOBEL, derivX, derivY, derivXX, derivXY, derivYY,
				BorderType.EXTENDED);

		// There's also a built in function for computing arbitrary derivatives
		AnyImageDerivative<GrayF32, GrayF32> derivative = GImageDerivativeOps.createAnyDerivatives(DerivativeType.SOBEL,
				GrayF32.class, GrayF32.class);

		// the boolean sequence indicates if its an X or Y derivative
		derivative.setInput(grey);
		GrayF32 derivXYX = derivative.getDerivative(true, false, true);

		// visualize the results
		ListDisplayPanel gui = new ListDisplayPanel();

		gui.addImage(ConvertBufferedImage.convertTo(grey, null), "Input Grey");
		gui.addImage(VisualizeImageData.colorizeSign(derivX, null, -1), "Sobel X");
		gui.addImage(VisualizeImageData.colorizeSign(derivY, null, -1), "Sobel X");

		// Use colours to show X and Y derivatives in one image
		// Looks pretty apparently?

		gui.addImage(VisualizeImageData.colorizeGradient(derivX, derivY, -1), "Sobel X and Y");
		gui.addImage(VisualizeImageData.colorizeSign(derivXX, null, -1), "Sobel XX");
		gui.addImage(VisualizeImageData.colorizeSign(derivXY, null, -1), "Sobel XY");
		gui.addImage(VisualizeImageData.colorizeSign(derivYY, null, -1), "Sobel YY");
		gui.addImage(VisualizeImageData.colorizeSign(derivXYX, null, -1), "Sobel XYX");

		
		//Output the results to disk
		String outputPath = new String("C:\\test\\output\\output00.png");
		
		File f = new File(outputPath);
		UtilImageIO.saveImage(derivX, outputPath);
		
		IP_Util_SaveBI(ConvertBufferedImage.convertTo(derivXYX, null),"DerivXYX_output");
		
		BufferedImage outputXYX = VisualizeImageData.colorizeSign(derivXYX, null, -1);
		IP_Util_SaveBI(outputXYX, "DerivXYX_output_colourized");
		
		BufferedImage outputX = VisualizeImageData.colorizeSign(derivX, null, -1);
		IP_Util_SaveBI(outputX, "DerivX_output_colourized");
		
		BufferedImage outputY = VisualizeImageData.colorizeSign(derivY, null, -1);
		IP_Util_SaveBI(outputY, "DerivY_output_colourized");
		
		BufferedImage outputXX = VisualizeImageData.colorizeSign(derivXX, null, -1);
		IP_Util_SaveBI(outputXX, "DerivXX_output_colourized");
		
		BufferedImage outputYY = VisualizeImageData.colorizeSign(derivYY, null, -1);
		IP_Util_SaveBI(outputYY, "DerivYY_output_colourized");
		
		frame.add(gui);
		frame.setVisible(true);		
		//ShowImages.showWindow(gui, "Image Derivatives", true);
	}

	public GrayF32 IP_DerivXYX(String imagePath) {

		BufferedImage input = UtilImageIO.loadImage(imagePath);

		// We will use floating point images here, but GrayU8 with GrayS16 for
		// derivatives also works
		GrayF32 grey = new GrayF32(input.getWidth(), input.getHeight());

		// There's also a built in function for computing arbitrary derivatives
		AnyImageDerivative<GrayF32, GrayF32> derivative = GImageDerivativeOps.createAnyDerivatives(DerivativeType.SOBEL,
				GrayF32.class, GrayF32.class);

		// the boolean sequence indicates if its an X or Y derivative
		derivative.setInput(grey);
		GrayF32 derivXYX = derivative.getDerivative(true, false, true);

		return derivXYX;
	}
	
	public GrayF32 IP_DerivSobelX(String imagePath) {
		
		BufferedImage input = UtilImageIO.loadImage(imagePath);

		// We will use floating point images here, but GrayU8 with GrayS16 for
		// derivatives also works
		GrayF32 grey = new GrayF32(input.getWidth(), input.getHeight());
		
				ConvertBufferedImage.convertFrom(input, grey);

				// First order derivative, also known as the gradient
				GrayF32 derivX = new GrayF32(grey.width, grey.height);
		
		return derivX;
	}

	/**
	 * This method builds a simple JTextPane that can later be use by a manner of
	 * other methods, to be implemented.
	 * 
	 * @return _window - A Debugging readout in JTextPane form pretty much useless
	 *         without other methods such as:
	 * 
	 *         DW_AddColouredText(String text, Color colour)
	 *
	 */
	public static JTextPane DebugWindow() {

		_window = new JTextPane();
		_window.setPreferredSize(new Dimension(200, 200));

		return _window;
	}

	public static void DW_AddColouredText(String text, Color colour) {
		StyledDocument doc = _window.getStyledDocument();
		javax.swing.text.Style style = _window.addStyle("Color Style", null);
		StyleConstants.setForeground(style, colour);
		try {
			doc.insertString(doc.getLength(), "\n", style);
			doc.insertString(doc.getLength(), text, style);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

}
