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
package com.aptana.editor.php.formatter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.php.internal.core.ast.nodes.ASTError;
import org.eclipse.php.internal.core.ast.nodes.ASTNode;
import org.eclipse.php.internal.core.ast.nodes.ArrayAccess;
import org.eclipse.php.internal.core.ast.nodes.ArrayCreation;
import org.eclipse.php.internal.core.ast.nodes.ArrayElement;
import org.eclipse.php.internal.core.ast.nodes.Assignment;
import org.eclipse.php.internal.core.ast.nodes.BackTickExpression;
import org.eclipse.php.internal.core.ast.nodes.Block;
import org.eclipse.php.internal.core.ast.nodes.BreakStatement;
import org.eclipse.php.internal.core.ast.nodes.CastExpression;
import org.eclipse.php.internal.core.ast.nodes.CatchClause;
import org.eclipse.php.internal.core.ast.nodes.ClassDeclaration;
import org.eclipse.php.internal.core.ast.nodes.ClassInstanceCreation;
import org.eclipse.php.internal.core.ast.nodes.ClassName;
import org.eclipse.php.internal.core.ast.nodes.CloneExpression;
import org.eclipse.php.internal.core.ast.nodes.Comment;
import org.eclipse.php.internal.core.ast.nodes.ConditionalExpression;
import org.eclipse.php.internal.core.ast.nodes.ConstantDeclaration;
import org.eclipse.php.internal.core.ast.nodes.ContinueStatement;
import org.eclipse.php.internal.core.ast.nodes.DeclareStatement;
import org.eclipse.php.internal.core.ast.nodes.DoStatement;
import org.eclipse.php.internal.core.ast.nodes.EchoStatement;
import org.eclipse.php.internal.core.ast.nodes.EmptyStatement;
import org.eclipse.php.internal.core.ast.nodes.Expression;
import org.eclipse.php.internal.core.ast.nodes.ExpressionStatement;
import org.eclipse.php.internal.core.ast.nodes.FieldAccess;
import org.eclipse.php.internal.core.ast.nodes.FieldsDeclaration;
import org.eclipse.php.internal.core.ast.nodes.ForEachStatement;
import org.eclipse.php.internal.core.ast.nodes.ForStatement;
import org.eclipse.php.internal.core.ast.nodes.FormalParameter;
import org.eclipse.php.internal.core.ast.nodes.FunctionDeclaration;
import org.eclipse.php.internal.core.ast.nodes.FunctionInvocation;
import org.eclipse.php.internal.core.ast.nodes.FunctionName;
import org.eclipse.php.internal.core.ast.nodes.GlobalStatement;
import org.eclipse.php.internal.core.ast.nodes.GotoLabel;
import org.eclipse.php.internal.core.ast.nodes.GotoStatement;
import org.eclipse.php.internal.core.ast.nodes.Identifier;
import org.eclipse.php.internal.core.ast.nodes.IfStatement;
import org.eclipse.php.internal.core.ast.nodes.IgnoreError;
import org.eclipse.php.internal.core.ast.nodes.InLineHtml;
import org.eclipse.php.internal.core.ast.nodes.Include;
import org.eclipse.php.internal.core.ast.nodes.InfixExpression;
import org.eclipse.php.internal.core.ast.nodes.InstanceOfExpression;
import org.eclipse.php.internal.core.ast.nodes.InterfaceDeclaration;
import org.eclipse.php.internal.core.ast.nodes.LambdaFunctionDeclaration;
import org.eclipse.php.internal.core.ast.nodes.ListVariable;
import org.eclipse.php.internal.core.ast.nodes.MethodDeclaration;
import org.eclipse.php.internal.core.ast.nodes.MethodInvocation;
import org.eclipse.php.internal.core.ast.nodes.NamespaceDeclaration;
import org.eclipse.php.internal.core.ast.nodes.NamespaceName;
import org.eclipse.php.internal.core.ast.nodes.ParenthesisExpression;
import org.eclipse.php.internal.core.ast.nodes.PostfixExpression;
import org.eclipse.php.internal.core.ast.nodes.PrefixExpression;
import org.eclipse.php.internal.core.ast.nodes.Program;
import org.eclipse.php.internal.core.ast.nodes.Quote;
import org.eclipse.php.internal.core.ast.nodes.Reference;
import org.eclipse.php.internal.core.ast.nodes.ReflectionVariable;
import org.eclipse.php.internal.core.ast.nodes.ReturnStatement;
import org.eclipse.php.internal.core.ast.nodes.Scalar;
import org.eclipse.php.internal.core.ast.nodes.SingleFieldDeclaration;
import org.eclipse.php.internal.core.ast.nodes.Statement;
import org.eclipse.php.internal.core.ast.nodes.StaticConstantAccess;
import org.eclipse.php.internal.core.ast.nodes.StaticFieldAccess;
import org.eclipse.php.internal.core.ast.nodes.StaticMethodInvocation;
import org.eclipse.php.internal.core.ast.nodes.StaticStatement;
import org.eclipse.php.internal.core.ast.nodes.SwitchCase;
import org.eclipse.php.internal.core.ast.nodes.SwitchStatement;
import org.eclipse.php.internal.core.ast.nodes.ThrowStatement;
import org.eclipse.php.internal.core.ast.nodes.TryStatement;
import org.eclipse.php.internal.core.ast.nodes.TypeDeclaration;
import org.eclipse.php.internal.core.ast.nodes.UnaryOperation;
import org.eclipse.php.internal.core.ast.nodes.UseStatement;
import org.eclipse.php.internal.core.ast.nodes.UseStatementPart;
import org.eclipse.php.internal.core.ast.nodes.Variable;
import org.eclipse.php.internal.core.ast.nodes.WhileStatement;
import org.eclipse.php.internal.core.ast.visitor.AbstractVisitor;

import com.aptana.core.util.StringUtil;
import com.aptana.editor.php.formatter.nodes.FormatterPHPAssignmentNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPBlockNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPCaseBodyNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPCaseColonNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPCaseNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPCommaNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPDeclarationNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPDefaultLineNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPEchoNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPElseIfNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPElseNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPExpressionWrapperNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPFunctionBodyNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPFunctionInvocationNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPIfNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPInvocationTextNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPLoopNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPModifierNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPNonBlockedWhileNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPSwitchNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPTextNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPTypeBodyNode;
import com.aptana.editor.php.formatter.nodes.PHPFormatterBreakNode;
import com.aptana.formatter.FormatterDocument;
import com.aptana.formatter.nodes.IFormatterContainerNode;

/**
 * A PHP formatter node builder.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPFormatterVisitor extends AbstractVisitor
{

	// Match words in a string
	private static final Pattern WORD_PATTERN = Pattern.compile("\\w+"); //$NON-NLS-1$
	public static final String INVOCATION_ARROW = "->"; //$NON-NLS-1$
	public static final String STATIC_INVOCATION = "::"; //$NON-NLS-1$
	private static final char[] SEMICOLON_AND_COLON = new char[] { ';', ',' };
	private static final char[] SEMICOLON = new char[] { ';' };

	private FormatterDocument document;
	private PHPFormatterNodeBuilder builder;

	/**
	 * @param builder
	 * @param document
	 */
	public PHPFormatterVisitor(FormatterDocument document, PHPFormatterNodeBuilder builder)
	{
		this.document = document;
		this.builder = builder;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.IfStatement
	 * )
	 */
	@Override
	public boolean visit(IfStatement ifStatement)
	{
		Statement falseStatement = ifStatement.getFalseStatement();
		Statement trueStatement = ifStatement.getTrueStatement();

		boolean isEmptyFalseBlock = (falseStatement == null);
		boolean hasTrueBlock = (trueStatement.getType() == ASTNode.BLOCK);
		boolean hasFalseBlock = (!isEmptyFalseBlock && falseStatement.getType() == ASTNode.BLOCK);
		// First, construct the if condition node
		FormatterPHPIfNode conditionNode = new FormatterPHPIfNode(document, hasTrueBlock, ifStatement);
		conditionNode.setBegin(builder.createTextNode(document, ifStatement.getStart(), ifStatement.getCondition()
				.getEnd() + 1));
		builder.push(conditionNode);

		// Construct the 'true' part of the 'if' and visit its children
		if (hasTrueBlock)
		{
			visitBlockNode((Block) trueStatement, ifStatement, isEmptyFalseBlock);
		}
		else
		{
			// Just visit the children
			trueStatement.accept(this);
		}
		builder.checkedPop(conditionNode, trueStatement.getEnd());

		if (!isEmptyFalseBlock)
		{
			// Construct the 'false' part if exist.
			// Note that the JS parser does not provide us with the start offset of the 'else' keyword, so we need
			// to locate it in between the end of the 'true' block and the begin of the 'false' block.
			// However, in case we have an 'elseif' case, the offset of the false block points to the start of the
			// 'elseif' word.
			int trueBlockEnd = trueStatement.getEnd();
			int falseBlockStart = falseStatement.getStart();
			String segment = document.get(trueBlockEnd + 1, falseBlockStart);
			int elsePos = segment.toLowerCase().indexOf("else"); //$NON-NLS-1$
			boolean isElseIf = (falseStatement.getType() == ASTNode.IF_STATEMENT);
			boolean isConnectedElsif = (isElseIf && elsePos < 0);
			FormatterPHPElseNode elseNode = null;
			if (!isConnectedElsif)
			{
				int elseBlockStart = elsePos + trueBlockEnd + 1;
				int elseBlockDeclarationEnd = elseBlockStart + 4; // +4 for the keyword 'else'
				elseNode = new FormatterPHPElseNode(document, hasFalseBlock, isElseIf, hasTrueBlock);
				elseNode.setBegin(builder.createTextNode(document, elseBlockStart, elseBlockDeclarationEnd));
				builder.push(elseNode);
			}
			if (!isConnectedElsif && hasFalseBlock)
			{
				visitBlockNode((Block) falseStatement, ifStatement, true);
			}
			else
			{
				if (isElseIf)
				{
					// Wrap the incoming 'if' with an Else-If node that will allow us later to break it and indent
					// it.
					FormatterPHPElseIfNode elseIfNode = new FormatterPHPElseIfNode(document);
					elseIfNode.setBegin(builder.createTextNode(document, falseBlockStart, falseBlockStart));
					builder.push(elseIfNode);
					falseStatement.accept(this);
					int falseBlockEnd = falseStatement.getEnd();
					builder.checkedPop(elseIfNode, falseBlockEnd);
					int end = elseIfNode.getEndOffset();
					elseIfNode.setEnd(builder.createTextNode(document, end, end));
				}
				else
				{
					// Just visit the children
					falseStatement.accept(this);
				}
			}
			if (elseNode != null)
			{
				builder.checkedPop(elseNode, falseStatement.getEnd());
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.ArrayAccess
	 * )
	 */
	@Override
	public boolean visit(ArrayAccess arrayAccess)
	{
		// TODO Auto-generated method stub
		return super.visit(arrayAccess);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.ArrayCreation
	 * )
	 */
	@Override
	public boolean visit(ArrayCreation arrayCreation)
	{
		// TODO Auto-generated method stub
		return super.visit(arrayCreation);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.ArrayElement
	 * )
	 */
	@Override
	public boolean visit(ArrayElement arrayElement)
	{
		// TODO Auto-generated method stub
		return super.visit(arrayElement);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.Assignment
	 * )
	 */
	@Override
	public boolean visit(Assignment assignment)
	{
		// TODO Auto-generated method stub
		return super.visit(assignment);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.ASTError)
	 */
	@Override
	public boolean visit(ASTError astError)
	{
		builder.setHasErrors(true);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * BackTickExpression)
	 */
	@Override
	public boolean visit(BackTickExpression backTickExpression)
	{
		// TODO Auto-generated method stub
		return super.visit(backTickExpression);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.Block)
	 */
	@Override
	public boolean visit(Block block)
	{
		FormatterPHPBlockNode blockNode = new FormatterPHPBlockNode(document,
				block.getParent().getType() == ASTNode.PROGRAM);
		blockNode.setBegin(builder.createTextNode(document, block.getStart(), block.getStart() + 1));
		builder.push(blockNode);
		block.childrenAccept(this);
		int end = block.getEnd();
		builder.checkedPop(blockNode, end - 1);
		if (block.isCurly())
		{
			int endWithSemicolon = locateCharMatchInLine(end, SEMICOLON_AND_COLON, document, false);
			blockNode.setEnd(builder.createTextNode(document, end - 1, endWithSemicolon));
		}
		else
		{
			blockNode.setEnd(builder.createTextNode(document, end, end));
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * BreakStatement)
	 */
	@Override
	public boolean visit(BreakStatement breakStatement)
	{
		PHPFormatterBreakNode breakNode = new PHPFormatterBreakNode(document, breakStatement.getParent());
		breakNode.setBegin(builder.createTextNode(document, breakStatement.getStart(), breakStatement.getEnd()));
		builder.push(breakNode);
		builder.checkedPop(breakNode, -1);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * ClassDeclaration)
	 */
	@Override
	public boolean visit(ClassDeclaration classDeclaration)
	{
		visitTypeDeclaration(classDeclaration);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * ClassInstanceCreation)
	 */
	@Override
	public boolean visit(ClassInstanceCreation classInstanceCreation)
	{
		// TODO Auto-generated method stub
		return super.visit(classInstanceCreation);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.ClassName
	 * )
	 */
	@Override
	public boolean visit(ClassName className)
	{
		// TODO Auto-generated method stub
		return super.visit(className);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * CloneExpression)
	 */
	@Override
	public boolean visit(CloneExpression cloneExpression)
	{
		// TODO Auto-generated method stub
		return super.visit(cloneExpression);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.Comment)
	 */
	@Override
	public boolean visit(Comment comment)
	{
		// TODO Auto-generated method stub
		return super.visit(comment);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * ConditionalExpression)
	 */
	@Override
	public boolean visit(ConditionalExpression conditionalExpression)
	{
		// TODO Auto-generated method stub
		return super.visit(conditionalExpression);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * ConstantDeclaration)
	 */
	@Override
	public boolean visit(ConstantDeclaration classConstantDeclaration)
	{
		// TODO Auto-generated method stub
		return super.visit(classConstantDeclaration);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * ContinueStatement)
	 */
	@Override
	public boolean visit(ContinueStatement continueStatement)
	{
		// TODO Auto-generated method stub
		return super.visit(continueStatement);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * DeclareStatement)
	 */
	@Override
	public boolean visit(DeclareStatement declareStatement)
	{
		// TODO Auto-generated method stub
		return super.visit(declareStatement);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.EchoStatement
	 * )
	 */
	@Override
	public boolean visit(EchoStatement echoStatement)
	{
		FormatterPHPEchoNode echoNode = new FormatterPHPEchoNode(document);
		int start = echoStatement.getStart();
		echoNode.setBegin(builder.createTextNode(document, start, start + 4));
		builder.push(echoNode);
		builder.checkedPop(echoNode, -1);
		// push the expressions one at a time, with comma nodes between them.
		List<Expression> expressions = echoStatement.expressions();
		for (int i = 0; i < expressions.size(); i++)
		{
			Expression expression = expressions.get(i);
			expression.accept(this);
			// add a comma if needed
			if (i + 1 < expressions.size())
			{
				FormatterPHPCommaNode commaNode = new FormatterPHPCommaNode(document);
				int startIndex = expression.getEnd();
				String text = document.get(startIndex, expressions.get(i + 1).getStart());
				startIndex += text.indexOf(',');
				commaNode.setBegin(builder.createTextNode(document, startIndex, startIndex + 1));
				builder.push(commaNode);
				builder.checkedPop(commaNode, -1);
			}
		}
		// locate the comma at the end of the expression. If exists, push it as a node.
		int end = expressions.get(expressions.size() - 1).getEnd();
		int semicolonOffsetInLine = builder.locateCharForward(document, ';', end);
		if (semicolonOffsetInLine != end)
		{
			FormatterPHPTextNode semicolonNode = new FormatterPHPTextNode(document, true, 0);
			semicolonNode.setBegin(builder.createTextNode(document, semicolonOffsetInLine, semicolonOffsetInLine + 1));
			builder.push(semicolonNode);
			builder.checkedPop(semicolonNode, -1);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * EmptyStatement)
	 */
	@Override
	public boolean visit(EmptyStatement emptyStatement)
	{
		// TODO Auto-generated method stub
		return super.visit(emptyStatement);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * ExpressionStatement)
	 */
	@Override
	public boolean visit(ExpressionStatement expressionStatement)
	{
		FormatterPHPExpressionWrapperNode expressionNode = new FormatterPHPExpressionWrapperNode(document,
				expressionStatement);
		int start = expressionStatement.getStart();
		int end = expressionStatement.getEnd();
		expressionNode.setBegin(builder.createTextNode(document, start, start));
		builder.push(expressionNode);
		expressionStatement.childrenAccept(this);
		int startEnd = expressionNode.getEndOffset();
		expressionNode.setEnd(builder.createTextNode(document, startEnd, end));
		builder.checkedPop(expressionNode, -1);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.FieldAccess
	 * )
	 */
	@Override
	public boolean visit(FieldAccess fieldAccess)
	{
		// TODO Auto-generated method stub
		return super.visit(fieldAccess);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * FormalParameter)
	 */
	@Override
	public boolean visit(FormalParameter formalParameter)
	{
		// TODO Auto-generated method stub
		return super.visit(formalParameter);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.ForStatement
	 * )
	 */
	@Override
	public boolean visit(ForStatement forStatement)
	{
		Statement body = forStatement.getBody();
		int declarationEnd = builder.locateCharBackward(document, ')', body.getStart()) + 1;
		visitCommonLoopBlock(forStatement, declarationEnd, forStatement.getBody());
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * ForEachStatement)
	 */
	@Override
	public boolean visit(ForEachStatement forEachStatement)
	{
		int declarationEnd = forEachStatement.getStatement().getStart();
		declarationEnd = builder.locateCharBackward(document, ')', declarationEnd) + 1;
		visitCommonLoopBlock(forEachStatement, declarationEnd, forEachStatement.getStatement());
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * WhileStatement)
	 */
	@Override
	public boolean visit(WhileStatement whileStatement)
	{
		int declarationEnd = whileStatement.getCondition().getEnd();
		declarationEnd = builder.locateCharForward(document, ')', declarationEnd) + 1;
		visitCommonLoopBlock(whileStatement, declarationEnd, whileStatement.getBody());
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.DoStatement
	 * )
	 */
	@Override
	public boolean visit(DoStatement doStatement)
	{
		Statement body = doStatement.getBody();
		// First, push the 'do' declaration node and its body
		visitCommonLoopBlock(doStatement, doStatement.getStart() + 2, body);
		// now deal with the 'while' condition part. we need to include the word 'while' that appears
		// somewhere between the block-end and the condition start.
		// We wrap this node as a begin-end node that will hold the condition internals as children
		FormatterPHPNonBlockedWhileNode whileNode = new FormatterPHPNonBlockedWhileNode(document);
		// Search for the exact 'while' start offset
		int whileBeginOffset = builder.locateCharForward(document, 'w', body.getEnd());
		int conditionEnd = locateCharMatchInLine(doStatement.getEnd(), SEMICOLON, document, true);
		whileNode.setBegin(builder.createTextNode(document, whileBeginOffset, conditionEnd));
		builder.push(whileNode);
		builder.checkedPop(whileNode, -1);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * FunctionDeclaration)
	 */
	@Override
	public boolean visit(FunctionDeclaration functionDeclaration)
	{
		visitFunctionDeclaration(functionDeclaration, functionDeclaration.getBody());
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * FunctionInvocation)
	 */
	@Override
	public boolean visit(FunctionInvocation functionInvocation)
	{
		visitFunctionInvocation(functionInvocation);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.FunctionName
	 * )
	 */
	@Override
	public boolean visit(FunctionName functionName)
	{
		// TODO Auto-generated method stub
		return super.visit(functionName);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * GlobalStatement)
	 */
	@Override
	public boolean visit(GlobalStatement globalStatement)
	{
		// TODO Auto-generated method stub
		return super.visit(globalStatement);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.GotoLabel
	 * )
	 */
	@Override
	public boolean visit(GotoLabel gotoLabel)
	{
		// TODO Auto-generated method stub
		return super.visit(gotoLabel);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.GotoStatement
	 * )
	 */
	@Override
	public boolean visit(GotoStatement gotoStatement)
	{
		// TODO Auto-generated method stub
		return super.visit(gotoStatement);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.Identifier
	 * )
	 */
	@Override
	public boolean visit(Identifier identifier)
	{
		// TODO Auto-generated method stub
		return super.visit(identifier);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.IgnoreError
	 * )
	 */
	@Override
	public boolean visit(IgnoreError ignoreError)
	{
		// TODO Auto-generated method stub
		return super.visit(ignoreError);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.Include)
	 */
	@Override
	public boolean visit(Include include)
	{
		// TODO Auto-generated method stub
		return super.visit(include);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * InfixExpression)
	 */
	@Override
	public boolean visit(InfixExpression infixExpression)
	{
		// TODO Auto-generated method stub
		return super.visit(infixExpression);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.InLineHtml
	 * )
	 */
	@Override
	public boolean visit(InLineHtml inLineHtml)
	{
		// TODO Auto-generated method stub
		return super.visit(inLineHtml);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * InstanceOfExpression)
	 */
	@Override
	public boolean visit(InstanceOfExpression instanceOfExpression)
	{
		// TODO Auto-generated method stub
		return super.visit(instanceOfExpression);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * InterfaceDeclaration)
	 */
	@Override
	public boolean visit(InterfaceDeclaration interfaceDeclaration)
	{
		visitTypeDeclaration(interfaceDeclaration);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * LambdaFunctionDeclaration)
	 */
	@Override
	public boolean visit(LambdaFunctionDeclaration lambdaFunctionDeclaration)
	{
		visitFunctionDeclaration(lambdaFunctionDeclaration, lambdaFunctionDeclaration.getBody());
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.ListVariable
	 * )
	 */
	@Override
	public boolean visit(ListVariable listVariable)
	{
		// TODO Auto-generated method stub
		return super.visit(listVariable);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * MethodDeclaration)
	 */
	@Override
	public boolean visit(MethodDeclaration methodDeclaration)
	{
		FunctionDeclaration function = methodDeclaration.getFunction();
		visitModifiers(methodDeclaration, function);
		// return true to have a continuous visit of the child function.
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * FieldsDeclaration)
	 */
	@Override
	public boolean visit(FieldsDeclaration fieldsDeclaration)
	{
		// A class field declaration is treated in a similar way we treat a class method declaration
		Variable[] variableNames = fieldsDeclaration.getVariableNames();
		Variable firstVariable = variableNames[0];
		visitModifiers(fieldsDeclaration, firstVariable);
		// visit the variables and their values
		Expression[] initialValues = fieldsDeclaration.getInitialValues();
		for (int i = 0; i < variableNames.length; i++)
		{
			Variable v = variableNames[i];
			Expression e = initialValues[i];
			visitTextNode(v, true, 0);
			if (e != null)
			{
				// We have a value assigned to this variable
				FormatterPHPAssignmentNode assignmentNode = new FormatterPHPAssignmentNode(document);
				String text = document.get(v.getEnd(), e.getStart());
				int startIndex = v.getEnd() + text.indexOf('=');
				assignmentNode.setBegin(builder.createTextNode(document, startIndex, startIndex + 1));
				builder.push(assignmentNode);
				builder.checkedPop(assignmentNode, -1);
				visitTextNode(e, true, 0);
			}
			if (i + 1 < variableNames.length)
			{
				// we need to add a comma node
				FormatterPHPCommaNode commaNode = new FormatterPHPCommaNode(document);
				int startIndex = (e != null) ? e.getEnd() : v.getEnd();
				String text = document.get(startIndex, variableNames[i + 1].getStart());
				startIndex += text.indexOf(',');
				commaNode.setBegin(builder.createTextNode(document, startIndex, startIndex + 1));
				builder.push(commaNode);
				builder.checkedPop(commaNode, -1);
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * MethodInvocation)
	 */
	@Override
	public boolean visit(MethodInvocation methodInvocation)
	{
		visitInvocation(methodInvocation.getDispatcher(), methodInvocation.getMethod(), INVOCATION_ARROW);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * StaticMethodInvocation)
	 */
	@Override
	public boolean visit(StaticMethodInvocation staticMethodInvocation)
	{
		visitInvocation(staticMethodInvocation.getClassName(), staticMethodInvocation.getMethod(), STATIC_INVOCATION);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * NamespaceDeclaration)
	 */
	@Override
	public boolean visit(NamespaceDeclaration namespaceDeclaration)
	{
		// TODO Auto-generated method stub
		return super.visit(namespaceDeclaration);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.NamespaceName
	 * )
	 */
	@Override
	public boolean visit(NamespaceName namespaceName)
	{
		// TODO Auto-generated method stub
		return super.visit(namespaceName);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * ParenthesisExpression)
	 */
	@Override
	public boolean visit(ParenthesisExpression parenthesisExpression)
	{
		// TODO Auto-generated method stub
		return super.visit(parenthesisExpression);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * PostfixExpression)
	 */
	@Override
	public boolean visit(PostfixExpression postfixExpression)
	{
		// TODO Auto-generated method stub
		return super.visit(postfixExpression);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * PrefixExpression)
	 */
	@Override
	public boolean visit(PrefixExpression prefixExpression)
	{
		// TODO Auto-generated method stub
		return super.visit(prefixExpression);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.Program)
	 */
	@Override
	public boolean visit(Program program)
	{
		// TODO Auto-generated method stub
		return super.visit(program);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.Quote)
	 */
	@Override
	public boolean visit(Quote quote)
	{
		// TODO Auto-generated method stub
		return super.visit(quote);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.Reference
	 * )
	 */
	@Override
	public boolean visit(Reference reference)
	{
		// TODO Auto-generated method stub
		return super.visit(reference);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * ReflectionVariable)
	 */
	@Override
	public boolean visit(ReflectionVariable reflectionVariable)
	{
		// TODO Auto-generated method stub
		return super.visit(reflectionVariable);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * ReturnStatement)
	 */
	@Override
	public boolean visit(ReturnStatement returnStatement)
	{
		if (isIncludingSemicolon(returnStatement))
		{
			IFormatterContainerNode lineNode = pushLineNode(returnStatement);
			returnStatement.childrenAccept(this);
			builder.checkedPop(lineNode, lineNode.getEndOffset());
			return false;
		}
		return true;
	}

	/**
	 * Common push and return of a FormatterPHPDefaultLineNode (a line that terminates with a semicolon).
	 * 
	 * @param node
	 * @return
	 */
	private FormatterPHPDefaultLineNode pushLineNode(ASTNode node)
	{
		FormatterPHPDefaultLineNode lineNode = new FormatterPHPDefaultLineNode(document);
		int start = node.getStart();
		lineNode.setBegin(builder.createTextNode(document, start, start));
		builder.push(lineNode);
		return lineNode;
	}

	/**
	 * @param returnStatement
	 * @return
	 */
	private boolean isIncludingSemicolon(ReturnStatement returnStatement)
	{
		return document.charAt(returnStatement.getEnd() - 1) == ';';
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.Scalar)
	 */
	@Override
	public boolean visit(Scalar scalar)
	{
		visitTextNode(scalar, true, 0);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * SingleFieldDeclaration)
	 */
	@Override
	public boolean visit(SingleFieldDeclaration singleFieldDeclaration)
	{
		// TODO Auto-generated method stub
		return super.visit(singleFieldDeclaration);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * StaticConstantAccess)
	 */
	@Override
	public boolean visit(StaticConstantAccess classConstantAccess)
	{
		// TODO Auto-generated method stub
		return super.visit(classConstantAccess);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * StaticFieldAccess)
	 */
	@Override
	public boolean visit(StaticFieldAccess staticFieldAccess)
	{
		// TODO Auto-generated method stub
		return super.visit(staticFieldAccess);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * StaticStatement)
	 */
	@Override
	public boolean visit(StaticStatement staticStatement)
	{
		// TODO Auto-generated method stub
		return super.visit(staticStatement);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * SwitchStatement)
	 */
	@Override
	public boolean visit(SwitchStatement switchStatement)
	{
		Block body = switchStatement.getBody();
		// In case the body is not curly, we are dealing with an alternative syntax (e.g. colon and 'endswitch' instead
		// of curly open and close for the body).
		boolean isAlternativeSyntax = !body.isCurly();
		// Push the switch-case declaration node
		FormatterPHPDeclarationNode switchNode = new FormatterPHPDeclarationNode(document, true, switchStatement);
		int rightParenthesis = builder.locateCharBackward(document, ')', body.getStart());
		switchNode.setBegin(builder.createTextNode(document, switchStatement.getStart(), rightParenthesis + 1));
		builder.push(switchNode);
		builder.checkedPop(switchNode, -1);

		// push a switch-case body node
		int blockStart = body.getStart();
		FormatterPHPSwitchNode blockNode = new FormatterPHPSwitchNode(document);
		blockNode.setBegin(builder.createTextNode(document, blockStart, blockStart + 1));
		builder.push(blockNode);
		// visit the children under that block node
		body.childrenAccept(this);
		int endingOffset = switchStatement.getEnd();
		int endWithSemicolon = locateCharMatchInLine(endingOffset, SEMICOLON, document, isAlternativeSyntax);
		endingOffset--;
		if (isAlternativeSyntax)
		{
			// deduct the 'endswitch' length.
			// we already removed 1 offset above, so we remove the extra 8.
			endingOffset -= 8;
		}
		// pop the block node
		builder.checkedPop(blockNode, endingOffset);
		blockNode.setEnd(builder.createTextNode(document, endingOffset, endWithSemicolon));
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.SwitchCase
	 * )
	 */
	@Override
	public boolean visit(SwitchCase switchCase)
	{
		List<Statement> actions = switchCase.actions();
		boolean hasBlockedChild = (actions.size() == 1 && actions.get(0).getType() == ASTNode.BLOCK);
		// compute the colon position
		int colonOffset;
		if (actions.size() > 0)
		{
			colonOffset = builder.locateCharBackward(document, ':', actions.get(0).getStart());
		}
		else
		{
			colonOffset = builder.locateCharForward(document, ':', switchCase.getValue().getEnd());
		}
		// push the case/default node till the colon.
		// We create a begin-end node that will hold a case-colon node as an inner child to manage its spacing.
		FormatterPHPCaseNode caseNode = new FormatterPHPCaseNode(document);
		// get the value-end offset. In case it's a 'default' case, set the end at the end offset of the word 'default'
		int valueEnd = switchCase.isDefault() ? switchCase.getStart() + 7 : switchCase.getValue().getEnd();
		caseNode.setBegin(builder.createTextNode(document, switchCase.getStart(), valueEnd));
		caseNode.setEnd(builder.createTextNode(document, colonOffset + 1, colonOffset + 1));
		builder.push(caseNode);
		// push the colon node
		FormatterPHPCaseColonNode caseColonNode = new FormatterPHPCaseColonNode(document, hasBlockedChild);
		caseColonNode.setBegin(builder.createTextNode(document, colonOffset, colonOffset + 1));
		builder.push(caseColonNode);
		builder.checkedPop(caseColonNode, -1);
		builder.checkedPop(caseNode, -1);
		// push the case/default content
		FormatterPHPCaseBodyNode caseBodyNode = new FormatterPHPCaseBodyNode(document, hasBlockedChild);
		if (hasBlockedChild)
		{
			Block body = (Block) actions.get(0);
			// we have a 'case' with a curly-block
			caseBodyNode.setBegin(builder.createTextNode(document, body.getStart(), body.getStart() + 1));
			builder.push(caseBodyNode);
			body.childrenAccept(this);
			int endingOffset = body.getEnd() - 1;
			builder.checkedPop(caseBodyNode, endingOffset);
			int end = locateCharMatchInLine(endingOffset + 1, SEMICOLON_AND_COLON, document, false);
			caseBodyNode.setEnd(builder.createTextNode(document, endingOffset, end));
		}
		else
		{
			if (!actions.isEmpty())
			{
				int start = actions.get(0).getStart();
				int end = actions.get(actions.size() - 1).getEnd();
				caseBodyNode.setBegin(builder.createTextNode(document, start, start));
				builder.push(caseBodyNode);
				for (Statement st : actions)
				{
					st.accept(this);
				}
				builder.checkedPop(caseBodyNode, switchCase.getEnd());
				caseBodyNode.setEnd(builder.createTextNode(document, end, end));
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * CastExpression)
	 */
	@Override
	public boolean visit(CastExpression castExpression)
	{
		// TODO Auto-generated method stub
		return super.visit(castExpression);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.CatchClause
	 * )
	 */
	@Override
	public boolean visit(CatchClause catchClause)
	{
		// TODO Auto-generated method stub
		int declarationEnd = catchClause.getClassName().getEnd();
		declarationEnd = builder.locateCharForward(document, ')', declarationEnd) + 1;
		visitCommonDeclaration(catchClause, declarationEnd, true);
		visitBlockNode(catchClause.getBody(), catchClause, true);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * ThrowStatement)
	 */
	@Override
	public boolean visit(ThrowStatement throwStatement)
	{
		// TODO Auto-generated method stub
		return super.visit(throwStatement);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.TryStatement
	 * )
	 */
	@Override
	public boolean visit(TryStatement tryStatement)
	{
		visitCommonDeclaration(tryStatement, tryStatement.getStart() + 3, true);
		visitBlockNode(tryStatement.getBody(), tryStatement, true);
		List<CatchClause> catchClauses = tryStatement.catchClauses();
		for (CatchClause catchClause : catchClauses)
		{
			catchClause.accept(this);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * UnaryOperation)
	 */
	@Override
	public boolean visit(UnaryOperation unaryOperation)
	{
		// TODO Auto-generated method stub
		return super.visit(unaryOperation);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.UseStatement
	 * )
	 */
	@Override
	public boolean visit(UseStatement useStatement)
	{
		// TODO Auto-generated method stub
		return super.visit(useStatement);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.
	 * UseStatementPart)
	 */
	@Override
	public boolean visit(UseStatementPart useStatementPart)
	{
		// TODO Auto-generated method stub
		return super.visit(useStatementPart);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse.php.internal.core.ast.nodes.Variable)
	 */
	@Override
	public boolean visit(Variable variable)
	{
		visitTextNode(variable, false, 0);
		return false;
	}

	// ###################### Helper Methods ###################### //

	/**
	 * Visit and push a class/interface declaration
	 * 
	 * @param typeDeclaration
	 */
	private void visitTypeDeclaration(TypeDeclaration typeDeclaration)
	{
		// Locate the end offset of the declaration (before the block starts)
		Block body = typeDeclaration.getBody();
		int declarationBeginEnd = body.getStart() - 1;
		List<Identifier> interfaces = typeDeclaration.interfaces();
		if (interfaces != null && !interfaces.isEmpty())
		{
			declarationBeginEnd = interfaces.get(interfaces.size() - 1).getEnd();
		}
		else if (typeDeclaration.getType() == ASTNode.CLASS_DECLARATION
				&& ((ClassDeclaration) typeDeclaration).getSuperClass() != null)
		{
			declarationBeginEnd = ((ClassDeclaration) typeDeclaration).getSuperClass().getEnd();
		}
		else
		{
			declarationBeginEnd = typeDeclaration.getName().getEnd();
		}
		FormatterPHPDeclarationNode typeNode = new FormatterPHPDeclarationNode(document, true, typeDeclaration);
		typeNode.setBegin(builder.createTextNode(document, typeDeclaration.getStart(), declarationBeginEnd));
		builder.push(typeNode);
		builder.checkedPop(typeNode, -1);

		// add the class body
		FormatterPHPTypeBodyNode typeBodyNode = new FormatterPHPTypeBodyNode(document);
		typeBodyNode.setBegin(builder.createTextNode(document, body.getStart(), body.getStart() + 1));
		builder.push(typeBodyNode);
		body.childrenAccept(this);
		int end = body.getEnd();
		builder.checkedPop(typeBodyNode, end - 1);
		int endWithSemicolon = locateCharMatchInLine(end, SEMICOLON_AND_COLON, document, false);
		typeBodyNode.setEnd(builder.createTextNode(document, end - 1, endWithSemicolon));
	}

	/**
	 * Visit and push an invocation node. This node can be a static invocation or a method invocation. We expect to get
	 * a left ASTNode that is the 'caller' and a right FunctionInvocation which is being invoked.
	 * 
	 * @param leftASTNode
	 * @param method
	 * @param invocationString
	 */
	private void visitInvocation(ASTNode leftASTNode, FunctionInvocation method, String invocationString)
	{
		FormatterPHPTextNode leftFormatterNode = new FormatterPHPTextNode(document);
		leftFormatterNode.setBegin(builder.createTextNode(document, leftASTNode.getStart(), leftASTNode.getEnd()));
		builder.push(leftFormatterNode);
		builder.checkedPop(leftFormatterNode, -1);

		// push the invocation text
		String txt = document.get(leftASTNode.getEnd(), method.getStart());
		int invocationOffset = txt.indexOf(invocationString) + leftASTNode.getEnd();
		FormatterPHPInvocationTextNode invocationNode = new FormatterPHPInvocationTextNode(document, invocationString);
		invocationNode.setBegin(builder.createTextNode(document, invocationOffset, invocationOffset
				+ invocationString.length()));
		builder.push(invocationNode);
		builder.checkedPop(invocationNode, -1);

		// Push the method (the FunctionInvocation)
		visitFunctionInvocation(method);
	}

	/**
	 * A visit for a function invocation. This visit can be performed in numerous occasions, so we have it as a separate
	 * method that will be called in those occasions.
	 * 
	 * @param functionInvocation
	 */
	private void visitFunctionInvocation(FunctionInvocation functionInvocation)
	{
		int fiEnd = functionInvocation.getEnd();
		int end = locateCharMatchInLine(fiEnd, SEMICOLON, document, false);
		boolean hasSemicolon = fiEnd != end;
		FormatterPHPFunctionInvocationNode node = new FormatterPHPFunctionInvocationNode(document, functionInvocation,
				hasSemicolon);
		node.setBegin(builder.createTextNode(document, functionInvocation.getStart(), end));
		builder.push(node);
		builder.checkedPop(node, -1);
	}

	/**
	 * Visits and push a modifiers section. This section can appear before a method or a variable in a class, before
	 * class definitions etc.
	 * 
	 * @param node
	 *            The node that holds the modifier
	 * @param nextNode
	 *            The next node that appears right after the modifiers.
	 */
	private void visitModifiers(ASTNode node, ASTNode nextNode)
	{
		// The gap between the start and the function holds the modifiers (if exist).
		// We create a node for each of these modifiers to remove any extra spaces they have between them.
		int startOffset = node.getStart();
		String modifiers = document.get(startOffset, nextNode.getStart());
		Matcher matcher = WORD_PATTERN.matcher(modifiers);
		boolean isFirst = true;
		while (matcher.find())
		{
			FormatterPHPModifierNode modifierNode = new FormatterPHPModifierNode(document, isFirst);
			modifierNode.setBegin(builder.createTextNode(document, matcher.start() + startOffset, matcher.end()
					+ startOffset));
			builder.push(modifierNode);
			builder.checkedPop(modifierNode, -1);
			isFirst = false;
		}
		if (isFirst)
		{
			// if we got to this point with the 'isFirst' as 'true', we know that the modifiers are empty.
			// in this case, we need to push an empty modifiers node.
			FormatterPHPModifierNode emptyModifier = new FormatterPHPModifierNode(document, isFirst);
			emptyModifier.setBegin(builder.createTextNode(document, startOffset, startOffset));
			builder.push(emptyModifier);
			builder.checkedPop(emptyModifier, -1);
		}
	}

	/**
	 * @param node
	 * @param declarationEndOffset
	 * @param body
	 */
	private void visitCommonLoopBlock(ASTNode node, int declarationEndOffset, Statement body)
	{
		boolean hasBlockedBody = (body != null && body.getType() == ASTNode.BLOCK);
		boolean emptyBody = (body != null && body.getType() == ASTNode.EMPTY_STATEMENT);
		visitCommonDeclaration(node, declarationEndOffset, hasBlockedBody);
		if (hasBlockedBody)
		{
			visitBlockNode((Block) body, node, true);
		}
		else if (body != null)
		{
			if (!emptyBody)
			{
				// wrap the body with a loop node
				FormatterPHPLoopNode loopNode = new FormatterPHPLoopNode(document, false);
				int start = body.getStart();
				int end = body.getEnd();
				loopNode.setBegin(builder.createTextNode(document, start, start));
				builder.push(loopNode);
				body.accept(this);
				builder.checkedPop(loopNode, end);
				loopNode.setEnd(builder.createTextNode(document, end, end));
			}
			else
			{
				// create and push a special node that represents this empty statement.
				// When visiting a loop, this statement will probably only holds a semicolon char, so we make sure
				// we attach the char to the end of the previous node.
				if (body.getLength() > 0)
				{
					FormatterPHPTextNode emptyStatement = new FormatterPHPTextNode(document, true, 0);
					emptyStatement.setBegin(builder.createTextNode(document, body.getStart(), body.getEnd()));
					builder.push(emptyStatement);
					builder.checkedPop(emptyStatement, -1);
				}
			}
		}
	}

	/**
	 * A simple visit and push of a node that pushes a PHP text node which consumes any white-spaces before that node by
	 * request.
	 * 
	 * @param node
	 * @param consumePreviousWhitespaces
	 * @param spacesCountBefore
	 */
	private void visitTextNode(ASTNode node, boolean consumePreviousWhitespaces, int spacesCountBefore)
	{
		FormatterPHPTextNode textNode = new FormatterPHPTextNode(document, consumePreviousWhitespaces,
				spacesCountBefore);
		textNode.setBegin(builder.createTextNode(document, node.getStart(), node.getEnd()));
		builder.push(textNode);
		builder.checkedPop(textNode, node.getEnd());
	}

	/**
	 * Visit and push a FormatterPHPBlockNode. <br>
	 * The given body can represent a curly-braces body, or even an alternative syntax body. This method will check the
	 * block to see if it's curly, and if not, it will try to match the alternative syntax closer according to the given
	 * parent node type.
	 * 
	 * @param block
	 *            The block
	 * @param parent
	 *            The block's parent
	 * @see http://www.php.net/manual/en/control-structures.alternative-syntax.php
	 */
	private void visitBlockNode(Block block, ASTNode parent, boolean consumeEndingSemicolon)
	{
		boolean isAlternativeSyntaxBlock = !block.isCurly();
		FormatterPHPBlockNode blockNode = new FormatterPHPBlockNode(document, false);
		blockNode.setBegin(builder.createTextNode(document, block.getStart(), block.getStart() + 1));
		builder.push(blockNode);
		// visit the children
		block.childrenAccept(this);
		int end = block.getEnd();
		int closingStartOffset = end;
		if (isAlternativeSyntaxBlock)
		{
			// The AST does not provide the block-end right on the alternative syntax closer, so we need to check for
			// white spaces and update the end in this case.
			for (; closingStartOffset < document.getLength(); closingStartOffset++)
			{
				if (!Character.isWhitespace(document.charAt(closingStartOffset)))
				{
					break;
				}
			}
		}
		else
		{
			closingStartOffset--;
		}
		int endWithSemicolon;
		if (!isAlternativeSyntaxBlock && !consumeEndingSemicolon)
		{
			endWithSemicolon = block.getEnd();
		}
		else
		{
			endWithSemicolon = locateCharMatchInLine(closingStartOffset, SEMICOLON, document, isAlternativeSyntaxBlock);
			if (isAlternativeSyntaxBlock)
			{
				String alternativeSyntaxCloser = getAlternativeSyntaxCloser(parent);
				int alternativeCloserLength = alternativeSyntaxCloser.length();
				if (closingStartOffset - alternativeCloserLength >= 0
						&& document.get(closingStartOffset - alternativeCloserLength, closingStartOffset).toLowerCase()
								.equals(alternativeSyntaxCloser))
				{
					closingStartOffset -= alternativeCloserLength;
				}
			}
		}

		// pop the block node
		builder.checkedPop(blockNode, Math.min(closingStartOffset, end));
		blockNode.setEnd(builder.createTextNode(document, closingStartOffset, endWithSemicolon));
	}

	/**
	 * Visit and push a function declaration. The declaration can be a 'regular' function or can be a lambda function.
	 * 
	 * @param functionDeclaration
	 * @param body
	 */
	private void visitFunctionDeclaration(ASTNode functionDeclaration, Block body)
	{
		// First, push the function declaration node
		FormatterPHPDeclarationNode declarationNode = new FormatterPHPDeclarationNode(document, true,
				functionDeclaration);
		int parametersCloseBracket = builder.locateCharBackward(document, ')', body.getStart()) + 1;
		declarationNode.setBegin(builder.createTextNode(document, functionDeclaration.getStart(),
				parametersCloseBracket));
		builder.push(declarationNode);
		builder.checkedPop(declarationNode, -1);

		// Then, push the body
		FormatterPHPFunctionBodyNode bodyNode = new FormatterPHPFunctionBodyNode(document);
		bodyNode.setBegin(builder.createTextNode(document, body.getStart(), body.getStart() + 1));
		builder.push(bodyNode);
		body.childrenAccept(this);
		int bodyEnd = body.getEnd();
		builder.checkedPop(bodyNode, bodyEnd - 1);
		int end = locateCharMatchInLine(bodyEnd, SEMICOLON_AND_COLON, document, false);
		bodyNode.setEnd(builder.createTextNode(document, bodyEnd - 1, end));
	}

	/**
	 * Visit and push a common declaration part of an expression.s
	 * 
	 * @param node
	 * @param declarationEndOffset
	 * @param hasBlockedBody
	 */
	private void visitCommonDeclaration(ASTNode node, int declarationEndOffset, boolean hasBlockedBody)
	{
		FormatterPHPDeclarationNode declarationNode = new FormatterPHPDeclarationNode(document, hasBlockedBody, node);
		declarationNode.setBegin(builder.createTextNode(document, node.getStart(), declarationEndOffset));
		builder.push(declarationNode);
		builder.checkedPop(declarationNode, -1);
	}

	/**
	 * Returns the string value that represents the closing of an alternative syntax block. In case non exists, this
	 * method returns an empty string.
	 * 
	 * @param parent
	 * @return The alternative syntax block-closing string.
	 */
	private String getAlternativeSyntaxCloser(ASTNode parent)
	{
		switch (parent.getType())
		{
			case ASTNode.IF_STATEMENT:
				return "endif"; //$NON-NLS-1$
			case ASTNode.WHILE_STATEMENT:
				return "endwhile"; //$NON-NLS-1$
			case ASTNode.FOR_EACH_STATEMENT:
				return "endforeach"; //$NON-NLS-1$
			case ASTNode.FOR_STATEMENT:
				return "endfor"; //$NON-NLS-1$
			case ASTNode.SWITCH_STATEMENT:
				return "endswitch"; //$NON-NLS-1$
			default:
				return StringUtil.EMPTY;
		}
	}

	/**
	 * Scan for a list of char terminator located at the <b>same line</b>. Return the given offset if non is found.<br>
	 * <b>See important note in the @return tag.</b>
	 * 
	 * @param offset
	 * @param chars
	 *            An array of chars to match
	 * @param document
	 * @param ignoreNonWhitespace
	 *            In case this flag is false, any non-whitespace char that appear before we located a requested char
	 *            will stop the search. In case it's true, the search will continue till the end of the line.
	 * @return The first match offset; The given offset if a match not found. <b>Note that the returned offset is in a
	 *         +1 position from the real character offset. This is to ease the caller process of adapting it to the
	 *         formatter-document's offsets.</b>
	 */
	private int locateCharMatchInLine(int offset, char[] chars, FormatterDocument document, boolean ignoreNonWhitespace)
	{
		int i = offset;
		int size = document.getLength();
		for (; i < size; i++)
		{
			char c = document.charAt(i);
			for (char toMatch : chars)
			{
				if (c == toMatch)
				{
					return i + 1;
				}
			}
			if (c == '\n' || c == '\r')
			{
				break;
			}
			if (!ignoreNonWhitespace && (c != ' ' || c != '\t'))
			{
				break;
			}
		}
		return offset;
	}
}
