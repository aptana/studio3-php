/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.php.internal.debug.ui.console;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.php.internal.debug.core.IPHPConsoleEventListener;
import org.eclipse.php.internal.debug.core.launching.DebugConsoleMonitor;
import org.eclipse.php.internal.debug.core.launching.PHPHyperLink;
import org.eclipse.php.internal.debug.core.zend.debugger.DebugError;
import org.eclipse.ui.console.IHyperlink;

public class PHPConsoleListener implements IPHPConsoleEventListener {

	protected ILaunch fLaunch;
	protected DebugConsoleMonitor fConsoleMonitor;
	protected PHPHyperLink fPHPHyperLink;

	public void init(ILaunch launch, DebugConsoleMonitor consoleMonitor, PHPHyperLink link) {
		fLaunch = launch;
		fConsoleMonitor = consoleMonitor;
		fPHPHyperLink = link;
	}

	public void handleEvent(DebugError debugError) {
		IHyperlink link = createLink(debugError);
		String message = debugError.toString().trim();
		fPHPHyperLink.addLink(link, message, message.length() - debugError.getErrorTextLength());
		fConsoleMonitor.append(debugError.toString() + '\n');
	}
	// TODO: SG - Check the file link
	protected IHyperlink createLink(DebugError debugError) {
		String fileName = debugError.getFullPathName();
		int lineNumber = debugError.getLineNumber();
		return new PHPFileLink(fileName, -1, -1, lineNumber);
	}
	
	/* OLD CODE 
	 protected IHyperlink createLink(DebugError debugError) {
		IHyperlink fileLink = null;
		String fileName = debugError.getFullPathName();
		int lineNumber = debugError.getLineNumber();
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(fileName));
		if (file == null) {
			Object fileObject = null;
			if (ExternalFilesRegistry.getInstance().isEntryExist(new Path(fileName).toOSString())) {
				fileObject = ExternalFilesRegistry.getInstance().getFileEntry(new Path(fileName).toOSString());
			} else {
				// Search for a file in a Workspace
				file = (IFile)ResourcesPlugin.getWorkspace().getRoot().findMember(fileName);
				if (file != null) {
					fileObject = file;
				} else {
//					PHPFileData fileData = null;
//					try {
//						fileData = PHPWorkspaceModelManager.getInstance().getModelForFile(fileName);
//					} catch (Exception e) {
//					}
//					if (fileData != null) {
//						fileObject = fileData;
//					} else {
//						File externalFile = new File(fileName);
//						if (externalFile.exists()) {
//							fileObject = externalFile;
//						} else {
//							fileObject = ExternalFileWrapper.createFile(fileName);
//						}
//					}
					//FIXME
				}
			}
			if (fileObject != null) {
				fileLink = new PHPFileLink(fileObject, -1, -1, lineNumber);
			}
		} else {
			fileLink = new PHPFileLink(file, -1, -1, lineNumber);
		}
		return fileLink;
	}
	 */
}