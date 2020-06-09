/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.core.tunneling;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aptana.core.logging.IdeLog;
import com.aptana.php.debug.IDebugScopes;
import com.aptana.php.debug.PHPDebugPlugin;

/**
 * A SSH tunnels (port forwarding) factory class, which also caches the generated factories and act as SSHTunnels
 * manager.
 * 
 * @author Shalom Gibly
 */
public class SSHTunnelFactory
{
	private static final String LOCALHOST = "localhost"; //$NON-NLS-1$
	private static Map<SSHTunnel, SSHTunnel> tunnels = new HashMap<SSHTunnel, SSHTunnel>();

	/**
	 * Returns a {@link SSHTunnel} for port-forwarding between a remote host to this localhost. The returned tunnel may
	 * be connected or disconnected.
	 * 
	 * @param remoteHost
	 * @param userName
	 * @param password
	 * @param localPort
	 * @param remotePort
	 * @param cacheTunnel
	 *            Load/save the result tunnel from/to an inner cache for further use (note that the tunnel is not
	 *            comparing passwords, so make sure that password is not changing when you are using the cache)
	 * @return A {@link SSHTunnel} instance.
	 */
	public static SSHTunnel getSSHTunnel(String remoteHost, String userName, String password, int localPort,
			int remotePort, boolean cacheTunnel)
	{
		SSHTunnel tunnel = createSSHTunnel(remoteHost, userName, password, localPort, remotePort);
		if (cacheTunnel)
		{
			if (tunnels.containsKey(tunnel))
			{
				tunnel = tunnels.get(tunnel);
			}
			else
			{
				tunnels.put(tunnel, tunnel);
			}
		}
		return tunnel;
	}

	/**
	 * A convenient call to returned a possibly cached SSHTunnel. In case it was not cached, a new tunnel will be
	 * returned and cached for further use. The returned tunnel may be connected or disconnected.
	 * 
	 * @param remoteHost
	 * @param userName
	 * @param password
	 * @param localPort
	 * @param remotePort
	 * @return An SSHTunnel
	 * @see #getSSHTunnel(String, String, String, int, int, boolean)
	 */
	public static SSHTunnel getSSHTunnel(String remoteHost, String userName, String password, int localPort,
			int remotePort)
	{
		return getSSHTunnel(remoteHost, userName, password, localPort, remotePort, true);
	}

	/**
	 * Returns whether or not there is a cached tunnel in this tunnel factory. The cached tunnel may be in a connected
	 * or a disconnected state.
	 * 
	 * @param remoteHost
	 * @param userName
	 * @param password
	 * @param localPort
	 * @param remotePort
	 * @return True, if there is a cached tunnel with the given parameters; False, otherwise.
	 */
	public static boolean hasSSHTunnel(String remoteHost, String userName, String password, int localPort,
			int remotePort)
	{
		SSHTunnel tunnel = createSSHTunnel(remoteHost, userName, password, localPort, remotePort);
		return tunnels.containsKey(tunnel);
	}

	/*
	 * Constructs and returns a new SSHTunnel instance.
	 * @param remoteHost
	 * @param userName
	 * @param password
	 * @param localPort
	 * @param remotePort
	 * @return A new SSHTunnel
	 */
	private static SSHTunnel createSSHTunnel(String remoteHost, String userName, String password, int localPort,
			int remotePort)
	{
		String localHost = LOCALHOST;
		try
		{
			localHost = InetAddress.getLocalHost().getHostName();
		}
		catch (UnknownHostException e)
		{
			IdeLog.logError(PHPDebugPlugin.getDefault(), "Unknown host", e, IDebugScopes.DEBUG); //$NON-NLS-1$
		}
		SSHTunnel tunnel = new SSHTunnel(localHost, remoteHost, userName, password, localPort, remotePort);
		return tunnel;
	}

	/**
	 * Closes all the SSHTunnel connections that were initiated and caches in this factory.
	 */
	public static void closeAllConnections()
	{
		for (SSHTunnel tunnel : tunnels.values())
		{
			tunnel.disconnect();
		}
	}

	/**
	 * Returns an unmodifiable List of the SSHTunnels that were created and cached using this factory. The returned
	 * SSHTunnels are the 'real' reference to the one that might be in use currently (so careful about disconnecting
	 * them).
	 * 
	 * @return An unmodifiable List of the SSHTunnels
	 */
	public static List<SSHTunnel> getAllTunnels()
	{
		return Collections.unmodifiableList(Arrays.asList(tunnels.values().toArray(new SSHTunnel[tunnels.size()])));
	}
}
