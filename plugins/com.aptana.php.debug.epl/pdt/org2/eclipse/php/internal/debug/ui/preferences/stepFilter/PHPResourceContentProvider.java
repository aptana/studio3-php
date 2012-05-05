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
package org2.eclipse.php.internal.debug.ui.preferences.stepFilter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org2.eclipse.php.internal.core.project.IIncludePathEntry;
import org2.eclipse.php.internal.core.project.options.PHPProjectOptions;
import org2.eclipse.php.internal.core.project.options.includepath.IncludePathVariableManager;

import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.core.PHPNature;
import com.aptana.editor.php.internal.core.IPHPConstants;
import com.aptana.editor.php.internal.ui.PHPPluginImages;
import com.aptana.ui.util.SWTUtils;

/**
 * A content provider to be used for Resource selection dialog
 * This special content provider will put the projects and their include paths
 * at the same tree level
 * @author yaronm
 */
public class PHPResourceContentProvider implements ITreeContentProvider {

	private IContentType phpContentType = Platform.getContentTypeManager().getContentType(IPHPConstants.CONTENT_TYPE_HTML_PHP);

	public Object[] getChildren(Object parentElement) {
		try {
			if (parentElement instanceof IContainer) {
				List<Object> r = new LinkedList<Object>();
				// Add all members:
				IContainer container = (IContainer) parentElement;
				IResource[] members = container.members();
				for (IResource member : members) {
					if (member instanceof IContainer && member.isAccessible() && !isResourceFiltered(member)) {
						if (member instanceof IProject) { // show only PHP projects
							IProject project = (IProject) member;
							if (project.hasNature(PHPNature.NATURE_ID)) {
								r.add(member);
							}
						} else {
							r.add(member);
						}
					}
				}
				if (parentElement instanceof IWorkspaceRoot) {
					return r.toArray();
				}
				// Add include paths:
				if (parentElement instanceof IProject) {
					IProject project = (IProject) parentElement;
					// IncludePath[] includePath = IncludePathManager.getInstance().getIncludePaths(project);
					PHPProjectOptions options = PHPProjectOptions.forProject(project);
					if (options != null) {
						IIncludePathEntry[] includePath = options.readRawIncludePath();
						r.addAll(Arrays.asList(includePath));
						//					for (IncludePath path : includePath) {
						//						if (path.isBuildpath()) {
						//							IBuildpathEntry buildpathEntry = (IBuildpathEntry) path.getEntry();
						//							if (buildpathEntry.getEntryKind() == IBuildpathEntry.BPE_LIBRARY || buildpathEntry.getEntryKind() == IBuildpathEntry.BPE_VARIABLE) {
						//								r.add(buildpathEntry);
						//							}
						//						}
						//					}
					}
					return r.toArray();
				}
			} else if (parentElement instanceof IIncludePathEntry) {
				IIncludePathEntry buildpathEntry = (IIncludePathEntry) parentElement;
				IPath path = buildpathEntry.getPath();
				File file = null;
				if (buildpathEntry.getEntryKind() == IIncludePathEntry.IPE_LIBRARY) {
					file = path.toFile();
				}
				else if (buildpathEntry.getEntryKind() == IIncludePathEntry.IPE_VARIABLE) {
					// path = DLTKCore.getResolvedVariablePath(path);
					path = IncludePathVariableManager.instance().resolveVariablePath(path.toString());
					if (path != null) {
						file = path.toFile();
					}
				}
				if (file != null) {
					return getChildren(new IncPathFile(buildpathEntry, file));
				}
			} else if (parentElement instanceof IncPathFile) {
				IncPathFile ipFile = (IncPathFile) parentElement;
				File file = ipFile.file;
				if (file.isDirectory()) {
					File files[] = file.listFiles();
					List<Object> r = new ArrayList<Object>(files.length);
					for (File currentFile : files) {
						r.add(new IncPathFile(ipFile.includePathEntry, currentFile));
					}
					return r.toArray();
				}
			}
		} catch (CoreException e) {
		}
		return new Object[0];
	}

	//filter out non PHP files
	private boolean isResourceFiltered(IResource member) {
		if (member instanceof IFile) {
			return !phpContentType.isAssociatedWith(member.getName());
		}
		return false;
	}

	public Object getParent(Object element) {
		if (element instanceof IResource) {
			return ((IResource) element).getParent();
		}
		if (element instanceof IncPathFile) {
			IncPathFile ipFile = (IncPathFile) element;
			return new IncPathFile(ipFile.includePathEntry, ipFile.file.getParentFile());
		}
		return null;
	}

	public boolean hasChildren(Object element) {
		return getChildren(element).length > 0;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}

class IncPathFile {
	//	IBuildpathEntry IBuildpathEntry;
	IIncludePathEntry includePathEntry;
	File file;

	IncPathFile(IIncludePathEntry includePathEntry, File file) {
		this.includePathEntry = includePathEntry;
		this.file = file;
	}

	public IIncludePathEntry getBuildpathEntry() {
		return includePathEntry;
	}

	public int hashCode() {
		return file.hashCode() + 13 * includePathEntry.hashCode();
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof IncPathFile)) {
			return false;
		}
		IncPathFile other = (IncPathFile) obj;
		return other.file.equals(file) && other.includePathEntry.equals(includePathEntry);
	}
}

class PHPResLabelProvider extends LabelProvider {

	private WorkbenchLabelProvider workbenchLabelProvider;

	public PHPResLabelProvider() {
		workbenchLabelProvider = new WorkbenchLabelProvider();
	}

	public Image getImage(Object element) {
		if (element instanceof IIncludePathEntry) {
			IIncludePathEntry buildpathEntry = (IIncludePathEntry) element;
			if (buildpathEntry.getEntryKind() == IIncludePathEntry.IPE_VARIABLE) {
				return PHPPluginImages.get(PHPPluginImages.IMG_OBJS_ENV_VAR);
			} else {
				return PHPPluginImages.get(PHPPluginImages.IMG_OBJS_LIBRARY);
			}
		}
		if (element instanceof IncPathFile) {
			IncPathFile currentFile = (IncPathFile) element;
			if (currentFile.file.isDirectory()) {
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
			} else {
				return SWTUtils.getImage(PHPEditorPlugin.getDefault(), "/icons/full/obj16/php.png"); //$NON-NLS-1$
			}
		}
		return workbenchLabelProvider.getImage(element);
	}

	public String getText(Object element) {
		if (element instanceof IIncludePathEntry) {
			IIncludePathEntry includePathEntry = (IIncludePathEntry) element;
			// TODO: SG - add a EnvironmentPathUtils replacement 
			// return EnvironmentPathUtils.getLocalPath(includePathEntry.getPath()).toOSString();
			return includePathEntry.getPath().toOSString();
		}
		if (element instanceof IncPathFile) {
			return ((IncPathFile) element).file.getName();
		}
		return workbenchLabelProvider.getText(element);
	}
}
