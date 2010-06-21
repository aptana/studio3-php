package org.eclipse.php.core.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.php.core.tests.codeassist.CodeAssistTests;

public class AllUITests
{
	public static Test suite()
	{
		TestSuite suite = new TestSuite(AllUITests.class.getName());
		// $JUnit-BEGIN$
		suite.addTest(CodeAssistTests.suite());
		// $JUnit-END$
		return suite;
	}
}
