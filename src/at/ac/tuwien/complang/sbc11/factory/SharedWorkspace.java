package at.ac.tuwien.complang.sbc11.factory;

import java.util.List;

import org.mozartspaces.notifications.NotificationListener;

import at.ac.tuwien.complang.sbc11.factory.exception.SharedWorkspaceException;
import at.ac.tuwien.complang.sbc11.parts.Computer;
import at.ac.tuwien.complang.sbc11.parts.Part;
import at.ac.tuwien.complang.sbc11.ui.Factory;
import at.ac.tuwien.complang.sbc11.workers.Tester.TestType;

public abstract class SharedWorkspace {
	
	protected Factory factory;
	
	// the shared workspace gets a factory object to do callbacks to the ui
	public SharedWorkspace(Factory factory) {
		this.factory = factory;
	}
	
	// hide default constructor
	private SharedWorkspace() {}
	
	// general methods
	public abstract List<Part> getAvailableParts() throws SharedWorkspaceException;
	public abstract List<Computer> getAvailableComputers() throws SharedWorkspaceException;
	public abstract List<Computer> getTrashedComputers() throws SharedWorkspaceException;
	
	// methods for dealing with parts (tasks of the producer)
	public abstract long getNextPartId() throws SharedWorkspaceException;
	public abstract void addPart(Part part) throws SharedWorkspaceException;
	
	// methods for assembling computers (tasks of the assembler)
	public abstract Part takePart(Class<?> partType) throws SharedWorkspaceException;
	public abstract void addComputer(Computer computer) throws SharedWorkspaceException;
	
	// methods for testing computers (tasks of the tester)
	public abstract Computer takeUntestedComputer(TestType untestedFor) throws SharedWorkspaceException;
	// uses also addComputer to put the tested computer back into space
	
	// methods for shipping computers (tasks of the logistician)
	public abstract Computer takeCompleteComputer() throws SharedWorkspaceException;
	public abstract void addComputerToTrash(Computer computer) throws SharedWorkspaceException;
	// uses also addComputer to put the error-free computer back into space
}
