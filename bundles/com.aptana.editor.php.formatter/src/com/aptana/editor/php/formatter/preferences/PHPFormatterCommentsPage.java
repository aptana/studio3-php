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
 * A formatter comments page for PHP.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPFormatterCommentsPage extends FormatterModifyTabPage
{
	private static final String WRAPPING_PREVIEW_FILE = "wrapping-preview.php"; //$NON-NLS-1$

	/**
	 * @param dialog
	 */
	public PHPFormatterCommentsPage(IFormatterModifyDialog dialog)
	{
		super(dialog);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.aptana.formatter.ui.FormatterModifyTabPage#createOptions(com.aptana.formatter.ui.IFormatterControlManager,
	 * org.eclipse.swt.widgets.Composite)
	 */
	protected void createOptions(IFormatterControlManager manager, Composite parent)
	{
		Group commentWrappingGroup = SWTFactory.createGroup(parent,
				Messages.PHPFormatterCommentsPage_formattingGroupLabel, 2, 1, GridData.FILL_HORIZONTAL);
		manager.createCheckbox(commentWrappingGroup, PHPFormatterConstants.WRAP_COMMENTS,
				Messages.PHPFormatterCommentsPage_enableWrapping, 2);
		manager.createNumber(commentWrappingGroup, PHPFormatterConstants.WRAP_COMMENTS_LENGTH,
				Messages.PHPFormatterCommentsPage_maxLineWidth);

	}

	protected URL getPreviewContent()
	{
		return getClass().getResource(WRAPPING_PREVIEW_FILE);
	}
}
