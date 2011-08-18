package com.aptana.editor.php.internal.parser.phpdoc;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Robin Debreuil
 */
public class FunctionDocumentation
{
	private static final TypedDescription[] EMPTY_TYPED_DESCRIPTIONS = new TypedDescription[0];
	private String fClassDescription = ""; //$NON-NLS-1$
	private String fMethodName = ""; //$NON-NLS-1$
	// private int fKind;
	private TypedDescription fExtends = new TypedDescription();
	private ArrayList<TypedDescription> fParams;
	private ArrayList<TypedDescription> fExceptions;
	private boolean fIsConstructor = false;
	private boolean fIsMethod = false;
	private List<TypedDescription> fVars;
	private String fDescription;
	private com.aptana.editor.php.internal.parser.phpdoc.TypedDescription fReturns;

	/**
	 * Gets the fDescription of the class if this function represents a constructor of the class.
	 * 
	 * @return Returns the fDescription of the class.
	 */
	public String getClassDescription()
	{
		return fClassDescription;
	}

	/**
	 * Sets the fDescription of the class if this function represents a constructor of the class.
	 * 
	 * @param value
	 *            The fDescription of the class.
	 */
	public void setClassDescription(String value)
	{
		fClassDescription = (value == null) ? "" : value; //$NON-NLS-1$
	}

	/**
	 * Gets the name of the method if different than the computed value.
	 * 
	 * @return Returns the name of the method.
	 */
	public String getMethodName()
	{
		return fMethodName;
	}

	/**
	 * Sets the name of the method if different than the computed value.
	 * 
	 * @param value
	 *            The name of the method.
	 */
	public void setMethodName(String value)
	{
		fMethodName = (value == null) ? "" : value; //$NON-NLS-1$
	}

	/**
	 * Gets the prototype based class this function extends, if any (default is Object).
	 * 
	 * @return Returns the prototype based class this function extends, if any (default is Object).
	 */
	public TypedDescription getExtends()
	{
		if (fExtends == null)
		{
			return new TypedDescription();
		}
		return fExtends;
	}

	/**
	 * Sets the prototype based class this function extends, if any (default is Object).
	 * 
	 * @param value
	 *            The name (or names, comma sepearted) of the prototype based class(es) this function extends.
	 */
	public void setExtends(TypedDescription value)
	{
		fExtends = value;
	}

	/**
	 * Gets an array of TypedDescription objects (prototype based class name and fDescription) that describe the
	 * parameters used by this function.
	 * 
	 * @return Returns the params, each described by a TypedDescription object.
	 */
	public TypedDescription[] getParams()
	{
		if (fParams == null)
		{
			return EMPTY_TYPED_DESCRIPTIONS;
		}

		return fParams.toArray(new TypedDescription[fParams.size()]);
	}

	/**
	 * Adds a TypedDescription object (prototype based class name and fDescription) that describes a parameter used by
	 * this function. To add multiple unkown parameters use the name '...'.
	 * 
	 * @param value
	 *            A TypedDescription object (prototype based class name and fDescription) that describes a parameter
	 *            used by this function.
	 */
	public void addParam(TypedDescription value)
	{
		if (fParams == null)
		{
			fParams = new ArrayList<TypedDescription>();
		}
		fParams.add(value);
	}

	/**
	 * Clears the params, used when merging param lists with external script doc files.
	 */
	public void clearParams()
	{
		if (fParams != null)
		{
			fParams.clear();
		}
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
	 * Gets true if this object is used as a constructor (an object can be used both as a method and a constructor, or
	 * neither).
	 * 
	 * @return Returns true if this object is used as a constructor.
	 */
	public boolean getIsConstructor()
	{
		return fIsConstructor;
	}

	/**
	 * Set to true if this object is used as a constructor (an object can be used both as a method and a constructor, or
	 * neither).
	 * 
	 * @param value
	 *            True if this object is used as a constructor.
	 */
	public void setIsConstructor(boolean value)
	{
		fIsConstructor = value;
	}

	/**
	 * Gets true if this object is used as a method (an object can be used both as a method and a constructor, or
	 * neither).
	 * 
	 * @return Returns true if this object is used as a method.
	 */
	public boolean getIsMethod()
	{
		return fIsMethod;
	}

	/**
	 * Set to true if this object is used as a method (an object can be used both as a method and a constructor, or
	 * neither).
	 * 
	 * @param value
	 *            True if this object is used as a method.
	 */
	public void setIsMethod(boolean value)
	{
		fIsMethod = value;
	}

	/**
	 * Gets any exceptions this function can throw.
	 * 
	 * @return Returns an array of exceptions this function can throw.
	 */
	public TypedDescription[] getExceptions()
	{
		if (fExceptions == null)
		{
			return EMPTY_TYPED_DESCRIPTIONS;
		}
		return fExceptions.toArray(new TypedDescription[fExceptions.size()]);
	}

	/**
	 * Adds an exception that this function can throw.
	 * 
	 * @param value
	 *            The name, type and fDescription of an exception that this function can throw.
	 */
	public void addException(TypedDescription value)
	{
		if (fExceptions == null)
		{
			fExceptions = new ArrayList<TypedDescription>();
		}
		fExceptions.add(value);
	}

	/**
	 * Clears the list of exceptions, used when merging docs.
	 */
	public void clearExceptions()
	{
		if (fExceptions != null)
		{
			fExceptions.clear();
		}
	}

	// /**
	// * @throws IOException
	// * @see com.aptana.editor.php.phpdoc.parsing.DocumentationBase#read(java.io.DataInput)
	// */
	// public void read(DataInput input) throws IOException
	// {
	// super.read(input);
	//
	// this.fClassDescription = input.readUTF();
	// this.fExtends = new TypedDescription();
	// this.fExtends.read(input);
	//
	// int size = input.readInt();
	// if (size > 0)
	// {
	// this.fParams = new ArrayList();
	//
	// for (int i = 0; i < size; i++)
	// {
	// TypedDescription param = new TypedDescription();
	//
	// param.read(input);
	// this.fParams.add(param);
	// }
	// }
	//
	// size = input.readInt();
	// if (size > 0)
	// {
	// this.fExceptions = new ArrayList();
	//
	// for (int i = 0; i < size; i++)
	// {
	// TypedDescription exception = new TypedDescription();
	//
	// exception.read(input);
	// this.fExceptions.add(exception);
	// }
	// }
	//
	// this.fIsConstructor = input.readBoolean();
	// this.fIsMethod = input.readBoolean();
	// this.setIsIgnored(input.readBoolean());
	// }

	// /**
	// * @throws IOException
	// * @see com.aptana.editor.php.phpdoc.parsing.DocumentationBase#write(java.io.DataOutput)
	// */
	// public void write(DataOutput output) throws IOException
	// {
	// super.write(output);
	//
	// output.writeUTF(this.fClassDescription);
	// this.fExtends.write(output);
	//
	// if (this.fParams != null)
	// {
	// output.writeInt(this.fParams.size());
	//
	// for (int i = 0; i < this.fParams.size(); i++)
	// {
	// TypedDescription param = (TypedDescription) this.fParams.get(i);
	//
	// param.write(output);
	// }
	// }
	// else
	// {
	// output.writeInt(0);
	// }
	//
	// if (this.fExceptions != null)
	// {
	// output.writeInt(this.fExceptions.size());
	//
	// for (int i = 0; i < this.fExceptions.size(); i++)
	// {
	// TypedDescription exception = (TypedDescription) this.fExceptions.get(i);
	//
	// exception.write(output);
	// }
	// }
	// else
	// {
	// output.writeInt(0);
	// }
	//
	// output.writeBoolean(this.fIsConstructor);
	// output.writeBoolean(this.fIsMethod);
	// output.writeBoolean(this.getIsIgnored());
	// }

	public void addVar(TypedDescription typeDescr)
	{
		if (fVars == null)
		{
			fVars = new ArrayList<TypedDescription>();
		}
		fVars.add(typeDescr);
	}

	public List<TypedDescription> getVars()
	{
		return fVars;
	}

	/**
	 * Set a fDescription of the function.
	 * 
	 * @param fDescription
	 */
	public void setDescription(String description)
	{
		this.fDescription = description;
	}

	/**
	 * Returns the function's String fDescription
	 * 
	 * @param fDescription
	 * @return A String description, or null if none was set.
	 */
	public String getDescription()
	{
		return fDescription;
	}

	// public static final int kindConstructor = 1;
	// public static final int kindMethod = 2;
	// public static final int kindEventHandler = 4;
	// public static final int kindFunction = 8;
	// /**
	// * Gets the kind of function this represents (constructor | method | eventHandler | function). Functions can be
	// used in more than one way, so these values are 'or'able.
	// * @return Returns an int represeneting the kind of function this function object can be. Use the 'kindXXX'
	// statics in this class to determine the use.
	// */
	// public int getKind()
	// {
	// return fKind;
	// }
	// /**
	// * Sets the kind of function this represents (constructor | method | eventHandler | function). Functions can be
	// used in more than one way, so these values are 'or'able.
	// * @param value An int represeneting the kind of function this function object can be. Use the 'kindXXX' statics
	// in this class to determine the use.
	// */
	// public void setKind(int value)
	// {
	// fKind = value;
	// }
}
