/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.indexer.language;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org2.eclipse.php.internal.core.PHPVersion;
import org2.eclipse.php.internal.core.documentModel.phpElementData.IPHPDocBlock;
import org2.eclipse.php.internal.core.documentModel.phpElementData.IPHPDocTag;
import org2.eclipse.php.internal.core.documentModel.phpElementData.PHPDocBlockImp;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.CollectionsUtil;
import com.aptana.core.util.StringUtil;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.epl.PHPEplPlugin;
import com.aptana.editor.php.indexer.IElementsIndex;
import com.aptana.editor.php.internal.parser.PHPParser;
import com.aptana.editor.php.internal.parser.nodes.IPHPParseNode;
import com.aptana.editor.php.internal.parser.nodes.PHPBaseParseNode;
import com.aptana.editor.php.internal.parser.nodes.PHPClassParseNode;
import com.aptana.editor.php.internal.parser.nodes.PHPConstantNode;
import com.aptana.editor.php.internal.parser.nodes.PHPFunctionParseNode;
import com.aptana.editor.php.internal.parser.nodes.PHPVariableParseNode;
import com.aptana.parsing.ast.IParseNode;

/**
 * @author Pavel Petrochenko
 */
public final class PHPBuiltins
{

	public static final String LANGUAGE_LIBRARY_PATH_BASE = "Resources/language/php"; //$NON-NLS-1$
	public static final String PHP4_LANGUAGE_LIBRARY_PATH = LANGUAGE_LIBRARY_PATH_BASE + "4"; //$NON-NLS-1$
	public static final String PHP5_LANGUAGE_LIBRARY_PATH = LANGUAGE_LIBRARY_PATH_BASE + "5"; //$NON-NLS-1$
	public static final String PHP53_LANGUAGE_LIBRARY_PATH = LANGUAGE_LIBRARY_PATH_BASE + "5.3"; //$NON-NLS-1$
	public static final String PHP54_LANGUAGE_LIBRARY_PATH = LANGUAGE_LIBRARY_PATH_BASE + "5.4"; //$NON-NLS-1$

	private static final int INITIAL_CAPACITY = 5000;
	private static final IPHPDocTag[] NO_TAGS = new IPHPDocTag[0];
	private static final PHPBuiltins instance = new PHPBuiltins();
	@SuppressWarnings("nls")
	private static final Set<String> PHP4_RESTRICTED = CollectionsUtil.newSet("namespace", "using", "goto", "use");

	private Object mutex = new Object();

	private Map<PHPVersion, Set<String>> phpNames;

	// Holds a function name map to the resource name that contains it
	private Map<String, String> builtInFunctions = new HashMap<String, String>();
	// Holds a Class/Constant name map to the resource name that contains it
	private Map<String, String> builtInClasses = new HashMap<String, String>();
	private Map<String, String> builtInConstants = new HashMap<String, String>();

	private SortedSet<Object> builtins;
	private boolean initializing;

	private void addKeywords()
	{
		// additions after reviewing PHP manuals
		addKeyword("cfunction", "cfunction");//$NON-NLS-1$ //$NON-NLS-2$
		// reserved class and method names
		addKeyword("stdClass", "stdClass");//$NON-NLS-1$ //$NON-NLS-2$
		addMagicMethod("__construct", "__construct", PHPVersion.PHP5, PHPVersion.PHP5_3);//$NON-NLS-1$ //$NON-NLS-2$
		addMagicMethod("__destruct", "__destruct", PHPVersion.PHP5, PHPVersion.PHP5_3);//$NON-NLS-1$ //$NON-NLS-2$
		addMagicMethod("__call", "__call", PHPVersion.PHP5, PHPVersion.PHP5_3);//$NON-NLS-1$ //$NON-NLS-2$
		addMagicMethod("__callStatic", "__callStatic", PHPVersion.PHP5, PHPVersion.PHP5_3);//$NON-NLS-1$ //$NON-NLS-2$
		addMagicMethod("__get", "__get", PHPVersion.PHP5, PHPVersion.PHP5_3);//$NON-NLS-1$ //$NON-NLS-2$
		addMagicMethod("__set", "__set", PHPVersion.PHP5, PHPVersion.PHP5_3);//$NON-NLS-1$ //$NON-NLS-2$
		addMagicMethod("__isset", "__isset", PHPVersion.PHP5, PHPVersion.PHP5_3);//$NON-NLS-1$ //$NON-NLS-2$
		addMagicMethod("__unset", "__unset", PHPVersion.PHP5, PHPVersion.PHP5_3);//$NON-NLS-1$ //$NON-NLS-2$
		addMagicMethod("__sleep", "__sleep", PHPVersion.PHP5, PHPVersion.PHP5_3);//$NON-NLS-1$ //$NON-NLS-2$
		addMagicMethod("__wakeup", "__wakeup", PHPVersion.PHP5, PHPVersion.PHP5_3);//$NON-NLS-1$ //$NON-NLS-2$
		addMagicMethod("__toString", "__toString", PHPVersion.PHP5, PHPVersion.PHP5_3);//$NON-NLS-1$ //$NON-NLS-2$
		addMagicMethod("__invoke", "__invoke", PHPVersion.PHP5_3);//$NON-NLS-1$ //$NON-NLS-2$
		addMagicMethod("__set_state", "__set_state", PHPVersion.PHP5, PHPVersion.PHP5_3);//$NON-NLS-1$ //$NON-NLS-2$
		addMagicMethod("__clone", "__clone", PHPVersion.PHP5, PHPVersion.PHP5_3);//$NON-NLS-1$ //$NON-NLS-2$
		// magic constants (PHP 5.x < 5.3)
		addMagicConstant("__LINE__", "__LINE__", PHPVersion.PHP5, PHPVersion.PHP5_3); //$NON-NLS-1$ //$NON-NLS-2$
		addMagicConstant("__FILE__", "__FILE__", PHPVersion.PHP5, PHPVersion.PHP5_3); //$NON-NLS-1$ //$NON-NLS-2$
		addMagicConstant("__FUNCTION__", "__FUNCTION__", PHPVersion.PHP5, PHPVersion.PHP5_3); //$NON-NLS-1$ //$NON-NLS-2$
		addMagicConstant("__CLASS__", "__CLASS__", PHPVersion.PHP5, PHPVersion.PHP5_3); //$NON-NLS-1$ //$NON-NLS-2$
		addMagicConstant("__METHOD__", "__METHOD__", PHPVersion.PHP5, PHPVersion.PHP5_3); //$NON-NLS-1$ //$NON-NLS-2$
		// Super Globals
		addSuperGlobal("$PHP_SELF", "$PHP_SELF");//$NON-NLS-1$ //$NON-NLS-2$
		addSuperGlobal("$GLOBALS", "$GLOBALS");//$NON-NLS-1$ //$NON-NLS-2$
		addSuperGlobal("$_SERVER", "$_SERVER");//$NON-NLS-1$ //$NON-NLS-2$
		addSuperGlobal("$_GET", "$_GET"); //$NON-NLS-1$ //$NON-NLS-2$
		addSuperGlobal("$_POST", "$_POST"); //$NON-NLS-1$ //$NON-NLS-2$
		addSuperGlobal("$_COOKIE", "$_COOKIE"); //$NON-NLS-1$ //$NON-NLS-2$
		addSuperGlobal("$_FILES", "$_FILES"); //$NON-NLS-1$ //$NON-NLS-2$
		addSuperGlobal("$_ENV", "$_ENV"); //$NON-NLS-1$ //$NON-NLS-2$
		addSuperGlobal("$_REQUEST", "$_REQUEST"); //$NON-NLS-1$ //$NON-NLS-2$
		addSuperGlobal("$_SESSION", "$_SESSION"); //$NON-NLS-1$ //$NON-NLS-2$
		addSuperGlobal("$HTTP_POST_VARS", "$HTTP_POST_VARS"); //$NON-NLS-1$ //$NON-NLS-2$
		addSuperGlobal("$HTTP_GET_VARS", "$HTTP_GET_VARS"); //$NON-NLS-1$ //$NON-NLS-2$
		addSuperGlobal("$HTTP_ENV_VARS", "$HTTP_ENV_VARS"); //$NON-NLS-1$ //$NON-NLS-2$
		addSuperGlobal("$HTTP_SERVER_VARS", "$HTTP_SERVER_VARS"); //$NON-NLS-1$ //$NON-NLS-2$
		addSuperGlobal("$HTTP_COOKIE_VARS", "$HTTP_COOKIE_VARS"); //$NON-NLS-1$ //$NON-NLS-2$
		// ///////////////////////////////////
		addKeyword("abstract", "abstract", PHPVersion.PHP5, PHPVersion.PHP5_3); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("array", "array"); //$NON-NLS-1$ //$NON-NLS-2$
		addBuiltin("KEYWORD", "ARRAY_CAST", "Array Cast"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		addKeyword("as", "as"); //$NON-NLS-1$ //$NON-NLS-2$
		addBuiltin("KEYWORD", "BOOL_CAST", "Boolean Cast"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		addKeyword("break", "break"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("case", "case"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("catch", "catch"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("class", "class"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("clone", "clone"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("const", "const"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("continue", "continue"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("declare", "declare"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("default", "default"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("do", "do"); //$NON-NLS-1$ //$NON-NLS-2$
		addBuiltin("KEYWORD", "DOUBLE_CAST", "Double Cast"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		addKeyword("echo", "echo"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("else", "else"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("elseif", "elseif"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("empty", "empty"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("enddeclare", "enddeclare"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("endfor", "endfor"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("endforeach", "endforeach"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("endif", "endif"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("endswitch", "endswitch"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("endwhile", "endwhile"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("eval", "eval"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("exception", "exception"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("exit", "exit"); //$NON-NLS-1$ //$NON-NLS-2$
		addBuiltin("die", "EXIT", "exit"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		addKeyword("extends", "extends"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("final", "final", PHPVersion.PHP5, PHPVersion.PHP5_3); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("for", "for"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("foreach", "foreach"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("function", "function"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("global", "global"); //$NON-NLS-1$ //$NON-NLS-2$
		addBuiltin("__halt_compiler", "HALT_COMPILER", "Halt compiler"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		addKeyword("if", "if"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("implements", "implements"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("include", "include"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("include_once", "include_once"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("instanceof", "instanceof"); //$NON-NLS-1$ //$NON-NLS-2$
		addBuiltin("KEYWORD", "INT_CAST", "Integer Cast"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		addKeyword("interface", "interface", PHPVersion.PHP5, PHPVersion.PHP5_3); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("isset", "isset"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("list", "list"); //$NON-NLS-1$ //$NON-NLS-2$
		addBuiltin("and", "LOGICAL_AND", "Logical and"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		addBuiltin("or", "LOGICAL_OR", "Logical or"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		addBuiltin("xor", "LOGICAL_XOR", "Logical xor"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		addKeyword("new", "new"); //$NON-NLS-1$ //$NON-NLS-2$
		addBuiltin("KEYWORD", "OBJECT_CAST", "Object Cast"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		addKeyword("print", "print"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("private", "private", PHPVersion.PHP5, PHPVersion.PHP5_3); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("protected", "protected", PHPVersion.PHP5, PHPVersion.PHP5_3); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("public", "public", PHPVersion.PHP5, PHPVersion.PHP5_3); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("require", "require"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("require_once", "require_once"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("return", "return"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("static", "static"); //$NON-NLS-1$ //$NON-NLS-2$
		addBuiltin("KEYWORD", "STRING_CAST", "String Cast"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		addKeyword("switch", "switch"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("throw", "throw"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("try", "try"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("unset", "unset"); //$NON-NLS-1$ //$NON-NLS-2$
		addBuiltin("KEYWORD", "UNSET_CAST", "Unset Case"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		addKeyword("var", "var"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("while", "while"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("namespace", "namespace", PHPVersion.PHP5_3); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("goto", "goto", PHPVersion.PHP5_3); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("use", "use", PHPVersion.PHP5_3); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("trait", "trait", PHPVersion.PHP5_4); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("callable", "callable", PHPVersion.PHP5_4); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("insteadof", "insteadof", PHPVersion.PHP5_4); //$NON-NLS-1$ //$NON-NLS-2$

		// not in Zend's lexer grammar
		addKeyword("false", "false"); //$NON-NLS-1$ //$NON-NLS-2$
		//addKeyword("from", "from"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("null", "null"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("old_function", "old_function"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("parent", "parent"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("php_user_filter", "php_user_filter"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("$this", "$this"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("self", "self"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("true", "true"); //$NON-NLS-1$ //$NON-NLS-2$

		// Note: Predefined constants are parsed from the built-in php scripts.
	}

	/**
	 * Adds built-in. By default, the type of node that will be added is IPHPParseNode.KEYWORD_NODE.
	 * 
	 * @param string
	 * @param nodeName
	 * @param description
	 * @param phpVersions
	 *            An optional array of supported php-versions. In case none is passed, the built-in will be added to all
	 *            versions
	 * @see PHPBuiltins#addBuiltin(String, String, String, short, PHPVersion...)
	 */
	private void addBuiltin(String string, String nodeName, String description, PHPVersion... phpVersions)
	{
		addBuiltin(string, nodeName, description, IPHPParseNode.KEYWORD_NODE, phpVersions);
	}

	/**
	 * Adds built-in.
	 * 
	 * @param string
	 * @param nodeName
	 * @param description
	 * @param nodeType
	 * @param phpVersions
	 *            An optional array of supported php-versions. In case none is passed, the built-in will be added to all
	 *            versions
	 */
	private void addBuiltin(String string, String nodeName, String description, short nodeType,
			PHPVersion... phpVersions)
	{
		PHPBaseParseNode node = new PHPBaseParseNode(nodeType, 0, -1, -1, nodeName);
		builtins.add(node);
		node.setDocumentation(new PHPDocBlockImp(description, StringUtil.EMPTY, NO_TAGS, 0));
		if (phpVersions != null && phpVersions.length > 0)
		{
			for (PHPVersion version : phpVersions)
			{
				phpNames.get(version).add(string);
			}
		}
		else
		{
			// add to all versions
			for (PHPVersion version : EnumSet.allOf(PHPVersion.class))
			{
				phpNames.get(version).add(string);
			}
		}
	}

	/**
	 * Adds keyword.
	 * 
	 * @param string
	 * @param nodeName
	 */
	private void addKeyword(String string, String nodeName, PHPVersion... phpVersions)
	{
		addBuiltin(string, nodeName, MessageFormat.format(Messages.KEYWORD_LABEL, nodeName), phpVersions);
	}

	/**
	 * Adds a magic constant (see http://us.php.net/manual/en/language.constants.predefined.php).
	 * 
	 * @param string
	 * @param nodeName
	 */
	private void addMagicConstant(String string, String nodeName, PHPVersion... phpVersions)
	{
		addBuiltin(string, nodeName, MessageFormat.format(Messages.MAGIC_CONSTANT_LABEL, nodeName), phpVersions);
	}

	/**
	 * Adds a magic method (see http://us.php.net/manual/en/language.oop5.magic.php).
	 * 
	 * @param string
	 * @param nodeName
	 */
	private void addMagicMethod(String string, String nodeName, PHPVersion... phpVersions)
	{
		addBuiltin(string, nodeName, MessageFormat.format(Messages.MAGIC_METHOD_LABEL, nodeName), phpVersions);
	}

	/**
	 * Adds a predefined constant (see http://us.php.net/manual/en/reserved.constants.php).
	 * 
	 * @param string
	 * @param nodeName
	 */
	@SuppressWarnings("unused")
	private void addPredefinedConstant(String string, String nodeName, PHPVersion phpVersion)
	{
		addBuiltin(string, nodeName, MessageFormat.format(Messages.PREDEFINED_CONSTANT_LABEL, nodeName), phpVersion);
	}

	/**
	 * Adds keyword.
	 * 
	 * @param string
	 * @param nodeName
	 */
	private void addSuperGlobal(String string, String nodeName)
	{
		// add the super-globals as constants.
		addBuiltin(string, nodeName, MessageFormat.format(Messages.SUPERGLOBAL_LABEL, nodeName),
				IPHPParseNode.CONST_NODE);
	}

	/**
	 * Returns the Built-in PHP index. This method will no block, and a Job will be started to collect the built-ins.
	 * 
	 * @return builtins object
	 */
	public Collection<Object> getBuiltins()
	{
		if (builtins == null)
		{
			synchronized (mutex)
			{
				if (initializing)
				{
					return null;
				}
			}
			Job parseBuiltins = new Job(Messages.PHPBuiltins_indexingLibraries)
			{
				protected IStatus run(IProgressMonitor monitor)
				{
					clean(monitor);
					return Status.OK_STATUS;
				}
			};
			parseBuiltins.setPriority(Job.BUILD);
			parseBuiltins.schedule();
		}
		return builtins;
	}

	/**
	 * Returns <code>true</code> in case the given parse-node name exists in the built-in PHP name elements for the
	 * given version.
	 * 
	 * @param version
	 * @param node
	 * @return <code>true</code> in case the given parse-node name exists in the built-ins; <code>false</code>
	 *         otherwise.
	 */
	public boolean existsIn(PHPVersion version, IPHPParseNode node)
	{
		if (node == null || version == null)
		{
			return false;
		}
		if (version.equals(PHPVersion.PHP4) && node.getClass() == PHPBaseParseNode.class)
		{
			if (PHP4_RESTRICTED.contains(node.getNodeName()))
			{
				return false;
			}
		}
		return phpNames.get(version).contains(node.getNodeName());
	}

	public boolean isBuiltinFunction(String name)
	{
		return builtInFunctions.containsKey(name);
	}

	public boolean isBuiltinClassOrConstant(String name)
	{
		return builtInClasses.containsKey(name) || builtInConstants.containsKey(name);
	}

	public boolean isBuiltinConstant(String name)
	{
		return builtInConstants.containsKey(name);
	}

	public boolean isBuiltinClass(String name)
	{
		return builtInClasses.containsKey(name);
	}

	/**
	 * Returns an input stream for the built-in resource that is associated to the given entry. With this method, we
	 * make sure that the appropriate resource is read for the entry (since we have multiple built-ins).
	 * 
	 * @param entry
	 *            The name/path of the PHP entry.
	 * @return A associated built-in stream; Null, if non is located.
	 * @throws IOException
	 *             In case the steam could not be opened.
	 */
	public InputStream getBuiltinResourceStream(String entry) throws IOException
	{
		String path = null;
		if (isBuiltinFunction(entry))
		{
			path = builtInFunctions.get(entry);
		}
		else if (isBuiltinClassOrConstant(entry))
		{
			path = builtInClasses.get(entry);
			if (path == null)
			{
				path = builtInConstants.get(entry);
			}
		}
		if (path != null)
		{
			try
			{
				URL url = new URL(path);
				return url.openStream();
			}
			catch (MalformedURLException e)
			{
				IdeLog.logWarning(PHPEditorPlugin.getDefault(),
						"Error retrieving the built-in resource", e, PHPEditorPlugin.INDEXER_SCOPE); //$NON-NLS-1$
			}

		}
		return null;
	}

	/**
	 * Built-ins initialization entry point.
	 * 
	 * @param monitor
	 */
	private void initBuiltins(IProgressMonitor monitor)
	{
		try
		{
			IdeLog.logInfo(PHPEditorPlugin.getDefault(),
					"Indexing the PHP API libraries...", null, PHPEditorPlugin.INDEXER_SCOPE); //$NON-NLS-1$
			this.builtins = new TreeSet<Object>(new Comparator<Object>()
			{
				public int compare(Object arg0, Object arg1)
				{
					PHPBaseParseNode node0 = (PHPBaseParseNode) arg0;
					PHPBaseParseNode node1 = (PHPBaseParseNode) arg1;
					if (node0.getClass() == node1.getClass())
					{
						return node0.getNodeName().toLowerCase().compareTo(node1.getNodeName().toLowerCase());
					}
					return node0.getNodeName().compareTo(node1.getNodeName());
				}
			});

			Map<Object, Object> builtins = new HashMap<Object, Object>(INITIAL_CAPACITY);
			long start = System.currentTimeMillis();
			for (PHPVersion version : EnumSet.allOf(PHPVersion.class))
			{
				long timeMillis = System.currentTimeMillis();
				monitor.setTaskName(MessageFormat.format(Messages.PHPBuiltins_languageSupportTaskName,
						version.getAlias()));
				initPHPBuiltins(version, builtins);
				if (PHPEditorPlugin.INDEXER_DEBUG)
				{
					IdeLog.logInfo(PHPEditorPlugin.getDefault(),
							MessageFormat.format("Parsed {0} built-ins ({1}ms)", version.getAlias(), //$NON-NLS-1$
									(System.currentTimeMillis() - timeMillis)), null, PHPEditorPlugin.INDEXER_SCOPE);
					timeMillis = System.currentTimeMillis();
				}
			}
			this.builtins.addAll(builtins.values());
			addKeywords();

			IdeLog.logInfo(
					PHPEditorPlugin.getDefault(),
					MessageFormat.format("Loaded all PHP built-ins ({0}ms)", (System.currentTimeMillis() - start)), null, //$NON-NLS-1$
					PHPEditorPlugin.INDEXER_SCOPE);
		}
		catch (Throwable t)
		{
			IdeLog.logError(PHPEditorPlugin.getDefault(), "Error loading the PHP Built-in API", t); //$NON-NLS-1$
		}
	}

	/**
	 * Initialized the built-ins for a given {@link PHPVersion}.
	 * 
	 * @param version
	 *            A {@link PHPVersion}
	 * @param builtins
	 *            A map that will be loaded with the detected built-ins.
	 */
	private void initPHPBuiltins(PHPVersion version, Map<Object, Object> builtins)
	{
		PHPParser parser = new PHPParser(version, false);
		try
		{
			URL[] urls = getBuiltinsURLs(getLibraryPath(version));
			for (URL url : urls)
			{
				try
				{
					IParseNode parseNode = parser.parse(url.openStream());
					Set<String> names = phpNames.get(version);
					for (IParseNode node : parseNode)
					{
						String name = node.getNameNode().getName().intern();
						names.add(name);
						if (node instanceof PHPFunctionParseNode)
						{
							builtInFunctions.put(name, url.toString().intern());
						}
						else
						{
							addBuiltinClassOrConstant(node, url);
						}
						// Since the constant nodes are inserted to the
						// built-ins directly, don't deal with them here.
						// (they were already dealt with on the
						// addBuiltinClassOrConstant call above)
						if (!(node instanceof PHPConstantNode))
						{
							builtins.put(name, node);
						}
					}
				}
				catch (Exception e)
				{
					IdeLog.logError(PHPEditorPlugin.getDefault(),
							"Error loading the built-in PHP API for " + url.getFile(), e); //$NON-NLS-1$
				}
			}
		}
		catch (Exception e)
		{
			IdeLog.logError(PHPEditorPlugin.getDefault(), "Error loading the built-in PHP API.", e); //$NON-NLS-1$
		}
	}

	/**
	 * Returns the library path for a given {@link PHPVersion}.
	 * 
	 * @param version
	 * @return A library path.
	 */
	private String getLibraryPath(PHPVersion version)
	{
		switch (version)
		{
			case PHP4:
				return PHP4_LANGUAGE_LIBRARY_PATH;
			case PHP5:
				return PHP5_LANGUAGE_LIBRARY_PATH;
			case PHP5_3:
				return PHP53_LANGUAGE_LIBRARY_PATH;
			case PHP5_4:
				return PHP54_LANGUAGE_LIBRARY_PATH;
		}
		IdeLog.logError(PHPEditorPlugin.getDefault(), "Unknows PHPVersion " + version); //$NON-NLS-1$
		return null;
	}

	/**
	 * Adds a built-in class, variable or constant to the hash of built-ins. This method will also add the inner
	 * functions and variables inside a given class parse node.
	 * 
	 * @param child
	 *            A PHPClassParseNode or a PHPVariableParseNode (any other type is ignored)
	 * @param url
	 */
	private void addBuiltinClassOrConstant(IParseNode child, URL url)
	{
		if (child instanceof PHPClassParseNode)
		{
			builtInClasses.put(child.getNameNode().getName(), url.toString().intern());
			IParseNode[] children = child.getChildren();
			for (IParseNode node : children)
			{
				if (node instanceof PHPFunctionParseNode || node instanceof PHPVariableParseNode
						|| node instanceof PHPConstantNode)
				{
					builtInClasses.put(child.getNameNode().getName() + IElementsIndex.DELIMITER
							+ node.getNameNode().getName(), url.toString().intern());
				}
			}
		}
		else if (child instanceof PHPConstantNode || child.getNodeType() == IPHPParseNode.KEYWORD_NODE
				|| child.getNodeType() == IPHPParseNode.CONST_NODE)
		{
			addAsKeyword(child, url);
		}
	}

	private void addAsKeyword(IParseNode child, URL url)
	{
		// Convert this PHP constant to a PHP parse node with a Keyword
		// type.
		// Also, make sure that the documentation is providing some basics.
		PHPBaseParseNode phpChild = (PHPBaseParseNode) child;
		IPHPDocBlock documentation = phpChild.getDocumentation();
		if (documentation == null || StringUtil.EMPTY.equals(documentation.getShortDescription()))
		{
			documentation = new PHPDocBlockImp(MessageFormat.format(Messages.PREDEFINED_CONSTANT_LABEL, child
					.getNameNode().getName()), StringUtil.EMPTY, NO_TAGS, 0);
		}
		short type = (child.getNodeType() == IPHPParseNode.KEYWORD_NODE) ? IPHPParseNode.KEYWORD_NODE
				: IPHPParseNode.CONST_NODE;
		PHPBaseParseNode node = new PHPBaseParseNode(type, phpChild.getModifiers(), child.getStartingOffset(),
				child.getEndingOffset(), phpChild.getNameNode().getName());
		node.setDocumentation(documentation);
		builtins.add(node);
		String parentName = (child.getParent() != null) ? child.getParent().getNameNode().getName() : StringUtil.EMPTY;
		if (parentName == null)
		{
			parentName = StringUtil.EMPTY;
		}
		else if (parentName.length() > 0)
		{
			parentName += IElementsIndex.DELIMITER;
		}
		builtInConstants.put(parentName + child.getNameNode().getName(), url.toString().intern());
	}

	/*
	 * Returns the resources URLs that are contained in the given root resource.
	 * @param libraryPath
	 * @return A URL array holding the resources for the children of the root library.
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	private static URL[] getBuiltinsURLs(String libraryPath)
	{
		List<URL> urls = new ArrayList<URL>();
		PHPEplPlugin plugin = PHPEplPlugin.getDefault();
		if (plugin != null)
		{
			try
			{
				Enumeration entries = plugin.getBundle().findEntries(libraryPath, "*.php", true); //$NON-NLS-1$
				while (entries.hasMoreElements())
				{
					urls.add((URL) entries.nextElement());
				}
				return urls.toArray(new URL[urls.size()]);
			}
			catch (IllegalStateException ise)
			{
				// Ignore those, the bundle is probably shutting down.
			}
		}
		return new URL[0];
	}

	/*
	 * Returns a file according to the URL protocol.
	 * @param url
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings("unused")
	private static File getFile(URL url) throws IOException
	{
		if ("file".equals(url.getProtocol())) //$NON-NLS-1$
			return new File(url.getPath());
		if ("jar".equals(url.getProtocol())) { //$NON-NLS-1$
			String path = url.getPath();
			if (path.startsWith("file:")) { //$NON-NLS-1$
				// strip off the file: and the !/
				path = path.substring(5, path.length() - 2);
				return new File(path);
			}
		}
		throw new IOException("Unknown protocol"); //$NON-NLS-1$
	}

	private PHPBuiltins()
	{
		// initiate empty values
		initNames();
	}

	/**
	 * @return instance of built ins
	 */
	public static PHPBuiltins getInstance()
	{
		return instance;
	}

	/**
	 * Clean and recreate the PHP built-ins.
	 * 
	 * @param monitor
	 *            A non null progress monitor.
	 */
	public synchronized void clean(IProgressMonitor monitor)
	{
		long start = System.currentTimeMillis();
		initializing = true;
		initNames();
		this.builtInFunctions = new HashMap<String, String>();
		this.builtInClasses = new HashMap<String, String>();
		this.builtInConstants = new HashMap<String, String>();

		if (monitor == null)
		{
			initializing = false;
			throw new IllegalArgumentException("The progress monitor should not be null"); //$NON-NLS-1$
		}
		if (this.builtins == null)
		{
			initBuiltins(monitor);
		}
		initializing = false;
		IdeLog.logInfo(PHPEditorPlugin.getDefault(), "Built-ins clean: " //$NON-NLS-1$
				+ (System.currentTimeMillis() - start) + "ms", null, PHPEditorPlugin.INDEXER_SCOPE); //$NON-NLS-1$
	}

	private void initNames()
	{
		phpNames = new HashMap<PHPVersion, Set<String>>();
		for (PHPVersion version : EnumSet.allOf(PHPVersion.class))
		{
			phpNames.put(version, new HashSet<String>());
		}
	}

	/**
	 * Returns true if the PHP built-ins are being initialized.
	 * 
	 * @return <code>true</code> if the built-ins are currently initialized. <code>false</code> otherwise.
	 */
	public synchronized boolean isInitializing()
	{
		return initializing;
	}
}
