/*******************************************************************************
 * Copyright (c) 2006 Zend Corporation and IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org2.eclipse.php.internal.core.util;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.FormColors;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class ScrolledCompositeImpl extends ScrolledComposite {

	private FormToolkit fToolkit;

	public ScrolledCompositeImpl(Composite parent) {
		this(parent, SWT.V_SCROLL | SWT.H_SCROLL);
	}

	public ScrolledCompositeImpl(Composite parent, int style) {
		super(parent, style);

		setFont(parent.getFont());

		FormColors colors = new FormColors(parent.getDisplay());
		colors.setBackground(null);
		colors.setForeground(null);

		fToolkit = new FormToolkit(colors);

		setExpandHorizontal(true);
		setExpandVertical(true);

		Composite body = new Composite(this, SWT.NONE);
		body.setFont(parent.getFont());
		setContent(body);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Widget#dispose()
	 */
	public void dispose() {
		fToolkit.dispose();
		super.dispose();
	}

	public void adaptChild(Control childControl) {
		fToolkit.adapt(childControl, true, true);
	}

	public Composite getBody() {
		return (Composite) getContent();
	}

}
