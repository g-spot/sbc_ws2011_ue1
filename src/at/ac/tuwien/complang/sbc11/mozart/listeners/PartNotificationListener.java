package at.ac.tuwien.complang.sbc11.mozart.listeners;

import java.io.Serializable;
import java.util.List;

import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.Operation;

import at.ac.tuwien.complang.sbc11.ui.Factory;

public class PartNotificationListener implements NotificationListener {
	
	private Factory factory;
	
	@SuppressWarnings("unused")
	private PartNotificationListener() {}
	
	public PartNotificationListener(Factory factory) {
		this.factory = factory;
	}

	@Override
	public void entryOperationFinished(Notification arg0, Operation arg1,
			List<? extends Serializable> arg2) {
		factory.updatePartList();
	}

}
