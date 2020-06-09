/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.core.tunneling;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jsch.core.IJSchLocation;
import org.eclipse.jsch.core.IJSchService;

import com.aptana.core.util.CollectionsUtil;
import com.aptana.php.debug.PHPDebugPlugin;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * A SSH Tunnel session is the actual class that interacts with jcraft JSch sessions. A new session is be created as
 * needed when the {@link #getSession()} method is called.
 * 
 * @author Shalom Gibly
 */
public class SSHTunnelSession
{
	private static final int DEFAULT_TIMEOUT = 45000;

	private static Map<String, SSHTunnelSession> pool = new HashMap<String, SSHTunnelSession>();

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
				// may throw a JSchException
				session = createSession(service, jlocation, password, monitor);
				if (session == null)
				{
					throw new JSchException("Could not create a debug tunneling session to " + hostname); //$NON-NLS-1$
				}
				if (session.getTimeout() != DEFAULT_TIMEOUT)
				{
					session.setTimeout(DEFAULT_TIMEOUT);
				}
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
				throw new OperationCanceledException("SSH Tunnel Session: Authentication Canceled"); //$NON-NLS-1$
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
		{
			session.setPassword(password);
		}
		service.connect(session, DEFAULT_TIMEOUT, monitor);
		return session;
	}

	private static IJSchService getJSchService()
	{
		return PHPDebugPlugin.getDefault().getJSchService();
	}

	private static String getPoolKey(String username, String hostname, int port)
	{
		return MessageFormat.format("{0}@{1}:{2}", username, hostname, port); //$NON-NLS-1$
	}

	/**
	 * Disconnect <b>all</b> of the SSH tunnel sessions that were statically created by this SSHTunnelSession class.
	 */
	public static void shutdown()
	{
		if (getJSch() != null && !CollectionsUtil.isEmpty(pool))
		{
			for (SSHTunnelSession session : pool.values())
			{
				try
				{
					session.getSession().disconnect();
				}
				catch (Exception ee) // $codepro.audit.disable emptyCatchClause
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
