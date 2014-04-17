package crackSim.core;

import crackSim.core.BackingGrid.Cell;

public interface CAUpdateCalculator {

	public Cell4D getInitialCrackPosition(Grid currentState);
	public Cell4D getCrackUpdate(Grid currentState, CrackPropagator currentCrack);
	
}
