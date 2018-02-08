/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Zend Technologies
 *******************************************************************************/
package org2.eclipse.php.internal.core.search;

import java.util.List;

import org2.eclipse.php.internal.core.CoreMessages;
import org2.eclipse.php.internal.core.ast.nodes.ASTNode;
import org2.eclipse.php.internal.core.ast.nodes.ASTNodes;
import org2.eclipse.php.internal.core.ast.nodes.ClassDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.ClassInstanceCreation;
import org2.eclipse.php.internal.core.ast.nodes.ClassName;
import org2.eclipse.php.internal.core.ast.nodes.Expression;
import org2.eclipse.php.internal.core.ast.nodes.FormalParameter;
import org2.eclipse.php.internal.core.ast.nodes.FunctionInvocation;
import org2.eclipse.php.internal.core.ast.nodes.Identifier;
import org2.eclipse.php.internal.core.ast.nodes.Include;
import org2.eclipse.php.internal.core.ast.nodes.InterfaceDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.NamespaceName;
import org2.eclipse.php.internal.core.ast.nodes.Program;
import org2.eclipse.php.internal.core.ast.nodes.StaticMethodInvocation;
import org2.eclipse.php.internal.core.ast.nodes.StructuralPropertyDescriptor;
import org2.eclipse.php.internal.core.ast.nodes.Variable;

import com.aptana.editor.php.core.model.IMethod;
import com.aptana.editor.php.core.model.IModelElement;
import com.aptana.editor.php.core.model.ISourceModule;
import com.aptana.editor.php.core.model.IType;
import com.aptana.editor.php.core.typebinding.IBinding;


public class IncludeOccurrencesFinder extends AbstractOccurrencesFinder {

	private static final String INCLUDE_POINT_OF = CoreMessages
			.getString("IncludeOccurrencesFinder.0"); //$NON-NLS-1$
	public static final String ID = "RequireFinder"; //$NON-NLS-1$
	private IModelElement source;
	private IBinding binding;
	private Include includeNode;
	private List<IType> types;
	private List<IMethod> methods;

	/**
	 * @param root
	 *            the AST root
	 * @param node
	 *            the selected node
	 * @return returns a message if there is a problem
	 */
	public String initialize(Program root, ASTNode node) {
		fASTRoot = root;

		this.includeNode = getIncludeExpression(node);
		if (this.includeNode != null) {
			binding = includeNode.resolveBinding();
			if (binding == null) {
				return null;
			}
			source = binding.getPHPElement();
			if (source != null) {
				ISourceModule module = (ISourceModule) source;
				try {
					this.types = module.getTopLevelTypes();
					this.methods = module.getTopLevelMethods();
					return null;
				} catch (Exception e) {
					fDescription = "MethodExitsFinder_occurrence_exit_description"; //$NON-NLS-1$
					return fDescription;
				}
			}

		}
		fDescription = "MethodExitsFinder_occurrence_exit_description"; //$NON-NLS-1$
		return fDescription;
	}

	private final Include getIncludeExpression(ASTNode node) {
		boolean isInclude = (node != null && node.getType() == ASTNode.INCLUDE);
		if (isInclude) {
			return (Include) node;
		}
		ASTNode parent = ASTNodes.getParent(node, Include.class);
		return (parent != null && parent.getType() == ASTNode.INCLUDE) ? (Include) parent
				: null;
	}

	protected void findOccurrences() {
		if (source == null) {
			return;
		}
		fDescription = Messages.format(INCLUDE_POINT_OF, this.source
				.getElementName());
		getASTRoot().accept(this);
		int offset = includeNode.getStart();
		int length = includeNode.getLength();
		fResult.add(new OccurrenceLocation(offset, length,
				getOccurrenceType(null), fDescription));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org2.eclipse.php.internal.ui.search.AbstractOccurrencesFinder#
	 * getOccurrenceReadWriteType
	 * (org2.eclipse.php.internal.core.ast.nodes.ASTNode)
	 */
	protected int getOccurrenceType(ASTNode node) {
		return IOccurrencesFinder.K_OCCURRENCE;
	}

	public String getElementName() {
		return binding.getName();
	}

	public String getID() {
		return ID;
	}

	public String getJobLabel() {
		return "RncludeFinder_job_label"; //$NON-NLS-1$
	}

	public int getSearchKind() {
		return IOccurrencesFinder.K_EXIT_POINT_OCCURRENCE;
	}

	public String getUnformattedPluralLabel() {
		return "IncludeFinder_label_plural"; //$NON-NLS-1$
	}

	public String getUnformattedSingularLabel() {
		return "IncludeFinder_label_singular"; //$NON-NLS-1$
	}

	@Override
	public boolean visit(ClassName className)
	{
		Expression expression = className.getName();
		markImportedTypes(expression);
		return false;
	}

	/* (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.ClassDeclaration)
	 */
	@Override
	public boolean visit(ClassDeclaration classDeclaration)
	{
		Expression superClass = classDeclaration.getSuperClass();
		markImportedTypes(superClass);
		List<Identifier> interfaces = classDeclaration.interfaces();
		for (Identifier interfaceIdentifier : interfaces)
		{
			markImportedTypes(interfaceIdentifier);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org2.eclipse.php.internal.core.ast.nodes.InterfaceDeclaration)
	 */
	@Override
	public boolean visit(InterfaceDeclaration interfaceDeclaration)
	{
		// TODO Auto-generated method stub
		return super.visit(interfaceDeclaration);
	}

	/* (non-Javadoc)
	 * @see org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#endVisit(org2.eclipse.php.internal.core.ast.nodes.ClassInstanceCreation)
	 */
	@Override
	public boolean visit(ClassInstanceCreation classInstanceCreation)
	{
		ClassName className = classInstanceCreation.getClassName();
		Expression expression = className.getName();
		markImportedTypes(expression);
		return false;
	}

	private void markImportedTypes(Expression expression)
	{
		String name = null;
		if (expression == null)
		{
			return;
		}
		if (expression.getType() == ASTNode.NAMESPACE_NAME)
		{
			name = ((NamespaceName) expression).getName();
		}
		if (name == null && expression.getType() == ASTNode.IDENTIFIER)
		{
			Identifier id = (Identifier) expression;
			name = id.getName();
		}
		if (name != null)
		{
			for (IType type : types)
			{
				if (type.getElementName().equals(name))
					fResult.add(new OccurrenceLocation(expression.getStart(), expression.getLength(),
							getOccurrenceType(null), fDescription));
			}
		}
	}

	@Override
	public boolean visit(Identifier className) {
		final StructuralPropertyDescriptor location = className
				.getLocationInParent();
		if (location == ClassDeclaration.SUPER_CLASS_PROPERTY
				|| location == ClassDeclaration.INTERFACES_PROPERTY
				|| location == StaticMethodInvocation.CLASS_NAME_PROPERTY
				|| location == FormalParameter.PARAMETER_TYPE_PROPERTY) {
			String name = className.getName();
			for (IType type : types) {
				if (type.getElementName().equals(name))
					fResult.add(new OccurrenceLocation(className.getStart(),
							className.getLength(), getOccurrenceType(null),
							fDescription));
			}
		}
		return false;
	}

	@Override
	public boolean visit(FunctionInvocation functionInvocation)
	{
		Expression functionName2 = functionInvocation.getFunctionName().getName();
		if (functionName2.getType() == ASTNode.VARIABLE)
		{
			functionName2 = ((Variable) functionName2).getName();
		}
		if (functionName2.getType() == ASTNode.IDENTIFIER)
		{
			Identifier id = (Identifier) functionName2;
			String name = id.getName();
			for (IMethod method : methods)
			{
				if (method.getElementName().equals(name))
					fResult.add(new OccurrenceLocation(functionInvocation.getStart(), functionInvocation.getLength(),
							getOccurrenceType(null), fDescription));
			}
		}
		return true;
	}
}
