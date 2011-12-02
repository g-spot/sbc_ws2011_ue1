package at.ac.tuwien.complang.sbc11.workers;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import at.ac.tuwien.complang.sbc11.factory.SharedWorkspaceHelper;
import at.ac.tuwien.complang.sbc11.factory.exception.SharedWorkspaceException;
import at.ac.tuwien.complang.sbc11.parts.CPU;
import at.ac.tuwien.complang.sbc11.parts.Computer;
import at.ac.tuwien.complang.sbc11.parts.GraphicBoard;
import at.ac.tuwien.complang.sbc11.parts.Mainboard;
import at.ac.tuwien.complang.sbc11.parts.Part;
import at.ac.tuwien.complang.sbc11.parts.RAM;
import at.ac.tuwien.complang.sbc11.workers.shutdown.SecureShutdownApplication;
import at.ac.tuwien.complang.sbc11.workers.shutdown.ShutdownInterceptor;

public class AsyncAssembler extends Worker implements SecureShutdownApplication, Serializable  {
	private static final long serialVersionUID = -4137829457317599010L;
	
	transient private CPU cpu = null;
	transient private Mainboard mainboard = null;
	transient private GraphicBoard graphicBoard = null;
	transient private List<Part> ramList = null;
	
	transient private boolean responseReceivedForGraphicBoard;
	transient private Throwable asyncException;
	
	
	transient private Logger logger;

	public AsyncAssembler() {
		logger = Logger.getLogger("at.ac.tuwien.complang.sbc11.workers.Assembler");
		ramList = new ArrayList<Part>();
		try {
			sharedWorkspace = SharedWorkspaceHelper.getWorkspaceImplementation();
		} catch (SharedWorkspaceException e) {
			System.out.println(e.getMessage());
		}
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
			
			// reset values
			cpu = null;
			mainboard = null;
			graphicBoard = null;
			ramList.clear();
			responseReceivedForGraphicBoard = false;
			asyncException = null;
			
			try {
				// do all the following methods using the simple transaction support
				sharedWorkspace.startTransaction();
				
				// async: get cpu from shared workspace
				logger.info("Trying to take CPU from shared workspace...");
				sharedWorkspace.takePartsAsync(CPU.class, true, 1, this);

				
				// async: get mainboard from shared workspace
				logger.info("Trying to take mainboard from shared workspace...");
				sharedWorkspace.takePartsAsync(Mainboard.class, true, 1, this);
				
				// async: get graphic board from shared workspace
				logger.info("Trying to take graphic board from shared workspace...");
				sharedWorkspace.takePartsAsync(GraphicBoard.class, false, 1, this);
				
				// get ram from shared workspace
				// first try synchronously to fetch 4 or 2 ram modules
				// if nothing can be fetched, try asynchronously to fetch
				// 1 module (blocking)
				logger.info("Trying to take ram from shared workspace...");
				ramList = sharedWorkspace.takeParts(RAM.class, false, 4);
				if(ramList == null || ramList.isEmpty()) {
					// if no 4 ram modules are available, try 2 ram modules, non-blocking
					ramList = sharedWorkspace.takeParts(RAM.class, false, 2);
					if(ramList == null || ramList.isEmpty()) {
						// if no 2 ram modules are available, wait until at least 1 module can be retrieved
						sharedWorkspace.takePartsAsync(RAM.class, true, 1, this);
					}
				}
				
				logger.info("NOW WAITING UNTIL ALL PARTS ARE TAKEN");
				// wait until all parts are taken
				while(!allPartsTaken() && asyncException == null) {
					// wait 1/2 second between polling
					// TODO why do i have to wait???
					try {
						duration = 500;
						Thread.sleep(duration);
					} catch (InterruptedException e) { }
				}
				logger.info("FINISHED WAITING FOR PARTS");
				
				if(asyncException != null)
					throw new SharedWorkspaceException("Error in asynchronous call: " + asyncException.getMessage());
				
				// let's have it another synchronous try to retrieve a graphic board
				if(graphicBoard == null) {
					List<Part> graphicBoardList = sharedWorkspace.takeParts(GraphicBoard.class, false, 1);
					if(graphicBoardList != null && !graphicBoardList.isEmpty())
						graphicBoard = (GraphicBoard) graphicBoardList.get(0);
				}
				
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
	
	/**
	 * checks wether all needed parts are taken
	 * @return true if at least a cpu, a mainboard and one ram module are taken
	 * 			false otherwhise
	 */
	private boolean allPartsTaken() {
		boolean result = cpu != null && mainboard != null && responseReceivedForGraphicBoard && ramList != null && !ramList.isEmpty();
		//logger.info("cpu!=null (" + (cpu!=null) + ") mainboard!=null (" + (mainboard!=null) + ") responseGraphic (" + responseReceivedForGraphicBoard + ") ramlist!=null (" + (ramList!=null) + ")");
		return result;
	}
	
	public void setAsyncException(Throwable asyncException) {
		this.asyncException = asyncException;
	}

	public void setResponseReceivedForGraphicBoard(
			boolean responseReceivedForGraphicBoard) {
		this.responseReceivedForGraphicBoard = responseReceivedForGraphicBoard;
	}

	public void setParts(List<Part> parts) {
		for(Part part:parts) {
			if(part.getClass().equals(CPU.class)) {
				cpu = (CPU)part;
				break;
			}
			if(part.getClass().equals(Mainboard.class)) {
				mainboard = (Mainboard)part;
				break;
			}
			if(part.getClass().equals(GraphicBoard.class)) {
				graphicBoard = (GraphicBoard)part;
				responseReceivedForGraphicBoard = true;
				break;
			}
			if(part.getClass().equals(RAM.class)) {
				if(ramList == null)
					ramList = new ArrayList<Part>();
				ramList.add(part);
			}
		}
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
		AsyncAssembler asyncAssembler = new AsyncAssembler();
		
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
		asyncAssembler.setId(id);
		
		ShutdownInterceptor interceptor = new ShutdownInterceptor(asyncAssembler);
		Runtime.getRuntime().addShutdownHook(interceptor);
		asyncAssembler.run();
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
