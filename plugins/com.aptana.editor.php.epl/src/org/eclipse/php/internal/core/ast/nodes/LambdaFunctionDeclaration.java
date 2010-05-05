/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Zend Technologies
 *******************************************************************************/
package org.eclipse.php.internal.core.ast.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.php.internal.core.ast.visitor.Visitor;

/**
 * Represents a lambda function declaration
 * e.g.<pre>
 * function & (parameters) use (lexical vars) { body }
 * </pre>
 * @see http://wiki.php.net/rfc/closures
 */
public class LambdaFunctionDeclaration extends Expression {

	private boolean isReference;
	private final ASTNode.NodeList<FormalParameter> formalParameters = new ASTNode.NodeList<FormalParameter>();
	private final ASTNode.NodeList<Expression> lexicalVariables = new ASTNode.NodeList<Expression>();
	private Block body;

	
	
	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List<StructuralPropertyDescriptor> PROPERTY_DESCRIPTORS;
	static {
		List<StructuralPropertyDescriptor> propertyList = new ArrayList<StructuralPropertyDescriptor>(4);
		PROPERTY_DESCRIPTORS = Collections.unmodifiableList(propertyList);
	}	
	
	public LambdaFunctionDeclaration(int start, int end,  List formalParameters, List lexicalVars, Block body, final boolean isReference) {
		super(start, end);
		
		if (formalParameters == null) {
			throw new IllegalArgumentException();
		}
		
		setIsReference(isReference);
		
		Iterator<FormalParameter> paramIt = formalParameters.iterator();
		while (paramIt.hasNext()) {
			this.formalParameters.add(paramIt.next());
		}
		if (lexicalVars != null) {
			Iterator<Expression> varsIt = lexicalVars.iterator();
			while (varsIt.hasNext()) {
				this.lexicalVariables.add(varsIt.next());
			}
		}
		if (body != null) {
			setBody(body);	
		}
	}

	

	public void accept(Visitor visitor) {
		visitor.visit(this);
		//childrenAccept(visitor);
		//visitor.endVisit(this);
	}	

	public void childrenAccept(Visitor visitor) {
		for (ASTNode node : this.formalParameters) {
			node.accept(visitor);
		}
		for (ASTNode node : this.lexicalVariables) {
			node.accept(visitor);
		}
		body.setParent(this);
		if (body != null) {
			body.accept(visitor);
		}
	}

	public void traverseTopDown(Visitor visitor) {
		accept(visitor);
		for (ASTNode node : this.formalParameters) {
			node.traverseTopDown(visitor);
		}
		for (ASTNode node : this.lexicalVariables) {
			node.accept(visitor);
		}
		if (body != null) {
			body.traverseTopDown(visitor);
		}
	}

	public void traverseBottomUp(Visitor visitor) {
		for (ASTNode node : this.formalParameters) {
			node.traverseBottomUp(visitor);
		}
		for (ASTNode node : this.lexicalVariables) {
			node.accept(visitor);
		}
		if (body != null) {
			body.traverseBottomUp(visitor);
		}
		accept(visitor);
	}

	public void toString(StringBuffer buffer, String tab) {
		buffer.append(tab).append("<LambdaFunctionDeclaration"); //$NON-NLS-1$
		appendInterval(buffer);
		buffer.append(" isReference='").append(isReference).append("'>\n"); //$NON-NLS-1$ //$NON-NLS-2$

		buffer.append(TAB).append(tab).append("<FormalParameters>\n"); //$NON-NLS-1$
		for (ASTNode node : this.formalParameters) {
			node.toString(buffer, TAB + TAB + tab);
			buffer.append("\n"); //$NON-NLS-1$
		}
		buffer.append(TAB).append(tab).append("</FormalParameters>\n"); //$NON-NLS-1$
		
		buffer.append(TAB).append(tab).append("<LexicalVariables>\n"); //$NON-NLS-1$
		for (ASTNode node : this.lexicalVariables) {
			node.toString(buffer, TAB + TAB + tab);
			buffer.append("\n"); //$NON-NLS-1$
		}
		buffer.append(TAB).append(tab).append("</LexicalVariables>\n"); //$NON-NLS-1$
		
		buffer.append(TAB).append(tab).append("<FunctionBody>\n"); //$NON-NLS-1$
		if (body != null) {
			body.toString(buffer, TAB + TAB + tab);
			buffer.append("\n"); //$NON-NLS-1$
		}
		buffer.append(TAB).append(tab).append("</FunctionBody>\n"); //$NON-NLS-1$
		buffer.append(tab).append("</LambdaFunctionDeclaration>"); //$NON-NLS-1$
	}

	public int getType() {
		return ASTNode.LAMBDA_FUNCTION_DECLARATION;
	}

	/**
	 * Body of this function declaration
	 * 
	 * @return Body of this function declaration
	 */
	public Block getBody() {
		return body;
	}
	
	/**
	 * Sets the body part of this function declaration
	 * <p>
	 * 
	 * @param body of this function declaration 
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setBody(Block body) {
		if (body == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.body;
		this.body = body;
		
	}	

	/**
	 * List of the formal parameters of this function declaration
	 * 
	 * @return the parameters of this declaration   
	 */
	public List<FormalParameter> formalParameters() {
		return this.formalParameters;
	}
	
	/**
	 * List of the lexical variables of this lambda function declaration
	 * 
	 * @return the lexical variables of this declaration 
	 */
	public List<Expression> lexicalVariables() {
		return this.lexicalVariables;
	}
	
	/**
	 * True if this function's return variable will be referenced 
	 * @return True if this function's return variable will be referenced
	 */
	public boolean isReference() {
		return isReference;
	}
	
	/**
	 * Sets to true if this function's return variable will be referenced
	 * 
	 * @param value for referenced function return value 
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */
	public final void setIsReference(boolean value) {
		this.isReference = value;
		
	}
	
	
}
