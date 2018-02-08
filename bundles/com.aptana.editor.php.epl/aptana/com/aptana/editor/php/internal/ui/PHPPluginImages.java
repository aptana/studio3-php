/*******************************************************************************
 * Copyright (c) 2006 Zend Corporation and IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package com.aptana.editor.php.internal.ui;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.osgi.framework.Bundle;

import com.aptana.editor.php.epl.PHPEplPlugin;

@SuppressWarnings("unused")
public class PHPPluginImages
{
	public static final IPath ICONS_PATH = new Path("/icons/full"); //$NON-NLS-1$

	private static final String NAME_PREFIX = "com.aptana.editor.php.epl."; //$NON-NLS-1$
	private static final int NAME_PREFIX_LENGTH = NAME_PREFIX.length();

	// The plug-in registry
	private static ImageRegistry fgImageRegistry = null;

	private static HashMap<String, ImageDescriptor> fgAvoidSWTErrorMap = null;
	private static final String T_OBJ = "obj16"; //$NON-NLS-1$
	private static final String T_OVR = "ovr16"; //$NON-NLS-1$
	private static final String T_WIZBAN = "wizban"; //$NON-NLS-1$
	private static final String T_ELCL = "elcl16"; //$NON-NLS-1$
	private static final String T_DLCL = "dlcl16"; //$NON-NLS-1$
	private static final String T_ETOOL = "etool16"; //$NON-NLS-1$
	//	private static final String T_EVIEW = "eview16"; //$NON-NLS-1$

	// Images
	public static final String IMG_OBJS_EXCEPTION = NAME_PREFIX + "jexception_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_ERROR = NAME_PREFIX + "error_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_BREAKPOINT_INSTALLED = NAME_PREFIX + "brkpi_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_QUICK_ASSIST = NAME_PREFIX + "quickassist_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_FIXABLE_PROBLEM = NAME_PREFIX + "quickfix_warning_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_FIXABLE_ERROR = NAME_PREFIX + "quickfix_error_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_WARNING = NAME_PREFIX + "warning_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_ENV_VAR = NAME_PREFIX + "envvar_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_LIBRARY = NAME_PREFIX + "library_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_PHP_PROJECT = NAME_PREFIX + "php_project_obj.gif"; //$NON-NLS-1$
	public static final String IMG_OBJS_PHP_FOLDER = NAME_PREFIX + "folder_opened.gif"; //$NON-NLS-1$

	// Descriptors
	public static final ImageDescriptor DESC_OVR_WARNING = createUnManagedCached(T_OVR, "warning_co.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_OVR_ERROR = createUnManagedCached(T_OVR, "error_co.gif"); //$NON-NLS-1$
	public static final ImageDescriptor DESC_OBJS_WARNING = createManagedFromKey(T_OBJ, IMG_OBJS_WARNING);
	public static final ImageDescriptor DESC_OBJS_ENV_VAR = createManagedFromKey(T_OBJ, IMG_OBJS_ENV_VAR);
	public static final ImageDescriptor DESC_OBJS_LIBRARY = createManagedFromKey(T_OBJ, IMG_OBJS_LIBRARY);
	public static final ImageDescriptor DESC_OBJS_PHP_FOLDER = createManagedFromKey(T_OBJ, IMG_OBJS_PHP_FOLDER);
	public static final ImageDescriptor DESC_OBJS_PHP_PROJECT = createManagedFromKey(T_OBJ, IMG_OBJS_PHP_PROJECT);
	public static final ImageDescriptor DESC_OBJS_FIXABLE_ERROR = createManagedFromKey(T_OBJ, IMG_OBJS_FIXABLE_ERROR);

	private static final class CachedImageDescriptor extends ImageDescriptor
	{
		private ImageDescriptor fDescriptor;
		private ImageData fData;

		public CachedImageDescriptor(ImageDescriptor descriptor)
		{
			fDescriptor = descriptor;
		}

		public ImageData getImageData()
		{
			if (fData == null)
			{
				fData = fDescriptor.getImageData();
			}
			return fData;
		}
	}

	private static ImageDescriptor createUnManagedCached(String prefix, String name)
	{
		return new CachedImageDescriptor(create(prefix, name, true));
	}

	/**
	 * Returns the image managed under the given key in this registry.
	 * 
	 * @param key
	 *            the image's key
	 * @return the image managed under the given key
	 */
	public static Image get(String key)
	{
		return getImageRegistry().get(key);
	}

	/**
	 * Returns the image descriptor for the given key in this registry. Might be called in a non-UI thread.
	 * 
	 * @param key
	 *            the image's key
	 * @return the image descriptor for the given key
	 */
	public static ImageDescriptor getDescriptor(String key)
	{
		if (fgImageRegistry == null)
		{
			return (ImageDescriptor) fgAvoidSWTErrorMap.get(key);
		}
		return getImageRegistry().getDescriptor(key);
	}

	/**
	 * Sets the three image descriptors for enabled, disabled, and hovered to an action. The actions are retrieved from
	 * the *tool16 folders.
	 * 
	 * @param action
	 *            the action
	 * @param iconName
	 *            the icon name
	 */
	public static void setToolImageDescriptors(IAction action, String iconName)
	{
		setImageDescriptors(action, "tool16", iconName); //$NON-NLS-1$
	}

	/**
	 * Sets the three image descriptors for enabled, disabled, and hovered to an action. The actions are retrieved from
	 * the *lcl16 folders.
	 * 
	 * @param action
	 *            the action
	 * @param iconName
	 *            the icon name
	 */
	public static void setLocalImageDescriptors(IAction action, String iconName)
	{
		setImageDescriptors(action, "lcl16", iconName); //$NON-NLS-1$
	}

	/*
	 * Helper method to access the image registry from the DLTKPlugin class.
	 */
	/* package */static ImageRegistry getImageRegistry()
	{
		if (fgImageRegistry == null)
		{
			fgImageRegistry = new ImageRegistry();
			for (Iterator<String> iter = fgAvoidSWTErrorMap.keySet().iterator(); iter.hasNext();)
			{
				String key = (String) iter.next();
				fgImageRegistry.put(key, (ImageDescriptor) fgAvoidSWTErrorMap.get(key));
			}
			fgAvoidSWTErrorMap = null;
		}
		return fgImageRegistry;
	}

	// ---- Helper methods to access icons on the file system
	// --------------------------------------
	private static void setImageDescriptors(IAction action, String type, String relPath)
	{
		ImageDescriptor id = create("d" + type, relPath, false); //$NON-NLS-1$
		if (id != null)
			action.setDisabledImageDescriptor(id);
		/*
		 * id= create("c" + type, relPath, false); //$NON-NLS-1$ if (id != null) action.setHoverImageDescriptor(id);
		 */
		ImageDescriptor descriptor = create("e" + type, relPath); //$NON-NLS-1$
		action.setHoverImageDescriptor(descriptor);
		action.setImageDescriptor(descriptor);
	}

	private static ImageDescriptor createManagedFromKey(String prefix, String key)
	{
		return createManaged(prefix, key.substring(NAME_PREFIX_LENGTH), key);
	}

	private static ImageDescriptor createManaged(String prefix, String name, String key)
	{
		try
		{
			ImageDescriptor result = create(prefix, name, true);
			if (fgAvoidSWTErrorMap == null)
			{
				fgAvoidSWTErrorMap = new HashMap<String, ImageDescriptor>();
			}
			fgAvoidSWTErrorMap.put(key, result);
			if (fgImageRegistry != null)
			{
				if (PHPEplPlugin.DEBUG)
				{
					PHPEplPlugin.logError("Image registry already defined", null); //$NON-NLS-1$
				}
			}
			return result;
		}
		catch (Throwable ex)
		{
			PHPEplPlugin.logError(ex);
		}
		return null;
	}

	/*
	 * Creates an image descriptor for the given prefix and name in the DLTK UI bundle. The path can contain variables
	 * like $NL$. If no image could be found, <code>useMissingImageDescriptor</code> decides if either the 'missing
	 * image descriptor' is returned or <code>null</code>. or <code>null</code>.
	 */
	private static ImageDescriptor create(String prefix, String name, boolean useMissingImageDescriptor)
	{
		IPath path = ICONS_PATH.append(prefix).append(name);
		return createImageDescriptor(PHPEplPlugin.getDefault().getBundle(), path, useMissingImageDescriptor);
	}

	/*
	 * Creates an image descriptor for the given prefix and name in the DLTK UI bundle. The path can contain variables
	 * like $NL$. If no image could be found, the 'missing image descriptor' is returned.
	 */
	private static ImageDescriptor create(String prefix, String name)
	{
		return create(prefix, name, true);
	}

	/*
	 * Creates an image descriptor for the given path in a bundle. The path can contain variables like $NL$. If no image
	 * could be found, <code>useMissingImageDescriptor</code> decides if either the 'missing image descriptor' is
	 * returned or <code>null</code>. Added for 3.1.1.
	 */
	public static ImageDescriptor createImageDescriptor(Bundle bundle, IPath path, boolean useMissingImageDescriptor)
	{
		URL url = FileLocator.find(bundle, path, null);
		if (url != null)
		{
			return ImageDescriptor.createFromURL(url);
		}
		if (useMissingImageDescriptor)
		{
			return ImageDescriptor.getMissingImageDescriptor();
		}
		return null;
	}

	/*
	 * Creates an image descriptor for the given prefix and name in the DLTK UI bundle. The path can contain variables
	 * like $NL$. If no image could be found, the 'missing image descriptor' is returned.
	 */
	private static ImageDescriptor createUnManaged(String prefix, String name)
	{
		return create(prefix, name, true);
	}
}
