package crackSim.scheduling;

import java.util.List;

import crackSim.core.CrackPropagator;
import crackSim.core.Grid;

// An IO device. These differ from normal asynchronous processes because these are non-reversable; once you print something to the console, you can't take it back.
// The TimeWarp algorithm, for example, has to deal with these in a special way.
public interface IODevice {

	// TODO: what arguments should this take? A Grid that reflects the current state? a timestep? a list of CrackPropagators?
	// should we make a whole new class called SimulationState that just stores all of these values?
	public void update(Grid g, int macroTime, List<CrackPropagator> propagators);
}
