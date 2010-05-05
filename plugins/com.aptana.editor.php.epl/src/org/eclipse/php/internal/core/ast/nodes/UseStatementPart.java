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
 * Represents an element of the 'use' declaration.
 * <pre>e.g.<pre>MyNamespace;
 *MyNamespace as MyAlias;
 *MyProject\Sub\Level as MyAlias;
 *\MyProject\Sub\Level as MyAlias;
 */
public class UseStatementPart extends ASTNode {

	private NamespaceName name;
	private Identifier alias;


	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List<StructuralPropertyDescriptor> PROPERTY_DESCRIPTORS;
	
	static {
		List<StructuralPropertyDescriptor> properyList = new ArrayList<StructuralPropertyDescriptor>(3);
		PROPERTY_DESCRIPTORS = Collections.unmodifiableList(properyList);
	}
	

	
	public UseStatementPart(int start, int end, NamespaceName name, Identifier alias) {
		super(start, end);
		if (name == null) {
			throw new IllegalArgumentException();
		}
		
		setName(name);
		if (alias != null) {
			setAlias(alias);
		}
	}

	public void childrenAccept(Visitor visitor) {
		name.accept(visitor);
		if (alias != null) {
			alias.accept(visitor);
		}
	}

	public void traverseTopDown(Visitor visitor) {
		accept(visitor);
		name.traverseTopDown(visitor);
		if (alias != null) {
			alias.traverseTopDown(visitor);
		}
	}

	public void traverseBottomUp(Visitor visitor) {
		name.traverseBottomUp(visitor);
		if (alias != null) {
			alias.traverseBottomUp(visitor);
		}
		accept(visitor);
	}

	public void toString(StringBuffer buffer, String tab) {
		buffer.append(tab).append("<UseStatementPart"); //$NON-NLS-1$
		appendInterval(buffer);
		buffer.append(">\n"); //$NON-NLS-1$

		buffer.append(TAB).append(tab).append("<Name>\n"); //$NON-NLS-1$
		name.toString(buffer, TAB + TAB + tab);
		buffer.append("\n"); //$NON-NLS-1$
		buffer.append(TAB).append(tab).append("</Name>\n"); //$NON-NLS-1$
		
		if (alias != null) {
			buffer.append(TAB).append(tab).append("<Alias>\n"); //$NON-NLS-1$
			alias.toString(buffer, TAB + TAB + tab);
			buffer.append("\n").append(TAB).append(tab).append("</Alias>\n"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		buffer.append(tab).append("</UseStatementPart>"); //$NON-NLS-1$
	}

	public void accept(Visitor visitor) {
		visitor.visit(this);
		
			childrenAccept(visitor);
		
		visitor.endVisit(this);
	}	
	
	public int getType() {
		return ASTNode.USE_STATEMENT_PART;
	}

	/**
	 * Returns the name of this array element(null if missing).
	 * 
	 * @return the name of the array element 
	 */ 
	public NamespaceName getName() {
		return name;
	}
		
	/**
	 * Sets the name of this array element.
	 * 
	 * @param expression the left operand node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setName(NamespaceName name) {
		if (name == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.name;
		this.name = name;
		
	}

	/**
	 * Returns the alias expression of this array element.
	 * 
	 * @return the alias expression of this array element
	 */ 
	public Identifier getAlias() {
		return this.alias;
	}
		
	/**
	 * Sets the name of this array expression.
	 * 
	 * @param expression the right operand node
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setAlias(Identifier alias) {
		ASTNode oldChild = this.alias;
		this.alias = alias;
	
	}
	
	
	

}
