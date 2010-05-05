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
package com.aptana.editor.php.internal.indexer;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.internal.builder.IBuildPath;
import com.aptana.editor.php.internal.builder.IModule;

/**
 * Filter that does filter element entries the specified module build-path and it's dependencies.
 * 
 * @author Denis Denisenko
 */
public class BuildPathElementEntriesFilter implements IElementEntriesFilter
{
	
	/**
	 * Build-paths to accept.
	 */
	private HashSet<IBuildPath> activeBuildPaths  = new HashSet<IBuildPath>();
	
	/**
	 * Build-paths to accept.
	 */
	private HashSet<IBuildPath> passiveBuildPaths  = new HashSet<IBuildPath>();

	/**
	 * BuildPathElementEntriesFilter constructor.
	 * @param module - module, which build-path to use.
	 */
	public BuildPathElementEntriesFilter(IModule module)
	{
		if (module!=null) 
		{
			
			IBuildPath buildPath = module.getBuildPath();
			if (buildPath == null)
			{
				activeBuildPaths = null;
				return;
			}
			
			if (buildPath.isPassive())
			{
				passiveBuildPaths.add(buildPath);
			}
			else
			{
				activeBuildPaths.add(buildPath);
			}
			
			if(buildPath.getDependencies() != null)
			{
				for (IBuildPath dependency : buildPath.getDependencies())
				{
					if (dependency.isPassive())
					{
						passiveBuildPaths.add(dependency);
					}
					else
					{
						activeBuildPaths.add(dependency);
					}
				}
			}
		}
		else
		{
			activeBuildPaths = null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<IElementEntry> filter(Collection<IElementEntry> toFilter)
	{
		
		LinkedHashSet<IElementEntry> result = new LinkedHashSet<IElementEntry>();
		if (activeBuildPaths==null)
		{
			result.addAll(toFilter);
			return result;
		}
		if (activeBuildPaths.size() != 0 || passiveBuildPaths.size() != 0)
		{
			for (IElementEntry e : toFilter)
			{
				IModule module = e.getModule();
				boolean added  = false;
				
				if (activeBuildPaths.size() != 0)
				{
					if (module == null || activeBuildPaths.contains(module.getBuildPath()))
					{
						result.add(e);
						added = true;
					}
				}
				
				//checking passive build-paths in case we have some and we did not add this entry yet
				if (passiveBuildPaths.size() != 0 && !added)
				{
					for (IBuildPath passiveBuildPath : passiveBuildPaths)
					{
						if (passiveBuildPath.contains(module))
						{
							result.add(e);
							break;
						}
					}
				}
			}
		}
		
		return result;
	}
}
