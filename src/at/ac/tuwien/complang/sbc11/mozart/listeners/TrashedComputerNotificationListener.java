package at.ac.tuwien.complang.sbc11.mozart.listeners;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.mozartspaces.core.Entry;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.Operation;

import at.ac.tuwien.complang.sbc11.parts.Computer;
import at.ac.tuwien.complang.sbc11.ui.Factory;

public class TrashedComputerNotificationListener implements
		NotificationListener {

	private Factory factory;
	private HashMap<String, String> containerMap;
	private final char NEWLINE = '\n';
	
	@SuppressWarnings("unused")
	private TrashedComputerNotificationListener() {}
	
	public TrashedComputerNotificationListener(Factory factory, HashMap<String, String> containerMap) {
		this.factory = factory;
		this.containerMap = containerMap;
	}

	@Override
	public void entryOperationFinished(Notification source, Operation operation,
			List<? extends Serializable> entries) {
		
		if(operation.equals(Operation.TAKE) || operation.equals(Operation.WRITE)) {
			// update trash bin list
			factory.updateTrashBinList();
		}
				
		// update action log
		String message;
		//message = containerMap.get(source.getNotificationContainer().getId()) + ": ";
		message = source.getNotificationContainer().getStringRepresentation() + ": ";
		message += operation.name() + NEWLINE;
		for(Serializable s:entries) {
			Computer computer = null;
			try {
				if(s.getClass().equals(Entry.class))
					computer = (Computer)((Entry)s).getValue();
				else
					computer = (Computer)s;
			} catch(ClassCastException e) {
				message += "   Unknown object" + NEWLINE;
			}
			if(computer != null)
				message += "   " + computer.toString() + NEWLINE;
		}
		factory.appendActionLog(message);
	}

}
