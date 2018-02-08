package com.aptana.editor.php.core.model;

import java.util.List;

/**
 * Source model.
 * @author Denis Denisenko
 */
public interface ISourceModel extends IModelElement
{
	/**
	 * Gets source project by name.
	 * @param name - project name.
	 * @return source project.
	 */
	public ISourceProject getProject(String name);
	
	/**
	 * Gets source projects.
	 * @return source projects.
	 */
	public List<ISourceProject> getProjects();
}
