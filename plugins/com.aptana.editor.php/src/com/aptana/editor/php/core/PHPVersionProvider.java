package com.aptana.editor.php.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org2.eclipse.php.internal.core.PHPVersion;

import com.aptana.core.util.EclipseUtil;
import com.aptana.editor.php.PHPEditorPlugin;

/**
 * Provides the workspace/project-specific PHP version setting.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPVersionProvider
{
	public static final String DEFAULT_PREFERENCES_QUALIFIER = PHPEditorPlugin.PLUGIN_ID;
	private static PHPVersionProvider instance;
	private String preferencesQualifier;
	private Map<IProject, ListenerList> listeners;

	/**
	 * Returns an instance of the PHP version provider.
	 * 
	 * @return An instance of this class.
	 */
	public static PHPVersionProvider getInstance()
	{
		if (instance == null)
		{
			instance = new PHPVersionProvider();
		}
		return instance;
	}

	// Constructs a new PHPVersionProvider and register to listen on any PHP version changes.
	private PHPVersionProvider()
	{
		listeners = new HashMap<IProject, ListenerList>();
		preferencesQualifier = DEFAULT_PREFERENCES_QUALIFIER;
	}

	/**
	 * An option to set the preferences qualifier from which the PHP project settings will be read.
	 * @param qualifier
	 */
	public void setPreferencesQualifier(String qualifier)
	{
		if (qualifier == null)
		{
			throw new IllegalArgumentException("Qualifier cannot be null"); //$NON-NLS-1$
		}
		this.preferencesQualifier = qualifier;
	}

	/**
	 * Register a listener to be notified when a PHP version is modified on a specific project.
	 * 
	 * @param project
	 * @param listener
	 */
	public void addPHPVersionListener(IProject project, IPHPVersionListener listener)
	{
		ListenerList listenerList = listeners.get(project);
		if (listenerList == null)
		{
			listenerList = new ListenerList();
			listeners.put(project, listenerList);
		}
		listenerList.add(listener);
	}

	/**
	 * Unregister a modification listener from all the projects that it was registered on.
	 * 
	 * @param listener
	 */
	public void removePHPVersionListener(IPHPVersionListener listener)
	{
		Collection<ListenerList> lists = listeners.values();
		for (ListenerList listenerList : lists)
		{
			listenerList.remove(listener);
		}
	}

	/**
	 * Trigger a notification when a project's PHP version is modified.
	 * 
	 * @param project
	 * @param newVersion
	 */
	public void notifyChange(IProject project, PHPVersion newVersion)
	{
		ListenerList listenersList = listeners.get(project);
		if (listenersList != null)
		{
			Object[] allListeners = listenersList.getListeners();
			for (Object listener : allListeners)
			{
				((IPHPVersionListener) listener).phpVersionChanged(newVersion);
			}
		}
	}

	/**
	 * Returns the PHP version that is set in the preferences.
	 * 
	 * @param project
	 *            (optional) Pass a project reference to get a possible project specific PHP version value.
	 * @return The {@link PHPVersion}
	 */
	public static PHPVersion getPHPVersion(IProject project)
	{
		return getInstance().getVersion(project, CorePreferenceConstants.Keys.PHP_VERSION);
	}

	/**
	 * A continent method to check if the current version is 5.4
	 * 
	 * @param project
	 *            Optional project that might contain specific version which is different from the workspace default.
	 * @return True, if and only if the version is PHP 5.4
	 */
	public static boolean isPHP54(IProject project)
	{
		PHPVersion version = getPHPVersion(project);
		return PHPVersion.PHP5_4.equals(version);
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

	private PHPVersion getVersion(IProject project, String prefKey)
	{
		IPreferencesService service = Platform.getPreferencesService();
		IScopeContext[] contexts;
		if (project != null)
		{
			contexts = new IScopeContext[] { new ProjectScope(project), EclipseUtil.instanceScope(), EclipseUtil.defaultScope() };
		}
		else
		{
			contexts = new IScopeContext[] { EclipseUtil.instanceScope(), EclipseUtil.defaultScope() };
		}
		String versionAlias = service.getString(preferencesQualifier, prefKey, PHPVersion.getLatest().getAlias(), contexts);
		return PHPVersion.byAlias(versionAlias);
	}

	public static PHPVersion getDefaultPHPVersion()
	{
		return PHPVersion.getLatest();
	}
}