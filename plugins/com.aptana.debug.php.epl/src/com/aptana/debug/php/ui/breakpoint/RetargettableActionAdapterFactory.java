package com.aptana.debug.php.ui.breakpoint;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;

/**
 * @author Max Stepanov
 */
public class RetargettableActionAdapterFactory implements IAdapterFactory
{
	/**
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType)
	{
		if (adapterType == IToggleBreakpointsTarget.class)
		{
			return new ToggleBreakpointAdapter();
		}
		return null;
	}

	/**
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList()
	{
		return new Class[] { IToggleBreakpointsTarget.class };
	}
}
