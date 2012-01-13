package at.ac.tuwien.complang.sbc11.workers;

import java.awt.GridLayout;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import at.ac.tuwien.complang.sbc11.benchmark.BenchmarkStopper;
import at.ac.tuwien.complang.sbc11.factory.SharedWorkspace;
import at.ac.tuwien.complang.sbc11.factory.SharedWorkspaceHelper;
import at.ac.tuwien.complang.sbc11.factory.exception.SharedWorkspaceException;
import at.ac.tuwien.complang.sbc11.parts.CPU;
import at.ac.tuwien.complang.sbc11.parts.GraphicBoard;
import at.ac.tuwien.complang.sbc11.parts.Mainboard;
import at.ac.tuwien.complang.sbc11.parts.Part;
import at.ac.tuwien.complang.sbc11.parts.RAM;
import at.ac.tuwien.complang.sbc11.workers.shutdown.SecureShutdownApplication;

public class LoadBalancer extends JFrame implements SecureShutdownApplication, Serializable {

	private static final long serialVersionUID = -7849712771488583918L;
	transient private static final int BALANCE_LIMIT = 51;
	transient private static final String NEWLINE = "\n";
	
	transient private Logger logger;
	transient private List<SharedWorkspace> factoryList;
	transient private Map<SharedWorkspace, PartCount> partCountMap;
	
	transient private JTextArea textAreaParts;
	transient private JTextArea textAreaLog;
	
	private long id;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public LoadBalancer() {
		logger = Logger.getLogger("at.ac.tuwien.complang.sbc11.workers.Assembler");
		factoryList = new ArrayList<SharedWorkspace>();
		initUI();
	}
	
	private void initUI() {
		this.setTitle("Load Balancer");
		this.setSize(1024, 400);
		
		JPanel mainPanel = new JPanel(new GridLayout(1, 2));
		JPanel leftPanel = new JPanel(new GridLayout(1, 1));
		JPanel rightPanel = new JPanel(new GridLayout(1, 1));
		leftPanel.setBorder(BorderFactory.createTitledBorder("Parts all over the world"));
		rightPanel.setBorder(BorderFactory.createTitledBorder("Balancing log"));
		textAreaParts = new JTextArea();
		textAreaLog = new JTextArea();
		JScrollPane scrollPaneParts = new JScrollPane(textAreaParts);
		JScrollPane scrollPaneLog = new JScrollPane(textAreaLog);
		leftPanel.add(scrollPaneParts);
		rightPanel.add(scrollPaneLog);
		mainPanel.add(leftPanel);
		mainPanel.add(rightPanel);
		
		this.setContentPane(mainPanel);
		this.setVisible(true);
	}
	
	public void addFactory(String uri) {
		try {
			factoryList.add(SharedWorkspaceHelper.getWorkspaceImplementation(uri));
		} catch (SharedWorkspaceException e) {
			logger.severe("Workspace implementation could not be retrieved");
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length < 3) // at least id and 2 workspaces to deal with
			usage();
		long id = 0;
		try {
			id = Long.parseLong(args[0]);
		} catch(NumberFormatException e) {
			usage();
		}
		LoadBalancer balancer = new LoadBalancer();
		balancer.setId(id);
		for(int i=1;i<args.length;i++) {
			System.out.println(args[i]);
			balancer.addFactory(args[i]);
		}
		balancer.run();
	}
	
	public static void usage() {
		System.out.println("Argument 1: ID of LoadBalancer (long)");
		System.out.println("Following arguments: uris of shared workspaces to deal with (string)");
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
		balance();
	}

	@Override
	public void shutdown() {
		if(factoryList != null) {
			for(SharedWorkspace sharedWorkspace:factoryList) {
				try {
					sharedWorkspace.secureShutdown();
				} catch (SharedWorkspaceException e) {
					logger.severe(e.getMessage());
				}
			}
		}
		logger.info("bye.");
	}
	
	public void balance() {
		logger.info("Starting balancing...");
		do
		{
			partCountMap = new HashMap<SharedWorkspace, PartCount>();
			try {
				for(SharedWorkspace sharedWorkspace:factoryList) {
					sharedWorkspace.startTransaction();
					List<Part> partList = sharedWorkspace.getAvailableParts();
					PartCount partCount = new PartCount();
					for(Part part:partList) {
						if(part.getClass().equals(CPU.class))
							partCount.cpu++;
						else if(part.getClass().equals(Mainboard.class))
							partCount.mainboard++;
						else if(part.getClass().equals(RAM.class))
							partCount.ram++;
						else if(part.getClass().equals(GraphicBoard.class))
							partCount.graphicBoard++;
					}
					partCountMap.put(sharedWorkspace, partCount);
				}
				printPartCount();
				balanceParts();
				for(SharedWorkspace sharedWorkspace:factoryList) {
					sharedWorkspace.commitTransaction();
				}
			} catch(SharedWorkspaceException e) {
				// rollback all transactions
				for(SharedWorkspace sharedWorkspace:factoryList) {
					try {
						sharedWorkspace.rollbackTransaction();
					} catch(SharedWorkspaceException e1) {
						logger.severe(e1.getMessage());
					}
				}
			}
			// wait a second
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} while(true);
	}
	
	private void balanceParts() throws SharedWorkspaceException {
		List<Class<?>> partTypes = new ArrayList<Class<?>>();
		partTypes.add(CPU.class);
		partTypes.add(Mainboard.class);
		partTypes.add(RAM.class);
		partTypes.add(GraphicBoard.class);
		
		// check for every factory and every part type, if there is a shortage
		for(SharedWorkspace sharedWorkspaceLow:partCountMap.keySet()) {
			for(Class<?> partType:partTypes) {
				
				// found a factory with low resources of this part type
				if(partCountMap.get(sharedWorkspaceLow).lowParts(partType)) {
					
					SharedWorkspace sharedWorkspaceLoadFrom = null;
					appendActionLog(sharedWorkspaceLow.getWorkspaceID() + " needs " + partType.getSimpleName() + " (only " + partCountMap.get(sharedWorkspaceLow).getCount(partType) + " left)");
					
					// search for another factory with enough resources of this part type
					// take the factory with the highest amount of resources of this part type
					for(SharedWorkspace sharedWorkspaceHigh:partCountMap.keySet()) {
						
						if(!partCountMap.get(sharedWorkspaceHigh).lowParts(partType)) {
							if(sharedWorkspaceLoadFrom == null || 
									partCountMap.get(sharedWorkspaceHigh).getCount(partType) >
									partCountMap.get(sharedWorkspaceLoadFrom).getCount(partType))
								sharedWorkspaceLoadFrom = sharedWorkspaceHigh;
						}
					}
					
					// there is no factory with enough resources of this part type
					if(sharedWorkspaceLoadFrom == null) {
						appendActionLog("Not able to balance - not enough resources in other factories" + NEWLINE);
					}
					else {
						// found a factory with enough resources --> balance
						List<Part> partList = sharedWorkspaceLoadFrom.takeParts(partType, false, partCountMap.get(sharedWorkspaceLoadFrom).getCount(partType) / 2);
						if(partList != null) {
							sharedWorkspaceLow.addParts(partList);
							partCountMap.get(sharedWorkspaceLoadFrom).setCount(partType, partCountMap.get(sharedWorkspaceLoadFrom).getCount(partType) - partList.size());
							partCountMap.get(sharedWorkspaceLow).setCount(partType, partCountMap.get(sharedWorkspaceLow).getCount(partType) + partList.size());
							appendActionLog("Loaded " + partList.size() + " " + partType.getSimpleName() + "s from " + sharedWorkspaceLoadFrom.getWorkspaceID() + NEWLINE);
						}
					}
				}
			}
		}
	}
	
	private void printPartCount() {
		textAreaParts.setText("");
		for(SharedWorkspace sharedWorkspace:partCountMap.keySet()) {
			textAreaParts.append(sharedWorkspace.getWorkspaceID() + NEWLINE);
			textAreaParts.append(partCountMap.get(sharedWorkspace).toString() + NEWLINE + NEWLINE);
		}
	}
	
	private void appendActionLog(String message) {
		textAreaLog.append(message + NEWLINE);
	}
	
	private class PartCount {
		private int cpu;
		private int mainboard;
		private int ram;
		private int graphicBoard;
		
		public PartCount() {
			cpu = 0;
			mainboard = 0;
			ram = 0;
			graphicBoard = 0;
		}
		
		public String toString() {
			String result = "";
			result += "  #CPU: " + cpu + NEWLINE;
			result += "  #MAINBOARD: " + mainboard + NEWLINE;
			result += "  #RAM: " + ram + NEWLINE;
			result += "  #GRAPHICBOARD: " + graphicBoard;
			return result;
		}
		
		public boolean lowParts(Class<?> partType) {
			if(partType.equals(CPU.class))
				return cpu < BALANCE_LIMIT;
			if(partType.equals(Mainboard.class))
				return mainboard < BALANCE_LIMIT;
			if(partType.equals(RAM.class))
				return ram < BALANCE_LIMIT;
			if(partType.equals(GraphicBoard.class))
				return graphicBoard < BALANCE_LIMIT;
			return false;
		}
		
		public int getCount(Class<?> partType) {
			if(partType.equals(CPU.class))
				return cpu;
			if(partType.equals(Mainboard.class))
				return mainboard;
			if(partType.equals(RAM.class))
				return ram;
			if(partType.equals(GraphicBoard.class))
				return graphicBoard;
			return 0;
		}
		
		public void setCount(Class<?> partType, int count) {
			if(partType.equals(CPU.class))
				cpu = count;
			if(partType.equals(Mainboard.class))
				mainboard = count;
			if(partType.equals(RAM.class))
				ram = count;
			if(partType.equals(GraphicBoard.class))
				graphicBoard = count;
		}
	}

}
