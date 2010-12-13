/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org.eclipse.php.internal.debug.core.zend.debugger;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.php.debug.core.debugger.parameters.IDebugParametersInitializer;
import org.eclipse.php.debug.core.debugger.parameters.IWebDebugParametersInitializer;
import org.eclipse.php.internal.debug.core.IPHPDebugConstants;
import org.eclipse.php.internal.debug.core.Logger;
import org.eclipse.php.internal.debug.core.PHPDebugCoreMessages;
import org.eclipse.php.internal.debug.core.launching.PHPLaunchUtilities;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;

import com.aptana.debug.php.core.launch.remote.RemoteDebugRedirector;
import com.aptana.debug.php.core.util.NameValuePair;
import com.aptana.debug.php.epl.PHPDebugEPLPlugin;

/**
 * A debug session initializer.
 * There are two ways to initialize debug session (configurable in launch configuration dialog):
 * 	<ul>
 * 	<li>Open an internal Web browser that will issue a request to the debug server </li>
 *  <li>Use URL connection to send the request to the the debug server directly </li>
 *  </ul> 
 */
public class PHPWebServerDebuggerInitializer implements IDebuggerInitializer {

	public void debug(ILaunch launch) throws DebugException {

		IDebugParametersInitializer parametersInitializer = DebugParametersInitializersRegistry.getBestMatchDebugParametersInitializer(launch);

		boolean openInBrowser = false;
		boolean useInternalBrowser = false;
		try {
			openInBrowser = launch.getLaunchConfiguration().getAttribute(IPHPDebugConstants.OPEN_IN_BROWSER, false);
			useInternalBrowser = launch.getLaunchConfiguration().getAttribute(IPHPDebugConstants.USE_INTERNAL_BROWSER, false);
		} catch (CoreException e) {
		}
		if (openInBrowser) {
			openBrowser(launch, parametersInitializer, useInternalBrowser);
		} else {
			openUrlConnection(launch, parametersInitializer);
		}
	}

	/**
	 * Start the debug session by openning a browser that will actually trigger the URL connection
	 * to the debug server.
	 * @param launch
	 * @param parametersInitializer
	 * @param useInternalBrowser 
	 * @throws DebugException
	 */
	protected void openBrowser(final ILaunch launch, IDebugParametersInitializer parametersInitializer, final boolean useInternalBrowser) throws DebugException {

		boolean runWithDebug = true;
		try {
			runWithDebug = launch.getLaunchConfiguration().getAttribute(IPHPDebugConstants.RUN_WITH_DEBUG_INFO, true);
		} catch (CoreException e) {
			// nothing to do
		}

		URL requestURL = parametersInitializer.getRequestURL(launch);
		
		// First, build the basic GET query that will be used
		List getData = Collections.EMPTY_LIST;
		try
		{
			getData = launch.getLaunchConfiguration().getAttribute(IPHPDebugConstants.ATTR_HTTP_GET, Collections.EMPTY_LIST);
		}
		catch (CoreException e1)
		{
			Logger.logException(e1);
		}
		StringBuilder urlQuery = new StringBuilder();
		if (requestURL.getQuery() != null)
		{
			urlQuery.append('?');
			urlQuery.append(requestURL.getQuery());
		}
		if (!getData.isEmpty())
		{
			if (urlQuery.length() > 0)
			{
				urlQuery.append('&');
			}
			else
			{
				urlQuery.append('?');
			}
			try
			{
				fillParams(getData, urlQuery);
			}
			catch (UnsupportedEncodingException e)
			{
				Logger.logException(e);
				String errorMessage = PHPDebugCoreMessages.Debugger_Unexpected_Error_1;
				throw new DebugException(new Status(IStatus.ERROR, PHPDebugEPLPlugin.PLUGIN_ID, IPHPDebugConstants.INTERNAL_ERROR, errorMessage, e));
			}
		}
		
		// In case we are in a Debug/Profile mode, attache the additional query string to the GET request
		try {
			if (runWithDebug && !ILaunchManager.RUN_MODE.equals(launch.getLaunchMode())) {
				String query = PHPLaunchUtilities.generateQuery(launch, parametersInitializer);
				if (query != null && query.length() > 0)
				{
					if (urlQuery.length() > 0)
					{
						urlQuery.append('&');
						urlQuery.append(query);
					}
					else
					{
						urlQuery.append('?');
						urlQuery.append(query);
					}
				}
			}
			requestURL = new URL(requestURL.getProtocol(), requestURL.getHost(), requestURL.getPort(), requestURL.getPath() + urlQuery.toString());
		} catch (MalformedURLException e) {
			Logger.logException(e);
			String errorMessage = PHPDebugCoreMessages.Debugger_Unexpected_Error_1;
			throw new DebugException(new Status(IStatus.ERROR, PHPDebugEPLPlugin.PLUGIN_ID, IPHPDebugConstants.INTERNAL_ERROR, errorMessage, e));
		}
		final DebugException[] exception = new DebugException[1];
		final URL debugURL = requestURL;
		
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				try
				{
					List postData = launch.getLaunchConfiguration().getAttribute(IPHPDebugConstants.ATTR_HTTP_POST, Collections.EMPTY_LIST);
					boolean hasPostData = (postData != null && !postData.isEmpty());
					IWebBrowser browser = null;
					if (useInternalBrowser)
					{
						StringBuilder browserTitle = new StringBuilder(debugURL.getProtocol())
							.append("://")
							.append(debugURL.getHost());
						
						if (debugURL.getPort() != -1)
						{
							browserTitle.append(':').append(debugURL.getPort());
						}
						browserTitle.append(debugURL.getPath());
						browser = PHPLaunchUtilities.openInternalBrowser(browserTitle.toString());
						// Clear the all session data from the internal browsers. Note that this one clear everything! But it's needed for an accurate functionality when we execute a run
						// after a debug.
						Browser.clearSessions();
					}
					else
					{
						browser = PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser();
					}
					// Set the URL target into the browser
					if (hasPostData)
					{
						new RemoteDebugRedirector().redirect(browser, debugURL, postData);
					}
					else
					{
						
						browser.openURL(debugURL);
					}
					if (PHPDebugEPLPlugin.DEBUG)
					{
						System.out.println("Opening URL in a browser: " + debugURL.toString());
					}
				}
				catch (Throwable t)
				{
					Logger.logException("Error initializing the web browser.", t);
					String errorMessage = PHPDebugCoreMessages.Debugger_Unexpected_Error_1;
					exception[0] = new DebugException(new Status(IStatus.ERROR, PHPDebugEPLPlugin.PLUGIN_ID, IPHPDebugConstants.INTERNAL_ERROR, errorMessage, t));
				}
			}
		});
		if (exception[0] != null) {
			throw exception[0];
		}
	}

	/**
	 * Issue the request to the debug server using URL connection mechanism
	 * @param launch
	 * @param parametersInitializer
	 * @throws DebugException
	 */
	protected void openUrlConnection(ILaunch launch, IDebugParametersInitializer parametersInitializer) throws DebugException {
		URL requestURL = parametersInitializer.getRequestURL(launch);
		try {
			// We only support this kind of debug session initializer here:
			if (parametersInitializer instanceof IWebDebugParametersInitializer) {
				IWebDebugParametersInitializer webParametersInitializer = (IWebDebugParametersInitializer) parametersInitializer;

				StringBuilder getParams = new StringBuilder();
				
				// Initialize with additional GET parameters
				// Aptana Mod - SG: Replace PDT's implementation. This one will not work with existing GET parameters that were manually entered into the target URL
				// Also, add the debug parameter AFTER the script parameters to allow a normal flow of the PHP application.
				List getRequestParameters = launch.getLaunchConfiguration().getAttribute(IPHPDebugConstants.ATTR_HTTP_GET, Collections.EMPTY_LIST);
				String queryInURL = requestURL.getQuery();
				if (queryInURL != null)
				{
					getParams.append(queryInURL);
					getParams.append('&');
				}
				fillParams(getRequestParameters, getParams);
				getParams.append('&');
				
				// Add the Zend debug parameters
				Hashtable<String, String> debugParameters = parametersInitializer.getDebugParameters(launch);
				if (debugParameters != null && !debugParameters.isEmpty()) {
					Enumeration<String> k = debugParameters.keys();
					while (k.hasMoreElements()) {
						String key = k.nextElement();
						String value = debugParameters.get(key);
						getParams.append(URLEncoder.encode(key, IPHPDebugConstants.URL_ENCODING)).append('=').append(URLEncoder.encode(value, IPHPDebugConstants.URL_ENCODING));
						if (k.hasMoreElements()) {
							getParams.append('&');
						}
					}
				}
				String paramsString = getParams.toString();
				if (paramsString.length() != 0)
				{
					paramsString = '?' + paramsString;
				}
				if (paramsString.endsWith("&"))
				{
					paramsString = paramsString.substring(0, paramsString.length() - 1);
				}
				requestURL = new URL(requestURL.getProtocol(), requestURL.getHost(), requestURL.getPort(), requestURL.getPath() + paramsString);
				
				// Open the connection:
				if (PHPDebugEPLPlugin.DEBUG) {
					System.out.println("Opening URL connection: " + requestURL.toString());
				}
				HttpURLConnection urlConection = (HttpURLConnection) requestURL.openConnection();
				urlConection.setDoInput(true);
				urlConection.setDoOutput(true);

				//if (requestMethod != null) {
			    //	urlConection.setRequestMethod("GET");
				//}

				// Add additional headers
				Hashtable<String, String> headers = webParametersInitializer.getRequestHeaders(launch);
				if (headers != null) {
					Enumeration<String> k = headers.keys();
					while (k.hasMoreElements()) {
						String key = k.nextElement();
						String value = URLEncoder.encode(headers.get(key), IPHPDebugConstants.URL_ENCODING);
						if (PHPDebugEPLPlugin.DEBUG) {
							System.out.println("Adding HTTP header: " + key + "=" + value);
						}
						urlConection.addRequestProperty(key, value);
					}
				}

				// Set cookies
				Hashtable<String, String> cookies = webParametersInitializer.getRequestCookies(launch);
				if (cookies != null) {
					StringBuilder cookieBuf = new StringBuilder();
					Enumeration<String> k = cookies.keys();
					while (k.hasMoreElements()) {
						String key = k.nextElement();
						String value = cookies.get(key);
						cookieBuf.append(URLEncoder.encode(key, IPHPDebugConstants.URL_ENCODING)).append('=').append(URLEncoder.encode(value, IPHPDebugConstants.URL_ENCODING));
						if (k.hasMoreElements()) {
							cookieBuf.append("; ");
						}
					}
					if (PHPDebugEPLPlugin.DEBUG) {
						System.out.println("Setting cookies: " + cookieBuf.toString());
					}
					urlConection.addRequestProperty("Cookie", cookieBuf.toString());
				}

				DataOutputStream outputStream = new DataOutputStream(urlConection.getOutputStream());
				try {
					// Aptana Mod - SG: Replace PDT's implementation for the POST collection.
					// We collect it from the configuration dialog settings.
					List postRequestParameters = launch.getLaunchConfiguration().getAttribute(IPHPDebugConstants.ATTR_HTTP_POST, Collections.EMPTY_LIST);
					// Initialize with additional POST parameters
					StringBuilder postParams = new StringBuilder();
					fillParams(postRequestParameters, postParams);
					if (postParams.length() > 0)
					{
						outputStream.writeBytes(postParams.toString());
					}

					// Add raw data
					String rawData = webParametersInitializer.getRequestRawData(launch);
					if (rawData != null) {
						outputStream.writeBytes(rawData);
					}
				} finally {
					outputStream.flush();
					outputStream.close();
				}

				String headerKey = urlConection.getHeaderFieldKey(1);
				if (headerKey == null) {
					Logger.log(Logger.WARNING, "No HeaderKey returned by server. Most likely not started");
					String errorMessage = PHPDebugCoreMessages.DebuggerConnection_Problem_1;
					throw new DebugException(new Status(IStatus.ERROR, PHPDebugEPLPlugin.PLUGIN_ID, IPHPDebugConstants.INTERNAL_ERROR, errorMessage, null));
				}

				for (int i = 1; (headerKey = urlConection.getHeaderFieldKey(i)) != null; i++) {
					if (headerKey.equals("X-Zend-Debug-Server")) {
						String headerValue = urlConection.getHeaderField(headerKey);
						if (!headerValue.equals("OK")) {
							Logger.log(Logger.WARNING, "Unexpected Header Value returned by Server. " + headerValue);
							String errorMessage = PHPDebugCoreMessages.DebuggerConnection_Problem_2 + " - " + headerValue;
							throw new DebugException(new Status(IStatus.ERROR, PHPDebugEPLPlugin.PLUGIN_ID, IPHPDebugConstants.INTERNAL_ERROR, errorMessage, null));
						}
						break;
					}
				}

				InputStream inputStream = urlConection.getInputStream();
				while (inputStream.read() != -1) {
					// do nothing on the content returned by standard stream
				}
				inputStream.close();
			}
		} catch (UnknownHostException e) {
			Logger.logException("Unknown host: " + requestURL.getHost(), e);
		} catch (ConnectException e) {
			Logger.logException("Unable to connect to URL " + requestURL, e);
		} catch (IOException e) {
			Logger.logException("Unable to connect to URL " + requestURL, e);
		} catch (Exception e) {
			Logger.logException("Unexpected exception communicating with Web server", e);
			String errorMessage = e.getMessage();
			throw new DebugException(new Status(IStatus.ERROR, PHPDebugEPLPlugin.PLUGIN_ID, IPHPDebugConstants.INTERNAL_ERROR, errorMessage, e));
		}
	}

	/**
	 * Parse the GET/POST parameters from the given list and inject them into the given paramsBuilder.
	 * WE expect for the list to have elements with a toString value of 'x=y'. 
	 * The params will be parsed, URLEncoded, and inserted into the StringBuilder with a separating ampersand char.
	 * 
	 * @param requestParams
	 * @param builtParams
	 * @throws UnsupportedEncodingException
	 */
	private void fillParams(List requestParams, StringBuilder paramsBuilder) throws UnsupportedEncodingException
	{
		int size;
		size = requestParams.size();
		for (int i = 0; i < size ; i++)
		{
			NameValuePair nameValuePair = NameValuePair.fromPairString(requestParams.get(i).toString());
			if (nameValuePair != null)
			{
				paramsBuilder.append(URLEncoder.encode(nameValuePair.name, IPHPDebugConstants.URL_ENCODING))
						.append('=')
						.append(URLEncoder.encode(nameValuePair.value, IPHPDebugConstants.URL_ENCODING));
				if (i + 1 < size)
				{
					paramsBuilder.append('&');
				}
			}
		}
	}
}