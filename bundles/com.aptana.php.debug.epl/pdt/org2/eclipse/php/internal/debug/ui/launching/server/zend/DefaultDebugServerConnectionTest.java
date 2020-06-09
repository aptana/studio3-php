/*******************************************************************************
 * Copyright (c) 2008 Zend Corporation and IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org2.eclipse.php.internal.debug.ui.launching.server.zend;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org2.eclipse.php.internal.debug.core.preferences.PHPDebugCorePreferenceNames;
import org2.eclipse.php.internal.debug.core.zend.communication.DebuggerCommunicationDaemon;
import org2.eclipse.php.internal.debug.core.zend.testConnection.DebugServerTestController;
import org2.eclipse.php.internal.debug.core.zend.testConnection.DebugServerTestEvent;
import org2.eclipse.php.internal.debug.core.zend.testConnection.IDebugServerTestListener;
import org2.eclipse.php.internal.debug.ui.IDebugServerConnectionTest;
import org2.eclipse.php.internal.debug.ui.launching.server.PHPServerUIMessages;

import com.aptana.core.util.StringUtil;
import com.aptana.php.debug.core.preferences.PHPDebugPreferencesUtil;
import com.aptana.php.debug.epl.PHPDebugEPLPlugin;
import com.aptana.webserver.core.IServer;

public class DefaultDebugServerConnectionTest implements IDebugServerConnectionTest, IDebugServerTestListener
{

	protected Shell fShell;
	protected IServer fServer;
	protected String fURL;
	private Boolean isFinished = false;
	private ProgressMonitorDialog progressDialog = null;
	private final static int DEFAULT_TIMEOUT = 10000;
	private List<String> timeoutServerList = new ArrayList<String>();

	public void testConnection(IServer server, Shell shell)
	{
		fServer = server;
		fShell = shell;
		fURL = server.getBaseURL().toString();

		// check:
		// 1. server is available
		// 2. dummy.php exists
		// 3. check debugger communication
		// 4. debugger version
		IRunnableWithProgress runnableWithProgress = new IRunnableWithProgress()
		{
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
			{
				monitor.beginTask(
						PHPServerUIMessages.getString("DefaultDebugServerConnectionTest_testingConnectivity"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$

				try
				{
					// Check existence of both web server and dummy.php
					checkWebServerExistence();
					if (monitor.isCanceled())
					{// exit point from the runnable after clicking 'cancel' button
						return;
					}

					String[] hosts = getAllLocalHostsAddresses();
					DebugServerTestController.getInstance().addListener(DefaultDebugServerConnectionTest.this);
					for (String clientHost : hosts)
					{
						if (monitor.isCanceled())
						{
							return;
						}
						isFinished = false;
						// Build Query String to call debugger via GET
						String debugQuery = generateDebugQuery(clientHost);
						// Calling the debugger
						try
						{
							activateTestDebug(monitor, clientHost, debugQuery);
						}
						catch (SocketTimeoutException ste)
						{// debugger caused timeout
							if (!isFinished)
							{
								String generalTimeout = NLS.bind(PHPServerUIMessages
										.getString("DefaultDebugServerConnectionTest_timeOutMessage"), fURL); //$NON-NLS-1$
								showCustomErrorDialog(generalTimeout); //$NON-NLS-1$
								return;
							}
						}
						// the following condition test is due to immediate return, but the client host
						// that was sent is unavailable, i.e the debugger will not return to Neon
						if (isFinished)
						{
							break;
						}
						else
						{
							Thread.sleep(DEFAULT_TIMEOUT);
							if (isFinished)
							{
								break;
							}
							timeoutServerList.add(clientHost);
						}
					}
					if (!isFinished)
					{
						showCustomErrorDialog(addTimeOutsMessage("A timeout occurred when the debug server attempted to connect to the following client hosts/IPs:\n")); //$NON-NLS-1$
					}
				}
				catch (FileNotFoundException fnfe)
				{// dummy.php was not found
					showCustomErrorDialog(NLS
							.bind(PHPServerUIMessages
									.getString("DefaultDebugServerConnectionTest_theURLCouldNotBeFound"), fURL)); //$NON-NLS-1$
					return;
				}
				catch (SocketTimeoutException ste)
				{
					if (!isFinished)
					{
						showCustomErrorDialog(NLS.bind(
								PHPServerUIMessages.getString("DefaultDebugServerConnectionTest_timeOutMessage"), fURL)); //$NON-NLS-1$
						return;
					}
				}
				catch (ConnectException ce)
				{// usually when firewall blocks
					showCustomErrorDialog(NLS
							.bind(PHPServerUIMessages
									.getString("DefaultDebugServerConnectionTest_webServerConnectionFailed"), fURL)); //$NON-NLS-1$
					return;
				}
				catch (IOException er)
				{// server not found / server is down
					showCustomErrorDialog(NLS
							.bind(PHPServerUIMessages
									.getString("DefaultDebugServerConnectionTest_webServerConnectionFailed"), fURL)); //$NON-NLS-1$
					return;
				}
				finally
				{
					removeThisListener();
				}
			}

			private void activateTestDebug(IProgressMonitor monitor, String clientHost, String debugQuery)
					throws IOException
			{
				monitor.subTask(NLS.bind(
						PHPServerUIMessages.getString("DefaultDebugServerConnectionTest_testingCommunication"), clientHost)); //$NON-NLS-1$
				InputStream inputStream = null;
				try
				{
					URL checkDebugURL = new URL(debugQuery);
					final URLConnection debugConnection = checkDebugURL.openConnection();
					debugConnection.setReadTimeout(DEFAULT_TIMEOUT);
					inputStream = debugConnection.getInputStream();
				}
				finally
				{
					if (inputStream != null)
					{
						inputStream.close();
					}
				}
			}

			private void checkWebServerExistence() throws MalformedURLException, IOException
			{
				InputStream inputStream = null;
				try
				{
					// 1. check base URL (http://HOST_NAME)
					// 2.check dummy file existence
					final URL checkURL = new URL(fURL + "/dummy.php"); //$NON-NLS-1$
					URLConnection connection = checkURL.openConnection();

					connection.setConnectTimeout(5000);
					connection.setReadTimeout(DEFAULT_TIMEOUT);
					inputStream = connection.getInputStream();// this will fail when host not found and/or dummy.php not
																// found (2 different exception
				}
				finally
				{
					if (inputStream != null)
					{
						inputStream.close();
					}
				}
			}

		};
		progressDialog = new ProgressMonitorDialog(fShell);
		progressDialog.setBlockOnOpen(false);
		progressDialog.setCancelable(true);

		try
		{
			progressDialog.run(true, true, runnableWithProgress);
		}
		catch (Exception e)
		{
			PHPDebugEPLPlugin.logError(e);
		}
	}

	private void removeThisListener()
	{
		DebugServerTestController.getInstance().removeListener(this);
	}

	protected void showCustomErrorDialog(final String message)
	{
		fShell.getDisplay().asyncExec(new Runnable()
		{
			public void run()
			{
				DefaultServerTestMessageDialog dialog = new DefaultServerTestMessageDialog(fShell, PHPServerUIMessages
						.getString("DefaultDebugServerConnectionTest_testDebugServer"), null, // accept //$NON-NLS-1$
						message, MessageDialog.ERROR, new String[] { IDialogConstants.OK_LABEL }, 0);
				dialog.open();
			}
		});
	}

	protected String generateDebugQuery(String host)
	{
		String urlToDebug = ""; //$NON-NLS-1$
		StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append(fURL);
		queryBuilder.append("/dummy.php?start_debug=1&debug_port="); //$NON-NLS-1$
		String port = Integer.toString(PHPDebugEPLPlugin.getDebugPort(DebuggerCommunicationDaemon.ZEND_DEBUGGER_ID));

		queryBuilder.append(port);
		queryBuilder.append("&debug_fastfile=1&debug_host="); //$NON-NLS-1$

		queryBuilder.append(host + "&testConnection=true"); //$NON-NLS-1$
		urlToDebug = queryBuilder.toString();
		return urlToDebug;
	}

	private String[] getAllLocalHostsAddresses()
	{
		String hosts = PHPDebugPreferencesUtil.getString(PHPDebugCorePreferenceNames.CLIENT_IP, StringUtil.EMPTY);
		StringTokenizer tokenizer = new StringTokenizer(hosts, ", "); //$NON-NLS-1$
		List<String> list = new ArrayList<String>();
		while (tokenizer.hasMoreTokens())
		{
			list.add(tokenizer.nextToken());
		}

		return list.toArray(new String[list.size()]);
	}

	private void showSuccessMessage()
	{
		String message = PHPServerUIMessages.getString("DefaultDebugServerConnectionTest_success"); //$NON-NLS-1$
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(message);

		if (timeoutServerList.size() > 0)
		{
			stringBuilder.append("\n"); //$NON-NLS-1$
			stringBuilder.append(PHPServerUIMessages.getString("DefaultDebugServerConnectionTest_however")); //$NON-NLS-1$
			stringBuilder.append(addTimeOutsMessage("")); //$NON-NLS-1$
		}

		MessageDialog.openInformation(fShell, PHPServerUIMessages
				.getString("DefaultDebugServerConnectionTest_testDebugServer"), stringBuilder.toString()); //$NON-NLS-1$
	}

	private String addTimeOutsMessage(String message)
	{
		String result = message;
		if (timeoutServerList.size() > 0)
		{
			Iterator<String> iter = timeoutServerList.iterator();
			StringBuilder stringBuilder = new StringBuilder();
			while (iter.hasNext())
			{
				stringBuilder.append('-' + iter.next() + '\n');
			}
			stringBuilder.append(PHPServerUIMessages.getString("DefaultDebugServerConnectionTest_theClientHostIPs")); //$NON-NLS-1$
			result = result + stringBuilder.toString();
		}
		return result;
	}

	public void testEventReceived(final DebugServerTestEvent e)
	{
		fShell.getDisplay().asyncExec(new Runnable()
		{
			public void run()
			{
				removeThisListener();
				if (e.getEventType() == DebugServerTestEvent.TEST_SUCCEEDED)
				{
					isFinished = true;
					showSuccessMessage();
				}
				else
				{
					switch (e.getEventType())
					{
						case DebugServerTestEvent.TEST_FAILED_DEBUGER_VERSION:
							MessageDialog.openError(
									fShell,
									PHPServerUIMessages.getString("DefaultDebugServerConnectionTest_testDebugServer"), PHPServerUIMessages.getString("DefaultDebugServerConnectionTest_oldDebuggerVersion")); //$NON-NLS-1$ //$NON-NLS-2$
							break;
						case DebugServerTestEvent.TEST_TIMEOUT:
							showCustomErrorDialog(NLS.bind(
									PHPServerUIMessages.getString("DefaultDebugServerConnectionTest_timeOutMessage"), fURL)); //$NON-NLS-1$
							break;
					}
				}
			}
		});
	}
}
