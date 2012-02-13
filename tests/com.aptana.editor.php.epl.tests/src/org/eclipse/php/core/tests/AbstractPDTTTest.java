/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Zend Technologies
 *******************************************************************************/
package org.eclipse.php.core.tests;

import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.util.SafeRunnable;
import org.osgi.framework.Bundle;

import com.aptana.editor.php.epl.tests.Activator;

/**
 * This is an abstract test for .pdtt tests
 * 
 * @author michael
 */
public abstract class AbstractPDTTTest extends TestCase
{

	public AbstractPDTTTest()
	{
		super();
	}

	public AbstractPDTTTest(String name)
	{
		super(name);
	}

	protected static String[] getPDTTFiles(String testsDirectory)
	{
		return getPDTTFiles(testsDirectory, Activator.getDefault().getBundle());
	}

	protected static String[] getPDTTFiles(String testsDirectory, Bundle bundle)
	{
		return getFiles(testsDirectory, bundle, ".pdtt"); //$NON-NLS-1$
	}

	protected static String[] getFiles(String testsDirectory, String ext)
	{
		return getFiles(testsDirectory, Activator.getDefault().getBundle(), ext);
	}

	protected static String[] getFiles(String testsDirectory, Bundle bundle, String ext)
	{
		List<String> files = new LinkedList<String>();
		Enumeration<String> entryPaths = bundle.getEntryPaths(testsDirectory);
		if (entryPaths != null)
		{
			while (entryPaths.hasMoreElements())
			{
				final String path = (String) entryPaths.nextElement();
				URL entry = bundle.getEntry(path);
				// check whether the file is readable:
				try
				{
					entry.openStream().close();
				}
				catch (Exception e)
				{
					continue;
				}
				int pos = path.lastIndexOf('/');
				final String name = (pos >= 0 ? path.substring(pos + 1) : path);
				if (!name.endsWith(ext))
				{ // check fhe file extention
					continue;
				}
				files.add(path);
			}
		}
		return (String[]) files.toArray(new String[files.size()]);
	}

	protected void assertContents(String expected, String actual)
	{
		String diff = TestUtils.compareContents(expected, actual);
		if (diff != null)
		{
			fail(diff);
		}
	}

	protected static void safeDelete(final IResource resource) throws Exception
	{
		if (resource == null)
		{
			return;
		}
		SafeRunnable safeRunnable = new SafeRunnable("Deleting a " + resource.getName() + "...") //$NON-NLS-1$ //$NON-NLS-2$
		{
			public void handleException(Throwable e)
			{
				// Try deleting again after a a short delay
				try
				{
					Thread.sleep(1000L);
					doDelete(resource);
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
				e.printStackTrace();
			}

			public void run() throws Exception
			{
				doDelete(resource);
			}

			private void doDelete(IResource resource) throws CoreException
			{
				resource.getParent().refreshLocal(IResource.DEPTH_ONE, null);
				resource.delete(true, null);
				resource.getParent().refreshLocal(IResource.DEPTH_ONE, null);
			}
		};
		SafeRunnable.run(safeRunnable);
	}
}
