package crackSim.viz;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import crackSim.core.Grid;
import crackSim.scheduling.IODevice;

/**
 * This creates the GUI which visually represents the simulation.
 * This is shamelessly copies from the previous project in the hopes that some of the code is portable.
 * 
 * @author Daniel Keyes
 */
public class Visualizer implements IODevice {


	private JLabel view;
	private BufferedImage surface;
	
	/**
	 * Constructor that initializes all the elements defined above. 
	 */
	public Visualizer(){
		
		surface = new BufferedImage(600,600,BufferedImage.TYPE_INT_RGB);
        	view = new JLabel(new ImageIcon(surface));
	}

	private class VisualizationHandler {
		public void onUpdate() {
	        Graphics g = surface.getGraphics();		//Create a new instance of Graphics (GUI platform)
	        
	        int width = surface.getWidth();
	        int height = surface.getHeight();
	        
	        // do things with g
	        
	        view.repaint(); //repaint the simulation with updated elements
		}
	}

	public void show() {
		// do some JFrame boilerplate
		JFrame frame = new JFrame("Crack propagation simulator"); //title
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// I'm backing this with a JPanel containing a Bufferent Image. We could probably also just use a
		// JApplet, or a Canvas, or something. Java GUIs aren't my forte.

        frame.setSize(600, 600);
        frame.setContentPane(view);
        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);
	}

	@Override
	public void update(Grid g) {
		// TODO call VisualizationHandler.onUpdate() whenever we get an update
	}

}
