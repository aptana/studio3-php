package com.aptana.editor.php.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.aptana.editor.php.core.CoreTests;

public class AllTests
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite(AllTests.class.getName());
		// $JUnit-BEGIN$
		suite.addTestSuite(PHPScopesTest.class);
		suite.addTestSuite(PHTMLSourcePartitionScannerTest.class);
		suite.addTestSuite(PHTMLTagScannerTest.class);
		suite.addTestSuite(FastPHPStringTokenScannerTestCase.class);
		suite.addTest(CoreTests.suite());
		// $JUnit-END$
		return suite;
	}

}
