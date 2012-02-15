// $codepro.audit.disable platformSpecificLineSeparator
package com.aptana.editor.php.internal.ui.hover;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;
import org2.eclipse.php.internal.core.compiler.ast.nodes.PHPDocBlock;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.StringUtil;
import com.aptana.editor.common.contentassist.ILexemeProvider;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.internal.contentAssist.ContentAssistUtils;
import com.aptana.editor.php.internal.contentAssist.PHPTokenType;
import com.aptana.editor.php.internal.contentAssist.ParsingUtils;
import com.aptana.editor.php.internal.contentAssist.mapping.PHPOffsetMapper;
import com.aptana.editor.php.internal.indexer.AbstractPHPEntryValue;
import com.aptana.editor.php.internal.indexer.PHPDocUtils;
import com.aptana.editor.php.internal.parser.nodes.PHPBaseParseNode;
import com.aptana.editor.php.internal.parser.phpdoc.FunctionDocumentation;
import com.aptana.editor.php.internal.ui.editor.PHPSourceEditor;
import com.aptana.ide.ui.io.internal.UniformFileStoreEditorInput;
import com.aptana.parsing.lexer.Lexeme;

/**
 * Provides PHPDoc as hover info for PHP elements.
 */
public class PHPDocHover extends AbstractPHPTextHover
{

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.hover.AbstractDocumentationHover#getHeader(java.lang.Object,
	 * org.eclipse.ui.IEditorPart, org.eclipse.jface.text.IRegion)
	 */
	@Override
	protected String getHeader(Object element, IEditorPart editorPart, IRegion hoverRegion)
	{
		// Set the header to display the file location
		if (element instanceof IElementEntry)
		{
			IElementEntry entry = (IElementEntry) element;
			if (entry.getModule() != null)
			{
				String moduleName = entry.getModule().getShortName();
				// In case the module is from a remote location, we would like to display the file name as is, and not
				// the temp file name that was created.
				if (editorPart != null && editorPart.getEditorInput() instanceof UniformFileStoreEditorInput)
				{
					UniformFileStoreEditorInput uniformInput = (UniformFileStoreEditorInput) editorPart
							.getEditorInput();
					if (uniformInput.isRemote())
					{
						moduleName = editorPart.getTitle();
					}
				}
				return moduleName;
			}
		}
		else if (element instanceof PHPBaseParseNode)
		{
			return "PHP API"; //$NON-NLS-1$
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.hover.AbstractDocumentationHover#getDocumentation(java.lang.Object,
	 * org.eclipse.ui.IEditorPart, org.eclipse.jface.text.IRegion)
	 */
	@Override
	protected String getDocumentation(Object element, IEditorPart editorPart, IRegion hoverRegion)
	{
		String computedDocumentation = null;
		if (element instanceof IElementEntry)
		{
			IElementEntry entry = (IElementEntry) element;
			AbstractPHPEntryValue phpValue = (AbstractPHPEntryValue) entry.getValue();
			int startOffset = phpValue.getStartOffset();
			PHPDocBlock comment = PHPDocUtils.findFunctionPHPDocComment(entry, startOffset);
			FunctionDocumentation documentation = PHPDocUtils.getFunctionDocumentation(comment);
			computedDocumentation = PHPDocUtils.computeDocumentation(documentation, entry.getEntryPath());
		}
		else if (element instanceof PHPBaseParseNode)
		{
			PHPBaseParseNode node = (PHPBaseParseNode) element;
			computedDocumentation = ContentAssistUtils.getDocumentation(node, node.getNodeName());
		}
		return computedDocumentation;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextHoverExtension2#getHoverInfo2(org.eclipse.jface.text.ITextViewer,
	 * org.eclipse.jface.text.IRegion)
	 */
	public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion)
	{
		if (isHoverEnabled())
		{
			Object[] elements = getPHPElementsAt(textViewer, hoverRegion);
			if (elements == null || elements.length == 0 || elements[0] == null)
			{
				return null;
			}
			return getHoverInfo(elements[0], isBrowserControlAvailable(textViewer), null, getEditor(), hoverRegion);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.aptana.editor.php.internal.ui.hover.AbstractPHPTextHover#getPHPElementsAt(org.eclipse.jface.text.ITextViewer,
	 * org.eclipse.jface.text.IRegion)
	 */
	protected Object[] getPHPElementsAt(ITextViewer textViewer, IRegion hoverRegion)
	{
		PHPSourceEditor editor = (PHPSourceEditor) getEditor();
		if (editor == null)
		{
			return null;
		}
		ILexemeProvider<PHPTokenType> lexemeProvider = ParsingUtils.createLexemeProvider(editor.getDocumentProvider()
				.getDocument(editor.getEditorInput()), hoverRegion.getOffset());

		Lexeme<PHPTokenType> lexeme = lexemeProvider.getLexemeFromOffset(hoverRegion.getOffset());
		if (lexeme == null)
		{
			return null;
		}
		PHPOffsetMapper offsetMapper = editor.getOffsetMapper();
		Object entry = offsetMapper.findEntry(lexeme, lexemeProvider);
		if (entry == null)
		{
			try
			{
				String name = textViewer.getDocument().get(hoverRegion.getOffset(), hoverRegion.getLength());
				if (!StringUtil.EMPTY.equals(name))
				{
					List<Object> elements = ContentAssistUtils.selectModelElements(name, true);
					if (elements != null && !elements.isEmpty())
					{
						// return the first element only
						entry = elements.get(0);
					}
				}
			}
			catch (BadLocationException e)
			{
				IdeLog.logError(PHPEditorPlugin.getDefault(),
						"PHP documentation hover - error getting an element at offset - " + hoverRegion.getOffset(), e); //$NON-NLS-1$
			}

		}
		if (entry != null)
		{
			return new Object[] { entry };
		}
		return null;
	}
}
