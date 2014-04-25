package crackSim.core;

import java.util.LinkedList;
import java.util.TreeMap;

import crackSim.core.BackingGrid.Cell;

// a CrackPropagator that additional has an undo() function to rollback to an earlier state
public class ReversableCrackPropagator extends CrackPropagator {

	private final int startTimestep;
	private int conflictStartTime = -1;
	
	// store old updates
	private LinkedList<Cell4D> pastUpdates;
	// if we ever roll back, save the future updates
	private LinkedList<Cell4D> futureUpdates;

	// public ReversableCrackPropagator(int currentTimestep, BackingGrid backingGrid, CAUpdateCalculator updater) {
	// super(currentTimestep, backingGrid, updater);
	// startTimestep = currentTimestep;
	// }

	public ReversableCrackPropagator(Cell initialCell, int currentTime, BackingGrid localBackingGrid, CAUpdateCalculator updater) {
		super(initialCell, currentTime, localBackingGrid, updater);
		startTimestep = currentTime;
		pastUpdates = new LinkedList<Cell4D>();
		futureUpdates = new LinkedList<Cell4D>();
	}

	@Override
	public int update() {
		// call super.update(damaged), but also store a record of updates
		Cell4D damaged;
		if(futureUpdates.size() > 0){
			damaged = futureUpdates.removeFirst();
		} else {
			damaged = updater.getCrackUpdate(currentGrid, this);
		}
		pastUpdates.add(damaged);
		return update(damaged);
	}

	/**
	 * Rolls back this crack propagator until the current time is less than or equal to the specified timestamp
	 * 
	 * @param timestamp
	 */
	public void undo(int timestamp) {
		while(this.currentTimestep > timestamp){
			Cell4D damaged = pastUpdates.removeLast();
			futureUpdates.addFirst(damaged);
			currentGrid.removeDamaged(damaged.c);
			if(pastUpdates.isEmpty())
				this.currentTimestep = this.startTimestep;
			else
				this.currentTimestep = pastUpdates.peekLast().t;
			this.nextTimestep = damaged.t;
		}
	}
	public void undoConflict(){
		this.hasAdjacentCracks = false;
		this.conflictStartTime = -1;
	}

	/**
	 * Checks whether this ReversableCrackPropagator contains an area that conflicts with ReversableCrackPropagator. In addition to the
	 * requirements from CrackPropagator, a both ReversableCrackPropagators' current times must be after their start times--you can't
	 * roll a propagator back to before it was created.
	 * 
	 * Note: this does *not* override CrackPropagator.conflictsWith(CrackPropagator), because java does not perform dispatch on method
	 * parameters. It overloads the method instead.
	 * 
	 * @param that
	 *            the other propagator to check against
	 * @return true if there is a conflict at this timestep or an earlier timestep, after both start timesteps
	 */
	public boolean conflictsWith(ReversableCrackPropagator that) {
		return this.conflictsWith((CrackPropagator) that) && this.currentTimestep >= that.startTimestep
				&& that.currentTimestep >= this.startTimestep;
	}
	
	public void affectAdjacent(ReversableCrackPropagator that) {
		if(!this.hasAdjacentCracks){
			conflictStartTime = currentTimestep;
		}
		if(!that.hasAdjacentCracks){
			that.conflictStartTime = that.currentTimestep;
		}
		super.affectAdjacent(that);
	}

	public int getStartTime() {
		return startTimestep;
	}
	
	public int getConflictStartTime(){
		return conflictStartTime;
	}

}
