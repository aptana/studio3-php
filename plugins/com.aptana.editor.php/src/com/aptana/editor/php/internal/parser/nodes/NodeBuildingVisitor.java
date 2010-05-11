/**
 * 
 */
package com.aptana.editor.php.internal.parser.nodes;

import java.util.List;

import org.eclipse.php.internal.core.ast.nodes.ClassConstantDeclaration;
import org.eclipse.php.internal.core.ast.nodes.ClassDeclaration;
import org.eclipse.php.internal.core.ast.nodes.ConstantDeclaration;
import org.eclipse.php.internal.core.ast.nodes.Expression;
import org.eclipse.php.internal.core.ast.nodes.FieldsDeclaration;
import org.eclipse.php.internal.core.ast.nodes.FormalParameter;
import org.eclipse.php.internal.core.ast.nodes.FunctionDeclaration;
import org.eclipse.php.internal.core.ast.nodes.FunctionInvocation;
import org.eclipse.php.internal.core.ast.nodes.Identifier;
import org.eclipse.php.internal.core.ast.nodes.InLineHtml;
import org.eclipse.php.internal.core.ast.nodes.Include;
import org.eclipse.php.internal.core.ast.nodes.InterfaceDeclaration;
import org.eclipse.php.internal.core.ast.nodes.MethodDeclaration;
import org.eclipse.php.internal.core.ast.nodes.NamespaceDeclaration;
import org.eclipse.php.internal.core.ast.nodes.ParenthesisExpression;
import org.eclipse.php.internal.core.ast.nodes.UseStatement;
import org.eclipse.php.internal.core.ast.nodes.UseStatementPart;
import org.eclipse.php.internal.core.ast.nodes.Variable;
import org.eclipse.php.internal.core.phpModel.phpElementData.PHPDocBlock;

import com.aptana.editor.php.utils.PHPASTVisitorStub;

public final class NodeBuildingVisitor extends PHPASTVisitorStub
{
	private final NodeBuilderClient parserClient;

	public NodeBuildingVisitor(NodeBuilderClient parserClient)
	{
		this.parserClient = parserClient;
	}

	@Override
	public void endVisit(ClassConstantDeclaration classConstantDeclaration)
	{

		Identifier[] variableNames = classConstantDeclaration.getVariableNames();
		for (Identifier i : variableNames)
		{
			parserClient.handleClassConstDeclaration(i.getName(), null, i.getStart(), i.getEnd(),
					classConstantDeclaration.getEnd());
		}
		super.endVisit(classConstantDeclaration);
	}

	@Override
	public void endVisit(ClassDeclaration classDeclaration)
	{
		Identifier name = classDeclaration.getName();
		parserClient.handleClassDeclarationEnds(name.getName(), name.getEnd() - 1);
		super.endVisit(classDeclaration);
	}

	@Override
	public boolean visit(InLineHtml inLineHtml)
	{
		parserClient.handlePHPEnd(inLineHtml.getStart(), -1);
		parserClient.handlePHPStart(inLineHtml.getEnd(), -1);
		return super.visit(inLineHtml);
	}

	@Override
	public void endVisit(InterfaceDeclaration classDeclaration)
	{
		parserClient.handleClassDeclarationEnds(classDeclaration.getName().getName(), classDeclaration.getStart());
		super.endVisit(classDeclaration);

	}

	@Override
	public boolean visit(InterfaceDeclaration classDeclaration)
	{
		parserClient.hadleClassDeclarationStarts(classDeclaration.getName().getName(), classDeclaration.getStart());
		String name = classDeclaration.getName().getName();

		Identifier[] interfaces = classDeclaration.getInterfaces();
		String[] iNames = new String[interfaces.length];
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
		if (interfaces.length == 0)
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

		Identifier[] interfaces = classDeclaration.getInterfaces();
		String[] iNames = new String[interfaces.length];
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
		if (interfaces.length == 0)
		{
			string = null;
		}
		parserClient.handleClassDeclaration(name, 0, classDeclaration.getSuperClass() == null ? null : classDeclaration
				.getSuperClass().getName(), string, null, nameIdentifier.getStart(), nameIdentifier.getEnd(), -1);
		return super.visit(classDeclaration);
	}

	@Override
	public void endVisit(FunctionInvocation functionInvocation)
	{
		if (functionInvocation.getName().equals("define")) //$NON-NLS-1$
		{
			Expression[] parameters = functionInvocation.getParameters();
			if (parameters.length > 0)
			{
				String name = parameters[0].getName();
				String value = parameters[1].getName();
				parserClient.handleDefine(name, value, null, functionInvocation.getStart(),
						functionInvocation.getEnd(), functionInvocation.getEnd());
			}
		}
		super.endVisit(functionInvocation);
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
			Expression variableName = v.getVariableName();
			vars.append(variableName.getName());
			vars.append(',');
		}
		vars = vars.deleteCharAt(vars.length() - 1);
		String variables = vars.toString();
		parserClient.handleClassVariablesDeclaration(variables, modifier, docInfo, startPosition, endPosition,
				stopPosition);
		super.visit(fieldsDeclaration);
		return true;
	}

	@Override
	public void endVisit(FieldsDeclaration fieldsDeclaration)
	{
		super.endVisit(fieldsDeclaration);
	}

	@Override
	public void endVisit(FunctionDeclaration functionDeclaration)
	{
		boolean isClassFunction = functionDeclaration.getParent() instanceof MethodDeclaration;
		// FormalParameter[] formalParameters = functionDeclaration.getFormalParameters();

		Identifier functionName = functionDeclaration.getFunctionName();
		parserClient.handleFunctionDeclarationEnds(functionName.getName(), isClassFunction,
				functionName.getEnd() - 1);

		super.endVisit(functionDeclaration);
	}

	@Override
	public boolean visit(ClassConstantDeclaration node)
	{
		Identifier[] variableNames = node.getVariableNames();
		for (Identifier i : variableNames)
		{
			parserClient.handleClassConstDeclaration(i.getName(), null, i.getStart(), i.getEnd(), i.getEnd());
		}
		return super.visit(node);
	}

	@Override
	public void endVisit(Include include)
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
		Expression expr = include.getExpr();
		if (expr instanceof ParenthesisExpression)
		{
			ParenthesisExpression pa = (ParenthesisExpression) expr;
			expr = pa.getExpr();
		}
		parserClient.handleIncludedFile(includeType, expr.getName(), null, include.getStart(), include.getEnd(),
				include.getEnd(), -1);
		super.endVisit(include);
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
		FormalParameter[] formalParameters = functionDeclaration.getFormalParameters();
		for (FormalParameter p : formalParameters)
		{
			String type = p.getParameterType() != null ? p.getParameterType().getName() : null;
			String vName = p.getParameterName() != null ? p.getParameterName().getName() : null;

			parserClient.handleFunctionParameter(type, vName, false, false, p.getDefaultValue() != null ? p
					.getDefaultValue().getName() : null, p.getStart(), p.getEnd(), p.getEnd(), -1);
		}
		Identifier functionName = functionDeclaration.getFunctionName();
		parserClient.handleFunctionDeclaration(functionName.getName(), isClassFunction,
				modifiers, null, functionName.getStart(), functionName.getEnd(), -1);
		return super.visit(functionDeclaration);
	}

}