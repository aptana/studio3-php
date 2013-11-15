package com.aptana.editor.php.core;

import org.eclipse.core.resources.IProject;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org2.eclipse.php.internal.core.PHPVersion;

import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.tests.ProjectBasedTestCase;

public class PHPVersionProviderTest extends ProjectBasedTestCase
{

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.tests.ProjectBasedTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		PHPVersionProvider.getInstance().setPreferencesQualifier(PHPEditorPlugin.PLUGIN_ID);
	}

	@Override
	protected String getProjectName()
	{
		return "php-1"; //$NON-NLS-1$
	}

	@Override
	protected PHPVersion getInitialPHPVersion()
	{
		return PHPVersion.PHP5_3;
	}

	@Override
	protected String getPluginPreferenceQualifier()
	{
		return PHPEditorPlugin.PLUGIN_ID;
	}

	@Override
	protected void setProjectOptions(IProject project) throws BackingStoreException
	{
		Preferences preferences = getPreferences(project);
		preferences.put(CorePreferenceConstants.Keys.PHP_VERSION, PHPVersion.PHP5_3.getAlias());
		preferences.flush();
	}

	public void testGetDefaultPHPVersion()
	{
		assertTrue(PHPVersion.getLatest().equals(PHPVersionProvider.getDefaultPHPVersion()));
	}

	public void testInitialProjectVersion()
	{
		assertTrue(PHPVersionProvider.isPHP53(project));
		assertFalse(PHPVersionProvider.isPHP5(project));
		assertFalse(PHPVersionProvider.isPHP4(project));
		assertFalse(PHPVersionProvider.isPHP54(project));
	}

	public void testProjectVersionChange() throws BackingStoreException
	{
		Preferences preferences = getPreferences(project);
		preferences.put(CorePreferenceConstants.Keys.PHP_VERSION, PHPVersion.PHP5.getAlias());
		preferences.flush();

		assertTrue(PHPVersionProvider.isPHP5(project));
		assertFalse(PHPVersionProvider.isPHP53(project));
		assertFalse(PHPVersionProvider.isPHP4(project));
		assertFalse(PHPVersionProvider.isPHP54(project));
	}
}
