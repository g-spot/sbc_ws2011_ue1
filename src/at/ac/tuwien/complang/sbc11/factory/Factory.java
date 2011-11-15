package at.ac.tuwien.complang.sbc11.factory;

import java.util.List;

import at.ac.tuwien.complang.sbc11.parts.Part;

public interface Factory {
	public long getNextPartId();
	public void addPart(Part part);
	public List<Part> getAvailableParts();
}
