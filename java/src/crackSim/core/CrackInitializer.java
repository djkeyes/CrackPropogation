package crackSim.core;

import crackSim.core.BackingGrid.Cell;

public class CrackInitializer {

	private CAUpdateCalculator updater;

	private BackingGrid defaultLocalBackingGrid;

	public CrackInitializer(CAUpdateCalculator updater, BackingGrid defaultLocalBackingGrid) {
		this.updater = updater;
		this.defaultLocalBackingGrid = defaultLocalBackingGrid;
	}

	public CrackPropagator createNextCrack(Grid currentGrid) {
		Cell4D initialCrack = updater.nextInitialCrackPosition(currentGrid);
		if(initialCrack==null){
			return null;
		}
		// create a new backing grid for the local crack environment
		BackingGrid localBackingGrid = defaultLocalBackingGrid;
		return new CrackPropagator(initialCrack.c, initialCrack.t, localBackingGrid, updater);
	}

	public ReversableCrackPropagator createNextReversableCrack(Grid currentGrid) {
		Cell4D initialCrack = updater.nextInitialCrackPosition(currentGrid);
		if(initialCrack==null){
			return null;
		}
		// create a new backing grid for the local crack environment
		BackingGrid localBackingGrid = defaultLocalBackingGrid;
		return new ReversableCrackPropagator(initialCrack.c, initialCrack.t, localBackingGrid, updater);
	}
}
