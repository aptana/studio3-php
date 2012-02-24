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
