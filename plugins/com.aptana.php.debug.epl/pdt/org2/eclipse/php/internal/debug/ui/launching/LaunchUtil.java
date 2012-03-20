/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org2.eclipse.php.internal.debug.ui.launching;

import java.util.ArrayList;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager;

import com.aptana.editor.php.internal.core.IPHPConstants;

public class LaunchUtil {
	
	public static final String ORG_ECLIPSE_WST_HTML_CORE_HTMLSOURCE = "org.eclipse.wst.html.core.htmlsource";
	
	public static String[] getRequiredNatures()
    {
    	// return new String[] {org2.eclipse.php.internal.core.project.PHPNature.ID};
		// TODO: SG - Check if we need to limit it to Aptana's PHP nature
    	return new String[] {};
    }
    
    public static String[] getFileExtensions()
    {
    	ArrayList<String> extensions = new ArrayList<String>();
    	IContentTypeManager typeManager = Platform.getContentTypeManager();
    	
    	IContentType type = typeManager.getContentType(IPHPConstants.CONTENT_TYPE_HTML_PHP);
    	
        String[] phpExtensions = (type != null) ? type.getFileSpecs(IContentType.FILE_EXTENSION_SPEC) : new String[]{"php"}; //$NON-NLS-1$
        
        //IContentType htmlContentType = typeManager.getContentType(ORG_ECLIPSE_WST_HTML_CORE_HTMLSOURCE);
        String[] htmlExtensions = null;//htmlContentType.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);

        if(phpExtensions != null)
        	for(int i=0; i<phpExtensions.length; i++)
        		extensions.add(phpExtensions[i]);
        
        if(htmlExtensions != null)
        	for(int i=0; i<htmlExtensions.length; i++)
        		extensions.add(htmlExtensions[i]);
        
    	if(extensions.isEmpty())
    		return null;
    	
    	return (String[])extensions.toArray(new String[extensions.size()]);
    }
}
