package crackSim.core;

// a CrackPropagator that additional has an undo() function to rollback to an earlier state
public class ReversableCrackPropagator extends CrackPropagator {


	public ReversableCrackPropagator(BackingGrid backingGrid, CAUpdateCalculator updater) {
		super(backingGrid, updater);
	}

	@Override
	public int update(){
		// TODO: perform super.update(), but also store a record of the update
		return -1;
	}
	
	// TODO: make this method signature correct. I guess it should probably take a timestep or something like that.
	public void undo() {};
}
