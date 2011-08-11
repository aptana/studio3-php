/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.ui.preferences;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.progress.UIJob;

import com.aptana.editor.php.internal.builder.IPHPLibrary;
import com.aptana.editor.php.internal.builder.LibraryManager;
import com.aptana.editor.php.internal.builder.preferences.ProjectDependencies;

/**
 * Composite for editing PHP project build path.
 * 
 * @author Pavel Petrochenko
 * @author Denis Denisenko
 */
public class BuildPathEditingComposite extends Composite
{
	private TableViewer workspaceViewer;
	private TableViewer directoryViewer;
	private CheckboxTableViewer libraryViewer;

	private boolean usesCustomLibs;

	private Set<String> notusedLibraries = new HashSet<String>();
	private Button customLibsButton;
	private Button select;
	private Button deselect;
	private IProject project;
	private final PropertyPage propertyPage;

	/**
	 * Constructs a new BuildPathEditingComposite
	 * 
	 * @param propertyPage
	 *            The {@link PropertyPage} that contains this composite.
	 * @param parent
	 * @param style
	 */
	public BuildPathEditingComposite(PropertyPage propertyPage, Composite parent, int style)
	{
		super(parent, style);
		Assert.isNotNull(propertyPage, "PropertyPage was null"); //$NON-NLS-1$
		this.propertyPage = propertyPage;
		this.project = (IProject) propertyPage.getElement().getAdapter(IProject.class);
		this.setLayout(new FillLayout());
		TabFolder fld = new TabFolder(this, SWT.LEFT);
		TabItem item = new TabItem(fld, SWT.NONE);
		item.setText(Messages.BuildPathEditingComposite_ProjectsTabTitle);
		createWorkspacePart(item);
		TabItem item1 = new TabItem(fld, SWT.NONE);
		item1.setText(Messages.BuildPathEditingComposite_DirectoriesTabTitle);
		TabItem item2 = new TabItem(fld, SWT.NONE);
		item2.setText(Messages.BuildPathEditingComposite_libraries);
		createDirectoryPart(item1);
		createLibraryPart(item2);
	}

	private void createLibraryPart(TabItem item)
	{
		TabFolder parent2 = item.getParent();
		Composite projectPart = new Composite(parent2, SWT.NONE);
		projectPart.setLayout(new GridLayout(2, false));
		customLibsButton = new Button(projectPart, SWT.CHECK);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		customLibsButton.setLayoutData(layoutData);
		customLibsButton.setText(Messages.BuildPathEditingComposite_projectSpecific);
		libraryViewer = CheckboxTableViewer.newCheckList(projectPart, SWT.BORDER);
		libraryViewer.setContentProvider(new ArrayContentProvider());
		IPHPLibrary[] allLibraries = LibraryManager.getInstance().getAllLibraries();
		libraryViewer.setInput(allLibraries);
		libraryViewer.setComparator(new ViewerComparator());
		final Map<URL,Image> images = new HashMap<URL, Image>();
		libraryViewer.setLabelProvider(new LibraryLabelProvider(images));
		final Composite buttons = new Composite(projectPart, SWT.NONE);
		projectPart.addDisposeListener(new DisposeListener()
		{
			public void widgetDisposed(DisposeEvent e)
			{
				for (Image m : images.values())
				{
					m.dispose();
				}
			}
		});
		libraryViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 0;
		buttons.setLayout(layout);
		select = new Button(buttons, SWT.NONE);
		select.setText(Messages.LibrariesPage_selectAll);
		select.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		deselect = new Button(buttons, SWT.NONE);
		deselect.setText(Messages.LibrariesPage_deselectAll);
		deselect.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		customLibsButton.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent e)
			{
			}

			public void widgetSelected(SelectionEvent e)
			{
				boolean selection = customLibsButton.getSelection();
				libraryViewer.getControl().setEnabled(selection);
				select.setEnabled(selection);
				deselect.setEnabled(selection);
			}
		});
		select.addSelectionListener(new SelectAction(true));
		deselect.addSelectionListener(new SelectAction(false));
		item.setControl(projectPart);

	}

	private void createDirectoryPart(TabItem item)
	{
		TabFolder parent2 = item.getParent();
		Composite projectPart = new Composite(parent2, SWT.NONE);
		projectPart.setLayout(new GridLayout(2, false));
		directoryViewer = new TableViewer(projectPart, SWT.BORDER | SWT.MULTI);
		directoryViewer.setLabelProvider(new LabelProvider()
		{

			@Override
			public Image getImage(Object element)
			{
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
			}

			@Override
			public String getText(Object element)
			{
				File fl = (File) element;
				return fl.getAbsolutePath();
			}

		});
		directoryViewer.setContentProvider(new ArrayContentProvider());
		directoryViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		Composite buttonsComp = new Composite(projectPart, SWT.NONE);
		buttonsComp.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		buttonsComp.setLayout(new GridLayout(1, false));
		Button add = new Button(buttonsComp, SWT.NONE);
		add.setText(Messages.BuildPathEditingComposite_AddDirectory);
		add.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				DirectoryDialog dialog = new DirectoryDialog(Display.getCurrent().getActiveShell(), SWT.NONE);
				dialog.setText(Messages.BuildPathEditingComposite_AddDirectoryDescription);
				String open = dialog.open();
				if (open != null)
				{
					addWithValidation(directoryViewer, new File(open));
				}
			}
		});
		add.setLayoutData(GridDataFactory.fillDefaults().hint(100, -1).create());
		final Button remove = new Button(buttonsComp, SWT.NONE);
		remove.setText(Messages.BuildPathEditingComposite_RemoveDirectory);
		remove.setLayoutData(GridDataFactory.fillDefaults().hint(100, -1).create());
		directoryViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{

			public void selectionChanged(SelectionChangedEvent event)
			{
				remove.setEnabled(!event.getSelection().isEmpty());
			}

		});
		remove.setEnabled(false);
		remove.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				IStructuredSelection sel = (IStructuredSelection) directoryViewer.getSelection();
				Object[] selections = sel.toArray();
				for (Object selected : selections)
				{
					directoryViewer.remove(selected);
				}
			}
		});
		directoryViewer.setSorter(new ViewerSorter());
		item.setControl(projectPart);
	}

	private void createWorkspacePart(TabItem item)
	{
		TabFolder parent2 = item.getParent();
		Composite projectPart = new Composite(parent2, SWT.NONE);
		item.setControl(projectPart);
		projectPart.setLayout(new GridLayout(2, false));
		workspaceViewer = new TableViewer(projectPart, SWT.BORDER | SWT.MULTI);
		final FullPathWorkbenchLabelProvider workbenchLabelProvider = new FullPathWorkbenchLabelProvider(project);
		workspaceViewer.setLabelProvider(workbenchLabelProvider);
		workspaceViewer.getControl().addDisposeListener(new DisposeListener()
		{
			public void widgetDisposed(DisposeEvent e)
			{
				workbenchLabelProvider.doDispose();
			}
		});
		workspaceViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		workspaceViewer.setContentProvider(new ArrayContentProvider());
		Composite buttonsComp = new Composite(projectPart, SWT.NONE);
		buttonsComp.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		buttonsComp.setLayout(new GridLayout(1, false));
		Button add = new Button(buttonsComp, SWT.NONE);
		add.setText(Messages.BuildPathEditingComposite_AddProjectTitle);
		add.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				WorkspaceFolderSelectionDialog dialog = new WorkspaceFolderSelectionDialog(Display.getCurrent()
						.getActiveShell());
				dialog.setTitle(Messages.BuildPathEditingComposite_ProjectsSelectionTitle);
				dialog.setMessage(Messages.BuildPathEditingComposite_AddProjectDesription);
				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				dialog.setInput(workspace);

				// filtering out current project and its contents
				dialog.addFilter(new ViewerFilter()
				{

					@Override
					public boolean select(Viewer viewer, Object parentElement, Object element)
					{
						if (element instanceof IResource)
						{
							if (project.equals(((IResource) element).getProject()))
							{
								return false;
							}
						}

						return true;
					}

				});
				if (dialog.open() == Dialog.OK)
				{
					addWithValidation(workspaceViewer, dialog.getResult());
				}
			}
		});
		add.setLayoutData(GridDataFactory.fillDefaults().hint(100, -1).create());
		final Button remove = new Button(buttonsComp, SWT.NONE);
		remove.setText(Messages.BuildPathEditingComposite_RemoveProjectTitle);
		remove.setEnabled(false);
		workspaceViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			public void selectionChanged(SelectionChangedEvent event)
			{
				if (project != null)
				{
					// Do not allow removal in case the default project is selected
					remove.setEnabled(!event.getSelection().isEmpty()
							&& !((IStructuredSelection) event.getSelection()).toList().contains(project));
				}
				else
				{
					remove.setEnabled(!event.getSelection().isEmpty());
				}
			}
		});
		remove.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				IStructuredSelection sel = (IStructuredSelection) workspaceViewer.getSelection();
				Object[] selections = sel.toArray();
				for (Object selected : selections)
				{
					workspaceViewer.remove(selected);
				}
			}
		});
		remove.setLayoutData(GridDataFactory.fillDefaults().hint(100, -1).create());
		workspaceViewer.setSorter(new ViewerSorter());
	}

	/**
	 * Initializes composite.
	 * 
	 * @param workspaceResources
	 *            - workspace resources.
	 * @param directories
	 *            - external directories.
	 */
	public void init(List<IResource> workspaceResources, List<File> directories)
	{
		if (project != null && !workspaceResources.contains(project))
		{
			workspaceResources.add(0, project);
		}
		workspaceViewer.setInput(workspaceResources.toArray());
		directoryViewer.setInput(directories.toArray());
	}

	/**
	 * Add an item to the table viewer after verifying that the path of that item does not exist or is not containing
	 * the path for this item.
	 * 
	 * @param tableViewer
	 * @param item
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	private void addWithValidation(TableViewer tableViewer, Object item)
	{
		propertyPage.setMessage(null);
		TableItem[] tableItems = tableViewer.getTable().getItems();
		if (tableItems == null || tableItems.length == 0)
		{
			// Just add
			if (item instanceof Object[])
			{
				Object[] items = (Object[]) item;
				for (Object obj : items)
				{
					tableViewer.add(obj);
					return;
				}
			}
			else
			{
				if (item != null)
				{
					tableViewer.add(item);
					return;
				}
			}
		}
		List toAdd = new ArrayList();
		boolean hasAlreadyIncludedPaths = false;
		if (item instanceof File)
		{
			File directoryAdded = (File) item;
			// Check for files that are already in the table.
			boolean shouldAdd = true;
			for (TableItem tableItem : tableItems)
			{
				Object data = tableItem.getData();
				if (data instanceof File)
				{
					File existinsDirectory = (File) data;
					if (existinsDirectory.equals(directoryAdded)
							|| directoryAdded.getAbsolutePath().startsWith(existinsDirectory.getAbsolutePath()))
					{
						hasAlreadyIncludedPaths = true;
						shouldAdd = false;
						break;
					}
				}
			}
			if (shouldAdd)
			{
				toAdd.add(directoryAdded);
			}
		}
		else if (item instanceof Object[])
		{
			Object[] elements = (Object[]) item;

			for (Object element : elements)
			{
				boolean shouldAdd = true;
				for (TableItem tableItem : tableItems)
				{
					if (element instanceof IResource && tableItem.getData() instanceof IResource)
					{
						IResource resource = (IResource) element;
						IResource resourceInTable = (IResource) tableItem.getData();
						if ((resourceInTable).getFullPath().isPrefixOf(resource.getFullPath()))
						{
							hasAlreadyIncludedPaths = true;
							shouldAdd = false;
							break;
						}
					}
				}
				if (shouldAdd)
				{
					toAdd.add(element);
				}
			}
		}
		tableViewer.add(toAdd.toArray());
		if (hasAlreadyIncludedPaths)
		{
			propertyPage.setMessage(Messages.BuildPathEditingComposite_selectionIncludedNotice, DialogPage.INFORMATION);
			Job job = new UIJob("Build-Path::Remove message") //$NON-NLS-1$
			{
				public IStatus runInUIThread(IProgressMonitor monitor)
				{
					if (propertyPage != null && !propertyPage.getControl().isDisposed())
					{
						propertyPage.setMessage(null);
					}
					return Status.OK_STATUS;
				}
			};
			job.schedule(4000); // wait 4 seconds before removing the message
		}
	}

	/**
	 * Fills composite result.
	 * 
	 * @param workspaceResources
	 *            - workspace resource to fill.
	 * @param fileList
	 *            - external folders to fill.
	 */
	public void fillResult(List<IResource> workspaceResources, List<File> fileList)
	{
		TableItem[] items = workspaceViewer.getTable().getItems();
		for (TableItem i : items)
		{
			IResource resource = (IResource) i.getData();
			if (resource != project)
			{
				workspaceResources.add(resource);
			}
		}
		items = directoryViewer.getTable().getItems();
		for (TableItem i : items)
		{
			fileList.add((File) i.getData());
		}
	}

	public void init(ProjectDependencies buildPath)
	{
		init(buildPath.getWorkspaceResources(), buildPath.getDirectories());
		usesCustomLibs = buildPath.isUsesCustomLibs();
		List<String> notUsedLibrariesIds = buildPath.getNotUsedLibrariesIds();
		notusedLibraries = new HashSet<String>(notUsedLibrariesIds);
		customLibsButton.setSelection(usesCustomLibs);
		for (IPHPLibrary l : LibraryManager.getInstance().getAllLibraries())
		{
			if (!notusedLibraries.contains(l.getId()))
			{
				libraryViewer.setChecked(l, true);
			}
		}
		libraryViewer.getControl().setEnabled(usesCustomLibs);
		select.setEnabled(usesCustomLibs);
		deselect.setEnabled(usesCustomLibs);
	}

	public void fillResult(ProjectDependencies bp)
	{
		List<IResource> ps = new ArrayList<IResource>();
		List<File> fs = new ArrayList<File>();
		fillResult(ps, fs);
		bp.set(ps, fs);
		bp.setUsesCustomLibs(customLibsButton.getSelection());
		List<String> notUsed = new ArrayList<String>();
		for (IPHPLibrary l : LibraryManager.getInstance().getAllLibraries())
		{
			if (!libraryViewer.getChecked(l))
			{
				notUsed.add(l.getId());
			}
		}
		bp.setNotUsedLibrariesIds(notUsed);
	}

	private final class SelectAction implements SelectionListener
	{

		boolean select;

		private SelectAction(boolean doSelect)
		{
			this.select = doSelect;
		}

		public void widgetDefaultSelected(SelectionEvent e)
		{

		}

		public void widgetSelected(SelectionEvent e)
		{
			libraryViewer.setAllChecked(select);
		}
	}

}
