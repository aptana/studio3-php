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
package org.eclipse.php.internal.debug.ui.breakpoint;

import java.text.MessageFormat;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.php.internal.debug.core.PHPDebugCoreMessages;
import org.eclipse.php.internal.debug.ui.breakpoint.provider.PHPBreakpointProvider;
import org.eclipse.ui.texteditor.IMarkerUpdater;

import com.aptana.debug.php.epl.PHPDebugEPLPlugin;

/**
 * Responsible for update the PHPConditionalBreakpointMarker info while saving the document.
 * The updated attributes are line number and marker message.   
 * @author moshe
 *
 */
public class BreakpointMarkerUpdater implements IMarkerUpdater {

	public BreakpointMarkerUpdater() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IMarkerUpdater#getAttribute()
	 */
	public String[] getAttribute() {
		return new String[] { IMarker.LINE_NUMBER, IMarker.MESSAGE };
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IMarkerUpdater#getMarkerType()
	 */
	public String getMarkerType() {
		return "org.eclipse.php.debug.core.PHPConditionalBreakpointMarker"; //$NON-NLS-1$
	}

	/**
	 * @return 	true  - the breakpoint marker updated
	 * 			false - the breakpoint marker will deleted
	 */
	public boolean updateMarker(IMarker marker, IDocument document, Position position) {
		if (position.isDeleted()) {
			return false;
		}
		try {
			int newLine = document.getLineOfOffset(position.offset) + 1;
			int offset = PHPBreakpointProvider.getValidPosition(document, newLine);
			// offset equals -1 means there is no valid position for this breakpoint marker
			// and the breakpoint will delete.
			if (offset != -1) {
				newLine = document.getLineOfOffset(offset) + 1;
				marker.setAttribute(IMarker.MESSAGE, MessageFormat.format(PHPDebugCoreMessages.LineBreakPointMessage_1, new Object[] { marker.getResource().getName(), newLine }));
				if (marker.getAttribute(IMarker.LINE_NUMBER, -1) == newLine) {
					return true;
				}
				marker.setAttribute(IMarker.LINE_NUMBER, newLine);
				return true;
			}
		} catch (Exception e) {
			PHPDebugEPLPlugin.logError(e);
		}

		return false;
	}
}
