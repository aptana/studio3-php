package com.aptana.editor.php.core.model;

import java.util.List;
import org.eclipse.core.resources.IResource;

/**
 * Module.
 * 
 * @author Denis Denisenko
 */
public interface ISourceModule extends IModelElement, IParent
{
	/**
	 * Gets resource if available.
	 * 
	 * @return resource.
	 */
	IResource getResource();

	/**
	 * Gets module top-level types.
	 * 
	 * @return module top-level types.
	 */
	List<IType> getTopLevelTypes();

	/**
	 * Gets module path.
	 * 
	 * @return module path.
	 */
	String getPath();

	List<IMethod> getTopLevelMethods();

	IType getType(String name);

	char[] getSourceAsCharArray();

}
