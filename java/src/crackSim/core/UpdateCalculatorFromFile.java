package crackSim.core;

import crackSim.core.BackingGrid.Cell;

public class UpdateCalculatorFromFile implements CAUpdateCalculator {

	// it might be easier to read stuff from a file rather than interfacing with fortran code
	// especially for the initial crack positions, since we just generate those once
	
	@Override
	public Cell getInitialCrackPosition(Grid currentState) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Cell getCrackUpdate(Grid currentState) {
		// TODO Auto-generated method stub
		return null;
	}

}
