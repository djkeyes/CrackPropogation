package crackSim.scheduling;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import crackSim.core.BackingGrid;
import crackSim.core.CAUpdateCalculator;
import crackSim.core.CrackInitializer;
import crackSim.core.CrackPropagator;
import crackSim.core.Grid;

// performs scheduling by running processes in parallel and using the TimeWarp algorithm to resolve problems when two processes conflict
// implementation: this has a few inner classes to handle process creation
public class TimeWarpScheduler implements Scheduler {

	private List<IODevice> ioDevices;
	private List<CrackPropagator> propagators;

	public TimeWarpScheduler(BackingGrid macroBackingGrid, BackingGrid microBackingGrid, CAUpdateCalculator uc) {
		ioDevices = new LinkedList<IODevice>();

		// initializer = new CrackInitializer(uc);
		// each crack has its own micro-scale grid. feed this grid the initializer.
		CrackInitializer initializer = new CrackInitializer(uc, microBackingGrid);


		final Grid g = new Grid(macroBackingGrid);

		// initial update
		for (IODevice ioDevice : ioDevices) {
			ioDevice.update(g, 0, propagators);
		}
		
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
			
			// initialization
			
			
			
			// start up some processes
			IoProcess iop = null;
			CrackInitializerProcess cip = null;
			
			
			new Thread(cip).start();
			new Thread(iop).start();
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
