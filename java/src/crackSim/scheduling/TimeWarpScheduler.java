package crackSim.scheduling;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

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
	private static final int MAX_NUMBER_CRACKS = 50;

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
				if (nextInitialCrack == null) {
					break;
				}
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

	private static int n = 0;

	// runs a process for updating a CrackPropagator, sending messages, and performing rollbacks when necessary
	private class CrackPropagatorProcess implements Runnable {

		@Override
		public String toString() {
			return "[TWScheduler " + id + "]";
		}

		private int id;

		private ReversableCrackPropagator crack;
		private PriorityBlockingQueue<Message> messageQueue; // automagically sorted
		private LinkedList<Message> oldMessages; // not sorted, so always store these in the right order
		private Set<CrackPropagatorProcess> conflictMessageSent;

		public CrackPropagatorProcess(ReversableCrackPropagator initialCrack) {
			id = ++n;
			this.crack = initialCrack;
			this.messageQueue = new PriorityBlockingQueue<Message>();
			this.oldMessages = new LinkedList<Message>();

			this.conflictMessageSent = new HashSet<CrackPropagatorProcess>();

			// enqueue the first update
			Message m = new Message(crack.getNextTimestep(), /* sender */this, /* receiver */this, /* isAntiMessage */false);
			m.isUpdate = true;
			messageQueue.add(m);
		}

		@Override
		public void run() {
			while (true) {
				// pop the next action from the queue
				if (messageQueue.isEmpty())
					continue;

				Message cur = messageQueue.remove();

				if (cur.isConflict) {
					// if it's a conflict message, we may need to do rollbacks
					// check if we're too far ahead and time
					// ideally, the next timestamp should be AFTER the conflict
					// but the current timestamp should be BEFORE the conflict
					boolean nextStepAfter = crack.getNextTimestep() >= cur.timestamp;
					boolean curStepBefore = cur.timestamp >= crack.getCurrentTimestep();
					System.out.println(id + ": rolling back--conflict message sent at t=" + cur.timestamp + " from " + cur.senderCrack
							+ " to " + cur.receiverCrack);
					System.out.println(id + ": this is a" + (cur.isAntiMessage ? "n anti" : " normal ") + "message");
					System.out.println(id + ": this process is " + this + ", started at t=" + this.crack.getStartTime());

					while (!oldMessages.isEmpty() && (!nextStepAfter || !curStepBefore)) {
						// roll back one message
						Message earlier = oldMessages.removeLast();
						System.out.println(id + ": earlier message sent at t=" + earlier.timestamp);
						System.out.println(id + ": current crack time - " + this.crack.getCurrentTimestep());
						messageQueue.add(earlier);
						crack.undo(earlier.timestamp);

						// in this case, we also need to send out an antimessage
						if (earlier.isConflict && !earlier.isAntiMessage && earlier.senderCrack == this) {
							Message m = new Message(earlier.timestamp, this, earlier.receiverCrack, true);
							m.normalMessage = earlier;
							m.isConflict = true;
							earlier.receiverCrack.messageQueue.add(m);
							conflictMessageSent.remove(earlier.receiverCrack);
						}

						nextStepAfter = crack.getNextTimestep() >= cur.timestamp;
						curStepBefore = cur.timestamp >= crack.getCurrentTimestep();
					}
					// we've now rolled back to the correct timestep, or as early as possible.

					// if this was a normal message, just apply the conflict and continue
					// if it was an anti message, undo the conflict, remove both messages, and continue
					if (!cur.isAntiMessage) {
						// normal
						// everything is now rolled back. apply conflict effects.
						cur.senderCrack.crack.affectAdjacent(cur.receiverCrack.crack);
					} else {
						// anti
						if (!messageQueue.remove(cur.normalMessage)) {
							System.err.println(id + ": Processed an anti-message, but could not find and remove original message!");
						}
						this.crack.undoConflict();
					}

				} else if (cur.isUpdate) {

					// check for any conflicts
					for (CrackPropagatorProcess p : cpps) {
						if (p == this)
							continue;

						if (this.crack.conflictsWith(p.crack)) {
							// no need to send a message unless it's about something new
							// if both cracks are already conflicted, they can continue as usualy
							if ((p.crack.getConflictStartTime() == -1 || p.crack.getConflictStartTime() > this.crack
									.getCurrentTimestep()) && !conflictMessageSent.contains(p)) {
								System.out.println(id + ": sending conflict message from " + this + " to " + p);
								p.crack.affectAdjacent(this.crack);
								Message m = new Message(this.crack.getCurrentTimestep(), this, p, false);
								m.isConflict = true;
								p.messageQueue.add(m);
								conflictMessageSent.add(p);
							}
						}
					}

					// then update
					crack.update();

					// and enqueue next update
					if (crack.getNextTimestep() < Integer.MAX_VALUE) {
						Message m = new Message(crack.getNextTimestep(), /* sender */this, /* receiver */this, /* isAntiMessage */false);
						m.isUpdate = true;
						messageQueue.add(m);
					}
				}

				oldMessages.addLast(cur);

				// TODO: remove this block to enable as-fast-as-possible runtime
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}

	// simple container class for message contents
	// there are a few kinds of messages:
	// update messages (which aren't actually sent between processes, they're just used by each individual process to manage history)
	// and conflict messages
	// really this should be an enum or have subclasses, but we'll just use booleans for now
	private class Message implements Comparable<Message> {
		public int timestamp; // in cycles, aka world time

		public boolean isUpdate = false;
		public boolean isConflict = false;

		public boolean isAntiMessage;
		public Message normalMessage;

		public CrackPropagatorProcess senderCrack;
		public CrackPropagatorProcess receiverCrack;

		public Message(int timestamp, CrackPropagatorProcess senderCrack, CrackPropagatorProcess receiverCrack, boolean isAntiMessage) {
			this.timestamp = timestamp;
			this.senderCrack = senderCrack;
			this.receiverCrack = receiverCrack;
			this.isAntiMessage = isAntiMessage;
		}

		@Override
		public int compareTo(Message that) {
			return this.timestamp - that.timestamp;
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
				if (minimumTime == Integer.MAX_VALUE)
					minimumTime = 0;

				// then just keep calling for updates
				for (IODevice ioDevice : ioDevices) {
					// System.out.println("redraw");
					ioDevice.update(minimumTimeGrid, minimumTime, propagators);
				}

			}

		}

	}
}
