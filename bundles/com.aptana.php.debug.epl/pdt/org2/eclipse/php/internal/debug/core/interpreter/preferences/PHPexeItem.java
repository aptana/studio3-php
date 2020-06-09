/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org2.eclipse.php.internal.debug.core.interpreter.preferences;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org2.eclipse.php.internal.debug.core.interpreter.phpIni.PHPINIUtil;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.ProcessUtil;
import com.aptana.core.util.StringUtil;
import com.aptana.php.debug.core.interpreter.IInterpreter;
import com.aptana.php.debug.core.util.FileUtils;
import com.aptana.php.debug.epl.PHPDebugEPLPlugin;

/**
 * A PHP executable item. An item has a name, version, path, configuration file path and a debugger ID. It can be
 * editable or non-editable in case it was loaded from an extension point.
 * 
 * @author shalom, michael
 */
public class PHPexeItem implements IInterpreter
{
	private static final String LD_LIBRARY_PATH = "LD_LIBRARY_PATH"; //$NON-NLS-1$
	private static final String DYLD_LIBRARY_PATH = "DYLD_LIBRARY_PATH"; //$NON-NLS-1$
	public static final String SAPI_CLI = "CLI"; //$NON-NLS-1$
	public static final String SAPI_CGI = "CGI"; //$NON-NLS-1$

	private static final Pattern PHP_VERSION = Pattern.compile("PHP (\\d\\.\\d\\.\\d+).*? \\((.*?)\\)"); //$NON-NLS-1$
	private static final Pattern PHP_CLI_CONFIG = Pattern.compile("Configuration File \\(php.ini\\) Path => (.*?)"); //$NON-NLS-1$
	private static final Pattern PHP_CGI_CONFIG = Pattern
			.compile("Configuration File \\(php.ini\\) Path </td><td class=\"v\">(.*?)</td>"); //$NON-NLS-1$

	private String sapiType;
	private String name;
	private File config;
	private File detectedConfig;
	private File executable;
	private String version;
	private boolean editable = true;
	private String debuggerID;
	private boolean isDefault;

	private HashSet<String> extensions = new HashSet<String>();

	/**
	 * Constructs a new PHP Interpreter item.
	 * 
	 * @param name
	 * @param phpDirectoryPath
	 * @param config
	 *            The configuration file location (can be null)
	 * @param debuggerID
	 */
	public PHPexeItem(String name, String executable, String config, String debuggerID)
	{
		this.name = name;
		this.debuggerID = debuggerID;
		this.executable = new File(executable);
		if (!StringUtil.isEmpty(config))
		{
			this.config = new File(config);
		}

		detectFromPHPExe();
	}

	/**
	 * Constructs a new PHP executable item.
	 * 
	 * @param name
	 * @param executable
	 * @param iniLocation
	 * @param debuggerID
	 * @param editable
	 */
	public PHPexeItem(String name, File executable, File iniLocation, String debuggerID, boolean editable)
	{
		this.name = name;
		this.executable = executable;
		this.config = iniLocation;
		this.debuggerID = debuggerID;
		this.editable = editable;
		try
		{
			detectFromPHPExe();
		}
		catch (Exception e)
		{
			PHPDebugEPLPlugin.logError(e.getMessage(), e);
		}
	}

	/**
	 * Constructs a new PHP executable item.
	 */
	public PHPexeItem()
	{
	}

	/**
	 * @return extensions
	 */
	public String[] getExtensions()
	{
		return extensions.toArray(new String[extensions.size()]);
	}

	/**
	 * @param extensions
	 */
	public void setExtensions(String[] extensions)
	{
		HashSet<String> exts = new HashSet<String>();
		for (String s : extensions)
		{
			exts.add(s);
		}
		this.extensions = exts;

	}

	/**
	 * Returns the debugger ID set for this item.
	 * 
	 * @return The debugger ID.
	 */
	public String getDebuggerID()
	{
		return debuggerID;
	}

	/**
	 * Set the debugger ID that can use this item.
	 * 
	 * @param debuggerID
	 *            A debugger ID.
	 */
	public void setDebuggerID(String debuggerID)
	{
		this.debuggerID = debuggerID;
	}

	/**
	 * Returns the configuration file path. The returned value can be null in case the value was not set. In this case,
	 * the ini location is assumed to be next to the php executable.
	 * 
	 * @return The configuration file location.
	 */
	public File getINILocation()
	{
		return config;
	}

	/**
	 * Returns the detected configuration file path.
	 * 
	 * @return The detected configuration file location.
	 */
	public File getDetectedINILocation()
	{
		return detectedConfig;
	}

	/**
	 * Set the PHP ini location.
	 * 
	 * @param location
	 *            The ini location (can be null).
	 */
	public void setINILocation(File location)
	{
		this.config = location;
	}

	/**
	 * Returns SAPI type of this PHP executable
	 * 
	 * @return
	 */
	public String getSapiType()
	{
		return sapiType;
	}

	/**
	 * Sets SAPI type of this PHP executable
	 * 
	 * @param sapiType
	 */
	public void setSapiType(String sapiType)
	{
		this.sapiType = sapiType;
	}

	/**
	 * Returns the name of this PHP executable item.
	 * 
	 * @return The name of the item.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the name of this item.
	 * 
	 * @param name
	 *            The name of the item.
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Returns the php executable file.
	 * 
	 * @return The php executable file.iniLocation
	 */
	public File getExecutable()
	{
		return executable;
	}

	/**
	 * Sets the php executable path. Setting the path also sets the executable directory and reset the ini location to
	 * null.
	 * 
	 * @return The php executable file.
	 * @throws IllegalArgumentException
	 *             in case the file is null.
	 */
	public void setExecutable(File executable)
	{
		if (executable == null)
		{
			throw new IllegalArgumentException("PHP executable path is null"); //$NON-NLS-1$
		}

		if (executable.equals(this.executable))
		{
			return;
		}

		this.executable = executable;
		this.config = null;

		detectFromPHPExe();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((executable == null) ? 0 : executable.hashCode());
		result = prime * result + ((config == null) ? 0 : config.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		final PHPexeItem other = (PHPexeItem) obj;
		if (config == null)
		{
			if (other.config != null)
			{
				return false;
			}
		}
		else if (!config.equals(other.config))
		{
			return false;
		}
		if (name == null)
		{
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuilder buf = new StringBuilder(name);
		buf.append(" [path: ").append(executable != null ? executable.getAbsolutePath() : "<unknown>"); //$NON-NLS-1$ //$NON-NLS-2$
		buf.append(", config file: ").append(config.getAbsolutePath()); //$NON-NLS-1$
		buf.append(", sapi: ").append(sapiType); //$NON-NLS-1$
		buf.append("]"); //$NON-NLS-1$
		return buf.toString();
	}

	/**
	 * Returns the version of the item.
	 * 
	 * @return The item's version.
	 */
	public String getVersion()
	{
		return version;
	}

	/**
	 * Sets the version of the item.
	 * 
	 * @param version
	 *            The item's version.
	 */
	public void setVersion(String version)
	{
		this.version = version;
	}

	/**
	 * Returns if this item is editable (e.g. a user defined item).
	 * 
	 * @return True, if this item can be edited.
	 */
	public boolean isEditable()
	{
		return editable;
	}

	/**
	 * Returns if this item is the default item.
	 * 
	 * @return if this item is the default item.
	 */
	public boolean isDefault()
	{
		return isDefault;
	}

	/**
	 * Set or un-set this item to be the default php executable item.
	 * 
	 * @param isDefault
	 *            the value to set
	 */
	public void setDefault(boolean isDefault)
	{
		this.isDefault = isDefault;
	}

	/**
	 * Detects various things like: type, version, default configuration file, etc. from the PHP binary
	 */
	protected void detectFromPHPExe()
	{
		if (executable == null)
		{
			throw new IllegalStateException("PHP executable path is null"); //$NON-NLS-1$
		}

		// Create empty configuration file:
		File tempPHPIni = PHPINIUtil.createTemporaryPHPINIFile();

		changePermissions(executable);

		// Detect version and type:
		Map<String, String> environment = new ProcessBuilder().environment();

		if (Platform.OS_MACOSX.equals(Platform.getOS()))
		{
			environment.put(DYLD_LIBRARY_PATH, executable.getParent());
		}
		else if (!Platform.OS_WIN32.equals(Platform.getOS()))
		{
			environment.put(LD_LIBRARY_PATH, executable.getParent());
		}
		String output = ProcessUtil.outputForCommand(executable.getAbsolutePath(), Path.fromOSString(executable
				.getParent()), environment, "-c", tempPHPIni.getParentFile().getAbsolutePath(), "-v"); //$NON-NLS-1$ //$NON-NLS-2$
		if (PHPDebugEPLPlugin.getDefault().isDebugging() || PHPDebugEPLPlugin.DEBUG)
		{
			IdeLog.logInfo(PHPDebugEPLPlugin.getDefault(), output);
		}
		Matcher m = PHP_VERSION.matcher(output);
		if (m.find())
		{
			version = m.group(1);
			String type = m.group(2);
			if (type.startsWith("cgi")) { //$NON-NLS-1$
				sapiType = SAPI_CGI;
			}
			else if (type.startsWith("cli")) { //$NON-NLS-1$
				sapiType = SAPI_CLI;
			}
			else
			{
				PHPDebugEPLPlugin.logWarning("Can't determine type of the PHP executable"); //$NON-NLS-1$
				this.executable = null;
				return;
			}

			if (name == null)
			{
				name = "PHP " + version + " (" + sapiType + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		else
		{
			PHPDebugEPLPlugin.logWarning("Can't determine version of the PHP Interpreter"); //$NON-NLS-1$
			this.executable = null;
			return;
		}

		// Detect default PHP.ini location:
		if (detectedConfig == null)
		{
			output = ProcessUtil.outputForCommand(executable.getAbsolutePath(), Path.fromOSString(executable
					.getParent()), environment, "-c", tempPHPIni.getParentFile().getAbsolutePath(), "-i"); //$NON-NLS-1$ //$NON-NLS-2$
			if (sapiType == SAPI_CLI)
			{
				m = PHP_CLI_CONFIG.matcher(output);
			}
			else if (sapiType == SAPI_CGI)
			{
				m = PHP_CGI_CONFIG.matcher(output);
			}
			if (m.find())
			{
				String configDir = m.group(1);
				detectedConfig = new File(configDir.trim(), "php.ini"); //$NON-NLS-1$
				if (!detectedConfig.exists())
				{
					detectedConfig = null;
				}
			}
			else
			{
				PHPDebugEPLPlugin.logWarning("Can't determine PHP.ini location of the PHP Interpreter"); //$NON-NLS-1$
				this.executable = null;
				return;
			}
		}
	}

	public static String getLibVarriable(File executable)
	{
		StringBuilder buf = new StringBuilder();
		final String os = Platform.getOS();
		if (!Platform.OS_WIN32.equals(os))
		{
			boolean isMac = Platform.OS_MACOSX.equals(os);
			if (isMac)
			{
				buf.append(DYLD_LIBRARY_PATH);
			}
			else
			{
				buf.append(LD_LIBRARY_PATH);
			}
			buf.append('=');
			buf.append(executable.getParent());
		}
		return buf.toString();
	}

	/**
	 * Change to executable permissions for non-windows machines.
	 */
	public static void changePermissions(File file)
	{
		FileUtils.setExecutablePermissions(file);
	}
}
