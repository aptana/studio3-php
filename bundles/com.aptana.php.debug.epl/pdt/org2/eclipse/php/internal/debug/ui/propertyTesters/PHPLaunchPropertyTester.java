/*******************************************************************************
 * Copyright (c) 2006 Zend Corporation and IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org2.eclipse.php.internal.debug.ui.propertyTesters;

import java.util.List;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.ui.part.FileEditorInput;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.php.core.PHPNature;
import com.aptana.editor.php.internal.core.IPHPConstants;
import com.aptana.php.debug.epl.PHPDebugEPLPlugin;

/**
 * A property tester for the launch shortcuts.
 * 
 * @author shalom
 */
public class PHPLaunchPropertyTester extends PropertyTester
{

	private static final Object PHP_SOURCE_ID = "org2.eclipse.php.core.phpsource";
	private static final String SCRIPT_ID = "script";
	private IContentType phpContentType = Platform.getContentTypeManager().getContentType(
			IPHPConstants.CONTENT_TYPE_HTML_PHP);

	/**
	 * Executes the property test determined by the parameter <code>property</code>.
	 * 
	 * @param receiver
	 *            the receiver of the property test
	 * @param property
	 *            the property to test
	 * @param args
	 *            additional arguments to evaluate the property. If no arguments are specified in the <code>test</code>
	 *            expression an array of length 0 is passed
	 * @param expectedValue
	 *            the expected value of the property. The value is either of type <code>java.lang.String</code> or a
	 *            boxed base type. If no value was specified in the <code>test</code> expressions then <code>null</code>
	 *            is passed
	 * @return returns <code>true<code> if the property is equal to the expected value; 
	 *  otherwise <code>false</code> is returned
	 */
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue)
	{
		if (receiver instanceof List)
		{
			List list = (List) receiver;
			if (list.size() > 0)
			{
				String launchType = args.length > 0 ? args[0].toString() : "";
				// Test only the first element
				IFile file = null;
				Object obj = list.get(0);
				if (obj instanceof FileEditorInput)
				{
					FileEditorInput editorInput = (FileEditorInput) list.get(0);
					file = editorInput.getFile();
				}
				else if (obj instanceof IFile)
				{
					file = (IFile) obj;
				}
				else if (obj instanceof IProject)
				{
					try
					{
						return ((IProject) obj).hasNature(PHPNature.NATURE_ID)
								&& !SCRIPT_ID.equalsIgnoreCase(launchType);
					}
					catch (CoreException e)
					{
						IdeLog.logError(PHPDebugEPLPlugin.getDefault(), e);
					}
				}
				else if (SCRIPT_ID.equalsIgnoreCase(launchType))
				{
					// if (obj instanceof LocalFileStorageEditorInput) {
					// // In this case, the editor input is probably an external file.
					// // Allow only script run/debug on this kind of file (internal executable launch).
					// LocalFileStorageEditorInput editorInput = (LocalFileStorageEditorInput) obj;
					// // Try to get it first from the external files registry.
					// IPath fullPath = editorInput.getStorage().getFullPath();
					// file = ExternalFilesRegistry.getInstance().getFileEntry(fullPath.toOSString());
					// if (file == null) {
					// file = ((IWorkspaceRoot) ResourcesPlugin.getWorkspace().getRoot()).getFile(fullPath);
					// }
					// } else if (obj instanceof IPathEditorInput || obj instanceof NonExistingFileEditorInput) {
					// IPath fullPath = null;
					// if (obj instanceof IPathEditorInput) {
					// fullPath = URIUtil.toPath(((IURIEditorInput) obj).getURI());
					// } else {
					// fullPath = ((NonExistingFileEditorInput) obj).getPath();
					// }
					//
					// file = ExternalFilesRegistry.getInstance().getFileEntry(fullPath.toOSString());
					// }
					// FIXME
				}
				try
				{
					// Allow only a PHP Script launch shortcut in case the file is part of a non-PHP project.
					if (file != null && file.getProject() != null && !file.getProject().hasNature(PHPNature.NATURE_ID)
							&& !SCRIPT_ID.equalsIgnoreCase(launchType))
					{
						return false;
					}
					if (file != null)
					{
						if (phpContentType.isAssociatedWith(file.getName()))
						{
							return true;
						}
					}
				}
				catch (CoreException ce)
				{
				}
				try
				{
					if (file == null)
					{
						return true;
					}
					return file.getContentDescription().getContentType().getId().equals(PHP_SOURCE_ID);
				}
				catch (ResourceException re)
				{
					return false;
				}
				catch (Exception e)
				{
				}
			}
		}
		return false;
	}
}
