package crackSim.core;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.StringTokenizer;

// it might be easier to read stuff from a file rather than interfacing with fortran code
// especially for the initial crack positions, since we just generate those once

/**
 * Rather than interfacting directly with FORTRAN code, this reads results from files. The initial crack positions should be stored in
 * one file, and the crack propagation positions should be stored in another. Because the crack propagation algorithm depends on some
 * feedback, the executable file for the CPA algorithm also needs to be specified so that it can be called by this class.
 */
public class UpdateCalculatorFromFile implements CAUpdateCalculator {
	private String ciaResultFile;
	private String cpaExecutableFile;
	private String cpaDirectory;
	private String cpaInputFile;
	private String cpaCycleFile;
	private String cpaElemFile;
	private FemBackingGrid macroBackingGrid;

	private PriorityQueue<Cell4D> initialCracks;
	private Map<Integer, LinkedList<int[]>> simTimePropagators;
	private Map<Integer, Integer> propagatorStartTimes; // this shadows some data in ReversableCrackPropagator. oh well.

	// TODO: this takes like a million string arguments. clean it up so it's easier to remember
	public UpdateCalculatorFromFile(String ciaResultFile, String cpaExecutableFile, String cpaDirectory, String cpaInputFile,
			String cpaCycleFile, String cpaElemFile, FemBackingGrid macroBackingGrid) {

		this.ciaResultFile = ciaResultFile;
		this.cpaExecutableFile = cpaExecutableFile;
		this.cpaDirectory = cpaDirectory;
		this.cpaInputFile = cpaInputFile;
		this.cpaCycleFile = cpaCycleFile;
		this.cpaElemFile = cpaElemFile;
		this.macroBackingGrid = macroBackingGrid;

		initialCracks = loadInitialCracksFromFile();
		simTimePropagators = new HashMap<Integer, LinkedList<int[]>>();
		propagatorStartTimes = new HashMap<Integer, Integer>();
	}

	private PriorityQueue<Cell4D> loadInitialCracksFromFile() {
		BufferedReader bf;
		try {
			bf = new BufferedReader(new FileReader(ciaResultFile));
		} catch (FileNotFoundException e) {
			System.err.println("Could not read Crack Initialization file: '" + ciaResultFile + "'");
			return new PriorityQueue<Cell4D>();
		}
		// I think the file already stores these cracks in order, but we'll use
		// a PQ just in case.
		PriorityQueue<Cell4D> result = new PriorityQueue<Cell4D>();
		String line;
		try {
			while ((line = bf.readLine()) != null) {
				StringTokenizer st = new StringTokenizer(line);
				int index = Integer.parseInt(st.nextToken());
				st.nextToken();
				int cycle = (int) Math.ceil(Double.parseDouble(st.nextToken()));
				st.nextToken();

				result.add(new Cell4D(macroBackingGrid.getCell(index), cycle));
			}
		} catch (IOException e) {
			System.err.println("IOException while reading CrackInitializationFile: '" + ciaResultFile + "'");
			e.printStackTrace();
			return new PriorityQueue<Cell4D>();
		} finally {
			try {
				bf.close();
			} catch (IOException e) {
				System.err.println("Couldn't close buffered reader in UpdateCalculatorFromFile. FML.");
				e.printStackTrace();
			}
		}

		return result;
	}

	@Override
	public Cell4D nextInitialCrackPosition(Grid currentState) {
		if (initialCracks.isEmpty()) {
			return null;
		}

		Cell4D result = initialCracks.remove();
		int resultIndex = macroBackingGrid.getIndex(result.c);
		// System.out.println(resultIndex);

		// design decision: call the CPA code when we first return the initial
		// crack location
		// (that way we'll block the crack initializer, not the crack
		// propagators)

		// the fortran code requires us to delete the output files before calling
		// TODO: fix this so we don't have to do that
		File file = new File(cpaCycleFile);
		file.delete();
		file = new File(cpaElemFile);
		file.delete();

		// First call the fortran code and wait until it finishes. The fortran code expects the current cell index to be written to a
		// file.
		PrintWriter out;
		try {
			out = new PrintWriter(cpaInputFile);
			out.write("" + resultIndex);
			out.close();
		} catch (FileNotFoundException e2) {
			System.err.println("Could not find CPA input file to write to.");
			e2.printStackTrace();
		}

		// Next call the fortran executable.
		// this involves creating a whole new process (which is expensive, much more so than just starting up a new thread)
		// oh well.
		ProcessBuilder process = new ProcessBuilder(cpaExecutableFile);
		process.directory(new File(cpaDirectory));
		// process.redirectErrorStream(true);
		System.out.println("started fortran code in " + process.directory());
		Process p = null;
		try {
			p = process.start();
			// print the output for feedback
			int count = 0;
			while (true) {
				int c = p.getInputStream().read();
				if (c == -1)
					break;
				// System.out.write((char) c);
				if (c == '\n')
					count++;

				// this code runs much longer than we actually need, so just kill it early
				// TODO: this is a *terrible* way to do this. if the fortran code has even a little delay between printing to stdout
				// and writing to a file, we could prevent half the file from being written
				if (count > 10000) {
					p.destroy();
					break;
				}
			}
			p.waitFor();
		} catch (IOException e1) {
			System.err.println("IOException while running fortran code.");
			e1.printStackTrace();
		} catch (InterruptedException e) {
			System.err.println("Thread interrupted while waiting for fortran code.");
			e.printStackTrace();
		} finally {
			if (p != null)
				p.destroy();
		}

		LinkedList<int[]> queue = new LinkedList<int[]>();
		try {
			BufferedReader bf1 = new BufferedReader(new FileReader(cpaCycleFile));
			BufferedReader bf2 = new BufferedReader(new FileReader(cpaElemFile));
			String line1, line2;
			while ((line1 = bf1.readLine()) != null && (line2 = bf2.readLine()) != null) {
				int cycle = Integer.parseInt(line1.trim());
				int index = Integer.parseInt(line2.trim());

				queue.add(new int[] { index, cycle });
			}
		} catch (FileNotFoundException e) {
			System.err.println("Could not find CPA file.");
			e.printStackTrace();
		} catch (NumberFormatException e) {
			System.err.println("CPA file is not formatted correctly.");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Error reading from CPA file.");
			e.printStackTrace();
		}
		// this doesn't have to be synchronized because it's only called from the CrackInitiazer process
		// System.out.println("first output: " + resultIndex + "--" + Arrays.toString(queue.get(0)));
		simTimePropagators.put(resultIndex, queue);
		propagatorStartTimes.put(resultIndex, result.t);

		return result;
	}

	@Override
	public Cell4D getCrackUpdate(Grid currentState, CrackPropagator currentCrack) {
		// this is based on a micro-level grid, so it's a little more
		// complicated
		int currentCrackIndex = macroBackingGrid.getIndex(currentCrack.getInitialCrackLocation());
		if (simTimePropagators.get(currentCrackIndex).size() == 0)
			return null;
		int[] head = simTimePropagators.get(currentCrackIndex).removeFirst();
		int startTime = propagatorStartTimes.get(currentCrackIndex);

		// System.out.println("next crack schduled for " + (head[1]+startTime));
		// System.out.println("update: " + head[1]);
		FemBackingGrid microBackingGrid = (FemBackingGrid) currentState.getBackingGrid();
		return new Cell4D(microBackingGrid.getCell(head[0]), head[1] + startTime);
	}

	@Override
	public int getNextCrackUpdateTime(CrackPropagator currentCrack) {
		// complicated
		int currentCrackIndex = macroBackingGrid.getIndex(currentCrack.getInitialCrackLocation());
		if (simTimePropagators.get(currentCrackIndex).isEmpty()) {
			return Integer.MAX_VALUE;
		}
		int[] head = simTimePropagators.get(currentCrackIndex).getFirst();

		return head[1];
	}

}
