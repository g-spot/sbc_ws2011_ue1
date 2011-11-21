package at.ac.tuwien.complang.sbc11.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.border.TitledBorder;

import at.ac.tuwien.complang.sbc11.factory.SharedWorkspace;
import at.ac.tuwien.complang.sbc11.factory.SharedWorkspaceMozartImpl;
import at.ac.tuwien.complang.sbc11.factory.exception.SharedWorkspaceException;
import at.ac.tuwien.complang.sbc11.parts.CPU;
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
	private JTextArea textAreaLog;
	
	private int producerCount = 0;
	
	private SharedWorkspace factory;

	public Factory() {
		this.initUI();
		
		// initializes the mozart implementation of the shared workspace
		try {
			factory = new SharedWorkspaceMozartImpl(this);
		} catch (SharedWorkspaceException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
		
		// initializes an alternative implementation of the shared workspace
		//factory = new SharedWorkspaceAlternativeImpl();
	}
	
	public void updateBlackboard() {
		textAreaLog.setText("");
		try {
			for(Part p:factory.getAvailableParts()) {
				textAreaLog.append(p.toString() + "\n");
			}
		} catch (SharedWorkspaceException e) {
			JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
	
	private void addProducer(Class<?> partType, long partCount, double errorRate) {
		Producer producer = new Producer(partCount, errorRate, partType, factory);
		producer.setId(++producerCount);
		Executors.defaultThreadFactory().newThread(producer).start();
	}
	
	private void initUI() {
		this.setSize(800, 600);
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
		JButton buttonTestNewId = new JButton("Test id retrieval");
		topPanel.add(buttonTestNewId);
		JButton buttonAddProducer = new JButton("Add Producer");
		topPanel.add(buttonAddProducer);
		
		buttonTestNewId.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					long nextID = factory.getNextPartId();
					JOptionPane.showMessageDialog(null, "Next id: " + nextID);
				} catch (SharedWorkspaceException e1) {
					JOptionPane.showMessageDialog(null, e1.getMessage());
				}
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
		
		textAreaLog = new JTextArea();
		JScrollPane scrollPaneLog = new JScrollPane(textAreaLog);
		mainPanel.add(scrollPaneLog, BorderLayout.CENTER);
		
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
