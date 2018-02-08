package com.aptana.editor.php.core.model;

/**
 * Abstract model element.
 * @author Denis Denisenko
 */
public interface IModelElement
{
	/**
	 * Source model.
	 */
	int MODEL = 1;
	
	/**
	 * Constant representing a  project.
	 * An element with this type can be safely cast to <code>ISourceProject</code>.
	 */
	int PROJECT = 2;

	/**
	 * Constant representing a folder.
	 * An element with this type can be safely cast to <code>ISourceFolder</code>.
	 */
	int FOLDER = 4;
	
	/**
	 * Constant representing a source module.
	 * An element with this type can be safely cast to <code>ISourceModule</code>.
	 */
	int MODULE = 5;
	
	/**
	 * Constant representing a type (a class or interface).
	 * An element with this type can be safely cast to <code>IType</code>.
	 */
	int TYPE = 7;
	
	/**
	 * Constant representing a field or variable.
	 * An element with this type can be safely cast to <code>IField</code>.
	 */
	int FIELD = 8;

	/**
	 * Constant representing a method or procedure.
	 * An element with this type can be safely cast to <code>IMethod</code>.
	 */
	int METHOD = 9;
	
	/**
	 * Gets element type.
	 * @return element type code.
	 */
	int getElementType();
	
	/**
	 * Whether the element exists.
	 * @return whether the element exists.
	 */
	boolean exists();
	
	/**
	 * Gets element parent.
	 * @return element parent.
	 */
	IModelElement getParent();
	
	/**
	 * Gets the name of this element.
	 *
	 * @return the element name
	 */
	String getElementName();
	
	/**
	 * Returns the first ancestor of this script element that has the given type.
	 * Returns <code>null</code> if no such an ancestor can be found.
	 * This is a handle-only method.
	 * 
	 * @param ancestorType the given type
	 * @return the first ancestor of this script element that has the given type, null if no such an ancestor can be found
	 *
	 */
	IModelElement getAncestor(int ancestorType);
	
	/**
	 * Gets element source project.
	 * @return source project.
	 */
	ISourceProject getSourceProject();
	
	/**
	 * Accepts model element.
	 * @param visitor - visitor to accept.
	 */
	void accept(IModelElementVisitor visitor );
	
	ISourceModel getModel();
}
