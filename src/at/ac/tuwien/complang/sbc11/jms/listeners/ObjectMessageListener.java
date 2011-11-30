package at.ac.tuwien.complang.sbc11.jms.listeners;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

public class ObjectMessageListener implements MessageListener
{
	public ObjectMessageListener()
	{
		
	}
	
	@Override
	public void onMessage(Message message) 
	{
        ObjectMessage objMessage = (ObjectMessage) message;
        try 
        {
			objMessage.clearBody();
			
			// TODO pass back
			
			
		} catch (JMSException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
