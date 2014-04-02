package crackSim.core;

public class FortranUpdateCalculator implements CAUpdateCalculator {

	// TODO: add native jni calls to fortran code
	
	@Override
	public native void getInitialCrackPosition();

	@Override
	public native void getCrackUpdate();

}
