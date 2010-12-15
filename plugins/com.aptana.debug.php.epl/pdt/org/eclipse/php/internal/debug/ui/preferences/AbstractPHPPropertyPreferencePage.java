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
package org.eclipse.php.internal.debug.ui.preferences;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.ControlEnableState;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.php.internal.core.util.ScrolledCompositeImpl;
import org.eclipse.php.internal.ui.PHPUIMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceSorter;

public abstract class AbstractPHPPropertyPreferencePage extends PropertyPage implements IWorkbenchPreferencePage {
	/*
	 * Disable link data, prevents the display of a "workspace" or "project"
	 * settings link to prevent recursive dialog launching
	 */
	private static final Object DISABLE_LINK = "DISABLE_LINK"; //$NON-NLS-1$

	protected Map fData = null;
	protected Button fEnableProjectSettings;
	private Link fProjectSettingsLink;

	public AbstractPHPPropertyPreferencePage() {
		super();
	}

	public final void applyData(Object data) {
		super.applyData(data);
		if (data instanceof Map) {
			fData = (Map) data;
			updateLinkEnablement();
		}
	}

	protected Control createWorkspaceContents(Composite composite) {
		ScrolledCompositeImpl scrolledCompositeImpl = new ScrolledCompositeImpl(composite, SWT.V_SCROLL | SWT.H_SCROLL);
		Composite group = new Composite(scrolledCompositeImpl, SWT.NONE);
		group.setLayout(new GridLayout());
		return scrolledCompositeImpl;
	}

	protected Control createProjectContents(Composite composite) {
		ScrolledCompositeImpl scrolledCompositeImpl = new ScrolledCompositeImpl(composite, SWT.V_SCROLL | SWT.H_SCROLL);
		Composite group = new Composite(scrolledCompositeImpl, SWT.NONE);
		group.setLayout(new GridLayout());
		return scrolledCompositeImpl;
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	public final Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);

		GridLayout layout = new GridLayout();
		layout.marginHeight=0;
		layout.marginWidth=0;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(data);
		if (hasLink()){
		Composite checkLinkComposite = new Composite(composite, SWT.NONE);
		checkLinkComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		checkLinkComposite.setLayout(new GridLayout(2, false));

		if (getProject() != null) {
			fEnableProjectSettings = new Button(checkLinkComposite, SWT.CHECK);
			fEnableProjectSettings.setText(PHPUIMessages.getString("AbstractPHPPropertyPreferencePage.0")); //$NON-NLS-1$
			fEnableProjectSettings.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
			boolean enabledForProject = createPreferenceScopes()[0].getNode(getPreferenceNodeQualifier()).getBoolean(getProjectSettingsKey(), false);
			fEnableProjectSettings.setSelection(enabledForProject);
		} else {
			Label spacer = new Label(checkLinkComposite, SWT.CHECK);
			spacer.setLayoutData(new GridData());
		}
		if (hasLink()){

		fProjectSettingsLink = new Link(checkLinkComposite, SWT.NONE);
		fProjectSettingsLink.setFont(composite.getFont());
		fProjectSettingsLink.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, true, false));

		/*
		 * "element" should be a project, if null, link to per-project
		 * properties
		 */
		if (getProject() != null) {
			fProjectSettingsLink.setText("<a>" + PHPUIMessages.getString("AbstractPHPPropertyPreferencePage.1") + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
		} else {
			fProjectSettingsLink.setText("<a>" + PHPUIMessages.getString("AbstractPHPPropertyPreferencePage.2") + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
		}
		
		updateLinkEnablement();

		fProjectSettingsLink.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				if (getProject() == null) {
					openProjectSettings();
				} else {
					openWorkspaceSettings();
				}
			}

		});
		}
		}
		if (getProject() != null) {
//			Label line = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
//			line.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
		}

//		final Control common = createProjectContents(composite);
//		common.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		if (fEnableProjectSettings == null) {
			final Control workspaceControls = createWorkspaceContents(composite);
			workspaceControls.setLayoutData(new GridData(GridData.FILL_BOTH));
		} else {
			SelectionAdapter selectionAdapter = new SelectionAdapter() {
				ControlEnableState enablements = null;

				public void widgetSelected(SelectionEvent e) {
					super.widgetSelected(e);
					if (fEnableProjectSettings.getSelection()) {
						if (enablements != null) {
							enablements.restore();
							enablements = null;
						}
					} else { 
						//enablements = ControlEnableState.disable(common);
					}
				}
			};
			selectionAdapter.widgetSelected(null);
			fEnableProjectSettings.addSelectionListener(selectionAdapter);
		}
		return composite;
	}

	protected boolean hasLink()
	{
		return true;
	}

	public void createControl(Composite parent) {
		if (getProject() != null) {
			noDefaultAndApplyButton();
		}
		super.createControl(parent);
	}

	protected IScopeContext[] createPreferenceScopes() {
		IProject project = getProject();
		if (project != null) {
			return new IScopeContext[] { new ProjectScope(project), new InstanceScope(), new DefaultScope() };
		}
		return new IScopeContext[] { new InstanceScope(), new DefaultScope() };
	}

	protected abstract String getPreferenceNodeQualifier();

	protected abstract String getPreferencePageID();

	protected IProject getProject() {
		if (getElement() != null && getElement() instanceof IProject) {
			return (IProject) getElement();
		}
		return null;
	}

	protected abstract String getProjectSettingsKey();

	protected abstract String getPropertyPageID();

	protected boolean isElementSettingsEnabled() {
		return fEnableProjectSettings != null && fEnableProjectSettings.getSelection();
	}

	void openProjectSettings() {
		ListDialog dialog = new ListDialog(getShell()) {

			protected Control createDialogArea(Composite container) {
				Control area = super.createDialogArea(container);
				getTableViewer().setSorter(new ResourceSorter(ResourceSorter.NAME));
				return area;
			}
		};
		dialog.setMessage(PHPUIMessages.getString("AbstractPHPPropertyPreferencePage.3")); //$NON-NLS-1$
		dialog.setContentProvider(new IStructuredContentProvider() {
			public void dispose() {
			}

			public Object[] getElements(Object inputElement) {
				return ((IWorkspace) inputElement).getRoot().getProjects();
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
		dialog.setLabelProvider(new DecoratingLabelProvider(new WorkbenchLabelProvider(), PlatformUI.getWorkbench().getDecoratorManager().getLabelDecorator()));
		dialog.setInput(ResourcesPlugin.getWorkspace());
		dialog.setTitle(PHPUIMessages.getString("AbstractPHPPropertyPreferencePage.4")); //$NON-NLS-1$
		if (dialog.open() == Window.OK) {
			Object[] result = dialog.getResult();
			if (result.length > 0) {
				IProject project = (IProject) dialog.getResult()[0];
				Map data = new HashMap();
				data.put(DISABLE_LINK, Boolean.TRUE);
				PreferencesUtil.createPropertyDialogOn(getShell(), project, getPropertyPageID(), new String[] { getPropertyPageID() }, data).open();
			}
		}
	}

	void openWorkspaceSettings() {
		Map data = new HashMap();
		data.put(DISABLE_LINK, Boolean.TRUE);
		PreferencesUtil.createPreferenceDialogOn(getShell(), getPreferencePageID(), new String[] { getPreferencePageID() }, data).open();
	}

	public boolean performOk() {
		boolean ok = super.performOk();

		IScopeContext[] preferenceScopes = createPreferenceScopes();
		if (getProject() != null) {
			if (isElementSettingsEnabled()) {
				preferenceScopes[0].getNode(getPreferenceNodeQualifier()).putBoolean(getProjectSettingsKey(), fEnableProjectSettings.getSelection());
			} else {
				preferenceScopes[0].getNode(getPreferenceNodeQualifier()).remove(getProjectSettingsKey());
			}
		}

//		if (projectScopeAddons != null) {
//			for (int i = 0; i < projectScopeAddons.length; i++) {
//				projectScopeAddons[i].performOK(isElementSettingsEnabled());
//			}
//		}
//		if (workspaceAddons != null) {
//			for (int i = 0; i < workspaceAddons.length; i++) {
//				workspaceAddons[i].performOK(false);
//			}
//		}
		return ok;
	}

	public void performDefaults() {
//		for (int i = 0; i < projectScopeAddons.length; i++) {
//			projectScopeAddons[i].performDefaults();
//		}
//		for (int i = 0; i < workspaceAddons.length; i++) {
//			workspaceAddons[i].performDefaults();
//		}
		super.performDefaults();
	}

	public void performApply() {
		super.performApply(); // Will execute the preformOK()
	}

	private void updateLinkEnablement() {
		if (fData != null && fProjectSettingsLink != null) {
			fProjectSettingsLink.setEnabled(!Boolean.TRUE.equals(fData.get(DISABLE_LINK)));
		}
	}
}
