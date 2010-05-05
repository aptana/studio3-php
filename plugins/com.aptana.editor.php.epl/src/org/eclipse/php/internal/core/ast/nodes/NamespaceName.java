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
 * Represents namespace name:
 * <pre>e.g.<pre>MyNamespace;
 *MyProject\Sub\Level;
 *namespace\MyProject\Sub\Level;
 */
public class NamespaceName extends Expression {
	
	protected ASTNode.NodeList<Identifier> segments = new ASTNode.NodeList<Identifier>();
	
	/** Whether the namespace name has '\' prefix, which means it relates to the global scope */
	private boolean global;
	
	/** Whether the namespace name has 'namespace' prefix, which means it relates to the current namespace scope */
	private boolean current;
	
	/**
	
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
	
	public NamespaceName() {
		super(0,0);
	}

	public NamespaceName(int start, int end,Identifier[] segments, boolean global, boolean current) {
		super(start, end);

		if (segments == null) {
			throw new IllegalArgumentException();
		}
		for (Identifier name : segments) {
			this.segments.add(name);
		}
		this.global = global;
		this.current = current;
	}
	
	public NamespaceName(int start, int end,  List segments, boolean global, boolean current) {
		super(start, end);

		if (segments == null) {
			throw new IllegalArgumentException();
		}
		Iterator<Identifier> it = segments.iterator();
		while (it.hasNext()) {
			this.segments.add(it.next());
		}
		
		this.global = global;
		this.current = current;
	}

	public void childrenAccept(Visitor visitor) {
		for (ASTNode node : this.segments) {
			node.accept(visitor);
		}
	}

	public void traverseTopDown(Visitor visitor) {
		accept(visitor);
		for (ASTNode node : this.segments) {
			node.traverseTopDown(visitor);
		}
	}

	public void traverseBottomUp(Visitor visitor) {
		for (ASTNode node : this.segments) {
			node.traverseBottomUp(visitor);
		}
		accept(visitor);
	}

	public void toString(StringBuffer buffer, String tab) {
		buffer.append(tab).append("<NamespaceName"); //$NON-NLS-1$
		appendInterval(buffer);
		buffer.append(" global='").append(global).append('\'');
		buffer.append(" current='").append(current).append('\'');
		buffer.append(">\n"); //$NON-NLS-1$
		for (ASTNode node : this.segments) {
			node.toString(buffer, TAB + tab);
			buffer.append("\n"); //$NON-NLS-1$
		}				
		buffer.append(tab).append("</NamespaceName>"); //$NON-NLS-1$
	}

	public void accept(Visitor visitor) {
		visitor.visit(this);
		childrenAccept(visitor);		
		visitor.endVisit(this);
	}	
	
	public int getType() {
		return ASTNode.NAMESPACE_NAME;
	}
	
	/**
	 * Returns whether this namespace name has global context (starts with '\')
	 * @return
	 */
	public boolean isGlobal() {
		return global;
	}
	
	public void setGlobal(boolean global) {
		this.global = global;
	}
	
	/**
	 * Returns whether this namespace name has current namespace context (starts with 'namespace')
	 * @return
	 */
	public boolean isCurrent() {
		return current;
	}
	
	public void setCurrent(boolean current) {
		this.current = current;
	}

	/**
	 * Retrieves names parts of the namespace
	 * @return segments. If names list is empty, that means that this namespace is global.
	 */
	public List<Identifier> segments() {
		return this.segments;
	}

	public String getFullName() {
		StringBuilder bld=new StringBuilder();
		int a=0;
		int size = segments.size();
		for (Identifier s:segments){
			a++;
			bld.append(s.getName());
			if (a!=size){
				bld.append('\\');
			}
		}
		return bld.toString();
	}
	

}
