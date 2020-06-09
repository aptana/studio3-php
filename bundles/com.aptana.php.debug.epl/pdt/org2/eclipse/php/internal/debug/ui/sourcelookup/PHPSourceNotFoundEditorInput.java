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
package org2.eclipse.php.internal.debug.ui.sourcelookup;

import java.text.MessageFormat;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org2.eclipse.php.internal.debug.core.sourcelookup.PHPSourceNotFoundInput;
import org2.eclipse.php.internal.debug.ui.PHPDebugUIMessages;

/**
 * Editor input for a stack frame for which source could not be located.
 *
 */
public class PHPSourceNotFoundEditorInput extends PlatformObject implements IEditorInput {

	/**
	 * Associated stack frame
	 */
	private IStackFrame fFrame;

	/**
	 * Stack frame text (cached on creation)
	 */
	private String fFrameText;
	private String fTooltipText;

	/**
	 * Constructs an editor input for the given stack frame,
	 * to indicate source could not be found.
	 * A default tooltip and text will appear in the editor.
	 *
	 * @param frame stack frame
	 * @see #PHPSourceNotFoundEditorInput(PHPSourceNotFoundInput, String)
	 */
	public PHPSourceNotFoundEditorInput(PHPSourceNotFoundInput input) {
		fFrame = input.getStackFrame();
		IDebugModelPresentation pres = DebugUITools.newDebugModelPresentation(fFrame.getModelIdentifier());
		fFrameText = pres.getText(fFrame);
		pres.dispose();
	}

	/**
	 * Constructs an editor input for the given stack frame,
	 * to indicate source could not be found.
	 *
	 * @param frame stack frame
	 * @param tooltipText The text that will appear in the editor.
	 */
	public PHPSourceNotFoundEditorInput(PHPSourceNotFoundInput input, String tooltipText) {
		fFrame = input.getStackFrame();
		IDebugModelPresentation pres = DebugUITools.newDebugModelPresentation(fFrame.getModelIdentifier());
		fFrameText = pres.getText(fFrame);
		pres.dispose();
		fTooltipText = tooltipText;
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#exists()
	 */
	public boolean exists() {
		return false;
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return DebugUITools.getDefaultImageDescriptor(fFrame);
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#getName()
	 */
	public String getName() {
		try {
			String fullName = fFrame.getName();
			String lastSegment = new Path(fullName).lastSegment();
			return lastSegment == null ? fullName : lastSegment;
		} catch (DebugException e) {
			return PHPDebugUIMessages.SourceNotFoundEditorInput_Source_Not_Found_1; 
		}
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#getPersistable()
	 */
	public IPersistableElement getPersistable() {
		return null;
	}

	/**
	 * @see org.eclipse.ui.IEditorInput#getToolTipText()
	 */
	public String getToolTipText() {
		if (fTooltipText == null) {
			return MessageFormat.format(PHPDebugUIMessages.SourceNotFoundEditorInput_Source_not_found_for__0__2, new String[] { fFrameText }); 
		}
		return fTooltipText;
	}

	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof PHPSourceNotFoundEditorInput) {
			if (fFrame != null) {
				return fFrame.equals(((PHPSourceNotFoundEditorInput) obj).fFrame);
			}
		}
		// To avoid openning of a source-not-found while the source appears in another editor
		// we also check for StorageEditorInputs.
		// TODO - Need a fix! An editor is still opened when clicking a breakpoint in the breakpoints view, while
		// a remote sotrage editor is still open.
		//		if (obj instanceof StorageEditorInput) {
		//			StorageEditorInput storageEditorInput = (StorageEditorInput) obj;
		//			String storageLocation = storageEditorInput.getStorage().getFullPath().toString();
		//			return storageLocation.equals(fFrameText);
		//		}
		return super.equals(obj);
	}

}
