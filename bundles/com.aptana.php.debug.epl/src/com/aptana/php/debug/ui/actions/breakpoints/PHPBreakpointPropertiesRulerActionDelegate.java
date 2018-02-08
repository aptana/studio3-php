/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.aptana.php.debug.ui.actions.breakpoints;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.texteditor.AbstractRulerActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Action to open a properties dialog on a PHP breakpoint from a ruler context menu.
 * <p>
 * This action can also be contributed to a vertical ruler context menu via the <code>popupMenus</code> extension point,
 * by referencing the ruler's context menu identifier in the <code>targetID</code> attribute.
 * 
 * <pre>
 * &lt;extension point=&quot;org.eclipse.ui.popupMenus&quot;&gt;
 *   &lt;viewerContribution
 *     targetID=&quot;example.rulerContextMenuId&quot;
 *     id=&quot;example.RulerPopupActions&quot;&gt;
 *       &lt;action
 *         label=&quot;Properties&quot;
 *         class=&quot;com.aptana.php.debug.ui.actions.breakpoints.PHPBreakpointPropertiesRulerActionDelegate&quot;
 *         menubarPath=&quot;additions&quot;
 *         id=&quot;example.rulerContextMenu.phpBreakpointPropertiesAction&quot;&gt;
 *       &lt;/action&gt;
 *   &lt;/viewerContribution&gt;
 * </pre>
 * 
 * </p>
 * <p>
 * Clients may refer to this class as an action delegate in plug-in XML.
 * </p>
 * 
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @noextend This class is not intended to be subclassed by clients.
 */
public class PHPBreakpointPropertiesRulerActionDelegate extends AbstractRulerActionDelegate
{

	/**
	 * @see AbstractRulerActionDelegate#createAction(ITextEditor, IVerticalRulerInfo)
	 */
	@Override
	protected IAction createAction(final ITextEditor editor, final IVerticalRulerInfo rulerInfo)
	{
		return new PHPBreakpointPropertiesRulerAction(editor, rulerInfo);
	}
}