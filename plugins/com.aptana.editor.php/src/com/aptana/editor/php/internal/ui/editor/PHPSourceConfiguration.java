package com.aptana.editor.php.internal.ui.editor;

import static com.aptana.editor.php.internal.core.IPHPConstants.COMMAND;
import static com.aptana.editor.php.internal.core.IPHPConstants.CONTENT_TYPE_PHP;
import static com.aptana.editor.php.internal.core.IPHPConstants.DEFAULT;
import static com.aptana.editor.php.internal.core.IPHPConstants.PHP_DOC_COMMENT;
import static com.aptana.editor.php.internal.core.IPHPConstants.PHP_MULTI_LINE_COMMENT;
import static com.aptana.editor.php.internal.core.IPHPConstants.PHP_SINGLE_LINE_COMMENT;
import static com.aptana.editor.php.internal.core.IPHPConstants.PHP_STRING_DOUBLE;
import static com.aptana.editor.php.internal.core.IPHPConstants.PHP_STRING_SINGLE;
import static com.aptana.editor.php.internal.core.IPHPConstants.PREFIX;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;

import com.aptana.editor.common.CommonEditorPlugin;
import com.aptana.editor.common.IPartitioningConfiguration;
import com.aptana.editor.common.ISourceViewerConfiguration;
import com.aptana.editor.common.scripting.IContentTypeTranslator;
import com.aptana.editor.common.scripting.QualifiedContentType;
import com.aptana.editor.common.text.rules.ISubPartitionScanner;
import com.aptana.editor.common.text.rules.SubPartitionScanner;
import com.aptana.editor.php.internal.ui.editor.scanner.PHPCodeScanner;
import com.aptana.theme.IThemeManager;
import com.aptana.theme.ThemePlugin;

public class PHPSourceConfiguration implements IPartitioningConfiguration, ISourceViewerConfiguration
{
	public static final String[] CONTENT_TYPES = new String[] { DEFAULT, PHP_SINGLE_LINE_COMMENT,
		PHP_DOC_COMMENT, PHP_MULTI_LINE_COMMENT, COMMAND, PHP_STRING_SINGLE, PHP_STRING_DOUBLE };/* REGULAR_EXPRESSION */

	private static final String[][] TOP_CONTENT_TYPES = new String[][] { { CONTENT_TYPE_PHP } };

	private IPredicateRule[] partitioningRules = new IPredicateRule[] {
			new EndOfLineRule("//", new Token(PHP_SINGLE_LINE_COMMENT)), //$NON-NLS-1$
			new EndOfLineRule("#", new Token(PHP_SINGLE_LINE_COMMENT)), //$NON-NLS-1$
			new MultiLineRule("/**", "*/", new Token(PHP_DOC_COMMENT), (char) 0, true), //$NON-NLS-1$ //$NON-NLS-2$
			new MultiLineRule("/*", "*/", new Token(PHP_MULTI_LINE_COMMENT), (char) 0, true), //$NON-NLS-1$ //$NON-NLS-2$
			new MultiLineRule("\'", "\'", new Token(PHP_STRING_SINGLE), (char) 0, true), //$NON-NLS-1$ //$NON-NLS-2$
			new MultiLineRule("\"", "\"", new Token(PHP_STRING_DOUBLE), (char) 0, true), //$NON-NLS-1$ //$NON-NLS-2$
			new SingleLineRule("\"", "\"", new Token(PHP_STRING_DOUBLE), '\\'), //$NON-NLS-1$ //$NON-NLS-2$
			new SingleLineRule("\'", "\'", new Token(PHP_STRING_SINGLE), '\\') }; //$NON-NLS-1$ //$NON-NLS-2$

	private PHPCodeScanner codeScanner;
	private RuleBasedScanner singleLineCommentScanner;
	private RuleBasedScanner multiLineCommentScanner;
	// private RuleBasedScanner commandScanner;
	private RuleBasedScanner singleQuotedStringScanner;
	private RuleBasedScanner doubleQuotedStringScanner;

	private RuleBasedScanner phpDocCommentScanner;

	private static PHPSourceConfiguration instance;

	static
	{
		IContentTypeTranslator c = CommonEditorPlugin.getDefault().getContentTypeTranslator();
		c.addTranslation(new QualifiedContentType(CONTENT_TYPE_PHP), new QualifiedContentType("source.php")); //$NON-NLS-1$
		c.addTranslation(new QualifiedContentType(PHP_STRING_SINGLE), new QualifiedContentType(
				"string.quoted.single.php")); //$NON-NLS-1$
		c.addTranslation(new QualifiedContentType(PHP_STRING_DOUBLE), new QualifiedContentType(
				"string.quoted.double.php")); //$NON-NLS-1$
		c.addTranslation(new QualifiedContentType(PHP_SINGLE_LINE_COMMENT), new QualifiedContentType(
				"comment.line.double-slash.php")); //$NON-NLS-1$
		c.addTranslation(new QualifiedContentType(PHP_DOC_COMMENT), new QualifiedContentType(
				"comment.block.documentation.phpdoc.php")); //$NON-NLS-1$
		c.addTranslation(new QualifiedContentType(PHP_MULTI_LINE_COMMENT), new QualifiedContentType(
		        "comment.php")); //$NON-NLS-1$
	}

	public static PHPSourceConfiguration getDefault()
	{
		if (instance == null)
		{
			instance = new PHPSourceConfiguration();
		}
		return instance;
	}

	/**
	 * @see com.aptana.editor.common.IPartitioningConfiguration#getContentTypes()
	 */
	public String[] getContentTypes()
	{
		return CONTENT_TYPES;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.ITopContentTypesProvider#getTopContentTypes()
	 */
	public String[][] getTopContentTypes()
	{
		return TOP_CONTENT_TYPES;
	}

	/**
	 * @see com.aptana.editor.common.IPartitioningConfiguration#getPartitioningRules()
	 */
	public IPredicateRule[] getPartitioningRules()
	{
		return partitioningRules;
	}

	/**
	 * @see com.aptana.editor.common.IPartitioningConfiguration#createSubPartitionScanner()
	 */
	public ISubPartitionScanner createSubPartitionScanner()
	{
		return new SubPartitionScanner(partitioningRules, CONTENT_TYPES, new Token(DEFAULT));
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.IPartitioningConfiguration#getDocumentDefaultContentType()
	 */
	public String getDocumentContentType(String contentType)
	{
		if (contentType.startsWith(PREFIX))
		{
			return CONTENT_TYPE_PHP;
		}
		return null;
	}

	/**
	 * @see com.aptana.editor.common.ISourceViewerConfiguration#setupPresentationReconciler(org.eclipse.jface.text.presentation.PresentationReconciler,
	 *      org.eclipse.jface.text.source.ISourceViewer)
	 */
	public void setupPresentationReconciler(PresentationReconciler reconciler, ISourceViewer sourceViewer)
	{
		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getCodeScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		reconciler.setDamager(dr, DEFAULT);
		reconciler.setRepairer(dr, DEFAULT);

		dr = new DefaultDamagerRepairer(getSingleLineCommentScanner());
		reconciler.setDamager(dr, PHP_SINGLE_LINE_COMMENT);
		reconciler.setRepairer(dr, PHP_SINGLE_LINE_COMMENT);

		dr = new DefaultDamagerRepairer(getPhpDocCommentScanner());
		reconciler.setDamager(dr, PHP_DOC_COMMENT);
		reconciler.setRepairer(dr, PHP_DOC_COMMENT);
		
		dr = new DefaultDamagerRepairer(getMultiLineCommentScanner());
		reconciler.setDamager(dr, PHP_MULTI_LINE_COMMENT);
		reconciler.setRepairer(dr, PHP_MULTI_LINE_COMMENT);

		// dr = new DefaultDamagerRepairer(getCommandScanner());
		// reconciler.setDamager(dr, PHPSourceConfiguration.COMMAND);
		// reconciler.setRepairer(dr, PHPSourceConfiguration.COMMAND);

		dr = new DefaultDamagerRepairer(getSingleQuotedStringScanner());
		reconciler.setDamager(dr, PHP_STRING_SINGLE);
		reconciler.setRepairer(dr, PHP_STRING_SINGLE);

		dr = new DefaultDamagerRepairer(getDoubleQuotedStringScanner());
		reconciler.setDamager(dr, PHP_STRING_DOUBLE);
		reconciler.setRepairer(dr, PHP_STRING_DOUBLE);
	}

	private ITokenScanner getCodeScanner()
	{
		if (codeScanner == null)
		{
			codeScanner = new PHPCodeScanner();
		}
		return codeScanner;
	}

	private ITokenScanner getPhpDocCommentScanner()
	{
		if (phpDocCommentScanner == null)
		{
			phpDocCommentScanner = new RuleBasedScanner();
			phpDocCommentScanner.setDefaultReturnToken(getToken("comment.block.documentation.phpdoc.php")); //$NON-NLS-1$
		}
		return phpDocCommentScanner;
	}
	
	private ITokenScanner getMultiLineCommentScanner()
	{
		if (multiLineCommentScanner == null)
		{
			multiLineCommentScanner = new RuleBasedScanner();
			multiLineCommentScanner.setDefaultReturnToken(getToken("comment.php")); //$NON-NLS-1$
		}
		return multiLineCommentScanner;
	}

	private ITokenScanner getSingleLineCommentScanner()
	{
		if (singleLineCommentScanner == null)
		{
			singleLineCommentScanner = new RuleBasedScanner();
			singleLineCommentScanner.setDefaultReturnToken(getToken("comment.line.number-sign.php")); //$NON-NLS-1$
		}
		return singleLineCommentScanner;
	}

	private ITokenScanner getSingleQuotedStringScanner()
	{
		if (singleQuotedStringScanner == null)
		{
			singleQuotedStringScanner = new RuleBasedScanner();
			singleQuotedStringScanner.setDefaultReturnToken(getToken("string.quoted.single.php")); //$NON-NLS-1$
		}
		return singleQuotedStringScanner;
	}

	private ITokenScanner getDoubleQuotedStringScanner()
	{
		if (doubleQuotedStringScanner == null)
		{
			doubleQuotedStringScanner = new RuleBasedScanner();
			doubleQuotedStringScanner.setDefaultReturnToken(getToken("string.quoted.double.php")); //$NON-NLS-1$
		}
		return doubleQuotedStringScanner;
	}

	protected IToken getToken(String tokenName)
	{
		return getThemeManager().getToken(tokenName);
	}

	protected IThemeManager getThemeManager()
	{
		return ThemePlugin.getDefault().getThemeManager();
	}
}
