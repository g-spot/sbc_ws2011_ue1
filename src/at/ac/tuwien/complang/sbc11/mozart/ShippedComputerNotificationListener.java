package at.ac.tuwien.complang.sbc11.mozart;

import java.io.Serializable;
import java.util.List;

import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.Operation;

import at.ac.tuwien.complang.sbc11.ui.Factory;

public class ShippedComputerNotificationListener implements
		NotificationListener {

	private Factory factory;
	
	@SuppressWarnings("unused")
	private ShippedComputerNotificationListener() {}
	
	public ShippedComputerNotificationListener(Factory factory) {
		this.factory = factory;
	}

	@Override
	public void entryOperationFinished(Notification arg0, Operation arg1,
			List<? extends Serializable> arg2) {
		factory.updateShippedList();
	}

}
