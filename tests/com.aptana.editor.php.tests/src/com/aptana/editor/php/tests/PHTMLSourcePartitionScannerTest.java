/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.tests;

import junit.framework.TestCase;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import com.aptana.editor.common.ExtendedFastPartitioner;
import com.aptana.editor.common.TextUtils;
import com.aptana.editor.common.text.rules.CompositePartitionScanner;
import com.aptana.editor.html.HTMLSourceConfiguration;
import com.aptana.editor.php.internal.ui.editor.PHPPartitionerSwitchStrategy;
import com.aptana.editor.php.internal.ui.editor.PHPSourceConfiguration;

@SuppressWarnings("nls")
public class PHTMLSourcePartitionScannerTest extends TestCase
{

	private ExtendedFastPartitioner partitioner;

	@Override
	protected void tearDown() throws Exception
	{
		partitioner = null;
		super.tearDown();
	}

	public void testPartition()
	{
		String source = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\n" //$NON-NLS-1$
				+ "<html>\n" //$NON-NLS-1$
				+ "<head><script><? echo  \"Hello, world!\" ?></script></head>\n" //$NON-NLS-1$
				+ "<body>\n" //$NON-NLS-1$
				+ "<p>The current time is <?= Time.now  ?>.</p>\n" //$NON-NLS-1$
				+ "</body></html>"; //$NON-NLS-1$
		// DOCTYPE
		assertContentType(HTMLSourceConfiguration.HTML_DOCTYPE, source, 0);
		// html tag
		assertContentType(HTMLSourceConfiguration.HTML_TAG, source, 110); // '<'html
		// inline JS script
		assertContentType(HTMLSourceConfiguration.HTML_SCRIPT, source, 123); // '<'script
		// PHP start switch
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 131); // '<'
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 132); // '?'
		// inline PHP inside the script
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 133); // ' 'echo
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 155); // "' '
		// PHP end switch
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 156); // '?'
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 157); // '>'
		// back to html
		assertContentType(HTMLSourceConfiguration.HTML_TAG_CLOSE, source, 158); // '<'html
		// a different PHP start switch
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 205); // '<'
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 207); // ?'='
		// inline PHP inside HTML
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 208); // ' 'Time
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 217); // now' '
		// a different PHP end switch
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 219); // '?'
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 220); // '>'
		// back to html
		assertContentType(HTMLSourceConfiguration.DEFAULT, source, 221); // '\n'
	}

	public void testSplitTag()
	{
		String source = "<body onload='alert()' <?= Time.now ?> id='body'>"; //$NON-NLS-1$
		assertContentType(HTMLSourceConfiguration.HTML_TAG, source, 0); // '<'body
		// PHP start switch
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 23); // '<'
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 24); // '?'
		// inline PHP inside the script
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 26); // ' 'Time
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 35); // now' '
		// PHP end switch
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 36); // '?'
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 37); // '>'
		// back to body
		assertContentType(HTMLSourceConfiguration.HTML_TAG, source, 38); // ' 'id
		assertContentType(HTMLSourceConfiguration.HTML_TAG, source, 39); // 'i'd=
	}

	public void testSplitAttribute()
	{
		String source = "<body class=' <?= Time.now ?> ' id='body'>"; //$NON-NLS-1$
		assertContentType(HTMLSourceConfiguration.HTML_TAG, source, 0); // '<'body
		// PHP start switch
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 14); // '<'
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 15); // '?'
		// inline PHP inside the script
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 17); // ' 'Time
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 26); // now' '
		// PHP end switch
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 27); // '?'
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 28); // '>'
		// back to body
		assertContentType(HTMLSourceConfiguration.HTML_TAG, source, 29); // %>' ''
		assertContentType(HTMLSourceConfiguration.HTML_TAG, source, 32); // 'i'd=
	}

	public void testBetweenTags()
	{
		String source = "<body class=''><?= Time.now ?></div>"; //$NON-NLS-1$
		assertContentType(HTMLSourceConfiguration.HTML_TAG, source, 0); // '<'body
		assertContentType(HTMLSourceConfiguration.HTML_TAG, source, 14); // '>'
		// PHP start switch
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 15); // '<'
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 16); // '?'
		// inline PHP inside the script
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 18); // ' 'Time
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 27); // now' '
		// PHP end switch
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 28); // '?'
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 29); // '>'
		// div
		assertContentType(HTMLSourceConfiguration.HTML_TAG_CLOSE, source, 30); // ?>'<'
	}

	public void testAfterTagBeforeSpace()
	{
		String source = "<body class=''><?= Time.now ?> </div>"; //$NON-NLS-1$
		assertContentType(HTMLSourceConfiguration.HTML_TAG, source, 0); // '<'body
		assertContentType(HTMLSourceConfiguration.HTML_TAG, source, 14); // '>'
		// PHP start switch
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 15); // '<'
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 16); // '?'
		// inline PHP inside the script
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 18); // ' 'Time
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 27); // now' '
		// PHP end switch
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 28); // '?'
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 29); // '>'
		// space
		assertContentType(HTMLSourceConfiguration.DEFAULT, source, 30); // ' '
		// div
		assertContentType(HTMLSourceConfiguration.HTML_TAG_CLOSE, source, 31); // ?>'<'
	}

	public void testAfterSpaceBeforeTag()
	{
		String source = "<body class=''> <?= Time.now ?></div>"; //$NON-NLS-1$
		assertContentType(HTMLSourceConfiguration.HTML_TAG, source, 0); // '<'body
		assertContentType(HTMLSourceConfiguration.HTML_TAG, source, 14); // '>'
		// space
		assertContentType(HTMLSourceConfiguration.DEFAULT, source, 15); // ' '
		// PHP start switch
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 16); // '<'
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 17); // '?'
		// inline PHP inside the script
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 19); // ' 'Time
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 28); // now' '
		// PHP end switch
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 29); // '?'
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 30); // '>'
		// div
		assertContentType(HTMLSourceConfiguration.HTML_TAG_CLOSE, source, 31); // ?>'<'
	}

	public void testPHPDoc()
	{
		String source = "<?php\n" + "/**\n" + " * This is a PHPDoc partition.\n" + " **/\n" + "?>";
		// PHP start switch
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 0); // '<'
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 1); // '?'
		// PHPDoc comment
		assertContentType(PHPSourceConfiguration.PHP_DOC_COMMENT, source, 6); // /**
		assertContentType(PHPSourceConfiguration.PHP_DOC_COMMENT, source, 17); //
		assertContentType(PHPSourceConfiguration.PHP_DOC_COMMENT, source, 44); // **/
		// PHP end switch
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 46); // '?'
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 47); // '>'
	}

	public void testMultilinePHPComment()
	{
		String source = "<?php\n" + "/*\n" + " * This is not a PHPDoc partition.\n" + " */\n" + "?>";
		// PHP start switch
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 0); // '<'
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 1); // '?'
		// Multiline comment
		assertContentType(PHPSourceConfiguration.PHP_MULTI_LINE_COMMENT, source, 6); // /*
		assertContentType(PHPSourceConfiguration.PHP_MULTI_LINE_COMMENT, source, 17); //
		assertContentType(PHPSourceConfiguration.PHP_MULTI_LINE_COMMENT, source, 46); // */
		// PHP end switch
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 48); // '?'
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 49); // '>'
	}

	public void testHashSingleLinePHPComment()
	{
		String source = "<?php\n" + "# This is a singleline comment.\n" + "?>";
		// PHP start switch
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 0); // '<'
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 1); // '?'
		// Single line comment
		assertContentType(PHPSourceConfiguration.PHP_HASH_LINE_COMMENT, source, 6); // #
		assertContentType(PHPSourceConfiguration.PHP_HASH_LINE_COMMENT, source, 21); //
		assertContentType(PHPSourceConfiguration.PHP_HASH_LINE_COMMENT, source, 36); // .
		// PHP end switch
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 38); // '?'
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 39); // '>'
	}

	public void testSlashSingleLinePHPComment()
	{
		String source = "<?php\n" + "// This is a singleline comment.\n" + "?>";
		// PHP start switch
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 0); // '<'
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 1); // '?'
		// Single line comment
		assertContentType(PHPSourceConfiguration.PHP_SLASH_LINE_COMMENT, source, 6); // /
		assertContentType(PHPSourceConfiguration.PHP_SLASH_LINE_COMMENT, source, 7); // /
		assertContentType(PHPSourceConfiguration.PHP_SLASH_LINE_COMMENT, source, 21); //
		assertContentType(PHPSourceConfiguration.PHP_SLASH_LINE_COMMENT, source, 37); // .
		// PHP end switch
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 39); // '?'
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 40); // '>'
	}

	public void testSingleQuotedString()
	{
		String source = "<?php\n" + "'This is a single quoted string.'\n" + "?>";
		// PHP start switch
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 0); // '<'
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 1); // '?'
		// Single quoted string
		assertContentType(PHPSourceConfiguration.PHP_STRING_SINGLE, source, 6); // '
		assertContentType(PHPSourceConfiguration.PHP_STRING_SINGLE, source, 7); // T
		assertContentType(PHPSourceConfiguration.PHP_STRING_SINGLE, source, 20); //
		assertContentType(PHPSourceConfiguration.PHP_STRING_SINGLE, source, 37); // .
		assertContentType(PHPSourceConfiguration.PHP_STRING_SINGLE, source, 38); // '
		// PHP end switch
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 40); // '?'
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 41); // '>'
	}

	public void testDoubleQuotedString()
	{
		String source = "<?php\n" + "\"This is a double quoted string.\"\n" + "?>";
		// PHP start switch
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 0); // '<'
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 1); // '?'
		// Double quoted string
		assertContentType(PHPSourceConfiguration.PHP_STRING_DOUBLE, source, 6); // "
		assertContentType(PHPSourceConfiguration.PHP_STRING_DOUBLE, source, 7); // T
		assertContentType(PHPSourceConfiguration.PHP_STRING_DOUBLE, source, 20); //
		assertContentType(PHPSourceConfiguration.PHP_STRING_DOUBLE, source, 37); // .
		assertContentType(PHPSourceConfiguration.PHP_STRING_DOUBLE, source, 38); // "
		// PHP end switch
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 40); // '?'
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 41); // '>'
	}

	// TODO Add a test for a heredoc partition
	// TODO Add a test for a nowdoc partition

	private void assertContentType(String contentType, String code, int offset)
	{
		assertEquals("Content type doesn't match expectations for: " + code.charAt(offset), contentType,
				getContentType(code, offset));
	}

	private String getContentType(String content, int offset)
	{
		if (partitioner == null)
		{
			IDocument document = new Document(content);
			CompositePartitionScanner partitionScanner = new CompositePartitionScanner(HTMLSourceConfiguration
					.getDefault().createSubPartitionScanner(), PHPSourceConfiguration.getDefault()
					.createSubPartitionScanner(), PHPPartitionerSwitchStrategy.getDefault());
			partitioner = new ExtendedFastPartitioner(partitionScanner, TextUtils.combine(new String[][] {
					CompositePartitionScanner.SWITCHING_CONTENT_TYPES,
					HTMLSourceConfiguration.getDefault().getContentTypes(),
					PHPSourceConfiguration.getDefault().getContentTypes() }));
			partitionScanner.setPartitioner(partitioner);
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
		return partitioner.getContentType(offset);
	}
}
