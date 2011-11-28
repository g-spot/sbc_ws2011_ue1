package at.ac.tuwien.complang.sbc11.factory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.CoordinationData;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.IsolationLevel;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsConstants.Selecting;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.notifications.NotificationManager;
import org.mozartspaces.notifications.Operation;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

import at.ac.tuwien.complang.sbc11.factory.exception.SharedWorkspaceException;
import at.ac.tuwien.complang.sbc11.mozart.SpaceUtils;
import at.ac.tuwien.complang.sbc11.mozart.StandaloneServer;
import at.ac.tuwien.complang.sbc11.mozart.listeners.IncompleteComputerNotificationListener;
import at.ac.tuwien.complang.sbc11.mozart.listeners.PartNotificationListener;
import at.ac.tuwien.complang.sbc11.mozart.listeners.ShippedComputerNotificationListener;
import at.ac.tuwien.complang.sbc11.mozart.listeners.TrashedComputerNotificationListener;
import at.ac.tuwien.complang.sbc11.parts.CPU;
import at.ac.tuwien.complang.sbc11.parts.Computer;
import at.ac.tuwien.complang.sbc11.parts.GraphicBoard;
import at.ac.tuwien.complang.sbc11.parts.Mainboard;
import at.ac.tuwien.complang.sbc11.parts.Part;
import at.ac.tuwien.complang.sbc11.parts.RAM;
import at.ac.tuwien.complang.sbc11.ui.Factory;
import at.ac.tuwien.complang.sbc11.workers.Tester.TestState;
import at.ac.tuwien.complang.sbc11.workers.Tester.TestType;

public class SharedWorkspaceMozartImpl extends SharedWorkspace {
	
	private URI spaceURI;
	private MzsCore core;
	private Capi capi;
	private NotificationManager notificationManager;
	
	// containers
	private ContainerReference partIdContainer;
	private ContainerReference partContainer;
	private ContainerReference mainboardContainer;
	private ContainerReference incompleteContainer;
	private ContainerReference trashedContainer;
	private ContainerReference shippedContainer;
	private Logger logger;
	
	// for transaction purposes
	private TransactionReference currentTransaction = null;
	
	// global constants
	private final String LABEL_COMPLETELY_TESTED = "label_completely_tested";
	private final String LABEL_NOT_COMPLETELY_TESTED = "label_not_completely_tested";
	
	/**
	 * the default constructor is used by assembler, testers and logisticians
	 * it does no things that should be done only once, e.g. initializing system wide notifications
	 * @throws SharedWorkspaceException
	 */
	public SharedWorkspaceMozartImpl() throws SharedWorkspaceException {
		super(null);
		logger = Logger.getLogger("at.ac.tuwien.complang.sbc11.factory.SharedWorkspaceMozartImpl");
		try {
			initCoreLogging();
			spaceURI = new URI("xvsm://localhost:" + String.valueOf(StandaloneServer.SERVER_PORT));
			core = DefaultMzsCore.newInstance(0);
			capi = new Capi(core);
			
			// get container
			logger.info("Retrieving containers...");
			partIdContainer = SpaceUtils.getOrCreatePartIDContainer(spaceURI, capi);
			partContainer = SpaceUtils.getOrCreateLindaContainer(SpaceUtils.CONTAINER_PARTS, spaceURI, capi);
			mainboardContainer = SpaceUtils.getOrCreateFIFOContainer(SpaceUtils.CONTAINER_MAINBOARDS, spaceURI, capi);
			incompleteContainer = SpaceUtils.getOrCreateIncompleteContainer(spaceURI, capi);
			trashedContainer = SpaceUtils.getOrCreateAnyContainer(SpaceUtils.CONTAINER_TRASHED, spaceURI, capi);
			shippedContainer = SpaceUtils.getOrCreateAnyContainer(SpaceUtils.CONTAINER_SHIPPED, spaceURI, capi);
		} catch (MzsCoreException e) {
			throw new SharedWorkspaceException("Shared workspace could not be initialized: Error in MzsCore (" + e.getMessage() + ")");
		} catch (URISyntaxException e) {
			throw new SharedWorkspaceException("Shared workspace could not be initialized: Error with URI (" + e.getMessage() + ")");
		}
	}
	
	/**
	 * creates the space based implementation of the shared workspace
	 * should be called only once in the system - by the ui
	 * @param factory
	 * 				callback object for notification of workspace changes to the ui
	 * @throws SharedWorkspaceException
	 */
	public SharedWorkspaceMozartImpl(Factory factory) throws SharedWorkspaceException {
		super(factory);
		logger = Logger.getLogger("at.ac.tuwien.complang.sbc11.factory.SharedWorkspaceMozartImpl");
		try {
			initCoreLogging();
			spaceURI = new URI("xvsm://localhost:" + String.valueOf(StandaloneServer.SERVER_PORT));
			core = DefaultMzsCore.newInstance(StandaloneServer.SERVER_PORT); // port 0 = choose a free port
			
			// uses external standalone server:
			//core = DefaultMzsCore.newInstance(0);
			
			capi = new Capi(core);
			
			// create container
			logger.info("Retrieving containers...");
			partIdContainer = SpaceUtils.getOrCreatePartIDContainer(spaceURI, capi);
			partContainer = SpaceUtils.getOrCreateLindaContainer(SpaceUtils.CONTAINER_PARTS, spaceURI, capi);
			mainboardContainer = SpaceUtils.getOrCreateFIFOContainer(SpaceUtils.CONTAINER_MAINBOARDS, spaceURI, capi);
			incompleteContainer = SpaceUtils.getOrCreateIncompleteContainer(spaceURI, capi);
			trashedContainer = SpaceUtils.getOrCreateAnyContainer(SpaceUtils.CONTAINER_TRASHED, spaceURI, capi);
			shippedContainer = SpaceUtils.getOrCreateAnyContainer(SpaceUtils.CONTAINER_SHIPPED, spaceURI, capi);
			
			// store all containers in a hashmap
			HashMap<String, String> containerMap = new HashMap<String, String>();
			containerMap.put(partIdContainer.getStringRepresentation(), SpaceUtils.CONTAINER_PART_ID);
			containerMap.put(partContainer.getStringRepresentation(), SpaceUtils.CONTAINER_PARTS);
			containerMap.put(mainboardContainer.getStringRepresentation(), SpaceUtils.CONTAINER_MAINBOARDS);
			containerMap.put(incompleteContainer.getStringRepresentation(), SpaceUtils.CONTAINER_INCOMPLETE);
			containerMap.put(trashedContainer.getStringRepresentation(), SpaceUtils.CONTAINER_TRASHED);
			containerMap.put(shippedContainer.getStringRepresentation(), SpaceUtils.CONTAINER_SHIPPED);
			
			// init notifications
			logger.info("Registering notifications...");
			notificationManager = new NotificationManager(core);
			HashSet<Operation> operations = new HashSet<Operation>();
			operations.add(Operation.WRITE);
			operations.add(Operation.TAKE);
			operations.add(Operation.DELETE);
			notificationManager.createNotification(partContainer, new PartNotificationListener(factory, containerMap), operations, null, null);
			notificationManager.createNotification(mainboardContainer, new PartNotificationListener(factory, containerMap), operations, null, null);
			notificationManager.createNotification(incompleteContainer, new IncompleteComputerNotificationListener(factory, containerMap), operations, null, null);
			notificationManager.createNotification(trashedContainer, new TrashedComputerNotificationListener(factory, containerMap), operations, null, null);
			notificationManager.createNotification(shippedContainer, new ShippedComputerNotificationListener(factory, containerMap), operations, null, null);
			
		} catch (MzsCoreException e) {
			throw new SharedWorkspaceException("Shared workspace could not be initialized: Error in MzsCore (" + e.getMessage() + ")");
		} catch (URISyntaxException e) {
			throw new SharedWorkspaceException("Shared workspace could not be initialized: Error with URI (" + e.getMessage() + ")");
		} catch (InterruptedException e) {
			throw new SharedWorkspaceException("Shared workspace could not be initialized: Error with UI notification (" + e.getMessage() + ")");
		}
	}
	
	private void initCoreLogging() {
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		JoranConfigurator configurator = new JoranConfigurator();
		configurator.setContext(context);
		context.reset();
		try {
			configurator.doConfigure("logback.xml");
		} catch (JoranException e) {
			logger.warning("Could not configure core logging.");
		}
	}
	
	/**
	 * does a secure shutdown of the MzsCore
	 */
	@Override
	public void secureShutdown() throws SharedWorkspaceException {
		if(currentTransaction != null)
			try {
				capi.rollbackTransaction(currentTransaction);
			} catch (MzsCoreException e) {
				throw new SharedWorkspaceException("Last transaction could not be royblacked: Error in MzsCore (" + e.getMessage() + ")");
			}
		core.shutdown(true);
	}

	/**
	 * gets the next system wide identifier for parts
	 * uses an autonomous transaction (i.e. this method is not affected by
	 * the built-in simple transaction control of the class)
	 * @return the next part id
	 */
	@Override
	public long getNextPartId() throws SharedWorkspaceException {
		logger.info("Starting getNextPartId()...");
		logger.info("CURRENT TRANSACTION=" + currentTransaction);
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
				logger.info("Finished.");
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
		logger.info("Finished.");
		return nextID;
	}

	/**
	 * adds a new part to either the mainboardContainer (if the part is a mainboard)
	 * or the partContainer (for all other parts)
	 * uses the current simple transaction, if one exists
	 * @param part
	 * 				the part to insert
	 */
	@Override
	public void addPart(Part part) throws SharedWorkspaceException {
		logger.info("Starting addPart()...");
		logger.info("CURRENT TRANSACTION=" + currentTransaction);
		try {
			// if part is a mainboard, insert into mainboardContainer
			if(part.getClass().equals(Mainboard.class))
				capi.write(new Entry(part, FifoCoordinator.newCoordinationData()), mainboardContainer, RequestTimeout.DEFAULT, currentTransaction);
			else
				capi.write(new Entry(part, LindaCoordinator.newCoordinationData()), partContainer, RequestTimeout.DEFAULT, currentTransaction);
		} catch (MzsCoreException e) {
			throw new SharedWorkspaceException("Part could not be written: Error in MzsCore (" + e.getMessage() + ")");
		}
		logger.info("Finished.");
	}

	/**
	 * reads and returns a list of all available (unused) parts in the containers
	 * partContainer and mainbordContainer
	 * uses an autonomous transaction (i.e. this method is not affected by
	 * the built-in simple transaction control of the class)
	 */
	@Override
	public List<Part> getAvailableParts() throws SharedWorkspaceException {
		logger.info("Starting getAvailableParts()...");
		logger.info("CURRENT TRANSACTION=" + currentTransaction);
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
			/*cpuList = capi.read(partContainer, cpuSelector, RequestTimeout.TRY_ONCE, transaction);
			ramList = capi.read(partContainer, ramSelector, RequestTimeout.TRY_ONCE, transaction);
			graphicBoardList = capi.read(partContainer, graphicBoardSelector, RequestTimeout.TRY_ONCE, transaction);
			mainboardList = capi.read(mainboardContainer, mainboardSelector, RequestTimeout.TRY_ONCE, transaction);*/
			cpuList = capi.read(partContainer, Arrays.asList(cpuSelector), RequestTimeout.TRY_ONCE, transaction, IsolationLevel.READ_COMMITTED, null);
			ramList = capi.read(partContainer, Arrays.asList(ramSelector), RequestTimeout.TRY_ONCE, transaction, IsolationLevel.READ_COMMITTED, null);
			graphicBoardList = capi.read(partContainer, Arrays.asList(graphicBoardSelector), RequestTimeout.TRY_ONCE, transaction, IsolationLevel.READ_COMMITTED, null);
			mainboardList = capi.read(mainboardContainer, Arrays.asList(mainboardSelector), RequestTimeout.TRY_ONCE, transaction, IsolationLevel.READ_COMMITTED, null);
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
		logger.info("Finished.");
		return result;
	}
	
	/**
	 * returns all incomplete computers in the shared workspace
	 * uses the current simple transaction, if one exists
	 * @return list of computers
	 */
	@Override
	public List<Computer> getIncompleteComputers()
			throws SharedWorkspaceException {
		logger.info("Starting getIncompleteComputers()...");
		logger.info("CURRENT TRANSACTION=" + currentTransaction);
		Computer pattern = new Computer();
		pattern.setCompletenessTested(null); // take any computers
		pattern.setCorrectnessTested(null);
		Selector computerSelector = LindaCoordinator.newSelector(pattern, Selecting.COUNT_MAX);
		try {
			logger.info("Finished.");
			return capi.read(incompleteContainer, computerSelector, RequestTimeout.TRY_ONCE, currentTransaction);
		} catch (MzsCoreException e) {
			throw new SharedWorkspaceException("Parts could not be read: Error in MzsCore (" + e.getMessage() + ")");
		}
	}

	/**
	 * returns all shipped computers
	 * @return list of computers
	 */
	@Override
	public List<Computer> getShippedComputers() throws SharedWorkspaceException {
		logger.info("Starting getShippedComputers()...");
		logger.info("CURRENT TRANSACTION=" + currentTransaction);
		
		Selector computerSelector = AnyCoordinator.newSelector(Selecting.COUNT_MAX);
		try {
			logger.info("Finished.");
			return capi.read(shippedContainer, computerSelector, RequestTimeout.TRY_ONCE, currentTransaction);
		} catch (MzsCoreException e) {
			throw new SharedWorkspaceException("Parts could not be read: Error in MzsCore (" + e.getMessage() + ")");
		}
	}

	/**
	 * returns all trashed computers
	 * @return list of computers
	 */
	@Override
	public List<Computer> getTrashedComputers() throws SharedWorkspaceException {
		logger.info("Starting getTrashedComputers()...");
		logger.info("CURRENT TRANSACTION=" + currentTransaction);
		
		Selector computerSelector = AnyCoordinator.newSelector(Selecting.COUNT_MAX);
		try {
			logger.info("Finished.");
			return capi.read(trashedContainer, computerSelector, RequestTimeout.TRY_ONCE, currentTransaction);
		} catch (MzsCoreException e) {
			throw new SharedWorkspaceException("Parts could not be read: Error in MzsCore (" + e.getMessage() + ")");
		}
	}

	/**
	 * takes a part from the shared workspace, i.e. returns the object and removes it
	 * uses the current simple transaction, if one exists
	 * @param partType
	 * 				class type of the part
	 * @param blocking
	 * 				if true, takePart() waits until a part can be found and returned
	 * 				if false, takePart() tries only once to get the part
	 * @param partCount
	 * 				specifies the number of parts to be taken
	 * @return a part of type @partType, null if @blocking = false and no @partCount parts can be found
	 */
	@Override
	public List<Part> takeParts(Class<?> partType, boolean blocking, int partCount) throws SharedWorkspaceException {
		logger.info("Starting takeParts()...");
		logger.info("CURRENT TRANSACTION=" + currentTransaction);
		List<Part> result = null;
		Selector partSelector = null;
		ContainerReference container = null;
		List<Selector> selectorList = new ArrayList<Selector>();
		
		if(partType.equals(Mainboard.class))
		{
			partSelector = FifoCoordinator.newSelector(partCount);
			container = mainboardContainer;
			selectorList.add(partSelector);
		}
		else
		{
			try {
				partSelector = LindaCoordinator.newSelector((Part)partType.newInstance(), partCount);
				selectorList.add(partSelector);
			} catch (InstantiationException e) {
				throw new SharedWorkspaceException("Part could not be taken: Part Type could not be instantiated (" + e.getMessage() + ")");
			} catch (IllegalAccessException e) {
				throw new SharedWorkspaceException("Part could not be taken: Part Type could not be instantiated (" + e.getMessage() + ")");
			}
			container = partContainer;
		}
		
		try {
			// TODO test isolation level READ_COMMITTED
			//result = capi.take(container, selectorList, RequestTimeout.INFINITE, currentTransaction, IsolationLevel.READ_COMMITTED, null);
			if(blocking)
				//result = capi.take(container, partSelector, RequestTimeout.INFINITE, currentTransaction);
				result = capi.take(container, selectorList, RequestTimeout.INFINITE, currentTransaction, IsolationLevel.READ_COMMITTED, null);
			else
			{
				try {
					//result = capi.take(container, partSelector, RequestTimeout.TRY_ONCE, currentTransaction);
					result = capi.take(container, selectorList, RequestTimeout.TRY_ONCE, currentTransaction, IsolationLevel.READ_COMMITTED, null);
				} catch(CountNotMetException e) {
					// ok with that, return null
					logger.info("Finished.");
					return null;
				}
			}
			
			if(result.isEmpty())
			{
				logger.info("Finished.");
				return null;
			}
			
		} catch (MzsCoreException e) {
			e.printStackTrace();
			throw new SharedWorkspaceException("Part could not be taken: Error in MzsCore (" + e.getMessage() + ")");
		} catch (IndexOutOfBoundsException e) {
			throw new SharedWorkspaceException("Part could not be taken: Index out of bounds (" + e.getMessage() + ")");
		}
		logger.info("Finished.");
		return result;
	}

	/**
	 * adds a fresh new computer to the workspace (i.e. to the incomplete computer container)
	 * if the computer is completely tested, it is labeled as completely tested
	 * uses the current simple transaction, if one exists
	 * @param computer
	 * 				the computer to add
	 */
	@Override
	public void addComputer(Computer computer) throws SharedWorkspaceException {
		logger.info("Starting addUntestedComputer()...");
		logger.info("CURRENT TRANSACTION=" + currentTransaction);
		
		String label = null;
		if(computer.isCompletelyTested())
			label = LABEL_COMPLETELY_TESTED;
		else
			label = LABEL_NOT_COMPLETELY_TESTED;
		
		List<CoordinationData> coordinationData = new ArrayList<CoordinationData>();
		coordinationData.add(LindaCoordinator.newCoordinationData());
		coordinationData.add(LabelCoordinator.newCoordinationData(label));
		
		Entry entry = new Entry(computer, coordinationData);
		try {
			capi.write(entry, incompleteContainer, RequestTimeout.DEFAULT, currentTransaction);
		} catch (MzsCoreException e) {
			throw new SharedWorkspaceException("Computer could not be written: Error in MzsCore (" + e.getMessage() + ")");
		}
		logger.info("Finished.");
	}

	/**
	 * takes a computer from the space which is not tested yet for the given TestType
	 * @param untestedFor
	 * 				the test type
	 * @return a computer object, which is not tested for the given TestType - the method blocks until a untested computer arrives in the space
	 */
	@Override
	public Computer takeUntestedComputer(TestType untestedFor) throws SharedWorkspaceException {
		logger.info("Starting takeUntestedComputer()...");
		logger.info("CURRENT TRANSACTION=" + currentTransaction);
		
		Computer pattern = new Computer();
		// set the desired TestType to NOT_TESTED, the other to null (to ignore the value)
		if(untestedFor.equals(TestType.COMPLETENESS)) {
			pattern.setCompletenessTested(TestState.NOT_TESTED);
			pattern.setCorrectnessTested(null);
		} else if(untestedFor.equals(TestType.CORRECTNESS)) {
			pattern.setCompletenessTested(null);
			pattern.setCorrectnessTested(TestState.NOT_TESTED);
		}
		Selector computerSelector = LindaCoordinator.newSelector(pattern, 1);
		
		try {
			logger.info("trying to get computer with pattern:");
			logger.info(pattern.toString());
			List<Computer> computerList = capi.take(incompleteContainer, computerSelector, RequestTimeout.INFINITE, currentTransaction);
			if(computerList != null && computerList.size() > 0)
			{
				logger.info("Finished.");
				return computerList.get(0);
			}
		} catch (MzsCoreException e) {
			throw new SharedWorkspaceException("Parts could not be read: Error in MzsCore (" + e.getMessage() + ")");
		}
		return null;
	}

	/**
	 * adds a computer to the shipped container
	 * @param computer
	 * 				the computer
	 */
	@Override
	public void shipComputer(Computer computer) throws SharedWorkspaceException {
		logger.info("Starting shipComputer()...");
		logger.info("CURRENT TRANSACTION=" + currentTransaction);
		try {
			capi.write(new Entry(computer, AnyCoordinator.newCoordinationData()), shippedContainer, RequestTimeout.DEFAULT, currentTransaction);
		} catch (MzsCoreException e) {
			throw new SharedWorkspaceException("Computer could not be written: Error in MzsCore (" + e.getMessage() + ")");
		}
		logger.info("Finished.");
	}

	/**
	 * adds a computer to the trash container
	 * @param computer
	 * 				the computer
	 */
	@Override
	public void addComputerToTrash(Computer computer) throws SharedWorkspaceException {
		logger.info("Starting addComputerToTrash()...");
		logger.info("CURRENT TRANSACTION=" + currentTransaction);
		try {
			capi.write(new Entry(computer, AnyCoordinator.newCoordinationData()), trashedContainer, RequestTimeout.DEFAULT, currentTransaction);
		} catch (MzsCoreException e) {
			throw new SharedWorkspaceException("Computer could not be written: Error in MzsCore (" + e.getMessage() + ")");
		}
		logger.info("Finished.");
	}
	
	/**
	 * takes a completely tested computer from the shared workspace
	 * @return the computer
	 */
	@Override
	public Computer takeCompletelyTestedComputer()
			throws SharedWorkspaceException {
		logger.info("Starting takeCompletelyTestedComputer()...");
		logger.info("CURRENT TRANSACTION=" + currentTransaction);
		
		Selector computerSelector = LabelCoordinator.newSelector(LABEL_COMPLETELY_TESTED, 1);
		
		try {
			List<Computer> computerList = capi.take(incompleteContainer, computerSelector, RequestTimeout.INFINITE, currentTransaction);
			if(computerList != null && computerList.size() > 0)
			{
				logger.info("Finished.");
				return computerList.get(0);
			}
		} catch (MzsCoreException e) {
			throw new SharedWorkspaceException("Parts could not be read: Error in MzsCore (" + e.getMessage() + ")");
		}
		return null;
	}

	/**
	 * starts a new simple transaction (only if currently no transaction is active)
	 */
	@Override
	public void startTransaction() throws SharedWorkspaceException {
		logger.info("Starting startTransaction()...");
		logger.info("CURRENT TRANSACTION=" + currentTransaction);
		if(currentTransaction == null) {
			try {
				currentTransaction = capi.createTransaction(RequestTimeout.INFINITE, spaceURI);
			} catch (MzsCoreException e) {
				throw new SharedWorkspaceException("Transaction could not be started: Error in MzsCore (" + e.getMessage() + ")");
			}
		}
		logger.info("Finished.");
	}

	/**
	 * commits the current simple transaction
	 */
	@Override
	public void commitTransaction() throws SharedWorkspaceException {
		logger.info("Starting commitTransaction()...");
		logger.info("CURRENT TRANSACTION=" + currentTransaction);
		if(currentTransaction != null) {
			try {
				capi.commitTransaction(currentTransaction);
			} catch (MzsCoreException e) {
				throw new SharedWorkspaceException("Transaction could not be commited: Error in MzsCore (" + e.getMessage() + ")");
			} finally {
				currentTransaction = null;
			}
		}
		logger.info("Finished.");
	}

	/**
	 * rollbacks the current simple transaction
	 */
	@Override
	public void rollbackTransaction() throws SharedWorkspaceException {
		logger.info("Starting rollbackTransaction()...");
		logger.info("CURRENT TRANSACTION=" + currentTransaction);
		if(currentTransaction != null) {
			try {
				capi.rollbackTransaction(currentTransaction);
			} catch (MzsCoreException e) {
				throw new SharedWorkspaceException("Transaction could not be royblacked: Error in MzsCore (" + e.getMessage() + ")");
			} finally {
				currentTransaction = null;
			}
		}
		logger.info("Finished.");
	}

}
