package crackSim.scheduling;

import crackSim.core.BackingGrid;

/**
 * Mock scheduler. Rather than allowing propagators to run independently, this just calls them in sequential order. It is a sequential,
 * rather than parallel, scheduler.
 * 
 * This is convenient because there are no anti-messages needed for now.
 */
public class MockScheduler implements Scheduler {

	// TODO: feed this BackingGrid into the crack initializer and CrackPropagator
	public MockScheduler(BackingGrid bg) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addIODevice(IODevice ioDevice) {
		// TODO Auto-generated method stub
		
	}

}
