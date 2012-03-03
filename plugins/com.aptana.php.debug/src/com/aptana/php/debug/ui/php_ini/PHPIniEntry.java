/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.ui.php_ini;

import java.util.Map;

/**
 * PHP ini entry.
 * 
 * @author Denis Denisenko, Shalom Gibly
 */
public class PHPIniEntry implements Map.Entry<String, String>
{
	public static final int PHP_EXTENSION_VALIDATION_UNKNOWN = 0;
	public static final int PHP_EXTENSION_VALIDATION_OK = 1;
	public static final int PHP_EXTENSION_VALIDATION_ERROR = 2;
	public static final int PHP_EXTENSION_VALIDATION_WARNING = 3;

	private static final String[] EXTENSION_ENTRIES = { "extension", "zend_extension", "zend_extension_ts" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	/**
	 * Key.
	 */
	private String key;

	/**
	 * Value.
	 */
	private String value;

	/**
	 * Whether entry is commented.
	 */
	private boolean commented;

	/**
	 * Holds the validity state of this element.
	 * 
	 * @see INIFileModifier#PHP_EXTENSION_VALIDATION_OK
	 * @see INIFileModifier#PHP_EXTENSION_VALIDATION_ERROR
	 * @see INIFileModifier#PHP_EXTENSION_VALIDATION_WARNING
	 * @see INIFileModifier#PHP_EXTENSION_VALIDATION_UNKNOWN
	 */
	private int validity;

	/**
	 * Holds the validation note (if exists). A validation note is either an error note or a warning note that was
	 * generated while executing the PHP interpreter.
	 */
	private String validationNote;

	/**
	 * Parent section.
	 */
	private final INIFileSection parent;

	/**
	 * PHPIniEntry constructor.
	 * 
	 * @param key
	 *            - entry key.
	 * @param value
	 *            - entry value.
	 * @param parent
	 *            - parent section.
	 */
	public PHPIniEntry(String key, String value, INIFileSection parent)
	{
		this.key = key;
		this.value = value;
		this.parent = parent;
		this.validity = PHP_EXTENSION_VALIDATION_UNKNOWN;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getKey()
	{
		return key;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getValue()
	{
		return value;
	}

	/**
	 * {@inheritDoc}
	 */
	public String setValue(String val)
	{
		value = val;
		return val;
	}

	/**
	 * Returns section.
	 * 
	 * @return section.
	 */
	public INIFileSection getSection()
	{
		return parent;
	}

	/**
	 * Returns the validation state of this entry. Note that the state is not set until the PHP ini validator is
	 * invoked.
	 * 
	 * @return the validation state of this entry.
	 * @see INIFileModifier#PHP_EXTENSION_VALIDATION_OK
	 * @see INIFileModifier#PHP_EXTENSION_VALIDATION_ERROR
	 * @see INIFileModifier#PHP_EXTENSION_VALIDATION_WARNING
	 * @see INIFileModifier#PHP_EXTENSION_VALIDATION_UNKNOWN
	 */
	public int getValidationState()
	{
		return validity;
	}

	/**
	 * Sets the validation state and note for this entry.
	 * 
	 * @param state
	 * @param validationNote
	 */
	public void setValidationState(int state, String validationNote)
	{
		this.validity = state;
		this.validationNote = validationNote;
	}

	/**
	 * Returns the validation note that was set for this element when the PHP ini validator was invoked.
	 * 
	 * @return the validation note; or null.
	 */
	public String getValidationNote()
	{
		return this.validationNote;
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	/**
	 * Sets commented state.
	 * 
	 * @param commented
	 *            - commented state to set.
	 */
	public void setCommented(boolean commented)
	{
		this.commented = commented;
	}

	/**
	 * Gets whether this entry is commented.
	 * 
	 * @return true if commented, false otherwise.
	 */
	public boolean getCommented()
	{
		return commented;
	}

	/**
	 * Returns whether this entry marks a PHP extension entry. Note: Commented entries will always return false.
	 * 
	 * @return True, if it's an extension entry; False, otherwise.
	 */
	public boolean isExtensionEntry()
	{
		if (getCommented())
		{
			return false;
		}
		for (String ext : EXTENSION_ENTRIES)
		{
			if (ext.equalsIgnoreCase(getKey()))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	// $codepro.audit.disable
	// com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.obeyEqualsContract.obeyGeneralContractOfEquals
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
		if (getClass() != obj.getClass()) // $codepro.audit.disable useEquals
		{
			return false;
		}
		final PHPIniEntry other = (PHPIniEntry) obj;
		if (key == null)
		{
			if (other.key != null)
			{
				return false;
			}
		}
		else if (!key.equals(other.key))
		{
			return false;
		}
		if (value == null)
		{
			if (other.value != null)
			{
				return false;
			}
		}
		else if (!value.equals(other.value))
		{
			return false;
		}
		return true;
	}
}