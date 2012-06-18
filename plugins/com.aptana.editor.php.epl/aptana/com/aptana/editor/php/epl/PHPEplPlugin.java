/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.epl;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org2.eclipse.php.internal.ui.editor.ASTProvider;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.php.internal.ui.viewsupport.ProblemMarkerManager;
import com.aptana.ui.util.UIUtils;

public class PHPEplPlugin extends AbstractUIPlugin
{

	// The plug-in ID
	public static final String PLUGIN_ID = "com.aptana.editor.php.epl"; //$NON-NLS-1$
	public static final String DEBUG_SCOPE = PLUGIN_ID + "/debug"; //$NON-NLS-1$
	public static final boolean DEBUG = Boolean.valueOf(Platform.getDebugOption(DEBUG_SCOPE)).booleanValue();

	// The shared instance
	private static PHPEplPlugin plugin;

	private ASTProvider fASTProvider;

	private ProblemMarkerManager fProblemMarkerManager;
	private Map<String, String> _pool;

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
		this._pool = new Hashtable<String, String>();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception
	{
		this._pool = null;
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

	/**
	 * Returns the AST provider.
	 * 
	 * @return the AST provider
	 * @since 3.0
	 */
	public synchronized ASTProvider getASTProvider()
	{
		if (fASTProvider == null)
		{
			fASTProvider = new ASTProvider();
		}

		return fASTProvider;
	}

	/**
	 * Returns a {@link ProblemMarkerManager}
	 * 
	 * @return {@link ProblemMarkerManager}
	 */
	public synchronized ProblemMarkerManager getProblemMarkerManager()
	{
		if (fProblemMarkerManager == null)
		{
			fProblemMarkerManager = new ProblemMarkerManager();
		}
		return fProblemMarkerManager;
	}

	/**
	 * Returns the standard display to be used. The method first checks, if the thread calling this method has an
	 * associated display. If so, this display is returned. Otherwise the method returns the default display.
	 * 
	 * @return returns the standard display to be used
	 */
	public static Display getStandardDisplay()
	{
		Display display;
		display = Display.getCurrent();
		if (display == null)
		{
			display = Display.getDefault();
		}
		return display;
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
	 * @deprecated Use {@link UIUtils#getActivePage()}
	 * @return
	 */
	public static IWorkbenchPage getActivePage()
	{
		return getDefault().internalGetActivePage();
	}

	/**
	 * @deprecated Use {@link UIUtils#getActiveEditor()}
	 * @return
	 */
	public static IEditorPart getActiveEditor()
	{
		IWorkbenchPage activePage = getActivePage();
		if (activePage != null)
		{
			return activePage.getActiveEditor();
		}
		return null;
	}

	private IWorkbenchPage internalGetActivePage()
	{
		IWorkbenchWindow window = getWorkbench().getActiveWorkbenchWindow();
		if (window == null)
		{
			return null;
		}
		return getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}

	/**
	 * @deprecated Use {@link IdeLog}
	 */
	public static void logInfo(String string, Throwable e)
	{
		IdeLog.logInfo(getDefault(), string, e, DEBUG_SCOPE);
	}

	/**
	 * @deprecated Use {@link IdeLog}
	 */
	public static void logError(Throwable e)
	{
		IdeLog.logError(getDefault(), e);
	}

	/**
	 * @deprecated Use {@link IdeLog}
	 */
	public static void logError(String string, Throwable e)
	{
		IdeLog.logError(getDefault(), string, e);
	}

	/**
	 * @deprecated Use {@link IdeLog}
	 */
	public static void logWarning(String message)
	{
		IdeLog.logWarning(getDefault(), message);
	}

	/**
	 * @deprecated Use {@link IdeLog}
	 */
	public static void log(IStatus status)
	{
		IdeLog.log(getDefault(), status);
	}

	public static IWorkspace getWorkspace()
	{
		return ResourcesPlugin.getWorkspace();
	}

	public String sharedString(String value)
	{
		if (value == null)
		{
			return value;
		}

		String result = _pool.get(value);
		if (result != null)
		{
			return result;
		}
		_pool.put(value, value);
		return value;
	}
}