package org.eclipse.php.core.tests;

import org.eclipse.php.core.tests.codeassist.CodeAssistTest;
import org.eclipse.php.core.tests.codeassist.PHPContentAssistProcessorTest;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllUITests
{
	public static Test suite()
	{
		TestSuite suite = new TestSuite(AllUITests.class.getName());
		// $JUnit-BEGIN$
		 suite.addTest(CodeAssistTest.suite());
		 suite.addTestSuite(PHPContentAssistProcessorTest.class);
		// $JUnit-END$
		return suite;
	}
}
