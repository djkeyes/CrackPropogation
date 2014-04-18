package crackSim.core;

import crackSim.core.BackingGrid.Cell;

// This class implements CAUpdateCalculator by directly calling FORTRAN code using JNI. If that's too difficult, we can always just read info from a file.
public class FortranUpdateCalculator implements CAUpdateCalculator {

	// TODO: add native jni calls to fortran code
	@Override
	public native Cell4D nextInitialCrackPosition(Grid currentState);

	@Override
	public native Cell4D getCrackUpdate(Grid currentState, CrackPropagator crack);

	@Override
	public int getNextCrackUpdateTime(CrackPropagator currentCrack) {
		// TODO Auto-generated method stub
		return -1;
	}

}
