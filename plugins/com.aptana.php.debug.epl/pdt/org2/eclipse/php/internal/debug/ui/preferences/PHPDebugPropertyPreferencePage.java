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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org2.eclipse.php.internal.core.util.ScrolledCompositeImpl;
import org2.eclipse.php.internal.debug.core.IPHPDebugConstants;
import org2.eclipse.php.internal.debug.core.preferences.PHPProjectPreferences;
import org2.eclipse.php.internal.debug.ui.PHPDebugUIMessages;

import com.aptana.php.debug.core.IPHPDebugCorePreferenceKeys;
import com.aptana.php.debug.epl.PHPDebugEPLPlugin;

/**
 * The main PHP | Debug preferences page.
 * 
 * @author Shalom Gibly
 */
public class PHPDebugPropertyPreferencePage extends AbstractPHPPropertyPreferencePage
{


	/**
	 * a list of the field editors
	 * 
	 * @since 3.2
	 */
	private List fFieldEditors=new ArrayList<Object>();
	
	protected Label fDefaultURLLabel;
	
	
	
	protected Text fDefaultURLTextBox;

	private PHPDebuggersTable table;

	public PHPDebugPropertyPreferencePage()
	{
		super();

	}

	protected String getPreferenceNodeQualifier()
	{
		return PHPProjectPreferences.getPreferenceNodeQualifier();
	}

	protected String getPreferencePageID()
	{
		return IPHPDebugConstants.PREFERENCE_PAGE_ID;
	}

	protected String getProjectSettingsKey()
	{
		return PHPProjectPreferences.getProjectSettingsKey();
	}

	protected String getPropertyPageID()
	{
		return IPHPDebugConstants.PROJECT_PAGE_ID;
	}

	public void init(IWorkbench workbench)
	{
	}

	public String getTitle()
	{
		return PHPDebugUIMessages.PhpDebugPreferencePage_8;
	}
	
	protected boolean hasLink()
	{
		return false;
	}

	/**
	 * Override the default creation on the workspace content to add a fixed debuggers table that will display all the
	 * supported debuggers and will allow their preferences modification.
	 * @param composite 
	 * @return control
	 */
	protected Control createWorkspaceContents(Composite composite)
	{
		ScrolledCompositeImpl scrolledCompositeImpl = new ScrolledCompositeImpl(composite, SWT.V_SCROLL | SWT.H_SCROLL);
		
		
		
		Composite group = new Composite(scrolledCompositeImpl, SWT.NONE);
		group.setLayout(new GridLayout());

		Composite cm=new Composite(group,SWT.NONE);
		
		// Save dirty editors...
		// Allow multiple debug sessions
		FieldEditor edit = new RadioGroupFieldEditor(IPHPDebugCorePreferenceKeys.ALLOW_MULTIPLE_LAUNCHES,
				PHPDebugUIMessages.PHPLaunchingPreferencePage_multipleMessage, 3, new String[][] {
						{ PHPDebugUIMessages.PHPLaunchingPreferencePage_Always, MessageDialogWithToggle.ALWAYS },
						{ PHPDebugUIMessages.PHPLaunchingPreferencePage_Never, MessageDialogWithToggle.NEVER },
						{ PHPDebugUIMessages.PHPLaunchingPreferencePage_Prompt, MessageDialogWithToggle.PROMPT } },
				cm, true);
		this.fFieldEditors.add(edit);

		// Switch back to the previously used perspective when the debug is terminated
		edit = new RadioGroupFieldEditor(IPHPDebugCorePreferenceKeys.SWITCH_BACK_TO_PREVIOUS_PERSPECTIVE,
				PHPDebugUIMessages.PHPLaunchingPreferencePage_switchToPHPMessage, 3, new String[][] {
						{ PHPDebugUIMessages.PHPLaunchingPreferencePage_Always, MessageDialogWithToggle.ALWAYS },
						{ PHPDebugUIMessages.PHPLaunchingPreferencePage_Never, MessageDialogWithToggle.NEVER },
						{ PHPDebugUIMessages.PHPLaunchingPreferencePage_Prompt, MessageDialogWithToggle.PROMPT } },
				cm, true);
		this.fFieldEditors.add(edit);
		
		// Break on first line when an unknown remote session (JIT) is requested
		edit = new RadioGroupFieldEditor(IPHPDebugCorePreferenceKeys.BREAK_ON_FIRST_LINE_FOR_UNKNOWN_JIT,
				PHPDebugUIMessages.PHPLaunchingPreferencePage_breakOnFirstLineForUnknownJIT, 3, new String[][] {
				{ PHPDebugUIMessages.PHPLaunchingPreferencePage_Always, MessageDialogWithToggle.ALWAYS },
				{ PHPDebugUIMessages.PHPLaunchingPreferencePage_Never, MessageDialogWithToggle.NEVER },
				{ PHPDebugUIMessages.PHPLaunchingPreferencePage_Prompt, MessageDialogWithToggle.PROMPT }},
				cm, true);
		this.fFieldEditors.add(edit);
		
		// Notify the user when a non-standard debug port is in use
		edit = new RadioGroupFieldEditor(IPHPDebugCorePreferenceKeys.NOTIFY_NON_STANDARD_PORT,
				PHPDebugUIMessages.PHPLaunchingPreferencePage_nofityNonStandardPortMessage, 2, new String[][] {
				{ PHPDebugUIMessages.PHPLaunchingPreferencePage_Always, MessageDialogWithToggle.ALWAYS },
				{ PHPDebugUIMessages.PHPLaunchingPreferencePage_Never, MessageDialogWithToggle.NEVER }},
				cm, true);
		this.fFieldEditors.add(edit);
		
		GridLayout gridLayout = new GridLayout(1,true);
		gridLayout.marginLeft=3;
		gridLayout.marginRight=3;
		cm.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		cm.setLayout(gridLayout);
		this.initFieldEditors();
		
		
		// Add the debuggers table
		this.createDebuggersTable(group);
		try
		{
			// workspaceAddons = PHPPreferencePageBlocksRegistry.getPHPPreferencePageBlock(getPreferencePageID());
			// for (int i = 0; i < workspaceAddons.length; i++) {
			// workspaceAddons[i].setCompositeAddon(group);
			// workspaceAddons[i].initializeValues(this);
			// }
		}
		catch (Exception e)
		{
			PHPDebugEPLPlugin.logError(e);
		}
		scrolledCompositeImpl.setContent(group);
		return scrolledCompositeImpl;
	}
	
	/**
     * Returns the preference store of this preference page.
     * <p>
     * This is a framework hook method for subclasses to return a
     * page-specific preference store. The default implementation
     * returns <code>null</code>.
     * </p>
     *
     * @return the preference store, or <code>null</code> if none
     */
    protected IPreferenceStore doGetPreferenceStore() {
        return PHPDebugEPLPlugin.getDefault().getPreferenceStore();
    }
	
	private void initFieldEditors()
	{
		FieldEditor editor;
		for (int i = 0; i < this.fFieldEditors.size(); i++)
		{
			editor = (FieldEditor) this.fFieldEditors.get(i);
			editor.setPreferenceStore(this.getPreferenceStore());
			editor.load();
		}
	}

	/**
	 * Overrides the super preformDefaults to make sure that the debuggers table also gets updated to its default
	 * values.
	 */
	public void performDefaults()
	{
		this.table.performDefaults();
		super.performDefaults();
		for (int i = 0; i < this.fFieldEditors.size(); i++)
		{
			((FieldEditor) this.fFieldEditors.get(i)).loadDefault();
		}
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	public boolean performOk()
	{
		for (int i = 0; i < this.fFieldEditors.size(); i++)
		{
			((FieldEditor) this.fFieldEditors.get(i)).store();
		}
		return super.performOk();
	}

	/**
	 * Creates the debuggers table. The created table allows only viewing and modifying any existing debugger that is
	 * registered thought the phpDebuggers extension point.
	 * 
	 * @param composite
	 */
	protected void createDebuggersTable(Composite composite)
	{
		this.table = new PHPDebuggersTable();
		this.table.createControl(composite);
	}
}
