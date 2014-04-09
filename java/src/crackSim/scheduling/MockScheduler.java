package crackSim.scheduling;

import java.util.LinkedList;
import java.util.List;

import crackSim.core.BackingGrid;

/**
 * Mock scheduler. Rather than allowing propagators to run independently, this just calls them in sequential order. It is a sequential,
 * rather than parallel, scheduler.
 * 
 * This is convenient because there are no anti-messages needed for now.
 */
public class MockScheduler implements Scheduler {

	private BackingGrid backingGrid;
	private List<IODevice> ioDevices;

	// TODO: feed this BackingGrid into the crack initializer and CrackPropagator
	public MockScheduler(BackingGrid bg) {
		backingGrid = bg;
		ioDevices = new LinkedList<IODevice>();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

		// for now, just start up the visualizer in Runner and send it a couple dummy updates.
		for (IODevice ioDevice : ioDevices) {
			// TODO
			ioDevice.update(null);
		}
	}

	@Override
	public void addIODevice(IODevice ioDevice) {
		ioDevices.add(ioDevice);
	}

}
