package at.ac.tuwien.complang.sbc11.factory;

import java.util.List;

import at.ac.tuwien.complang.sbc11.factory.exception.SharedWorkspaceException;
import at.ac.tuwien.complang.sbc11.parts.Computer;
import at.ac.tuwien.complang.sbc11.parts.Part;
import at.ac.tuwien.complang.sbc11.ui.Factory;
import at.ac.tuwien.complang.sbc11.workers.Tester.TestType;
import at.ac.tuwien.complang.sbc11.workers.Worker;

public abstract class SharedWorkspace {
	
	protected Factory factory;
	
	// the shared workspace gets a factory object to do callbacks to the ui
	public SharedWorkspace(Factory factory) {
		this.factory = factory;
	}
	
	// safely shuts down current instance of the shared workspace
	public abstract void secureShutdown() throws SharedWorkspaceException;
	
	// general methods
	public abstract List<Part> getAvailableParts() throws SharedWorkspaceException;
	public abstract List<Computer> getIncompleteComputers() throws SharedWorkspaceException;
	public abstract List<Computer> getShippedComputers() throws SharedWorkspaceException;
	public abstract List<Computer> getTrashedComputers() throws SharedWorkspaceException;
	
	// simple transaction control
	// distributed transactions won't work with mozart spaces
	// so if there's a blocking request, the state of the black board
	// is not guarenteed to be consistent with the actual state of the space
	// e.g.: assembler takes 1 entry of a fifo-container, 
	public abstract void startTransaction() throws SharedWorkspaceException;
	public abstract void commitTransaction() throws SharedWorkspaceException;
	public abstract void rollbackTransaction() throws SharedWorkspaceException;
	
	// methods for dealing with parts (tasks of the producer)
	public abstract long getNextPartId() throws SharedWorkspaceException;
	public abstract void addPart(Part part) throws SharedWorkspaceException;
	
	// methods for assembling computers (tasks of the assembler)
	public abstract long getNextComputerId() throws SharedWorkspaceException;
	public abstract List<Part> takeParts(Class<?> partType, boolean blocking, int partCount) throws SharedWorkspaceException;
	public abstract void addComputer(Computer computer) throws SharedWorkspaceException;
	
	// methods for testing computers (tasks of the tester)
	public abstract Computer takeUntestedComputer(TestType untestedFor) throws SharedWorkspaceException;
	// uses also addComputer to put the tested computer back into space
	
	// methods for shipping computers (tasks of the logistician)
	public abstract Computer takeCompletelyTestedComputer() throws SharedWorkspaceException;
	public abstract void shipComputer(Computer computer) throws SharedWorkspaceException;
	public abstract void addComputerToTrash(Computer computer) throws SharedWorkspaceException;
	
	// asynchronous takesParts
	public abstract void takePartsAsnchronous(Class<?> partType, boolean blocking, int partCount, Worker callback) throws SharedWorkspaceException;
}
