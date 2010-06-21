package org.eclipse.php.core.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite(AllTests.class.getName());
		// $JUnit-BEGIN$
		suite.addTest(AllCoreTests.suite());
		suite.addTest(AllUITests.suite());
		// $JUnit-END$
		return suite;
	}

}
