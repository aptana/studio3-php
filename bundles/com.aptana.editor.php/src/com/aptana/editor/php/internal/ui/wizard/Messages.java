package com.aptana.editor.php.internal.ui.wizard;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "com.aptana.editor.php.internal.ui.wizard.messages"; //$NON-NLS-1$
	public static String NewPHPProjectWizard_conflictDirectory;
	public static String NewPHPProjectWizard_creationProblem;
	public static String NewPHPProjectWizard_internalError;
	public static String NewPHPProjectWizard_natureConfigurationError;
	public static String NewPHPProjectWizard_projectWizardDescription;
	public static String NewPHPProjectWizard_projectWizardTitle;
	public static String NewPHPProjectWizard_projectWizardStepLbl;

	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
