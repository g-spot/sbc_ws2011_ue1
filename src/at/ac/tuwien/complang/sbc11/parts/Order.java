package at.ac.tuwien.complang.sbc11.parts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.complang.sbc11.parts.CPU.CPUType;

public class Order implements Serializable {

	private static final long serialVersionUID = -4183337383419669910L;

	private long id;
	private int computerCount;
	private CPUType cpuType;
	private int ramCount;
	private boolean usingGraphicBoard;
	
	private List<Computer> producedComputer;
	
	public Order(long id, int computerCount, CPUType cpuType, int ramCount, boolean usingGraphicBoard) {
		this.id = id;
		this.computerCount = computerCount;
		this.cpuType = cpuType;
		this.ramCount = ramCount;
		this.usingGraphicBoard = usingGraphicBoard;
		this.setProducedComputer(new ArrayList<Computer>());
	}
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public int getComputerCount() {
		return computerCount;
	}
	public void setComputerCount(int computerCount) {
		this.computerCount = computerCount;
	}
	public CPUType getCpuType() {
		return cpuType;
	}
	public void setCpuType(CPUType cpuType) {
		this.cpuType = cpuType;
	}
	public int getRamCount() {
		return ramCount;
	}
	public void setRamCount(int ramCount) {
		this.ramCount = ramCount;
	}
	public boolean isUsingGraphicBoard() {
		return usingGraphicBoard;
	}
	public void setUsingGraphicBoard(boolean useGraphicBoard) {
		this.usingGraphicBoard = useGraphicBoard;
	}

	public List<Computer> getProducedComputer() {
		return producedComputer;
	}

	public void setProducedComputer(List<Computer> producedComputer) {
		this.producedComputer = producedComputer;
	}

	@Override
	public String toString() {
		String result;
		final char NEWLINE = '\n';
		final String INDENT = "   ";
		
		result = "ORDER[" + id + "]" + NEWLINE;
		result += INDENT + "COUNT=" + computerCount + ", ";
		if(cpuType != null)
			result += "CPU=" + cpuType.toString() + NEWLINE;
		else
			result += "CPU=[n.a.]" + NEWLINE;
		result += INDENT + "RAM=" + ramCount + ", ";
		result += "GRAPHICS=" + usingGraphicBoard + NEWLINE;
		result += INDENT + "FINISHED " + producedComputer.size() + "/" + computerCount + " COMPUTERS";
		return result;
	}
}
