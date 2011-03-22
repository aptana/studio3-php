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
/**
 * 
 */
package org2.eclipse.php.internal.core.search;


import org2.eclipse.php.core.compiler.PHPFlags;
import org2.eclipse.php.internal.core.CoreMessages;
import org2.eclipse.php.internal.core.ast.nodes.ASTNode;
import org2.eclipse.php.internal.core.ast.nodes.ClassDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.IBinding;
import org2.eclipse.php.internal.core.ast.nodes.Identifier;
import org2.eclipse.php.internal.core.ast.nodes.InterfaceDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.MethodDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.NamespaceDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.NamespaceName;
import org2.eclipse.php.internal.core.ast.nodes.Program;
import org2.eclipse.php.internal.core.ast.nodes.StructuralPropertyDescriptor;
import org2.eclipse.php.internal.core.ast.nodes.TypeDeclaration;

import com.aptana.editor.php.core.typebinding.IMethodBinding;
import com.aptana.editor.php.core.typebinding.ITypeBinding;
import com.aptana.editor.php.internal.core.typebinding.Bindings;

/**
 * Finds all implement occurrences of an extended class or an implemented
 * interface.
 * 
 * @author Shalom
 * 
 */
public class ImplementOccurrencesFinder extends AbstractOccurrencesFinder {

	public static final String ID = "ImplementOccurrencesFinder"; //$NON-NLS-1$
	private Identifier fIdentifier;
	private TypeDeclaration fTypeDeclaration;
	private ITypeBinding fBinding;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org2.eclipse.php.internal.ui.search.IOccurrencesFinder#initialize(org.
	 * eclipse.php.internal.core.ast.nodes.Program,
	 * org2.eclipse.php.internal.core.ast.nodes.ASTNode)
	 */
	public String initialize(Program root, ASTNode node) {
		fASTRoot = root;
		if (isIdendifier(node)) {
			fIdentifier = (Identifier) node;
			StructuralPropertyDescriptor locationInParent;
			// [Aptana Mod] - Since in our model the NamespaceName holds the binding,
			// make sure that the "binding holder" is set currectly.
			Identifier bindingHolder = fIdentifier;
			if (fIdentifier.getParent() instanceof NamespaceName) {
				locationInParent = fIdentifier.getParent()
						.getLocationInParent();
				bindingHolder = (Identifier) fIdentifier.getParent();
			} else {
				locationInParent = fIdentifier.getLocationInParent();
			}
			if (locationInParent != NamespaceDeclaration.NAME_PROPERTY
					&& locationInParent != ClassDeclaration.SUPER_CLASS_PROPERTY
					&& locationInParent != ClassDeclaration.INTERFACES_PROPERTY
					&& locationInParent != InterfaceDeclaration.INTERFACES_PROPERTY) {
				return "ImplementOccurrencesFinder_invalidTarget"; //$NON-NLS-1$
			}
			IBinding resolvedBinding = bindingHolder.resolveBinding();
			if (resolvedBinding == null
					|| !(resolvedBinding instanceof ITypeBinding)) {
				return "ImplementOccurrencesFinder_invalidTarget"; //$NON-NLS-1$
			} else {
				fBinding = (ITypeBinding) resolvedBinding;
			}
			if (fIdentifier.getParent() instanceof NamespaceName) {
				fTypeDeclaration = (TypeDeclaration) fIdentifier.getParent()
						.getParent();
			} else {
				fTypeDeclaration = (TypeDeclaration) fIdentifier.getParent();
			}
			return null;
		}
		fDescription = "OccurrencesFinder_occurrence_description"; //$NON-NLS-1$
		return fDescription;
	}

	/*
	 * Check if the node is an identifier node.
	 */
	private final boolean isIdendifier(ASTNode node) {
		return node != null && node.getType() == ASTNode.IDENTIFIER;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org2.eclipse.php.internal.ui.search.AbstractOccurrencesFinder#findOccurrences
	 * ()
	 */
	@Override
	protected void findOccurrences()
	{
		fDescription = Messages.format(CoreMessages.getString("ImplementOccurrencesFinder.3"), fIdentifier.getName()); //$NON-NLS-1$
		String baseDescription = Messages.format(BASE_DESCRIPTION, fIdentifier.getName());
		fTypeDeclaration.accept(this);
		fResult.add(new OccurrenceLocation(fIdentifier.getStart(), fIdentifier.getLength(),
				getOccurrenceType(fIdentifier), baseDescription));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor#visit(org.eclipse
	 * .php.internal.core.ast.nodes.MethodDeclaration)
	 */
	public boolean visit(MethodDeclaration methodDeclaration) {
		IMethodBinding methodBinding = methodDeclaration.resolveMethodBinding();
		if (methodBinding != null
				&& !PHPFlags.isStatic(methodBinding.getModifiers())) {
			IMethodBinding method = Bindings.findOverriddenMethodInHierarchy(
					fBinding, methodBinding);
			if (method != null) {
				Identifier name = methodDeclaration.getFunction()
						.getFunctionName();
				fResult.add(new OccurrenceLocation(name.getStart(), name
						.getLength(), 0, fDescription));
			}
		}
		return super.visit(methodDeclaration);
	}

	/**
	 * Always returns IOccurrencesFinder.K_IMPLEMENTS_OCCURRENCE.
	 * 
	 * @see AbstractOccurrencesFinder#getOccurrenceType(ASTNode)
	 * @see IOccurrencesFinder#K_IMPLEMENTS_OCCURRENCE
	 */
	protected int getOccurrenceType(ASTNode node) {
		return IOccurrencesFinder.K_IMPLEMENTS_OCCURRENCE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org2.eclipse.php.internal.ui.search.IOccurrencesFinder#getElementName()
	 */
	public String getElementName() {
		return fIdentifier.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org2.eclipse.php.internal.ui.search.IOccurrencesFinder#getID()
	 */
	public String getID() {
		return ID;
	}
}
