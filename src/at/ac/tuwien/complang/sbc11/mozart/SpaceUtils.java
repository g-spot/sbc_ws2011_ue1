package at.ac.tuwien.complang.sbc11.mozart;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.Coordinator;
import org.mozartspaces.capi3.FifoCoordinator;
import org.mozartspaces.capi3.KeyCoordinator;
import org.mozartspaces.capi3.LabelCoordinator;
import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsConstants.Container;
import org.mozartspaces.core.MzsConstants.RequestTimeout;

public class SpaceUtils {
	
	public static final String CONTAINER_ID = "<Identifiers>";
	public static final String CONTAINER_PARTS = "<Parts>";
	public static final String CONTAINER_MAINBOARDS = "<Mainboards>";
	public static final String CONTAINER_INCOMPLETE = "<Incomplete>";
	public static final String CONTAINER_TRASHED = "<Trashed>";
	public static final String CONTAINER_SHIPPED = "<Shipped>";
	public static final String CONTAINER_ORDERS = "<Orders>";
	
	public static final String CONTAINER_START_SIGNAL = "<StartSignal>";
	
	/**
	 * searches for a named fifo container, creates a new named container, if nothing is found
	 * @param containerName
	 * 			the name of the container
	 * @param spaceURI
	 * 			the uri where to search for/create the container
	 * @param capi
	 * @return
	 * 			the found/created container
	 * @throws MzsCoreException
	 */
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
	
	/**
	 * searches for a named any container, creates a new named container, if nothing is found
	 * @param containerName
	 * 			the name of the container
	 * @param spaceURI
	 * 			the uri where to search for/create the container
	 * @param capi
	 * @return
	 * 			the found/created container
	 * @throws MzsCoreException
	 */
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
	
	/**
	 * searches for a named linda container, creates a new named container, if nothing is found
	 * @param containerName
	 * 			the name of the container
	 * @param spaceURI
	 * 			the uri where to search for/create the container
	 * @param capi
	 * @return
	 * 			the found/created container
	 * @throws MzsCoreException
	 */
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
	
	/**
	 * searches for the "incomplete computers"-container, creates it, if nothing is found.
	 * this container works with a LindaCoordinator and a LabelCoordinator
	 * @param spaceURI
	 * 			the uri where to search for/create the container
	 * @param capi
	 * @return
	 * 			the found/created container
	 * @throws MzsCoreException
	 */
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
	
	/**
	 * searches for the "id"-container, creates it, if nothing is found.
	 * this container works with a KeyCoordinator
	 * @param spaceURI
	 * 			the uri where to search for/create the container
	 * @param capi
	 * @return
	 * 			the found/created container
	 * @throws MzsCoreException
	 */
	public static final ContainerReference getOrCreateIDContainer(URI spaceURI, Capi capi) throws MzsCoreException {
		ContainerReference container = null;
		
		try
		{
			container = capi.lookupContainer(CONTAINER_ID, spaceURI, RequestTimeout.DEFAULT, null);
		}
		catch(MzsCoreException e)
		{
			container = capi.createContainer(CONTAINER_ID, spaceURI, Container.UNBOUNDED, null, new KeyCoordinator());
		}
		return container;
	}

	public static ContainerReference getOrCreateOrderContainer(URI spaceURI, Capi capi) throws MzsCoreException {
		ContainerReference container = null;
		try
		{
			container = capi.lookupContainer(CONTAINER_ORDERS, spaceURI, RequestTimeout.DEFAULT, null);
		}
		catch(MzsCoreException e)
		{
			List<Coordinator> obligatoryCoords = new ArrayList<Coordinator>();
			obligatoryCoords.add(new FifoCoordinator());
			obligatoryCoords.add(new KeyCoordinator());
			obligatoryCoords.add(new LabelCoordinator());
			container = capi.createContainer(CONTAINER_ORDERS, spaceURI, Container.UNBOUNDED, obligatoryCoords, null, null);
		}
		return container;
	}
	
	public static final ContainerReference getOrCreateStartSignalContainer(URI spaceURI, Capi capi) throws MzsCoreException {
		ContainerReference container = null;
		
		try
		{
			container = capi.lookupContainer(CONTAINER_START_SIGNAL, spaceURI, RequestTimeout.DEFAULT, null);
		}
		catch(MzsCoreException e)
		{
			container = capi.createContainer(CONTAINER_START_SIGNAL, spaceURI, Container.UNBOUNDED, null, new LabelCoordinator());
		}
		return container;
	}
}
