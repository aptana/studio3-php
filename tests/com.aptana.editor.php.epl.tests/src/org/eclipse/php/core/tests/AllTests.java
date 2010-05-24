package org.eclipse.php.core.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.php.core.tests.dom_ast.parser.DomParserTests;

public class AllTests
{

	public static Test suite()
	{
		TestSuite suite = new TestSuite(AllTests.class.getName());
		// $JUnit-BEGIN$
		suite.addTest(DomParserTests.suite());
		// $JUnit-END$
		return suite;
	}

}
