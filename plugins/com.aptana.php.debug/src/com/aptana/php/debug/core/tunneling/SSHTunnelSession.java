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
package com.aptana.php.debug.core.tunneling;

import java.util.Enumeration;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jsch.core.IJSchLocation;
import org.eclipse.jsch.core.IJSchService;

import com.aptana.php.debug.PHPDebugPlugin;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * A SSH Tunnel session is the actual class that interacts with jcraft JSch sessions. A new session is be created as
 * needed when the {@link #getSession()} method is called.
 */
public class SSHTunnelSession
{
	private static final int DEFAULT_TIMEOUT = 45000;

	private static java.util.Hashtable<String, SSHTunnelSession> pool = new java.util.Hashtable<String, SSHTunnelSession>();

	private Session session;

	/**
	 * Returns a SSHTunnelSession for the given arguments. A new session will be created in case there is no active
	 * session with the given target and user name.
	 * 
	 * @param username
	 * @param password
	 * @param hostname
	 * @param port
	 * @param monitor
	 * @return A connected SSH tunnel session.
	 * @throws JSchException
	 *             In case a connection could not be established.
	 */
	public static SSHTunnelSession getSession(String username, String password, String hostname, int port,
			IProgressMonitor monitor) throws JSchException
	{
		String key = getPoolKey(username, hostname, port);

		try
		{
			SSHTunnelSession jschSession = (SSHTunnelSession) pool.get(key);
			if (jschSession != null && !jschSession.getSession().isConnected())
			{
				pool.remove(key);
				jschSession = null;
			}

			if (jschSession == null)
			{
				IJSchService service = getJSchService();
				IJSchLocation jlocation = service.getLocation(username, hostname, port);
				Session session = null;
				try
				{
					session = createSession(service, jlocation, password, monitor);
				}
				catch (JSchException e)
				{
					throw e;
				}
				if (session == null)
					throw new JSchException("Could not create a debug tunneling session to " + hostname); //$NON-NLS-1$
				if (session.getTimeout() != DEFAULT_TIMEOUT)
					session.setTimeout(DEFAULT_TIMEOUT);
				SSHTunnelSession schSession = new SSHTunnelSession(session);
				pool.put(key, schSession);
				return schSession;
			}
			return jschSession;
		}
		catch (JSchException e)
		{
			pool.remove(key);
			if (e.toString().indexOf("Auth cancel") != -1) { //$NON-NLS-1$
				throw new OperationCanceledException();
			}
			throw e;
		}
	}

	/*
	 * Creates a JSch session.
	 */
	private static Session createSession(IJSchService service, IJSchLocation location, String password,
			IProgressMonitor monitor) throws JSchException
	{
		Session session = service.createSession(location, null);
		session.setTimeout(DEFAULT_TIMEOUT);
		if (password != null)
			session.setPassword(password);
		service.connect(session, DEFAULT_TIMEOUT, monitor);
		return session;
	}

	private static IJSchService getJSchService()
	{
		return PHPDebugPlugin.getDefault().getJSchService();
	}

	private static String getPoolKey(String username, String hostname, int port)
	{
		return username + "@" + hostname + ":" + port; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Disconnect <b>all</b> of the SSH tunnel sessions that were statically created by this SSHTunnelSession class.
	 */
	public static void shutdown()
	{
		if (getJSch() != null && pool.size() > 0)
		{
			for (Enumeration<SSHTunnelSession> e = pool.elements(); e.hasMoreElements();)
			{
				SSHTunnelSession session = e.nextElement();
				try
				{
					session.getSession().disconnect();
				}
				catch (Exception ee)
				{
					// Ignore
				}
			}
			pool.clear();
		}
	}

	/**
	 * Returns a {@link JSch} from the JSch service.
	 * 
	 * @return {@link JSch}
	 */
	public static JSch getJSch()
	{
		return getJSchService().getJSch();
	}

	private SSHTunnelSession(Session session)
	{
		this.session = session;
	}

	/**
	 * Returns the {@link Session} instance.
	 * 
	 * @return {@link Session}
	 */
	public Session getSession()
	{
		return session;
	}

	/**
	 * Dispose the connection (disconnect).
	 */
	public void dispose()
	{
		pool.remove(getPoolKey(session.getUserName(), session.getHost(), session.getPort()));
		if (session.isConnected())
		{
			session.disconnect();
		}
	}

}
