/**
 * This file Copyright (c) 2005-2010 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain other free and open source software ("FOSS") code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.debug.php.core.tunneling;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * A SSH tunnels (port forwarding) factory class, which also caches the generated factories and act as SSHTunnels
 * manager.
 * 
 * @author Shalom Gibly
 */
public class SSHTunnelFactory
{
	private static HashMap<SSHTunnel, SSHTunnel> tunnels = new HashMap<SSHTunnel, SSHTunnel>();

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
		String localHost = "localhost"; //$NON-NLS-1$
		try
		{
			localHost = InetAddress.getLocalHost().getHostName();
		}
		catch (UnknownHostException e)
		{
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
