/**
 * Copyright (c) 2005-2006 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package com.aptana.editor.php.internal.parser2;

import org.eclipse.php.internal.core.phpModel.parser.ParserClient;
import org.eclipse.php.internal.core.phpModel.phpElementData.PHPDocBlock;

/**
 * @author Pavel Petrochenko
 */
public class ParserClientAdapter implements ParserClient
{
	/**
	 * 
	 */

	public ParserClientAdapter()
	{
		super();
	}

	/**
	 * adapter is do not needed more
	 */
	public void dispose()
	{

	}

	/**
	 * @param lastPosition
	 * @param lastLine
	 * @param lastModified
	 */
	public void finishParsing(int lastPosition, int lastLine, long lastModified)
	{

	}

	/**
	 * @param className
	 * @param startPosition
	 */
	public void handleClassDeclarationStarts(String className, int startPosition)
	{

	}

	/**
	 * @param constName
	 * @param docInfo
	 * @param startPosition
	 * @param endPosition
	 * @param stopPosition
	 */
	public void handleClassConstDeclaration(String constName, PHPDocBlock docInfo, int startPosition, int endPosition,
			int stopPosition)
	{

	}

	/**
	 * @param className
	 * @param modifier
	 * @param superClassName
	 * @param interfacesNames
	 * @param docInfo
	 * @param startPosition
	 * @param stopPosition
	 * @param lineNumber
	 */
	public void handleClassDeclaration(String className, int modifier, String superClassName, String interfacesNames,
			PHPDocBlock docInfo, int startPosition, int stopPosition, int lineNumber)
	{

	}

	/**
	 * @param className
	 * @param endPosition
	 */
	public void handleClassDeclarationEnds(String className, int endPosition)
	{

	}

	/**
	 * @param variables
	 * @param modifier
	 * @param docInfo
	 * @param startPosition
	 * @param endPosition
	 * @param stopPosition
	 */
	public void handleClassVariablesDeclaration(String variables, int modifier, PHPDocBlock docInfo, int startPosition,
			int endPosition, int stopPosition)
	{

	}

	/**
	 * @param name
	 * @param value
	 * @param docInfo
	 * @param startPosition
	 * @param endPosition
	 * @param stopPosition
	 */
	public void handleDefine(String name, String value, PHPDocBlock docInfo, int startPosition, int endPosition,
			int stopPosition)
	{

	}

	/**
	 * @param currToken
	 * @param currText
	 * @param rowOfProbe
	 * @param startPosition
	 * @param endPosition
	 * @param lineNumber
	 */
	public void handleSyntaxError(int currToken, String currText, short[] rowOfProbe, int startPosition,
			int endPosition, int lineNumber)
	{
	}

	/**
	 * @param description
	 * @param startPosition
	 * @param endPosition
	 * @param lineNumber
	 */
	public void handleError(String description, int startPosition, int endPosition, int lineNumber)
	{
	}

	/**
	 * @param functionName
	 * @param isClassFunction
	 * @param modifier
	 * @param docInfo
	 * @param startPosition
	 * @param stopPosition
	 * @param lineNumber
	 */
	public void handleFunctionDeclaration(String functionName, boolean isClassFunction, int modifier,
			PHPDocBlock docInfo, int startPosition, int stopPosition, int lineNumber)
	{

	}

	/**
	 * @param functionName
	 * @param isClassFunction
	 * @param endPosition
	 */
	public void handleFunctionDeclarationEnds(String functionName, boolean isClassFunction, int endPosition)
	{

	}

	/**
	 * @param functionName
	 */
	public void handleFunctionDeclarationStarts(String functionName)
	{

	}

	/**
	 * @param classType
	 * @param variableName
	 * @param isReference
	 * @param isConst
	 * @param defaultValue
	 * @param startPosition
	 * @param endPosition
	 * @param stopPosition
	 * @param lineNumber
	 */
	public void handleFunctionParameter(String classType, String variableName, boolean isReference, boolean isConst,
			String defaultValue, int startPosition, int endPosition, int stopPosition, int lineNumber)
	{

	}

	/**
	 * @param variableName
	 */
	public void handleGlobalVar(String variableName)
	{

	}

	/**
	 * @param includingType
	 * @param includeFileName
	 * @param docInfo
	 * @param startPosition
	 * @param endPosition
	 * @param stopPosition
	 * @param lineNumber
	 */
	public void handleIncludedFile(String includingType, String includeFileName, PHPDocBlock docInfo,
			int startPosition, int endPosition, int stopPosition, int lineNumber)
	{

	}

	/**
	 * @param variableName
	 * @param className
	 * @param ctorArrguments
	 * @param line
	 * @param startPosition
	 * @param isUserDocumentation
	 */
	public void handleObjectInstansiation(String variableName, String className, String ctorArrguments, int line,
			int startPosition, boolean isUserDocumentation)
	{

	}

	/**
	 * @param startOffset
	 * @param endOffset
	 */
	public void handlePHPEnd(int startOffset, int endOffset)
	{

	}

	/**
	 * @param startOffset
	 * @param endOffset
	 */
	public void handlePHPStart(int startOffset, int endOffset)
	{

	}

	/**
	 * @param variableName
	 */
	public void handleStaticVar(String variableName)
	{

	}

	/**
	 * @param taskName
	 * @param description
	 * @param startPosition
	 * @param endPosition
	 * @param lineNumber
	 */
	public void handleTask(String taskName, String description, int startPosition, int endPosition, int lineNumber)
	{

	}

	/**
	 * @param variableName
	 * @param line
	 */
	public void handleVariableName(String variableName, int line)
	{

	}

	/**
	 * 
	 */
	public void haveReturnValue()
	{

	}

	/**
	 * @param docBlock
	 */
	public void setFirstDocBlock(PHPDocBlock docBlock)
	{

	}

	/**
	 * @param fileName
	 */
	public void startParsing(String fileName)
	{

	}

}