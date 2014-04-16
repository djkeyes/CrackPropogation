package crackSim.scheduling;

import java.util.LinkedList;
import java.util.List;

// performs scheduling by running processes in parallel and using the TimeWarp algorithm to resolve problems when two processes conflict
public class TimeWarpScheduler implements Scheduler {

	private List<IODevice> ioDevices;

	public TimeWarpScheduler() {

		ioDevices = new LinkedList<IODevice>();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	@Override
	public void addIODevice(IODevice ioDevice) {
		ioDevices.add(ioDevice);
	}

}
