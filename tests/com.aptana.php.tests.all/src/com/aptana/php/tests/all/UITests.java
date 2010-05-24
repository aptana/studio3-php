package com.aptana.php.tests.all;

import junit.framework.Test;
import junit.framework.TestSuite;

public class UITests
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite(UITests.class.getName());
		// $JUnit-BEGIN$
		suite.addTest(org.eclipse.php.core.tests.AllTests.suite());
		// $JUnit-END$

		// Now add special test cases which require to be run after all plugins are loaded (dependency inversion in
		// test)

		// require HTML editor to have outline contents to test common editor commands
		// suite.addTestSuite(ExpandCollapseAllHandlerTest.class);
		// suite.addTestSuite(ExpandLevelHandlerTest.class);
		// suite.addTestSuite(NextPreviousEditorHandlerTest.class);
		return suite;
	}

}
