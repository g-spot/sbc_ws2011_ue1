package at.ac.tuwien.complang.sbc11.parts;

import java.io.Serializable;

import at.ac.tuwien.complang.sbc11.parts.CPU.CPUType;

public class Order implements Serializable {

	private static final long serialVersionUID = -4183337383419669910L;

	private long id;
	private CPUType cpuType;
	private int ramCount;
	private boolean usingGraphicBoard;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
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
}
