package at.ac.tuwien.complang.sbc11.factory;

import java.util.List;

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
public class SharedWorkspaceJMSImpl extends SharedWorkspace {

	public SharedWorkspaceJMSImpl(Factory factory) {
		super(factory);
		// TODO Auto-generated constructor stub
	}

	@Override
	public List<Part> getAvailableParts() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Computer> getAvailableComputers() {
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
	public Part takePart(Class<?> partType) {
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
	public Computer takeCompleteComputer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addComputerToTrash(Computer computer) {
		// TODO Auto-generated method stub

	}

}
