package at.ac.tuwien.complang.sbc11.factory;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.exolab.jms.administration.AdminConnectionFactory;
import org.exolab.jms.administration.JmsAdminServerIfc;

import at.ac.tuwien.complang.sbc11.factory.exception.SharedWorkspaceException;
import at.ac.tuwien.complang.sbc11.jms.listeners.ObjectMessageListener;
import at.ac.tuwien.complang.sbc11.parts.Computer;
import at.ac.tuwien.complang.sbc11.parts.Part;
import at.ac.tuwien.complang.sbc11.ui.Factory;
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
	
	// JMS specific
	private Context globalContext;
	private ConnectionFactory connectionFactory;
	private Connection jmsConnection;
	private Session jmsSession;
	private JmsAdminServerIfc admin;
	private String adminUrl;
	
	// JMS specific destinations
	private HashMap<String,Destination> destinationMap;
	
	public SharedWorkspaceJMSImpl() 
	{
		super(null);
		// g-spot: danke f�r den hinweis!
		
		logger = Logger.getLogger("at.ac.tuwien.complang.sbc11.factory.SharedWorkspaceJMSImpl");
				
		try 
		{
			//JMS Connection
			initJMSConnection();
			
			//Destinations used
			initDestiantions();
			
			//JMS admin interface needed for counting messages in a queue and so on
			this.admin = AdminConnectionFactory.create(this.adminUrl);
			
		} catch (NamingException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.info("Initialization done.");
	}

	public SharedWorkspaceJMSImpl(Factory factory) 
	{
		super(factory);
		
		logger = Logger.getLogger("at.ac.tuwien.complang.sbc11.factory.SharedWorkspaceJMSImpl");
		
		try 
		{
			//JMS Connection
			initJMSConnection();
			
			//Destinations used
			initDestiantions();
			
			//JMS admin interface needed for counting messages in a queue and so on
			this.admin = AdminConnectionFactory.create(this.adminUrl);
			
		} catch (NamingException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		logger.info("Initialization done.");
	}
	
	private void initDestiantions() throws NamingException
	{
		logger.info("Initializing destinationMap...");
		this.destinationMap.put("part", (Destination) this.globalContext.lookup("part"));
		this.destinationMap.put("mainboard", (Destination) globalContext.lookup("mainboard"));
		
		this.destinationMap.put("incomplete", (Destination) globalContext.lookup("incomplete"));
		this.destinationMap.put("trashed", (Destination) globalContext.lookup("trashed"));
		this.destinationMap.put("shipped", (Destination) globalContext.lookup("shipped"));
	}
	private void initJMSConnection() throws NamingException, JMSException
	{
		logger.info("Initializing context...");
		this.globalContext = new InitialContext();
		
		logger.info("Initializing connectionFactory...");
		this.connectionFactory = (ConnectionFactory) this.globalContext.lookup("ConnectionFactory");
		
		logger.info("Initializing connection...");
		this.jmsConnection = this.connectionFactory.createConnection();
		
		logger.info("Initializing session...");
		this.jmsSession = this.jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	}
	
	private void dropMessageObject(String destinationName, ObjectMessage message) throws SharedWorkspaceException
	{
		logger.info("Start dropping Object...");
		
		if(!this.destinationMap.containsKey(destinationName))
			throw new SharedWorkspaceException("Object could not be dropped to this destination: Error in dropMessageObject: Unknown Destination: " + destinationName + "");
		try 
		{
			Destination targetDestination = this.destinationMap.get(destinationName);
			
			this.jmsConnection.start();
		    MessageProducer sender = this.jmsSession.createProducer(targetDestination);
	    
		    // only serialized objects can be sent
			sender.send(message);
		} catch (JMSException e) 
		{
			throw new SharedWorkspaceException("Object could not be dropped to this destination: Error in dropMessageObject (" + e.getMessage() + ")");
		}
	    
	    logger.info("Dropping Object done...");
	}
	
	private Part fetchPartObject(String destinationName) throws SharedWorkspaceException
	{
		logger.info("Start fetchinging Object...");
		Part somePart = null;
		int messageCount = 0;
		
		if(!this.destinationMap.containsKey(destinationName))
		{	
			throw new SharedWorkspaceException("Object could not be fetched from this destination: Error in fetchMessageObject: Unknown Destination: " + destinationName + "");
		}
		
		try 
		{
			messageCount = this.admin.getQueueMessageCount(destinationName);
		} catch (JMSException e1) 
		{
			throw new SharedWorkspaceException("Problem counting number of elements in queue: Error in fetchMessageObject: Error (" + e1.getMessage() + ")");
		}
		
		if(messageCount > 0)
		{
			try 
		    {
				Destination targetDestination = this.destinationMap.get(destinationName);
		
				MessageConsumer receiver = this.jmsSession.createConsumer(targetDestination);
			    // start the connection to enable message delivery
				this.jmsConnection.start();
				
				ObjectMessage someObject = (ObjectMessage) receiver.receive();
	
				//TODO here may be some check if the class is valid
				if(!someObject.getClass().equals(Computer.class))
				{
					somePart = (Part) someObject;
				}
				
			} catch (JMSException e) 
			{
				throw new SharedWorkspaceException("Object could not be dropped to this destination: Error in fetchMessageObject (" + e.getMessage() + ")");
			}
		}
	    //return part or null
	    return somePart;
	}
	
	private Computer fetchComputerObject(String destinationName) throws SharedWorkspaceException 
	{
		logger.info("Start fetchinging ComputerObject...");
		
		Computer someComputer = null;
		int messageCount = 0;
		
		if(!this.destinationMap.containsKey(destinationName))
		{	
			throw new SharedWorkspaceException("Object could not be fetched from this destination: Error in fetchMessageObject: Unknown Destination: " + destinationName + "");
		}
		
		try 
		{
			messageCount = this.admin.getQueueMessageCount(destinationName);
		} catch (JMSException e1) 
		{
			throw new SharedWorkspaceException("Problem counting number of elements in queue: Error in fetchMessageObject: Error (" + e1.getMessage() + ")");
		}
		
		if(messageCount > 0)
		{
			try 
		    {
				Destination targetDestination = this.destinationMap.get(destinationName);
		
				MessageConsumer receiver = this.jmsSession.createConsumer(targetDestination);
			    // start the connection to enable message delivery
				this.jmsConnection.start();
				
				ObjectMessage someObject = (ObjectMessage) receiver.receive();
	
				//TODO here may be some check if the class is valid
				someComputer = (Computer) someObject;
				
			} catch (JMSException e) 
			{
				throw new SharedWorkspaceException("Object could not be dropped to this destination: Error in fetchMessageObject (" + e.getMessage() + ")");
			}
		}
		logger.info("Finished fetchinging ComputerObject.");
	    //return part or null
	    return someComputer;
	}
	
	@Override
	public void secureShutdown() throws SharedWorkspaceException 
	{
		
		// probably remove all objects in queues and close connection, session...
		try 
		{
			this.jmsSession.close();
			this.jmsConnection.close();
			this.globalContext.close();
			this.destinationMap.clear();
			this.admin.close();
		
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
		List<Part> result = new ArrayList<Part>();
		// create the browser
		Queue queue;
		
		// TODO optimization: only use queues with parts in it -> how can i differ without hard code it?
		// TODO use fetchPartObject function
		
		// Iterate over all destinations
		Iterator<Entry<String, Destination>> entryIterator = this.destinationMap.entrySet().iterator();
		Entry<String,Destination> entrySet;
		
		try 
		{
			entrySet = entryIterator.next();
			
			while(entrySet != null)
			{
				entrySet = entryIterator.next();
				// lookup destination with name
				queue = (Queue) this.globalContext.lookup(entrySet.getKey());
				
				// create a browser (not consuming)
				QueueBrowser browser = this.jmsSession.createBrowser(queue);
		
		        // start the connection
		        this.jmsConnection.start();
		
		        @SuppressWarnings("rawtypes")
				Enumeration messages = browser.getEnumeration();
		        
		        while (messages.hasMoreElements()) 
		        {
		            Message message = (Message) messages.nextElement();
		            if (message instanceof ObjectMessage) 
		            {
		                ObjectMessage somePart = (ObjectMessage) message;
		                
		                //without clearbody the object would be read only!
		                message.clearBody();
		                
		                //Check if we have a part
		                if(!somePart.getClass().equals(Computer.class))
		                {
		                	result.add((Part) somePart);
		                }
		                
		            } else if (message != null) 
		            {
		                // not our problem
		            }
		        }
			}
	        
		} catch (NamingException e) 
		{
			throw new SharedWorkspaceException("NamingException in getAvailableParts: Error (" + e.getMessage() + ")");
		} catch (JMSException e) 
		{
			throw new SharedWorkspaceException("JMSException in getAvailableParts: Error (" + e.getMessage() + ")");
		}
		return result;
	}

	@Override
	public List<Computer> getIncompleteComputers() throws SharedWorkspaceException 
	{
		List<Computer> result = new ArrayList<Computer>();
		
		Computer someComputer = fetchComputerObject("incomplete");
		
		while(someComputer != null)
		{
			if(someComputer.getClass().equals(Computer.class))
			{
				result.add(someComputer);
			}
			someComputer = fetchComputerObject("incomplete");
		}
		
		return result;
	}

	@Override
	public List<Computer> getShippedComputers() throws SharedWorkspaceException 
	{
		List<Computer> result = new ArrayList<Computer>();
		
		Computer someComputer = fetchComputerObject("shipped");
		
		while(someComputer != null)
		{
			if(someComputer.getClass().equals(Computer.class))
			{
				result.add(someComputer);
			}
			someComputer = fetchComputerObject("shipped");
		}
		
		return result;
	}

	@Override
	public List<Computer> getTrashedComputers() throws SharedWorkspaceException 
	{
		List<Computer> result = new ArrayList<Computer>();
		
		Computer someComputer = fetchComputerObject("trashed");
		
		while(someComputer != null)
		{
			if(someComputer.getClass().equals(Computer.class))
			{
				result.add(someComputer);
			}
			someComputer = fetchComputerObject("trashed");
		}
		
		return result;
	}

	@Override
	public void startTransaction() throws SharedWorkspaceException {
		// TODO Auto-generated method stub
		// JMS breaker @Folienblock 3, Seite 12
	}

	@Override
	public void commitTransaction() throws SharedWorkspaceException 
	{
		// TODO Auto-generated method stub
		// JMS breaker @Folienblock 3, Seite 12
	}

	@Override
	public void rollbackTransaction() throws SharedWorkspaceException 
	{
		// TODO Auto-generated method stub
		// JMS breaker @Folienblock 3, Seite 12
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
		dropMessageObject("shipped",(ObjectMessage) computer);
	}

	@Override
	public void addComputerToTrash(Computer computer) throws SharedWorkspaceException 
	{
		dropMessageObject("trashed",(ObjectMessage) computer);
	}

	@Override
	public long getNextComputerId() 
	{
		// TODO Auto-generated method stub
		return 0;
	}
}
