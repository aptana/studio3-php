/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Zend Technologies
 *******************************************************************************/
package org2.eclipse.php.internal.debug.core.zend.model;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointManager;
import org.eclipse.debug.core.IBreakpointManagerListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IStepFilters;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org2.eclipse.php.debug.core.debugger.IDebugHandler;
import org2.eclipse.php.debug.core.debugger.parameters.IDebugParametersInitializer;
import org2.eclipse.php.debug.core.debugger.pathmapper.PathMapper;
import org2.eclipse.php.internal.debug.core.IPHPConsoleEventListener;
import org2.eclipse.php.internal.debug.core.IPHPDebugConstants;
import org2.eclipse.php.internal.debug.core.Logger;
import org2.eclipse.php.internal.debug.core.launching.PHPProcess;
import org2.eclipse.php.internal.debug.core.model.BreakpointSet;
import org2.eclipse.php.internal.debug.core.model.DebugOutput;
import org2.eclipse.php.internal.debug.core.model.IPHPDebugTarget;
import org2.eclipse.php.internal.debug.core.model.PHPConditionalBreakpoint;
import org2.eclipse.php.internal.debug.core.model.PHPDebugElement;
import org2.eclipse.php.internal.debug.core.model.PHPLineBreakpoint;
import org2.eclipse.php.internal.debug.core.model.PHPRunToLineBreakpoint;
import org2.eclipse.php.internal.debug.core.pathmapper.DebugSearchEngine;
import org2.eclipse.php.internal.debug.core.pathmapper.PathEntry;
import org2.eclipse.php.internal.debug.core.pathmapper.PathMapperRegistry;
import org2.eclipse.php.internal.debug.core.pathmapper.PathEntry.Type;
import org2.eclipse.php.internal.debug.core.zend.communication.DebugConnectionThread;
import org2.eclipse.php.internal.debug.core.zend.debugger.Breakpoint;
import org2.eclipse.php.internal.debug.core.zend.debugger.DebugError;
import org2.eclipse.php.internal.debug.core.zend.debugger.DebugHandlersRegistry;
import org2.eclipse.php.internal.debug.core.zend.debugger.DebugParametersInitializersRegistry;
import org2.eclipse.php.internal.debug.core.zend.debugger.DefaultExpressionsManager;
import org2.eclipse.php.internal.debug.core.zend.debugger.Expression;
import org2.eclipse.php.internal.debug.core.zend.debugger.IRemoteDebugger;
import org2.eclipse.php.internal.debug.core.zend.debugger.PHPSessionLaunchMapper;
import org2.eclipse.php.internal.debug.core.zend.debugger.RemoteDebugger;

import com.aptana.php.debug.core.IPHPDebugCorePreferenceKeys;
import com.aptana.php.debug.epl.PHPDebugEPLPlugin;

/**
 * PHP Debug Target
 */
public class PHPDebugTarget extends PHPDebugElement implements IPHPDebugTarget, IBreakpointManagerListener, IStepFilters {

	private ContextManager fContextManager;

	//use step filter or not
	boolean isStepFiltersEnabled;

	// containing launch object
	protected ILaunch fLaunch;
	protected IProcess fProcess;

	// program name
	protected String fName;
	protected String fURL;
	protected int fRequestPort;
	protected DebugOutput fOutput = new DebugOutput();

	// suspend state
	protected boolean fSuspended = false;

	// terminated state
	protected boolean fTerminated = false;
	protected boolean fTermainateCalled = false;

	// threads
	protected PHPThread fThread;
	protected IThread[] fThreads;
	protected IRemoteDebugger debugger;
	protected String fLastcmd;
	protected boolean fStatus;
	protected int fLastStop;
	protected String fLastFileName;
	protected boolean fIsPHPCGI;
	protected boolean fIsRunAsDebug;

	protected PHPResponseHandler fPHPResponseHandler;
	protected org2.eclipse.php.internal.debug.core.zend.debugger.Debugger.StartResponseHandler fStartResponseHandler;
	protected org2.eclipse.php.internal.debug.core.zend.debugger.Debugger.BreakpointAddedResponseHandler fBreakpointAddedResponseHandler;
	protected org2.eclipse.php.internal.debug.core.zend.debugger.Debugger.BreakpointRemovedResponseHandler fBreakpointRemovedResponseHandler;
	protected org2.eclipse.php.internal.debug.core.zend.debugger.Debugger.StepIntoResponseHandler fStepIntoResponseHandler;
	protected org2.eclipse.php.internal.debug.core.zend.debugger.Debugger.StepOverResponseHandler fStepOverResponseHandler;
	protected org2.eclipse.php.internal.debug.core.zend.debugger.Debugger.StepOutResponseHandler fStepOutResponseHandler;
	protected org2.eclipse.php.internal.debug.core.zend.debugger.Debugger.GoResponseHandler fGoResponseHandler;
	protected org2.eclipse.php.internal.debug.core.zend.debugger.Debugger.PauseResponseHandler fPauseResponseHandler;
	protected DefaultExpressionsManager expressionsManager;

	//	private IVariable[] fVariables;
	protected IProject fProject;
	protected int fSuspendCount;
	protected Vector<IPHPConsoleEventListener> fConsoleEventListeners = new Vector<IPHPConsoleEventListener>();
	protected Set<DebugError> fDebugErrors = new HashSet<DebugError>();
	protected StartLock fStartLock = new StartLock();
	protected BreakpointSet fBreakpointSet;
	protected IBreakpointManager fBreakpointManager;
	protected boolean fIsServerWindows = false;
	protected DebugConnectionThread fConnectionThread;

	/**
	 * Constructs a new debug target in the given launch for the associated PHP
	 * Debugger on a Apache Server.
	 *
	 * @param connectionThread 	The debug connection thread for the communication read and write processes.
	 * @param launch 			containing launch
	 * @param URL   			URL of the debugger
	 * @param requestPort		port to send requests to the bebugger *
	 * @exception CoreException if unable to connect to host
	 */
	public PHPDebugTarget(DebugConnectionThread connectionThread, ILaunch launch, String URL, int requestPort, IProcess process, boolean runAsDebug, boolean stopAtFirstLine, IProject project) throws CoreException {
		super(null);
		fConnectionThread = connectionThread;
		fURL = URL;
		fIsPHPCGI = false;

		initDebugTarget(launch, requestPort, process, runAsDebug, stopAtFirstLine, project);
	}

	/**
	 * Constructs a new debug target in the given launch for the associated PHP
	 * Debugger using PHP exe.
	 *
	 * @param connectionThread 	The debug connection thread for the communication read and write processes.
	 * @param launch 			containing launch
	 * @param String 			full path to the PHP executable
	 * @param requestPort 		port to send requests to the bebugger *
	 * @exception CoreException	if unable to connect to host
	 */
	public PHPDebugTarget(DebugConnectionThread connectionThread, ILaunch launch, String phpExe, IFile fileToDebug, int requestPort, IProcess process, boolean runAsDebug, boolean stopAtFirstLine, IProject project) throws CoreException {
		this(connectionThread, launch, phpExe, fileToDebug.getName(), requestPort, process, runAsDebug, stopAtFirstLine, project);
	}

	public PHPDebugTarget(DebugConnectionThread connectionThread, ILaunch launch, String phpExe, String fileToDebug, int requestPort, IProcess process, boolean runAsDebug, boolean stopAtFirstLine, IProject project) throws CoreException {
		super(null);
		fConnectionThread = connectionThread;
		fName = fileToDebug;
		fIsPHPCGI = true;
		initDebugTarget(launch, requestPort, process, runAsDebug, stopAtFirstLine, project);
	}

	/**
	 * Returns the DebugConnectionThread for this PHPDebugTarget.
	 * @return The DebugConnectionThread.
	 */
	public DebugConnectionThread getConnectionThread() {
		return fConnectionThread;
	}

	/*
	 * Initialize the debug target.
	 * @param launch
	 * @param requestPort
	 * @param process
	 * @param runAsDebug
	 * @param stopAtFirstLine
	 * @param project
	 * @throws CoreException
	 */
	private void initDebugTarget(ILaunch launch, int requestPort, IProcess process, boolean runAsDebug, boolean stopAtFirstLine, IProject project) throws CoreException {
		fLaunch = launch;
		fProcess = process;
		fIsRunAsDebug = runAsDebug;
		fProject = project;
		fProcess.setAttribute(IProcess.ATTR_PROCESS_TYPE, IPHPDebugConstants.PHPProcessType);
		((PHPProcess) fProcess).setDebugTarget(this);
		fRequestPort = requestPort;

		//		synchronized (fUsedPorts) {
		//			if (fUsedPorts.containsKey(String.valueOf(requestPort))) {
		//				Logger.debugMSG("PHPDebugTarget: Debug Port already in use");
		//				String errorMessage = PHPDebugCoreMessages.DebuggerDebugPortInUse_1;
		//				completeTerminated();
		//				throw new DebugException(new Status(IStatus.ERROR, PHPDebugPlugin.getID(), IPHPConstants.INTERNAL_ERROR, errorMessage, null));
		//			} else {
		//				fUsedPorts.put(String.valueOf(requestPort), new Integer(requestPort));
		//				fRequestPort = requestPort;
		//			}
		//		}

		fBreakpointSet = new BreakpointSet(project, fIsPHPCGI);

		IDebugHandler debugHandler = null;
		IDebugParametersInitializer parametersInitializer = DebugParametersInitializersRegistry.getBestMatchDebugParametersInitializer(launch);
		if (parametersInitializer != null) {
			String debugHandlerID = parametersInitializer.getDebugHandler();
			if (debugHandlerID != null) {
				try {
					debugHandler = DebugHandlersRegistry.getHandler(debugHandlerID);
				} catch (Exception e) {
					PHPDebugEPLPlugin.logError(e);
				}
			}
		}
		// If couldn't find contributed IDebugHandler - use default
		if (debugHandler == null) {
			debugHandler = new ServerDebugHandler();
		}
		debugHandler.setDebugTarget(this);
		debugger = debugHandler.getRemoteDebugger();

		fThread = new PHPThread(this);
		fThreads = new IThread[] { fThread };
		fTerminated = false;

		// create response handlers
		fPHPResponseHandler = new PHPResponseHandler(this);
		fStartResponseHandler = fPHPResponseHandler.new StartResponseHandler();
		fBreakpointAddedResponseHandler = fPHPResponseHandler.new BreakpointAddedResponseHandler();
		fBreakpointRemovedResponseHandler = fPHPResponseHandler.new BreakpointRemovedResponseHandler();
		fStepIntoResponseHandler = fPHPResponseHandler.new StepIntoResponseHandler();
		fStepOverResponseHandler = fPHPResponseHandler.new StepOverResponseHandler();
		fStepOutResponseHandler = fPHPResponseHandler.new StepOutResponseHandler();
		fGoResponseHandler = fPHPResponseHandler.new GoResponseHandler();
		fPauseResponseHandler = fPHPResponseHandler.new PauseResponseHandler();

		fSuspendCount = 0;
		fContextManager = new ContextManager(this, debugger);

		fBreakpointManager = DebugPlugin.getDefault().getBreakpointManager();
		fBreakpointManager.addBreakpointListener(this);
		fBreakpointManager.addBreakpointManagerListener(this);
	}

	public IProcess getProcess() {
		return fProcess;
	}

	public IRemoteDebugger getRemoteDebugger() {
		return debugger;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IDebugTarget#getThreads()
	 */
	public IThread[] getThreads() throws DebugException {
		return fThreads;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IDebugTarget#hasThreads()
	 */
	public boolean hasThreads() throws DebugException {
		return !fTerminated && fThreads.length > 0;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IDebugTarget#getName()
	 */
	public String getName() throws DebugException {
		if (fName == null) {
			fName = fURL;
		}
		return fName;
	}

	public String getURL() {
		return fURL;
	}

	int getLastStop() {
		return fLastStop;
	}

	String getLastFileName() {
		return fLastFileName;
	}

	int getSuspendCount() {
		return fSuspendCount;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IDebugTarget#supportsBreakpoint(org.eclipse.debug.core.model.IBreakpoint)
	 */
	public boolean supportsBreakpoint(IBreakpoint breakpoint) {
		if (breakpoint.getModelIdentifier().equals(IPHPDebugConstants.ID_PHP_DEBUG_CORE)) {
			boolean support = fBreakpointSet.supportsBreakpoint(breakpoint);
			return support;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IDebugElement#getDebugTarget()
	 */
	public IDebugTarget getDebugTarget() {
		return this;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IDebugElement#getLaunch()
	 */
	public ILaunch getLaunch() {
		return fLaunch;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	public boolean canTerminate() {
		return !fTerminated;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	public boolean isTerminated() {
		return fTerminated;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	public void terminate() throws DebugException {
		StartLock startLock = getStartLock();
		// Don't synchronize on lock, debugger may be hung. Just terminate.
		if (!startLock.isStarted()) {
			terminated();
			fTermainateCalled = true;
			return;
		}

		IThread[] threads = getThreads();
		if (threads != null && threads.length > 0) {
			((PHPThread) threads[0]).setStepping(false);
		}
		fTerminated = true;
		fSuspended = false;
		fLastcmd = "terminate";
		Logger.debugMSG("PHPDebugTarget: Calling closeDebugSession()");
		debugger.closeDebugSession();

		terminated();
		fTermainateCalled = true;
	}

	/**
	 * Called when this debug target terminates.
	 */
	public void terminated() {
		fTerminated = true;
		fSuspended = false;
		Logger.debugMSG("PHPDebugTarget: Calling debugger.closeConnection()");
		if (!fTermainateCalled) {
			debugger.closeConnection();
		}
		completeTerminated();
		PHPSessionLaunchMapper.updateSystemProperty(DebugPlugin.getDefault().getLaunchManager().getLaunches());
	}

	private void completeTerminated() {
		fTerminated = true;
		fSuspended = false;
		fThreads = new IThread[0];
		try {
			fProcess.terminate();
		} catch (DebugException e) {
			// PHPprocess doesn't throw this exception
		}
		Logger.debugMSG("PHPDebugTarget: Calling removeBreakpointListener(this);");
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointManagerListener(this);
		Logger.debugMSG("PHPDebugTarget: Firing terminate");
		fireTerminateEvent();

		// Refresh the launch-viewer to display the debug elements in their real terminated state.
		// This is needed since the migration to 3.3 (Europa)
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (activeWorkbenchWindow == null) {
					return;
				}
				IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
				if (page == null)
					return;
				AbstractDebugView view = (AbstractDebugView) page.findView(IDebugUIConstants.ID_DEBUG_VIEW);
				if (view == null)
					return;
				view.getViewer().refresh();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.ISuspendResume#canResume()
	 */
	public boolean canResume() {
		return !isTerminated() && isSuspended();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.ISuspendResume#canSuspend()
	 */
	public boolean canSuspend() {
		return !isTerminated() && !isSuspended();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.ISuspendResume#isSuspended()
	 */
	public boolean isSuspended() {
		return fSuspended;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.ISuspendResume#resume()
	 */
	public void resume() throws DebugException {
		fLastcmd = "resume";
		// Fix for bug #163780 - Debugger irregular state control
		// Call for the resumed before the debugger.stepOut
		int detail = DebugEvent.CLIENT_REQUEST;
		resumed(detail);
		((PHPThread) getThreads()[0]).setStepping(false);
		fStatus = debugger.go(fGoResponseHandler);
		if (!fStatus) {
			Logger.log(Logger.ERROR, "PHPDebugTarget: debugger.go return false");
		}
	}

	/**
	 * Notification the target has resumed for the given reason
	 *
	 * @param detail
	 *            reason for the resume
	 */
	private void resumed(int detail) {
		fSuspended = false;
		fThread.fireResumeEvent(detail);
	}

	/**
	 * Notification the target has suspended for the given reason
	 *
	 * @param detail
	 *            reason for the suspend
	 */
	public void suspended(int detail) {
		fSuspended = true;
		fSuspendCount++;
		System.setProperty("org.eclipse.debugger.variables", "true");
		try {
			((PHPThread) getThreads()[0]).setStepping(false);
		} catch (DebugException e) {
			// PHPThread doesn't throw exception
		}
		fThread.fireSuspendEvent(detail);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.ISuspendResume#suspend()
	 */
	public void suspend() throws DebugException {
		fLastcmd = "suspend";
		((PHPThread) getThreads()[0]).setStepping(false);
		fStatus = debugger.pause(fPauseResponseHandler);
		if (!fStatus) {
			Logger.log(Logger.ERROR, "PHPDebugTarget: debugger.pause return false");
		}
		int detail = DebugEvent.CLIENT_REQUEST;
		suspended(detail);
	}

	/**
	 * Creates a breakpoint
	 *
	 * @param resource
	 *            File resource to add breakpoint
	 * @param lineNumber
	 *            Line Number to add breakpoint
	 *
	 */
	public static IBreakpoint createBreakpoint(IResource resource, int lineNumber) throws CoreException {

		return createBreakpoint(resource, lineNumber, new HashMap<String, String>(10));
	}

	/**
	 * Creates a breakpoint
	 *
	 * @param resource
	 *            File resource to add breakpoint
	 * @param lineNumber
	 *            Line Number to add breakpoint
	 * @param attributes
	 *            java.util.Map of attributes to add to breakpoint
	 *
	 */
	public static IBreakpoint createBreakpoint(IResource resource, int lineNumber, Map<String, String> attributes) throws CoreException {
		IBreakpoint point = null;
		try {
			point = new PHPConditionalBreakpoint(resource, lineNumber, attributes);
		} catch (CoreException e1) {
			Logger.logException("PHPDebugTarget: error creating breakpoint", e1);
			throw e1;
		}
		return point;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointAdded(org.eclipse.debug.core.model.IBreakpoint)
	 */
	public void breakpointAdded(IBreakpoint breakpoint) {
		if (supportsBreakpoint(breakpoint)) {
			try {
				if (breakpoint.isEnabled()) {
					fLastcmd = "breakpointAdded";
					PHPLineBreakpoint bp = (PHPLineBreakpoint) breakpoint;
					IMarker marker = bp.getMarker();
					IResource resource = null;
					Breakpoint runtimeBreakpoint = bp.getRuntimeBreakpoint();
					int lineNumber = runtimeBreakpoint.getLineNumber();
					if (breakpoint instanceof PHPRunToLineBreakpoint) {
						PHPRunToLineBreakpoint rtl = (PHPRunToLineBreakpoint) breakpoint;
						resource = rtl.getSourceFile();
					} else {
						resource = marker.getResource();
						lineNumber = marker.getAttribute(IMarker.LINE_NUMBER, 0);
						runtimeBreakpoint.setLineNumber(lineNumber);
					}
					String fileName;
					if (!fIsPHPCGI) {
						if (resource instanceof IWorkspaceRoot) {
							if (IPHPDebugConstants.STORAGE_TYPE_REMOTE.equals(marker.getAttribute(IPHPDebugConstants.STORAGE_TYPE))) {
								fileName = (String) marker.getAttribute(IPHPDebugConstants.STORAGE_FILE);
								fileName = marker.getAttribute(IPHPDebugConstants.SECONDARY_ID_KEY, fileName);
							} else {
								String includeFile = (String) marker.getAttribute(IPHPDebugConstants.STORAGE_FILE);
								if (IPHPDebugConstants.STORAGE_TYPE_INCLUDE.equals(marker.getAttribute(IPHPDebugConstants.STORAGE_TYPE))) {
									includeFile = marker.getAttribute(IPHPDebugConstants.SECONDARY_ID_KEY, includeFile);
								}
								fileName = RemoteDebugger.convertToRemoteFilename(includeFile, this);
							}
						} else {
							fileName = RemoteDebugger.convertToRemoteFilename(resource.getFullPath().toString(), this);
						}
					} else {
						if (resource instanceof IWorkspaceRoot) {
							// If the breakpoint was set on a non-workspace file, make sure that the file name for the breakpoint
							// is taken correctly.
							fileName = (String) marker.getAttribute(IPHPDebugConstants.STORAGE_FILE);
							if (IPHPDebugConstants.STORAGE_TYPE_INCLUDE.equals(marker.getAttribute(IPHPDebugConstants.STORAGE_TYPE))) {
								fileName = marker.getAttribute(IPHPDebugConstants.SECONDARY_ID_KEY, fileName);
							}
						} else {
							IPath location = resource.getRawLocation();
							if (location == null) {
								fileName = resource.getFullPath().toOSString();
							} else {
								fileName = location.toOSString();
							}
						}
					}

					runtimeBreakpoint.setFileName(fileName);
					Logger.debugMSG("PHPDebugTarget: Setting Breakpoint - File " + fileName + " Line Number " + lineNumber);
					debugger.addBreakpoint(bp.getRuntimeBreakpoint(), fBreakpointAddedResponseHandler);
				}
			} catch (CoreException e1) {
				Logger.logException("PHPDebugTarget: Exception Adding Breakpoint", e1);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointRemoved(org.eclipse.debug.core.model.IBreakpoint,
	 *      org.eclipse.core.resources.IMarkerDelta)
	 */
	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
		if (supportsBreakpoint(breakpoint)) {
			if (breakpoint instanceof PHPRunToLineBreakpoint)
				return;
			fLastcmd = "breakpointRemoved";
			PHPLineBreakpoint bp = (PHPLineBreakpoint) breakpoint;
			Breakpoint runtimeBreakpoint = bp.getRuntimeBreakpoint();
			Logger.debugMSG("PHPDebugTarget: Removing Breakpoint - File " + runtimeBreakpoint.getFileName() + " Line Number " + runtimeBreakpoint.getLineNumber());
			fStatus = debugger.removeBreakpoint(runtimeBreakpoint, fBreakpointRemovedResponseHandler);
			if (!fStatus && debugger.isActive()) {
				Logger.log(Logger.ERROR, "PHPDebugTarget: debugger.removeBreakpoint return false");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.IBreakpointListener#breakpointChanged(org.eclipse.debug.core.model.IBreakpoint,
	 *      org.eclipse.core.resources.IMarkerDelta)
	 */
	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
		if (!fBreakpointManager.isEnabled())
			return;
		int deltaLNumber = delta.getAttribute(IMarker.LINE_NUMBER, 0);
		IMarker marker = breakpoint.getMarker();
		int lineNumber = marker.getAttribute(IMarker.LINE_NUMBER, 0);
		if (supportsBreakpoint(breakpoint)) {
			try {
				if (((PHPLineBreakpoint) breakpoint).isConditionChanged()) {
					((PHPLineBreakpoint) breakpoint).setConditionChanged(false);
					if (breakpoint.isEnabled()) {
						breakpointRemoved(breakpoint, null);
					} else {
						return;
					}
				}
				if (lineNumber != deltaLNumber) {
					if (breakpoint.isEnabled()) {
						breakpointRemoved(breakpoint, null);
					} else {
						return;
					}
				}
				if (breakpoint.isEnabled()) {
					breakpointAdded(breakpoint);
				} else {
					breakpointRemoved(breakpoint, null);
				}
			} catch (CoreException e) {
				Logger.logException("PHPDebugTarget: Exception Changing Breakpoint", e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IDisconnect#canDisconnect()
	 */
	public boolean canDisconnect() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IDisconnect#disconnect()
	 */
	public void disconnect() throws DebugException {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IDisconnect#isDisconnected()
	 */
	public boolean isDisconnected() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval#supportsStorageRetrieval()
	 */
	public boolean supportsStorageRetrieval() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.debug.core.model.IMemoryBlockRetrieval#getMemoryBlock(long,
	 *      long)
	 */
	public IMemoryBlock getMemoryBlock(long startAddress, long length) throws DebugException {
		return null;
	}

	/**
	 * Notification we have connected to the debugger and it has started. .
	 */
	public void started() {
		fSuspended = false;
		fireCreationEvent();
	}

	/**
	 * Install breakpoints that are already registered with the breakpoint
	 * manager.
	 * In case {@link #isRunWithDebug()} returns true, nothing will happen.
	 */
	public void installDeferredBreakpoints() throws CoreException {
		/*if (fIsRunAsDebug) {
			return;
		}
		if (!fBreakpointManager.isEnabled())
			return;
		IBreakpoint[] breakpoints = fBreakpointManager.getBreakpoints(IPHPDebugConstants.ID_PHP_DEBUG_CORE);
		for (IBreakpoint element : breakpoints) {
			((PHPLineBreakpoint) element).setConditionChanged(false);
			if (element.isEnabled()){
				breakpointAdded(element);
			}
		}*/
	}

	/**
	 * Returns the current stack frames in the target.
	 *
	 * @return the current stack frames in the target
	 * @throws DebugException
	 *             if unable to perform the request
	 */
	protected IStackFrame[] getStackFrames() throws DebugException {
		return fContextManager.getStackFrames();
	}

	/**
	 * Returns the Expression Manager for the Debug Target.
	 *
	 * @return the current Expression Manager target
	 */
	public DefaultExpressionsManager getExpressionManager() {
		return expressionsManager;
	}

	public void setExpressionManager(DefaultExpressionsManager expressionManager) {
		this.expressionsManager = expressionManager;
	}

	/**
	 * Returns the Parameter Stack for the Debug Target.
	 *
	 * @return the Parameter Stack for the target
	 */
	public Expression[] getStackVariables(PHPStackFrame stack) {
		return fContextManager.getStackVariables(stack);
	}

	/**
	 * Step Return the debugger.
	 *
	 * @throws DebugException
	 *             if the request fails
	 */
	protected void stepReturn() throws DebugException {
		fLastcmd = "stepReturn";
		Logger.debugMSG("PHPDebugTarget: stepReturn ");
		// Fix for bug #163780 - Debugger irregular state control
		// Call for the resumed before the debugger.stepOut
		int detail = DebugEvent.STEP_RETURN;
		resumed(detail);
		fStatus = debugger.stepOut(fStepOutResponseHandler);
		if (!fStatus) {
			Logger.log(Logger.ERROR_DEBUG, "PHPDebugTarget: debugger.stepOut return false");
		}
	}

	/**
	 * Step Over the debugger.
	 *
	 * @throws DebugException
	 *             if the request fails
	 */
	protected void stepOver() throws DebugException {
		fLastcmd = "stepOver";
		Logger.debugMSG("PHPDebugTarget: stepOver");
		// Fix for bug #163780 - Debugger irregular state control
		// Call for the resumed before the debugger.stepOver
		int detail = DebugEvent.STEP_OVER;
		resumed(detail);
		fStatus = debugger.stepOver(fStepOverResponseHandler);
		if (!fStatus) {
			Logger.log(Logger.ERROR_DEBUG, "PHPDebugTarget: debugger.stepOver return false");
		}
	}

	/**
	 * Step Into the debugger.
	 *
	 * @throws DebugException
	 *             if the request fails
	 */
	protected void stepInto() throws DebugException {
		Logger.debugMSG("PHPDebugTarget: stepInto ");
		fLastcmd = "stepInto";
		// Fix for bug #163780 - Debugger irregular state control
		// Call for the resumed before the debugger.stepInto
		int detail = DebugEvent.STEP_INTO;
		resumed(detail);
		fStatus = debugger.stepInto(fStepIntoResponseHandler);
		if (!fStatus) {
			Logger.log(Logger.ERROR_DEBUG, "PHPDebugTarget: debugger.stepInto return false");
		}
	}

	/**
	 * Returns the Local Variabales for the Debug Target.
	 *
	 * @return the Local Variabales for the target
	 */
	public IVariable[] getVariables(PHPStackFrame stackFrame) {
		return fContextManager.getVariables(stackFrame);
	}

	/**
	 * Notification a breakpoint was encountered. Determine which breakpoint was
	 * hit and fire a suspend event.
	 *
	 * @param event
	 *            debug event
	 */
	public void breakpointHit(String fileName, int lineNumber) {
		// determine which breakpoint was hit, and set the thread's breakpoint
		IBreakpoint breakpoint = findBreakpoint(fileName, lineNumber);
		if (breakpoint != null) {
			fThread.setBreakpoints(new IBreakpoint[] { breakpoint });
		} else {
			fThread.setBreakpoints(new IBreakpoint[] {});
		}

		suspended(DebugEvent.BREAKPOINT);
	}

	/**
	 * Finds the breakpoint hit
	 *
	 * @param fileName
	 *            Filename containing the breakpoint
	 * @param lineNumber
	 *            Linenumber of breakpoint
	 * @return the Local Variabales for the target
	 *
	 */
	protected IBreakpoint findBreakpoint(String fileName, int lineNumber) {
		// determine which breakpoint was hit, and set the thread's breakpoint
		IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(IPHPDebugConstants.ID_PHP_DEBUG_CORE);
		for (IBreakpoint breakpoint : breakpoints) {
			if (supportsBreakpoint(breakpoint)) {
				if (breakpoint instanceof PHPLineBreakpoint) {
					PHPLineBreakpoint lineBreakpoint = (PHPLineBreakpoint) breakpoint;
					Breakpoint zBP = lineBreakpoint.getRuntimeBreakpoint();
					String bFileName = zBP.getFileName();
					int bLineNumber = zBP.getLineNumber();
					if (bLineNumber == lineNumber && bFileName.equals(fileName)) {
						return breakpoint;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Finds the breakpoint hit
	 *
	 * @param enabled
	 *            Enabled or Disable breakpoints.
	 *
	 */
	public void breakpointManagerEnablementChanged(boolean enabled) {
		IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(getModelIdentifier());
		for (IBreakpoint element : breakpoints) {
			if (supportsBreakpoint(element)) {
				if (enabled) {
					((PHPLineBreakpoint) element).setConditionChanged(false);
					breakpointAdded(element);
				} else {
					breakpointRemoved(element, null);
				}
			}
		}
	}

	/**
	 * Registers the given event listener. The listener will be notified of
	 * events in the program being interpretted. Has no effect if the listener
	 * is already registered.
	 *
	 * @param listener event listener
	 */
	public void addConsoleEventListener(IPHPConsoleEventListener listener) {
		if (!fConsoleEventListeners.contains(listener)) {
			fConsoleEventListeners.add(listener);
		}
		for (DebugError debugError : fDebugErrors) {
			listener.handleEvent(debugError);
		}
	}

	/**
	 * Deregisters the given event listener. Has no effect if the listener is
	 * not currently registered.
	 *
	 * @param listener event listener
	 */
	public void removeConsoleEventListener(IPHPConsoleEventListener listener) {
		fConsoleEventListeners.remove(listener);
	}

	public List<IPHPConsoleEventListener> getConsoleEventListeners() {
		return fConsoleEventListeners;
	}

	/**
	 * Returns the Output buffer for the Debug Target.
	 *
	 * @return the Output buffer for the target
	 */
	public DebugOutput getOutputBuffer() {
		return fOutput;
	}

	/**
	 * Returns whether running with debug info.
	 *
	 * @return boolean - whether running with debug info
	 */
	public boolean isRunWithDebug() {
		return fIsRunAsDebug;
	}

	/**
	 * Throws a IStatus in a Debug Event
	 *
	 */
	public void fireError(String errorMessage, Exception e1) {
		Status status = new Status(IStatus.ERROR, PHPDebugEPLPlugin.PLUGIN_ID, IPHPDebugConstants.INTERNAL_ERROR, errorMessage, e1);
		DebugEvent event = new DebugEvent(this, DebugEvent.MODEL_SPECIFIC);
		event.setData(status);
		fireEvent(event);
	}

	/**
	 * Throws a IStatus in a Debug Event
	 *
	 */
	public void fireError(IStatus status) {
		DebugEvent event = new DebugEvent(this, DebugEvent.MODEL_SPECIFIC);
		event.setData(status);
		fireEvent(event);
	}

	public void setLastStop(int lineNumber) {
		fLastStop = lineNumber;
	}

	public void setLastFileName(String string) {
		fLastFileName = string;
	}

	public void setBreakpoints(IBreakpoint[] breakpoints) {
		fThread.setBreakpoints(new IBreakpoint[] {});
	}

	public boolean isPHPCGI() {
		return fIsPHPCGI;
	}

	public StartLock getStartLock() {
		return fStartLock;
	}

	public void setLastCommand(String command) {
		fLastcmd = command;
	}

	public String getLastCommand() {
		return fLastcmd;
	}

	public int getRequestPort() {
		return fRequestPort;
	}

	public IProject getProject() {
		return fProject;
	}

	public void setProject(IProject project) {
		fProject = project;
	}

	public Collection<DebugError> getDebugErrors() {
		return fDebugErrors;
	}

	public boolean isServerWindows() {
		return fIsServerWindows;
	}

	public void setServerWindows(boolean isServerWindows) {
		fIsServerWindows = isServerWindows;
	}

	public org2.eclipse.php.internal.debug.core.zend.debugger.Debugger.StartResponseHandler getStartResponseHandler() {
		return fStartResponseHandler;
	}

	public ContextManager getContextManager() {
		return fContextManager;
	}

	public IBreakpointManager getBreakpointManager() {
		return fBreakpointManager;
	}

	/**
	 * Maps first debug file in the path mapper
	 * @param remoteFile Server file path
	 * @return mapped path entry or <code>null</code> in case of error
	 */
	public PathEntry mapFirstDebugFile(String remoteFile) {

		if (getContextManager().isResolveBlacklisted(remoteFile)) {
			return null;
		}

		try {
			ILaunchConfiguration launchConfiguration = getLaunch().getLaunchConfiguration();
			PathMapper pathMapper = PathMapperRegistry.getByLaunchConfiguration(launchConfiguration);

			if (pathMapper != null) {
				PathEntry pathEntry = pathMapper.getLocalFile(remoteFile);

				// If such file doesn't exist in path mapper yet, add it:
				if (pathEntry == null) {
					// Try to find a map point:
					String debugFileName = launchConfiguration.getAttribute(IPHPDebugCorePreferenceKeys.ATTR_SERVER_FILE_NAME, (String) null);
					if (debugFileName == null) {
						debugFileName = launchConfiguration.getAttribute(IPHPDebugCorePreferenceKeys.ATTR_FILE, (String) null);
					}
					if (debugFileName != null) {
						IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(debugFileName);
						if (resource instanceof IFile) {
							pathEntry = new PathEntry(debugFileName, Type.WORKSPACE, resource.getParent());
						} else if (new File(debugFileName).exists()) {
							pathEntry = new PathEntry(debugFileName, Type.EXTERNAL, new File(debugFileName).getParentFile());
						}
					}
					if (pathEntry != null) {
						// Map remote file to the map point:
						pathMapper.addEntry(remoteFile, pathEntry);
						PathMapperRegistry.storeToPreferences();
					} else {
						// Find the local file, and map it:
						pathEntry = DebugSearchEngine.find(remoteFile, this);
					}
				}

				// Assign this project to Debug Target:
				if (getProject() == null && pathEntry != null && pathEntry.getType() == Type.WORKSPACE) {
					IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(pathEntry.getPath());
					IProject project = resource.getProject();
					setProject(project);
					try {
						ILaunchConfigurationWorkingCopy wc = launchConfiguration.getWorkingCopy();
						wc.getAttribute(IPHPDebugConstants.PHP_Project, project.getName());
						wc.doSave();
					} catch (CoreException e) {
						PHPDebugEPLPlugin.logError(e);
					}
				}

				return pathEntry;
			}
		} catch (Exception e) {
			PHPDebugEPLPlugin.logError(e);
		}
		return null;
	}

	public boolean isStepFiltersEnabled() {
		return isStepFiltersEnabled;
	}

	public void setStepFiltersEnabled(boolean enabled) {
		isStepFiltersEnabled = enabled;
	}

	public boolean supportsStepFilters() {
		return true;
	}
}
