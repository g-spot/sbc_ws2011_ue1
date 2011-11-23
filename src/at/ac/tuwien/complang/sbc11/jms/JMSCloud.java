package at.ac.tuwien.complang.sbc11.jms;

import java.util.Hashtable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

abstract class JMSCloud 
{
	protected Context context;
	protected Hashtable properties;
	protected ConnectionFactory factory;
	protected Session session;
	protected Connection connection;
	
	public JMSCloud()
	{
	    this.properties = new Hashtable();
	    properties.put(Context.INITIAL_CONTEXT_FACTORY,
	                   "org.exolab.jms.jndi.InitialContextFactory");
	    properties.put(Context.PROVIDER_URL, "tcp://localhost:3035/");

	    try
	    {
			this.context = new InitialContext(properties);
			this.factory = (ConnectionFactory) context.lookup("ConnectionFactory");
			this.connection = factory.createConnection();
			this.session = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			
		}catch (NamingException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	abstract Object read();
	abstract void write();
	abstract Object delete(); // bei einer queue?
	
}
