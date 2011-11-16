package at.ac.tuwien.complang.sbc11.mozart;

import org.mozartspaces.core.Server;

public class StandaloneServer {

	public static final long SERVER_PORT = 1234;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String arguments[] = { String.valueOf(SERVER_PORT) };
		Server.main(arguments);
	}

}
