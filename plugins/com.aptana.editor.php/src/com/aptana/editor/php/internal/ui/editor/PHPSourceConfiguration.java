package com.aptana.editor.php.internal.ui.editor;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.source.ISourceViewer;

import com.aptana.core.util.StringUtil;
import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.editor.common.CommonEditorPlugin;
import com.aptana.editor.common.CommonUtil;
import com.aptana.editor.common.IPartitioningConfiguration;
import com.aptana.editor.common.ISourceViewerConfiguration;
import com.aptana.editor.common.scripting.IContentTypeTranslator;
import com.aptana.editor.common.scripting.QualifiedContentType;
import com.aptana.editor.common.text.rules.CommentScanner;
import com.aptana.editor.common.text.rules.CompositePartitionScanner;
import com.aptana.editor.common.text.rules.EmptyCommentRule;
import com.aptana.editor.common.text.rules.ISubPartitionScanner;
import com.aptana.editor.common.text.rules.PartitionerSwitchingIgnoreRule;
import com.aptana.editor.common.text.rules.SubPartitionScanner;
import com.aptana.editor.common.text.rules.ThemeingDamagerRepairer;
import com.aptana.editor.css.ICSSConstants;
import com.aptana.editor.html.HTMLSourceConfiguration;
import com.aptana.editor.html.IHTMLConstants;
import com.aptana.editor.js.IJSConstants;
import com.aptana.editor.php.internal.contentAssist.PHPContentAssistProcessor;
import com.aptana.editor.php.internal.core.IPHPConstants;
import com.aptana.editor.php.internal.parser.HeredocRule;
import com.aptana.editor.php.internal.parser.PHPTokenType;
import com.aptana.editor.php.internal.text.rules.DoubleQuotedStringRule;
import com.aptana.editor.php.internal.text.rules.FastPHPStringTokenScanner;
import com.aptana.editor.php.internal.ui.editor.scanner.PHPCodeScanner;
import com.aptana.editor.php.internal.ui.editor.scanner.PHPDocScanner;

public class PHPSourceConfiguration implements IPartitioningConfiguration, ISourceViewerConfiguration, IPHPConstants
{
	public static final String[] CONTENT_TYPES = new String[] { DEFAULT, PHP_HASH_LINE_COMMENT, PHP_SLASH_LINE_COMMENT,
			PHP_DOC_COMMENT, PHP_MULTI_LINE_COMMENT, PHP_STRING_SINGLE, PHP_STRING_DOUBLE, PHP_HEREDOC, PHP_NOWDOC };/* REGULAR_EXPRESSION */

	private static final String[][] TOP_CONTENT_TYPES = new String[][] { { CONTENT_TYPE_PHP } };

	private IPredicateRule[] partitioningRules = new IPredicateRule[] {
			new EndOfLineRule("//", getToken(PHP_SLASH_LINE_COMMENT)), //$NON-NLS-1$
			new EndOfLineRule("#", getToken(PHP_HASH_LINE_COMMENT)), //$NON-NLS-1$
			new EmptyCommentRule(getToken(PHP_MULTI_LINE_COMMENT)),
			new PartitionerSwitchingIgnoreRule(
					new MultiLineRule("/**", "*/", getToken(PHP_DOC_COMMENT), (char) 0, true)), //$NON-NLS-1$ //$NON-NLS-2$
			new PartitionerSwitchingIgnoreRule(new MultiLineRule(
					"/*", "*/", getToken(PHP_MULTI_LINE_COMMENT), (char) 0, true)), //$NON-NLS-1$ //$NON-NLS-2$
			new PartitionerSwitchingIgnoreRule(new MultiLineRule("\'", "\'", getToken(PHP_STRING_SINGLE), '\\', true)), //$NON-NLS-1$ //$NON-NLS-2$
			new PartitionerSwitchingIgnoreRule(new DoubleQuotedStringRule(getToken(PHP_STRING_DOUBLE))),
			new PartitionerSwitchingIgnoreRule(new HeredocRule(getToken(PHP_HEREDOC), false)),
			new PartitionerSwitchingIgnoreRule(new HeredocRule(getToken(PHP_NOWDOC), true)), };

	private static PHPSourceConfiguration instance;

	static
	{
		IContentTypeTranslator c = CommonEditorPlugin.getDefault().getContentTypeTranslator();
		// Toplevel
		c.addTranslation(new QualifiedContentType(CONTENT_TYPE_HTML_PHP), new QualifiedContentType("text.html.basic")); //$NON-NLS-1$
		c.addTranslation(new QualifiedContentType(CONTENT_TYPE_HTML_PHP, IDocument.DEFAULT_CONTENT_TYPE),
				new QualifiedContentType("text.html.basic")); //$NON-NLS-1$

		// Inside PHP tags
		c.addTranslation(new QualifiedContentType(CONTENT_TYPE_HTML_PHP, CONTENT_TYPE_PHP), new QualifiedContentType(
				"text.html.basic", "source.php.embedded.block.html")); //$NON-NLS-1$ //$NON-NLS-2$

		// Outside PHP tags
		c.addTranslation(new QualifiedContentType(CONTENT_TYPE_HTML_PHP, IHTMLConstants.CONTENT_TYPE_HTML),
				new QualifiedContentType("text.html.basic")); //$NON-NLS-1$

		// CSS
		c.addTranslation(new QualifiedContentType(CONTENT_TYPE_HTML_PHP, ICSSConstants.CONTENT_TYPE_CSS),
				new QualifiedContentType("text.html.basic", "source.css.embedded.html")); //$NON-NLS-1$ //$NON-NLS-2$

		// JS
		c.addTranslation(new QualifiedContentType(CONTENT_TYPE_HTML_PHP, IJSConstants.CONTENT_TYPE_JS),
				new QualifiedContentType("text.html.basic", "source.js.embedded.html")); //$NON-NLS-1$ //$NON-NLS-2$

		// Single-quoted string
		c.addTranslation(new QualifiedContentType(PHP_STRING_SINGLE), new QualifiedContentType(
				PHPTokenType.STRING_SINGLE.toString()));

		// Double-quoted string
		c.addTranslation(new QualifiedContentType(PHP_STRING_DOUBLE), new QualifiedContentType(
				PHPTokenType.STRING_DOUBLE.toString()));

		// heredoc
		c.addTranslation(new QualifiedContentType(PHP_HEREDOC),
				new QualifiedContentType(PHPTokenType.HEREDOC.toString()));

		// '#' Single line comments
		c.addTranslation(new QualifiedContentType(PHP_HASH_LINE_COMMENT), new QualifiedContentType(
				PHPTokenType.COMMENT_HASH.toString()));

		// '//' Single line comments
		c.addTranslation(new QualifiedContentType(PHP_SLASH_LINE_COMMENT), new QualifiedContentType(
				PHPTokenType.COMMENT_SLASH.toString()));

		// PHPDoc
		c.addTranslation(new QualifiedContentType(PHP_DOC_COMMENT), new QualifiedContentType(
				PHPTokenType.COMMENT_PHPDOC.toString()));

		// Multiline comments
		c.addTranslation(new QualifiedContentType(PHP_MULTI_LINE_COMMENT), new QualifiedContentType(
				PHPTokenType.COMMENT_BLOCK.toString()));

		// PHP Start tags
		c.addTranslation(new QualifiedContentType(CONTENT_TYPE_HTML_PHP, CompositePartitionScanner.START_SWITCH_TAG),
				new QualifiedContentType(
						"text.html.basic", "source.php.embedded.block.html", "punctuation.section.embedded.begin.php")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		// PHP End tags
		c.addTranslation(new QualifiedContentType(CONTENT_TYPE_HTML_PHP, CompositePartitionScanner.END_SWITCH_TAG),
				new QualifiedContentType(
						"text.html.basic", "source.php.embedded.block.html", "punctuation.section.embedded.end.php")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	private PHPSourceConfiguration()
	{
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
		return new SubPartitionScanner(partitioningRules, CONTENT_TYPES, getToken(DEFAULT));
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
		DefaultDamagerRepairer dr = new ThemeingDamagerRepairer(getCodeScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		reconciler.setDamager(dr, DEFAULT);
		reconciler.setRepairer(dr, DEFAULT);

		dr = new ThemeingDamagerRepairer(getSingleLineCommentScanner());
		reconciler.setDamager(dr, PHP_HASH_LINE_COMMENT);
		reconciler.setRepairer(dr, PHP_HASH_LINE_COMMENT);

		dr = new ThemeingDamagerRepairer(getSingleLineCommentScanner());
		reconciler.setDamager(dr, PHP_SLASH_LINE_COMMENT);
		reconciler.setRepairer(dr, PHP_SLASH_LINE_COMMENT);

		dr = new ThemeingDamagerRepairer(getPhpDocCommentScanner());
		reconciler.setDamager(dr, PHP_DOC_COMMENT);
		reconciler.setRepairer(dr, PHP_DOC_COMMENT);

		dr = new ThemeingDamagerRepairer(getMultiLineCommentScanner());
		reconciler.setDamager(dr, PHP_MULTI_LINE_COMMENT);
		reconciler.setRepairer(dr, PHP_MULTI_LINE_COMMENT);

		dr = new ThemeingDamagerRepairer(getSingleQuotedStringScanner());
		reconciler.setDamager(dr, PHP_STRING_SINGLE);
		reconciler.setRepairer(dr, PHP_STRING_SINGLE);

		dr = new ThemeingDamagerRepairer(getDoubleQuotedStringScanner());
		reconciler.setDamager(dr, PHP_STRING_DOUBLE);
		reconciler.setRepairer(dr, PHP_STRING_DOUBLE);

		dr = new ThemeingDamagerRepairer(getHeredocScanner());
		reconciler.setDamager(dr, PHP_HEREDOC);
		reconciler.setRepairer(dr, PHP_HEREDOC);

		dr = new ThemeingDamagerRepairer(getNowdocScanner());
		reconciler.setDamager(dr, PHP_NOWDOC);
		reconciler.setRepairer(dr, PHP_NOWDOC);

	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.ISourceViewerConfiguration#getContentAssistProcessor(com.aptana.editor.common.
	 * AbstractThemeableEditor, java.lang.String)
	 */
	public IContentAssistProcessor getContentAssistProcessor(AbstractThemeableEditor editor, String contentType)
	{
		if (contentType.startsWith(IPHPConstants.PREFIX))
		{
			return new PHPContentAssistProcessor(editor);
		}
		// In any other case, call the HTMLSourceViewerConfiguration to compute the assist processor.
		return HTMLSourceConfiguration.getDefault().getContentAssistProcessor(editor, contentType);
	}

	private ITokenScanner getCodeScanner()
	{
		return new PHPCodeScanner();
	}

	private ITokenScanner getPhpDocCommentScanner()
	{
		return new PHPDocScanner();
	}

	private ITokenScanner getMultiLineCommentScanner()
	{
		return new CommentScanner(getToken(PHPTokenType.COMMENT_BLOCK));
	}

	private ITokenScanner getSingleLineCommentScanner()
	{
		return new CommentScanner(getToken(StringUtil.EMPTY));
	}

	private ITokenScanner getSingleQuotedStringScanner()
	{
		return new SingleQuotedStringScanner();
	}

	private ITokenScanner getDoubleQuotedStringScanner()
	{
		return new FastPHPStringTokenScanner(getToken(PHPTokenType.META_STRING_CONTENTS_DOUBLE));
	}

	private ITokenScanner getHeredocScanner()
	{
		return new FastPHPStringTokenScanner(getToken(PHPTokenType.HEREDOC));
	}

	private ITokenScanner getNowdocScanner()
	{
		RuleBasedScanner nowdocScanner = new RuleBasedScanner();
		nowdocScanner.setDefaultReturnToken(getToken(PHPTokenType.NOWDOC));
		return nowdocScanner;
	}

	static IToken getToken(PHPTokenType type)
	{
		return getToken(type.toString());
	}

	private static IToken getToken(String tokenName)
	{
		return CommonUtil.getToken(tokenName);
	}

}
