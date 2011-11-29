package at.ac.tuwien.complang.sbc11.mozart.listeners;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.mozartspaces.core.Entry;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.Operation;

import at.ac.tuwien.complang.sbc11.ui.Factory;

public abstract class MozartContainerListener implements NotificationListener {
	
	protected Factory factory;
	protected HashMap<String, String> containerMap;
	private final char NEWLINE = '\n';
	
	protected abstract void updateBlackboard();

	private String getContainerName(String containerId) {
		String containerName = "UNKNOWN CONTAINER";
		for(String key:containerMap.keySet()) {
			if(key.equals(containerId)) {
				containerName = containerMap.get(key);
				break;
			}
		}
		return containerName;
	}
	
	private String getEntriesDescription(List<? extends Serializable> entries) {
		String description = "   ";
		if(entries == null || entries.size() == 0)
			return description + "NO ENTRIES FOUND";
		for(Serializable s:entries) {
			if(s.getClass().equals(Entry.class))
				description += ((Entry)s).getValue().toString() + NEWLINE;
			else
				description += s.toString() + NEWLINE;
		}
		return description;
	}
	
	@Override
	public void entryOperationFinished(Notification source, Operation operation,
			List<? extends Serializable> entries) {
		
		if(operation.equals(Operation.TAKE) || operation.equals(Operation.WRITE)) {
			// calls the right abstract method to update the blackboard
			updateBlackboard();
		}
		
		String containerName = getContainerName(source.getObservedContainer().getStringRepresentation());
		
		// update action log
		String message = operation.name() + " on container: " + containerName + NEWLINE;
		message += getEntriesDescription(entries);
		factory.appendActionLog(message);
	}

}
