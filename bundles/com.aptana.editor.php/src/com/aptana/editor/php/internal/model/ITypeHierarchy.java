/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.model;

import java.io.OutputStream;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import com.aptana.editor.php.core.model.IType;
import com.aptana.editor.php.core.model.ITypeHierarchyChangedListener;

/**
 * A type hierarchy provides navigations between a type and its resolved supertypes and subtypes.
 */
public interface ITypeHierarchy
{

	/**
	 * Adds the given listener for changes to this type hierarchy. Listeners are notified when this type hierarchy
	 * changes and needs to be refreshed. Has no effect if an identical listener is already registered.
	 * 
	 * @param listener
	 *            the listener
	 */
	void addTypeHierarchyChangedListener(ITypeHierarchyChangedListener listener);

	/**
	 * Returns whether the given type is part of this hierarchy.
	 * 
	 * @param type
	 *            the given type
	 * @return true if the given type is part of this hierarchy, false otherwise
	 */
	boolean contains(IType type);

	/**
	 * Returns whether the type and project this hierarchy was created on exist.
	 * 
	 * @return true if the type and project this hierarchy was created on exist, false otherwise
	 */
	boolean exists();

	/**
	 * Returns all classes in this type hierarchy's graph, in no particular order.
	 * 
	 * @return all classes in this type hierarchy's graph
	 */
	List<IType> getAllClasses();

	/**
	 * Returns all resolved subtypes (direct and indirect) of the given type, in no particular order, limited to the
	 * types in this type hierarchy's graph. An empty array is returned if there are no resolved subtypes for the given
	 * type.
	 * 
	 * @param type
	 *            the given type
	 * @return all resolved subtypes (direct and indirect) of the given type
	 */
	List<IType> getAllSubtypes(IType type);

	/**
	 * Returns all resolved superclasses of the given class, in bottom-up order. An empty array is returned if there are
	 * no resolved superclasses for the given class.
	 * 
	 * @param type
	 *            the given type
	 * @return all resolved superclasses of the given class, in bottom-up order, an empty array if none.
	 */
	List<IType> getAllSuperclasses(IType type);

	/**
	 * Returns all resolved supertypes of the given type, in bottom-up order. An empty array is returned if there are no
	 * resolved supertypes for the given type.
	 * 
	 * @param type
	 *            the given type
	 * @return all resolved supertypes of the given class, in bottom-up order, an empty array if none
	 */
	List<IType> getAllSupertypes(IType type);

	/**
	 * Returns all types in this type hierarchy's graph, in no particular order. Any types in the creation region which
	 * were not resolved to have any subtypes or supertypes are not included in the result.
	 * 
	 * @return all types in this type hierarchy's grap
	 */
	List<IType> getAllTypes();

	/**
	 * Return the flags associated with the given type (would be equivalent to <code>IMember.getFlags()</code>), or
	 * <code>-1</code> if this information wasn't cached on the hierarchy during its computation.
	 * 
	 * @param type
	 *            the given type
	 * @return the modifier flags for this member
	 * @see Flags
	 */
	int getCachedFlags(IType type);

	/**
	 * Returns all classes in the graph which have no resolved superclass, in no particular order.
	 * 
	 * @return all classes in the graph which have no resolved superclass
	 */
	List<IType> getRootClasses();

	/**
	 * Returns the direct resolved subclasses of the given class, in no particular order, limited to the classes in this
	 * type hierarchy's graph. Returns an empty collection if the given type is an interface, or if no classes were
	 * resolved to be subclasses of the given class.
	 * 
	 * @param type
	 *            the given type
	 * @return the direct resolved subclasses of the given class limited to the classes in this type hierarchy's graph,
	 *         an empty collection if none.
	 */
	List<IType> getSubclasses(IType type);

	/**
	 * Returns the direct resolved subtypes of the given type, in no particular order, limited to the types in this type
	 * hierarchy's graph. If the type is a class, this returns the resolved subclasses. If the type is an interface,
	 * this returns both the classes which implement the interface and the interfaces which extend it.
	 * 
	 * @param type
	 *            the given type
	 * @return the direct resolved subtypes of the given type limited to the types in this type hierarchy's graph
	 */
	List<IType> getSubtypes(IType type);

	/**
	 * Returns the resolved superclass of the given class, or <code>null</code> if the given class has no superclass,
	 * the superclass could not be resolved, or if the given type is an interface.
	 * 
	 * @param type
	 *            the given type
	 * @return the resolved superclass of the given class, or <code>null</code> if the given class has no superclass,
	 *         the superclass could not be resolved, or if the given type is an interface
	 */
	List<IType> getSuperclass(IType type);

	/**
	 * Returns the resolved supertypes of the given type, in no particular order, limited to the types in this type
	 * hierarchy's graph. For classes, this returns its superclass and the interfaces that the class implements. For
	 * interfaces, this returns the interfaces that the interface extends.
	 * 
	 * @param type
	 *            the given type
	 * @return the resolved supertypes of the given type limited to the types in this type hierarchy's graph
	 */
	List<IType> getSupertypes(IType type);

	/**
	 * Returns the type this hierarchy was computed for. Returns <code>null</code> if this hierarchy was computed for a
	 * region.
	 * 
	 * @return the type this hierarchy was computed for
	 */
	IType getType();

	/**
	 * Re-computes the type hierarchy reporting progress.
	 * 
	 * @param monitor
	 *            the given progress monitor
	 * @exception ModelException
	 *                if unable to refresh the hierarchy
	 */
	void refresh(IProgressMonitor monitor) throws ModelException;

	/**
	 * Removes the given listener from this type hierarchy. Has no affect if an identical listener is not registered.
	 * 
	 * @param listener
	 *            the listener
	 */
	void removeTypeHierarchyChangedListener(ITypeHierarchyChangedListener listener);

	/**
	 * Stores the type hierarchy in an output stream. This stored hierarchy can be load by
	 * IType#loadTypeHierachy(IScriptProject, InputStream, IProgressMonitor). Listeners of this hierarchy are not
	 * stored. Only hierarchies created by the following methods can be store:
	 * <ul>
	 * <li>IType#newSupertypeHierarchy(IProgressMonitor)</li>
	 * <li>IType#newTypeHierarchy(IProgressMonitor)</li> </u>
	 * 
	 * @param outputStream
	 *            output stream where the hierarchy will be stored
	 * @param monitor
	 *            the given progress monitor
	 * @exception ModelException
	 *                if unable to store the hierarchy in the ouput stream
	 * @see IType#loadTypeHierachy(java.io.InputStream, IProgressMonitor)
	 */
	void store(OutputStream outputStream, IProgressMonitor monitor) throws ModelException;
}
