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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;

import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.indexer.IElementsIndex;
import com.aptana.editor.php.indexer.PHPGlobalIndexer;
import com.aptana.editor.php.internal.builder.IBuildPath;
import com.aptana.editor.php.internal.builder.IModule;
import com.aptana.editor.php.internal.builder.LocalModule;
import com.aptana.editor.php.internal.model.utils.ModelUtils;
import com.aptana.editor.php.model.IMethod;
import com.aptana.editor.php.model.IModelElement;
import com.aptana.editor.php.model.ISourceModule;
import com.aptana.editor.php.model.IType;
import com.aptana.editor.php.model.env.ModelElementInfo;

/**
 * SourceModule
 * 
 * @author Denis Denisenko
 */
public class SourceModule extends AbstractResourceElement implements ISourceModule
{

	/**
	 * SourceModule constructor.
	 * 
	 * @param module
	 */
	public SourceModule(IModule module)
	{
		super(module);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getElementType()
	{
		return MODULE;
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IModelElement> getChildren()
	{
		IElementsIndex index = PHPGlobalIndexer.getInstance().getIndex();
		List<IElementEntry> entries = index.getModuleEntries(getModule());

		List<IModelElement> result = new ArrayList<IModelElement>();

		for (IElementEntry entry : entries)
		{
			String pathStr = entry.getEntryPath();
			if (pathStr == null)
			{
				continue;
			}

			if (pathStr.contains(Character.toString(IElementsIndex.DELIMITER)))
			{
				continue;
			}

			IModelElement element = ModelUtils.convertEntry(entry);
			if (element != null)
			{
				result.add(element);
			}
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasChildren()
	{
		return getChildren().size() > 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public org.eclipse.core.resources.IResource getResource()
	{
		if (getModule() instanceof LocalModule)
		{
			return ((LocalModule) getModule()).getFile();
		}

		return null;
	}

	/**
	 * Gets internal module.
	 * 
	 * @return module.
	 */
	protected IModule getModule()
	{
		return (IModule) getBPResource();
	}

	/**
	 * {@inheritDoc}
	 */
	public List<IType> getTopLevelTypes()
	{
		// TODO support non-top-level types when such a support is implemented in PHP indexer.
		List<IModelElement> chidren = getChildren();

		List<IType> result = new ArrayList<IType>();

		for (IModelElement child : chidren)
		{
			if (child instanceof IType)
			{
				result.add((IType) child);
			}
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getPath()
	{
		IResource resource = getResource();
		if (resource == null)
		{
			return null;
		}

		return resource.getProjectRelativePath().toPortableString();
	}

	public List<IMethod> getTopLevelMethods()
	{
		List<IModelElement> chidren = getChildren();

		List<IMethod> result = new ArrayList<IMethod>();

		for (IModelElement child : chidren)
		{
			if (child instanceof IMethod)
			{
				result.add((IMethod) child);
			}
		}
		return result;
	}

	public IType getType(String name)
	{
		for (IType t : getTopLevelTypes())
		{
			if (t.getElementName().equals(name))
			{
				return t;
			}
		}
		return null;
	}

	public ISourceModule getModule(String include)
	{
		Path path = new Path(include);
		IModule module = getModule();
		IBuildPath buildPath = module.getBuildPath();
		IModule includedModule = buildPath.resolveRelativePath(module, path);
		if (includedModule != null)
		{
			return ModelUtils.convertModule(includedModule);
		}
		return null;
	}

	@Override
	public ModelElementInfo getElementInfo()
	{
		ModelElementInfo info = new ModelElementInfo();
		List<IModelElement> children = getChildren();

		if (children != null && children.size() != 0)
		{
			info.setChildren(children);
		}

		return info;
	}
}
