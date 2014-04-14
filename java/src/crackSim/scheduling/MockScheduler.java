package crackSim.scheduling;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Timer;

import crackSim.core.BackingGrid;
import crackSim.core.BackingGrid.Cell;
import crackSim.core.CAUpdateCalculator;
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

	private BackingGrid backingGrid;
	private List<IODevice> ioDevices;
	private CAUpdateCalculator updateCalculator;

	private CrackInitializer initializer;
	private List<CrackPropagator> propagators;

	// TODO: feed this BackingGrid into the crack initializer and CrackPropagator
	public MockScheduler(BackingGrid bg, CAUpdateCalculator uc) {
		ioDevices = new LinkedList<IODevice>();
		backingGrid = bg;
		updateCalculator = uc;

		initializer = new CrackInitializer(uc);
		// I'm using an array list for east of lookup when checking for conflicting cracks
		// Using a linked list would probably be faster, but it would require more coding to identify and remove processes while
		// concurrently iterating through the list.
		propagators = new ArrayList<CrackPropagator>();
	}

	@Override
	public void run() {

		final Grid g = new Grid(backingGrid);

		// initial update
		for (IODevice ioDevice : ioDevices) {
			ioDevice.update(g);
		}

		// this just runs on a clock, but it could be put inside a while-loop instead to run as-fast-as-possible
		int delay = 1; // in milliseconds
		Timer t = new Timer(delay, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.out.println("update");
				// for now, just start up the visualizer in Runner and send it a couple dummy updates.
				// Cell newInitialCrack = updateCalculator.getInitialCrackPosition(g);
				CrackPropagator newCrack = initializer.createNextCrack(g);
				if (newCrack != null){
					propagators.add(newCrack);
				}

				for (CrackPropagator prop : propagators) {
					prop.update();
					// TODO: show a small visualization for each propagator
					// instead of this:
					g.addDamaged(prop.getGrid());
				}
				for (int i = 0; i < propagators.size(); i++) {
					// protip: we're going through j in backwards order to make removal easier
					// for(j=N-1; j > 0; j--)
					// ....if(condition)
					// .........list.remove(j)
					// this won't accidentally skip elements or have weird out-of-bounds exceptions, which you'd need to check for if
					// you went in forwards order.
					for (int j = propagators.size() - 1; j > i; j--) {
						CrackPropagator first = propagators.get(i);
						CrackPropagator second = propagators.get(j);
						// if there's a conflict, merge the processes
						if (first.conflictsWith(second)) {
							first.merge(second);
							propagators.remove(j);
						}
					}
				}
				for (IODevice ioDevice : ioDevices) {
					ioDevice.update(g);
				}
				
			}
		});
		t.start();
	}

	@Override
	public void addIODevice(IODevice ioDevice) {
		ioDevices.add(ioDevice);
	}

}
