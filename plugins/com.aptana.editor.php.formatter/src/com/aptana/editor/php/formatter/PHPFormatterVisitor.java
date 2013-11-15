/**
 * Aptana Studio
 * Copyright (c) 2005-2013 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.formatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.IRegion;
import org2.eclipse.php.core.compiler.PHPFlags;
import org2.eclipse.php.internal.core.ast.nodes.ASTError;
import org2.eclipse.php.internal.core.ast.nodes.ASTNode;
import org2.eclipse.php.internal.core.ast.nodes.ArrayAccess;
import org2.eclipse.php.internal.core.ast.nodes.ArrayCreation;
import org2.eclipse.php.internal.core.ast.nodes.ArrayElement;
import org2.eclipse.php.internal.core.ast.nodes.Assignment;
import org2.eclipse.php.internal.core.ast.nodes.BackTickExpression;
import org2.eclipse.php.internal.core.ast.nodes.Block;
import org2.eclipse.php.internal.core.ast.nodes.BreakStatement;
import org2.eclipse.php.internal.core.ast.nodes.CastExpression;
import org2.eclipse.php.internal.core.ast.nodes.CatchClause;
import org2.eclipse.php.internal.core.ast.nodes.ChainingInstanceCall;
import org2.eclipse.php.internal.core.ast.nodes.ClassDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.ClassInstanceCreation;
import org2.eclipse.php.internal.core.ast.nodes.ClassName;
import org2.eclipse.php.internal.core.ast.nodes.CloneExpression;
import org2.eclipse.php.internal.core.ast.nodes.Comment;
import org2.eclipse.php.internal.core.ast.nodes.ConditionalExpression;
import org2.eclipse.php.internal.core.ast.nodes.ConstantDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.ContinueStatement;
import org2.eclipse.php.internal.core.ast.nodes.DeclareStatement;
import org2.eclipse.php.internal.core.ast.nodes.DereferenceNode;
import org2.eclipse.php.internal.core.ast.nodes.DoStatement;
import org2.eclipse.php.internal.core.ast.nodes.EchoStatement;
import org2.eclipse.php.internal.core.ast.nodes.EmptyStatement;
import org2.eclipse.php.internal.core.ast.nodes.Expression;
import org2.eclipse.php.internal.core.ast.nodes.ExpressionStatement;
import org2.eclipse.php.internal.core.ast.nodes.FieldAccess;
import org2.eclipse.php.internal.core.ast.nodes.FieldsDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.ForEachStatement;
import org2.eclipse.php.internal.core.ast.nodes.ForStatement;
import org2.eclipse.php.internal.core.ast.nodes.FormalParameter;
import org2.eclipse.php.internal.core.ast.nodes.FullyQualifiedTraitMethodReference;
import org2.eclipse.php.internal.core.ast.nodes.FunctionDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.FunctionInvocation;
import org2.eclipse.php.internal.core.ast.nodes.FunctionName;
import org2.eclipse.php.internal.core.ast.nodes.GlobalStatement;
import org2.eclipse.php.internal.core.ast.nodes.GotoLabel;
import org2.eclipse.php.internal.core.ast.nodes.GotoStatement;
import org2.eclipse.php.internal.core.ast.nodes.Identifier;
import org2.eclipse.php.internal.core.ast.nodes.IfStatement;
import org2.eclipse.php.internal.core.ast.nodes.IgnoreError;
import org2.eclipse.php.internal.core.ast.nodes.InLineHtml;
import org2.eclipse.php.internal.core.ast.nodes.Include;
import org2.eclipse.php.internal.core.ast.nodes.InfixExpression;
import org2.eclipse.php.internal.core.ast.nodes.InstanceOfExpression;
import org2.eclipse.php.internal.core.ast.nodes.InterfaceDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.LambdaFunctionDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.ListVariable;
import org2.eclipse.php.internal.core.ast.nodes.MethodDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.MethodInvocation;
import org2.eclipse.php.internal.core.ast.nodes.NamespaceDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.NamespaceName;
import org2.eclipse.php.internal.core.ast.nodes.PHPArrayDereferenceList;
import org2.eclipse.php.internal.core.ast.nodes.ParenthesisExpression;
import org2.eclipse.php.internal.core.ast.nodes.PostfixExpression;
import org2.eclipse.php.internal.core.ast.nodes.PrefixExpression;
import org2.eclipse.php.internal.core.ast.nodes.Quote;
import org2.eclipse.php.internal.core.ast.nodes.Reference;
import org2.eclipse.php.internal.core.ast.nodes.ReflectionVariable;
import org2.eclipse.php.internal.core.ast.nodes.ReturnStatement;
import org2.eclipse.php.internal.core.ast.nodes.Scalar;
import org2.eclipse.php.internal.core.ast.nodes.Statement;
import org2.eclipse.php.internal.core.ast.nodes.StaticConstantAccess;
import org2.eclipse.php.internal.core.ast.nodes.StaticFieldAccess;
import org2.eclipse.php.internal.core.ast.nodes.StaticMethodInvocation;
import org2.eclipse.php.internal.core.ast.nodes.StaticStatement;
import org2.eclipse.php.internal.core.ast.nodes.SwitchCase;
import org2.eclipse.php.internal.core.ast.nodes.SwitchStatement;
import org2.eclipse.php.internal.core.ast.nodes.ThrowStatement;
import org2.eclipse.php.internal.core.ast.nodes.TraitAlias;
import org2.eclipse.php.internal.core.ast.nodes.TraitDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.TraitPrecedence;
import org2.eclipse.php.internal.core.ast.nodes.TraitStatement;
import org2.eclipse.php.internal.core.ast.nodes.TraitUseStatement;
import org2.eclipse.php.internal.core.ast.nodes.TryStatement;
import org2.eclipse.php.internal.core.ast.nodes.TypeDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.UnaryOperation;
import org2.eclipse.php.internal.core.ast.nodes.UseStatement;
import org2.eclipse.php.internal.core.ast.nodes.UseStatementPart;
import org2.eclipse.php.internal.core.ast.nodes.Variable;
import org2.eclipse.php.internal.core.ast.nodes.VariableBase;
import org2.eclipse.php.internal.core.ast.nodes.WhileStatement;
import org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor;

import com.aptana.core.util.StringUtil;
import com.aptana.editor.php.formatter.nodes.FormatterPHPArrayElementNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPBlockNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPBreakNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPCaseBodyNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPCaseColonNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPDeclarationNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPElseIfNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPElseNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPExcludedTextNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPExpressionWrapperNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPFunctionBodyNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPFunctionInvocationNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPHeredocNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPIfNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPImplicitBlockNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPKeywordNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPLineStartingNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPNamespaceBlockNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPNonBlockedWhileNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPOperatorNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPParenthesesNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPPunctuationNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPSwitchNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPTextNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPTraitPrecedenceWrapperNode;
import com.aptana.editor.php.formatter.nodes.FormatterPHPTypeBodyNode;
import com.aptana.editor.php.internal.indexer.PHPDocUtils;
import com.aptana.formatter.FormatterDocument;
import com.aptana.formatter.FormatterUtils;
import com.aptana.formatter.nodes.AbstractFormatterNodeBuilder;
import com.aptana.formatter.nodes.IFormatterContainerNode;
import com.aptana.formatter.nodes.NodeTypes.TypeBracket;
import com.aptana.formatter.nodes.NodeTypes.TypeOperator;
import com.aptana.formatter.nodes.NodeTypes.TypePunctuation;

/**
 * A PHP formatter node builder.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPFormatterVisitor extends AbstractVisitor
{

	// Match words in a string
	private static final Pattern WORD_PATTERN = Pattern.compile("\\w+"); //$NON-NLS-1$
	private static final Pattern LINE_SPLIT_PATTERN = Pattern.compile("\r?\n|\r"); //$NON-NLS-1$
	public static final String INVOCATION_ARROW = "->"; //$NON-NLS-1$
	public static final String STATIC_INVOCATION = "::"; //$NON-NLS-1$
	private static final char[] SEMICOLON_AND_COLON = new char[] { ';', ',' };
	private static final char[] SEMICOLON = new char[] { ';' };

	private FormatterDocument document;
	private PHPFormatterNodeBuilder builder;
	private Set<Integer> multiLinecommentsEndOffsets;
	private Set<Integer> singleLinecommentsEndOffsets;
	private List<IRegion> onOffRegions;
	private List<Comment> comments;

	/**
	 * @param builder
	 * @param document
	 * @param comments
	 */
	public PHPFormatterVisitor(FormatterDocument document, PHPFormatterNodeBuilder builder, List<Comment> comments)
	{
		this.document = document;
		this.builder = builder;
		processComments(comments);
	}

	/**
	 * Collect the comments ending offsets (including the white-spaces that appear after them). Also, in case a
	 * formatter on/off tag was set, process the comments content to exclude some of the source from formatting.
	 * 
	 * @param comments
	 */
	private void processComments(List<Comment> comments)
	{
		this.comments = comments;
		multiLinecommentsEndOffsets = new HashSet<Integer>();
		singleLinecommentsEndOffsets = new HashSet<Integer>();
		if (comments == null)
		{
			return;
		}
		boolean onOffEnabled = document.getBoolean(PHPFormatterConstants.FORMATTER_OFF_ON_ENABLED);
		LinkedHashMap<Integer, String> commentsMap = onOffEnabled ? new LinkedHashMap<Integer, String>(comments.size())
				: null;
		for (Comment comment : comments)
		{
			int commentType = comment.getCommentType();
			int end = comment.getEnd();
			if (commentType == Comment.TYPE_SINGLE_LINE)
			{
				singleLinecommentsEndOffsets.add(builder.getNextNonWhiteCharOffset(document, end));
			}
			else if (commentType == Comment.TYPE_MULTILINE || commentType == Comment.TYPE_PHPDOC)
			{
				multiLinecommentsEndOffsets.add(builder.getNextNonWhiteCharOffset(document, end));
			}
			// Add to the map of comments when the On-Off is enabled.
			if (onOffEnabled)
			{
				int start = comment.getStart();
				String commentStr = document.get(start, end);
				commentsMap.put(start, commentStr);
			}
		}
		// Generate the On-Off regions
		if (onOffEnabled && !commentsMap.isEmpty())
		{
			Pattern onPattern = Pattern.compile(Pattern.quote(document.getString(PHPFormatterConstants.FORMATTER_ON)));
			Pattern offPattern = Pattern
					.compile(Pattern.quote(document.getString(PHPFormatterConstants.FORMATTER_OFF)));
			onOffRegions = FormatterUtils.resolveOnOffRegions(commentsMap, onPattern, offPattern,
					document.getLength() - 1);
		}
	}

	/**
	 * Returns the On-Off formatting regions, as detected from the comments.<br>
	 * In case the formatter preferences have this option disabled, or in case there are no On-Off regions, the returned
	 * list is <code>null</code>.
	 * 
	 * @return A {@link List} that hold the regions that should be skipped when formatting the source; Null, in case
	 *         there are no on-off regions.
	 */
	public List<IRegion> getOnOffRegions()
	{
		return onOffRegions;
	}

	/**
	 * Returns true if there is a any type of comment right before the given element.<br>
	 * There should be only whitespaces between the given offset and the comment.
	 * 
	 * @param offset
	 * @return True, if the given offset is right after a comment.
	 */
	private boolean hasAnyCommentBefore(int offset)
	{
		return multiLinecommentsEndOffsets.contains(offset) || singleLinecommentsEndOffsets.contains(offset);
	}

	/**
	 * Returns true if there is a multi-line comment right before the given element.<br>
	 * There should be only whitespaces between the given offset and the comment.
	 * 
	 * @param offset
	 * @return True, if the given offset is right after a comment.
	 */
	private boolean hasMultiLineCommentBefore(int offset)
	{
		return multiLinecommentsEndOffsets.contains(offset);
	}

	/**
	 * Returns true if there is a single-line comment right before the given element.<br>
	 * There should be only whitespaces between the given offset and the comment.
	 * 
	 * @param offset
	 * @return True, if the given offset is right after a comment.
	 */
	private boolean hasSingleLineCommentBefore(int offset)
	{
		return singleLinecommentsEndOffsets.contains(offset);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.IfStatement
	 * )
	 */
	@Override
	public boolean visit(IfStatement ifStatement)
	{
		Statement falseStatement = ifStatement.getFalseStatement();
		Statement trueStatement = ifStatement.getTrueStatement();

		boolean isEmptyFalseBlock = (falseStatement == null);
		boolean hasTrueBlock = (trueStatement.getType() == ASTNode.BLOCK);
		boolean hasFalseBlock = (!isEmptyFalseBlock && falseStatement.getType() == ASTNode.BLOCK);
		// First, construct the if condition node
		int start = ifStatement.getStart();
		FormatterPHPIfNode conditionNode = new FormatterPHPIfNode(document, hasTrueBlock, ifStatement);
		int startLength = (document.charAt(start) == 'e') ? 6 : 2;
		// If the expression starts with an 'e', it's an "elseif" expression. Otherwise, it's an "if" expression.
		conditionNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, start, start + startLength));
		builder.push(conditionNode);
		// push the condition elements that appear in parentheses
		pushNodeInParentheses('(', ')', start + startLength, trueStatement.getStart(), ifStatement.getCondition(),
				TypeBracket.CONDITIONAL_PARENTHESIS);
		// Construct the 'true' part of the 'if' and visit its children
		if (hasTrueBlock)
		{
			visitBlockNode((Block) trueStatement, ifStatement, isEmptyFalseBlock);
		}
		else
		{
			// Wrap with an empty block node and visit the children
			wrapInImplicitBlock(trueStatement, false);
		}
		builder.checkedPop(conditionNode, trueStatement.getEnd());

		if (!isEmptyFalseBlock)
		{
			// Construct the 'false' part if exist.
			// Note that the PHP parser does not provide us with the start offset of the 'else' keyword, so we need
			// to locate it in between the end of the 'true' block and the begin of the 'false' block.
			// However, in case we have an 'elseif' case, the offset of the false block points to the start of the
			// 'elseif' word.
			int trueBlockEnd = trueStatement.getEnd();
			int falseBlockStart = falseStatement.getStart();
			String segment = (trueBlockEnd != falseBlockStart) ? document.get(trueBlockEnd, falseBlockStart)
					: StringUtil.EMPTY;
			int elsePos = segment.toLowerCase().indexOf("else"); //$NON-NLS-1$
			boolean isElseIf = (falseStatement.getType() == ASTNode.IF_STATEMENT);
			boolean isConnectedElsif = (isElseIf && elsePos < 0);
			FormatterPHPElseNode elseNode = null;
			if (!isConnectedElsif)
			{
				int elseBlockStart = elsePos + trueBlockEnd;
				int elseBlockDeclarationEnd = elseBlockStart + 4; // +4 for the keyword 'else'
				elseNode = new FormatterPHPElseNode(document, hasFalseBlock, isElseIf, hasTrueBlock,
						hasAnyCommentBefore(elseBlockStart));
				elseNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, elseBlockStart,
						elseBlockDeclarationEnd));
				builder.push(elseNode);
			}
			if (!isConnectedElsif && hasFalseBlock)
			{
				visitBlockNode((Block) falseStatement, ifStatement, true);
			}
			else
			{
				if (isElseIf)
				{
					// Wrap the incoming 'if' with an Else-If node that will allow us later to break it and indent
					// it.
					FormatterPHPElseIfNode elseIfNode = new FormatterPHPElseIfNode(document,
							hasAnyCommentBefore(falseBlockStart));
					elseIfNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, falseBlockStart,
							falseBlockStart));
					builder.push(elseIfNode);
					falseStatement.accept(this);
					int falseBlockEnd = falseStatement.getEnd();
					builder.checkedPop(elseIfNode, falseBlockEnd);
					int end = elseIfNode.getEndOffset();
					elseIfNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, end, end));
				}
				else
				{
					// Wrap with an empty block node and visit the children
					wrapInImplicitBlock(falseStatement, false);
				}
			}
			if (elseNode != null)
			{
				builder.checkedPop(elseNode, falseStatement.getEnd());
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.ArrayAccess
	 * )
	 */
	@Override
	public boolean visit(ArrayAccess arrayAccess)
	{
		Expression index = arrayAccess.getIndex();
		VariableBase name = arrayAccess.getName();
		name.accept(this);
		if (arrayAccess.getArrayType() == ArrayAccess.VARIABLE_HASHTABLE)
		{
			// push a curly brackets and visit the index
			pushNodeInParentheses('{', '}', name.getEnd(), arrayAccess.getEnd(), index, TypeBracket.ARRAY_CURLY);
		}
		else
		{
			// push a square brackets and visit the index
			pushNodeInParentheses('[', ']', name.getEnd(), arrayAccess.getEnd(), index, TypeBracket.ARRAY_SQUARE);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * ArrayCreation )
	 */
	@Override
	public boolean visit(ArrayCreation arrayCreation)
	{
		// PHP 5.4 introduced a short-syntax for array creations.
		// for example: $args = ['superman', 'foo' => 'bar', 'batman', 'spiderman'];
		boolean isShortSyntax = !arrayCreation.isHasArrayKey();
		// we need to make sure we do not add a new line in front of the 'array' in some cases,
		// therefore, we push a common declaration. We set the 'hasBlockedBody' to true to avoid
		// indentation.
		int declarationEndOffset = arrayCreation.getStart();
		if (!isShortSyntax)
		{
			declarationEndOffset += 5;
			visitCommonDeclaration(arrayCreation, declarationEndOffset, true);
		}
		List<ArrayElement> elements = arrayCreation.elements();
		// It's possible to have an extra comma at the end of the array creation. This comma is not
		// included in the elements given to us by the arrayCreation so we have to look for it by passing 'true'
		// as the value of 'lookForExtraComma'.
		TypeBracket bracketType = isShortSyntax ? TypeBracket.ARRAY_SQUARE : TypeBracket.ARRAY_PARENTHESIS;
		pushParametersInParentheses(declarationEndOffset, arrayCreation.getEnd(), elements,
				TypePunctuation.ARRAY_COMMA, true, bracketType, false);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * ArrayElement )
	 */
	@Override
	public boolean visit(ArrayElement arrayElement)
	{
		Expression key = arrayElement.getKey();
		Expression value = arrayElement.getValue();
		List<ASTNode> leftNodes = new ArrayList<ASTNode>(1);
		List<ASTNode> rightNodes = null;
		if (key == null)
		{
			leftNodes.add(value);
		}
		else
		{
			leftNodes.add(key);
			rightNodes = new ArrayList<ASTNode>(1);
			rightNodes.add(value);
		}
		ArrayCreation parent = (ArrayCreation) arrayElement.getParent();
		boolean hasSingleElement = parent.elements().size() == 1;
		FormatterPHPArrayElementNode arrayElementNode = new FormatterPHPArrayElementNode(document, hasSingleElement,
				hasAnyCommentBefore(arrayElement.getStart()));
		arrayElementNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, arrayElement.getStart(),
				arrayElement.getStart()));
		builder.push(arrayElementNode);
		visitNodeLists(leftNodes, rightNodes, TypeOperator.KEY_VALUE, null);
		arrayElementNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, arrayElement.getEnd(),
				arrayElement.getEnd()));
		builder.checkedPop(arrayElementNode, -1);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.Assignment
	 * )
	 */
	@Override
	public boolean visit(Assignment assignment)
	{
		VariableBase leftHandSide = assignment.getLeftHandSide();
		Expression rightHandSide = assignment.getRightHandSide();
		String operationString = assignment.getOperationString();
		visitLeftRightExpression(assignment, leftHandSide, rightHandSide, operationString);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * FormalParameter)
	 */
	@Override
	public boolean visit(FormalParameter formalParameter)
	{
		Expression parameterName = formalParameter.getParameterName();
		Expression parameterType = formalParameter.getParameterType();
		if (parameterType != null)
		{
			if (parameterType.getType() == ASTNode.IDENTIFIER)
			{
				visit((Identifier) parameterType);
			}
			else if (parameterType.getType() == ASTNode.NAMESPACE_NAME)
			{
				visit((NamespaceName) parameterType);
			}
			visitTextNode(parameterName, true, 1);
		}
		else
		{
			parameterName.accept(this);
		}
		Expression defaultValue = formalParameter.getDefaultValue();
		if (defaultValue != null)
		{
			// locate the '=' operator and push it before visiting the defaultValue
			int assignmentOffset = builder.getNextNonWhiteCharOffset(document, parameterName.getEnd());
			pushTypeOperator(TypeOperator.ASSIGNMENT, assignmentOffset, false);
			visitTextNode(defaultValue, true, 0);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.ASTError
	 * )
	 */
	@Override
	public boolean visit(ASTError astError)
	{
		builder.setHasErrors(true);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * BackTickExpression)
	 */
	@Override
	public boolean visit(BackTickExpression backTickExpression)
	{
		visitTextNode(backTickExpression, true, 0);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.Block)
	 */
	@Override
	public boolean visit(Block block)
	{
		// the default visit for a block assumes that there is an open char for that block, but not necessarily a
		// closing char. See also visitBlockNode() for other block visiting options.
		FormatterPHPBlockNode blockNode = new FormatterPHPBlockNode(document,
				block.getParent().getType() == ASTNode.PROGRAM);
		blockNode
				.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, block.getStart(), block.getStart() + 1));
		builder.push(blockNode);
		block.childrenAccept(this);
		int end = block.getEnd();
		builder.checkedPop(blockNode, end - 1);
		if (block.isCurly())
		{
			int endWithSemicolon = locateCharMatchInLine(end, SEMICOLON_AND_COLON, document, false);
			blockNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, end - 1, endWithSemicolon));
		}
		else
		{
			blockNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, end, end));
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * BreakStatement)
	 */
	@Override
	public boolean visit(BreakStatement breakStatement)
	{
		Expression expression = breakStatement.getExpression();
		int start = breakStatement.getStart();
		int end = breakStatement.getEnd();
		if (expression == null)
		{
			FormatterPHPBreakNode breakNode = new FormatterPHPBreakNode(document, breakStatement.getParent());
			breakNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, start, start + 5));
			builder.push(breakNode);
			builder.checkedPop(breakNode, -1);
		}
		else
		{
			// treat it as we treat the 'continue' statement
			// push the 'break' keyword.
			pushKeyword(start, 5, true, false);
			// visit the break expression
			expression.accept(this);
		}
		findAndPushPunctuationNode(TypePunctuation.SEMICOLON, end - 1, false, true);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * ClassDeclaration)
	 */
	@Override
	public boolean visit(ClassDeclaration classDeclaration)
	{
		visitTypeDeclaration(classDeclaration);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * ClassInstanceCreation)
	 */
	@Override
	public boolean visit(ClassInstanceCreation classInstanceCreation)
	{
		ClassName className = classInstanceCreation.getClassName();
		int creationEnd = classInstanceCreation.getEnd();
		boolean hasParentheses = creationEnd != className.getEnd();
		// push the 'new' keyword. We push it as a text node and not as a keyword to handle cases
		// were we have a reference preceding the new-instance creation ('&new MyClass')
		int start = classInstanceCreation.getStart();
		visitTextNode(start, start + 3, true, 0);
		className.accept(this);
		if (hasParentheses)
		{
			// create a constructor node
			List<Expression> ctorParams = classInstanceCreation.ctorParams();
			pushParametersInParentheses(className.getEnd(), classInstanceCreation.getEnd(), ctorParams,
					TypePunctuation.COMMA, false, TypeBracket.DECLARATION_PARENTHESIS, false);
		}
		// PHP 5.4 - chainingInstanceCall
		ChainingInstanceCall chainingInstanceCall = classInstanceCreation.getChainingInstanceCall();
		if (chainingInstanceCall != null)
		{
			// This is the only place that we can call the visit for the ChainingInstanceCall.
			// It's not being called by the regular AST visitor, so we need to call it here.
			chainingInstanceCall.accept(this);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.ClassName
	 * )
	 */
	@Override
	public boolean visit(ClassName className)
	{
		visitTextNode(className, true, 1);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * CloneExpression)
	 */
	@Override
	public boolean visit(CloneExpression cloneExpression)
	{
		// push the 'clone' as invocation.
		int cloneStart = cloneExpression.getStart();
		pushFunctionInvocationName(cloneExpression, cloneStart, cloneStart + 5);
		// push the expression as if it's in a parentheses expression
		List<ASTNode> expressionInList = new ArrayList<ASTNode>(1);
		expressionInList.add(cloneExpression.getExpression());
		pushParametersInParentheses(cloneStart + 5, cloneExpression.getEnd(), expressionInList, TypePunctuation.COMMA,
				false, TypeBracket.INVOCATION_PARENTHESIS, true);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * ConditionalExpression)
	 */
	@Override
	public boolean visit(ConditionalExpression conditionalExpression)
	{
		// (APSTUD-5303) Since PHP 5.3, it is possible to leave out the middle part of the ternary operator.
		// Expression expr1 ?: expr3 returns expr1 if expr1 evaluates to TRUE, and expr3 otherwise.
		Expression condition = conditionalExpression.getCondition();
		condition.accept(this);
		Expression ifTrue = conditionalExpression.getIfTrue();
		Expression ifFalse = conditionalExpression.getIfFalse();
		// push the conditional operator
		int startLookup;
		int endLookup;
		if (ifTrue != null)
		{
			startLookup = ifTrue.getStart();
			endLookup = ifTrue.getEnd();
		}
		else
		{
			startLookup = ifFalse.getStart();
			endLookup = condition.getEnd();
		}
		int conditionalOpOffset = condition.getEnd() + document.get(condition.getEnd(), startLookup).indexOf('?');
		pushTypeOperator(TypeOperator.CONDITIONAL, conditionalOpOffset, false);
		// visit the true part
		if (ifTrue != null)
		{
			ifTrue.accept(this);
		}
		// push the colon separator
		int colonOffset = endLookup + document.get(endLookup, ifFalse.getStart()).indexOf(':');
		pushTypeOperator(TypeOperator.CONDITIONAL_COLON, colonOffset, false);
		// visit the false part
		ifFalse.accept(this);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * ConstantDeclaration)
	 */
	@Override
	public boolean visit(ConstantDeclaration classConstantDeclaration)
	{
		// push the 'const' keyword.
		pushKeyword(classConstantDeclaration.getStart(), 5, true, false);
		// Push the declarations. Each has an assignment char and they are separated by commas.
		List<? extends ASTNode> leftNodes = classConstantDeclaration.names();
		List<? extends ASTNode> rightNodes = classConstantDeclaration.initializers();
		visitNodeLists(leftNodes, rightNodes, TypeOperator.ASSIGNMENT, TypePunctuation.COMMA);
		// locate the semicolon at the end of the expression. If exists, push it as a node.
		int end = rightNodes.get(rightNodes.size() - 1).getEnd();
		findAndPushPunctuationNode(TypePunctuation.SEMICOLON, end, false, true);
		return false;
	}

	/**
	 * This method is used to visit and push nodes that are separated with some delimiter, and potentially have
	 * operators between them.<br>
	 * For example, const A='a', B='b';
	 * 
	 * @param leftNodes
	 *            A list of ASTNodes.
	 * @param rightNodes
	 *            A list of ASTNodes that are pairing with the leftNodes. In case there are no pairs, this list may be
	 *            null. However, if there are pairs (such as assignments), the size of this group must match the size of
	 *            the left group.
	 * @param pairsOperator
	 *            The operator {@link TypeOperator} that should appear between the left and the right pair, when there
	 *            are pairs. May be null only when the rightNodes are null.
	 * @param pairsSeparator
	 *            A separator that appears between the leftNodes. If there are pairs, the separator appears between one
	 *            pair to the other (may only be null in case a separator is not needed, e.g. we have only one
	 *            item/pair)
	 */
	private void visitNodeLists(List<? extends ASTNode> leftNodes, List<? extends ASTNode> rightNodes,
			TypeOperator pairsOperator, TypePunctuation pairsSeparator)
	{
		// push the expressions one at a time, with comma nodes between them.
		int leftSize = leftNodes.size();
		for (int i = 0; i < leftSize; i++)
		{
			ASTNode left = leftNodes.get(i);
			ASTNode right = (rightNodes != null) ? rightNodes.get(i) : null;
			left.accept(this);
			if (right != null && pairsOperator != null)
			{
				int startIndex = left.getEnd();
				String text = document.get(startIndex, right.getStart());
				String typeStr = pairsOperator.toString();
				startIndex += text.indexOf(typeStr);
				pushTypeOperator(pairsOperator, startIndex, false);
				right.accept(this);
			}
			// add a separator if needed
			if (pairsSeparator != null && i + 1 < leftNodes.size())
			{
				int startIndex = left.getEnd();
				String text = document.get(startIndex, leftNodes.get(i + 1).getStart());
				String separatorStr = pairsSeparator.toString();
				startIndex += text.indexOf(separatorStr);
				pushTypePunctuation(pairsSeparator, startIndex);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * ContinueStatement)
	 */
	@Override
	public boolean visit(ContinueStatement continueStatement)
	{
		// push the 'continue' keyword.
		int continueStart = continueStatement.getStart();
		int end = continueStatement.getEnd() - 1;
		Expression expression = continueStatement.getExpression();

		pushKeyword(continueStart, 8, true, expression == null);
		// visit the continue expression, if exists
		if (expression != null)
		{
			expression.accept(this);
		}
		findAndPushPunctuationNode(TypePunctuation.SEMICOLON, end, false, true);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * DeclareStatement)
	 */
	@Override
	public boolean visit(DeclareStatement declareStatement)
	{
		Statement body = declareStatement.getBody();
		List<Identifier> directiveNames = declareStatement.directiveNames();
		List<Expression> directiveValues = declareStatement.directiveValues();
		// push the declare keyword as a function invocation
		int start = declareStatement.getStart();
		pushFunctionInvocationName(declareStatement, start, start + 7);
		// push the parentheses with the names and the values that we have inside
		int openParen = PHPFormatterNodeBuilder.locateCharForward(document, '(', start + 7, comments);
		int closeParen = PHPFormatterNodeBuilder.locateCharBackward(document, ')', (body != null) ? body.getStart()
				: declareStatement.getEnd(), comments);
		FormatterPHPParenthesesNode parenthesesNode = new FormatterPHPParenthesesNode(document,
				TypeBracket.DECLARATION_PARENTHESIS);
		parenthesesNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, openParen, openParen + 1));
		builder.push(parenthesesNode);
		// push the list of names and values
		visitNodeLists(directiveNames, directiveValues, TypeOperator.ASSIGNMENT, TypePunctuation.COMMA);
		builder.checkedPop(parenthesesNode, -1);
		parenthesesNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, closeParen, closeParen + 1));
		if (body != null)
		{
			body.accept(this);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * EchoStatement )
	 */
	@Override
	public boolean visit(EchoStatement echoStatement)
	{
		// push the 'echo' invocation.
		int echoStart = echoStatement.getStart();
		pushFunctionInvocationName(echoStatement, echoStart, echoStart + 4);
		// push the expressions one at a time, with comma nodes between them.
		List<Expression> expressions = echoStatement.expressions();
		pushParametersInParentheses(echoStart + 4, echoStatement.getEnd(), expressions, TypePunctuation.COMMA, false,
				TypeBracket.INVOCATION_PARENTHESIS, true);
		// locate the semicolon at the end of the expression. If exists, push it as a node.
		int end = Math.max(echoStatement.getEnd() - 1, expressions.get(expressions.size() - 1).getEnd());
		findAndPushPunctuationNode(TypePunctuation.SEMICOLON, end, false, true);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * EmptyStatement)
	 */
	@Override
	public boolean visit(EmptyStatement emptyStatement)
	{
		visitTextNode(emptyStatement, true, 0);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * ExpressionStatement)
	 */
	@Override
	public boolean visit(ExpressionStatement expressionStatement)
	{
		int expressionEnd = expressionStatement.getEnd();
		boolean endsWithSemicolon = document.charAt(expressionEnd - 1) == ';';
		FormatterPHPExpressionWrapperNode expressionNode = new FormatterPHPExpressionWrapperNode(document);
		int start = expressionStatement.getStart();
		int end = expressionEnd;
		if (endsWithSemicolon)
		{
			end--;
		}
		expressionNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, start, start));
		builder.push(expressionNode);
		expressionStatement.childrenAccept(this);
		expressionNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, end, end));
		builder.checkedPop(expressionNode, -1);
		// push a semicolon if we have one
		if (endsWithSemicolon)
		{
			findAndPushPunctuationNode(TypePunctuation.SEMICOLON, end, false, true);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.FieldAccess
	 * )
	 */
	@Override
	public boolean visit(FieldAccess fieldAccess)
	{
		VariableBase dispatcher = fieldAccess.getDispatcher();
		VariableBase member = fieldAccess.getMember();
		visitLeftRightExpression(fieldAccess, dispatcher, member, INVOCATION_ARROW);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * ForStatement )
	 */
	@Override
	public boolean visit(ForStatement forStatement)
	{
		List<Expression> initializers = forStatement.initializers();
		List<Expression> conditions = forStatement.conditions();
		List<Expression> updaters = forStatement.updaters();
		Statement body = forStatement.getBody();
		// visit the 'for' keyword
		int declarationEndOffset = forStatement.getStart() + 3;
		visitCommonDeclaration(forStatement, declarationEndOffset, true);
		// visit the elements in the parentheses
		int expressionEndOffset = (body != null) ? body.getStart() : forStatement.getEnd();
		int openParen = PHPFormatterNodeBuilder.locateCharForward(document, '(', declarationEndOffset, comments);
		int closeParen = PHPFormatterNodeBuilder.locateCharBackward(document, ')', expressionEndOffset, comments);
		FormatterPHPParenthesesNode parenthesesNode = new FormatterPHPParenthesesNode(document,
				TypeBracket.LOOP_PARENTHESIS);
		parenthesesNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, openParen, openParen + 1));
		builder.push(parenthesesNode);
		// visit the initializers, the conditions and the updaters.
		// between them, push the semicolons
		visitNodeLists(initializers, null, null, TypePunctuation.COMMA);
		int semicolonOffset = PHPFormatterNodeBuilder.locateCharForward(document, ';', declarationEndOffset, comments);
		pushTypePunctuation(TypePunctuation.FOR_SEMICOLON, semicolonOffset);
		visitNodeLists(conditions, null, null, TypePunctuation.COMMA);
		semicolonOffset = PHPFormatterNodeBuilder.locateCharForward(document, ';', semicolonOffset + 1, comments);
		pushTypePunctuation(TypePunctuation.FOR_SEMICOLON, semicolonOffset);
		visitNodeLists(updaters, null, null, TypePunctuation.COMMA);
		// close the parentheses node.
		builder.checkedPop(parenthesesNode, -1);
		parenthesesNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, closeParen, closeParen + 1));
		// in case we have a 'body', visit it.
		commonVisitBlockBody(forStatement, body);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * ForEachStatement)
	 */
	@Override
	public boolean visit(ForEachStatement forEachStatement)
	{
		Expression expression = forEachStatement.getExpression();
		Expression key = forEachStatement.getKey(); // the 'key' is optional
		Expression value = forEachStatement.getValue();
		Statement body = forEachStatement.getStatement();
		// visit the 'foreach' keyword
		int declarationEndOffset = forEachStatement.getStart() + 7;
		visitCommonDeclaration(forEachStatement, declarationEndOffset, true);
		// visit the elements in the parentheses
		int expressionEndOffset = (body != null) ? body.getStart() : forEachStatement.getEnd();
		int openParen = PHPFormatterNodeBuilder.locateCharForward(document, '(', declarationEndOffset, comments);
		int closeParen = PHPFormatterNodeBuilder.locateCharBackward(document, ')', expressionEndOffset, comments);
		FormatterPHPParenthesesNode parenthesesNode = new FormatterPHPParenthesesNode(document,
				TypeBracket.LOOP_PARENTHESIS);
		parenthesesNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, openParen, openParen + 1));
		builder.push(parenthesesNode);
		// push the expression
		visitTextNode(expression, true, 0);
		// add the 'as' node (it's between the expression and the key/value)
		int endLookupForAs = (key != null) ? key.getStart() : value.getStart();
		String txt = document.get(expression.getEnd(), endLookupForAs);
		int asStart = expression.getEnd() + txt.toLowerCase().indexOf("as"); //$NON-NLS-1$
		visitTextNode(asStart, asStart + 2, true, 1, 1);
		// push the key and the value.
		if (key != null)
		{
			visitLeftRightExpression(null, key, value, TypeOperator.KEY_VALUE.toString());
		}
		else
		{
			// push only the value as a text node
			visitTextNode(value, true, 1);
		}
		// close the parentheses node.
		builder.checkedPop(parenthesesNode, -1);
		parenthesesNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, closeParen, closeParen + 1));

		// in case we have a 'body', visit it.
		commonVisitBlockBody(forEachStatement, body);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * WhileStatement)
	 */
	@Override
	public boolean visit(WhileStatement whileStatement)
	{
		visitCommonLoopBlock(whileStatement, whileStatement.getStart() + 5, whileStatement.getBody(),
				whileStatement.getCondition());
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.DoStatement
	 * )
	 */
	@Override
	public boolean visit(DoStatement doStatement)
	{
		Statement body = doStatement.getBody();
		// First, push the 'do' declaration node and its body
		visitCommonLoopBlock(doStatement, doStatement.getStart() + 2, body, null);
		// now deal with the 'while' condition part. we need to include the word 'while' that appears
		// somewhere between the block-end and the condition start.
		// We wrap this node as a begin-end node that will hold the condition internals as children
		FormatterPHPNonBlockedWhileNode whileNode = new FormatterPHPNonBlockedWhileNode(document);
		// Search for the exact 'while' start offset
		int whileBeginOffset = PHPFormatterNodeBuilder.locateCharForward(document, 'w', body.getEnd(), comments);
		int conditionEnd = locateCharMatchInLine(doStatement.getEnd(), SEMICOLON, document, true);
		whileNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, whileBeginOffset, conditionEnd));
		builder.push(whileNode);
		builder.checkedPop(whileNode, -1);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * FunctionDeclaration)
	 */
	@Override
	public boolean visit(FunctionDeclaration functionDeclaration)
	{
		visitFunctionDeclaration(functionDeclaration, functionDeclaration.getFunctionName(),
				functionDeclaration.formalParameters(), null, functionDeclaration.getBody());
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * FunctionInvocation)
	 */
	@Override
	public boolean visit(FunctionInvocation functionInvocation)
	{
		visitFunctionInvocation(functionInvocation);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * GlobalStatement)
	 */
	@Override
	public boolean visit(GlobalStatement globalStatement)
	{
		pushKeyword(globalStatement.getStart(), 6, true, false);
		List<Variable> variables = globalStatement.variables();
		visitNodeLists(variables, null, null, TypePunctuation.COMMA);
		// we also need to push the semicolon for the global
		findAndPushPunctuationNode(TypePunctuation.SEMICOLON, globalStatement.getEnd() - 1, false, true);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.GotoLabel
	 * )
	 */
	@Override
	public boolean visit(GotoLabel gotoLabel)
	{
		// To Goto label is setting the end by including the spaces between the last non-white char
		// and the terminating colon. We trim those spaces in the formatting.
		FormatterPHPLineStartingNode lineStartingNode = new FormatterPHPLineStartingNode(document);
		int start = gotoLabel.getStart();
		int end = gotoLabel.getEnd();
		int trimmedLength = document.get(start, end - 1).trim().length();
		int labelEnd = end - (end - start - trimmedLength);
		lineStartingNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, start, labelEnd));
		builder.push(lineStartingNode);
		builder.checkedPop(lineStartingNode, -1);
		findAndPushPunctuationNode(TypePunctuation.GOTO_COLON, end - 1, false, true);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * GotoStatement )
	 */
	@Override
	public boolean visit(GotoStatement gotoStatement)
	{
		Identifier label = gotoStatement.getLabel();
		pushKeyword(gotoStatement.getStart(), 4, true, false);
		label.accept(this);
		findAndPushPunctuationNode(TypePunctuation.SEMICOLON, gotoStatement.getEnd() - 1, false, true);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.Identifier
	 * )
	 */
	@Override
	public boolean visit(Identifier identifier)
	{
		visitTextNode(identifier, true, 0);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.IgnoreError
	 * )
	 */
	@Override
	public boolean visit(IgnoreError ignoreError)
	{
		// push the first sign ('@') as a simple text node.
		int start = ignoreError.getStart();
		int end = start + 1;
		visitTextNode(start, end, true, 0);
		// visit the expression
		ignoreError.getExpression().accept(this);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.Include
	 * )
	 */
	@Override
	public boolean visit(Include include)
	{
		// push the 'include' keyword.
		int includeStart = include.getStart();
		int keywordLength = Include.getType(include.getIncludeType()).length();
		boolean firstInLine = include.getParent().getType() != ASTNode.IGNORE_ERROR;
		pushKeyword(includeStart, keywordLength, firstInLine, false, true);
		// visit the include expression.
		Expression expression = include.getExpression();
		expression.accept(this);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * InfixExpression)
	 */
	@Override
	public boolean visit(InfixExpression infixExpression)
	{
		ASTNode left = infixExpression.getLeft();
		ASTNode right = infixExpression.getRight();
		// Instead of calling InfixExpression.getOperator(infixExpression.getOperator()), we grab the operator
		// from the document as is. This way, we will be able to handle case-insensitive operators, as well as 'synonym'
		// operators such as <> and !=.
		// However, in case the length of the original operator is smaller, we use it anyway (see #APSTUD-4356 as a
		// reason for that)
		String operatorStr = InfixExpression.getOperator(infixExpression.getOperator());
		String operatorStringAsIs = document.get(left.getEnd(), right.getStart()).trim();
		if (operatorStr.length() < operatorStringAsIs.length())
		{
			operatorStringAsIs = operatorStr;
		}
		visitLeftRightExpression(infixExpression, left, right, operatorStringAsIs);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.InLineHtml
	 * )
	 */
	@Override
	public boolean visit(InLineHtml inLineHtml)
	{
		visitTextNode(inLineHtml, false, 0);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * InstanceOfExpression)
	 */
	@Override
	public boolean visit(InstanceOfExpression instanceOfExpression)
	{
		Expression expression = instanceOfExpression.getExpression();
		ClassName className = instanceOfExpression.getClassName();
		// visit the left expression
		expression.accept(this);
		// locate the word 'instanceof' in the gap between the expression and the class name.
		int exprEnd = expression.getEnd();
		String txt = document.get(exprEnd, className.getStart());
		int instanceOfStart = exprEnd + txt.toLowerCase().indexOf("instanceof"); //$NON-NLS-1$
		visitTextNode(instanceOfStart, instanceOfStart + 10, true, 1);
		// visit the right class name
		className.accept(this);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * InterfaceDeclaration)
	 */
	@Override
	public boolean visit(InterfaceDeclaration interfaceDeclaration)
	{
		visitTypeDeclaration(interfaceDeclaration);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * LambdaFunctionDeclaration)
	 */
	@Override
	public boolean visit(LambdaFunctionDeclaration lambdaFunctionDeclaration)
	{
		if (lambdaFunctionDeclaration.isStatic())
		{
			// Unfortunately, the only way to get to the 'static' keyword is by traversing back and look for the 's'.
			int lambdaStart = lambdaFunctionDeclaration.getStart();
			int staticStart = PHPFormatterNodeBuilder.locateCharBackward(document, 's', lambdaStart);
			if (lambdaStart != staticStart)
			{
				// push a 'static' keyword node
				FormatterPHPKeywordNode staticNode = new FormatterPHPKeywordNode(document, false, false);
				staticNode
						.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, staticStart, staticStart + 6));
				builder.push(staticNode);
				builder.checkedPop(staticNode, -1);
			}
		}
		visitFunctionDeclaration(lambdaFunctionDeclaration, null, lambdaFunctionDeclaration.formalParameters(),
				lambdaFunctionDeclaration.lexicalVariables(), lambdaFunctionDeclaration.getBody());
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * ListVariable )
	 */
	@Override
	public boolean visit(ListVariable listVariable)
	{
		List<VariableBase> variables = listVariable.variables();
		int start = listVariable.getStart();
		pushFunctionInvocationName(listVariable, start, start + 4);
		pushParametersInParentheses(start + 4, listVariable.getEnd(), variables, TypePunctuation.COMMA, false,
				TypeBracket.DECLARATION_PARENTHESIS, false);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * MethodDeclaration)
	 */
	@Override
	public boolean visit(MethodDeclaration methodDeclaration)
	{
		FunctionDeclaration function = methodDeclaration.getFunction();
		visitModifiers(methodDeclaration.getStart(), function.getStart());
		function.accept(this);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * FieldsDeclaration)
	 */
	@Override
	public boolean visit(FieldsDeclaration fieldsDeclaration)
	{
		// A class field declaration is treated in a similar way we treat a class method declaration
		Variable[] variableNames = fieldsDeclaration.getVariableNames();
		Variable firstVariable = variableNames[0];
		visitModifiers(fieldsDeclaration.getStart(), firstVariable.getStart());
		// visit the variables and their values
		Expression[] initialValues = fieldsDeclaration.getInitialValues();
		// visit the variables and their initial values
		List<? extends ASTNode> variablesList = Arrays.asList(variableNames);
		List<? extends ASTNode> valuesList = (initialValues != null) ? Arrays.asList(initialValues) : null;
		visitNodeLists(variablesList, valuesList, TypeOperator.ASSIGNMENT, TypePunctuation.COMMA);
		// locate the push the semicolon at the end
		findAndPushPunctuationNode(TypePunctuation.SEMICOLON, fieldsDeclaration.getEnd() - 1, false, true);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * MethodInvocation)
	 */
	@Override
	public boolean visit(MethodInvocation methodInvocation)
	{
		visitLeftRightExpression(methodInvocation, methodInvocation.getDispatcher(), methodInvocation.getMethod(),
				INVOCATION_ARROW);
		// note: we push the semicolon as part of the function-invocation that we have in this node.
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * StaticMethodInvocation)
	 */
	@Override
	public boolean visit(StaticMethodInvocation staticMethodInvocation)
	{
		visitLeftRightExpression(staticMethodInvocation, staticMethodInvocation.getClassName(),
				staticMethodInvocation.getMethod(), STATIC_INVOCATION);
		// note: we push the semicolon as part of the function-invocation that we have in this node.
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * NamespaceDeclaration)
	 */
	@Override
	public boolean visit(NamespaceDeclaration namespaceDeclaration)
	{
		int start = namespaceDeclaration.getStart();
		pushKeyword(start, 9, true, false);
		int end = start + 9;
		NamespaceName namespaceName = namespaceDeclaration.getName();
		if (namespaceName != null)
		{
			namespaceName.accept(this);
			end = namespaceName.getEnd();
		}
		findAndPushPunctuationNode(TypePunctuation.SEMICOLON, end, false, true);
		// visit the namespace body block. If this block is invisible one, wrap it in a special
		// namespace block to allow indentation customization.
		FormatterPHPNamespaceBlockNode bodyNode = new FormatterPHPNamespaceBlockNode(document);
		Block body = namespaceDeclaration.getBody();
		if (body.isCurly())
		{
			body.accept(this);
		}
		else
		{
			int bodyStart = body.getStart();
			int bodyEnd = body.getEnd();
			bodyNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, bodyStart, bodyStart));
			builder.push(bodyNode);
			body.childrenAccept(this);
			bodyNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, bodyEnd, bodyEnd));
			builder.checkedPop(bodyNode, namespaceDeclaration.getEnd());
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * NamespaceName )
	 */
	@Override
	public boolean visit(NamespaceName namespaceName)
	{
		List<Identifier> segments = namespaceName.segments();
		int start = namespaceName.getStart();
		if (namespaceName.isGlobal())
		{
			// look for the '\' that came before the name and push it separately.
			start = PHPFormatterNodeBuilder.locateCharBackward(document, '\\', start, comments);
			pushTypePunctuation(TypePunctuation.NAMESPACE_SEPARATOR, start);
		}
		// Push the rest of the segments as a list of nodes.
		visitNodeLists(segments, null, null, TypePunctuation.NAMESPACE_SEPARATOR);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * ParenthesisExpression)
	 */
	@Override
	public boolean visit(ParenthesisExpression parenthesisExpression)
	{
		FormatterPHPParenthesesNode parenthesesNode = new FormatterPHPParenthesesNode(document, TypeBracket.PARENTHESIS);
		int start = parenthesisExpression.getStart();
		parenthesesNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, start, start + 1));
		builder.push(parenthesesNode);
		parenthesisExpression.childrenAccept(this);
		builder.checkedPop(parenthesesNode, -1);
		int end = parenthesisExpression.getEnd();
		parenthesesNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, end - 1, end));
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * PostfixExpression)
	 */
	@Override
	public boolean visit(PostfixExpression postfixExpression)
	{
		VariableBase var = postfixExpression.getVariable();
		TypeOperator op;
		if (postfixExpression.getOperator() == PostfixExpression.OP_INC)
		{
			op = TypeOperator.POSTFIX_INCREMENT;
		}
		else
		{
			op = TypeOperator.POSTFIX_DECREMENT;
		}
		var.accept(this);
		int leftOffset = var.getEnd();
		int operatorOffset = document.get(leftOffset, postfixExpression.getEnd()).indexOf(op.toString()) + leftOffset;
		pushTypeOperator(op, operatorOffset, false);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * PrefixExpression)
	 */
	@Override
	public boolean visit(PrefixExpression prefixExpression)
	{
		VariableBase var = prefixExpression.getVariable();
		TypeOperator op;
		if (prefixExpression.getOperator() == PrefixExpression.OP_INC)
		{
			op = TypeOperator.PREFIX_INCREMENT;
		}
		else
		{
			op = TypeOperator.PREFIX_DECREMENT;
		}
		int leftOffset = prefixExpression.getStart();
		int operatorOffset = document.get(leftOffset, var.getStart()).indexOf(op.toString()) + leftOffset;
		pushTypeOperator(op, operatorOffset, false);
		var.accept(this);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.Quote)
	 */
	@Override
	public boolean visit(Quote quote)
	{
		int quoteType = quote.getQuoteType();
		String quoteStr = document.get(quote.getStart(), quote.getEnd());
		// Check for HEREDOC, NOWDOC and multi-lines strings.
		if (quoteType == Quote.QT_HEREDOC || quoteType == Quote.QT_NOWDOC
				|| LINE_SPLIT_PATTERN.split(quoteStr, 2).length == 2)
		{
			FormatterPHPHeredocNode heredocNode = new FormatterPHPHeredocNode(document, quote.getStart(),
					quote.getEnd());
			IFormatterContainerNode parentNode = builder.peek();
			parentNode.addChild(heredocNode);
		}
		else
		{
			visitTextNode(quote, true, 0);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.Reference
	 * )
	 */
	@Override
	public boolean visit(Reference reference)
	{
		// push the first reference sign ('&') as a simple text node.
		int start = reference.getStart();
		int end = start + 1;
		visitTextNode(start, end, true, 0);
		// visit the referenced expression
		reference.getExpression().accept(this);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * ReflectionVariable)
	 */
	@Override
	public boolean visit(ReflectionVariable reflectionVariable)
	{
		// push the first dollar sign as a simple text node.
		int start = reflectionVariable.getStart();
		int end = start + 1;
		visitTextNode(start, end, true, 0);
		// visit the name variable
		reflectionVariable.getName().accept(this);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * ReturnStatement)
	 */
	@Override
	public boolean visit(ReturnStatement returnStatement)
	{
		// push the 'return' keyword.
		int returnStart = returnStatement.getStart();
		Expression expression = returnStatement.getExpression();
		pushKeyword(returnStart, 6, true, expression == null);
		// visit the return expression.
		if (expression != null)
		{
			expression.accept(this);
		}
		// Check if the statement ends with a semicolon. If so, push it as a text node.
		// push the ending semicolon
		findAndPushPunctuationNode(TypePunctuation.SEMICOLON, returnStatement.getEnd() - 1, false, true);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.Scalar)
	 */
	@Override
	public boolean visit(Scalar scalar)
	{
		// In case the Scalar is a string, we need to check if the string spans across multiple lines. If this is the
		// case, we have to exclude part of this scalar from the formatting. Otherwise, we'll change the content of that
		// string.
		if (scalar.getScalarType() == Scalar.TYPE_STRING)
		{
			String[] split = LINE_SPLIT_PATTERN.split(scalar.getStringValue(), 2);
			if (split.length > 1)
			{
				// We have a multi-line string.
				FormatterPHPExcludedTextNode heredocNode = new FormatterPHPExcludedTextNode(document, 0, 0);
				heredocNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, scalar.getStart(),
						scalar.getEnd()));
				IFormatterContainerNode parentNode = builder.peek();
				parentNode.addChild(heredocNode);
				return false;
			}
		}
		visitTextNode(scalar, true, 0);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * StaticConstantAccess)
	 */
	@Override
	public boolean visit(StaticConstantAccess classConstantAccess)
	{
		visitLeftRightExpression(classConstantAccess, classConstantAccess.getClassName(),
				classConstantAccess.getConstant(), TypeOperator.STATIC_INVOCATION.toString());
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * StaticFieldAccess)
	 */
	@Override
	public boolean visit(StaticFieldAccess staticFieldAccess)
	{
		visitLeftRightExpression(staticFieldAccess, staticFieldAccess.getClassName(), staticFieldAccess.getField(),
				TypeOperator.STATIC_INVOCATION.toString());
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * StaticStatement)
	 */
	@Override
	public boolean visit(StaticStatement staticStatement)
	{
		pushKeyword(staticStatement.getStart(), 6, true, false);
		List<Expression> expressions = staticStatement.expressions();
		visitNodeLists(expressions, null, null, TypePunctuation.COMMA);
		// push the ending semicolon
		findAndPushPunctuationNode(TypePunctuation.SEMICOLON, staticStatement.getEnd() - 1, false, true);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * SwitchStatement)
	 */
	@Override
	public boolean visit(SwitchStatement switchStatement)
	{
		Block body = switchStatement.getBody();
		// In case the body is not curly, we are dealing with an alternative syntax (e.g. colon and 'endswitch' instead
		// of curly open and close for the body).
		boolean isAlternativeSyntax = !body.isCurly();
		// Push the switch-case declaration node
		FormatterPHPDeclarationNode switchNode = new FormatterPHPDeclarationNode(document, true, switchStatement);
		int rightParenthesis = PHPFormatterNodeBuilder.locateCharBackward(document, ')', body.getStart(), comments);
		switchNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, switchStatement.getStart(),
				rightParenthesis + 1));
		builder.push(switchNode);
		builder.checkedPop(switchNode, -1);

		// push a switch-case body node
		int blockStart = body.getStart();
		FormatterPHPSwitchNode blockNode = new FormatterPHPSwitchNode(document);
		blockNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, blockStart, blockStart + 1));
		builder.push(blockNode);
		// visit the children under that block node
		body.childrenAccept(this);
		int endingOffset = switchStatement.getEnd();
		endingOffset--;
		if (isAlternativeSyntax)
		{
			// deduct the 'endswitch' length.
			// we already removed 1 offset above, so we remove the extra 8.
			endingOffset -= 8;
		}
		// APSTUD-3382 - Check if we have a comment right before the end of this switch.
		if (hasAnyCommentBefore(endingOffset))
		{
			Comment comment = PHPDocUtils.getCommentByType(comments, endingOffset, document.getText(), -1);
			if (comment != null)
			{
				// push a text node for that comment
				blockNode.addChild(AbstractFormatterNodeBuilder.createTextNode(document, comment.getStart(),
						comment.getEnd()));
			}
		}
		blockNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, endingOffset, endingOffset + 1));
		// pop the block node
		builder.checkedPop(blockNode, -1);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.SwitchCase
	 * )
	 */
	@Override
	public boolean visit(SwitchCase switchCase)
	{
		List<Statement> actions = switchCase.actions();
		boolean hasBlockedChild = (actions.size() == 1 && actions.get(0).getType() == ASTNode.BLOCK);
		// compute the colon position
		int endCaseOffset = (switchCase.isDefault()) ? switchCase.getStart() + 7 : switchCase.getValue().getEnd();
		int colonOffset = PHPFormatterNodeBuilder.locateCharForward(document, ':', endCaseOffset, comments);
		// push the case/default node till the colon.
		// We create a begin-end node that will hold a case-colon node as an inner child to manage its spacing.
		FormatterPHPExpressionWrapperNode caseNode = new FormatterPHPExpressionWrapperNode(document);
		// get the value-end offset. In case it's a 'default' case, set the end at the end offset of the word 'default'
		int valueEnd = switchCase.isDefault() ? switchCase.getStart() + 7 : switchCase.getValue().getEnd();
		caseNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, switchCase.getStart(), valueEnd));
		caseNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, colonOffset + 1, colonOffset + 1));
		builder.push(caseNode);
		// push the colon node
		FormatterPHPCaseColonNode caseColonNode = new FormatterPHPCaseColonNode(document, hasBlockedChild);
		caseColonNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, colonOffset, colonOffset + 1));
		builder.push(caseColonNode);
		builder.checkedPop(caseColonNode, -1);
		builder.checkedPop(caseNode, -1);
		// push the case/default content
		FormatterPHPCaseBodyNode caseBodyNode = new FormatterPHPCaseBodyNode(document, hasBlockedChild, hasBlockedChild
				&& hasAnyCommentBefore(actions.get(0).getStart()));
		if (hasBlockedChild)
		{
			Block body = (Block) actions.get(0);
			// we have a 'case' with a curly-block
			caseBodyNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, body.getStart(),
					body.getStart() + 1));
			builder.push(caseBodyNode);
			body.childrenAccept(this);
			int endingOffset = body.getEnd() - 1;
			builder.checkedPop(caseBodyNode, endingOffset);
			int end = locateCharMatchInLine(endingOffset + 1, SEMICOLON_AND_COLON, document, false);
			caseBodyNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, endingOffset, end));
		}
		else
		{
			if (!actions.isEmpty())
			{
				int start = actions.get(0).getStart();
				if (hasAnyCommentBefore(start))
				{
					start = caseColonNode.getEndOffset();
				}
				int end = actions.get(actions.size() - 1).getEnd();
				caseBodyNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, start, start));
				builder.push(caseBodyNode);
				for (Statement st : actions)
				{
					st.accept(this);
				}
				builder.checkedPop(caseBodyNode, switchCase.getEnd());
				caseBodyNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, end, end));
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * CastExpression)
	 */
	@Override
	public boolean visit(CastExpression castExpression)
	{
		Expression expression = castExpression.getExpression();
		// push the parentheses with the case type inside them
		int castCloserOffset = PHPFormatterNodeBuilder.locateCharBackward(document, ')', expression.getStart(),
				comments);
		visitTextNode(castExpression.getStart(), castCloserOffset, true, 0);
		// push the expression
		expression.accept(this);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.CatchClause
	 * )
	 */
	@Override
	public boolean visit(CatchClause catchClause)
	{
		int declarationEnd = catchClause.getClassName().getEnd();
		declarationEnd = PHPFormatterNodeBuilder.locateCharForward(document, ')', declarationEnd, comments) + 1;
		visitCommonDeclaration(catchClause, declarationEnd, true);
		visitBlockNode(catchClause.getBody(), catchClause, true);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * ThrowStatement)
	 */
	@Override
	public boolean visit(ThrowStatement throwStatement)
	{
		pushKeyword(throwStatement.getStart(), 5, true, false);
		Expression expression = throwStatement.getExpression();
		if (expression instanceof ParenthesisExpression)
		{
			expression.accept(this);
		}
		else
		{
			// Unlike PHP 5.3, the PHP 5.4 parser does not give us a ParenthesisExpression when there is an expression
			// like "throw (new Exception());"
			// We need to manually check for parenthesis that wrap the expression.
			String text = document.get(throwStatement.getStart() + 5, expression.getStart());
			if (text.trim().startsWith("(")) { //$NON-NLS-1$
				pushNodeInParentheses('(', ')', throwStatement.getStart() + 5, expression.getEnd(), expression,
						TypeBracket.PARENTHESIS);
			}
			else
			{
				expression.accept(this);
			}
		}
		findAndPushPunctuationNode(TypePunctuation.SEMICOLON, expression.getEnd(), false, true);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * TryStatement )
	 */
	@Override
	public boolean visit(TryStatement tryStatement)
	{
		visitCommonDeclaration(tryStatement, tryStatement.getStart() + 3, true);
		visitBlockNode(tryStatement.getBody(), tryStatement, true);
		List<CatchClause> catchClauses = tryStatement.catchClauses();
		for (CatchClause catchClause : catchClauses)
		{
			catchClause.accept(this);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * UnaryOperation)
	 */
	@Override
	public boolean visit(UnaryOperation unaryOperation)
	{
		Expression expression = unaryOperation.getExpression();
		String operationString = unaryOperation.getOperationString();
		TypeOperator typeOperator = TypeOperator.getTypeOperator(operationString);
		pushTypeOperator(typeOperator, unaryOperation.getStart(), true);
		expression.accept(this);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * UseStatement )
	 */
	@Override
	public boolean visit(UseStatement useStatement)
	{
		pushKeyword(useStatement.getStart(), 3, true, false);
		List<UseStatementPart> parts = useStatement.parts();
		visitNodeLists(parts, null, null, TypePunctuation.COMMA);
		findAndPushPunctuationNode(TypePunctuation.SEMICOLON, useStatement.getEnd() - 1, false, true);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * UseStatementPart)
	 */
	@Override
	public boolean visit(UseStatementPart useStatementPart)
	{
		NamespaceName namespaceName = useStatementPart.getName();
		Identifier alias = useStatementPart.getAlias();
		// visit the namespace name
		namespaceName.accept(this);
		// in case it has an alias, add the 'as' node and then visit the alias name.
		if (alias != null)
		{
			String text = document.get(namespaceName.getEnd(), alias.getStart());
			int asOffset = text.toLowerCase().indexOf("as"); //$NON-NLS-1$
			asOffset += namespaceName.getEnd();
			pushKeyword(asOffset, 2, false, false);
			alias.accept(this);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.Variable
	 * )
	 */
	@Override
	public boolean visit(Variable variable)
	{
		visitTextNode(variable, true, 0);
		return false;
	}

	// ### PHP 5.4 nodes ### //

	/**
	 * ChainingInstanceCall visit. For example: <code>$X = (new foo)->setX(20)->getX();</code>
	 * 
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 *      ChainingInstanceCall)
	 */
	@Override
	public boolean visit(ChainingInstanceCall chainingCall)
	{
		// We skip this one. It's being covered by the method/function visits.
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * FullyQualifiedTraitMethodReference)
	 */
	@Override
	public boolean visit(FullyQualifiedTraitMethodReference node)
	{
		// e.g. B::foo in a trait 'use' expression.
		visitLeftRightExpression(node, node.getClassName(), node.getFunctionName(),
				TypeOperator.STATIC_INVOCATION.toString());
		return false;
	}

	/**
	 * PHPArrayDereferenceList visit. For example:
	 * 
	 * <pre>
	 * function cars() {
	 *   return ['Honda', 'Toyota', 'Lotus'];
	 * }
	 * echo cars()[2]; // Outputs: Lotus
	 * </pre>
	 * 
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 *      PHPArrayDereferenceList)
	 */
	@Override
	public boolean visit(PHPArrayDereferenceList dereferenceList)
	{
		// Drill down to the DereferenceNode visit...
		return super.visit(dereferenceList);
	}

	/**
	 * DereferenceNode visit for the square brackets we have at the PHPArrayDereferenceList expression.
	 * 
	 * @see PHPArrayDereferenceList
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 *      DereferenceNode)
	 */
	@Override
	public boolean visit(DereferenceNode dereferenceNode)
	{
		pushNodeInParentheses('[', ']', dereferenceNode.getStart(), dereferenceNode.getEnd(),
				dereferenceNode.getName(), TypeBracket.ARRAY_SQUARE);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.TraitAlias
	 * )
	 */
	@Override
	public boolean visit(TraitAlias node)
	{
		// @formatter:off
		// For example, each line in this block is a TraitAliasStatement node:
		// use A {
		//   B::foo as C;
		//   myFunc as protected;
		//   sayHello as private myPrivateHello;
		// }
		// @formatter:on

		// Wrap this one with a node
		FormatterPHPTraitPrecedenceWrapperNode wrapperNode = new FormatterPHPTraitPrecedenceWrapperNode(document);
		int start = node.getStart();
		int end = node.getEnd();
		wrapperNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, start, start));
		builder.push(wrapperNode);

		// Visit the method
		Expression traitMethod = node.getTraitMethod();
		traitMethod.accept(this);

		// push the 'as' keyword. Start by looking for the functionName and a possible modifier. Note that in case the
		// modifer is 'public', we have to check for an actual 'public' keyword. The default modifier is 'public',
		// however, it's not neccessary to include the keyword in the PHP code.
		String modifier = PHPFlags.toString(node.getModifier());
		Identifier functionName = node.getFunctionName();
		int traitMethodEndOffset = traitMethod.getEnd();
		String txt = document.get(traitMethodEndOffset,
				functionName != null ? functionName.getStart() : node.getModifierOffset());
		int asStart = traitMethodEndOffset + txt.toLowerCase().indexOf("as"); //$NON-NLS-1$
		visitTextNode(asStart, asStart + 2, true, 1, 1);

		// Visit any modifier we have
		int modifierOffset = node.getModifierOffset();
		if (txt.indexOf(modifier) > -1 || functionName == null)
		{
			visitTextNode(modifierOffset, modifierOffset + modifier.length(), true, 1, functionName != null ? 1 : 0);
		}
		// Visit the function name or push the modifier string
		if (functionName != null)
		{
			functionName.accept(this);
		}

		// Close the wrapper
		wrapperNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, end, end));
		builder.checkedPop(wrapperNode, -1);

		// Push a semicolon and make sure it's a line-terminating one.
		findAndPushPunctuationNode(TypePunctuation.SEMICOLON, node.getEnd() - 1, false, true);

		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * TraitDeclaration)
	 */
	@Override
	public boolean visit(TraitDeclaration traitDeclaration)
	{
		visitTypeDeclaration(traitDeclaration);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * TraitPrecedence)
	 */
	@Override
	public boolean visit(TraitPrecedence node)
	{
		// @formatter:off
		// For example, each line in this block is a TraitPrecedence node:
		// use A {
		//   B::foo insteadof A, B, C;
		//   A::fii insteadof D;
		// }
		// @formatter:on

		// Wrap this one with a node
		FormatterPHPTraitPrecedenceWrapperNode wrapperNode = new FormatterPHPTraitPrecedenceWrapperNode(document);
		int start = node.getStart();
		int end = node.getEnd();
		wrapperNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, start, start));
		builder.push(wrapperNode);

		// Visit the fully-qualified trait reference that appears before the 'insteadof' keyword.
		FullyQualifiedTraitMethodReference methodReference = node.getMethodReference();
		methodReference.accept(this);

		// push the 'insteadof' keyword.
		List<NamespaceName> trList = node.getTrList();
		int exprEnd = methodReference.getEnd();
		String txt = document.get(exprEnd, trList.get(0).getStart());
		int insteadofStart = exprEnd + txt.toLowerCase().indexOf("insteadof"); //$NON-NLS-1$
		visitTextNode(insteadofStart, insteadofStart + 9, true, 1, 1);

		// Visit the list of trait names that appear after the 'insteadof' keyword.
		visitNodeLists(trList, null, null, TypePunctuation.COMMA);

		// Close the wrapper
		wrapperNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, end, end));
		builder.checkedPop(wrapperNode, -1);

		// Push a semicolon and make sure it's a line-terminating one.
		findAndPushPunctuationNode(TypePunctuation.SEMICOLON, trList.get(trList.size() - 1).getEnd() - 1, false, true);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * TraitUseStatement)
	 */
	@Override
	public boolean visit(TraitUseStatement traitUse)
	{
		pushKeyword(traitUse.getStart(), 3, true, false);
		// Push the trait list
		List<NamespaceName> traitList = traitUse.getTraitList();
		visitNodeLists(traitList, null, null, TypePunctuation.COMMA);
		// This will visit the NamespaceName nodes that exist as the TraitUseStatement children.

		// visit any 'conflict-resolution' block (e.g. B::foo insteadof A;)
		List<TraitStatement> tsList = traitUse.getTsList();
		if (tsList != null)
		{
			// look for curly brackets. Note that there is no easy way to tell if the list is empty because it does not
			// exist, or it's empty because we have an empty block.
			int lastTraitListOffset = traitList.get(traitList.size() - 1).getEnd() - 1;
			int openCurlyOffset = PHPFormatterNodeBuilder.locateCharForward(document, '{', lastTraitListOffset,
					comments);
			FormatterPHPBlockNode blockNode = null;
			if (openCurlyOffset != lastTraitListOffset && openCurlyOffset < traitUse.getEnd())
			{
				blockNode = new FormatterPHPBlockNode(document, false);
				blockNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, openCurlyOffset,
						openCurlyOffset + 1));
				builder.push(blockNode);
			}
			if (!tsList.isEmpty())
			{
				for (TraitStatement statement : tsList)
				{
					statement.accept(this);
				}
			}
			if (blockNode != null)
			{
				// locate the closin curly and push it.
				int closeCurlyOffset = PHPFormatterNodeBuilder.locateCharBackward(document, '}', traitUse.getEnd(),
						comments);
				blockNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, closeCurlyOffset,
						closeCurlyOffset + 1));
				builder.checkedPop(blockNode, traitUse.getEnd());
			}

		}
		return false;
	}

	// ###################### Helper Methods ###################### //

	/**
	 * Visit and push a class/interface declaration
	 * 
	 * @param typeDeclaration
	 */
	private void visitTypeDeclaration(TypeDeclaration typeDeclaration)
	{
		// Locate the end offset of the declaration (before the block starts)
		Block body = typeDeclaration.getBody();
		int declarationBeginEnd = body.getStart() - 1;
		List<Identifier> interfaces = typeDeclaration.interfaces();
		if (interfaces != null && !interfaces.isEmpty())
		{
			declarationBeginEnd = interfaces.get(interfaces.size() - 1).getEnd();
		}
		else if (typeDeclaration.getType() == ASTNode.CLASS_DECLARATION
				&& ((ClassDeclaration) typeDeclaration).getSuperClass() != null)
		{
			declarationBeginEnd = ((ClassDeclaration) typeDeclaration).getSuperClass().getEnd();
		}
		else
		{
			declarationBeginEnd = typeDeclaration.getName().getEnd();
		}
		FormatterPHPDeclarationNode typeNode = new FormatterPHPDeclarationNode(document, true, typeDeclaration);
		typeNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, typeDeclaration.getStart(),
				declarationBeginEnd));
		builder.push(typeNode);
		builder.checkedPop(typeNode, -1);

		// add the class body
		FormatterPHPTypeBodyNode typeBodyNode = new FormatterPHPTypeBodyNode(document,
				hasAnyCommentBefore(body.getStart()));
		typeBodyNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, body.getStart(),
				body.getStart() + 1));
		builder.push(typeBodyNode);
		body.childrenAccept(this);
		int end = body.getEnd();
		builder.checkedPop(typeBodyNode, end - 1);
		typeBodyNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, end - 1, end));
	}

	/**
	 * A visit for a function invocation. This visit can be performed in numerous occasions, so we have it as a separate
	 * method that will be called in those occasions.
	 * 
	 * @param functionInvocation
	 */
	private void visitFunctionInvocation(FunctionInvocation functionInvocation)
	{
		FunctionName functionName = functionInvocation.getFunctionName();
		// push the function's name
		pushFunctionInvocationName(functionInvocation, functionName.getStart(), functionName.getEnd());
		// push the parenthesis and the parameters (if exist)
		List<Expression> invocationParameters = functionInvocation.parameters();
		pushParametersInParentheses(functionName.getEnd(), functionInvocation.getEnd(), invocationParameters,
				TypePunctuation.COMMA, false, TypeBracket.INVOCATION_PARENTHESIS, false);
		// PHP 5.4
		PHPArrayDereferenceList arrayDereferenceList = functionInvocation.getArrayDereferenceList();
		if (arrayDereferenceList != null)
		{
			arrayDereferenceList.accept(this);
		}
	}

	/**
	 * Push the name part of a function invocation.
	 * 
	 * @param invocationNode
	 * @param nameStart
	 * @param nameEnd
	 */
	private void pushFunctionInvocationName(ASTNode invocationNode, int nameStart, int nameEnd)
	{
		FormatterPHPFunctionInvocationNode node = new FormatterPHPFunctionInvocationNode(document, invocationNode);
		node.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, nameStart, nameEnd));
		builder.push(node);
		builder.checkedPop(node, -1);
	}

	/**
	 * Push a FormatterPHPParenthesesNode that contains a parameters array. <br>
	 * Each parameter in the parameters list is expected to be separated from the others with a comma.
	 * 
	 * @param declarationEndOffset
	 * @param expressionEndOffset
	 * @param parameters
	 * @param punctuationType
	 *            A {@link TypePunctuation}. Usually this should be a COMMA, but it can also be something else to
	 *            provide special formatting styles.
	 * @param lookForExtraComma
	 *            Indicate that the parameters list may end with an extra comma that is not included in them. This
	 *            function will look for that comma if the value is true and will add it as a punctuation node in case
	 *            it was found.
	 * @param bracketsType
	 *            The type of parentheses to push.
	 * @param needsMatchingBracketVerification
	 *            Indicate that a brackets check needs to be done in order to determine if a parentheses node is to be
	 *            added. This is needed in some special cases where the brackets are optional (like with 'echo'
	 *            statements).
	 */
	private void pushParametersInParentheses(int declarationEndOffset, int expressionEndOffset,
			List<? extends ASTNode> parameters, TypePunctuation punctuationType, boolean lookForExtraComma,
			TypeBracket bracketsType, boolean needsMatchingBracketVerification)
	{
		// in some cases, we get a ParethesisExpression inside a single parameter.
		// for those cases, we skip the parentheses node push and go straight to the
		// push of the ParethesisExpression, which should handle the rest.
		boolean pushParenthesisNode = parameters.size() != 1 || parameters.size() == 1
				&& parameters.get(0).getType() != ASTNode.PARENTHESIS_EXPRESSION;

		int openParenOffset = builder.getNextNonWhiteCharOffset(document, declarationEndOffset);
		if (needsMatchingBracketVerification && pushParenthesisNode)
		{
			// This check will handle different cases, like:
			// echo 1;
			// echo (new IDE) -> first();
			// echo ('hello');
			if (openParenOffset > -1 && document.charAt(openParenOffset) == bracketsType.getLeft().charAt(0))
			{
				// We make sure that the expression starts and ends with a matching parentheses that wraps it.
				pushParenthesisNode = isWrappedInMatchingBrackets(bracketsType, declarationEndOffset,
						expressionEndOffset, document);
			}
		}
		FormatterPHPParenthesesNode parenthesesNode = null;
		if (pushParenthesisNode)
		{
			if (bracketsType.getLeft().charAt(0) == document.charAt(openParenOffset))
			{
				parenthesesNode = new FormatterPHPParenthesesNode(document, false, parameters.size(), bracketsType);
				parenthesesNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, openParenOffset,
						openParenOffset + 1));
			}
			else
			{
				parenthesesNode = new FormatterPHPParenthesesNode(document, true, parameters.size(), bracketsType);
				parenthesesNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, openParenOffset,
						openParenOffset));
			}
			builder.push(parenthesesNode);
		}

		if (parameters != null && parameters.size() > 0)
		{
			visitNodeLists(parameters, null, null, punctuationType);
			if (lookForExtraComma)
			{
				// Look ahead to find any extra comma that we may have. If found, push it as a punctuation node.
				int lastParamEnd = parameters.get(parameters.size() - 1).getEnd();
				int nextNonWhitespace = builder.getNextNonWhiteCharOffset(document, lastParamEnd);
				if (document.charAt(nextNonWhitespace) == ',')
				{
					pushTypePunctuation(punctuationType, nextNonWhitespace);
				}
			}
		}
		if (pushParenthesisNode)
		{
			int closeParenStart = expressionEndOffset;
			int closeParenEnd = expressionEndOffset;
			if (!parenthesesNode.isAsWrapper())
			{
				closeParenStart = PHPFormatterNodeBuilder.locateCharBackward(document, bracketsType.getRight()
						.charAt(0), expressionEndOffset - 1, comments);
				closeParenEnd = closeParenStart + 1;
			}
			if (hasSingleLineCommentBefore(closeParenStart))
			{
				// Make sure that the closing pair will not get pushed up when there is a comment line right before it.
				parenthesesNode.setNewLineBeforeClosing(true);
				builder.checkedPop(parenthesesNode, closeParenStart);
			}
			else if (hasMultiLineCommentBefore(closeParenStart))
			{
				builder.checkedPop(parenthesesNode, closeParenStart);
			}
			else
			{
				builder.checkedPop(parenthesesNode, -1);
			}
			parenthesesNode.setEnd(AbstractFormatterNodeBuilder
					.createTextNode(document, closeParenStart, closeParenEnd));
		}
	}

	/**
	 * Push a FormatterPHPParenthesesNode that contains an ASTNode (expression). <br>
	 * 
	 * @param openChar
	 *            The parentheses open char (e.g. '(', '[' etc.)
	 * @param closeChar
	 *            The parentheses close char (e.g. ')', ']' etc.)
	 * @param declarationEndOffset
	 * @param expressionEndOffset
	 * @param node
	 */
	private void pushNodeInParentheses(char openChar, char closeChar, int declarationEndOffset,
			int expressionEndOffset, ASTNode node, TypeBracket type)
	{
		int openParen = PHPFormatterNodeBuilder.locateCharForward(document, openChar, declarationEndOffset, comments);
		int closeParen = PHPFormatterNodeBuilder.locateCharBackward(document, closeChar, expressionEndOffset, comments);
		FormatterPHPParenthesesNode parenthesesNode = new FormatterPHPParenthesesNode(document, type);
		parenthesesNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, openParen, openParen + 1));
		builder.push(parenthesesNode);
		if (node != null)
		{
			node.accept(this);
		}
		builder.checkedPop(parenthesesNode, -1);
		parenthesesNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, closeParen, closeParen + 1));
	}

	/**
	 * Visits and push a modifiers section. This section can appear before a method or a variable in a class, before
	 * class definitions etc.
	 * 
	 * @param node
	 *            The node that holds the modifier
	 * @param nextNode
	 *            The next node that appears right after the modifiers.
	 */
	private void visitModifiers(int startOffset, int endOffset)
	{
		// The gap between the start and the function holds the modifiers (if exist).
		// We create a node for each of these modifiers to remove any extra spaces they have between them.
		String modifiers = document.get(startOffset, endOffset);
		Matcher matcher = WORD_PATTERN.matcher(modifiers);
		boolean isFirst = true;
		while (matcher.find())
		{
			FormatterPHPKeywordNode modifierNode = new FormatterPHPKeywordNode(document, isFirst, false);
			modifierNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, matcher.start() + startOffset,
					matcher.end() + startOffset));
			builder.push(modifierNode);
			builder.checkedPop(modifierNode, -1);
			isFirst = false;
		}
		if (isFirst)
		{
			// if we got to this point with the 'isFirst' as 'true', we know that the modifiers are empty.
			// in this case, we need to push an empty modifiers node.
			FormatterPHPKeywordNode emptyModifier = new FormatterPHPKeywordNode(document, isFirst, false);
			emptyModifier.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, startOffset, startOffset));
			builder.push(emptyModifier);
			builder.checkedPop(emptyModifier, -1);
		}
	}

	/**
	 * @param node
	 * @param declarationEndOffset
	 * @param body
	 */
	private void visitCommonLoopBlock(ASTNode node, int declarationEndOffset, Statement body, ASTNode condition)
	{
		visitCommonDeclaration(node, declarationEndOffset, true);
		// if we have conditions, visit them as well
		if (condition != null)
		{
			int conditionEnd = (body != null) ? body.getStart() : node.getEnd();
			pushNodeInParentheses('(', ')', declarationEndOffset, conditionEnd, condition, TypeBracket.LOOP_PARENTHESIS);
		}
		// visit the body
		commonVisitBlockBody(node, body);
	}

	/**
	 * A common visit for a body ASTNode, which can be a Block or a different statement that will be wrapped in an
	 * implicit block node.
	 * 
	 * @param parent
	 * @param body
	 */
	private void commonVisitBlockBody(ASTNode parent, ASTNode body)
	{
		boolean hasBlockedBody = (body != null && body.getType() == ASTNode.BLOCK);
		boolean emptyBody = (body != null && body.getType() == ASTNode.EMPTY_STATEMENT);
		if (hasBlockedBody)
		{
			visitBlockNode((Block) body, parent, true);
		}
		else if (body != null)
		{
			if (!emptyBody)
			{
				wrapInImplicitBlock(body, true);
			}
			else
			{
				// create and push a special node that represents this empty statement.
				// When visiting a loop, this statement will probably only holds a semicolon char, so we make sure
				// we attach the char to the end of the previous node.
				body.accept(this);
			}
		}
	}

	/**
	 * A simple visit and push of a node that pushes a PHP text node which consumes any white-spaces before that node by
	 * request.
	 * 
	 * @param node
	 * @param consumePreviousWhitespaces
	 * @param spacesCountBefore
	 * @see #visitTextNode(int, int, boolean, int)
	 */
	private void visitTextNode(ASTNode node, boolean consumePreviousWhitespaces, int spacesCountBefore)
	{
		visitTextNode(node.getStart(), node.getEnd(), consumePreviousWhitespaces, spacesCountBefore);
	}

	/**
	 * A simple visit and push of a node that pushes a PHP text node which consumes any white-spaces before that node by
	 * request.
	 * 
	 * @param startOffset
	 * @param endOffset
	 * @param consumePreviousWhitespaces
	 * @param spacesCountBefore
	 * @see #visitTextNode(ASTNode, boolean, int)
	 */
	private void visitTextNode(int startOffset, int endOffset, boolean consumePreviousWhitespaces, int spacesCountBefore)
	{
		visitTextNode(startOffset, endOffset, consumePreviousWhitespaces, spacesCountBefore, 0);
	}

	/**
	 * A simple visit and push of a node that pushes a PHP text node which consumes any white-spaces before that node by
	 * request.
	 * 
	 * @param startOffset
	 * @param endOffset
	 * @param consumePreviousWhitespaces
	 * @param spacesCountBefore
	 * @param spacesCountAfter
	 */
	private void visitTextNode(int startOffset, int endOffset, boolean consumePreviousWhitespaces,
			int spacesCountBefore, int spacesCountAfter)
	{
		FormatterPHPTextNode textNode = new FormatterPHPTextNode(document, consumePreviousWhitespaces,
				spacesCountBefore, spacesCountAfter);
		textNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, startOffset, endOffset));
		builder.push(textNode);
		builder.checkedPop(textNode, endOffset);
	}

	/**
	 * Visit and push a FormatterPHPBlockNode. <br>
	 * The given body can represent a curly-braces body, or even an alternative syntax body. This method will check the
	 * block to see if it's curly, and if not, it will try to match the alternative syntax closer according to the given
	 * parent node type.
	 * 
	 * @param block
	 *            The block
	 * @param parent
	 *            The block's parent
	 * @see http://www.php.net/manual/en/control-structures.alternative-syntax.php
	 */
	private void visitBlockNode(Block block, ASTNode parent, boolean consumeEndingSemicolon)
	{
		boolean isAlternativeSyntaxBlock = !block.isCurly();
		FormatterPHPBlockNode blockNode = new FormatterPHPBlockNode(document, hasAnyCommentBefore(block.getStart()));
		blockNode
				.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, block.getStart(), block.getStart() + 1));
		builder.push(blockNode);
		// visit the children
		block.childrenAccept(this);
		int end = block.getEnd();
		int closingStartOffset = end;
		if (!isAlternativeSyntaxBlock)
		{
			closingStartOffset--;
		}
		if (isAlternativeSyntaxBlock)
		{
			String alternativeSyntaxCloser = getAlternativeSyntaxCloser(parent);
			int alternativeCloserLength = alternativeSyntaxCloser.length();
			if (closingStartOffset - alternativeCloserLength >= 0
					&& document.get(closingStartOffset - alternativeCloserLength, closingStartOffset).toLowerCase()
							.equals(alternativeSyntaxCloser))
			{
				closingStartOffset -= alternativeCloserLength;
			}
		}

		// pop the block node
		builder.checkedPop(blockNode, Math.min(closingStartOffset, end));
		blockNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, closingStartOffset,
				Math.max(closingStartOffset, end)));
	}

	/**
	 * Visit and push a function declaration. The declaration can be a 'regular' function or can be a lambda function.
	 * 
	 * @param functionDeclaration
	 * @param functionName
	 * @param formalParameters
	 * @param lexicalParameters
	 * @param body
	 */
	private void visitFunctionDeclaration(ASTNode functionDeclaration, Identifier functionName,
			List<FormalParameter> formalParameters, List<Expression> lexicalParameters, Block body)
	{
		// First, push the function declaration node
		int declarationEnd = functionDeclaration.getStart() + 8;
		visitCommonDeclaration(functionDeclaration, declarationEnd, true);
		// push the function name node, if exists
		if (functionName != null)
		{
			visitTextNode(functionName, true, 1);
			declarationEnd = functionName.getEnd();
		}
		boolean hasLexicalParams = (lexicalParameters != null && !lexicalParameters.isEmpty());
		int parametersEnd = (body != null) ? body.getStart() : functionDeclaration.getEnd();
		if (hasLexicalParams)
		{
			int firstLexicalOffset = lexicalParameters.get(0).getStart();
			// Search backward for the letter 'u' from the word 'use'
			parametersEnd = PHPFormatterNodeBuilder.locateCharBackward(document, 'u', firstLexicalOffset, comments) - 1;
		}
		// push the function parameters
		pushParametersInParentheses(declarationEnd, parametersEnd, formalParameters, TypePunctuation.COMMA, false,
				TypeBracket.DECLARATION_PARENTHESIS, false);
		// In case we have 'lexical' parameters, like we get with a lambda-function, we push them after pushing the
		// 'use' keyword (for example: function($aaa) use ($bbb, $ccc)...)
		if (hasLexicalParams)
		{
			// Locate and push the 'use'
			int useKeywordStart = builder.getNextNonWhiteCharOffset(document, builder.peek().getEndOffset());
			pushKeyword(useKeywordStart, 3, false, false);
			// Push the lexical parameters
			pushParametersInParentheses(useKeywordStart + 3, body.getStart(), lexicalParameters, TypePunctuation.COMMA,
					false, TypeBracket.DECLARATION_PARENTHESIS, false);
		}

		// Then, push the body
		if (body != null)
		{
			FormatterPHPFunctionBodyNode bodyNode = new FormatterPHPFunctionBodyNode(document,
					hasAnyCommentBefore(body.getStart()));
			bodyNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, body.getStart(),
					body.getStart() + 1));
			builder.push(bodyNode);
			body.childrenAccept(this);
			int bodyEnd = body.getEnd();
			builder.checkedPop(bodyNode, bodyEnd - 1);
			bodyNode.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, bodyEnd - 1, bodyEnd));
		}
	}

	/**
	 * Visit and push a common declaration part of an expression.s
	 * 
	 * @param node
	 * @param declarationEndOffset
	 * @param hasBlockedBody
	 */
	private void visitCommonDeclaration(ASTNode node, int declarationEndOffset, boolean hasBlockedBody)
	{
		FormatterPHPDeclarationNode declarationNode = new FormatterPHPDeclarationNode(document, hasBlockedBody, node);
		declarationNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, node.getStart(),
				declarationEndOffset));
		builder.push(declarationNode);
		builder.checkedPop(declarationNode, -1);
	}

	/**
	 * Visit an expression with left node, right node and an operator in between.<br>
	 * Note that the left <b>or</b> the right may be null.
	 * 
	 * @param left
	 * @param right
	 * @param operatorString
	 */
	private void visitLeftRightExpression(ASTNode parentNode, ASTNode left, ASTNode right, String operatorString)
	{
		int leftOffset;
		int rightOffset;
		if (left != null)
		{
			left.accept(this);
			leftOffset = left.getEnd();
		}
		else
		{
			leftOffset = parentNode.getStart();
		}
		if (right != null)
		{
			rightOffset = right.getStart();
		}
		else
		{
			rightOffset = parentNode.getEnd();
		}
		int operatorOffset = document.get(leftOffset, rightOffset).indexOf(operatorString) + leftOffset;
		TypeOperator typeOperator = TypeOperator.getTypeOperator(operatorString.toLowerCase());
		pushTypeOperator(typeOperator, operatorOffset, false);
		if (right != null)
		{
			right.accept(this);
		}
	}

	private void pushTypeOperator(TypeOperator operator, int startOffset, boolean isUnary)
	{
		FormatterPHPOperatorNode node = new FormatterPHPOperatorNode(document, operator, isUnary);
		node.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, startOffset, startOffset
				+ operator.toString().length()));
		builder.push(node);
		builder.checkedPop(node, -1);
	}

	private void pushTypePunctuation(TypePunctuation punctuation, int startOffset)
	{
		FormatterPHPPunctuationNode node = new FormatterPHPPunctuationNode(document, punctuation);
		node.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, startOffset, startOffset
				+ punctuation.toString().length()));
		builder.push(node);
		builder.checkedPop(node, -1);
	}

	/**
	 * Returns the string value that represents the closing of an alternative syntax block. In case non exists, this
	 * method returns an empty string.
	 * 
	 * @param parent
	 * @return The alternative syntax block-closing string.
	 */
	private String getAlternativeSyntaxCloser(ASTNode parent)
	{
		switch (parent.getType())
		{
			case ASTNode.IF_STATEMENT:
				return "endif"; //$NON-NLS-1$
			case ASTNode.WHILE_STATEMENT:
				return "endwhile"; //$NON-NLS-1$
			case ASTNode.FOR_EACH_STATEMENT:
				return "endforeach"; //$NON-NLS-1$
			case ASTNode.FOR_STATEMENT:
				return "endfor"; //$NON-NLS-1$
			case ASTNode.SWITCH_STATEMENT:
				return "endswitch"; //$NON-NLS-1$
			default:
				return StringUtil.EMPTY;
		}
	}

	/**
	 * Push a keyword (e.g. 'const', 'echo', 'private' etc.)
	 * 
	 * @param start
	 * @param keywordLength
	 * @param isFirstInLine
	 * @param isLastInLine
	 */
	private void pushKeyword(int start, int keywordLength, boolean isFirstInLine, boolean isLastInLine)
	{
		pushKeyword(start, keywordLength, isFirstInLine, isLastInLine, false);
	}

	/**
	 * Push a keyword (e.g. 'const', 'echo', 'private' etc.)
	 * 
	 * @param start
	 * @param keywordLength
	 * @param isFirstInLine
	 * @param isLastInLine
	 * @param consumeSpaces
	 *            Consume any spaces before the keyword.
	 */
	private void pushKeyword(int start, int keywordLength, boolean isFirstInLine, boolean isLastInLine,
			boolean consumeSpaces)
	{
		FormatterPHPKeywordNode keywordNode = new FormatterPHPKeywordNode(document, isFirstInLine, isLastInLine,
				consumeSpaces);
		keywordNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, start, start + keywordLength));
		builder.push(keywordNode);
		builder.checkedPop(keywordNode, -1);
	}

	/**
	 * Locate and push a punctuation char node.
	 * 
	 * @param offsetToSearch
	 *            - The offset that will be used as the start for the search of the semicolon.
	 * @param ignoreNonWhitespace
	 *            indicate that a non-whitespace chars that appear before the semicolon will be ignored. If this flag is
	 *            false, and a non-whitespace appear between the given offset and the semicolon, the method will
	 *            <b>not</b> push a semicolon node.
	 * @param isLineTerminating
	 *            Indicates that this punctuation node is a line terminating one.
	 */
	private void findAndPushPunctuationNode(TypePunctuation type, int offsetToSearch, boolean ignoreNonWhitespace,
			boolean isLineTerminating)
	{
		char punctuationType = type.toString().charAt(0);
		int punctuationOffset = PHPFormatterNodeBuilder.locateCharForward(document, punctuationType, offsetToSearch,
				comments);
		if (punctuationOffset != offsetToSearch || document.charAt(punctuationOffset) == punctuationType)
		{
			if (offsetToSearch + 1 < punctuationOffset)
			{
				// Check this when the punctuation type was found a few characters ahead (not off by one).
				String segment = document.get(offsetToSearch, punctuationOffset);
				if (!ignoreNonWhitespace && segment.trim().length() > 0)
				{
					return;
				}
			}
			if (isLineTerminating)
			{
				// We need to make sure that the termination only happens when the line does not
				// have a terminator already.
				int lineEnd = locateWhitespaceLineEndingOffset(punctuationOffset + 1);
				isLineTerminating = lineEnd < 0;
			}
			FormatterPHPPunctuationNode punctuationNode = new FormatterPHPPunctuationNode(document, type,
					isLineTerminating);
			punctuationNode.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, punctuationOffset,
					punctuationOffset + 1));
			builder.push(punctuationNode);
			builder.checkedPop(punctuationNode, -1);
		}
	}

	/**
	 * Wrap a given node in an implicit block node and visit the node to insert it as a child of that block.
	 * 
	 * @param node
	 *            The node to wrap and visit.
	 * @param indent
	 */
	private void wrapInImplicitBlock(ASTNode node, boolean indent)
	{
		FormatterPHPImplicitBlockNode emptyBlock = new FormatterPHPImplicitBlockNode(document, false, indent, 0);
		int start = node.getStart();
		int end = node.getEnd();
		emptyBlock.setBegin(AbstractFormatterNodeBuilder.createTextNode(document, start, start));
		builder.push(emptyBlock);
		node.accept(this);
		builder.checkedPop(emptyBlock, -1);
		emptyBlock.setEnd(AbstractFormatterNodeBuilder.createTextNode(document, end, end));
	}

	/**
	 * Locate a line ending offset. The line should only contain whitespace characters.
	 * 
	 * @return The line ending offset, or -1 in case not found.
	 */
	private int locateWhitespaceLineEndingOffset(int start)
	{
		int length = document.getLength();
		for (int offset = start; offset < length; offset++)
		{
			char c = document.charAt(offset);
			if (c == '\n' || c == '\r')
			{
				return offset;
			}
			if (!Character.isWhitespace(c))
			{
				return -1;
			}
		}
		return -1;
	}

	/**
	 * Scan for a list of char terminator located at the <b>same line</b>. Return the given offset if non is found.<br>
	 * <b>See important note in the @return tag.</b>
	 * 
	 * @param offset
	 * @param chars
	 *            An array of chars to match
	 * @param document
	 * @param ignoreNonWhitespace
	 *            In case this flag is false, any non-whitespace char that appear before we located a requested char
	 *            will stop the search. In case it's true, the search will continue till the end of the line.
	 * @return The first match offset; The given offset if a match not found. <b>Note that the returned offset is in a
	 *         +1 position from the real character offset. This is to ease the caller process of adapting it to the
	 *         formatter-document's offsets.</b>
	 */
	private int locateCharMatchInLine(int offset, char[] chars, FormatterDocument document, boolean ignoreNonWhitespace)
	{
		int i = offset;
		int size = document.getLength();
		for (; i < size; i++)
		{
			char c = document.charAt(i);
			for (char toMatch : chars)
			{
				if (c == toMatch)
				{
					return i + 1;
				}
			}
			if (c == '\n' || c == '\r')
			{
				break;
			}
			if (!ignoreNonWhitespace && (c != ' ' || c != '\t'))
			{
				break;
			}
		}
		return offset;
	}

	/**
	 * Check if the document range is wrapped in matching brackets. Not only that the brackets have to be balanced, they
	 * also need to wrap the range (excluding whitespaces).
	 * 
	 * @param bracketsType
	 * @param startOffset
	 * @param endOffset
	 * @param document
	 * @return <code>true</code> iff the range is wrapped with the bracket-type open and close chars; <code>false</code>
	 *         otherwise.
	 */
	private static boolean isWrappedInMatchingBrackets(TypeBracket bracketsType, int startOffset, int endOffset,
			FormatterDocument document)
	{
		endOffset = Math.min(endOffset, document.getLength() - 1);
		if (document.charAt(endOffset) == ';')
		{
			endOffset--;
		}
		char openChar = bracketsType.getLeft().charAt(0);
		char closeChar = bracketsType.getRight().charAt(0);
		Stack<Character> brackets = new Stack<Character>();
		for (; startOffset <= endOffset; startOffset++)
		{
			char c = document.charAt(startOffset);
			if (c == openChar)
			{
				brackets.push(c);
			}
			else if (c == closeChar)
			{
				if (brackets.isEmpty() || brackets.pop().charValue() != openChar)
				{
					return false;
				}
			}
			else if (!Character.isWhitespace(c) && brackets.isEmpty())
			{
				return false;
			}
		}
		return brackets.isEmpty();
	}
}
