package crackSim.core;

import crackSim.core.BackingGrid.Cell;


public class CrackInitializer {
	
	private CAUpdateCalculator updater;
	public CrackInitializer(CAUpdateCalculator updater){
		this.updater = updater;
	}

	public CrackPropagator createNextCrack(Grid currentGrid){
		Cell initialCrack = updater.getInitialCrackPosition(currentGrid);
		if(initialCrack != null)
			return new CrackPropagator(initialCrack, currentGrid.getBackingGrid(), updater);
		else
			return null;
	}

	public ReversableCrackPropagator createNextReversableCrack(){
		// TODO return a CrackPropagator based on then next occurring crack produces by e-N
		return null;
	}
}
