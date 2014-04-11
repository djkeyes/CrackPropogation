package crackSim.core;

import crackSim.core.BackingGrid.Cell;

public class CrackPropagator {
	
	protected int currentTimestep;
	protected Grid currentGrid;
	private CAUpdateCalculator updater;

	public CrackPropagator(BackingGrid backingGrid, CAUpdateCalculator updater){
		currentGrid = new Grid(backingGrid);
		currentTimestep = 0;
		this.updater = updater;
	}
	
	public CrackPropagator(Cell initialCrack, BackingGrid backingGrid, CAUpdateCalculator updater) {
		this(backingGrid, updater);
		currentGrid.addDamaged(initialCrack);
	}

	// updates the simulation and returns the next timestep (in simulation time) that the next update() call will use
	public int update(){
		Cell damaged = updater.getCrackUpdate(currentGrid);
		
		if (damaged != null)
			currentGrid.addDamaged(damaged);
		
		return ++currentTimestep;
	}

	/**
	 * Checks whether this crack propagator contains an area that conflicts with another crack propagator
	 * @param that the other propagator to check against
	 * @return true if there is a conflict at this timestep or an earlier timestep
	 */
	public boolean conflictsWith(CrackPropagator that) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Merges the passed propagator with this one. Afterwards, the passed propagator should be discarded.
	 * @param The propagator to merge. The passed CrackPropagator is merged INTO the implicit CrackPropagator.
	 */
	public void merge(CrackPropagator second) {
		// TODO Auto-generated method stub
		
	}

	public Grid getGrid() {
		return currentGrid;
	}
}
