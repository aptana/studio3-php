package com.aptana.editor.php.core.model;

import java.util.List;

/**
 * Method.
 * @author Denis Denisenko
 */
public interface IMethod extends IMember
{
	/**
	 * Gets parameters.
	 * @return method parameters.
	 */
	public List<String> getParameters();
	
	/**
	 * Gets direct parameter types (those set in documentation or hints if and only if
	 * parameter has the only direct type).
	 * If some parameter has no direct type, list will contain null at that position.
	 * @return parameter types.
	 */
	public List<String> getDirectParameterTypes();
	
	/**
	 * Gets whether method is constructor.
	 * @return true if method is constructor, false otherwise.
	 */
	boolean isConstructor();

	public int getModifiers();
}
