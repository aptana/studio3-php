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
package org2.eclipse.php.internal.debug.ui.preferences;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.internal.forms.widgets.FormUtil;
import org.osgi.service.prefs.BackingStoreException;
import org2.eclipse.php.internal.debug.core.interpreter.preferences.PHPexeItem;
import org2.eclipse.php.internal.debug.core.preferences.PHPDebugCorePreferenceNames;
import org2.eclipse.php.internal.debug.core.preferences.PHPDebuggersRegistry;
import org2.eclipse.php.internal.debug.core.preferences.PHPProjectPreferences;
import org2.eclipse.php.internal.debug.core.preferences.PHPexes;
import org2.eclipse.php.internal.debug.core.xdebug.communication.XDebugCommunicationDaemon;
import org2.eclipse.php.internal.debug.core.zend.communication.DebuggerCommunicationDaemon;
import org2.eclipse.php.internal.debug.ui.PHPDebugUIMessages;

import com.aptana.editor.php.util.ScrolledPageContent;
import com.aptana.php.debug.core.IPHPDebugCorePreferenceKeys;
import com.aptana.php.debug.core.preferences.PHPDebugPreferencesUtil;
import com.aptana.php.debug.core.server.PHPServersManager;
import com.aptana.php.debug.epl.PHPDebugEPLPlugin;
import com.aptana.webserver.core.IServer;

/**
 * PHP debug options preferences add-on. This add-on specifies the default debugger, executable and server for the
 * workspace or the project specific.
 * 
 * @author Shalom Gibly
 */
public class PHPDebugPreferencesAddon extends AbstractPHPPreferencePageBlock
{

	private static final String SERVERS_PAGE_ID = "org2.eclipse.php.server.internal.ui.PHPServersPreferencePage"; //$NON-NLS-1$
	private static final String PHP_EXE_PAGE_ID = "org2.eclipse.php.debug.ui.preferencesphps.PHPsPreferencePage"; //$NON-NLS-1$
	private Button fStopAtFirstLine;
	private Text fClientIP;
	private Label fClientIPLabel;
	private Combo fDefaultDebugger;
	private Combo fDefaultServer;
	private Combo fDefaultPHPExe;
	private Collection<String> debuggersIds;
	// private EncodingSettings fDebugEncodingSettings;
	// private EncodingSettings fOutputEncodingSettings;
	private PreferencePage propertyPage;
	private ExpandableComposite expandbleDebugEncoding;
	private ExpandableComposite expandbleOutputEncoding;

	public void setCompositeAddon(Composite parent)
	{
		Composite composite = this.addPageContents(parent);
		this.addProjectPreferenceSubsection(this.createSubsection(composite,
				PHPDebugUIMessages.PhpDebugPreferencePage_6));
	}

	public void initializeValues(PreferencePage propertyPage)
	{
		this.propertyPage = propertyPage;
		IScopeContext[] preferenceScopes = this.createPreferenceScopes(propertyPage);

		boolean stopAtFirstLine = PHPDebugPreferencesUtil.getBoolean(PHPDebugCorePreferenceNames.STOP_AT_FIRST_LINE,
				true);
		String debuggerName = PHPDebuggersRegistry.getDebuggerName(PHPDebugPreferencesUtil.getString(
				IPHPDebugCorePreferenceKeys.PHP_DEBUGGER_ID, XDebugCommunicationDaemon.XDEBUG_DEBUGGER_ID));
		String serverName = PHPServersManager.getDefaultServer(null).getName();
		PHPexes exes = PHPexes.getInstance();
		String phpExeName = PHPDebugUIMessages.PhpDebugPreferencePage_noExeDefined;
		if (exes.hasItems(PHPDebugEPLPlugin.getCurrentDebuggerId()))
		{
			phpExeName = exes.getDefaultItem(PHPDebugEPLPlugin.getCurrentDebuggerId()).getName();
		}
		String transferEncoding = PHPDebugPreferencesUtil.getString(PHPDebugCorePreferenceNames.TRANSFER_ENCODING,
				"UTF-8"); //$NON-NLS-1$
		String outputEncoding = PHPDebugPreferencesUtil.getString(PHPDebugCorePreferenceNames.OUTPUT_ENCODING, "UTF-8"); //$NON-NLS-1$
		this.loadDebuggers(this.fDefaultDebugger);
		this.loadServers(this.fDefaultServer);
		boolean exeLoaded = false;
		// Update the values in case we have a project-specific settings.
		if (preferenceScopes[0] instanceof ProjectScope)
		{
			IEclipsePreferences node = preferenceScopes[0].getNode(this.getPreferenceNodeQualifier());
			if ((node != null) && (this.getProject(propertyPage) != null))
			{
				String projectServerName = PHPServersManager.getDefaultServer(this.getProject(propertyPage)).getName();
				if (!projectServerName.equals("")) { //$NON-NLS-1$
					String debuggerId = node.get(IPHPDebugCorePreferenceKeys.PHP_DEBUGGER_ID,
							PHPDebugEPLPlugin.getCurrentDebuggerId());
					debuggerName = PHPDebuggersRegistry.getDebuggerName(debuggerId);
					serverName = projectServerName;
					stopAtFirstLine = node.getBoolean(PHPDebugCorePreferenceNames.STOP_AT_FIRST_LINE, stopAtFirstLine);
					transferEncoding = node.get(PHPDebugCorePreferenceNames.TRANSFER_ENCODING, ""); //$NON-NLS-1$
					outputEncoding = node.get(PHPDebugCorePreferenceNames.OUTPUT_ENCODING, ""); //$NON-NLS-1$
					phpExeName = node.get(PHPDebugCorePreferenceNames.DEFAULT_PHP, phpExeName);
					// Check that if the project had a non-defined exe, and now there is one that is valid. we set
					// it with the new valid default exe.
					if (PHPDebugUIMessages.PhpDebugPreferencePage_noExeDefined.equals(phpExeName))
					{
						if (exes.hasItems(debuggerId))
						{
							phpExeName = exes.getDefaultItem(debuggerId).getName();
							node.put(PHPDebugCorePreferenceNames.DEFAULT_PHP, phpExeName);
							try
							{
								node.flush();
							}
							catch (BackingStoreException e)
							{
							}
						}
					}
					loadPHPExes(
							this.fDefaultPHPExe,
							exes.getItems(node.get(IPHPDebugCorePreferenceKeys.PHP_DEBUGGER_ID,
									PHPDebugEPLPlugin.getCurrentDebuggerId())));
					exeLoaded = true;
				}
			}
		}
		if (!exeLoaded)
		{
			loadPHPExes(this.fDefaultPHPExe, exes.getItems(PHPDebugEPLPlugin.getCurrentDebuggerId()));
		}
		this.fStopAtFirstLine.setSelection(stopAtFirstLine);
		this.fClientIP.setText(PHPDebugPreferencesUtil.getString(PHPDebugCorePreferenceNames.CLIENT_IP, "127.0.0.1")); //$NON-NLS-1$
		this.fDefaultDebugger.select(this.fDefaultDebugger.indexOf(debuggerName));
		this.fDefaultServer.select(this.fDefaultServer.indexOf(serverName));
		this.fDefaultPHPExe.select(this.fDefaultPHPExe.indexOf(phpExeName));
		// fDebugEncodingSettings.setIANATag(transferEncoding);
		// fOutputEncodingSettings.setIANATag(outputEncoding);

		if (this.getProject(propertyPage) != null)
		{
			this.fClientIP.setVisible(false);
			this.fClientIPLabel.setVisible(false);
		}
	}

	public boolean performOK(boolean isProjectSpecific)
	{
		this.savePreferences(isProjectSpecific);
		return true;
	}

	public void performApply(boolean isProjectSpecific)
	{
		this.performOK(isProjectSpecific);
	}

	public boolean performCancel()
	{
		return true;
	}

	public void performDefaults()
	{
		IEclipsePreferences prefs = new DefaultScope().getNode(PHPDebugEPLPlugin.PLUGIN_ID);
		this.fStopAtFirstLine.setSelection(prefs.getBoolean(PHPDebugCorePreferenceNames.STOP_AT_FIRST_LINE, true));
		this.fClientIP.setText(prefs.get(PHPDebugCorePreferenceNames.CLIENT_IP, "127.0.0.1")); //$NON-NLS-1$
		this.loadDebuggers(this.fDefaultDebugger);
		this.loadServers(this.fDefaultServer);
		loadPHPExes(this.fDefaultPHPExe, PHPexes.getInstance().getItems(PHPDebugEPLPlugin.getCurrentDebuggerId()));
		// fDebugEncodingSettings.setIANATag(prefs.getDefaultString(PHPDebugCorePreferenceNames.TRANSFER_ENCODING));
		// fOutputEncodingSettings.setIANATag(prefs.getDefaultString(PHPDebugCorePreferenceNames.OUTPUT_ENCODING));
	}

	protected String getPreferenceNodeQualifier()
	{
		return PHPProjectPreferences.getPreferenceNodeQualifier();
	}

	private void addProjectPreferenceSubsection(Composite composite)
	{
		// Set a height hint for the group.
		GridData gd = (GridData) composite.getLayoutData();
		gd.heightHint = 260;
		composite.setLayoutData(gd);
		this.addLabelControl(composite, PHPDebugUIMessages.PhpDebugPreferencePage_phpDebugger,
				IPHPDebugCorePreferenceKeys.PHP_DEBUGGER_ID);
		this.fDefaultDebugger = this.addCombo(composite, 2);
		new Label(composite, SWT.NONE); // dummy label
		this.addLabelControl(composite, PHPDebugUIMessages.PhpDebugPreferencePage_9,
				PHPServersManager.DEFAULT_SERVER_PREFERENCES_KEY);
		this.fDefaultServer = this.addCombo(composite, 2);
		this.addLink(composite, PHPDebugUIMessages.PhpDebugPreferencePage_serversLink,
				PHPDebugPreferencesAddon.SERVERS_PAGE_ID);
		this.addLabelControl(composite, PHPDebugUIMessages.PhpDebugPreferencePage_12,
				PHPDebugCorePreferenceNames.DEFAULT_PHP);
		this.fDefaultPHPExe = this.addCombo(composite, 2);
		this.addLink(composite, PHPDebugUIMessages.PhpDebugPreferencePage_installedPHPsLink,
				PHPDebugPreferencesAddon.PHP_EXE_PAGE_ID);

		final ScrolledPageContent sc1 = new ScrolledPageContent(composite);
		Composite comp = sc1.getBody();
		GridLayout layout = new GridLayout(3, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		comp.setLayout(layout);

		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		sc1.setLayoutData(gd);

		this.expandbleDebugEncoding = this.createStyleSection(comp,
				PHPDebugUIMessages.PHPDebugPreferencesAddon_debugTransferEncoding, 3);
		Composite inner = new Composite(this.expandbleDebugEncoding, SWT.NONE);
		inner.setFont(composite.getFont());
		inner.setLayout(new GridLayout(3, false));
		this.expandbleDebugEncoding.setClient(inner);
		// fDebugEncodingSettings = addEncodingSettings(inner,
		// PHPDebugUIMessages.PHPDebugPreferencesAddon_selectedEncoding);

		this.expandbleOutputEncoding = this.createStyleSection(comp,
				PHPDebugUIMessages.PHPDebugPreferencesAddon_debugOutputEncoding, 3);
		inner = new Composite(this.expandbleOutputEncoding, SWT.NONE);
		inner.setFont(composite.getFont());
		inner.setLayout(new GridLayout(3, false));
		this.expandbleOutputEncoding.setClient(inner);
		// fOutputEncodingSettings = addEncodingSettings(inner,
		// PHPDebugUIMessages.PHPDebugPreferencesAddon_selectedEncoding);
		// expandbleOutputEncoding.setText(PHPDebugUIMessages.PHPDebugPreferencesAddon_debugOutputEncoding + " (" +
		// fOutputEncodingSettings.getIANATag() + ")");
		this.fStopAtFirstLine = this.addCheckBox(composite, PHPDebugUIMessages.PhpDebugPreferencePage_1,
				PHPDebugCorePreferenceNames.STOP_AT_FIRST_LINE, 0);

		this.fClientIPLabel = new Label(composite, SWT.NONE);
		this.fClientIPLabel.setText("Client Host/IP:");
		this.fClientIP = new Text(composite, SWT.BORDER);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		this.fClientIP.setLayoutData(layoutData);

		Display.getDefault().asyncExec(new Runnable()
		{
			public void run()
			{
				// Expand the debug encoding after the component is layout.
				// This code fixes an issue that caused the top encoding combo to scroll automatically
				// without any reasonable cause.
				PHPDebugPreferencesAddon.this.expandbleDebugEncoding.setExpanded(true);
				ScrolledPageContent spc = (ScrolledPageContent) FormUtil
						.getScrolledComposite(PHPDebugPreferencesAddon.this.expandbleDebugEncoding);
				Point p = spc.getSize();
				spc.setSize(p.x, 70);
				spc.getParent().layout();
			}
		});

		// Add a default debugger listener that will update the possible executables
		// and, maybe, servers that can work with this debugger.
		this.fDefaultDebugger.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				String selectedDebugger = PHPDebugPreferencesAddon.this.getSelectedDebuggerId();
				PHPexeItem[] items = PHPexes.getInstance().getItems(selectedDebugger);
				loadPHPExes(PHPDebugPreferencesAddon.this.fDefaultPHPExe, items);
			}
		});
	}

	private void loadPHPExes(Combo combo, PHPexeItem[] items)
	{
		combo.removeAll();
		if ((items == null) || (items.length == 0))
		{
			combo.add(PHPDebugUIMessages.PhpDebugPreferencePage_noExeDefined);
			combo.select(0);
			return;
		}
		for (PHPexeItem element : items)
		{
			combo.add(element.getName());
		}
		// select the default item for the current selected debugger
		if (this.fDefaultDebugger.getItemCount() > 0)
		{
			PHPexeItem defaultItem = PHPexes.getInstance().getDefaultItem(this.getSelectedDebuggerId());
			String defaultItemName;
			if (defaultItem != null)
			{
				defaultItemName = defaultItem.getName();
			}
			else
			{
				defaultItemName = PHPDebugUIMessages.PHPDebuggersTable_notDefined;
				if (combo.indexOf(defaultItemName) == -1)
				{
					combo.add(defaultItemName, 0);
					combo.select(0);
				}
			}
			int index = combo.indexOf(defaultItemName);
			if (index > -1)
			{
				combo.select(index);
			}
			else if (combo.getItemCount() > 0)
			{
				// select first item in list
				combo.select(0);
			}
		}

	}

	private void loadServers(Combo combo)
	{
		combo.removeAll();
		List<IServer> servers = PHPServersManager.getServers();
		if (servers != null)
		{
			for (IServer element : servers)
			{
				combo.add(element.getName());
			}
			// select first item in list
			if (combo.getItemCount() > 0)
			{
				combo.select(0);
			}
		}
	}

	private void loadDebuggers(Combo combo)
	{
		this.debuggersIds = PHPDebuggersRegistry.getDebuggersIds();
		String defaultDebuggerID = DebuggerCommunicationDaemon.ZEND_DEBUGGER_ID;
		combo.removeAll();
		Iterator<String> debuggers = this.debuggersIds.iterator();
		int defaultIndex = 0;
		int index = 0;
		while (debuggers.hasNext())
		{
			String id = debuggers.next();
			if (defaultDebuggerID.equals(id))
			{
				defaultIndex = index;
			}
			else
			{
				index++;
			}
			String debuggerName = PHPDebuggersRegistry.getDebuggerName(id);
			combo.add(debuggerName);
		}
		// select the default item in list
		if (combo.getItemCount() > 0)
		{
			combo.select(defaultIndex);
		}
	}

	private ExpandableComposite createStyleSection(Composite parent, String label, int nColumns)
	{
		ExpandableComposite excomposite = new ExpandableComposite(parent, SWT.NONE, ExpandableComposite.TWISTIE
				| ExpandableComposite.CLIENT_INDENT);
		excomposite.setText(label);
		excomposite.setExpanded(false);
		excomposite.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));
		excomposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, nColumns, 1));
		excomposite.addExpansionListener(new ExpansionAdapter()
		{
			public void expansionStateChanged(ExpansionEvent e)
			{
				PHPDebugPreferencesAddon.this.expandedStateChanged((ExpandableComposite) e.getSource());
			}
		});
		return excomposite;
	}

	private void expandedStateChanged(ExpandableComposite expandable)
	{
		// if (expandable.isExpanded()) {
		// if (expandable == expandbleDebugEncoding) {
		// expandbleDebugEncoding.setText(PHPDebugUIMessages.PHPDebugPreferencesAddon_debugTransferEncoding);
		// expandbleOutputEncoding.setText(PHPDebugUIMessages.PHPDebugPreferencesAddon_debugOutputEncoding + " (" +
		// fOutputEncodingSettings.getIANATag() + ")");
		// expandbleOutputEncoding.setExpanded(false);
		// } else {
		// expandbleOutputEncoding.setText(PHPDebugUIMessages.PHPDebugPreferencesAddon_debugOutputEncoding);
		// expandbleDebugEncoding.setText(PHPDebugUIMessages.PHPDebugPreferencesAddon_debugTransferEncoding + " (" +
		// fDebugEncodingSettings.getIANATag() + ")");
		// expandbleDebugEncoding.setExpanded(false);
		// }
		// } else { // folded
		// if (expandable == expandbleDebugEncoding) {
		// expandbleDebugEncoding.setText(PHPDebugUIMessages.PHPDebugPreferencesAddon_debugTransferEncoding + " (" +
		// fDebugEncodingSettings.getIANATag() + ")");
		// expandbleOutputEncoding.setText(PHPDebugUIMessages.PHPDebugPreferencesAddon_debugOutputEncoding);
		// expandbleOutputEncoding.setExpanded(true);
		// } else {
		// expandbleOutputEncoding.setText(PHPDebugUIMessages.PHPDebugPreferencesAddon_debugOutputEncoding + " (" +
		// fOutputEncodingSettings.getIANATag() + ")");
		// expandbleDebugEncoding.setText(PHPDebugUIMessages.PHPDebugPreferencesAddon_debugTransferEncoding);
		// expandbleDebugEncoding.setExpanded(true);
		// }
		// }

		ScrolledPageContent parentScrolledComposite = this.getParentScrolledComposite(expandable);
		if (parentScrolledComposite != null)
		{
			parentScrolledComposite.reflow(true);
		}
	}

	private ScrolledPageContent getParentScrolledComposite(Control control)
	{
		Control parent = control.getParent();
		while (!(parent instanceof ScrolledPageContent) && (parent != null))
		{
			parent = parent.getParent();
		}
		if (parent instanceof ScrolledPageContent)
		{
			return (ScrolledPageContent) parent;
		}
		return null;
	}

	private void addLink(Composite parent, String label, final String propertyPageID)
	{
		Link link = new Link(parent, SWT.NONE);
		link.setFont(parent.getFont());
		link.setLayoutData(new GridData(SWT.END, SWT.BEGINNING, true, false));
		link.setText(label);
		link.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(
						PHPDebugPreferencesAddon.this.propertyPage.getShell(), propertyPageID, null, null);
				dialog.setBlockOnOpen(true);
				dialog.addPageChangedListener(new IPageChangedListener()
				{
					public void pageChanged(PageChangedEvent event)
					{
						Display.getDefault().asyncExec(new Runnable()
						{
							public void run()
							{
								String selectedDebugger = PHPDebugPreferencesAddon.this.fDefaultDebugger.getText();
								String selectedServer = PHPDebugPreferencesAddon.this.fDefaultServer.getText();
								String selectedPHP = PHPDebugPreferencesAddon.this.fDefaultPHPExe.getText();
								PHPDebugPreferencesAddon.this
										.loadDebuggers(PHPDebugPreferencesAddon.this.fDefaultDebugger);
								PHPDebugPreferencesAddon.this.loadServers(PHPDebugPreferencesAddon.this.fDefaultServer);
								loadPHPExes(PHPDebugPreferencesAddon.this.fDefaultPHPExe, PHPexes.getInstance()
										.getItems(PHPDebugPreferencesAddon.this.getSelectedDebuggerId()));
								PHPDebugPreferencesAddon.this.selectComboItem(
										PHPDebugPreferencesAddon.this.fDefaultDebugger,
										PHPDebugPreferencesAddon.this.fDefaultDebugger.indexOf(selectedDebugger));
								PHPDebugPreferencesAddon.this.selectComboItem(
										PHPDebugPreferencesAddon.this.fDefaultServer,
										PHPDebugPreferencesAddon.this.fDefaultServer.indexOf(selectedServer));
								PHPDebugPreferencesAddon.this.selectComboItem(
										PHPDebugPreferencesAddon.this.fDefaultPHPExe,
										PHPDebugPreferencesAddon.this.fDefaultPHPExe.indexOf(selectedPHP));
							}
						});
					}
				});
				dialog.open();
			}
		});
	}

	private void selectComboItem(Combo combo, int itemIndex)
	{
		if (itemIndex < 0)
		{
			if (combo.getItemCount() > 0)
			{
				combo.select(0);
			}
		}
		else
		{
			combo.select(itemIndex);
		}
	}

	private Combo addCombo(Composite parent, int horizontalIndent)
	{
		Combo combo = new Combo(parent, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = horizontalIndent;
		combo.setLayoutData(gd);
		return combo;
	}

	private void savePreferences(boolean isProjectSpecific)
	{
		String phpExe = this.fDefaultPHPExe.getText();
		// Check if it's still valid, since this method can be called after removing a PHP Interpreter from another
		// preferences page (PHP Interpreters)
		PHPexes exes = PHPexes.getInstance();
		if (exes.getItem(this.getSelectedDebuggerId(), phpExe) == null)
		{
			PHPexeItem item = exes.getDefaultItem(this.getSelectedDebuggerId());
			if (item != null)
			{
				phpExe = item.getName();
			}
			else
			{
				phpExe = ""; //$NON-NLS-1$
			}
		}
		// TODO - Might do the same for the default server
		IEclipsePreferences prefs = new InstanceScope().getNode(PHPDebugEPLPlugin.PLUGIN_ID);
		IScopeContext[] preferenceScopes = this.createPreferenceScopes(this.propertyPage);
		IEclipsePreferences debugUINode = preferenceScopes[0].getNode(this.getPreferenceNodeQualifier());
		IProject project = this.getProject(this.propertyPage);
		if (isProjectSpecific && (debugUINode != null) && (preferenceScopes[0] instanceof ProjectScope)
				&& (project != null))
		{
			debugUINode
					.putBoolean(PHPDebugCorePreferenceNames.STOP_AT_FIRST_LINE, this.fStopAtFirstLine.getSelection());
			debugUINode.put(PHPDebugCorePreferenceNames.DEFAULT_PHP, phpExe);
			// debugUINode.put(PHPDebugCorePreferenceNames.TRANSFER_ENCODING, fDebugEncodingSettings.getIANATag());
			// debugUINode.put(PHPDebugCorePreferenceNames.OUTPUT_ENCODING, fOutputEncodingSettings.getIANATag());
			debugUINode.put(IPHPDebugCorePreferenceKeys.PHP_DEBUGGER_ID, this.getSelectedDebuggerId());
			// ServersManager.setDefaultServer(project, fDefaultServer.getText());
		}
		else
		{
			if (project == null)
			{
				// Workspace settings
				prefs.putBoolean(PHPDebugCorePreferenceNames.STOP_AT_FIRST_LINE, this.fStopAtFirstLine.getSelection());
				prefs.put(PHPDebugCorePreferenceNames.CLIENT_IP, this.fClientIP.getText());
				// prefs.setValue(PHPDebugCorePreferenceNames.TRANSFER_ENCODING, fDebugEncodingSettings.getIANATag());
				// prefs.setValue(PHPDebugCorePreferenceNames.OUTPUT_ENCODING, fOutputEncodingSettings.getIANATag());
				prefs.put(IPHPDebugCorePreferenceKeys.PHP_DEBUGGER_ID, this.getSelectedDebuggerId());
				exes.setDefaultItem(this.getSelectedDebuggerId(), phpExe);
				// ServersManager.setDefaultServer(null, fDefaultServer.getText());
			}
			else
			{
				if (debugUINode != null)
				{
					// Removed a project specific
					debugUINode.remove(PHPDebugCorePreferenceNames.STOP_AT_FIRST_LINE);
					// debugUINode.remove(PHPDebugCorePreferenceNames.ZEND_DEBUG_PORT); // No need
					debugUINode.remove(PHPDebugCorePreferenceNames.DEFAULT_PHP);
					// PHPServersManager.setDefaultServer(project, (IServer) null);
					debugUINode.remove(PHPDebugCorePreferenceNames.TRANSFER_ENCODING);
					debugUINode.remove(PHPDebugCorePreferenceNames.OUTPUT_ENCODING);
					debugUINode.remove(IPHPDebugCorePreferenceKeys.PHP_DEBUGGER_ID);
				}
			}
		}
		try
		{
			debugUINode.flush();
			exes.save();
			prefs.flush();
		}
		catch (BackingStoreException e)
		{
			PHPDebugEPLPlugin.logError(e);
		}
	}

	private String getSelectedDebuggerId()
	{
		int selectedIndex = this.fDefaultDebugger.getSelectionIndex();
		String debuggerId = XDebugCommunicationDaemon.XDEBUG_DEBUGGER_ID; // default
		if ((selectedIndex > -1) && (this.debuggersIds.size() > selectedIndex))
		{
			debuggerId = this.debuggersIds.toArray()[selectedIndex].toString();
		}
		return debuggerId;
	}
}
