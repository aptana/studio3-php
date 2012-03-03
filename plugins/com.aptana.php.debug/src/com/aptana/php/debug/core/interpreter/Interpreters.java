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
package com.aptana.php.debug.core.interpreter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import com.aptana.php.debug.PHPDebugPlugin;

/**
 * Manager of interpreters..
 * 
 * @author Denis Denisenko
 */
public class Interpreters
{
	/**
	 * Default interpreter info.
	 * 
	 * @author Denis Denisenko
	 */
	private static class DefaultInterpreterInfo
	{
		public IInterpreter interperter;

		public int priority;
	}

	/**
	 * Extension point name.
	 */
	private static final String EXTENSION_POINT_NAME = "com.aptana.php.debug.interpreterProvider"; //$NON-NLS-1$

	/**
	 * Provider element name.
	 */
	private static final String PROVIDER_ELEMENT_NAME = "provider"; //$NON-NLS-1$

	/**
	 * Class attribute name.
	 */
	private static final String CLASS_ATTRIBUTE_NAME = "class"; //$NON-NLS-1$

	/**
	 * Language attribute name.
	 */
	private static final String LANGUAGE_ATTRIBUTE_NAME = "lang"; //$NON-NLS-1$

	/**
	 * Priority attribute name.
	 */
	private static final String PRIORITY_ATTRIBUTE_NAME = "priority"; //$NON-NLS-1$

	/**
	 * OS attribute name.
	 */
	private static final String OS_ATTRIBUTE_NAME = "OpSystem"; //$NON-NLS-1$

	/**
	 * Singleton instance.
	 */
	private static Interpreters instance = new Interpreters();

	/**
	 * Cached interpreters.
	 */
	private Map<String, List<IInterpreter>> interpreters;

	/**
	 * Default interpreters.
	 */
	private Map<String, DefaultInterpreterInfo> defaultInterpreters;

	/**
	 * Gets default Interpreters instance.
	 * 
	 * @return default Interpreters instance.
	 */
	public static Interpreters getDefault()
	{
		return instance;
	}

	/**
	 * Gets language interpreters.
	 * 
	 * @param language
	 *            - language to get interpreters for.
	 * @return language interpreters.
	 */
	public synchronized List<IInterpreter> getInterpreters(String language)
	{
		collectInterpretersInfo();

		List<IInterpreter> result = interpreters.get(language);
		if (result == null)
		{
			result = Collections.emptyList();
		}

		return result;
	}

	/**
	 * Gets default interpreter for a language.
	 * 
	 * @param language
	 *            - language to get default interpreter for.
	 * @return default interpreter for a language.
	 */
	public synchronized IInterpreter getDefaultInterpreter(String language)
	{
		collectInterpretersInfo();

		DefaultInterpreterInfo info = defaultInterpreters.get(language);
		if (info == null)
		{
			return null;
		}

		return info.interperter;
	}

	/**
	 * Collects interpreters information.
	 */
	private void collectInterpretersInfo()
	{
		interpreters = new HashMap<String, List<IInterpreter>>();
		defaultInterpreters = new HashMap<String, DefaultInterpreterInfo>();

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint ep = registry.getExtensionPoint(EXTENSION_POINT_NAME);

		if (ep != null)
		{
			IExtension[] extensions = ep.getExtensions();

			for (int i = 0; i < extensions.length; i++)
			{
				IExtension extension = extensions[i];
				IConfigurationElement[] elements = extension.getConfigurationElements();

				for (int j = 0; j < elements.length; j++)
				{
					IConfigurationElement element = elements[j];
					String elementName = element.getName();

					if (elementName.equals(PROVIDER_ELEMENT_NAME))
					{
						String language = element.getAttribute(LANGUAGE_ATTRIBUTE_NAME);
						String os = element.getAttribute(OS_ATTRIBUTE_NAME);
						String currentOS = System.getProperty("os.name").toLowerCase(); //$NON-NLS-1$
						if (os != null && os.length() != 0 && !currentOS.startsWith(os))
						{
							continue;
						}

						int priority = 0;
						String priorityString = element.getAttribute(PRIORITY_ATTRIBUTE_NAME);
						if (priorityString != null)
						{
							try
							{
								priority = Integer.parseInt(priorityString);
							}
							catch (NumberFormatException ex)
							{
								PHPDebugPlugin
										.logError(
												Messages.getString("Interpreters.ERR_WrongPriorityFormatPrefix") //$NON-NLS-1$
														+ elementName
														+ Messages
																.getString("Interpreters.ERR_WrongPriorityFormatSuffix"), null); //$NON-NLS-1$
							}
						}
						try
						{
							IInterpreterProvider provider = (IInterpreterProvider) element
									.createExecutableExtension(CLASS_ATTRIBUTE_NAME);
							List<IInterpreter> currentInterpreters;
							try
							{
								currentInterpreters = provider.getInterpreters();
								List<IInterpreter> oldInterpreters = interpreters.get(language);
								if (oldInterpreters == null)
								{
									oldInterpreters = new ArrayList<IInterpreter>();
								}
								oldInterpreters.addAll(currentInterpreters);
								interpreters.put(language, oldInterpreters);

								IInterpreter defaultInterpreter = provider.getDefaultInterpreter();
								if (defaultInterpreter != null)
								{
									DefaultInterpreterInfo oldInfo = defaultInterpreters.get(language);
									if (oldInfo == null || oldInfo.priority < priority)
									{
										DefaultInterpreterInfo newInfo = new DefaultInterpreterInfo();
										newInfo.priority = priority;
										newInfo.interperter = defaultInterpreter;

										defaultInterpreters.put(language, newInfo);
									}
								}
							}
							catch (Throwable th)
							{
								PHPDebugPlugin.logError(
										Messages.getString("Interpreters.ERR_UnableToGetInterpretersFromProvider"), th); //$NON-NLS-1$
							}
						}
						catch (CoreException e)
						{
							PHPDebugPlugin.logError(Messages.getString("Interpreters.ERR_UnableCreatingProvider"), e); //$NON-NLS-1$
						}
					}
				}
			}
		}
	}

	/**
	 * InterpretersManager private constructor.
	 */
	private Interpreters()
	{
	}
}
