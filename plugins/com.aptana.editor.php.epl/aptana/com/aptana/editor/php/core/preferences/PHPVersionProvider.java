package com.aptana.editor.php.core.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.php.internal.core.PHPVersion;
import org.eclipse.php.internal.core.preferences.CorePreferenceConstants;

import com.aptana.editor.php.epl.Activator;

/**
 * Provides the workspace/project-specific PHP version setting.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPVersionProvider
{
	/**
	 * Returns the PHP version that is set in the preferences.
	 * 
	 * @param project
	 *            (optional) Pass a project reference to get a possible project specific PHP version value.
	 * @return The {@link PHPVersion}
	 */
	public static PHPVersion getPHPVersion(IProject project)
	{
		return getVersion(project, CorePreferenceConstants.Keys.PHP_VERSION);
	}

	/**
	 * A continent method to check if the current version is 5.3
	 * 
	 * @param project
	 *            Optional project that might contain specific version which is different from the workspace default.
	 * @return True, if and only if the version is PHP 5.3
	 */
	public static boolean isPHP53(IProject project)
	{
		PHPVersion version = getPHPVersion(project);
		return PHPVersion.PHP5_3.equals(version);
	}

	/**
	 * A continent method to check if the current version is 5
	 * 
	 * @param project
	 *            Optional project that might contain specific version which is different from the workspace default.
	 * @return True, if and only if the version is PHP 5
	 */
	public static boolean isPHP5(IProject project)
	{
		PHPVersion version = getPHPVersion(project);
		return PHPVersion.PHP5.equals(version);
	}

	/**
	 * A continent method to check if the current version is 4
	 * 
	 * @param project
	 *            Optional project that might contain specific version which is different from the workspace default.
	 * @return True, if and only if the version is PHP 4
	 */
	public static boolean isPHP4(IProject project)
	{
		PHPVersion version = getPHPVersion(project);
		return PHPVersion.PHP4.equals(version);
	}

	private static PHPVersion getVersion(IProject project, String prefKey)
	{
		IPreferencesService service = Platform.getPreferencesService();
		IScopeContext[] contexts;
		if (project != null)
		{
			contexts = new IScopeContext[] { new ProjectScope(project), new InstanceScope(), new DefaultScope() };
		}
		else
		{
			contexts = new IScopeContext[] { new InstanceScope(), new DefaultScope() };
		}
		String versionAlias = service.getString(Activator.PLUGIN_ID, prefKey, PHPVersion.PHP5.getAlias(), contexts);
		return PHPVersion.byAlias(versionAlias);
	}
}