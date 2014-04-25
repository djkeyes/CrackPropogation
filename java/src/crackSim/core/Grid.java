package crackSim.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import crackSim.core.BackingGrid.Cell;
import crackSim.core.BackingGrid.GridPoint;

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
	/**
	 * Adds a single damaged cell to the grid.
	 * 
	 * @param A
	 *            cell
	 */
	public synchronized void addDamaged(Cell c) {
		damaged.add(c);
		alive.remove(c);
	}

	/**
	 * Adds all damaged cells in the passed grid to this grid. If a cell is damaged in either grid or in both grids, it will be marked
	 * as damaged in this grid as a result. Both grids be based off the same backing grid (and contain the same type of backing cells).
	 * 
	 * @param grid
	 *            A grid containing any amount of damaged cells to merge into this grid.
	 */
	public synchronized void addDamaged(Grid grid) {
		assert(this.backingGrid == grid.backingGrid);
		
		for(Cell c : grid.damaged){
			this.alive.remove(c);
			this.damaged.add(c);
		}
	}

	public synchronized Set<Cell> getAlive() {
		return new HashSet<Cell>(alive);
	}

	public synchronized Set<Cell> getDamaged() {
		return new HashSet<Cell>(damaged);
	}

	/**
	 * Returns all cells adjacent to the current cell
	 */
	public synchronized List<? extends Cell> getAdjacent(Cell c) {
		return backingGrid.getNeighbors(c);
	}

	public synchronized Set<? extends GridPoint> getGridPoints() {
		return backingGrid.getGridPoints();
	}

	public synchronized BackingGrid getBackingGrid() {
		return backingGrid;
	}

	public synchronized void removeDamaged(Cell c) {
		damaged.remove(c);
		alive.add(c);
	}
}
