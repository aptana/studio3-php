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

import com.aptana.php.debug.PHPDebugPlugin;
import com.aptana.php.debug.core.util.NameValuePair;

/**
 * Provides a HTTP redirect for a debug session initialization. This redirector should be called when there is a need to
 * pass POST data. A debug session that is configured to start with pre-defined POST data should call this
 * redirecting-server with the target URL and the POST data {@link Map}. This server, in turn, will redirect to the
 * final URL destination with the POST data by responding with a JavaScript post redirection to the target URL.
 * 
 * @author Shalom Gibly
 * @since PHP 1.1.1
 */
public class RemoteDebugRedirector
{
	public static String URL_ENCODING = "UTF-8"; //$NON-NLS-1$
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
			browser.openURL(new URL("http://127.0.0.1:" + server.getLocalPort())); //$NON-NLS-1$
		}
		catch (Exception e)
		{
			PHPDebugPlugin.logError("Error while redirecting the PHP Debug session", e); //$NON-NLS-1$
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
					responseBuilder.append(URLEncoder.encode(nameValuePair.value, URL_ENCODING));
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
		Job job = new Job("PHP Debug Server Redirector") //$NON-NLS-1$
		{
			protected IStatus run(IProgressMonitor monitor)
			{
				try
				{
					// Accept and send the response
					Socket socket = server.accept();
					DataOutputStream writer = new DataOutputStream(socket.getOutputStream());
					writer.writeBytes("HTTP/1.1 200 OK\r\n"); //$NON-NLS-1$
					writer.writeBytes("Server: PHP Debug Proxy Server"); //$NON-NLS-1$
					writer.writeBytes("Content-Type: text/html\r\n"); //$NON-NLS-1$
					writer.writeBytes("Content-Length: " + response.length() + "\r\n"); //$NON-NLS-1$ //$NON-NLS-2$
					writer.writeBytes("Connection: close\r\n"); //$NON-NLS-1$
					writer.writeBytes("\r\n"); //$NON-NLS-1$
					writer.writeBytes(response);
					writer.flush();
				}
				catch (IOException e)
				{
					e.printStackTrace();
					PHPDebugPlugin.logError("PHP Debug Proxy error", e); //$NON-NLS-1$
				}
				finally
				{
					stopServer();
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
		job.schedule();
	}

	/**
	 * Stop the server. This will stop the server with a little delay of about 3 seconds.
	 */
	protected void stopServer()
	{
		Job stoppingServerJob = new Job("PHP Debug Server Redirector Shutdown") //$NON-NLS-1$
		{
			protected IStatus run(IProgressMonitor monitor)
			{
				if (server != null)
				{
					try
					{
						server.close();
					}
					catch (IOException e)
					{
						e.printStackTrace();
					}
					server = null;
				}
				return Status.OK_STATUS;
			}
		};
		stoppingServerJob.setSystem(true);
		stoppingServerJob.schedule(3000);
	}
}
