/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.parser.nodes;

import com.aptana.editor.php.epl.PHPEplPlugin;

/**
 * Models function parameter
 * 
 * @author Pavel Petrochenko
 */
public class Parameter
{

	private final String classType;
	private final String variableName;
	private final boolean isReference;
	private final String defaultValue;

	/**
	 * @param classType
	 * @param variableName
	 * @param defaultValue
	 * @param isReference
	 * @param isConst
	 */
	public Parameter(String classType, String variableName, String defaultValue, boolean isReference, boolean isConst)
	{
		this.classType = classType;
		this.variableName = PHPEplPlugin.getDefault().sharedString(variableName);
		this.isReference = isReference;
		this.defaultValue = PHPEplPlugin.getDefault().sharedString(defaultValue);
	}

	/**
	 * @return type
	 */
	public String getClassType()
	{
		return classType;
	}

	/**
	 * @return name
	 */
	public String getVariableName()
	{
		return variableName;
	}

	/**
	 * @return is this parameter passed by ref
	 */
	public boolean isReference()
	{
		return isReference;
	}

	/**
	 * @return default value for this parameter
	 */
	public String getDefaultValue()
	{
		return defaultValue;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((classType == null) ? 0 : classType.hashCode());
		result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
		result = prime * result + (isReference ? 1231 : 1237);
		result = prime * result + ((variableName == null) ? 0 : variableName.hashCode());
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		final Parameter other = (Parameter) obj;
		if (classType == null)
		{
			if (other.classType != null)
			{
				return false;
			}
		}
		else if (!classType.equals(other.classType))
		{
			return false;
		}
		if (defaultValue == null)
		{
			if (other.defaultValue != null)
			{
				return false;
			}
		}
		else if (!defaultValue.equals(other.defaultValue))
		{
			return false;
		}
		if (isReference != other.isReference)
		{
			return false;
		}
		if (variableName == null)
		{
			if (other.variableName != null)
			{
				return false;
			}
		}
		else if (!variableName.equals(other.variableName))
		{
			return false;
		}
		return true;
	}

	/**
	 * prints label to given StringBuffer
	 * 
	 * @param buf
	 */
	public void addLabel(StringBuffer buf)
	{
		if (this.classType != null && this.classType.length() > 0)
		{
			buf.append(this.classType);
			buf.append(' ');
		}
		if (this.isReference)
		{
			buf.append('&');
		}
		buf.append('$' + this.getVariableName());
	}

}
