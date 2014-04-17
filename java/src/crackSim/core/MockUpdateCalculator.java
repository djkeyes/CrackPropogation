package crackSim.core;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import crackSim.core.BackingGrid.Cell;

/**
 * A dummy implementation of CAUpdateCalculator that returns arbitrary values, for testing and rapid prototyping.
 */
public class MockUpdateCalculator implements CAUpdateCalculator {

	private int simTimeInitializer = 0;
	private Map<CrackPropagator, Integer> simTimePropagators = new HashMap<CrackPropagator, Integer>();
	private Random rng = new Random(12345);
	private BackingGrid macroBackingGrid;

	/**
	 * 
	 * @param bg
	 *            the BackingGrid to use at the macro level
	 */
	public MockUpdateCalculator(BackingGrid bg) {
		this.macroBackingGrid = bg;
	}

	@Override
	public Cell4D getInitialCrackPosition(Grid currentState) {
		// randomly return a new crack every 5 ticks
		simTimeInitializer += 5;
		int size = macroBackingGrid.getCells().size();
		Cell randomResult = (Cell) macroBackingGrid.getCells().toArray()[rng.nextInt(size)];
		return new Cell4D(randomResult, simTimeInitializer);
	}

	@Override
	public Cell4D getCrackUpdate(Grid currentState, CrackPropagator currentCrack) {
		// this uses the micro-level grid, not the macro level one
		// pick randomly from undamaged cells that are adjacent to damaged cells
		List<Cell> adjacentUndamaged = new LinkedList<Cell>();
		for (Cell c : currentState.getDamaged()) {
			for (Cell a : currentState.getAdjacent(c)) {
				if (!currentState.getDamaged().contains(a)) {
					adjacentUndamaged.add(a);
				}
			}
		}
		// if there's nothing currently damaged, just pick at random
		if(adjacentUndamaged.isEmpty()){
			adjacentUndamaged.addAll(currentState.getAlive());
		}

		// for the time, things are a little tricky. Each crack propagator expands independently, so we keep track of how far in time
		// each propagator is. In our case, we schedule the next crack for 5 timeticks afterwards.
		if(!simTimePropagators.containsKey(currentCrack)){
			simTimePropagators.put(currentCrack, 0);
		}
		int crackTime = simTimePropagators.get(currentCrack);
		crackTime += 5;
		simTimePropagators.put(currentCrack, crackTime);

		if (adjacentUndamaged.size() > 0){
			return new Cell4D(adjacentUndamaged.get(rng.nextInt(adjacentUndamaged.size())), crackTime);
		}else{
			return null;
		}
	}

}
