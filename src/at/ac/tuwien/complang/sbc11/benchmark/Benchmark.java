package at.ac.tuwien.complang.sbc11.benchmark;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.MzsCore;

import at.ac.tuwien.complang.sbc11.factory.SharedWorkspace;
import at.ac.tuwien.complang.sbc11.factory.SharedWorkspaceHelper;
import at.ac.tuwien.complang.sbc11.factory.exception.SharedWorkspaceException;
import at.ac.tuwien.complang.sbc11.parts.CPU;
import at.ac.tuwien.complang.sbc11.parts.Computer;
import at.ac.tuwien.complang.sbc11.parts.GraphicBoard;
import at.ac.tuwien.complang.sbc11.parts.Mainboard;
import at.ac.tuwien.complang.sbc11.parts.Part;
import at.ac.tuwien.complang.sbc11.parts.CPU.CPUType;
import at.ac.tuwien.complang.sbc11.parts.RAM;

public class Benchmark {
	
	private static final String URI_FACTORY_ONE = "xvsm://localhost:7777";
	private static final String URI_FACTORY_TWO = "xvsm://localhost:8888";
	private static final String URI_FACTORY_THREE = "xvsm://localhost:9999";
	public static final int PORT_START_SIGNAL = 7887;
	
	private SharedWorkspace factory1;
	private SharedWorkspace factory2;
	private SharedWorkspace factory3;
	private long partId = 1;
	
	public Benchmark() {
		//try {
			//URI spaceURI = new URI("xvsm://localhost:" + String.valueOf(PORT_START_SIGNAL));
			
		@SuppressWarnings("unused")
		MzsCore core = DefaultMzsCore.newInstance(PORT_START_SIGNAL);
			//Capi capi = new Capi(core);
		//} catch (URISyntaxException e) {
		//	e.printStackTrace();
		//}
	}
	
	public void connectFactories() {
		try {
			factory1 = SharedWorkspaceHelper.getWorkspaceImplementation(URI_FACTORY_ONE);
			factory2 = SharedWorkspaceHelper.getWorkspaceImplementation(URI_FACTORY_TWO);
			factory3 = SharedWorkspaceHelper.getWorkspaceImplementation(URI_FACTORY_THREE);
		} catch(SharedWorkspaceException e1) {
			e1.printStackTrace();
		}
	}
	
	public void preprocessResources() {
		try {
			// factory 1
			factory1.addParts(createPartList(CPU.class, 500));
			factory1.addParts(createPartList(Mainboard.class, 500));
			factory1.addParts(createPartList(RAM.class, 800));
			factory1.addParts(createPartList(GraphicBoard.class, 300));
			
			// factory 2
			factory2.addParts(createPartList(RAM.class, 1800));
			factory2.addParts(createPartList(CPU.class, 100));
			factory2.addParts(createPartList(Mainboard.class, 10));
			//factory2.addParts(createPartList(GraphicBoard.class, 0));
			
			// factory 3
			factory3.addParts(createPartList(CPU.class, 2000));
			factory3.addParts(createPartList(Mainboard.class, 1000));
			factory3.addParts(createPartList(GraphicBoard.class, 1300));
			factory3.addParts(createPartList(RAM.class, 50));
			
		} catch (SharedWorkspaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private List<Part> createPartList(Class<?> partClass, int partCount) {
		List<Part> partList = new ArrayList<Part>();
		for(int i=0;i<partCount;i++) {
			Part part;
			try {
				part = (Part) partClass.newInstance();
			} catch (InstantiationException e) {
				return null;
			} catch (IllegalAccessException e) {
				return null;
			}
			part.setId(partId);
			part.setDefect(false);
			part.setProducer(null);
			if(partClass.equals((CPU.class)))
				((CPU)part).setCpuType(CPUType.SINGLE_CORE);
			partList.add(part);
			partId++;
		}
		return partList;
	}
	
	public void startBenchmark() {
		try {
			SharedWorkspaceHelper.sendStartSignal();
		} catch (SharedWorkspaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void stopBenchmark() {
		try {
			SharedWorkspaceHelper.sendStopSignal();
		} catch (SharedWorkspaceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void printResults() {
		try {
			// factory 1
			List<Computer> computersFactory1 = factory1.getShippedComputers();
			int sumFactory1;
			if(computersFactory1 != null)
				sumFactory1 = computersFactory1.size();
			else
				sumFactory1 = 0;
			System.out.println("Factory 1 shipped " + sumFactory1 + " computers");
			
			// factory 2
			List<Computer> computersFactory2 = factory2.getShippedComputers();
			int sumFactory2;
			if(computersFactory2 != null)
				sumFactory2 = computersFactory2.size();
			else
				sumFactory2 = 0;
			System.out.println("Factory 2 shipped " + sumFactory2 + " computers");
			
			// factory 3
			List<Computer> computersFactory3 = factory3.getShippedComputers();
			int sumFactory3;
			if(computersFactory3 != null)
				sumFactory3 = computersFactory3.size();
			else
				sumFactory3 = 0;
			System.out.println("Factory 3 shipped " + sumFactory3 + " computers");
			
			System.out.println("Total: " + (sumFactory1 + sumFactory2 + sumFactory3) + " computers shipped");
		} catch(SharedWorkspaceException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Initializing benchmark server...");
		Benchmark benchmark = new Benchmark();
		System.out.println("Successfully initialized benchmark server.");
		System.out.println("Please start factories and workers now. Hit (y) key when finished...");
		waitForInput();
		benchmark.connectFactories();
		System.out.println("Successfully connected to all factories.");
		System.out.println("Hit (y) key to start preprocessing of resources...");
		waitForInput();
		benchmark.preprocessResources();
		System.out.println("Successfully preprocessed all resources.");
		System.out.println("Hit (y) to start benchmark...");
		waitForInput();
		benchmark.startBenchmark();
		System.out.println("Benchmark running...");
		
		for(int i=1;i<=60;i++) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			System.out.print("#");
		}
		
		benchmark.stopBenchmark();
		System.out.println("Benchmark stopped.");
		benchmark.printResults();
		System.out.println("Exiting...");
		System.exit(0);
	}
	
	public static void waitForInput() {
		char command = ' ';
		do {
			try {
				command = (char)System.in.read();
			} catch (IOException e) { 
				System.out.println("Error reading from command line (" + e.getMessage() + ")");
			}
		} while(command != 'y');
	}

}
