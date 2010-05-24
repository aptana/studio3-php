package com.aptana.editor.php.core;

import junit.framework.Test;
import junit.framework.TestSuite;

public class CoreTests
{
	public static Test suite()
	{
		TestSuite suite = new TestSuite(CoreTests.class.getName());
		// $JUnit-BEGIN$
		suite.addTestSuite(PHPVersionProviderTest.class);
		// $JUnit-END$
		return suite;
	}
}
