/**
 * This file Copyright (c) 2005-2008 Aptana, Inc. This program is
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

import com.aptana.editor.php.PHPEditorPlugin;

/**
 * 
 * @author Pavel Petrochenko
 *
 */
public class PHPLibrary  implements IPHPLibrary{

	private final IConfigurationElement element;

	public PHPLibrary(IConfigurationElement element) {
		this.element = element;
		//super(element);
	}

	public File getPath() {
		String attribute = element.getAttribute("path");
		String namespaceIdentifier = element.getNamespaceIdentifier();
		Bundle bundle = Platform.getBundle(namespaceIdentifier);
		if (bundle!=null) {
			File bundleFile;
			try {
				bundleFile = getBundleFile(bundle);
			} catch (IOException e) {
				PHPEditorPlugin.logError(e);
				return null;
			}
			return new File(bundleFile,attribute);
		}
		return null;
	}
	/**
	 * Returns a file for the contents of the specified bundle.  Depending 
	 * on how the bundle is installed the returned file may be a directory or a jar file 
	 * containing the bundle content.  
	 * 
	 * @param bundle the bundle
	 * @return a file with the contents of the bundle
	 * @throws IOException if an error occurs during the resolution
	 * 
	 * @since org.eclipse.equinox.common 3.4
	 */
	public static File getBundleFile(Bundle bundle) throws IOException {
		URL rootEntry = bundle.getEntry("/"); //$NON-NLS-1$
		rootEntry = FileLocator.resolve(rootEntry);
		if ("file".equals(rootEntry.getProtocol())) //$NON-NLS-1$
			return new File(rootEntry.getPath());
		if ("jar".equals(rootEntry.getProtocol())) { //$NON-NLS-1$
			String path = rootEntry.getPath();
			if (path.startsWith("file:")) {
				// strip off the file: and the !/
				path = path.substring(5, path.length() - 2);
				return new File(path);
			}
		}
		throw new IOException("Unknown protocol"); //$NON-NLS-1$
	}

	public boolean isTurnedOn() {
		return LibraryManager.getInstance().isTurnedOn(this);
	}

	public String getName() {
		return element.getAttribute("name")+"(built-in)";
	}

	public URL getIcon() {
		String namespaceIdentifier = element.getNamespaceIdentifier();
		Bundle bundle = Platform.getBundle(namespaceIdentifier);
		return bundle.getResource(element.getAttribute("icon"));
	}

	public List<String> getDirectories() {
		return Collections.singletonList(getPath().getAbsolutePath());
	}

	@Override
	public String getId()
	{
		return element.getAttribute("id");
	}
}
