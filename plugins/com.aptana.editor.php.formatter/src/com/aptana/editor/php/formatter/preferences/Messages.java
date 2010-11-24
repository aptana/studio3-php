/**
 * This file Copyright (c) 2005-2010 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain other free and open source software ("FOSS") code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.formatter.preferences;

import org.eclipse.osgi.util.NLS;

/**
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.aptana.editor.php.formatter.preferences.Messages"; //$NON-NLS-1$
	public static String PHPFormatterBlankLinesPage_afterClassDeclaration;
	public static String PHPFormatterBlankLinesPage_afterFunctionDeclaration;
	public static String PHPFormatterBlankLinesPage_blankLinesGroupLabel;
	public static String PHPFormatterBlankLinesPage_existingBlankLinesGroupLabel;
	public static String PHPFormatterBlankLinesPage_existingBlankLinesToPreserve;
	public static String PHPFormatterBracesPage_blocks;
	public static String PHPFormatterBracesPage_caseStateMent;
	public static String PHPFormatterBracesPage_classDeclaraion;
	public static String PHPFormatterBracesPage_functionDeclaraion;
	public static String PHPFormatterBracesPage_switchStatement;
	public static String PHPFormatterCommentsPage_enableWrapping;
	public static String PHPFormatterCommentsPage_formattingGroupLabel;
	public static String PHPFormatterCommentsPage_maxLineWidth;
	public static String PHPFormatterTabPage_indentGroupLabel;
	public static String PHPFormatterTabPage_newLinesGroupLabel;
	public static String PHPFormatterIndentationTabPage_indentationGeneralGroupLabel;
	public static String PHPFormatterModifyDialog_blankLinesTabName;
	public static String PHPFormatterModifyDialog_commentsTabName;
	public static String PHPFormatterModifyDialog_jsFormatterTitle;
	public static String PHPFormatterModifyDialog_intentationTabName;
	public static String PHPFormatterModifyDialog_bracesTabName;

	public static String PHPFormatterModifyDialog_newLinesTabName;
	public static String PHPFormatterNewLinesPage_newLineBeforeBlocks;
	public static String PHPFormatterNewLinesPage_newLineBeforeCatch;
	public static String PHPFormatterNewLinesPage_newLineBeforeElse;
	public static String PHPFormatterNewLinesPage_newLineBreakElseIf;
	public static String PHPFormatterNewLinesPage_newLineBeforeIf;
	public static String PHPFormatterModifyDialog_whiteSpacesTabName;
	public static String PHPFormatterNewLinesPage_newLineBeforeWhileInDo;

	public static String PHPFormatterIndentationTabPage_statementsWithinBlocks;
	public static String PHPFormatterIndentationTabPage_statementsWithinNamespaceBlocks;
	public static String PHPFormatterIndentationTabPage_statementsWithinSwitch;
	public static String PHPFormatterIndentationTabPage_statementsWithinCase;
	public static String PHPFormatterIndentationTabPage_statementsWithinTypes;
	public static String PHPFormatterIndentationTabPage_statementsWithinFunctions;
	public static String PHPFormatterIndentationTabPage_breakWithinCase;
	public static String PHPFormatterWhiteSpacesPage_after;
	public static String PHPFormatterWhiteSpacesPage_arithmeticOperators;
	public static String PHPFormatterWhiteSpacesPage_assignments;
	public static String PHPFormatterWhiteSpacesPage_before;
	public static String PHPFormatterWhiteSpacesPage_caseColon;
	public static String PHPFormatterWhiteSpacesPage_semicolonsInFor;
	public static String PHPFormatterWhiteSpacesPage_parentheses;
	public static String PHPFormatterWhiteSpacesPage_commas;
	public static String PHPFormatterWhiteSpacesPage_concatenationOperator;
	public static String PHPFormatterWhiteSpacesPage_conditionalOperators;
	public static String PHPFormatterWhiteSpacesPage_invocationOperators;
	public static String PHPFormatterWhiteSpacesPage_keyValueOperator;
	public static String PHPFormatterWhiteSpacesPage_operatorsGroupTitle;
	public static String PHPFormatterWhiteSpacesPage_postfixOperators;
	public static String PHPFormatterWhiteSpacesPage_prefixOperators;
	public static String PHPFormatterWhiteSpacesPage_puctuationElementsGroupTitle;
	public static String PHPFormatterWhiteSpacesPage_relationalOperators;
	public static String PHPFormatterWhiteSpacesPage_staticInvocationOperator;
	public static String PHPFormatterWhiteSpacesPage_unaryOperators;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
