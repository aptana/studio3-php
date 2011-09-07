/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.tests;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.VerticalRuler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPage;

import com.aptana.editor.common.CommonEditorPlugin;
import com.aptana.editor.common.ExtendedFastPartitioner;
import com.aptana.editor.common.IPartitioningConfiguration;
import com.aptana.editor.common.TextUtils;
import com.aptana.editor.common.scripting.IDocumentScopeManager;
import com.aptana.editor.common.text.rules.CompositePartitionScanner;
import com.aptana.editor.html.HTMLSourceConfiguration;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.internal.core.IPHPConstants;
import com.aptana.editor.php.internal.ui.editor.PHPPartitionerSwitchStrategy;
import com.aptana.editor.php.internal.ui.editor.PHPSourceConfiguration;
import com.aptana.editor.php.internal.ui.editor.PHPSourceViewerConfiguration;

@SuppressWarnings({ "nls", "restriction" })
public class PHPScopesTest extends TestCase
{

	private Map<Integer, ISourceViewer> fViewers;

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		fViewers = new HashMap<Integer, ISourceViewer>();
	}

	@Override
	protected void tearDown() throws Exception
	{
		try
		{
			for (ISourceViewer viewer : fViewers.values())
			{
				((TextViewer) viewer).getControl().dispose();
			}
		}
		finally
		{
			fViewers = null;
			super.tearDown();
		}
	}

	public void testPHPDoc()
	{
		String source = "<?php\n" + "/**\n" + " * This is a PHPDoc partition.\n" + " **/\n" + "?>";
		// PHP start switch
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php", source, 0); // '<'
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php", source, 1); // '?'
		// PHPDoc comment
		assertScope("text.html.basic source.php.embedded.block.html comment.block.documentation.phpdoc.php", source, 6); // /**
		assertScope("text.html.basic source.php.embedded.block.html comment.block.documentation.phpdoc.php", source, 17); //
		assertScope("text.html.basic source.php.embedded.block.html comment.block.documentation.phpdoc.php", source, 44); // **/
		// PHP end switch
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source, 46); // '?'
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source, 47); // '>'
	}

	// TODO Test task tags in PHPDoc comments

	public void testPHPDocTags()
	{
		String[] tags = new String[] { "@abstract", "@access", "@author", "@category", "@copyright", "@deprecated",
				"@example", "@final", "@filesource", "@global", "@ignore", "@internal", "@license", "@link", "@method",
				"@name", "@package", "@param", "@property", "@return", "@see", "@since", "@static", "@staticvar",
				"@subpackage", "@todo", "@tutorial", "@uses", "@var", "@version" };
		for (String tag : tags)
		{
			String source = "<?php\n" + "/**\n" + " * " + tag + "  a PHPDoc partition.\n" + " **/\n" + "?>";
			// PHP start switch
			assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php",
					source, 0); // '<'
			assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php",
					source, 1); // '?'
			// PHPDoc comment
			assertScope("text.html.basic source.php.embedded.block.html comment.block.documentation.phpdoc.php",
					source, 6); // /**
			// tags
			assertScope(
					"text.html.basic source.php.embedded.block.html comment.block.documentation.phpdoc.php keyword.other.phpdoc.php",
					source, 13); // @
			assertScope(
					"text.html.basic source.php.embedded.block.html comment.block.documentation.phpdoc.php keyword.other.phpdoc.php",
					source, 12 + tag.length()); // end of tag
			// comments
			assertScope("text.html.basic source.php.embedded.block.html comment.block.documentation.phpdoc.php",
					source,
					38 + tag.length()); // **/
			// PHP end switch
			assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source,
					40 + tag.length()); // '?'
			assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source,
					41 + tag.length()); // '>'
		}
	}

	public void testMultilinePHPComment()
	{
		String source = "<?php\n" + "/*\n" + " * This is not a PHPDoc partition.\n" + " */\n" + "?>";
		// PHP start switch
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php", source, 0); // '<'
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php", source, 1); // '?'
		// Multiline comment
		assertScope("text.html.basic source.php.embedded.block.html comment.block.php", source, 6); // /*
		assertScope("text.html.basic source.php.embedded.block.html comment.block.php", source, 17); //
		assertScope("text.html.basic source.php.embedded.block.html comment.block.php", source, 46); // */
		// PHP end switch
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source, 48); // '?'
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source, 49); // '>'
	}

	// TODO Test task tags in multiline comments

	public void testSingleLineHashComment()
	{
		String source = "<?php\n" + "# This is a singleline comment.\n" + "?>";
		// PHP start switch
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php", source, 0); // '<'
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php", source, 1); // '?'
		// Single line comment
		assertScope("text.html.basic source.php.embedded.block.html comment.line.number-sign.php", source, 6); // #
		assertScope("text.html.basic source.php.embedded.block.html comment.line.number-sign.php", source, 21); //
		assertScope("text.html.basic source.php.embedded.block.html comment.line.number-sign.php", source, 36); // .
		// PHP end switch
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source, 38); // '?'
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source, 39); // '>'
	}

	public void testSingleLineHashCommentTask()
	{
		String source = "<?php\n" + "# TODO is a singleline comment.\n" + "?>";
		// PHP start switch
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php", source, 0); // '<'
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php", source, 1); // '?'
		// Single line comment
		assertScope("text.html.basic source.php.embedded.block.html comment.line.number-sign.php", source, 6); // #
		// Task marker
		assertScope(
				"text.html.basic source.php.embedded.block.html comment.line.number-sign.php keyword.other.documentation.task",
				source, 8); // T
		assertScope(
				"text.html.basic source.php.embedded.block.html comment.line.number-sign.php keyword.other.documentation.task",
				source, 11); // O
		// Comment
		assertScope("text.html.basic source.php.embedded.block.html comment.line.number-sign.php", source, 21); //
		assertScope("text.html.basic source.php.embedded.block.html comment.line.number-sign.php", source, 36); // .
		// PHP end switch
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source, 38); // '?'
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source, 39); // '>'
	}

	public void testSingleLineSlashComment()
	{
		String source = "<?php\n" + "// This is a singleline comment.\n" + "?>";
		// PHP start switch
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php", source, 0); // '<'
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php", source, 1); // '?'
		// Single line comment
		assertScope("text.html.basic source.php.embedded.block.html comment.line.double-slash.php", source, 6); // /
		assertScope("text.html.basic source.php.embedded.block.html comment.line.double-slash.php", source, 7); // /
		assertScope("text.html.basic source.php.embedded.block.html comment.line.double-slash.php", source, 21); //
		assertScope("text.html.basic source.php.embedded.block.html comment.line.double-slash.php", source, 37); // .
		// PHP end switch
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source, 39); // '?'
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source, 40); // '>'
	}

	public void testSingleLineSlashCommentTask()
	{
		String source = "<?php\n" + "// TODO is a singleline comment.\n" + "?>";
		// PHP start switch
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php", source, 0); // '<'
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php", source, 1); // '?'
		// Single line comment
		assertScope("text.html.basic source.php.embedded.block.html comment.line.double-slash.php", source, 6); // /
		assertScope("text.html.basic source.php.embedded.block.html comment.line.double-slash.php", source, 7); // /
		// Task marker
		assertScope(
				"text.html.basic source.php.embedded.block.html comment.line.double-slash.php keyword.other.documentation.task",
				source, 9); // T
		assertScope(
				"text.html.basic source.php.embedded.block.html comment.line.double-slash.php keyword.other.documentation.task",
				source, 12); // O
		// Comment
		assertScope("text.html.basic source.php.embedded.block.html comment.line.double-slash.php", source, 21); //
		assertScope("text.html.basic source.php.embedded.block.html comment.line.double-slash.php", source, 37); // .
		// PHP end switch
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source, 39); // '?'
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source, 40); // '>'
	}

	public void testSingleQuotedString()
	{
		String source = "<?php\n" + "'This is a single quoted string.'\n" + "?>";
		// PHP start switch
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php", source, 0); // '<'
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php", source, 1); // '?'
		// Single quoted string
		assertScope(
				"text.html.basic source.php.embedded.block.html string.quoted.single.php punctuation.definition.string.begin.php",
				source, 6); // '
		assertScope(
				"text.html.basic source.php.embedded.block.html string.quoted.single.php meta.string-contents.quoted.single.php",
				source, 7); // T
		assertScope(
				"text.html.basic source.php.embedded.block.html string.quoted.single.php meta.string-contents.quoted.single.php",
				source, 20); //
		assertScope(
				"text.html.basic source.php.embedded.block.html string.quoted.single.php meta.string-contents.quoted.single.php",
				source, 37); // .
		assertScope(
				"text.html.basic source.php.embedded.block.html string.quoted.single.php punctuation.definition.string.end.php",
				source, 38); // '
		// PHP end switch
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source, 40); // '?'
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source, 41); // '>'
	}

	public void testDoubleQuotedString()
	{
		String source = "<?php\n" + "\"This is a double quoted string.\"\n" + "?>";
		// PHP start switch
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php", source, 0); // '<'
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php", source, 1); // '?'
		// Double quoted string
		assertScope(
				"text.html.basic source.php.embedded.block.html string.quoted.double.php punctuation.definition.string.begin.php",
				source, 6); // "
		assertScope(
				"text.html.basic source.php.embedded.block.html string.quoted.double.php meta.string-contents.quoted.double.php",
				source, 7); // T
		assertScope(
				"text.html.basic source.php.embedded.block.html string.quoted.double.php meta.string-contents.quoted.double.php",
				source, 20); //
		assertScope(
				"text.html.basic source.php.embedded.block.html string.quoted.double.php meta.string-contents.quoted.double.php",
				source, 37); // .
		assertScope(
				"text.html.basic source.php.embedded.block.html string.quoted.double.php punctuation.definition.string.end.php",
				source, 38); // "
		// PHP end switch
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source, 40); // '?'
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source, 41); // '>'
	}

	public void testDoubleQuotedStringEscapedOctal()
	{
		String source = "<?php\n" + "\"This is a double\\012quoted string.\"\n" + "?>";
		// PHP start switch
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php", source, 0); // '<'
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php", source, 1); // '?'
		// Double quoted string
		assertScope(
				"text.html.basic source.php.embedded.block.html string.quoted.double.php punctuation.definition.string.begin.php",
				source, 6); // "
		assertScope(
				"text.html.basic source.php.embedded.block.html string.quoted.double.php meta.string-contents.quoted.double.php",
				source, 7); // T
		assertScope(
				"text.html.basic source.php.embedded.block.html string.quoted.double.php meta.string-contents.quoted.double.php",
				source, 22); // e
		// Escaped octal
		assertScope(
				"text.html.basic source.php.embedded.block.html string.quoted.double.php meta.string-contents.quoted.double.php constant.character.escape.php",
				source, 23); // \
		assertScope(
				"text.html.basic source.php.embedded.block.html string.quoted.double.php meta.string-contents.quoted.double.php constant.character.escape.php",
				source, 24); // 0
		assertScope(
				"text.html.basic source.php.embedded.block.html string.quoted.double.php meta.string-contents.quoted.double.php constant.character.escape.php",
				source, 25); // 1
		assertScope(
				"text.html.basic source.php.embedded.block.html string.quoted.double.php meta.string-contents.quoted.double.php constant.character.escape.php",
				source, 26); // 2
		// String
		assertScope(
				"text.html.basic source.php.embedded.block.html string.quoted.double.php meta.string-contents.quoted.double.php",
				source, 27); // q
		assertScope(
				"text.html.basic source.php.embedded.block.html string.quoted.double.php meta.string-contents.quoted.double.php",
				source, 40); // .
		assertScope(
				"text.html.basic source.php.embedded.block.html string.quoted.double.php punctuation.definition.string.end.php",
				source, 41); // "
		// PHP end switch
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source, 43); // '?'
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source, 44); // '>'
	}

	public void testDoubleQuotedStringEscapedHex()
	{
		String source = "<?php\n" + "\"This is a double\\xa9quoted string.\"\n" + "?>";
		// PHP start switch
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php", source, 0); // '<'
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php", source, 1); // '?'
		// Double quoted string
		assertScope(
				"text.html.basic source.php.embedded.block.html string.quoted.double.php punctuation.definition.string.begin.php",
				source, 6); // "
		assertScope(
				"text.html.basic source.php.embedded.block.html string.quoted.double.php meta.string-contents.quoted.double.php",
				source, 7); // T
		assertScope(
				"text.html.basic source.php.embedded.block.html string.quoted.double.php meta.string-contents.quoted.double.php",
				source, 22); // e
		// Escaped hex
		assertScope(
				"text.html.basic source.php.embedded.block.html string.quoted.double.php meta.string-contents.quoted.double.php constant.character.escape.php",
				source, 23); // \
		assertScope(
				"text.html.basic source.php.embedded.block.html string.quoted.double.php meta.string-contents.quoted.double.php constant.character.escape.php",
				source, 24); // x
		assertScope(
				"text.html.basic source.php.embedded.block.html string.quoted.double.php meta.string-contents.quoted.double.php constant.character.escape.php",
				source, 25); // a
		assertScope(
				"text.html.basic source.php.embedded.block.html string.quoted.double.php meta.string-contents.quoted.double.php constant.character.escape.php",
				source, 26); // 9
		// String
		assertScope(
				"text.html.basic source.php.embedded.block.html string.quoted.double.php meta.string-contents.quoted.double.php",
				source, 27); // q
		assertScope(
				"text.html.basic source.php.embedded.block.html string.quoted.double.php meta.string-contents.quoted.double.php",
				source, 40); // .
		assertScope(
				"text.html.basic source.php.embedded.block.html string.quoted.double.php punctuation.definition.string.end.php",
				source, 41); // "
		// PHP end switch
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source, 43); // '?'
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source, 44); // '>'
	}

	public void testDoubleQuotedStringEscapedCharacters()
	{
		String[] escaped = new String[] { "\\n", "\\r", "\\t", "\\v", "\\f", "\\\\", "\\$", "\\\"" };
		for (String escape : escaped)
		{
			String source = "<?php\n" + "\"This is a double" + escape + "quoted string.\"\n" + "?>";
			// PHP start switch
			assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php",
					source, 0); // '<'
			assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php",
					source, 1); // '?'
			// Double quoted string
			assertScope(
					"text.html.basic source.php.embedded.block.html string.quoted.double.php punctuation.definition.string.begin.php",
					source, 6); // "
			assertScope(
					"text.html.basic source.php.embedded.block.html string.quoted.double.php meta.string-contents.quoted.double.php",
					source, 7); // T
			assertScope(
					"text.html.basic source.php.embedded.block.html string.quoted.double.php meta.string-contents.quoted.double.php",
					source, 22); // e
			// Escaped character
			assertScope(
					"text.html.basic source.php.embedded.block.html string.quoted.double.php meta.string-contents.quoted.double.php constant.character.escape.php",
					source, 23); // \
			assertScope(
					"text.html.basic source.php.embedded.block.html string.quoted.double.php meta.string-contents.quoted.double.php constant.character.escape.php",
					source, 24); // r
			// String
			assertScope(
					"text.html.basic source.php.embedded.block.html string.quoted.double.php meta.string-contents.quoted.double.php",
					source, 25); // q
			assertScope(
					"text.html.basic source.php.embedded.block.html string.quoted.double.php meta.string-contents.quoted.double.php",
					source, 38); // .
			assertScope(
					"text.html.basic source.php.embedded.block.html string.quoted.double.php punctuation.definition.string.end.php",
					source, 39); // "
			// PHP end switch
			assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source,
					41); // '?'
			assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source,
					42); // '>'
		}
	}

	// TODO Add a test for a heredoc partition
	// TODO Add a test for a nowdoc partition

	public void testFunctionDeclaration()
	{
		String source = "<?php\n" + "function example() {}\n" + "?>";
		// PHP start switch
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php", source, 0); // '<'
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php", source, 1); // '?'
		// function keyword
		assertScope("text.html.basic source.php.embedded.block.html meta.function.php storage.type.function.php",
				source, 6); // f
		assertScope("text.html.basic source.php.embedded.block.html meta.function.php storage.type.function.php",
				source, 13); // n

		// space
		// assertScope("text.html.basic source.php.embedded.block.html meta.function.php", source, 14);

		// function name
		assertScope("text.html.basic source.php.embedded.block.html meta.function.php entity.name.function.php",
				source, 15); // e
		assertScope("text.html.basic source.php.embedded.block.html meta.function.php entity.name.function.php",
				source, 21); // e

		// parens
		// assertScope("text.html.basic source.php.embedded.block.html meta.function.php punctuation.definition.parameters.begin.php",
		// source, 22); // (

		// space

		// curlies
		assertScope("text.html.basic source.php.embedded.block.html", source, 25); // {
		assertScope("text.html.basic source.php.embedded.block.html", source, 26); // }

		// newline

		// PHP end switch
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source, 28); // '?'
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source, 29); // '>'
	}

	public void testClassDeclaration()
	{
		String source = "<?php\n" + "class SimpleClass {}\n" + "?>";
		// PHP start switch
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php", source, 0); // '<'
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php", source, 1); // '?'
		// class keyword
		assertScope("text.html.basic source.php.embedded.block.html storage.type.class.php", source, 6); // c
		assertScope("text.html.basic source.php.embedded.block.html storage.type.class.php", source, 10); // s
		// space
		// assertScope("text.html.basic source.php.embedded.block.html", source, 11);
		// class name
		assertScope("text.html.basic source.php.embedded.block.html entity.name.type.class.php", source, 12); // S
		assertScope("text.html.basic source.php.embedded.block.html entity.name.type.class.php", source, 22); // s
		// space
		// assertScope("text.html.basic source.php.embedded.block.html", source, 23);
		// curlies
		assertScope("text.html.basic source.php.embedded.block.html", source, 24); // {
		assertScope("text.html.basic source.php.embedded.block.html", source, 25); // }
		// newline
		// PHP end switch
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source, 27); // '?'
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source, 28); // '>'
	}

	public void testAPSTUD3187()
	{
		String source = "<?= $this->layout()->sidenav ?>";
		// PHP start switch
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php", source, 0); // '<'
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php", source, 1); // '?'

		// $this variable
		assertScope("text.html.basic source.php.embedded.block.html variable.language.php", source, 7); // 'i'

		// '->'
		assertScope("text.html.basic source.php.embedded.block.html keyword.operator.class.php", source, 9); // '-'
		assertScope("text.html.basic source.php.embedded.block.html keyword.operator.class.php", source, 10); // '>'

		// 'layout()'
		assertScope("text.html.basic source.php.embedded.block.html meta.function-call.object.php", source, 14); // 'o'

		// '->'
		assertScope("text.html.basic source.php.embedded.block.html keyword.operator.class.php", source, 19); // -
		assertScope("text.html.basic source.php.embedded.block.html keyword.operator.class.php", source, 20); // >

		// sidenav
		assertScope("text.html.basic source.php.embedded.block.html variable.other.property.php", source, 21); // s
		assertScope("text.html.basic source.php.embedded.block.html variable.other.property.php", source, 27); // v
	}

	public void testAPSTUD2790()
	{
		String source = "<?php\n" + //
				"function check_login() {\n" + //
				"	$info = $this->input->post();\n" + //
				"	$this->form_validation->set_rules();\n" + //
				"	if($user[0]->activated == 0) {\n" + //
				"	}\n" + //
				"}\n" + //
				"?>";
		// PHP start switch
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php", source, 0); // '<'
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php", source, 1); // '?'
		// function keyword
		assertScope("text.html.basic source.php.embedded.block.html meta.function.php storage.type.function.php",
				source, 6); // f
		assertScope("text.html.basic source.php.embedded.block.html meta.function.php storage.type.function.php",
				source, 13); // n
		// space
		// assertScope("text.html.basic source.php.embedded.block.html meta.function.php", source, 14);
		// function name
		assertScope("text.html.basic source.php.embedded.block.html meta.function.php entity.name.function.php",
				source, 15); // c
		assertScope("text.html.basic source.php.embedded.block.html meta.function.php entity.name.function.php",
				source, 25); // n
		// parens
		assertScope(
				"text.html.basic source.php.embedded.block.html meta.function.php punctuation.definition.parameters.begin.php",
				source, 26); // (
		assertScope(
				"text.html.basic source.php.embedded.block.html meta.function.php punctuation.definition.parameters.end.php",
				source, 27); // )
		// space
		// assertScope("text.html.basic source.php.embedded.block.html", source, 28);
		// curly
		assertScope("text.html.basic source.php.embedded.block.html", source, 29); // {
		// newline
		// $info
		// FIXME We don't separate out the dollar sign as a unique scope yet! Probably can do this in PHPCodeScanner if
		// we break the token up and push the pieces into queue!
		// assertScope("text.html.basic source.php.embedded.block.html variable.other.php punctuation.definition.variable.php",
		// source, 32); // $
		assertScope("text.html.basic source.php.embedded.block.html variable.other.php", source, 33); // i
		assertScope("text.html.basic source.php.embedded.block.html variable.other.php", source, 36); // o
		// space
		// assertScope("text.html.basic source.php.embedded.block.html", source, 37);
		// =
		assertScope("text.html.basic source.php.embedded.block.html keyword.operator.assignment.php", source, 38); // =
		// space
		// assertScope("text.html.basic source.php.embedded.block.html", source, 39);
		// $this
		// NOTE: We use variable.language.php while Textmate uses variable.other.php here.
		// assertScope("text.html.basic source.php.embedded.block.html variable.language.php punctuation.definition.variable.php",
		// source, 40); // $
		assertScope("text.html.basic source.php.embedded.block.html variable.language.php", source, 41); // t
		assertScope("text.html.basic source.php.embedded.block.html variable.language.php", source, 44); // s
		// ->
		assertScope("text.html.basic source.php.embedded.block.html keyword.operator.class.php", source, 45);
		assertScope("text.html.basic source.php.embedded.block.html keyword.operator.class.php", source, 46);
		// input
		assertScope("text.html.basic source.php.embedded.block.html variable.other.property.php", source, 47); // i
		assertScope("text.html.basic source.php.embedded.block.html variable.other.property.php", source, 51); // t
		// ->
		assertScope("text.html.basic source.php.embedded.block.html keyword.operator.class.php", source, 52);
		assertScope("text.html.basic source.php.embedded.block.html keyword.operator.class.php", source, 53);
		// post
		assertScope("text.html.basic source.php.embedded.block.html meta.function-call.object.php", source, 54); // p
		assertScope("text.html.basic source.php.embedded.block.html meta.function-call.object.php", source, 57); // t
		// ()
		// ;
		assertScope("text.html.basic source.php.embedded.block.html punctuation.terminator.expression.php", source, 60); // ;

		// ...

		// $user
		// assertScope(
		// "text.html.basic source.php.embedded.block.html variable.language.php punctuation.definition.variable.php",
		// source, 104); // $
		assertScope("text.html.basic source.php.embedded.block.html variable.other.php", source, 105); // u
		assertScope("text.html.basic source.php.embedded.block.html variable.other.php", source, 108); // r
		// [
		assertScope(
				"text.html.basic source.php.embedded.block.html variable.other.php keyword.operator.index-start.php",
				source, 109); // [
		// 0
		assertScope("text.html.basic source.php.embedded.block.html variable.other.php constant.numeric.php", source,
				110); // 0
		// ]
		assertScope("text.html.basic source.php.embedded.block.html variable.other.php keyword.operator.index-end.php",
				source, 111); // ]

		// ...

		// PHP end switch
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source,
				source.length() - 2); // '?'
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source,
				source.length() - 1); // '>'
	}

	public void testAPSTUD3207()
	{
		// @formatter:off
		String source = "<?php\n" +
				"define (\"valid_username\",\"Valid username\");\n" + 
				"echo valid_username; ?>";
		// @formatter:on

		// PHP start switch
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php", source, 0); // '<'
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php", source, 1); // '?'

		// define
		assertScope("text.html.basic source.php.embedded.block.html support.function.builtin_functions.php", source, 6);
		assertScope("text.html.basic source.php.embedded.block.html support.function.builtin_functions.php", source, 11);

		// assertScope("text.html.basic source.php.embedded.block.html", source, 12); //
		assertScope("text.html.basic source.php.embedded.block.html", source, 13); // (
		// FIXME We don't have the punctuation and meta scopes for strings proper!
		// assertScope(
		// "text.html.basic source.php.embedded.block.html string.quoted.double.php punctuation.definition.string.begin.php",
		// source, 14); // "
		// assertScope(
		// "text.html.basic source.php.embedded.block.html string.quoted.double.php meta.string-contents.quoted.double.php",
		// source, 15); // v
		// assertScope(
		// "text.html.basic source.php.embedded.block.html string.quoted.double.php meta.string-contents.quoted.double.php",
		// source, 28); // e
		// assertScope(
		// "text.html.basic source.php.embedded.block.html string.quoted.double.php punctuation.definition.string.begin.php",
		// source, 29); // "
		assertScope("text.html.basic source.php.embedded.block.html", source, 30); // ,
		// FIXME We don't have the punctuation and meta scopes for strings proper!
		// assertScope(
		// "text.html.basic source.php.embedded.block.html string.quoted.double.php punctuation.definition.string.begin.php",
		// source, 31); // "
		// assertScope(
		// "text.html.basic source.php.embedded.block.html string.quoted.double.php meta.string-contents.quoted.double.php",
		// source, 32); // V
		// assertScope(
		// "text.html.basic source.php.embedded.block.html string.quoted.double.php meta.string-contents.quoted.double.php",
		// source, 45); // e
		// assertScope(
		// "text.html.basic source.php.embedded.block.html string.quoted.double.php punctuation.definition.string.begin.php",
		// source, 46); // "

		// echo
		assertScope("text.html.basic source.php.embedded.block.html support.function.construct.php", source, 50); // e
		assertScope("text.html.basic source.php.embedded.block.html support.function.construct.php", source, 53); // o

		// assertScope("text.html.basic source.php.embedded.block.html", source, 54); //

		// valid_username
		assertScope("text.html.basic source.php.embedded.block.html constant.other.php", source, 55); // v
		assertScope("text.html.basic source.php.embedded.block.html constant.other.php", source, 68); // e

		assertScope("text.html.basic source.php.embedded.block.html punctuation.terminator.expression.php", source, 69); // ;
		// assertScope("text.html.basic source.php.embedded.block.html", source, 70); //

		// PHP end switch
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source, 71); // '?'
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source, 72); // '>'
	}

	public void testAPSTUD3378()
	{
		// @formatter:off
		String source = "<div>\n" +
				"  <p><?= \"No content found\" ?></p>\n" +
				"</div>";
		// @formatter:on

		// <div>
		assertScope("text.html.basic meta.tag.block.any.html punctuation.definition.tag.begin.html", source, 0); // <
		assertScope("text.html.basic meta.tag.block.any.html entity.name.tag.block.any.html", source, 1); // d
		assertScope("text.html.basic meta.tag.block.any.html punctuation.definition.tag.end.html", source, 4); // >

		// <p>
		assertScope("text.html.basic meta.tag.block.any.html punctuation.definition.tag.begin.html", source, 8); // <
		assertScope("text.html.basic meta.tag.block.any.html entity.name.tag.block.any.html", source, 9); // p
		assertScope("text.html.basic meta.tag.block.any.html punctuation.definition.tag.end.html", source, 10); // >

		// <?= "No content found" ?>
		// FIXME These are all saying block.html, but Textmate uses line.html for single liner php code
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php", source, 11); // <
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php", source, 12); // ?
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.begin.php", source, 13); // =
		assertScope("text.html.basic source.php.embedded.block.html", source, 14); //
		// FIXME we don't offer up string quote punctuation scopes, but should!
		assertScope(
				"text.html.basic source.php.embedded.block.html string.quoted.double.php punctuation.definition.string.begin.php",
				source, 15); // "
		assertScope(
				"text.html.basic source.php.embedded.block.html string.quoted.double.php meta.string-contents.quoted.double.php",
				source, 16); // N
		assertScope(
				"text.html.basic source.php.embedded.block.html string.quoted.double.php punctuation.definition.string.end.php",
				source, 32); // "
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source, 34); // ?
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source, 35); // >

		// </p>
		assertScope("text.html.basic meta.tag.block.any.html punctuation.definition.tag.begin.html", source, 36); // <
		assertScope("text.html.basic meta.tag.block.any.html punctuation.definition.tag.begin.html", source, 37); // /
		assertScope("text.html.basic meta.tag.block.any.html entity.name.tag.block.any.html", source, 38); // p
		assertScope("text.html.basic meta.tag.block.any.html punctuation.definition.tag.end.html", source, 39); // >
	}

	// comment = 'In PHP, any identifier which is not a variable is taken to be a constant.
	// However, if there is no constant defined with the given name then a notice
	// is generated and the constant is assumed to have the value of its name.';
	// match = '[a-zA-Z_\x{7f}-\x{ff}][a-zA-Z0-9_\x{7f}-\x{ff}]*';

	// TODO Add test for all the new tokens added in PHPTokenMapperFactory

	private void assertScope(String scope, String code, int offset)
	{
		try
		{
			assertEquals("Scope doesn't match expectations for: " + code.charAt(offset), scope, getScope(code, offset));
		}
		catch (BadLocationException e)
		{
			fail(e.getMessage());
		}
	}

	private String getScope(String content, int offset) throws BadLocationException
	{
		ISourceViewer viewer = fViewers.get(content.hashCode());
		if (viewer == null)
		{
			Control parent = ((WorkbenchPage) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage())
					.getEditorPresentation().getLayoutPart().getControl();
			viewer = new SourceViewer((Composite) parent, new VerticalRuler(5), SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI
					| SWT.BORDER | SWT.FULL_SELECTION);
			viewer.configure(new PHPSourceViewerConfiguration(PHPEditorPlugin.getDefault().getPreferenceStore(), null));

			IDocument document = new Document(content);
			viewer.setDocument(document);

			CompositePartitionScanner partitionScanner = new CompositePartitionScanner(HTMLSourceConfiguration
					.getDefault().createSubPartitionScanner(), PHPSourceConfiguration.getDefault()
					.createSubPartitionScanner(), PHPPartitionerSwitchStrategy.getDefault());
			ExtendedFastPartitioner partitioner = new ExtendedFastPartitioner(partitionScanner,
					TextUtils.combine(new String[][] { CompositePartitionScanner.SWITCHING_CONTENT_TYPES,
							HTMLSourceConfiguration.getDefault().getContentTypes(),
							PHPSourceConfiguration.getDefault().getContentTypes() }));
			partitionScanner.setPartitioner(partitioner);
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
			// order of these next two statements is important!
			getDocumentScopeManager().setDocumentScope(document, IPHPConstants.CONTENT_TYPE_HTML_PHP, "");
			getDocumentScopeManager().registerConfigurations(
					document,
					new IPartitioningConfiguration[] { HTMLSourceConfiguration.getDefault(),
							PHPSourceConfiguration.getDefault() });

			fViewers.put(content.hashCode(), viewer);
		}
		return getDocumentScopeManager().getScopeAtOffset(viewer, offset);
	}

	protected IDocumentScopeManager getDocumentScopeManager()
	{
		return CommonEditorPlugin.getDefault().getDocumentScopeManager();
	}
}
