package at.ac.tuwien.complang.sbc11.factory;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.complang.sbc11.parts.Computer;
import at.ac.tuwien.complang.sbc11.parts.Part;
import at.ac.tuwien.complang.sbc11.workers.Tester.TestType;

public class SharedWorkspaceMozartImpl implements SharedWorkspace {
	
	// TODO replace List with space
	private List<Part> parts;
	
	public SharedWorkspaceMozartImpl() {
		parts = new ArrayList<Part>();
	}

	@Override
	public long getNextPartId() {
		// TODO get a system wide unused identifier - take id from a container?
		return 0;
	}

	@Override
	public void addPart(Part part) {
		// TODO add part to space
		parts.add(part);
	}

	@Override
	public List<Part> getAvailableParts() {
		// TODO return list of all parts in the space
		return parts;
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
