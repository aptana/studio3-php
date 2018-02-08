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

import java.util.HashMap;

@SuppressWarnings( { "unchecked", "nls", "rawtypes" })
public class BasicPHPDocTag implements IPHPDocTag
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private String value;
	private static HashMap nameToID;

	public BasicPHPDocTag(int id, String value)
	{
		this.id = id;
		this.value = value;
	}

	public int getID()
	{
		return id;
	}

	/**
	 * This one returns -1 on every call.
	 */
	public int getTagKind()
	{
		return -1;
	}

	public String getValue()
	{
		return value;
	}

	public static String getName(int id)
	{
		switch (id)
		{
			case ABSTRACT:
				return "abstract";
			case AUTHOR:
				return "author";
			case ACCESS:
				return "access";
			case CATEGORY:
				return "category";
			case COPYRIGHT:
				return "copyright";
			case DEPRECATED:
				return "deprecated";
			case EXAMPLE:
				return "example";
			case FINAL:
				return "final";
			case FILESOURCE:
				return "filesource";
			case GLOBAL:
				return "global";
			case IGNORE:
				return "ignore";
			case INTERNAL:
				return "internal";
			case LICENSE:
				return "license";
			case LINK:
				return "link";
			case NAME:
				return "name";
			case RETURN:
				return "return";
			case PACKAGE:
				return "package";
			case PARAM:
				return "param";
			case SEE:
				return "see";
			case SINCE:
				return "since";
			case STATIC:
				return "static";
			case STATICVAR:
				return "staticvar";
			case SUBPACKAGE:
				return "subpackage";
			case THROWS:
				return "throws";
			case TODO:
				return "todo";
			case TUTORIAL:
				return "tutorial";
			case USES:
				return "uses";
			case VAR:
				return "var";
			case VERSION:
				return "version";
			case DESC:
				return "desc";
		}
		return "";
	}

	public static int getID(String name)
	{
		Integer rv = (Integer) getNameToID().get(name);
		if (rv == null)
		{
			return -1;
		}
		return rv.intValue();
	}

	private static HashMap getNameToID()
	{
		if (nameToID == null)
		{
			nameToID = new HashMap();
			nameToID.put("abstract", new Integer(ABSTRACT));
			nameToID.put("access", new Integer(ACCESS));
			nameToID.put("author", new Integer(AUTHOR));
			nameToID.put("category", new Integer(CATEGORY));
			nameToID.put("copyright", new Integer(COPYRIGHT));
			nameToID.put("deprecated", new Integer(DEPRECATED));
			nameToID.put("desc", new Integer(DESC));
			nameToID.put("example", new Integer(EXAMPLE));
			nameToID.put("final", new Integer(FINAL));
			nameToID.put("filesource", new Integer(FILESOURCE));
			nameToID.put("global", new Integer(GLOBAL));
			nameToID.put("ignore", new Integer(IGNORE));
			nameToID.put("internal", new Integer(INTERNAL));
			nameToID.put("license", new Integer(LICENSE));
			nameToID.put("link", new Integer(LINK));
			nameToID.put("name", new Integer(NAME));
			nameToID.put("return", new Integer(RETURN));
			nameToID.put("package", new Integer(PACKAGE));
			nameToID.put("param", new Integer(PARAM));
			nameToID.put("see", new Integer(SEE));
			nameToID.put("since", new Integer(SINCE));
			nameToID.put("static", new Integer(STATIC));
			nameToID.put("staticvar", new Integer(STATICVAR));
			nameToID.put("subpackage", new Integer(SUBPACKAGE));
			nameToID.put("throws", new Integer(THROWS));
			nameToID.put("todo", new Integer(TODO));
			nameToID.put("tutorial", new Integer(TUTORIAL));
			nameToID.put("uses", new Integer(USES));
			nameToID.put("var", new Integer(VAR));
			nameToID.put("version", new Integer(VERSION));
		}
		return nameToID;
	}

	public String toString()
	{
		StringBuffer b = new StringBuffer("@");
		b.append("<b>" + getName(getID()) + "</b>");
		b.append(" ");
		if (getValue() != null)
		{
			b.append(getValue().toString());
		}
		return b.toString();
	}

	/**
	 * Create a document-model doc tag from a give AST doc tag type. <br>
	 * [Note: Aptana Addition]
	 * 
	 * @param docTag
	 *            An AST IPHPDocTag (can be null).
	 * @return A IPHPDocTag; Null if the given docTag was null, or there is no matching doc type.
	 */
	public static IPHPDocTag fromASTDocTag(IPHPDocTag docTag)
	{
		if (docTag == null)
		{
			return null;
		}
		String tagKind = org2.eclipse.php.internal.core.compiler.ast.nodes.PHPDocTag.getTagKind(docTag.getTagKind());
		int thisId = getID(tagKind);
		if (thisId == -1)
		{
			// There is no matching model doc tag
			return null;
		}
		return new BasicPHPDocTag(thisId, docTag.getValue());
	}
}
