package at.ac.tuwien.complang.sbc11.mozart;

import java.net.URI;

import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsConstants.Container;
import org.mozartspaces.core.MzsConstants.RequestTimeout;

public class SpaceUtils {
	
	public static final ContainerReference getOrCreateNamedFIFOContainer(String containerName, URI spaceURI, Capi capi) throws MzsCoreException {
		ContainerReference container = null;
		
		try
		{
			System.out.println("LOOKING UP FIFO CONTAINER WITH NAME " + containerName + "...");
			container = capi.lookupContainer(containerName, spaceURI, RequestTimeout.DEFAULT, null);
			System.out.println("CONTAINER FOUND.");
		}
		catch(MzsCoreException e)
		{
			System.out.println("CONTAINER NOT FOUND - TRYING TO CREATE A NEW CONTAINER...");
			container = capi.createContainer(containerName, spaceURI, Container.UNBOUNDED, null, new LindaCoordinator());
			System.out.println("CONTAINER WITH NAME " + containerName + " CREATED.");
		}
		return container;
	}
	
}
