/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
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