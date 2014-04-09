package crackSim.core;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

// implements a backing grid by just using a fixed-size array of cells
public class MockBackingGrid implements BackingGrid {

	// Just use an array of cells.
	// This is in row-major; cells[i][j] is row i, col j.
	private Cell[][] cells;
	private List<Cell> cellsList;

	/**
	 * Creates a simple CA backing grid
	 * 
	 * @param n
	 *            number of rows
	 * @param m
	 *            number of columns
	 */
	public MockBackingGrid(int n, int m) {
		cells = new Cell[n][m];
		cellsList = new LinkedList<Cell>();
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				cells[i][j] = new MockCell(i, j);
				cellsList.add(cells[i][j]);
			}
		}
	}

	@Override
	public List<Cell> getNeighbors(Cell c) {
		// Assume a MockBackingGrid cell is being passed
		MockCell mc = (MockCell) c;
		int i = mc.row;
		int j = mc.col;
		List<Cell> result = new LinkedList<Cell>();
		if(i > 0)
			result.add(cells[i-1][j]);
		if(j > 0)
			result.add(cells[i][j-1]);
		if(i < cells.length-1)
			result.add(cells[i+1][j]);
		if(j < cells[i].length-1)
			result.add(cells[i][j+1]);
		if(i > 0 && j > 0)
			result.add(cells[i-1][j-1]);
		if(i > 0 && j < cells[i].length-1)
			result.add(cells[i-1][j+1]);
		if(i < cells.length-1 && j > 0)
			result.add(cells[i+1][j-1]);
		if(i < cells.length-1 && j < cells[i].length-1)
			result.add(cells[i+1][j+1]);
		return result;
	}

	@Override
	public Collection<? extends Cell> getCells() {
		return cellsList;
	}

	/**
	 * Stores each cell using row-col coords (to test for equality and adjancy). Stores vertices as points of a unit square in the
	 * plane.
	 */
	private class MockCell implements Cell {
		private int row, col;

		public MockCell(int row, int col) {
			this.row = row;
			this.col = col;
		}

		@Override
		public List<GridPoint> getVertices() {
			List<GridPoint> result = new LinkedList<GridPoint>();
			// vertices of a unit square, in clockwise order around the y axis
			result.add(new GridPoint(col, row, 0));
			result.add(new GridPoint(col, row + 1, 0));
			result.add(new GridPoint(col + 1, row + 1, 0));
			result.add(new GridPoint(col + 1, row, 0));
			return result;
		}

	}

}
