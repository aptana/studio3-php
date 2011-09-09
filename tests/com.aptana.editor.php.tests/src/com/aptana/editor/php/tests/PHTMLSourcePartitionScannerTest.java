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
import com.aptana.editor.css.CSSSourceConfiguration;
import com.aptana.editor.html.HTMLSourceConfiguration;
import com.aptana.editor.js.JSSourceConfiguration;
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
		assertContentType(HTMLSourceConfiguration.HTML_DOCTYPE, source, 0, 109);
		assertContentType(HTMLSourceConfiguration.DEFAULT, source, 109, 1); // '\n'
		assertContentType(HTMLSourceConfiguration.HTML_TAG, source, 110, 6); // '<'html
		assertContentType(HTMLSourceConfiguration.DEFAULT, source, 116, 1); // '\n'
		assertContentType(HTMLSourceConfiguration.HTML_TAG, source, 117, 6);
		assertContentType(HTMLSourceConfiguration.HTML_SCRIPT, source, 123, 8); // '<'script
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 131, 2); // '<'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 133, 7); // ' 'echo
		assertContentType(PHPSourceConfiguration.PHP_STRING_DOUBLE, source, 140, 15); // ' 'echo
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 155, 1);
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 156, 2); // '?'
		assertContentType(HTMLSourceConfiguration.HTML_TAG_CLOSE, source, 158, 16);
		assertContentType(HTMLSourceConfiguration.DEFAULT, source, 174, 1); // '\n'
		assertContentType(HTMLSourceConfiguration.HTML_TAG, source, 175, 6); // '<'body
		assertContentType(HTMLSourceConfiguration.DEFAULT, source, 181, 1); // '\n'
		assertContentType(HTMLSourceConfiguration.HTML_TAG, source, 182, 3); // '<'p
		assertContentType(HTMLSourceConfiguration.DEFAULT, source, 185, 20);
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 205, 3); // '<'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 208, 11); // ' 'Time
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 219, 2); // '?'
		assertContentType(HTMLSourceConfiguration.DEFAULT, source, 221, 1); // '.'
		assertContentType(HTMLSourceConfiguration.HTML_TAG_CLOSE, source, 222, 4);
		assertContentType(HTMLSourceConfiguration.DEFAULT, source, 226, 1); // '\n'
		assertContentType(HTMLSourceConfiguration.HTML_TAG_CLOSE, source, 227, 14);
	}

	public void testSplitTag()
	{
		String source = "<body onload='alert()' <?= Time.now ?> id='body'>"; //$NON-NLS-1$
		assertContentType(HTMLSourceConfiguration.HTML_TAG, source, 0, 23); // '<'body
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 23, 3); // '<'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 26, 10); // ' 'Time
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 36, 2); // '?'
		assertContentType(HTMLSourceConfiguration.HTML_TAG, source, 38, 11); // ' 'id
	}

	public void testSplitAttribute()
	{
		String source = "<body class=' <?= Time.now ?> ' id='body'>"; //$NON-NLS-1$
		assertContentType(HTMLSourceConfiguration.HTML_TAG, source, 0, 14); // '<'body
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 14, 3); // '<'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 17, 10); // ' 'Time
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 27, 2); // '?'
		assertContentType(HTMLSourceConfiguration.HTML_TAG, source, 29, 13); // %>' ''
	}

	public void testBetweenTags()
	{
		String source = "<body class=''><?= Time.now ?></div>"; //$NON-NLS-1$
		assertContentType(HTMLSourceConfiguration.HTML_TAG, source, 0, 15); // '<'body
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 15, 3); // '<'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 18, 10); // ' 'Time
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 28, 2); // '?'
		assertContentType(HTMLSourceConfiguration.HTML_TAG_CLOSE, source, 30, 6); // ?>'<'
	}

	public void testAPSTUD2755()
	{
		String source = "<li<?php ?>><a onclick=\"'<?php ?>'\"><?php ?></a></li>"; //$NON-NLS-1$
		assertContentType(HTMLSourceConfiguration.HTML_TAG, source, 0, 3); // '<'li
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 3, 5); // '<'?
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 8, 1); // ' '
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 9, 2); // '?'
		assertContentType(HTMLSourceConfiguration.HTML_TAG, source, 11, 14); // ?>'>'
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 25, 5); // '<'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 30, 1); // ' '
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 31, 2); // '?'
		assertContentType(HTMLSourceConfiguration.HTML_TAG, source, 33, 3); // ?>'''
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 36, 5); // '<'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 41, 1); // ' '
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 42, 2); // '?'
		assertContentType(HTMLSourceConfiguration.HTML_TAG_CLOSE, source, 44, 9); // '<'/a
	}

	public void testAPSTUD3387_DoubleQuotes()
	{
		String source = "<style type=\"text/css\">\n\"<?= Time.now ?>x\";\n</style>"; //$NON-NLS-1$
		assertContentType(HTMLSourceConfiguration.HTML_STYLE, source, 0, 23); // '<'style
		assertContentType(CSSSourceConfiguration.DEFAULT, source, 23, 1); // '\n'
		assertContentType(CSSSourceConfiguration.STRING_DOUBLE, source, 24, 1); // '''
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 25, 3); // '<'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 28, 10); // ' 'Time
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 38, 2); // '?'
		assertContentType(CSSSourceConfiguration.STRING_DOUBLE, source, 40, 2); // 'x'
		assertContentType(CSSSourceConfiguration.DEFAULT, source, 42, 2); // ';'
		assertContentType(HTMLSourceConfiguration.HTML_TAG_CLOSE, source, 44, 8); // '<'
	}

	public void testAPSTUD3387_SingleQuotes()
	{
		String source = "<style type=\"text/css\">\n'<?= Time.now ?>x';\n</style>"; //$NON-NLS-1$
		assertContentType(HTMLSourceConfiguration.HTML_STYLE, source, 0, 23); // '<'style
		assertContentType(CSSSourceConfiguration.DEFAULT, source, 23, 1); // '\n'
		assertContentType(CSSSourceConfiguration.STRING_SINGLE, source, 24, 1); // '''
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 25, 3); // '<'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 28, 10); // ' 'Time
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 38, 2); // '?'
		assertContentType(CSSSourceConfiguration.STRING_SINGLE, source, 40, 2); // 'x'
		assertContentType(CSSSourceConfiguration.DEFAULT, source, 42, 2); // ';'
		assertContentType(HTMLSourceConfiguration.HTML_TAG_CLOSE, source, 44); // '<'
	}

	public void testAfterTagBeforeSpace()
	{
		String source = "<body class=''><?= Time.now ?> </div>"; //$NON-NLS-1$
		assertContentType(HTMLSourceConfiguration.HTML_TAG, source, 0, 15); // '<'body
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 15, 3); // '<'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 18, 10); // ' 'Time
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 28, 2); // '?'
		assertContentType(HTMLSourceConfiguration.DEFAULT, source, 30); // ' '
		assertContentType(HTMLSourceConfiguration.HTML_TAG_CLOSE, source, 31, 6); // ?>'<'
	}

	public void testAfterSpaceBeforeTag()
	{
		String source = "<body class=''> <?= Time.now ?></div>"; //$NON-NLS-1$
		assertContentType(HTMLSourceConfiguration.HTML_TAG, source, 0, 15); // '<'body
		assertContentType(HTMLSourceConfiguration.DEFAULT, source, 15, 1); // ' '
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 16, 3); // '<'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 19, 10); // ' 'Time
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 29, 2); // '?'
		assertContentType(HTMLSourceConfiguration.HTML_TAG_CLOSE, source, 31, 6); // ?>'<'
	}

	public void testHTMLAttributeDoubleSplit()
	{
		String source = "<meta http-equiv=\"Refresh\" content=\"<?php echo $pause?>;url=<?php echo $url?>\"/>"; //$NON-NLS-1$
		assertContentType(HTMLSourceConfiguration.HTML_TAG, source, 0, 36); // '<'meta
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 36, 5); // '<'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 41, 12); // ' 'echo
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 53, 2); // '?'
		assertContentType(HTMLSourceConfiguration.HTML_TAG, source, 55, 5); // ?>';'
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 60, 5); // '<'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 65, 10); // ' 'echo
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 75, 2); // '?'
		assertContentType(HTMLSourceConfiguration.HTML_TAG, source, 77, 3); // ?>'"'
	}

	public void testJSStringSplit()
	{
		String source = "<script>var i=\"x<?= Time.now ?>y\";</script>"; //$NON-NLS-1$
		assertContentType(HTMLSourceConfiguration.HTML_SCRIPT, source, 0, 7); // '<'script>
		assertContentType(JSSourceConfiguration.DEFAULT, source, 8, 6); // 'v'
		assertContentType(JSSourceConfiguration.STRING_DOUBLE, source, 14, 2); // '"'
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 16, 3); // '<'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 19, 10); // ' 'Time
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 29, 2); // '?'
		assertContentType(JSSourceConfiguration.STRING_DOUBLE, source, 31, 2); // ?>'y'
		assertContentType(JSSourceConfiguration.DEFAULT, source, 33); // ';'
		assertContentType(HTMLSourceConfiguration.HTML_TAG_CLOSE, source, 34, 8); // '<'/script>
	}

	public void testPHPDoc()
	{
		String source = "<?php\n" + "/**\n" + " * This is a PHPDoc partition.\n" + " **/\n" + "?>";
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 0, 5); // '<?'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 5, 1);
		assertContentType(PHPSourceConfiguration.PHP_DOC_COMMENT, source, 6, 39); // /** **/
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 45, 1);
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 46, 2); // '?>'
	}

	public void testMultilinePHPComment()
	{
		String source = "<?php\n" + "/*\n" + " * This is not a PHPDoc partition.\n" + " */\n" + "?>";
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 0, 5); // '<?php'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 5, 1);
		assertContentType(PHPSourceConfiguration.PHP_MULTI_LINE_COMMENT, source, 6, 41); // /**/
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 47, 1);
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 48, 2); // '?>'
	}

	public void testHashSingleLinePHPComment()
	{
		String source = "<?php\n" + "# This is a singleline comment.\n" + "?>";
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 0, 5); // '<?'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 5, 1); // '\n'
		assertContentType(PHPSourceConfiguration.PHP_HASH_LINE_COMMENT, source, 6, 32); // #
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 38, 2); // '?>'
	}

	public void testSlashSingleLinePHPComment()
	{
		String source = "<?php\n" + "// This is a singleline comment.\n" + "?>";
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 0, 5); // '<?'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 5, 1); // '\n'
		assertContentType(PHPSourceConfiguration.PHP_SLASH_LINE_COMMENT, source, 6, 33); // /
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 39, 2); // '?>'
	}

	public void testSingleQuotedString()
	{
		String source = "<?php\n" + "'This is a single quoted string.'\n" + "?>";
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 0, 5); // '<'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 5, 1); // '\n'
		assertContentType(PHPSourceConfiguration.PHP_STRING_SINGLE, source, 6, 33); // ''
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 39, 1); // '\n'
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 40, 2); // '?'
	}

	public void testDoubleQuotedString()
	{
		String source = "<?php\n" + "\"This is a double quoted string.\"\n" + "?>";
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 0, 5); // '<?'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 5, 1); // '\n'
		assertContentType(PHPSourceConfiguration.PHP_STRING_DOUBLE, source, 6, 33); // ""
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 39, 1); // '\n'
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 40, 2); // '?>'
	}

	public void testDoubleQuotedStringWithSimpleVariable1()
	{
		String source = "<?php\n" + "\"Variable = $a\"\n" + "?>";
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 0, 5); // '<?'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 5, 1); // '\n'
		assertContentType(PHPSourceConfiguration.PHP_STRING_DOUBLE, source, 6, 15); // ""
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 21, 1); // '\n'
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 22, 2); // '?>'
	}

	public void testDoubleQuotedStringWithSimpleVariable2()
	{
		String source = "<?php\n" + "\"Variable = $a['0']\"\n" + "?>";
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 0, 5); // '<?'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 5, 1); // '\n'
		assertContentType(PHPSourceConfiguration.PHP_STRING_DOUBLE, source, 6, 20); // ""
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 26, 1); // '\n'
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 27, 2); // '?>'
	}

	public void testDoubleQuotedStringWithSimpleVariable3()
	{
		String source = "<?php\n" + "\"Variable = ${a}\"\n" + "?>";
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 0, 5); // '<?'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 5, 1); // '\n'
		assertContentType(PHPSourceConfiguration.PHP_STRING_DOUBLE, source, 6, 17); // ""
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 23, 1); // '\n'
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 24, 2); // '?>'
	}

	public void testDoubleQuotedStringWithComplexVariable1()
	{
		String source = "<?php\n" + "\"Variable = {$a}\"\n" + "?>";
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 0, 5); // '<?'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 5, 1); // '\n'
		assertContentType(PHPSourceConfiguration.PHP_STRING_DOUBLE, source, 6, 17); // ""
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 23, 1); // '\n'
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 24, 2); // '?>'
	}

	public void testDoubleQuotedStringWithComplexVariable2()
	{
		String source = "<?php\n" + "\"Variable = {$a->b}\"\n" + "?>";
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 0, 5); // '<?'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 5, 1); // '\n'
		assertContentType(PHPSourceConfiguration.PHP_STRING_DOUBLE, source, 6, 20); // ""
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 26, 1); // '\n'
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 27, 2); // '?>'
	}

	public void testDoubleQuotedStringWithComplexVariable3()
	{
		String source = "<?php\n" + "\"Variable = {$a->b()}\"\n" + "?>";
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 0, 5); // '<?'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 5, 1); // '\n'
		assertContentType(PHPSourceConfiguration.PHP_STRING_DOUBLE, source, 6, 22); // ""
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 28, 1); // '\n'
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 29, 2); // '?>'
	}

	public void testDoubleQuotedStringWithComplexVariable4()
	{
		String source = "<?php\n" + "\"Variable = {$a['a']}\"\n" + "?>";
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 0, 5); // '<?'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 5, 1); // '\n'
		assertContentType(PHPSourceConfiguration.PHP_STRING_DOUBLE, source, 6, 22); // ""
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 28, 1); // '\n'
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 29, 2); // '?>'
	}

	public void testDoubleQuotedStringWithComplexVariableLiteral()
	{
		String source = "<?php\n" + "\"Variable = {${$a}}\"\n" + "?>";
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 0, 5); // '<?'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 5, 1); // '\n'
		assertContentType(PHPSourceConfiguration.PHP_STRING_DOUBLE, source, 6, 20); // ""
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 26, 1); // '\n'
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 27, 2); // '?>'
	}

	public void testDoubleQuotedStringWithComplexVariableLiteralWithQuotes()
	{
		String source = "<?php\n" + "\"Variable = {${\"a\"}}\"\n" + "?>";
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 0, 5); // '<?'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 5, 1); // '\n'
		assertContentType(PHPSourceConfiguration.PHP_STRING_DOUBLE, source, 6, 21); // ""
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 27, 1); // '\n'
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 28, 2); // '?>'
	}

	public void testDoubleQuotedStringWithComplexVariableWithQuotes()
	{
		String source = "<?php\n" + "\"Variable = {$a[\"a\"]}\"\n" + "?>";
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 0, 5); // '<?'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 5, 1); // '\n'
		assertContentType(PHPSourceConfiguration.PHP_STRING_DOUBLE, source, 6, 22); // ""
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 28, 1); // '\n'
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 29, 2); // '?>'
	}

	public void testHeredoc()
	{
		String source = "<?php\n" + "<<<EOT\nThis is a heredoc string.\nEOT\n" + "?>";
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 0, 5); // '<?'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 5, 1); // '\n'
		assertContentType(PHPSourceConfiguration.PHP_HEREDOC, source, 6, 36); // ""
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 42, 1); // '\n'
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 43, 2); // '?>'
	}

	public void testHeredocWithSemicolon()
	{
		String source = "<?php\n" + "<<<EOT\nThis is a heredoc string.\nEOT;\n" + "?>";
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 0, 5); // '<?'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 5, 1); // '\n'
		assertContentType(PHPSourceConfiguration.PHP_HEREDOC, source, 6, 36); // ""
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 42, 2); // '\n'
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 44, 2); // '?>'
	}

	public void testHeredocWithFakeEnding()
	{
		String source = "<?php\n" + "<<<EOT\nThis is a \nEOTheredoc string.\nEOT\n" + "?>";
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 0, 5); // '<?'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 5, 1); // '\n'
		assertContentType(PHPSourceConfiguration.PHP_HEREDOC, source, 6, 40); // ""
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 46, 1); // '\n'
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 47, 2); // '?>'
	}

	public void testHeredocWithVariable()
	{
		String source = "<?php\n" + "<<<EOT\nThis is a heredoc {$string}.\nEOT\n" + "?>";
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 0, 5); // '<?'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 5, 1); // '\n'
		assertContentType(PHPSourceConfiguration.PHP_HEREDOC, source, 6, 39); // ""
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 45, 1); // '\n'
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 46, 2); // '?>'
	}

	public void testNowdoc()
	{
		String source = "<?php\n" + "<<<'EOT'\nThis is  nowdoc string.\nEOT\n" + "?>";
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 0, 5); // '<?'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 5, 1); // '\n'
		assertContentType(PHPSourceConfiguration.PHP_NOWDOC, source, 6, 36); // ""
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 42, 1); // '\n'
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 43, 2); // '?>'
	}

	public void testNowdocWithSemicolon()
	{
		String source = "<?php\n" + "<<<'EOT'\nThis is  nowdoc string.\nEOT;\n" + "?>";
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 0, 5); // '<?'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 5, 1); // '\n'
		assertContentType(PHPSourceConfiguration.PHP_NOWDOC, source, 6, 36); // ""
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 42, 2); // '\n'
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 44, 2); // '?>'
	}

	public void testNowdocWithFakeEnding()
	{
		String source = "<?php\n" + "<<<'EOT'\nThis is  \nEOTnowdoc string.\nEOT\n" + "?>";
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 0, 5); // '<?'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 5, 1); // '\n'
		assertContentType(PHPSourceConfiguration.PHP_NOWDOC, source, 6, 40); // ""
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 46, 1); // '\n'
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 47, 2); // '?>'
	}

	public void testNowdocWithVariable()
	{
		String source = "<?php\n" + "<<<'EOT'\nThis is  nowdoc {$string}.\nEOT\n" + "?>";
		assertContentType(CompositePartitionScanner.START_SWITCH_TAG, source, 0, 5); // '<?'
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 5, 1); // '\n'
		assertContentType(PHPSourceConfiguration.PHP_NOWDOC, source, 6, 39); // ""
		assertContentType(PHPSourceConfiguration.DEFAULT, source, 45, 1); // '\n'
		assertContentType(CompositePartitionScanner.END_SWITCH_TAG, source, 46, 2); // '?>'
	}

	private void assertContentType(String contentType, String code, int offset)
	{
		assertEquals("Content type doesn't match expectations for: " + code.charAt(offset), contentType,
				getContentType(code, offset));
	}

	private void assertContentType(String contentType, String code, int offset, int length)
	{
		for (int i = 0; i < length; ++i)
		{
			assertContentType(contentType, code, offset + i);
		}
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
