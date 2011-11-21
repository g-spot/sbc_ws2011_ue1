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
import at.ac.tuwien.complang.sbc11.factory.SharedWorkspaceMozartImpl;
import at.ac.tuwien.complang.sbc11.factory.exception.SharedWorkspaceException;
import at.ac.tuwien.complang.sbc11.parts.CPU;
import at.ac.tuwien.complang.sbc11.parts.Computer;
import at.ac.tuwien.complang.sbc11.parts.GraphicBoard;
import at.ac.tuwien.complang.sbc11.parts.Mainboard;
import at.ac.tuwien.complang.sbc11.parts.Part;
import at.ac.tuwien.complang.sbc11.parts.RAM;
import at.ac.tuwien.complang.sbc11.workers.Producer;

@SuppressWarnings("serial")
public class Factory extends JFrame {
	
	private JComboBox comboPartType;
	private JTextField textPartCount;
	private JTextField textErrorRate;
	private JTextArea textAreaLogParts;
	private JTextArea textAreaLogComputers;
	private JTextArea textAreaLogTrashBin;
	private JTextArea textAreaLogShipped;
	private JButton buttonAddProducer;
	
	private int workerCount = 0;
	private int producerCount = 0;
	
	private SharedWorkspace factory;
	private List<Part> partList = null;

	public Factory() {
		this.initUI();
		
		// initializes the mozart implementation of the shared workspace
		try {
			factory = new SharedWorkspaceMozartImpl(this);
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
				textAreaLogParts.setText("Unused parts in workspace\n");
				textAreaLogParts.append("-------------------------\n");
				if(partList != null)
					for(Part p:partList) {
						textAreaLogParts.append(p.toString() + "\n");
					}
			}
		});
		
	}
	
	public void updateComputerList() {
		textAreaLogComputers.setText("Unused computers in workspace\n");
		textAreaLogComputers.append("-------------------------------\n");
		try {
			for(Computer c:factory.getUntestedComputers()) {
				textAreaLogComputers.append(c.toString() + "\n");
			}
		} catch (SharedWorkspaceException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	public void updateTrashBinList() {
		textAreaLogTrashBin.setText("Computers in trash bin\n");
		textAreaLogTrashBin.append("----------------------\n");
	}
	
	public void updateShippedList() {
		textAreaLogShipped.setText("Shipped computers\n");
		textAreaLogShipped.append("-----------------\n");
	}
	
	private void addProducer(Class<?> partType, long partCount, double errorRate) {
		Producer producer = new Producer(partCount, errorRate, partType, factory);
		producer.setId(++workerCount);
		producerCount++;
		buttonAddProducer.setText("Add Producer (currently: " + producerCount + ")");
		Executors.defaultThreadFactory().newThread(producer).start();
	}
	
	private void initUI() {
		//this.setSize(800, 600);
		this.setSize(Toolkit.getDefaultToolkit().getScreenSize());
		this.setTitle("MozartFactory");
		
		JPanel mainPanel = new JPanel(new BorderLayout());
		
		JPanel topPanel = new JPanel(new GridLayout(4, 2));
		
		JLabel labelPartType = new JLabel("Part type");
		topPanel.add(labelPartType);
		comboPartType = new JComboBox();
		comboPartType.addItem(new String("CPU"));
		comboPartType.addItem(new String("Mainboard"));
		comboPartType.addItem(new String("RAM"));
		comboPartType.addItem(new String("GraphicBoard"));
		topPanel.add(comboPartType);
		JLabel labelPartCount = new JLabel("Part count");
		topPanel.add(labelPartCount);
		textPartCount = new JTextField("10");
		topPanel.add(textPartCount);
		JLabel labelPartErrorRate = new JLabel("Error rate");
		topPanel.add(labelPartErrorRate);
		textErrorRate = new JTextField("0.1");
		topPanel.add(textErrorRate);
		//topPanel.add(new JLabel());
		JButton buttonTest = new JButton("Update blackboard manually");
		topPanel.add(buttonTest);
		buttonAddProducer = new JButton("Add Producer (currently: " + producerCount + ")");
		topPanel.add(buttonAddProducer);
		
		// TODO remove test code
		buttonTest.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateAllLists();
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
		topPanel.setBorder(title);
		
		mainPanel.add(topPanel, BorderLayout.PAGE_START);
		
		JPanel centerPanel = new JPanel(new GridLayout(1, 4));
		
		textAreaLogParts = new JTextArea();
		JScrollPane scrollPaneLogParts = new JScrollPane(textAreaLogParts);
		centerPanel.add(scrollPaneLogParts);
		
		textAreaLogComputers = new JTextArea();
		JScrollPane scrollPangeLogUntested = new JScrollPane(textAreaLogComputers);
		centerPanel.add(scrollPangeLogUntested);
		
		textAreaLogTrashBin = new JTextArea();
		JScrollPane scrollPaneLogTrashBin = new JScrollPane(textAreaLogTrashBin);
		centerPanel.add(scrollPaneLogTrashBin);
		
		textAreaLogShipped = new JTextArea();
		JScrollPane scrollPaneLogShipped = new JScrollPane(textAreaLogShipped);
		centerPanel.add(scrollPaneLogShipped);
		
		mainPanel.add(centerPanel, BorderLayout.CENTER);
		
		this.setContentPane(mainPanel);
		this.setVisible(true);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		Factory factory = new Factory();
	}

}
