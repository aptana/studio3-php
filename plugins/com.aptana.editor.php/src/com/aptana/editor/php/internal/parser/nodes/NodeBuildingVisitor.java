package com.aptana.editor.php.internal.parser.nodes;

import java.util.List;

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
import org.eclipse.php.internal.core.ast.nodes.ParenthesisExpression;
import org.eclipse.php.internal.core.ast.nodes.Scalar;
import org.eclipse.php.internal.core.ast.nodes.UseStatement;
import org.eclipse.php.internal.core.ast.nodes.UseStatementPart;
import org.eclipse.php.internal.core.ast.nodes.Variable;
import org.eclipse.php.internal.core.ast.visitor.AbstractVisitor;
import org.eclipse.php.internal.core.phpModel.phpElementData.PHPDocBlock;

public final class NodeBuildingVisitor extends AbstractVisitor
{
	private final NodeBuilderClient parserClient;

	public NodeBuildingVisitor(NodeBuilderClient parserClient)
	{
		this.parserClient = parserClient;
	}

	@Override
	public boolean visit(InLineHtml inLineHtml)
	{
		parserClient.handlePHPEnd(inLineHtml.getStart(), -1);
		parserClient.handlePHPStart(inLineHtml.getEnd(), -1);
		return super.visit(inLineHtml);
	}

	@Override
	public boolean visit(InterfaceDeclaration classDeclaration)
	{
		parserClient.hadleClassDeclarationStarts(classDeclaration.getName().getName(), classDeclaration.getStart());
		String name = classDeclaration.getName().getName();

		List<Identifier> interfaces = classDeclaration.interfaces();
		String[] iNames = new String[interfaces.size()];
		StringBuilder bld = new StringBuilder();
		for (int a = 0; a < iNames.length; a++)
		{
			bld.append(iNames[a]);
			if (a != iNames.length - 1)
			{
				bld.append(',');
			}
		}

		String string = bld.toString();
		if (interfaces.size() == 0)
		{
			string = null;
		}
		parserClient.handleClassDeclaration(name, 0, null, string, null, classDeclaration.getStart(), classDeclaration
				.getEnd(), -1);
		return super.visit(classDeclaration);
	}

	@Override
	public boolean visit(ClassDeclaration classDeclaration)
	{
		Identifier nameIdentifier = classDeclaration.getName();
		parserClient.hadleClassDeclarationStarts(nameIdentifier.getName(), nameIdentifier.getStart());
		String name = nameIdentifier.getName();

		List<Identifier> interfaces = classDeclaration.interfaces();
		String[] iNames = new String[interfaces.size()];
		StringBuilder bld = new StringBuilder();
		for (int a = 0; a < iNames.length; a++)
		{
			bld.append(iNames[a]);
			if (a != iNames.length - 1)
			{
				bld.append(',');
			}
		}
		String string = bld.toString();
		if (interfaces.isEmpty())
		{
			string = null;
		}
		// TODO - Shalom - Take a look at the PDT ClassHighlighting (handle namespaces)
		Expression superClass = classDeclaration.getSuperClass();
		String superClassName = null;
		if (superClass != null && superClass instanceof Identifier)
		{
			superClassName = ((Identifier) superClass).getName();
		}
		parserClient.handleClassDeclaration(name, 0, superClassName, string, null, nameIdentifier.getStart(),
				nameIdentifier.getEnd(), -1);
		return super.visit(classDeclaration);
	}

	@Override
	public boolean visit(FieldsDeclaration fieldsDeclaration)
	{
		int modifier = fieldsDeclaration.getModifier();
		int startPosition = fieldsDeclaration.getStart();
		int endPosition = fieldsDeclaration.getStart();
		int stopPosition = endPosition;
		PHPDocBlock docInfo = null;
		StringBuilder vars = new StringBuilder();
		for (Variable v : fieldsDeclaration.getVariableNames())
		{
			// TODO - Shalom: TEST this
			Expression variableName = v.getName();
			if (variableName.isStaticScalar())
			{
				vars.append(((Scalar) variableName).getStringValue());
				vars.append(',');
			}
		}
		vars = vars.deleteCharAt(vars.length() - 1);
		String variables = vars.toString();
		parserClient.handleClassVariablesDeclaration(variables, modifier, docInfo, startPosition, endPosition,
				stopPosition);
		super.visit(fieldsDeclaration);
		return true;
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
		if (expr instanceof ParenthesisExpression)
		{
			ParenthesisExpression pa = (ParenthesisExpression) expr;
			expr = pa.getExpression();
		}
		if (expr instanceof Scalar)
		{
			parserClient.handleIncludedFile(includeType, ((Scalar) expr).getStringValue(), null, include.getStart(),
					include.getEnd(), include.getEnd(), -1);
		}
		super.visit(include);
		return true;
	}

	@Override
	public boolean visit(ConstantDeclaration node)
	{
		List<Identifier> variableNames = node.names();
		for (Identifier i : variableNames)
		{
			parserClient.handleDefine('"' + i.getName() + '"', "...", null, i.getStart(), i.getEnd(), i.getEnd()); //$NON-NLS-1$
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(NamespaceDeclaration node)
	{
		List<Identifier> segments = node.getName().segments();
		StringBuilder stringBuilder = new StringBuilder();
		for (Identifier i : segments)
		{
			stringBuilder.append(i.getName());
			stringBuilder.append('\\');
		}
		stringBuilder.deleteCharAt(stringBuilder.length() - 1);
		String segmentsString = stringBuilder.toString();
		parserClient.handleNamespace(segmentsString, node.getStart(), node.getEnd());
		return true;
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
			parserClient.handleUse(segmentsString, alias != null ? alias.getName() : null, node.getStart(), node
					.getEnd());
		}
		return super.visit(node);
	}

	@Override
	public boolean visit(FunctionDeclaration functionDeclaration)
	{
		boolean isClassFunction = functionDeclaration.getParent() instanceof MethodDeclaration;
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
			if (parameterType instanceof Variable)
				type = ((Identifier) ((Variable) parameterType).getName()).getName();
			if (parameterName instanceof Variable)
				vName = ((Identifier) ((Variable) parameterName).getName()).getName();
			if (defaultValue != null && defaultValue.isStaticScalar())
				defaultVal = ((Scalar) defaultValue).getStringValue();

			parserClient.handleFunctionParameter(type, vName, false, false, defaultVal, p.getStart(), p.getEnd(), p
					.getEnd(), -1);
		}
		Identifier functionName = functionDeclaration.getFunctionName();
		parserClient.handleFunctionDeclaration(functionName.getName(), isClassFunction, modifiers, null, functionName
				.getStart(), functionName.getEnd(), -1);
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.aptana.editor.php.utils.PHPASTVisitorStub#endVisit(org.eclipse.php.internal.core.ast.nodes.ClassDeclaration)
	 */
	@Override
	public void endVisit(ClassDeclaration classDeclaration)
	{
		parserClient.handleClassDeclarationEnds(null, -1);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.aptana.editor.php.utils.PHPASTVisitorStub#endVisit(org.eclipse.php.internal.core.ast.nodes.ConstantDeclaration
	 * )
	 */
	@Override
	public void endVisit(ConstantDeclaration node)
	{
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.aptana.editor.php.utils.PHPASTVisitorStub#endVisit(org.eclipse.php.internal.core.ast.nodes.FieldsDeclaration)
	 */
	@Override
	public void endVisit(FieldsDeclaration fieldsDeclaration)
	{
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.aptana.editor.php.utils.PHPASTVisitorStub#endVisit(org.eclipse.php.internal.core.ast.nodes.FunctionDeclaration
	 * )
	 */
	@Override
	public void endVisit(FunctionDeclaration functionDeclaration)
	{
		parserClient.handleFunctionDeclarationEnds(null, false, -1);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.utils.PHPASTVisitorStub#endVisit(org.eclipse.php.internal.core.ast.nodes.Include)
	 */
	@Override
	public void endVisit(Include include)
	{
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.utils.PHPASTVisitorStub#endVisit(org.eclipse.php.internal.core.ast.nodes.InLineHtml)
	 */
	@Override
	public void endVisit(InLineHtml inLineHtml)
	{
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.aptana.editor.php.utils.PHPASTVisitorStub#endVisit(org.eclipse.php.internal.core.ast.nodes.InterfaceDeclaration
	 * )
	 */
	@Override
	public void endVisit(InterfaceDeclaration interfaceDeclaration)
	{
		parserClient.handleClassDeclarationEnds(null, -1);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.aptana.editor.php.utils.PHPASTVisitorStub#endVisit(org.eclipse.php.internal.core.ast.nodes.NamespaceDeclaration
	 * )
	 */
	@Override
	public void endVisit(NamespaceDeclaration node)
	{
		// Do nothing
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.utils.PHPASTVisitorStub#endVisit(org.eclipse.php.internal.core.ast.nodes.UseStatement)
	 */
	@Override
	public void endVisit(UseStatement node)
	{
		// Do nothing
	}
}