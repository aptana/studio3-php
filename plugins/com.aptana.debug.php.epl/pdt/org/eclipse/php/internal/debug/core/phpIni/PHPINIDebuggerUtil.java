
package org.eclipse.php.internal.debug.core.phpIni;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.php.internal.debug.core.preferences.PHPexes;

public class PHPINIDebuggerUtil {

	private static final String PHP_INI_FILE = "php.ini"; //$NON-NLS-1$
	private static final String INCLUDE_PATH = "include_path"; //$NON-NLS-1$
	private static final String ZEND_EXTENSION = "zend_extension"; //$NON-NLS-1$
	private static final String ZEND_EXTENSION_TS = "zend_extension_ts"; //$NON-NLS-1$
	private static final String EXTENSIONS_DIR = "extension_dir"; //$NON-NLS-1$

	private static void modifyDebuggerExtensionPath(File phpIniFile, String extensionPath, boolean isXdebug) {
		try {
			INIFileModifier m = new INIFileModifier(phpIniFile);
			if (isXdebug) {
				if (Platform.OS_WIN32.equals(Platform.getOS())) {
					if (m.removeAllEntries(ZEND_EXTENSION_TS, Pattern.quote("..\\php_xdebug\\php_xdebug.dll"))) { //$NON-NLS-1$
						m.addEntry(ZEND_EXTENSION_TS, extensionPath);
					}
				} else {
					if (m.removeAllEntries(ZEND_EXTENSION, Pattern.quote("../php_xdebug/xdebug.so"))) { //$NON-NLS-1$
						m.addEntry(ZEND_EXTENSION, extensionPath);
					}
					if (m.removeAllEntries(ZEND_EXTENSION_TS, Pattern.quote("../php_xdebug/xdebug.so"))) { //$NON-NLS-1$
						m.addEntry(ZEND_EXTENSION_TS, extensionPath);
					}
				}
			} else {
				if (Platform.OS_WIN32.equals(Platform.getOS())) {
					if (m.removeAllEntries(ZEND_EXTENSION_TS, Pattern.quote("..\\php_zend_debugger\\ZendDebugger.dll"))) { //$NON-NLS-1$
						m.addEntry(ZEND_EXTENSION_TS, extensionPath);
					}
				} else {
					if (m.removeAllEntries(ZEND_EXTENSION, Pattern.quote("../php_zend_debugger/ZendDebugger.so"))) { //$NON-NLS-1$
						m.addEntry(ZEND_EXTENSION, extensionPath);
					}
					if (m.removeAllEntries(ZEND_EXTENSION_TS, Pattern.quote("../php_zend_debugger/ZendDebugger.so"))) { //$NON-NLS-1$
						m.addEntry(ZEND_EXTENSION_TS, extensionPath);
					}
				}
			}
			m.close();
		} catch (IOException e) {
			Activator.log(e);
		}
	}

	private static void modifyExtensionsDirectoryPath(File phpIniFile, String extensionsDirPath) {
		try {
			INIFileModifier m = new INIFileModifier(phpIniFile);
			if (m.removeAllEntries(EXTENSIONS_DIR)) { 
				m.addEntry(EXTENSIONS_DIR, '\"' + extensionsDirPath + '\"');
			}
			m.close();
		} catch (IOException e) {
			Activator.log(e);
		}
	}
	
	/**
	 * Make some preparations before debug session:
	 * <ul>
	 * 	<li>Adds include path
	 * 	<li>Modifies Zend Debugger path in the PHP configuration file
	 * </ul>
	 *
	 * @param phpIniFile PHP configuration file instance
	 * @param phpExePath Path to the PHP Interpreter
	 * @param project Current project
	 * @return created temporary PHP configuration file
	 */
	public static File prepareBeforeDebug(File phpIniFile, String phpExePath, IProject project, String debuggerID) {
		File tempIniFile = PHPINIUtil.createTemporaryPHPINIFile(phpIniFile);

		// Modify include path:
		if (project != null) {
			Object[] path = PHPSearchEngine.buildIncludePath(project);
			List<String> includePath = new ArrayList<String>(path.length);
			for (Object pathObject : path) {
				if (pathObject instanceof IIncludePathEntry) {
					IIncludePathEntry entry = (IIncludePathEntry) pathObject;
					IPath entryPath = entry.getPath();
					if (entry.getEntryKind() == IIncludePathEntry.IPE_VARIABLE) {
						entryPath = IncludePathVariableManager.instance().resolveVariablePath(entryPath.toString());
					}
					if (entryPath != null) {
						includePath.add(entryPath.toFile().getAbsolutePath());
					}
				} else if (pathObject instanceof IContainer) {
					IContainer container = (IContainer) pathObject;
					IPath location = container.getLocation();
					if (location != null) {
						includePath.add(location.toOSString());
					}
				} else {
					includePath.add(pathObject.toString());
				}
			}
			includePath.addAll(PHPIncludePathUtils.getInterpreterIncludePath(project));
			PHPINIUtil.modifyIncludePath(tempIniFile, includePath.toArray(new String[includePath.size()]));
		}

		if (phpIniFile != null) {
			if (PHPexes.XDEBUG_DEBUGGER_ID.equals(debuggerID)) {
				// Modify the XDebug Debugger extension entry
				File debuggerFile = new File(phpIniFile.getParentFile(), Platform.OS_WIN32.equals(Platform.getOS()) ? "php_xdebug.dll" : "xdebug.so"); //$NON-NLS-1$ //$NON-NLS-2$
				if (debuggerFile.exists()) {
					modifyDebuggerExtensionPath(tempIniFile, '"' + debuggerFile.getAbsolutePath() + '"', true);
				}
			} else if (PHPexes.ZEND_DEBUGGER_ID.equals(debuggerID)) {
				// Modify Zend Debugger extension entry:
				File debuggerFile = new File(phpIniFile.getParentFile(), Platform.OS_WIN32.equals(Platform.getOS()) ? "ZendDebugger.dll" : "ZendDebugger.so"); //$NON-NLS-1$ //$NON-NLS-2$
				if (debuggerFile.exists()) {
					modifyDebuggerExtensionPath(tempIniFile, '"' + debuggerFile.getAbsolutePath() + '"', false);
				} 
			}
			// Modify extensions directory entry:
			File extensionsDirectory = new File(phpIniFile.getParentFile().getParentFile(), "php5/ext");//$NON-NLS-1$ 
			if(extensionsDirectory.exists()) {
				modifyExtensionsDirectoryPath(tempIniFile, extensionsDirectory.getAbsolutePath());
			}
		}

		return tempIniFile;
	}
}

