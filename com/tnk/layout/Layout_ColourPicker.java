package com.tnk.layout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import net.miginfocom.swing.MigLayout;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Panel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JSlider;

import boofcv.alg.color.ColorHsv;
import boofcv.gui.image.ImagePanel;
import boofcv.gui.image.ShowImages;
import boofcv.io.UtilIO;
import boofcv.io.image.ConvertBufferedImage;
import boofcv.io.image.UtilImageIO;
import boofcv.struct.image.GrayF32;
import boofcv.struct.image.Planar;
import georegression.metric.UtilAngle;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.ScrollPane;

public class Layout_ColourPicker extends JPanel {

	private JFrame frame;
	private static JScrollPane scrollPane;
	private static ImagePanel outputImagePanel;
	private JSlider slider;
	private JSlider slider_1;
	private JPanel panel;
	private JPanel panel_1;

	/**
	 * Create the panel.
	 */
	public Layout_ColourPicker() {
		
		InitializeLayout();
		Initialize();

	}
	
	private void InitializeLayout() {
		frame = new JFrame();
		frame.setBounds(0, 0, 800, 600); // for my small ass monitor :'(
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(new MigLayout("", "[grow][][grow][grow]", "[grow][][grow]"));

		panel_1 = new JPanel();
		
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.PAGE_AXIS));

		JLabel lblHue = new JLabel("Hue");
		panel_1.add(lblHue);

		slider = new JSlider();
		panel_1.add(slider);

		JLabel lblSaturation = new JLabel("Saturation");
		panel_1.add(lblSaturation);

		slider_1 = new JSlider();
		panel_1.add(slider_1);

		JButton btnNewButton = new JButton("New button");
		panel_1.add(btnNewButton);

		scrollPane = new JScrollPane();
		
		frame.getContentPane().add(panel_1, "cell 0 0,grow");
		frame.getContentPane().add(scrollPane, "cell 1 0,grow");
	}

	public void Initialize() {

		// this will be used for populating the portions of the layout
		slider.setMinimum(0);
		slider.setMaximum(1);

		slider_1.setMinimum(0);
		slider_1.setMaximum(1);
		
		outputImagePanel = new ImagePanel();
		
		BufferedImage image = UtilImageIO.loadImage(UtilIO.pathExample("C:\\test\\images.jpg"));

		// Let the user select a colour
		//printClickedColour(image);
		//Display pre-selected colours
		showSelectedColour("other", image, 0.5f, 0.65f);
		

	}

	public static void printClickedColour(final BufferedImage image) {
		ImagePanel gui = new ImagePanel(image);
		gui.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				float[] color = new float[3];
				int rgb = image.getRGB(e.getX(), e.getY());
				ColorHsv.rgbToHsv((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, color);
				System.out.println("H = " + color[0] + " S = " + color[1] + "V = " + color[2]);

				// LABEL NOTE
				// This is ideally how the segmentation is called
				// but RGB is intensity invariant so we have to drop one
				// layer of colour information
				// showSelectedColour("Selected", image,color[0],color[1],color[2]);

				showSelectedColour("selected", image, color[0], color[1]);
			}

			/**
			 * Selectively displays only pixels which have a similar hue and saturation
			 * values to what is provided. This is intended to be a simple example of colour
			 * based segmentation . Colour based segmentation can be done in RGB colour, but
			 * is more problematic due to it not being intensity invariant. More robust
			 * techniques can use Gaussian models instead of a uniform distribution, as is
			 * done below.
			 */
			
		});

	}
	public static void showSelectedColour(String name, BufferedImage image, float hue, float saturation) {
		Planar<GrayF32> input = ConvertBufferedImage.convertFromPlanar(image, null, true, GrayF32.class);

		Planar<GrayF32> hsv = input.createSameShape();

		// Convert into HSV
		ColorHsv.rgbToHsv_F32(input, hsv);

		// Eculidean distance squared threshold for deciding
		// which pixels are members of the selected set
		float maxDist2 = 0.4f * 0.4f;

		// Extract hue and saturation bands which are independent of intensity
		GrayF32 H = hsv.getBand(0);
		GrayF32 S = hsv.getBand(1);

		// Adjust the relative importance of Hue and Saturation
		// Hue has a range of 0 to 2Pi and Saturation from 0 to 1.
		float adjustUnits = (float) (Math.PI / 2.0);

		// Step through each pixel and mark how close it is to the selected
		// colour
		BufferedImage output = new BufferedImage(input.width, input.height, BufferedImage.TYPE_INT_RGB);

		for (int t = 0; t < hsv.height; t++) {
			// Hue has an angle in radians, so simple -tion doesn't work
			for (int x = 0; x < hsv.width; x++) {
				float dh = UtilAngle.dist(H.unsafe_get(x, t), hue);
				float ds = (S.unsafe_get(x, t) - saturation) * adjustUnits;

				// LABEL NOTE!
				// This distance measures is a bit naive, but good enough
				// to demonstrate the concept
				float dist2 = dh * dh + ds * ds;
				if (dist2 <= maxDist2) {
					output.setRGB(x, t, image.getRGB(x, t));
				}

			}
		}
		
		//ShowImages.showWindow(output, "Showing "+name);

		outputImagePanel = new ImagePanel(output);
		JPanel newPanel = new JPanel();
		newPanel.add(outputImagePanel);
		scrollPane.setViewportView(newPanel);

	}


	public static void main(String arg[]) {

		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Layout_ColourPicker window = new Layout_ColourPicker();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
		
	}
}
