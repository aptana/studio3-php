/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.formatter.preferences;

import java.net.URL;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import com.aptana.editor.php.formatter.PHPFormatterConstants;
import com.aptana.formatter.ui.IFormatterControlManager;
import com.aptana.formatter.ui.IFormatterModifyDialog;
import com.aptana.formatter.ui.preferences.FormatterModifyTabPage;
import com.aptana.formatter.ui.util.SWTFactory;

/**
 * Blank-lines configuration tab for the PHP code formatter.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPFormatterBlankLinesPage extends FormatterModifyTabPage
{

	private static final String BLANK_LINES_PREVIEW_FILE = "blank-lines-preview.php"; //$NON-NLS-1$

	/**
	 * @param dialog
	 */
	public PHPFormatterBlankLinesPage(IFormatterModifyDialog dialog)
	{
		super(dialog);
	}

	protected void createOptions(IFormatterControlManager manager, Composite parent)
	{
		Group blankLinesGroup = SWTFactory.createGroup(parent, Messages.PHPFormatterBlankLinesPage_blankLinesGroupLabel,
				2, 1, GridData.FILL_HORIZONTAL);
		manager.createNumber(blankLinesGroup, PHPFormatterConstants.LINES_AFTER_TYPE_DECLARATION,
				Messages.PHPFormatterBlankLinesPage_afterClassDeclaration);
		manager.createNumber(blankLinesGroup, PHPFormatterConstants.LINES_AFTER_FUNCTION_DECLARATION,
				Messages.PHPFormatterBlankLinesPage_afterFunctionDeclaration);

		Group preserveLinesGroup = SWTFactory.createGroup(parent,
				Messages.PHPFormatterBlankLinesPage_existingBlankLinesGroupLabel, 2, 1, GridData.FILL_HORIZONTAL);
		manager.createNumber(preserveLinesGroup, PHPFormatterConstants.PRESERVED_LINES,
				Messages.PHPFormatterBlankLinesPage_existingBlankLinesToPreserve);
	}

	protected URL getPreviewContent()
	{
		return getClass().getResource(BLANK_LINES_PREVIEW_FILE);
	}

}
