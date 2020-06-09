package com.aptana.editor.php.internal.ui.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.ui.preferences.IStatusChangeListener;
import com.aptana.editor.php.ui.preferences.OptionsConfigurationBlock;
import com.aptana.editor.php.util.Key;

public abstract class PHPCoreOptionsConfigurationBlock extends OptionsConfigurationBlock
{

	protected PHPCoreOptionsConfigurationBlock(IStatusChangeListener context, IProject project, Key[] allKeys,
			IWorkbenchPreferenceContainer container)
	{
		super(context, project, allKeys, container);
	}

	protected abstract Control createContents(Composite parent);

	protected abstract void validateSettings(Key changedKey, String oldValue, String newValue);

	protected abstract String[] getFullBuildDialogStrings(boolean workspaceSettings);

	protected final static Key getPHPCoreKey(String key)
	{
		return getKey(PHPEditorPlugin.PLUGIN_ID, key);
	}
}
