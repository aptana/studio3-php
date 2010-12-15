package org.eclipse.php.internal.debug.ui.breakpoint.provider;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jface.text.ITextHover;

import com.aptana.debug.php.ui.hover.PHPDebugHover;

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
		if (adapterType == ITextHover.class)
		{
			return new PHPDebugHover();
		}
		return null;
	}

	/**
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList()
	{
		return new Class[] { IToggleBreakpointsTarget.class, ITextHover.class };
	}
}
