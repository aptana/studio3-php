/*******************************************************************************
 * Copyright (c) 2006 Zend Corporation and IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org.eclipse.php.internal.core.ast.nodes;

/**
 * base class for all the static access
 */
public abstract class StaticDispatch extends VariableBase {

	private final Expression className;

	public StaticDispatch(int start, int end, Expression className) {
		super(start, end);

		assert className != null;
		this.className = className;
		className.setDescriptor(StaticMethodInvocation.CLASS_NAME_PROPERTY);
		className.setParent(this);
	}

	public Expression getClassName() {
		return className;
	}

	public abstract ASTNode getMember();

	public Identifier getClassNameAsIdentifier() {
		return (Identifier) (className instanceof Identifier?className:new Identifier(className.getStart(),className.getEnd(),""));
	}
}
