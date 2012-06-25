/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.ui.editor;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.projection.ProjectionAnnotation;
import org2.eclipse.php.internal.ui.preferences.PreferenceConstants;

import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.editor.common.text.AbstractFoldingComputer;
import com.aptana.editor.html.HTMLFoldingComputer;
import com.aptana.editor.php.epl.PHPEplPlugin;
import com.aptana.editor.php.internal.core.IPHPConstants;
import com.aptana.editor.php.internal.parser.PHPParseRootNode;
import com.aptana.editor.php.internal.parser.nodes.PHPBaseParseNode;
import com.aptana.editor.php.internal.parser.nodes.PHPClassParseNode;
import com.aptana.editor.php.internal.parser.nodes.PHPCommentNode;
import com.aptana.editor.php.internal.parser.nodes.PHPFunctionParseNode;
import com.aptana.parsing.ast.IParseNode;

public class PHPFoldingComputer extends AbstractFoldingComputer
{

	private boolean hasCachedAST;

	public PHPFoldingComputer(AbstractThemeableEditor editor, IDocument document)
	{
		super(editor, document);
	}

	@Override
	public boolean isFoldable(IParseNode child)
	{
		if (IPHPConstants.CONTENT_TYPE_PHP.equals(child.getLanguage()))
		{
			// FIXME The PHP parser hacks php start/end blocks. It just creates one PHPBlockNode with 0,0 offsets to
			// hold
			// all children and then grabs the children out of it and attaches them to the parse root node.
			// So until that is changed, there's no way to be able to fold <?php ... ?> blocks.
			if (child instanceof PHPCommentNode)
			{
				PHPCommentNode commentNode = (PHPCommentNode) child;
				return commentNode.isMultiline() || commentNode.isPHPDoc();
			}
			switch (child.getNodeType())
			{
				case PHPBaseParseNode.HTML_NODE:
				case PHPBaseParseNode.FUNCTION_NODE:
				case PHPBaseParseNode.CLASS_NODE:
				case PHPBaseParseNode.FOR_NODE:
				case PHPBaseParseNode.WHILE_NODE:
				case PHPBaseParseNode.DO_NODE:
				case PHPBaseParseNode.SWITCH_NODE:
				case PHPBaseParseNode.SWITCH_CASE_NODE:
				case PHPBaseParseNode.TRY_NODE:
				case PHPBaseParseNode.CATCH_NODE:
				case PHPBaseParseNode.IF_ELSE_NODE:
					return true;
			}
			return false;
		}
		return htmlFoldingComputer().isFoldable(child);
	}

	private synchronized AbstractFoldingComputer htmlFoldingComputer()
	{
		return new HTMLFoldingComputer(getEditor(), getDocument());
	}

	@Override
	public boolean isCollapsed(IParseNode child)
	{
		if (IPHPConstants.CONTENT_TYPE_PHP.equals(child.getLanguage()))
		{
			if (child instanceof PHPFunctionParseNode)
			{
				return Platform.getPreferencesService().getBoolean(PHPEplPlugin.PLUGIN_ID,
						PreferenceConstants.EDITOR_FOLDING_FUNCTIONS, false, null);
			}
			if (child instanceof PHPClassParseNode)
			{
				return Platform.getPreferencesService().getBoolean(PHPEplPlugin.PLUGIN_ID,
						PreferenceConstants.EDITOR_FOLDING_CLASSES, false, null);
			}
			if (child instanceof PHPCommentNode)
			{
				PHPCommentNode commentNode = (PHPCommentNode) child;
				if (commentNode.isPHPDoc())
				{
					return Platform.getPreferencesService().getBoolean(PHPEplPlugin.PLUGIN_ID,
							PreferenceConstants.EDITOR_FOLDING_PHPDOC, false, null);
				}
				return Platform.getPreferencesService().getBoolean(PHPEplPlugin.PLUGIN_ID,
						PreferenceConstants.EDITOR_FOLDING_COMMENTS, false, null);
			}
		}
		return htmlFoldingComputer().isCollapsed(child);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.aptana.editor.common.text.AbstractFoldingComputer#getPositions(org.eclipse.core.runtime.IProgressMonitor,
	 * com.aptana.parsing.ast.IParseNode)
	 */
	@Override
	protected Map<ProjectionAnnotation, Position> getPositions(IProgressMonitor monitor, IParseNode parseNode)
	{
		if (parseNode instanceof PHPParseRootNode)
		{
			hasCachedAST = ((PHPParseRootNode) parseNode).isCached();
		}
		return super.getPositions(monitor, parseNode);
	}

	/**
	 * Returns <code>true</code> if the {@link #getPositions(IProgressMonitor, IParseNode)} call detected that the AST
	 * that was passed is cached (e.g. there is a parse error that prevented a creation of a new AST).
	 * 
	 * @return <code>true</code> if the AST is cached; <code>false</code> otherwise.
	 */
	public boolean hasCachedAST()
	{
		return hasCachedAST;
	}
}
