/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.util;

import java.io.File;
import java.net.URI;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.progress.UIJob;
import org.osgi.service.prefs.BackingStoreException;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.EclipseUtil;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.internal.ui.editor.PHPSourceEditor;
import com.aptana.theme.Theme;
import com.aptana.theme.ThemePlugin;

public class EditorUtils
{
	/**
	 * Open a file in an editor and return the opened editor part.<br>
	 * This method will try to open the file in an internal editor, unless there is no editor descriptor assigned to
	 * that file type.
	 * 
	 * @param file
	 * @return The {@link IEditorPart} that was created when the file was opened; Return null in case of an error
	 */
	public static IEditorPart openInEditor(File file)
	{
		if (file == null)
		{
			IdeLog.logError(PHPEditorPlugin.getDefault(),
					"Error open a file in the editor", new IllegalArgumentException("file is null")); //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		}
		try
		{
			URI uri = file.toURI();
			IEditorDescriptor desc = getEditorDescriptor(uri);
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			if (desc == null)
			{
				return IDE.openEditor(page, uri, IEditorRegistry.SYSTEM_EXTERNAL_EDITOR_ID, true);
			}
			else
			{
				return IDE.openEditor(page, uri, desc.getId(), true);
			}
		}
		catch (Exception e)
		{
			IdeLog.logError(PHPEditorPlugin.getDefault(), "Error open a file in the editor", e); //$NON-NLS-1$
		}
		return null;
	}

	/**
	 * Returns the editor descriptor for the given URI. The editor descriptor is computed by the last segment of the URI
	 * (the file name).
	 * 
	 * @param uri
	 *            A file URI
	 * @return the descriptor of the default editor, or null if not found
	 */
	public static IEditorDescriptor getEditorDescriptor(URI uri)
	{
		IEditorRegistry editorReg = PlatformUI.getWorkbench().getEditorRegistry();
		if (uri.getPath() == null || uri.getPath().equals("/") || uri.getPath().trim().equals("")) //$NON-NLS-1$ //$NON-NLS-2$
			return null;
		IPath path = new Path(uri.getPath());
		return editorReg.getDefaultEditor(path.lastSegment());
	}

	/**
	 * Update the PHP Occurrences colors to match the Theme's colors.
	 */
	public static void setOccurrenceColors()
	{
		Job job = new UIJob("Setting occurrence colors") //$NON-NLS-1$
		{
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				IEclipsePreferences prefs = EclipseUtil.instanceScope().getNode("org.eclipse.ui.editors"); //$NON-NLS-1$
				Theme theme = ThemePlugin.getDefault().getThemeManager().getCurrentTheme();
				prefs.put("PHPReadOccurrenceIndicationColor", StringConverter.asString(theme.getSearchResultColor())); //$NON-NLS-1$
				prefs.put("PHPWriteOccurrenceIndicationColor", StringConverter.asString(theme.getSearchResultColor())); //$NON-NLS-1$
				try
				{
					prefs.flush();
				}
				catch (BackingStoreException e) // $codepro.audit.disable emptyCatchClause
				{
					// ignore
				}
				return Status.OK_STATUS;
			}
		};
		EclipseUtil.setSystemForJob(job);
		job.setPriority(Job.LONG);
		job.schedule();
	}

	/**
	 * Returns the PHP source editor that match the given ITextViewer document.
	 * 
	 * @return the matching PHP source editor; Null, if none was found.
	 */
	public static final PHPSourceEditor getPHPSourceEditor(final ITextViewer viewer)
	{
		Display display = Display.getCurrent();
		if (display == null)
		{
			display = Display.getDefault();
		}
		final PHPSourceEditor[] editorResult = new PHPSourceEditor[1];
		display.syncExec(new Runnable()
		{
			public void run()
			{
				IWorkbench workbench = PlatformUI.getWorkbench();
				IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
				IEditorPart activeEditor = page.getActiveEditor();
				if (isSameDocument(viewer, activeEditor))
				{
					editorResult[0] = (PHPSourceEditor) activeEditor;
				}

				// Check for any other open editor in the workspace
				IEditorReference[] references = page.getEditorReferences();
				for (int i = 0; i < references.length; i++)
				{
					IEditorReference reference = references[i];
					IEditorPart editor = reference.getEditor(false);
					if (activeEditor != editor && isSameDocument(viewer, editor))
					{
						editorResult[0] = (PHPSourceEditor) editor;
					}
				}
			}
		});

		return editorResult[0];
	}

	/*
	 * Check if the given text viewer and the given editor are referring to the same IDocument.
	 */
	private static boolean isSameDocument(final ITextViewer viewer, IEditorPart editor)
	{
		return editor instanceof PHPSourceEditor
				&& ((PHPSourceEditor) editor).getDocumentProvider().getDocument(editor.getEditorInput()) == viewer
						.getDocument();
	}
}
