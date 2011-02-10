/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.formatter.preferences;

import java.net.URL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.aptana.editor.php.formatter.PHPFormatterConstants;
import com.aptana.formatter.ui.IFormatterControlManager;
import com.aptana.formatter.ui.IFormatterModifyDialog;
import com.aptana.formatter.ui.preferences.FormatterModifyTabPage;
import com.aptana.formatter.ui.util.SWTFactory;

/**
 * White-Spaces configuration tab for the PHP code formatter.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPFormatterWhiteSpacesPage extends FormatterModifyTabPage
{

	private static final String WHITE_SPACES_PREVIEW_FILE = "white-spaces-preview.php"; //$NON-NLS-1$

	/**
	 * Constructs a new PHPFormatterWhiteSpacesPage
	 * 
	 * @param dialog
	 */
	public PHPFormatterWhiteSpacesPage(IFormatterModifyDialog dialog)
	{
		super(dialog);
	}

	/*
	 * (non-Javadoc)
	 * @seecom.aptana.formatter.ui.preferences.FormatterModifyTabPage#createOptions(com.aptana.formatter.ui.
	 * IFormatterControlManager, org.eclipse.swt.widgets.Composite)
	 */
	protected void createOptions(IFormatterControlManager manager, Composite parent)
	{
		// Punctuation Group
		Group punctuationGroup = SWTFactory.createGroup(parent,
				Messages.PHPFormatterWhiteSpacesPage_puctuationElementsGroupTitle, 5, 1, GridData.FILL_HORIZONTAL);
		// Comma
		Label label = new Label(punctuationGroup, SWT.NONE);
		label.setText(Messages.PHPFormatterWhiteSpacesPage_commas);
		manager.createNumber(punctuationGroup, PHPFormatterConstants.SPACES_BEFORE_COMMAS,
				Messages.PHPFormatterWhiteSpacesPage_before);
		manager.createNumber(punctuationGroup, PHPFormatterConstants.SPACES_AFTER_COMMAS,
				Messages.PHPFormatterWhiteSpacesPage_after);

		// Parentheses
		label = new Label(punctuationGroup, SWT.NONE);
		label.setText(Messages.PHPFormatterWhiteSpacesPage_parentheses);
		manager.createNumber(punctuationGroup, PHPFormatterConstants.SPACES_BEFORE_PARENTHESES,
				Messages.PHPFormatterWhiteSpacesPage_before);
		manager.createNumber(punctuationGroup, PHPFormatterConstants.SPACES_AFTER_PARENTHESES,
				Messages.PHPFormatterWhiteSpacesPage_after);
		
		// Semicolon in 'for' statements
		label = new Label(punctuationGroup, SWT.NONE);
		label.setText(Messages.PHPFormatterWhiteSpacesPage_semicolonsInFor);
		manager.createNumber(punctuationGroup, PHPFormatterConstants.SPACES_BEFORE_FOR_SEMICOLON,
				Messages.PHPFormatterWhiteSpacesPage_before);
		manager.createNumber(punctuationGroup, PHPFormatterConstants.SPACES_AFTER_FOR_SEMICOLON,
				Messages.PHPFormatterWhiteSpacesPage_after);

		// Case colons
		label = new Label(punctuationGroup, SWT.NONE);
		label.setText(Messages.PHPFormatterWhiteSpacesPage_caseColon);
		manager.createNumber(punctuationGroup, PHPFormatterConstants.SPACES_BEFORE_CASE_COLON_OPERATOR,
				Messages.PHPFormatterWhiteSpacesPage_before);
		manager.createNumber(punctuationGroup, PHPFormatterConstants.SPACES_AFTER_CASE_COLON_OPERATOR,
				Messages.PHPFormatterWhiteSpacesPage_after);

		// Operators Group
		Group operatorsGroup = SWTFactory.createGroup(parent, Messages.PHPFormatterWhiteSpacesPage_operatorsGroupTitle,
				5, 1, GridData.FILL_HORIZONTAL);
		// Arithmetic
		label = new Label(operatorsGroup, SWT.NONE);
		label.setText(Messages.PHPFormatterWhiteSpacesPage_arithmeticOperators);
		manager.createNumber(operatorsGroup, PHPFormatterConstants.SPACES_BEFORE_ARITHMETIC_OPERATOR,
				Messages.PHPFormatterWhiteSpacesPage_before);
		manager.createNumber(operatorsGroup, PHPFormatterConstants.SPACES_AFTER_ARITHMETIC_OPERATOR,
				Messages.PHPFormatterWhiteSpacesPage_after);

		// Relational
		label = new Label(operatorsGroup, SWT.NONE);
		label.setText(Messages.PHPFormatterWhiteSpacesPage_relationalOperators);
		manager.createNumber(operatorsGroup, PHPFormatterConstants.SPACES_BEFORE_RELATIONAL_OPERATORS,
				Messages.PHPFormatterWhiteSpacesPage_before);
		manager.createNumber(operatorsGroup, PHPFormatterConstants.SPACES_AFTER_RELATIONAL_OPERATORS,
				Messages.PHPFormatterWhiteSpacesPage_after);

		// Unary
		label = new Label(operatorsGroup, SWT.NONE);
		label.setText(Messages.PHPFormatterWhiteSpacesPage_unaryOperators);
		manager.createNumber(operatorsGroup, PHPFormatterConstants.SPACES_BEFORE_UNARY_OPERATOR,
				Messages.PHPFormatterWhiteSpacesPage_before);
		manager.createNumber(operatorsGroup, PHPFormatterConstants.SPACES_AFTER_UNARY_OPERATOR,
				Messages.PHPFormatterWhiteSpacesPage_after);

		// Assignment
		label = new Label(operatorsGroup, SWT.NONE);
		label.setText(Messages.PHPFormatterWhiteSpacesPage_assignments);
		manager.createNumber(operatorsGroup, PHPFormatterConstants.SPACES_BEFORE_ASSIGNMENT_OPERATOR,
				Messages.PHPFormatterWhiteSpacesPage_before);
		manager.createNumber(operatorsGroup, PHPFormatterConstants.SPACES_AFTER_ASSIGNMENT_OPERATOR,
				Messages.PHPFormatterWhiteSpacesPage_after);

		// Prefix
		label = new Label(operatorsGroup, SWT.NONE);
		label.setText(Messages.PHPFormatterWhiteSpacesPage_prefixOperators);
		manager.createNumber(operatorsGroup, PHPFormatterConstants.SPACES_BEFORE_PREFIX_OPERATOR,
				Messages.PHPFormatterWhiteSpacesPage_before);
		manager.createNumber(operatorsGroup, PHPFormatterConstants.SPACES_AFTER_PREFIX_OPERATOR,
				Messages.PHPFormatterWhiteSpacesPage_after);

		// Postfix
		label = new Label(operatorsGroup, SWT.NONE);
		label.setText(Messages.PHPFormatterWhiteSpacesPage_postfixOperators);
		manager.createNumber(operatorsGroup, PHPFormatterConstants.SPACES_BEFORE_POSTFIX_OPERATOR,
				Messages.PHPFormatterWhiteSpacesPage_before);
		manager.createNumber(operatorsGroup, PHPFormatterConstants.SPACES_AFTER_POSTFIX_OPERATOR,
				Messages.PHPFormatterWhiteSpacesPage_after);

		// Conditional
		label = new Label(operatorsGroup, SWT.NONE);
		label.setText(Messages.PHPFormatterWhiteSpacesPage_conditionalOperators);
		manager.createNumber(operatorsGroup, PHPFormatterConstants.SPACES_BEFORE_CONDITIONAL_OPERATOR,
				Messages.PHPFormatterWhiteSpacesPage_before);
		manager.createNumber(operatorsGroup, PHPFormatterConstants.SPACES_AFTER_CONDITIONAL_OPERATOR,
				Messages.PHPFormatterWhiteSpacesPage_after);

		// Arrow
		label = new Label(operatorsGroup, SWT.NONE);
		label.setText(Messages.PHPFormatterWhiteSpacesPage_invocationOperators);
		manager.createNumber(operatorsGroup, PHPFormatterConstants.SPACES_BEFORE_ARROW_OPERATOR,
				Messages.PHPFormatterWhiteSpacesPage_before);
		manager.createNumber(operatorsGroup, PHPFormatterConstants.SPACES_AFTER_ARROW_OPERATOR,
				Messages.PHPFormatterWhiteSpacesPage_after);

		// Static
		label = new Label(operatorsGroup, SWT.NONE);
		label.setText(Messages.PHPFormatterWhiteSpacesPage_staticInvocationOperator);
		manager.createNumber(operatorsGroup, PHPFormatterConstants.SPACES_BEFORE_STATIC_INVOCATION_OPERATOR,
				Messages.PHPFormatterWhiteSpacesPage_before);
		manager.createNumber(operatorsGroup, PHPFormatterConstants.SPACES_AFTER_STATIC_INVOCATION_OPERATOR,
				Messages.PHPFormatterWhiteSpacesPage_after);

		// Dot
		label = new Label(operatorsGroup, SWT.NONE);
		label.setText(Messages.PHPFormatterWhiteSpacesPage_concatenationOperator);
		manager.createNumber(operatorsGroup, PHPFormatterConstants.SPACES_BEFORE_CONCATENATION_OPERATOR,
				Messages.PHPFormatterWhiteSpacesPage_before);
		manager.createNumber(operatorsGroup, PHPFormatterConstants.SPACES_AFTER_CONCATENATION_OPERATOR,
				Messages.PHPFormatterWhiteSpacesPage_after);

		// Key-Value
		label = new Label(operatorsGroup, SWT.NONE);
		label.setText(Messages.PHPFormatterWhiteSpacesPage_keyValueOperator);
		manager.createNumber(operatorsGroup, PHPFormatterConstants.SPACES_BEFORE_KEY_VALUE_OPERATOR,
				Messages.PHPFormatterWhiteSpacesPage_before);
		manager.createNumber(operatorsGroup, PHPFormatterConstants.SPACES_AFTER_KEY_VALUE_OPERATOR,
				Messages.PHPFormatterWhiteSpacesPage_after);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.formatter.ui.preferences.FormatterModifyTabPage#getPreviewContent()
	 */
	protected URL getPreviewContent()
	{
		return getClass().getResource(WHITE_SPACES_PREVIEW_FILE);
	}

}
