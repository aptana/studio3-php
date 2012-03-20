package org2.eclipse.php.internal.debug.ui.console;

import org.eclipse.debug.ui.console.FileLink;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.IHyperlink;

import com.aptana.php.debug.epl.PHPDebugEPLPlugin;

/**
 * @author seva A version of {@link FileLink} which also supports external resources
 */
public class PHPFileLink implements IHyperlink
{

	private Object fFile;
	private int fFileOffset;
	private int fFileLength;
	private int fFileLineNumber;

	/**
	 * Constructs a hyperlink to the specified file.
	 * 
	 * @param file
	 *            the file to open when activated <code>null</code> if the default editor should be used
	 * @param fileOffset
	 *            the offset in the file to select when activated, or -1
	 * @param fileLength
	 *            the length of text to select in the file when activated or -1
	 * @param fileLineNumber
	 *            the line number to select in the file when activated, or -1
	 */
	public PHPFileLink(Object file, int fileOffset, int fileLength, int fileLineNumber)
	{
		fFile = file;
		fFileOffset = fileOffset;
		fFileLength = fileLength;
		fFileLineNumber = fileLineNumber;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.console.IConsoleHyperlink#linkActivated()
	 */
	public void linkActivated()
	{
		IEditorInput editorInput=SourceDisplayUtil.getEditorInput(fFile);
		try
		{
			SourceDisplayUtil.openInEditor(editorInput, fFileLineNumber);
		}
		catch (PartInitException e)
		{
			PHPDebugEPLPlugin.logError(e);
		}
	}

	public void linkEntered()
	{
	}

	public void linkExited()
	{
	}
}