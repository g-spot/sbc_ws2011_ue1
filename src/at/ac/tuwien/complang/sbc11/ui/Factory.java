package at.ac.tuwien.complang.sbc11.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

import at.ac.tuwien.complang.sbc11.factory.SharedWorkspace;
import at.ac.tuwien.complang.sbc11.factory.SharedWorkspaceHelper;
import at.ac.tuwien.complang.sbc11.factory.SharedWorkspaceMozartImpl;
import at.ac.tuwien.complang.sbc11.factory.exception.SharedWorkspaceException;
import at.ac.tuwien.complang.sbc11.parts.CPU;
import at.ac.tuwien.complang.sbc11.parts.Computer;
import at.ac.tuwien.complang.sbc11.parts.GraphicBoard;
import at.ac.tuwien.complang.sbc11.parts.Mainboard;
import at.ac.tuwien.complang.sbc11.parts.Part;
import at.ac.tuwien.complang.sbc11.parts.RAM;
import at.ac.tuwien.complang.sbc11.workers.Producer;
import at.ac.tuwien.complang.sbc11.workers.Tester.TestType;

@SuppressWarnings("serial")
public class Factory extends JFrame {
	
	private JComboBox comboPartType;
	private JTextField textPartCount;
	private JTextField textErrorRate;
	private JTextArea textAreaLogParts;
	private JTextArea textAreaLogComputers;
	private JTextArea textAreaLogTrashBin;
	private JTextArea textAreaLogShipped;
	private JTextArea textAreaActionLog;
	private JButton buttonAddProducer;
	
	private int workerCount = 0;
	private int producerCount = 0;
	
	private SharedWorkspace factory;
	private List<Part> partList = null;
	
	private final char NEWLINE = '\n';

	public Factory() {
		this.initUI();
		
		// initializes the mozart implementation of the shared workspace
		try {
			factory = SharedWorkspaceHelper.getWorkspaceImplementation(this);
		} catch (SharedWorkspaceException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		
		updateAllLists();
		
		// initializes an alternative implementation of the shared workspace
		//factory = new SharedWorkspaceAlternativeImpl();
	}
	
	public void updateAllLists() {
		updatePartList();
		updateComputerList();
		updateTrashBinList();
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
				//textAreaLogParts.setText("Unused parts in workspace" + NEWLINE);
				//textAreaLogParts.append("-------------------------" + NEWLINE);
				textAreaLogParts.setText("");
				if(partList != null)
					for(Part p:partList) {
						textAreaLogParts.append(p.toString() + NEWLINE);
					}
			}
		});
		
	}
	
	public void updateComputerList() {
		//textAreaLogComputers.setText("Unused computers in workspace" + NEWLINE);
		//textAreaLogComputers.append("-------------------------------" + NEWLINE);
		textAreaLogComputers.setText("");
		try {
			for(Computer c:factory.getIncompleteComputers()) {
				textAreaLogComputers.append(c.toString() + NEWLINE + NEWLINE);
			}
		} catch (SharedWorkspaceException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	public void updateTrashBinList() {
		//textAreaLogTrashBin.setText("Computers in trash bin" + NEWLINE);
		//textAreaLogTrashBin.append("----------------------" + NEWLINE);
	}
	
	public void updateShippedList() {
		//textAreaLogShipped.setText("Shipped computers" + NEWLINE);
		//textAreaLogShipped.append("-----------------" + NEWLINE);
	}
	
	public void updateActionLog() {
		
	}
	
	private void addProducer(Class<?> partType, long partCount, double errorRate) {
		Producer producer = new Producer(partCount, errorRate, partType, factory);
		producer.setId(++workerCount);
		producerCount++;
		buttonAddProducer.setText("Add Producer (currently: " + producerCount + ")");
		Executors.defaultThreadFactory().newThread(producer).start();
	}
	
	//TODO remove test code
	public void test2() {
		try {
			Computer computer = factory.takeUntestedComputer(TestType.COMPLETENESS);
			JOptionPane.showMessageDialog(this, "Took: " + computer.toString());
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
		JPanel actionLogPanel = new JPanel(new GridLayout(1, 1));
		TitledBorder titleActionLog = BorderFactory.createTitledBorder("Action log");
		actionLogPanel.setBorder(titleActionLog);
		textAreaActionLog = new JTextArea();
		JScrollPane scrollPaneActionLog = new JScrollPane(textAreaActionLog);
		actionLogPanel.add(scrollPaneActionLog);
		
		
		JPanel formPanel = new JPanel(new GridLayout(4, 2));
		
		JLabel labelPartType = new JLabel("Part type");
		formPanel.add(labelPartType);
		comboPartType = new JComboBox();
		comboPartType.addItem(new String("CPU"));
		comboPartType.addItem(new String("Mainboard"));
		comboPartType.addItem(new String("RAM"));
		comboPartType.addItem(new String("GraphicBoard"));
		formPanel.add(comboPartType);
		JLabel labelPartCount = new JLabel("Part count");
		formPanel.add(labelPartCount);
		textPartCount = new JTextField("10");
		formPanel.add(textPartCount);
		JLabel labelPartErrorRate = new JLabel("Error rate");
		formPanel.add(labelPartErrorRate);
		textErrorRate = new JTextField("0.1");
		formPanel.add(textErrorRate);
		//topPanel.add(new JLabel());
		JPanel testPanel = new JPanel();
		JButton buttonTest = new JButton("Update blackboard manually");
		JButton buttonTest2 = new JButton("Test");
		testPanel.add(buttonTest);
		testPanel.add(buttonTest2);
		formPanel.add(testPanel);
		buttonAddProducer = new JButton("Add Producer (currently: " + producerCount + ")");
		formPanel.add(buttonAddProducer);
		
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
					JOptionPane.showMessageDialog(null, "hier fehler: " + ex.getMessage());
					ex.printStackTrace();
				}
			}
		});
		
		TitledBorder title = BorderFactory.createTitledBorder("New Producer");
		formPanel.setBorder(title);
		topPanel.add(formPanel);
		topPanel.add(actionLogPanel);
		
		mainPanel.add(topPanel, BorderLayout.PAGE_START);
		
		JPanel centerPanel = new JPanel(new GridLayout(1, 4));
		
		JPanel logPartsPanel = new JPanel(new GridLayout(1, 1));
		logPartsPanel.setBorder(BorderFactory.createTitledBorder("Unused parts in workspace"));
		textAreaLogParts = new JTextArea();
		JScrollPane scrollPaneLogParts = new JScrollPane(textAreaLogParts);
		logPartsPanel.add(scrollPaneLogParts);
		centerPanel.add(logPartsPanel);
		
		JPanel logComputersPanel = new JPanel(new GridLayout(1, 1));
		logComputersPanel.setBorder(BorderFactory.createTitledBorder("Unused computers in workspace"));
		textAreaLogComputers = new JTextArea();
		JScrollPane scrollPaneLogUntested = new JScrollPane(textAreaLogComputers);
		logComputersPanel.add(scrollPaneLogUntested);
		centerPanel.add(logComputersPanel);
		
		JPanel logTrashPanel = new JPanel(new GridLayout(1, 1));
		logTrashPanel.setBorder(BorderFactory.createTitledBorder("Computers in trash bin"));
		textAreaLogTrashBin = new JTextArea();
		JScrollPane scrollPaneLogTrashBin = new JScrollPane(textAreaLogTrashBin);
		logTrashPanel.add(scrollPaneLogTrashBin);
		centerPanel.add(logTrashPanel);
		
		JPanel logShippedPanel = new JPanel(new GridLayout(1, 1));
		logShippedPanel.setBorder(BorderFactory.createTitledBorder("Shipped computers"));
		textAreaLogShipped = new JTextArea();
		JScrollPane scrollPaneLogShipped = new JScrollPane(textAreaLogShipped);
		logShippedPanel.add(scrollPaneLogShipped);
		centerPanel.add(logShippedPanel);
		
		mainPanel.add(centerPanel, BorderLayout.CENTER);
		
		this.setContentPane(mainPanel);
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
