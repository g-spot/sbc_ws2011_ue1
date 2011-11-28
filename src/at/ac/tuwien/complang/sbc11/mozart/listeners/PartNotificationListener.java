package at.ac.tuwien.complang.sbc11.mozart.listeners;

import java.io.Serializable;
import java.util.List;

import org.mozartspaces.core.Entry;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.Operation;

import at.ac.tuwien.complang.sbc11.parts.Part;
import at.ac.tuwien.complang.sbc11.ui.Factory;

public class PartNotificationListener implements NotificationListener {
	
	private Factory factory;
	private final char NEWLINE = '\n';
	
	@SuppressWarnings("unused")
	private PartNotificationListener() {}
	
	public PartNotificationListener(Factory factory) {
		this.factory = factory;
	}

	@Override
	public void entryOperationFinished(Notification source, Operation operation,
			List<? extends Serializable> entries) {
		System.out.println("Performing operation: " + operation.name());
		// update part list
		factory.updatePartList();
		
		// update action log
		String message;
		message = source.getNotificationContainer().getStringRepresentation() + ": ";
		message += operation.name() + NEWLINE;
		for(Serializable s:entries) {
			Part part = (Part)((Entry)s).getValue();
			message += "   " + part.toString() + NEWLINE;
		}
		factory.appendActionLog(message);
	}

}
