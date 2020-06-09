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

package org2.eclipse.php.internal.debug.ui.launching.exe;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.internal.editors.text.NonExistingFileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org2.eclipse.php.debug.core.debugger.parameters.IDebugParametersKeys;
import org2.eclipse.php.internal.debug.core.IPHPDebugConstants;
import org2.eclipse.php.internal.debug.core.debugger.AbstractDebuggerConfiguration;
import org2.eclipse.php.internal.debug.core.interpreter.preferences.PHPexeItem;
import org2.eclipse.php.internal.debug.core.preferences.PHPDebugCorePreferenceNames;
import org2.eclipse.php.internal.debug.core.preferences.PHPDebuggersRegistry;
import org2.eclipse.php.internal.debug.core.preferences.PHPProjectPreferences;
import org2.eclipse.php.internal.debug.core.preferences.PHPexes;
import org2.eclipse.php.internal.debug.core.sourcelookup.containers.LocalFileStorage;
import org2.eclipse.php.internal.debug.ui.PHPDebugUIMessages;

import com.aptana.editor.php.internal.core.IPHPConstants;
import com.aptana.php.debug.core.IPHPDebugCorePreferenceKeys;
import com.aptana.php.debug.epl.PHPDebugEPLPlugin;

public class PHPExeLaunchShortcut implements ILaunchShortcut {

	/**
	 * PHPExeLaunchShortcut constructor.
	 */
	public PHPExeLaunchShortcut() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchShortcut#launch(org.eclipse.jface.viewers.ISelection, java.lang.String)
	 */
	public void launch(ISelection selection, String mode) {
		if (selection instanceof IStructuredSelection) {
			searchAndLaunch(((IStructuredSelection) selection).toArray(), mode, getPHPExeLaunchConfigType());
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchShortcut#launch(org.eclipse.ui.IEditorPart, java.lang.String)
	 */
	public void launch(IEditorPart editor, String mode) {
		IEditorInput input = editor.getEditorInput();
		IFile file = (IFile) input.getAdapter(IFile.class);
		if (file == null) {
			IPath path = null;
			if (input instanceof IStorageEditorInput) {
				IStorageEditorInput editorInput = (IStorageEditorInput) input;
				try {
					LocalFileStorage fileStorage = (LocalFileStorage) editorInput.getStorage();
					path = fileStorage.getFullPath();
				} catch (CoreException e) {
					PHPDebugEPLPlugin.logError(e);
				}
			} else if (input instanceof IPathEditorInput) {
				path = (((IPathEditorInput) input).getPath());
			} else if (input instanceof NonExistingFileEditorInput) {
				// handle untitled document debugging
				// first save the file to the disk and after that set the document as dirty
				try {
					if (editor instanceof ITextEditor) {
						ITextEditor textEditor = (ITextEditor) editor;
						final TextFileDocumentProvider documentProvider = (TextFileDocumentProvider) textEditor.getDocumentProvider();
						final IDocument document = documentProvider.getDocument(input);
						documentProvider.saveDocument(null, input, document, true);
						// set document dirty
						document.replace(0, 0, "");
					}
				} catch (Exception e) {
					PHPDebugEPLPlugin.logError(e);
				}
				path = ((NonExistingFileEditorInput) input).getPath(null);//Untitled dummy path
			}
			if (path != null) {
				// if (ExternalFilesRegistry.getInstance().isEntryExist(path.toOSString())) {
				//		 file = ExternalFilesRegistry.getInstance().getFileEntry(path.toOSString());
				// } else {
				//	     file = ExternalFileWrapper.createFile(path.toOSString());
				// }
				File systemFile = new File(path.toOSString());
				if (systemFile.exists()) {
					searchAndLaunch(new Object[] { systemFile }, mode, getPHPExeLaunchConfigType());
				}
			}
		} else {
			searchAndLaunch(new Object[] { file }, mode, getPHPExeLaunchConfigType());
		}
	}
	
	// TODO: SG - Revert this code to support the saving of a breakpoints
	//copy the given line breakpoints to the file of the given path
	//	private void copyBreakPoints(IPath newPath, int[] lineNumbers) throws CoreException {
	//		IResource resource = ResourcesPlugin.getWorkspace().getRoot().getFile(newPath);
	//		for (int i = 0; i < lineNumbers.length; i++) {
	//			DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(PHPDebugTarget.createBreakpoint(resource, lineNumbers[i]));
	//		}
	//	}

	//reteive all the line numbers of breakpoints that exist within the file in the given path 
	//	private int[] getBreakpointLines(IPath path) throws CoreException {
	//		IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(IPHPDebugConstants.ID_PHP_DEBUG_CORE);
	//		ArrayList<Integer> list = new ArrayList<Integer>();
	//		for (int i = 0; i < breakpoints.length; i++) {
	//			PHPConditionalBreakpoint breakPoint = (PHPConditionalBreakpoint) breakpoints[i];
	//			if (breakPoint.getRuntimeBreakpoint().getFileName().equals(path.toString())) {
	//				list.add(breakPoint.getLineNumber());
	//
	//			}
	//		}
	//		int[] result = new int[list.size()];
	//		for (int i = 0; i < result.length; i++) {
	//			result[i] = list.get(i);
	//		}
	//		return result;
	//	}

	protected ILaunchConfigurationType getPHPExeLaunchConfigType() {
		ILaunchManager lm = DebugPlugin.getDefault().getLaunchManager();
		return lm.getLaunchConfigurationType(IPHPDebugConstants.PHPEXELaunchType);
	}

	public static void searchAndLaunch(Object[] search, String mode, ILaunchConfigurationType configType) {
		int entries = search == null ? 0 : search.length;
		for (int i = 0; i < entries; i++) {
			try {
				String phpPathString = null;
				String phpFileLocation = null;
				IProject project = null;
				Object obj = search[i];

				//TODO: if IProject, offer choices?
//				if (obj instanceof PHPCodeData) {
//					obj = PHPModelUtil.getResource(obj);
//				}
				//FIXME
				if (obj instanceof IFile) {
					IFile file = (IFile) obj;
					project = file.getProject();
					IContentType contentType = Platform.getContentTypeManager().getContentType(IPHPConstants.CONTENT_TYPE_HTML_PHP);
					if (contentType.isAssociatedWith(file.getName())) {
						if (new File(file.getFullPath().toOSString()).exists()) {
							phpPathString = file.getFullPath().toOSString();
						} else {
							phpPathString = file.getFullPath().toString();
						}
						IPath location = file.getLocation();
						//check for non null values - EFS issues
						if (location != null) {
							phpFileLocation = location.toOSString();
						} else {
							phpFileLocation = file.getFullPath().toString();
						}
					}
				} else if (obj instanceof File) {
					File systemFile = (File) obj;
					phpPathString = systemFile.getAbsolutePath();
					phpFileLocation = phpPathString;
				}

				if (phpPathString == null) {
					// Could not find target to launch
					throw new CoreException(new Status(IStatus.ERROR, PHPDebugEPLPlugin.PLUGIN_ID, IStatus.OK, PHPDebugUIMessages.launch_failure_no_target, null));
				}

				PHPexeItem defaultEXE = getDefaultPHPExe(project);
				if (defaultEXE == null) {
					defaultEXE = getWorkspaceDefaultExe();
				}
				String phpExeName = (defaultEXE != null) ? defaultEXE.getExecutable().getAbsolutePath().toString() : null;

				if (phpExeName == null) {
					MessageDialog.openError(PHPDebugEPLPlugin.getActiveWorkbenchShell(), PHPDebugUIMessages.launch_noexe_msg_title, PHPDebugUIMessages.launch_noexe_msg_text);
					PreferencesUtil.createPreferenceDialogOn(PHPDebugEPLPlugin.getActiveWorkbenchShell(), "org2.eclipse.php.debug.ui.preferencesphps.PHPsPreferencePage", null, null).open();
					return;
				}

				// Launch the app
				ILaunchConfiguration config = findLaunchConfiguration(project, phpPathString, phpFileLocation, defaultEXE, mode, configType);
				if (config != null) {
					DebugUITools.launch(config, mode);
				} else {
					// Could not find launch configuration
					throw new CoreException(new Status(IStatus.ERROR, PHPDebugEPLPlugin.PLUGIN_ID, IStatus.OK, PHPDebugUIMessages.launch_failure_no_config, null));
				}
			} catch (CoreException ce) {
				final IStatus stat = ce.getStatus();
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						ErrorDialog.openError(PHPDebugEPLPlugin.getActiveWorkbenchShell(), PHPDebugUIMessages.launch_failure_msg_title, PHPDebugUIMessages.launch_failure_exec_msg_text, stat);
					}
				});
			}
		}
	}

	private static PHPexeItem getWorkspaceDefaultExe() {
		String phpDebuggerId = PHPDebugEPLPlugin.getCurrentDebuggerId();
		return PHPexes.getInstance().getDefaultItem(phpDebuggerId);
	}

	// Returns the default php executable name for the current project. 
	// In case the project does not have any special settings, return the workspace default.
	private static PHPexeItem getDefaultPHPExe(IProject project) {
		// Take the default workspace item for the debugger's id.
		String phpDebuggerId = PHPDebugEPLPlugin.getCurrentDebuggerId();
		PHPexeItem defaultItem = PHPexes.getInstance().getDefaultItem(phpDebuggerId);
		if (defaultItem == null) {
			// We have no executable defined for this debugger. 
			return null;
		}
		String phpExe = defaultItem.getName();
		if (project != null) {
			// In case that the project is not null, check that we have project-specific settings for it.
			// Otherwise, map it to the workspace default server.
			IScopeContext[] preferenceScopes = createPreferenceScopes(project);
			if (preferenceScopes[0] instanceof ProjectScope) {
				IEclipsePreferences node = preferenceScopes[0].getNode(PHPProjectPreferences.getPreferenceNodeQualifier());
				if (node != null) {
					// Replace the workspace defaults with the project-specific settings.
					phpDebuggerId = node.get(IPHPDebugCorePreferenceKeys.PHP_DEBUGGER_ID, phpDebuggerId);
					phpExe = node.get(PHPDebugCorePreferenceNames.DEFAULT_PHP, phpExe);
				}
			}
		}
		return PHPexes.getInstance().getItem(phpDebuggerId, phpExe);
	}

	// Creates a preferences scope for the given project.
	// This scope will be used to search for preferences values.
	private static IScopeContext[] createPreferenceScopes(IProject project) {
		if (project != null) {
			return new IScopeContext[] { new ProjectScope(project), new InstanceScope(), new DefaultScope() };
		}
		return new IScopeContext[] { new InstanceScope(), new DefaultScope() };
	}

	/**
	 * Locate a configuration to relaunch for the given type.  If one cannot be found, create one.
	 * 
	 * @return a re-useable config or <code>null</code> if none
	 */
	protected static ILaunchConfiguration findLaunchConfiguration(IProject phpProject, String phpPathString, String phpFileFullLocation, PHPexeItem defaultEXE, String mode, ILaunchConfigurationType configType) {
		ILaunchConfiguration config = null;

		try {
			ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(configType);

			int numConfigs = configs == null ? 0 : configs.length;
			for (int i = 0; i < numConfigs; i++) {
				boolean usFile=configs[i].getAttribute(IPHPDebugCorePreferenceKeys.ATTR_USE_SPECIFIC_FILE, false);
				if (!usFile){
					continue;
				}
				String fileName = configs[i].getAttribute(IPHPDebugCorePreferenceKeys.ATTR_FILE, (String) null);
				String exeName =  configs[i].getAttribute(IPHPDebugConstants.ATTR_EXECUTABLE_LOCATION, (String) null);
				String iniPath =  configs[i].getAttribute(IPHPDebugConstants.ATTR_INI_LOCATION, (String) null);
				PHPexeItem item = PHPexes.getInstance().getItemForFile(exeName, iniPath);

				if (phpPathString.equals(fileName) && defaultEXE.equals(item)) {
					config = configs[i];
					break;
				}
			}

			if (config == null) {
				config = createConfiguration(phpProject, phpPathString, phpFileFullLocation, defaultEXE, configType);
			}
		} catch (CoreException ce) {
			ce.printStackTrace();
		}
		return config;
	}

	/**
	 * Create & return a new configuration
	 */
	protected static ILaunchConfiguration createConfiguration(IProject phpProject, String phpPathString, String phpFileFullLocation, PHPexeItem defaultEXE, ILaunchConfigurationType configType) throws CoreException {
		ILaunchConfiguration config = null;
		ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, getNewConfigurationName(phpPathString));

		// Set the delegate class according to selected executable.
		wc.setAttribute(IPHPDebugCorePreferenceKeys.PHP_DEBUGGER_ID, defaultEXE.getDebuggerID());
		AbstractDebuggerConfiguration debuggerConfiguration = PHPDebuggersRegistry.getDebuggerConfiguration(defaultEXE.getDebuggerID());
		wc.setAttribute(IPHPDebugCorePreferenceKeys.CONFIGURATION_DELEGATE_CLASS, debuggerConfiguration.getScriptLaunchDelegateClass());
		wc.setAttribute(IPHPDebugCorePreferenceKeys.ATTR_FILE, phpPathString);
		wc.setAttribute(IPHPDebugCorePreferenceKeys.ATTR_USE_SPECIFIC_FILE, true);
		wc.setAttribute(IPHPDebugConstants.ATTR_FILE_FULL_PATH, phpFileFullLocation);
		wc.setAttribute(IPHPDebugConstants.ATTR_EXECUTABLE_LOCATION, defaultEXE.getExecutable().getAbsolutePath().toString());
		String iniPath = defaultEXE.getINILocation() != null ? defaultEXE.getINILocation().toString() : null;
		wc.setAttribute(IPHPDebugConstants.ATTR_INI_LOCATION, iniPath);
		wc.setAttribute(IPHPDebugConstants.RUN_WITH_DEBUG_INFO, PHPDebugEPLPlugin.getDebugInfoOption());
		wc.setAttribute(IDebugParametersKeys.FIRST_LINE_BREAKPOINT, PHPProjectPreferences.getStopAtFirstLine(phpProject));

		config = wc.doSave();

		return config;
	}

	/**
	 * Returns a name for a newly created launch configuration according to the given file name.
	 * In case the name generation fails, return the "New_configuration" string.
	 * 
	 * @param fileName	The original file name that this shortcut shoul execute.
	 * @return The new configuration name, or "New_configuration" in case it fails for some reason.
	 */
	protected static String getNewConfigurationName(String fileName) {
		String configurationName = "New_configuration";
		try {
			IPath path = Path.fromOSString(fileName);
			String fileExtention = path.getFileExtension();
			String lastSegment = path.lastSegment();
			if (lastSegment != null) {
				if (fileExtention != null) {
					lastSegment = lastSegment.replaceFirst("." + fileExtention, "");
				}
				configurationName = lastSegment;
			}
		} catch (Exception e) {
			PHPDebugEPLPlugin.logError("Could not generate configuration name for " + fileName + ".\nThe default name will be used.", e);
		}
		return DebugPlugin.getDefault().getLaunchManager().generateUniqueLaunchConfigurationNameFrom(configurationName);
	}
}
