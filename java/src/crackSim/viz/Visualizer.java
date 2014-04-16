package crackSim.viz;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JPanel;

import crackSim.core.BackingGrid;
import crackSim.core.Grid;
import crackSim.scheduling.IODevice;

public class Visualizer implements IODevice {

	private VisualizerPanel top;
	private VisualizerPanel bottom;

	public Visualizer(BackingGrid bg) {
		// do some JFrame boilerplate
		JFrame frame = new JFrame("Crack propagation simulator"); // title
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		top = new VisualizerPanel(bg, VisualizerPanel.Camera.TOP);
		bottom = new VisualizerPanel(bg, VisualizerPanel.Camera.BOTTOM);
		JPanel[] placeholders = new JPanel[4];
		for (int i = 0; i < 4; i++) {
			placeholders[i] = new JPanel();
			placeholders[i].setSize(200, 200);
			placeholders[i].setBackground(Arrays.asList(Color.MAGENTA,
					Color.CYAN, Color.YELLOW, Color.RED).get(i));
		}

		JPanel macroPanel = new JPanel();
		macroPanel.setLayout(new GridLayout(1, 2));
		JPanel microPanel = new JPanel();
		microPanel.setLayout(new GridLayout(1, 4));
		frame.add(macroPanel);
		frame.add(microPanel);

		macroPanel.add(top);
		macroPanel.add(bottom);
		for (int i = 0; i < 4; i++)
			microPanel.add(placeholders[i]);

		// sizes
		frame.setSize(800, 600);
		macroPanel.setSize(800, 400);
		top.setSize(400, 400);
		bottom.setSize(400, 400);
		microPanel.setSize(800, 200);
		frame.setLocationByPlatform(true);
		frame.setVisible(true);
	}

	@Override
	public void update(Grid g) {
		top.update(g);
		bottom.update(g);
	}

}
