package at.ac.tuwien.complang.sbc11.factory;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import at.ac.tuwien.complang.sbc11.factory.exception.SharedWorkspaceException;
import at.ac.tuwien.complang.sbc11.jms.listeners.ObjectMessageListener;
import at.ac.tuwien.complang.sbc11.parts.Computer;
import at.ac.tuwien.complang.sbc11.parts.Part;
import at.ac.tuwien.complang.sbc11.ui.Factory;
import at.ac.tuwien.complang.sbc11.workers.AsyncAssembler;
import at.ac.tuwien.complang.sbc11.workers.Tester.TestType;
import at.ac.tuwien.complang.sbc11.workers.Worker;

/* Implements the shared workspace with an alternative technology.
 * The technology used is JMS. OpenJMS is the implementation that will be used here.
 * There are other implementations that are not completely conform to a standard.
 * OpenJMS provides a clean implementation of handling Objects.
 */


public class SharedWorkspaceJMSImpl extends SharedWorkspace 
{
	// General
	private Logger logger;
	
	// JMS specific
	private Context globalContext;
	private ConnectionFactory connectionFactory;
	private Connection jmsConnection;
	private Session jmsSession;
	
	// JMS specific destinations
	private HashMap<String,Destination> destinationMap;
	
	public SharedWorkspaceJMSImpl() 
	{
		super(null);
		// TODO g-spot: danke für den hinweis!
	}

	public SharedWorkspaceJMSImpl(Factory factory) 
	{
		super(factory);
		
		logger = Logger.getLogger("at.ac.tuwien.complang.sbc11.factory.SharedWorkspaceJMSImpl");
		
		try 
		{
			logger.info("Initializing context...");
			this.globalContext = new InitialContext();
			
			logger.info("Initializing connectionFactory...");
			this.connectionFactory = (ConnectionFactory) this.globalContext.lookup("ConnectionFactory");
			
			logger.info("Initializing connection...");
			this.jmsConnection = this.connectionFactory.createConnection();
			
			logger.info("Initializing session...");
			this.jmsSession = this.jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			
			logger.info("Initializing destinationMap...");
			this.destinationMap.put("part", (Destination) globalContext.lookup("part"));
			this.destinationMap.put("mainboard", (Destination) globalContext.lookup("mainboard"));
			this.destinationMap.put("incomplete", (Destination) globalContext.lookup("incomplete"));
			this.destinationMap.put("trashed", (Destination) globalContext.lookup("trashed"));
			this.destinationMap.put("shipped", (Destination) globalContext.lookup("shipped"));
			
			
			
		} catch (NamingException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.info("Initialization done.");
	}

	private void dropMessageObject(String destinationName, Part part) throws SharedWorkspaceException
	{
		logger.info("Start dropping Object...");
		
		if(!this.destinationMap.containsKey(destinationName))
			throw new SharedWorkspaceException("Object could not be dropped to this destination: Error in dropMessageObject: Unknown Destination: " + destinationName + "");
		try 
		{
			Destination targetDestination = this.destinationMap.get(destinationName);
			
			this.jmsConnection.start();
		    MessageProducer sender = this.jmsSession.createProducer(targetDestination);
		    ObjectMessage message = (ObjectMessage) part;
	    
			sender.send(message);
		} catch (JMSException e) 
		{
			throw new SharedWorkspaceException("Object could not be dropped to this destination: Error in dropMessageObject (" + e.getMessage() + ")");
		}
	    
	    logger.info("Dropping Object done...");
	}
	
	private Part fetchMessageObject(String destinationName) throws SharedWorkspaceException
	{
		logger.info("Start fetchinging Object...");
		
		if(!this.destinationMap.containsKey(destinationName))
			throw new SharedWorkspaceException("Object could not be fetched from this destination: Error in fetchMessageObject: Unknown Destination: " + destinationName + "");
	    try 
	    {
			Destination targetDestination = this.destinationMap.get(destinationName);
	
			MessageConsumer receiver = this.jmsSession.createConsumer(targetDestination);
		    receiver.setMessageListener(new ObjectMessageListener());
	
		    // start the connection to enable message delivery
			this.jmsConnection.start();
			
		} catch (JMSException e) 
		{
			throw new SharedWorkspaceException("Object could not be dropped to this destination: Error in fetchMessageObject (" + e.getMessage() + ")");
		}
	    
	    //TODO return part
	    return new Part();
	}
	
	@Override
	public void secureShutdown() throws SharedWorkspaceException 
	{
		// TODO Auto-generated method stub
		
		// probably remove all objects in queues and close connection, session...
		try 
		{
			this.jmsSession.close();
			this.jmsConnection.close();
			this.globalContext.close();
			this.destinationMap.clear();
		
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NamingException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public List<Part> getAvailableParts() throws SharedWorkspaceException 
	{
		
		// create the browser
		Queue queue;
		try 
		{
			queue = (Queue) this.globalContext.lookup("part");

			QueueBrowser browser = this.jmsSession.createBrowser(queue);
	
	        // start the connection
	        this.jmsConnection.start();
	
	        Enumeration messages = browser.getEnumeration();
	        
	        while (messages.hasMoreElements()) 
	        {
	            Message message = (Message) messages.nextElement();
	            if (message instanceof ObjectMessage) 
	            {
	                ObjectMessage text = (ObjectMessage) message;
	                // DO SOMETHING with the element
	            } else if (message != null) 
	            {
	                // not our problem
	            }
	        }
	        
		} catch (NamingException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<Computer> getIncompleteComputers() throws SharedWorkspaceException 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Computer> getShippedComputers() throws SharedWorkspaceException 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Computer> getTrashedComputers() throws SharedWorkspaceException 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void startTransaction() throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void commitTransaction() throws SharedWorkspaceException 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void rollbackTransaction() throws SharedWorkspaceException 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public long getNextPartId() throws SharedWorkspaceException 
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void addPart(Part part) throws SharedWorkspaceException 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Part> takeParts(Class<?> partType, boolean blocking, int partCount) throws SharedWorkspaceException 
			{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addComputer(Computer computer) throws SharedWorkspaceException 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public Computer takeUntestedComputer(TestType untestedFor) throws SharedWorkspaceException 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Computer takeCompletelyTestedComputer() throws SharedWorkspaceException 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void shipComputer(Computer computer) throws SharedWorkspaceException 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addComputerToTrash(Computer computer) throws SharedWorkspaceException 
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public long getNextComputerId() 
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void takePartsAsync(Class<?> partType, boolean blocking,
			int partCount, AsyncAssembler callback) throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		
	}
}
