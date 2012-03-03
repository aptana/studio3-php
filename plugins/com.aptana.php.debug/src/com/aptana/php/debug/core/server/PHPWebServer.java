/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.core.server;

import java.net.MalformedURLException;
import java.net.URL;

import com.aptana.core.util.StringUtil;
import com.aptana.php.debug.PHPDebugPlugin;
import com.aptana.webserver.core.ExternalWebServer;

/**
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPWebServer extends ExternalWebServer
{
	/**
	 * Server name attribute.
	 */
	public static final String NAME_ATTR = "serverName"; //$NON-NLS-1$

	private static final String HTTP = "http"; //$NON-NLS-1$
	private static final String HTTPS = "https"; //$NON-NLS-1$

	private boolean persistent;

	/**
	 * Constructs a new PHPWebServer.<br>
	 * By default, the server is persistent.
	 */
	public PHPWebServer()
	{
		this.persistent = true;
	}

	/**
	 * Constructs a new PHPWebServer.
	 * 
	 * @param host
	 * @param port
	 * @param isSecure
	 * @param persistent
	 *            Mark this server as persistent or not.
	 */
	public PHPWebServer(String host, int port, boolean isSecure, boolean persistent)
	{
		this.persistent = persistent;
		try
		{
			setBaseURL(new URL(isSecure ? HTTPS : HTTP, host, port, StringUtil.EMPTY));
		}
		catch (MalformedURLException e)
		{
			PHPDebugPlugin.logError(e);
		}
	}

	public boolean isPersistent()
	{
		return persistent;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof PHPWebServer)
		{
			PHPWebServer other = (PHPWebServer) obj;
			return this.getBaseURL().equals(other.getBaseURL());
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		int hash = super.hashCode();
		URL baseURL = getBaseURL();
		return hash * 31 + (baseURL == null ? 0 : baseURL.hashCode());
	}
}
