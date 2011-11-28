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
		// update part list
		System.out.println("update part list");
		factory.updatePartList();
		System.out.println("finished.");
		
		// update action log
		System.out.println("update action log");
		String message;
		message = "Container " + source.getNotificationContainer().getStringRepresentation() + ": ";
		message += operation.name() + NEWLINE;
		//message = operation.name() + ": ";
		//message += source.getNotificationContainer().getSpace().getAuthority() + ", ";
		//System.out.println(source.getNotificationContainer().)
		//System.out.println("test 1");
		for(Serializable s:entries) {
			System.out.println("test 2");
			Part part = (Part)((Entry)s).getValue();
			message += "   " + part.toString() + NEWLINE;
		}
		System.out.println("test3");
		factory.updateActionLog(message);
		System.out.println("finished.");
	}

}
