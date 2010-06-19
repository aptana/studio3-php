package org.eclipse.php.core.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.php.core.tests.codeassist.CodeAssistTests;
import org.eclipse.php.core.tests.dom_ast.parser.DomParserTests;
import org.eclipse.php.core.tests.markoccurrence.MarkOccurrenceTests;

public class AllTests
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite(AllTests.class.getName());
		// $JUnit-BEGIN$
		suite.addTest(DomParserTests.suite());
		suite.addTest(MarkOccurrenceTests.suite());
		suite.addTest(CodeAssistTests.suite());
		// $JUnit-END$
		return suite;
	}

}
