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
		suite.addTest(CoreTests.suite());
		// $JUnit-END$
		return suite;
	}

}
