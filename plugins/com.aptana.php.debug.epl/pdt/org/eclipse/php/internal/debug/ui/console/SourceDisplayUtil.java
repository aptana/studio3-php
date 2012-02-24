package org.eclipse.php.internal.debug.ui.console;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import com.aptana.php.debug.epl.PHPDebugEPLPlugin;


/**
 * @author Pavel Petrochenko
 */
public final class SourceDisplayUtil
{
	private SourceDisplayUtil()
	{
	}

	/**
	 * getEditorInput
	 * 
	 * @param element
	 * @return IEditorInput
	 */
	public static IEditorInput getEditorInput(Object element)
	{
		if (element instanceof IFile)
		{
			return new FileEditorInput((IFile) element);
		}
		if (element instanceof File){
			return new FileStoreEditorInput(EFS.getLocalFileSystem().fromLocalFile((File) element));
		}
		if (element instanceof String) { // Called when the PHPFileLink is invoked
			IResource file = ResourcesPlugin.getWorkspace().getRoot().findMember(element.toString());
			if (file != null) {
				if (file.getType() == IResource.FILE && file.isAccessible()) {
					return new FileEditorInput((IFile)file);
				}
				PHPDebugEPLPlugin.logError("Could not get the editor input. File does not exist or not accessible"); //$NON-NLS-1$
			}
		}
		return null;
	}

	/**
	 * getEditorId
	 * 
	 * @param input
	 * @param element
	 * @return String
	 */
	public static String getEditorId(IEditorInput input, Object element)
	{
		try
		{
			IEditorDescriptor descriptor = IDE.getEditorDescriptor(input.getName());
			return descriptor.getId();
		}
		catch (PartInitException e)
		{
			return null;
		}
	}

	/**
	 * openInEditor
	 * 
	 * @param input
	 * @param lineNumber
	 * @throws PartInitException
	 */
	public static void openInEditor(IEditorInput input, int lineNumber) throws PartInitException
	{
		openInEditor(getActivePage(), input, lineNumber);
	}

	/**
	 * openInEditor
	 * 
	 * @param page
	 * @param input
	 * @param lineNumber
	 * @throws PartInitException
	 */
	public static void openInEditor(IWorkbenchPage page, IEditorInput input, int lineNumber) throws PartInitException
	{
		IEditorPart editorPart = IDE.openEditor(page, input, getEditorId(input, null));
		revealLineInEditor(editorPart, lineNumber);
	}

	/**
	 * revealLineInEditor
	 * 
	 * @param editorPart
	 * @param lineNumber
	 */
	public static void revealLineInEditor(IEditorPart editorPart, int lineNumber)
	{
		if (lineNumber > 0)
		{
			ITextEditor textEditor = null;
			if (editorPart instanceof ITextEditor)
			{
				textEditor = (ITextEditor) editorPart;
			}
			else
			{
				textEditor = (ITextEditor) editorPart.getAdapter(ITextEditor.class);
			}
			if (textEditor != null)
			{
				IDocumentProvider provider = textEditor.getDocumentProvider();
				IDocument document = provider.getDocument(textEditor.getEditorInput());
				try
				{
					IRegion line = document.getLineInformation(lineNumber - 1); // documents start at 0
					textEditor.selectAndReveal(line.getOffset(), line.getLength());
				}
				catch (BadLocationException e)
				{
				}
				finally
				{
					provider.disconnect(document);
				}
			}
		}
		IWorkbenchPage page = editorPart.getSite().getPage();
		if (!page.isPartVisible(editorPart))
		{
			page.activate(editorPart);
		}
	}

	/**
	 * findEditor
	 * 
	 * @param input
	 * @return IEditorPart
	 */
	public static IEditorPart findEditor(IEditorInput input)
	{
		return getActivePage().findEditor(input);
	}

	private static IWorkbenchPage getActivePage()
	{
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}
}
