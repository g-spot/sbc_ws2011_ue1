package at.ac.tuwien.complang.sbc11.factory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.ActiveMQQueueBrowser;
import org.apache.activemq.ActiveMQSession;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.command.ActiveMQDestination;

import at.ac.tuwien.complang.sbc11.factory.exception.SharedWorkspaceException;
import at.ac.tuwien.complang.sbc11.parts.CPU;
import at.ac.tuwien.complang.sbc11.parts.CPU.CPUType;
import at.ac.tuwien.complang.sbc11.parts.Computer;
import at.ac.tuwien.complang.sbc11.parts.Order;
import at.ac.tuwien.complang.sbc11.parts.Part;
import at.ac.tuwien.complang.sbc11.ui.Factory;
import at.ac.tuwien.complang.sbc11.workers.AsyncAssembler;
import at.ac.tuwien.complang.sbc11.workers.Tester.TestState;
import at.ac.tuwien.complang.sbc11.workers.Tester.TestType;

/* Implements the shared workspace with an alternative technology.
 * The technology used is JMS. OpenJMS is the implementation that will be used here.
 * There are other implementations that are not completely conform to a standard.
 * OpenJMS provides a clean implementation of handling Objects.
 */


public class SharedWorkspaceJMSImpl extends SharedWorkspace 
{
	// General
	private Logger logger;
	private Factory factory;
	
	//ActiveMQ
	private ActiveMQConnectionFactory connectionFactory;
	private ActiveMQConnection connection;
	private ActiveMQSession session;
	private ActiveMQDestination destination;
	private int serverPort;
	
	/*
	 * Functions here do the same as in SharedWorkspaceMozartImpl but with ActiveMQ
	 * 
	 */
	
	public SharedWorkspaceJMSImpl() throws SharedWorkspaceException 
	{
		super(null);
		
		this.factory = null;
		logger = Logger.getLogger("at.ac.tuwien.complang.sbc11.factory.SharedWorkspaceJMSImpl");
		
		// ActiveMQ init		
		BrokerService broker = new BrokerService();
		broker.setUseJmx(true);
		serverPort = 0;
		try 
		{
			broker.addConnector("tcp://localhost:61616");
			broker.start();
			
			connectionFactory = new ActiveMQConnectionFactory(
					                  ActiveMQConnection.DEFAULT_USER,
					                  ActiveMQConnection.DEFAULT_PASSWORD,
					                  ActiveMQConnection.DEFAULT_BROKER_URL);
			
			connection = (ActiveMQConnection) connectionFactory.createConnection();
	
			connection.start();
			
			
			//destination = session.createQueue("mmy first active mq queue");
			
			
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("Initialization done.");
	}
	public SharedWorkspaceJMSImpl(int serverPort) throws SharedWorkspaceException {
		super(null);
		
		this.factory = null;
		logger = Logger.getLogger("at.ac.tuwien.complang.sbc11.factory.SharedWorkspaceJMSImpl");
		
		// ActiveMQ init		
		BrokerService broker = new BrokerService();
		broker.setUseJmx(true);
		
		try 
		{
			this.serverPort = serverPort;
			broker.addConnector("tcp://localhost:" + String.valueOf(serverPort));
			broker.start();
			
			connectionFactory = new ActiveMQConnectionFactory(
					                  ActiveMQConnection.DEFAULT_USER,
					                  ActiveMQConnection.DEFAULT_PASSWORD,
					                  ActiveMQConnection.DEFAULT_BROKER_URL);
			
			connection = (ActiveMQConnection) connectionFactory.createConnection();
	
			connection.start();
			
			//destination = session.createQueue("mmy first active mq queue");
			
			
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("Initialization done.");
	}
	public SharedWorkspaceJMSImpl(Factory factory) throws SharedWorkspaceException 
	{
		super(factory);
		
		logger = Logger.getLogger("at.ac.tuwien.complang.sbc11.factory.SharedWorkspaceJMSImpl");
		this.factory = factory;	
		
		// ActiveMQ init		
		BrokerService broker = new BrokerService();
		broker.setUseJmx(true);
		serverPort = 0;
		
		try 
		{
			broker.addConnector("tcp://localhost:61616");
			broker.start();
			
			connectionFactory = new ActiveMQConnectionFactory(
					                ActiveMQConnection.DEFAULT_USER,
					                ActiveMQConnection.DEFAULT_PASSWORD,
					                ActiveMQConnection.DEFAULT_BROKER_URL);
			
			connection = (ActiveMQConnection) connectionFactory.createConnection();
	
			connection.start();
			session = (ActiveMQSession) connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
			
			initDestinations();		
			
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.info("Initialization done.");
	}
	
	private void initDestinations()
	{
		try 
		{
			/* part properties: 
			 *  - type: cpu, mainboard,graphicboard, ram
			 *  - cputype: CPUType
			 * 
			 */
			session.createQueue("parts");
			/* computer properties:
			 *  - CompletenessTested: bool
			 *  - CorrectnessTested: bool
			 *  - CompletelyTested: bool
			 *  - Deconstructed: bool
			 * 
			 */
			session.createQueue("computers");
			session.createQueue("trashed");
			session.createQueue("shipped");
			/* oder properties:
			 *  - completelytested: bool
			 * 
			 */
			session.createQueue("orders");
			session.createQueue("finished");
			session.createQueue("balanceContainer");
			
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("initDestinations done.");
	}
	
	private long getNextId()
	{
		Date theDate = new Date();
		logger.info("getNextId done.");
		return theDate.getTime();
	}
	
	@Override
	public long getNextComputerId() 
	{
		return this.getNextId();
	}

	@Override
	public long getNextPartId() throws SharedWorkspaceException 
	{
		return this.getNextId();
	}
	
	@Override
	public void secureShutdown() throws SharedWorkspaceException 
	{
		logger.info("Shutting down...");
		try 
		{
			session.rollback();
			session.close();
			connection.close();
			logger.info("Shutdown complete.");
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void startTransaction() throws SharedWorkspaceException 
	{
		logger.info("startTransaction...");
		try 
		{
			session = (ActiveMQSession) connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("startTransaction done.");
	}

	@Override
	public void commitTransaction() throws SharedWorkspaceException 
	{
		logger.info("commitTransaction...");
		try 
		{
			session.commit();
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("commitTransaction done.");
	}

	@Override
	public void rollbackTransaction() throws SharedWorkspaceException 
	{
		logger.info("rollbackTransaction...");
		try 
		{
			session.rollback();
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("rollbackTransaction done.");
	}
	
	private void sendMessage(ObjectMessage objMessage)
	{
		MessageProducer producer;
		
		try 
		{
			producer = session.createProducer(destination);

			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
		
			logger.info("Sending message.");
			producer.send(objMessage);
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private ObjectMessage receiveMessage(String filter)
	{
		ObjectMessage message = null;
		try 
		{
			MessageConsumer consumer = session.createConsumer(destination, filter);
			message = (ObjectMessage) consumer.receive();
			message.clearBody();
			logger.info("receiveing message...");
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return message;
	}

	@Override
	public void addPart(Part part) throws SharedWorkspaceException 
	{
		// TODO Auto-generated method stub
		ObjectMessage message;
		try 
		{
			logger.info("addPart...");
			message = session.createObjectMessage(part);
			message.setJMSType(part.getClass().toString()); //set standard type
			
			if(part.getClass().equals(CPU.class)) // set custom attribute
			{
				message.setStringProperty("CPUType", ((CPU)part).getCpuType().toString());
			}
			
			destination = (ActiveMQDestination) session.createQueue("parts");
			
			sendMessage(message);
			logger.info("addPart done.");
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(factory != null)
			factory.updatePartList();
	}
	
	@Override
	public List<Part> takeParts(Class<?> partType, boolean blocking, int partCount) throws SharedWorkspaceException 
	{
		ArrayList<Part> result = new ArrayList<Part>();
		String filter;
		Part p;
		logger.info("takeParts...");
		filter = "JMSType='"+partType.getClass()+"'";
		
//		if(partType.getClass().equals(CPU.class))
//		{
//			filter += " AND CPUType='"+((CPU)part).getCpuType().toString()+"'"; 
//		}
		//"JMSType = 'car' AND color = 'blue' AND weight > 2500"
		
		for(int i = 0; i < partCount; i++)
		{
			p = (Part) receiveMessage(filter);
			if(p!=null)
				result.add(p);
		}
		logger.info("takeParts done.");
		if(factory != null)
			factory.updatePartList();
		return result;
	}

	@Override
	public List<CPU> takeCPU(CPUType cpuType, boolean blocking, int partCount) throws SharedWorkspaceException 
	{
		ArrayList<CPU> result = new ArrayList<CPU>();
		String filter;
		CPU p;
		logger.info("takeCPU...");
		filter = "JMSType='"+CPU.class.toString()+"'";
		
		filter += " AND CPUType='"+cpuType.toString()+"'"; 
		
		//"JMSType = 'car' AND color = 'blue' AND weight > 2500"
		
		for(int i = 0; i < partCount; i++)
		{
			p = (CPU) receiveMessage(filter);
			if(p!=null)
				result.add(p);
		}
		logger.info("takeCPU done.");
		if(factory != null)
			factory.updatePartList();
		return result;
	}
	
	@Override
	public void addParts(List<Part> parts) throws SharedWorkspaceException 
	{
		logger.info("addParts...");
		for(Part part:parts)
		{
			addPart(part);
		}
		if(factory != null)
			factory.updatePartList();
		logger.info("addParts done.");
	}
	
	@Override
	public String getWorkspaceID() 
	{
		logger.info("getWorkspaceID done.");
		if(serverPort != 0)
			return String.valueOf(serverPort);
		else
			return null;
	}
	
	@Override
	public void shipComputer(Computer computer) throws SharedWorkspaceException 
	{
		logger.info("shipComputer...");
		try 
		{
			destination = (ActiveMQDestination) session.createQueue("shipped");

			ObjectMessage message = session.createObjectMessage(computer);
			message.setBooleanProperty("Deconstructed", computer.isDeconstructed());
			
			sendMessage(message);
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(factory != null)
			factory.updateShippedList();
		logger.info("shipComputer done.");
	}

	@Override
	public void addComputerToTrash(Computer computer) throws SharedWorkspaceException 
	{
		logger.info("addComputerToTrash...");
		try 
		{
			destination = (ActiveMQDestination) session.createQueue("trashed");

			ObjectMessage message = session.createObjectMessage(computer);
			message.setBooleanProperty("Deconstructed", computer.isDeconstructed());
			
			sendMessage(message);
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(factory != null)
			factory.updateComputerList();
		logger.info("addComputerToTrash done.");
	}

	@Override
	public void addComputer(Computer computer) throws SharedWorkspaceException 
	{
		logger.info("addComputer...");
		try 
		{
			destination = (ActiveMQDestination) session.createQueue("computers");

			ObjectMessage message = session.createObjectMessage(computer);
			if(computer.getCompletenessTested().equals(TestState.NOT_TESTED))
				message.setBooleanProperty("CompletenessTested", false);
			else
				message.setBooleanProperty("CompletenessTested", true);
			
			if(computer.getCorrectnessTested().equals(TestState.NOT_TESTED))
				message.setBooleanProperty("CorrectnessTested", false);
			else
				message.setBooleanProperty("CorrectnessTested", true);
			
			if(computer.getOrder() != null)
				message.setLongProperty("orderid", computer.getOrder().getId());
			
			message.setBooleanProperty("CompletelyTested", computer.isCompletelyTested());
			
			message.setBooleanProperty("Deconstructed", computer.isDeconstructed());
			
			sendMessage(message);
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(factory != null)
			factory.updateComputerList();
		logger.info("addComputer done.");
	}
	
	@Override
	public Computer takeUntestedComputer(TestType untestedFor) throws SharedWorkspaceException 
	{
		logger.info("takeUntestedComputer...");
		
		Computer computer = null;
		String filter = "";
		try 
		{
			destination = (ActiveMQDestination) session.createQueue("computers");
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(untestedFor.equals(TestType.COMPLETENESS)) {
			filter = "NOT CompletenessTested";
		} else if(untestedFor.equals(TestType.CORRECTNESS)) {
			filter = "NOT CorrectnessTested";
		}
		ObjectMessage message = receiveMessage(filter);
		try 
		{
			computer = (Computer) message.getObject();
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("takeUntestedComputer done.");
		if(factory != null)
			factory.updateComputerList();
		return computer;
	}

	@Override
	public Computer takeNormalCompletelyTestedComputer() throws SharedWorkspaceException 
	{
		logger.info("takeNormalCompletelyTestedComputer...");
		
		Computer computer = null;
		String filter = "";
		try 
		{
			destination = (ActiveMQDestination) session.createQueue("computers");
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		filter = "CompletelyTested";

		ObjectMessage message = receiveMessage(filter);
		try 
		{
			computer = (Computer) message.getObject();
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(factory != null)
			factory.updateComputerList();
		
		logger.info("takeNormalCompletelyTestedComputer done.");
		return computer;
	}
	
	@Override
	public List<Part> getAvailableParts() throws SharedWorkspaceException 
	{
		logger.info("getAvailableParts...");
		
		ArrayList<Part> result = new ArrayList<Part>();
		ActiveMQQueueBrowser qb;
		try 
		{
			//session.createQueue("parts");
			qb = (ActiveMQQueueBrowser) session.createBrowser(session.createQueue("parts"),"");
			while(qb.hasMoreElements())
			{
				result.add((Part)qb.nextElement());
			}
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("getAvailableParts done.");

		return result;
	}

	@Override
	public List<Computer> getIncompleteComputers() throws SharedWorkspaceException 
	{
		logger.info("getIncompleteComputers...");
		
		ArrayList<Computer> result = new ArrayList<Computer>();
		ActiveMQQueueBrowser qb;
		try 
		{
			//session.createQueue("parts");
			qb = (ActiveMQQueueBrowser) session.createBrowser(session.createQueue("computers"),"NOT Deconstructed");
			while(qb.hasMoreElements())
			{	
				result.add((Computer) qb.nextElement());
			}
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("getIncompleteComputers done.");
		
		return result;
	}

	@Override
	public List<Computer> getShippedComputers() throws SharedWorkspaceException 
	{
		logger.info("getShippedComputers...");
		
		ArrayList<Computer> result = new ArrayList<Computer>();
		ActiveMQQueueBrowser qb;
		try 
		{
			//session.createQueue("parts");
			qb = (ActiveMQQueueBrowser) session.createBrowser(session.createQueue("shipped"),"");
			while(qb.hasMoreElements())
			{	
				result.add((Computer) qb.nextElement());
			}
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("getShippedComputers done.");
		
		return result;
	}

	@Override
	public List<Computer> getTrashedComputers() throws SharedWorkspaceException 
	{
		logger.info("getTrashedComputers...");
		
		ArrayList<Computer> result = new ArrayList<Computer>();
		ActiveMQQueueBrowser qb;
		try 
		{
			//session.createQueue("parts");
			qb = (ActiveMQQueueBrowser) session.createBrowser(session.createQueue("trashed"),"");
			while(qb.hasMoreElements())
			{	
				result.add((Computer) qb.nextElement());
			}
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("getTrashedComputers done.");
		
		return result;
	}

	@Override
	public List<Computer> getDeconstructedComputers() throws SharedWorkspaceException 
	{
		logger.info("getDeconstructedComputers...");
		ArrayList<Computer> result = new ArrayList<Computer>();
		ActiveMQQueueBrowser qb;
		try 
		{
			//session.createQueue("parts");
			qb = (ActiveMQQueueBrowser) session.createBrowser(session.createQueue("computers"),"Deconstructed");
			while(qb.hasMoreElements())
			{	
				result.add((Computer) qb.nextElement());
			}
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("getDeconstructedComputers done.");
		return result;
	}
	
	@Override
	public void addOrder(Order order) throws SharedWorkspaceException 
	{
		logger.info("addOrder...");
		try 
		{
			destination = (ActiveMQDestination) session.createQueue("orders");

			ObjectMessage message = session.createObjectMessage(order);
			message.setLongProperty("orderid", order.getId());
			
			sendMessage(message);
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(factory != null)
			factory.updateUnfinishedOrderList();
		logger.info("addOrder done.");
	}

	@Override
	public void finishOrder(Order order) throws SharedWorkspaceException 
	{
		logger.info("finishOrder...");
		
		try 
		{
			//Grep from oders
			destination = (ActiveMQDestination) session.createQueue("orders");
			ObjectMessage message = receiveMessage("oerderid = " + String.valueOf(order.getId()));
			
			//move to finished
			destination = (ActiveMQDestination) session.createQueue("finished"); 
			sendMessage(message);
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(factory != null)
			factory.updateUnfinishedOrderList();
		if(factory != null)
			factory.updateFinishedOrderList();
		logger.info("finishOrder done.");
	}
	
	@Override
	public List<Order> getUnfinishedOrders() throws SharedWorkspaceException 
	{
		logger.info("getUnfinishedOrders...");
		
		ArrayList<Order> result = new ArrayList<Order>();
		ActiveMQQueueBrowser qb;
		try 
		{
			//session.createQueue("parts");
			qb = (ActiveMQQueueBrowser) session.createBrowser(session.createQueue("orders"),"");
			while(qb.hasMoreElements())
			{	
				result.add((Order) qb.nextElement());
			}
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("getUnfinishedOrders done.");
		
		return result;
	}

	@Override
	public List<Order> getFinishedOrders() throws SharedWorkspaceException 
	{
		logger.info("getFinishedOrders...");
		
		ArrayList<Order> result = new ArrayList<Order>();
		ActiveMQQueueBrowser qb;
		try 
		{
			//session.createQueue("parts");
			qb = (ActiveMQQueueBrowser) session.createBrowser(session.createQueue("finished"),"");
			while(qb.hasMoreElements())
			{	
				result.add((Order) qb.nextElement());
			}
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("getFinishedOrders done.");
		
		return result;
	}

	@Override
	public boolean testOrderCountMet(Order order) throws SharedWorkspaceException 
	{
		logger.info("testOrderCountMet...");
		
		boolean result = false;
		int resultCount = 0;
		
		ActiveMQQueueBrowser qb;
		try 
		{
			//session.createQueue("parts");
			qb = (ActiveMQQueueBrowser) session.createBrowser(session.createQueue("Computer"),"order = " + String.valueOf(order.getId()));
			while(qb.hasMoreElements())
			{	
				resultCount++;
			}
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(resultCount < order.getComputerCount())
			result = false;
		else
			result = true;
		
		logger.info("testOrderCountMet done.");
		
		return result;
	}
	
	@Override
	public List<Computer> takeAllOrderedComputers(Order order) throws SharedWorkspaceException 
	{
		logger.info("takeAllOrderedComputers...");
		
		ArrayList<Computer> result = new ArrayList<Computer>();
		String filter = "";
		ObjectMessage message;
		try 
		{
			//session.createQueue("parts");
			destination = (ActiveMQDestination) session.createQueue("computers");
			
			filter = "oderid = "+String.valueOf(order.getId());
			
			for(int i = 0; i < order.getComputerCount(); i++)
			{
				message = receiveMessage(filter);

				result.add((Computer) message.getObject());
			}
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("takeAllOrderedComputers done.");
		if(factory != null)
			factory.updateComputerList();
		return result;
	}


	
	
	@Override
	public void startBalancing() throws SharedWorkspaceException 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopBalancing() throws SharedWorkspaceException 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void waitForBalancing() throws SharedWorkspaceException 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isCurrentlyBalancing() throws SharedWorkspaceException 
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void takePartsAsync(Class<?> partType, boolean blocking, int partCount, AsyncAssembler callback) throws SharedWorkspaceException 
	{
		// TODO Auto-generated method stub
		
	}

}
