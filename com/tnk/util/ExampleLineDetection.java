package com.tnk.util;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.List;

import boofcv.abst.feature.detect.line.DetectLineHoughFoot;
import boofcv.abst.feature.detect.line.DetectLineHoughFootSubimage;
import boofcv.abst.feature.detect.line.DetectLineHoughPolar;
import boofcv.factory.feature.detect.line.ConfigHoughFoot;
import boofcv.factory.feature.detect.line.ConfigHoughFootSubimage;
import boofcv.factory.feature.detect.line.ConfigHoughPolar;
import boofcv.factory.feature.detect.line.FactoryDetectLineAlgs;
import boofcv.gui.ListDisplayPanel;
import boofcv.gui.feature.ImageLinePanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.UtilIO;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayS16;
import boofcv.struct.image.GrayU8;
import boofcv.struct.image.ImageGray;
import georegression.struct.line.LineParametric2D_F32;

public class ExampleLineDetection {
	
	/**
	 * Adjusts edge threshold for identifying pixels
	 * belonging to a line
	 */
	private static final float edgeThreshold = 25;
	/**
	 * Adjust the maximum number of found lines in
	 * an image
	 */
	private static final int maxLines = 10;
	
	private static ListDisplayPanel listPanel = new ListDisplayPanel();
	
	
	/**
	 * Detects lines inside the image using different types of Hough detectors
	 * 
	 * 
	 * @param image Input image
	 * @param imagetype type of image processed by line detector
	 * @param derivType Type of image derivative
	 */
	
	public static<T extends ImageGray<T>, D extends ImageGray<D>>
	void detectLines( BufferedImage image , 
					  Class<T> imageType ,
					  Class<D> derivType )
{
		// convert the line into a single band
		T input = ConvertBufferedImage.convertFromSingle(image, null, imageType);
		
		// Comment/uncomment to try a different type of line detector
		DetectLineHoughPolar<T,D> detectorPolar = FactoryDetectLineAlgs.houghPolar(
				new ConfigHoughPolar(3,30,2, Math.PI / 180,edgeThreshold, maxLines), imageType, derivType);
		DetectLineHoughFoot<T,D> detectorFoot = FactoryDetectLineAlgs.houghFoot(
				new ConfigHoughFoot(3,8,5, edgeThreshold,maxLines),imageType, derivType );
		DetectLineHoughFootSubimage<T,D> detectorFootsubimage = FactoryDetectLineAlgs.houghFootSub(
				new ConfigHoughFootSubimage(3,8,5, edgeThreshold, maxLines,2,2), imageType, derivType);
		
		List<LineParametric2D_F32> found = detectorPolar.detect(input);
		
		//display the results
		ImageLinePanel gui = new ImageLinePanel();
		gui.setBackground(image);
		gui.setLines(found);
		gui.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
		
		listPanel.addItem(gui, "Found Lines");
		
}
	
	
	public static void main( String args[] ) {
		BufferedImage input = UtilImageIO.loadImage(UtilIO.pathExample("C:\\Users\\Tom\\Pictures\\download.jpg"));
		
		detectLines(input, GrayU8.class, GrayS16.class);
		
		ShowImages.showWindow(listPanel, "Detected Lines", true);
	}
}
	