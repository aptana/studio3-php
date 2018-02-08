package com.aptana.editor.php.internal.parser.nodes;

import java.util.Collections;
import java.util.List;

import org2.eclipse.php.core.compiler.PHPFlags;
import org2.eclipse.php.internal.core.ast.nodes.ASTNode;
import org2.eclipse.php.internal.core.ast.nodes.CatchClause;
import org2.eclipse.php.internal.core.ast.nodes.ClassDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.ConstantDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.DoStatement;
import org2.eclipse.php.internal.core.ast.nodes.Expression;
import org2.eclipse.php.internal.core.ast.nodes.FieldsDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.ForEachStatement;
import org2.eclipse.php.internal.core.ast.nodes.ForStatement;
import org2.eclipse.php.internal.core.ast.nodes.FormalParameter;
import org2.eclipse.php.internal.core.ast.nodes.FunctionDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.FunctionInvocation;
import org2.eclipse.php.internal.core.ast.nodes.FunctionName;
import org2.eclipse.php.internal.core.ast.nodes.Identifier;
import org2.eclipse.php.internal.core.ast.nodes.IfStatement;
import org2.eclipse.php.internal.core.ast.nodes.InLineHtml;
import org2.eclipse.php.internal.core.ast.nodes.Include;
import org2.eclipse.php.internal.core.ast.nodes.InterfaceDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.MethodDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.NamespaceDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.NamespaceName;
import org2.eclipse.php.internal.core.ast.nodes.ParenthesisExpression;
import org2.eclipse.php.internal.core.ast.nodes.Scalar;
import org2.eclipse.php.internal.core.ast.nodes.Statement;
import org2.eclipse.php.internal.core.ast.nodes.SwitchCase;
import org2.eclipse.php.internal.core.ast.nodes.SwitchStatement;
import org2.eclipse.php.internal.core.ast.nodes.TraitDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.TryStatement;
import org2.eclipse.php.internal.core.ast.nodes.UseStatement;
import org2.eclipse.php.internal.core.ast.nodes.UseStatementPart;
import org2.eclipse.php.internal.core.ast.nodes.Variable;
import org2.eclipse.php.internal.core.ast.nodes.WhileStatement;
import org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor;
import org2.eclipse.php.internal.core.documentModel.phpElementData.BasicPHPDocTag;
import org2.eclipse.php.internal.core.documentModel.phpElementData.IPHPDocBlock;
import org2.eclipse.php.internal.core.documentModel.phpElementData.IPHPDocTag;
import org2.eclipse.php.internal.core.documentModel.phpElementData.PHPDocBlockImp;

import com.aptana.editor.php.internal.indexer.PHPDocUtils;

/**
 * An AST visitor that is used to build the PHP outline nodes.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public final class NodeBuildingVisitor extends AbstractVisitor
{
	private static final String DEFINE = "define"; //$NON-NLS-1$
	private NodeBuilder nodeBuilder;
	private String source;

	/**
	 * Construct a new visitor with a given NodeBuilder that will be used for the actual creation of PHP nodes.s
	 * 
	 * @param nodeBuilder
	 */
	public NodeBuildingVisitor(NodeBuilder nodeBuilder, String source)
	{
		this.nodeBuilder = nodeBuilder;
		this.source = source;
	}

	@Override
	public boolean visit(InLineHtml inLineHtml)
	{
		nodeBuilder.handleInlineHtml(inLineHtml.getStart(), inLineHtml.getEnd());
		return super.visit(inLineHtml);
	}

	@Override
	public boolean visit(InterfaceDeclaration interfaceDeclaration)
	{
		Identifier nameIdentifier = interfaceDeclaration.getName();
		String name = nameIdentifier.getName();
		org2.eclipse.php.internal.core.compiler.ast.nodes.PHPDocBlock docComment = PHPDocUtils.findPHPDocComment(
				interfaceDeclaration.getProgramRoot().comments(), interfaceDeclaration.getStart(), source);
		PHPDocBlockImp docBlock = convertToDocBlock(docComment);
		nodeBuilder.handleClassDeclaration(name, PHPFlags.AccInterface, docBlock, interfaceDeclaration.getStart(),
				interfaceDeclaration.getEnd() - 1, -1);
		List<Identifier> interfaces = interfaceDeclaration.interfaces();
		handleInterfaces(interfaces);
		nodeBuilder.setNodeName(nameIdentifier);
		return super.visit(interfaceDeclaration);
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * TraitDeclaration)
	 */
	@Override
	public boolean visit(TraitDeclaration traitDeclaration)
	{
		Identifier nameIdentifier = traitDeclaration.getName();
		String name = nameIdentifier.getName();
		org2.eclipse.php.internal.core.compiler.ast.nodes.PHPDocBlock docComment = PHPDocUtils.findPHPDocComment(
				traitDeclaration.getProgramRoot().comments(), traitDeclaration.getStart(), source);
		PHPDocBlockImp docBlock = convertToDocBlock(docComment);
		nodeBuilder.handleTraitDeclaration(name, traitDeclaration.getModifier(), docBlock, traitDeclaration.getStart(),
				traitDeclaration.getEnd() - 1, -1);
		Expression superClass = traitDeclaration.getSuperClass();
		if (superClass != null && superClass.getType() == ASTNode.IDENTIFIER)
		{
			Identifier superClassName = (Identifier) superClass;
			nodeBuilder.handleTraitSuperclass(superClassName.getName(), superClassName.getStart(),
					superClassName.getEnd() - 1);
		}
		List<Identifier> interfaces = traitDeclaration.interfaces();
		handleInterfaces(interfaces);
		nodeBuilder.setNodeName(nameIdentifier);
		return super.visit(traitDeclaration);
	}

	@Override
	public boolean visit(ClassDeclaration classDeclaration)
	{
		Identifier nameIdentifier = classDeclaration.getName();
		String name = nameIdentifier.getName();
		org2.eclipse.php.internal.core.compiler.ast.nodes.PHPDocBlock docComment = PHPDocUtils.findPHPDocComment(
				classDeclaration.getProgramRoot().comments(), classDeclaration.getStart(), source);
		PHPDocBlockImp docBlock = convertToDocBlock(docComment);
		nodeBuilder.handleClassDeclaration(name, classDeclaration.getModifier(), docBlock, classDeclaration.getStart(),
				classDeclaration.getEnd() - 1, -1);
		// Handle class inheritance elements (extends and implements)
		// TODO - Shalom - Take a look at the PDT ClassHighlighting (handle namespaces)
		Expression superClass = classDeclaration.getSuperClass();
		if (superClass != null && superClass.getType() == ASTNode.IDENTIFIER)
		{
			Identifier superClassName = (Identifier) superClass;
			nodeBuilder.handleSuperclass(superClassName.getName(), superClassName.getStart(),
					superClassName.getEnd() - 1);
		}
		List<Identifier> interfaces = classDeclaration.interfaces();
		handleInterfaces(interfaces);
		nodeBuilder.setNodeName(nameIdentifier);
		return super.visit(classDeclaration);
	}

	@Override
	public boolean visit(FieldsDeclaration fieldsDeclaration)
	{
		int modifier = fieldsDeclaration.getModifier();
		int startPosition = -1;
		int endPosition = -1;
		IPHPDocBlock docInfo = null;
		StringBuilder vars = new StringBuilder();
		for (Variable v : fieldsDeclaration.getVariableNames())
		{
			Expression variableName = v.getName();
			if (variableName.getType() == ASTNode.IDENTIFIER)
			{
				if (startPosition < 0)
					startPosition = variableName.getStart();
				endPosition = variableName.getEnd();
				vars.append(((Identifier) variableName).getName());
				vars.append(',');
			}
		}
		vars = vars.deleteCharAt(vars.length() - 1);
		String variables = vars.toString();
		// Just in case of an error, make sure that we have start and end positions.
		if (startPosition < 0 || endPosition < 0)
		{
			startPosition = fieldsDeclaration.getStart();
			endPosition = fieldsDeclaration.getEnd();
		}
		int stopPosition = endPosition - 1;
		nodeBuilder.handleClassVariablesDeclaration(variables, modifier, docInfo, startPosition, endPosition - 1,
				stopPosition);
		return super.visit(fieldsDeclaration);
	}

	@Override
	public boolean visit(Include include)
	{
		int includeT = include.getIncludeType();
		String includeType = "include"; //$NON-NLS-1$
		switch (includeT)
		{
			case Include.IT_INCLUDE:
				includeType = "include"; //$NON-NLS-1$
				break;

			case Include.IT_INCLUDE_ONCE:
				includeType = "include_once"; //$NON-NLS-1$
				break;
			case Include.IT_REQUIRE_ONCE:
				includeType = "require_once"; //$NON-NLS-1$
				break;
			case Include.IT_REQUIRE:
				includeType = "require"; //$NON-NLS-1$
				break;
			default:
				break;
		}
		Expression expr = include.getExpression();
		if (expr != null && expr.getType() == ASTNode.PARENTHESIS_EXPRESSION)
		{
			ParenthesisExpression pa = (ParenthesisExpression) expr;
			expr = pa.getExpression();
		}
		String expStringValue = null;
		if (expr != null)
		{
			int type = expr.getType();
			if (type == ASTNode.SCALAR)
			{
				expStringValue = ((Scalar) expr).getStringValue();
			}
			else if (type == ASTNode.INFIX_EXPRESSION)
			{
				// This expression may contain nested infix-expressions, so we just grab the text directly.
				expStringValue = this.source.substring(expr.getStart(), expr.getEnd());
			}
		}
		if (expStringValue != null)
		{
			nodeBuilder.handleIncludedFile(includeType, expStringValue, null, expr.getStart(), expr.getEnd() - 1, -1,
					-1);
		}
		return super.visit(include);
	}

	public boolean visit(FunctionInvocation functionInvocation)
	{
		FunctionName funcName = functionInvocation.getFunctionName();
		if (funcName == null)
		{
			return super.visit(functionInvocation);
		}
		Expression name = funcName.getName();
		if (name instanceof Identifier)
		{
			if (!DEFINE.equals(((Identifier) name).getName().toLowerCase()))
			{
				return super.visit(functionInvocation);
			}
		}
		if (name instanceof Variable)
		{
			Variable nameVar = (Variable) name;
			name = nameVar.getName();
			if (name instanceof Identifier)
			{
				if (!DEFINE.equals(((Identifier) name).getName()))
				{
					return super.visit(functionInvocation);
				}
			}
		}

		List<Expression> parameters = functionInvocation.parameters();

		if (parameters.size() >= 2)
		{
			Expression param = parameters.get(0);
			if (param.getType() == ASTNode.SCALAR && Scalar.TYPE_STRING == ((Scalar) param).getScalarType())
			{
				// Get the 'define' name
				String define = ((Scalar) param).getStringValue();
				if (define.startsWith("\"")) //$NON-NLS-1$
				{
					define = define.substring(1);
				}
				if (define.endsWith("\"")) //$NON-NLS-1$
				{
					define = define.substring(0, define.length() - 1);
				}
				if (define.startsWith("\'")) //$NON-NLS-1$;
				{
					define = define.substring(1);
				}
				if (define.endsWith("\'")) //$NON-NLS-1$
				{
					define = define.substring(0, define.length() - 1);
				}
				org2.eclipse.php.internal.core.compiler.ast.nodes.PHPDocBlock docComment = PHPDocUtils
						.findPHPDocComment(functionInvocation.getProgramRoot().comments(),
								functionInvocation.getStart(), source);
				PHPDocBlockImp docBlock = convertToDocBlock(docComment);
				nodeBuilder.handleDefine(define, null, docBlock, param.getStart(), param.getEnd() - 1, -1);
			}
		}
		return super.visit(functionInvocation);
	}

	@Override
	public boolean visit(ConstantDeclaration node)
	{
		List<Identifier> variableNames = node.names();
		for (Identifier i : variableNames)
		{
			nodeBuilder.handleDefine(i.getName(), null, null, i.getStart(), node.getEnd() - 1, -1);
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(NamespaceDeclaration node)
	{
		NamespaceName name = node.getName();
		List<Identifier> segments;
		if (name == null)
		{
			segments = Collections.emptyList();
		}
		else
		{
			segments = name.segments();
		}
		StringBuilder stringBuilder = new StringBuilder();
		for (Identifier i : segments)
		{
			stringBuilder.append(i.getName());
			stringBuilder.append('\\');
		}
		if (stringBuilder.length() > 0)
		{
			stringBuilder.deleteCharAt(stringBuilder.length() - 1);
		}
		String segmentsString = stringBuilder.toString();
		int nameEndOffset = (name != null) ? name.getEnd() - 1 : node.getStart() + 8;
		nodeBuilder.handleNamespaceDeclaration(segmentsString, node.getStart(), node.getEnd() - 1, nameEndOffset);
		return super.visit(node);
	}

	@Override
	public boolean visit(UseStatement node)
	{
		List<UseStatementPart> parts = node.parts();
		for (UseStatementPart p : parts)
		{
			Identifier alias = p.getAlias();
			List<Identifier> segments = p.getName().segments();
			StringBuilder stringBuilder = new StringBuilder();
			for (Identifier i : segments)
			{
				stringBuilder.append(i.getName());
				stringBuilder.append('\\');
			}
			stringBuilder.deleteCharAt(stringBuilder.length() - 1);
			String segmentsString = stringBuilder.toString();
			nodeBuilder.handleUse(segmentsString, (alias != null) ? alias.getName() : null, node.getStart(),
					node.getEnd() - 1);
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(FunctionDeclaration functionDeclaration)
	{
		ASTNode parent = functionDeclaration.getParent();
		boolean isClassFunction = parent != null && parent.getType() == ASTNode.METHOD_DECLARATION;
		int modifiers = 0;
		if (isClassFunction)
		{
			MethodDeclaration md = (MethodDeclaration) functionDeclaration.getParent();
			modifiers = md.getModifier();
		}
		List<FormalParameter> formalParameters = functionDeclaration.formalParameters();
		for (FormalParameter p : formalParameters)
		{
			// TODO - Shalom: Test this
			String type = null;
			String vName = null;
			String defaultVal = null;
			Expression parameterType = p.getParameterType();
			Expression parameterName = p.getParameterName();
			Expression defaultValue = p.getDefaultValue();
			if (parameterType != null && parameterType.getType() == ASTNode.VARIABLE)
				type = ((Identifier) ((Variable) parameterType).getName()).getName();
			if (parameterName != null && parameterName.getType() == ASTNode.VARIABLE)
				vName = ((Identifier) ((Variable) parameterName).getName()).getName();
			if (defaultValue != null && defaultValue.getType() == ASTNode.SCALAR)
				defaultVal = ((Scalar) defaultValue).getStringValue();

			nodeBuilder.handleFunctionParameter(type, vName, false, false, defaultVal, p.getStart(), p.getEnd(),
					p.getEnd() - 1, -1);
		}
		Identifier functionName = functionDeclaration.getFunctionName();
		org2.eclipse.php.internal.core.compiler.ast.nodes.PHPDocBlock docComment = PHPDocUtils.findPHPDocComment(
				functionDeclaration.getProgramRoot().comments(), functionDeclaration.getStart(), source);
		PHPDocBlockImp docBlock = convertToDocBlock(docComment);
		nodeBuilder.handleFunctionDeclaration(functionName.getName(), isClassFunction, modifiers, docBlock,
				functionDeclaration.getStart(), functionDeclaration.getEnd() - 1, -1);
		nodeBuilder.setNodeName(functionName);
		return super.visit(functionDeclaration);
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * TryStatement)
	 */
	@Override
	public boolean visit(TryStatement tryStatement)
	{
		List<CatchClause> catchClauses = tryStatement.catchClauses();
		int end = tryStatement.getEnd();
		if (catchClauses != null && !catchClauses.isEmpty())
		{
			end = catchClauses.get(0).getStart() - 1;
		}
		nodeBuilder.handleTryStatement(tryStatement.getStart(), end);
		return super.visit(tryStatement);
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
		nodeBuilder.handleCatchStatement(catchClause.getStart(), catchClause.getEnd());
		return super.visit(catchClause);
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
		nodeBuilder.handleDoStatement(doStatement.getStart(), doStatement.getEnd());
		return super.visit(doStatement);
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * ForEachStatement)
	 */
	@Override
	public boolean visit(ForEachStatement forEachStatement)
	{
		nodeBuilder.handleForEachStatement(forEachStatement.getStart(), forEachStatement.getEnd());
		return super.visit(forEachStatement);
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * ForStatement)
	 */
	@Override
	public boolean visit(ForStatement forStatement)
	{
		nodeBuilder.handleForStatement(forStatement.getStart(), forStatement.getEnd());
		return super.visit(forStatement);
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
		Statement trueStatement = ifStatement.getTrueStatement();
		Statement falseStatement = ifStatement.getFalseStatement();
		if (trueStatement != null && trueStatement.getType() != ASTNode.IF_STATEMENT)
		{
			nodeBuilder.handleIfElseStatement(trueStatement.getStart(), trueStatement.getEnd(), "if"); //$NON-NLS-1$
		}
		if (falseStatement != null)
		{
			nodeBuilder.handleIfElseStatement(falseStatement.getStart(), falseStatement.getEnd(), "else"); //$NON-NLS-1$
		}
		return super.visit(ifStatement);
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
		nodeBuilder.handleSwitchCaseStatement(switchCase.getStart(), switchCase.getEnd());
		return super.visit(switchCase);
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * SwitchStatement)
	 */
	@Override
	public boolean visit(SwitchStatement switchStatement)
	{
		nodeBuilder.handleSwitchStatement(switchStatement.getStart(), switchStatement.getEnd());
		return super.visit(switchStatement);
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.
	 * WhileStatement)
	 */
	@Override
	public boolean visit(WhileStatement whileStatement)
	{
		nodeBuilder.handleWhileStatement(whileStatement.getStart(), whileStatement.getEnd());
		return super.visit(whileStatement);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#endVisit(org2.eclipse.php.internal.core.ast.nodes.
	 * CatchClause)
	 */
	@Override
	public void endVisit(CatchClause catchClause)
	{
		nodeBuilder.handleCommonDeclarationEnd();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#endVisit(org2.eclipse.php.internal.core.ast.nodes.
	 * DoStatement)
	 */
	@Override
	public void endVisit(DoStatement doStatement)
	{
		nodeBuilder.handleCommonDeclarationEnd();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#endVisit(org2.eclipse.php.internal.core.ast.nodes.
	 * ForEachStatement)
	 */
	@Override
	public void endVisit(ForEachStatement forEachStatement)
	{
		nodeBuilder.handleCommonDeclarationEnd();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#endVisit(org2.eclipse.php.internal.core.ast.nodes.
	 * ForStatement)
	 */
	@Override
	public void endVisit(ForStatement forStatement)
	{
		nodeBuilder.handleCommonDeclarationEnd();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#endVisit(org2.eclipse.php.internal.core.ast.nodes.
	 * IfStatement)
	 */
	@Override
	public void endVisit(IfStatement ifStatement)
	{
		if (ifStatement.getTrueStatement() != null && ifStatement.getTrueStatement().getType() != ASTNode.IF_STATEMENT)
		{
			nodeBuilder.handleCommonDeclarationEnd();
		}
		if (ifStatement.getFalseStatement() != null)
		{
			nodeBuilder.handleCommonDeclarationEnd();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#endVisit(org2.eclipse.php.internal.core.ast.nodes.
	 * SwitchCase)
	 */
	@Override
	public void endVisit(SwitchCase switchCase)
	{
		nodeBuilder.handleCommonDeclarationEnd();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#endVisit(org2.eclipse.php.internal.core.ast.nodes.
	 * SwitchStatement)
	 */
	@Override
	public void endVisit(SwitchStatement switchStatement)
	{
		nodeBuilder.handleCommonDeclarationEnd();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#endVisit(org2.eclipse.php.internal.core.ast.nodes.
	 * TryStatement)
	 */
	@Override
	public void endVisit(TryStatement tryStatement)
	{
		nodeBuilder.handleCommonDeclarationEnd();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#endVisit(org2.eclipse.php.internal.core.ast.nodes.
	 * WhileStatement)
	 */
	@Override
	public void endVisit(WhileStatement whileStatement)
	{
		nodeBuilder.handleCommonDeclarationEnd();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#endVisit(org2.eclipse.php.internal.core.ast.nodes.
	 * TraitDeclaration)
	 */
	@Override
	public void endVisit(TraitDeclaration traitDeclaration)
	{
		nodeBuilder.handleCommonDeclarationEnd();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#endVisit(org2.eclipse.php.internal.core.ast.nodes.
	 * ClassDeclaration)
	 */
	@Override
	public void endVisit(ClassDeclaration classDeclaration)
	{
		nodeBuilder.handleCommonDeclarationEnd();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#endVisit(org2.eclipse.php.internal.core.ast.nodes.
	 * FunctionDeclaration)
	 */
	@Override
	public void endVisit(FunctionDeclaration functionDeclaration)
	{
		nodeBuilder.handleCommonDeclarationEnd();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#endVisit(org2.eclipse.php.internal.core.ast.nodes.
	 * InterfaceDeclaration)
	 */
	@Override
	public void endVisit(InterfaceDeclaration interfaceDeclaration)
	{
		nodeBuilder.handleCommonDeclarationEnd();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#endVisit(org2.eclipse.php.internal.core.ast.nodes.
	 * NamespaceDeclaration)
	 */
	@Override
	public void endVisit(NamespaceDeclaration namespaceDeclaration)
	{
		nodeBuilder.handleCommonDeclarationEnd();
	}

	/**
	 * @param interfaces
	 */
	private void handleInterfaces(List<Identifier> interfaces)
	{
		String[] extendedInterfacesNames = new String[interfaces.size()];
		int[][] extendedInterfacesStartEnd = new int[extendedInterfacesNames.length][2];
		for (int i = 0; i < extendedInterfacesNames.length; i++)
		{
			Identifier interfaceName = interfaces.get(i);
			extendedInterfacesNames[i] = interfaceName.getName();
			extendedInterfacesStartEnd[i][0] = interfaceName.getStart();
			extendedInterfacesStartEnd[i][1] = interfaceName.getEnd() - 1;
		}
		nodeBuilder.handleImplements(extendedInterfacesNames, extendedInterfacesStartEnd);
	}

	/**
	 * Converts a {@link org2.eclipse.php.internal.core.compiler.ast.nodes.PHPDocBlock} to a {@link PHPDocBlockImp}.
	 * 
	 * @param docComment
	 * @return A new {@link PHPDocBlockImp}, or null if the given docComment is null
	 */
	private PHPDocBlockImp convertToDocBlock(org2.eclipse.php.internal.core.compiler.ast.nodes.PHPDocBlock docComment)
	{
		if (docComment == null)
		{
			return null;
		}
		IPHPDocTag[] docTags = docComment.getTags();
		IPHPDocTag[] tags = new IPHPDocTag[docTags.length];
		for (int i = 0; i < docTags.length; i++)
		{
			tags[i] = BasicPHPDocTag.fromASTDocTag(docTags[i]);
		}
		return new PHPDocBlockImp(docComment.getShortDescription(), "", tags, docComment.getCommentType()); //$NON-NLS-1$
	}

}