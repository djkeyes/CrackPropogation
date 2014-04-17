package crackSim.scheduling;

import java.util.LinkedList;
import java.util.List;

// performs scheduling by running processes in parallel and using the TimeWarp algorithm to resolve problems when two processes conflict
// implementation: this has a few inner classes to handle process creation
public class TimeWarpScheduler implements Scheduler {

	private List<IODevice> ioDevices;

	public TimeWarpScheduler() {

		ioDevices = new LinkedList<IODevice>();
	}

	@Override
	public void run() {
		// start by launching an initializer process
	}

	@Override
	public void addIODevice(IODevice ioDevice) {
		ioDevices.add(ioDevice);
	}

	// runs a process for making new cracks. launches CrackPropagatorProcess threads.
	private class CrackInitializerProcess implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub

		}

	}

	// runs a process for updating a CrackPropagator, sending messages, and performing rollbacks when necessary
	private class CrackPropagatorProcess implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub

		}

	}

	// runs a process for Io events; it just calculated the minimum global time and displays the macro simulation at that level.
	// at a smaller level, it shows the micro level simulatoins
	private class IoProcess implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}

	}
}
