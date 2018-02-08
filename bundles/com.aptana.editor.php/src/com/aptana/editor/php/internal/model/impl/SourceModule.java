/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
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
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org2.eclipse.dltk.internal.core.util.Util;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.resources.IUniformResource;
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
import com.aptana.editor.php.internal.builder.FileSystemModule;
import com.aptana.editor.php.internal.builder.LocalModule;
import com.aptana.editor.php.internal.core.builder.IBuildPath;
import com.aptana.editor.php.internal.core.builder.IModule;
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

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.core.model.IModelElement#getElementType()
	 */
	public int getElementType()
	{
		return MODULE;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.core.model.IParent#getChildren()
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

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.core.model.IParent#hasChildren()
	 */
	public boolean hasChildren()
	{
		return getChildren().size() > 0;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.core.model.ISourceModule#getResource()
	 */
	public Object getResource()
	{
		IModule module = getModule();
		if (module instanceof LocalModule)
		{
			return ((LocalModule) module).getFile();
		}
		else if (module instanceof FileSystemModule)
		{
			FileSystemModule fsm = (FileSystemModule) module;
			// In case this 'FileSystemModule' is actually pointing to a location in the workspace, which is not under a
			// PHP project, we return an IFile for the path of this resource so that the error annotations will work on
			// it (see https://aptana.lighthouseapp.com/projects/35272-studio/tickets/1346)
			if (fsm.isInWorkspace())
			{
				IPath iPath = Path.fromOSString(fsm.getFullPath());
				return ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(iPath);
			}
			return fsm.getExternalFile();
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

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.core.model.ISourceModule#getTopLevelTypes()
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

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.core.model.ISourceModule#getPath()
	 */
	public String getPath()
	{
		Object resource = getResource();
		if (resource == null)
		{
			return null;
		}
		if (resource instanceof IResource)
		{
			return ((IResource) resource).getProjectRelativePath().toPortableString();
		}
		else
		{
			return ((IUniformResource) resource).getURI().toString();
		}

	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.core.model.ISourceModule#getTopLevelMethods()
	 */
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

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.core.model.ISourceModule#getType(java.lang.String)
	 */
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

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.internal.model.impl.AbstractModelElement#getElementInfo()
	 */
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

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.core.model.ISourceModule#getSourceAsCharArray()
	 */
	public char[] getSourceAsCharArray() throws CoreException
	{
		Object resource = getResource();
		IFile file = null;
		if (resource instanceof IFile)
		{
			file = (IFile) resource;
		}
		if (file == null)
		{
			File f = new File(getModule().getFullPath());
			if (!f.exists())
			{
				throw new IllegalStateException("Source module resource was null"); //$NON-NLS-1$
			}
			try
			{
				return IOUtil.read(new FileInputStream(f)).toCharArray(); // $codepro.audit.disable closeWhereCreated
			}
			catch (FileNotFoundException e)
			{
				throw new CoreException(new Status(IStatus.ERROR, PHPEditorPlugin.PLUGIN_ID,
						"Error reading the file's content", e)); //$NON-NLS-1$
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
			IdeLog.logError(PHPEditorPlugin.getDefault(),
					"Error getting a source content as char-array", e, PHPEditorPlugin.DEBUG_SCOPE); //$NON-NLS-1$
		}
		return EMPTY_CONTENT;
	}
}
