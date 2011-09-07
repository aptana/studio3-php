/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.tests;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import com.aptana.core.util.IOUtil;
import com.aptana.editor.php.internal.core.IPHPConstants;
import com.aptana.editor.php.internal.parser.nodes.ParseNodesIterator;
import com.aptana.parsing.ParserPoolFactory;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.parsing.ast.IParseRootNode;

/**
 * PHP Outline tests. We test the outline through the PHP Parser since it generates the NodeBuilderVisitor and the
 * NodeBuilder that generate the input for the outline.
 * 
 * @author Shalom Gibly <sgibly@appcelerator.com>
 */
public class PHTMLOutlineTest extends TestCase
{
	// Indicate that the test should generate the output files goldens (should be false on a normal testing operation)
	private static final boolean GENERATE_OUTPUT = false;

	private static String TEST_BUNDLE_ID = "com.aptana.editor.php.tests";
	private static final String PHP_FILE_TYPE = ".php";
	private static final String INPUT_DIRECTORY = "resources/outline/input";
	private static final String OUTPUT_DIRECTORY = "resources/outline/output";

	/**
	 * Creates a test suite that loads a dynamic list of test-cases, one for each file in the working-directory.
	 * 
	 * @return A {@link Test} that wraps a {@link TestSuite}.
	 */
	public Test suite()
	{
		TestSuite suite = new TestSuite("PHP Outline Tests");
		String[] inputFiles = getFiles(INPUT_DIRECTORY);
		for (final String fileName : inputFiles)
		{
			suite.addTest(new TestCase(Path.fromPortableString(fileName).lastSegment())
			{
				@Override
				protected void runTest() throws Throwable
				{
					outlineTest(fileName);
				}
			});
		}

		// Wrap everything in a TestSetup
		TestSetup setup = new TestSetup(suite)
		{
			protected void setUp() throws Exception
			{
				setUpSuite();
			}

			protected void tearDown() throws Exception
			{
				tearDownSuite();
			}
		};
		return setup;
	}

	/**
	 * Setup the test suite.
	 * 
	 * @throws Exception
	 */
	protected void setUpSuite() throws Exception
	{
		// do nothing for now
	}

	/**
	 * Tear-down the test suite.
	 * 
	 * @throws Exception
	 */
	protected void tearDownSuite()
	{
		// do nothing for now
	}

	/**
	 * Execute a single outline test.
	 * 
	 * @param file
	 * @param filename
	 * @param fileType
	 */
	protected void outlineTest(String filename)
	{
		try
		{
			String source = readContent(filename);
			IParseRootNode parseRootNode = ParserPoolFactory.parse(IPHPConstants.CONTENT_TYPE_PHP, source);
			ParseNodesIterator iterator = new ParseNodesIterator(parseRootNode);
			StringBuilder builder = new StringBuilder();
			while (iterator.hasNext())
			{
				IParseNode next = iterator.next();
				builder.append(next.getElementName());
				builder.append(" [");
				builder.append(next.getLanguage());
				builder.append(", (");
				builder.append(next.getStartingOffset());
				builder.append(", ");
				builder.append(next.getEndingOffset());
				builder.append(")]\n");
			}
			String outputFileName = filename.replaceFirst(INPUT_DIRECTORY, OUTPUT_DIRECTORY);
			if (GENERATE_OUTPUT)
			{
				writeContent(outputFileName, builder.toString());
			}
			else
			{
				// grab the output file content and compare to the buffer.
				assertEquals("Unexpected outline output", readContent(outputFileName), builder.toString());
			}
		}
		catch (Exception e)
		{
			assertFalse(e.getMessage(), true);
		}
	}

	/**
	 * Write the content into a file in the output directory. In case the file exists, no writing will be performed.
	 * 
	 * @param outputFileName
	 *            The output file name.
	 * @param content
	 * @throws IOException
	 */
	private void writeContent(String outputFileName, String content) throws IOException
	{
		// in case the output file does not exist, create one and inject the output into it.
		URL fileURL = FileLocator.find(Platform.getBundle(TEST_BUNDLE_ID), Path.fromPortableString(outputFileName),
				null);
		if (fileURL != null)
		{
			// we do not overwrite existing output files.
			return;
		}
		// This one is done locally when generating the tests, so we rely on absolute file paths.
		IPath path = Path.fromOSString(new File("").getAbsolutePath());
		path = path.removeLastSegments(1);
		path = path.append(TEST_BUNDLE_ID);
		path = path.append(outputFileName);
		IOUtil.write(new FileOutputStream(path.toFile()), content);
	}

	/**
	 * Returns the content for the file-name
	 * 
	 * @param filename
	 * @return The string content
	 * @throws IOException
	 */
	private String readContent(String filename) throws IOException
	{
		InputStream stream = FileLocator.openStream(Platform.getBundle(TEST_BUNDLE_ID),
				Path.fromPortableString(filename), false);
		return IOUtil.read(stream);
	}

	/**
	 * Returns the file that will be tested.
	 * 
	 * @param directory
	 *            The input/output directory to scan files for.
	 */
	@SuppressWarnings("unchecked")
	protected String[] getFiles(String directory)
	{
		Enumeration<String> entryPaths = Platform.getBundle(TEST_BUNDLE_ID).getEntryPaths(directory);
		ArrayList<String> filePaths = new ArrayList<String>();
		String path;

		while (entryPaths.hasMoreElements())
		{
			path = entryPaths.nextElement();
			// Check for correct file type
			if (path.endsWith(PHP_FILE_TYPE))
			{
				filePaths.add(path);
			}
		}
		return filePaths.toArray(new String[filePaths.size()]);
	}
}
