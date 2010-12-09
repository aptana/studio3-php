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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;

import com.aptana.debug.php.PHPDebugPlugin;

/**
 * A class that is intended to test a tunnel connection.
 * 
 * @author Shalom Gibly
 */
public class TunnelTester
{

	public static final int PASSWORD_CHANGED_CODE = SSHTunnel.CONNECTION_PASSWORD_CHANGED_CODE;

	/**
	 * Test SSH tunnel connection. This test will try to establish a connection using a SSHTunnel. In case successful,
	 * the connection will be closed at the end of the test. There are several return values possibilities to this test,
	 * which are derived from the {@link SSHTunnel#connect()} method:<br>
	 * <ul>
	 * <li>Status OK - Signals that the connection was successful with no errors or warnings</li>
	 * <li>Status ERROR - Signals that the connection was unsuccessful</li>
	 * <li>Status WARNING - Signals that the connection was successful, however there are a few warning notifications
	 * that should be reviewed</li>
	 * <li>Status INFO - Signals that the connection was successful, however there was a modification to the connection
	 * data that is expressed in the INFO code (such as a password change data)</li>
	 * </ul>
	 * <br>
	 * A MultiStatus will be returned in case the connection state could not be determined (the tunnel connection did
	 * not indicate a fatal error, however, the SSH connection returned false for an isConnected query).
	 * 
	 * @param remoteHost
	 * @param userName
	 * @param password
	 * @param localPort
	 * @param remotePort
	 * @return The IStatus for the connection creation.
	 * @see SSHTunnel#connent
	 */
	public static IStatus test(String remoteHost, String userName, String password, int localPort, int remotePort)
	{
		SSHTunnel sshTunnel = SSHTunnelFactory.getSSHTunnel(remoteHost, userName, password, localPort, remotePort,
				false);
		IStatus connectionResult = sshTunnel.connect();
		if (connectionResult.getSeverity() != IStatus.ERROR)
		{
			if (sshTunnel.isConnected())
			{
				sshTunnel.disconnect();
				return connectionResult;
			}
			else
			{
				sshTunnel.disconnect();
				MultiStatus status = new MultiStatus(PHPDebugPlugin.PLUGIN_ID, 0, Messages.TunnelTester_testMessage,
						null);
				status.add(connectionResult); // add any other statuses into the
				// multi-status, so we can track a password change, for example.
				return status;
			}
		}
		return connectionResult;
	}
}
