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
	public Cell4D nextInitialCrackPosition(Grid currentState) {
		// randomly return a new crack every 5 ticks
		simTimeInitializer += 5;
		int size = macroBackingGrid.getCells().size();
		Cell randomResult = (Cell) macroBackingGrid.getCells().toArray()[rng.nextInt(size)];
		return new Cell4D(randomResult, simTimeInitializer);
	}

	// the Cell4D.t time returned by this method is relative to the current timestep.
	// if the current time is 123 and the Cell4D object has time 5, the crack should propagate at time 128.
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
		if (adjacentUndamaged.isEmpty()) {
			adjacentUndamaged.addAll(currentState.getAlive());
		}
		
		if (adjacentUndamaged.size() > 0) {
			// for the time, things are a little tricky. Each crack propagator expands independently, so we keep track of how far in time
			// each propagator is. In our case, we schedule the next crack for 5 timeticks afterwards.
			int crackTime;
			// we need to block other threads briefly while handling the simTimePropagators map.
			synchronized (simTimePropagators) {
				if (!simTimePropagators.containsKey(currentCrack)) {
					simTimePropagators.put(currentCrack, currentCrack.getNextTimestep());
				}
				crackTime = simTimePropagators.get(currentCrack);
				crackTime += 5;
				simTimePropagators.put(currentCrack, crackTime);
			}
			return new Cell4D(adjacentUndamaged.get(rng.nextInt(adjacentUndamaged.size())), crackTime);
		} else {
			return null;
		}
	}

	@Override
	public int getNextCrackUpdateTime(CrackPropagator currentCrack) {
		// cracks always happen 5 ticks after one another.
		return 5;
	}

}
