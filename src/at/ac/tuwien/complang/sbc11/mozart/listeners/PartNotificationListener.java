package at.ac.tuwien.complang.sbc11.mozart.listeners;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.mozartspaces.core.Entry;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.Operation;

import at.ac.tuwien.complang.sbc11.parts.Part;
import at.ac.tuwien.complang.sbc11.ui.Factory;

public class PartNotificationListener implements NotificationListener {
	
	private Factory factory;
	private HashMap<Notification, String> containerMap;
	private final char NEWLINE = '\n';
	
	@SuppressWarnings("unused")
	private PartNotificationListener() {}
	
	public PartNotificationListener(Factory factory) {
		this.factory = factory;
	}
	
	public void setContainerMap(HashMap<Notification, String> containerMap) {
		this.containerMap = containerMap;
	}

	@Override
	public void entryOperationFinished(Notification source, Operation operation,
			List<? extends Serializable> entries) {
		
		if(operation.equals(Operation.TAKE) || operation.equals(Operation.WRITE)) {
			// update part list
			factory.updatePartList();
		}
		
		/*for(String key:containerMap.keySet()) {
			System.out.println("Key: " + key + ", Value: " + containerMap.get(key));
		}*/
		
		// update action log
		String message;
		try
		{
			for(Notification notification:containerMap.keySet()) {
				System.out.println("====>>> " + containerMap.get(notification));
			}
			System.out.println("====> actual: " + containerMap.get(source));
		} catch(Exception e) {
			System.out.println("========> error");
			e.printStackTrace();
		}
		//System.out.println("current container: " + source.getNotificationContainer().getStringRepresentation());
		//message = containerMap.get(source.getNotificationContainer().getStringRepresentation()) + ": ";
		message = source.getNotificationContainer().getStringRepresentation() + ": ";
		message += operation.name() + NEWLINE;
		for(Serializable s:entries) {
			Part part = null;
			try {
				if(s.getClass().equals(Entry.class))
					part = (Part)((Entry)s).getValue();
				else
					part = (Part)s;
			} catch(ClassCastException e) {
				message += "   Unknown object" + NEWLINE;
			}
			if(part != null)
				message += "   " + part.toString() + NEWLINE;
		}
		factory.appendActionLog(message);
	}

}
