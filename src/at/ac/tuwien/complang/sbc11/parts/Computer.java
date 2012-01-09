package at.ac.tuwien.complang.sbc11.parts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.capi3.Index;
import org.mozartspaces.capi3.Queryable;

import at.ac.tuwien.complang.sbc11.workers.Tester.TestState;
import at.ac.tuwien.complang.sbc11.workers.Tester.TestType;
import at.ac.tuwien.complang.sbc11.workers.Worker;

@Queryable
public class Computer implements Serializable {

	private static final long serialVersionUID = 4357416927653129937L;

	// general information
	private long id;
	@Index
	private TestState completenessTested;
	@Index
	private TestState correctnessTested;
	@Index
	private Boolean deconstructed;
	
	// parts
	private CPU cpu = null;
	private Mainboard mainboard = null;
	private List<RAM> ramModules = null;
	private GraphicBoard graphicBoard = null;
	
	// who did the work
	private List<Worker> workers;
	
	// is the computer part of an order?
	@Index
	private Order order;
	
	public Computer() {
		ramModules = new ArrayList<RAM>();
		workers = new ArrayList<Worker>();
		completenessTested = TestState.NOT_TESTED;
		correctnessTested = TestState.NOT_TESTED;
		deconstructed = false;
	}
	
	/**
	 * returns true if at least one test has failed
	 */
	public boolean isDefect() {
		// returns true if at least one test has failed
		return (completenessTested == TestState.FAILED || correctnessTested == TestState.FAILED);
	}
	
	/**
	 * returns false if at least one test has not been done yet
	 */
	public boolean isCompletelyTested() {
		// returns false if at least one test has not been done yet
		return (completenessTested != TestState.NOT_TESTED && correctnessTested != TestState.NOT_TESTED);
	}
	
	/**
	 * returns true if the computer has been assigned a cpu, a mainboard and at least one ram module
	 */
	public boolean isComplete() {
		// returns true if the computer has been assigned
		// a cpu, a mainboard and at least one ram module
		return (cpu != null && mainboard != null && ramModules != null && ramModules.size() > 0);
	}

	// getters and setters
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public TestState getCompletenessTested() {
		return completenessTested;
	}

	public void setCompletenessTested(TestState completenessTested) {
		this.completenessTested = completenessTested;
	}

	public TestState getCorrectnessTested() {
		return correctnessTested;
	}

	public void setCorrectnessTested(TestState correctnessTested) {
		this.correctnessTested = correctnessTested;
	}

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
		final char NEWLINE = '\n';
		final String INDENT = "   ";
		String result = "COMPUTER[" + getId() + "]" + NEWLINE;
		result += INDENT + toStringParts() + NEWLINE;
		result += INDENT + toStringTests() + NEWLINE;
		result += INDENT + toStringDeconstructed() + NEWLINE;
		result += INDENT + toStringWorkers() + NEWLINE;
		result += INDENT + toStringOrder();
		return result;
	}
	
	private String toStringParts() {
		String result = "";
		if(cpu != null)
			result += cpu.getCpuType().toString() + "[" + cpu.getId() + "]";
		else
			result += "CPU[n.a.]";
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
		else
			result += "[n.a.]";
		return result;
	}
	
	private String toStringTests() {
		String result = "TESTED=" + isCompletelyTested() + " (";
		result += TestType.COMPLETENESS.toString() + "=";
		if(completenessTested != null)
			result +=completenessTested.toString() + ",";
		else
			result +="n.a.,";
		result += TestType.CORRECTNESS.toString() + "=";
		if(correctnessTested != null)
			result += correctnessTested.toString();
		else
			result += "n.a.";
		result += "), DEFECT=" + isDefect();
		return result;
	}
	
	private String toStringDeconstructed() {
		return "DECONSTRUCTED=" + this.deconstructed;
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
	
	private String toStringOrder() {
		String result = "";
		if(order != null)
			result = "ORDER[" + order.getId() + "]";
		else
			result = "NO ORDER";
		return result;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public Boolean isDeconstructed() {
		return deconstructed;
	}

	public void setDeconstructed(Boolean deconstructed) {
		this.deconstructed = deconstructed;
	}
}
