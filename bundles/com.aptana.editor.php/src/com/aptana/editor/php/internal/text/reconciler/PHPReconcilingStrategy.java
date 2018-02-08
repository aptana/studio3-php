/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.text.reconciler;

import com.aptana.core.build.ReconcileContext;
import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.editor.common.text.reconciler.CommonReconcilingStrategy;
import com.aptana.editor.common.text.reconciler.IFoldingComputer;
import com.aptana.editor.php.internal.ui.editor.PHPFoldingComputer;

/**
 * PHP reconciling strategy.
 * 
 * @author Shalom Gibly <sgibly@appcelerator.com>
 */
public class PHPReconcilingStrategy extends CommonReconcilingStrategy
{
	/**
	 * Constructs a new reconciling strategy.
	 * 
	 * @param editor
	 */
	public PHPReconcilingStrategy(AbstractThemeableEditor editor)
	{
		super(editor);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.text.reconciler.CommonReconcilingStrategy#createContext()
	 */
	@Override
	protected ReconcileContext createContext()
	{
		return new PHPReconcileContext(getEditor(), getFile(), getDocument().get());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.aptana.editor.common.text.reconciler.CommonReconcilingStrategy#shouldUpdatePositions(com.aptana.editor.common
	 * .text.reconciler.IFoldingComputer)
	 */
	@Override
	protected boolean shouldUpdatePositions(IFoldingComputer folder)
	{
		if (folder instanceof PHPFoldingComputer)
		{
			return !((PHPFoldingComputer) folder).hasCachedAST();
		}
		return super.shouldUpdatePositions(folder);
	}
}
