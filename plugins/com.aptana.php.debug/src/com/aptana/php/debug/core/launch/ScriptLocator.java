package com.aptana.php.debug.core.launch;

import java.net.MalformedURLException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;

import com.aptana.debug.core.IActiveResourcePathGetterAdapter;
import com.aptana.debug.ui.internal.ActiveResourcePathGetterAdapter;
import com.aptana.php.debug.core.IPHPDebugCorePreferenceKeys;

/**
 * @author Pavel Petrochenko
 */
@SuppressWarnings("restriction")
public final class ScriptLocator
{

	private ScriptLocator()
	{

	}

	/**
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	public static String getScriptFile(ILaunchConfiguration configuration) throws CoreException
	{
		boolean attribute = configuration.getAttribute(IPHPDebugCorePreferenceKeys.ATTR_USE_SPECIFIC_FILE, false);
		if (attribute)
		{
			return configuration.getAttribute(IPHPDebugCorePreferenceKeys.ATTR_FILE, (String) null);
		}
		try
		{
			IResource currentEditorResource = new ScriptLocator().getCurrentEditorResource();
			if (currentEditorResource == null)
			{
				return null;
			}
			String string = currentEditorResource.getFullPath().toOSString();
			return string;
		}
		catch (MalformedURLException e)
		{
			throw new RuntimeException();
		}
	}

	/**
	 * Gets script file name.
	 * 
	 * @param configuration
	 *            - configuration.
	 * @param fileConfKey
	 *            - file attribute configuration key.
	 * @return script file name.
	 * @throws CoreException
	 *             IF core exception occurs
	 */
	public static String getScriptFile(ILaunchConfiguration configuration, String fileConfKey) throws CoreException
	{
		boolean attribute = configuration.getAttribute(IPHPDebugCorePreferenceKeys.ATTR_USE_SPECIFIC_FILE, false);
		if (attribute)
		{
			return configuration.getAttribute(fileConfKey, (String) null);
		}
		try
		{
			IResource currentEditorResource = new ScriptLocator().getCurrentEditorResource();
			if (currentEditorResource == null)
			{
				return null;
			}
			String string = currentEditorResource.getFullPath().toOSString();
			return string;
		}
		catch (MalformedURLException e)
		{
			throw new RuntimeException();
		}
	}

	/**
	 * @return
	 * @throws MalformedURLException
	 */
	protected IResource getCurrentEditorResource() throws MalformedURLException
	{
		IActiveResourcePathGetterAdapter adapter = (IActiveResourcePathGetterAdapter) getContributedAdapter(IActiveResourcePathGetterAdapter.class);
		if (adapter != null)
		{
			return adapter.getActiveResource();
		}
		return null;
	}

	/**
	 * @return
	 * @throws MalformedURLException
	 */
	protected IPath getCurrentEditorPath() throws MalformedURLException
	{
		IActiveResourcePathGetterAdapter adapter = (IActiveResourcePathGetterAdapter) getContributedAdapter(IActiveResourcePathGetterAdapter.class);
		if (adapter != null)
		{
			return adapter.getActiveResourcePath();
		}
		return null;
	}

	/**
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	protected Object getContributedAdapter(Class clazz)
	{
		return new ActiveResourcePathGetterAdapter();
	}
}
