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
 * Represents a class or namespace constant declaration
 * <pre>e.g.<pre> const MY_CONST = 5;
 * const MY_CONST = 5, YOUR_CONSTANT = 8;
 */
public class ConstantDeclaration extends Statement {



	private ArrayList names=new ArrayList();
	private ArrayList initializers=new ArrayList();

	private ConstantDeclaration(int start, int end, List<Identifier> names, List<Expression> initializers) {
		super(start, end);
		
		if (names == null || initializers == null || names.size() != initializers.size()) {
			throw new IllegalArgumentException();
		}

		Iterator<Identifier> iteratorNames = names.iterator();
		Iterator<Expression> iteratorInitializers = initializers.iterator();
		while (iteratorNames.hasNext()) {
			this.names.add(iteratorNames.next());
			this.initializers.add(iteratorInitializers.next());
		}
	}
	
	public ConstantDeclaration(int start, int end, List variablesAndDefaults) {
		super(start, end);
		if (variablesAndDefaults == null || variablesAndDefaults == null || variablesAndDefaults.size() == 0) {
			throw new IllegalArgumentException();
		}
		
		for (Iterator iter = variablesAndDefaults.iterator(); iter.hasNext();) {
			ASTNode[] element = (ASTNode[]) iter.next();
			assert element != null && element.length == 2 &&  element[0] != null && element[1] != null;
			
			this.names.add((Identifier) element[0]);
			this.initializers.add((Expression) element[1]);
		}
	}

	
	
	public void accept(Visitor visitor) {
		visitor.visit(this);
		
			childrenAccept(visitor);
		
		visitor.endVisit(this);
	}

	public void childrenAccept(Visitor visitor) {
		Iterator<Identifier> iterator1 = names.iterator();
		Iterator<Expression> iterator2 = initializers.iterator();
		while (iterator1.hasNext()) {
			iterator1.next().accept(visitor);
			iterator2.next().accept(visitor);
		}
	}

	public void traverseTopDown(Visitor visitor) {
		accept(visitor);
		Iterator<Identifier> iterator1 = names.iterator();
		Iterator<Expression> iterator2 = initializers.iterator();
		while (iterator1.hasNext()) {
			iterator1.next().traverseTopDown(visitor);
			iterator2.next().traverseTopDown(visitor);
		}
	}

	public void traverseBottomUp(Visitor visitor) {
		Iterator<Identifier> iterator1 = names.iterator();
		Iterator<Expression> iterator2 = initializers.iterator();
		while (iterator1.hasNext()) {
			iterator1.next().traverseBottomUp(visitor);
			iterator2.next().traverseBottomUp(visitor);
		}
		accept(visitor);
	}

	public void toString(StringBuffer buffer, String tab) {
		buffer.append(tab).append("<ConstantDeclaration"); //$NON-NLS-1$
		appendInterval(buffer);
		buffer.append(">\n"); //$NON-NLS-1$
		Iterator<Identifier> iterator1 = names.iterator();
		Iterator<Expression> iterator2 = initializers.iterator();
		while (iterator1.hasNext()) {
			buffer.append(tab).append(TAB).append("<VariableName>\n"); //$NON-NLS-1$
			iterator1.next().toString(buffer, TAB + TAB + tab);
			buffer.append("\n"); //$NON-NLS-1$
			buffer.append(tab).append(TAB).append("</VariableName>\n"); //$NON-NLS-1$
			buffer.append(tab).append(TAB).append("<InitialValue>\n"); //$NON-NLS-1$
			Expression expr = iterator2.next();
			if (expr != null) {
				expr.toString(buffer, TAB + TAB + tab);
				buffer.append("\n"); //$NON-NLS-1$
			}
			buffer.append(tab).append(TAB).append("</InitialValue>\n"); //$NON-NLS-1$
		}
		buffer.append(tab).append("</ConstantDeclaration>"); //$NON-NLS-1$
	}

	public int getType() {
		return ASTNode.CONSTANT_DECLARATION;
	}

	/**
	 * @return constant initializers expressions
	 */
	public List<Expression> initializers() {
		return this.initializers;
	}
	
	/**
	 * @return the constant names 
	 */
	public List<Identifier> names() {
		return this.names;
	}
	
	
	/**
	 * @deprecated use {@link #names()}
	 */
	public Identifier[] getVariableNames() {
		return (Identifier[]) names.toArray(new Identifier[names.size()]);
	}


}
