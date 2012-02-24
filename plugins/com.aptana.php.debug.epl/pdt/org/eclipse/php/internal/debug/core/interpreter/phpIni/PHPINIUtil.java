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
package org.eclipse.php.internal.debug.core.interpreter.phpIni;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.internal.filesystem.local.LocalFile;
import org.eclipse.core.runtime.NullProgressMonitor;

import com.aptana.editor.php.util.StringUtils;
import com.aptana.php.debug.epl.PHPDebugEPLPlugin;
import com.aptana.php.debug.ui.phpIni.PHPIniEntry;

public class PHPINIUtil {

	// Hold a queue of the last 5 temporary dirs that were created for the debug/smarty sessions.
	// The rest of the files should be deleted.
	private static LinkedList<String> sessionsTempDirs = new LinkedList<String>();
	private static final int MAX_SESSIONS_TO_HOLD = 5;
	
	private static final String PHP_INI_FILE = "php.ini"; //$NON-NLS-1$
	private static final String INCLUDE_PATH = "include_path"; //$NON-NLS-1$

	/**
	 * Modify the include path to include a concatenation of the php.ini existing include_path directive and the
	 * given include path array items.
	 * 
	 * @param phpIniFile The php.ini file reference
	 * @param includePath An array of include paths
	 */
	public static void modifyIncludePath(File phpIniFile, String[] includePath) {
		try {
			INIFileModifier m = new INIFileModifier(phpIniFile);
			StringBuilder valueBuf = new StringBuilder("\""); //$NON-NLS-1$
			PHPIniEntry includeEntry = m.getEntryByName(INCLUDE_PATH);
			String iniIncludes = (includeEntry != null) ? includeEntry.getValue() : ""; //$NON-NLS-1$
			iniIncludes = StringUtils.trimStringQuotes(iniIncludes);
			if (!containsCurrentDir(iniIncludes))
			{
				if (iniIncludes.length() > 0)
				{
					valueBuf.append('.').append(File.pathSeparatorChar).append(iniIncludes);
				}
				else
				{
					// just add the dot
					valueBuf.append('.');
				}
			}
			else
			{
				valueBuf.append(iniIncludes);
			}
			for (String path : includePath) {
				valueBuf.append(File.pathSeparatorChar).append(path);
			}
			valueBuf.append('\"');
			m.removeAllEntries(INCLUDE_PATH);
			m.addEntry(INCLUDE_PATH, valueBuf.toString());
			m.flush();
		} catch (IOException e) {
			PHPDebugEPLPlugin.logError("IOException occured", e);//$NON-NLS-1$
		}
	}

	private static boolean containsCurrentDir(String path)
	{
		String[] pathSegments = path.split(File.pathSeparator);
		for (String segment : pathSegments)
		{
			if (".".equals(segment)) //$NON-NLS-1$
			{
				return true;
			}
		}
		return false;
	}
	/**
	 * Creates temporary PHP configuration file and returns its instance of <code>null</code> in case of error.
	 * This file will be removed when the program exits.
	 *
	 * @return temporary PHP configuration file instance
	 */
	public static File createTemporaryPHPINIFile() {
		return createTemporaryPHPINIFile(null);
	}

	/**
	 * Creates temporary PHP configuration file and returns its instance of <code>null</code> in case of error.
	 * This file will be removed when the program exits.
	 *
	 * @param originalPHPIniFile If specified - its contents will be copied to the temporary file
	 * @return temporary PHP configuration file instance
	 */
	public static File createTemporaryPHPINIFile(File originalPHPIniFile) {
		File phpIniFile = null;
		try {
			// Create temporary directory:
			File tempDir = new File(System.getProperty("java.io.tmpdir"), "aptana_debug"); //$NON-NLS-1$ //$NON-NLS-2$
			if (!tempDir.exists()) {
				tempDir.mkdir();
				tempDir.deleteOnExit();
			}
			final File sessionDir = File.createTempFile("session", null, tempDir); //$NON-NLS-1$
			sessionDir.delete(); // delete temp file
			sessionDir.mkdir();
			sessionDir.deleteOnExit();
			if (sessionsTempDirs.size() == MAX_SESSIONS_TO_HOLD)
			{
				String toDelete = sessionsTempDirs.poll();
				try
				{
					// Delete the other temp dirs
					File tempDirToDelete = new File(tempDir, toDelete);
					if (tempDirToDelete.exists())
					{
						File[] files = tempDirToDelete.listFiles();
						for (File innerFile : files)
						{
							innerFile.delete();
						}
						tempDirToDelete.delete();
					}
				}
				catch (Exception e)
				{
					// Do nothing. These files are marked for deletion anyway.
				}

			}
			// Create empty configuration file:
			phpIniFile = new File(sessionDir, PHP_INI_FILE);
			phpIniFile.createNewFile();
			phpIniFile.deleteOnExit();
			sessionsTempDirs.add(sessionDir.getName());
			if (originalPHPIniFile != null && originalPHPIniFile.exists()) {
				new LocalFile(originalPHPIniFile).copy(new LocalFile(phpIniFile), EFS.OVERWRITE, new NullProgressMonitor());
			}
		} catch (Exception e) {
			PHPDebugEPLPlugin.logError("IOException occured", e);//$NON-NLS-1$
		}
		return phpIniFile;
	}

	/**
	 * Locate and return a PHP configuration file path for the given PHP executable.
	 * The locating is done by trying to return a PHP configuration file that is located next to the executable.
	 * The return value can be null in case it fails to locate a valid file.
	 *
	 * @param phpExe The PHP executable path.
	 * @return A PHP configuration file path, or <code>null</code> if it fails.
	 */
	public static File findPHPIni(String phpExe) {
		File phpExeFile = new File(phpExe);
		File phpIniFile = new File(phpExeFile.getParentFile(), PHP_INI_FILE);

		if (!phpIniFile.exists() || !phpIniFile.canRead()) {
			// Try to detect via library:
			try {
				Process p = Runtime.getRuntime().exec(new String[] { phpExeFile.getAbsolutePath(), "-i"});//$NON-NLS-1$
				BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String l;
				while ((l = r.readLine()) != null) {
					int i = l.indexOf(" => ");//$NON-NLS-1$
					if (i > 0) {
						String key = l.substring(0, i);
						String value = l.substring(i + 4);
						if ("Loaded Configuration File".equals(key)) {//$NON-NLS-1$
							phpIniFile = new File(value);
							break;
						}
					}
				}
				r.close();
			} catch (IOException e) {
			}
		}
		
		if (phpIniFile.exists() && phpIniFile.canRead()) {
			return phpIniFile;
		}
		
		return null;
	}
}
