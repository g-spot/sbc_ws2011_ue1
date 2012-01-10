package at.ac.tuwien.complang.sbc11.mozart.listeners;

import java.util.HashMap;
import at.ac.tuwien.complang.sbc11.ui.Factory;

public class TrashedComputerNotificationListener extends MozartContainerListener {
	
	@SuppressWarnings("unused")
	private TrashedComputerNotificationListener() {}
	
	public TrashedComputerNotificationListener(Factory factory, HashMap<String, String> containerMap) {
		this.factory = factory;
		this.containerMap = containerMap;
	}

	@Override
	protected void updateBlackboard() {
		factory.updateFinishedOrderList();
	}

}