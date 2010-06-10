/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Zend Technologies
 *******************************************************************************/
package org.eclipse.php.internal.core.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.php.internal.core.IPHPEplCoreConstants;

import com.aptana.editor.php.epl.PHPEplPlugin;

/**
 * PHP (EPL) core preferences constants.
 */
public class CorePreferenceConstants
{

	public interface Keys
	{
		public static final String PHP_VERSION = "phpVersion"; //$NON-NLS-1$
		public static final String EDITOR_USE_ASP_TAGS = "use_asp_tags_as_php"; //$NON-NLS-1$
	}

	public static IPreferenceStore getPreferenceStore()
	{
		return PHPEplPlugin.getDefault().getPreferenceStore();
	}

	/**
	 * Initializes the given preference store with the default values.
	 * 
	 * @param store
	 *            the preference store to be initialized
	 */
	public static void initializeDefaultValues()
	{
		IPreferenceStore store = getPreferenceStore();
		store.setDefault(IPHPEplCoreConstants.TASK_TAGS, IPHPEplCoreConstants.DEFAULT_TASK_TAGS);
		store.setDefault(IPHPEplCoreConstants.TASK_PRIORITIES, IPHPEplCoreConstants.DEFAULT_TASK_PRIORITIES);
		store.setDefault(IPHPEplCoreConstants.TASK_CASE_SENSITIVE, IPHPEplCoreConstants.ENABLED);
		store
				.setDefault(IPHPEplCoreConstants.FORMATTER_INDENTATION_SIZE,
						IPHPEplCoreConstants.DEFAULT_INDENTATION_SIZE);
		store.setDefault(IPHPEplCoreConstants.FORMATTER_USE_TABS, true);
		/*
		 * IEclipsePreferences node = new DefaultScope().getNode(PHPCorePlugin.ID); node.put(Keys.PHP_VERSION,
		 * PHPVersion.PHP5.getAlias()); node .put(PHPCoreConstants.TASK_TAGS, PHPCoreConstants.DEFAULT_TASK_TAGS);
		 * node.put(PHPCoreConstants.TASK_PRIORITIES, PHPCoreConstants.DEFAULT_TASK_PRIORITIES); node
		 * .put(PHPCoreConstants.TASK_CASE_SENSITIVE, PHPCoreConstants.ENABLED);
		 * node.putBoolean(Keys.EDITOR_USE_ASP_TAGS, false); node.putBoolean(PHPCoreConstants.CODEGEN_ADD_COMMENTS,
		 * false); node.put(PHPCoreConstants.WORKSPACE_DEFAULT_LOCALE, ULocale .getDefault().toString());
		 * node.put(PHPCoreConstants.WORKSPACE_LOCALE, ULocale.getDefault() .toString());
		 * node.putBoolean(PHPCoreConstants.CODEASSIST_ADDIMPORT, true);
		 * node.putBoolean(PHPCoreConstants.CODEASSIST_FILL_ARGUMENT_NAMES, false);
		 * node.putBoolean(PHPCoreConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, true);
		 * node.putBoolean(PHPCoreConstants.CODEASSIST_AUTOINSERT, true);
		 * node.putBoolean(PHPCoreConstants.CODEASSIST_INSERT_COMPLETION, true); node.putBoolean(
		 * PHPCoreConstants.CODEASSIST_SHOW_VARIABLES_FROM_OTHER_FILES, false);
		 * node.putBoolean(PHPCoreConstants.CODEASSIST_SHOW_STRICT_OPTIONS, false);
		 * node.putBoolean(PHPCoreConstants.CODEASSIST_AUTOACTIVATION, true);
		 * node.putInt(PHPCoreConstants.CODEASSIST_AUTOACTIVATION_DELAY, 500);
		 */
	}

	// Don't instantiate
	private CorePreferenceConstants()
	{
	}
}
