package at.ac.tuwien.complang.sbc11.factory;

import java.util.ArrayList;
import java.util.List;

import at.ac.tuwien.complang.sbc11.parts.Part;

public class MozartFactory implements Factory {
	
	// TODO replace List with space
	private List<Part> parts;
	
	public MozartFactory() {
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

}
