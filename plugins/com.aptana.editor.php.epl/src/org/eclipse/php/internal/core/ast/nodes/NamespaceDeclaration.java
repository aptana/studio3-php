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
import java.util.List;

import org.eclipse.php.internal.core.ast.visitor.Visitor;

/**
 * Represents namespace declaration:
 * <pre>e.g.<pre>namespace MyNamespace;
 *namespace MyProject\Sub\Level;
 */
public class NamespaceDeclaration extends Statement {
	
	private NamespaceName name;
	private Block body;
	private boolean bracketed = true;
	

	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List<StructuralPropertyDescriptor> PROPERTY_DESCRIPTORS;
	static {
		List<StructuralPropertyDescriptor> properyList = new ArrayList<StructuralPropertyDescriptor>(2);
		PROPERTY_DESCRIPTORS = Collections.unmodifiableList(properyList);
	}

	public NamespaceDeclaration(int start, int end,  NamespaceName name, Block body, boolean bracketed) {
		super(start, end);
		
		if (bracketed && name == null) {
			throw new IllegalArgumentException("Namespace name must not be null in a bracketed statement");
		}
		
		this.bracketed = bracketed;
		
		if (body == null) {
			body = new Block(start, end,  new ArrayList());
		}
		body.setParent(this);
		
		this.name = name;
		this.body = body;
	}
	
	/**
	 * Returns whether this namespace declaration has a bracketed syntax
	 * @return
	 */
	public boolean isBracketed() {
		return bracketed;
	}
	
	public void setBracketed(boolean bracketed) {
		this.bracketed = bracketed;
		
	}
	
//	public void addStatement(Statement statement) {
//		Block body = getBody();
//		body.statements().add(statement);
//		
//		int statementEnd = statement.getEnd();
//		int bodyStart = body.getStart();
//		body.setSourceRange(bodyStart, statementEnd - bodyStart);
//		
//		int namespaceStart = getStart();
//		setSourceRange(namespaceStart, statementEnd - namespaceStart);
//	}
	
	/**
	 * The body component of this namespace declaration node 
	 * @return body component of this namespace declaration node
	 */
	public Block getBody() {
		return body;
	}

	/**
	 * Sets the name of this parameter
	 * 
	 * @param name of this type declaration.
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setBody(Block block) {
		// an Assignment may occur inside a Expression - must check cycles
		ASTNode oldChild = this.body;
		this.body = block;
	}
	
	/**
	 * The name component of this namespace declaration node 
	 * @return name component of this namespace declaration node
	 */
	public NamespaceName getName() {
		return name;
	}

	/**
	 * Sets the name of this parameter
	 * 
	 * @param name of this type declaration.
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setName(NamespaceName name) {
		// an Assignment may occur inside a Expression - must check cycles
		ASTNode oldChild = this.name;
		this.name = name;
	}
	
	public void childrenAccept(Visitor visitor) {
		NamespaceName name = getName();
		if (name != null) {
			name.accept(visitor);
		}
		Block body = getBody();
		body.accept(visitor);
	}

	public void traverseTopDown(Visitor visitor) {
		accept(visitor);
		NamespaceName name = getName();
		if (name != null) {
			name.accept(visitor);
		}
		Block body = getBody();
		body.accept(visitor);
	}

	public void traverseBottomUp(Visitor visitor) {
		NamespaceName name = getName();
		if (name != null) {
			name.accept(visitor);
		}
		Block body = getBody();
		body.accept(visitor);
		accept(visitor);
	}

	public void toString(StringBuffer buffer, String tab) {
		buffer.append(tab).append("<NamespaceDeclaration"); //$NON-NLS-1$
		appendInterval(buffer);
		buffer.append(" isBracketed='").append(bracketed).append("'>\n"); //$NON-NLS-1$ //$NON-NLS-2$
		
		NamespaceName name = getName();
		if (name != null) {
			name.toString(buffer, TAB + tab);
			buffer.append("\n"); //$NON-NLS-1$
		}
		
		Block body = getBody();
		body.toString(buffer, TAB + tab);
		buffer.append("\n"); //$NON-NLS-1$

		buffer.append(tab).append("</NamespaceDeclaration>"); //$NON-NLS-1$
	}

	public void accept(Visitor visitor) {
		visitor.visit(this);		
	}	
	
	public int getType() {
		return ASTNode.NAMESPACE;
	}

	public void addStatement(Statement statement) {
		this.body.add(statement);
	}


}
