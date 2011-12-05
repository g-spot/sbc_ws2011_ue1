package at.ac.tuwien.complang.sbc11.factory;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Vector;
import java.util.logging.Logger;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageFormatException;
import javax.jms.MessageNotWriteableException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.Topic;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.exolab.jms.administration.AdminConnectionFactory;
import org.exolab.jms.administration.JmsAdminServerIfc;
import org.exolab.jms.message.ObjectMessageImpl;

import at.ac.tuwien.complang.sbc11.factory.exception.SharedWorkspaceException;
import at.ac.tuwien.complang.sbc11.parts.CPU;
import at.ac.tuwien.complang.sbc11.parts.Computer;
import at.ac.tuwien.complang.sbc11.parts.GraphicBoard;
import at.ac.tuwien.complang.sbc11.parts.Mainboard;
import at.ac.tuwien.complang.sbc11.parts.Part;
import at.ac.tuwien.complang.sbc11.parts.RAM;
import at.ac.tuwien.complang.sbc11.ui.Factory;
import at.ac.tuwien.complang.sbc11.workers.Tester.TestState;
import at.ac.tuwien.complang.sbc11.workers.AsyncAssembler;
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
	private Factory factory;
	
	// JMS specific destinations
	private HashMap<String,Destination> destinationMap;
	
	public SharedWorkspaceJMSImpl() throws SharedWorkspaceException 
	{
		super(null);
		
		this.factory = null;
		logger = Logger.getLogger("at.ac.tuwien.complang.sbc11.factory.SharedWorkspaceJMSImpl");
		this.adminUrl = "tcp://localhost:3035/";
		
		this.destinationMap = new HashMap<String,Destination>();
		
		try 
		{
			//JMS admin interface needed for counting messages in a queue and so on
			this.admin = AdminConnectionFactory.create(this.adminUrl);
			//createDestinations();
			
			//JMS Connection
			initJMSConnection();
			
			//Destinations used
			initDestiantions();

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

	public SharedWorkspaceJMSImpl(Factory factory) throws SharedWorkspaceException 
	{
		super(factory);
		
		logger = Logger.getLogger("at.ac.tuwien.complang.sbc11.factory.SharedWorkspaceJMSImpl");
		this.factory = factory;
		this.destinationMap = new HashMap<String,Destination>();
		this.adminUrl = "tcp://localhost:3035/";
		
		try 
		{
			//JMS admin interface needed for counting messages in a queue and so on
			this.admin = AdminConnectionFactory.create(this.adminUrl);
			createDestinations();
			
			//JMS Connection
			initJMSConnection();
			
			//Destinations used
			initDestiantions();
			
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
			
		//this.destinationMap.put("part", (Destination) this.globalContext.lookup("part"));
		this.destinationMap.put("ram", (Destination) this.globalContext.lookup("ram"));
		this.destinationMap.put("graphicboard", (Destination) this.globalContext.lookup("graphicboard"));
		this.destinationMap.put("cpu", (Destination) this.globalContext.lookup("cpu"));
		this.destinationMap.put("mainboard", (Destination) globalContext.lookup("mainboard"));
		
		this.destinationMap.put("incomplete", (Destination) globalContext.lookup("incomplete"));
		this.destinationMap.put("incompletetested", (Destination) globalContext.lookup("incompletetested"));
		this.destinationMap.put("incorrecttested", (Destination) globalContext.lookup("incorrecttested"));
		
		this.destinationMap.put("complete", (Destination) globalContext.lookup("complete"));
		this.destinationMap.put("trashed", (Destination) globalContext.lookup("trashed"));
		this.destinationMap.put("shipped", (Destination) globalContext.lookup("shipped"));
	}
	
	private void createDestinations()
	{
		try 
		{
			//Create Destinations - second argument is if it's a queue
			if (!admin.destinationExists("ram"))
				this.admin.addDestination("ram", Boolean.TRUE);
			if (!admin.destinationExists("graphicboard"))
				this.admin.addDestination("graphicboard", Boolean.TRUE);
			if (!admin.destinationExists("cpu"))
				this.admin.addDestination("cpu", Boolean.TRUE);
			if (!admin.destinationExists("mainboard"))
				this.admin.addDestination("mainboard", Boolean.TRUE);
			
			if (!admin.destinationExists("incomplete"))
				this.admin.addDestination("incomplete", Boolean.TRUE);
			if (!admin.destinationExists("incompletetested"))
				this.admin.addDestination("incompletetested", Boolean.TRUE);
			if (!admin.destinationExists("incorrecttested"))
				this.admin.addDestination("incorrecttested", Boolean.TRUE);
			
			if (!admin.destinationExists("complete"))
				this.admin.addDestination("complete", Boolean.TRUE);
			if (!admin.destinationExists("trashed"))
				this.admin.addDestination("trashed", Boolean.TRUE);
			if (!admin.destinationExists("shipped"))
				this.admin.addDestination("shipped", Boolean.TRUE);
			
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Destination listing
		    @SuppressWarnings("rawtypes")
			Vector destinations;
			try 
			{
				destinations = admin.getAllDestinations();
			    @SuppressWarnings("rawtypes")
				Iterator iterator = destinations.iterator();
			    while (iterator.hasNext()) 
			    {
			      Destination destination = (Destination) iterator.next();
			      if (destination instanceof Queue) 
			      {
			         Queue queue = (Queue) destination;
			         logger.info("queue:" + queue.getQueueName());
			      } 
			      else 
			      {
			         Topic topic = (Topic) destination;
			         logger.info("topic:" + topic.getTopicName());
			      }
			    }
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	@SuppressWarnings("unchecked")
	private void initJMSConnection() throws NamingException, JMSException, SharedWorkspaceException
	{
	    @SuppressWarnings("rawtypes")
		Hashtable properties = new Hashtable();
	    properties.put(Context.INITIAL_CONTEXT_FACTORY, 
	                   "org.exolab.jms.jndi.InitialContextFactory");
	    properties.put(Context.PROVIDER_URL, "tcp://localhost:3035/");
	    try
	    {
			logger.info("Initializing context...");
			this.globalContext = new InitialContext(properties);
			
			logger.info("Initializing connectionFactory...");
			this.connectionFactory = (ConnectionFactory) this.globalContext.lookup("ConnectionFactory");
			
			logger.info("Initializing connection...");
			this.jmsConnection = this.connectionFactory.createConnection();
			
			logger.info("Initializing session...");
			this.jmsSession = this.jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		
	    }catch(Exception e)
	    {
			throw new SharedWorkspaceException("JMS Server cannot be initialized: Server not started? (" + e.getMessage() + ")");
	    }
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
				
				ObjectMessage someObject = (ObjectMessageImpl) receiver.receive();
				someObject.clearBody();	
				
				//TODO here may be some check if the class is valid
				if(!someObject.getClass().equals(Computer.class))
				{
					somePart = (Part) someObject.getObject();
				}
				
			} catch (JMSException e) 
			{
				throw new SharedWorkspaceException("Object could not be dropped to this destination: Error in fetchMessageObject (" + e.getMessage() + ")");
			}
		}
		logger.info("Fetchinging Object done.");
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
				
				ObjectMessage someObject = (ObjectMessageImpl) receiver.receive();
				someObject.clearBody();
				
				//TODO here may be some check if the class is valid
				someComputer = (Computer) someObject.getObject();
				
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
		logger.info("Shutting down connections...");
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
		logger.info("Shutting down connections done.");
	}

	@Override
	public List<Part> getAvailableParts() throws SharedWorkspaceException 
	{
		logger.info("Listing available parts.");
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
		                ObjectMessage somePart = (ObjectMessageImpl) message;
		                
		                //without clearbody the object would be read only!
		                somePart.clearBody();
		                
		                //Check if we have a part
		                if(!somePart.getClass().equals(Computer.class))
		                {
		                	result.add((Part) somePart.getObject());
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
		logger.info("Finished listing parts.");
		return result;
	}

	@Override
	public List<Computer> getIncompleteComputers() throws SharedWorkspaceException 
	{
		logger.info("Fetching incomplete computers...");
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
		logger.info("Fetching incomplete computers finished.");
		if(this.factory != null)
		{
			this.factory.updateComputerList();
		}
		return result;
	}

	@Override
	public List<Computer> getShippedComputers() throws SharedWorkspaceException 
	{
		logger.info("Fetching shipped computers...");
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
		if(this.factory != null)
		{
			this.factory.updateComputerList();
			this.factory.updateShippedList();
		}
		logger.info("Fetching shipped computers done.");
		return result;
	}

	@Override
	public List<Computer> getTrashedComputers() throws SharedWorkspaceException 
	{
		logger.info("Fetching trashed computers...");
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
		if(this.factory != null)
		{
			this.factory.updateComputerList();
			this.factory.updateTrashBinList();
		}
		logger.info("Fetching trashed computers done.");
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
		Date theDate = new Date();
		return theDate.getTime();
	}

	@Override
	public void addPart(Part part) throws SharedWorkspaceException 
	{
		logger.info("Adding part...");
		
		ObjectMessageImpl objMsg = null;
		
		try 
		{
			objMsg = new ObjectMessageImpl();
			objMsg.setObject(part);
			
		} catch (MessageFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessageNotWriteableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(part.getClass().equals(Mainboard.class))
		{
			dropMessageObject("mainboard", objMsg);
		}
		else if(part.getClass().equals(RAM.class))
		{
			dropMessageObject("ram", objMsg);
		}
		else if(part.getClass().equals(GraphicBoard.class))
		{
			dropMessageObject("graphicboard", objMsg);
		}
		else if(part.getClass().equals(CPU.class))
		{
			dropMessageObject("cpu", objMsg);
		}
		
		if(this.factory != null)
		{
			this.factory.updatePartList();
		}
		logger.info("Adding part done.");
	}

	@Override
	public List<Part> takeParts(Class<?> partType, boolean blocking, int partCount) throws SharedWorkspaceException 
	{
		logger.info("Taking parts...");
		
		List<Part> result = new ArrayList<Part>();
		Part temporeryResult = null;
		String destinationName = "";

		if(partType.getClass().equals(Mainboard.class))
		{
			destinationName = "mainboard";
		}
		else if(partType.getClass().equals(RAM.class))
		{
			destinationName = "ram";
		}
		else if(partType.getClass().equals(GraphicBoard.class))
		{
			destinationName = "graphicboard";
		}
		else if(partType.getClass().equals(CPU.class))
		{
			destinationName = "cpu";
		}
		
		for(int i = 0; i < partCount; i++)
		{
			temporeryResult = fetchPartObject(destinationName);
			if(temporeryResult != null)
				result.add(temporeryResult);
		}
		if(this.factory != null)
		{
			this.factory.updatePartList();
		}
		logger.info("Taking parts done.");
		return result;
	}

	@Override
	public void addComputer(Computer computer) throws SharedWorkspaceException 
	{
		logger.info("Adding computer...");
		// switch what teststate it is
		if(computer.isCompletelyTested())
		{
			//drop in complete
			dropMessageObject("complete",(ObjectMessage) computer);
		}
		else if(computer.getCompletenessTested() == TestState.NOT_TESTED)
		{
			// drop to incompletetested
			dropMessageObject("incompletetested",(ObjectMessage) computer);
		}
		else if(computer.getCorrectnessTested() == TestState.NOT_TESTED)
		{
			// drop to incorrecttested
			dropMessageObject("incorrecttested",(ObjectMessage) computer);
		}
		if(this.factory != null)
			this.factory.updateComputerList();
		logger.info("Adding computer done.");
	}

	@Override
	public Computer takeUntestedComputer(TestType untestedFor) throws SharedWorkspaceException 
	{
		logger.info("Taking computer...");
		Computer someComputer = null;
		
		if(untestedFor == TestType.COMPLETENESS)
		{
			someComputer = fetchComputerObject("incompletetested");
		}
		else if(untestedFor == TestType.CORRECTNESS)
		{
			someComputer = fetchComputerObject("incorrecttested");
		}
		if(this.factory != null)
		{
			this.factory.updateComputerList();
		}
		logger.info("Taking computer done.");
		return someComputer;
	}

	@Override
	public Computer takeCompletelyTestedComputer() throws SharedWorkspaceException 
	{
		logger.info("Taking completely tested computer...");
		Computer someComputer = fetchComputerObject("complete");
		
		if(this.factory != null)
		{
			this.factory.updateComputerList();
		}
		
		if(someComputer.getClass().equals(Computer.class))
		{
			logger.info("Taking completely tested computer done.");
			return someComputer;
		}
		else
		{
			logger.info("Taking completely tested computer done. - no computer");
			return null;
		}
		
	}

	@Override
	public void shipComputer(Computer computer) throws SharedWorkspaceException 
	{
		logger.info("Shipping computer...");
		
		dropMessageObject("shipped",(ObjectMessage) computer);
		if(this.factory != null)
		{
			this.factory.updateComputerList();
			this.factory.updateShippedList();
		}
		logger.info("Shipping computer done.");
	}

	@Override
	public void addComputerToTrash(Computer computer) throws SharedWorkspaceException 
	{
		logger.info("Trashing computer...");
		dropMessageObject("trashed",(ObjectMessage) computer);
		if(this.factory != null)
		{
			this.factory.updateComputerList();
			this.factory.updateTrashBinList();
		}
		logger.info("Trashing computer done.");
	}

	@Override
	public long getNextComputerId() 
	{
		Date theDate = new Date();
		return theDate.getTime();
	}

	@Override
	public void takePartsAsync(Class<?> partType, boolean blocking, int partCount, AsyncAssembler callback) throws SharedWorkspaceException 
	{
		// TODO Auto-generated method stub
		
	}
}
