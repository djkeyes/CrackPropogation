package crackSim.scheduling;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Timer;

import crackSim.core.BackingGrid;
import crackSim.core.BackingGrid.Cell;
import crackSim.core.CAUpdateCalculator;
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

	// TODO: feed this BackingGrid into the crack initializer and CrackPropagator
	public MockScheduler(BackingGrid bg, CAUpdateCalculator uc) {
		backingGrid = bg;
		ioDevices = new LinkedList<IODevice>();
		updateCalculator = uc;
	}

	@Override
	public void run() {

		final Grid g = new Grid(backingGrid);
		
		// initial update
		for (IODevice ioDevice : ioDevices) {
			ioDevice.update(g);
		}
		
		int delay = 1000; // 1000ms
		Timer t = new Timer(delay, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.out.println("update");
				// for now, just start up the visualizer in Runner and send it a couple dummy updates.
				// TODO: schedule these updates using a CrackInitializer and CrackPropagators and a priority queue
				Cell newInitialCrack = updateCalculator.getInitialCrackPosition(g);
				Cell newPropagatedCrack = updateCalculator.getCrackUpdate(g);
				if (newInitialCrack != null)
					g.addDamaged(newInitialCrack);
				if (newPropagatedCrack != null)
					g.addDamaged(newPropagatedCrack);
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
