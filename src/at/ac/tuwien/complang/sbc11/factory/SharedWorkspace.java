package at.ac.tuwien.complang.sbc11.factory;

import java.util.List;

import at.ac.tuwien.complang.sbc11.parts.Computer;
import at.ac.tuwien.complang.sbc11.parts.Part;
import at.ac.tuwien.complang.sbc11.workers.Tester.TestType;

public interface SharedWorkspace {
	
	// general methods
	public List<Part> getAvailableParts();
	public List<Computer> getAvailableComputers();
	public List<Computer> getTrashedComputers();
	
	// methods for dealing with parts (tasks of the producer)
	public long getNextPartId();
	public void addPart(Part part);
	
	// methods for assembling computers (tasks of the assembler)
	public Part takePart(Class<?> partType);
	public void addComputer(Computer computer);
	
	// methods for testing computers (tasks of the tester)
	public Computer takeUntestedComputer(TestType untestedFor);
	// uses also addComputer to put the tested computer back into space
	
	// methods for shipping computers (tasks of the logistician)
	public Computer takeCompleteComputer();
	public void addComputerToTrash(Computer computer);
	// uses also addComputer to put the error-free computer back into space
}
