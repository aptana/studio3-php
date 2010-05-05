/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.php.internal.core.ast.nodes;

import org.eclipse.php.internal.core.ast.visitor.AbstractVisitor;

;


/**
 * Utilities used for Ast nodes
 * @author Eden
 *
 */
public class ASTNodes {


	public static ASTNode getParent(ASTNode node, Class parentClass) {
		if (node == null) 
			return null;
		
		do {
			node= node.getParent();
		} while (node != null && !parentClass.isInstance(node));
		return node;
	}
	
	public static ASTNode getParent(ASTNode node, int nodeType) {
		if (node == null) 
			return null;
		
		do {
			node= node.getParent();
		} while (node != null && node.getType() != nodeType);
		return node;
	}
	
	/** 
	 * @param node
	 * @return whether the given node is the only statement of a control statement
	 */
	public static boolean isControlStatement(ASTNode node) {
		assert node != null;		
		int type = node.getType();
		
		return  (type == ASTNode.IF_STATEMENT
		|| type == ASTNode.FOR_STATEMENT
		|| type == ASTNode.FOR_EACH_STATEMENT
		|| type == ASTNode.WHILE_STATEMENT
		|| type == ASTNode.DO_STATEMENT
		);
	}
	
	/**
	 * Aggregates the strings for a given node 
	 * @param node
	 * @return the aggregated strings for a given node 
	 */
	public static String getScalars(ASTNode node) {
		final StringBuilder builder = new StringBuilder();
		node.accept(new AbstractVisitor() {

			@Override
			public void visit(Scalar scalar) {
				builder.append(scalar.getStringValue());				
			}

			@Override
			public void endVisit(GotoLabel gotoLabel) {				
			}

			@Override
			public void endVisit(GotoStatement gotoStatement) {
				
			}

			@Override
			public void endVisit(ConstantDeclaration constantDeclaration) {
				
			}

			@Override
			public void endVisit(UseStatementPart useStatementPart) {
				
			}

			@Override
			public void endVisit(UseStatement useStatement) {
				
			}

			@Override
			public void endVisit(
					LambdaFunctionDeclaration lambdaFunctionDeclaration) {
				
			}

			@Override
			public void endVisit(NamespaceName namespaceName) {
				
			}

			@Override
			public void endVisit(NamespaceDeclaration namespaceDeclaration) {
				
			}

			@Override
			public void visit(GotoLabel gotoLabel) {
				
			}

			@Override
			public void visit(GotoStatement gotoStatement) {
				
			}

			@Override
			public void visit(ConstantDeclaration constantDeclaration) {
				
			}

			@Override
			public void visit(UseStatementPart useStatementPart) {
				
			}

			@Override
			public void visit(UseStatement useStatement) {
				
			}

			@Override
			public void visit(
					LambdaFunctionDeclaration lambdaFunctionDeclaration) {
				
			}

			@Override
			public void visit(NamespaceName namespaceName) {
				
			}

			@Override
			public boolean visit(NamespaceDeclaration namespaceDeclaration) {
				return false;
			}
				
		});
		
		return builder.toString();
	}
	
	
}
