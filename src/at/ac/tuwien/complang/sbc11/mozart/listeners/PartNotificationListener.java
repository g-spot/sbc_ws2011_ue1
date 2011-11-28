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
	private HashMap<String, String> containerMap;
	private final char NEWLINE = '\n';
	
	@SuppressWarnings("unused")
	private PartNotificationListener() {}
	
	public PartNotificationListener(Factory factory, HashMap<String, String> containerMap) {
		this.factory = factory;
		this.containerMap = containerMap;
	}

	@Override
	public void entryOperationFinished(Notification source, Operation operation,
			List<? extends Serializable> entries) {
		System.out.println("Performing operation: " + operation.name());
		// update part list
		factory.updatePartList();
		
		for(String key:containerMap.keySet()) {
			System.out.println("Key: " + key + ", Value: " + containerMap.get(key));
		}
		
		// update action log
		String message;
		System.out.println("current container: " + source.getNotificationContainer().getStringRepresentation());
		message = containerMap.get(source.getNotificationContainer().getStringRepresentation()) + ": ";
		message += operation.name() + NEWLINE;
		for(Serializable s:entries) {
			Part part = (Part)((Entry)s).getValue();
			message += "   " + part.toString() + NEWLINE;
		}
		factory.appendActionLog(message);
	}

}
