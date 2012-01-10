package at.ac.tuwien.complang.sbc11.mozart.listeners;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.Operation;

import at.ac.tuwien.complang.sbc11.ui.Factory;

public class OrderNotificationListener extends MozartContainerListener {
	
	@SuppressWarnings("unused")
	private OrderNotificationListener() {}
	
	public OrderNotificationListener(Factory factory, HashMap<String, String> containerMap) {
		this.factory = factory;
		this.containerMap = containerMap;
	}

	@Override
	protected void updateBlackboard() {
		factory.updateUnfinishedOrderList();
		factory.updateFinishedOrderList();
	}
	
	@Override
	public void entryOperationFinished(Notification source, Operation operation,
			List<? extends Serializable> entries) {
		
		// update blackboard
		if(operation.equals(Operation.TAKE) || operation.equals(Operation.WRITE)) {
			// calls the right abstract method to update the blackboard
			updateBlackboard();
		}
		
		// update action log
		String containerName = getContainerName(source.getObservedContainer().getStringRepresentation());
		String message = operation.name() + " on container: " + containerName + NEWLINE;
		message += getEntriesDescription(entries);
		factory.appendActionLog(message);
	}
}