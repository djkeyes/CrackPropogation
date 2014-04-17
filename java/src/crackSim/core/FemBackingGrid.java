package crackSim.core;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

// implements BackingGrid by reading FEM elements from a file and storing them as a graph, so that each cell can somehow lookup its neighbors
public class FemBackingGrid implements BackingGrid {

	// store cells as a graph, using an adjacency list
	private Map<Cell, List<FemCell>> adjacencyList;
	// store info from the bdf file. bdf files are convenient in that every element has a unique integer index, so we can index, sort,
	// and and hash them easily.
	private Map<Integer, FemGridPoint> gridpoints;
	private Map<Integer, FemCell> cells;

	public FemBackingGrid(String filename) throws FileNotFoundException, IOException {

		gridpoints = new HashMap<Integer, FemGridPoint>();
		cells = new HashMap<Integer, FemCell>();

		load(filename);
	}

	private void load(String filename) throws FileNotFoundException, IOException {
		BufferedReader bf = new BufferedReader(new FileReader(filename));
		CommandFactory p = new CommandFactory();
		List<ParseCommand> commands = new LinkedList<ParseCommand>();
		String curLine = bf.readLine();
		String nextLine;
		while ((nextLine = bf.readLine()) != null) {
			// if the next line starts with a space, use it too
			// TODO: this might not be a hard-and-fast rule. I'm not 100% sure of BDF syntax.
			if (nextLine.indexOf(' ') == 0 || nextLine.indexOf('\t') == 0) {
				curLine += nextLine;
			} else {
				commands.add(p.parse(curLine));
				curLine = nextLine;
			}
			// TODO: this might have weird behavior if the file does not end in a newline or has a command on the last line. I think?
		}
		for (ParseCommand command : commands) {
			command.execute();
		}
		// after all the data is loaded, associate each cell with its gridpoints and store their adjacency lists
		for (FemCell c : cells.values()) {
			c.storeGridpoints(gridpoints);
		}
		adjacencyList = generateAdjacencyList();
	}

	@Override
	public List<? extends Cell> getNeighbors(Cell c) {
		return adjacencyList.get(c);
	}

	@Override
	public Collection<? extends Cell> getCells() {
		return cells.values();
	}
	
	/**
	 * Returns a cell based on its index specified by the BDF file input.
	 * @param femId the integer node index originally specified by a TRIA3 or QUAD4 command 
	 * @return the cell corrospinding to this index
	 */
	public Cell getCell(int femId){
		return cells.get(femId);
	}

	@Override
	public Set<? extends GridPoint> getGridPoints() {
		// TODO: don't create a new hash set on every call to this method. just save the old one.
		return new HashSet<GridPoint>(gridpoints.values());
	}

	public Map<Cell, List<FemCell>> generateAdjacencyList() {
		Map<Cell, List<FemCell>> result = new HashMap<Cell, List<FemCell>>();
		for (FemCell c : cells.values()) {
			result.put(c, c.getAdjacent());
		}
		return result;
	}

	/**
	 * A parser for NASTRAN BDF files. This parser uses two OO paradigms: the command pattern and the factory pattern. Each line of a
	 * parsed file is fed to a factory method, which produces ParseCommand objects. The behavior of each ParseCommand depends on the
	 * implementation of that particular ParseCommand--here I've just used anonymous classes for convenience.
	 */
	private class CommandFactory {
		/**
		 * Parse an individual line of a NASTRAN BDF file
		 * 
		 * @param line
		 *            The full line of a bdf file. Lines should begin with a command or a comment, or they should consist of
		 *            whitespace. Commands spanning multiple lines should be combined and input as one string.
		 */
		public ParseCommand parse(String line) {
			// behavior depends on the command given in the line
			String commandString = line.split(" ")[0];
			return createCommand(commandString, line);
		}

		public void addGridPoint(double x, double y, double z, int index) {
			gridpoints.put(index, new FemGridPoint(x, y, z, index));
		}

		public void addQuad(int p1, int p2, int p3, int p4, int index) {
			cells.put(index, new Quadrilateral(p1, p2, p3, p4, index));
		}

		public void addTri(int p1, int p2, int p3, int index) {
			cells.put(index, new Triangle(p1, p2, p3, index));
		}

		// factory method to produceCommand objects
		private ParseCommand createCommand(final String commandString, final String line) {
			ParseCommand curCommand = null;
			// If we were using java 8, I could switch on strings and store commands as Functional Interfaces. but whatever.

			// The BDF file i'm using has a lot of irrelevant commands. I assume they're not important for this simulation, but if
			// they are, they should be removed from this list and have their behavior specified.
			List<String> ignoredCommands = Arrays.asList("", "CBAR", "PSHELL", "RBE3", "SOL", "CEND", "ECHO", "SUBCASE", "BEGIN", "PARAM",
					"PBARL", "MAT1*", "*", "SPCADD", "LOAD", "SPC1", "FORCE", "MOMENT", "ENDDATA", "SET", "TITLE");
			boolean isIgnoredCommand = false;
			for (String s : ignoredCommands) {
				if (s.equalsIgnoreCase(commandString)) {
					isIgnoredCommand = true;
					break;
				}
			}
			if (isIgnoredCommand) {
				// These commands aren't relevant for the mesh. Do nothing.
				curCommand = new ParseCommand() {
					@Override
					public void execute() {
					}
				};
			} else if (commandString.equalsIgnoreCase("$")) {
				// this command is just a comment. do nothing.
				curCommand = new ParseCommand() {
					@Override
					public void execute() {
					}
				};
			} else if (commandString.equalsIgnoreCase("GRID")) {
				// this encodes positions of gridpoints
				curCommand = new ParseCommand() {
					@Override
					public void execute() {
						StringTokenizer st = new StringTokenizer(line);
						// first token is GRID
						st.nextToken();
						// second is index
						int index = Integer.parseInt(st.nextToken());
						// 3-5 are coordinates
						double x = Double.parseDouble(st.nextToken());
						double y = Double.parseDouble(st.nextToken());
						double z = Double.parseDouble(st.nextToken());

						addGridPoint(x, y, z, index);
					}
				};
			} else if (commandString.equalsIgnoreCase("CQUAD4")) {
				// quadrilateral
				curCommand = new ParseCommand() {
					@Override
					public void execute() {
						StringTokenizer st = new StringTokenizer(line);
						// first token is CQUAD4
						st.nextToken();
						// second is index
						int index = Integer.parseInt(st.nextToken());
						// I think third is the index of a PSHELL element (not used here)
						st.nextToken();
						// 4-7 are indices of corrosponding grid points
						int n1 = Integer.parseInt(st.nextToken());
						int n2 = Integer.parseInt(st.nextToken());
						int n3 = Integer.parseInt(st.nextToken());
						int n4 = Integer.parseInt(st.nextToken());
						addQuad(n1, n2, n3, n4, index);
					}
				};
			} else if (commandString.equalsIgnoreCase("CTRIA3")) {
				// triangle
				curCommand = new ParseCommand() {
					@Override
					public void execute() {
						StringTokenizer st = new StringTokenizer(line);
						// first token is CTRIA3
						st.nextToken();
						// second is index
						int index = Integer.parseInt(st.nextToken());
						// I think third is the index of a PSHELL element (not used here)
						st.nextToken();
						// 4-6 are indices of corrosponding grid points
						int n1 = Integer.parseInt(st.nextToken());
						int n2 = Integer.parseInt(st.nextToken());
						int n3 = Integer.parseInt(st.nextToken());
						addTri(n1, n2, n3, index);
					}
				};
			} else {
//				System.out.println("line: " + "'" + line + "'");
//				System.out.println("cmdstring: "  + "'" + commandString + "'");
//				throw new RuntimeException("command not found: " + commandString + ". meep.");
				System.err.println("line: " + "'" + line + "'");
				System.err.println("cmdstring: "  + "'" + commandString + "'");
				curCommand = new ParseCommand() {
					@Override
					public void execute() {
					}
				};
			}
			return curCommand;
		}

	}

	private static interface ParseCommand {
		public void execute();
	}

	private class FemGridPoint extends GridPoint {
		public int index;

		public List<FemCell> incidentCells;

		public FemGridPoint(double x, double y, double z, int index) {
			super(x, y, z);
			incidentCells = new LinkedList<FemCell>();
			this.index = index;
		}
	}

	private abstract class FemCell implements Cell {
		public int index;

		public FemCell(int index) {
			this.index = index;
		}

		/**
		 * Associates this cell with its gridpoints, and vice-versa. This must be called before attempting to access adjacency info,
		 * because adjacency us calculated based off of shared gridpoints.
		 * 
		 * @param gridpoints
		 *            a collection of all FemGridPoints in the current simulation, so that raw index information can be looked up.
		 */
		public abstract void storeGridpoints(Map<Integer, FemGridPoint> gridpoints);

		/**
		 * Returns a list a cells that share gridpoints with this cell, excluding this cell.
		 */
		public List<FemCell> getAdjacent() {
			Set<FemCell> adjacent = new HashSet<FemCell>();
			for (GridPoint g : getVertices()) {
				FemGridPoint f = (FemGridPoint) g;
				for (FemCell fc : f.incidentCells) {
					adjacent.add(fc);
				}
			}
			return new LinkedList<FemCell>(adjacent);
		}

	}

	/**
	 * Simple container class
	 */
	private class Quadrilateral extends FemCell {

		private int p1ind, p2ind, p3ind, p4ind;
		private FemGridPoint p1, p2, p3, p4;

		public Quadrilateral(int p1, int p2, int p3, int p4, int index) {
			super(index);
			this.p1ind = p1;
			this.p2ind = p2;
			this.p3ind = p3;
			this.p4ind = p4;
		}

		@Override
		public void storeGridpoints(Map<Integer, FemGridPoint> gridpoints) {
			p1 = gridpoints.get(p1ind);
			p1.incidentCells.add(this);
			p2 = gridpoints.get(p2ind);
			p2.incidentCells.add(this);
			p3 = gridpoints.get(p3ind);
			p3.incidentCells.add(this);
			p4 = gridpoints.get(p4ind);
			p4.incidentCells.add(this);
		}

		@Override
		public List<GridPoint> getVertices() {
			List<GridPoint> result = new LinkedList<GridPoint>();
			result.add(p1);
			result.add(p2);
			result.add(p3);
			result.add(p4);
			return result;
		}

	}

	/**
	 * Simple container class
	 */
	private class Triangle extends FemCell {

		private int p1ind, p2ind, p3ind;
		private FemGridPoint p1, p2, p3;

		public Triangle(int p1, int p2, int p3, int index) {
			super(index);
			this.p1ind = p1;
			this.p2ind = p2;
			this.p3ind = p3;
		}

		@Override
		public void storeGridpoints(Map<Integer, FemGridPoint> gridpoints) {
			p1 = gridpoints.get(p1ind);
			p1.incidentCells.add(this);
			p2 = gridpoints.get(p2ind);
			p2.incidentCells.add(this);
			p3 = gridpoints.get(p3ind);
			p3.incidentCells.add(this);
		}

		@Override
		public List<GridPoint> getVertices() {
			List<GridPoint> result = new LinkedList<GridPoint>();
			result.add(p1);
			result.add(p2);
			result.add(p3);
			return result;
		}

	}
}
