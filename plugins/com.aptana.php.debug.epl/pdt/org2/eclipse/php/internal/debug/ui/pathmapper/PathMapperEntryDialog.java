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
package org2.eclipse.php.internal.debug.ui.pathmapper;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org2.eclipse.php.debug.core.debugger.pathmapper.PathMapper.Mapping;
import org2.eclipse.php.internal.core.project.IIncludePathEntry;
import org2.eclipse.php.internal.core.project.options.PHPProjectOptions;
import org2.eclipse.php.internal.core.project.options.includepath.IncludePathVariableManager;
import org2.eclipse.php.internal.debug.core.pathmapper.PathEntry.Type;
import org2.eclipse.php.internal.debug.core.pathmapper.VirtualPath;
import org2.eclipse.php.internal.debug.ui.pathmapper.PathMapperEntryDialog.WorkspaceBrowseDialog.IPFile;
import org2.eclipse.php.internal.ui.util.PixelConverter;
import org2.eclipse.php.util.StatusInfo;

import com.aptana.editor.php.core.PHPNature;
import com.aptana.editor.php.internal.ui.PHPPluginImages;

public class PathMapperEntryDialog extends StatusDialog {

	private Mapping fEditData;
	private Text fServerPathText;
	private Text fWorkspacePathText;
	private Button fWorkspacePathBrowseBtn;

	public PathMapperEntryDialog(Shell parent) {
		this(parent, null);
		setResizable(parent);
	}

	public PathMapperEntryDialog(Shell parent, Mapping editData) {
		super(parent);
		setResizable(parent);
		if (editData != null) {
			fEditData = editData.clone();
			setTitle("Edit Path Mapping");
		} else {
			setTitle("Add new Path Mapping");
		}
		setHelpAvailable(false);
	}

	public Mapping getResult() {
		return fEditData;
	}

	protected Control createDialogArea(Composite parent) {
		parent = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Remote path text field:
		Label label = new Label(composite, SWT.NONE);
		label.setText("Path on &Server:");

		fServerPathText = new Text(composite, SWT.BORDER);
		ModifyListener modifyListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validate();
			}
		};
		fServerPathText.addModifyListener(modifyListener);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		fServerPathText.setLayoutData(layoutData);
		addExampleText(composite, "e.g. '/', '/public' etc.");
		label = new Label(composite, SWT.NONE);
		label.setText("Path in &Workspace");
		fWorkspacePathText = new Text(composite, SWT.BORDER);
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		fWorkspacePathText.setLayoutData(layoutData);
		fWorkspacePathText.addModifyListener(modifyListener);

		fWorkspacePathBrowseBtn = new Button(composite, SWT.NONE);
		fWorkspacePathBrowseBtn.setText("&Workspace...");
		fWorkspacePathBrowseBtn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				WorkspaceBrowseDialog dialog = new WorkspaceBrowseDialog(getShell());
				if (dialog.open() == Window.OK) {
					Object selectedElement = dialog.getSelectedElement();
					fWorkspacePathText.setData(null);
					if (selectedElement instanceof IResource) {
						IResource resource = (IResource) selectedElement;
						fWorkspacePathText.setData(Type.WORKSPACE);
						fWorkspacePathText.setText(resource.getFullPath().toString());
					} else if (selectedElement instanceof IIncludePathEntry) {
						IIncludePathEntry includePathEntry = (IIncludePathEntry) selectedElement;
						fWorkspacePathText.setData(includePathEntry.getEntryKind() == IIncludePathEntry.IPE_VARIABLE ? Type.INCLUDE_VAR : Type.INCLUDE_FOLDER);
						if (includePathEntry.getEntryKind() == IIncludePathEntry.IPE_VARIABLE) {
							// IPath path = EnvironmentPathUtils.getLocalPath(includePathEntry.getPath());
							IPath incPath = IncludePathVariableManager.instance().resolveVariablePath(includePathEntry.getPath().toString());
							if (incPath != null) {
								fWorkspacePathText.setText(incPath.toOSString());
							}
						} else {
							// fWorkspacePathText.setText(EnvironmentPathUtils.getLocalPath(includePathEntry.getPath()).toOSString());
							fWorkspacePathText.setText(includePathEntry.getPath().toOSString());
						}
					} else if (selectedElement instanceof IPFile) {
						IPFile ipFile = (IPFile) selectedElement;
						IIncludePathEntry includePathEntry = ipFile.includePathEntry;
						fWorkspacePathText.setData(includePathEntry.getEntryKind() == IIncludePathEntry.IPE_VARIABLE ? Type.INCLUDE_VAR : Type.INCLUDE_FOLDER);
						fWorkspacePathText.setText(ipFile.file.getAbsolutePath());
					}
				}
			}
		});


		applyDialogFont(composite);
		initializeValues();

		return parent;
	}

	private void setResizable(Shell parent) 
	{
		int style = SWT.RESIZE;
		if (parent != null)
		{
			style |= parent.getStyle();
		}
		setShellStyle(style);
	}

	/**
	 * Adds an example text label. 
	 */
	protected void addExampleText(Composite parent, String text)
	{
		new Label(parent,SWT.NONE);
		Label banner=new Label(parent,SWT.NONE);
		banner.setText(text);
		Font defaultFont = JFaceResources.getTextFont();
		final Font smallFont = new Font(banner.getDisplay(),defaultFont.getFontData()[0].getName(),8,SWT.NONE);
		banner.setFont(smallFont);
		banner.addDisposeListener(new DisposeListener(){

			public void widgetDisposed(DisposeEvent e)
			{
				smallFont.dispose();
			}

		});
		GridData gds=new GridData(GridData.FILL_HORIZONTAL);
		gds.horizontalSpan=2;
		gds.verticalIndent=-2;
		gds.verticalAlignment=SWT.TOP;
		banner.setLayoutData(gds);
	}

	protected void initializeValues() {
		if (fEditData != null) {
			fServerPathText.setText(fEditData.remotePath.toString());
			fWorkspacePathText.setData(fEditData.type);
			fWorkspacePathText.setText(fEditData.localPath.toString());
		}
	}

	protected void setError(String error) {
		updateStatus(new StatusInfo(IStatus.ERROR, error));
	}

	protected void validate() {
		Mapping mapping = new Mapping();

		String remotePathStr = fServerPathText.getText().trim();
		if (remotePathStr.length() == 0) {
			setError("Path on server must not be empty!");
			return;
		}
		try {
			mapping.remotePath = new VirtualPath(remotePathStr);
		} catch (IllegalArgumentException e) {
			setError("Path on server is illegal or not absolute!");
			return;
		}

		// Workspace file:
		String workspacePath = fWorkspacePathText.getText().trim();
		if (workspacePath.length() == 0) {
			setError("Path in workspace must not be empty!");
			return;
		}

		boolean pathExistsInWorkspace = false;
		mapping.type = (Type) fWorkspacePathText.getData();
		if (mapping.type == Type.INCLUDE_FOLDER  || mapping.type == Type.INCLUDE_VAR) {
			pathExistsInWorkspace = new File(workspacePath).exists();
		} else {
			pathExistsInWorkspace = (ResourcesPlugin.getWorkspace().getRoot().findMember(workspacePath) != null);
		}
		if (!pathExistsInWorkspace) {
			setError(NLS.bind("Path ''{0}'' doesn't exist in workspace!", workspacePath));
			return;
		}
		try {
			mapping.localPath = new VirtualPath(workspacePath);
		} catch (IllegalArgumentException e) {
			setError("Path in workspace is illegal or not absolute!");
			return;
		}

		fEditData = mapping;

		updateStatus(Status.OK_STATUS);
	}

	class WorkspaceBrowseDialog extends StatusDialog {
		private TreeViewer fViewer;
		private Object selectedElement;

		public WorkspaceBrowseDialog(Shell parent) {
			super(parent);
			setTitle("Select Workspace Resource");
		}

		public Object getSelectedElement() {
			return selectedElement;
		}

		protected Control createDialogArea(Composite parent) {
			parent = (Composite) super.createDialogArea(parent);
			parent.setLayoutData(new GridData(GridData.FILL_BOTH));

			PixelConverter pixelConverter = new PixelConverter(parent);

			fViewer = new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
			GridData layoutData = new GridData(GridData.FILL_BOTH);
			layoutData.widthHint = pixelConverter.convertWidthInCharsToPixels(70);
			layoutData.heightHint = pixelConverter.convertHeightInCharsToPixels(20);
			fViewer.getControl().setLayoutData(layoutData);

			fViewer.setContentProvider(new ContentProvider());
			fViewer.setLabelProvider(new LabelProvider());

			fViewer.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					validate();
				}
			});

			fViewer.setInput(ResourcesPlugin.getWorkspace().getRoot());

			fViewer.addDoubleClickListener(new IDoubleClickListener() {
	            public void doubleClick(DoubleClickEvent event) {
	                okPressed();
	            }
	        });
			return parent;
		}

		protected void validate() {
			IStructuredSelection selection = (IStructuredSelection) fViewer.getSelection();
			Object element = selection.getFirstElement();
			// TODO: buildpath entry selection 
			if (element == null/* || element instanceof IncludeNode*/) {
				updateStatus(new StatusInfo(IStatus.ERROR, ""));
				return;
			}
			selectedElement = element;
			updateStatus(Status.OK_STATUS);
		}

		class IPFile {
			IIncludePathEntry includePathEntry;
			File file;

			IPFile(IIncludePathEntry includePathEntry, File file) {
				this.includePathEntry = includePathEntry;
				this.file = file;
			}

			public int hashCode() {
				return file.hashCode() + 13 * includePathEntry.hashCode();
			}

			public boolean equals(Object obj) {
				if (!(obj instanceof IPFile)) {
					return false;
				}
				IPFile other = (IPFile) obj;
				return other.file.equals(file) && other.includePathEntry.equals(includePathEntry);
			}
		}

		class ContentProvider implements ITreeContentProvider {

			public Object[] getChildren(Object parentElement) {
				try {
					if (parentElement instanceof IContainer) {
						List<Object> r = new LinkedList<Object>();
						// Add all members:
						IContainer container = (IContainer) parentElement;
						IResource[] members = container.members();
						for (IResource member : members) {
							if (member instanceof IContainer && member.isAccessible()) {
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
						// Add include paths:
						if (parentElement instanceof IProject) {
							IProject project = (IProject) parentElement;
							PHPProjectOptions options = PHPProjectOptions.forProject(project);
							if (options != null) {
								IIncludePathEntry[] includePath = options.readRawIncludePath();
								r.addAll(Arrays.asList(includePath));
							}
							// TODO: SG - Check if the use of build path is needed
							/*
							IncludePath[] includePath = IncludePathManager.getInstance().getIncludePaths(project);
							for (IncludePath path : includePath) {
								if (path.isBuildpath()) {
									IIncludePathEntry buildpathEntry = (IIncludePathEntry) path.getEntry();
									if (buildpathEntry.getEntryKind() == IIncludePathEntry.IPE_LIBRARY || buildpathEntry.getEntryKind() == IIncludePathEntry.IPE_VARIABLE) {
										r.add(buildpathEntry);
									}
								}
							}
							*/
						}
						return r.toArray();
					} else if (parentElement instanceof IIncludePathEntry) {
						IIncludePathEntry includePathEntry = (IIncludePathEntry) parentElement;
						// IPath path = EnvironmentPathUtils.getLocalPath(includePathEntry.getPath());
						IPath path = includePathEntry.getPath();
						File file = null;
						if (includePathEntry.getEntryKind() == IIncludePathEntry.IPE_LIBRARY) {
							file = path.toFile();
						} else if (includePathEntry.getEntryKind() == IIncludePathEntry.IPE_VARIABLE) {
							path = IncludePathVariableManager.instance().resolveVariablePath(path.toString());
							if (path != null) {
								file = path.toFile();
							}
						}
						if (file != null) {
							return getChildren(new IPFile(includePathEntry, file));
						}
					} else if (parentElement instanceof IPFile) {
						IPFile ipFile = (IPFile) parentElement;
						File file = ipFile.file;
						if (file.isDirectory()) {
							File dirs[] = file.listFiles(new FileFilter() {
								public boolean accept(File pathname) {
									return pathname.isDirectory();
								}
							});
							List<Object> r = new ArrayList<Object>(dirs.length);
							for (File dir : dirs) {
								r.add(new IPFile(ipFile.includePathEntry, dir));
							}
							return r.toArray();
						}
					}
				} catch (CoreException e) {
				}
				return new Object[0];
			}

			public Object getParent(Object element) {
				if (element instanceof IResource) {
					return ((IResource) element).getParent();
				}
				if (element instanceof IPFile) {
					IPFile ipFile = (IPFile) element;
					return new IPFile(ipFile.includePathEntry, ipFile.file.getParentFile());
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

		// class LabelProvider extends ScriptUILabelProvider {
		/**
		 * A proxy label provider that proxy any unknown image request to the WorkbenchLabelProvider
		 */
		class LabelProvider implements ILabelProvider {
			private WorkbenchLabelProvider workbenchLabelProvider;

			public LabelProvider() {
				workbenchLabelProvider = new WorkbenchLabelProvider();
			}

			public Image getImage(Object element) {
				if (element instanceof IIncludePathEntry) {
					IIncludePathEntry includePathEntry = (IIncludePathEntry) element;
					if (includePathEntry.getEntryKind() == IIncludePathEntry.IPE_VARIABLE) {
						return PHPPluginImages.get(PHPPluginImages.IMG_OBJS_ENV_VAR);
					} else {
						return PHPPluginImages.get(PHPPluginImages.IMG_OBJS_LIBRARY);
					}
				}
				if (element instanceof IPFile) {
					return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
				}
				return workbenchLabelProvider.getImage(element);
			}

			public String getText(Object element) {
				if (element instanceof IIncludePathEntry) {
					IIncludePathEntry includePathEntry = (IIncludePathEntry) element;
					// 	return EnvironmentPathUtils.getLocalPath(includePathEntry.getPath()).toOSString();
					return includePathEntry.getPath().toOSString();
				}
				if (element instanceof IPFile) {
					return ((IPFile) element).file.getName();
				}
				return workbenchLabelProvider.getText(element);
			}

			public void addListener(ILabelProviderListener listener)
			{
				workbenchLabelProvider.addListener(listener);
			}

			public void dispose()
			{
				workbenchLabelProvider.dispose();
			}

			public boolean isLabelProperty(Object element, String property)
			{
				return workbenchLabelProvider.isLabelProperty(element, property);
			}

			public void removeListener(ILabelProviderListener listener)
			{
				workbenchLabelProvider.removeListener(listener);
			}
		}
	}
}
