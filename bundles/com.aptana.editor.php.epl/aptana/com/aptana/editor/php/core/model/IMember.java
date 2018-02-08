package com.aptana.editor.php.core.model;

/**
 * Abstract module member.
 * @author Denis Denisenko
 */
public interface IMember extends IModelElement, IParent, ISourceReference
{
	/**
	 * Gets name range.
	 * @return name range.
	 */
	ISourceRange getNameRange();
	
	/**
	 * Gets member flags.
	 * @return member flags.
	 */
	public int getFlags();
	
	/**
	 * Gets a type, member is declared in or null.
	 * @return a type, member is declared in or null.
	 */
	IType getDeclaringType();
	
	/**
	 * Gets source module, element is defined in.
	 * @return source module.
	 */
	ISourceModule getSourceModule();
}
