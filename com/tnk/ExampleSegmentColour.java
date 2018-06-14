package com.tnk;

import boofcv.alg.color.ColorHsv;
import boofcv.gui.image.ImagePanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.UtilIO;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.Planar;
import georegression.metric.UtilAngle;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * Example which demonstrates how colour can be used to segment an image. The colour
 * space is converter from RGB into HSV. HSV separators intensity from colour and
 * allows you to search for a specific colour based on two values independent of
 * lighting conditions. Other colour spaces supported, such as YUV, XYZ, and LAB.
 * @author Tom
 *
 */

public class ExampleSegmentColour {

	/**
	 * Shows a colour image and allows the user to select a pixel, convert it to
	 * HSV, print the HSV values, and calls the function below to display
	 * similar pixels.
	 */
	
	public static void printClickedColour( final BufferedImage image ) {
		ImagePanel gui = new ImagePanel(image);
		gui.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e ) {
				float[] colour = new float[3];
				int rgb = image.getRGB(e.getX(),  e.getY());
				ColorHsv.rgbToHsv((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, colour);
				System.out.println("H = " + colour[0]+" S = "+colour[1]+" V = "+colour[2]);

				showSelectedColour("Selected",image,colour[0],colour[1]);
			
			}
		});
		ShowImages.showWindow(gui,  "Colour Selector");
	}
	
	/** 
	 * Selectively displays only pixels which have a similar hue and saturation 
	 * values to what is provided. 
	 * This is intended to be a simple example of colour based segmentation.
	 * Colour based segmentation can be done in RGB colour, but more problematic
	 * due to it not being intensity invariant. More robust techniques can use
	 * Gaussian models instead of a uniform distribution, as is done below.
	 * 
	 * @param args
	 */
	
	public static void showSelectedColour( String name ,
											BufferedImage image,
											float hue,
											float saturation) {
		Planar<GrayF32> input = ConvertBufferedImage.convertFromPlanar(image,
																		null,
																		true,
																		GrayF32.class);
		Planar<GrayF32> hsv = input.createSameShape();

		// Convert into HSV
		ColorHsv.rgbToHsv_F32(input,hsv);

		// Euclidean distance squared threshold for deciding which pixels are members of the selected set
		float maxDist2 = 0.4f*0.4f;

		// Extract hue and saturation bands which are independent of intensity
		GrayF32 H = hsv.getBand(0);
		GrayF32 S = hsv.getBand(1);

		// Adjust the relative importance of Hue and Saturation.
		// Hue has a range of 0 to 2*PI and Saturation from 0 to 1.
		float adjustUnits = (float)(Math.PI/2.0);

		// Step through each pixel and mark how close it is to the selected colour
		BufferedImage output = new BufferedImage(input.width, input.height, 
												 BufferedImage.TYPE_INT_RGB);
		for ( int y = 0; y <hsv.height; y++) {
			//Hue is an angle in radians, so simple subtraction doesn't work
			for (int x=0; x <hsv.width; x++) {
				
			float dh = UtilAngle.dist(H.unsafe_get(x, y),hue);
			float ds = (S.unsafe_get(x, y)-saturation)*adjustUnits;
			
			// This distance measure is a bit naive, but good enough to
			// demonstrate the concept
			float dist2 = dh*dh + ds*ds;
			if( dist2 <= maxDist2 ) {
				output.setRGB(x,y,image.getRGB(x,y));
			}
			}
		}
		
		ShowImages.showWindow(output, "Showing "+name);
																		
	}
	
	public static void main(String args[]) {
		// TODO Auto-generated method stub
		BufferedImage image = UtilImageIO.loadImage(UtilIO.pathExample("C:\\test\\images.jpg"));

		// Let the user select a colour
		printClickedColour(image);
		//Display pre-selected colours
		showSelectedColour("Yellow", image , 1f , 1f);
		showSelectedColour("Green", image, 1.5f, 0.65f);
		showSelectedColour("other", image, 0.5f, 0.65f);
	}

}
