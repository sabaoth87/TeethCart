package com.tnk;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import boofcv.alg.color.ColorHsv;
import boofcv.gui.image.ImageZoomPanel.ImagePanel;

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

				showSelectedColor("Selected",image,colour[0],colour[1]);
			
			}
		});
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
