package com.aptana.editor.php.internal.parser.nodes;

import java.util.List;

import org.eclipse.php.core.compiler.PHPFlags;
import org.eclipse.php.internal.core.ast.nodes.ASTNode;
import org.eclipse.php.internal.core.ast.nodes.ClassDeclaration;
import org.eclipse.php.internal.core.ast.nodes.ConstantDeclaration;
import org.eclipse.php.internal.core.ast.nodes.Expression;
import org.eclipse.php.internal.core.ast.nodes.FieldsDeclaration;
import org.eclipse.php.internal.core.ast.nodes.FormalParameter;
import org.eclipse.php.internal.core.ast.nodes.FunctionDeclaration;
import org.eclipse.php.internal.core.ast.nodes.Identifier;
import org.eclipse.php.internal.core.ast.nodes.InLineHtml;
import org.eclipse.php.internal.core.ast.nodes.Include;
import org.eclipse.php.internal.core.ast.nodes.InterfaceDeclaration;
import org.eclipse.php.internal.core.ast.nodes.MethodDeclaration;
import org.eclipse.php.internal.core.ast.nodes.NamespaceDeclaration;
import org.eclipse.php.internal.core.ast.nodes.NamespaceName;
import org.eclipse.php.internal.core.ast.nodes.ParenthesisExpression;
import org.eclipse.php.internal.core.ast.nodes.Scalar;
import org.eclipse.php.internal.core.ast.nodes.UseStatement;
import org.eclipse.php.internal.core.ast.nodes.UseStatementPart;
import org.eclipse.php.internal.core.ast.nodes.Variable;
import org.eclipse.php.internal.core.ast.visitor.AbstractVisitor;
import org.eclipse.php.internal.core.phpModel.phpElementData.PHPDocBlock;

/**
 * An AST visitor that is used to build the PHP outline nodes.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public final class NodeBuildingVisitor extends AbstractVisitor
{
	private final NodeBuilder nodeBuilder;

	/**
	 * Construct a new visitor with a given NodeBuilder that will be used for the actual creation of PHP nodes.s
	 * 
	 * @param nodeBuilder
	 */
	public NodeBuildingVisitor(NodeBuilder nodeBuilder)
	{
		this.nodeBuilder = nodeBuilder;
	}

	@Override
	public boolean visit(InLineHtml inLineHtml)
	{
		nodeBuilder.handlePHPEnd(inLineHtml.getStart(), -1);
		nodeBuilder.handlePHPStart(inLineHtml.getEnd(), -1);
		return super.visit(inLineHtml);
	}

	@Override
	public boolean visit(InterfaceDeclaration interfaceDeclaration)
	{
		Identifier nameIdentifier = interfaceDeclaration.getName();
		String name = nameIdentifier.getName();
		nodeBuilder.handleClassDeclaration(name, PHPFlags.AccInterface, null, interfaceDeclaration.getStart(),
				interfaceDeclaration.getEnd() - 1, -1);
		List<Identifier> interfaces = interfaceDeclaration.interfaces();
		handleInterfaces(interfaces);
		nodeBuilder.setNodeName(nameIdentifier);
		return super.visit(interfaceDeclaration);
	}

	@Override
	public boolean visit(ClassDeclaration classDeclaration)
	{
		Identifier nameIdentifier = classDeclaration.getName();
		String name = nameIdentifier.getName();
		nodeBuilder.handleClassDeclaration(name, classDeclaration.getModifier(), null, classDeclaration.getStart(),
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
		PHPDocBlock docInfo = null;
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
		if (expr != null && expr.getType() == ASTNode.SCALAR)
		{
			nodeBuilder.handleIncludedFile(includeType, ((Scalar) expr).getStringValue(), null, expr.getStart(), expr
					.getEnd() - 1, -1, -1);
		}
		return super.visit(include);
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
		List<Identifier> segments = name.segments();
		StringBuilder stringBuilder = new StringBuilder();
		for (Identifier i : segments)
		{
			stringBuilder.append(i.getName());
			stringBuilder.append('\\');
		}
		stringBuilder.deleteCharAt(stringBuilder.length() - 1);
		String segmentsString = stringBuilder.toString();
		nodeBuilder.handleNamespaceDeclaration(segmentsString, node.getStart(), node.getEnd() - 1, name.getEnd() - 1);
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
			nodeBuilder.handleUse(segmentsString, alias != null ? alias.getName() : null, node.getStart(), node
					.getEnd() - 1);
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

			nodeBuilder.handleFunctionParameter(type, vName, false, false, defaultVal, p.getStart(), p.getEnd(), p
					.getEnd() - 1, -1);
		}
		Identifier functionName = functionDeclaration.getFunctionName();
		nodeBuilder.handleFunctionDeclaration(functionName.getName(), isClassFunction, modifiers, null,
				functionDeclaration.getStart(), functionDeclaration.getEnd() - 1, -1);
		nodeBuilder.setNodeName(functionName);
		return super.visit(functionDeclaration);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#endVisit(org.eclipse.php.internal.core.ast.nodes.
	 * ClassDeclaration)
	 */
	@Override
	public void endVisit(ClassDeclaration classDeclaration)
	{
		nodeBuilder.handleClassDeclarationEnd(classDeclaration);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#endVisit(org.eclipse.php.internal.core.ast.nodes.
	 * FunctionDeclaration)
	 */
	@Override
	public void endVisit(FunctionDeclaration functionDeclaration)
	{
		nodeBuilder.handleFunctionDeclarationEnd(functionDeclaration);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#endVisit(org.eclipse.php.internal.core.ast.nodes.
	 * InterfaceDeclaration)
	 */
	@Override
	public void endVisit(InterfaceDeclaration interfaceDeclaration)
	{
		nodeBuilder.handleClassDeclarationEnd(interfaceDeclaration);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.php.internal.core.ast.visitor.AbstractVisitor#endVisit(org.eclipse.php.internal.core.ast.nodes.
	 * NamespaceDeclaration)
	 */
	@Override
	public void endVisit(NamespaceDeclaration namespaceDeclaration)
	{
		// TODO Auto-generated method stub
		nodeBuilder.handleNamespaceDeclarationEnd(namespaceDeclaration);
	}

	/**
	 * @param interfaces
	 */
	protected void handleInterfaces(List<Identifier> interfaces)
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

}