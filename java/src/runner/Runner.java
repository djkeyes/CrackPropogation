package runner;

import java.awt.GridLayout;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFrame;

import crackSim.core.BackingGrid;
import crackSim.core.CAUpdateCalculator;
import crackSim.core.FemBackingGrid;
import crackSim.core.MockUpdateCalculator;
import crackSim.scheduling.MockScheduler;
import crackSim.scheduling.Scheduler;
import crackSim.viz.Visualizer;
import crackSim.viz.VisualizerPanel;

public class Runner {

	private Scheduler programSched;

	// TODO: add different parameters to specify which runtime classes we're
	// using
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
		
		Visualizer viz = new Visualizer(bg);

		programSched.addIODevice(viz);
	}

	public void run() {
		programSched.run();
	}

	public static void main(String[] args) {
		Runner simRunner = new Runner();
		simRunner.run();

	}
}
