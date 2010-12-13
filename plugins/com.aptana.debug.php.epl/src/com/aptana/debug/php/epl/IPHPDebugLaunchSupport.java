package com.aptana.debug.php.epl;

import java.util.List;

import org.eclipse.core.resources.IProject;

/**
 * PHP debug launch support interface.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public interface IPHPDebugLaunchSupport
{
	/**
	 * 
	 * @param project
	 * @return
	 */
	List<String> getInterpreterIncludePath(IProject project);
}
