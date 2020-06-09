/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.core.launch.remote;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.browser.IWebBrowser;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.FileUtil;
import com.aptana.core.util.IOUtil;
import com.aptana.core.util.StringUtil;
import com.aptana.php.debug.IDebugScopes;
import com.aptana.php.debug.PHPDebugPlugin;
import com.aptana.php.debug.core.util.NameValuePair;

/**
 * Provides a HTTP redirect for a debug session initialization.<br>
 * This redirector should be called when there is a need to pass <code><b>POST</b></code> data. A debug session that is
 * configured to start with pre-defined <code>POST</code> data should call this redirecting-server with the target URL
 * and the <code>POST</code> data {@link Map}. This server, in turn, will redirect to the final URL destination with the
 * <code>POST</code> data by responding with a <code>JavaScript</code> post redirection to the target URL.
 * 
 * @author Shalom Gibly
 * @since PHP 1.1.1
 */
public class RemoteDebugRedirector
{
	private static final int REDIRECTION_SERVER_DELAY = 3000;
	private static final String LINE_TERMINATOR = FileUtil.NEW_LINE;
	private ServerSocket server;
	private String response;

	/**
	 * Start a debug session by redirecting the URL request and appending the POST data.
	 * 
	 * @param browser
	 * @param target
	 * @param postData
	 * @return The status of this redirect (OK or ERROR)
	 */
	public IStatus redirect(IWebBrowser browser, URL target, List<String> postData)
	{
		if (browser == null)
		{
			throw new IllegalArgumentException("The given target browser was null"); //$NON-NLS-1$
		}
		try
		{
			startServer();
			buildResponse(target, postData);
			Thread.sleep(1000L);
			browser.openURL(new URL("http", "127.0.0.1", server.getLocalPort(), StringUtil.EMPTY)); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch (Exception e)
		{
			IdeLog.logError(PHPDebugPlugin.getDefault(), "Error while redirecting the PHP Debug session", e, //$NON-NLS-1$
					IDebugScopes.DEBUG);
			stopServer();
			return new Status(IStatus.ERROR, PHPDebugPlugin.PLUGIN_ID, Messages.RemoteDebugRedirector_redirectingError);
		}
		return Status.OK_STATUS;
	}

	private void buildResponse(URL target, List<String> postData) throws UnsupportedEncodingException
	{
		StringBuilder responseBuilder = new StringBuilder();
		responseBuilder.append("<HTML><BODY><FORM id=\"postForm\" action=\""); //$NON-NLS-1$
		responseBuilder.append(target);
		responseBuilder.append("\" method=\"post\" target=\"_self\">"); //$NON-NLS-1$
		if (postData != null)
		{
			for (String pair : postData)
			{
				NameValuePair nameValuePair = NameValuePair.fromPairString(pair);
				if (nameValuePair != null)
				{
					responseBuilder.append("<input type=\"hidden\" name=\""); //$NON-NLS-1$
					responseBuilder.append(nameValuePair.name);
					responseBuilder.append("\" value=\""); //$NON-NLS-1$
					responseBuilder.append(URLEncoder.encode(nameValuePair.value, IOUtil.UTF_8));
					responseBuilder.append("\">"); //$NON-NLS-1$
				}
			}
		}
		responseBuilder
				.append("</FORM><SCRIPT type=\"text/javascript\"> function submitPost() { var frm = document.getElementById(\"postForm\"); frm.submit(); } window.onload = submitPost; </SCRIPT>"); //$NON-NLS-1$
		response = responseBuilder.toString();
	}

	/**
	 * Starts the server and handle the debug request.
	 * 
	 * @throws IOException
	 */
	protected void startServer() throws IOException
	{
		server = new ServerSocket(0);
		Job job = new Job("PHP Debug Server Redirector") //$NON-NLS-1$ (system job)
		{
			protected IStatus run(IProgressMonitor monitor)
			{
				try
				{
					// Accept and send the response
					Socket socket = null;
					DataOutputStream writer = null;
					try
					{
						socket = server.accept();
						writer = new DataOutputStream(socket.getOutputStream());
						writer.writeBytes("HTTP/1.1 200 OK" + LINE_TERMINATOR); //$NON-NLS-1$
						writer.writeBytes("Server: PHP Debug Proxy Server"); //$NON-NLS-1$
						writer.writeBytes("Content-Type: text/html" + LINE_TERMINATOR); //$NON-NLS-1$
						writer.writeBytes("Content-Length: " + response.length() + LINE_TERMINATOR); //$NON-NLS-1$ 
						writer.writeBytes("Connection: close" + LINE_TERMINATOR); //$NON-NLS-1$
						writer.writeBytes(LINE_TERMINATOR);
						writer.writeBytes(response);
						writer.flush();
					}
					finally
					{
						if (writer != null)
						{
							writer.close();
						}
						if (socket != null)
						{
							socket.close();
						}
					}
				}
				catch (IOException e)
				{
					IdeLog.logError(PHPDebugPlugin.getDefault(), "PHP Debug Proxy error", e); //$NON-NLS-1$
				}
				finally
				{
					stopServer();
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule(REDIRECTION_SERVER_DELAY);
	}

	/**
	 * Stop the server. This will stop the server with a little delay of about 3 seconds.
	 */
	protected void stopServer()
	{
		Job stoppingServerJob = new Job("PHP Debug Server Redirector Shutdown") //$NON-NLS-1$ (system job)
		{
			protected IStatus run(IProgressMonitor monitor)
			{
				if (server != null)
				{
					try
					{
						server.close(); // $codepro.audit.disable closeInFinally
					}
					catch (IOException e)
					{
						IdeLog.logError(PHPDebugPlugin.getDefault(), "Error closing a server redirector", e); //$NON-NLS-1$
					}
					server = null;
				}
				return Status.OK_STATUS;
			}
		};
		stoppingServerJob.setSystem(true);
		stoppingServerJob.schedule(REDIRECTION_SERVER_DELAY);
	}
}
