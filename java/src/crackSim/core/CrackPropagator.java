package crackSim.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import crackSim.core.BackingGrid.Cell;

/**
 * This class represents a simulation of a local micro-scale crack. It Handles updates to and conflicts between multiple different
 * micro-scale cracks through its update(), conflictsWith(), and affectAdjacent() methods.
 * 
 * Precisely how adjacent cracks affect one another in the real world is difficult to understand--but in our case, we'll assume that if
 * two cracks are adjacent, they speed each other up. Using the jargon of this simulation, if a crack has any damaged cells on the edge
 * of its local micro-level simulation, it will speed up the crack propagation speed of all adjacent macro-level cells by a constant
 * factor after the affectAdjacant() function is applied. factor.
 * 
 * @author daniel
 * 
 */
public class CrackPropagator {

	protected int currentTimestep;
	protected int nextTimestep;
	protected Grid currentGrid;
	protected CAUpdateCalculator updater;
	// the initial position in the macro scale
	private Cell initialCrackLocation;

	private boolean hasDamageOnEdge = false;
	private Set<Cell> edgeCells;

	protected boolean hasAdjacentCracks = false;
	private final double adjacentSpeedup = 1.0 / 1.5; // multiply the required cycle count by adjacentSpeedup times

//	public CrackPropagator(int initialTime, BackingGrid backingGrid, CAUpdateCalculator updater) {
//		currentGrid = new Grid(backingGrid);
//		this.updater = updater;
//
//		this.currentTimestep = initialTime;
//		this.nextTimestep = currentTimestep + updater.getNextCrackUpdateTime(this);
//	}

	public CrackPropagator(Cell initialCrack, int initialTime, BackingGrid backingGrid, CAUpdateCalculator updater) {
		currentGrid = new Grid(backingGrid);
		this.updater = updater;
		
		this.initialCrackLocation = initialCrack;

		this.edgeCells = new HashSet<Cell>();
		// this uses some prior knowedge: the backing grid is composed only of triangles, and they're arranged in such a way that every
		// triangle has 14 neighbors.
		// if there are fewer neighbors, assume it's an edge cell
		int normalEdgeCount = 14;
		for (Cell c : backingGrid.getCells()) {
			if (backingGrid.getNeighbors(c).size() < normalEdgeCount) {
				edgeCells.add(c);
			}
		}


		this.currentTimestep = initialTime;
		this.nextTimestep = currentTimestep + updater.getNextCrackUpdateTime(this);
		
	}

	// updates the simulation and returns the next timestep (in simulation time) that the next update() call will use
	public int update() {
		Cell4D damaged = updater.getCrackUpdate(currentGrid, this);
		return update(damaged);
	}
	
	protected int update(Cell4D damaged){
		currentTimestep = nextTimestep;

		if (damaged != null) {
			currentGrid.addDamaged(damaged.c);

			// after adding damage, we might affect neighboring cells
			hasDamageOnEdge = edgeCells.contains(damaged.c);

			// this logic is a little contrived, because there are like 3 different pieces of code that are all handing this timestep
			// business.
			// just roll with it.
			int diff = damaged.t - currentTimestep;
//			System.out.println("diff=" + diff);
			if (hasAdjacentCracks)
				diff = (int) (diff * adjacentSpeedup);
			diff = Math.max(diff, 1);
//			System.out.println("diff=" + diff);
			nextTimestep += diff;
//			System.out.println("current=" + currentTimestep + ", next=" + nextTimestep);
			return nextTimestep;
		}
		

		// TODO: does this ever happen?
		// It might happen if we're reading updates from a file and run out of updates? or if we fill up the CA grid?
		// should we just terminate this process if we're out of updates?
		// or maybe return infinity?
		return (currentTimestep = nextTimestep++);
	}

	/**
	 * Checks whether this crack propagator contains an area that conflicts with another crack propagator.
	 * 
	 * @param that
	 *            the other propagator to check against
	 * @return true if there is a conflict at this timestep or an earlier timestep
	 */
	public boolean conflictsWith(CrackPropagator that) {
		// if the cracks don't have any common vertices, they can't possibly conflict
		// (because they aren't adjacent)
		Cell thisMacroCrack = this.getInitialCrackLocation();
		Cell thatMacroCrack = that.getInitialCrackLocation();
		if (Collections.disjoint(thisMacroCrack.getVertices(), thatMacroCrack.getVertices()))
			return false;

		// TODO: uncomment the return statement
		// if this returns true, adjacent cracks will always conflict, which makes this simulation trivial.
		// if we use the condition, they will only conflict after some duration
		return true; // this.hasDamageOnEdge || that.hasDamageOnEdge;
	}

	/**
	 * Makes one crack speed up the growth rate of another crack. Afterwards, the passed propagator should be discarded. It is up to the caller to
	 * make sure these cracks actually conflict and are already at adjacent timesteps.
	 * 
	 * @param that
	 *            the propagator to merge. The passed CrackPropagator is merged INTO the implicit CrackPropagator.
	 */
	public void affectAdjacent(CrackPropagator that) {
		// quick sanity check: make sure these cracks are actually adjacent and same timestep --even though the caller should check
		// these
		if (!this.conflictsWith(that)) {
			System.err.println("Can not perform interaction because the cracks are not adjacent! " + this + ", " + that);
			return;
		}

		// this has to be before the other cracks next update
		// (and vice versa)
//		if(this.currentTimestep >= that.nextTimestep || that.currentTimestep >= this.nextTimestep){
//			System.err.println("Can not perform interaction because one crack is too far ahead in time! " + this + "(t=" +this.currentTimestep+ "-> " +this.nextTimestep+"), " + that + "(t=" + that.currentTimestep+ "->" + that.nextTimestep+ ")");
//			return;
//		}
		
//		System.out.println(this + " and " + that +" + are adjacent! hoorah!");
		// the cracks conflict and are at the same time period. awesome.
		this.hasAdjacentCracks = true;
	}

	public Grid getGrid() {
		return currentGrid;
	}

	public int getNextTimestep() {
		return nextTimestep;
	}

	public int getCurrentTimestep() {
		return currentTimestep;
	}
	
	public Cell getInitialCrackLocation() {
		return initialCrackLocation;
	}
}
