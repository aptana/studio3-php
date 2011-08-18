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

public interface IPHPDocTag extends Serializable {

	 int ABSTRACT = 0;

	 int AUTHOR = 1;

	 int DEPRECATED = 2;

	 int FINAL = 3;

	 int GLOBAL = 4;

	 int NAME = 5;

	 int RETURN = 6;

	 int PARAM = 7;

	 int SEE = 8;

	 int STATIC = 9;

	 int STATICVAR = 10;

	 int TODO = 11;

	 int VAR = 12;

	 int PACKAGE = 13;

	 int ACCESS = 14;

	 int CATEGORY = 15;

	 int COPYRIGHT = 16;

	 int DESC = 17;

	 int EXAMPLE = 18;

	 int FILESOURCE = 19;

	 int IGNORE = 20;

	 int INTERNAL = 21;

	 int LICENSE = 22;

	 int LINK = 23;

	 int SINCE = 24;

	 int SUBPACKAGE = 25;

	 int TUTORIAL = 26;

	 int USES = 27;

	 int VERSION = 28;

	 int THROWS = 29;

	int getID();

	String getValue();

	int getTagKind();
}
