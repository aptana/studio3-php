/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.indexer;

import gnu.trove.set.hash.THashSet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.indexer.IPHPIndexConstants;

/**
 * PHP entry value for functions.
 * 
 * @author Denis Denisenko
 */
public class FunctionPHPEntryValue extends AbstractPHPEntryValue implements IPHPFunctionEntryValue
{

	private static final String[] NO_STRING_PARAMS = new String[0];
	private static final boolean[] NO_BOOLEAN_PARAMS = new boolean[0];
	private static final Object[] NO_OBJECT_PARAMS = new Object[0];
	private static final int[] NO_INT_PARAMS = new int[0];

	/**
	 * Whether the function is method.
	 */
	private boolean isMethod;

	/**
	 * Whether this function is a method in a trait.
	 */
	private boolean isTraitMethod;

	/**
	 * Parameters names.
	 */
	private String[] parameterNames;

	/**
	 * Parameter types.
	 */
	private Object[] parameterTypes;

	private int[] parameterStartPositions;

	/**
	 * Indicates whether the parameter is a mandatory one.
	 */
	private boolean[] parameterMandatories;

	/**
	 * Function return types. Might be string value in case of direct type value or reference that might be used to
	 * count the type indirectly.
	 */
	private Object returnTypes;

	/**
	 * FunctionPHPEntryValue constructor.
	 * 
	 * @param modifiers
	 *            - modifiers.
	 * @param isMethod
	 *            - if is method.
	 * @param startPosition
	 *            - declaration start position.
	 */
	public FunctionPHPEntryValue(int modifiers, boolean isMethod, int startPosition, String nameSpace)
	{
		super(modifiers, nameSpace);
		this.setStartOffset(startPosition);
		this.isMethod = isMethod;
	}

	/**
	 * FunctionPHPEntryValue constructor.
	 * 
	 * @param modifiers
	 *            - modifiers.
	 * @param isMethod
	 *            - if is method.
	 * @param parameters
	 *            - function parameters.
	 * @param parameterStartPositions
	 *            - start position of each parameter.
	 * @param parameterMandatories
	 *            - boolean array in the size of the parameters that indicates which of the params is a mandatory one.
	 * @param startPosition
	 *            - declaration start position.
	 * @throws IllegalArgumentException
	 *             in any case where the parameterMandatories parameter length is different from the expected.
	 */
	public FunctionPHPEntryValue(int modifiers, boolean isMethod, Map<String, Set<Object>> parameters,
			int[] parameterStartPositions, boolean[] parameterMandatories, int startPosition, String nameSpace)
	{
		super(modifiers, nameSpace);

		if (parameters != null)
		{
			if (parameters.size() != 0)
			{
				if (parameterStartPositions == null || parameters.size() < parameterStartPositions.length)
				{
					// Log the problem. This one may happen due to a duplicate argument name in a function declaration.
					IdeLog.logWarning(
							PHPEditorPlugin.getDefault(),
							MessageFormat
									.format("Illegal parameter start positions: parameters = {0} while the parameterStartPositions = {1}. Check for duplicate arguments in your function declaration.", //$NON-NLS-1$
											parameters, Arrays.toString(parameterStartPositions)), (String) null);
				}
				else if (parameterStartPositions != null && parameters.size() > parameterStartPositions.length)
				{
					// Just log a potential problem. Probably the PHPDoc comment defines an extra parameter that
					// does not exist in the function declaration.
					IdeLog.logWarning(PHPEditorPlugin.getDefault(),
							"An extra parameter is defined in the PHPDoc, but not in the function declaration.", //$NON-NLS-1$
							(String) null);
				}
			}

			this.parameterStartPositions = parameterStartPositions;

			if (parameterMandatories == null || parameters.size() != parameterMandatories.length)
			{
				// IdeLog.log(PHPPlugin.getDefault(), IStatus.WARNING,
				// "Optionals array length is not as expected. Expected " + parameters.size() + " and got "
				// + ((parameterMandatories == null) ? "null" : parameterMandatories.length), null);
				// try to recover from that
				if (parameterMandatories == null)
				{
					parameterMandatories = new boolean[parameters.size()];
				}
				else if (parameters.size() > parameterMandatories.length)
				{
					boolean[] newMandatories = new boolean[parameters.size()];
					System.arraycopy(parameterMandatories, 0, newMandatories, 0, parameterMandatories.length);
					parameterMandatories = newMandatories;
				}
				else
				// parameters.size() < parameterMandatories.length
				{
					// reduce the parameters mandatories size, as this is less important
					boolean[] newMandatories = new boolean[parameters.size()];
					System.arraycopy(parameterMandatories, 0, newMandatories, 0, newMandatories.length);
					parameterMandatories = newMandatories;
				}
			}
			this.parameterMandatories = parameterMandatories;
			parameterNames = new String[parameters.size()];
			parameterTypes = new Object[parameters.size()];

			int i = 0;
			for (Entry<String, Set<Object>> entry : parameters.entrySet())
			{
				parameterNames[i] = entry.getKey();
				Set<Object> types = entry.getValue();
				if (types != null && types.size() != 0)
				{
					if (types.size() == 1)
					{
						Object type = types.iterator().next();
						parameterTypes[i] = type;
					}
					else
					{
						Set<Object> typesToSave = new THashSet<Object>(types.size());
						typesToSave.addAll(types);
						parameterTypes[i] = typesToSave;
					}
				}
				i++;
			}
		}
		this.setStartOffset(startPosition);
		this.isMethod = isMethod;
	}

	public FunctionPHPEntryValue(DataInputStream di) throws IOException
	{
		super(di);
		internalRead(di);
	}

	/**
	 * Sets function return type.
	 * 
	 * @param type
	 *            - might be string value in case of direct type value or reference that might be used to count the type
	 *            indirectly.
	 */
	public void setReturnType(Object type)
	{
		returnTypes = type;
	}

	/**
	 * Sets function return types.
	 * 
	 * @param type
	 *            - might be string value in case of direct type value or reference that might be used to count the type
	 *            indirectly.
	 */
	public void setReturnTypes(Set<Object> types)
	{
		if (types != null && types.size() != 0)
		{
			if (types.size() == 1)
			{
				setReturnType(types.iterator().next());
			}
			else
			{
				Object[] typesArray = new Object[types.size()];
				types.toArray(typesArray);
				this.returnTypes = typesArray;
			}
		}
	}

	/**
	 * Gets return type.
	 * 
	 * @return string value in case of direct type value or reference that might be used to count the type indirectly.
	 *         null means unknown type.
	 */
	public Set<Object> getReturnTypes()
	{
		if (returnTypes == null)
		{
			return Collections.emptySet();
		}

		if (returnTypes instanceof Object[])
		{
			Object[] returnTypesArray = (Object[]) returnTypes;
			Set<Object> result = new THashSet<Object>(returnTypesArray.length);
			for (int i = 0; i < returnTypesArray.length; i++)
			{
				result.add(returnTypesArray[i]);
			}

			return result;
		}
		else
		{
			Set<Object> result = new THashSet<Object>(1);
			result.add(returnTypes);
			return result;
		}
	}

	/**
	 * Returns an array of boolean describing which of the parameters is mandatory and which is optional.
	 * 
	 * @return an array of boolean describing which of the parameters is mandatory and which is optional; Returns an
	 *         empty array in case there are no parameters.
	 */
	public boolean[] getMandatoryParams()
	{
		if (parameterMandatories == null)
		{
			return NO_BOOLEAN_PARAMS;
		}
		boolean[] toReturn = new boolean[parameterMandatories.length];
		System.arraycopy(parameterMandatories, 0, toReturn, 0, parameterMandatories.length);
		return toReturn;
	}

	/**
	 * Gets whether function is method.
	 * 
	 * @return true if method, false otherwise.
	 */
	public boolean isMethod()
	{
		return isMethod;
	}

	/**
	 * Returns true if this function is a method in a trait type.
	 * 
	 * @return <code>true</code> if this function entry is a method in a trait type. Otherwise, return
	 *         <code>false</code> to signal that this is either a regular function, or a method in a class or interface.
	 * @see FunctionPHPEntryValue#isMethod()
	 */
	public boolean isTraitMethod()
	{
		return isTraitMethod;
	}

	/**
	 * Mark this function as a method in a trait type.
	 * 
	 * @param isTraitMethod
	 *            <code>true</code> to set this function as a trait method;
	 */
	public void setIsTraitMethod(boolean isTraitMethod)
	{
		this.isTraitMethod = isTraitMethod;
	}

	/**
	 * Gets function parameters.
	 * 
	 * @return function parameters.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Set<Object>> getParameters()
	{
		if (parameterNames != null)
		{
			Map<String, Set<Object>> result = new LinkedHashMap<String, Set<Object>>(parameterNames.length);
			for (int i = 0; i < parameterNames.length; i++)
			{
				Set<Object> types = new HashSet<Object>();
				Object typeObj = parameterTypes[i];

				if (typeObj != null)
				{
					if (typeObj instanceof Set)
					{
						types.addAll((Set) typeObj);
					}
					else
					{
						types.add(typeObj);
					}
				}
				result.put(parameterNames[i], types);
			}

			return result;
		}

		return Collections.emptyMap();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (isMethod ? 1231 : 1237);
		result = prime * result + (isTraitMethod ? 1231 : 1237);
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (!super.equals(obj))
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		final FunctionPHPEntryValue other = (FunctionPHPEntryValue) obj;
		if (isMethod != other.isMethod || isTraitMethod != other.isTraitMethod)
		{
			return false;
		}
		return true;
	}

	@Override
	public int getKind()
	{
		return IPHPIndexConstants.FUNCTION_CATEGORY;
	}

	/**
	 * Gets parameter start positions.
	 * 
	 * @return parameter start positions.
	 */
	public int[] getParameterStartPositions()
	{
		return parameterStartPositions;
	}

	@Override
	protected void internalWrite(DataOutputStream da) throws IOException
	{
		da.writeBoolean(isMethod);
		da.writeBoolean(isTraitMethod);
		IndexPersistence.writeType(returnTypes, da);
		int len = (parameterNames == null) ? 0 : parameterNames.length;
		da.writeInt(len);
		for (int a = 0; a < len; a++)
		{
			da.writeUTF(parameterNames[a]);
			da.writeBoolean(parameterMandatories[a]);
			da.writeInt(parameterStartPositions[a]);
			IndexPersistence.writeType(parameterTypes[a], da);
		}
	}

	@Override
	protected void internalRead(DataInputStream di) throws IOException
	{
		isMethod = di.readBoolean();
		isTraitMethod = di.readBoolean();
		returnTypes = IndexPersistence.readType(di);
		int pc = di.readInt();
		if (pc > 0)
		{
			parameterNames = new String[pc];
			parameterMandatories = new boolean[pc];
			parameterTypes = new Object[pc];
			parameterStartPositions = new int[pc];
			for (int a = 0; a < pc; a++)
			{
				parameterNames[a] = di.readUTF();
				parameterMandatories[a] = di.readBoolean();
				parameterStartPositions[a] = di.readInt();
				parameterTypes[a] = IndexPersistence.readType(di);
			}
		}
		else
		{
			parameterNames = NO_STRING_PARAMS;
			parameterMandatories = NO_BOOLEAN_PARAMS;
			parameterStartPositions = NO_INT_PARAMS;
			parameterTypes = NO_OBJECT_PARAMS;
		}
	}

}
