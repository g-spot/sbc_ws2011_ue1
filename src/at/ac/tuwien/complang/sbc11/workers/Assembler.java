package at.ac.tuwien.complang.sbc11.workers;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

import at.ac.tuwien.complang.sbc11.factory.SharedWorkspaceHelper;
import at.ac.tuwien.complang.sbc11.factory.exception.SharedWorkspaceException;
import at.ac.tuwien.complang.sbc11.parts.CPU;
import at.ac.tuwien.complang.sbc11.parts.Computer;
import at.ac.tuwien.complang.sbc11.parts.GraphicBoard;
import at.ac.tuwien.complang.sbc11.parts.Mainboard;
import at.ac.tuwien.complang.sbc11.parts.Order;
import at.ac.tuwien.complang.sbc11.parts.Part;
import at.ac.tuwien.complang.sbc11.parts.RAM;
import at.ac.tuwien.complang.sbc11.workers.shutdown.SecureShutdownApplication;
import at.ac.tuwien.complang.sbc11.workers.shutdown.ShutdownInterceptor;

public class Assembler extends Worker implements SecureShutdownApplication, Serializable  {
	private static final long serialVersionUID = -4137829457317599010L;
	
	transient private Logger logger;

	public Assembler() {
		logger = Logger.getLogger("at.ac.tuwien.complang.sbc11.workers.Assembler");
		try {
			sharedWorkspace = SharedWorkspaceHelper.getWorkspaceImplementation();
		} catch (SharedWorkspaceException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public Computer assembleForOrder(Order order) {
		// TODO assemble a computer for the given order
		// non-blocking
		// return null (not all parts available) or the computer object
		// run in transaction
		return null;
	}
	
	public void assemble() {
		logger.info("Starting assembling...");
		long productionCount = 0;
		do {
			logger.info("Starting to assemble computer #" + (productionCount + 1));
			long duration = (long)((Math.random() * 10000)%2000 + 1000);
			try {
				duration = 0;
				Thread.sleep(duration);
			} catch (InterruptedException e) { }
			
			CPU cpu = null;
			Mainboard mainboard = null;
			GraphicBoard graphicBoard = null;
			List<Part> ramList = null;
			
			try {
				// do all the following methods using the simple transaction support
				sharedWorkspace.startTransaction();
				
				// get cpu from shared workspace
				logger.info("Trying to take CPU from shared workspace...");
				List<Part> cpuList = sharedWorkspace.takeParts(CPU.class, true, 1);
				if(cpuList != null && !cpuList.isEmpty()) {
					cpu = (CPU) cpuList.get(0);
					logger.info("Successfully took " + cpuList.size() + " CPU(s).");
				}
				
				// get mainboard from shared workspace
				logger.info("Trying to take mainboard from shared workspace...");
				List<Part> mainboardList = sharedWorkspace.takeParts(Mainboard.class, true, 1);
				if(mainboardList != null && !mainboardList.isEmpty()) {
					mainboard = (Mainboard) mainboardList.get(0);
					logger.info("Successfully took " + mainboardList.size() + " mainboard(s).");
				}
				
				// get graphic board from shared workspace
				logger.info("Trying to take graphic board from shared workspace...");
				List<Part> graphicBoardList = sharedWorkspace.takeParts(GraphicBoard.class, false, 1);
				if(graphicBoardList != null && !graphicBoardList.isEmpty()) {
					graphicBoard = (GraphicBoard) graphicBoardList.get(0);
					logger.info("Successfully took " + graphicBoardList.size() + " graphic board(s).");
				}
				
				// get ram from shared workspace
				logger.info("Trying to take ram from shared workspace...");
				// first try to get 4 ram modules, non-blocking
				ramList = sharedWorkspace.takeParts(RAM.class, false, 4);
				if(ramList == null) {
					// if no 4 ram modules are available, try 2 ram modules, non-blocking
					ramList = sharedWorkspace.takeParts(RAM.class, false, 2);
					if(ramList == null) {
						// if no 2 ram modules are available, wait until at least 1 module can be retrieved
						ramList = sharedWorkspace.takeParts(RAM.class, true, 1);
					}
				}
				if(ramList != null)
					logger.info("Successfully took " + ramList.size()+ " ram module(s).");
				
				// no we have all parts we need, let's construct a new computer
				Computer computer = new Computer();
				long nextID = sharedWorkspace.getNextComputerId();
				computer.setId(nextID);
				computer.setCpu(cpu);
				computer.setMainboard(mainboard);
				computer.setGraphicBoard(graphicBoard);
				
				for(Part p:ramList)
					computer.getRamModules().add((RAM)p);
				computer.getWorkers().add(this);
				
				// now write it to the shared workspace
				sharedWorkspace.addComputer(computer);
				
				sharedWorkspace.commitTransaction();
			} catch (SharedWorkspaceException e) {
				logger.severe(e.getMessage());
				try {
					sharedWorkspace.rollbackTransaction();
				} catch (SharedWorkspaceException e1) {
					logger.severe(e1.getMessage());
				}
			}
			
			logger.info("Finished assembling.");
		//} while(assembleAnotherComputer());
		} while(true);
	}
	
	@SuppressWarnings("unused")
	private boolean assembleAnotherComputer() {
		char command = ' ';
		System.out.println("Do you want to assemble another computer? (y/n)");
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
	
	public static void main(String args[]) throws IOException {
		Assembler assembler = new Assembler();
		
		long id = 0;
		if(args.length > 0)
		{
			// parse first command line argument as long
			try {
				id = Long.parseLong(args[0]);
			} catch(NumberFormatException e) {
				id = 0;
			}
		}
		assembler.setId(id);
		
		ShutdownInterceptor interceptor = new ShutdownInterceptor(assembler);
		Runtime.getRuntime().addShutdownHook(interceptor);
		assembler.run();
		//Executors.defaultThreadFactory().newThread(assembler).start();
	}

	@Override
	public void run() {
		assemble();
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