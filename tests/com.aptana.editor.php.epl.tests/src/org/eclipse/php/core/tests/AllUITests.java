package org.eclipse.php.core.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.php.core.tests.codeassist.CodeAssistTests;
import org.eclipse.php.core.tests.codeassist.PHPContentAssistProcessorTests;

public class AllUITests
{
	public static Test suite()
	{
		TestSuite suite = new TestSuite(AllUITests.class.getName());
		// $JUnit-BEGIN$
		suite.addTest(CodeAssistTests.suite());
		suite.addTestSuite(PHPContentAssistProcessorTests.class);
		// $JUnit-END$
		return suite;
	}
}
