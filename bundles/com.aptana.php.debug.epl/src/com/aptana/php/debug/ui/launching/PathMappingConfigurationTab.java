/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.ui.launching;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org2.eclipse.php.debug.core.debugger.pathmapper.PathMapper;
import org2.eclipse.php.internal.debug.core.pathmapper.PathMapperRegistry;
import org2.eclipse.php.internal.debug.ui.pathmapper.PathMappingComposite;

import com.aptana.core.util.StringUtil;
import com.aptana.editor.php.epl.PHPEplPlugin;
import com.aptana.php.debug.core.IPHPDebugCorePreferenceKeys;
import com.aptana.php.debug.core.server.PHPServersManager;
import com.aptana.php.debug.epl.PHPDebugEPLPlugin;
import com.aptana.php.debug.ui.pathmapper.PathMapperDialog;
import com.aptana.ui.util.SWTUtils;
import com.aptana.webserver.core.IServer;

/**
 * A PHP server Path Mapping tab that should be displayed on the launch configuration for the remote debugging.
 * 
 * @author Shalom Gibly
 */
public class PathMappingConfigurationTab extends AbstractLaunchConfigurationTab
{
	private static final String PATH_MAPPING_ICON = "/icons/full/obj16/path_mapping.gif"; //$NON-NLS-1$
	/*
	 * TODO: 1. Add a link at the bottom of this tab that presents the actual path to the first page 2. Notify the
	 * ServerLaunchConfigurationTab about the change, and in case needed, make the configuration tab change the path.
	 * Then force apply. 3. Display the other launch configurations that are using this server and mark them as changed
	 * when
	 */
	private PathMappingComposite pathMappingPreview;
	private Link debugLinkPreview; // TODO: SG - Add this debug URL preview at the bottom of this tab
	private ILaunchConfiguration workingCopy;

	/**
	 * Constructs a new PHPServerMappingConfigurationTab
	 */
	public PathMappingConfigurationTab()
	{
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label msg1 = new Label(composite, SWT.WRAP);
		msg1.setText(PathMappingMessages.PathMappingConfigurationTab_mappingTableDescription);
		msg1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Link link = new Link(composite, SWT.NONE);
		link.setText(PathMappingMessages.PathMappingConfigurationTab_changeMappingLink);
		link.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		link.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				IServer server = getServer();
				if (server != null)
				{
					PathMapperDialog dialog = new PathMapperDialog(getShell(), server);
					if (dialog.open() == Window.OK)
					{
						// Notify the change to all other launch configurations that use this server.
						PathMappingUpdater pathMappingUpdater = new PathMappingUpdater();
						pathMappingUpdater.updatePaths(server, new String[] { workingCopy.getName() });
						// We must set the working copy with the new values before we call for an update on the rest of
						// the tabs
						// The same rules also apply here. We should not modify the working copy if it does not use a
						// specific file
						// or does not auto-generate the script when using a specific file.
						try
						{
							if (workingCopy.getAttribute(IPHPDebugCorePreferenceKeys.ATTR_USE_SPECIFIC_FILE, false)
									&& workingCopy.getAttribute(IPHPDebugCorePreferenceKeys.ATTR_AUTO_GENERATED_URL,
											false))
							{
								// Update the current launch config
								PathMappingUpdater.updateConfigurations(Arrays.asList(workingCopy), server);
								// Update the preview table and the other tabs
								ILaunchConfigurationTab[] tabs = getLaunchConfigurationDialog().getTabs();
								for (ILaunchConfigurationTab tab : tabs)
								{
									tab.initializeFrom(workingCopy);
								}
							}
							else
							{
								// just update this tab (this will update the preview table)
								initializeFrom(workingCopy);
							}
						}
						catch (CoreException ce)
						{
							PHPDebugEPLPlugin.logError("Error updating the configurations", ce); //$NON-NLS-1$
						}
					}
				}
				else
				{
					MessageDialog.openInformation(getShell(),
							PathMappingMessages.PathMappingConfigurationTab_noServersDialogTitle,
							PathMappingMessages.PathMappingConfigurationTab_noServersDialogMessage);
				}
			}
		});
		// Add the path mapper table preview
		pathMappingPreview = new PathMappingComposite(composite, SWT.NO_FOCUS, false);
		pathMappingPreview.setLayoutData(new GridData(GridData.FILL_BOTH));

		// // Add the debug link preview at the bottom
		// Label l = new Label(composite, SWT.WRAP);
		// link.setText("test test test test test test test test test vtest ");
		// link.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		setControl(composite);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getImage()
	 */
	public Image getImage()
	{
		return SWTUtils.getImage(PHPEplPlugin.getDefault(), PATH_MAPPING_ICON);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName()
	{
		return PathMappingMessages.PathMappingConfigurationTab_tabName;
	}

	/**
	 * Returns the server for the working copy, or null if the working copy is null or a server was not found.
	 * 
	 * @return IServer
	 */
	private IServer getServer()
	{
		if (workingCopy != null)
		{
			try
			{
				String serverName = workingCopy.getAttribute(IPHPDebugCorePreferenceKeys.ATTR_SERVER_NAME,
						StringUtil.EMPTY);
				if (!StringUtil.isEmpty(serverName))
				{
					return PHPServersManager.getServer(serverName);
				}
			}
			catch (CoreException e)
			{
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.debug.ui.AbstractLaunchConfigurationTab#activated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy
	 * )
	 */
	public void activated(ILaunchConfigurationWorkingCopy workingCopy)
	{
		this.workingCopy = workingCopy;
		super.activated(workingCopy);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration)
	{
		workingCopy = configuration;
		if (pathMappingPreview != null)
		{
			try
			{
				String serverName = configuration.getAttribute(IPHPDebugCorePreferenceKeys.ATTR_SERVER_NAME,
						StringUtil.EMPTY);
				if (!StringUtil.isEmpty(serverName))
				{
					IServer server = PHPServersManager.getServer(serverName);
					if (server != null)
					{
						PathMapper mapper = PathMapperRegistry.getByServer(server);
						if (mapper != null)
						{
							pathMappingPreview.setData(mapper.getMapping());
							return;
						}
					}
				}
			}
			catch (CoreException e)
			{
				PHPDebugEPLPlugin.logError("Error initializing the Path Mapper configuration tab", e);//$NON-NLS-1$
			}
			// If we got here, return null to display a cleared list of mapping.
			pathMappingPreview.setData(null);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration)
	{
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration)
	{
	}
}
