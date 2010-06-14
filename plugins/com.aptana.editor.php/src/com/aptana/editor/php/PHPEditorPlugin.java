package com.aptana.editor.php;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.aptana.editor.php.indexer.PHPGlobalIndexer;
import com.aptana.editor.php.internal.indexer.language.PHPBuiltins;
import com.aptana.editor.php.internal.model.ModelManager;

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
		index();
		Job loadBuiltins = new Job("Index PHP API...") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				PHPBuiltins.getInstance().getBuiltins();
				return Status.OK_STATUS;
			}
		};
		loadBuiltins.setSystem(true);
		loadBuiltins.setPriority(Job.BUILD);
		loadBuiltins.schedule(2000L);
	}

	private void index()
	{
		// initializing code indexing
		Job indexerJob = new Job(Messages.PHPEditorPlugin_indexingJobMessage) {
			protected IStatus run(IProgressMonitor monitor)
			{
				// TODO - Use that monitor to provide some progress on that indexing job
				long mark = 0L;
				if (DEBUG)
				{
					System.out.println("PHPPlugin call to PHPGlobalIndexer starts"); //$NON-NLS-1$
					mark = System.currentTimeMillis();
				}
				PHPGlobalIndexer.getInstance();
				if (DEBUG)
				{
					System.out
							.println("PHPPlugin call to PHPGlobalIndexer ended (done after " + (System.currentTimeMillis() - mark) + "ms)"); //$NON-NLS-1$ //$NON-NLS-2$
					System.out.println("PHPPlugin call to ModelManager starts"); //$NON-NLS-1$
					mark = System.currentTimeMillis();
				}
				// initializing model
				ModelManager.getInstance(); // FIXME - SG - This is where the hang starts
				if (DEBUG)
				{
					System.out
							.println("PHPPlugin call to ModelManager ended (done after " + (System.currentTimeMillis() - mark) + "ms)"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				return Status.OK_STATUS;
			}
		};
		indexerJob.setPriority(Job.BUILD);
		indexerJob.schedule(1000L);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception
	{
		PHPGlobalIndexer.getInstance().save();
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
	 * Returns the active workbench window shell.
	 * 
	 * @return the active workbench window shell; Null if none exists.
	 */
	public static Shell getShell()
	{
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null)
		{
			IWorkbenchWindow[] windows = PlatformUI.getWorkbench().getWorkbenchWindows();
			if (windows.length > 0)
			{
				return windows[0].getShell();
			}
		}
		else
		{
			Shell shell = window.getShell();
			if (shell == null)
			{
				// Try to get it straight from the Display
				Display disp = PlatformUI.getWorkbench().getDisplay();
				if (disp != null)
				{
					return disp.getActiveShell();
				}
			}
		}
		return null;
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
