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
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import com.aptana.core.util.StringUtil;
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
		Group wrappingGroup = SWTFactory.createGroup(parent, Messages.PHPFormatterWhiteSpacesPage_spacingSettingsGroup,
				1, 1, GridData.FILL_HORIZONTAL);

		// Parentheses Group
		ExpandableComposite expandibleComposite = SWTFactory.createExpandibleComposite(wrappingGroup,
				Messages.PHPFormatterWhiteSpacesPage_parenthesesGroupTitle, 4);
		Composite parenthesesGroup = SWTFactory
				.createComposite(expandibleComposite, 4, 20, 1, GridData.FILL_HORIZONTAL);
		expandibleComposite.setClient(parenthesesGroup);

		// @formatter:off
		SWTFactory.createCenteredLabel(parenthesesGroup, StringUtil.EMPTY);
		SWTFactory.createCenteredLabel(parenthesesGroup, Messages.PHPFormatterWhiteSpacesPage_parentheses_beforeOpening);
		SWTFactory.createCenteredLabel(parenthesesGroup, Messages.PHPFormatterWhiteSpacesPage_parentheses_afterOpening);
		SWTFactory.createCenteredLabel(parenthesesGroup, Messages.PHPFormatterWhiteSpacesPage_parentheses_beforeClosing);
		// @formatter:on

		// Declarations parentheses
		SWTFactory.createLabel(parenthesesGroup, Messages.PHPFormatterWhiteSpacesPage_declarationExpressions);
		manager.createSpinner(parenthesesGroup, PHPFormatterConstants.SPACES_BEFORE_OPENING_DECLARATION_PARENTHESES);
		manager.createSpinner(parenthesesGroup, PHPFormatterConstants.SPACES_AFTER_OPENING_DECLARATION_PARENTHESES);
		manager.createSpinner(parenthesesGroup, PHPFormatterConstants.SPACES_BEFORE_CLOSING_DECLARATION_PARENTHESES);

		// Invocations parentheses
		SWTFactory.createLabel(parenthesesGroup, Messages.PHPFormatterWhiteSpacesPage_invocationExpressions);
		manager.createSpinner(parenthesesGroup, PHPFormatterConstants.SPACES_BEFORE_OPENING_INVOCATION_PARENTHESES);
		manager.createSpinner(parenthesesGroup, PHPFormatterConstants.SPACES_AFTER_OPENING_INVOCATION_PARENTHESES);
		manager.createSpinner(parenthesesGroup, PHPFormatterConstants.SPACES_BEFORE_CLOSING_INVOCATION_PARENTHESES);

		// Conditionals parentheses
		SWTFactory.createLabel(parenthesesGroup, Messages.PHPFormatterWhiteSpacesPage_conditionalExpressions);
		manager.createSpinner(parenthesesGroup, PHPFormatterConstants.SPACES_BEFORE_OPENING_CONDITIONAL_PARENTHESES);
		manager.createSpinner(parenthesesGroup, PHPFormatterConstants.SPACES_AFTER_OPENING_CONDITIONAL_PARENTHESES);
		manager.createSpinner(parenthesesGroup, PHPFormatterConstants.SPACES_BEFORE_CLOSING_CONDITIONAL_PARENTHESES);

		// Loops parentheses
		SWTFactory.createLabel(parenthesesGroup, Messages.PHPFormatterWhiteSpacesPage_loopExpressions);
		manager.createSpinner(parenthesesGroup, PHPFormatterConstants.SPACES_BEFORE_OPENING_LOOP_PARENTHESES);
		manager.createSpinner(parenthesesGroup, PHPFormatterConstants.SPACES_AFTER_OPENING_LOOP_PARENTHESES);
		manager.createSpinner(parenthesesGroup, PHPFormatterConstants.SPACES_BEFORE_CLOSING_LOOP_PARENTHESES);

		// Array-access parentheses
		SWTFactory.createLabel(parenthesesGroup, Messages.PHPFormatterWhiteSpacesPage_arrayAccessExpressions);
		manager.createSpinner(parenthesesGroup, PHPFormatterConstants.SPACES_BEFORE_OPENING_ARRAY_ACCESS_PARENTHESES);
		manager.createSpinner(parenthesesGroup, PHPFormatterConstants.SPACES_AFTER_OPENING_ARRAY_ACCESS_PARENTHESES);
		manager.createSpinner(parenthesesGroup, PHPFormatterConstants.SPACES_BEFORE_CLOSING_ARRAY_ACCESS_PARENTHESES);

		// All the rest of the parenthesis types
		SWTFactory.createLabel(parenthesesGroup, Messages.PHPFormatterWhiteSpacesPage_otherParenthesesExpressions);
		manager.createSpinner(parenthesesGroup, PHPFormatterConstants.SPACES_BEFORE_OPENING_PARENTHESES);
		manager.createSpinner(parenthesesGroup, PHPFormatterConstants.SPACES_AFTER_OPENING_PARENTHESES);
		manager.createSpinner(parenthesesGroup, PHPFormatterConstants.SPACES_BEFORE_CLOSING_PARENTHESES);

		// Punctuation Group
		expandibleComposite = SWTFactory.createExpandibleComposite(wrappingGroup,
				Messages.PHPFormatterWhiteSpacesPage_puctuationElementsGroupTitle, 3);
		Composite punctuationGroup = SWTFactory
				.createComposite(expandibleComposite, 3, 20, 1, GridData.FILL_HORIZONTAL);
		expandibleComposite.setClient(punctuationGroup);
		SWTFactory.createCenteredLabel(punctuationGroup, StringUtil.EMPTY);
		SWTFactory.createCenteredLabel(punctuationGroup, Messages.PHPFormatterWhiteSpacesPage_before);
		SWTFactory.createCenteredLabel(punctuationGroup, Messages.PHPFormatterWhiteSpacesPage_after);

		// Comma
		SWTFactory.createLabel(punctuationGroup, Messages.PHPFormatterWhiteSpacesPage_commas);
		manager.createSpinner(punctuationGroup, PHPFormatterConstants.SPACES_BEFORE_COMMAS);
		manager.createSpinner(punctuationGroup, PHPFormatterConstants.SPACES_AFTER_COMMAS);

		// Semicolon in 'for' statements
		SWTFactory.createLabel(punctuationGroup, Messages.PHPFormatterWhiteSpacesPage_semicolonsInFor);
		manager.createSpinner(punctuationGroup, PHPFormatterConstants.SPACES_BEFORE_FOR_SEMICOLON);
		manager.createSpinner(punctuationGroup, PHPFormatterConstants.SPACES_AFTER_FOR_SEMICOLON);

		// Case colons
		SWTFactory.createLabel(punctuationGroup, Messages.PHPFormatterWhiteSpacesPage_caseColon);
		manager.createSpinner(punctuationGroup, PHPFormatterConstants.SPACES_BEFORE_CASE_COLON_OPERATOR);
		manager.createSpinner(punctuationGroup, PHPFormatterConstants.SPACES_AFTER_CASE_COLON_OPERATOR);

		// Operators Group
		expandibleComposite = SWTFactory.createExpandibleComposite(wrappingGroup,
				Messages.PHPFormatterWhiteSpacesPage_operatorsGroupTitle, 3);
		Composite operatorsGroup = SWTFactory.createComposite(expandibleComposite, 3, 20, 1, GridData.FILL_HORIZONTAL);
		expandibleComposite.setClient(operatorsGroup);

		SWTFactory.createCenteredLabel(operatorsGroup, StringUtil.EMPTY);
		SWTFactory.createCenteredLabel(operatorsGroup, Messages.PHPFormatterWhiteSpacesPage_before);
		SWTFactory.createCenteredLabel(operatorsGroup, Messages.PHPFormatterWhiteSpacesPage_after);

		// Arithmetic
		SWTFactory.createLabel(operatorsGroup, Messages.PHPFormatterWhiteSpacesPage_arithmeticOperators);
		manager.createSpinner(operatorsGroup, PHPFormatterConstants.SPACES_BEFORE_ARITHMETIC_OPERATOR);
		manager.createSpinner(operatorsGroup, PHPFormatterConstants.SPACES_AFTER_ARITHMETIC_OPERATOR);

		// Relational
		SWTFactory.createLabel(operatorsGroup, Messages.PHPFormatterWhiteSpacesPage_relationalOperators);
		manager.createSpinner(operatorsGroup, PHPFormatterConstants.SPACES_BEFORE_RELATIONAL_OPERATORS);
		manager.createSpinner(operatorsGroup, PHPFormatterConstants.SPACES_AFTER_RELATIONAL_OPERATORS);

		// Unary
		SWTFactory.createLabel(operatorsGroup, Messages.PHPFormatterWhiteSpacesPage_unaryOperators);
		manager.createSpinner(operatorsGroup, PHPFormatterConstants.SPACES_BEFORE_UNARY_OPERATOR);
		manager.createSpinner(operatorsGroup, PHPFormatterConstants.SPACES_AFTER_UNARY_OPERATOR);

		// Assignment
		SWTFactory.createLabel(operatorsGroup, Messages.PHPFormatterWhiteSpacesPage_assignments);
		manager.createSpinner(operatorsGroup, PHPFormatterConstants.SPACES_BEFORE_ASSIGNMENT_OPERATOR);
		manager.createSpinner(operatorsGroup, PHPFormatterConstants.SPACES_AFTER_ASSIGNMENT_OPERATOR);

		// Prefix
		SWTFactory.createLabel(operatorsGroup, Messages.PHPFormatterWhiteSpacesPage_prefixOperators);
		manager.createSpinner(operatorsGroup, PHPFormatterConstants.SPACES_BEFORE_ASSIGNMENT_OPERATOR);
		manager.createSpinner(operatorsGroup, PHPFormatterConstants.SPACES_AFTER_ASSIGNMENT_OPERATOR);

		// Postfix
		SWTFactory.createLabel(operatorsGroup, Messages.PHPFormatterWhiteSpacesPage_postfixOperators);
		manager.createSpinner(operatorsGroup, PHPFormatterConstants.SPACES_BEFORE_POSTFIX_OPERATOR);
		manager.createSpinner(operatorsGroup, PHPFormatterConstants.SPACES_AFTER_POSTFIX_OPERATOR);

		// Conditional
		SWTFactory.createLabel(operatorsGroup, Messages.PHPFormatterWhiteSpacesPage_conditionalOperators);
		manager.createSpinner(operatorsGroup, PHPFormatterConstants.SPACES_BEFORE_CONDITIONAL_OPERATOR);
		manager.createSpinner(operatorsGroup, PHPFormatterConstants.SPACES_AFTER_CONDITIONAL_OPERATOR);

		// Arrow
		SWTFactory.createLabel(operatorsGroup, Messages.PHPFormatterWhiteSpacesPage_invocationOperators);
		manager.createSpinner(operatorsGroup, PHPFormatterConstants.SPACES_BEFORE_ARROW_OPERATOR);
		manager.createSpinner(operatorsGroup, PHPFormatterConstants.SPACES_AFTER_ARROW_OPERATOR);

		// Static
		SWTFactory.createLabel(operatorsGroup, Messages.PHPFormatterWhiteSpacesPage_staticInvocationOperator);
		manager.createSpinner(operatorsGroup, PHPFormatterConstants.SPACES_BEFORE_STATIC_INVOCATION_OPERATOR);
		manager.createSpinner(operatorsGroup, PHPFormatterConstants.SPACES_AFTER_STATIC_INVOCATION_OPERATOR);

		// Dot
		SWTFactory.createLabel(operatorsGroup, Messages.PHPFormatterWhiteSpacesPage_concatenationOperator);
		manager.createSpinner(operatorsGroup, PHPFormatterConstants.SPACES_BEFORE_CONCATENATION_OPERATOR);
		manager.createSpinner(operatorsGroup, PHPFormatterConstants.SPACES_AFTER_CONCATENATION_OPERATOR);

		// Key-Value
		SWTFactory.createLabel(operatorsGroup, Messages.PHPFormatterWhiteSpacesPage_keyValueOperator);
		manager.createSpinner(operatorsGroup, PHPFormatterConstants.SPACES_BEFORE_KEY_VALUE_OPERATOR);
		manager.createSpinner(operatorsGroup, PHPFormatterConstants.SPACES_AFTER_KEY_VALUE_OPERATOR);
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
