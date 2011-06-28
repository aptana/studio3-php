package com.aptana.php.tests.all;

import junit.framework.Test;
import junit.framework.TestSuite;

public class UITests
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite(UITests.class.getName());
		// $JUnit-BEGIN$
		suite.addTest(org.eclipse.php.core.tests.AllUITests.suite());
		suite.addTest(com.aptana.editor.php.tests.AllTests.suite());
		// $JUnit-END$
		return suite;
	}

}
