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
package com.aptana.debug.php.ui.hover;

import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.php.internal.debug.core.xdebug.dbgp.model.DBGpTarget;
import org.eclipse.php.internal.debug.core.xdebug.dbgp.model.DBGpVariable;
import org.eclipse.php.internal.debug.core.zend.debugger.DefaultExpressionsManager;
import org.eclipse.php.internal.debug.core.zend.debugger.Expression;
import org.eclipse.php.internal.debug.core.zend.model.PHPDebugTarget;
import org.eclipse.php.internal.debug.core.zend.model.PHPStackFrame;
import org.eclipse.ui.IEditorPart;
import org.w3c.dom.Node;
import org2.eclipse.php.util.StringUtils;

import com.aptana.debug.php.core.IPHPDebugCorePreferenceKeys;
import com.aptana.editor.common.hover.CustomBrowserInformationControl;
import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.indexer.IPHPIndexConstants;
import com.aptana.editor.php.indexer.PHPGlobalIndexer;
import com.aptana.editor.php.internal.ui.hover.AbstractPHPTextHover;

/**
 * @author Pavel Petrochenko
 */
public class PHPDebugHover extends AbstractPHPTextHover
{
	private IDebugModelPresentation modelPresentation;

	// Returns the php debug target that is in contex.
	// In case that
	protected PHPDebugTarget getDebugTarget()
	{
		IAdaptable adaptable = DebugUITools.getDebugContext();
		if (adaptable instanceof PHPStackFrame)
		{
			PHPStackFrame stackFrame = (PHPStackFrame) adaptable;
			PHPDebugTarget debugTarget = (PHPDebugTarget) stackFrame.getDebugTarget();
			return debugTarget;
		}
		return null;
	}

	/**
	 * Returns the variable value.
	 * 
	 * @param debugTarget
	 * @param variable
	 *            The variable name
	 * @return value
	 */
	protected String getValue(PHPDebugTarget debugTarget, String variable)
	{
		DefaultExpressionsManager expressionManager = debugTarget.getExpressionManager();
		Expression expression = expressionManager.buildExpression(variable);

		// Get the value from the debugger
		debugTarget.getExpressionManager().getExpressionValue(expression, 1);
		expressionManager.update(expression, 1);
		String value = expression.getValue().getValueAsString();

		if (value != null && value.length() == 0)
		{
			value = "Empty"; //$NON-NLS-1$
			return value;
		}

		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.aptana.editor.php.internal.ui.hover.AbstractPHPTextHover#getHoverInfo2(org.eclipse.jface.text.ITextViewer,
	 * org.eclipse.jface.text.IRegion)
	 */
	public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion)
	{
		IStackFrame frame = getFrame();
		if (frame != null)
		{
			// first check for 'this' - code resolve does not resolve java elements for 'this'
			IDocument document = textViewer.getDocument();
			if (document != null)
			{
				try
				{
					String variableName = document.get(hoverRegion.getOffset(), hoverRegion.getLength());
					try
					{

						for (IVariable v : frame.getVariables())
						{
							if (v.getName().equals(variableName))
							{
								return getVariableText(v);
							}
						}

						// It might be a constant
						String strippedVarName = variableName;
						boolean shouldResolveConst = false;
						if (strippedVarName.startsWith("'") || strippedVarName.startsWith("\"")) //$NON-NLS-1$ //$NON-NLS-2$
						{
							strippedVarName = strippedVarName.substring(1);
							if (strippedVarName.endsWith("'") || strippedVarName.endsWith("\"")) //$NON-NLS-1$ //$NON-NLS-2$
							{
								strippedVarName = strippedVarName.substring(0, strippedVarName.length() - 1);
							}
						}
						List<IElementEntry> entries = PHPGlobalIndexer.getInstance().getIndex()
								.getEntries(IPHPIndexConstants.CONST_CATEGORY, variableName);
						if (entries.isEmpty())
						{
							if (!strippedVarName.equals(variableName))
							{
								entries = PHPGlobalIndexer.getInstance().getIndex()
										.getEntries(IPHPIndexConstants.CONST_CATEGORY, strippedVarName);
							}
							else
							{
								// Try to search for it by appending the quotes to the var
								entries = PHPGlobalIndexer.getInstance().getIndex()
										.getEntries(IPHPIndexConstants.CONST_CATEGORY, '\'' + variableName + '\'');
							}
						}
						shouldResolveConst = !entries.isEmpty();
						if (frame.getDebugTarget() instanceof DBGpTarget)
						{
							DBGpTarget xdebugTarget = (DBGpTarget) frame.getDebugTarget();
							String testExp = strippedVarName.trim();
							Node result = xdebugTarget.eval(testExp);
							if (result != null)
							{
								DBGpVariable tempVar = new DBGpVariable(xdebugTarget, result, "0"); //$NON-NLS-1$
								if (tempVar.getName().length() == 0)
								{
									// Happens in constants!
									// We want to display the tooltip in a form of <type> <name>=<value>, so we inject
									// the name into it.
									tempVar.setFullName(strippedVarName);
								}
								// In case we should resolve it anyway, we should return the value.
								// However, in case we did not locate any constant that fits the value that we hover on,
								// we
								// should return the eval value only if it's different then the one we hover on (not
								// perfect... but will do the job)
								if (shouldResolveConst || tempVar.getValue() != null
										&& !strippedVarName.equals(tempVar.getValue().getValueString()))
									return getVariableText(tempVar);
							}
						}
					}
					catch (DebugException e)
					{
						return null;
					}
				}
				catch (BadLocationException e)
				{
					return null;
				}
			}
		}
		return null;
	}

	/**
	 * @see org.eclipse.jface.text.ITextHover#getHoverInfo(org.eclipse.jface.text.ITextViewer,
	 *      org.eclipse.jface.text.IRegion)
	 */
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion)
	{
		String hoverInfo = getHoverInfo(textViewer, hoverRegion);
		return hoverInfo != null ? hoverInfo.toString() : null;
	}

	/**
	 * getFrame
	 * 
	 * @return {@link IStackFrame}
	 */
	public static IStackFrame getFrame()
	{
		IAdaptable adaptable = DebugUITools.getDebugContext();
		if (adaptable instanceof IStackFrame)
		{
			return (IStackFrame) adaptable;
		}
		if (adaptable != null)
		{
			Object frame = adaptable.getAdapter(PHPStackFrame.class);
			if (frame instanceof IStackFrame)
			{
				return (IStackFrame) frame;
			}
		}
		return null;
	}

	/**
	 * Returns HTML text for the given variable
	 */
	private String getVariableText(IVariable variable)
	{
		StringBuffer buffer = new StringBuffer();
		IDebugModelPresentation modelPresentation = getModelPresentation();
		buffer.append("<p><pre>"); //$NON-NLS-1$
		String variableText = modelPresentation.getText(variable);
		buffer.append(StringUtils.convertToHTMLContent(variableText));
		buffer.append("</pre></p>"); //$NON-NLS-1$
		if (buffer.length() > 0)
		{
			return buffer.toString();
		}
		return null;
	}

	private IDebugModelPresentation getModelPresentation()
	{
		if (modelPresentation == null)
		{
			modelPresentation = DebugUITools
					.newDebugModelPresentation(IPHPDebugCorePreferenceKeys.PHP_DEBUG_MODEL_PRESENTATION_ID);
			modelPresentation.setAttribute(IDebugModelPresentation.DISPLAY_VARIABLE_TYPE_NAMES, Boolean.TRUE);
		}
		return modelPresentation;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.hover.AbstractDocumentationHover#getHeader(java.lang.Object,
	 * org.eclipse.ui.IEditorPart, org.eclipse.jface.text.IRegion)
	 */
	@Override
	protected String getHeader(Object element, IEditorPart editorPart, IRegion hoverRegion)
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.hover.AbstractDocumentationHover#getDocumentation(java.lang.Object,
	 * org.eclipse.ui.IEditorPart, org.eclipse.jface.text.IRegion)
	 */
	@Override
	protected String getDocumentation(Object element, IEditorPart editorPart, IRegion hoverRegion)
	{
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.hover.AbstractDocumentationHover#populateToolbarActions(org.eclipse.jface.action.
	 * ToolBarManager, com.aptana.editor.common.hover.CustomBrowserInformationControl)
	 */
	@Override
	protected void populateToolbarActions(ToolBarManager tbm, CustomBrowserInformationControl iControl)
	{
	}
}
