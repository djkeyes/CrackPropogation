package crackSim.core;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import crackSim.core.BackingGrid.Cell;

/**
 * A dummy implementation of CAUpdateCalculator that returns arbitrary values, for testing and rapid prototyping.
 */
public class MockUpdateCalculator implements CAUpdateCalculator {

	private int initialCount = 0;
	private Random rng = new Random(12345);

	@Override
	public Cell getInitialCrackPosition(Grid currentState) {
		// randomly return a new crack every 5 ticks
		initialCount++;
		if (initialCount != 5) {
			return null;
		}

		initialCount = 0;
		int size = currentState.getAlive().size();
		Cell randomResult = (Cell) currentState.getAlive().toArray()[rng.nextInt(size)];
		return randomResult;
	}

	@Override
	public Cell getCrackUpdate(Grid currentState) {
		// pick randomly from undamaged cells that are adjacent to damaged cells
		List<Cell> adjacentUndamaged = new LinkedList<Cell>();
		for (Cell c : currentState.getDamaged()) {
			for (Cell a : currentState.getAdjacent(c)) {
				if (!currentState.getDamaged().contains(a)) {
					adjacentUndamaged.add(a);
				}
			}
		}
		if (adjacentUndamaged.size() > 0)
			return adjacentUndamaged.get(rng.nextInt(adjacentUndamaged.size()));
		else
			return null;
	}

}
