package com.aptana.editor.php.core.model;

import java.util.List;

/**
 * Abstract element that is a parent of other elements.
 */
public interface IParent {
	
	/**
	 * Gets element children.
	 * @return
	 */
	List<IModelElement> getChildren();
	
	/**
	 * Gets whether element has children.
	 * @return whether element has children.
	 */
	boolean hasChildren();
}
