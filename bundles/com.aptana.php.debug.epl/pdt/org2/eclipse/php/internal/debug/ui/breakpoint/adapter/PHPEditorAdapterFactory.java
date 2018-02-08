/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org2.eclipse.php.internal.debug.ui.breakpoint.adapter;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.ui.actions.IRunToLineTarget;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org2.eclipse.php.internal.debug.core.resources.ExternalFilesRegistry;

public class PHPEditorAdapterFactory implements IAdapterFactory {

	@SuppressWarnings("unchecked")
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		ITextEditor editorPart = (ITextEditor) adaptableObject;
		IResource resource = (IResource) editorPart.getEditorInput().getAdapter(IResource.class);
		if (resource == null && editorPart.getEditorInput() instanceof IPathEditorInput) {
			IPathEditorInput input = (IPathEditorInput) editorPart.getEditorInput();
			String filePath = input.getPath().toOSString();
			resource = ExternalFilesRegistry.getInstance().getFileEntry(filePath);
			if (resource == null && filePath.length() > 0 && filePath.charAt(0) == '/') {
				resource = ExternalFilesRegistry.getInstance().getFileEntry(filePath.substring(1));
			}
		}
		if (resource == null) {
			return null;
		}
		if (resource.getType() != IResource.FILE) {
			return null;
		}
//		if (!PHPToolkitUtil.isPhpFile((IFile) resource)) {
//			return null;
//		}
		if (adapterType.equals(IRunToLineTarget.class)) {
			return new PHPRunToLineAdapter();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public Class[] getAdapterList() {
		return new Class[] { IRunToLineTarget.class };
	}
}
