package com.aptana.editor.php.epl;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class PHPEplPlugin extends AbstractUIPlugin
{

	// The plug-in ID
	public static final String PLUGIN_ID = "com.aptana.editor.php.epl"; //$NON-NLS-1$

	// The shared instance
	private static PHPEplPlugin plugin;

	/**
	 * The constructor
	 */
	public PHPEplPlugin()
	{
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception
	{
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception
	{
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static PHPEplPlugin getDefault()
	{
		return plugin;
	}

	public static void logInfo(String string, Throwable e)
	{
		getDefault().getLog().log(new Status(IStatus.INFO, PLUGIN_ID, string, e));
	}

	public static void logError(Throwable e)
	{
		logError(e.getLocalizedMessage(), e);
	}

	public static void logError(String string, Throwable e)
	{
		getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, string, e));
	}

	public static void logWarning(String message)
	{
		getDefault().getLog().log(new Status(IStatus.WARNING, PLUGIN_ID, message));
	}
}