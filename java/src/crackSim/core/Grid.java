package crackSim.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import crackSim.core.BackingGrid.Cell;

/**
 * Grid class to store adjacent FEM elements, akin to a cellular automaton. Each propogator keeps a grid updated at its own timespeed,
 * but multiple grids can refer to a single BackingGrid, which stores the original layour.
 */
public class Grid {

	// TODO: rather than Set or List, a lot of these should be read-only; they should return an unmodifiableList or whatever have you.
	private BackingGrid backingGrid;

	private Set<Cell> alive;
	private Set<Cell> damaged;

	public Grid(BackingGrid backingGrid) {
		this.backingGrid = backingGrid;
		alive = new HashSet<Cell>();
		damaged = new HashSet<Cell>();

		alive.addAll(backingGrid.getCells());
	}

	// TODO: I'm not sure how users will obtain a reference to a Cell object. do they get it from the BackingGrid? or from the
	// CAUpdator? or should this take an FEM index instead of a cell? Or should callers construct new cells on the fly (in which case
	// we should override cell.hashCode())
	public void addDamaged(Cell c) {
		damaged.add(c);
		alive.remove(c);
	}

	public Set<Cell> getAlive() {
		return alive;
	}

	public Set<Cell> getDamaged() {
		return damaged;
	}

	/**
	 * Returns all cells adjacent to the current cell
	 */
	public List<? extends Cell> getAdjacent(Cell c) {
		return backingGrid.getNeighbors(c);
	}
}
