/**
 * Copyright (c) 2005-2006 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package com.aptana.editor.php.internal.parser2;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;

import com.aptana.ide.editors.unified.LanguageRegistry;
import com.aptana.ide.parsing.IParser;

/**
 * @author Kevin Lindsey
 * @author Pavel Petrochenko
 */
public class PHPLanguageRegistry
{
	private static String TAG_PARSER = "parser"; //$NON-NLS-1$
	private static String TAG_PI_LANGUAGE = "pi-language"; //$NON-NLS-1$
	private static String TAG_SINGLE_LINE_COMMENT_LANGUAGE = "single-line-comment-language"; //$NON-NLS-1$
	private static String TAG_MULTI_LINE_COMMENT_LANGUAGE = "multi-line-comment-language"; //$NON-NLS-1$
	private static String TAG_DOCUMENTATION_COMMENT_LANGUAGE = "documentation-comment-language"; //$NON-NLS-1$

	// parser attribute
	private static String ATTR_LANGUAGE = "language"; //$NON-NLS-1$
	//private static String ATTR_PARSER = "parser"; //$NON-NLS-1$

	// pi-language attribute
	private static String ATTR_PI_NAME = "pi-name"; //$NON-NLS-1$

	private static String SINGLE_LINE_COMMENT_KEY = "//"; //$NON-NLS-1$
	private static String MULTI_LINE_COMMENT_KEY = "/*"; //$NON-NLS-1$
	private static String DOCUMENTATION_COMMENT_KEY = "/**"; //$NON-NLS-1$

	private HashMap<Object,Object> _byKey;

	/**
	 * ElementRegistry
	 */
	public PHPLanguageRegistry()
	{
		this._byKey = new HashMap<Object,Object>();
	}

	/**
	 * loadFromExtension
	 * 
	 * @param extension
	 * @return IParser[]
	 */
	public IParser[] loadFromExtension(IExtension extension)
	{
		IConfigurationElement[] elements = extension.getConfigurationElements();
		ArrayList<IParser> parsers = new ArrayList<IParser>();

		for (int i = 0; i < elements.length; i++)
		{
			IConfigurationElement element = elements[i];

			if (element.getName().equals(TAG_PARSER))
			{
				String language = element.getAttribute(ATTR_LANGUAGE);

				if (language != null && language.length() > 0)
				{
					// create parser instance
					//parser = (IParser) element.createExecutableExtension(ATTR_PARSER);
					IParser parser = LanguageRegistry.createParser(language);

					// register all pi-languages using this parser
					this.registerPILanguages(element.getChildren(TAG_PI_LANGUAGE), parser);
					
					// register all single-line comment languages
					this.registerCommentLanguages(SINGLE_LINE_COMMENT_KEY, element.getChildren(TAG_SINGLE_LINE_COMMENT_LANGUAGE), parser);
					
					// register all multi-line comment languages
					this.registerCommentLanguages(MULTI_LINE_COMMENT_KEY, element.getChildren(TAG_MULTI_LINE_COMMENT_LANGUAGE), parser);
					
					// register all document comment languages
					this.registerCommentLanguages(DOCUMENTATION_COMMENT_KEY, element.getChildren(TAG_DOCUMENTATION_COMMENT_LANGUAGE), parser);

					// add parser for our return result
					parsers.add(parser);
				}
			}
		}

		return parsers.toArray(new IParser[parsers.size()]);
	}

	/**
	 * registerPILanguage
	 * 
	 * @param element
	 * @param parsers
	 */
	private void registerPILanguages(IConfigurationElement[] elements, IParser parser)
	{
		for (int i = 0; i < elements.length; i++)
		{
			// get pi-language
			IConfigurationElement element = elements[i];

			// build full pi-name
			String piName = "<?" + element.getAttribute(ATTR_PI_NAME); //$NON-NLS-1$

			// register transition
			this.registerParser(piName, parser);
		}
	}
	
	/**
	 * registerPILanguage
	 * 
	 * @param element
	 * @param parsers
	 */
	private void registerCommentLanguages(String key, IConfigurationElement[] elements, IParser parser)
	{
		for (int i = 0; i < elements.length; i++)
		{
			// get pi-language
			IConfigurationElement element = elements[i];

			// build full pi-name
			String piName = "<?" + element.getAttribute(ATTR_PI_NAME); //$NON-NLS-1$

			// register transition
			this.registerParser(piName, parser);
		}
	}

	/**
	 * registerParser
	 * 
	 * @param key
	 * @param parser
	 */
	private void registerParser(String key, IParser parser)
	{
		this._byKey.put(key, parser);
	}

	/**
	 * getMultiLineCommentParser
	 * 
	 * @param key
	 * @return IParser
	 */
	public IParser getParser(String key)
	{
		IParser result = null;

		if (this._byKey.containsKey(key))
		{
			result = (IParser) this._byKey.get(key);
		}

		return result;
	}

	/**
	 * getSingleLineCommentParser
	 * 
	 * @return IParser
	 */
	public IParser getSingleLineCommentParser()
	{
		return this.getParser(SINGLE_LINE_COMMENT_KEY);
	}

	/**
	 * getMultiLineCommentParser
	 * 
	 * @return IParser
	 */
	public IParser getMultiLineCommentParser()
	{
		return this.getParser(MULTI_LINE_COMMENT_KEY);
	}

	/**
	 * getDocumentationCommentParser
	 * 
	 * @return IParser
	 */
	public IParser getDocumentationCommentParser()
	{
		return this.getParser(DOCUMENTATION_COMMENT_KEY);
	}

	/**
	 * getPILanguage
	 * 
	 * @param processInstructionName
	 * @return IParser
	 */
	public IParser getProcessingInstructionLanguage(String processInstructionName)
	{
		return this.getParser(processInstructionName);
	}
}
