package com.aptana.editor.php.util;

import java.io.File;
import java.net.URI;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.internal.ui.editor.PHPSourceEditor;

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
			PHPEditorPlugin.logError(new IllegalArgumentException("The file cannot be null")); //$NON-NLS-1$
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
			PHPEditorPlugin.logError(e);
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
	 * Returns the PHP source editor that match the given ITextViewer document.
	 * 
	 * @return the matching PHP source editor; Null, if none was found.
	 */
	public static final PHPSourceEditor getPHPSourceEditor(final ITextViewer viewer)
	{
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorPart activeEditor = page.getActiveEditor();
		if (isSameDocument(viewer, activeEditor))
		{
			return (PHPSourceEditor) activeEditor;
		}

		// Check for any other open editor in the workspace
		IEditorReference[] references = page.getEditorReferences();
		for (int i = 0; i < references.length; i++)
		{
			IEditorReference reference = references[i];
			IEditorPart editor = reference.getEditor(false);
			if (activeEditor != editor && isSameDocument(viewer, editor))
			{
				return (PHPSourceEditor) editor;
			}
		}
		return null;
	}

	/*
	 * Check if the given text viewer and the given editor are referring to the same IDocument.
	 */
	private static boolean isSameDocument(final ITextViewer viewer, IEditorPart editor)
	{
		return editor instanceof PHPSourceEditor
				&& ((PHPSourceEditor) editor).getDocumentProvider().getDocument(editor.getEditorInput()) == viewer.getDocument();
	}
}
