package com.aptana.editor.php.internal.editor;

import static com.aptana.editor.php.internal.IPHPConstants.COMMAND;
import static com.aptana.editor.php.internal.IPHPConstants.CONTENT_TYPE_PHP;
import static com.aptana.editor.php.internal.IPHPConstants.DEFAULT;
import static com.aptana.editor.php.internal.IPHPConstants.PHP_MULTI_LINE_COMMENT;
import static com.aptana.editor.php.internal.IPHPConstants.PHP_SINGLE_LINE_COMMENT;
import static com.aptana.editor.php.internal.IPHPConstants.PHP_STRING_DOUBLE;
import static com.aptana.editor.php.internal.IPHPConstants.PHP_STRING_SINGLE;
import static com.aptana.editor.php.internal.IPHPConstants.PREFIX;

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
import com.aptana.editor.common.theme.IThemeManager;
import com.aptana.editor.php.internal.editor.scanner.PHPCodeScanner;

public class PHPSourceConfiguration implements IPartitioningConfiguration, ISourceViewerConfiguration
{
	public static final String[] CONTENT_TYPES = new String[] { DEFAULT, PHP_SINGLE_LINE_COMMENT,
			PHP_MULTI_LINE_COMMENT, COMMAND, PHP_STRING_SINGLE, PHP_STRING_DOUBLE };/* REGULAR_EXPRESSION */

	private static final String[][] TOP_CONTENT_TYPES = new String[][] { { CONTENT_TYPE_PHP } };

	private IPredicateRule[] partitioningRules = new IPredicateRule[] {
			new EndOfLineRule("//", new Token(PHP_SINGLE_LINE_COMMENT)), //$NON-NLS-1$
			new MultiLineRule("/*", "*/", new Token(PHP_MULTI_LINE_COMMENT), (char) 0, true), //$NON-NLS-1$ //$NON-NLS-2$
			new SingleLineRule("\"", "\"", new Token(PHP_STRING_DOUBLE), '\\'), //$NON-NLS-1$ //$NON-NLS-2$
			new SingleLineRule("\'", "\'", new Token(PHP_STRING_SINGLE), '\\') }; //$NON-NLS-1$ //$NON-NLS-2$

	private PHPCodeScanner codeScanner;
	private RuleBasedScanner singleLineCommentScanner;
	private RuleBasedScanner multiLineCommentScanner;
	private RuleBasedScanner commandScanner;
	private RuleBasedScanner singleQuotedStringScanner;
	private RuleBasedScanner doubleQuotedStringScanner;

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
				"comment.line.number-sign.php")); //$NON-NLS-1$
		c.addTranslation(new QualifiedContentType(PHP_MULTI_LINE_COMMENT), new QualifiedContentType(
				"comment.block.documentation.php")); //$NON-NLS-1$
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

	private ITokenScanner getMultiLineCommentScanner()
	{
		if (multiLineCommentScanner == null)
		{
			multiLineCommentScanner = new RuleBasedScanner();
			multiLineCommentScanner.setDefaultReturnToken(getToken("comment.block.documentation.php")); //$NON-NLS-1$
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

	// private ITokenScanner getRegexpScanner()
	// {
	// if (regexpScanner == null)
	// {
	// regexpScanner = new PHPRegexpScanner();
	// }
	// return regexpScanner;
	// }

	private ITokenScanner getCommandScanner()
	{
		if (commandScanner == null)
		{
			commandScanner = new RuleBasedScanner();
			commandScanner.setDefaultReturnToken(getToken("string.interpolated.php")); //$NON-NLS-1$
		}
		return commandScanner;
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
		return CommonEditorPlugin.getDefault().getThemeManager();
	}
}
