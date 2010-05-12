package com.aptana.editor.php.core.model;

import java.util.List;

/**
 * Type.
 * @author Denis Denisenko
 */
public interface IType extends IMember
{
	/**
	 * Gets direct type super-classes.
	 * @return type super-classes.
	 */
	List<IType> getSuperClasses();
	
	/**
	 * Gets type interfaces.
	 * @return type interfaces.
	 */
	List<IType> getInterfaces();
	
	/**
	 * Gets both super-classes and interfaces.
	 * @return supertypes.
	 */
	List<IType> getSuperTypes();
	
	/**
	 * Gets direct type super class names.
	 * @return type super class names.
	 */
	List<String> getSuperClassNames();
	
	/**
	 * Gets type interface names.
	 * @return type interface names.
	 */
	List<String> getInterfaceNames();
	
	/**
	 * Gets type fields.
	 * @return type fields.
	 */
	List<IField> getFields();
	
	/**
	 * Gets type field by name.
	 * @param fieldName - field name.
	 * @return field.
	 */
	IField getField(String fieldName);
	
	/**
	 * Gets type methods.
	 * @return type methods.
	 */
	List<IMethod> getMethods();
	
	/**
	 * Gets type methods having the name specified.
	 * @param methodName - method name.
	 * @return type methods having the name specified.
	 */
	List<IMethod> getMethods(String methodName);
	
	/**
	 * Whether the type is interface.
	 * @return whether the type is interface.
	 */
	boolean isInterface();
}
