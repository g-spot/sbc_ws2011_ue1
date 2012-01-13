package at.ac.tuwien.complang.sbc11.benchmark;

import at.ac.tuwien.complang.sbc11.factory.SharedWorkspaceHelper;

public class BenchmarkStopper extends Thread {
	
	public BenchmarkStopper() {
		
	}
	
	@Override
	public void run() {
		System.out.println("Waiting for stop signal...");
		SharedWorkspaceHelper.waitForStopSignal();
		System.out.println("Stop signal received... exiting...");
		System.exit(0);
	}
}
