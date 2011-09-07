package com.aptana.editor.php.core;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.aptana.editor.php.tests.PHTMLOutlineTest;

public class CoreTests
{
	public static Test suite()
	{
		TestSuite suite = new TestSuite(CoreTests.class.getName());
		// $JUnit-BEGIN$
		suite.addTestSuite(PHPVersionProviderTest.class);
		suite.addTest(new PHTMLOutlineTest().suite());
		// $JUnit-END$
		return suite;
	}
}
