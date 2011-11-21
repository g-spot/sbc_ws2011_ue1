package at.ac.tuwien.complang.sbc11.factory;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsConstants.Selecting;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.NotificationManager;
import org.mozartspaces.notifications.Operation;

import at.ac.tuwien.complang.sbc11.factory.exception.SharedWorkspaceException;
import at.ac.tuwien.complang.sbc11.mozart.SpaceUtils;
import at.ac.tuwien.complang.sbc11.mozart.StandaloneServer;
import at.ac.tuwien.complang.sbc11.parts.CPU;
import at.ac.tuwien.complang.sbc11.parts.Computer;
import at.ac.tuwien.complang.sbc11.parts.GraphicBoard;
import at.ac.tuwien.complang.sbc11.parts.Mainboard;
import at.ac.tuwien.complang.sbc11.parts.Part;
import at.ac.tuwien.complang.sbc11.parts.RAM;
import at.ac.tuwien.complang.sbc11.ui.Factory;
import at.ac.tuwien.complang.sbc11.workers.Tester.TestType;

public class SharedWorkspaceMozartImpl extends SharedWorkspace implements NotificationListener {

	private URI spaceURI;
	private MzsCore core;
	private Capi capi;
	private NotificationManager notificationManager;
	
	private ContainerReference partIdContainer;
	private ContainerReference partContainer;
	private ContainerReference mainboardContainer;
	private Logger logger;
	
	public SharedWorkspaceMozartImpl(Factory factory) throws SharedWorkspaceException {
		super(factory);
		logger = Logger.getAnonymousLogger();
		try {
			spaceURI = new URI("xvsm://localhost:" + String.valueOf(StandaloneServer.SERVER_PORT));
			core = DefaultMzsCore.newInstance(StandaloneServer.SERVER_PORT); // port 0 = choose a free port
			
			// uses external standalone server:
			//core = DefaultMzsCore.newInstance(0);
			
			capi = new Capi(core);
			
			// create container
			partIdContainer = SpaceUtils.getOrCreatePartIDContainer(spaceURI, capi);
			partContainer = SpaceUtils.getOrCreateLindaContainer(SpaceUtils.CONTAINER_PARTS, spaceURI, capi);
			mainboardContainer = SpaceUtils.getOrCreateFIFOContainer(SpaceUtils.CONTAINER_MAINBOARDS, spaceURI, capi);
			
			// init notifications
			notificationManager = new NotificationManager(core);
			HashSet<Operation> operations = new HashSet<Operation>();
			operations.add(Operation.WRITE);
			operations.add(Operation.TAKE);
			operations.add(Operation.DELETE);
			notificationManager.createNotification(partContainer, this, operations, null, null);
			notificationManager.createNotification(mainboardContainer, this, operations, null, null);
			
		} catch (MzsCoreException e) {
			throw new SharedWorkspaceException("Shared workspace could not be initialized: Error in MzsCore (" + e.getMessage() + ")");
		} catch (URISyntaxException e) {
			throw new SharedWorkspaceException("Shared workspace could not be initialized: Error with URI (" + e.getMessage() + ")");
		} catch (InterruptedException e) {
			throw new SharedWorkspaceException("Shared workspace could not be initialized: Error with UI notification (" + e.getMessage() + ")");
		}
	}

	@Override
	public long getNextPartId() throws SharedWorkspaceException {
		long nextID = 0;
		TransactionReference transaction = null;
		try {
			transaction = capi.createTransaction(MzsConstants.TransactionTimeout.INFINITE, spaceURI);
			Selector idSelector = AnyCoordinator.newSelector(1);
			
			// let's see if there is already an id in the container:
			try {
				capi.test(partIdContainer);
			} catch(MzsCoreException e) {
				// no --> then insert the first one
				nextID = 1;
				capi.write(new Entry(new Long(1)), partIdContainer, RequestTimeout.TRY_ONCE, transaction);
				capi.commitTransaction(transaction);
				return nextID;
			}
			
			// if the container contains an id: take the id, increment it and insert it again
			if(nextID == 0) {
				//List<Selector> selectors = Collections.singletonList(idSelector);
				ArrayList<Long> result = capi.take(partIdContainer, idSelector, RequestTimeout.TRY_ONCE, transaction);
				nextID = result.get(0);
				nextID++;

				capi.write(partIdContainer, RequestTimeout.TRY_ONCE, transaction, new Entry(nextID));
			}
			
			capi.commitTransaction(transaction);
		} catch (MzsCoreException e) {
			try {
				capi.rollbackTransaction(transaction);
			} catch (MzsCoreException e1) {
				throw new SharedWorkspaceException("Next part id could not be retrieved: Error during rollback of transaction (" + e1.getMessage() + ")");
			}
			throw new SharedWorkspaceException("Next part id could not be retrieved: Error in MzsCore (" + e.getMessage() + ")");
		} catch(Exception e) {
			try {
				capi.rollbackTransaction(transaction);
			} catch (MzsCoreException e1) {
				throw new SharedWorkspaceException("Next part id could not be retrieved: Error during rollback of transaction (" + e1.getMessage() + ")");
			}
			throw new SharedWorkspaceException("Next part id could not be retrieved: Other error (" + e.getMessage() + ")");
		}
		return nextID;
	}

	@Override
	public void addPart(Part part) throws SharedWorkspaceException {
		try {
			// if part is a mainboard, insert into mainboardContainer
			if(part.getClass().equals(Mainboard.class))
				capi.write(mainboardContainer, new Entry(part, FifoCoordinator.newCoordinationData()));
			else
				capi.write(partContainer, new Entry(part, LindaCoordinator.newCoordinationData()));
		} catch (MzsCoreException e) {
			throw new SharedWorkspaceException("Part could not be written: Error in MzsCore (" + e.getMessage() + ")");
		}
	}

	@Override
	public List<Part> getAvailableParts() throws SharedWorkspaceException {
		List<Part> result = new ArrayList<Part>();
		List<CPU> cpuList = null;
		List<RAM> ramList = null;
		List<GraphicBoard> graphicBoardList = null;
		List<Mainboard> mainboardList = null;
		// did not find out how to get all part types at once if there is one part type where no part exists
		// workaround: one request per part type
		Selector cpuSelector = LindaCoordinator.newSelector(new CPU(), Selecting.COUNT_MAX);
		Selector ramSelector = LindaCoordinator.newSelector(new RAM(), Selecting.COUNT_MAX);
		Selector graphicBoardSelector = LindaCoordinator.newSelector(new GraphicBoard(), Selecting.COUNT_MAX);
		Selector mainboardSelector = FifoCoordinator.newSelector(Selecting.COUNT_MAX);
		
		TransactionReference transaction = null;
		try {
			transaction = capi.createTransaction(RequestTimeout.INFINITE, spaceURI);
			cpuList = capi.read(partContainer, cpuSelector, RequestTimeout.TRY_ONCE, transaction);
			ramList = capi.read(partContainer, ramSelector, RequestTimeout.TRY_ONCE, transaction);
			graphicBoardList = capi.read(partContainer, graphicBoardSelector, RequestTimeout.TRY_ONCE, transaction);
			mainboardList = capi.read(mainboardContainer, mainboardSelector, RequestTimeout.TRY_ONCE, transaction);
			result.addAll(cpuList);
			result.addAll(ramList);
			result.addAll(graphicBoardList);
			result.addAll(mainboardList);
			
		} catch (MzsCoreException e) {
			throw new SharedWorkspaceException("Parts could not be read: Error in MzsCore (" + e.getMessage() + ")");
		} finally {
			try {
				capi.commitTransaction(transaction);
			} catch (MzsCoreException e) {
				throw new SharedWorkspaceException("Parts could not be read: Error with transaction (" + e.getMessage() + ")");
			}
		}
		return result;
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
