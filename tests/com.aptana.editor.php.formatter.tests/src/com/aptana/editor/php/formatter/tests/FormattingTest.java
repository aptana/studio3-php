package com.aptana.editor.php.formatter.tests;

import java.util.Arrays;

import org.junit.Before;
import org.junit.runners.Parameterized.Parameters;

import com.aptana.editor.common.formatting.tests.AbstractFormatterTestCase;
import com.aptana.editor.php.formatter.PHPFormatterFactory;

public class FormattingTest extends AbstractFormatterTestCase
{

	private static String FORMATTER_FACTORY_ID = "com.aptana.editor.php.formatterFactory"; //$NON-NLS-1$
	private static String TEST_BUNDLE_ID = "com.aptana.editor.php.formatter.tests"; //$NON-NLS-1$
	private static String FILE_TYPE = "php"; //$NON-NLS-1$

	@Parameters(name = "{0}")
	public static Iterable<Object[]> data()
	{
		return Arrays.asList(AbstractFormatterTestCase.getFiles(TEST_BUNDLE_ID, FILE_TYPE));
	}
	
	@Before
	public void setUp() throws Exception
	{
		factory = new PHPFormatterFactory();
	}

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
}
