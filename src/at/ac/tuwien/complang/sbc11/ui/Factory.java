package at.ac.tuwien.complang.sbc11.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import at.ac.tuwien.complang.sbc11.factory.SharedWorkspace;
import at.ac.tuwien.complang.sbc11.factory.SharedWorkspaceHelper;
import at.ac.tuwien.complang.sbc11.factory.SharedWorkspaceMozartImpl;
import at.ac.tuwien.complang.sbc11.factory.exception.SharedWorkspaceException;
import at.ac.tuwien.complang.sbc11.parts.CPU;
import at.ac.tuwien.complang.sbc11.parts.CPU.CPUType;
import at.ac.tuwien.complang.sbc11.parts.Computer;
import at.ac.tuwien.complang.sbc11.parts.GraphicBoard;
import at.ac.tuwien.complang.sbc11.parts.Mainboard;
import at.ac.tuwien.complang.sbc11.parts.Order;
import at.ac.tuwien.complang.sbc11.parts.Part;
import at.ac.tuwien.complang.sbc11.parts.RAM;
import at.ac.tuwien.complang.sbc11.workers.Producer;

@SuppressWarnings("serial")
public class Factory extends JFrame {
	
	private JComboBox comboPartType;
	private JTextField textPartCount;
	private JTextField textErrorRate;
	private JTextArea textAreaLogParts;
	private JTextArea textAreaLogUntestedComputers;
	private JTextArea textAreaLogDeconstructedComputers;
	private JTextArea textAreaLogFinishedOrders;
	private JTextArea textAreaLogShipped;
	private JTextArea textAreaActionLog;
	private JTextArea textAreaLogUnfinishedOrders;
	private JButton buttonAddProducer;
	private JTextField textOrderComputerCount;
	private JComboBox comboOrderCPUType;
	private JCheckBox checkOrderUseGraphicBoard;
	private JTextField textOrderRAMCount;
	private JButton buttonAddOrder;
	
	private int workerCount = 0;
	private int producerCount = 0;
	private int orderCount = 0;
	
	private SharedWorkspace factory;
	private List<Part> partList = null;
	
	private final char NEWLINE = '\n';

	public Factory() {
		this.initUI();
		
		
		// initializes the mozart implementation of the shared workspace
		try {
			if(SharedWorkspaceHelper.useMozartImplementation())
			{
				int serverPort;
				do {
					try {
						serverPort = Integer.parseInt(JOptionPane.showInputDialog("Port:"));
						factory = new SharedWorkspaceMozartImpl(this, serverPort);
						break;
					} catch(SharedWorkspaceException e1) {
						JOptionPane.showMessageDialog(this, e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					} catch(NumberFormatException e2) {
						JOptionPane.showMessageDialog(this, e2.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					} catch(Exception e3) {
						JOptionPane.showMessageDialog(this, e3.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
				} while(true);
				this.setTitle(factory.getWorkspaceID());
			}
			else
				factory = SharedWorkspaceHelper.getWorkspaceImplementation(this, 0);
		} catch (SharedWorkspaceException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		
		updateAllLists();
	}
	
	public void updateAllLists() {
		updatePartList();
		updateComputerList();
		updateFinishedOrderList();
		updateUnfinishedOrderList();
		updateShippedList();
	}
	
	public void updatePartList() {
		partList = null;
		try {
			partList = factory.getAvailableParts();
		} catch (SharedWorkspaceException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			
		}
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				textAreaLogParts.setText("");
				if(partList != null)
					textAreaLogParts.append(partList.size() + " total." + NEWLINE + NEWLINE);
					for(Part p:partList) {
						textAreaLogParts.append(p.toString() + NEWLINE);
					}
			}
		});
		
	}
	
	public void updateComputerList() {
		textAreaLogUntestedComputers.setText("");
		textAreaLogDeconstructedComputers.setText("");
		try {
			List<Computer> incompleteComputers = factory.getIncompleteComputers();
			if(incompleteComputers != null)
				textAreaLogUntestedComputers.append(incompleteComputers.size() + " total." + NEWLINE + NEWLINE);
			for(Computer c:incompleteComputers) {
				textAreaLogUntestedComputers.append(c.toString() + NEWLINE + NEWLINE);
			}
			List<Computer> deconstructedComputers = factory.getDeconstructedComputers();
			if(deconstructedComputers != null)
				textAreaLogDeconstructedComputers.append(deconstructedComputers.size() + " total." + NEWLINE + NEWLINE);
			for(Computer c:deconstructedComputers) {
				textAreaLogDeconstructedComputers.append(c.toString() + NEWLINE + NEWLINE);
			}
		} catch (SharedWorkspaceException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	public void updateFinishedOrderList() {
		textAreaLogFinishedOrders.setText("");
		try {
			List<Order> finishedOrders = factory.getFinishedOrders();
			if(finishedOrders != null)
				textAreaLogFinishedOrders.append(finishedOrders.size() + " total." + NEWLINE + NEWLINE);
			for(Order o:finishedOrders) {
				textAreaLogFinishedOrders.append(o.toString() + NEWLINE + NEWLINE);
			}
		} catch (SharedWorkspaceException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	public void updateShippedList() {
		textAreaLogShipped.setText("");
		try {
			List<Computer> shippedComputers = factory.getShippedComputers();
			if(shippedComputers != null)
				textAreaLogShipped.append(shippedComputers.size() + " total." + NEWLINE + NEWLINE);
			for(Computer c:shippedComputers) {
				textAreaLogShipped.append(c.toString() + NEWLINE + NEWLINE);
			}
		} catch (SharedWorkspaceException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	public void updateUnfinishedOrderList() {
		textAreaLogUnfinishedOrders.setText("");
		try {
			List<Order> unfinishedOrders = factory.getUnfinishedOrders();
			if(unfinishedOrders != null)
				textAreaLogUnfinishedOrders.append(unfinishedOrders.size() + " total." + NEWLINE + NEWLINE);
			for(Order o:unfinishedOrders) {
				textAreaLogUnfinishedOrders.append(o.toString() + NEWLINE + NEWLINE);
			}
		} catch (SharedWorkspaceException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	public void appendActionLog(String message) {
		//String text = textAreaActionLog.getText();
		//textAreaActionLog.setText(text + message); // workaround for automatic scrolling
		textAreaActionLog.append(message);
		textAreaActionLog.setCaretPosition(textAreaActionLog.getText().length() - 1);
	}
	
	private void addProducer(Class<?> partType, long partCount, double errorRate) {
		Producer producer = new Producer(partCount, errorRate, partType, factory);
		producer.setId(++workerCount);
		producerCount++;
		buttonAddProducer.setText("Add Producer (currently: " + producerCount + ")");
		Executors.defaultThreadFactory().newThread(producer).start();
	}
	
	private void addOrder(int computerCount, CPUType cpuType, int ramCount, boolean useGraphicBoard) {
		Order order = new Order((long)++orderCount, computerCount, cpuType, ramCount, useGraphicBoard);
		try {
			factory.addOrder(order);
			JOptionPane.showMessageDialog(this, "Successfully added order with ID=" + orderCount);
		} catch (SharedWorkspaceException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	//TODO remove test code
	public void test2() {
		try {
			//Computer computer = factory.takeCompletelyTestedComputer();
			List<CPU> p = factory.takeCPU(CPUType.DUAL_CORE, false, 1);
			if(p != null)
				JOptionPane.showMessageDialog(this, "Took: " + p.get(0).toString());
			else
				JOptionPane.showMessageDialog(this, "Result is null");
		} catch (SharedWorkspaceException e) {
			JOptionPane.showMessageDialog(this, e.getMessage());
		}
	}
	
	private void initUI() {
		//this.setSize(800, 600);
		this.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		this.setTitle("MozartFactory");

		JPanel mainPanel = new JPanel(new BorderLayout());
		
		JPanel topPanel = new JPanel(new GridLayout(1, 2));
		topPanel.setPreferredSize(new Dimension(200,200));
		JPanel actionLogPanel = new JPanel(new GridLayout(1, 1));
		TitledBorder titleActionLog = BorderFactory.createTitledBorder("Action log");
		actionLogPanel.setBorder(titleActionLog);
		textAreaActionLog = new JTextArea();
		JScrollPane scrollPaneActionLog = new JScrollPane(textAreaActionLog);
		actionLogPanel.add(scrollPaneActionLog);
		
		JPanel orderLogPanel = new JPanel(new GridLayout(1, 1));
		TitledBorder titleOrderLog = BorderFactory.createTitledBorder("Unfinished Orders");
		orderLogPanel.setBorder(titleOrderLog);
		textAreaLogUnfinishedOrders = new JTextArea();
		JScrollPane scrollPaneOrderLog = new JScrollPane(textAreaLogUnfinishedOrders);
		orderLogPanel.add(scrollPaneOrderLog);
		
		JPanel formProducerPanel = new JPanel(new GridLayout(4, 2));
		
		JLabel labelPartType = new JLabel("Part type");
		formProducerPanel.add(labelPartType);
		comboPartType = new JComboBox();
		comboPartType.addItem(new String("CPU"));
		comboPartType.addItem(new String("Mainboard"));
		comboPartType.addItem(new String("RAM"));
		comboPartType.addItem(new String("GraphicBoard"));
		formProducerPanel.add(comboPartType);
		JLabel labelPartCount = new JLabel("Part count");
		formProducerPanel.add(labelPartCount);
		textPartCount = new JTextField("10");
		formProducerPanel.add(textPartCount);
		JLabel labelPartErrorRate = new JLabel("Error rate");
		formProducerPanel.add(labelPartErrorRate);
		textErrorRate = new JTextField("0.1");
		formProducerPanel.add(textErrorRate);
		//topPanel.add(new JLabel());
		JPanel testPanel = new JPanel();
		JButton buttonTest = new JButton("Update blackboard manually");
		JButton buttonTest2 = new JButton("Test");
		testPanel.add(buttonTest);
		//testPanel.add(buttonTest2);
		formProducerPanel.add(testPanel);
		buttonAddProducer = new JButton("Add Producer (currently: " + producerCount + ")");
		formProducerPanel.add(buttonAddProducer);
		
		// TODO remove test code
		buttonTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateAllLists();
			}
		});
		
		buttonTest2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				test2();
			}
		});
		
		buttonAddProducer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try
				{
					String partType = (String)comboPartType.getSelectedItem();
					Class<?> partTypeClass = null;
					long partCount = Long.parseLong(textPartCount.getText());
					double errorRate = Double.parseDouble(textErrorRate.getText());
					if(partType.equals("CPU")) {
						partTypeClass = CPU.class;
					} else if(partType.equals("Mainboard")) {
						partTypeClass = Mainboard.class;
					} else if(partType.equals("RAM")) {
						partTypeClass = RAM.class;
					} else if(partType.equals("GraphicBoard")) {
						partTypeClass = GraphicBoard.class;
					} else {
						throw new Exception("Part type not correct");
					}
					addProducer(partTypeClass, partCount, errorRate);
				} catch(Exception ex) {
					JOptionPane.showMessageDialog(null, "Fehler: " + ex.getMessage());
					ex.printStackTrace();
				}
			}
		});
		
		JPanel formOrderPanel = new JPanel(new GridLayout(5, 2));
		JLabel labelOrderComputerCount = new JLabel("Computer count");
		textOrderComputerCount = new JTextField("10");
		JLabel labelOrderCPUType = new JLabel("CPU type");
		comboOrderCPUType = new JComboBox();
		comboOrderCPUType.addItem(CPUType.SINGLE_CORE.toString());
		comboOrderCPUType.addItem(CPUType.DUAL_CORE.toString());
		comboOrderCPUType.addItem(CPUType.QUAD_CORE.toString());
		JLabel labelOrderUseGraphicBoard = new JLabel("Use graphic board");
		checkOrderUseGraphicBoard = new JCheckBox();
		JLabel labelOrderRAMCount = new JLabel("RAM count");
		textOrderRAMCount = new JTextField("2");
		buttonAddOrder = new JButton("Submit Order");
		formOrderPanel.add(labelOrderComputerCount);
		formOrderPanel.add(textOrderComputerCount);
		formOrderPanel.add(labelOrderCPUType);
		formOrderPanel.add(comboOrderCPUType);
		formOrderPanel.add(labelOrderRAMCount);
		formOrderPanel.add(textOrderRAMCount);
		formOrderPanel.add(labelOrderUseGraphicBoard);
		formOrderPanel.add(checkOrderUseGraphicBoard);
		formOrderPanel.add(new JPanel());
		formOrderPanel.add(buttonAddOrder);
		
		formProducerPanel.setBorder(BorderFactory.createTitledBorder("New Producer"));
		formOrderPanel.setBorder(BorderFactory.createTitledBorder("New Order"));
		
		buttonAddOrder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					int computerCount = Integer.parseInt(textOrderComputerCount.getText());
					CPUType cpuType = CPUType.valueOf((String) comboOrderCPUType.getSelectedItem());
					int ramCount = Integer.parseInt(textOrderRAMCount.getText());
					boolean useGraphicBoard = checkOrderUseGraphicBoard.isSelected();
					addOrder(computerCount, cpuType, ramCount, useGraphicBoard);
				} catch(Exception ex) {
					JOptionPane.showMessageDialog(null, "Fehler: " + ex.getMessage());
					ex.printStackTrace();
				}
			}
		});
		
		topPanel.add(actionLogPanel);
		topPanel.add(orderLogPanel);
		
		mainPanel.add(topPanel, BorderLayout.PAGE_START);
		
		JPanel centerPanel = new JPanel(new GridLayout(1, 4));
		
		JPanel logPartsPanel = new JPanel(new GridLayout(1, 1));
		logPartsPanel.setBorder(BorderFactory.createTitledBorder("Unused parts in workspace"));
		textAreaLogParts = new JTextArea();
		JScrollPane scrollPaneLogParts = new JScrollPane(textAreaLogParts);
		logPartsPanel.add(scrollPaneLogParts);
		centerPanel.add(logPartsPanel);
		
		JPanel logComputersPanel = new JPanel(new GridLayout(2, 1));
		JPanel logUntestedComputersPanel = new JPanel(new GridLayout(1, 1));
		logUntestedComputersPanel.setBorder(BorderFactory.createTitledBorder("Unused computers in workspace"));
		textAreaLogUntestedComputers = new JTextArea();
		JScrollPane scrollPaneLogUntested = new JScrollPane(textAreaLogUntestedComputers);
		logUntestedComputersPanel.add(scrollPaneLogUntested);
		logComputersPanel.add(logUntestedComputersPanel);
		JPanel logDeconstructedComputersPanel = new JPanel(new GridLayout(1, 1));
		logDeconstructedComputersPanel.setBorder(BorderFactory.createTitledBorder("Deconstructed computers"));
		textAreaLogDeconstructedComputers = new JTextArea();
		JScrollPane scrollPaneLogDeconstructed = new JScrollPane(textAreaLogDeconstructedComputers);
		logDeconstructedComputersPanel.add(scrollPaneLogDeconstructed);
		logComputersPanel.add(logDeconstructedComputersPanel);
		centerPanel.add(logComputersPanel);
		
		JPanel logFinishedOrderPanel = new JPanel(new GridLayout(1, 1));
		logFinishedOrderPanel.setBorder(BorderFactory.createTitledBorder("Finished orders"));
		textAreaLogFinishedOrders = new JTextArea();
		JScrollPane scrollPaneLogFinishedOrders = new JScrollPane(textAreaLogFinishedOrders);
		logFinishedOrderPanel.add(scrollPaneLogFinishedOrders);
		centerPanel.add(logFinishedOrderPanel);
		
		JPanel logShippedPanel = new JPanel(new GridLayout(1, 1));
		logShippedPanel.setBorder(BorderFactory.createTitledBorder("Shipped computers"));
		textAreaLogShipped = new JTextArea();
		JScrollPane scrollPaneLogShipped = new JScrollPane(textAreaLogShipped);
		logShippedPanel.add(scrollPaneLogShipped);
		centerPanel.add(logShippedPanel);
		
		mainPanel.add(centerPanel, BorderLayout.CENTER);
		
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add("Log", mainPanel);
		
		JPanel inputPanel = new JPanel(new GridLayout(4, 2));
		inputPanel.add(formProducerPanel);
		inputPanel.add(new JPanel());
		inputPanel.add(formOrderPanel);
		inputPanel.add(new JPanel());
		inputPanel.add(new JPanel());
		inputPanel.add(new JPanel());
		inputPanel.add(new JPanel());
		inputPanel.add(new JPanel());
		tabbedPane.add("Input", inputPanel);
		
		this.setContentPane(tabbedPane);
		this.setVisible(true);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				@SuppressWarnings("unused")
				Factory factory = new Factory();
			}
		});
	}

}
