package at.ac.tuwien.complang.sbc11.parts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.complang.sbc11.workers.Tester;
import at.ac.tuwien.complang.sbc11.workers.Tester.TestType;
import at.ac.tuwien.complang.sbc11.workers.Worker;

public class Computer implements Serializable {

	private static final long serialVersionUID = 4357416927653129937L;

	// general information
	private Boolean haveTestsFailed[];
	
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
		haveTestsFailed = new Boolean[TestType.values().length];
		for(TestType testType:Tester.TestType.values()) {
			haveTestsFailed[testType.ordinal()] = null;
		}
		workers = new ArrayList<Worker>();
	}
	
	public void setTested(TestType testType, boolean wasSuccessful) {
		haveTestsFailed[testType.ordinal()] = wasSuccessful;
	}
	
	public boolean isDefect() {
		// returns true if at least one test has failed
		for(Boolean testFailed:haveTestsFailed) {
			if(testFailed != null && testFailed)
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
	
	public boolean isComplete() {
		// returns true if the computer has been assigned
		// a cpu, a mainboard and at least one ram module
		return (cpu != null && mainboard != null && ramModules != null && ramModules.size() > 0);
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
	
	@Override
	public String toString() {
		String result = "COMPUTER CPU=";
		if(cpu != null)
			result += "[" + cpu.getId() + "]";
		else
			result += "n.a.";
		result += ", MAINBOARD=";
		if(mainboard != null)
			result += "[" + mainboard.getId() + "]";
		else
			result += "n.a.";
		result += ", GRAPHICS=";
		if(graphicBoard != null)
			result += "[" + graphicBoard.getId() + "]";
		else
			result += "n.a.";
		result += ", RAM=";
		if(ramModules != null) {
			result +="[";
			for(RAM ram:ramModules) {
				result += ram.getId() + ",";
			}
			result = result.substring(0, result.length() - 1);
			result += "], ";
		}
		result += "TESTED=" + isCompletelyTested() + ", DEFECT=" + isDefect();
		
		return result;
	}
}
