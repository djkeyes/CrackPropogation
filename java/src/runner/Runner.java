package runner;

import java.io.FileNotFoundException;
import java.io.IOException;

import crackSim.core.BackingGrid;
import crackSim.core.CAUpdateCalculator;
import crackSim.core.FemBackingGrid;
import crackSim.core.MockUpdateCalculator;
import crackSim.scheduling.MockScheduler;
import crackSim.scheduling.Scheduler;
import crackSim.viz.Visualizer;

public class Runner {

	private Scheduler programSched;

	// TODO: add different parameters to specify which runtime classes we're
	// using
	public Runner() {
		// BackingGrid bg = new MockBackingGrid(20, 30);
		BackingGrid macroBg = null;
		try {
			// bg = new FemBackingGrid("YCRM.bdf");
			macroBg = new FemBackingGrid("YCRM_v10_ST_14_0313.bdf");
		} catch (FileNotFoundException e) {
			System.err.println("Could not find BDF file!");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Error reading from BDF file!");
			e.printStackTrace();
		}
		BackingGrid microBg = null;
		try {
			microBg = new FemBackingGrid("CP.bdf");
		} catch (FileNotFoundException e) {
			System.err.println("Could not find BDF file!");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Error reading from BDF file!");
			e.printStackTrace();
		}

		CAUpdateCalculator uc = new MockUpdateCalculator(macroBg);

		programSched = new MockScheduler(macroBg, microBg, uc);

		Visualizer viz = new Visualizer(macroBg, microBg);

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
