/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.core.tunneling;

import org.eclipse.osgi.util.NLS;

/**
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.aptana.php.debug.core.tunneling.messages"; //$NON-NLS-1$
	public static String SSHTunnel_nonInitializedTunnelWarning;
	public static String SSHTunnel_tunnelConnectionFailError;
	public static String SSHTunnel_tunnelFailRetryError;
	public static String TunnelTester_testMessage;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
