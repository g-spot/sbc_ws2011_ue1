package at.ac.tuwien.complang.sbc11.workers;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import at.ac.tuwien.complang.sbc11.benchmark.BenchmarkStopper;
import at.ac.tuwien.complang.sbc11.factory.SharedWorkspaceHelper;
import at.ac.tuwien.complang.sbc11.factory.exception.SharedWorkspaceException;
import at.ac.tuwien.complang.sbc11.parts.Computer;
import at.ac.tuwien.complang.sbc11.parts.Order;
import at.ac.tuwien.complang.sbc11.workers.shutdown.SecureShutdownApplication;
import at.ac.tuwien.complang.sbc11.workers.shutdown.ShutdownInterceptor;

public class Logistician extends Worker implements SecureShutdownApplication, Serializable {

	private static final long serialVersionUID = 9093226385712963149L;
	transient private static final long ORDER_NORMAL_PRODUCTION_ID = -1;
	transient private Logger logger;
	
	public Logistician(int serverPort) {
		logger = Logger.getLogger("at.ac.tuwien.complang.sbc11.workers.Tester");
		try {
			sharedWorkspace = SharedWorkspaceHelper.getWorkspaceImplementation(serverPort);
		} catch (SharedWorkspaceException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void distributeWithOrders() {
		logger.info("Starting distributing...");
		do {
			// 1. get all orders from the container
			List<Order> orderList = null;
			try {
				orderList = sharedWorkspace.getUnfinishedOrders();
				// add marked "order" to the end of the order list
				// if the for loop reaches this "order" a normal computer is produced
				Order orderNormalProduction = new Order(ORDER_NORMAL_PRODUCTION_ID, 0, null, 0, false);
				orderList.add(orderNormalProduction);
			} catch (SharedWorkspaceException e2) {
				logger.severe(e2.getMessage());
			}
			
			// 2. process orders in given (fifo) order
			for(Order order:orderList) {
				try {
					if(order.getId() == ORDER_NORMAL_PRODUCTION_ID) {
						distributeNormalComputer();
						break;
					}
					
					sharedWorkspace.startTransaction();
					// check if all computers for this order are ready
					if(sharedWorkspace.testOrderCountMet(order)) {
						List<Computer> computerList = sharedWorkspace.takeAllOrderedComputers(order);
						int i = 1;
						for(Computer computer:computerList) {
							computer.getWorkers().add(this);
							// if too much computers have been produced for this order
							// ship the rest as normal computers
							if(i > order.getComputerCount())
								computer.setOrder(null);
							sharedWorkspace.shipComputer(computer);
							i++;
						}
					}
					sharedWorkspace.finishOrder(order);
					sharedWorkspace.commitTransaction();
					
				} catch(SharedWorkspaceException e) {
					logger.severe(e.getMessage());
					try {
						sharedWorkspace.rollbackTransaction();
					} catch (SharedWorkspaceException e1) {
						logger.severe(e1.getMessage());
					}
				}
			}
		} while(true);
	}
	
	public void distributeNormalComputer() {
		try {
			// do all the following methods using the simple transaction support
			sharedWorkspace.startTransaction();
			
			logger.info("Trying to take completely tested computer from the space...");
			// first get a completely tested computer from the shared workspace
			Computer computer = sharedWorkspace.takeNormalCompletelyTestedComputer();
			if(computer == null)
				throw new SharedWorkspaceException("No computer found.");
			logger.info("Took computer[" + computer.getId() + "]");
			
			computer.getWorkers().add(this);
			
			// now check if the computer is defect or not and distribute it to the right place
			if(computer.isDefect())
			{
				logger.info("Computer is defect, try to move it to the trash...");
				sharedWorkspace.addComputerToTrash(computer);
			}
			else
			{
				logger.info("Computer is fine, try to ship it...");
				sharedWorkspace.shipComputer(computer);
			}
			
			logger.info("Finished.");
			
			sharedWorkspace.commitTransaction();
		} catch (SharedWorkspaceException e) {
			logger.severe(e.getMessage());
			try {
				sharedWorkspace.rollbackTransaction();
			} catch (SharedWorkspaceException e1) {
				logger.severe(e1.getMessage());
			}
		}
	}
	
	@Deprecated
	public void distribute() {
		logger.info("Starting distributing...");
		
		do {
			try {
				// do all the following methods using the simple transaction support
				sharedWorkspace.startTransaction();
				
				logger.info("Trying to take completely tested computer from the space...");
				// first get a completely tested computer from the shared workspace
				Computer computer = sharedWorkspace.takeNormalCompletelyTestedComputer();
				if(computer == null)
					throw new SharedWorkspaceException("No computer found.");
				logger.info("Took computer[" + computer.getId() + "]");
				
				computer.getWorkers().add(this);
				
				// now check if the computer is defect or not and distribute it to the right place
				if(computer.isDefect())
				{
					logger.info("Computer is defect, try to move it to the trash...");
					sharedWorkspace.addComputerToTrash(computer);
				}
				else
				{
					logger.info("Computer is fine, try to ship it...");
					sharedWorkspace.shipComputer(computer);
				}
				
				logger.info("Finished.");
				
				sharedWorkspace.commitTransaction();
			} catch (SharedWorkspaceException e) {
				logger.severe(e.getMessage());
				try {
					sharedWorkspace.rollbackTransaction();
				} catch (SharedWorkspaceException e1) {
					logger.severe(e1.getMessage());
				}
			}
		//} while(distributeAnotherComputer());
		} while(true);
	}
	
	@SuppressWarnings("unused")
	private boolean distributeAnotherComputer() {
		char command = ' ';
		System.out.println("Do you want to distribute another computer? (y/n)");
		do {
			try {
				command = (char)System.in.read();
			} catch (IOException e) { 
				System.out.println("Error reading from command line (" + e.getMessage() + ")");
			}
			if(command == 'y')
				return true;
			else if(command == 'n')
				return false;
		} while(true);
	}
	
	public static void main(String args[]) {
		// parse command line arguments (id and testType)
		long id = 0;
		int serverPort = 0;
		if(args.length == 2)
		{
			// parse first command line argument as long
			try {
				id = Long.parseLong(args[0]);
				serverPort = Integer.parseInt(args[1]);
			} catch(Exception e) {
				usage();
			}
		}
		else
			usage();
		
		Logistician logistician = new Logistician(serverPort);
		logistician.setId(id);
		
		// add shutdown interceptor and run tester
		ShutdownInterceptor interceptor = new ShutdownInterceptor(logistician);
		Runtime.getRuntime().addShutdownHook(interceptor);
		logistician.run();
	}
	
	public static void usage() {
		System.out.println("Argument 1: ID of Logistician (long)");
		System.out.println("Argument 2: Port of shared workspace server (int)");
		System.exit(-1);
	}

	@Override
	public void run() {
		logger.info("Waiting for start signal...");
		SharedWorkspaceHelper.waitForStartSignal();
		logger.info("Got start signal");
		if(SharedWorkspaceHelper.usesSignal()) {
			Executors.defaultThreadFactory().newThread(new BenchmarkStopper()).start();
		}
		distributeWithOrders();
	}

	@Override
	public void shutdown() {
		try {
			sharedWorkspace.secureShutdown();
		} catch (SharedWorkspaceException e) {
			logger.severe(e.getMessage());
		}
		logger.info("bye.");
	}

}
