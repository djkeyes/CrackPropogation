package crackSim.scheduling;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import crackSim.core.BackingGrid;
import crackSim.core.CAUpdateCalculator;
import crackSim.core.CrackInitializer;
import crackSim.core.CrackPropagator;
import crackSim.core.Grid;
import crackSim.core.ReversableCrackPropagator;

// performs scheduling by running processes in parallel and using the TimeWarp algorithm to resolve problems when two processes conflict
// implementation: this has a few inner classes to handle process creation
public class TimeWarpScheduler implements Scheduler {

	// limit the computation to only use this many cracks/processes: 
	private static final int MAX_NUMBER_CRACKS = 100;
	
	private List<IODevice> ioDevices;

	private Collection<ReversableCrackPropagator> propagators;

	private CrackInitializerProcess cip;
	private Collection<CrackPropagatorProcess> cpps;
	private IoUpdaterProcess iup;
	// this grid should be updated to match the global minimum simulation time
	private BackingGrid macroBackingGrid;
	private Grid minimumTimeGrid;
	private int minimumTime;

	public TimeWarpScheduler(BackingGrid macroBackingGrid, BackingGrid microBackingGrid, CAUpdateCalculator uc) {
		ioDevices = new LinkedList<IODevice>();

		// initializer = new CrackInitializer(uc);
		// each crack has its own micro-scale grid. feed this grid the initializer.
		CrackInitializer initializer = new CrackInitializer(uc, microBackingGrid);

		// the only operations we ever do on these are add and read operations. so use a LinkedBlockingQueue for the easy adding
		// and no ConcurrentModificationExceptions.
		propagators = new LinkedBlockingQueue<ReversableCrackPropagator>();
		cpps = new LinkedBlockingQueue<CrackPropagatorProcess>();

		cip = new CrackInitializerProcess(initializer, macroBackingGrid);
		iup = new IoUpdaterProcess();

		this.macroBackingGrid = macroBackingGrid;
		minimumTimeGrid = new Grid(macroBackingGrid);
		minimumTime = 0;

		// initial update
		for (IODevice ioDevice : ioDevices) {
			ioDevice.update(minimumTimeGrid, 0, propagators);
		}

	}

	@Override
	public void run() {
		// start up some processes
		new Thread(cip).start();
		new Thread(iup).start();
	}

	@Override
	public void addIODevice(IODevice ioDevice) {
		ioDevices.add(ioDevice);
	}

	// runs a process for making new cracks. launches CrackPropagatorProcess threads.
	private class CrackInitializerProcess implements Runnable {

		private CrackInitializer initializer;
		// unlike the macro-level grid used for IO, this one runs as fast as possible. It contains a damaged cell for every
		// CrackPropagator created.
		private Grid fastMacroGrid;

		public CrackInitializerProcess(CrackInitializer initializer, BackingGrid bg) {
			this.initializer = initializer;
			this.fastMacroGrid = new Grid(bg);
		}

		@Override
		public void run() {
			// just keep launching new cracks
			while (true) {

				ReversableCrackPropagator nextInitialCrack = initializer.createNextReversableCrack(fastMacroGrid);
				if (propagators.size() < MAX_NUMBER_CRACKS) {
					propagators.add(nextInitialCrack);
					CrackPropagatorProcess cpp = new CrackPropagatorProcess(nextInitialCrack);
					cpps.add(cpp);

					fastMacroGrid.addDamaged(nextInitialCrack.getInitialCrackLocation());

					new Thread(cpp).start();
				}
			}
		}

	}

	// runs a process for updating a CrackPropagator, sending messages, and performing rollbacks when necessary
	private class CrackPropagatorProcess implements Runnable {

		private ReversableCrackPropagator crack;

		public CrackPropagatorProcess(ReversableCrackPropagator initialCrack) {
			this.crack = initialCrack;
		}

		@Override
		public void run() {
			while (true) {
				// check for any conflicts
				// TODO

				// then update
				crack.update();
			}

		}

	}

	// runs a process for IO events; it just calculated the minimum global time and displays the macro simulation at that level.
	// at a smaller level, it shows the micro level simulatoins
	private class IoUpdaterProcess implements Runnable {

		@Override
		public void run() {
			while (true) {

				// we could mantain the minimum time and minimumtimegrid in a clever way, but this way is easier to code:
				// just check every propagator on every timestep. This also means we don't have to synchronize during window painting.
				minimumTime = Integer.MAX_VALUE;
				minimumTimeGrid = new Grid(macroBackingGrid);
				for (CrackPropagator p : propagators) {
					minimumTime = Math.min(minimumTime, p.getCurrentTimestep());
				}
				if (minimumTime < Integer.MAX_VALUE) {
					for (ReversableCrackPropagator p : propagators) {
						if (p.getStartTime() <= p.getCurrentTimestep()) {
							minimumTimeGrid.addDamaged(p.getInitialCrackLocation());
						}
					}
				}
				if(minimumTime == Integer.MAX_VALUE) minimumTime = 0;

				// then just keep calling for updates
//				try {
//					// wait a brief amount each time
//					Thread.sleep(100);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				for (IODevice ioDevice : ioDevices) {
					// System.out.println("redraw");
					ioDevice.update(minimumTimeGrid, minimumTime, propagators);
				}

			}

		}

	}
}
