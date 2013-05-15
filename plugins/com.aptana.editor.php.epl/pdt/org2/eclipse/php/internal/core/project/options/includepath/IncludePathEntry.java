/*******************************************************************************
 * Copyright (c) 2006 Zend Corporation and IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org2.eclipse.php.internal.core.project.options.includepath;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.preferences.IWorkingCopyManager;
import org.w3c.dom.Element;
import org2.eclipse.php.internal.core.CoreMessages;
import org2.eclipse.php.internal.core.project.IIncludePathEntry;
import org2.eclipse.php.internal.core.project.options.PHPProjectOptions;
import org2.eclipse.php.internal.core.project.options.XMLWriter;
import org2.eclipse.php.internal.core.util.preferences.XMLPreferencesReader;

import com.aptana.core.util.StringUtil;
import com.aptana.editor.php.epl.PHPEplPlugin;
import com.aptana.editor.php.util.Key;

public class IncludePathEntry implements IIncludePathEntry
{
	public static final String TAG_INCLUDEPATH = "includepath"; //$NON-NLS-1$
	public static final String TAG_INCLUDEPATHENTRY = "includepathentry"; //$NON-NLS-1$
	public static final String TAG_ENTRY_KIND = "kind"; //$NON-NLS-1$
	public static final String TAG_CONTENT_KIND = "contentKind"; //$NON-NLS-1$
	public static final String TAG_PATH = "path"; //$NON-NLS-1$
	public static final String TAG_RESOURCE = "resource"; //$NON-NLS-1$
	public static final String TAG_ROOTPATH = "rootpath"; //$NON-NLS-1$
	public static final String TAG_EXPORTED = "exported"; //$NON-NLS-1$
	public static final String TAG_CREATEDREFERENCE = "createdReference"; //$NON-NLS-1$

	public int entryKind;
	public int contentKind;
	public IPath path;
	public IResource resource;
	public boolean isExported;
	private boolean createdReference;

	/**
	 * Creates a class path entry of the specified kind with the given path.
	 */
	public IncludePathEntry(int contentKind, int entryKind, IPath path, IResource resource, boolean isExported)
	{

		this.contentKind = contentKind;
		this.entryKind = entryKind;
		this.path = path;
		this.resource = resource;
		this.isExported = isExported;
	}

	/**
	 * This method gets the include path entries for a given project
	 * 
	 * @param preferenceKey
	 * @param project
	 * @param projectScope
	 * @param workingCopyManager
	 * @return List of IIncludePathEntrys for a given project
	 */
	public static List<IIncludePathEntry> getIncludePathEntriesFromPreferences(Key preferenceKey, IProject project,
			ProjectScope projectScope, IWorkingCopyManager workingCopyManager)
	{
		if (preferenceKey == null || project == null || projectScope == null)
		{
			throw new IllegalArgumentException("Null arguments are not allowed"); //$NON-NLS-1$
		}
		List<IIncludePathEntry> entries = new ArrayList<IIncludePathEntry>();

		Map[] maps = XMLPreferencesReader.read(preferenceKey, projectScope, workingCopyManager);
		if (maps != null)
		{
			for (Map map : maps)
			{
				IncludePathEntryDescriptor descriptor = new IncludePathEntryDescriptor();
				descriptor.restoreFromMap(map);
				entries.add(IncludePathEntry.elementDecode(descriptor, project.getFullPath()));
			}
		}
		return entries;
	}

	/**
	 * This method gets the include path entries for a given project as a string and returns a "decoded" List of
	 * IIncludePathEntrys
	 * 
	 * @param String
	 *            representing the entries they way they are saved into the preferences
	 * @param project
	 * @return List of IIncludePathEntrys for a given project
	 */
	public static List<IIncludePathEntry> getIncludePathEntriesFromPreferences(String entriesString, IProject project)
	{
		if (entriesString == null || project == null)
		{
			throw new IllegalArgumentException("Null arguments are not allowed"); //$NON-NLS-1$
		}
		List<IIncludePathEntry> entries = new ArrayList<IIncludePathEntry>();

		Map[] maps = XMLPreferencesReader.getHashFromStoredValue(entriesString);
		if (maps != null)
		{
			for (Map map : maps)
			{
				IncludePathEntryDescriptor descriptor = new IncludePathEntryDescriptor();
				descriptor.restoreFromMap(map);
				entries.add(IncludePathEntry.elementDecode(descriptor, project.getFullPath()));
			}
		}
		return entries;
	}

	public int getContentKind()
	{
		return contentKind;
	}

	public int getEntryKind()
	{
		return entryKind;
	}

	public IPath getPath()
	{
		return this.path;
	}

	public IResource getResource()
	{
		return this.resource;
	}

	public boolean isExported()
	{
		return isExported;
	}

	public static IIncludePathEntry elementDecode(Element element, PHPProjectOptions options)
	{
		if (element == null || options == null)
		{
			throw new IllegalArgumentException("Null arguments are not allowed"); //$NON-NLS-1$
		}
		IPath projectPath = options.getProject().getFullPath();
		String entryKindAttr = element.getAttribute(TAG_ENTRY_KIND);
		String contentKindAttr = element.getAttribute(TAG_CONTENT_KIND);
		String pathAttr = element.getAttribute(TAG_PATH);
		String resourceAttr = element.getAttribute(TAG_RESOURCE);

		// exported flag (optional)
		boolean isExported = element.getAttribute(TAG_EXPORTED).equals("true"); //$NON-NLS-1$

		IIncludePathEntry entry = getEntry(pathAttr, entryKindAttr, contentKindAttr, resourceAttr, isExported,
				projectPath);
		return entry;
	}

	public static IIncludePathEntry elementDecode(IncludePathEntryDescriptor descriptor, IPath projectPath)
	{
		if (descriptor == null || projectPath == null)
		{
			throw new IllegalArgumentException("Null arguments are not allowed"); //$NON-NLS-1$
		}
		IIncludePathEntry entry = getEntry(descriptor.getPath(), descriptor.getEntryKind(),
				descriptor.getContentKind(), descriptor.getResourceName(), descriptor.isExported(), projectPath);
		return entry;
	}

	public static IIncludePathEntry getEntry(String sPath, String sEntryKind, String sContentKind, String sResource,
			boolean isExported, IPath projectPath)
	{
		if (sPath == null || sEntryKind == null || sContentKind == null || sResource == null || projectPath == null)
		{
			throw new IllegalArgumentException("Null arguments are not allowed"); //$NON-NLS-1$
		}
		// ensure path is absolute
		IPath path = new Path(sPath);
		int entryKind = entryKindFromString(sEntryKind);
		if (entryKind != IIncludePathEntry.IPE_VARIABLE && entryKind != IIncludePathEntry.IPE_CONTAINER
				&& !path.isAbsolute())
		{
			path = projectPath.append(path);
		}
		IResource resource = null;

		// recreate the CP entry
		IIncludePathEntry entry = null;
		switch (entryKind)
		{

			case IIncludePathEntry.IPE_PROJECT:
				try
				{
					resource = ResourcesPlugin.getWorkspace().getRoot().getProject(sResource);
				}
				catch (Exception e)
				{
					// Do nothing
				}
				entry = newProjectEntry(path, resource, isExported);
				break;
			case IIncludePathEntry.IPE_LIBRARY:
			case IIncludePathEntry.IPE_JRE:
				entry = new IncludePathEntry(contentKindFromString(sContentKind), IIncludePathEntry.IPE_LIBRARY, path,
						resource, isExported);
				break;
			case IIncludePathEntry.IPE_SOURCE:
				// must be an entry in this project or specify another project
				entry = newSourceEntry(path, resource);
				break;
			case IIncludePathEntry.IPE_VARIABLE:
				entry = newVariableEntry(path, resource, isExported);
				break;
			case IIncludePathEntry.IPE_CONTAINER:
				entry = newContainerEntry(path, resource, isExported);
				break;
			default:
				throw new AssertionError(NLS.bind(CoreMessages.getString("includePath_unknownKind"), sEntryKind));
		}
		return entry;
	}

	public static IIncludePathEntry newProjectEntry(IPath path, IResource resource, boolean isExported)
	{
		if (path == null || resource == null)
		{
			throw new IllegalArgumentException("Null arguments are not allowed"); //$NON-NLS-1$
		}
		if (!path.isAbsolute())
		{
			throw new IllegalArgumentException("Path for IIncludePathEntry must be absolute"); //$NON-NLS-1$
		}
		return new IncludePathEntry(K_SOURCE, IIncludePathEntry.IPE_PROJECT, path, resource, isExported);

	}

	public static IIncludePathEntry newContainerEntry(IPath containerPath, IResource containerResource,
			boolean isExported)
	{

		if (containerPath == null)
		{
			throw new IllegalArgumentException("Container path cannot be null"); //$NON-NLS-1$
		}
		if (containerPath.segmentCount() < 1)
		{
			throw new IllegalArgumentException(
					"Illegal include path container path: \'" + containerPath.makeRelative().toString() + "\', must have at least one segment (containerID+hints)"); //$NON-NLS-1$//$NON-NLS-2$
		}
		return new IncludePathEntry(K_SOURCE, IIncludePathEntry.IPE_CONTAINER, containerPath, containerResource,
				isExported);
	}

	public static IIncludePathEntry newVariableEntry(IPath variablePath, IResource variableResource, boolean isExported)
	{

		if (variablePath == null)
		{
			throw new IllegalArgumentException("Variable path cannot be null"); //$NON-NLS-1$
		}
		if (variablePath.segmentCount() < 1)
		{
			throw new IllegalArgumentException(
					"Illegal classpath variable path: \'" + variablePath.makeRelative().toString() + "\', must have at least one segment"); //$NON-NLS-1$//$NON-NLS-2$
		}

		return new IncludePathEntry(K_SOURCE, IIncludePathEntry.IPE_VARIABLE, variablePath, variableResource,
				isExported);
	}

	public static IIncludePathEntry newSourceEntry(IPath path, IResource resource)
	{

		if (path == null)
		{
			throw new IllegalArgumentException("Source path cannot be null"); //$NON-NLS-1$
		}
		if (!path.isAbsolute())
		{
			throw new IllegalArgumentException("Path for IIncludePathEntry must be absolute"); //$NON-NLS-1$
		}
		return new IncludePathEntry(K_SOURCE, IIncludePathEntry.IPE_SOURCE, path, resource, false);
	}

	public void elementEncode(XMLWriter writer, IPath projectPath, boolean newLine)
	{
		// Keeping this as a HashMap (not a Map) for the XMLWriter
		HashMap parameters = new HashMap();

		parameters.put(TAG_ENTRY_KIND, IncludePathEntry.entryKindToString(this.entryKind));
		parameters.put(TAG_CONTENT_KIND, IncludePathEntry.contentKindToString(this.contentKind));
		parameters.put(TAG_CREATEDREFERENCE, createdReference ? "true" : "false"); //$NON-NLS-1$ //$NON-NLS-2$

		IPath xmlPath = this.path;
		if (this.entryKind != IIncludePathEntry.IPE_VARIABLE && this.entryKind != IIncludePathEntry.IPE_CONTAINER)
		{
			// translate to project relative from absolute (unless a device path)
			if (projectPath != null && projectPath.isPrefixOf(xmlPath))
			{
				if (xmlPath.segment(0).equals(projectPath.segment(0)))
				{
					xmlPath = xmlPath.removeFirstSegments(1);
					xmlPath = xmlPath.makeRelative();
				}
				else
				{
					xmlPath = xmlPath.makeAbsolute();
				}
			}
		}
		parameters.put(TAG_PATH, String.valueOf(xmlPath));
		if (resource != null)
		{
			parameters.put(TAG_RESOURCE, resource.getName());
		}
		if (this.isExported)
		{
			parameters.put(TAG_EXPORTED, "true");//$NON-NLS-1$
		}

		writer.printTag(TAG_INCLUDEPATHENTRY, parameters);
		writer.endTag(TAG_INCLUDEPATHENTRY);
	}

	public String elementEncode(IPath projectPath)
	{
		IncludePathEntryDescriptor descriptor = new IncludePathEntryDescriptor(this, projectPath);
		return descriptor.toString();
	}

	public static void updateProjectReferences(IIncludePathEntry[] newEntries, IIncludePathEntry[] oldEntries,
			final IProject project, SubProgressMonitor monitor)
	{
		try
		{
			boolean changedReferences = false;
			final IProjectDescription projectDescription = project.getDescription();
			List<IProject> referenced = new ArrayList<IProject>();
			List<String> referencedNames = new ArrayList<String>();
			IProject[] referencedProjects = projectDescription.getReferencedProjects();
			for (IProject refProject : referencedProjects)
			{
				referenced.add(refProject);
				referencedNames.add(refProject.getName());
			}

			for (IIncludePathEntry oldEntry : oldEntries)
			{
				if (oldEntry.getEntryKind() == IIncludePathEntry.IPE_PROJECT)
				{
					String projectName = oldEntry.getPath().lastSegment();
					if (!containsProject(newEntries, projectName))
					{
						if (((IncludePathEntry) oldEntry).createdReference)
						{
							int index = referencedNames.indexOf(projectName);
							if (index >= 0)
							{
								changedReferences = true;
								referencedNames.remove(index);
								referenced.remove(index);
							}
						}
					}
				}
			}

			for (IIncludePathEntry newEntry : newEntries)
			{
				if (newEntry.getEntryKind() == IIncludePathEntry.IPE_PROJECT)
				{
					String projectName = newEntry.getPath().lastSegment();
					if (!containsProject(oldEntries, projectName))
					{
						if (!referencedNames.contains(projectName))
						{
							changedReferences = true;
							((IncludePathEntry) newEntry).createdReference = true;
							referenced.add(ResourcesPlugin.getWorkspace().getRoot().getProject(projectName));
							referencedNames.add(projectName);
						}
					}
				}
			}
			if (changedReferences)
			{
				IProject[] referenceProjects = (IProject[]) referenced.toArray(new IProject[referenced.size()]);
				projectDescription.setReferencedProjects(referenceProjects);
				WorkspaceJob job = new WorkspaceJob(CoreMessages.getString("IncludePathEntry_2")) //$NON-NLS-1$
				{
					public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException
					{
						project.setDescription(projectDescription, monitor);
						return Status.OK_STATUS;
					}
				};
				job.setRule(project.getParent());
				job.schedule();
			}
		}
		catch (CoreException e)
		{
			PHPEplPlugin.logError(e);
		}
	}

	private static boolean containsProject(IIncludePathEntry[] entries, String projectName)
	{
		for (IIncludePathEntry entry : entries)
		{
			if (entry.getEntryKind() == IIncludePathEntry.IPE_PROJECT)
			{
				if (entry.getPath().lastSegment().equals(projectName))
				{
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns the entry kind of a <code>PackageFragmentRoot</code> from its <code>String</code> form.
	 */
	static int entryKindFromString(String kindStr)
	{

		if (kindStr.equalsIgnoreCase("prj")) //$NON-NLS-1$
		{
			return IIncludePathEntry.IPE_PROJECT;
		}
		if (kindStr.equalsIgnoreCase("var")) //$NON-NLS-1$
		{
			return IIncludePathEntry.IPE_VARIABLE;
		}
		if (kindStr.equalsIgnoreCase("con")) //$NON-NLS-1$
		{
			return IIncludePathEntry.IPE_CONTAINER;
		}
		if (kindStr.equalsIgnoreCase("src")) //$NON-NLS-1$
		{
			return IIncludePathEntry.IPE_SOURCE;
		}
		if (kindStr.equalsIgnoreCase("lib")) //$NON-NLS-1$
		{
			return IIncludePathEntry.IPE_LIBRARY;
		}
		if (kindStr.equalsIgnoreCase("jre")) //$NON-NLS-1$
		{
			return IIncludePathEntry.IPE_JRE;
		}
		return -1;
	}

	/**
	 * Returns a <code>String</code> for the entry kind of a class path entry.
	 */
	static String entryKindToString(int kind)
	{

		switch (kind)
		{
			case IIncludePathEntry.IPE_PROJECT:
				return "prj"; //$NON-NLS-1$
			case IIncludePathEntry.IPE_SOURCE:
				return "src"; //$NON-NLS-1$
			case IIncludePathEntry.IPE_LIBRARY:
				return "lib"; //$NON-NLS-1$
			case IIncludePathEntry.IPE_JRE:
				return "jre"; //$NON-NLS-1$
			case IIncludePathEntry.IPE_VARIABLE:
				return "var"; //$NON-NLS-1$
			case IIncludePathEntry.IPE_CONTAINER:
				return "con"; //$NON-NLS-1$
			default:
				return "unknown"; //$NON-NLS-1$
		}
	}

	/**
	 * Returns the content kind of a <code>PackageFragmentRoot</code> from its <code>String</code> form.
	 */
	static int contentKindFromString(String kindStr)
	{

		if (kindStr.equalsIgnoreCase("binary")) //$NON-NLS-1$
		{
			return IIncludePathEntry.K_BINARY;
		}
		if (kindStr.equalsIgnoreCase("source")) //$NON-NLS-1$
		{
			return IIncludePathEntry.K_SOURCE;
		}
		return -1;
	}

	/**
	 * Returns a <code>String</code> for the content kind of a class path entry.
	 */
	static String contentKindToString(int kind)
	{

		switch (kind)
		{
			case IIncludePathEntry.K_BINARY:
				return "binary"; //$NON-NLS-1$
			case IIncludePathEntry.K_SOURCE:
				return "source"; //$NON-NLS-1$
			default:
				return "unknown"; //$NON-NLS-1$
		}
	}

	public String validate()
	{
		String message = null;

		switch (entryKind)
		{

			case IIncludePathEntry.IPE_PROJECT:
				if (resource == null || !resource.exists())
				{
					message = CoreMessages.getString("IncludePathEntry_4") + path.toOSString(); //$NON-NLS-1$
				}
				break;
			case IIncludePathEntry.IPE_LIBRARY:
			case IIncludePathEntry.IPE_JRE:
				if (resource == null || !resource.exists())
				{
					File file = new File(path.toOSString());
					if (!file.exists())
					{
						message = CoreMessages.getString("IncludePathEntry_5") + path.toOSString(); //$NON-NLS-1$
					}
				}
				break;
			case IIncludePathEntry.IPE_SOURCE:
				if (resource == null || !resource.exists())
				{
					message = CoreMessages.getString("IncludePathEntry_6") + path.toOSString(); //$NON-NLS-1$
				}
				break;
			case IIncludePathEntry.IPE_VARIABLE:
				// if (resource == null || !resource.exists())
				// message = "included variable not found: " + path.toOSString();
				break;
			case IIncludePathEntry.IPE_CONTAINER:
				break;
			default:
				throw new AssertionError(NLS.bind(CoreMessages.getString("includePath_unknownKind"), StringUtil.EMPTY)); //$NON-NLS-1$
		}
		return message;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.project.IIncludePathEntry#setResource(org.eclipse.core.resources.IResource)
	 */
	public void setResource(IResource resource)
	{
		this.resource = resource;

	}

	public int hashCode()
	{
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + contentKind;
		result = PRIME * result + entryKind;
		result = PRIME * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		IncludePathEntry other = (IncludePathEntry) obj;
		if (contentKind != other.contentKind)
		{
			return false;
		}
		if (entryKind != other.entryKind)
		{
			return false;
		}
		if (path == null)
		{
			if (other.path != null)
			{
				return false;
			}
		}
		else if (!path.equals(other.path))
		{
			return false;
		}
		return true;
	}

}
