package com.tnk.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;

import boofcv.io.image.UtilImageIO;

/**
 * Send a BufferedImage and a name for the file.
 * This guy will output the image to a .png in the output folder
 * 
 * @author Tom
 *
 */
public class SaveBIOutput implements Runnable{

	private String TAG = "SaveBIOutput";
	private String outputName;
	private BufferedImage bimg;
	
	public SaveBIOutput(BufferedImage bi, String selectedPath) {
		outputName = selectedPath;
		bimg = bi;
	}
	
	@Override
	public void run() {
		Date outputDate = new Date();
		String outputPath = new String("C:\\test\\output\\"+
				outputDate.getMonth()+"-"+
				outputDate.getDate()+" " +
				outputDate.getHours()+"h"+
				outputDate.getMinutes()+"m "+
				outputName + ".png");
		File f = new File(outputPath);
		UtilImageIO.saveImage(bimg, outputPath);
		System.out.println(TAG + " Saved " + outputPath);		
	}	
}
