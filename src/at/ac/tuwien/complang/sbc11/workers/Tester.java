package at.ac.tuwien.complang.sbc11.workers;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import at.ac.tuwien.complang.sbc11.factory.SharedWorkspaceHelper;
import at.ac.tuwien.complang.sbc11.factory.exception.SharedWorkspaceException;
import at.ac.tuwien.complang.sbc11.parts.Computer;
import at.ac.tuwien.complang.sbc11.parts.RAM;
import at.ac.tuwien.complang.sbc11.workers.shutdown.SecureShutdownApplication;
import at.ac.tuwien.complang.sbc11.workers.shutdown.ShutdownInterceptor;

public class Tester extends Worker implements SecureShutdownApplication, Serializable {

	private static final long serialVersionUID = -89701659089204368L;

	public enum TestType { COMPLETENESS, CORRECTNESS };
	public enum TestState { NOT_TESTED, FAILED, PASSED };
	
	transient private Logger logger;
	
	public Tester(int serverPort) {
		logger = Logger.getLogger("at.ac.tuwien.complang.sbc11.workers.Tester");
		try {
			sharedWorkspace = SharedWorkspaceHelper.getWorkspaceImplementation(serverPort);
		} catch (SharedWorkspaceException e) {
			System.out.println(e.getMessage());
		}
	}
	
	public void test() {
		logger.info("Starting testing...");
		
		do {
			try {
				// do all the following methods using the simple transaction support
				sharedWorkspace.startTransaction();
				
				logger.info("Trying to take untested computer from space...");
				// first of all take an untested computer from the space
				Computer computer = sharedWorkspace.takeUntestedComputer(this.testType);
				if(computer == null)
					throw new SharedWorkspaceException("No computer found");
				logger.info("Took computer[" + computer.getId() + "] from space.");
				
				// now do test
				if(this.testType == TestType.COMPLETENESS) {
					logger.info("Testing computer for completeness...");
					// if the computer has a cpu, a mainboard and at least one ram module, the test is successful
					if(computer.getCpu() != null && computer.getMainboard() != null && computer.getRamModules() != null && computer.getRamModules().size() > 0)
						computer.setCompletenessTested(TestState.PASSED);
					else
						computer.setCompletenessTested(TestState.FAILED);
					logger.info("Test result: " + computer.getCompletenessTested().toString());
				} else if (this.testType == TestType.CORRECTNESS) {
					logger.info("Testing computer for correctness...");
					// incomplete computers shouldnt be tested for correctness, as they could possibly still get defect components
					if(computer.getCpu() == null || computer.getMainboard() == null || computer.getRamModules() == null || computer.getRamModules().size() == 0)
						computer.setCorrectnessTested(TestState.NOT_TESTED);
					else
					{
						// the computer passes the test, if none of his components is defect
						computer.setCorrectnessTested(TestState.PASSED);
						if(computer.getCpu().isDefect())
							computer.setCorrectnessTested(TestState.FAILED);
						if(computer.getMainboard().isDefect())
							computer.setCorrectnessTested(TestState.FAILED);
						if(computer.getGraphicBoard() != null && computer.getGraphicBoard().isDefect())
							computer.setCorrectnessTested(TestState.FAILED);
						for(RAM ram:computer.getRamModules()) {
							if(ram.isDefect())
							{
								computer.setCorrectnessTested(TestState.FAILED);
								break;
							}
						}
					}
					logger.info("Test result: " + computer.getCompletenessTested().toString());
				}
				
				if(computer.isDefect()) {
					// deconstruct the computer
					// = write all good parts back to the shared workspace
					if(computer.getCpu() != null && !computer.getCpu().isDefect()) {
						sharedWorkspace.addPart(computer.getCpu());
						computer.setCpu(null);
					}
					if(computer.getMainboard() != null && !computer.getMainboard().isDefect()) {
						sharedWorkspace.addPart(computer.getMainboard());
						computer.setMainboard(null);
					}
					if(computer.getGraphicBoard() != null && !computer.getGraphicBoard().isDefect()) {
						sharedWorkspace.addPart(computer.getGraphicBoard());
						computer.setGraphicBoard(null);
					}
					Collection<RAM> partsToRemove = new ArrayList<RAM>();
					for(RAM ram:computer.getRamModules()) {
						if(!ram.isDefect()) {
							partsToRemove.add(ram);
							sharedWorkspace.addPart(ram);
						}
					}
					computer.getRamModules().removeAll(partsToRemove);
					// finally mark the computer as deconstructed
					computer.setDeconstructed(true);
				}
				
				// finally write tested computer back to the space
				logger.info("Trying to write tested computer back to space...");
				computer.getWorkers().add(this);
				sharedWorkspace.addComputer(computer);
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
		//} while(testAnotherComputer());
		} while(true);
	}

	@SuppressWarnings("unused")
	private boolean testAnotherComputer() {
		char command = ' ';
		System.out.println("Do you want to test another computer? (y/n)");
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
	
	private TestType testType;

	public TestType getTestType() {
		return testType;
	}

	public void setTestType(TestType testType) {
		this.testType = testType;
	}
	
	public static void main(String args[]) {
		// parse command line arguments (id and testType)
		long id = 0;
		int testTypeOrdinal = 0;
		int serverPort = 0;
		TestType testType = null;
		if(args.length == 3)
		{
			// parse first command line argument as long
			try {
				id = Long.parseLong(args[0]);
				testTypeOrdinal = Integer.parseInt(args[1]);
				if(testTypeOrdinal == TestType.COMPLETENESS.ordinal())
					testType = TestType.COMPLETENESS;
				else if(testTypeOrdinal == TestType.CORRECTNESS.ordinal())
					testType = TestType.CORRECTNESS;
				else
					throw new Exception();
				serverPort = Integer.parseInt(args[2]);
			} catch(Exception e) {
				usage();
			}
		}
		else
			usage();
		
		// create new tester
		Tester tester = new Tester(serverPort);
		tester.setId(id);
		tester.setTestType(testType);
		
		// add shutdown interceptor and run tester
		ShutdownInterceptor interceptor = new ShutdownInterceptor(tester);
		Runtime.getRuntime().addShutdownHook(interceptor);
		tester.run();
	}
	
	public static void usage() {
		System.out.println("Argument 1: ID of Tester (long)");
		System.out.println("Argument 2: TestType (" + TestType.COMPLETENESS.ordinal() + "=" + TestType.COMPLETENESS.toString() + ", " + TestType.CORRECTNESS.ordinal() + "=" + TestType.CORRECTNESS.toString() + ")");
		System.out.println("Argument 3: Port of shared workspace server");
		System.exit(-1);
	}

	@Override
	public void run() {
		test();
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
