/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.ui.actions.breakpoints;

import org.eclipse.osgi.util.NLS;

/**
 * @author Shalom
 *
 */
public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.aptana.php.debug.ui.actions.breakpoints.messages"; //$NON-NLS-1$
	public static String PHPBreakpointPropertiesRulerAction_actionText;
	public static String ToggleBreakpointAdapter_toggleBreakpointJobName;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
