package com.aptana.editor.php.core.model;

import org.eclipse.core.resources.IProject;

/**
 * A source project interface.
 * 
 * @author Denis Denisenko, Shalom Gibly
 */
public interface ISourceProject extends IModelElement, IParent
{
	/**
	 * Returns the project associated to this ISourceProject instance.
	 */
	public IProject getProject();
}
