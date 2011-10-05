/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.indexer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Memory-optimized call path like $a->method()->$b->$c->method2()
 * 
 * @author Denis Denisenko
 */
public class CallPath
{
	/**
	 * Path entry. May be either VariableEntry or MethodEntry
	 * 
	 * @author Denis Denisenko
	 */
	public abstract static class Entry // $codepro.audit.disable noAbstractMethods
	{
		/**
		 * Entry name.
		 */
		private String name;

		/**
		 * Entry constructor.
		 * 
		 * @param name
		 *            - entry name.
		 */
		protected Entry(String name)
		{
			this.name = name;
		}

		/**
		 * Gets entry name
		 * 
		 * @return name.
		 */
		public String getName()
		{
			return name;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final Entry other = (Entry) obj;
			if (name == null)
			{
				if (other.name != null)
					return false;
			}
			else if (!name.equals(other.name))
				return false;
			return true;
		}
	}

	/**
	 * Variable entry.
	 * 
	 * @author Denis Denisenko
	 */
	public final static class VariableEntry extends Entry
	{

		/**
		 * VariableEntry constructor.
		 * 
		 * @param name
		 *            - variable name.
		 */
		public VariableEntry(String name)
		{
			super(name);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString()
		{
			return "var: " + getName(); //$NON-NLS-1$
		}
	}

	/**
	 * Class entry.
	 * 
	 * @author Denis Denisenko
	 */
	public final static class ClassEntry extends Entry
	{

		/**
		 * ClassEntry constructor.
		 * 
		 * @param name
		 *            - class name.
		 */
		public ClassEntry(String name)
		{
			super(name);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString()
		{
			return "class: " + getName(); //$NON-NLS-1$
		}
	}

	/**
	 * Method entry.
	 * 
	 * @author Denis Denisenko
	 */
	public final static class MethodEntry extends Entry
	{

		/**
		 * MethodEntry constructor.
		 * 
		 * @param name
		 *            - variable name.
		 */
		public MethodEntry(String name)
		{
			super(name);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString()
		{
			return "func: " + getName(); //$NON-NLS-1$
		}
	}

	/**
	 * Masks for bit access.
	 */
	private final static int[] _masks = { 0x80000000, 0x40000000, 0x20000000, 0x10000000, 0x08000000, 0x04000000,
			0x02000000, 0x01000000, 0x00800000, 0x00400000, 0x00200000, 0x00100000, 0x00080000, 0x00040000, 0x00020000,
			0x00010000, 0x00008000, 0x00004000, 0x00002000, 0x00001000, 0x00000800, 0x00000400, 0x00000200, 0x00000100,
			0x00000080, 0x00000040, 0x00000020, 0x00000010, 0x00000008, 0x00000004, 0x00000002, 0x00000001 };

	/**
	 * Entry names.
	 */
	private List<String> names = new ArrayList<String>(1);

	/**
	 * Entry types.
	 */
	private int types;

	/**
	 * Whether first entry is of a ClassEntry type.
	 */
	private boolean firstEntryOfClassType = false;

	/**
	 * Almost never used. Is required for the paths with the length greater then 31.
	 */
	private List<Boolean> typesArray = null;

	/**
	 * Gets size.
	 * 
	 * @return path size.
	 */
	public int getSize()
	{
		return names.size();
	}

	/**
	 * Sets first entry as a class entry.
	 * 
	 * @param name
	 *            - class name.
	 */
	public void setClassEntry(String name)
	{
		if (names.size() != 0)
		{
			throw new IllegalArgumentException("Only the first entry might have Class type"); //$NON-NLS-1$
		}

		firstEntryOfClassType = true;

		names.add(name);
	}

	/**
	 * Adds variable entry to the right of the path.
	 * 
	 * @param name
	 *            - variable name.
	 */
	public void addVariableEntry(String name)
	{
		if (names.size() == 31)
		{
			typesArray = new ArrayList<Boolean>(32);
			typesArray.add(true);
		}
		else if (names.size() > 31)
		{
			typesArray.add(true);
		}
		else
		{
			// sets the appropriate bit to 1
			types |= _masks[names.size()];
		}

		names.add(name);
	}

	/**
	 * Adds method entry to the right of the path.
	 * 
	 * @param name
	 *            - method name.
	 */
	public void addMethodEntry(String name)
	{
		if (names.size() == 31)
		{
			typesArray = new ArrayList<Boolean>(32);
			typesArray.add(false);
		}
		else if (names.size() > 31)
		{
			typesArray.add(false);
		}
		// else - appropriate bit is already 0 so doing nothing

		names.add(name);
	}

	/**
	 * Adds the specified path as a tail to the current path.
	 * 
	 * @param path
	 *            - path to add.
	 */
	public void addPath(CallPath path)
	{
		for (CallPath.Entry entry : path.getEntries())
		{
			if (entry instanceof ClassEntry)
			{
				throw new IllegalArgumentException(
						"Class entry must start the path, so path containing Class entry can not be added."); //$NON-NLS-1$
			}
			else if (entry instanceof MethodEntry)
			{
				addMethodEntry(entry.getName());
			}
			else if (entry instanceof VariableEntry)
			{
				addVariableEntry(entry.getName());
			}
			else
			{
				throw new IllegalArgumentException("Unknown entry type: " + entry.getClass().getCanonicalName()); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Inserts variable entry to the beginning of the path.
	 * 
	 * @param name
	 *            - variable name.
	 */
	public void insertVariableEntry(String name)
	{
		if (names.size() == 31)
		{
			typesArray = new ArrayList<Boolean>(32);
			typesArray.add(0, true);
		}
		else if (names.size() > 31)
		{
			typesArray.add(0, true);
		}
		else
		{
			// shifting types
			types = types >>> 1;
			// sets the appropriate bit to 1
			types |= _masks[0];
		}

		names.add(0, name);
	}

	/**
	 * Inserts method entry to the beginning of the path.
	 * 
	 * @param name
	 *            - method name.
	 */
	public void insertMethodEntry(String name)
	{
		if (names.size() == 31)
		{
			typesArray = new ArrayList<Boolean>(32);
			typesArray.add(0, false);
		}
		else if (names.size() > 31)
		{
			typesArray.add(0, false);
		}
		else
		{
			// shifting types
			types = types >>> 1;
			// appropriate bit is already 0 so setting nothing
		}

		names.add(0, name);
	}

	/**
	 * Returns sub path.
	 * 
	 * @param start
	 *            - index of sub path start.
	 * @return sub path.
	 */
	public CallPath subPath(int start)
	{
		if (start >= names.size())
		{
			return new CallPath();
		}

		CallPath result = new CallPath();
		List<Entry> entries = getEntries();
		for (int i = start; i < entries.size(); i++)
		{
			Entry entry = entries.get(i);
			if (entry instanceof VariableEntry)
			{
				result.addVariableEntry(entry.getName());
			}
			else
			{
				result.addMethodEntry(entry.getName());
			}
		}

		return result;
	}

	/**
	 * Gets entries.
	 * 
	 * @return entries.
	 */
	public List<Entry> getEntries()
	{
		List<Entry> result = new ArrayList<Entry>(names.size());

		for (int i = 0; i < names.size(); i++)
		{
			if (i == 0 && firstEntryOfClassType)
			{
				result.add(new ClassEntry(names.get(i)));
			}
			else
			{
				if (getType(i))
				{
					result.add(new VariableEntry(names.get(i)));
				}
				else
				{
					result.add(new MethodEntry(names.get(i)));
				}
			}
		}

		return result;
	}

	/**
	 * Gets type for the position.
	 * 
	 * @param i
	 *            - position.
	 * @return true if variable, false if method.
	 */
	private boolean getType(int i)
	{
		if (names.size() > 31)
		{
			return typesArray.get(i);
		}
		else
		{
			return (types & _masks[i]) != 0;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString()
	{
		return getEntries().toString();
	}

	/**
	 * Gets whether current path is equal to the other path.
	 * 
	 * @param other
	 *            - path to compare to.
	 * @return
	 */
	public boolean compare(CallPath other)
	{
		if (names.size() != other.names.size())
		{
			return false;
		}

		if (typesArray != null)
		{
			if (other.typesArray == null)
			{
				return false;
			}

			if (!typesArray.equals(other.typesArray))
			{
				return false;
			}
		}
		else
		{
			if (types != other.types)
			{
				return false;
			}
		}

		if (!names.equals(other.names))
		{
			return false;
		}

		return true;
	}

	public void write(DataOutputStream da) throws IOException
	{
		int size = names.size();
		da.writeInt(size);
		da.writeInt(types);
		da.writeBoolean(firstEntryOfClassType);
		for (String s : names)
		{
			da.writeUTF(s);
		}
		da.writeBoolean(typesArray != null);
		if (typesArray != null)
		{
			da.writeInt(typesArray.size());
			for (Boolean b : typesArray)
			{
				da.writeBoolean(b);
			}
		}
	}

	public CallPath()
	{

	}

	public CallPath(DataInputStream di) throws IOException
	{
		int readInt = di.readInt();
		names = new ArrayList<String>(readInt);
		types = di.readInt();
		firstEntryOfClassType = di.readBoolean();
		for (int a = 0; a < readInt; a++)
		{
			names.add(di.readUTF().intern());
		}
		if (di.readBoolean())
		{
			int sz = di.readInt();
			typesArray = new ArrayList<Boolean>(sz);
			for (int a = 0; a < sz; a++)
			{
				typesArray.add(di.readBoolean());
			}
		}
	}
}
