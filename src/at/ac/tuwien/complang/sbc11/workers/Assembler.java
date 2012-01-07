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
	
	transient private static final long ORDER_NORMAL_PRODUCTION_ID = -1;
	
	transient private Logger logger;

	public Assembler() {
		logger = Logger.getLogger("at.ac.tuwien.complang.sbc11.workers.Assembler");
		try {
			sharedWorkspace = SharedWorkspaceHelper.getWorkspaceImplementation();
		} catch (SharedWorkspaceException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public Computer assembleWithOrders() {
		logger.info("Starting assembling...");
		long productionCount = 0;
		do {
			
			CPU cpu = null;
			Mainboard mainboard = null;
			GraphicBoard graphicBoard = null;
			List<Part> ramList = null;

			logger.info("Starting to assemble computer #" + (productionCount + 1));
			long duration = (long)((Math.random() * 10000)%2000 + 1000);
			try {
				duration = 0;
				Thread.sleep(duration);
			} catch (InterruptedException e) { }
			
			// 1. get all orders from the container
			List<Order> orderList = null;
			try {
				orderList = sharedWorkspace.getOrders();
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
						assembleNormalComputer();
						break;
					}
					
					sharedWorkspace.startTransaction();
					
					// get cpu from shared workspace
					logger.info("Trying to take CPU from shared workspace...");
					List<CPU> cpuList = sharedWorkspace.takeCPU(order.getCpuType(), false, 1);
					if(cpuList != null && !cpuList.isEmpty()) {
						cpu = (CPU) cpuList.get(0);
						logger.info("Successfully took " + cpuList.size() + " CPU(s).");
					}
					else
						throw new SharedWorkspaceException("CPU could not be taken");
					
					// get mainboard from shared workspace
					logger.info("Trying to take mainboard from shared workspace...");
					List<Part> mainboardList = sharedWorkspace.takeParts(Mainboard.class, false, 1);
					if(mainboardList != null && !mainboardList.isEmpty()) {
						mainboard = (Mainboard) mainboardList.get(0);
						logger.info("Successfully took " + mainboardList.size() + " mainboard(s).");
					}
					else
						throw new SharedWorkspaceException("Mainboard could not be taken");
					
					if(order.isUsingGraphicBoard()) {
						// get graphic board from shared workspace
						logger.info("Trying to take graphic board from shared workspace...");
						List<Part> graphicBoardList = sharedWorkspace.takeParts(GraphicBoard.class, false, 1);
						if(graphicBoardList != null && !graphicBoardList.isEmpty()) {
							graphicBoard = (GraphicBoard) graphicBoardList.get(0);
							logger.info("Successfully took " + graphicBoardList.size() + " graphic board(s).");
						}
						else
							throw new SharedWorkspaceException("Graphic board could not be taken");
					}
					
					// get ram modules from shared workspace
					logger.info("Trying to take ram modules from shared workspace...");
					ramList = sharedWorkspace.takeParts(RAM.class, false, order.getRamCount());
					if(ramList == null || ramList.size() != order.getRamCount())
						throw new SharedWorkspaceException("RAM modules could not be taken");
					
					// now we have all parts we need, let's construct a new computer
					Computer computer = new Computer();
					long nextID = sharedWorkspace.getNextComputerId();
					computer.setId(nextID);
					computer.setCpu(cpu);
					computer.setMainboard(mainboard);
					computer.setGraphicBoard(graphicBoard);
					computer.setOrder(order);
					
					for(Part p:ramList)
						computer.getRamModules().add((RAM)p);
					computer.getWorkers().add(this);
					
					// now write it to the shared workspace
					sharedWorkspace.addComputer(computer);
					
					sharedWorkspace.commitTransaction();
					
					// if the computer was successfully produced, exit the loop
					// and start from the beginning (with the first order)
					break;
				} catch(SharedWorkspaceException e) {
					logger.severe(e.getMessage());
					try {
						sharedWorkspace.rollbackTransaction();
					} catch (SharedWorkspaceException e1) {
						logger.severe(e1.getMessage());
					}
				}
				// if one part can not be fetched from the workspace, roll the transaction back
				// and start with the next order
			}
		} while(true);
	}
	
	public void assembleNormalComputer() {
		logger.info("Starting to assemble normal computer");
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
			
			// now we have all parts we need, let's construct a new computer
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
	}
	
	@Deprecated
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
				
				// now we have all parts we need, let's construct a new computer
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
		//assemble();
		assembleWithOrders();
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