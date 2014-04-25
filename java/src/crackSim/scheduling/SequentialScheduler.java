package crackSim.scheduling;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Timer;

import crackSim.core.BackingGrid;
import crackSim.core.CAUpdateCalculator;
import crackSim.core.Cell4D;
import crackSim.core.CrackInitializer;
import crackSim.core.CrackPropagator;
import crackSim.core.Grid;
import crackSim.core.ReversableCrackPropagator;

/**
 * Sequential scheduler. Rather than allowing propagators to run independently, this just calls them in sequential order. It is a
 * sequential, rather than parallel, scheduler.
 * 
 * This runs *at* simulation time, because it calls each process update as-fast-as-possible.
 * 
 * IO device calls are dispatched in parallel, so that we have a good basis for comparison of the core processes.
 */
public class SequentialScheduler implements Scheduler {

	private BackingGrid macroBackingGrid;
	private List<IODevice> ioDevices;

	private CrackInitializer initializer;
	private List<CrackPropagator> propagators;

	private int globalTime;
	private Grid globalTimeGrid;

	// buffer to store the next crack update
	private CrackPropagator nextInitialCrack = null;

	public SequentialScheduler(BackingGrid macroBackingGrid, BackingGrid microBackingGrid, CAUpdateCalculator uc) {
		ioDevices = new LinkedList<IODevice>();
		this.macroBackingGrid = macroBackingGrid;
		this.globalTimeGrid = new Grid(macroBackingGrid);

		// initializer = new CrackInitializer(uc);
		// each crack has its own micro-scale grid. feed this grid the initializer.
		initializer = new CrackInitializer(uc, microBackingGrid);

		// I'm using an array list for east of lookup when checking for conflicting cracks
		// Using a linked list would probably be faster, but it would require more coding to identify and remove processes while
		// concurrently iterating through the list.
		propagators = new ArrayList<CrackPropagator>();
		globalTime = 0;

		// add an initial crack
		propagators.add(initializer.createNextCrack(new Grid(macroBackingGrid)));
	}

	@Override
	public void run() {

		// initial update
		for (IODevice ioDevice : ioDevices) {
			ioDevice.update(globalTimeGrid, 0, propagators);
		}

		new Thread(new IoUpdaterProcess()).start();

		while (true) {
			// System.out.println("update--time=" + globalTime);

			int nextTime = Integer.MAX_VALUE;

			if (nextInitialCrack == null) {
				nextInitialCrack = initializer.createNextCrack(globalTimeGrid);
			}
			while (nextInitialCrack != null && nextInitialCrack.getCurrentTimestep() <= globalTime) {
				propagators.add(nextInitialCrack);
				globalTimeGrid.addDamaged(nextInitialCrack.getInitialCrackLocation());

				nextInitialCrack = initializer.createNextCrack(globalTimeGrid);
			}

			for (CrackPropagator prop : propagators) {
				if (globalTime >= prop.getNextTimestep()) {
					prop.update();
				}
				nextTime = Math.min(nextTime, prop.getNextTimestep());
			}

			// also check for conflicts
			for (CrackPropagator first : propagators) {
				for (CrackPropagator second : propagators) {
					if (first == second)
						continue;

					if (first.conflictsWith(second)) {
						first.affectAdjacent(second);
						second.affectAdjacent(first);
					}
				}
			}

			globalTime = nextTime;
		}

	}

	@Override
	public void addIODevice(IODevice ioDevice) {
		ioDevices.add(ioDevice);
	}

	private class IoUpdaterProcess implements Runnable {

		private List<CrackPropagator> propagatorsCopy = new LinkedList<CrackPropagator>();

		@Override
		public void run() {
			while (true) {
				if (propagatorsCopy.size() < propagators.size()) {
					propagatorsCopy = new LinkedList<CrackPropagator>(propagators);
				}
				for (IODevice ioDevice : ioDevices) {
					ioDevice.update(globalTimeGrid, globalTime, propagatorsCopy);
				}

			}

		}

	}

}
