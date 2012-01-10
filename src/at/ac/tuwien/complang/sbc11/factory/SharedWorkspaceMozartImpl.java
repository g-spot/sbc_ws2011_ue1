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
import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.capi3.KeyCoordinator.KeySelector;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.core.AsyncCapi;
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
import at.ac.tuwien.complang.sbc11.mozart.TakePartsRequestCallbackHandler;
import at.ac.tuwien.complang.sbc11.mozart.listeners.IncompleteComputerNotificationListener;
import at.ac.tuwien.complang.sbc11.mozart.listeners.OrderNotificationListener;
import at.ac.tuwien.complang.sbc11.mozart.listeners.PartNotificationListener;
import at.ac.tuwien.complang.sbc11.mozart.listeners.ShippedComputerNotificationListener;
import at.ac.tuwien.complang.sbc11.mozart.listeners.TrashedComputerNotificationListener;
import at.ac.tuwien.complang.sbc11.parts.CPU;
import at.ac.tuwien.complang.sbc11.parts.CPU.CPUType;
import at.ac.tuwien.complang.sbc11.parts.Computer;
import at.ac.tuwien.complang.sbc11.parts.GraphicBoard;
import at.ac.tuwien.complang.sbc11.parts.Mainboard;
import at.ac.tuwien.complang.sbc11.parts.Order;
import at.ac.tuwien.complang.sbc11.parts.Part;
import at.ac.tuwien.complang.sbc11.parts.RAM;
import at.ac.tuwien.complang.sbc11.ui.Factory;
import at.ac.tuwien.complang.sbc11.workers.AsyncAssembler;
import at.ac.tuwien.complang.sbc11.workers.Tester.TestState;
import at.ac.tuwien.complang.sbc11.workers.Tester.TestType;

public class SharedWorkspaceMozartImpl extends SharedWorkspace {
	
	private URI spaceURI;
	private MzsCore core;
	private Capi capi;
	private AsyncCapi asyncCapi;
	private NotificationManager notificationManager;
	
	// containers
	private ContainerReference idContainer;
	private ContainerReference partContainer;
	private ContainerReference mainboardContainer;
	private ContainerReference incompleteContainer;
	private ContainerReference trashedContainer;
	private ContainerReference shippedContainer;
	private ContainerReference orderContainer;
	private Logger logger;
	
	// for transaction purposes
	private TransactionReference currentTransaction = null;

	// global constants
	private final String LABEL_COMPLETELY_TESTED = "label_completely_tested";
	private final String LABEL_NOT_COMPLETELY_TESTED = "label_not_completely_tested";
	private final String LABEL_ORDER_COMPLETELY_TESTED = "label_order_completely_tested";
	private final String KEY_ID_PART = "key_id_part";
	private final String KEY_ID_COMPUTER = "key_id_computer";
	private final String LABEL_ORDER_FINISHED = "label_order_finished";
	private final String LABEL_ORDER_NOT_FINISHED = "label_order_not_finished";
	
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
			asyncCapi = new AsyncCapi(core);
			
			// get container
			logger.info("Retrieving containers...");
			initContainers();
			
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
			
			if(SharedWorkspaceHelper.useStandaloneMozartServer())
				core = DefaultMzsCore.newInstance(0);
			else
				core = DefaultMzsCore.newInstance(StandaloneServer.SERVER_PORT); // port 0 = choose a free port
			
			// uses external standalone server:
			//core = DefaultMzsCore.newInstance(0);
			
			capi = new Capi(core);
			asyncCapi = new AsyncCapi(core);
			
			// create container
			logger.info("Retrieving containers...");
			initContainers();
			
			// store all containers in a hashmap
			HashMap<String, String> containerMap = new HashMap<String, String>();
			containerMap.put(idContainer.getStringRepresentation(), SpaceUtils.CONTAINER_ID);
			containerMap.put(partContainer.getStringRepresentation(), SpaceUtils.CONTAINER_PARTS);
			containerMap.put(mainboardContainer.getStringRepresentation(), SpaceUtils.CONTAINER_MAINBOARDS);
			containerMap.put(incompleteContainer.getStringRepresentation(), SpaceUtils.CONTAINER_INCOMPLETE);
			containerMap.put(trashedContainer.getStringRepresentation(), SpaceUtils.CONTAINER_TRASHED);
			containerMap.put(shippedContainer.getStringRepresentation(), SpaceUtils.CONTAINER_SHIPPED);
			containerMap.put(orderContainer.getStringRepresentation(), SpaceUtils.CONTAINER_ORDERS);
			
			// init notifications
			logger.info("Registering notifications...");
			notificationManager = new NotificationManager(core);
			HashSet<Operation> operations = new HashSet<Operation>();
			operations.add(Operation.WRITE);
			operations.add(Operation.TAKE);
			
			notificationManager.createNotification(partContainer, new PartNotificationListener(factory, containerMap), operations, null, null);
			notificationManager.createNotification(mainboardContainer, new PartNotificationListener(factory, containerMap), operations, null, null);
			notificationManager.createNotification(incompleteContainer, new IncompleteComputerNotificationListener(factory, containerMap), operations, null, null);
			notificationManager.createNotification(trashedContainer, new TrashedComputerNotificationListener(factory, containerMap), operations, null, null);
			notificationManager.createNotification(shippedContainer, new ShippedComputerNotificationListener(factory, containerMap), operations, null, null);
			notificationManager.createNotification(orderContainer, new OrderNotificationListener(factory, containerMap), operations, null, null);
			
		} catch (MzsCoreException e) {
			throw new SharedWorkspaceException("Shared workspace could not be initialized: Error in MzsCore (" + e.getMessage() + ")");
		} catch (URISyntaxException e) {
			throw new SharedWorkspaceException("Shared workspace could not be initialized: Error with URI (" + e.getMessage() + ")");
		} catch (InterruptedException e) {
			throw new SharedWorkspaceException("Shared workspace could not be initialized: Error with UI notification (" + e.getMessage() + ")");
		}
	}
	
	/**
	 * includes the configuration file for the MzsCore logging mechanism
	 */
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
	 * creates or lookups all used containers
	 * @throws MzsCoreException
	 */
	private void initContainers() throws MzsCoreException {
		idContainer = SpaceUtils.getOrCreateIDContainer(spaceURI, capi);
		partContainer = SpaceUtils.getOrCreateLindaContainer(SpaceUtils.CONTAINER_PARTS, spaceURI, capi);
		mainboardContainer = SpaceUtils.getOrCreateFIFOContainer(SpaceUtils.CONTAINER_MAINBOARDS, spaceURI, capi);
		incompleteContainer = SpaceUtils.getOrCreateIncompleteContainer(spaceURI, capi);
		trashedContainer = SpaceUtils.getOrCreateAnyContainer(SpaceUtils.CONTAINER_TRASHED, spaceURI, capi);
		shippedContainer = SpaceUtils.getOrCreateAnyContainer(SpaceUtils.CONTAINER_SHIPPED, spaceURI, capi);
		orderContainer = SpaceUtils.getOrCreateOrderContainer(spaceURI, capi);
	}
	
	/**
	 * does a secure shutdown of the MzsCore
	 */
	@Override
	public void secureShutdown() throws SharedWorkspaceException {
		logger.info("Shutting down application...");
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
		
		long nextID = getNextId(KEY_ID_PART);
		
		logger.info("Finished.");
		return nextID;
	}
	
	/**
	 * gets the next system wide identifier for computers
	 * uses an autonomous transaction (i.e. this method is not affected by
	 * the built-in simple transaction control of the class)
	 * @return the next computer id
	 */
	@Override
	public long getNextComputerId() throws SharedWorkspaceException {
		logger.info("Starting getNextComputerId()...");
		logger.info("CURRENT TRANSACTION=" + currentTransaction);
		
		long nextID = getNextId(KEY_ID_COMPUTER);
		
		logger.info("Finished.");
		return nextID;
	}
	
	private long getNextId(String keyType) throws SharedWorkspaceException {
		long nextID = 0;
		TransactionReference transaction = null;
		try {
			transaction = capi.createTransaction(MzsConstants.TransactionTimeout.INFINITE, spaceURI);
			Selector idSelector = KeyCoordinator.newSelector(keyType, 1);
			
			// let's see if there is already an id in the container:
			try {
				capi.test(idContainer, idSelector, RequestTimeout.TRY_ONCE, transaction);
			} catch(MzsCoreException e) {
				// no --> then insert the first one
				nextID = 1;
				capi.write(new Entry(new Long(1), KeyCoordinator.newCoordinationData(keyType)), idContainer, RequestTimeout.TRY_ONCE, transaction);
				capi.commitTransaction(transaction);
				logger.info("Finished.");
				return nextID;
			}
			
			// if the container contains an id: take the id, increment it and insert it again
			if(nextID == 0) {
				//List<Selector> selectors = Collections.singletonList(idSelector);
				ArrayList<Long> result = capi.take(idContainer, idSelector, RequestTimeout.TRY_ONCE, transaction);
				nextID = result.get(0);
				nextID++;

				capi.write(new Entry(nextID, KeyCoordinator.newCoordinationData(keyType)), idContainer, RequestTimeout.TRY_ONCE, transaction);
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
	 * returns all incomplete computers in the shared workspace, except deconstructed computers
	 * uses the current simple transaction, if one exists
	 * @return list of computers
	 */
	@Override
	public List<Computer> getIncompleteComputers()
			throws SharedWorkspaceException {
		logger.info("Starting getIncompleteComputers()...");
		logger.info("CURRENT TRANSACTION=" + currentTransaction);
		Computer pattern = new Computer();
		pattern.setCompletenessTested(null); // take any computers that are not deconstructed
		pattern.setCorrectnessTested(null);
		pattern.setDeconstructed(false);
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
	 * returns all deconstructed computers
	 * @return list of computers
	 */
	@Override
	public List<Computer> getDeconstructedComputers() throws SharedWorkspaceException {
		logger.info("Starting getDeconstructedComputers()...");
		logger.info("CURRENT TRANSACTION=" + currentTransaction);
		Computer pattern = new Computer();
		pattern.setCompletenessTested(null);
		pattern.setCorrectnessTested(null);
		pattern.setDeconstructed(true);
		Selector computerSelector = LindaCoordinator.newSelector(pattern, Selecting.COUNT_MAX);
		try {
			logger.info("Finished.");
			return capi.read(incompleteContainer, computerSelector, RequestTimeout.TRY_ONCE, currentTransaction);
		} catch (MzsCoreException e) {
			throw new SharedWorkspaceException("Parts could not be read: Error in MzsCore (" + e.getMessage() + ")");
		}
	}
	
	/**
	 * returns unfinished all orders
	 * @return list of orders
	 */
	@Override
	public List<Order> getUnfinishedOrders() throws SharedWorkspaceException {
		logger.info("Starting getUnfinishedOrders()...");
		logger.info("CURRENT TRANSACTION=" + currentTransaction);
		List<Selector> selectors = new ArrayList<Selector>();
		selectors.add(FifoCoordinator.newSelector(Selecting.COUNT_MAX));
		selectors.add(LabelCoordinator.newSelector(LABEL_ORDER_NOT_FINISHED, Selecting.COUNT_MAX));
		try {
			logger.info("Finished.");
			return capi.read(orderContainer, selectors, RequestTimeout.TRY_ONCE, currentTransaction);
		} catch (MzsCoreException e) {
			throw new SharedWorkspaceException("Orders could not be read: Error in MzsCore (" + e.getMessage() + ")");
		}
	}
	
	/**
	 * returns finished orders
	 * @return list of orders
	 */
	@Override
	public List<Order> getFinishedOrders() throws SharedWorkspaceException {
		logger.info("Starting getFinishedOrders()...");
		logger.info("CURRENT TRANSACTION=" + currentTransaction);
		List<Selector> selectors = new ArrayList<Selector>();
		selectors.add(FifoCoordinator.newSelector(Selecting.COUNT_MAX));
		selectors.add(LabelCoordinator.newSelector(LABEL_ORDER_FINISHED, Selecting.COUNT_MAX));
		try {
			logger.info("Finished.");
			return capi.read(orderContainer, selectors, RequestTimeout.TRY_ONCE, currentTransaction);
		} catch (MzsCoreException e) {
			throw new SharedWorkspaceException("Orders could not be read: Error in MzsCore (" + e.getMessage() + ")");
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
			if(blocking)
				result = capi.take(container, selectorList, RequestTimeout.INFINITE, currentTransaction, IsolationLevel.READ_COMMITTED, null);
			else
			{
				try {
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
		{
			if(computer.getOrder() == null)
				label = LABEL_COMPLETELY_TESTED;
			else
				label = LABEL_ORDER_COMPLETELY_TESTED;
		}
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
			pattern.setDeconstructed(null);
		} else if(untestedFor.equals(TestType.CORRECTNESS)) {
			pattern.setCompletenessTested(null);
			pattern.setCorrectnessTested(TestState.NOT_TESTED);
			pattern.setDeconstructed(null);
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
	public Computer takeNormalCompletelyTestedComputer()
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
			throw new SharedWorkspaceException("Computers could not be read: Error in MzsCore (" + e.getMessage() + ")");
		}
		return null;
	}
	
	/**
	 * tests whether the required amount of computers for the given order has been produced
	 * @param order
	 * 			the order
	 * @return false if there are still computers missing, true otherwise
	 */
	@Override
	public boolean testOrderCountMet(Order order) throws SharedWorkspaceException {
		logger.info("Starting testOrderCountMet()...");
		logger.info("CURRENT TRANSACTION=" + currentTransaction);
		
		Selector labelSelector = LabelCoordinator.newSelector(LABEL_ORDER_COMPLETELY_TESTED, Selecting.COUNT_MAX);
		Computer template = new Computer();
		template.setCompletenessTested(null);
		template.setCorrectnessTested(null);
		template.setDeconstructed(false);
		template.setOrder(order);
		Selector orderSelector = LindaCoordinator.newSelector(template, Selecting.COUNT_MAX);
		List<Selector> selectors = new ArrayList<Selector>();
		selectors.add(orderSelector);
		selectors.add(labelSelector);
		
		
		try {
			int computerCount = capi.test(incompleteContainer, selectors, RequestTimeout.TRY_ONCE, currentTransaction);
			logger.info("Finished computerCount=" + computerCount);
			if(computerCount < order.getComputerCount())
				return false;
			return true;
		} catch (MzsCoreException e) {
			throw new SharedWorkspaceException("Computers could not be read: Error in MzsCore (" + e.getMessage() + ")");
		}
	}
	
	/**
	 * takes all completely tested computers that are part of the given order
	 * @param order
	 * 			the order
	 * @return all completely tested computers that are part of the given order
	 */
	@Override
	public List<Computer> takeAllOrderedComputers(Order order)
			throws SharedWorkspaceException {
		logger.info("Starting takeAllOrderedComputers()...");
		logger.info("CURRENT TRANSACTION=" + currentTransaction);
		
		Selector labelSelector = LabelCoordinator.newSelector(LABEL_ORDER_COMPLETELY_TESTED, Selecting.COUNT_MAX);
		Computer template = new Computer();
		template.setCompletenessTested(null);
		template.setCorrectnessTested(null);
		template.setDeconstructed(false);
		template.setOrder(order);
		Selector orderSelector = LindaCoordinator.newSelector(template, Selecting.COUNT_MAX);
		List<Selector> selectors = new ArrayList<Selector>();
		selectors.add(orderSelector);
		selectors.add(labelSelector);
		
		try {
			List<Computer> computerList = capi.take(incompleteContainer, selectors, RequestTimeout.TRY_ONCE, currentTransaction);
			logger.info("Finished.");
			return computerList;
		} catch (MzsCoreException e) {
			throw new SharedWorkspaceException("Computers could not be read: Error in MzsCore (" + e.getMessage() + ")");
		}
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

	/**
	 * asynchronously takes a part from the shared workspace, i.e. returns the object and removes it
	 * uses the current simple transaction, if one exists
	 * the @callback will be handed over to a new TakePartsRequestCallbackHandler
	 * 
	 * @param partType
	 * 				class type of the part
	 * @param blocking
	 * 				if true, takePart() waits until a part can be found and returned
	 * 				if false, takePart() tries only once to get the part
	 * @param partCount
	 * 				specifies the number of parts to be taken
	 * @param callback
	 * 				specifies the assembler to notify when the result of the
	 *              request arrives 
	 */
	@Override
	public void takePartsAsync(Class<?> partType, boolean blocking,
			int partCount, AsyncAssembler callback) throws SharedWorkspaceException {
		logger.info("Starting takePartsAsync()...");
		logger.info("CURRENT TRANSACTION=" + currentTransaction);
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

		// call asynchronous take
		if(blocking)
			asyncCapi.take(container,
							selectorList,
							RequestTimeout.INFINITE, // wait for the specified number of parts
							currentTransaction,
							IsolationLevel.READ_COMMITTED,
							null,
							new TakePartsRequestCallbackHandler(callback));
		else
			asyncCapi.take(container,
					selectorList,
					RequestTimeout.TRY_ONCE, // try once, do not wait
					currentTransaction,
					IsolationLevel.READ_COMMITTED,
					null,
					new TakePartsRequestCallbackHandler(callback));
		logger.info("Finished.");
	}
	
	/**
	 * adds a new order to the orderContainer
	 * uses the current simple transaction, if one exists
	 * @param order
	 * 				the order to insert
	 */
	@Override
	public void addOrder(Order order) throws SharedWorkspaceException {
		logger.info("Starting addOrder()...");
		logger.info("CURRENT TRANSACTION=" + currentTransaction);
		try {
			List<CoordinationData> coords = new ArrayList<CoordinationData>();
			coords.add(FifoCoordinator.newCoordinationData());
			coords.add(LabelCoordinator.newCoordinationData(LABEL_ORDER_NOT_FINISHED));
			coords.add(KeyCoordinator.newCoordinationData(String.valueOf(order.getId())));
			capi.write(new Entry(order, coords), orderContainer, RequestTimeout.DEFAULT, currentTransaction);
		} catch (MzsCoreException e) {
			throw new SharedWorkspaceException("Order could not be written: Error in MzsCore (" + e.getMessage() + ")");
		}
		logger.info("Finished.");
	}
	
	/**
	 * deletes the given order from the order container
	 * @param order
	 * 			the order to delete
	 */
	@Override
	public void finishOrder(Order order) throws SharedWorkspaceException {
		logger.info("Starting finishOrder()...");
		logger.info("CURRENT TRANSACTION=" + currentTransaction);

		List<KeySelector> selectors = Arrays.asList(KeyCoordinator.newSelector(String.valueOf(order.getId())));
		List<CoordinationData> coords = new ArrayList<CoordinationData>();
		coords.add(FifoCoordinator.newCoordinationData());
		coords.add(LabelCoordinator.newCoordinationData(LABEL_ORDER_FINISHED));
		coords.add(KeyCoordinator.newCoordinationData(String.valueOf(order.getId())));
		
		try {
			//if(currentTransaction == null) {
			//	this.startTransaction();
			//TODO add transaction control (nested transaction possible?)
				capi.delete(orderContainer, selectors, RequestTimeout.TRY_ONCE, currentTransaction, IsolationLevel.READ_COMMITTED, null);
				capi.write(new Entry(order, coords), orderContainer, RequestTimeout.DEFAULT, currentTransaction);
			//	this.commitTransaction();
			//}
		} catch (MzsCoreException e) {
			e.printStackTrace();
			throw new SharedWorkspaceException("Order could not be finished: Error in MzsCore (" + e.getMessage() + ")");
		}
		logger.info("Finished.");
	}
	
	/**
	 * takes the next order from the shared workspace, i.e. returns the object and removes it
	 * uses the current simple transaction, if one exists
	 * @param order
	 * 			if set to true, the method waits until an order arrives
	 */
	/*@Override
	public Order takeOrder(boolean blocking) throws SharedWorkspaceException {
		logger.info("Starting takeOrder()...");
		logger.info("CURRENT TRANSACTION=" + currentTransaction);
		List<Order> result = null;
		Order order = null;

		List<FifoSelector> selectors = Arrays.asList(FifoCoordinator.newSelector(1));
		
		try {
			if(order)
				result = capi.take(orderContainer, selectors, RequestTimeout.INFINITE, currentTransaction, IsolationLevel.READ_COMMITTED, null);
			else
			{
				try {
					result = capi.take(orderContainer, selectors, RequestTimeout.TRY_ONCE, currentTransaction, IsolationLevel.READ_COMMITTED, null);
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
			
			order = result.get(0);
			
		} catch (MzsCoreException e) {
			e.printStackTrace();
			throw new SharedWorkspaceException("Order could not be taken: Error in MzsCore (" + e.getMessage() + ")");
		} catch (IndexOutOfBoundsException e) {
			throw new SharedWorkspaceException("Order could not be taken: Index out of bounds (" + e.getMessage() + ")");
		}
		logger.info("Finished.");
		return order;
	}*/

	/**
	 * takes a cpu from the shared workspace, i.e. returns the object and removes it
	 * uses the current simple transaction, if one exists
	 * @param cpuType
	 * 				type of the cpu
	 * @param blocking
	 * 				if true, takeCPU() waits until a cpu can be found and returned
	 * 				if false, takeCPU() tries only once to get the part
	 * @param partCount
	 * 				specifies the number of parts to be taken
	 * @return a cpu with @cpuType, null if @blocking = false and no @partCount parts can be found
	 */
	@Override
	public List<CPU> takeCPU(CPUType cpuType, boolean blocking, int partCount)
			throws SharedWorkspaceException {
		logger.info("Starting takeCPU()...");
		logger.info("CURRENT TRANSACTION=" + currentTransaction);
		List<CPU> result = null;
		Selector partSelector = null;
		List<Selector> selectorList = new ArrayList<Selector>();

		CPU pattern = new CPU();
		pattern.setCpuType(cpuType);
		partSelector = LindaCoordinator.newSelector(pattern, partCount);
		selectorList.add(partSelector);
		
		try {
			if(blocking)
				result = capi.take(partContainer, selectorList, RequestTimeout.INFINITE, currentTransaction, IsolationLevel.READ_COMMITTED, null);
			else
			{
				try {
					result = capi.take(partContainer, selectorList, RequestTimeout.TRY_ONCE, currentTransaction, IsolationLevel.READ_COMMITTED, null);
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
			throw new SharedWorkspaceException("CPU could not be taken: Error in MzsCore (" + e.getMessage() + ")");
		} catch (IndexOutOfBoundsException e) {
			throw new SharedWorkspaceException("CPU could not be taken: Index out of bounds (" + e.getMessage() + ")");
		}
		logger.info("Finished.");
		return result;
	}

}
