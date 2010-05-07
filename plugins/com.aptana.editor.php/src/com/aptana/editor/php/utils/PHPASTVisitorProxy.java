/**
 * This file Copyright (c) 2005-2008 Aptana, Inc. This program is
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
package com.aptana.editor.php.utils;

import org.eclipse.php.internal.core.ast.nodes.ASTError;
import org.eclipse.php.internal.core.ast.nodes.ArrayAccess;
import org.eclipse.php.internal.core.ast.nodes.ArrayCreation;
import org.eclipse.php.internal.core.ast.nodes.ArrayElement;
import org.eclipse.php.internal.core.ast.nodes.Assignment;
import org.eclipse.php.internal.core.ast.nodes.BackTickExpression;
import org.eclipse.php.internal.core.ast.nodes.Block;
import org.eclipse.php.internal.core.ast.nodes.BreakStatement;
import org.eclipse.php.internal.core.ast.nodes.CastExpression;
import org.eclipse.php.internal.core.ast.nodes.CatchClause;
import org.eclipse.php.internal.core.ast.nodes.ClassConstantDeclaration;
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
import org.eclipse.php.internal.core.ast.nodes.StaticConstantAccess;
import org.eclipse.php.internal.core.ast.nodes.StaticFieldAccess;
import org.eclipse.php.internal.core.ast.nodes.StaticMethodInvocation;
import org.eclipse.php.internal.core.ast.nodes.StaticStatement;
import org.eclipse.php.internal.core.ast.nodes.SwitchCase;
import org.eclipse.php.internal.core.ast.nodes.SwitchStatement;
import org.eclipse.php.internal.core.ast.nodes.ThrowStatement;
import org.eclipse.php.internal.core.ast.nodes.TryStatement;
import org.eclipse.php.internal.core.ast.nodes.UnaryOperation;
import org.eclipse.php.internal.core.ast.nodes.UseStatement;
import org.eclipse.php.internal.core.ast.nodes.UseStatementPart;
import org.eclipse.php.internal.core.ast.nodes.Variable;
import org.eclipse.php.internal.core.ast.nodes.WhileStatement;
import org.eclipse.php.internal.core.ast.visitor.Visitor;

/**
 * PHP AST visitor proxy compensates the inabilities of the original visitors system to cancel children visiting and to
 * notify visitor about the end of node visit.
 * <p>
 * Sample usage:
 * 
 * <pre>
 * Program program = getParseResults();
 * IPHPASTVisitor visitor = createVisitor();
 * 
 * PHPASTVisitorProxy proxy = new PHPASTVisitorProxy(visitor);
 * proxy.visit(program);
 * 
 * </pre>
 * 
 * </p>
 * 
 * @author Denis Denisenko
 */
public class PHPASTVisitorProxy implements Visitor
{
	/**
	 * Ast visitor.
	 */
	private IPHPASTVisitor _astVisitor = null;

	/**
	 * PHPASTVisitorProxy constructor.
	 * 
	 * @param visitor
	 *            - visitor.
	 */
	public PHPASTVisitorProxy(IPHPASTVisitor visitor)
	{
		_astVisitor = visitor;
	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(ArrayAccess arrayAccess)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(arrayAccess);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(arrayAccess);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			arrayAccess.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(arrayAccess);
		_astVisitor.endVisit(arrayAccess);
		_astVisitor.afterEndVisitNode(arrayAccess);
	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(ArrayCreation arrayCreation)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(arrayCreation);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(arrayCreation);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			arrayCreation.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(arrayCreation);
		_astVisitor.endVisit(arrayCreation);
		_astVisitor.afterEndVisitNode(arrayCreation);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(ArrayElement arrayElement)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(arrayElement);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(arrayElement);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			arrayElement.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(arrayElement);
		_astVisitor.endVisit(arrayElement);
		_astVisitor.afterEndVisitNode(arrayElement);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(Assignment assignment)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(assignment);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(assignment);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			assignment.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(assignment);
		_astVisitor.endVisit(assignment);
		_astVisitor.afterEndVisitNode(assignment);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(ASTError astError)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(astError);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(astError);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			astError.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(astError);
		_astVisitor.endVisit(astError);
		_astVisitor.afterEndVisitNode(astError);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(BackTickExpression backTickExpression)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(backTickExpression);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(backTickExpression);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			backTickExpression.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(backTickExpression);
		_astVisitor.endVisit(backTickExpression);
		_astVisitor.afterEndVisitNode(backTickExpression);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(Block block)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(block);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(block);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			block.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(block);
		_astVisitor.endVisit(block);
		_astVisitor.afterEndVisitNode(block);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(BreakStatement breakStatement)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(breakStatement);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(breakStatement);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			breakStatement.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(breakStatement);
		_astVisitor.endVisit(breakStatement);
		_astVisitor.afterEndVisitNode(breakStatement);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(CastExpression castExpression)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(castExpression);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(castExpression);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			castExpression.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(castExpression);
		_astVisitor.endVisit(castExpression);
		_astVisitor.afterEndVisitNode(castExpression);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(CatchClause catchClause)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(catchClause);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(catchClause);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			catchClause.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(catchClause);
		_astVisitor.endVisit(catchClause);
		_astVisitor.afterEndVisitNode(catchClause);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(ClassConstantDeclaration classConstantDeclaration)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(classConstantDeclaration);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(classConstantDeclaration);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			classConstantDeclaration.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(classConstantDeclaration);
		_astVisitor.endVisit(classConstantDeclaration);
		_astVisitor.afterEndVisitNode(classConstantDeclaration);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(ClassDeclaration classDeclaration)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(classDeclaration);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(classDeclaration);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			classDeclaration.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(classDeclaration);
		_astVisitor.endVisit(classDeclaration);
		_astVisitor.afterEndVisitNode(classDeclaration);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(ClassInstanceCreation classInstanceCreation)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(classInstanceCreation);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(classInstanceCreation);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			classInstanceCreation.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(classInstanceCreation);
		_astVisitor.endVisit(classInstanceCreation);
		_astVisitor.afterEndVisitNode(classInstanceCreation);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(ClassName className)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(className);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(className);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			className.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(className);
		_astVisitor.endVisit(className);
		_astVisitor.afterEndVisitNode(className);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(CloneExpression cloneExpression)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(cloneExpression);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(cloneExpression);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			cloneExpression.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(cloneExpression);
		_astVisitor.endVisit(cloneExpression);
		_astVisitor.afterEndVisitNode(cloneExpression);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(Comment comment)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(comment);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(comment);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			comment.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(comment);
		_astVisitor.endVisit(comment);
		_astVisitor.afterEndVisitNode(comment);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(ConditionalExpression conditionalExpression)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(conditionalExpression);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(conditionalExpression);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			conditionalExpression.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(conditionalExpression);
		_astVisitor.endVisit(conditionalExpression);
		_astVisitor.afterEndVisitNode(conditionalExpression);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(ContinueStatement continueStatement)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(continueStatement);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(continueStatement);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			continueStatement.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(continueStatement);
		_astVisitor.endVisit(continueStatement);
		_astVisitor.afterEndVisitNode(continueStatement);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(DeclareStatement declareStatement)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(declareStatement);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(declareStatement);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			declareStatement.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(declareStatement);
		_astVisitor.endVisit(declareStatement);
		_astVisitor.afterEndVisitNode(declareStatement);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(DoStatement doStatement)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(doStatement);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(doStatement);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			doStatement.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(doStatement);
		_astVisitor.endVisit(doStatement);
		_astVisitor.afterEndVisitNode(doStatement);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(EchoStatement echoStatement)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(echoStatement);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(echoStatement);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			echoStatement.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(echoStatement);
		_astVisitor.endVisit(echoStatement);
		_astVisitor.afterEndVisitNode(echoStatement);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(EmptyStatement emptyStatement)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(emptyStatement);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(emptyStatement);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			emptyStatement.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(emptyStatement);
		_astVisitor.endVisit(emptyStatement);
		_astVisitor.afterEndVisitNode(emptyStatement);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(ExpressionStatement expressionStatement)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(expressionStatement);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(expressionStatement);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			expressionStatement.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(expressionStatement);
		_astVisitor.endVisit(expressionStatement);
		_astVisitor.afterEndVisitNode(expressionStatement);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(FieldAccess fieldAccess)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(fieldAccess);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(fieldAccess);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			fieldAccess.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(fieldAccess);
		_astVisitor.endVisit(fieldAccess);
		_astVisitor.afterEndVisitNode(fieldAccess);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(FieldsDeclaration fieldsDeclaration)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(fieldsDeclaration);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(fieldsDeclaration);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			fieldsDeclaration.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(fieldsDeclaration);
		_astVisitor.endVisit(fieldsDeclaration);
		_astVisitor.afterEndVisitNode(fieldsDeclaration);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(ForEachStatement forEachStatement)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(forEachStatement);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(forEachStatement);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			forEachStatement.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(forEachStatement);
		_astVisitor.endVisit(forEachStatement);
		_astVisitor.afterEndVisitNode(forEachStatement);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(FormalParameter formalParameter)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(formalParameter);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(formalParameter);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			formalParameter.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(formalParameter);
		_astVisitor.endVisit(formalParameter);
		_astVisitor.afterEndVisitNode(formalParameter);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(ForStatement node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(FunctionDeclaration node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(FunctionInvocation node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(FunctionName node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(GlobalStatement node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(Identifier node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(IfStatement node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(IgnoreError node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(Include node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(InfixExpression node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(InLineHtml node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(InstanceOfExpression node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(InterfaceDeclaration node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(ListVariable node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(MethodDeclaration node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(MethodInvocation node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(ParenthesisExpression node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(PostfixExpression node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(PrefixExpression node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(Program node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(Quote node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(Reference node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(ReflectionVariable node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(ReturnStatement node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(Scalar node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(StaticConstantAccess node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(StaticFieldAccess node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(StaticMethodInvocation node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(StaticStatement node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(SwitchCase node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(SwitchStatement node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(ThrowStatement node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(TryStatement node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(UnaryOperation node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(Variable node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	/**
	 * {@inheritDoc}
	 */
	public void visit(WhileStatement node)
	{
		// calling general handler
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	public void endVisit(GotoLabel gotoLabel)
	{

	}

	public void endVisit(GotoStatement gotoStatement)
	{

	}

	public void endVisit(ConstantDeclaration constantDeclaration)
	{
	}

	public void endVisit(UseStatementPart useStatementPart)
	{
	}

	public void endVisit(UseStatement useStatement)
	{
	}

	public void endVisit(LambdaFunctionDeclaration lambdaFunctionDeclaration)
	{
	}

	public void endVisit(NamespaceName namespaceName)
	{

	}

	public void endVisit(NamespaceDeclaration namespaceDeclaration)
	{
	}

	public void visit(GotoLabel node)
	{
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);
	}

	public void visit(GotoStatement node)
	{
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);
	}

	public void visit(ConstantDeclaration node)
	{
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);
	}

	public void visit(UseStatementPart node)
	{
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);
	}

	public void visit(UseStatement node)
	{
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);
	}

	public void visit(LambdaFunctionDeclaration node)
	{
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);
	}

	public void visit(NamespaceName node)
	{
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);

	}

	public boolean visit(NamespaceDeclaration node)
	{
		boolean generalHandlerVisitResults = _astVisitor.startVisitNode(node);

		// calling specific handler
		boolean visitResult = _astVisitor.visit(node);

		// visiting children if both general and specific handlers
		// accept such a visit
		if (generalHandlerVisitResults && visitResult)
		{
			node.childrenAccept(this);
		}

		// calling end node handlers
		_astVisitor.beforeEndVisitNode(node);
		_astVisitor.endVisit(node);
		_astVisitor.afterEndVisitNode(node);
		return true;
	}
}
