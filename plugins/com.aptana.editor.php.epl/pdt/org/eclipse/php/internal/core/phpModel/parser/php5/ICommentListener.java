/**
 * Copyright (c) 2005-2006 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package org.eclipse.php.internal.core.phpModel.parser.php5;

/**
 * 
 * @author Pavel Petrochenko
 *
 */		
public interface ICommentListener {

	/**
	 * 
	 * @param startOffset
	 * @param endOffset
	 */
	void handleComment(int startOffset, int endOffset);

	/**
	 * 
	 * @param commentStartPosition
	 * @param commentEndPosition 
	 */
	void handlePHPDoc(int commentStartPosition, int commentEndPosition);
	
}
