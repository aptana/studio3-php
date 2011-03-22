/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package com.aptana.editor.php.core.model.env;



/** 
 *Element info for IMember elements. 
 */
public abstract class MemberElementInfo extends SourceRefElementInfo {	
	protected int flags;

	/**
	 * The start position of this member's name in the its
	 * openable's buffer.
	 */
	protected int nameStart= -1;

	/**
	 * The last position of this member's name in the its
	 * openable's buffer.
	 */
	protected int nameEnd= -1;

	/**
	 * @see org2.eclipse.dltk.internal.compiler.env.IGenericType#getModifiers()
	 * @see com.aptana.editor.php.contentassist.model.internal.env.dltk.internal.compiler.env.IGenericMethod#getModifiers()
	 * @see org2.eclipse.dltk.internal.compiler.env.IGenericField#getModifiers()
	 */
	public int getModifiers() {
		return this.flags;
	}
	/**
	 * @see org2.eclipse.dltk.internal.compiler.env.ISourceType#getNameSourceEnd()
	 * @see com.aptana.editor.php.contentassist.model.internal.env.dltk.internal.compiler.env.ISourceMethod#getNameSourceEnd()
	 * @see org2.eclipse.dltk.internal.compiler.env.ISourceField#getNameSourceEnd()
	 */
	public int getNameSourceEnd() {
		return this.nameEnd;
	}
	/**
	 * @see org2.eclipse.dltk.internal.compiler.env.ISourceType#getNameSourceStart()
	 * @see com.aptana.editor.php.contentassist.model.internal.env.dltk.internal.compiler.env.ISourceMethod#getNameSourceStart()
	 * @see org2.eclipse.dltk.internal.compiler.env.ISourceField#getNameSourceStart()
	 */
	public int getNameSourceStart() {
		return this.nameStart;
	}
	public void setFlags(int flags) {
		this.flags = flags;
	}
	/**
	 * Sets the last position of this member's name, relative
	 * to its openable's source buffer.
	 */
	public void setNameSourceEnd(int end) {
		this.nameEnd= end;
	}
	/**
	 * Sets the start position of this member's name, relative
	 * to its openable's source buffer.
	 */
	public void setNameSourceStart(int start) {
		this.nameStart= start;
	}
}
