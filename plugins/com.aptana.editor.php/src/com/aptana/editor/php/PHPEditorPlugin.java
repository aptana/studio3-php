/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.projects.templates.ProjectTemplate;
import com.aptana.core.projects.templates.TemplateType;
import com.aptana.core.util.CollectionsUtil;
import com.aptana.core.util.EclipseUtil;
import com.aptana.editor.php.indexer.PHPGlobalIndexer;
import com.aptana.editor.php.internal.indexer.language.PHPBuiltins;
import com.aptana.editor.php.internal.model.ModelManager;
import com.aptana.editor.php.internal.ui.editor.PHPDocumentProvider;
import com.aptana.editor.php.util.EditorUtils;
import com.aptana.projects.ProjectsPlugin;
import com.aptana.projects.templates.IDefaultProjectTemplate;
import com.aptana.theme.IThemeManager;
import com.aptana.theme.ThemePlugin;

/**
 * The activator class controls the plug-in life cycle
 */
public class PHPEditorPlugin extends AbstractUIPlugin
{

	// The plug-in ID
	public static final String PLUGIN_ID = "com.aptana.editor.php"; //$NON-NLS-1$
	public static final String DEBUG_SCOPE = PLUGIN_ID + "/debug"; //$NON-NLS-1$
	public static final String INDEXER_SCOPE = PLUGIN_ID + "/debug/indexer"; //$NON-NLS-1$
	public static final String BUILDER_ID = PLUGIN_ID + ".aptanaPhpBuilder"; //$NON-NLS-1$
	public static final boolean DEBUG = Boolean.valueOf(Platform.getDebugOption(DEBUG_SCOPE)).booleanValue();
	public static final boolean INDEXER_DEBUG = Boolean.valueOf(Platform.getDebugOption(INDEXER_SCOPE)).booleanValue();

	// The shared instance
	private static PHPEditorPlugin plugin;

	private IPreferenceChangeListener fThemeChangeListener;

	private PHPDocumentProvider phpDocumentProvider;

	private static class DefaultPHPProjectTemplate extends ProjectTemplate implements IDefaultProjectTemplate
	{

		private static final String ID = "com.aptana.php.default"; //$NON-NLS-1$

		public DefaultPHPProjectTemplate()
		{
			super("default.zip", TemplateType.PHP, Messages.PHPEditorPlugin_DefaultPHPProjectTemplate_Name, //$NON-NLS-1$
					false, Messages.PHPEditorPlugin_DefaultPHPProjectTemplate_Description, null, ID, 1, CollectionsUtil
							.newList("PHP")); //$NON-NLS-1$
		}

		@Override
		public IStatus apply(IProject project, boolean promptForOverwrite)
		{
			// just returns success
			return Status.OK_STATUS;
		}
	}

	/**
	 * The constructor
	 */
	public PHPEditorPlugin() // $codepro.audit.disable
	{
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception // $codepro.audit.disable declaredExceptions
	{
		long start = System.currentTimeMillis();
		super.start(context);
		plugin = this;
		ProjectsPlugin.getDefault().getTemplatesManager().addTemplate(new DefaultPHPProjectTemplate());

		if (DEBUG)
		{
			System.out.println("PHP Plugin - Super start: " + (System.currentTimeMillis() - start)); //$NON-NLS-1$
			start = System.currentTimeMillis();
		}
		addThemeListener();
		if (DEBUG)
		{
			System.out.println("PHP Plugin - Add theme listener: " + (System.currentTimeMillis() - start)); //$NON-NLS-1$
			start = System.currentTimeMillis();
		}
		index();
		if (DEBUG)
		{
			System.out.println("PHP Plugin - index(): " + (System.currentTimeMillis() - start)); //$NON-NLS-1$
			start = System.currentTimeMillis();
		}
		Job loadBuiltins = new Job("Index PHP API...") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor)
			{
				PHPBuiltins.getInstance().getBuiltins();
				return Status.OK_STATUS;
			}
		};
		EclipseUtil.setSystemForJob(loadBuiltins);
		loadBuiltins.setPriority(Job.BUILD);
		loadBuiltins.schedule(2000L);
		if (DEBUG)
		{
			System.out.println("PHP Plugin - Load Built-ins: " + (System.currentTimeMillis() - start)); //$NON-NLS-1$
		}
	}

	/**
	 * Hook up a listener for theme changes, and change the PHP occurrence colors!
	 */
	private void addThemeListener()
	{
		fThemeChangeListener = new IPreferenceChangeListener()
		{

			public void preferenceChange(PreferenceChangeEvent event)
			{
				if (event.getKey().equals(IThemeManager.THEME_CHANGED))
				{
					EditorUtils.setOccurrenceColors();
				}
			}
		};
		EclipseUtil.instanceScope().getNode(ThemePlugin.PLUGIN_ID).addPreferenceChangeListener(fThemeChangeListener);
	}

	private void removeThemeListener()
	{
		if (fThemeChangeListener != null)
		{
			EclipseUtil.instanceScope().getNode(ThemePlugin.PLUGIN_ID)
					.removePreferenceChangeListener(fThemeChangeListener);
			fThemeChangeListener = null;
		}
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
					IdeLog.logInfo(PHPEditorPlugin.getDefault(), "PHPPlugin call to PHPGlobalIndexer starts", null, //$NON-NLS-1$
							PHPEditorPlugin.DEBUG_SCOPE);
					mark = System.currentTimeMillis();
				}
				PHPGlobalIndexer.getInstance();
				if (DEBUG)
				{
					IdeLog.logInfo(PHPEditorPlugin.getDefault(),
							"PHPPlugin call to PHPGlobalIndexer ended (done after " //$NON-NLS-1$
									+ (System.currentTimeMillis() - mark) + "ms)", null, PHPEditorPlugin.DEBUG_SCOPE); //$NON-NLS-1$
					IdeLog.logInfo(PHPEditorPlugin.getDefault(), "PHPPlugin call to ModelManager starts", null, //$NON-NLS-1$
							PHPEditorPlugin.DEBUG_SCOPE);
					mark = System.currentTimeMillis();
				}
				// initializing model
				ModelManager.getInstance(); // FIXME - SG - This is where the hang starts
				if (DEBUG)
				{
					IdeLog.logInfo(PHPEditorPlugin.getDefault(), "PHPPlugin call to ModelManager ended (done after " //$NON-NLS-1$
							+ (System.currentTimeMillis() - mark) + "ms)", null, PHPEditorPlugin.DEBUG_SCOPE); //$NON-NLS-1$
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
	public void stop(BundleContext context) throws Exception // $codepro.audit.disable declaredExceptions
	{
		try
		{
			PHPGlobalIndexer.getInstance().save();
			removeThemeListener();
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
	 * Returns PHP document provider
	 * 
	 * @return
	 */
	public synchronized PHPDocumentProvider getPHPDocumentProvider()
	{
		if (phpDocumentProvider == null)
		{
			phpDocumentProvider = new PHPDocumentProvider();
		}
		return phpDocumentProvider;
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

	/**
	 * Log a particular status
	 * 
	 * @deprecated Use IdeLog instead
	 */
	public static void log(IStatus status)
	{
		IdeLog.log(getDefault(), status);
	}

	/**
	 * logError
	 * 
	 * @param e
	 * @deprecated Use IdeLog instead
	 */
	public static void log(Throwable e)
	{
		IdeLog.logError(getDefault(), e.getLocalizedMessage(), e);
	}

	/**
	 * logError
	 * 
	 * @deprecated Use IdeLog instead
	 * @param message
	 * @param e
	 */
	public static void logError(Throwable e)
	{
		IdeLog.logError(getDefault(), e.getLocalizedMessage(), e);
	}

	/**
	 * logError
	 * 
	 * @deprecated Use IdeLog instead
	 * @param message
	 * @param e
	 */
	public static void logError(String message, Throwable e)
	{
		IdeLog.logError(getDefault(), message, e);
	}

	/**
	 * logWarning
	 * 
	 * @deprecated Use IdeLog instead
	 * @param message
	 * @param e
	 */
	public static void logWarning(String message)
	{
		IdeLog.logWarning(getDefault(), message, null, null);
	}

	/**
	 * logWarning
	 * 
	 * @deprecated Use IdeLog instead
	 * @param message
	 * @param e
	 */
	public static void logWarning(String message, Throwable e)
	{
		IdeLog.logWarning(getDefault(), message, e, null);
	}

	/**
	 * logInfo
	 * 
	 * @deprecated Use IdeLog instead
	 * @param message
	 */
	public static void logInfo(String message)
	{
		IdeLog.logInfo(getDefault(), message, null);
	}
}
