package at.ac.tuwien.complang.sbc11.workers;

import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Logger;

import at.ac.tuwien.complang.sbc11.factory.SharedWorkspaceHelper;
import at.ac.tuwien.complang.sbc11.factory.exception.SharedWorkspaceException;
import at.ac.tuwien.complang.sbc11.parts.Computer;
import at.ac.tuwien.complang.sbc11.workers.shutdown.SecureShutdownApplication;
import at.ac.tuwien.complang.sbc11.workers.shutdown.ShutdownInterceptor;

public class Logistician extends Worker implements SecureShutdownApplication, Serializable {

	private static final long serialVersionUID = 9093226385712963149L;
	transient private Logger logger;
	
	public Logistician() {
		logger = Logger.getLogger("at.ac.tuwien.complang.sbc11.workers.Tester");
		try {
			sharedWorkspace = SharedWorkspaceHelper.getWorkspaceImplementation();
		} catch (SharedWorkspaceException e) {
			System.out.println(e.getMessage());
		}
	}
	
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
		if(args.length == 1)
		{
			// parse first command line argument as long
			try {
				id = Long.parseLong(args[0]);
			} catch(Exception e) {
				usage();
			}
		}
		else
			usage();
		
		Logistician logistician = new Logistician();
		logistician.setId(id);
		
		// add shutdown interceptor and run tester
		ShutdownInterceptor interceptor = new ShutdownInterceptor(logistician);
		Runtime.getRuntime().addShutdownHook(interceptor);
		logistician.run();
	}
	
	public static void usage() {
		System.out.println("Argument 1: ID of Logistician (long)");
		System.exit(-1);
	}

	@Override
	public void run() {
		distribute();
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
