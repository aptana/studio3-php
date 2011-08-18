package com.aptana.editor.php.formatter.tests;

import com.aptana.editor.common.formatting.AbstractFormatterTestCase;

public class FormattingTests extends AbstractFormatterTestCase
{
	// Turn this flag on for development only (used to generate the formatted files)
	// To generate formatted files, place php files under the 'formatting' folder and run these tests from the
	// com.aptana.editor.php.formatter.tests plugin
	// NOTE: Ensure that the contents section ends with a newline, or the generation may not work.
	private static boolean INITIALIZE_MODE = false;
	// Turning on the overwrite will re-generate the formatted block and overwrite it into the test files.
	// This is a drastic move that will require a review of the output right after to make sure we have the
	// right formatting for all the test file, so turn it on at your own risk.
	private static boolean OVERWRITE_MODE = false;

	private static String FORMATTER_FACTORY_ID = "com.aptana.editor.php.formatterFactory"; //$NON-NLS-1$
	private static String TEST_BUNDLE_ID = "com.aptana.editor.php.formatter.tests"; //$NON-NLS-1$
	private static String FILE_TYPE = "php"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.formatting.AbstractFormatterTestCase#getTestBundleId()
	 */
	@Override
	protected String getTestBundleId()
	{
		return TEST_BUNDLE_ID;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.formatting.AbstractFormatterTestCase#getFormatterId()
	 */
	@Override
	protected String getFormatterId()
	{
		return FORMATTER_FACTORY_ID;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.formatting.AbstractFormatterTestCase#getFileType()
	 */
	@Override
	protected String getFileType()
	{
		return FILE_TYPE;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.formatting.AbstractFormatterTestCase#isOverriteMode()
	 */
	@Override
	protected boolean isOverriteMode()
	{
		return OVERWRITE_MODE;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.formatting.AbstractFormatterTestCase#isInitializeMode()
	 */
	@Override
	protected boolean isInitializeMode()
	{
		return INITIALIZE_MODE;
	}
}
