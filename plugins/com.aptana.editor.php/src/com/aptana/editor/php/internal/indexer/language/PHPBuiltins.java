/**
 * Copyright (c) 2005-2006 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.php.internal.core.PHPVersion;
import org.eclipse.php.internal.core.documentModel.phpElementData.IPHPDocBlock;
import org.eclipse.php.internal.core.documentModel.phpElementData.IPHPDocTag;
import org.eclipse.php.internal.core.documentModel.phpElementData.PHPDocBlockImp;

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

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	public static final String LANGUAGE_LIBRARY_PATH_BASE = "Resources/language/php"; //$NON-NLS-1$
	public static final String PHP4_LANGUAGE_LIBRARY_PATH = LANGUAGE_LIBRARY_PATH_BASE + "4"; //$NON-NLS-1$
	public static final String PHP5_LANGUAGE_LIBRARY_PATH = LANGUAGE_LIBRARY_PATH_BASE + "5"; //$NON-NLS-1$
	public static final String PHP53_LANGUAGE_LIBRARY_PATH = LANGUAGE_LIBRARY_PATH_BASE + "5.3"; //$NON-NLS-1$

	private static final int INITIAL_CAPACITY = 5000;
	private static final IPHPDocTag[] NO_TAGS = new IPHPDocTag[0];
	private static final PHPBuiltins instance = new PHPBuiltins();

	private Object mutex = new Object();

	private HashSet<Object> php4Names = new HashSet<Object>();
	private HashSet<Object> php5Names = new HashSet<Object>();
	private HashSet<Object> php53Names = new HashSet<Object>();
	// Holds a function name map to the resource name that contains it
	private HashMap<String, String> builtInFunctions = new HashMap<String, String>();
	// Holds a Class/Constant name map to the resource name that contains it
	private HashMap<String, String> builtInClasses = new HashMap<String, String>();
	private HashMap<String, String> builtInConstants = new HashMap<String, String>();

	private TreeSet<Object> builtins;
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
		addKeyword("abstract", "abstract"); //$NON-NLS-1$ //$NON-NLS-2$
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
		addKeyword("final", "final"); //$NON-NLS-1$ //$NON-NLS-2$
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
		addKeyword("interface", "interface"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("isset", "isset"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("list", "list"); //$NON-NLS-1$ //$NON-NLS-2$
		addBuiltin("and", "LOGICAL_AND", "Logical and"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		addBuiltin("or", "LOGICAL_OR", "Logical or"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		addBuiltin("xor", "LOGICAL_XOR", "Logical xor"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		addKeyword("new", "new"); //$NON-NLS-1$ //$NON-NLS-2$
		addBuiltin("KEYWORD", "OBJECT_CAST", "Object Cast"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		addKeyword("print", "print"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("private", "private"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("protected", "protected"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("public", "public"); //$NON-NLS-1$ //$NON-NLS-2$
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
		addKeyword("use", "use"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("var", "var"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("while", "while"); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("namespace", "namespace", PHPVersion.PHP5_3); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("goto", "goto", PHPVersion.PHP5_3); //$NON-NLS-1$ //$NON-NLS-2$
		addKeyword("use", "use", PHPVersion.PHP5_3); //$NON-NLS-1$ //$NON-NLS-2$
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
	 * Adds built-in.
	 * 
	 * @param string
	 * @param nodeName
	 * @param description
	 * @param phpVersions
	 *            An optional array of supported php-versions. In case none is passed, the built-in will be added to all
	 *            versions
	 */
	private void addBuiltin(String string, String nodeName, String description, PHPVersion... phpVersions)
	{
		PHPBaseParseNode node = new PHPBaseParseNode(IPHPParseNode.KEYWORD_NODE, 0, -1, -1, nodeName);
		builtins.add(node);
		node.setDocumentation(new PHPDocBlockImp(description, EMPTY_STRING, NO_TAGS, 0));
		if (phpVersions != null && phpVersions.length > 0)
		{
			for (PHPVersion version : phpVersions)
			{
				if (version == PHPVersion.PHP5_3)
				{
					php53Names.add(string);
				}
				else if (version == PHPVersion.PHP5)
				{
					php5Names.add(string);
				}
				else
				{
					php4Names.add(string);
				}
			}
		}
		else
		{
			// add to all versions
			php53Names.add(string);
			php5Names.add(string);
			php4Names.add(string);
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
		addBuiltin(string, nodeName, MessageFormat.format(Messages.SUPERGLOBAL_LABEL, nodeName));
	}

	/**
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
	 * @param func
	 * @return is this function exists in php4 built ins
	 */
	public boolean existsInPHP4(IPHPParseNode func)
	{
		if (func.getClass() == PHPBaseParseNode.class)
		{
			if (func.getNodeName().equals("namespace") || func.getNodeName().equals("using") || func.getNodeName().equals("goto")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			{
				return false;
			}
		}
		return php4Names.contains(func.getNodeName());
	}

	/**
	 * @param func
	 * @return is this function exists in php5 built ins
	 */
	public boolean existsInPHP5(IPHPParseNode func)
	{
		return php5Names.contains(func.getNodeName());
	}

	/**
	 * @param func
	 * @return is this function exists in php5.3 built ins
	 */
	public boolean existsInPHP53(IPHPParseNode func)
	{
		return php53Names.contains(func.getNodeName());
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
				PHPEditorPlugin.logError("Error retrieving the built-in resource.", e); //$NON-NLS-1$
			}

		}
		return null;
	}

	private void initBuiltins(IProgressMonitor monitor)
	{
		try
		{
			if (PHPEditorPlugin.DEBUG || PHPEditorPlugin.INDEXER_DEBUG)
			{
				System.out.println("Indexing the PHP API libraries..."); //$NON-NLS-1$
			}
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
					int res = node0.getNodeName().compareTo(node1.getNodeName());
					if (res == 0)
					{
						res = 0;
					}
					return res;
				}
			});
			HashMap<Object, Object> builtins = new HashMap<Object, Object>(INITIAL_CAPACITY);
			monitor.setTaskName(Messages.PHPBuiltins_addingPhp4);
			PHPParser parser = new PHPParser(PHPVersion.PHP4);
			long timeMillis = System.currentTimeMillis();
			initPHP4Builtins(parser, builtins);
			if (PHPEditorPlugin.DEBUG)
			{
				System.out.println("Parsed PHP4 built-ins (" + (System.currentTimeMillis() - timeMillis) + "ms)"); //$NON-NLS-1$ //$NON-NLS-2$
				timeMillis = System.currentTimeMillis();
			}
			monitor.setTaskName(Messages.PHPBuiltins_addingPhp5);
			parser = new PHPParser(PHPVersion.PHP5);
			initPHP5Builtins(parser, builtins);
			if (PHPEditorPlugin.DEBUG)
			{
				System.out.println("Parsed PHP5 built-ins (" + (System.currentTimeMillis() - timeMillis) + "ms)"); //$NON-NLS-1$ //$NON-NLS-2$
				timeMillis = System.currentTimeMillis();
			}
			monitor.setTaskName(Messages.PHPBuiltins_addingPhp53);
			parser = new PHPParser(PHPVersion.PHP5_3);
			initPHP53Builtins(parser, builtins);
			if (PHPEditorPlugin.DEBUG)
			{
				System.out.println("Parsed PHP53 built-ins (" + (System.currentTimeMillis() - timeMillis) + "ms)"); //$NON-NLS-1$ //$NON-NLS-2$
				timeMillis = System.currentTimeMillis();
			}
			this.builtins.addAll(builtins.values());
			/*
			 * // Keep that block to generate the php lexer types when needed. Iterator<Object> iterator =
			 * this.builtins.iterator(); int count = 0; while (iterator.hasNext()) { Object obj = iterator.next(); if
			 * (!(obj instanceof PHPParseNode)) { continue; } int typeIndex = ((PHPParseNode)obj).getTypeIndex(); if
			 * (typeIndex == PHPParseNode.KEYWORD_NODE || typeIndex == PHPParseNode.CONST_NODE || obj instanceof
			 * PHPConstantNode) { String nodeName = ((PHPParseNode)obj).getNodeName(); String string =
			 * builtInClasses.get(nodeName); if (string != null && (string.endsWith("basic.php") ||
			 * string.endsWith("standard.php"))) { System.out.println("<string case-insensitive=\"true\">"
			 * +nodeName+"</string>"); count++; } } }
			 */
			addKeywords();
			if (PHPEditorPlugin.DEBUG)
			{
				System.out.println("Loaded all built-ins (" + (System.currentTimeMillis() - timeMillis) + "ms)"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		catch (Throwable t)
		{
			PHPEditorPlugin.logError(t);
		}
	}

	private void initPHP4Builtins(PHPParser parser, HashMap<Object, Object> builtins)
	{
		try
		{
			URL[] urls = getBuiltinsURLs(PHP4_LANGUAGE_LIBRARY_PATH);
			for (URL url : urls)
			{
				try
				{
					IParseNode php4 = parser.parse(url.openStream());
					for (int a = 0; a < php4.getChildCount(); a++)
					{
						IParseNode child = (IParseNode) php4.getChild(a);
						String name = child.getNameNode().getName().intern();
						php4Names.add(name);
						if (child instanceof PHPFunctionParseNode)
						{
							builtInFunctions.put(name, url.toString().intern());
						}
						else
						{
							addBuiltinClassOrConstant(child, url);
						}
						// Since the constant nodes are inserted to the
						// built-ins directly, don't deal with them here.
						// (they were already dealt with on the
						// addBuiltinClassOrConstant call above)
						if (!(child instanceof PHPConstantNode))
						{
							builtins.put(name, child);
						}
					}
				}
				catch (Exception e)
				{
					PHPEditorPlugin.logError("Error loading the built-in PHP API for " + url.getFile(), e); //$NON-NLS-1$
				}
			}
		}
		catch (IOException ioe)
		{
			PHPEditorPlugin.logError("Error loading the built-in PHP API.", ioe); //$NON-NLS-1$
		}
	}

	private void initPHP5Builtins(PHPParser parser, HashMap<Object, Object> builtins)
	{
		try
		{
			URL[] urls = getBuiltinsURLs(PHP5_LANGUAGE_LIBRARY_PATH);
			for (URL url : urls)
			{
				try
				{
					IParseNode php5 = parser.parse(url.openStream());
					for (int a = 0; a < php5.getChildCount(); a++)
					{
						IParseNode child = (IParseNode) php5.getChild(a);
						String name = child.getNameNode().getName().intern();
						php5Names.add(name);
						if (child instanceof PHPFunctionParseNode)
						{
							builtInFunctions.put(name, url.toString().intern());
						}
						else
						{
							addBuiltinClassOrConstant(child, url);
						}
						// Since the constant nodes are inserted to the
						// built-ins directly, don't deal with them here.
						// (they were already dealt with on the
						// addBuiltinClassOrConstant call above)
						if (!(child instanceof PHPConstantNode))
						{
							builtins.put(name, child);
						}
					}
				}
				catch (Exception e)
				{
					PHPEditorPlugin.logError("Error loading the built-in PHP API for " + url.getFile(), //$NON-NLS-1$
							e);
				}
			}
		}
		catch (IOException ioe)
		{
			PHPEditorPlugin.logError("Error loading the built-in PHP API", ioe); //$NON-NLS-1$
		}
	}

	private void initPHP53Builtins(PHPParser parser, HashMap<Object, Object> builtins)
	{
		try
		{
			URL[] urls = getBuiltinsURLs(PHP53_LANGUAGE_LIBRARY_PATH);
			for (URL url : urls)
			{
				try
				{
					IParseNode php53 = parser.parse(url.openStream());
					for (int a = 0; a < php53.getChildCount(); a++)
					{
						IParseNode child = (IParseNode) php53.getChild(a);
						String name = child.getNameNode().getName().intern();
						php53Names.add(name);
						if (child instanceof PHPFunctionParseNode)
						{
							builtInFunctions.put(name, url.toString().intern());
						}
						else
						{
							addBuiltinClassOrConstant(child, url);
						}
						// Since the constant nodes are inserted to the
						// built-ins directly, don't deal with them here.
						// (they were already dealt with on the
						// addBuiltinClassOrConstant call above)
						if (!(child instanceof PHPConstantNode))
						{
							builtins.put(name, child);
						}
					}
				}
				catch (Exception e)
				{
					PHPEditorPlugin.logError("Error loading the built-in PHP API for " //$NON-NLS-1$
							+ url.getFile(), e);
				}
			}
		}
		catch (IOException ioe)
		{
			PHPEditorPlugin.logError("Error loading the built-in PHP API for " //$NON-NLS-1$
					, ioe);
		}
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
				if (node instanceof PHPFunctionParseNode || node instanceof PHPVariableParseNode)
				{
					builtInClasses.put(child.getNameNode().getName() + IElementsIndex.DELIMITER
							+ node.getNameNode().getName(), url.toString().intern());
				}
			}
		}
		else if (child instanceof PHPConstantNode || child.getNodeType() == IPHPParseNode.KEYWORD_NODE
				|| child.getNodeType() == IPHPParseNode.CONST_NODE)
		{
			// Convert this PHP constant to a PHP parse node with a Keyword
			// type.
			// Also, make sure that the documentation is providing some basics.
			PHPBaseParseNode phpChild = (PHPBaseParseNode) child;
			IPHPDocBlock documentation = phpChild.getDocumentation();
			@SuppressWarnings("unused")
			boolean docsFromBuiltinSource = true;
			if (documentation == null || EMPTY_STRING.equals(documentation.getShortDescription()))
			{
				docsFromBuiltinSource = false;
				documentation = new PHPDocBlockImp(MessageFormat.format(Messages.PREDEFINED_CONSTANT_LABEL, child
						.getNameNode().getName()), EMPTY_STRING, NO_TAGS, 0);
			}
			PHPBaseParseNode node = new PHPBaseParseNode(IPHPParseNode.KEYWORD_NODE, phpChild.getModifiers(), child
					.getStartingOffset(), child.getEndingOffset(), phpChild.getNameNode().getName());
			node.setDocumentation(documentation);
			builtins.add(node);
			// if (docsFromBuiltinSource)
			// {
			builtInConstants.put(child.getNameNode().getName(), url.toString().intern());
			// }
		}
	}

	/*
	 * Returns the resources URLs that are contained in the given root resource.
	 * @param libraryPath
	 * @return A URL array holding the resources for the children of the root library.
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	private static URL[] getBuiltinsURLs(String libraryPath) throws IOException
	{
		List<URL> urls = new ArrayList<URL>();
		Enumeration entries = PHPEplPlugin.getDefault().getBundle().findEntries(libraryPath, "*.php", true); //$NON-NLS-1$
		while (entries.hasMoreElements())
		{
			urls.add((URL) entries.nextElement());
		}
		return urls.toArray(new URL[urls.size()]);
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
		this.php4Names = new HashSet<Object>();
		this.php5Names = new HashSet<Object>();
		this.php53Names = new HashSet<Object>();
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
		if (PHPEditorPlugin.INDEXER_DEBUG)
		{
			System.out.println("Built-ins clean: " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

}
