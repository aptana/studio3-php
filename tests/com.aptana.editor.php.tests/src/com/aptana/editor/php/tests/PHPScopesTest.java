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
		assertScope("source.php source.php.embedded.block.html comment.block.documentation.phpdoc.php", source, 6); // /**
		assertScope("source.php source.php.embedded.block.html comment.block.documentation.phpdoc.php", source, 17); //
		assertScope("source.php source.php.embedded.block.html comment.block.documentation.phpdoc.php", source, 44); // **/
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
			assertScope("source.php source.php.embedded.block.html comment.block.documentation.phpdoc.php",
					source, 6); // /**
			// tags
			assertScope(
					"source.php source.php.embedded.block.html comment.block.documentation.phpdoc.php keyword.other.phpdoc.php",
					source, 13); // @
			assertScope(
					"source.php source.php.embedded.block.html comment.block.documentation.phpdoc.php keyword.other.phpdoc.php",
					source, 12 + tag.length()); // end of tag
			// comments
			assertScope("source.php source.php.embedded.block.html comment.block.documentation.phpdoc.php",
					source, 38 + tag.length()); // **/
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
		assertScope("source.php source.php.embedded.block.html comment.block.php", source, 6); // /*
		assertScope("source.php source.php.embedded.block.html comment.block.php", source, 17); //
		assertScope("source.php source.php.embedded.block.html comment.block.php", source, 46); // */
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
		assertScope("source.php source.php.embedded.block.html comment.line.number-sign.php", source, 6); // #
		assertScope("source.php source.php.embedded.block.html comment.line.number-sign.php", source, 21); //
		assertScope("source.php source.php.embedded.block.html comment.line.number-sign.php", source, 36); // .
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
		assertScope("source.php source.php.embedded.block.html comment.line.number-sign.php", source, 6); // #
		// Task marker
		assertScope(
				"source.php source.php.embedded.block.html comment.line.number-sign.php keyword.other.documentation.task",
				source, 8); // T
		assertScope(
				"source.php source.php.embedded.block.html comment.line.number-sign.php keyword.other.documentation.task",
				source, 11); // O
		// Comment
		assertScope("source.php source.php.embedded.block.html comment.line.number-sign.php", source, 21); //
		assertScope("source.php source.php.embedded.block.html comment.line.number-sign.php", source, 36); // .
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
		assertScope("source.php source.php.embedded.block.html comment.line.double-slash.php", source, 6); // /
		assertScope("source.php source.php.embedded.block.html comment.line.double-slash.php", source, 7); // /
		assertScope("source.php source.php.embedded.block.html comment.line.double-slash.php", source, 21); //
		assertScope("source.php source.php.embedded.block.html comment.line.double-slash.php", source, 37); // .
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
		assertScope("source.php source.php.embedded.block.html comment.line.double-slash.php", source, 6); // /
		assertScope("source.php source.php.embedded.block.html comment.line.double-slash.php", source, 7); // /
		// Task marker
		assertScope(
				"source.php source.php.embedded.block.html comment.line.double-slash.php keyword.other.documentation.task",
				source, 9); // T
		assertScope(
				"source.php source.php.embedded.block.html comment.line.double-slash.php keyword.other.documentation.task",
				source, 12); // O
		// Comment
		assertScope("source.php source.php.embedded.block.html comment.line.double-slash.php", source, 21); //
		assertScope("source.php source.php.embedded.block.html comment.line.double-slash.php", source, 37); // .
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
		assertScope("source.php source.php.embedded.block.html string.quoted.single.php", source, 6); // '
		assertScope("source.php source.php.embedded.block.html string.quoted.single.php", source, 7); // T
		assertScope("source.php source.php.embedded.block.html string.quoted.single.php", source, 20); //
		assertScope("source.php source.php.embedded.block.html string.quoted.single.php", source, 37); // .
		assertScope("source.php source.php.embedded.block.html string.quoted.single.php", source, 38); // '
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
		assertScope("source.php source.php.embedded.block.html string.quoted.double.php", source, 6); // "
		assertScope("source.php source.php.embedded.block.html string.quoted.double.php", source, 7); // T
		assertScope("source.php source.php.embedded.block.html string.quoted.double.php", source, 20); //
		assertScope("source.php source.php.embedded.block.html string.quoted.double.php", source, 37); // .
		assertScope("source.php source.php.embedded.block.html string.quoted.double.php", source, 38); // "
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
		assertScope("source.php source.php.embedded.block.html string.quoted.double.php", source, 6); // "
		assertScope("source.php source.php.embedded.block.html string.quoted.double.php", source, 7); // T
		assertScope("source.php source.php.embedded.block.html string.quoted.double.php", source, 22); // e
		// Escaped octal
		assertScope(
				"source.php source.php.embedded.block.html string.quoted.double.php constant.character.escape.php",
				source, 23); // \
		assertScope(
				"source.php source.php.embedded.block.html string.quoted.double.php constant.character.escape.php",
				source, 24); // 0
		assertScope(
				"source.php source.php.embedded.block.html string.quoted.double.php constant.character.escape.php",
				source, 25); // 1
		assertScope(
				"source.php source.php.embedded.block.html string.quoted.double.php constant.character.escape.php",
				source, 26); // 2
		// String
		assertScope("source.php source.php.embedded.block.html string.quoted.double.php", source, 27); // q
		assertScope("source.php source.php.embedded.block.html string.quoted.double.php", source, 40); // .
		assertScope("source.php source.php.embedded.block.html string.quoted.double.php", source, 41); // "
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
		assertScope("source.php source.php.embedded.block.html string.quoted.double.php", source, 6); // "
		assertScope("source.php source.php.embedded.block.html string.quoted.double.php", source, 7); // T
		assertScope("source.php source.php.embedded.block.html string.quoted.double.php", source, 22); // e
		// Escaped hex
		assertScope(
				"source.php source.php.embedded.block.html string.quoted.double.php constant.character.escape.php",
				source, 23); // \
		assertScope(
				"source.php source.php.embedded.block.html string.quoted.double.php constant.character.escape.php",
				source, 24); // x
		assertScope(
				"source.php source.php.embedded.block.html string.quoted.double.php constant.character.escape.php",
				source, 25); // a
		assertScope(
				"source.php source.php.embedded.block.html string.quoted.double.php constant.character.escape.php",
				source, 26); // 9
		// String
		assertScope("source.php source.php.embedded.block.html string.quoted.double.php", source, 27); // q
		assertScope("source.php source.php.embedded.block.html string.quoted.double.php", source, 40); // .
		assertScope("source.php source.php.embedded.block.html string.quoted.double.php", source, 41); // "
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
			assertScope("source.php source.php.embedded.block.html string.quoted.double.php", source, 6); // "
			assertScope("source.php source.php.embedded.block.html string.quoted.double.php", source, 7); // T
			assertScope("source.php source.php.embedded.block.html string.quoted.double.php", source, 22); // e
			// Escaped character
			assertScope(
					"source.php source.php.embedded.block.html string.quoted.double.php constant.character.escape.php",
					source, 23); // \
			assertScope(
					"source.php source.php.embedded.block.html string.quoted.double.php constant.character.escape.php",
					source, 24); // r
			// String
			assertScope("source.php source.php.embedded.block.html string.quoted.double.php", source, 25); // q
			assertScope("source.php source.php.embedded.block.html string.quoted.double.php", source, 38); // .
			assertScope("source.php source.php.embedded.block.html string.quoted.double.php", source, 39); // "
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
		assertScope("source.php source.php.embedded.block.html storage.type.function.php", source, 6); // f
		assertScope("source.php source.php.embedded.block.html storage.type.function.php", source, 13); // n

		// space
		// assertScope("source.php source.php.embedded.block.html", source, 14);

		// function name
		assertScope("source.php source.php.embedded.block.html entity.name.function.php", source, 15); // e
		assertScope("source.php source.php.embedded.block.html entity.name.function.php", source, 21); // e

		// parens
		// assertScope("source.php source.php.embedded.block.html punctuation.definition.parameters.begin.php",
		// source, 22); // (

		// space

		// curlies
		assertScope("source.php source.php.embedded.block.html", source, 25); // {
		assertScope("source.php source.php.embedded.block.html", source, 26); // }

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
		assertScope("source.php source.php.embedded.block.html storage.type.class.php", source, 6); // c
		assertScope("source.php source.php.embedded.block.html storage.type.class.php", source, 10); // s
		// space
		// assertScope("source.php source.php.embedded.block.html", source, 11);
		// class name
		assertScope("source.php source.php.embedded.block.html entity.name.type.class.php", source, 12); // S
		assertScope("source.php source.php.embedded.block.html entity.name.type.class.php", source, 22); // s
		// space
		// assertScope("source.php source.php.embedded.block.html", source, 23);
		// curlies
		assertScope("source.php source.php.embedded.block.html", source, 24); // {
		assertScope("source.php source.php.embedded.block.html", source, 25); // }
		// newline
		// PHP end switch
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source, 27); // '?'
		assertScope("text.html.basic source.php.embedded.block.html punctuation.section.embedded.end.php", source, 28); // '>'
	}

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
