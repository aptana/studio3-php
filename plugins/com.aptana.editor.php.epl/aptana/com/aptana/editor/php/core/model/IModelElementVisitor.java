package com.aptana.editor.php.core.model;

/**
 * Model element visitor.
 * @author Denis Denisenko
 */
public interface IModelElementVisitor
{
	/**
	 * Visits model element.
	 * @param element - element visited.
	 * @return true if to visit element children, false otherwise.
	 */
	boolean visit(IModelElement element);
}
