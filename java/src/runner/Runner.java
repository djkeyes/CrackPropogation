package runner;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.Timer;

import crackSim.core.BackingGrid;
import crackSim.core.CAUpdateCalculator;
import crackSim.core.FemBackingGrid;
import crackSim.core.MockUpdateCalculator;
import crackSim.scheduling.MockScheduler;
import crackSim.scheduling.Scheduler;
import crackSim.viz.Visualizer;

public class Runner {

	private Scheduler programSched;

	// TODO: add different parameters to specify which runtime classes we're using
	public Runner() {
		// BackingGrid bg = new MockBackingGrid(20, 30);
		BackingGrid bg = null;
		try {
			bg = new FemBackingGrid("YCRM.bdf");
		} catch (FileNotFoundException e) {
			System.err.println("Could not find BDF file!");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Error reading from BDF file!");
			e.printStackTrace();
		}

		CAUpdateCalculator uc = new MockUpdateCalculator(bg);

		programSched = new MockScheduler(bg, uc);

		// do some JFrame boilerplate
		JFrame frame = new JFrame("Crack propagation simulator"); // title
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Visualizer top = new Visualizer(bg, Visualizer.Camera.TOP);
		Visualizer down = new Visualizer(bg, Visualizer.Camera.BOTTOM);

		frame.setLayout(new GridLayout(1, 2));
		frame.add(top);
		frame.add(down);
		frame.setSize(800, 600);
		top.setSize(400, 600);
		down.setSize(400, 600);
		frame.setLocationByPlatform(true);
		frame.setVisible(true);
		programSched.addIODevice(top);
		programSched.addIODevice(down);
	}

	public void run() {
		programSched.run();
	}

	public static void main(String[] args) {
		Runner simRunner = new Runner();
		simRunner.run();

	}
}
