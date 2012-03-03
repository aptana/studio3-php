/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.ui.php_ini;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.FileUtil;
import com.aptana.php.debug.IDebugScopes;
import com.aptana.php.debug.PHPDebugPlugin;
import com.aptana.php.debug.core.PHPDebugSupportManager;
import com.aptana.php.debug.core.util.FileUtils;

/**
 * A class that is used to validate the extensions directives in the php.ini. The class executes the PHP process and
 * parses the error output to determine which of the extensions is causing an error and which causes warnings. Every
 * error or warning is registered into the PHPIniEntrys that the INIFileModifier is holding.
 * 
 * @author Shalom G
 */
// $codepro.audit.disable assignmentInCondition
public class PHPIniValidator
{
	private static final String LOADING_ERROR = "unable to load"; //$NON-NLS-1$
	private final PHPIniContentProvider provider;
	private final String phpExePath;
	private String libraryPath;
	private File iniFile;
	private boolean isRunning;
	private Object lock = new Object();
	private List<PHPIniEntry> faultingExtensions;
	private Map<String, PHPIniEntry> mappedEntries;
	private boolean validationCanceled;
	private boolean validationCompleted;
	private final String debuggerID;

	/**
	 * Constructs a new PHPIniValidator.
	 * 
	 * @param provider
	 * @param phpExePath
	 */
	public PHPIniValidator(PHPIniContentProvider provider, String phpExePath, String debuggerID)
	{
		this.provider = provider;
		this.phpExePath = phpExePath;
		this.debuggerID = debuggerID;
		File exePath = new File(phpExePath);
		libraryPath = exePath.getParent();
		iniFile = new File(provider.getFileName());
		faultingExtensions = new ArrayList<PHPIniEntry>(5);
		FileUtils.setExecutablePermissions(exePath);
		loadActiveExtensions();
	}

	/**
	 * Map the extensions that are currently active in the php.ini
	 */
	private void loadActiveExtensions()
	{
		mappedEntries = new HashMap<String, PHPIniEntry>();
		List<INIFileSection> sections = provider.getSections();
		for (INIFileSection section : sections)
		{
			List<PHPIniEntry> entries = section.getEntries();
			for (PHPIniEntry entry : entries)
			{
				if (entry.isExtensionEntry())
				{
					String extensionValue = entry.getValue();
					if (extensionValue.endsWith(".so")) //$NON-NLS-1$
					{
						extensionValue = extensionValue.substring(0, extensionValue.length() - 3);
					}
					else if (extensionValue.endsWith(".dll")) //$NON-NLS-1$
					{
						extensionValue = extensionValue.substring(0, extensionValue.length() - 4);
					}
					mappedEntries.put(extensionValue, entry);
				}
			}
		}

	}

	/**
	 * Validate the php.ini directives.
	 */
	public void validate()
	{
		synchronized (lock)
		{
			if (isRunning)
			{
				return;
			}
			isRunning = true;
		}
		List<PHPIniEntry> extensions = new ArrayList<PHPIniEntry>();
		extensions.addAll(mappedEntries.values());
		// Mark all entries as unknown.
		for (PHPIniEntry entry : extensions)
		{
			entry.setValidationState(PHPIniEntry.PHP_EXTENSION_VALIDATION_UNKNOWN, null);
		}
		Shell activeShell = PHPDebugPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
		try
		{
			IRunnableWithProgress op = new ExtensionsValidatorRunnable(extensions);
			ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(activeShell);
			progressMonitorDialog.run(true, true, op);
		}
		catch (InvocationTargetException e)
		{
			// handle exception
			IdeLog.logError(PHPDebugPlugin.getDefault(),
					"Error while validating the PHP extensions", e, IDebugScopes.DEBUG); //$NON-NLS-1$
		}
		catch (InterruptedException e) // $codepro.audit.disable emptyCatchClause
		{
			// handle cancellation in the finally block

		}
		finally
		{
			// Revert the changes made to the ini
			for (PHPIniEntry entry : faultingExtensions)
			{
				provider.uncommentEntry(entry);
			}
			faultingExtensions.clear();
			// Mark all the unknown entries as resolved.
			for (PHPIniEntry entry : mappedEntries.values())
			{
				if (validationCanceled)
				{
					entry.setValidationState(PHPIniEntry.PHP_EXTENSION_VALIDATION_UNKNOWN, null);
				}
				else if (entry.getValidationState() == PHPIniEntry.PHP_EXTENSION_VALIDATION_UNKNOWN)
				{
					entry.setValidationState(PHPIniEntry.PHP_EXTENSION_VALIDATION_OK, null);
				}
			}
			try
			{
				provider.save();
			}
			catch (IOException e)
			{
				IdeLog.logError(PHPDebugPlugin.getDefault(), "Error while saving the php.ini configuration.", e); //$NON-NLS-1$
			}
			synchronized (lock)
			{
				isRunning = false;
			}
		}
	}

	private class ExtensionsValidatorRunnable implements IRunnableWithProgress
	{

		private final List<PHPIniEntry> extensions;

		ExtensionsValidatorRunnable(List<PHPIniEntry> extensions)
		{
			this.extensions = extensions;
		}

		public void run(IProgressMonitor monitor)
		{
			monitor.beginTask(Messages.PHPIniValidator_validatingExtensionTaskName, extensions.size());

			try
			{
				innerValidate(extensions, monitor);
			}
			catch (IOException e)
			{
				monitor.done();
				IdeLog.logError(PHPDebugPlugin.getDefault(),
						"Error while validating the PHP extensions", e, IDebugScopes.DEBUG); //$NON-NLS-1$
				MessageDialog.openError(null, Messages.PHPIniValidator_errorTitle,
						Messages.PHPIniValidator_extensionValidationErrorMessage);
			}
			finally
			{
				monitor.done();
				validationCompleted = true;
			}
		}
	}

	/*
	 * Do the validation in a recursively until the process is loaded without any fatal errors.
	 */
	private void innerValidate(List<PHPIniEntry> extensions, IProgressMonitor monitor) throws IOException
	{
		validationCanceled = false;
		validationCompleted = false;
		List<String> processExecutionResults = getProcessExecutionResults(monitor);
		if (processExecutionResults == null)
		{
			// the user canceled, or we had an error validating.
			return;
		}
		if (processExecutionResults.isEmpty())
		{
			// no errors / warnings
			monitor.worked(extensions.size());
		}
		else
		{
			List<String> fatalErrors = extractFatalErrors(processExecutionResults);
			if (fatalErrors.isEmpty())
			{
				// we only have warnings and can move on
				markEntriesWithWarning(processExecutionResults);
				monitor.worked(extensions.size());
			}
			else
			{
				// remove the faulting extension, and try again.
				// this process can take several times until we get no fatal errors.
				monitor.worked(1);
				if (!monitor.isCanceled())
				{
					removeFaultingEntries(fatalErrors, extensions, monitor);
				}
			}
		}
	}

	/**
	 * Mark the PHPIniEntries with a warning
	 * 
	 * @param processExecutionResults
	 */
	private void markEntriesWithWarning(List<String> warnings)
	{
		markMatchingEntries(warnings, false, null);
	}

	private void removeFaultingEntries(List<String> fatalErrors, List<PHPIniEntry> extensions, IProgressMonitor monitor)
			throws IOException
	{
		markMatchingEntries(fatalErrors, true, extensions);
		provider.save();
		innerValidate(extensions, monitor);
	}

	/*
	 * Search for any reference for the PHPIniEntries in the error/warning lines and mark them with error/warning. (In
	 * case of errors, the entry is commented out) @param processOutputLines @param isError If true, mark the
	 * PHPIniEntries as errors; Otherwise, mark them as warnings. @param extensions When isError==true this parameter
	 * must not be null.
	 */
	private void markMatchingEntries(List<String> processOutputLines, boolean isError, List<PHPIniEntry> extensions)
	{
		// Check for each of the .so/.dll entries in the ini
		for (String line : processOutputLines)
		{
			for (String entryValue : mappedEntries.keySet())
			{
				String testEntry = entryValue;
				if (testEntry.toLowerCase().startsWith("php_")) //$NON-NLS-1$ 
				{
					testEntry = testEntry.substring(4);
				}
				if (line.indexOf(testEntry) > -1 || line.indexOf(entryValue) > -1)
				{
					PHPIniEntry iniEntry = mappedEntries.get(entryValue);
					if (isError)
					{
						iniEntry.setValidationState(PHPIniEntry.PHP_EXTENSION_VALIDATION_ERROR, line);
						faultingExtensions.add(iniEntry);
						extensions.remove(iniEntry);
						provider.commentEntry(iniEntry);
					}
					else
					{
						// Some of the warnings can also indicate errors which are not fatal to the process.
						// Mark these warnings as errors.
						if (line.toLowerCase().indexOf(LOADING_ERROR) > -1)
						{
							iniEntry.setValidationState(PHPIniEntry.PHP_EXTENSION_VALIDATION_ERROR, line);
						}
						else
						{
							iniEntry.setValidationState(PHPIniEntry.PHP_EXTENSION_VALIDATION_WARNING, line);
						}
					}
					break;
				}
			}
		}
	}

	/*
	 * Execute the PHP process and collect the errors and the warnings into an array of strings.
	 */
	private List<String> getProcessExecutionResults(final IProgressMonitor monitor) throws IOException
	{
		File tempIniFile = PHPDebugSupportManager.getLaunchSupport().generatePhpIni(this.iniFile, phpExePath, null,
				debuggerID);

		ProcessBuilder builder = new ProcessBuilder(phpExePath, "-c", tempIniFile.getAbsolutePath(), "-i"); //$NON-NLS-1$ //$NON-NLS-2$
		// builder.environment().put("PHPRC", tempIniFile.getParent()); //$NON-NLS-1$
		if (!Platform.OS_WIN32.equals(Platform.getOS()))
		{
			if (Platform.OS_MACOSX.equals(Platform.getOS()))
			{
				builder.environment().put("DYLD_LIBRARY_PATH", libraryPath); //$NON-NLS-1$
			}
			else
			{
				builder.environment().put("LD_LIBRARY_PATH", libraryPath); //$NON-NLS-1$
			}
		}
		builder.directory(new File(phpExePath).getParentFile());

		final Process process = builder.start();

		Job monitorCancelListener = new Job("Validation Cancel Listener") //$NON-NLS-1$
		{
			protected IStatus run(IProgressMonitor monitor2)
			{
				int timeLimit = 60; // time limit for 1 minute
				while (--timeLimit > 0)
				{
					try
					{
						Thread.sleep(1000); // $codepro.audit.disable disallowSleepInsideWhile
					}
					catch (InterruptedException e) // $codepro.audit.disable emptyCatchClause
					{
						// ignore
					}
					if (validationCompleted)
					{
						return Status.CANCEL_STATUS;
					}
					if (monitor.isCanceled())
					{
						try
						{
							process.exitValue();
						}
						catch (IllegalThreadStateException itse)
						{
							process.destroy();
							validationCanceled = true;
							return Status.OK_STATUS;
						}
						return Status.CANCEL_STATUS;
					}
				}
				return Status.CANCEL_STATUS;
			}
		};
		monitorCancelListener.setProgressGroup(monitor, IProgressMonitor.UNKNOWN);
		monitorCancelListener.schedule();
		// Important - First read the output, then read the errors.
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String line = null;
			while (!monitor.isCanceled() && (line = reader.readLine()) != null)
			{
				if (PHPDebugPlugin.DEBUG)
				{
					// $codepro.audit.disable debuggingCode
					System.out.println(line);
				}
			}
		}
		finally
		{
			if (reader != null)
			{
				reader.close();
			}
		}
		if (monitor.isCanceled())
		{
			return null;
		}
		// Read the errors
		return getProcessErrorLines(process.getErrorStream());
	}

	/**
	 * Reads the process error stream and extract the PHP warnings and errors.
	 * 
	 * @param errorStream
	 * @return A string list of all the PHP-related warnings and errors from the error stream
	 * @throws IOException
	 */
	public static List<String> getProcessErrorLines(InputStream errorStream) throws IOException
	{
		List<String> lines = new ArrayList<String>();
		BufferedReader reader = null;
		try
		{
			reader = new BufferedReader(new InputStreamReader(errorStream));
			String line = null;
			while ((line = reader.readLine()) != null)
			{
				if (PHPDebugPlugin.DEBUG)
				{
					// $codepro.audit.disable debuggingCode
					System.err.println(line);
				}
				if (line.startsWith("PHP")) //$NON-NLS-1$
				{
					lines.add(line);
				}
				else if (line.startsWith("dyld:") && line.indexOf("error", 4) > -1) //$NON-NLS-1$ //$NON-NLS-2$
				{
					StringBuilder stringBuilder = new StringBuilder();
					// read the next 3 lines
					int linesCount = 3;
					while ((line = reader.readLine()) != null && linesCount-- > 0)
					{
						if (PHPDebugPlugin.DEBUG)
						{
							// $codepro.audit.disable debuggingCode
							System.err.println(line);
						}
						// just to make sure
						if (line.startsWith("PHP")) //$NON-NLS-1$
						{
							lines.add(line);
							break;
						}
						stringBuilder.append(FileUtil.NEW_LINE);
						stringBuilder.append(line);
						if (line.toLowerCase().startsWith("reason")) //$NON-NLS-1$
							break; // just to make sure
					}
					lines.add(stringBuilder.toString());
				}
			}
		}
		finally
		{
			if (reader != null)
			{
				reader.close();
			}
		}
		return lines;
	}

	/**
	 * Extract the fatal errors from the given list. The fatal errors are removed from the given list and returned in a
	 * new list.
	 * 
	 * @param processExecutionResults
	 *            The PHP process error and warning lines
	 * @return A list containing only the fatal errors.
	 */
	public static List<String> extractFatalErrors(List<String> processExecutionResults)
	{
		List<String> errors = new ArrayList<String>(processExecutionResults.size());
		List<String> warnings = new ArrayList<String>(processExecutionResults.size());
		for (String str : processExecutionResults)
		{
			String lowerCaseStr = str.toLowerCase();
			if (lowerCaseStr.indexOf(" fatal ") > -1 || lowerCaseStr.indexOf("dyld:") > -1) //$NON-NLS-1$ //$NON-NLS-2$ $NON-NLS-2$
			{
				errors.add(str);
			}
			else
			{
				warnings.add(str);
			}
		}
		// update the warnings
		processExecutionResults.clear();
		processExecutionResults.addAll(warnings);
		return errors;
	}
}
