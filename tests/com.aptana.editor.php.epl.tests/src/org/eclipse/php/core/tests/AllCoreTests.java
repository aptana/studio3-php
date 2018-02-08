package org.eclipse.php.core.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.php.core.tests.codeassist.CodeAssistTest;
import org.eclipse.php.core.tests.codeassist.PHPContentAssistProcessorTest;
import org.eclipse.php.core.tests.dom_ast.parser.DomParserTest;
import org.eclipse.php.core.tests.markoccurrence.MarkOccurrenceTest;

public class AllCoreTests
{
	public static Test suite()
	{
		TestSuite suite = new TestSuite(AllCoreTests.class.getName());
		// $JUnit-BEGIN$
		suite.addTest(DomParserTest.suite());
		suite.addTest(MarkOccurrenceTest.suite());
//		suite.addTest(CodeAssistTests.suite()); // UI tests!
//		suite.addTestSuite(PHPContentAssistProcessorTests.class); // UI tests!
		// $JUnit-END$
		return suite;
	}
}
