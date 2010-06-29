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
package com.aptana.editor.php.internal.refactoring;

import com.aptana.editor.php.internal.core.builder.IBuildPath;

/**
 * Constructed include path.
 * 
 * Can be satisfied or unsatisfied.
 * 
 * Unsatisfied path requires base build-path to depend from 
 * required build path to become satisfied.
 * 
 * @author Denis Denisenko
 */
public class ConstructedIncludePath
{
	/**
	 * Include path.
	 */
	private String includePath;
	
	/**
	 * Base build-path.
	 */
	private IBuildPath baseBuildPath;
	
	/**
	 * Required build-path.
	 */
	private IBuildPath requiredBuildPath;
	
	/**
	 * ConstructedIncludePath constructor.
	 * 
	 * @param includePath - include path.
	 * @param baseBuildPath - base build-path.
	 * @param requiredBuildPath - required build-path.
	 */
	public ConstructedIncludePath(String includePath, IBuildPath baseBuildPath,
			IBuildPath requiredBuildPath)
	{
		this.includePath = includePath;
		this.requiredBuildPath = requiredBuildPath;
		this.baseBuildPath = baseBuildPath;
	}
	
	/**
	 * Gets include path.
	 * @return include path.
	 */
	public String getIncludePath()
	{
		return includePath;
	}
	
	/**
	 * Gets base build-path.
	 * @return base build-path.
	 */
	public IBuildPath baseBuildPath()
	{
		return baseBuildPath;
	}
	
	/**
	 * Gets required build-path.
	 * @return required build-path.
	 */
	public IBuildPath getRequiredBuildPath()
	{
		return requiredBuildPath;
	}
	
	/**
	 * Gets whether constructed include path is satisfied.
	 * @return whether constructed include path is satisfied.
	 */
	public boolean isSatisfied()
	{
		return requiredBuildPath == null && baseBuildPath == null;
	}
}