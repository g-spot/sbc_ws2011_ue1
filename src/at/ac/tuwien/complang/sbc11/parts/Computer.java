package at.ac.tuwien.complang.sbc11.parts;

import java.util.List;

import at.ac.tuwien.complang.sbc11.workers.Tester;
import at.ac.tuwien.complang.sbc11.workers.Tester.TestType;
import at.ac.tuwien.complang.sbc11.workers.Worker;

public class Computer {
	// general information
	private boolean isCompletelyTested;
	private boolean isComplete;
	private Boolean haveTestsFailed[];
	
	// parts
	private CPU cpu = null;
	private Mainboard mainboard = null;
	private List<RAM> ramModules = null;
	private GraphicBoard graphicBoard = null;
	
	// who did the work
	private List<Worker> workers;
	
	public Computer() {
		/* haveTestsFailed is an array of type Boolean
		 * for each test of the enumeration TestType
		 * the value in the array can either be
		 *   - null (not tested yet)
		 *   - true (tested and failed)
		 *   - false (tested and successful)
		 */
		haveTestsFailed = new Boolean[TestType.values().length];
		for(TestType testType:Tester.TestType.values()) {
			haveTestsFailed[testType.ordinal()] = null;
		}
	}
	
	public void setTested(TestType testType, boolean wasSuccessful) {
		haveTestsFailed[testType.ordinal()] = wasSuccessful;
	}
	
	public boolean isDefect() {
		// returns true if at least one test has failed
		for(Boolean testFailed:haveTestsFailed) {
			if(testFailed)
				return true;
		}
		return false;
	}
	
	public boolean isCompletelyTested() {
		// returns false if at least one test has not been done yet
		for(Boolean testFailed:haveTestsFailed) {
			if(testFailed == null)
				return false;
		}
		return true;
	}
}
