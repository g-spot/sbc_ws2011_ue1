package at.ac.tuwien.complang.sbc11.workers.shutdown;

public class ShutdownInterceptor extends Thread {
	private SecureShutdownApplication application;
	
	@SuppressWarnings("unused")
	private ShutdownInterceptor () {}
	
	public ShutdownInterceptor(SecureShutdownApplication application) {
		this.application = application;
	}
	
	@Override
	public void run() {
		application.shutdown();
	}
}
