/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.model.impl;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;

import com.aptana.editor.php.core.model.ISourceModule;
import com.aptana.editor.php.core.model.IType;
import com.aptana.editor.php.core.model.ITypeHierarchyChangedListener;
import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.indexer.IElementsIndex;
import com.aptana.editor.php.indexer.IModuleIndexListener;
import com.aptana.editor.php.indexer.IPHPIndexConstants;
import com.aptana.editor.php.indexer.PHPGlobalIndexer;
import com.aptana.editor.php.internal.core.builder.IDirectory;
import com.aptana.editor.php.internal.core.builder.IModule;
import com.aptana.editor.php.internal.model.ITypeHierarchy;
import com.aptana.editor.php.internal.model.utils.ModelUtils;
import com.aptana.editor.php.internal.model.utils.TypeHierarchyUtils;

/**
 * Global type hierarchy based on php index. This type hierarchy is not based on a single type, but instead is dynamic
 * and valid for the whole workspace.
 * 
 * @author Denis Denisenko
 */
public class IndexBasedTypeHierarchy implements ITypeHierarchy
{
	/**
	 * Listeners.
	 */
	private Set<ITypeHierarchyChangedListener> listeners = new HashSet<ITypeHierarchyChangedListener>();

	/**
	 * Module index listener.
	 */
	private IModuleIndexListener moduleIndexListener = null;

	/**
	 * Base type.
	 */
	private IType baseType;

	/**
	 * {@inheritDoc}
	 */
	public void addTypeHierarchyChangedListener(ITypeHierarchyChangedListener listener)
	{
		synchronized (listeners)
		{
			listeners.add(listener);
			ensureModelListenerCreated();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean contains(IType type)
	{
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean exists()
	{
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IType> getAllClasses()
	{
		IElementsIndex index = PHPGlobalIndexer.getInstance().getIndex();
		List<IElementEntry> entries = index.getEntriesStartingWith(IPHPIndexConstants.CLASS_CATEGORY, ""); //$NON-NLS-1$
		return ModelUtils.convertTypes(entries);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IType> getAllSubtypes(IType type)
	{
		// TODO - Shalom: add namespace and aliases support into IType
		IElementEntry typeEntry = getTypeEntry(type);
		List<IElementEntry> classDescendants = TypeHierarchyUtils.getClassDescendants(typeEntry.getModule(),
				typeEntry.getEntryPath(), PHPGlobalIndexer.getInstance().getIndex(), null, null);
		return ModelUtils.convertTypes(classDescendants);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IType> getAllSuperclasses(IType type)
	{
		// TODO - Shalom: add namespace and aliases support into IType
		IElementEntry typeEntry = getTypeEntry(type);
		List<IElementEntry> classAncestors = TypeHierarchyUtils.getClassAncestors(typeEntry.getModule(),
				typeEntry.getEntryPath(), PHPGlobalIndexer.getInstance().getIndex(), null, null);
		return ModelUtils.convertClasses(classAncestors);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IType> getAllSupertypes(IType type)
	{
		// TODO - Shalom: add namespace and aliases support into IType
		IElementEntry typeEntry = getTypeEntry(type);
		List<IElementEntry> classAncestors = TypeHierarchyUtils.getClassAncestors(typeEntry.getModule(),
				typeEntry.getEntryPath(), PHPGlobalIndexer.getInstance().getIndex(), null, null);
		return ModelUtils.convertTypes(classAncestors);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IType> getAllTypes()
	{
		IElementsIndex index = PHPGlobalIndexer.getInstance().getIndex();
		List<IElementEntry> entries = index.getEntriesStartingWith(IPHPIndexConstants.CLASS_CATEGORY, ""); //$NON-NLS-1$
		return ModelUtils.convertTypes(entries);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getCachedFlags(IType type)
	{
		return type.getFlags();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IType> getRootClasses()
	{
		if (baseType == null)
		{
			return Collections.emptyList();
		}

		Set<IType> result = new HashSet<IType>();
		addRootClassesRecursively(baseType, result);

		List<IType> toReturn = new ArrayList<IType>(result.size());
		toReturn.addAll(result);
		return toReturn;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IType> getSubclasses(IType type)
	{
		IElementEntry typeEntry = getTypeEntry(type);
		List<IElementEntry> classDescendants = TypeHierarchyUtils.getDirectClassDescendants(typeEntry.getModule(),
				typeEntry.getEntryPath(), PHPGlobalIndexer.getInstance().getIndex());
		return ModelUtils.convertClasses(classDescendants);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IType> getSubtypes(IType type)
	{
		IElementEntry typeEntry = getTypeEntry(type);
		List<IElementEntry> classDescendants = TypeHierarchyUtils.getDirectClassDescendants(typeEntry.getModule(),
				typeEntry.getEntryPath(), PHPGlobalIndexer.getInstance().getIndex());
		return ModelUtils.convertTypes(classDescendants);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IType> getSuperclass(IType type)
	{
		IElementEntry typeEntry = getTypeEntry(type);
		List<IElementEntry> classAncestors = TypeHierarchyUtils.getDirectClassAncestors(typeEntry.getModule(),
				typeEntry.getEntryPath(), PHPGlobalIndexer.getInstance().getIndex());
		return ModelUtils.convertClasses(classAncestors);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IType> getSupertypes(IType type)
	{
		IElementEntry typeEntry = getTypeEntry(type);
		List<IElementEntry> classAncestors = TypeHierarchyUtils.getDirectClassAncestors(typeEntry.getModule(),
				typeEntry.getEntryPath(), PHPGlobalIndexer.getInstance().getIndex());
		return ModelUtils.convertTypes(classAncestors);
	}

	/**
	 * {@inheritDoc}
	 */
	public IType getType()
	{
		return baseType;
	}

	/**
	 * {@inheritDoc}
	 */
	public void refresh(IProgressMonitor monitor)
	{
		monitor.done();
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeTypeHierarchyChangedListener(ITypeHierarchyChangedListener listener)
	{
		synchronized (listeners)
		{
			listeners.remove(listener);
			removeModelListenerIfNeeded();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void store(OutputStream outputStream, IProgressMonitor monitor)
	{
		monitor.done();
	}

	/**
	 * Sets base type.
	 * 
	 * @param type
	 *            - type.
	 */
	public void setType(IType type)
	{
		this.baseType = type;
	}

	/**
	 * Gets type entry.
	 * 
	 * @param type
	 *            - type.
	 * @return type entry.
	 */
	private IElementEntry getTypeEntry(IType type)
	{
		return ((EntryBasedType) type).getEntry();
	}

	/**
	 * Notifies hierarchy changed.
	 * 
	 * @param changed
	 *            - changed types.
	 */
	private void notifyChanged(Set<IType> changed)
	{
		Set<ITypeHierarchyChangedListener> listenersCopy = new HashSet<ITypeHierarchyChangedListener>();
		synchronized (listeners)
		{
			listenersCopy.addAll(listeners);
		}

		for (ITypeHierarchyChangedListener listener : listenersCopy)
		{
			listener.typeHierarchyChanged(changed);
		}
	}

	/**
	 * Ensures model listener is created.
	 */
	private void ensureModelListenerCreated()
	{
		if (moduleIndexListener == null)
		{
			moduleIndexListener = new IModuleIndexListener()
			{
				public void afterIndexChange(List<IModule> added, List<IModule> changed,
						List<IDirectory> addedDirectories)
				{
					Set<IType> changedTypes = new HashSet<IType>();

					for (IModule module : changed)
					{
						ISourceModule sourceModule = ModelUtils.convertModule(module);
						if (sourceModule != null)
						{
							changedTypes.addAll(sourceModule.getTopLevelTypes());
						}
					}

					for (IModule module : added)
					{
						ISourceModule sourceModule = ModelUtils.convertModule(module);
						if (sourceModule != null)
						{
							changedTypes.addAll(sourceModule.getTopLevelTypes());
						}
					}

					notifyChanged(changedTypes);
				}

				public void beforeIndexChange(List<IModule> changed, List<IModule> removed,
						List<IDirectory> removedDirectories)
				{
					Set<IType> changedTypes = new HashSet<IType>();

					for (IModule module : changed)
					{
						ISourceModule sourceModule = ModelUtils.convertModule(module);
						if (sourceModule != null)
						{
							changedTypes.addAll(sourceModule.getTopLevelTypes());
						}
					}

					for (IModule module : removed)
					{
						ISourceModule sourceModule = ModelUtils.convertModule(module);
						if (sourceModule != null)
						{
							changedTypes.addAll(sourceModule.getTopLevelTypes());
						}
					}

					notifyChanged(changedTypes);
				}
			};

			PHPGlobalIndexer.getInstance().addListener(moduleIndexListener);
		}
	}

	/**
	 * Removes model listener if needed.
	 */
	private void removeModelListenerIfNeeded()
	{
		if (listeners.size() == 0)
		{
			PHPGlobalIndexer.getInstance().removeListener(moduleIndexListener);
			moduleIndexListener = null;
		}
	}

	/**
	 * Adds root classes and interfaces recursivelly.
	 * 
	 * @param type
	 *            - type to check.
	 * @param result
	 *            - result.
	 */
	private void addRootClassesRecursively(IType type, Set<IType> result)
	{
		List<IType> superTypes = type.getSuperTypes();

		// if no ancestors found, adding this root type to the result list
		if (superTypes == null || superTypes.size() == 0)
		{
			result.add(type);
			return;
		}

		for (IType superType : superTypes)
		{
			addRootClassesRecursively(superType, result);
		}
	}
}
