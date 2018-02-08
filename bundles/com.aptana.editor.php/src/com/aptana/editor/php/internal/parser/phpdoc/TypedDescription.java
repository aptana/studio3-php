package com.aptana.editor.php.internal.parser.phpdoc;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This is a class that holds type info and descriptions, used when representing return types, params, and exceptions.
 */
public class TypedDescription
{
	private static final TypedDescription[] EMPTY_TYPED_DESCRIPTIONS = new TypedDescription[0];
	private static final String[] EMPTY_STR_ARR = new String[0];
	private ArrayList<String> fTypes;
	private ArrayList<TypedDescription> fDefaultValues;
	private String fDescription = ""; //$NON-NLS-1$
	private String fName = ""; //$NON-NLS-1$

	private static Map<String, String> builtInAliases;
	static
	{
		builtInAliases = new HashMap<String, String>();
		builtInAliases.put("object", "Object"); //$NON-NLS-1$ //$NON-NLS-2$
		builtInAliases.put("function", "Function"); //$NON-NLS-1$ //$NON-NLS-2$
		builtInAliases.put("array", "Array"); //$NON-NLS-1$ //$NON-NLS-2$
		builtInAliases.put("date", "Date"); //$NON-NLS-1$ //$NON-NLS-2$
		builtInAliases.put("error", "Error"); //$NON-NLS-1$ //$NON-NLS-2$
		builtInAliases.put("string", "String"); //$NON-NLS-1$ //$NON-NLS-2$
		builtInAliases.put("char", "String"); //$NON-NLS-1$ //$NON-NLS-2$
		builtInAliases.put("Char", "String"); //$NON-NLS-1$ //$NON-NLS-2$
		builtInAliases.put("number", "Number"); //$NON-NLS-1$ //$NON-NLS-2$
		builtInAliases.put("int", "Number"); //$NON-NLS-1$ //$NON-NLS-2$
		builtInAliases.put("Int", "Number"); //$NON-NLS-1$ //$NON-NLS-2$
		builtInAliases.put("integer", "Number"); //$NON-NLS-1$ //$NON-NLS-2$
		builtInAliases.put("Integer", "Number"); //$NON-NLS-1$ //$NON-NLS-2$
		builtInAliases.put("float", "Number"); //$NON-NLS-1$ //$NON-NLS-2$
		builtInAliases.put("Float", "Number"); //$NON-NLS-1$ //$NON-NLS-2$
		builtInAliases.put("boolean", "Boolean"); //$NON-NLS-1$ //$NON-NLS-2$
		builtInAliases.put("Bool", "Boolean"); //$NON-NLS-1$ //$NON-NLS-2$
		builtInAliases.put("bool", "Boolean"); //$NON-NLS-1$ //$NON-NLS-2$
		builtInAliases.put("regEx", "RegExp"); //$NON-NLS-1$ //$NON-NLS-2$
		builtInAliases.put("RegEx", "RegExp"); //$NON-NLS-1$ //$NON-NLS-2$
		builtInAliases.put("regex", "RegExp"); //$NON-NLS-1$ //$NON-NLS-2$
		builtInAliases.put("regExp", "RegExp"); //$NON-NLS-1$ //$NON-NLS-2$
		builtInAliases.put("regexp", "RegExp"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Creates a description of a prototype based class (type) that includes the given name if appropriate, and a
	 * description.
	 * 
	 * @param description
	 *            Description for this usage.
	 * @param name
	 *            The given name (eg, param name, 'return', or exeption arg name). This can be empty where not
	 *            appropriate - like extends and memberof.
	 */
	public TypedDescription(String description, String name)
	{
		fDescription = (description == null) ? "" : description; //$NON-NLS-1$
		fName = (name == null) ? "" : name; //$NON-NLS-1$
	}

	/**
	 * Creates a description of a prototype based class (type) that includes the given name if appropriate, and a
	 * description.
	 * 
	 * @param description
	 *            Description for this usage.
	 */
	public TypedDescription(String description)
	{
		fDescription = (description == null) ? "" : description; //$NON-NLS-1$
	}

	/**
	 * Creates a description of a prototype based class (type) that includes the given name if appropriate, and a
	 * description.
	 */
	public TypedDescription()
	{
	}

	/**
	 * A list of types this object represents.
	 * 
	 * @return Returns a list of types, by full name (including namespaces, if any).
	 */
	public String[] getTypes()
	{
		if (fTypes == null)
		{
			return EMPTY_STR_ARR;
		}
		return fTypes.toArray(new String[fTypes.size()]);
	}

	/**
	 * Clear known types, this can be removed once the doc return types coming in are stable (eg Math should return
	 * nothing or Math, not default to Object)
	 */
	public void clearTypes()
	{
		if (fTypes != null)
		{
			fTypes.clear();
		}
	}

	/**
	 * Adds a type to the type list.
	 * 
	 * @param value
	 *            The full name (including namespaces, if any) of type to add.
	 */
	public void addType(String value)
	{
		value = (value == null) ? "" : value; //$NON-NLS-1$

		if (fTypes == null)
		{
			fTypes = new ArrayList<String>();
		}

		String result = checkForBuiltInAlias(value);
		fTypes.add(result);
	}

	/**
	 * A list of default values for this object.
	 * 
	 * @return Returns a list of default values (really useful for parameters).
	 */
	public TypedDescription[] getDefaultValues()
	{
		if (fDefaultValues == null)
		{
			return EMPTY_TYPED_DESCRIPTIONS;
		}
		return fDefaultValues.toArray(new TypedDescription[fDefaultValues.size()]);
	}

	/**
	 * Clear known types, this can be removed once the doc return types coming in are stable (eg Math should return
	 * nothing or Math, not default to Object)
	 */
	public void clearDefaultValues()
	{
		if (fDefaultValues != null)
		{
			fDefaultValues.clear();
		}
	}

	/**
	 * Adds a default value to the default value list.
	 * 
	 * @param value
	 *            The full name (including namespaces, if any) of value to add.
	 */
	public void addDefaultValue(TypedDescription value)
	{
		if (value == null)
		{
			return;
		}

		if (fDefaultValues == null)
		{
			fDefaultValues = new ArrayList<TypedDescription>();
		}

		fDefaultValues.add(value);
	}

	/**
	 * Gets the name, if any, that this object represents. This includes param names, or 'return' in the case of a
	 * return type. This can be left empty in the case of memberof or extends.
	 * 
	 * @return Returns the name, if any, that this object represents. This includes param names, or 'return' in the case
	 *         of a return type.
	 */
	public String getName()
	{
		return fName;
	}

	/**
	 * Gets the name, if any, that this object represents. This includes param names, or 'return' in the case of a
	 * return type. This can be left empty in the case of memberof or extends.
	 * 
	 * @param value
	 *            The name, if any, that this object represents.
	 */
	public void setName(String value)
	{
		fName = (value == null) ? "" : value; //$NON-NLS-1$
	}

	/**
	 * Gets the description of this object. This can include simple html.
	 * 
	 * @return Returns the description of this object.
	 */
	public String getDescription()
	{
		return fDescription;
	}

	/**
	 * Sets the description of this object. This can include simple html.
	 * 
	 * @param value
	 *            The description of this object.
	 */
	public void setDescription(String value)
	{
		fDescription = (value == null) ? "" : value; //$NON-NLS-1$
	}

	/**
	 * Read in a binary representation of this object
	 * 
	 * @param input
	 *            The stream to read from
	 * @throws IOException
	 */
	public void read(DataInput input) throws IOException
	{
		int size = input.readInt();
		if (size > 0)
		{
			this.fDefaultValues = new ArrayList<TypedDescription>();

			for (int i = 0; i < size; i++)
			{
				TypedDescription param = new TypedDescription();

				param.read(input);
				this.fDefaultValues.add(param);
			}
		}

		size = input.readInt();
		if (size > 0)
		{
			this.fTypes = new ArrayList<String>();

			for (int i = 0; i < size; i++)
			{
				String type = input.readUTF();

				this.fTypes.add(type);
			}
		}
		this.fDescription = input.readUTF();
		this.fName = input.readUTF();
	}

	/**
	 * Write out a binary representation of this object
	 * 
	 * @param output
	 *            The stream to write to
	 * @throws IOException
	 */
	public void write(DataOutput output) throws IOException
	{
		if (this.fDefaultValues != null)
		{
			output.writeInt(this.fDefaultValues.size());

			for (int i = 0; i < this.fDefaultValues.size(); i++)
			{
				TypedDescription param = (TypedDescription) this.fDefaultValues.get(i);

				param.write(output);
			}
		}
		else
		{
			output.writeInt(0);
		}
		if (this.fTypes != null)
		{
			output.writeInt(this.fTypes.size());

			for (int i = 0; i < this.fTypes.size(); i++)
			{
				output.writeUTF((String) this.fTypes.get(i));
			}
		}
		else
		{
			output.writeInt(0);
		}
		output.writeUTF(this.fDescription);
		output.writeUTF(this.fName);
	}

	/**
	 * @param value
	 * @return Returns the alias type name based on common ScriptDoc usages
	 */
	private String checkForBuiltInAlias(String value)
	{
		if (builtInAliases.containsKey(value))
		{
			return (String) builtInAliases.get(value);
		}
		else
		{
			return value;
		}
	}
}
