/**
 * 
 */
package com.tnk.util.impro;

import java.awt.image.BufferedImage;

import org.ddogleg.struct.FastQueue;
import org.ddogleg.struct.GrowQueue_I32;

import boofcv.abst.segmentation.ImageSuperpixels;
import boofcv.alg.filter.blur.GBlurImageOps;
import boofcv.alg.segmentation.ComputeRegionMeanColor;
import boofcv.alg.segmentation.ImageSegmentationOps;
import boofcv.factory.segmentation.ConfigFh04;
import boofcv.factory.segmentation.FactoryImageSegmentation;
import boofcv.factory.segmentation.FactorySegmentationAlg;
import boofcv.gui.ListDisplayPanel;
import boofcv.gui.feature.VisualizeRegions;
import boofcv.gui.image.ShowImages;
import boofcv.io.UtilIO;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.feature.ColorQueue_F32;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.GrayS32;
import boofcv.struct.image.ImageBase;
import boofcv.struct.image.ImageType;
import boofcv.struct.image.Planar;

/**
 * LABEL [Image Segmentation]
 * LABEL [LIST OF TASKS]
 * 
 * Example demonstrating high level image segmentation interface.  An image segmented using this
 * interface will have each pixel assigned a unique label from 0 to N-1, where N is the number of regions.
 * All pixels which belong to the same region are connected.  These regions are also known as superpixels.
 *
 * Thanks Peter Abeles
 *
 * @author Tom
 *
 */
public class ImageSegmentationExample {

	public ImageSegmentationExample(String imagePath) {
		// TODO Do I fill this with anything?
		
		BufferedImage image = UtilImageIO.loadImage(UtilIO.pathExample(imagePath));

		// you probably don't want to segment along the image's alpha channel and the code below assumes 3 channels
		image = ConvertBufferedImage.stripAlphaChannel(image);

		// Select input image type.  Some algorithms behave different depending on image type
		ImageType<Planar<GrayF32>> imageType = ImageType.pl(3, GrayF32.class);
//		ImageType<Planar<GrayU8>> imageType = ImageType.pl(3,GrayU8.class);
//		ImageType<GrayF32> imageType = ImageType.single(GrayF32.class);
//		ImageType<GrayU8> imageType = ImageType.single(GrayU8.class);

//		ImageSuperpixels alg = FactoryImageSegmentation.meanShift(null, imageType);
//		ImageSuperpixels alg = FactoryImageSegmentation.slic(new ConfigSlic(400), imageType);
		ImageSuperpixels alg = FactoryImageSegmentation.fh04(new ConfigFh04(100,30), imageType);
//		ImageSuperpixels alg = FactoryImageSegmentation.watershed(null,imageType);

		// Convert image into BoofCV format
		ImageBase color = imageType.createImage(image.getWidth(),image.getHeight());
		ConvertBufferedImage.convertFrom(image, color, true);

		// Segment and display results
		performSegmentation(alg,color);
		
	}
	
	
	public static <T extends ImageBase<? super T>>
	void performSegmentation( ImageSuperpixels<? super T> alg , T colour )
	{
		// Segmentation often works better after blurring the image.  Reduces high frequency image components which
		// can cause over segmentation
		GBlurImageOps.gaussian(colour, colour, 0.5, -1, null);

		// Storage for segmented image.  Each pixel will be assigned a label from 0 to N-1, where N is the number
		// of segments in the image
		GrayS32 pixelToSegment = new GrayS32(colour.width,colour.height);

		// Segmentation magic happens here
		alg.segment(colour,pixelToSegment);

		// Displays the results
		visualize(pixelToSegment,colour,alg.getTotalSuperpixels());
	}
	
	/**
	 * Visualizes results three ways.  1) Colorized segmented image where each region is given a random color.
	 * 2) Each pixel is assigned the mean color through out the region. 3) Black pixels represent the border
	 * between regions.
	 */
	public static <T extends ImageBase<? super T>>
	void visualize(GrayS32 pixelToRegion , T color , int numSegments  )
	{
		// Computes the mean color inside each region
		ImageType<? super T> type = color.getImageType();
		ComputeRegionMeanColor<? super T> colorize = FactorySegmentationAlg.regionMeanColor(type);

		FastQueue<float[]> segmentColor = new ColorQueue_F32(type.getNumBands());
		segmentColor.resize(numSegments);

		GrowQueue_I32 regionMemberCount = new GrowQueue_I32();
		regionMemberCount.resize(numSegments);

		ImageSegmentationOps.countRegionPixels(pixelToRegion, numSegments, regionMemberCount.data);
		colorize.process(color,pixelToRegion,regionMemberCount,segmentColor);

		// Draw each region using their average color
		BufferedImage outColor = VisualizeRegions.regionsColor(pixelToRegion,segmentColor,null);
		// Draw each region by assigning it a random color
		BufferedImage outSegments = VisualizeRegions.regions(pixelToRegion, numSegments, null);

		// Make region edges appear red
		BufferedImage outBorder = new BufferedImage(color.width,color.height,BufferedImage.TYPE_INT_RGB);
		ConvertBufferedImage.convertTo(color, outBorder, true);
		VisualizeRegions.regionBorders(pixelToRegion,0xFF0000,outBorder);

		// Show the visualization results
		ListDisplayPanel gui = new ListDisplayPanel();
		gui.addImage(outColor,"Color of Segments");
		gui.addImage(outBorder, "Region Borders");
		gui.addImage(outSegments, "Regions");
		ShowImages.showWindow(gui,"Superpixels", true);
	}

	/*public static void main(String[] args) {
		BufferedImage image = UtilImageIO.loadImage(UtilIO.pathExample("C:\\test\\images.jpg"));

		// you probably don't want to segment along the image's alpha channel and the code below assumes 3 channels
		image = ConvertBufferedImage.stripAlphaChannel(image);

		// Select input image type.  Some algorithms behave different depending on image type
		ImageType<Planar<GrayF32>> imageType = ImageType.pl(3, GrayF32.class);
//		ImageType<Planar<GrayU8>> imageType = ImageType.pl(3,GrayU8.class);
//		ImageType<GrayF32> imageType = ImageType.single(GrayF32.class);
//		ImageType<GrayU8> imageType = ImageType.single(GrayU8.class);

//		ImageSuperpixels alg = FactoryImageSegmentation.meanShift(null, imageType);
//		ImageSuperpixels alg = FactoryImageSegmentation.slic(new ConfigSlic(400), imageType);
		ImageSuperpixels alg = FactoryImageSegmentation.fh04(new ConfigFh04(100,30), imageType);
//		ImageSuperpixels alg = FactoryImageSegmentation.watershed(null,imageType);

		// Convert image into BoofCV format
		ImageBase color = imageType.createImage(image.getWidth(),image.getHeight());
		ConvertBufferedImage.convertFrom(image, color, true);

		// Segment and display results
		performSegmentation(alg,color);
	}*/
	
	
}
