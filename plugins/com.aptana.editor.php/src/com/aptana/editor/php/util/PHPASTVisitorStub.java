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
 * Stub for PHP AST Node visitor.
 * 
 * @author Denis Denisenko
 */
public class PHPASTVisitorStub implements IPHPASTVisitor
{

	public void endVisit(ArrayAccess arrayAccess)
	{

	}

	public void endVisit(ArrayCreation arrayCreation)
	{

	}

	public void endVisit(ArrayElement arrayElement)
	{

	}

	public void endVisit(Assignment assignment)
	{

	}

	public void endVisit(ASTError astError)
	{

	}

	public void endVisit(BackTickExpression backTickExpression)
	{

	}

	public void endVisit(Block block)
	{

	}

	public void endVisit(BreakStatement breakStatement)
	{

	}

	public void endVisit(CastExpression castExpression)
	{

	}

	public void endVisit(CatchClause catchClause)
	{

	}

	public void endVisit(ClassDeclaration classDeclaration)
	{

	}

	public void endVisit(ClassInstanceCreation classInstanceCreation)
	{

	}

	public void endVisit(ClassName className)
	{

	}

	public void endVisit(CloneExpression cloneExpression)
	{

	}

	public void endVisit(Comment comment)
	{

	}

	public void endVisit(ConditionalExpression conditionalExpression)
	{

	}

	public void endVisit(ContinueStatement continueStatement)
	{

	}

	public void endVisit(DeclareStatement declareStatement)
	{

	}

	public void endVisit(DoStatement doStatement)
	{

	}

	public void endVisit(EchoStatement echoStatement)
	{

	}

	public void endVisit(EmptyStatement emptyStatement)
	{

	}

	public void endVisit(ExpressionStatement expressionStatement)
	{

	}

	public void endVisit(FieldAccess fieldAccess)
	{

	}

	public void endVisit(FieldsDeclaration fieldsDeclaration)
	{

	}

	public void endVisit(ForEachStatement forEachStatement)
	{

	}

	public void endVisit(FormalParameter formalParameter)
	{

	}

	public void endVisit(ForStatement forStatement)
	{

	}

	public void endVisit(FunctionDeclaration functionDeclaration)
	{

	}

	public void endVisit(FunctionInvocation functionInvocation)
	{

	}

	public void endVisit(FunctionName functionName)
	{

	}

	public void endVisit(GlobalStatement globalStatement)
	{

	}

	public void endVisit(Identifier identifier)
	{

	}

	public void endVisit(IfStatement ifStatement)
	{

	}

	public void endVisit(IgnoreError ignoreError)
	{

	}

	public void endVisit(Include include)
	{

	}

	public void endVisit(InfixExpression infixExpression)
	{

	}

	public void endVisit(InLineHtml inLineHtml)
	{

	}

	public void endVisit(InstanceOfExpression instanceOfExpression)
	{

	}

	public void endVisit(InterfaceDeclaration interfaceDeclaration)
	{

	}

	public void endVisit(ListVariable listVariable)
	{

	}

	public void endVisit(MethodDeclaration methodDeclaration)
	{

	}

	public void endVisit(MethodInvocation methodInvocation)
	{

	}

	public void endVisit(ParenthesisExpression parenthesisExpression)
	{

	}

	public void endVisit(PostfixExpression postfixExpression)
	{

	}

	public void endVisit(PrefixExpression prefixExpression)
	{

	}

	public void endVisit(Program program)
	{

	}

	public void endVisit(Quote quote)
	{

	}

	public void endVisit(Reference reference)
	{

	}

	public void endVisit(ReflectionVariable reflectionVariable)
	{

	}

	public void endVisit(ReturnStatement returnStatement)
	{

	}

	public void endVisit(Scalar scalar)
	{

	}

	public void endVisit(StaticConstantAccess classConstantAccess)
	{

	}

	public void endVisit(StaticFieldAccess staticFieldAccess)
	{

	}

	public void endVisit(StaticMethodInvocation staticMethodInvocation)
	{

	}

	public void endVisit(StaticStatement staticStatement)
	{

	}

	public void endVisit(SwitchCase switchCase)
	{

	}

	public void endVisit(SwitchStatement switchStatement)
	{

	}

	public void endVisit(ThrowStatement throwStatement)
	{

	}

	public void endVisit(TryStatement tryStatement)
	{

	}

	public void endVisit(UnaryOperation unaryOperation)
	{

	}

	public void endVisit(Variable variable)
	{

	}

	public void endVisit(WhileStatement whileStatement)
	{

	}

	public boolean visit(ArrayAccess arrayAccess)
	{

		return true;
	}

	public boolean visit(ArrayCreation arrayCreation)
	{

		return true;
	}

	public boolean visit(ArrayElement arrayElement)
	{

		return true;
	}

	public boolean visit(Assignment assignment)
	{

		return true;
	}

	public boolean visit(ASTError astError)
	{

		return true;
	}

	public boolean visit(BackTickExpression backTickExpression)
	{

		return true;
	}

	public boolean visit(Block block)
	{

		return true;
	}

	public boolean visit(BreakStatement breakStatement)
	{

		return true;
	}

	public boolean visit(CastExpression castExpression)
	{

		return true;
	}

	public boolean visit(CatchClause catchClause)
	{

		return true;
	}

	public boolean visit(ClassDeclaration classDeclaration)
	{

		return true;
	}

	public boolean visit(ClassInstanceCreation classInstanceCreation)
	{

		return true;
	}

	public boolean visit(ClassName className)
	{

		return true;
	}

	public boolean visit(CloneExpression cloneExpression)
	{

		return true;
	}

	public boolean visit(Comment comment)
	{

		return true;
	}

	public boolean visit(ConditionalExpression conditionalExpression)
	{

		return true;
	}

	public boolean visit(ContinueStatement continueStatement)
	{

		return true;
	}

	public boolean visit(DeclareStatement declareStatement)
	{

		return true;
	}

	public boolean visit(DoStatement doStatement)
	{

		return true;
	}

	public boolean visit(EchoStatement echoStatement)
	{

		return true;
	}

	public boolean visit(EmptyStatement emptyStatement)
	{

		return true;
	}

	public boolean visit(ExpressionStatement expressionStatement)
	{

		return true;
	}

	public boolean visit(FieldAccess fieldAccess)
	{

		return true;
	}

	public boolean visit(FieldsDeclaration fieldsDeclaration)
	{

		return true;
	}

	public boolean visit(ForEachStatement forEachStatement)
	{

		return true;
	}

	public boolean visit(FormalParameter formalParameter)
	{

		return true;
	}

	public boolean visit(ForStatement forStatement)
	{

		return true;
	}

	public boolean visit(FunctionDeclaration functionDeclaration)
	{

		return true;
	}

	public boolean visit(FunctionInvocation functionInvocation)
	{

		return true;
	}

	public boolean visit(FunctionName functionName)
	{

		return true;
	}

	public boolean visit(GlobalStatement globalStatement)
	{

		return true;
	}

	public boolean visit(Identifier identifier)
	{

		return true;
	}

	public boolean visit(IfStatement ifStatement)
	{

		return true;
	}

	public boolean visit(IgnoreError ignoreError)
	{

		return true;
	}

	public boolean visit(Include include)
	{

		return true;
	}

	public boolean visit(InfixExpression infixExpression)
	{

		return true;
	}

	public boolean visit(InLineHtml inLineHtml)
	{

		return true;
	}

	public boolean visit(InstanceOfExpression instanceOfExpression)
	{

		return true;
	}

	public boolean visit(InterfaceDeclaration interfaceDeclaration)
	{

		return true;
	}

	public boolean visit(ListVariable listVariable)
	{

		return true;
	}

	public boolean visit(MethodDeclaration methodDeclaration)
	{

		return true;
	}

	public boolean visit(MethodInvocation methodInvocation)
	{

		return true;
	}

	public boolean visit(ParenthesisExpression parenthesisExpression)
	{

		return true;
	}

	public boolean visit(PostfixExpression postfixExpression)
	{

		return true;
	}

	public boolean visit(PrefixExpression prefixExpression)
	{

		return true;
	}

	public boolean visit(Program program)
	{

		return true;
	}

	public boolean visit(Quote quote)
	{

		return true;
	}

	public boolean visit(Reference reference)
	{

		return true;
	}

	public boolean visit(ReflectionVariable reflectionVariable)
	{

		return true;
	}

	public boolean visit(ReturnStatement returnStatement)
	{

		return true;
	}

	public boolean visit(Scalar scalar)
	{

		return true;
	}

	public boolean visit(StaticConstantAccess classConstantAccess)
	{

		return true;
	}

	public boolean visit(StaticFieldAccess staticFieldAccess)
	{

		return true;
	}

	public boolean visit(StaticMethodInvocation staticMethodInvocation)
	{

		return true;
	}

	public boolean visit(StaticStatement staticStatement)
	{

		return true;
	}

	public boolean visit(SwitchCase switchCase)
	{

		return true;
	}

	public boolean visit(SwitchStatement switchStatement)
	{

		return true;
	}

	public boolean visit(ThrowStatement throwStatement)
	{

		return true;
	}

	public boolean visit(TryStatement tryStatement)
	{

		return true;
	}

	public boolean visit(UnaryOperation unaryOperation)
	{

		return true;
	}

	public boolean visit(Variable variable)
	{

		return true;
	}

	public boolean visit(WhileStatement whileStatement)
	{

		return true;
	}

	public void afterEndVisitNode(ASTNode node)
	{

	}

	public void beforeEndVisitNode(ASTNode node)
	{
	}

	public boolean startVisitNode(ASTNode node)
	{
		return true;
	}

	public void endVisit(NamespaceDeclaration node) {
		
	}

	public void endVisit(NamespaceName node)
	{
	}

	public void endVisit(LambdaFunctionDeclaration node)
	{

	}

	public void endVisit(GotoLabel node)
	{

	}

	public void endVisit(GotoStatement node)
	{

	}

	public void endVisit(ConstantDeclaration node)
	{

	}

	public void endVisit(UseStatementPart node)
	{

	}

	public void endVisit(UseStatement node)
	{

	}

	public boolean visit(NamespaceDeclaration node)
	{
		return true;
	}

	public boolean visit(NamespaceName node)
	{
		return true;
	}

	public boolean visit(LambdaFunctionDeclaration node)
	{
		return true;
	}

	public boolean visit(GotoLabel node)
	{
		return true;
	}

	public boolean visit(GotoStatement node)
	{
		return true;
	}

	public boolean visit(ConstantDeclaration node)
	{
		return true;
	}

	public boolean visit(UseStatementPart node)
	{
		return true;
	}

	public boolean visit(UseStatement node)
	{
		return true;
	}

}
