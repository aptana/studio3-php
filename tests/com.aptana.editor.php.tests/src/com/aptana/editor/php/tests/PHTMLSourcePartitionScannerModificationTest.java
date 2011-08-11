/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.editor.php.tests;

import junit.framework.TestCase;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;

import com.aptana.editor.common.ExtendedFastPartitioner;
import com.aptana.editor.common.TextUtils;
import com.aptana.editor.common.text.rules.CompositePartitionScanner;
import com.aptana.editor.html.HTMLSourceConfiguration;
import com.aptana.editor.php.internal.ui.editor.PHPPartitionerSwitchStrategy;
import com.aptana.editor.php.internal.ui.editor.PHPSourceConfiguration;

/**
 * @author Max Stepanov
 *
 */
@SuppressWarnings("nls")
public class PHTMLSourcePartitionScannerModificationTest extends TestCase {

	private IDocument document;
	private ExtendedFastPartitioner partitioner;

	@Override
	protected void setUp() throws Exception {
		CompositePartitionScanner partitionScanner = new CompositePartitionScanner(HTMLSourceConfiguration
				.getDefault().createSubPartitionScanner(), PHPSourceConfiguration.getDefault()
				.createSubPartitionScanner(), PHPPartitionerSwitchStrategy.getDefault());
		partitioner = new ExtendedFastPartitioner(partitionScanner, TextUtils.combine(new String[][] {
				CompositePartitionScanner.SWITCHING_CONTENT_TYPES,
				HTMLSourceConfiguration.getDefault().getContentTypes(),
				PHPSourceConfiguration.getDefault().getContentTypes() }));
		partitionScanner.setPartitioner(partitioner);
		
		document = new Document();
		partitioner.connect(document);
		document.setDocumentPartitioner(partitioner);
	}

	@Override
	protected void tearDown() throws Exception {
		partitioner.disconnect();
		partitioner = null;
		document = null;
	}
	
	private void paste(int offset, int replaceLength, String text) throws BadLocationException {
		document.replace(offset, 0, text);		
	}

	private void paste(int offset, String text) throws BadLocationException {
		paste(offset, 0, text);
	}

	private void type(int offset, String text) throws BadLocationException {
		for (char ch : text.toCharArray()) {
			document.replace(offset++, 0, String.valueOf(ch));
		}
	}

	private void assertContentType(String contentType, int offset, int length) throws BadLocationException {
		String messagePrefix = "doesn't match expectations for: " + document.get(offset, length);
		ITypedRegion partition = partitioner.getPartition(offset);
		assertEquals("Content type "+messagePrefix, contentType, partition.getType());
		assertEquals("Offset "+messagePrefix, offset, partition.getOffset());
		assertEquals("Length "+messagePrefix, length, partition.getLength());
	}
	
	private void typeAndTest(int offset, String text, String contentType) throws BadLocationException {
		type(offset, text);
		assertContentType(contentType, offset, text.length());
	}

	private void typeAndTest(String text, String contentType) throws BadLocationException {
		typeAndTest(document.getLength(), text, contentType);
	}

	public void testSimpleTags() throws BadLocationException {
		typeAndTest("<p>", HTMLSourceConfiguration.HTML_TAG);
		typeAndTest("</p>", HTMLSourceConfiguration.HTML_TAG_CLOSE);
	}
	
	public void testPHPBetweenTags() throws BadLocationException {
		typeAndTest("<p>", HTMLSourceConfiguration.HTML_TAG);
		typeAndTest("<?", CompositePartitionScanner.START_SWITCH_TAG);
		typeAndTest("?>", CompositePartitionScanner.END_SWITCH_TAG);
		typeAndTest("</p>", HTMLSourceConfiguration.HTML_TAG_CLOSE);
	}

	public void testPHPBetweenTagsInserted() throws BadLocationException {
		typeAndTest("<p>", HTMLSourceConfiguration.HTML_TAG);
		typeAndTest("</p>", HTMLSourceConfiguration.HTML_TAG_CLOSE);
		typeAndTest(3, "<?", CompositePartitionScanner.START_SWITCH_TAG);
		typeAndTest(5, "?>", CompositePartitionScanner.END_SWITCH_TAG);
		
		assertContentType(HTMLSourceConfiguration.HTML_TAG, 0, 3);
		assertContentType(HTMLSourceConfiguration.HTML_TAG_CLOSE, 7, 4);
	}

	public void testPHPBetweenTagsPasted() throws BadLocationException {
		typeAndTest("<p>", HTMLSourceConfiguration.HTML_TAG);
		typeAndTest("</p>", HTMLSourceConfiguration.HTML_TAG_CLOSE);
		paste(3, "<??>");
		
		assertContentType(HTMLSourceConfiguration.HTML_TAG, 0, 3);
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, 3, 2);
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, 5, 2);
		assertContentType(HTMLSourceConfiguration.HTML_TAG_CLOSE, 7, 4);
	}

	public void testPHPInTag() throws BadLocationException {
		typeAndTest("<p ", HTMLSourceConfiguration.HTML_TAG);
		typeAndTest("<?=", CompositePartitionScanner.START_SWITCH_TAG);
		typeAndTest("?>", CompositePartitionScanner.END_SWITCH_TAG);
		typeAndTest(" >", HTMLSourceConfiguration.HTML_TAG);
		
		assertContentType(HTMLSourceConfiguration.HTML_TAG, 0, 3);
		assertContentType(HTMLSourceConfiguration.HTML_TAG, 8, 2);
	}

	public void testPHPInTagInserted() throws BadLocationException {
		typeAndTest("<p  >", HTMLSourceConfiguration.HTML_TAG);
		typeAndTest(3, "<?=", CompositePartitionScanner.START_SWITCH_TAG);
		typeAndTest(6, "?>", CompositePartitionScanner.END_SWITCH_TAG);
		assertContentType(HTMLSourceConfiguration.HTML_TAG, 0, 3);
		
		assertContentType(HTMLSourceConfiguration.HTML_TAG, 8, 2);
	}

	public void testPHPInTagPasted() throws BadLocationException {
		typeAndTest("<p  >", HTMLSourceConfiguration.HTML_TAG);
		paste(3, "<?=?>");
		
		assertContentType(HTMLSourceConfiguration.HTML_TAG, 0, 3);
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, 3, 3);
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, 6, 2);
		assertContentType(HTMLSourceConfiguration.HTML_TAG, 8, 2);
	}

	public void testPHPInAttribute() throws BadLocationException {
		typeAndTest("<p class=\"", HTMLSourceConfiguration.HTML_TAG);
		typeAndTest("<?=", CompositePartitionScanner.START_SWITCH_TAG);
		typeAndTest("?>", CompositePartitionScanner.END_SWITCH_TAG);
		typeAndTest("\" >", HTMLSourceConfiguration.HTML_TAG);
		
		assertContentType(HTMLSourceConfiguration.HTML_TAG, 0, 10);
		assertContentType(HTMLSourceConfiguration.HTML_TAG, 15, 3);
	}

	public void testPHPInAttributeInserted() throws BadLocationException {
		typeAndTest("<p class=\"\" >", HTMLSourceConfiguration.HTML_TAG);
		typeAndTest(10, "<?=", CompositePartitionScanner.START_SWITCH_TAG);
		typeAndTest(13, "?>", CompositePartitionScanner.END_SWITCH_TAG);
		
		assertContentType(HTMLSourceConfiguration.HTML_TAG, 0, 10);
		assertContentType(HTMLSourceConfiguration.HTML_TAG, 15, 3);
	}

	public void testPHPInAttributePasted() throws BadLocationException {
		typeAndTest("<p class=\"\" >", HTMLSourceConfiguration.HTML_TAG);
		paste(10, "<?=?>");
		
		assertContentType(HTMLSourceConfiguration.HTML_TAG, 0, 10);
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, 10, 3);
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, 13, 2);
		assertContentType(HTMLSourceConfiguration.HTML_TAG, 15, 3);
	}

	public void testPHPInAttributeWithGT() throws BadLocationException {
		typeAndTest("<p class=\"", HTMLSourceConfiguration.HTML_TAG);
		typeAndTest("<?=", CompositePartitionScanner.START_SWITCH_TAG);
		typeAndTest("?>", CompositePartitionScanner.END_SWITCH_TAG);
		typeAndTest(">\" >", HTMLSourceConfiguration.HTML_TAG);
		
		assertContentType(HTMLSourceConfiguration.HTML_TAG, 0, 10);
		assertContentType(HTMLSourceConfiguration.HTML_TAG, 15, 4);
	}

	public void testPHPInAttributeWithGTInserted() throws BadLocationException {
		typeAndTest("<p class=\">\" >", HTMLSourceConfiguration.HTML_TAG);
		typeAndTest(10, "<?=", CompositePartitionScanner.START_SWITCH_TAG);
		typeAndTest(13, "?>", CompositePartitionScanner.END_SWITCH_TAG);
		assertContentType(HTMLSourceConfiguration.HTML_TAG, 0, 10);
		assertContentType(HTMLSourceConfiguration.HTML_TAG, 15, 4);
	}

	public void testPHPInAttributeWithGTPasted() throws BadLocationException {
		typeAndTest("<p class=\">\" >", HTMLSourceConfiguration.HTML_TAG);
		paste(10, "<?=?>");
		assertContentType(HTMLSourceConfiguration.HTML_TAG, 0, 10);
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, 10, 3);
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, 13, 2);
		assertContentType(HTMLSourceConfiguration.HTML_TAG, 15, 4);
	}

	public void testHTMLAroundPHPInserted1() throws BadLocationException {
		typeAndTest("<?=", CompositePartitionScanner.START_SWITCH_TAG);
		typeAndTest("?>", CompositePartitionScanner.END_SWITCH_TAG);
		typeAndTest(0, "<p ", HTMLSourceConfiguration.HTML_TAG);
		typeAndTest(8, " >", HTMLSourceConfiguration.HTML_TAG);
		
		assertContentType(HTMLSourceConfiguration.HTML_TAG, 0, 3);
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, 3, 3);
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, 6, 2);
		assertContentType(HTMLSourceConfiguration.HTML_TAG, 8, 2);
	}

	public void failing_testHTMLAroundPHPInserted2() throws BadLocationException {
		typeAndTest("<?=", CompositePartitionScanner.START_SWITCH_TAG);
		typeAndTest("?>", CompositePartitionScanner.END_SWITCH_TAG);
		typeAndTest(5, " >", HTMLSourceConfiguration.DEFAULT);
		typeAndTest(0, "<p ", HTMLSourceConfiguration.HTML_TAG);
		
		assertContentType(HTMLSourceConfiguration.HTML_TAG, 0, 3);
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, 3, 3);
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, 6, 2);
		assertContentType(HTMLSourceConfiguration.HTML_TAG, 8, 2);
	}

}
