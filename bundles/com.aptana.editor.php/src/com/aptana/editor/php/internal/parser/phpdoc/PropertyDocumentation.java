package com.aptana.editor.php.internal.parser.phpdoc;

import java.util.ArrayList;

import com.aptana.core.IUserAgent;
import com.aptana.core.IUserAgentManager;

/**
 * Documentation available for properties or functions.
 */
public class PropertyDocumentation extends DocumentationBase
{
	private TypedDescription fReturns = new TypedDescription();
	private TypedDescription fMember = new TypedDescription();
	private TypedDescription fAlias = new TypedDescription();
	private String fDeprecatedDescription = ""; //$NON-NLS-1$
	private String fSince = ""; //$NON-NLS-1$
	private ArrayList<IUserAgent> fUserAgents;

	private boolean fIsDeprecated = false;
	private boolean fIsPrivate = false;
	private boolean fIsProtected = false;
	private boolean fIsInternal = false;
	private boolean fIsNative = false;
	private boolean fIsInstance = false;
	private boolean fIsInvocationOnly = false;
	private boolean fIsIgnored = false;

	/**
	 * Adds a type to the type list.
	 * 
	 * @param value
	 *            The full name (including namespaces, if any) of type to add.
	 */
	public void addUserAgent(IUserAgent value)
	{
		if (fUserAgents == null)
		{
			fUserAgents = new ArrayList<IUserAgent>();
		}
		//		if(!value.getPlatform().equals("")) //$NON-NLS-1$
		// {
		fUserAgents.add(value);
		// }
	}

	/**
	 * Returns a list of new user agents
	 * 
	 * @return Returns a list of new user agents
	 */
	public IUserAgent[] getUserAgents()
	{
		if (fUserAgents == null)
		{
			return IUserAgentManager.NO_USER_AGENTS;
		}

		return fUserAgents.toArray(new IUserAgent[fUserAgents.size()]);
	}

	/**
	 * Gets true if this member has been deprecated.
	 * 
	 * @return Returns true if this member has been deprecated.
	 */
	public boolean getIsDeprecated()
	{
		return fIsDeprecated;
	}

	/**
	 * Gets true if this member has been deprecated.
	 * 
	 * @param value
	 *            True if this member has been deprecated.
	 */
	public void setIsDeprecated(boolean value)
	{
		fIsDeprecated = value;
	}

	/**
	 * Gets information about the deprecation of this object (optional).
	 * 
	 * @return Returns a description of the deprecated object.
	 */
	public String getDeprecatedDescription()
	{
		return fDeprecatedDescription;
	}

	/**
	 * Sets information about the deprecation of this object (optional).
	 * 
	 * @param value
	 *            The information about the deprecation of this object (optional).
	 */
	public void setDeprecatedDescription(String value)
	{
		fDeprecatedDescription = (value == null) ? "" : value; //$NON-NLS-1$
	}

	/**
	 * Gets the prototype based class this function is a member of, if any. If this function belongs to multiple types,
	 * they can be added in the TypedDescription types list and the member can be commented accordingly.
	 * 
	 * @return Returns the base type, if any described by a TypedDescription object.
	 */
	public TypedDescription getMemberOf()
	{
		if (fMember == null)
		{
			fMember = new TypedDescription();
		}

		return fMember;
	}

	/**
	 * Gets a list of aliases, if any. Aliases are used in cases where functions are defined in one place (perhaps
	 * anonymously or in a deep namespace) and then aliased to a new (usually simpler) name.
	 * 
	 * @return Returns a list of aliases, if any.
	 */
	public TypedDescription getAliases()
	{
		if (fAlias == null)
		{
			fAlias = new TypedDescription();
		}

		return fAlias;
	}

	/**
	 * Gets the return value of this element. This can be from
	 * 
	 * @type for functions and for properties
	 * @return The return type of the object, and its descrpititon.
	 */
	public TypedDescription getReturn()
	{
		if (fReturns == null)
		{
			fReturns = new TypedDescription();
		}

		return fReturns;
	}

	/**
	 * Gets the version that this element was introduced in (optional).
	 * 
	 * @return Returns the version that this element was introduced in (optional).
	 */
	public String getSince()
	{
		return fSince;
	}

	/**
	 * Sets the version that this element was introduced in (optional).
	 * 
	 * @param value
	 *            Tthe version that this element was introduced in (optional).
	 */
	public void setSince(String value)
	{
		fSince = (value == null) ? "" : value; //$NON-NLS-1$
	}

	/**
	 * Gets true if this object is not to be accessed from outside the class (optional, default is false).
	 * 
	 * @return Returns true if this object is not to be accessed from outside the class.
	 */
	public boolean getIsPrivate()
	{
		return fIsPrivate;
	}

	/**
	 * Sets to true if this object is not to be accessed from outside the class (optional, default is false).
	 * 
	 * @param value
	 *            Boolean parameter, true if private.
	 */
	public void setIsPrivate(boolean value)
	{
		fIsPrivate = value;
	}

	/**
	 * Gets true if this object is to be accessed from itself or subclasses (optional, default is false).
	 * 
	 * @return Returns true if this object is to be accessed from itself or subclasses.
	 */
	public boolean getIsProtected()
	{
		return fIsProtected;
	}

	/**
	 * Sets to true if this object is only to be accessed from itself or subclasses (optional, default is false).
	 * 
	 * @param value
	 *            Boolean parameter, true if private.
	 */
	public void setIsProtected(boolean value)
	{
		fIsProtected = value;
	}

	/**
	 * Gets true if this object is not to be visible at all to javascript (eg. HTMLTableCellElement.createCaption() is
	 * internal to html, and not meant to be visible to javascript) (optional, default is false).
	 * 
	 * @return Returns true if this object is internal.
	 */
	public boolean getIsInternal()
	{
		return fIsInternal;
	}

	/**
	 * Set to true if this object is not to be visible at all to javascript (eg. HTMLTableCellElement.createCaption() is
	 * internal to html, and not meant to be visible to javascript) (optional, default is false).
	 * 
	 * @param value
	 *            Boolean parameter, true if internal.
	 */
	public void setIsInternal(boolean value)
	{
		fIsInternal = value;
	}

	/**
	 * Gets true if this object is from native code (eg. Math.abs() is native in javascript and thus can not be deleted)
	 * (optional, default is false).
	 * 
	 * @return Returns true if this object is native.
	 */
	public boolean getIsNative()
	{
		return fIsNative;
	}

	/**
	 * Set to true if this object is from native code (eg. Math.abs() is native in javascript and thus can not be
	 * deleted) (optional, default is false).
	 * 
	 * @param value
	 *            Boolean parameter, true if native.
	 */
	public void setIsNative(boolean value)
	{
		fIsNative = value;
	}

	/**
	 * Gets true if this is an instance only property (so belongs on the prototype).
	 * 
	 * @return Returns true if this is an instance only property (so belongs on the prototype).
	 */
	public boolean getIsInstance()
	{
		return fIsInstance;
	}

	/**
	 * Set to true if this is an instance only property (so belongs on the prototype).
	 * 
	 * @param value
	 *            Boolean parameter, true if this is an instance property.
	 */
	public void setIsInstance(boolean value)
	{
		fIsInstance = value;
	}

	/**
	 * Gets true if this object is "ignored" or not really meant to be seen publically in documentation.
	 * 
	 * @return Returns true if this object is supposed to be ignored.
	 */
	public boolean getIsIgnored()
	{
		return fIsIgnored;
	}

	/**
	 * Set to true if this object is "ignored" or not really meant to be seen publically in documentation.
	 * 
	 * @param value
	 *            True if this object is supposed to be ignored.
	 */
	public void setIsIgnored(boolean value)
	{
		fIsIgnored = value;
	}

	/**
	 * Gets true if this is available only at invocation time (like the arguments property inside a function).
	 * 
	 * @return Returns true if this is available only at invocation time (like the arguments property inside a
	 *         function).
	 */
	public boolean getIsInvocationOnly()
	{
		return fIsInvocationOnly;
	}

	/**
	 * Set to true if this is available only at invocation time (like the arguments property inside a function).
	 * 
	 * @param value
	 *            Boolean parameter, true if this is available only at invocation time.
	 */
	public void setIsInvocationOnly(boolean value)
	{
		fIsInvocationOnly = value;
	}

	/**
	 * @see com.aptana.metadata.IDocumentation#setExample(java.lang.String)
	 */
	public void setExample(String value)
	{

	}

}
