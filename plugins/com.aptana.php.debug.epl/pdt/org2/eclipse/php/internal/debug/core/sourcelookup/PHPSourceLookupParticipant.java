/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org2.eclipse.php.internal.debug.core.sourcelookup;

import java.io.File;

import org.eclipse.core.internal.filesystem.local.LocalFile;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.sourcelookup.AbstractSourceLookupParticipant;
import org2.eclipse.php.internal.debug.core.xdebug.dbgp.model.DBGpStackFrame;
import org2.eclipse.php.internal.debug.core.zend.model.PHPStackFrame;

/**
 * The PHP source lookup participant knows how to translate a 
 * PHP stack frame into a source file name 
 */
public class PHPSourceLookupParticipant extends AbstractSourceLookupParticipant {
	/* (non-Javadoc)
	 * @see org.eclipse.debug.internal.core.sourcelookup.ISourceLookupParticipant#getSourceName(java.lang.Object)
	 */
	public String getSourceName(Object object) throws CoreException {
		if (object instanceof PHPStackFrame) {
			return ((PHPStackFrame) object).getSourceName();
		}
		if (object instanceof DBGpStackFrame) {
			String src = ((DBGpStackFrame) object).getSourceName();
			if (src == null) {
				src = ((DBGpStackFrame) object).getQualifiedFile();
				IPath p = new Path(src);
				src = p.lastSegment();
			}
			return src;
		}
		return null;
	}

	public Object[] findSourceElements(Object object) throws CoreException {
		Object[] sourceElements = EMPTY;
		try {
			sourceElements = super.findSourceElements(object);
		} catch (Throwable e) {
			// Check if the lookup failed because the source is outside the workspace.
		}

		if (sourceElements == EMPTY) {
			// If the lookup returned an empty elements array, check if the source is outside the workspace.
			String fileName = null;
			if (object instanceof PHPStackFrame) {
				fileName = ((PHPStackFrame) object).getSourceName();
			} else if (object instanceof DBGpStackFrame) {
				DBGpStackFrame dbgpStackFrame = (DBGpStackFrame) object;
				fileName = dbgpStackFrame.getQualifiedFile();
				// Aptana Mod
				// TODO: SG - Check if this is also needed with Zend, since it's only activated for remotely-initiated session.
				if (fileName != null)
				{
					IFile[] fileFound = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(
							new Path(fileName));
					if (fileFound.length > 0)
					{
						return new IFile[] { fileFound[0] };
					}
				}
			}
			if (fileName != null) {
				File file = new File(fileName);
				if (!file.exists()) {
					return EMPTY;
				}

				LocalFile storage = new LocalFile(file);
				if (storage != null) {
					return new Object[] { storage };
				} else {
					return EMPTY;
				}
			}
		}
		return sourceElements;
	}

}
