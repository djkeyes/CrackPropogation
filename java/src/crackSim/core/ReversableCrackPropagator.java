package crackSim.core;

import java.util.Collections;

import crackSim.core.BackingGrid.Cell;

// a CrackPropagator that additional has an undo() function to rollback to an earlier state
public class ReversableCrackPropagator extends CrackPropagator {

	private final int startTimestep;
	
//	public ReversableCrackPropagator(int currentTimestep, BackingGrid backingGrid, CAUpdateCalculator updater) {
//		super(currentTimestep, backingGrid, updater);
//		startTimestep = currentTimestep;
//	}

	public ReversableCrackPropagator(Cell initialCell, int currentTime, BackingGrid localBackingGrid, CAUpdateCalculator updater) {
		super(initialCell, currentTime, localBackingGrid, updater);
		startTimestep = currentTime;
	}

	@Override
	public int update() {
		super.update();
		// TODO: perform super.update(), but also store a record of the update
		return -1;
	}

	// TODO: make this method signature correct. I guess it should probably take a timestep or something like that.
	public void undo() {
	};

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
		return this.conflictsWith((CrackPropagator) that)
				&& this.currentTimestep >= that.startTimestep
				&& that.currentTimestep >= this.startTimestep;
	}
	


	public int getStartTime() {
		return startTimestep;
	}
}
