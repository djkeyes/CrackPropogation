package crackSim.core;

import crackSim.core.BackingGrid.Cell;

/**
 * Container class for storing a cell affiliated with a time. It's a cell location in 3D space and 1D time, or 4D space-time.
 * 
 * These are comparable by their time value.
 */
public class Cell4D implements Comparable<Cell4D> {
	public final Cell c;
	public final int t;

	public Cell4D(Cell c, int t) {
		this.c = c;
		this.t = t;
	}

	@Override
	public int compareTo(Cell4D that) {
		return this.t - that.t;
	}
}