package com.aptana.editor.php.internal.parser.phpdoc;

import java.util.ArrayList;

import com.aptana.editor.common.contentassist.UserAgentManager.UserAgent;

/**
 * Documentation available for properties or functions.
 */
public class PropertyDocumentation extends DocumentationBase
{
	/**
	 * 
	 */
	private static final UserAgent[] EMPTY_USER_AGENTS = new UserAgent[0];
	private TypedDescription fReturns = new TypedDescription();
	private TypedDescription fMember = new TypedDescription();
	private TypedDescription fAlias = new TypedDescription();
	private String fDeprecatedDescription = ""; //$NON-NLS-1$
	private String fSince = ""; //$NON-NLS-1$
	private ArrayList<UserAgent> fUserAgents;

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
	public void addUserAgent(UserAgent value)
	{
		if (fUserAgents == null)
		{
			fUserAgents = new ArrayList<UserAgent>();
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
	public UserAgent[] getUserAgents()
	{
		if (fUserAgents == null)
		{
			return EMPTY_USER_AGENTS;
		}

		return fUserAgents.toArray(new UserAgent[fUserAgents.size()]);
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

	// /**
	// * Sets the prototype based class this function is a member of, if any.
	// * @param value A TypedDescription object representing the name and description of the prototype based class(es)
	// this function belongs to.
	// */
	// public void setMemberOf(TypedDescription value)
	// {
	// fMember = value;
	// }

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

	// /**
	// * Sets the return value of this element. This can be from @return for functions, and @type for properties
	// * Use the name 'return' for the TypedDescription name.
	// * @param value The return type of the object, and its descrpititon.
	// */
	// public void setReturn(TypedDescription value)
	// {
	// fReturns = value;
	// }

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

	// /**
	// * @throws IOException
	// * @see com.aptana.editor.php.phpdoc.parsing.DocumentationBase#read(java.io.DataInput)
	// */
	// public void read(DataInput input) throws IOException
	// {
	// super.read(input);
	//
	// int size = input.readInt();
	// if (size > 0)
	// {
	// this.fUserAgents = new ArrayList<UserAgent>();
	//
	// for (int i = 0; i < size; i++)
	// {
	// UserAgent param = new UserAgent();
	//
	// param.read(input);
	// this.fUserAgents.add(param);
	// }
	// }
	//
	// this.fReturns = new TypedDescription();
	// this.fReturns.read(input);
	// this.fMember = new TypedDescription();
	// this.fMember.read(input);
	// this.fDeprecatedDescription = input.readUTF();
	// this.fSince = input.readUTF();
	// this.fIsDeprecated = input.readBoolean();
	// this.fIsPrivate = input.readBoolean();
	// this.fIsProtected = input.readBoolean();
	// this.fIsInternal = input.readBoolean();
	// this.fIsNative = input.readBoolean();
	// this.fIsInstance = input.readBoolean();
	// this.fIsInvocationOnly = input.readBoolean();
	// this.fIsIgnored = input.readBoolean();
	// }

	// /**
	// * @throws IOException
	// * @see com.aptana.editor.php.phpdoc.parsing.DocumentationBase#write(java.io.DataOutput)
	// */
	// public void write(DataOutput output) throws IOException
	// {
	// super.write(output);
	//
	// if (this.fUserAgents != null)
	// {
	// output.writeInt(this.fUserAgents.size());
	//
	// for (int i = 0; i < this.fUserAgents.size(); i++)
	// {
	// UserAgent param = (UserAgent) this.fUserAgents.get(i);
	//
	// param.write(output);
	// }
	// }
	// else
	// {
	// output.writeInt(0);
	// }
	//
	// this.fReturns.write(output);
	// this.fMember.write(output);
	// output.writeUTF(this.fDeprecatedDescription);
	// output.writeUTF(this.fSince);
	// output.writeBoolean(this.fIsDeprecated);
	// output.writeBoolean(this.fIsPrivate);
	// output.writeBoolean(this.fIsProtected);
	// output.writeBoolean(this.fIsInternal);
	// output.writeBoolean(this.fIsNative);
	// output.writeBoolean(this.fIsInstance);
	// output.writeBoolean(this.fIsInvocationOnly);
	// output.writeBoolean(this.fIsIgnored);
	// }
	//
	// /**
	// * Returns a list of all the platforms this item is supported by
	// * @return Returns a list of all the platforms this item is supported by
	// */
	// public String[] getUserAgentPlatformNames()
	// {
	// ArrayList<String> al = new ArrayList<String>();
	// if (this.fUserAgents != null)
	// {
	// for (int i = 0; i < this.fUserAgents.size(); i++)
	// {
	// UserAgent param = (UserAgent) this.fUserAgents.get(i);
	// al.add(param.getPlatform());
	// }
	// }
	//
	// return al.toArray(new String[0]);
	// }

	/**
	 * @see com.aptana.metadata.IDocumentation#setExample(java.lang.String)
	 */
	public void setExample(String value)
	{

	}

	// private boolean fIsStatic = false;
	// /**
	// * Gets true if this object is to be accessed in a static way (optional, default is false).
	// * @return Returns true if this object is to be accessed in a static way.
	// */
	// public boolean getIsStatic()
	// {
	// return fIsStatic;
	// }
	// /**
	// * Sets to true if this object is to be accessed in a static way (optional, default is false).
	// * @param value Boolean parameter, true if static.
	// */
	// public void setIsStatic(boolean value)
	// {
	// fIsStatic = value;
	// }

	// private boolean fIsEnum;
	// private boolean fIsFlags;
	// private boolean fIsPrivate;
	// private boolean fIsFinal;
	// private boolean fIsIgnore;
	// private String fRequires;
	// private boolean fIsEvent;
	// public static final int usagePrivate = 1;
	// public static final int usageProtected = 2;
	// public static final int usageInternal = 4;
	// public static final int usageFinal = 8;
	// public static final int usageNative = 16;
	// /**
	// * Gets the usage of this object (private | protected | internal | final | native). This is a set of 'or'able
	// ints.
	// * 'native' means this is from native code, and all memebers go directly on the instance rather than looked up
	// from the prototype.
	// * @return Returns the usage. Use the 'usageXXX' statics in this class to determine the use.
	// */
	// public int getUsage()
	// {
	// return fUsage;
	// }
	// /**
	// * Sets the usage of this object (private | protected | internal | final | native). This is a set of 'or'able
	// ints.
	// * @param value The usage. Use the 'or'able 'usageXXX' statics in this class to determine the use.
	// */
	// public void setUsage(int value)
	// {
	// fUsage = value;
	// }
	// public boolean getIsEvent()
	// {
	// return fIsEvent;
	// }
	// public void setIsEvent(boolean value)
	// {
	// fIsEvent = value;
	// }
	//
	// public boolean getIsEnum()
	// {
	// return fIsEnum;
	// }
	// public void setIsEnum(boolean value)
	// {
	// fIsEnum = value;
	// }
	//
	// public boolean getIsFlags()
	// {
	// return fIsFlags;
	// }
	// public void setIsFlags(boolean value)
	// {
	// fIsFlags = value;
	// }
	//
	// public boolean getIsIgnore()
	// {
	// return fIsIgnore;
	// }
	// public void setIsIgnore(boolean value)
	// {
	// fIsIgnore = value;
	// }
	//
	// public String getRequires()
	// {
	// return fRequires;
	// }
	// public void setRequires(String value)
	// {
	// fRequires = value;
	// }

}
