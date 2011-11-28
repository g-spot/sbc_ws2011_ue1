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
public class SharedWorkspaceJMSImpl extends SharedWorkspace {
	
	public SharedWorkspaceJMSImpl() {
		super(null);
		// TODO shorty: dieser konstruktor wird von den workern verwendet
		// der andere (mit dem Factory-Objekt) wird vom GUI verwendet, factory ist das callback-Objekt
		// fŸr GUI-€nderungen
		// zu Bedenken: der Konstruktor ohne Parameter wird beliebig oft aufgerufen, 
		// deshalb dŸrfen hier keine systemweiten Initialisierungen rein
		// --> sonst Ÿberschreibst du dir eventuell Sachen, die schon in Verwendung sind
		// der Konstruktor mit Factory-Parameter wird definitiv immer als erstes und auch nur 1x
		// aufgerufen, deshalb kšnnen systemweite Initialisierungen (falls vorhanden) dort rein
	}

	public SharedWorkspaceJMSImpl(Factory factory) {
		super(factory);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void secureShutdown() throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Part> getAvailableParts() throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Computer> getIncompleteComputers()
			throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Computer> getShippedComputers() throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Computer> getTrashedComputers() throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		return null;
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
	public long getNextPartId() throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void addPart(Part part) throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Part> takeParts(Class<?> partType, boolean blocking,
			int partCount) throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addComputer(Computer computer) throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Computer takeUntestedComputer(TestType untestedFor)
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
	public void shipComputer(Computer computer) throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addComputerToTrash(Computer computer)
			throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long getNextComputerId() {
		// TODO Auto-generated method stub
		return 0;
	}


}
