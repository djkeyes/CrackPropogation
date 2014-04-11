package crackSim.core;

import java.util.Collection;
import java.util.List;
import java.util.Set;

// a read-only grid to represent the current CA model. multiple write-able Grid objects can have a reference to a single BackingGrid
public interface BackingGrid {

	// TODO: what functions should be in here?
	// Probably some kind of getNeighbors(Cell c) function that looks up a cell in a collection (based on hashcode or something) and
	// returns its neighbors. Right now i'm leaving all of those as an interface and leaving it up to subclasses to define their
	// behavior.

	/**
	 * Returns a list cells adjacent to the current cell. Subclasses of BackingGrid should provide Cell implementations that allow for
	 * easy lookup in their backing datastructure. If a Cell object is passed that does not corrospond to this subclasses backing data
	 * structure (ie if a caller is mixing and matching different noncompatible subclasses of Cell objects from different subclasses of
	 * BackingGrid), an exception is thrown.
	 * 
	 * @param c
	 *            The cell to look up.
	 * @return Cells that are considered immediately adjacent to c. This does not include c.
	 */
	public List<? extends Cell> getNeighbors(Cell c);

	/**
	 * Returns a full list of all cells in this grid.
	 * 
	 * @return a collection of Cell objects or subclasses thereof.
	 */
	public Collection<? extends Cell> getCells();

	/**
	 * Simple container class representing a Cell from this simulation. In this sense, because our cells correspond to 2d meshes of
	 * elements, cells should store their vertices and possibly other identifiers to check for equality.
	 */
	public interface Cell {
		/**
		 * A list containing GridPoints that represent the positions of vertices of the polygonal cell in space
		 * 
		 * @return a list of GridPoints, one grid point for each vertex
		 */
		public List<GridPoint> getVertices();
	}

	/**
	 * Simple container class representing a point in 3space. This is intended to be helpful for storing state in the CA model.
	 */
	public class GridPoint {
		public GridPoint(double x, double y, double z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public final double x, y, z;
	}

	/**
	 * Cells in a BackingGrid share a set of gridpoints with one another. For convenience, this returns that set of gridpoints, without
	 * repeats.
	 * 
	 * @return a set of unique 3d points
	 */
	public Set<? extends GridPoint> getGridPoints();
}
