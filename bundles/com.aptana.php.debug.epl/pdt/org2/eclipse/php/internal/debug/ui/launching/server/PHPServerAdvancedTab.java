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
package org2.eclipse.php.internal.debug.ui.launching.server;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;
import org2.eclipse.php.internal.debug.core.IPHPDebugConstants;
import org2.eclipse.php.internal.debug.core.launching.PHPLaunchUtilities;
import org2.eclipse.php.internal.debug.core.xdebug.communication.XDebugCommunicationDaemon;
import org2.eclipse.php.internal.ui.util.PixelConverter;

import com.aptana.php.debug.core.IPHPDebugCorePreferenceKeys;
import com.aptana.php.debug.core.tunneling.TunnelTester;
import com.aptana.php.debug.core.util.MD5;
import com.aptana.php.debug.epl.PHPDebugEPLPlugin;

/**
 * A PHPServerAdvancedTab for selecting advanced debug options, such as 'Debug all Pages', 'Start Debug from' etc.
 * 
 * @author shalom
 */
public class PHPServerAdvancedTab extends AbstractLaunchConfigurationTab
{

	private Button debugFirstPageBt;
	private Button debugAllPagesBt;
	private Button debugStartFromBt;
	private Button debugContinueBt;
	private Button resetBt;
	private Text debugFromTxt;
	protected Button openBrowser;
	protected Button internalBrowser;
	protected WidgetListener listener;
	protected ILaunchConfiguration launchConfiguration;
	private Group tunnelGroup;
	private Group sessionGroup;
	protected boolean isOpenInBrowser;
	private boolean isUsingExternalBrowser;
	private boolean internalWebBrowserAvailable;
	private Button debugThroughTunnel;
	private Text userName;
	private Text password;
	private Button testButton;
	private CLabel testResultLabel;
	private Label nameLabel;
	private Label passwordLabel;
	public boolean isTextModificationChange;

	/**
	 * Constructor
	 */
	public PHPServerAdvancedTab()
	{
		listener = new WidgetListener();
		internalWebBrowserAvailable = PlatformUI.getWorkbench().getBrowserSupport().isInternalWebBrowserAvailable();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		layout.numColumns = 1;
		composite.setLayout(layout);

		createAdvanceControl(composite);
		createExtensionControls(composite);

		Dialog.applyDialogFont(composite);
		setControl(composite);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getImage()
	 */
	public Image getImage()
	{
		// Returns a 'security' icon.
		return ServersPluginImages.get(ServersPluginImages.IMG_SECURITY);
	}

	/**
	 * Create the advanced control.
	 * 
	 * @param composite
	 */
	protected void createAdvanceControl(Composite composite)
	{
		// == Groups ==
		tunnelGroup = new Group(composite, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		tunnelGroup.setLayout(layout);
		tunnelGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		tunnelGroup.setText("SSH Tunnel");

		Group browserGroup = new Group(composite, SWT.NONE);
		layout = new GridLayout(1, false);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		browserGroup.setLayout(layout);
		browserGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		browserGroup.setText("Browser");

		sessionGroup = new Group(composite, SWT.NONE);
		layout = new GridLayout(3, false);
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		sessionGroup.setLayout(layout);
		sessionGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		sessionGroup.setText("Session");

		// == Controls ==
		// Add the tunneling controls
		PixelConverter converter = new PixelConverter(composite);
		debugThroughTunnel = new Button(tunnelGroup, SWT.CHECK);
		debugThroughTunnel.setText("Debug Through SSH Tunnel");
		Composite credentialsComposite = new Composite(tunnelGroup, SWT.NONE);
		credentialsComposite.setLayout(new GridLayout(2, false));
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalIndent = 20;
		credentialsComposite.setLayoutData(data);
		nameLabel = new Label(credentialsComposite, SWT.NONE);
		nameLabel.setText("User Name:");
		userName = new Text(credentialsComposite, SWT.BORDER | SWT.SINGLE);
		data = new GridData();
		data.widthHint = converter.convertHorizontalDLUsToPixels(150);
		userName.setLayoutData(data);
		passwordLabel = new Label(credentialsComposite, SWT.NONE);
		passwordLabel.setText("Password:");
		password = new Text(credentialsComposite, SWT.PASSWORD | SWT.BORDER | SWT.SINGLE);
		data = new GridData();
		data.widthHint = converter.convertHorizontalDLUsToPixels(150);
		password.setLayoutData(data);
		final Composite testConnectionComposite = new Composite(credentialsComposite, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		testConnectionComposite.setLayout(layout);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		testConnectionComposite.setLayoutData(data);
		testButton = new Button(testConnectionComposite, SWT.PUSH);
		testButton.setText("Test Connection");
		testResultLabel = new CLabel(testConnectionComposite, SWT.NONE);
		testResultLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		testButton.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				// Run a test for the connection
				testTunnelConnection();
			}
		});
		testResultLabel.addMouseListener(new MouseAdapter()
		{
			public void mouseUp(MouseEvent e)
			{
				Object messageData = testResultLabel.getData("info");
				if (messageData != null)
				{
					MessageDialog.openInformation(getShell(), "SSH Tunnel Test", messageData.toString());
				}
			}
		});
		debugThroughTunnel.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent se)
			{
				Button b = (Button) se.getSource();
				boolean selection = b.getSelection();
				updateTunnelComponents(selection);
				updateLaunchConfigurationDialog();
			}
		});

		// Add the Browser group controls
		openBrowser = new Button(browserGroup, SWT.CHECK);
		openBrowser.setText("Open in Browser");
		internalBrowser = new Button(browserGroup, SWT.CHECK);
		if (internalWebBrowserAvailable)
		{
			internalBrowser.setText("Use Internal Browser");
		}
		else
		{
			internalBrowser.setText("Use Internal Browser (Unavailable)");
			internalBrowser.setEnabled(false);
		}
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalIndent = 20;
		internalBrowser.setLayoutData(data);
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 3;
		openBrowser.setLayoutData(data);
		openBrowser.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent se)
			{
				Button b = (Button) se.getSource();
				isOpenInBrowser = b.getSelection();
				if (!isOpenInBrowser)
				{
					debugFirstPageBt.setSelection(true);
					debugAllPagesBt.setSelection(false);
				}
				else
				{
					debugFirstPageBt.setSelection(false);
					debugAllPagesBt.setSelection(true);
				}
				debugStartFromBt.setSelection(false);
				debugContinueBt.setSelection(false);
				enableSessionSettingButtons(isOpenInBrowser
						&& ILaunchManager.DEBUG_MODE.equals(getLaunchConfigurationDialog().getMode()));
				updateLaunchConfigurationDialog();
			}
		});

		// Add the Session group controls
		debugAllPagesBt = createRadioButton(sessionGroup, "Debug &All Pages");
		data = (GridData) debugAllPagesBt.getLayoutData();
		data.horizontalSpan = 3;
		data.horizontalIndent = 20;

		debugFirstPageBt = createRadioButton(sessionGroup, "Debug &First Page Only");
		data = (GridData) debugFirstPageBt.getLayoutData();
		data.horizontalSpan = 3;
		data.horizontalIndent = 20;

		debugStartFromBt = createRadioButton(sessionGroup, "&Start Debug from:");
		data = (GridData) debugStartFromBt.getLayoutData();
		data.horizontalIndent = 20;

		debugFromTxt = new Text(sessionGroup, SWT.SINGLE | SWT.BORDER);
		data = new GridData(GridData.FILL_HORIZONTAL);
		debugFromTxt.setLayoutData(data);

		resetBt = createPushButton(sessionGroup, "Default", null);
		resetBt.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				if (launchConfiguration != null)
				{
					try
					{
						debugFromTxt.setText(launchConfiguration.getAttribute(IPHPDebugCorePreferenceKeys.ATTR_SERVER_BASE_URL, ""));
					}
					catch (CoreException e1)
					{
					}
				}
			}
		});

		debugContinueBt = createCheckButton(sessionGroup, "&Continue Debug from This Page");
		data = (GridData) debugContinueBt.getLayoutData();
		data.horizontalSpan = 3;
		data.horizontalIndent = 40;

		// Add listeners
		debugStartFromBt.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				updateDebugFrom();
			}
		});

		updateDebugFrom();

		// Add widget listeners
		debugFirstPageBt.addSelectionListener(listener);
		debugAllPagesBt.addSelectionListener(listener);
		debugContinueBt.addSelectionListener(listener);
		debugStartFromBt.addSelectionListener(listener);
		debugFromTxt.addModifyListener(listener);
		internalBrowser.addSelectionListener(listener);
		debugThroughTunnel.addSelectionListener(listener);
		userName.addModifyListener(listener);
		password.addModifyListener(listener);

		KeyListener userInputListener = new KeyListener()
		{
			public void keyReleased(KeyEvent e)
			{
				testResultLabel.setText("");
				testButton.setEnabled((userName.getText().trim().length() > 0));
			}

			public void keyPressed(KeyEvent e)
			{
				testResultLabel.setText("");
			}
		};
		userName.addKeyListener(userInputListener);
		password.addKeyListener(userInputListener);
	}

	/**
	 * Update the tunnel components enablement state.
	 * This is called on initiation and when a user check/uncheck the enable button.
	 * 
	 * @param enabled
	 */
	protected void updateTunnelComponents(boolean enabled)
	{
		testResultLabel.setText("");
		setEnabled(enabled, userName, password, nameLabel, passwordLabel, testResultLabel);
		testButton.setEnabled(enabled && userName.getText().trim().length() > 0);
	}

	/**
	 * Set multiple control enablement state.
	 * 
	 * @param enabled
	 * @param controls
	 */
	protected void setEnabled(boolean enabled, Control... controls)
	{
		for (Control c : controls)
		{
			c.setEnabled(enabled);
		}
	}

	private void enableSessionSettingButtons(boolean isOpenInBrowser)
	{
		debugFirstPageBt.setEnabled(isOpenInBrowser);
		debugAllPagesBt.setEnabled(isOpenInBrowser);
		debugStartFromBt.setEnabled(isOpenInBrowser);
		debugContinueBt.setEnabled(false);
		resetBt.setEnabled(false);
		debugFromTxt.setEnabled(false);
		internalBrowser.setEnabled(internalWebBrowserAvailable && isOpenInBrowser);
		internalBrowser.setEnabled(internalWebBrowserAvailable && isOpenInBrowser);
	}

	/**
	 * Test a connection with the user name and password that are currently typed in their designated boxes.
	 * We assume here that the validation of the dialog already eliminated a situation where the Test button is
	 * enabled when there is a missing user-name or password.
	 */
	private void testTunnelConnection()
	{
		testButton.setEnabled(false);
		testResultLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_BLUE));
		testResultLabel.setText("Testing Connection...");
		testResultLabel.setCursor(Display.getDefault().getSystemCursor(SWT.CURSOR_WAIT));
		testResultLabel.setData("info", null);
		Job connectionTest = new UIJob("SSH Tunnel Test")
		{
			public IStatus runInUIThread(IProgressMonitor monitor)
			{
				try {
					String remoteHost = PHPLaunchUtilities.getDebugHost(launchConfiguration);
					int port = PHPLaunchUtilities.getDebugPort(launchConfiguration);
					if (remoteHost == null || remoteHost.length() == 0 || port < 0)
					{
						// The host was not yet set in the launch configuration. 
						testButton.setEnabled(true);
						testResultLabel.setCursor(Display.getDefault().getSystemCursor(SWT.CURSOR_HAND));
						testResultLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED));
						if (port > -1)
						{
							testResultLabel.setText("Missing host (click for more information)");
							testResultLabel.setData("info", "Missing host address. \nPlease fill in the Server part in the 'Server' tab before testing the connection.");
						}
						else
						{
							testResultLabel.setText("Error (click for more information)");
							testResultLabel.setData("info", "Could not determin the port to tunnel. \nPlease 'Apply' the dialog and try again.");
						}
					}


					testResultLabel.setCursor(Display.getDefault().getSystemCursor(SWT.CURSOR_WAIT));
					IStatus connectionStatus = TunnelTester.test(remoteHost, userName.getText().trim(), password.getText().trim(), port, port);
					testButton.setEnabled(true);
					testResultLabel.setCursor(null);
					if (connectionStatus.isOK())
					{
						testResultLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN));
						testResultLabel.setText("Successfully connected");
					}
					else if (connectionStatus.isMultiStatus())
					{
						// A case where the connection indicate that it was successful, however, we were still not able to verify that
						testResultLabel.setCursor(Display.getDefault().getSystemCursor(SWT.CURSOR_HAND));
						testResultLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_YELLOW));
						testResultLabel.setText("Undetermined (click for more information)");
						testResultLabel.setData("info", connectionStatus.getMessage());
						// Update the password fields in case the multi status also contains a password change information
						IStatus[] children = connectionStatus.getChildren();
						if (children != null)
						{
							for (IStatus child : children)
							{
								if (child.getSeverity() == IStatus.INFO && child.getCode() == TunnelTester.PASSWORD_CHANGED_CODE)
								{
									password.setText(child.getMessage());
									break;
								}
							}
						}
					}
					else if (connectionStatus.getSeverity() == IStatus.WARNING)
					{
						testResultLabel.setCursor(Display.getDefault().getSystemCursor(SWT.CURSOR_HAND));
						testResultLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN));
						testResultLabel.setText("Connected with warnings (click for more information)");
						testResultLabel.setData("info", connectionStatus.getMessage());
					}
					else if (connectionStatus.getSeverity() == IStatus.INFO )
					{
						testResultLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN));
						testResultLabel.setText("Successfully connected");
						//update the password field in case that the info indicated a password change.
						if (connectionStatus.getCode() == TunnelTester.PASSWORD_CHANGED_CODE)
						{
							password.setText(connectionStatus.getMessage());
						}
					} else if (connectionStatus.getSeverity() == IStatus.ERROR)
					{
						testResultLabel.setCursor(Display.getDefault().getSystemCursor(SWT.CURSOR_HAND));
						testResultLabel.setForeground(Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED));
						testResultLabel.setText("Failed to Connect (click for more information)");
						testResultLabel.setData("info", connectionStatus.getMessage());
					}
				} catch (OperationCanceledException oce)
				{
					testButton.setEnabled(true);
					testResultLabel.setCursor(null);
					testResultLabel.setForeground(null);
					testResultLabel.setText("Canceled");
				}
				return Status.OK_STATUS;
			}
		};
		connectionTest.setUser(true);
		connectionTest.setPriority(Job.LONG);
		connectionTest.schedule();
	}

	/**
	 * Override this method to add more widgets to this tab.
	 * 
	 * @param composite
	 */
	protected void createExtensionControls(Composite composite)
	{
		return;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName()
	{
		return "Advanced";
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration)
	{
		launchConfiguration = configuration;
		boolean isXdebugger = isXdebug(configuration);
		try
		{
			boolean isUsingTunnel = configuration.getAttribute(IPHPDebugConstants.USE_SSH_TUNNEL, false);
			debugThroughTunnel.setSelection(isUsingTunnel);
			updateTunnelComponents(isUsingTunnel);
			if (isUsingTunnel)
			{
				userName.setText(configuration.getAttribute(IPHPDebugConstants.SSH_TUNNEL_USER_NAME, ""));
				if (userName.getText().length() > 0)
				{
					// Load the password from the Secured Storage
					try
					{
						password.setText(PHPLaunchUtilities.getSecurePreferences(PHPLaunchUtilities.getDebugHost(launchConfiguration)).get(userName.getText(), ""));
					}
					catch (StorageException e)
					{
						PHPDebugEPLPlugin.logError("Error accessing the secured storage", e);
						password.setText("");
					}
				}
				else
				{
					password.setText("");
				}
			}
			// Zend debugger have the option not to use a browser to start a session. Since XDebug seems to start only
			// with a browser instance, we check for it and enable it anyway.
			isOpenInBrowser = isXdebugger || configuration.getAttribute(IPHPDebugConstants.OPEN_IN_BROWSER, true);
			isUsingExternalBrowser = internalWebBrowserAvailable
					&& configuration.getAttribute(IPHPDebugConstants.USE_INTERNAL_BROWSER, false);
			openBrowser.setSelection(isOpenInBrowser);
			if (isXdebugger)
			{
				openBrowser.setEnabled(false);
			}
			internalBrowser.setSelection(isUsingExternalBrowser);

			sessionGroup.setVisible(!isXdebugger);

			String debugSetting = configuration.getAttribute(IPHPDebugConstants.DEBUGGING_PAGES,
					IPHPDebugConstants.DEBUGGING_ALL_PAGES);
			if (IPHPDebugConstants.DEBUGGING_ALL_PAGES.equals(debugSetting))
			{
				debugFirstPageBt.setSelection(false);
				debugAllPagesBt.setSelection(true);
				debugStartFromBt.setSelection(false);
			}
			else if (IPHPDebugConstants.DEBUGGING_FIRST_PAGE.equals(debugSetting))
			{
				debugFirstPageBt.setSelection(true);
				debugAllPagesBt.setSelection(false);
				debugStartFromBt.setSelection(false);
			}
			else if (IPHPDebugConstants.DEBUGGING_START_FROM.equals(debugSetting))
			{
				debugFirstPageBt.setSelection(false);
				debugAllPagesBt.setSelection(false);
				debugStartFromBt.setSelection(true);
				boolean shouldContinue = configuration
						.getAttribute(IPHPDebugConstants.DEBUGGING_SHOULD_CONTINUE, false);
				debugContinueBt.setSelection(shouldContinue);
			}
			String startFromURL = configuration.getAttribute(IPHPDebugConstants.DEBUGGING_START_FROM_URL, "");
			debugFromTxt.setText(startFromURL);
			updateDebugFrom();
			// in case we are dealing with XDebug, enable the browser control anyway and do not restrict to debug mode
			enableSessionSettingButtons(isXdebugger
					|| (isOpenInBrowser && ILaunchManager.DEBUG_MODE.equals(getLaunchConfigurationDialog().getMode())));
		}
		catch (CoreException e)
		{
		}
		isValid(configuration);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.debug.ui.AbstractLaunchConfigurationTab#activated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy
	 * )
	 */
	public void activated(ILaunchConfigurationWorkingCopy workingCopy)
	{
		super.activated(workingCopy);
		// hide/show the session group in case the debugger type was modified in the 'main' tab
		boolean isXDebug = isXdebug(workingCopy);
		sessionGroup.setVisible(!isXDebug);
		openBrowser.setEnabled(!isXDebug);
		if (isXDebug)
		{
			openBrowser.setText("Open in Browser (locked for XDebug)");
		}
		else
		{
			openBrowser.setText("Open in Browser");
		}
	}

	/*
	 * Aptana addition - Check to see if this is a XDebug configuration. This value will be used to determine the
	 * options to display in this dialog.
	 */
	private boolean isXdebug(ILaunchConfiguration configuration)
	{
		try
		{
			String debuggerID = configuration.getAttribute(IPHPDebugCorePreferenceKeys.PHP_DEBUGGER_ID, "");
			if (debuggerID != null && !debuggerID.equals("")) { //$NON-NLS-1$
				return XDebugCommunicationDaemon.XDEBUG_DEBUGGER_ID.equals(debuggerID);
			}
		}
		catch (CoreException e)
		{
			PHPDebugEPLPlugin.logError("PHPServerAdvancedTab.isZendDebugger() failed to determine the debugger type", e);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration)
	{
		launchConfiguration = configuration;
		configuration.setAttribute(IPHPDebugConstants.USE_SSH_TUNNEL, debugThroughTunnel.getSelection());
		if (debugThroughTunnel.getSelection())
		{
			configuration.setAttribute(IPHPDebugConstants.SSH_TUNNEL_USER_NAME, userName.getText().trim());
			// We save a hash of the password and not the real one. This is only used to allow an apply when a password change happens.
			// The real password saving is done through the secured storage right after that line.
			String passwordDigest = MD5.digest(password.getText().trim());
			if (passwordDigest == null)
			{
				// as a default, use the string hash.
				passwordDigest = String.valueOf(password.getText().trim().hashCode());
			}
			configuration.setAttribute(IPHPDebugConstants.SSH_TUNNEL_PASSWORD, passwordDigest);

			// Save to secured storage
			try
			{
				// Note: At this point we write to the secure storage at any apply.
				// This might put in the storage some un-needed keys, so we also scan the launch configurations on startup
				// and make sure that the storage contains only what we need.
				if (!isTextModificationChange)
				{
					// We'll save to the secured storage only if the change was done outside text fields (that might contains the changes in the user-name and password as we type them).
					// This flag will be off when the apply button is actually clicked (or when other widgets are triggering the apply call).
					PHPLaunchUtilities.getSecurePreferences(PHPLaunchUtilities.getDebugHost(launchConfiguration)).put(userName.getText(), password.getText().trim(), true /*encrypt*/);
				}
			}
			catch (StorageException e)
			{
				PHPDebugEPLPlugin.logError("Error saving to the secured storage", e);
			}
		}
		else
		{
			configuration.setAttribute(IPHPDebugConstants.SSH_TUNNEL_USER_NAME, "");
			configuration.setAttribute(IPHPDebugConstants.SSH_TUNNEL_PASSWORD, "");
		}
		configuration.setAttribute(IPHPDebugConstants.OPEN_IN_BROWSER, isOpenInBrowser);
		configuration.setAttribute(IPHPDebugConstants.USE_INTERNAL_BROWSER, internalBrowser.getSelection());
		if (isOpenInBrowser)
		{
			if (debugAllPagesBt.getSelection())
			{
				configuration.setAttribute(IPHPDebugConstants.DEBUGGING_PAGES, IPHPDebugConstants.DEBUGGING_ALL_PAGES);
			}
			else if (debugFirstPageBt.getSelection())
			{
				configuration.setAttribute(IPHPDebugConstants.DEBUGGING_PAGES, IPHPDebugConstants.DEBUGGING_FIRST_PAGE);
			}
			else
			{
				configuration.setAttribute(IPHPDebugConstants.DEBUGGING_PAGES, IPHPDebugConstants.DEBUGGING_START_FROM);
				configuration.setAttribute(IPHPDebugConstants.DEBUGGING_START_FROM_URL, debugFromTxt.getText());
				configuration
						.setAttribute(IPHPDebugConstants.DEBUGGING_SHOULD_CONTINUE, debugContinueBt.getSelection());
			}
		}
		else
		{
			// Allow only debug-first-page
			configuration.setAttribute(IPHPDebugConstants.DEBUGGING_PAGES, IPHPDebugConstants.DEBUGGING_FIRST_PAGE);
		}
		applyExtension(configuration);
		isTextModificationChange = false; // reset this flag here.
	}

	/**
	 * Override this method to perform the apply in the extending classes.
	 * 
	 * @param configuration
	 */
	protected void applyExtension(ILaunchConfigurationWorkingCopy configuration)
	{
		return;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration)
	{
		launchConfiguration = configuration;
		setErrorMessage(null);
		configuration.setAttribute(IPHPDebugConstants.DEBUGGING_PAGES, IPHPDebugConstants.DEBUGGING_ALL_PAGES);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration launchConfig)
	{
		launchConfiguration = launchConfig;
		setMessage(null);
		setErrorMessage(null);
		if (debugThroughTunnel.getSelection())
		{
			boolean valid = userName.getText().trim().length() > 0;
			testButton.setEnabled(valid);
			if (!valid)
			{
				setErrorMessage("Missing user name for the SSH tunnel connection");
				return false;
			}
		}
		if (debugStartFromBt.getSelection())
		{
			if (debugFromTxt.getText().trim().equals(""))
			{
				setErrorMessage("Invalid debug start page");
				return false;
			}
			try
			{
				new URL(debugFromTxt.getText());
			}
			catch (MalformedURLException mue)
			{
				setErrorMessage("Invalid URL");
				return false;
			}
		}
		return isValidExtension(launchConfig);
	}

	/**
	 * Override this method to perform the isValid in the extending classes.
	 * 
	 * @param launchConfig
	 * @return true, if the extention is in a valid state.
	 */
	protected boolean isValidExtension(ILaunchConfiguration launchConfig)
	{
		return true;
	}

	// Update the 'debug from' related widgets
	private void updateDebugFrom()
	{
		if (launchConfiguration != null && debugFromTxt.getText().trim().equals(""))
		{
			try
			{
				debugFromTxt.setText(launchConfiguration.getAttribute(IPHPDebugCorePreferenceKeys.ATTR_SERVER_BASE_URL, ""));
			}
			catch (CoreException e)
			{
			}
		}
		Display.getDefault().asyncExec(new Runnable()
		{
			public void run()
			{
				try
				{
					boolean debugFromSelected = debugStartFromBt.getSelection();
					debugFromTxt.setEnabled(debugFromSelected);
					debugContinueBt.setEnabled(debugFromSelected);
					resetBt.setEnabled(debugFromSelected);
				}
				catch (SWTException se)
				{
					// Just in case the widget was disposed (cases such as the configuration deletion).
				}
			}
		});
	}

	protected class WidgetListener extends SelectionAdapter implements ModifyListener
	{
		public void modifyText(ModifyEvent e)
		{
			isTextModificationChange = true; // mark that this was a text modification change, so that the apply will not save to the secured storage.
			updateLaunchConfigurationDialog();
		}

		public void widgetSelected(SelectionEvent e)
		{
			setDirty(true);
			updateLaunchConfigurationDialog();
		}
	}
}
