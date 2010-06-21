/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Zend Technologies
 *******************************************************************************/
package org.eclipse.php.core.tests.codeassist;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.php.core.tests.AbstractPDTTTest;
import org.eclipse.php.core.tests.TestUtils;
import org.eclipse.php.core.tests.codeassist.CodeAssistPdttFile.ExpectedProposal;
import org.eclipse.php.internal.core.PHPVersion;

import com.aptana.editor.common.ExtendedFastPartitioner;
import com.aptana.editor.common.NullPartitionerSwitchStrategy;
import com.aptana.editor.common.text.rules.CompositePartitionScanner;
import com.aptana.editor.common.text.rules.NullSubPartitionScanner;
import com.aptana.editor.php.core.PHPNature;
import com.aptana.editor.php.core.model.IModelElement;
import com.aptana.editor.php.core.model.ISourceModule;
import com.aptana.editor.php.indexer.PHPGlobalIndexer;
import com.aptana.editor.php.internal.contentAssist.PHPCompletionProposal;
import com.aptana.editor.php.internal.contentAssist.PHPContentAssistProcessor;
import com.aptana.editor.php.internal.indexer.language.PHPBuiltins;
import com.aptana.editor.php.internal.model.ModelException;
import com.aptana.editor.php.internal.model.ModelManager;
import com.aptana.editor.php.internal.model.utils.ModelUtils;
import com.aptana.editor.php.internal.ui.editor.PHPSourceConfiguration;
import com.aptana.editor.php.internal.ui.editor.PHPSourceEditor;

@SuppressWarnings("nls")
public class CodeAssistTests extends AbstractPDTTTest {

	protected static final char OFFSET_CHAR = '|';
	protected static final Map<PHPVersion, String[]> TESTS = new LinkedHashMap<PHPVersion, String[]>();
	static {
		TESTS.put(PHPVersion.PHP5, new String[] {
				"/workspace/codeassist/php5/exclusive",
				"/workspace/codeassist/php5" });
		TESTS.put(PHPVersion.PHP5_3, new String[] {
				"/workspace/codeassist/php5", "/workspace/codeassist/php53" });
	};

	protected static IProject project;
	protected static IFile testFile;

	public static void setUpSuite() throws Exception {
		project = ResourcesPlugin.getWorkspace().getRoot().getProject(
				"CodeAssistTests");
		if (project.exists()) {
			return;
		}

		project.create(null);
		project.open(null);

		// configure nature
		IProjectDescription desc = project.getDescription();
		desc.setNatureIds(new String[] { PHPNature.NATURE_ID });
		project.setDescription(desc, null);
		PHPBuiltins.getInstance();
		PHPGlobalIndexer.getInstance();
		ModelManager.getInstance();
		try
		{
			Thread.sleep(4000L);
		}
		catch (InterruptedException ie){}
	}

	public static void tearDownSuite() throws Exception {
		project.close(null);
		project.delete(true, true, null);
		project = null;
	}

	public CodeAssistTests(String description) {
		super(description);
	}

	public static Test suite() {

		TestSuite suite = new TestSuite("Auto Code Assist Tests");

		for (final PHPVersion phpVersion : TESTS.keySet()) {
			TestSuite phpVerSuite = new TestSuite(phpVersion.getAlias());

			for (String testsDirectory : TESTS.get(phpVersion)) {

				for (final String fileName : getPDTTFiles(testsDirectory)) {
					try {
						final CodeAssistPdttFile pdttFile = new CodeAssistPdttFile(
								fileName);
						phpVerSuite.addTest(new CodeAssistTests(phpVersion
								.getAlias()
								+ " - /" + fileName) {

							protected void setUp() throws Exception {
								TestUtils.setProjectPhpVersion(project,
										phpVersion);
								pdttFile.applyPreferences();
							}

							protected void tearDown() throws Exception {
								if (testFile != null) {
									testFile.delete(true, null);
									testFile = null;
								}
							}

							protected void runTest() throws Throwable {
								ICompletionProposal[] proposals = getProposals(pdttFile
										.getFile());
								compareProposals(proposals, pdttFile);
							}
						});
					} catch (final Exception e) {
						phpVerSuite.addTest(new TestCase(fileName) { // dummy
									// test
									// indicating
									// PDTT
									// file
									// parsing
									// failure
									protected void runTest() throws Throwable {
										throw e;
									}
								});
					}
				}
			}
			suite.addTest(phpVerSuite);
		}

		// Create a setup wrapper
		TestSetup setup = new TestSetup(suite) {
			protected void setUp() throws Exception {
				setUpSuite();
			}

			protected void tearDown() throws Exception {
				tearDownSuite();
			}
		};
		return setup;
	}

	/**
	 * Creates test file with the specified content and calculates the offset at
	 * OFFSET_CHAR. Offset character itself is stripped off.
	 * 
	 * @param data
	 *            File data
	 * @return offset where's the offset character set.
	 * @throws Exception
	 */
	protected static int createFile(String data) throws Exception {
		int offset = data.lastIndexOf(OFFSET_CHAR);
		if (offset == -1) {
			throw new IllegalArgumentException("Offset character is not set");
		}

		// replace the offset character
		data = data.substring(0, offset) + data.substring(offset + 1);

		testFile = project.getFile("test.php");
		if (testFile.exists()) {
			testFile.refreshLocal(IResource.DEPTH_ZERO, null);
			testFile.delete(true, null);
		}
		testFile.create(new ByteArrayInputStream(data.getBytes()), true, null);
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		project.build(IncrementalProjectBuilder.FULL_BUILD, null);

		TestUtils.waitForIndexer();
		// PHPCoreTests.waitForAutoBuild();

		return offset;
	}

	protected static ISourceModule getSourceModule() {
		return ModelUtils.getModule(testFile);
	}

	public static ICompletionProposal[] getProposals(String data)
			throws Exception {
		int offset = createFile(data);
		return getProposals(offset);
	}

	public static ICompletionProposal[] getProposals(int offset)
			throws ModelException {
		return getProposals(getSourceModule(), offset);
	}

	public static ICompletionProposal[] getProposals(ISourceModule sourceModule,
			int offset) throws ModelException {
		PHPSourceEditor editor = new PHPSourceEditor();
		editor.computeModule(sourceModule.getResource().getLocationURI().toString());
		PHPContentAssistProcessor processor = new PHPContentAssistProcessor(editor);
		// 
		IDocument document = new Document(new String(sourceModule.getSourceAsCharArray()));
		CompositePartitionScanner partitionScanner = new CompositePartitionScanner(PHPSourceConfiguration
				.getDefault().createSubPartitionScanner(), new NullSubPartitionScanner(),
				new NullPartitionerSwitchStrategy());
		ExtendedFastPartitioner partitioner = new ExtendedFastPartitioner(partitionScanner, PHPSourceConfiguration.getDefault()
				.getContentTypes());
		partitionScanner.setPartitioner(partitioner);
		partitioner.connect(document);
		document.setDocumentPartitioner(partitioner);
		ICompletionProposal[] computeCompletionProposals = processor.computeCompletionProposals(document, offset);
		return computeCompletionProposals;
	}

	public static void compareProposals(ICompletionProposal[] proposals,
			CodeAssistPdttFile pdttFile) throws Exception {
		ExpectedProposal[] expectedProposals = pdttFile.getExpectedProposals();

		boolean proposalsEqual = true;
		if (proposals.length == expectedProposals.length) {
			Set<String> actualProposals = new HashSet<String>();
			for (ICompletionProposal proposal : proposals)
			{
				String string = new String(((PHPCompletionProposal)proposal).getReplacementString().replaceAll("\\(\\)", "").toLowerCase());
				if (string.startsWith("$"))
				{
					actualProposals.add(string.substring(1).trim());
				}
				else
				{
					actualProposals.add(string.trim());
				}
			}
			for (ExpectedProposal expectedProposal : pdttFile
					.getExpectedProposals()) {
				String name = expectedProposal.name.toLowerCase();
				if (expectedProposal.type == IModelElement.FIELD && name.startsWith("$"))
				{
					name = new String(name.substring(1));
				}
				if (!containsPrefixOf(actualProposals, name))
				{
					containsPrefixOf(actualProposals, name);
					proposalsEqual = false;
					break;
				}
			}
		} else {
			proposalsEqual = false;
		}

		if (!proposalsEqual) {
			StringBuilder errorBuf = new StringBuilder();
			errorBuf
					.append("\nEXPECTED COMPLETIONS LIST:\n-----------------------------\n");
			errorBuf.append(pdttFile.getExpected());
			errorBuf
					.append("\nACTUAL COMPLETIONS LIST:\n-----------------------------\n");
			for (ICompletionProposal p : proposals) {
				errorBuf.append("keyword(").append(p.getDisplayString()).append(")\n");
			}
			fail(errorBuf.toString());
		}
	}

	/**
	 * Test if the given set of proposals contains a method/function prefix of of the given name.
	 * 
	 * @param proposals
	 * @param name
	 * @return True if the prefix contained in the set; False, otherwise.
	 */
	private static boolean containsPrefixOf(Set<String> proposals, String name)
	{
		if (proposals.contains(name))
		{
			// The quick test
			return true;
		}
		// Otherwise, we have to scan the entire list to look for that prefix.
		for (String proposal : proposals)
		{
			if (proposal.startsWith(name))
			{
				if (proposal.charAt(name.length()) == '(')
				{
					return true;
				}
			}
		}
		return false;
	}
}
