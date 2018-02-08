/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.formatter.preferences;

import com.aptana.formatter.IScriptFormatterFactory;
import com.aptana.formatter.ui.IFormatterModifyDialogOwner;
import com.aptana.formatter.ui.preferences.FormatterModifyDialog;

/**
 * PHP formatter settings dialog.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPFormatterModifyDialog extends FormatterModifyDialog
{
	/**
	 * Constructs a new PHPFormatterModifyDialog
	 * 
	 * @param dialogOwner
	 * @param formatterFactory
	 */
	public PHPFormatterModifyDialog(IFormatterModifyDialogOwner dialogOwner, IScriptFormatterFactory formatterFactory)
	{
		super(dialogOwner, formatterFactory);
	}

	protected void addPages()
	{
		addTabPage(Messages.PHPFormatterModifyDialog_newLinesTabName, new PHPFormatterNewLinesPage(this));
		addTabPage(Messages.PHPFormatterModifyDialog_intentationTabName, new PHPFormatterIndentationTabPage(this));
		addTabPage(Messages.PHPFormatterModifyDialog_blankLinesTabName, new PHPFormatterBlankLinesPage(this));
		addTabPage(Messages.PHPFormatterModifyDialog_bracesTabName, new PHPFormatterBracesPage(this));
		addTabPage(Messages.PHPFormatterModifyDialog_whiteSpacesTabName, new PHPFormatterWhiteSpacesPage(this));
		addTabPage(Messages.PHPFormatterModifyDialog_commentsTabName, new PHPFormatterCommentsPage(this));
		addTabPage(com.aptana.formatter.ui.preferences.Messages.FormatterModifyDialog_OffOnTags,
				new PHPFormatterOffOnPage(this));
	}
}
