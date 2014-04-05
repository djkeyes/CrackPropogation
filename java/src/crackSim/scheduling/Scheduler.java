package crackSim.scheduling;

import crackSim.viz.Visualizer;

// TODO: feel free to make this an abstract class, ie if you want to put a List<CrackPropagator> variable in here.
public interface Scheduler {

	// TODO: should this have a public initialize method? or should run() just initialize everything first?
	public void run();

	// add an io device (like a visualizer).
	public void addIODevice(IODevice ioDevice);
}
