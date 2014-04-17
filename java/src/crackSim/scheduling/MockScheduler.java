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

/**
 * Mock scheduler. Rather than allowing propagators to run independently, this just calls them in sequential order. It is a sequential,
 * rather than parallel, scheduler.
 * 
 * This is convenient because there are no anti-messages needed for now.
 */
public class MockScheduler implements Scheduler {

	private BackingGrid macroBackingGrid;
	private List<IODevice> ioDevices;

	private CrackInitializer initializer;
	private List<CrackPropagator> propagators;

	private int globalTime;
	private CrackPropagator nextInitialCrack = null;

	public MockScheduler(BackingGrid macroBackingGrid, BackingGrid microBackingGrid, CAUpdateCalculator uc) {
		ioDevices = new LinkedList<IODevice>();
		this.macroBackingGrid = macroBackingGrid;

		// initializer = new CrackInitializer(uc);
		// each crack has its own micro-scale grid. feed this grid the initializer.
		initializer = new CrackInitializer(uc, microBackingGrid);

		// I'm using an array list for east of lookup when checking for conflicting cracks
		// Using a linked list would probably be faster, but it would require more coding to identify and remove processes while
		// concurrently iterating through the list.
		propagators = new ArrayList<CrackPropagator>();
		globalTime = 0;
	}

	@Override
	public void run() {

		final Grid g = new Grid(macroBackingGrid);

		// initial update
		for (IODevice ioDevice : ioDevices) {
			ioDevice.update(g, propagators);
		}

		// this just runs on a clock, but it could be put inside a while-loop instead to run as-fast-as-possible
		int delay = 1; // in milliseconds
		Timer t = new Timer(delay, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.out.println("update--time=" + globalTime);
//				System.out.println("propagators list:");
//				for (CrackPropagator p : propagators) {
//					System.out.println(p + ": t=" + p.getCurrentTimestep() + ", n=" + p.getNextTimestep());
//				}
				
				// for now, just start up the visualizer in Runner and send it a couple dummy updates.
				// Cell newInitialCrack = updateCalculator.getInitialCrackPosition(g);
				// note: the initializer does not run in simulation time--it runs *faster than* simulation time. If we call
				// createNextCrack() at timesteps 1, 2, and 3, it might return a crack that starts at timesteps 100, 500, and 10000.
				// This doesn't make our simulation less accurate, it just means we'll be store a lot of cracks that might never be
				// relevant. TODO: fix that.
				if (nextInitialCrack == null) {
					nextInitialCrack = initializer.createNextCrack(g);
				}
				if (nextInitialCrack != null && nextInitialCrack.getCurrentTimestep() <= globalTime) {
					propagators.add(nextInitialCrack);
					g.addDamaged(nextInitialCrack.getInitialCrackLocation());
					nextInitialCrack = null;
				}

				for (CrackPropagator prop : propagators) {
					if (globalTime == prop.getNextTimestep()) {
						prop.update();
					}
				}
				for (int i = 0; i < propagators.size(); i++) {
					for (int j = propagators.size() - 1; j > i; j--) {
						CrackPropagator first = propagators.get(i);
						CrackPropagator second = propagators.get(j);
						// if there's a conflict, merge the processes
						// Since we're running these as a single process for the MockScheduler, the times are already sorted by next
						// update, so no need to perform a time check or rollbacks
						if (first.conflictsWith(second)) {
							// this method affects both of them mutually, so
							// first.affectAdjacent(second)
							// is the same as
							// second.affectAdjacent(first)
							first.affectAdjacent(second);
						}
					}
				}
				for (IODevice ioDevice : ioDevices) {
					ioDevice.update(g, propagators);
				}

				globalTime++;
			}
		});
		t.start();
	}

	@Override
	public void addIODevice(IODevice ioDevice) {
		ioDevices.add(ioDevice);
	}

}
