package at.ac.tuwien.complang.sbc11.mozart;

import java.net.URI;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsConstants.Container;
import org.mozartspaces.core.MzsConstants.RequestTimeout;

public class SpaceUtils {
	
	public static final String CONTAINER_PART_ID = "ContainerPartId";
	public static final String CONTAINER_PARTS = "ContainerParts";
	public static final String CONTAINER_MAINBOARDS = "ContainerMainboards";
	public static final String CONTAINER_UNTESTED = "ContainerUntested";
	public static final String CONTAINER_DEFECT = "ContainerDefect";
	public static final String CONTAINER_READY = "ContainerReady";
	
	public static final ContainerReference getOrCreateNamedFIFOContainer(String containerName, URI spaceURI, Capi capi) throws MzsCoreException {
		ContainerReference container = null;
		
		try
		{
			//System.out.println("LOOKING UP FIFO CONTAINER WITH NAME " + containerName + "...");
			container = capi.lookupContainer(containerName, spaceURI, RequestTimeout.DEFAULT, null);
			//System.out.println("CONTAINER FOUND.");
		}
		catch(MzsCoreException e)
		{
			//System.out.println("CONTAINER NOT FOUND - TRYING TO CREATE A NEW CONTAINER...");
			container = capi.createContainer(containerName, spaceURI, Container.UNBOUNDED, null, new LindaCoordinator());
			//System.out.println("CONTAINER WITH NAME " + containerName + " CREATED.");
		}
		return container;
	}
	
	public static final ContainerReference getOrCreatePartIDContainer(URI spaceURI, Capi capi) throws MzsCoreException {
		ContainerReference container = null;
		
		try
		{
			container = capi.lookupContainer(CONTAINER_PART_ID, spaceURI, RequestTimeout.DEFAULT, null);
		}
		catch(MzsCoreException e)
		{
			// the container should take only one entry at a time
			//container = capi.createContainer(CONTAINER_PART_ID, spaceURI, 1, null, new AnyCoordinator());
			// it should work with size = 1, but i think there is a bug in transaction handling (see getNextPartid)
			container = capi.createContainer(CONTAINER_PART_ID, spaceURI, 2, null, new AnyCoordinator());
		}
		return container;
	}
}
