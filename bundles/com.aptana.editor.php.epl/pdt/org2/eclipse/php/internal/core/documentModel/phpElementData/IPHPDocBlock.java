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
package org2.eclipse.php.internal.core.documentModel.phpElementData;

import java.io.Serializable;
import java.util.Iterator;
@SuppressWarnings("rawtypes")
public interface IPHPDocBlock extends Serializable, IPHPDoc {

	int FILE_DOCBLOCK = 0;
	int CLASS_DOCBLOCK = 1;
	int FUNCTION_DOCBLOCK = 2;
	int VARIABLE_DOCBLOCK = 3;
	int GLOBAL_VAR_DOCBLOCK = 4;
	int DEFINE_DOCBLOCK = 5;
	int STATIC_VAR_DOCBLOCK = 6;
	int INCLUDE_FILE_DOCBLOCK = 7;
	int CLASS_CONST_DOCBLOCK = 8;
	int CLASS_VAR_DOCBLOCK = 9;

	String getShortDescription();

	String getLongDescription();

	IPHPDocTag[] getTags();

	IPHPDocTag[] getTagsAsArray();

	Iterator getTags(int id);
	
	boolean hasTagOf(int id);	

	int getType();

	int getStartPosition();

	int getEndPosition();

	void setStartPosition(int value);

	void setEndPosition(int value);

	void setShortDescription(String shortDescription);

	boolean containsPosition(int position);
}
