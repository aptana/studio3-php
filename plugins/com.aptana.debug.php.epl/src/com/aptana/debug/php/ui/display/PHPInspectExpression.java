/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Appcelerator - Modification to support PHP inspection
 *******************************************************************************/
package com.aptana.debug.php.ui.display;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IErrorReportingExpression;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IWatchExpressionResult;

/**
 * An implementation of an expression produced from the inspect action. An inspect expression removes itself from the
 * expression manager when its debug target terminates.
 */
public class PHPInspectExpression extends PlatformObject implements IErrorReportingExpression, IDebugEventSetListener
{

	/**
	 * The value of this expression
	 */
	private IValue fValue;

	/**
	 * The code snippet for this expression.
	 */
	private String fExpression;

	private IWatchExpressionResult fResult;

	/**
	 * Constructs a new inspect result for the given expression and resulting value. Starts listening to debug events
	 * such that this element will remove itself from the expression manager when its debug target terminates.
	 * 
	 * @param expression
	 *            code snippet
	 * @param value
	 *            value of the expression
	 */
	public PHPInspectExpression(String expression, IValue value)
	{
		fValue = value;
		fExpression = expression;
		DebugPlugin.getDefault().addDebugEventListener(this);
	}

	/**
	 * Constructs a new inspect result for the given evaluation result, which provides a snippet, value, and error
	 * messages, if any.
	 * 
	 * @param result
	 *            the evaluation result
	 */
	public PHPInspectExpression(IWatchExpressionResult result)
	{
		this(result.getExpressionText(), result.getValue());
		fResult = result;
	}

	/**
	 * @see IExpression#getExpressionText()
	 */
	public String getExpressionText()
	{
		return fExpression;
	}

	/**
	 * @see IExpression#getValue()
	 */
	public IValue getValue()
	{
		return fValue;
	}

	/**
	 * @see IDebugElement#getDebugTarget()
	 */
	public IDebugTarget getDebugTarget()
	{
		IValue value = getValue();
		if (value != null)
		{
			return getValue().getDebugTarget();
		}
		if (fResult != null)
		{
			return fResult.getValue().getDebugTarget();
		}
		// An expression should never be created with a null value *and*
		// a null result.
		return null;
	}

	/**
	 * @see IDebugElement#getModelIdentifier()
	 */
	public String getModelIdentifier()
	{
		return getDebugTarget().getModelIdentifier();
	}

	/**
	 * @see IDebugElement#getLaunch()
	 */
	public ILaunch getLaunch()
	{
		return getDebugTarget().getLaunch();
	}

	/**
	 * @see IDebugEventSetListener#handleDebugEvents(DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events)
	{
		for (int i = 0; i < events.length; i++)
		{
			DebugEvent event = events[i];
			switch (event.getKind())
			{
				case DebugEvent.TERMINATE:
					if (event.getSource().equals(getDebugTarget()))
					{
						DebugPlugin.getDefault().getExpressionManager().removeExpression(this);
					}
					break;
				case DebugEvent.SUSPEND:
					if (event.getDetail() != DebugEvent.EVALUATION_IMPLICIT)
					{
						if (event.getSource() instanceof IDebugElement)
						{
							IDebugElement source = (IDebugElement) event.getSource();
							if (source.getDebugTarget().equals(getDebugTarget()))
							{
								DebugPlugin.getDefault()
										.fireDebugEventSet(
												new DebugEvent[] { new DebugEvent(this, DebugEvent.CHANGE,
														DebugEvent.CONTENT) });
							}
						}
					}
					break;
			}
		}
	}

	/**
	 * @see IExpression#dispose()
	 */
	public void dispose()
	{
		DebugPlugin.getDefault().removeDebugEventListener(this);
	}

	/**
	 * @see org.eclipse.debug.core.model.IErrorReportingExpression#hasErrors()
	 */
	public boolean hasErrors()
	{
		return fResult != null && fResult.hasErrors();
	}

	/**
	 * @see org.eclipse.debug.core.model.IErrorReportingExpression#getErrorMessages()
	 */
	public String[] getErrorMessages()
	{
		return getErrorMessages(fResult);
	}

	public static String[] getErrorMessages(IWatchExpressionResult result)
	{
		if (result == null)
		{
			return new String[0];
		}
		String messages[] = result.getErrorMessages();
		if (messages.length > 0)
		{
			return messages;
		}
		DebugException exception = result.getException();
		if (exception != null)
		{
			return new String[] { exception.getMessage() };
		}
		return new String[0];
	}
}
