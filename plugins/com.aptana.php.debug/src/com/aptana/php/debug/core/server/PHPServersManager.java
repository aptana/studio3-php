/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.core.server;

import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.CollectionsUtil;
import com.aptana.php.debug.IDebugScopes;
import com.aptana.php.debug.PHPDebugPlugin;
import com.aptana.webserver.core.AbstractWebServer;
import com.aptana.webserver.core.IServer;
import com.aptana.webserver.core.WebServerCorePlugin;

/**
 * PHP servers manager.
 * 
 * @author Pavel Petrochenko, Shalom Gibly
 */
public class PHPServersManager
{
	/**
	 * DEFAULT_SERVER_PREFERENCES_KEY
	 */
	public static final String DEFAULT_SERVER_PREFERENCES_KEY = "server_preferences"; //$NON-NLS-1$

	// TODO: SG - We may need to hook the PathMapper (see the addManagerListener)
	// Maybe also needs a server manager event
	private static List<IServer> tempServers = new ArrayList<IServer>();
	private static final int DEFAULT_PORT = 80;

	/**
	 * Returns all the registered temporary servers. Temporary servers are not cached, and reset for every Studio
	 * session.
	 * 
	 * @return The temporary servers.
	 */
	public static IServer[] getTemporaryServers()
	{
		return tempServers.toArray(new IServer[tempServers.size()]);
	}

	/**
	 * Creates and return a temporary server. A temporary server exists only in the memory and never cached.
	 * 
	 * @param host
	 * @param port
	 * @param isSecure
	 * @return a new IServer
	 */
	public static AbstractWebServer createTemporaryServer(String host, int port, boolean isSecure)
	{
		return new PHPWebServer(host, port, isSecure, false);
	}

	/**
	 * Adds a temporary server. It's up to the user code to determine if that server is not in the list by calling
	 * {@link #getTemporaryServer(String, int)}.
	 * 
	 * @param server
	 */
	public static void addTemporaryServer(IServer server)
	{
		tempServers.add(server);
	}

	public void removeTemporaryServer(AbstractWebServer server)
	{
		if (!tempServers.remove(server))
		{
			// search for the server by its params
			URL url = server.getBaseURL();
			String host = url.getHost();
			int port = url.getPort();
			if (port < 0)
			{
				port = DEFAULT_PORT;
			}
			IServer temporaryServer = getTemporaryServer(host, port);
			tempServers.remove(temporaryServer);
		}
	}

	/**
	 * Returns a registered temporary server, or null if no server with the given host and port is registered.
	 * 
	 * @param host
	 * @param port
	 * @return A temporary IServer; Null if no such server was registered.
	 */
	public static IServer getTemporaryServer(String host, int port)
	{
		synchronized (tempServers)
		{
			for (IServer configuration : tempServers)
			{
				URL url = configuration.getBaseURL();
				if (url.getHost().equals(host) && url.getPort() == port)
				{
					return configuration;
				}
			}
		}
		return null;
	}

	/**
	 * Returns a registered server with the given name.
	 * 
	 * @param name
	 * @return IServer
	 */
	public static IServer getServer(String name)
	{
		List<IServer> servers = getServers();
		for (IServer p : servers)
		{
			if (p.getName().equals(name))
			{
				return p;
			}
		}
		return null;
	}

	/**
	 * Returns a server by its address.
	 * 
	 * @param address
	 *            An {@link InetAddress}
	 * @return The registered server with the same address, or null.
	 */
	public static IServer getServer(InetAddress address)
	{
		List<IServer> servers = getServers();
		for (IServer p : servers)
		{
			URL url = p.getBaseURL();
			String hostname = url.getHost();
			int port = url.getPort();
			if (port < 0)
			{
				port = DEFAULT_PORT;
			}
			try
			{
				InetAddress serverInetAddress = InetAddress.getByName(hostname);
				if (address.equals(serverInetAddress))
				{
					// TODO: SG - maybe add a check by port? (somehow...)
					return p;
				}
			}
			catch (UnknownHostException e)
			{
				IdeLog.logError(PHPDebugPlugin.getDefault(), "Unknown host", e, IDebugScopes.DEBUG); //$NON-NLS-1$
			}
		}
		return null;
	}

	/**
	 * Returns the default server. This implementation just returns the first server in the list of registered servers.
	 * 
	 * @param project
	 *            (not in use)
	 * @return The first server in the list of registered servers.
	 */
	public static IServer getDefaultServer(IProject project)
	{
		List<IServer> servers = getServers();
		if (CollectionsUtil.isEmpty(servers))
		{
			return null;
		}
		return servers.get(0);
	}

	/**
	 * Return all registered compatible servers.
	 * 
	 * @return
	 */
	public static List<IServer> getServers()
	{
		// TODO - Filter only the compatible servers?
		return WebServerCorePlugin.getDefault().getServerManager().getServers();
	}
}
