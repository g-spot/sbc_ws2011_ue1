package at.ac.tuwien.complang.sbc11.parts;

import org.mozartspaces.capi3.Index;
import org.mozartspaces.capi3.Queryable;

@Queryable
public class CPU extends Part {

	private static final long serialVersionUID = 1331527406898166484L;
	
	public enum CPUType { SINGLE_CORE, DUAL_CORE, QUAD_CORE };
	
	@Index
	private CPUType cpuType;

	public CPUType getCpuType() {
		return cpuType;
	}

	public void setCpuType(CPUType cpuType) {
		this.cpuType = cpuType;
	}
	
	@Override
	public String toString() {
		return this.getCpuType().toString() + "[" + id + "], DEFECT=" + isDefect + ", PRODUCER[" + producer.getId() + "]";
	}
}
