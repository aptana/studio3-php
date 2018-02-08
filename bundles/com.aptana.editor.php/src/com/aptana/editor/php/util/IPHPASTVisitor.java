/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.util;

import org2.eclipse.php.internal.core.ast.nodes.ASTError;
import org2.eclipse.php.internal.core.ast.nodes.ASTNode;
import org2.eclipse.php.internal.core.ast.nodes.ArrayAccess;
import org2.eclipse.php.internal.core.ast.nodes.ArrayCreation;
import org2.eclipse.php.internal.core.ast.nodes.ArrayElement;
import org2.eclipse.php.internal.core.ast.nodes.Assignment;
import org2.eclipse.php.internal.core.ast.nodes.BackTickExpression;
import org2.eclipse.php.internal.core.ast.nodes.Block;
import org2.eclipse.php.internal.core.ast.nodes.BreakStatement;
import org2.eclipse.php.internal.core.ast.nodes.CastExpression;
import org2.eclipse.php.internal.core.ast.nodes.CatchClause;
import org2.eclipse.php.internal.core.ast.nodes.ClassDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.ClassInstanceCreation;
import org2.eclipse.php.internal.core.ast.nodes.ClassName;
import org2.eclipse.php.internal.core.ast.nodes.CloneExpression;
import org2.eclipse.php.internal.core.ast.nodes.Comment;
import org2.eclipse.php.internal.core.ast.nodes.ConditionalExpression;
import org2.eclipse.php.internal.core.ast.nodes.ConstantDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.ContinueStatement;
import org2.eclipse.php.internal.core.ast.nodes.DeclareStatement;
import org2.eclipse.php.internal.core.ast.nodes.DoStatement;
import org2.eclipse.php.internal.core.ast.nodes.EchoStatement;
import org2.eclipse.php.internal.core.ast.nodes.EmptyStatement;
import org2.eclipse.php.internal.core.ast.nodes.ExpressionStatement;
import org2.eclipse.php.internal.core.ast.nodes.FieldAccess;
import org2.eclipse.php.internal.core.ast.nodes.FieldsDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.ForEachStatement;
import org2.eclipse.php.internal.core.ast.nodes.ForStatement;
import org2.eclipse.php.internal.core.ast.nodes.FormalParameter;
import org2.eclipse.php.internal.core.ast.nodes.FunctionDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.FunctionInvocation;
import org2.eclipse.php.internal.core.ast.nodes.FunctionName;
import org2.eclipse.php.internal.core.ast.nodes.GlobalStatement;
import org2.eclipse.php.internal.core.ast.nodes.GotoLabel;
import org2.eclipse.php.internal.core.ast.nodes.GotoStatement;
import org2.eclipse.php.internal.core.ast.nodes.Identifier;
import org2.eclipse.php.internal.core.ast.nodes.IfStatement;
import org2.eclipse.php.internal.core.ast.nodes.IgnoreError;
import org2.eclipse.php.internal.core.ast.nodes.InLineHtml;
import org2.eclipse.php.internal.core.ast.nodes.Include;
import org2.eclipse.php.internal.core.ast.nodes.InfixExpression;
import org2.eclipse.php.internal.core.ast.nodes.InstanceOfExpression;
import org2.eclipse.php.internal.core.ast.nodes.InterfaceDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.LambdaFunctionDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.ListVariable;
import org2.eclipse.php.internal.core.ast.nodes.MethodDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.MethodInvocation;
import org2.eclipse.php.internal.core.ast.nodes.NamespaceDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.NamespaceName;
import org2.eclipse.php.internal.core.ast.nodes.ParenthesisExpression;
import org2.eclipse.php.internal.core.ast.nodes.PostfixExpression;
import org2.eclipse.php.internal.core.ast.nodes.PrefixExpression;
import org2.eclipse.php.internal.core.ast.nodes.Program;
import org2.eclipse.php.internal.core.ast.nodes.Quote;
import org2.eclipse.php.internal.core.ast.nodes.Reference;
import org2.eclipse.php.internal.core.ast.nodes.ReflectionVariable;
import org2.eclipse.php.internal.core.ast.nodes.ReturnStatement;
import org2.eclipse.php.internal.core.ast.nodes.Scalar;
import org2.eclipse.php.internal.core.ast.nodes.StaticConstantAccess;
import org2.eclipse.php.internal.core.ast.nodes.StaticFieldAccess;
import org2.eclipse.php.internal.core.ast.nodes.StaticMethodInvocation;
import org2.eclipse.php.internal.core.ast.nodes.StaticStatement;
import org2.eclipse.php.internal.core.ast.nodes.SwitchCase;
import org2.eclipse.php.internal.core.ast.nodes.SwitchStatement;
import org2.eclipse.php.internal.core.ast.nodes.ThrowStatement;
import org2.eclipse.php.internal.core.ast.nodes.TryStatement;
import org2.eclipse.php.internal.core.ast.nodes.UnaryOperation;
import org2.eclipse.php.internal.core.ast.nodes.UseStatement;
import org2.eclipse.php.internal.core.ast.nodes.UseStatementPart;
import org2.eclipse.php.internal.core.ast.nodes.Variable;
import org2.eclipse.php.internal.core.ast.nodes.WhileStatement;

/**
 * Abstract PHP AST visitor.
 * 
 * @author Denis Denisenko
 */
public interface IPHPASTVisitor
{
	/**
	 * Is always called for every node type before any specific node visitor method calls.
	 * 
	 * @param node
	 *            - visiting node.
	 * @return true if to continue visiting this node and its children, false otherwise.
	 */
	public boolean startVisitNode(ASTNode node);

	/**
	 * Is always called for every node type after any specific node visitor method calls.
	 * 
	 * @param node
	 *            - visiting node.
	 */
	public void afterEndVisitNode(ASTNode node);

	/**
	 * Is always called for every node type before any specific node visitor method calls.
	 * 
	 * @param node
	 *            - visiting node.
	 */
	public void beforeEndVisitNode(ASTNode node);

	public boolean visit(ArrayAccess arrayAccess);

	public boolean visit(ArrayCreation arrayCreation);

	public boolean visit(ArrayElement arrayElement);

	public boolean visit(Assignment assignment);

	public boolean visit(ASTError astError);

	public boolean visit(BackTickExpression backTickExpression);

	public boolean visit(Block block);

	public boolean visit(BreakStatement breakStatement);

	public boolean visit(CastExpression castExpression);

	public boolean visit(CatchClause catchClause);

	public boolean visit(ClassDeclaration classDeclaration);

	public boolean visit(ClassInstanceCreation classInstanceCreation);

	public boolean visit(ClassName className);

	public boolean visit(CloneExpression cloneExpression);

	public boolean visit(Comment comment);

	public boolean visit(ConditionalExpression conditionalExpression);

	public boolean visit(ContinueStatement continueStatement);

	public boolean visit(DeclareStatement declareStatement);

	public boolean visit(DoStatement doStatement);

	public boolean visit(EchoStatement echoStatement);

	public boolean visit(EmptyStatement emptyStatement);

	public boolean visit(ExpressionStatement expressionStatement);

	public boolean visit(FieldAccess fieldAccess);

	public boolean visit(FieldsDeclaration fieldsDeclaration);

	public boolean visit(ForEachStatement forEachStatement);

	public boolean visit(FormalParameter formalParameter);

	public boolean visit(ForStatement forStatement);

	public boolean visit(FunctionDeclaration functionDeclaration);

	public boolean visit(FunctionInvocation functionInvocation);

	public boolean visit(FunctionName functionName);

	public boolean visit(GlobalStatement globalStatement);

	public boolean visit(Identifier identifier);

	public boolean visit(IfStatement ifStatement);

	public boolean visit(IgnoreError ignoreError);

	public boolean visit(Include include);

	public boolean visit(InfixExpression infixExpression);

	public boolean visit(InLineHtml inLineHtml);

	public boolean visit(InstanceOfExpression instanceOfExpression);

	public boolean visit(InterfaceDeclaration interfaceDeclaration);

	public boolean visit(ListVariable listVariable);

	public boolean visit(MethodDeclaration methodDeclaration);

	public boolean visit(MethodInvocation methodInvocation);

	public boolean visit(ParenthesisExpression parenthesisExpression);

	public boolean visit(PostfixExpression postfixExpression);

	public boolean visit(PrefixExpression prefixExpression);

	public boolean visit(Program program);

	public boolean visit(Quote quote);

	public boolean visit(Reference reference);

	public boolean visit(ReflectionVariable reflectionVariable);

	public boolean visit(ReturnStatement returnStatement);

	public boolean visit(Scalar scalar);

	public boolean visit(StaticConstantAccess classConstantAccess);

	public boolean visit(StaticFieldAccess staticFieldAccess);

	public boolean visit(StaticMethodInvocation staticMethodInvocation);

	public boolean visit(StaticStatement staticStatement);

	public boolean visit(SwitchCase switchCase);

	public boolean visit(SwitchStatement switchStatement);

	public boolean visit(ThrowStatement throwStatement);

	public boolean visit(TryStatement tryStatement);

	public boolean visit(UnaryOperation unaryOperation);

	public boolean visit(Variable variable);

	public boolean visit(WhileStatement whileStatement);

	// ///////

	public void endVisit(ArrayAccess arrayAccess);

	public void endVisit(ArrayCreation arrayCreation);

	public void endVisit(ArrayElement arrayElement);

	public void endVisit(Assignment assignment);

	public void endVisit(ASTError astError);

	public void endVisit(BackTickExpression backTickExpression);

	public void endVisit(Block block);

	public void endVisit(BreakStatement breakStatement);

	public void endVisit(CastExpression castExpression);

	public void endVisit(CatchClause catchClause);

	public void endVisit(ClassDeclaration classDeclaration);

	public void endVisit(ClassInstanceCreation classInstanceCreation);

	public void endVisit(ClassName className);

	public void endVisit(CloneExpression cloneExpression);

	public void endVisit(Comment comment);

	public void endVisit(ConditionalExpression conditionalExpression);

	public void endVisit(ContinueStatement continueStatement);

	public void endVisit(DeclareStatement declareStatement);

	public void endVisit(DoStatement doStatement);

	public void endVisit(EchoStatement echoStatement);

	public void endVisit(EmptyStatement emptyStatement);

	public void endVisit(ExpressionStatement expressionStatement);

	public void endVisit(FieldAccess fieldAccess);

	public void endVisit(FieldsDeclaration fieldsDeclaration);

	public void endVisit(ForEachStatement forEachStatement);

	public void endVisit(FormalParameter formalParameter);

	public void endVisit(ForStatement forStatement);

	public void endVisit(FunctionDeclaration functionDeclaration);

	public void endVisit(FunctionInvocation functionInvocation);

	public void endVisit(FunctionName functionName);

	public void endVisit(GlobalStatement globalStatement);

	public void endVisit(Identifier identifier);

	public void endVisit(IfStatement ifStatement);

	public void endVisit(IgnoreError ignoreError);

	public void endVisit(Include include);

	public void endVisit(InfixExpression infixExpression);

	public void endVisit(InLineHtml inLineHtml);

	public void endVisit(InstanceOfExpression instanceOfExpression);

	public void endVisit(InterfaceDeclaration interfaceDeclaration);

	public void endVisit(ListVariable listVariable);

	public void endVisit(MethodDeclaration methodDeclaration);

	public void endVisit(MethodInvocation methodInvocation);

	public void endVisit(ParenthesisExpression parenthesisExpression);

	public void endVisit(PostfixExpression postfixExpression);

	public void endVisit(PrefixExpression prefixExpression);

	public void endVisit(Program program);

	public void endVisit(Quote quote);

	public void endVisit(Reference reference);

	public void endVisit(ReflectionVariable reflectionVariable);

	public void endVisit(ReturnStatement returnStatement);

	public void endVisit(Scalar scalar);

	public void endVisit(StaticConstantAccess classConstantAccess);

	public void endVisit(StaticFieldAccess staticFieldAccess);

	public void endVisit(StaticMethodInvocation staticMethodInvocation);

	public void endVisit(StaticStatement staticStatement);

	public void endVisit(SwitchCase switchCase);

	public void endVisit(SwitchStatement switchStatement);

	public void endVisit(ThrowStatement throwStatement);

	public void endVisit(TryStatement tryStatement);

	public void endVisit(UnaryOperation unaryOperation);

	public void endVisit(Variable variable);

	public void endVisit(WhileStatement whileStatement);

	public boolean visit(NamespaceDeclaration node);

	public void endVisit(NamespaceDeclaration node);

	public boolean visit(NamespaceName node);

	public void endVisit(NamespaceName node);

	public boolean visit(LambdaFunctionDeclaration node);

	public void endVisit(LambdaFunctionDeclaration node);

	public boolean visit(GotoLabel node);

	public void endVisit(GotoLabel node);

	public boolean visit(GotoStatement node);

	public void endVisit(GotoStatement node);

	public boolean visit(ConstantDeclaration node);

	public void endVisit(ConstantDeclaration node);

	public boolean visit(UseStatementPart node);

	public void endVisit(UseStatementPart node);

	public boolean visit(UseStatement node);

	public void endVisit(UseStatement node);
}
