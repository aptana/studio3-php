/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org2.eclipse.php.internal.debug.ui.views;

import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org2.eclipse.php.internal.debug.core.zend.debugger.Expression;

public class PHPStackLabelProvider implements ILabelProvider {

    private Image image = null;

    public PHPStackLabelProvider() {
        super();
        image = DebugPluginImages.getImage(IDebugUIConstants.IMG_OBJS_VARIABLE);

    }

    public Image getImage(Object element) {
        // TODO Auto-generated method stub
        return image;
    }

    public String getText(Object element) {
        if (element instanceof Expression) {
            return "$" + ((Expression)element).toString();

        }
        return null;
    }

    public void addListener(ILabelProviderListener listener) {
        // TODO Auto-generated method stub

    }

    public void dispose() {
        // TODO Auto-generated method stub

    }

    public boolean isLabelProperty(Object element, String property) {
        // TODO Auto-generated method stub
        return false;
    }

    public void removeListener(ILabelProviderListener listener) {
        // TODO Auto-generated method stub

    }

}
