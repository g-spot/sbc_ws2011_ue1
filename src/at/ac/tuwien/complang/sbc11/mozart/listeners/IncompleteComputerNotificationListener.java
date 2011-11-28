package at.ac.tuwien.complang.sbc11.mozart.listeners;

import java.io.Serializable;
import java.util.List;

import org.mozartspaces.core.Entry;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.Operation;

import at.ac.tuwien.complang.sbc11.parts.Computer;
import at.ac.tuwien.complang.sbc11.ui.Factory;

public class IncompleteComputerNotificationListener implements
		NotificationListener {

	private Factory factory;
	private final char NEWLINE = '\n';
	
	@SuppressWarnings("unused")
	private IncompleteComputerNotificationListener() {}
	
	public IncompleteComputerNotificationListener(Factory factory) {
		this.factory = factory;
	}

	@Override
	public void entryOperationFinished(Notification source, Operation operation,
			List<? extends Serializable> entries) {
		// update computer list
		factory.updateComputerList();
		
		// update action log
		String message;
		message = source.getNotificationContainer().getStringRepresentation() + ": ";
		message += operation.name() + NEWLINE;
		for(Serializable s:entries) {
			Computer part = (Computer)((Entry)s).getValue();
			message += "   " + part.toString() + NEWLINE;
		}
		factory.appendActionLog(message);
	}

}
