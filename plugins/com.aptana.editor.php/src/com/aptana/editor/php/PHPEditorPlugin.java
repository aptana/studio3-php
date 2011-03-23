package com.aptana.editor.php;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;

import com.aptana.core.util.EclipseUtil;
import com.aptana.editor.php.indexer.PHPGlobalIndexer;
import com.aptana.editor.php.internal.indexer.language.PHPBuiltins;
import com.aptana.editor.php.internal.model.ModelManager;
import com.aptana.theme.IThemeManager;
import com.aptana.theme.Theme;
import com.aptana.theme.ThemePlugin;

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

	private IPreferenceChangeListener fThemeChangeListener;

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

		listenForThemeChanges();
		index();
		Job loadBuiltins = new Job("Index PHP API...") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				PHPBuiltins.getInstance().getBuiltins();
				return Status.OK_STATUS;
			}
		};
		loadBuiltins.setSystem(!EclipseUtil.showSystemJobs());
		loadBuiltins.setPriority(Job.BUILD);
		loadBuiltins.schedule(2000L);
	}

	/**
	 * Hook up a listener for theme changes, and change the PHP occurrence colors!
	 */
	private void listenForThemeChanges()
	{
		Job job = new UIJob("Set occurrence colors to theme") //$NON-NLS-1$
		{
			private void setOccurrenceColors()
			{
				IEclipsePreferences prefs = new InstanceScope().getNode("org.eclipse.ui.editors"); //$NON-NLS-1$
				Theme theme = ThemePlugin.getDefault().getThemeManager().getCurrentTheme();
				prefs.put("PHPReadOccurrenceIndicationColor", StringConverter.asString(theme.getSearchResultColor())); //$NON-NLS-1$
				prefs.put("PHPWriteOccurrenceIndicationColor", StringConverter.asString(theme.getSearchResultColor())); //$NON-NLS-1$
				try
				{
					prefs.flush();
				}
				catch (BackingStoreException e)
				{
					// ignore
				}
			}

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				fThemeChangeListener = new IPreferenceChangeListener()
				{
					public void preferenceChange(PreferenceChangeEvent event)
					{
						if (event.getKey().equals(IThemeManager.THEME_CHANGED))
						{
							setOccurrenceColors();
						}
					}
				};
				setOccurrenceColors();
				new InstanceScope().getNode(ThemePlugin.PLUGIN_ID).addPreferenceChangeListener(fThemeChangeListener);
				return Status.OK_STATUS;
			}
		};

		job.setSystem(true);
		job.schedule();
	}

	private void index()
	{
		// initializing code indexing
		Job indexerJob = new Job(Messages.PHPEditorPlugin_indexingJobMessage)
		{
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
		try
		{
			PHPGlobalIndexer.getInstance().save();
			if (fThemeChangeListener != null)
			{
				new InstanceScope().getNode(ThemePlugin.PLUGIN_ID).removePreferenceChangeListener(fThemeChangeListener);
				fThemeChangeListener = null;
			}
		}
		finally
		{
			plugin = null;
			super.stop(context);
		}
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
