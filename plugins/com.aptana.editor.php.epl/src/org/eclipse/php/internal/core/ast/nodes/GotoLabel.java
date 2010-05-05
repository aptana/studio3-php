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
 * Holds a label declaration that is used in goto expression. 
 * <pre>e.g.<pre> 
 *START:
 */
public class GotoLabel extends Statement {

	private Identifier name;

//	/**
//	 * The "expression" structural property of this node type.
//	 */
//	public static final ChildPropertyDescriptor NAME_PROPERTY = 
//		new ChildPropertyDescriptor(GotoLabel.class, "name", Identifier.class, MANDATORY, CYCLE_RISK); //$NON-NLS-1$

	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List<StructuralPropertyDescriptor> PROPERTY_DESCRIPTORS;
	
	static {
		List<StructuralPropertyDescriptor> propertyList = new ArrayList<StructuralPropertyDescriptor>(1);
		//propertyList.add(NAME_PROPERTY);
		PROPERTY_DESCRIPTORS = Collections.unmodifiableList(propertyList);
	}	

	public GotoLabel(int start, int end,  Identifier name) {
		super(start, end);

		if (name == null) {
			throw new IllegalArgumentException();
		}
		setName(name);
	}

	
	
	public void accept(Visitor visitor) {
		visitor.visit(this);
		if (true) {
			childrenAccept(visitor);
		}
		visitor.endVisit(this);
	}	
	
	public void childrenAccept(Visitor visitor) {
		name.accept(visitor);
	}

	public void traverseTopDown(Visitor visitor) {
		accept(visitor);
		name.traverseTopDown(visitor);
	}

	public void traverseBottomUp(Visitor visitor) {
		name.traverseBottomUp(visitor);
		accept(visitor);
	}

	public void toString(StringBuffer buffer, String tab) {
		buffer.append(tab).append("<GotoLabel"); //$NON-NLS-1$
		appendInterval(buffer);
		buffer.append(">\n"); //$NON-NLS-1$
		name.toString(buffer, TAB + tab);
		buffer.append("\n").append(tab).append("</GotoLabel>"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public int getType() {
		return ASTNode.GOTO_LABEL;
	}

	/**
	 * Returns the name of this goto label
	 * 
	 * @return the label name
	 */ 
	public Identifier getName() {
		return this.name;
	}
		
	/**
	 * Sets the name of this goto label
	 * 
	 * @param name of this goto label
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * <li>a cycle in would be created</li>
	 * </ul>
	 */ 
	public void setName(Identifier name) {
		if (name == null) {
			throw new IllegalArgumentException();
		}
		ASTNode oldChild = this.name;
		//preReplaceChild(oldChild, name, NAME_PROPERTY);
		this.name = name;
		//postReplaceChild(oldChild, name, NAME_PROPERTY);
	}
	
	

	
}
