package at.ac.tuwien.complang.sbc11.factory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants.Selecting;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsConstants.RequestTimeout;

import at.ac.tuwien.complang.sbc11.mozart.SpaceUtils;
import at.ac.tuwien.complang.sbc11.mozart.StandaloneServer;
import at.ac.tuwien.complang.sbc11.parts.CPU;
import at.ac.tuwien.complang.sbc11.parts.Computer;
import at.ac.tuwien.complang.sbc11.parts.Part;
import at.ac.tuwien.complang.sbc11.workers.Tester.TestType;

public class SharedWorkspaceMozartImpl implements SharedWorkspace {
	
	// TODO replace List with space
	private List<Part> parts;
	private ContainerReference workspaceContainer;
	private URI spaceURI;
	private MzsCore core;
	private Capi capi;
	
	public SharedWorkspaceMozartImpl() {
		parts = new ArrayList<Part>();
		try {
			spaceURI = new URI("xvsm://localhost:" + String.valueOf(StandaloneServer.SERVER_PORT));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		core = DefaultMzsCore.newInstance(0); // port 0 = choose a free port
		capi = new Capi(core);
		try {
			workspaceContainer = SpaceUtils.getOrCreateNamedFIFOContainer("FactoryWorkspace", spaceURI, capi);
		} catch (MzsCoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public long getNextPartId() {
		// TODO get a system wide unused identifier - take id from a container?
		return 0;
	}

	@Override
	public void addPart(Part part) {
		// TODO add part to space
		//parts.add(part);
		try {
			capi.write(workspaceContainer, new Entry(part, LindaCoordinator.newCoordinationData()));
		} catch (MzsCoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public List<Part> getAvailableParts() {
		// TODO return list of all parts in the space
		Selector partSelector = LindaCoordinator.newSelector(new CPU(), Selecting.COUNT_ALL);
		try {
			return capi.read(workspaceContainer, partSelector, RequestTimeout.TRY_ONCE, null);
		} catch (MzsCoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
