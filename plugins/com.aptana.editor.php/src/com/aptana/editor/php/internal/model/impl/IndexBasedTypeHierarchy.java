/**
 * This file Copyright (c) 2005-2008 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain other free and open source software ("FOSS") code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
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

import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.indexer.IElementsIndex;
import com.aptana.editor.php.indexer.IModuleIndexListener;
import com.aptana.editor.php.indexer.IPHPIndexConstants;
import com.aptana.editor.php.indexer.PHPGlobalIndexer;
import com.aptana.editor.php.internal.builder.IDirectory;
import com.aptana.editor.php.internal.builder.IModule;
import com.aptana.editor.php.internal.model.ITypeHierarchy;
import com.aptana.editor.php.internal.model.ModelException;
import com.aptana.editor.php.internal.model.utils.ModelUtils;
import com.aptana.editor.php.internal.model.utils.TypeHierarchyUtils;
import com.aptana.editor.php.model.ISourceModule;
import com.aptana.editor.php.model.IType;
import com.aptana.editor.php.model.ITypeHierarchyChangedListener;

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
		IElementEntry typeEntry = getTypeEntry(type);
		List<IElementEntry> classDescendants = TypeHierarchyUtils.getClassDescendants(typeEntry.getModule(), typeEntry
				.getEntryPath(), PHPGlobalIndexer.getInstance().getIndex());
		return ModelUtils.convertTypes(classDescendants);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IType> getAllSuperclasses(IType type)
	{
		IElementEntry typeEntry = getTypeEntry(type);
		List<IElementEntry> classAncestors = TypeHierarchyUtils.getClassAncestors(typeEntry.getModule(), typeEntry
				.getEntryPath(), PHPGlobalIndexer.getInstance().getIndex());
		return ModelUtils.convertClasses(classAncestors);
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IType> getAllSupertypes(IType type)
	{
		IElementEntry typeEntry = getTypeEntry(type);
		List<IElementEntry> classAncestors = TypeHierarchyUtils.getClassAncestors(typeEntry.getModule(), typeEntry
				.getEntryPath(), PHPGlobalIndexer.getInstance().getIndex());
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
	public void refresh(IProgressMonitor monitor) throws ModelException
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
	public void store(OutputStream outputStream, IProgressMonitor monitor) throws ModelException
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
