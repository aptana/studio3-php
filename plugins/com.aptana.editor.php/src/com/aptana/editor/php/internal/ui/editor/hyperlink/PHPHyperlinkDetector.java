package com.aptana.editor.php.internal.ui.editor.hyperlink;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.AbstractHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import com.aptana.editor.common.contentassist.LexemeProvider;
import com.aptana.editor.php.internal.contentAssist.PHPTokenType;
import com.aptana.editor.php.internal.contentAssist.ParsingUtils;
import com.aptana.editor.php.internal.contentAssist.mapping.ICodeLocation;
import com.aptana.editor.php.internal.contentAssist.mapping.PHPOffsetMapper;
import com.aptana.editor.php.internal.ui.editor.PHPSourceEditor;
import com.aptana.editor.php.util.EditorUtils;
import com.aptana.parsing.lexer.Lexeme;

public class PHPHyperlinkDetector extends AbstractHyperlinkDetector
{
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.hyperlink.IHyperlinkDetector#detectHyperlinks(org.eclipse.jface.text.ITextViewer,
	 * org.eclipse.jface.text.IRegion, boolean)
	 */
	public IHyperlink[] detectHyperlinks(ITextViewer textViewer, IRegion region, boolean canShowMultipleHyperlinks)
	{
		if (region == null || textViewer == null)
		{
			return null;
		}
		IEditorPart part = (IEditorPart) getAdapter(IEditorPart.class);
		if (!(part instanceof PHPSourceEditor))
		{
			return null;
		}
		PHPSourceEditor editor = (PHPSourceEditor) part;

		LexemeProvider<PHPTokenType> lexemeProvider = ParsingUtils.createLexemeProvider(textViewer.getDocument(),
				region.getOffset());

		Lexeme<PHPTokenType> lexeme = lexemeProvider.getLexemeFromOffset(region.getOffset());
		if (lexeme == null)
		{
			return null;
		}
		ArrayList<IHyperlink> result = new ArrayList<IHyperlink>();
		PHPOffsetMapper offsetMapper = editor.getOffsetMapper();
		ICodeLocation codeLocation = offsetMapper.findTarget(lexeme, lexemeProvider);
		if (codeLocation != null)
		{
			IRegion linkRegion = new Region(lexeme.getStartingOffset(), lexeme.getLength());
			IHyperlink link = new PHPHyperLink(codeLocation, linkRegion, EMPTY_STRING, EMPTY_STRING);
			result.add(link);
		}
		if (!result.isEmpty())
		{
			return result.toArray(new IHyperlink[result.size()]);
		}
		return null;
	}

	/**
	 * PHP Hyperlink implementation for PHP elements that we have their declaration identified.
	 * 
	 * @author Shalom Gibly <sgibly@aptana.com>
	 */
	class PHPHyperLink implements IHyperlink
	{
		private IRegion region;
		private String hyperlinkText;
		private String typeLabel;
		private final ICodeLocation codeLocation;

		public PHPHyperLink(ICodeLocation codeLocation, IRegion region, String hyperlinkText, String typeLabel)
		{
			this.codeLocation = codeLocation;
			this.region = region;
			this.hyperlinkText = hyperlinkText;
			this.typeLabel = typeLabel;
		}

		@Override
		public IRegion getHyperlinkRegion()
		{
			return region;
		}

		@Override
		public String getHyperlinkText()
		{
			return hyperlinkText;
		}

		@Override
		public String getTypeLabel()
		{
			return typeLabel;
		}

		/**
		 * @see org.eclipse.jface.text.hyperlink.IHyperlink#open()
		 */
		public void open()
		{
			if (codeLocation == null)
			{
				return;
			}
			openInEditor(codeLocation.getFullPath(), codeLocation.getStartLexeme());
		}

		private void openInEditor(String fileName, Lexeme<PHPTokenType> lexeme)
		{
			File file = new File(fileName);
			IEditorPart part = null;
			if (file.exists())
			{
				part = EditorUtils.openInEditor(file);
			}
			else
			{
				IResource findMember = ResourcesPlugin.getWorkspace().getRoot().findMember(fileName);
				if (findMember != null && findMember.exists() && findMember instanceof IFile)
				{
					part = EditorUtils.openInEditor(new File(((IFile) findMember).getLocationURI()));
				}
			}
			if (part instanceof PHPSourceEditor)
			{
				AbstractTextEditor editor = (AbstractTextEditor) part;
				editor.selectAndReveal(lexeme.getStartingOffset(), lexeme.getLength());
			}
		}
	}
}
