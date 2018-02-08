package com.aptana.editor.php.internal.contentAssist;

import java.io.CharArrayReader;
import java.io.IOException;

import javax.swing.text.Segment;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org2.eclipse.php.internal.core.PHPVersion;
import org2.eclipse.php.internal.core.documentModel.parser.AbstractPhpLexer;
import org2.eclipse.php.internal.core.documentModel.parser.PhpLexerFactory;
import org2.eclipse.php.internal.core.documentModel.parser.regions.PHPRegionTypes;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.core.PHPVersionProvider;
import com.aptana.editor.php.internal.core.IPHPConstants;
import com.aptana.editor.php.internal.ui.editor.PHPVersionDocumentManager;

/**
 * A PHP token scanner that scans and create tokens that are used when calculating the code assist scope.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPScopeScanner implements ITokenScanner
{
	private AbstractPhpLexer lexer;
	private int regionOffset;
	private int originalOffset;
	private int originalLength;
	private int prevTokenOffset;
	private int duplicateStartCount;
	private ITypedRegion[] partitions;
	private char[] content;
	private IDocument document;

	public int getTokenLength()
	{
		return lexer.yylength();
	}

	public int getTokenOffset()
	{
		return regionOffset + lexer.getTokenStart();
	}

	public IToken nextToken()
	{
		try
		{
			String token = lexer.getNextToken();
			int tokenOffset = getTokenOffset();
			if (prevTokenOffset == tokenOffset)
			{
				// we stumble into a case where the lexer failed to notify us with the end
				// token, so force a stop.
				if (duplicateStartCount == 3)
				{
					duplicateStartCount = 0;
					dispose();
					return Token.EOF;
				}
				duplicateStartCount++;
			}
			else
			{
				duplicateStartCount = 0;
				prevTokenOffset = tokenOffset;
				if (token == null)
				{
					dispose();
					return Token.EOF;
				}
			}
			if (PHPRegionTypes.PHP_CLOSETAG.equals(token))
			{
				if (partitions == null)
				{
					try
					{
						partitions = TextUtilities.computePartitioning(document,
								IDocumentExtension3.DEFAULT_PARTITIONING, originalOffset, originalLength, false);
					}
					catch (BadLocationException e)
					{
						IdeLog.logError(PHPEditorPlugin.getDefault(), "PHP scope-scanner error", e); //$NON-NLS-1$
					}
				}
				// Check if we have more regions of PHP after this close tag.
				// If so, reset the lexer for the next region.
				ITypedRegion nextPhpRegion = getNextPhpRegion();
				if (nextPhpRegion != null)
				{
					int nextRegionOffset = nextPhpRegion.getOffset() - this.originalOffset;
					Segment segment = new Segment(content, nextRegionOffset, content.length - nextRegionOffset);
					lexer.reset(segment);
					regionOffset = nextPhpRegion.getOffset();
				}
			}
			return new Token(token);
		}
		catch (IOException e)
		{
			IdeLog.logError(PHPEditorPlugin.getDefault(), "PHP scope-scanner error", e); //$NON-NLS-1$
		}
		dispose();
		return Token.EOF;
	}

	/*
	 * Dispose this scanner.
	 */
	private void dispose()
	{
		this.content = null;
		this.partitions = null;
		this.lexer = null;
		this.document = null;
	}

	/**
	 * Returns the next PHP region located after the current token end position
	 * 
	 * @return The next PHP region, or null if non exists.
	 */
	protected ITypedRegion getNextPhpRegion()
	{
		if (partitions != null)
		{
			int offset = getTokenOffset() + getTokenLength();
			for (ITypedRegion region : partitions)
			{
				if (region.getOffset() > offset)
				{
					if (region.getType().startsWith(IPHPConstants.DEFAULT))
					{
						return region;
					}
				}
			}
		}
		return null;
	}

	public void setRange(IDocument document, int offset, int length)
	{
		this.document = document;
		this.originalOffset = offset;
		this.originalLength = length;
		this.regionOffset = this.originalOffset;
		this.prevTokenOffset = -1;
		PHPVersion phpVersion = PHPVersionDocumentManager.getPHPVersion(document);
		if (phpVersion == null)
		{
			phpVersion = PHPVersionProvider.getDefaultPHPVersion();
		}
		try
		{
			content = document.get(offset, length).toCharArray();
			lexer = PhpLexerFactory.createLexer(new CharArrayReader(content), phpVersion); // $codepro.audit.disable
																							// closeWhereCreated
			// set initial lexer state - we use reflection here since we don't
			// know the constant value of
			// of this state in specific PHP version lexer
			int state = lexer.getClass().getField("ST_PHP_IN_SCRIPTING").getInt(lexer); //$NON-NLS-1$
			lexer.initialize(state);
			lexer.setPatterns(null);
			lexer.setAspTags(true);
		}
		catch (Exception e)
		{
			IdeLog.logError(PHPEditorPlugin.getDefault(), "PHP scope-scanner error (setRange)", e); //$NON-NLS-1$
		}
	}

}
