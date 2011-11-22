package at.ac.tuwien.complang.sbc11.mozart;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsConstants.Container;
import org.mozartspaces.core.MzsConstants.RequestTimeout;

public class SpaceUtils {
	
	public static final String CONTAINER_PART_ID = "ContainerPartId";
	public static final String CONTAINER_PARTS = "ContainerParts";
	public static final String CONTAINER_MAINBOARDS = "ContainerMainboards";
	public static final String CONTAINER_INCOMPLETE = "ContainerIncomplete";
	public static final String CONTAINER_TRASHED = "ContainerTrashed";
	public static final String CONTAINER_SHIPPED = "ContainerShipped";
	
	public static final ContainerReference getOrCreateFIFOContainer(String containerName, URI spaceURI, Capi capi) throws MzsCoreException {
		ContainerReference container = null;
		
		try
		{
			container = capi.lookupContainer(containerName, spaceURI, RequestTimeout.DEFAULT, null);
		}
		catch(MzsCoreException e)
		{
			container = capi.createContainer(containerName, spaceURI, Container.UNBOUNDED, null, new FifoCoordinator());
		}
		return container;
	}
	
	public static final ContainerReference getOrCreateAnyContainer(String containerName, URI spaceURI, Capi capi) throws MzsCoreException {
		ContainerReference container = null;
		
		try
		{
			container = capi.lookupContainer(containerName, spaceURI, RequestTimeout.DEFAULT, null);
		}
		catch(MzsCoreException e)
		{
			container = capi.createContainer(containerName, spaceURI, Container.UNBOUNDED, null, new AnyCoordinator());
		}
		return container;
	}
	
	public static final ContainerReference getOrCreateLindaContainer(String containerName, URI spaceURI, Capi capi) throws MzsCoreException {
		ContainerReference container = null;
		
		try
		{
			container = capi.lookupContainer(containerName, spaceURI, RequestTimeout.DEFAULT, null);
		}
		catch(MzsCoreException e)
		{
			container = capi.createContainer(containerName, spaceURI, Container.UNBOUNDED, null, new LindaCoordinator());
		}
		return container;
	}
	
	public static final ContainerReference getOrCreateIncompleteContainer(URI spaceURI, Capi capi) throws MzsCoreException {
		ContainerReference container = null;
		try
		{
			container = capi.lookupContainer(CONTAINER_INCOMPLETE, spaceURI, RequestTimeout.DEFAULT, null);
		}
		catch(MzsCoreException e)
		{
			List<Coordinator> obligatoryCoords = new ArrayList<Coordinator>();
			obligatoryCoords.add(new LindaCoordinator());
			obligatoryCoords.add(new LabelCoordinator());
			container = capi.createContainer(CONTAINER_INCOMPLETE, spaceURI, Container.UNBOUNDED, obligatoryCoords, null, null);
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
