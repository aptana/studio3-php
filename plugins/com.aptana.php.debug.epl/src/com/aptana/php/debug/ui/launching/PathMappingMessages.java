/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.ui.launching;

import org.eclipse.osgi.util.NLS;

/**
 * @author Shalom
 */
public class PathMappingMessages extends NLS
{
	private static final String BUNDLE_NAME = "com.aptana.php.debug.ui.launching.path_mapping_messages"; //$NON-NLS-1$

	public static String PathMappingConfigurationTab_changeMappingLink;
	public static String PathMappingConfigurationTab_mappingTableDescription;
	public static String PathMappingConfigurationTab_noServersDialogMessage;
	public static String PathMappingConfigurationTab_noServersDialogTitle;
	public static String PathMappingConfigurationTab_tabName;

	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, PathMappingMessages.class);
	}

	private PathMappingMessages()
	{
	}
}
