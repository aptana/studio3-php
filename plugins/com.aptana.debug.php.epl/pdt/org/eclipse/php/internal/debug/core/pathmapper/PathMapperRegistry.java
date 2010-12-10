/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org.eclipse.php.internal.debug.core.pathmapper;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.php.debug.core.debugger.pathmapper.PathMapper;
import org.eclipse.php.internal.core.util.preferences.IXMLPreferencesStorable;
import org.eclipse.php.internal.core.util.preferences.XMLPreferencesReader;
import org.eclipse.php.internal.core.util.preferences.XMLPreferencesWriter;
import org.eclipse.php.internal.debug.core.preferences.IPHPExesListener;
import org.eclipse.php.internal.debug.core.preferences.PHPExesEvent;
import org.eclipse.php.internal.debug.core.preferences.PHPexes;

public class PathMapperRegistry implements IXMLPreferencesStorable, IPHPExesListener, IServerManagerListener 
{
	
	/**
	 * 
	 */
	private static final String PATH_MAPPER_ATTRIBUTE = "pathMapper";

	/**
	 * 
	 */
	private static final String SERVER_TYPE_ATTRIBUTE = "serverType";

	/**
	 * 
	 */
	private static final String PATH_MAPPER_EXTENSION_ID = "com.aptana.ide.php.debug.core.pathMapper";

	private static final String PATH_MAPPER_PREF_KEY = Activator.getID() + ".pathMapper"; //$NON-NLS-1$

	private static PathMapperRegistry instance;
	private HashMap<IServer, PathMapper> serverPathMapper;
	private HashMap<PathMapper, IServer> pathMapperToServer;
	private HashMap<PHPexeItem, PathMapper> phpExePathMapper;

	private PathMapperRegistry() {
		serverPathMapper = new HashMap<IServer, PathMapper>();
		phpExePathMapper = new HashMap<PHPexeItem, PathMapper>();
		pathMapperToServer = new HashMap<PathMapper, IServer>();
		loadFromPreferences();
		//create the link to servers manager here in order not to create tightly coupled relationship
		PHPServersManager.addManagerListener(this);
	}

	public synchronized static PathMapperRegistry getInstance() {
		if (instance == null) {
			instance = new PathMapperRegistry();
		}
		return instance;
	}

	/**
	 * Return path mapper which corresponding to the given PHPexe item
	 * @param phpExe PHPExe item
	 * @return path mapper, or <code>null</code> if there's no one
	 */
	public static PathMapper getByPHPExe(PHPexeItem phpExe) {
		PathMapper result = getInstance().phpExePathMapper.get(phpExe);
		if (result == null) {
			result = new PathMapper();
			getInstance().phpExePathMapper.put(phpExe, result);
			PHPexes.getInstance().addPHPExesListener(getInstance());
		}
		return result;
	}

	/**
	 * Return path mapper which corresponding to the given Server instance
	 * @param server Server instance
	 * @return path mapper, or <code>null</code> if there's no one
	 */
	public static PathMapper getByServer(PHPServerProxy server) {
		IServer serv = server.getServer();
		PathMapper result = getInstance().serverPathMapper.get(serv);
		if (result == null) {
			result = getNewServerPathMapper(serv);
			getInstance().serverPathMapper.put(serv, result);
			getInstance().pathMapperToServer.put(result, serv);
		}
		return result;
	}

	/**
	 * Returns a new PHServerProxy that contains the {@link IServer} that is linked to the given {@link PathMapper}.
	 *  
	 * @param mapper A PathMapper instance
	 * @return A newly created PHPServerProxy; or <code>null</code> if no server is attached to the given mapper
	 */
	public static PHPServerProxy getByMapper(PathMapper mapper) {
		IServer server = getInstance().pathMapperToServer.get(mapper);
		if (server != null) {
			return new PHPServerProxy(server);
		}
		return null;
	}
	
	/**
	 * Returns path mapper associated with the given launch configuration
	 * @param launchConfiguration Launch configuration
	 * @return path mapper
	 */
	public static PathMapper getByLaunchConfiguration(ILaunchConfiguration launchConfiguration) {
		PathMapper pathMapper = null;
		try {
			String serverName = launchConfiguration.getAttribute(PHPServerProxy.NAME, (String) null);
			if (serverName != null) {
				pathMapper = getByServer(PHPServersManager.getServer(serverName));
			}/* else {
				String phpExe = launchConfiguration.getAttribute(PHPCoreConstants.ATTR_EXECUTABLE_LOCATION, (String) null);
				String phpIni = launchConfiguration.getAttribute(PHPCoreConstants.ATTR_INI_LOCATION, (String) null);
				if (phpExe != null) {
					pathMapper = getByPHPExe(PHPexes.getInstance().getItemForFile(phpExe, phpIni));
				}
			}*/
		} catch (CoreException e) {
			Activator.log(e);
		}
		return pathMapper;
	}

	@SuppressWarnings("unchecked")
	public void loadFromPreferences() {
		HashMap[] elements = XMLPreferencesReader.read(Activator.getDefault().getPluginPreferences(), PATH_MAPPER_PREF_KEY);
		if (elements.length == 1) {
			restoreFromMap(elements[0]);
		}
	}

	public static void storeToPreferences() {
		XMLPreferencesWriter.write(Activator.getDefault().getPluginPreferences(), PATH_MAPPER_PREF_KEY, getInstance());
	}

	@SuppressWarnings("unchecked")
	public synchronized void restoreFromMap(HashMap map) {
		if (map == null) {
			return;
		}
		serverPathMapper.clear();
		pathMapperToServer.clear();
		phpExePathMapper.clear();

		map = (HashMap)map.get("pathMappers"); //$NON-NLS-1$
		if (map == null) {
			return;
		}
		Iterator i = map.keySet().iterator();
		while (i.hasNext()) {
			HashMap entryMap = (HashMap) map.get(i.next());
			String serverName = (String) entryMap.get("server"); //$NON-NLS-1$
			String phpExeFile = (String) entryMap.get("phpExe"); //$NON-NLS-1$
			String phpIniFile = (String) entryMap.get("phpIni"); //$NON-NLS-1$
			PathMapper pathMapper = new PathMapper();
			pathMapper.restoreFromMap((HashMap) entryMap.get("mapper")); //$NON-NLS-1$
			if (serverName != null) {
				PHPServerProxy server = PHPServersManager.getServer(serverName);
				if (server != null)
				{
					// SG: Revert Denis's xdebug changes for setting up a new path mapper without loading the mapper values from the preferences... grrrr!
					// serverPathMapper.put(server.getServer(), getNewServerPathMapper(server.getServer()));
					serverPathMapper.put(server.getServer(), pathMapper);
					pathMapperToServer.put(pathMapper, server.getServer());
				}
			} else if (phpExeFile != null) {
				PHPexeItem phpExeItem = PHPexes.getInstance().getItemForFile(phpExeFile, phpIniFile);
				if (phpExeItem != null) {
					phpExePathMapper.put(phpExeItem, pathMapper);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public synchronized HashMap storeToMap() {
		HashMap elements = new HashMap();
		Iterator i = serverPathMapper.keySet().iterator();
		int c = 1;
		while (i.hasNext()) {
			HashMap entry = new HashMap();
			IServer server = (IServer) i.next();
			if (!server.isTransient())
			{
				PathMapper pathMapper = serverPathMapper.get(server);
				entry.put("server", server.getName()); //$NON-NLS-1$
				entry.put("mapper", pathMapper.storeToMap()); //$NON-NLS-1$
				elements.put("item" + (c++), entry); //$NON-NLS-1$
			}
		}
		i = phpExePathMapper.keySet().iterator();
		while (i.hasNext()) {
			HashMap entry = new HashMap();
			PHPexeItem phpExeItem = (PHPexeItem) i.next();
			PathMapper pathMapper = phpExePathMapper.get(phpExeItem);
			entry.put("phpExe", phpExeItem.getExecutable().toString()); //$NON-NLS-1$
			if (phpExeItem.getINILocation() != null){
				entry.put("phpIni", phpExeItem.getINILocation().toString()); //$NON-NLS-1$
			}
			entry.put("mapper", pathMapper.storeToMap()); //$NON-NLS-1$
			elements.put("item" + (c++), entry); //$NON-NLS-1$
		}
		HashMap root = new HashMap();
		root.put("pathMappers", elements); //$NON-NLS-1$
		return root;
	}

	public void phpExeAdded(PHPExesEvent event) {
		if (!phpExePathMapper.containsKey(event.getPHPExeItem())) {
			phpExePathMapper.put(event.getPHPExeItem(), new PathMapper());
		}
	}

	public void phpExeRemoved(PHPExesEvent event) {
		phpExePathMapper.remove(event.getPHPExeItem());
	}

	public void phpExeDefaultChanged(PHPexeItem oldDefault, PHPexeItem newDefault)
	{
		// Changed is ignored, as we don't really need to do anything.
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.ide.server.core.IServerManagerListener#serversChanged(com.aptana.ide.server.core.ServerManagerEvent)
	 */
	public void serversChanged(ServerManagerEvent event)
	{
		IServer server = event.getServer();
		switch (event.getKind())
		{
			case ServerManagerEvent.KIND_ADDED:
				if (!serverPathMapper.containsKey(server))
				{
					// Add only servers that can work with PHP
					if (server.isExternal() && server.isWebServer())
					{
						PathMapper mapper = getNewServerPathMapper(server);
						serverPathMapper.put(server, mapper);
						pathMapperToServer.put(mapper, server);
					}
				}
				break;
			case ServerManagerEvent.KIND_REMOVED:
				PathMapper removed = serverPathMapper.remove(server);
				if (removed != null) {
					pathMapperToServer.remove(removed);
				}
				break;
			// Changed is ignored, as we don't really need to do anything.
		}
	}
	
	private static PathMapper getNewServerPathMapper(IServer server)
	{
		PathMapper pathMapper = new PathMapper();
		if (server.isTransient())
		{
			// This server should not be persistent, nor should it get a path mapper from an extension.
			return pathMapper;
		}
		IConfigurationElement[] elements = 
			Platform.getExtensionRegistry().getConfigurationElementsFor(PATH_MAPPER_EXTENSION_ID);
		
		try
		{
			if (elements != null && elements.length != 0)
			{
				for (IConfigurationElement element : elements)
				{
					String attribute = element.getAttribute(SERVER_TYPE_ATTRIBUTE);
					if (attribute != null && attribute.equals(server.getServerType().getId()))
					{
						pathMapper = (PathMapper)
							element.createExecutableExtension(PATH_MAPPER_ATTRIBUTE);
						break;
					}
				}
			}
		}
		catch (Throwable th)
		{
			IdeLog.logError(Activator.getDefault(), 
					"Unexpected exception while getting server path mappers", th);
		}
		
		return pathMapper;
	}
}
