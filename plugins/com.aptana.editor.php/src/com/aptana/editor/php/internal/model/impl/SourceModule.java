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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.dltk.internal.core.util.Util;

import com.aptana.core.util.IOUtil;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.core.model.IMethod;
import com.aptana.editor.php.core.model.IModelElement;
import com.aptana.editor.php.core.model.ISourceModule;
import com.aptana.editor.php.core.model.IType;
import com.aptana.editor.php.core.model.env.ModelElementInfo;
import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.indexer.IElementsIndex;
import com.aptana.editor.php.indexer.PHPGlobalIndexer;
import com.aptana.editor.php.internal.builder.IBuildPath;
import com.aptana.editor.php.internal.builder.IModule;
import com.aptana.editor.php.internal.builder.LocalModule;
import com.aptana.editor.php.internal.model.utils.ModelUtils;

/**
 * SourceModule
 * 
 * @author Denis Denisenko
 */
public class SourceModule extends AbstractResourceElement implements ISourceModule
{

	private static final char[] EMPTY_CONTENT = new char[0];

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
		// Collect the methods. We limit the depth to 3, so we don't collect too much.
		getMethodsRecursively(chidren, result, 3);
		return result;
	}

	private void getMethodsRecursively(List<IModelElement> chidren, List<IMethod> result, int depth)
	{
		for (IModelElement child : chidren)
		{
			if (child instanceof IMethod)
			{
				result.add((IMethod) child);
			}
			else if (child instanceof IType)
			{
				if (depth > 0)
				{
					IType type = (IType) child;
					getMethodsRecursively(type.getChildren(), result, --depth);
				}
			}
		}
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

	@Override
	public char[] getSourceAsCharArray() throws CoreException
	{
		IFile file = (IFile) getResource();
		if (file == null)
		{
			File f = new File(getModule().getFullPath());
			if (!f.exists()) {
				throw new IllegalStateException("Source module resource was null"); //$NON-NLS-1$
			}
			try
			{
				return IOUtil.read(new FileInputStream(f)).toCharArray();
			}
			catch (FileNotFoundException e)
			{
				throw new CoreException(new Status(IStatus.ERROR, PHPEditorPlugin.PLUGIN_ID, "Error reading the file's content", e));  //$NON-NLS-1$
			}
		}
		if (!file.exists())
		{
			throw new IllegalStateException("Source module resource does not exist"); //$NON-NLS-1$
		}
		try
		{
			return Util.getResourceContentsAsCharArray(file, file.getCharset());
		}
		catch (CoreException e)
		{
			PHPEditorPlugin.logError(e);
		}
		return EMPTY_CONTENT;
	}
}
