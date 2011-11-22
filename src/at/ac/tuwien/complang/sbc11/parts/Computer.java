package at.ac.tuwien.complang.sbc11.parts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.capi3.Index;
import org.mozartspaces.capi3.Queryable;

import at.ac.tuwien.complang.sbc11.workers.Tester;
import at.ac.tuwien.complang.sbc11.workers.Tester.TestState;
import at.ac.tuwien.complang.sbc11.workers.Tester.TestType;
import at.ac.tuwien.complang.sbc11.workers.Worker;

@Queryable
public class Computer implements Serializable {

	private static final long serialVersionUID = 4357416927653129937L;

	// general information
	private long id;
	@Index
	private TestState testStates[];
	
	// parts
	private CPU cpu = null;
	private Mainboard mainboard = null;
	private List<RAM> ramModules = null;
	private GraphicBoard graphicBoard = null;
	
	// who did the work
	private List<Worker> workers;
	
	public Computer() {
		ramModules = new ArrayList<RAM>();
		/* haveTestsFailed is an array of type Boolean
		 * for each test of the enumeration TestType
		 * the value in the array can either be
		 *   - null (not tested yet)
		 *   - true (tested and failed)
		 *   - false (tested and successful)
		 */
		testStates = new TestState[TestType.values().length];
		for(TestType testType:Tester.TestType.values()) {
			testStates[testType.ordinal()] = TestState.NOT_TESTED;
		}
		workers = new ArrayList<Worker>();
	}
	
	public void setTested(TestType testType, TestState testState) {
		testStates[testType.ordinal()] = testState;
	}
	
	public boolean isDefect() {
		// returns true if at least one test has failed
		for(TestState testState:testStates) {
			if(testState == TestState.FAILED)
				return true;
		}
		return false;
	}
	
	public boolean isCompletelyTested() {
		// returns false if at least one test has not been done yet
		for(TestState testState:testStates) {
			if(testState == TestState.NOT_TESTED)
				return false;
		}
		return true;
	}
	
	public boolean isComplete() {
		// returns true if the computer has been assigned
		// a cpu, a mainboard and at least one ram module
		return (cpu != null && mainboard != null && ramModules != null && ramModules.size() > 0);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	// getters and setters
	public CPU getCpu() {
		return cpu;
	}

	public void setCpu(CPU cpu) {
		this.cpu = cpu;
	}

	public Mainboard getMainboard() {
		return mainboard;
	}

	public void setMainboard(Mainboard mainboard) {
		this.mainboard = mainboard;
	}

	public GraphicBoard getGraphicBoard() {
		return graphicBoard;
	}

	public void setGraphicBoard(GraphicBoard graphicBoard) {
		this.graphicBoard = graphicBoard;
	}

	public List<Worker> getWorkers() {
		return workers;
	}

	public List<RAM> getRamModules() {
		return ramModules;
	}

	public TestState[] getTestStates() {
		return testStates;
	}

	public void setTestStates(TestState[] testStates) {
		this.testStates = testStates;
	}
	
	@Override
	public String toString() {
		final char NEWLINE = '\n';
		final String INDENT = "   ";
		String result = "COMPUTER[" + getId() + "]" + NEWLINE;
		result += INDENT + toStringParts() + NEWLINE;
		result += INDENT + toStringTests() + NEWLINE;
		result += INDENT + toStringWorkers();
		return result;
	}
	
	private String toStringParts() {
		String result = "CPU";
		if(cpu != null)
			result += "[" + cpu.getId() + "]";
		else
			result += "[n.a.]";
		result += ", MAINBOARD";
		if(mainboard != null)
			result += "[" + mainboard.getId() + "]";
		else
			result += "[n.a.]";
		result += ", GRAPHICS";
		if(graphicBoard != null)
			result += "[" + graphicBoard.getId() + "]";
		else
			result += "[n.a.]";
		result += ", RAM";
		if(ramModules != null) {
			result +="[";
			for(RAM ram:ramModules) {
				result += ram.getId() + ",";
			}
			result = result.substring(0, result.length() - 1);
			result += "]";
		}
		return result;
	}
	
	private String toStringTests() {
		String result = "TESTED=" + isCompletelyTested() + " (";
		for(TestType testType:Tester.TestType.values()) {
			result += testType.toString() + "=" + testStates[testType.ordinal()] + ",";
		}
		result = result.substring(0, result.length() - 1);
		result += "), DEFECT=" + isDefect();
		return result;
	}
	
	private String toStringWorkers() {
		String result = "WORKERS(";
		if(workers != null && workers.size() > 0) {
			for(Worker worker:workers) {
				result += worker.getClass().getSimpleName() + "[" + worker.getId() + "],";
			}
			result = result.substring(0, result.length() - 1);
		}
		result += ")";
		return result;
	}
}
