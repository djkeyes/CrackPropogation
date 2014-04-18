package crackSim.core;

import crackSim.core.BackingGrid.Cell;

public interface CAUpdateCalculator {

	public Cell4D nextInitialCrackPosition(Grid currentState);
	public Cell4D getCrackUpdate(Grid currentState, CrackPropagator currentCrack);
	public int getNextCrackUpdateTime(CrackPropagator currentCrack);
	
}
