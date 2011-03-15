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
import com.aptana.formatter.ui.preferences.FormatterModifyDialog;
import com.aptana.formatter.ui.preferences.FormatterModifyTabPage;
import com.aptana.formatter.ui.util.SWTFactory;

/**
 * A PHP formatter tab for new-lines insertions.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPFormatterNewLinesPage extends FormatterModifyTabPage
{

	private static final String NEW_LINES_PREVIEW_FILE = "indentation-preview.php"; //$NON-NLS-1$

	/**
	 * Constructor.
	 * 
	 * @param dialog
	 *            A {@link FormatterModifyDialog}
	 */
	public PHPFormatterNewLinesPage(FormatterModifyDialog dialog)
	{
		super(dialog);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.aptana.formatter.ui.FormatterModifyTabPage#createOptions(com.aptana.formatter.ui.IFormatterControlManager,
	 * org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createOptions(IFormatterControlManager manager, Composite parent)
	{
		Group group = SWTFactory.createGroup(parent, Messages.PHPFormatterTabPage_newLinesGroupLabel, 1, 1,
				GridData.FILL_HORIZONTAL);
		manager.createCheckbox(group, PHPFormatterConstants.NEW_LINES_BEFORE_ELSE_STATEMENT,
				Messages.PHPFormatterNewLinesPage_newLineBeforeElse);
		manager.createCheckbox(group, PHPFormatterConstants.NEW_LINES_BEFORE_IF_IN_ELSEIF_STATEMENT,
				Messages.PHPFormatterNewLinesPage_newLineBreakElseIf);
		manager.createCheckbox(group, PHPFormatterConstants.NEW_LINES_BEFORE_CATCH_STATEMENT,
				Messages.PHPFormatterNewLinesPage_newLineBeforeCatch);
		manager.createCheckbox(group, PHPFormatterConstants.NEW_LINES_BEFORE_DO_WHILE_STATEMENT,
				Messages.PHPFormatterNewLinesPage_newLineBeforeWhileInDo);
		manager.createCheckbox(group, PHPFormatterConstants.NEW_LINES_BETWEEN_ARRAY_CREATION_ELEMENTS,
				Messages.PHPFormatterNewLinesPage_newLineBetweenArrayCreationElement);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.ui.FormatterModifyTabPage#getPreviewContent()
	 */
	protected URL getPreviewContent()
	{
		return getClass().getResource(NEW_LINES_PREVIEW_FILE);
	}
}
