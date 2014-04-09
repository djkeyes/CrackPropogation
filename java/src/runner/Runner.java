package runner;

import crackSim.core.BackingGrid;
import crackSim.core.CAUpdateCalculator;
import crackSim.core.MockBackingGrid;
import crackSim.core.MockUpdateCalculator;
import crackSim.scheduling.MockScheduler;
import crackSim.scheduling.Scheduler;
import crackSim.viz.Visualizer;

public class Runner {

	private Scheduler programSched;

	// TODO: add different parameters to specify which runtime classes we're using
	public Runner() {
		BackingGrid bg = new MockBackingGrid(20, 30);
		// BackingGrid bg = new FemBackingGrid("YCRM.bdf");

		CAUpdateCalculator uc = new MockUpdateCalculator();

		programSched = new MockScheduler(bg, uc);

		Visualizer viz = new Visualizer(bg);
		viz.show();
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
