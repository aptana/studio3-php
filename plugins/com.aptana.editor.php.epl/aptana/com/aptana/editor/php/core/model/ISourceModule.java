package com.aptana.editor.php.core.model;

import java.util.List;

import org.eclipse.core.runtime.CoreException;

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
	 * @return A resource. Either an IResource, IUniformResource or a null.
	 */
	Object getResource();

	/**
	 * Gets module top-level types.
	 * 
	 * @return module top-level types.
	 */
	List<IType> getTopLevelTypes();

	/**
	 * Returns the resource path. In case the resource is in the workspace, a project relative path portable string is
	 * returned. In case the resource is out of the workspace, a URI toString() is returned.
	 * 
	 * @return module path.
	 */
	String getPath();

	List<IMethod> getTopLevelMethods();

	IType getType(String name);

	char[] getSourceAsCharArray() throws CoreException;

}
