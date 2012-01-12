package at.ac.tuwien.complang.sbc11.factory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import at.ac.tuwien.complang.sbc11.factory.exception.SharedWorkspaceException;
import at.ac.tuwien.complang.sbc11.ui.Factory;

public class SharedWorkspaceHelper {
	
	private static final String PROPERTIES_FILENAME = "factory.properties";
	private static final String USE_IMPLEMENTATION_PROPERTY = "factory.use-implementation";
	private static final String USE_MOZART = "mozart";
	private static final String USE_JMS = "jms";
	private static final String MOZART_USE_STANDALONE_PROPERTY = "factory.mozart.use-standalone";
	private static final String MOZART_USE_STANDALONE_YES = "yes";
	
	/**
	 * gets the sharedWorkspace Implementation depending on the property "factory.use-implementation"
	 * in the configuration file factory.properties
	 * factory.use-implementation can take the values "mozart" or "jms"
	 * @return the shared workspace implementation
	 */
	public static SharedWorkspace getWorkspaceImplementation(int serverPort) throws SharedWorkspaceException {
		SharedWorkspace implementation = null;
		try {
			Properties properties = new Properties();
			InputStream inputStream = new FileInputStream(PROPERTIES_FILENAME);
			properties.load(inputStream);
			if(properties.getProperty(USE_IMPLEMENTATION_PROPERTY).equals(USE_MOZART))
				implementation = new SharedWorkspaceMozartImpl(serverPort);
			else if(properties.getProperty(USE_IMPLEMENTATION_PROPERTY).equals(USE_JMS))
				implementation = new SharedWorkspaceJMSImpl();
		} catch (IOException e) {
			throw new SharedWorkspaceException("Could not read " + PROPERTIES_FILENAME + " (" + e.getMessage() + ")");
		}
		return implementation;
	}
	
	/**
	 * gets the sharedWorkspace Implementation depending on the property "factory.use-implementation"
	 * in the configuration file factory.properties
	 * factory.use-implementation can take the values "mozart" or "jms"
	 * @return the shared workspace implementation
	 */
	public static SharedWorkspace getWorkspaceImplementation(Factory factory, int serverPort) throws SharedWorkspaceException {
		SharedWorkspace implementation = null;
		try {
			Properties properties = new Properties();
			InputStream inputStream = new FileInputStream(PROPERTIES_FILENAME);
			properties.load(inputStream);
			if(properties.getProperty(USE_IMPLEMENTATION_PROPERTY).equals(USE_MOZART))
				implementation = new SharedWorkspaceMozartImpl(factory, serverPort);
			else if(properties.getProperty(USE_IMPLEMENTATION_PROPERTY).equals(USE_JMS))
				implementation = new SharedWorkspaceJMSImpl(factory);
		} catch (IOException e) {
			throw new SharedWorkspaceException("Could not read " + PROPERTIES_FILENAME + " (" + e.getMessage() + ")");
		}
		return implementation;
	}
	
	/**
	 * returns true if the mozart implementation should be used, false otherwise
	 */
	public static boolean useMozartImplementation() throws SharedWorkspaceException {
		try {
			Properties properties = new Properties();
			InputStream inputStream = new FileInputStream(PROPERTIES_FILENAME);
			properties.load(inputStream);
			if(properties.getProperty(USE_IMPLEMENTATION_PROPERTY).equals(USE_MOZART))
				return true;
			return false;
		} catch(IOException e) {
			throw new SharedWorkspaceException("Could not read " + PROPERTIES_FILENAME + " (" + e.getMessage() + ")");
		}
	}
	
	public static boolean useStandaloneMozartServer() {
		boolean result = false;
		try {
			Properties properties = new Properties();
			InputStream inputStream = new FileInputStream(PROPERTIES_FILENAME);
			properties.load(inputStream);
			if(properties.getProperty(MOZART_USE_STANDALONE_PROPERTY).equals(MOZART_USE_STANDALONE_YES))
				return true;
		} catch (Exception e) {
		}
		return result;
	}
}
