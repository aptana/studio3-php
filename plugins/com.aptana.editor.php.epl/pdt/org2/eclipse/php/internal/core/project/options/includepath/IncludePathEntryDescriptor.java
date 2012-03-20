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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org2.eclipse.php.internal.core.project.IIncludePathEntry;
import org2.eclipse.php.internal.core.util.preferences.IXMLPreferencesStorable;

import com.aptana.core.util.StringUtil;

public class IncludePathEntryDescriptor implements IXMLPreferencesStorable
{

	private String entryKind = StringUtil.EMPTY;
	private String contentKind = StringUtil.EMPTY;
	private String path = StringUtil.EMPTY;
	private String resourceName = StringUtil.EMPTY;
	private boolean isExported = false;
	private boolean createdReference = false;

	public IncludePathEntryDescriptor()
	{
	}

	public String getContentKind()
	{
		return contentKind;
	}

	public void setContentKind(String contentKind)
	{
		this.contentKind = contentKind;
	}

	public boolean isCreatedReference()
	{
		return createdReference;
	}

	public void setCreatedReference(boolean createdReference)
	{
		this.createdReference = createdReference;
	}

	public String getEntryKind()
	{
		return entryKind;
	}

	public void setEntryKind(String entryKind)
	{
		this.entryKind = entryKind;
	}

	public boolean isExported()
	{
		return isExported;
	}

	public void setExported(boolean isExported)
	{
		this.isExported = isExported;
	}

	public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
	}

	public String getResourceName()
	{
		return resourceName;
	}

	public void setResourceName(String resourceName)
	{
		this.resourceName = resourceName;
	}

	public IncludePathEntryDescriptor(IncludePathEntry entry, IPath projectPath)
	{
		this.entryKind = IncludePathEntry.entryKindToString(entry.entryKind);
		this.contentKind = IncludePathEntry.contentKindToString(entry.contentKind);
		// path = entry.path.toOSString();
		if (entry.resource != null)
		{
			this.resourceName = entry.resource.getName();
		}
		this.isExported = entry.isExported;
		this.createdReference = false;

		IPath entryPath = entry.path;
		if (entry.entryKind != IIncludePathEntry.IPE_VARIABLE && entry.entryKind != IIncludePathEntry.IPE_CONTAINER)
		{
			// translate to project relative from absolute (unless a device path)
			if (projectPath != null && projectPath.isPrefixOf(entryPath))
			{
				if (entryPath.segment(0).equals(projectPath.segment(0)))
				{
					entryPath = entryPath.removeFirstSegments(1);
					entryPath = entryPath.makeRelative();
				}
				else
				{
					entryPath = entryPath.makeAbsolute();
				}
			}
		}
		this.path = String.valueOf(entryPath);
	}

	@SuppressWarnings("rawtypes")
	public void restoreFromMap(Map map)
	{
		Map entry = (Map) map.get("javabridge_entry"); //$NON-NLS-1$
		if (entry != null)
		{
			entryKind = (String) entry.get("entryKind"); //$NON-NLS-1$
			contentKind = (String) entry.get("contentKind"); //$NON-NLS-1$
			path = (String) entry.get("path"); //$NON-NLS-1$
			resourceName = (String) entry.get("resourceName"); //$NON-NLS-1$
			isExported = (Boolean.valueOf((String) entry.get("isExported"))).booleanValue(); //$NON-NLS-1$
			createdReference = (Boolean.valueOf((String) entry.get("referenceWasCreated"))).booleanValue(); //$NON-NLS-1$
		}
	}

	@SuppressWarnings("rawtypes")
	public Map storeToMap()
	{
		Map<String, Comparable> map = new HashMap<String, Comparable>(6);
		map.put("entryKind", entryKind); //$NON-NLS-1$
		map.put("contentKind", contentKind); //$NON-NLS-1$
		map.put("path", path); //$NON-NLS-1$
		map.put("resourceName", resourceName); //$NON-NLS-1$
		map.put("isExported", new Boolean(isExported)); //$NON-NLS-1$
		map.put("referenceWasCreated", new Boolean(createdReference)); //$NON-NLS-1$

		Map<String, Map> entry = new HashMap<String, Map>(1);
		entry.put("javabridge_entry", map); //$NON-NLS-1$

		return entry;
	}

}
