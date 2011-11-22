package at.ac.tuwien.complang.sbc11.factory;

import java.util.List;

import at.ac.tuwien.complang.sbc11.factory.exception.SharedWorkspaceException;
import at.ac.tuwien.complang.sbc11.parts.Computer;
import at.ac.tuwien.complang.sbc11.parts.Part;
import at.ac.tuwien.complang.sbc11.ui.Factory;
import at.ac.tuwien.complang.sbc11.workers.Tester.TestType;

/* Implements the shared workspace with an alternative technology.
 * For example:
 *   - RMI (see slides part III, slide 7 -> architecture limiter!!!
 *   - Sockets (same as RMI)
 *   - JMS (see slides part III, slide 12 -> possibly the best non-space-based approach)
 *   - ...
 * TODO Shorty: rename class to e.g. "SharedWorkspaceJMSImpl"
 * 
 */
public class SharedWorkspaceAlternativeImpl extends SharedWorkspace {

	public SharedWorkspaceAlternativeImpl(Factory factory) {
		super(factory);
		// TODO Auto-generated constructor stub
	}

	public SharedWorkspaceAlternativeImpl() {
		// TODO Auto-generated constructor stub
		super(null);
	}

	@Override
	public List<Part> getAvailableParts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Computer> getShippedComputers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Computer> getTrashedComputers() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getNextPartId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void addPart(Part part) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Part> takeParts(Class<?> partType, boolean blocking, int partCount) throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addComputer(Computer computer) {
		// TODO Auto-generated method stub

	}

	@Override
	public Computer takeUntestedComputer(TestType untestedFor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addComputerToTrash(Computer computer) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Computer> getIncompleteComputers()
			throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Computer takeCompletelyTestedComputer()
			throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void secureShutdown() throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startTransaction() throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void commitTransaction() throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void rollbackTransaction() throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shipComputer(Computer computer) throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		
	}

}
