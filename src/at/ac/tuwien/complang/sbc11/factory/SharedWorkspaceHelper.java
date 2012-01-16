package at.ac.tuwien.complang.sbc11.factory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Properties;
import java.util.logging.Logger;

import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCore;

import at.ac.tuwien.complang.sbc11.benchmark.Benchmark;
import at.ac.tuwien.complang.sbc11.factory.exception.SharedWorkspaceException;
import at.ac.tuwien.complang.sbc11.mozart.SpaceUtils;
import at.ac.tuwien.complang.sbc11.ui.Factory;

public class SharedWorkspaceHelper {
	
	private static final String PROPERTIES_FILENAME = "factory.properties";
	private static final String USE_IMPLEMENTATION_PROPERTY = "factory.use-implementation";
	private static final String USE_MOZART = "mozart";
	private static final String USE_JMS = "jms";
	private static final String MOZART_USE_STANDALONE_PROPERTY = "factory.mozart.use-standalone";
	private static final String MOZART_USE_STANDALONE_YES = "yes";
	private static final String WAIT_FOR_START_SIGNAL = "factory.wait-for-start-signal";
	private static final String WAIT_FOR_START_SIGNAL_YES = "yes";
	private static final String LABEL_SIGNAL_START = "START";
	private static final String LABEL_SIGNAL_STOP = "STOP";
	
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
	
	public static SharedWorkspace getWorkspaceImplementation(String uri) throws SharedWorkspaceException {
		SharedWorkspace implementation = null;
		try {
			Properties properties = new Properties();
			InputStream inputStream = new FileInputStream(PROPERTIES_FILENAME);
			properties.load(inputStream);
			if(properties.getProperty(USE_IMPLEMENTATION_PROPERTY).equals(USE_MOZART))
				implementation = new SharedWorkspaceMozartImpl(uri);
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
	
	public static void sendStartSignal() throws SharedWorkspaceException {
		try {
			URI spaceURI = new URI("xvsm://localhost:" + String.valueOf(Benchmark.PORT_START_SIGNAL));
			MzsCore core = DefaultMzsCore.newInstance(0);
			Capi capi = new Capi(core);
			ContainerReference signalContainer = SpaceUtils.getOrCreateStartSignalContainer(spaceURI, capi);
			capi.write(new Entry(new String("START"), LabelCoordinator.newCoordinationData(LABEL_SIGNAL_START)), signalContainer);
		} catch(Exception e) {
			throw new SharedWorkspaceException(e.getMessage());
		}
	}
	
	public static void sendStopSignal() throws SharedWorkspaceException {
		try {
			URI spaceURI = new URI("xvsm://localhost:" + String.valueOf(Benchmark.PORT_START_SIGNAL));
			MzsCore core = DefaultMzsCore.newInstance(0);
			Capi capi = new Capi(core);
			ContainerReference signalContainer = SpaceUtils.getOrCreateStartSignalContainer(spaceURI, capi);
			capi.write(new Entry(new String("STOP"), LabelCoordinator.newCoordinationData(LABEL_SIGNAL_STOP)), signalContainer);
		} catch(Exception e) {
			throw new SharedWorkspaceException(e.getMessage());
		}
	}
	
	/**
	 * blocks and waits for the start signal
	 * if the property factory.wait-for-start-signal is set to yes
	 */
	public static void waitForStartSignal() {
		try {
			Properties properties = new Properties();
			InputStream inputStream = new FileInputStream(PROPERTIES_FILENAME);
			properties.load(inputStream);
			if(properties.getProperty(WAIT_FOR_START_SIGNAL).equals(WAIT_FOR_START_SIGNAL_YES))
			{
				// BLOCK AND WAIT FOR START SIGNAL
				URI spaceURI = new URI("xvsm://localhost:" + String.valueOf(Benchmark.PORT_START_SIGNAL));
				MzsCore core = DefaultMzsCore.newInstance(0);
				Capi capi = new Capi(core);
				ContainerReference signalContainer = SpaceUtils.getOrCreateStartSignalContainer(spaceURI, capi);
				capi.read(signalContainer, LabelCoordinator.newSelector(LABEL_SIGNAL_START, 1), RequestTimeout.INFINITE, null);
			}
		} catch (Exception e) {
			Logger.getAnonymousLogger().severe(e.getMessage());
		}
	}
	
	/**
	 * blocks and waits for the stop signal
	 * if the property factory.wait-for-start-signal is set to yes
	 */
	public static void waitForStopSignal() {
		try {
			Properties properties = new Properties();
			InputStream inputStream = new FileInputStream(PROPERTIES_FILENAME);
			properties.load(inputStream);
			if(properties.getProperty(WAIT_FOR_START_SIGNAL).equals(WAIT_FOR_START_SIGNAL_YES))
			{
				// BLOCK AND WAIT FOR STOP SIGNAL
				URI spaceURI = new URI("xvsm://localhost:" + String.valueOf(Benchmark.PORT_START_SIGNAL));
				MzsCore core = DefaultMzsCore.newInstance(0);
				Capi capi = new Capi(core);
				ContainerReference signalContainer = SpaceUtils.getOrCreateStartSignalContainer(spaceURI, capi);
				capi.read(signalContainer, LabelCoordinator.newSelector(LABEL_SIGNAL_STOP, 1), RequestTimeout.INFINITE, null);
			}
		} catch (Exception e) {
			Logger.getAnonymousLogger().severe(e.getMessage());
		}
	}
	
	public static boolean usesSignal() {
		try {
			Properties properties = new Properties();
			InputStream inputStream = new FileInputStream(PROPERTIES_FILENAME);
			properties.load(inputStream);
			if(properties.getProperty(WAIT_FOR_START_SIGNAL).equals(WAIT_FOR_START_SIGNAL_YES))
				return true;
		} catch (Exception e) {
			Logger.getAnonymousLogger().severe(e.getMessage());
		}
		return false;
	}
}
