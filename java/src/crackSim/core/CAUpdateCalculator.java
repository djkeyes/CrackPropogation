package crackSim.core;

import crackSim.core.BackingGrid.Cell;

public interface CAUpdateCalculator {

	// TODO: what's the signature of these methods? what types should be passed and returned?
	// right now i'm passing the current grid state and returning a (possibly null) cell.
	// alternatively, we could return a cell and a time--the time at which that cell becomes damaged.
	public Cell getInitialCrackPosition(Grid currentState);
	public Cell getCrackUpdate(Grid currentState);
	
}
