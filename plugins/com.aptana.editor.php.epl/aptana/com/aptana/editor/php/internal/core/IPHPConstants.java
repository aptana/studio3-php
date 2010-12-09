package com.aptana.editor.php.internal.core;

import org.eclipse.jface.text.IDocument;

public interface IPHPConstants
{
	/**
	 * PHP document type
	 */
	public String CONTENT_TYPE_HTML_PHP = "com.aptana.contenttype.phtml.php"; //$NON-NLS-1$
	
	/**
	 * PHP partitions type
	 */
	public String CONTENT_TYPE_PHP = "com.aptana.contenttype.php"; //$NON-NLS-1$

	/**
	 * Scope names for PHP scopes.
	 */
	public static final String EMBEDDED_CSS_SCOPE = "source.css.embedded.html"; //$NON-NLS-1$
	public static final String EMBEDDED_JS_SCOPE = "source.js.embedded.html"; //$NON-NLS-1$
	// public static final String TOPLEVEL_RHTML_SCOPE = "text.html.ruby"; //$NON-NLS-1$

	public final static String PREFIX = "__php_"; //$NON-NLS-1$
	public final static String DEFAULT = "__php" + IDocument.DEFAULT_CONTENT_TYPE; //$NON-NLS-1$
	public static final String COMMAND = PREFIX + "command"; //$NON-NLS-1$

	public static final String PHP_SINGLE_LINE_COMMENT = PREFIX + "singleline_comment"; //$NON-NLS-1$
	public static final String PHP_MULTI_LINE_COMMENT = PREFIX + "multiline_comment"; //$NON-NLS-1$
	public static final String PHP_DOC_COMMENT = PREFIX + "phpdoc_comment"; //$NON-NLS-1$
	public static final String PHP_COMMENT = PREFIX + "comment"; //$NON-NLS-1$
	public final static String PHP_STRING_SINGLE = PREFIX + "string_single"; //$NON-NLS-1$
	public final static String PHP_STRING_DOUBLE = PREFIX + "string_double"; //$NON-NLS-1$
}
