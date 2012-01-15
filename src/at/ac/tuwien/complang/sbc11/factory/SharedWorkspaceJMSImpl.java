package at.ac.tuwien.complang.sbc11.factory;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import at.ac.tuwien.complang.sbc11.factory.exception.SharedWorkspaceException;
import at.ac.tuwien.complang.sbc11.parts.CPU;
import at.ac.tuwien.complang.sbc11.parts.CPU.CPUType;
import at.ac.tuwien.complang.sbc11.parts.Computer;
import at.ac.tuwien.complang.sbc11.parts.Order;
import at.ac.tuwien.complang.sbc11.parts.Part;
import at.ac.tuwien.complang.sbc11.ui.Factory;
import at.ac.tuwien.complang.sbc11.workers.AsyncAssembler;
import at.ac.tuwien.complang.sbc11.workers.Tester.TestType;

/* Implements the shared workspace with an alternative technology.
 * The technology used is JMS. OpenJMS is the implementation that will be used here.
 * There are other implementations that are not completely conform to a standard.
 * OpenJMS provides a clean implementation of handling Objects.
 */


public class SharedWorkspaceJMSImpl extends SharedWorkspace 
{
	// General
	private Logger logger;
	
	private Factory factory;
	
	public SharedWorkspaceJMSImpl() throws SharedWorkspaceException 
	{
		super(null);
		
		this.factory = null;
		logger = Logger.getLogger("at.ac.tuwien.complang.sbc11.factory.SharedWorkspaceJMSImpl");

		
		logger.info("Initialization done.");
	}

	public SharedWorkspaceJMSImpl(Factory factory) throws SharedWorkspaceException 
	{
		super(factory);
		
		logger = Logger.getLogger("at.ac.tuwien.complang.sbc11.factory.SharedWorkspaceJMSImpl");
		this.factory = factory;		
		logger.info("Initialization done.");
	}

	@Override
	public long getNextComputerId() 
	{
		Date theDate = new Date();
		return theDate.getTime();
	}

	@Override
	public void takePartsAsync(Class<?> partType, boolean blocking, int partCount, AsyncAssembler callback) throws SharedWorkspaceException 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Computer> getDeconstructedComputers()
			throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Order> getUnfinishedOrders() throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addOrder(Order order) throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void finishOrder(Order order) throws SharedWorkspaceException {
		// TODO Auto-generated method stub
	}

	@Override
	public List<CPU> takeCPU(CPUType cpuType, boolean blocking, int partCount)
			throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean testOrderCountMet(Order order)
			throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Computer> takeAllOrderedComputers(Order order)
			throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Order> getFinishedOrders() throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getWorkspaceID() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addParts(List<Part> parts) throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startBalancing() throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopBalancing() throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void waitForBalancing() throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isCurrentlyBalancing() throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		return false;
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
	public Computer takeNormalCompletelyTestedComputer()
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
}
