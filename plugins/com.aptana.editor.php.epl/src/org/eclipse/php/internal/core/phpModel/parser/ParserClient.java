/*******************************************************************************
 * Copyright (c) 2006 Zend Corporation and IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org.eclipse.php.internal.core.phpModel.parser;

import org.eclipse.php.internal.core.phpModel.phpElementData.PHPDocBlock;

/**
 * This is interface is to be used by the PhpParser as it's client.
 * It's functions are hooks from the parser actions to the implemeting class
 * of this interface.
 */
public interface ParserClient {
	
	void dispose();

	void handleFunctionDeclarationStarts(String functionName);

	void handleFunctionDeclaration(String functionName, boolean isClassFunction, int modifier, PHPDocBlock docInfo, int startPosition, int stopPosition, int lineNumber);

	void handleFunctionDeclarationEnds(String functionName, boolean isClassFunction, int endPosition);

	void handleFunctionParameter(String classType, String variableName, boolean isReference, boolean isConst, String defaultValue, int startPosition, int endPosition, int stopPosition, int lineNumber);

	//////////////////////////////////////////////////////////////////////////////////////////////////////

	void hadleClassDeclarationStarts(String className, int startPosition);

	void handleClassDeclaration(String className, int modifier, String superClassName, String interfacesNames, PHPDocBlock docInfo, int startPosition, int stopPosition, int lineNumber);

	void handleClassDeclarationEnds(String className, int endPosition);

	void handleClassVariablesDeclaration(String variables, int modifier, PHPDocBlock docInfo, int startPosition, int endPosition, int stopPosition);

	void handleClassConstDeclaration(String constName, PHPDocBlock docInfo, int startPosition, int endPosition, int stopPosition);

	//////////////////////////////////////////////////////////////////////////////////////////////////////

	void handleIncludedFile(String includingType, String includeFileName, PHPDocBlock docInfo, int startPosition, int endPosition, int stopPosition, int lineNumber);

	void haveReturnValue();

	void handleObjectInstansiation(String variableName, String className, String ctorArrguments, int line, int startPosition, boolean isUserDocumentation);

	void handleVariableName(String variableName, int line);

	void handleGlobalVar(String variableName);

	void handleStaticVar(String variableName);

	void handleDefine(String name, String value, PHPDocBlock docInfo, int startPosition, int endPosition, int stopPosition);

	//////////////////////////////////////////////////////////////////////////////////////////////////////

	void handleError(String description, int startPosition, int endPosition, int lineNumber);

	void handleSyntaxError(int currToken, String currText, short[] rowOfProbe, int startPosition, int endPosition, int lineNumber);

	void handleTask(String taskName, String description, int startPosition, int endPosition, int lineNumber);

	//////////////////////////////////////////////////////////////////////////////////////////////////////

	void handlePHPStart(int startOffset, int endOffset);

	void handlePHPEnd(int startOffset, int endOffset);

	void setFirstDocBlock(PHPDocBlock docBlock);

	//////////////////////////////////////////////////////////////////////////////////////////////////////

	void startParsing(String fileName);

	void finishParsing(int lastPosition, int lastLine, long lastModified);

	///////////////////////////////////////////////////////////////////////////////////////////////////////

}