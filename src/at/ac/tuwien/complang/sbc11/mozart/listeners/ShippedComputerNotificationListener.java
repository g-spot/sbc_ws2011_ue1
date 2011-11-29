package at.ac.tuwien.complang.sbc11.mozart.listeners;

import java.util.HashMap;
import at.ac.tuwien.complang.sbc11.ui.Factory;

public class ShippedComputerNotificationListener extends MozartContainerListener {
	
	@SuppressWarnings("unused")
	private ShippedComputerNotificationListener() {}
	
	public ShippedComputerNotificationListener(Factory factory, HashMap<String, String> containerMap) {
		this.factory = factory;
		this.containerMap = containerMap;
	}

	@Override
	protected void updateBlackboard() {
		factory.updateShippedList();
	}

}