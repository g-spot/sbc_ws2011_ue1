package at.ac.tuwien.complang.sbc11.factory;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
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
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.NotificationManager;
import org.mozartspaces.notifications.Operation;

import at.ac.tuwien.complang.sbc11.factory.exception.SharedWorkspaceException;
import at.ac.tuwien.complang.sbc11.mozart.SpaceUtils;
import at.ac.tuwien.complang.sbc11.mozart.StandaloneServer;
import at.ac.tuwien.complang.sbc11.parts.CPU;
import at.ac.tuwien.complang.sbc11.parts.Computer;
import at.ac.tuwien.complang.sbc11.parts.Part;
import at.ac.tuwien.complang.sbc11.ui.Factory;
import at.ac.tuwien.complang.sbc11.workers.Tester.TestType;

public class SharedWorkspaceMozartImpl extends SharedWorkspace implements NotificationListener {

	private ContainerReference workspaceContainer;
	private URI spaceURI;
	private MzsCore core;
	private Capi capi;
	private NotificationManager notificationManager;
	
	public SharedWorkspaceMozartImpl(Factory factory) throws SharedWorkspaceException {
		super(factory);
		try {
			spaceURI = new URI("xvsm://localhost:" + String.valueOf(StandaloneServer.SERVER_PORT));
			core = DefaultMzsCore.newInstance(StandaloneServer.SERVER_PORT); // port 0 = choose a free port
			capi = new Capi(core);
			notificationManager = new NotificationManager(core);
			workspaceContainer = SpaceUtils.getOrCreateNamedFIFOContainer("FactoryWorkspace", spaceURI, capi);
			HashSet<Operation> operations = new HashSet<Operation>();
			operations.add(Operation.WRITE);
			operations.add(Operation.TAKE);
			operations.add(Operation.DELETE);
			notificationManager.createNotification(workspaceContainer, this, operations, null, null);
		} catch (MzsCoreException e) {
			throw new SharedWorkspaceException("Shared workspace could not be initialized: Error in MzsCore (" + e.getMessage() + ")");
		} catch (URISyntaxException e) {
			throw new SharedWorkspaceException("Shared workspace could not be initialized: Error with URI (" + e.getMessage() + ")");
		} catch (InterruptedException e) {
			throw new SharedWorkspaceException("Shared workspace could not be initialized: Error with UI notification (" + e.getMessage() + ")");
		}
	}

	@Override
	public long getNextPartId() {
		// TODO get a system wide unused identifier - take id from a container?
		return 0;
	}

	@Override
	public void addPart(Part part) throws SharedWorkspaceException {
		// TODO add part to space
		//parts.add(part);
		try {
			capi.write(workspaceContainer, new Entry(part, LindaCoordinator.newCoordinationData()));
		} catch (MzsCoreException e) {
			throw new SharedWorkspaceException("Part could not be written: Error in MzsCore (" + e.getMessage() + ")");
		}
	}

	@Override
	public List<Part> getAvailableParts() throws SharedWorkspaceException {
		Selector partSelector = LindaCoordinator.newSelector(new CPU(), Selecting.COUNT_ALL);
		try {
			return capi.read(workspaceContainer, partSelector, RequestTimeout.TRY_ONCE, null);
		} catch (MzsCoreException e) {
			throw new SharedWorkspaceException("Parts could not be read: Error in MzsCore (" + e.getMessage() + ")");
		}
	}

	@Override
	public List<Computer> getAvailableComputers() throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Computer> getTrashedComputers() throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Part takePart(Class<?> partType) throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addComputer(Computer computer) throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Computer takeUntestedComputer(TestType untestedFor) throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Computer takeCompleteComputer() throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addComputerToTrash(Computer computer) throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void entryOperationFinished(Notification source, Operation operation,
			List<? extends Serializable> entries) {
		// notify ui about update
		factory.updateBlackboard();
	}

}
