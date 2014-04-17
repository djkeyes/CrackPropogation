package crackSim.core;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.PriorityQueue;

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
	private FemBackingGrid macroBackingGrid;
	
	
	private PriorityQueue<Cell4D> initialCracks;
	
	public UpdateCalculatorFromFile(String ciaResultFile, String cpaExecutableFile, FemBackingGrid macroBackingGrid) {

		this.ciaResultFile = ciaResultFile;
		this.cpaExecutableFile = cpaExecutableFile;
		this.macroBackingGrid = macroBackingGrid;
		
		initialCracks = loadInitialCracksFromFile(ciaResultFile);
		// TODO: figure out the initial crack format. just use some dummy values for now.
		// I believe these are all valid values from YCRM.bdf
		initialCracks.add(new Cell4D(macroBackingGrid.getCell(5251), 5));
		initialCracks.add(new Cell4D(macroBackingGrid.getCell(5351), 10));
		initialCracks.add(new Cell4D(macroBackingGrid.getCell(5451), 15));
		initialCracks.add(new Cell4D(macroBackingGrid.getCell(31), 20));
		initialCracks.add(new Cell4D(macroBackingGrid.getCell(1848), 25));
		
	}
	
	private static PriorityQueue<Cell4D> loadInitialCracksFromFile(String ciaResultFile){
		BufferedReader bf;
		try {
			bf = new BufferedReader(new FileReader(ciaResultFile));
		} catch (FileNotFoundException e) {
			System.err.println("Could not read Crack Initialization file: '" + ciaResultFile + "'");
			return new PriorityQueue<Cell4D>();
		}
		// I think the file already stores these cracks in order, but we'll use a PQ just in case.
		PriorityQueue<Cell4D> result = new PriorityQueue<Cell4D>();
		String line;
		try {
			while((line = bf.readLine()) != null){
				// TODO: parse line somehow. what's the format?
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
		return initialCracks.remove();
	}

	@Override
	public Cell4D getCrackUpdate(Grid currentState, CrackPropagator currentCrack) {
		// this is based on a micro-level grid, so it's a little more complicated
		
		
		return null;
	}

	@Override
	public int getNextCrackUpdateTime() {
		// TODO Auto-generated method stub
		return -1;
	}

}
