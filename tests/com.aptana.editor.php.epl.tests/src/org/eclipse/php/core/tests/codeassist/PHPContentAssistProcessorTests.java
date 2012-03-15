package org.eclipse.php.core.tests.codeassist;

import junit.framework.TestCase;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import com.aptana.editor.common.ExtendedFastPartitioner;
import com.aptana.editor.common.NullPartitionerSwitchStrategy;
import com.aptana.editor.common.tests.TextViewer;
import com.aptana.editor.common.text.rules.CompositePartitionScanner;
import com.aptana.editor.common.text.rules.NullSubPartitionScanner;
import com.aptana.editor.html.HTMLEditor;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.internal.contentAssist.PHPContentAssistProcessor;
import com.aptana.editor.php.internal.contentAssist.preferences.IContentAssistPreferencesConstants;
import com.aptana.editor.php.internal.ui.editor.PHPSourceConfiguration;
import com.aptana.editor.php.internal.ui.editor.PHPSourceEditor;

/**
 * @author Shalom Gibly <sgibly@appcelerator.com>
 */
@SuppressWarnings("nls")
public class PHPContentAssistProcessorTests extends TestCase
{

	private PHPSourceEditor editor;
	private PHPContentAssistProcessor processor;

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		editor = new PHPSourceEditor();
		processor = new PHPContentAssistProcessor(editor);
	}

	/*
	 * (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception
	{
		super.tearDown();
		editor = null;
		processor = null;
	}

	public void testInvalidEditor() throws Exception
	{
		HTMLEditor editor = new HTMLEditor();
		try
		{
			new PHPContentAssistProcessor(editor);
		}
		catch (Exception e)
		{
			return;
		}
		assertTrue("Created a PHPContentAssistProcessor with a non-PHP editor", false);
	}

	public void testNullEditor() throws Exception
	{
		try
		{
			new PHPContentAssistProcessor(null);
		}
		catch (Exception e)
		{
			return;
		}
		assertTrue("Created a PHPContentAssistProcessor with a null editor", false);
	}

	public void testAutoActivationLocation() throws Exception
	{
		IDocument document = new Document();
		document.set("<?ph");

		assertFalse("Illegal location for PHP auto-activation chars",
				processor.isValidAutoActivationLocation('f', 0, document, 4));

		document.set("<?php\n");
		assertTrue("Expected a valid PHP auto-activation char location",
				processor.isValidAutoActivationLocation('f', 0, document, 6));
	}

	public void testValidIdentifier() throws Exception
	{
		assertTrue("Expected $ to be a valid identifier", processor.isValidIdentifier('$', 0));
		assertTrue("Expected _ to be a valid identifier", processor.isValidIdentifier('_', 0));
		assertTrue("Expected 's' to be a valid identifier", processor.isValidIdentifier('s', 0));
		assertTrue("Expected 'A' to be a valid identifier", processor.isValidIdentifier('A', 0));
		assertFalse("Expected '^' to be an invalid identifier", processor.isValidIdentifier('^', 0));
		assertTrue("Expected '2' to be an invalid identifier", processor.isValidIdentifier('2', 0));
	}

	public void testContextInformationValidator() throws Exception
	{
		IContextInformationValidator validator = processor.getContextInformationValidator();
		assertNotNull("Expected a non-null context information validator", validator);
	}

	public void testValidActivationCharacters() throws Exception
	{
		assertNotNull("Expected a non-null completion proposal auto activation characters",
				processor.getCompletionProposalAutoActivationCharacters());
		assertNotNull("Expected a non-null context information auto activation characters",
				processor.getContextInformationAutoActivationCharacters());
	}

	public void testComputeContextProposals() throws Exception
	{
		IDocument document = new Document("<?php\ng");
		CompositePartitionScanner partitionScanner = new CompositePartitionScanner(PHPSourceConfiguration.getDefault()
				.createSubPartitionScanner(), new NullSubPartitionScanner(), new NullPartitionerSwitchStrategy());
		ExtendedFastPartitioner partitioner = new ExtendedFastPartitioner(partitionScanner, PHPSourceConfiguration
				.getDefault().getContentTypes());
		partitionScanner.setPartitioner(partitioner);
		partitioner.connect(document);
		document.setDocumentPartitioner(partitioner);
		ITextViewer viewer = new TextViewer(document);
		ICompletionProposal[] proposals = processor.computeCompletionProposals(viewer, 7);
		assertNotNull("Proposals were null", proposals);
		assertTrue("Expected items in the proposals list", proposals.length > 0);
	}

	public void testComputeContextInformation() throws Exception
	{
		IDocument document = new Document("<?php\n$a = new PDO(");
		CompositePartitionScanner partitionScanner = new CompositePartitionScanner(PHPSourceConfiguration.getDefault()
				.createSubPartitionScanner(), new NullSubPartitionScanner(), new NullPartitionerSwitchStrategy());
		ExtendedFastPartitioner partitioner = new ExtendedFastPartitioner(partitionScanner, PHPSourceConfiguration
				.getDefault().getContentTypes());
		partitionScanner.setPartitioner(partitioner);
		partitioner.connect(document);
		document.setDocumentPartitioner(partitioner);
		ITextViewer viewer = new TextViewer(document);
		IContextInformation[] contextInfo = processor.computeContextInformation(viewer, 19);
		assertNotNull("IContextInformation was null", contextInfo);
		assertTrue("Expected items in the contextInfo list", contextInfo.length > 0);
	}

	public void testInsertModeOverwrite() throws Exception
	{
		IPreferenceStore preferences = PHPEditorPlugin.getDefault().getPreferenceStore();
		try
		{
			preferences.setValue(IContentAssistPreferencesConstants.INSERT_MODE,
					IContentAssistPreferencesConstants.INSERT_MODE_INSERT);
			IDocument document = new Document("<?php\nfunction");
			CompositePartitionScanner partitionScanner = new CompositePartitionScanner(PHPSourceConfiguration.getDefault()
					.createSubPartitionScanner(), new NullSubPartitionScanner(), new NullPartitionerSwitchStrategy());
			ExtendedFastPartitioner partitioner = new ExtendedFastPartitioner(partitionScanner, PHPSourceConfiguration
					.getDefault().getContentTypes());
			partitionScanner.setPartitioner(partitioner);
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
			ITextViewer viewer = new TextViewer(document);
			ICompletionProposal[] proposals = processor.computeCompletionProposals(viewer, 7);
			assertNotNull("Proposals were null", proposals);
			assertTrue("Expected items in the proposals list", proposals.length > 0);
		}
		finally
		{
			preferences.setValue(IContentAssistPreferencesConstants.INSERT_MODE,
					IContentAssistPreferencesConstants.INSERT_MODE_OVERWRITE);
		}

	}
}
