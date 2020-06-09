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
package org2.eclipse.php.internal.debug.ui.propertyTesters;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import com.aptana.editor.php.core.PHPNature;
import com.aptana.php.debug.epl.PHPDebugEPLPlugin;

public class RunnablePropertyTester extends PropertyTester{

    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
        Class newClass = receiver.getClass();
        String name = newClass.getName();
        if (name.equals("org2.eclipse.php.internal.core.phpModel.parser.PHPCodeDataFactory$PHPFileDataImp")){
           return false; 
        }

        IProject project = null;
        if (receiver instanceof IFolder){
           IFolder folder = (IFolder)receiver;
           project = folder.getProject(); 
        }
        if (receiver instanceof IProject){
           project = (IProject)receiver;
        }

        if (project == null || !project.isOpen()) {
            return true;
        }
        try {
            if (project.isNatureEnabled(PHPNature.NATURE_ID)){
                return false;
            }
        } catch (CoreException e) {
           PHPDebugEPLPlugin.logError(e);
        }

        return true;
    }

}
