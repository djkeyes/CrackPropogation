package crackSim.core;

import crackSim.core.BackingGrid.Cell;

public class CrackPropagator {

	protected int currentTimestep;
	protected Grid currentGrid;
	private CAUpdateCalculator updater;
	// the initial position in the macro scale
	private Cell initialCrackLocation;

	public CrackPropagator(int initialTime, BackingGrid backingGrid, CAUpdateCalculator updater) {
		currentGrid = new Grid(backingGrid);
		this.currentTimestep = initialTime;
		this.updater = updater;
	}

	public CrackPropagator(Cell initialCrack, int initialTime, BackingGrid backingGrid, CAUpdateCalculator updater) {
		this(initialTime, backingGrid, updater);
		this.initialCrackLocation = initialCrack;
	}

	// updates the simulation and returns the next timestep (in simulation time) that the next update() call will use
	public int update() {
		Cell4D damaged = updater.getCrackUpdate(currentGrid, this);

		if (damaged != null)
			currentGrid.addDamaged(damaged.c);

		return currentTimestep = damaged.t;
	}

	/**
	 * Checks whether this crack propagator contains an area that conflicts with another crack propagator
	 * 
	 * @param that
	 *            the other propagator to check against
	 * @return true if there is a conflict at this timestep or an earlier timestep
	 */
	public boolean conflictsWith(CrackPropagator that) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Merges the passed propagator with this one. Afterwards, the passed propagator should be discarded.
	 * 
	 * @param The
	 *            propagator to merge. The passed CrackPropagator is merged INTO the implicit CrackPropagator.
	 */
	public void merge(CrackPropagator second) {
		// TODO Auto-generated method stub

	}

	public Grid getGrid() {
		return currentGrid;
	}
	
	
	public int getCurrentTimestep(){
		return currentTimestep;
	}
	
	public Cell getInitialCrackLocation(){
		return initialCrackLocation;
	}
}
