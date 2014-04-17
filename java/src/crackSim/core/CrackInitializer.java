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
		Cell4D initialCrack = updater.getInitialCrackPosition(currentGrid);
		// create a new backing grid for the local crack environment
		BackingGrid localBackingGrid = defaultLocalBackingGrid;
		System.out.println("creating a new crack propagator at " + initialCrack);
		return new CrackPropagator(initialCrack.c, initialCrack.t, localBackingGrid, updater);
	}

	public ReversableCrackPropagator createNextReversableCrack() {
		// TODO return a CrackPropagator based on then next occurring crack produces by e-N
		return null;
	}
}
