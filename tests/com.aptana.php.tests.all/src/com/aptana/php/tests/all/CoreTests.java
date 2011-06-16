package com.aptana.php.tests.all;

import junit.framework.Test;
import junit.framework.TestSuite;

public class CoreTests
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite(CoreTests.class.getName());
		// $JUnit-BEGIN$
		suite.addTest(org.eclipse.php.core.tests.AllCoreTests.suite());
		// $JUnit-END$
		return suite;
	}
}
