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
package org2.eclipse.php.internal.debug.ui.breakpoint.provider;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org2.eclipse.php.internal.core.documentModel.parser.regions.PHPRegionTypes;
import org2.eclipse.php.internal.debug.core.IPHPDebugConstants;
import org2.eclipse.php.internal.debug.core.sourcelookup.containers.LocalFileStorage;
import org2.eclipse.php.internal.debug.core.zend.model.PHPDebugTarget;
import org2.eclipse.php.internal.debug.ui.PHPDebugUIMessages;

import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.editor.common.contentassist.ILexemeProvider;
import com.aptana.editor.common.text.rules.CompositePartitionScanner;
import com.aptana.editor.php.internal.contentAssist.PHPTokenType;
import com.aptana.editor.php.internal.contentAssist.ParsingUtils;
import com.aptana.editor.php.internal.core.IPHPConstants;
import com.aptana.parsing.lexer.Lexeme;
import com.aptana.php.debug.epl.PHPDebugEPLPlugin;
import com.aptana.ui.util.StatusLineMessageTimerManager;

@SuppressWarnings("unchecked")
public class PHPBreakpointProvider implements IExecutableExtension
{

	public IStatus addBreakpoint(AbstractThemeableEditor unifiedEditor, IEditorInput input, int editorLineNumber,
			int offset) throws CoreException
	{
		IEditorInput editorInput = unifiedEditor.getEditorInput();
		if (!(editorInput instanceof IFileEditorInput))
		{
			final Display display = unifiedEditor.getISourceViewer().getTextWidget().getDisplay();
			display.asyncExec(new Runnable()
			{
				public void run()
				{
					MessageDialog.openInformation(display.getActiveShell(), "Aptana PHP Debugger",
							"The file you are trying to place a breakpoint on is an external (non-workspace) file. "
									+ "\nBreakpoints are supported for PHP files that are located inside projects.");
				}

			});
			return Status.OK_STATUS;
		}
		// check if there is a valid position to set breakpoint
		int pos = getValidPosition(unifiedEditor, editorLineNumber);
		IDocument doc = unifiedEditor.getISourceViewer().getDocument();
		// calculate the line number here so both workspace files AND externals will get it
		int originalLineNumber = editorLineNumber + 1;
		try
		{
			if (pos > -1)
			{
				editorLineNumber = doc.getLineOfOffset(pos) + 1;
			}
			else
			{
				return null;
			}
		}
		catch (BadLocationException e)
		{
			PHPDebugEPLPlugin.logError(e);
			return new Status(IStatus.ERROR, PHPDebugEPLPlugin.PLUGIN_ID, IStatus.ERROR,
					"Invalid breakpoint locationgetRawPath();", null);
		}
		IStatus status = null;
		IBreakpoint point = null;
		if (pos >= 0)
		{
			IResource res = getResourceFromInput(input);
			if (res != null && (input instanceof IFileEditorInput))
			{
				Integer lineNumberInt = Integer.valueOf(editorLineNumber);
				Integer originalLineNumberInt = Integer.valueOf(originalLineNumber);
				IMarker[] breakpoints = res.findMarkers(IBreakpoint.LINE_BREAKPOINT_MARKER, true, IResource.DEPTH_ZERO);
				if (breakpoints.length > 0)
				{
					boolean found = false;
					for (int i = 0; i < breakpoints.length; ++i)
					{
						Object object = breakpoints[i].getAttributes().get("lineNumber");
						if (object.equals(lineNumberInt) || object.equals(originalLineNumberInt))
						{
							breakpoints[i].delete();
							found = true;
						}
					}
					if (found)
					{
						return null;
					}
				}
				point = PHPDebugTarget.createBreakpoint(res, editorLineNumber);
			}
			else if (input instanceof IPathEditorInput)
			{
				Map<String, String> attributes = new HashMap<String, String>();
				IPathEditorInput pa = (IPathEditorInput) input;
				String pathName = pa.getPath().toOSString();
				// if (input instanceof IPlatformIndependentPathEditorInput) {
				// pathName = ((IPlatformIndependentPathEditorInput)input).getPath();
				// } else if (input instanceof IURIEditorInput) {
				// if (res instanceof ExternalFileWrapper) {
				// pathName = res.getFullPath().toOSString();
				// } else {
				// pathName = URIUtil.toPath(((IURIEditorInput) input).getURI()).toOSString();
				// }
				// } else {
				// pathName = ((NonExistingPHPFileEditorInput) input).getPath().toString();
				// }
				if (res instanceof IWorkspaceRoot)
				{
					// We are dealing with remote
					attributes.put(IPHPDebugConstants.STORAGE_TYPE, IPHPDebugConstants.STORAGE_TYPE_REMOTE);
				}
				else
				{
					// We are dealing with storage
					attributes.put(IPHPDebugConstants.STORAGE_TYPE, IPHPDebugConstants.STORAGE_TYPE_EXTERNAL);
				}
				attributes.put(IPHPDebugConstants.STORAGE_FILE, pathName);
				attributes.put(IPHPDebugConstants.SECONDARY_ID_KEY, pathName);

				Integer lineNumberInt = new Integer(editorLineNumber);
				IMarker[] breakpoints = res.findMarkers(IBreakpoint.LINE_BREAKPOINT_MARKER, true, IResource.DEPTH_ZERO);
				boolean found = false;
				for (int i = 0; i < breakpoints.length; ++i)
				{

					if (breakpoints[i].getAttributes().get("lineNumber").equals(lineNumberInt))
					{
						breakpoints[i].delete();
						found = true;
					}
				}
				if (found)
				{
					return null;
				}
				point = PHPDebugTarget.createBreakpoint(res, editorLineNumber, attributes);

			}
			else if (input instanceof IStorageEditorInput)
			{
				IStorage storage = ((IStorageEditorInput) input).getStorage();

				Map<String, String> attributes = new HashMap<String, String>();
				String fileName;

				String secondaryId = storage.getFullPath().toOSString();
				attributes.put(IPHPDebugConstants.SECONDARY_ID_KEY, secondaryId);

				if (storage instanceof LocalFileStorage)
				{
					attributes.put(IPHPDebugConstants.STORAGE_TYPE, IPHPDebugConstants.STORAGE_TYPE_INCLUDE);

					fileName = ((LocalFileStorage) storage).getName();
					String incDir = ((LocalFileStorage) storage).getIncBaseDirName();
					if (incDir != null)
					{
						fileName = secondaryId.substring(incDir.length() + 1);
					}
					IProject project = ((LocalFileStorage) storage).getProject();
					attributes.put(IPHPDebugConstants.STORAGE_PROJECT, project != null ? project.getName() : "");
					attributes.put(IPHPDebugConstants.STORAGE_INC_BASEDIR, incDir != null ? incDir : "");
				}
				else
				{
					attributes.put(IPHPDebugConstants.STORAGE_TYPE, IPHPDebugConstants.STORAGE_TYPE_REMOTE);
					fileName = storage.getName();
				}

				attributes.put(IPHPDebugConstants.STORAGE_FILE, fileName);
				point = PHPDebugTarget.createBreakpoint(res, editorLineNumber, attributes);
			}
		}
		if (point == null)
		{
			PHPDebugEPLPlugin.logError("break point was not installed"); //$NON-NLS-1$
			// StatusLineMessageTimerManager.setErrorMessage(PHPDebugUIMessages.ErrorCreatingBreakpoint_1, 1000, true);
			// // hide message after 1 second
		}
		if (status == null)
		{
			status = new Status(IStatus.OK, PHPDebugEPLPlugin.PLUGIN_ID, IStatus.OK, MessageFormat.format(
					PHPDebugUIMessages.BreakpointCreated_1, new Object[] {}), null);
		}
		return status;
	}

	public IResource getResource(IEditorInput input)
	{
		return getResourceFromInput(input);
	}

	private IResource getResourceFromInput(IEditorInput input)
	{
		IResource resource = (IResource) input.getAdapter(IFile.class);
		if (resource == null || !resource.exists())
		{
			// for non-workspace resources - use workspace root for storing breakpoints
			resource = ResourcesPlugin.getWorkspace().getRoot();
		}
		return resource;
	}

	/**
	 * Finds a valid position somewhere on lineNumber in document, idoc, where a breakpoint can be set and returns that
	 * position. -1 is returned if a position could not be found.
	 * 
	 * @param unifiedEditor
	 * @param editorLineNumber
	 * @return position to set breakpoint or -1 if no position could be found
	 */
	public static int getValidPosition(AbstractThemeableEditor unifiedEditor, int editorLineNumber)
	{

		try
		{
			IDocument document = unifiedEditor.getISourceViewer().getDocument();
			// First, check if we have a PHP region (partition) in the selected line.
			// If not, we should return this line position as invalid.
			IRegion lineInformation = document.getLineInformation(editorLineNumber);
			ITypedRegion[] typedRegions = document.getDocumentPartitioner().computePartitioning(
					lineInformation.getOffset(), lineInformation.getLength());
			boolean foundPHPPartition = false;
			// Check for a special case where the user click a line that has a language switch tag.
			// In this case, we are checking for the next line to see if we are in a PHP region, and the
			// switch tag was actually a PHP open tag.
			if (typedRegions.length > 0
					&& typedRegions[0].getType().startsWith(CompositePartitionScanner.START_SWITCH_TAG))
			{
				// Check if the next line starts in a PHP region.
				try
				{
					IRegion nextLineInformation = document.getLineInformation(editorLineNumber + 1);
					ITypedRegion[] nextTypeRegions = document.getDocumentPartitioner().computePartitioning(
							nextLineInformation.getOffset(), nextLineInformation.getLength());
					if (nextTypeRegions.length > 0 && nextTypeRegions[0].getType().startsWith(IPHPConstants.PREFIX))
					{
						foundPHPPartition = true;
						editorLineNumber++;
					}
				}
				catch (BadLocationException e)
				{
					// ignore
				}

			}
			if (!foundPHPPartition)
			{
				for (ITypedRegion region : typedRegions)
				{
					if (region.getType().startsWith(IPHPConstants.PREFIX))
					{
						foundPHPPartition = true;
						break;
					}
				}
			}
			if (foundPHPPartition)
			{
				ILexemeProvider<PHPTokenType> lexemeProvider = ParsingUtils.createLexemeProvider(document);
				for (int line = editorLineNumber; line < document.getNumberOfLines(); line++)
				{
					lineInformation = document.getLineInformation(line);
					// Make sure we are still starting with a PHP region
					typedRegions = document.getDocumentPartitioner().computePartitioning(lineInformation.getOffset(),
							lineInformation.getOffset());
					if (foundPHPPartition || typedRegions.length > 0
							&& typedRegions[0].getType().startsWith(IPHPConstants.PREFIX))
					{
						// reset the initial find after the first line visit.
						foundPHPPartition = false;
						int checkLine = checkLine(line, document, lexemeProvider);
						if (checkLine != -1)
						{
							return checkLine;
						}
					}
					else
					{
						break;
					}
				}
			}
			Display.getDefault().asyncExec(new Runnable()
			{
				public void run()
				{
					StatusLineMessageTimerManager.setErrorMessage("Could not set a breakpoint at the selected line",
							2000L, true);
				}
			});
			return -1;
		}
		catch (BadLocationException e)
		{
			return -1;
		}

	}

	/**
	 * Check a single line to make sure we are not in a comment, or just in a line with white-spaces.
	 * 
	 * @param editorLineNumber
	 * @param document
	 * @param lexemeProvider
	 * @return
	 * @throws BadLocationException
	 */
	private static int checkLine(int editorLineNumber, IDocument document, ILexemeProvider<PHPTokenType> lexemeProvider)
			throws BadLocationException
	{
		IRegion lineInformation = document.getLineInformation(editorLineNumber);
		int startIndex = lexemeProvider.getLexemeCeilingIndex(lineInformation.getOffset());
		int endIndex = lexemeProvider
				.getLexemeFloorIndex(lineInformation.getOffset() + lineInformation.getLength() - 1);
		for (int a = startIndex; a < endIndex; a++)
		{
			Lexeme<PHPTokenType> lexeme = lexemeProvider.getLexeme(a);
			PHPTokenType phpToken = lexeme.getType();
			String tokenType = phpToken.getType();
			if (tokenType != PHPRegionTypes.PHP_COMMENT && tokenType != PHPRegionTypes.PHP_LINE_COMMENT
					&& tokenType != PHPRegionTypes.PHPDOC_COMMENT && phpToken.getType() != PHPRegionTypes.WHITESPACE)
			{
				int lineOffset = lineInformation.getOffset();
				return lineOffset;
			}
		}
		return -1;
	}

	/**
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement,
	 *      java.lang.String, java.lang.Object)
	 */
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
	{
		// not used
	}

	public static int getValidPosition(IDocument document, int newLine)
	{
		try
		{
			int checkLine = document.getLineOffset(newLine);
			return checkLine;
		}
		catch (BadLocationException e)
		{
			return -1;
		}
	}

}
