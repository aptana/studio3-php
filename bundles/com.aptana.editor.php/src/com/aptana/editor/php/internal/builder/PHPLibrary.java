/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.builder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.php.PHPEditorPlugin;

/**
 * @author Pavel Petrochenko
 */
public class PHPLibrary implements IPHPLibrary
{

	private final IConfigurationElement element;

	public PHPLibrary(IConfigurationElement element)
	{
		this.element = element;
		// super(element);
	}

	public File getPath()
	{
		String attribute = element.getAttribute("path"); //$NON-NLS-1$
		String namespaceIdentifier = element.getNamespaceIdentifier();
		Bundle bundle = Platform.getBundle(namespaceIdentifier);
		if (bundle != null)
		{
			File bundleFile;
			try
			{
				bundleFile = FileLocator.getBundleFile(bundle);
			}
			catch (IOException e)
			{
				IdeLog.logError(PHPEditorPlugin.getDefault(), "Error locating the bundle file for a PHP library", e); //$NON-NLS-1$
				return null;
			}
			return new File(bundleFile, attribute);
		}
		return null;
	}

	public boolean isTurnedOn()
	{
		return LibraryManager.getInstance().isTurnedOn(this);
	}

	public String getName()
	{
		return element.getAttribute("name") + "(built-in)"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public URL getIcon()
	{
		String namespaceIdentifier = element.getNamespaceIdentifier();
		Bundle bundle = Platform.getBundle(namespaceIdentifier);
		return bundle.getResource(element.getAttribute("icon")); //$NON-NLS-1$
	}

	public List<String> getDirectories()
	{
		return Collections.singletonList(getPath().getAbsolutePath());
	}

	public String getId()
	{
		return element.getAttribute("id"); //$NON-NLS-1$
	}
}
