package com.aptana.editor.php;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class PHPEditorPlugin extends AbstractUIPlugin
{

	// The plug-in ID
	public static final String PLUGIN_ID = "com.aptana.editor.php"; //$NON-NLS-1$

	public static final String BUILDER_ID = PLUGIN_ID + ".aptanaPhpBuilder"; //$NON-NLS-1$
	public static final boolean DEBUG = Boolean.valueOf(Platform.getDebugOption(PLUGIN_ID + "/debug")).booleanValue(); //$NON-NLS-1$
	public static final boolean INDEXER_DEBUG = Boolean
			.valueOf(Platform.getDebugOption(PLUGIN_ID + "/indexer_debug")).booleanValue(); //$NON-NLS-1$

	// The shared instance
	private static PHPEditorPlugin plugin;

	/**
	 * The constructor
	 */
	public PHPEditorPlugin()
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
	public static PHPEditorPlugin getDefault()
	{
		return plugin;
	}

	/**
	 * getImage
	 * 
	 * @param path
	 * @return
	 */
	public static Image getImage(String path)
	{
		ImageRegistry registry = plugin.getImageRegistry();
		Image image = registry.get(path);

		if (image == null)
		{
			ImageDescriptor id = getImageDescriptor(path);

			if (id != null)
			{
				registry.put(path, id);
				image = registry.get(path);
			}
		}

		return image;
	}

	/**
	 * getImageDescriptor
	 * 
	 * @param path
	 * @return
	 */
	public static ImageDescriptor getImageDescriptor(String path)
	{
		return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
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

	public static void log(IStatus status)
	{
		getDefault().getLog().log(status);
	}
}
