package crackSim.viz;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Iterator;

import javax.swing.JFrame;
import javax.swing.JPanel;

import crackSim.core.BackingGrid;
import crackSim.core.CrackPropagator;
import crackSim.core.Grid;
import crackSim.scheduling.IODevice;

public class Visualizer implements IODevice {

	private VisualizerPanel topView;
	private VisualizerPanel bottomView;
	private VisualizerPanel[] microViews;

	public Visualizer(BackingGrid macroBackingGrid, BackingGrid microBackingGrid) {
		// do some JFrame boilerplate
		JFrame frame = new JFrame("Crack propagation simulator"); // title
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		topView = new VisualizerPanel(macroBackingGrid, VisualizerPanel.Camera.TOP, true);
		bottomView = new VisualizerPanel(macroBackingGrid, VisualizerPanel.Camera.BOTTOM, true);
		microViews = new VisualizerPanel[4];
		for (int i = 0; i < microViews.length; i++) {
			microViews[i] = new VisualizerPanel(microBackingGrid, VisualizerPanel.Camera.TOP, false);
		}

		JPanel macroPanel = new JPanel();
		macroPanel.setLayout(new GridLayout(1, 2));
		JPanel microPanel = new JPanel();
		microPanel.setLayout(new GridLayout(1, 4));
		frame.setLayout(new FlowLayout());
		frame.add(macroPanel);
		frame.add(microPanel);

		macroPanel.add(topView);
		macroPanel.add(bottomView);
		for(VisualizerPanel mv : microViews)
			microPanel.add(mv);
		
		// sizes
		frame.setPreferredSize(new Dimension(820, 645));
		macroPanel.setPreferredSize(new Dimension(800, 400));
		topView.setPreferredSize(new Dimension(400, 400));
		bottomView.setPreferredSize(new Dimension(400, 400));
		microPanel.setPreferredSize(new Dimension(800, 200));
		for(VisualizerPanel mv : microViews)
			mv.setPreferredSize(new Dimension(200,200));

		frame.pack();
		
		frame.setLocationByPlatform(true);
		frame.setVisible(true);

		topView.setBackground(Color.RED);
		bottomView.setBackground(Color.GREEN);
		microViews[0].setBackground(Color.BLUE);
		microViews[1].setBackground(Color.CYAN);
		microViews[2].setBackground(Color.MAGENTA);
		microViews[3].setBackground(Color.YELLOW);
	}

	@Override
	public void update(Grid g, int macroTime, Iterable<? extends CrackPropagator> ps) {
		topView.update(g, macroTime);
		bottomView.update(g, macroTime);
		
		Iterator<? extends CrackPropagator> iter = ps.iterator();
		for(int i=0; i < 4 && iter.hasNext(); i++){
			CrackPropagator p = iter.next();
			microViews[i].update(p.getGrid(), p.getCurrentTimestep());
		}
	}

}
