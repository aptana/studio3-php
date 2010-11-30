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
package org.eclipse.php.internal.ui.preferences;

import com.aptana.editor.common.preferences.CommonEditorPreferencePage;
import com.aptana.editor.php.epl.PHPEplPlugin;

/**
 * The page for setting the editor options for occurrences marking.
 */
public final class PHPPreferencePage extends
		CommonEditorPreferencePage {

	/**
	 * HTMLPreferencePage
	 */
	public PHPPreferencePage()
	{
		super();
		setDescription("Preferences for the Aptana PHP Editor");
		setPreferenceStore( PHPEplPlugin.getDefault().getPreferenceStore());
	}
	
	

}
