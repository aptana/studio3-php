/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.typebinding;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org2.eclipse.php.core.compiler.PHPFlags;
import org2.eclipse.php.internal.core.ast.nodes.ASTNode;
import org2.eclipse.php.internal.core.ast.nodes.Assignment;
import org2.eclipse.php.internal.core.ast.nodes.Block;
import org2.eclipse.php.internal.core.ast.nodes.CatchClause;
import org2.eclipse.php.internal.core.ast.nodes.ClassDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.ClassInstanceCreation;
import org2.eclipse.php.internal.core.ast.nodes.ClassName;
import org2.eclipse.php.internal.core.ast.nodes.Comment;
import org2.eclipse.php.internal.core.ast.nodes.ConstantDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.Dispatch;
import org2.eclipse.php.internal.core.ast.nodes.DoStatement;
import org2.eclipse.php.internal.core.ast.nodes.Expression;
import org2.eclipse.php.internal.core.ast.nodes.ExpressionStatement;
import org2.eclipse.php.internal.core.ast.nodes.FieldAccess;
import org2.eclipse.php.internal.core.ast.nodes.FieldsDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.ForEachStatement;
import org2.eclipse.php.internal.core.ast.nodes.ForStatement;
import org2.eclipse.php.internal.core.ast.nodes.FormalParameter;
import org2.eclipse.php.internal.core.ast.nodes.FunctionDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.FunctionInvocation;
import org2.eclipse.php.internal.core.ast.nodes.FunctionName;
import org2.eclipse.php.internal.core.ast.nodes.GlobalStatement;
import org2.eclipse.php.internal.core.ast.nodes.Identifier;
import org2.eclipse.php.internal.core.ast.nodes.IfStatement;
import org2.eclipse.php.internal.core.ast.nodes.Include;
import org2.eclipse.php.internal.core.ast.nodes.InfixExpression;
import org2.eclipse.php.internal.core.ast.nodes.InterfaceDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.MethodDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.MethodInvocation;
import org2.eclipse.php.internal.core.ast.nodes.ParenthesisExpression;
import org2.eclipse.php.internal.core.ast.nodes.Program;
import org2.eclipse.php.internal.core.ast.nodes.ReturnStatement;
import org2.eclipse.php.internal.core.ast.nodes.Scalar;
import org2.eclipse.php.internal.core.ast.nodes.StaticConstantAccess;
import org2.eclipse.php.internal.core.ast.nodes.StaticDispatch;
import org2.eclipse.php.internal.core.ast.nodes.StaticFieldAccess;
import org2.eclipse.php.internal.core.ast.nodes.StaticMethodInvocation;
import org2.eclipse.php.internal.core.ast.nodes.StaticStatement;
import org2.eclipse.php.internal.core.ast.nodes.SwitchStatement;
import org2.eclipse.php.internal.core.ast.nodes.TryStatement;
import org2.eclipse.php.internal.core.ast.nodes.TypeDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.Variable;
import org2.eclipse.php.internal.core.ast.nodes.VariableBase;
import org2.eclipse.php.internal.core.ast.nodes.WhileStatement;
import org2.eclipse.php.internal.core.ast.visitor.AbstractVisitor;
import org2.eclipse.php.internal.core.documentModel.phpElementData.IPHPDoc;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.core.IPHPTypeConstants;
import com.aptana.editor.php.core.model.ISourceModule;
import com.aptana.editor.php.core.model.IType;
import com.aptana.editor.php.core.typebinding.IBinding;
import com.aptana.editor.php.core.typebinding.IBindingReporter;
import com.aptana.editor.php.core.typebinding.ITypeBinding;
import com.aptana.editor.php.core.typebinding.TypeBinding;
import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.indexer.IElementsIndex;
import com.aptana.editor.php.indexer.PHPGlobalIndexer;
import com.aptana.editor.php.internal.core.typebinding.MethodBinding;
import com.aptana.editor.php.internal.core.typebinding.ModuleBinding;
import com.aptana.editor.php.internal.indexer.CallPath;
import com.aptana.editor.php.internal.indexer.ClassPHPEntryValue;
import com.aptana.editor.php.internal.indexer.FunctionPHPEntryValue;
import com.aptana.editor.php.internal.indexer.FunctionPathReference;
import com.aptana.editor.php.internal.indexer.PHPDocUtils;
import com.aptana.editor.php.internal.indexer.PHPTypeProcessor;
import com.aptana.editor.php.internal.indexer.StaticPathReference;
import com.aptana.editor.php.internal.indexer.VariablePHPEntryValue;
import com.aptana.editor.php.internal.indexer.VariablePathReference;
import com.aptana.editor.php.internal.model.impl.SourceModule;
import com.aptana.editor.php.internal.parser.phpdoc.FunctionDocumentation;
import com.aptana.editor.php.internal.parser.phpdoc.TypedDescription;
import com.aptana.editor.php.internal.search.PHPSearchEngine;

/**
 * PHP type binding builder.
 * 
 * @author Denis Denisenko
 * @author Pavel Petrochenko
 * @author Shalom Gibly <sgibly@aptana.com>
 * @see #buildBindings(Program, String) for the main entry point of this class.
 */
@SuppressWarnings("unused")
public class TypeBindingBuilder
{

	/**
	 * This.
	 */
	private static final String THIS = "this"; //$NON-NLS-1$

	/**
	 * Self.
	 */
	private static final String SELF = "self"; //$NON-NLS-1$

	/**
	 * Define function name.
	 */
	private static final String DEFINE = "define"; //$NON-NLS-1$

	/**
	 * Main entry point for building the bindings for a given Program (AST).
	 * 
	 * @param program
	 */
	public static void buildBindings(Program program)
	{
		buildBindings(program, null);
	}

	/**
	 * Main entry point for building the bindings for a given Program (AST).
	 * 
	 * @param program
	 * @param source
	 */
	public static void buildBindings(Program program, String source)
	{
		if (program != null)
		{
			new TypeBindingBuilder().indexModule(program, source, new IBindingReporter()
			{

				public void report(ASTNode node, IBinding binding)
				{
					node.setBinding(binding);
				}
			});
			program.setBindingCompleted(true);
		}
		else
		{
			IdeLog.logError(PHPEditorPlugin.getDefault(), "Error building the PHP bindings for the AST", //$NON-NLS-1$
					new IllegalArgumentException("Cannot build the PHP bindings with a null AST")); //$NON-NLS-1$
		}
	}

	/**
	 * Variable info.
	 * 
	 * @author Denis Denisenko
	 */
	private static class VariableInfo
	{
		/**
		 * Variable name.
		 */
		private String variableName;

		/**
		 * Variable types.
		 */
		private Set<Object> variableTypes;

		/**
		 * Variable scope.
		 */
		private Scope scope;

		/**
		 * Node start.
		 */
		private int nodeStart = 0;

		private int modifier = 0;

		/**
		 * VariableInfo constructor.
		 * 
		 * @param variableName
		 *            - variable name.
		 * @param variableType
		 *            - variable type.
		 * @param scope
		 * @param pos
		 */
		private VariableInfo(String variableName, Object variableType, Scope scope, int pos)
		{
			this.variableName = variableName;
			variableTypes = new HashSet<Object>(1);
			if (variableType != null)
			{
				variableTypes.add(variableType);
			}
			this.scope = scope;
			nodeStart = pos;
		}

		/**
		 * VariableInfo constructor.
		 * 
		 * @param variableName
		 *            - variable name.
		 * @param variableTypes
		 *            - variable types.
		 * @param scope
		 * @param pos
		 */
		private VariableInfo(String variableName, Set<Object> variableTypes, Scope scope, int pos)
		{
			this.variableName = variableName;

			this.variableTypes = variableTypes;

			this.scope = scope;
			nodeStart = pos;
		}

		/**
		 * VariableInfo constructor.
		 * 
		 * @param variableName
		 *            - variable name.
		 * @param variableTypes
		 *            - variable types.
		 * @param scope
		 *            - variable scope.
		 * @param pos
		 *            - variable declaration position.
		 * @param modifier
		 *            - variable modifier.
		 */
		private VariableInfo(String variableName, Set<Object> variableTypes, Scope scope, int pos, int modifier)
		{
			this.variableName = variableName;

			this.variableTypes = variableTypes;

			this.scope = scope;
			nodeStart = pos;
			this.modifier = modifier;
		}

		/**
		 * Gets variable type.
		 * 
		 * @return variable type.
		 */
		public Set<Object> getVariableTypes()
		{
			if (variableTypes != null)
			{
				return variableTypes;
			}
			else
			{
				return Collections.emptySet();
			}
		}

		/**
		 * Sets variable type.
		 * 
		 * @param variableType
		 *            - type to set.
		 */
		public void setVariableType(Object variableType)
		{
			variableTypes = new HashSet<Object>(1);
			variableTypes.add(variableType);
		}

		/**
		 * Sets variable types.
		 * 
		 * @param variableType
		 */
		public void setVariableTypes(Collection<Object> variableType)
		{
			variableTypes = new HashSet<Object>();
			variableTypes.addAll(variableType);
		}

		/**
		 * Adds variable type.
		 * 
		 * @param variableType
		 *            - type to add.
		 */
		public void addVariableType(Object variableType)
		{
			if (variableTypes == null)
			{
				variableTypes = new HashSet<Object>();
			}
			variableTypes.add(variableType);
		}

		/**
		 * Gets variable name.
		 * 
		 * @return variable name.
		 */
		public String getName()
		{
			return variableName;
		}

		/**
		 * Gets scope.
		 * 
		 * @return scope.
		 */
		public Scope getScope()
		{
			return scope;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString()
		{
			StringBuffer buffer = new StringBuffer();
			buffer.append(variableName);
			buffer.append(" types:"); //$NON-NLS-1$
			buffer.append(variableTypes.toString());
			return buffer.toString();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((variableName == null) ? 0 : variableName.hashCode());
			return result;
		}

		/**
		 * Gets node start.
		 * 
		 * @return node start.
		 */
		public int getNodeStart()
		{
			return nodeStart;
		}

		// /**
		// * Sets node start.
		// * @param nodeStart - node start to set.
		// */
		// public void setNodeStart(int nodeStart)
		// {
		// this.nodeStart = nodeStart;
		// }

		/**
		 * Gets node modifier.
		 * 
		 * @return modifier
		 */
		public int getModifier()
		{
			return modifier;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final VariableInfo other = (VariableInfo) obj;
			if (variableName == null)
			{
				if (other.variableName != null)
					return false;
			}
			else if (!variableName.equals(other.variableName))
				return false;
			return true;
		}
	}

	/**
	 * Variables scope.
	 * 
	 * @author Denis Denisenko
	 */
	private static class Scope
	{
		/**
		 * Scope root.
		 */
		private ASTNode root;

		/**
		 * Root entry.
		 */
		private IElementEntry entry;

		/**
		 * Scope variables.
		 */
		private Map<String, VariableInfo> variables;

		/**
		 * Scope parent.
		 */
		private Scope parent;

		/**
		 * Set of variables imported from global scope.
		 */
		private Set<String> globalImports = new HashSet<String>();

		/**
		 * Scope constructor.
		 * 
		 * @param root
		 *            - scope root.
		 * @param parent
		 *            - scope parent.
		 */
		private Scope(ASTNode root, Scope parent)
		{
			this.root = root;
			this.parent = parent;
			this.entry = null;
			variables = new HashMap<String, VariableInfo>(1);
		}

		/**
		 * Scope constructor.
		 * 
		 * @param root
		 *            - scope root.
		 * @param parent
		 *            - scope parent.
		 * @param entry
		 *            - root entry if exist. null is acceptable.
		 */
		private Scope(ASTNode root, Scope parent, IElementEntry entry)
		{
			this.root = root;
			this.parent = parent;
			this.entry = entry;
			variables = new HashMap<String, VariableInfo>(1);
		}

		/**
		 * Gets scope global imports.
		 * 
		 * @return scope golobal imports.
		 */
		public Set<String> getGlobalImports()
		{
			return globalImports;
		}

		/**
		 * Adds variables to import from global scope.
		 * 
		 * @param imports
		 *            - set of imports.
		 */
		public void addGlobalImports(Set<String> imports)
		{
			globalImports.addAll(imports);
		}

		/**
		 * Adds variable to import from global scope.
		 * 
		 * @param importVariable
		 *            - name of variable to import.
		 */
		public void addGlobalImport(String importVariable)
		{
			globalImports.add(importVariable);
		}

		/**
		 * Gets whether this scope is global.
		 * 
		 * @return true if global, false otherwise.
		 */
		public boolean isGlobalScope()
		{
			return root instanceof Program;
		}

		/**
		 * Adds variable to the scope. If variable has a type specified and this or parent scopes do contain such a
		 * variable, current type would be added to the list of variable types.
		 * 
		 * @param variable
		 *            - variable info.
		 */
		public void addVariable(VariableInfo variable)
		{
			VariableInfo existingVariable = getVariable(variable.getName());
			if (existingVariable != null)
			{
				if (variable.getVariableTypes() != null && !variable.getVariableTypes().isEmpty())
				{
					if (this.equals(existingVariable.getScope()))
					{
						existingVariable.setVariableTypes(variable.getVariableTypes());
					}
					else
					{
						for (Object type : variable.getVariableTypes())
						{
							existingVariable.addVariableType(type);
						}
					}
				}
			}
			else
			{
				variables.put(variable.getName(), variable);
			}
		}

		/**
		 * Gets variable by name from current scope or nearest of the parent scopes.
		 * 
		 * @param name
		 *            - variable name.
		 * @return variable info.
		 */
		public VariableInfo getVariable(String name)
		{
			return getVariable(name, new HashSet<String>());
		}

		/**
		 * Gets variable by name from current scope or nearest of the parent scopes.
		 * 
		 * @param name
		 *            - variable name.
		 * @param importsFromGlobal
		 *            - set of variables that might be imported from global scope.
		 * @return variable info.
		 */
		public VariableInfo getVariable(String name, Set<String> importsFromGlobal)
		{
			if (globalImports != null && !globalImports.isEmpty())
			{
				importsFromGlobal.addAll(globalImports);
			}

			VariableInfo info = variables.get(name);

			if (info != null)
			{
				return info;
			}

			if (parent != null)
			{
				if (parent.isGlobalScope())
				{
					if (importsFromGlobal.contains(name)
							|| !(root instanceof FunctionDeclaration || root instanceof ClassDeclaration))
					{
						return parent.getVariable(name, importsFromGlobal);
					}
				}
				else
				{
					return parent.getVariable(name, importsFromGlobal);
				}
				/*
				 * if (!parent.isGlobalScope() || importsFromGlobal.contains(name)) { return parent.getVariable(name,
				 * importsFromGlobal); }
				 */
			}

			return null;
		}

		/**
		 * Gets root element entry.
		 * 
		 * @return rooot element entry.
		 */
		public IElementEntry getEntry()
		{
			return entry;
		}

		/**
		 * Gets scope root.
		 * 
		 * @return scope root node.
		 */
		public ASTNode getRoot()
		{
			return root;
		}

		/**
		 * Gets scope parent scope or null if parent not exist.
		 * 
		 * @return scope parent.
		 */
		public Scope getParent()
		{
			return parent;
		}

		/**
		 * Gets unmodifiable variables set including variables from the upper scope being aware of global statement.
		 * 
		 * @return unmodifiable variables set.
		 */
		public Set<VariableInfo> getVariables()
		{
			return getVariables(new HashSet<String>());
		}

		/**
		 * Gets variables or nearest of the parent scopes.
		 * 
		 * @param importsFromGlobal
		 *            - set of variables that might be imported from global scope.
		 * @return variables.
		 */
		private Set<VariableInfo> getVariables(Set<String> importsFromGlobal)
		{
			if (globalImports != null && !globalImports.isEmpty())
			{
				importsFromGlobal.addAll(globalImports);
			}

			Set<VariableInfo> result = new HashSet<VariableInfo>();

			for (VariableInfo variable : variables.values())
			{
				result.add(variable);
			}

			if (parent != null)
			{
				if (!parent.isGlobalScope())
				{
					result.addAll(parent.getVariables(importsFromGlobal));
				}
				else
				{
					if (automaticallyDeriveGlobalVariables())
					{
						result.addAll(parent.getVariables(new HashSet<String>()));
					}
					else
					{
						for (String var : importsFromGlobal)
						{
							VariableInfo varInfo = parent.getVariable(var, importsFromGlobal);
							if (varInfo != null)
							{
								result.add(varInfo);
							}
						}
					}
				}
			}

			return result;
		}

		/**
		 * Checks whether this node must automatically derive variables from the global scope.
		 * 
		 * @return true if this node must automatically derive variables from the global scope, false otherwise.
		 */
		private boolean automaticallyDeriveGlobalVariables()
		{
			if (!parent.isGlobalScope())
			{
				return false;
			}

			if (this.getRoot() instanceof TypeDeclaration || this.getRoot() instanceof FunctionDeclaration
					|| this.getRoot() instanceof MethodDeclaration)
			{
				return false;
			}

			return true;
		}
	}

	/**
	 * Class scope information.
	 * 
	 * @author Denis Denisenko
	 */
	private static class ClassScopeInfo
	{

		/**
		 * Class fields.
		 */
		private Map<String, IElementEntry> fields = new HashMap<String, IElementEntry>();
		private String name;

		/**
		 * ClassScopeInfo constructor.
		 * 
		 * @param classEntry
		 *            - class entry.
		 */
		private ClassScopeInfo()
		{

		}

		private ClassScopeInfo(String name)
		{
			this.name = name;
		}

		public String getName()
		{
			return name;
		}

		/**
		 * Gets class fields.
		 * 
		 * @return class fields.
		 */
		public Collection<IElementEntry> getFields()
		{
			return fields.values();
		}

		/**
		 * Checks whether class has a field with a name specified.
		 * 
		 * @param fieldName
		 *            - field name to check.
		 * @return true if has field, false otherwise.
		 */
		public boolean hasField(String fieldName)
		{
			return fields.containsKey(fieldName);
		}

		/**
		 * Gets field by name.
		 * 
		 * @param fieldName
		 *            - field name to get.
		 * @return field entry or null.
		 */
		public IElementEntry getField(String fieldName)
		{
			return fields.get(fieldName);
		}

		/**
		 * Adds new field.
		 * 
		 * @param fieldName
		 *            - field name.
		 * @param field
		 *            - field entry.
		 */
		public void setField(String fieldName, IElementEntry field)
		{
			fields.put(fieldName, field);
		}

		/**
		 * Adds field types.
		 * 
		 * @param fieldName
		 *            - field name.
		 * @param types
		 *            - types to add.
		 * @return true if types are added, false otherwise.
		 */
		public boolean addFieldTypes(String fieldName, Set<Object> types)
		{
			IElementEntry fieldEntry = fields.get(fieldName);
			if (fieldEntry == null)
			{
				return false;
			}

			Object entryValue = fieldEntry.getValue();
			if (!(entryValue instanceof VariablePHPEntryValue))
			{
				return false;
			}

			VariablePHPEntryValue value = (VariablePHPEntryValue) entryValue;
			for (Object type : types)
			{
				value.addType(type);
			}

			return true;
		}
	}

	/**
	 * AST visitor.
	 * 
	 * @author Denis Denisenko
	 */
	private class PHPASTVisitor extends AbstractVisitor
	{
		/**
		 * Reporter.
		 */
		private IBindingReporter reporter;

		private Map<String, ITypeBinding> binding = new HashMap<String, ITypeBinding>();
		/**
		 * Module.
		 */
		private ISourceModule module;

		/**
		 * Current class.
		 */
		private ClassScopeInfo currentClass;

		/**
		 * Current function.
		 */
		private IElementEntry currentFunction;

		/**
		 * Variable scopes.
		 */
		private Stack<Scope> scopes = new Stack<Scope>();

		private String currentNamespace = ""; //$NON-NLS-1$

		// /**
		// * Backuped variable scopes.
		// */
		// private Stack<Scope> backupedScopes = new Stack<Scope>();
		//
		// /**
		// * Scopes of the previous node.
		// */
		// private Stack<Scope> previousNodeScopes = new Stack<Scope>();

		/**
		 * Whether the local stack was reported (required for local mode).
		 */
		boolean localStackReported = false;

		private Map<String, Set<Object>> localParametersMap;

		/**
		 * PHPASTVisitor constructor.
		 * 
		 * @param reporter2
		 *            - reporter to use.
		 * @param program
		 *            - current module.
		 */
		private PHPASTVisitor(IBindingReporter reporter2, Program program)
		{
			this.reporter = reporter2;
			this.module = program.getSourceModule();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(ClassDeclaration classDeclaration)
		{
			// int category = IPHPIndexConstants.CLASS_CATEGORY;
			List<Identifier> interfaces = classDeclaration.interfaces();
			List<String> interfaceNames = new ArrayList<String>(interfaces.size());
			for (int i = 0; i < interfaces.size(); i++)
			{
				interfaceNames.add(interfaces.get(i).getName());
				Identifier identifier = interfaces.get(i);
				identifier.setBinding(resolveTypeBinding(interfaces.get(i).getName()));
			}

			Expression superClassIdentifier = classDeclaration.getSuperClass();
			String superClassName = null;
			if (superClassIdentifier != null
					&& (superClassIdentifier.getType() == ASTNode.NAMESPACE_NAME || superClassIdentifier.getType() == ASTNode.IDENTIFIER))
			{
				superClassName = ((Identifier) superClassIdentifier).getName();
				superClassIdentifier.setBinding(resolveTypeBinding(superClassName));
			}
			classDeclaration.setBinding(resolveTypeBinding(classDeclaration.getName().getName()));
			ClassPHPEntryValue value = new ClassPHPEntryValue(classDeclaration.getModifier(), superClassName,
					interfaceNames, currentNamespace);

			value.setStartOffset(classDeclaration.getStart());
			value.setEndOffset(classDeclaration.getEnd());

			// FIXME IElementEntry currentClassEntry =
			// reporter.reportEntry(category,
			// classDeclaration.getName().getName(),value, module);
			currentClass = new ClassScopeInfo(classDeclaration.getName().getName());

			return true;
		}

		private IBinding resolveTypeBinding(String superClassName)
		{
			// TODO HANDLE IT BETTER
			IType[] convertClasses = PHPSearchEngine.getInstance().findTypes(superClassName, null);
			if (convertClasses != null && convertClasses.length > 0)
			{
				return new TypeBinding(convertClasses[0]);
			}
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(InterfaceDeclaration interfaceDeclaration)
		{
			List<Identifier> interfaces = interfaceDeclaration.interfaces();
			List<String> interfaceNames = new ArrayList<String>(interfaces.size());
			for (Identifier interfaceName : interfaces)
			{
				interfaceNames.add(interfaceName.getName());
			}

			ClassPHPEntryValue value = new ClassPHPEntryValue(PHPFlags.AccInterface, null, interfaceNames,
					currentNamespace);

			value.setStartOffset(interfaceDeclaration.getStart());
			value.setEndOffset(interfaceDeclaration.getEnd());

			// IElementEntry currentClassEntry = reporter.reportEntry(category,
			// interfaceDeclaration.getName().getName(),
			// value, module);
			// FIXME
			// currentClass = new ClassScopeInfo(currentClassEntry);
			return true;
		}

		@Override
		public boolean visit(StaticConstantAccess classConstantAccess)
		{
			visitStaticDispatch(classConstantAccess);
			return super.visit(classConstantAccess);
		}

		private void visitStaticDispatch(StaticDispatch classConstantAccess)
		{
			Expression className = classConstantAccess.getClassName();
			if (className != null
					&& (className.getType() == ASTNode.NAMESPACE_NAME || className.getType() == ASTNode.IDENTIFIER))
			{
				IBinding resolveTypeBinding = resolveTypeBinding(((Identifier) className).getName());
				reporter.report(classConstantAccess, resolveTypeBinding);
				reporter.report(classConstantAccess.getClassName(), resolveTypeBinding);
			}
		}

		@Override
		public boolean visit(StaticMethodInvocation staticMethodInvocation)
		{
			visitStaticDispatch(staticMethodInvocation);
			return super.visit(staticMethodInvocation);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(FunctionInvocation functionInvocation)
		{
			FunctionName funcName = functionInvocation.getFunctionName();
			if (funcName == null)
			{
				return true;
			}

			Expression functionName = funcName.getName();
			if (functionName != null)
			{
				if (functionName.getType() == ASTNode.IDENTIFIER)
				{
					if (!DEFINE.equals(((Identifier) functionName).getName()))
					{
						return true;
					}
				}
				if (functionName.getType() == ASTNode.VARIABLE)
				{
					Variable vr = (Variable) functionName;
					Expression name = vr.getName();
					if (name != null && name.getType() == ASTNode.IDENTIFIER)
					{
						if (!DEFINE.equals(((Identifier) name).getName()))
						{
							return true;
						}
					}
				}
			}

			List<Expression> parameters = functionInvocation.parameters();
			if (parameters.size() < 2)
			{
				return true;
			}

			if (!(parameters.get(0).getType() == ASTNode.SCALAR))
			{
				return true;
			}

			if (Scalar.TYPE_STRING != ((Scalar) parameters.get(0)).getScalarType())
			{
				return true;
			}

			// getting define name
			String defineName = ((Scalar) parameters.get(0)).getStringValue();
			if (defineName.startsWith("\"")) //$NON-NLS-1$
			{
				defineName = defineName.substring(1);
			}

			if (defineName.endsWith("\"")) //$NON-NLS-1$
			{
				defineName = defineName.substring(0, defineName.length() - 1);
			}
			if (defineName.startsWith("\'")) //$NON-NLS-1$;
			{
				defineName = defineName.substring(1);
			}

			if (defineName.endsWith("\'")) //$NON-NLS-1$
			{
				defineName = defineName.substring(0, defineName.length() - 1);
			}
			Set<Object> defineTypes = countExpressionTypes(parameters.get(1));
			if (defineTypes == null)
			{
				return true;
			}

			VariableInfo info = new VariableInfo(defineName, defineTypes, getCurrentScope(),
					functionInvocation.getStart(), PHPFlags.AccStatic);
			getCurrentScope().addVariable(info);
			// VariablePHPEntryValue entryValue = new VariablePHPEntryValue(0,
			// false, false, true, defineTypes,
			// FIXME functionInvocation.getStart());
			// reporter.reportEntry(IPHPIndexConstants.CONST_CATEGORY,
			// defineName, entryValue, module);
			return true;
		}

		@Override
		public boolean visit(FunctionDeclaration functionDeclaration)
		{
			// methods are handled in other type.
			if (functionDeclaration.getParent() != null && functionDeclaration.getParent() instanceof MethodDeclaration)
			{
				return true;
			}

			Comment comment = findFunctionPHPDocComment(functionDeclaration.getStart());

			// getting function name
			Identifier functionNameIdentifier = functionDeclaration.getFunctionName();
			if (functionNameIdentifier == null)
			{
				return true;
			}

			// String functionName = functionNameIdentifier.getName();

			// getting function parameters
			List<FormalParameter> parameters = functionDeclaration.formalParameters();
			int[] parameterPositions = (parameters == null || parameters.size() == 0) ? null : new int[parameters
					.size()];
			localParametersMap = null;
			if (parameters != null && parameters.size() > 0)
			{
				localParametersMap = new LinkedHashMap<String, Set<Object>>(parameters.size());
			}
			ArrayList<Boolean> mandatoryParams = new ArrayList<Boolean>();
			if (parameters != null)
			{
				int parCount = 0;
				for (FormalParameter parameter : parameters)
				{
					Identifier nameIdentifier = parameter.getParameterNameIdentifier();
					if (nameIdentifier == null)
					{
						continue;
					}
					String parameterName = nameIdentifier.getName();
					parameterPositions[parCount] = parameter.getStart();

					String parameterType = null;
					Expression parameterTypeIdentifier = parameter.getParameterType();
					if (parameterTypeIdentifier != null
							&& (parameterTypeIdentifier.getType() == ASTNode.NAMESPACE_NAME || parameterTypeIdentifier
									.getType() == ASTNode.IDENTIFIER))
					{
						parameterType = ((Identifier) parameterTypeIdentifier).getName();
					}

					Set<Object> types = null;
					if (parameterType != null)
					{
						types = new HashSet<Object>(1);
						types.add(parameterType);
					}
					localParametersMap.put(parameterName, types);
					mandatoryParams.add(parameter.getDefaultValue() == null || parameter.isMandatory());

					parCount++;
				}
			}

			// parsing PHP doc and adding types from it to the parameters
			String[] returnTypes = null;
			if (comment != null)
			{
				returnTypes = applyFunctionComment(comment, localParametersMap);
			}
			boolean[] mandatories = new boolean[mandatoryParams.size()];
			for (int j = 0; j < mandatoryParams.size(); j++)
			{
				mandatories[j] = mandatoryParams.get(j);
			}
			FunctionPHPEntryValue entryValue = new FunctionPHPEntryValue(0, false, localParametersMap,
					parameterPositions, mandatories, functionDeclaration.getStart(), currentNamespace);
			if (returnTypes != null)
			{
				Set<Object> returnTypesSet = new HashSet<Object>();
				for (String returnType : returnTypes)
				{
					returnTypesSet.add(returnType);
				}
				entryValue.setReturnTypes(returnTypesSet);
			}
			// String entryPath = ""; //$NON-NLS-1$
			// if (currentClass != null)
			// {
			// entryPath = currentClass.getClassEntry().getEntryPath() +
			// IElementsIndex.DELIMITER;
			// }

			// entryPath += functionName;
			// FIXME
			// currentFunction =
			// reporter.reportEntry(IPHPIndexConstants.FUNCTION_CATEGORY,
			// entryPath, entryValue, module);

			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(MethodDeclaration methodDeclaration)
		{
			Comment comment = findFunctionPHPDocComment(methodDeclaration.getStart());

			FunctionDeclaration functionDeclaration = methodDeclaration.getFunction();
			if (functionDeclaration == null)
			{
				return true;
			}

			// getting function name
			Identifier functionNameIdentifier = functionDeclaration.getFunctionName();
			if (functionNameIdentifier == null)
			{
				return true;
			}

			// String functionName = functionNameIdentifier.getName();

			// getting function parameters
			List<FormalParameter> parameters = functionDeclaration.formalParameters();
			int[] parameterPositions = (parameters == null || parameters.size() == 0) ? null : new int[parameters
					.size()];

			Map<String, Set<Object>> parametersMap = null;
			if (parameters.size() > 0)
			{
				parametersMap = new LinkedHashMap<String, Set<Object>>(parameters.size());
			}
			ArrayList<Boolean> mandatoryParams = new ArrayList<Boolean>();
			if (parameters != null)
			{
				int parCount = 0;

				for (FormalParameter parameter : parameters)
				{
					Identifier nameIdentifier = parameter.getParameterNameIdentifier();
					if (nameIdentifier == null)
					{
						continue;
					}
					String parameterName = nameIdentifier.getName();
					parameterPositions[parCount] = parameter.getStart();

					String parameterType = null;
					Expression parameterTypeIdentifier = parameter.getParameterType();
					if (parameterTypeIdentifier != null
							&& (parameterTypeIdentifier.getType() == ASTNode.NAMESPACE_NAME || parameterTypeIdentifier
									.getType() == ASTNode.IDENTIFIER))
					{
						parameterType = ((Identifier) parameterTypeIdentifier).getName();
					}

					Set<Object> types = null;
					if (parameterType != null)
					{
						types = new HashSet<Object>(1);
						types.add(parameterType);
					}
					parametersMap.put(parameterName, types);
					mandatoryParams.add(parameter.getDefaultValue() == null || parameter.isMandatory());
					parCount++;
				}
			}

			// parsing PHP doc and adding types from it to the parameters
			String[] returnTypes = null;
			if (comment != null)
			{
				returnTypes = applyFunctionComment(comment, parametersMap);
			}

			int modifier = methodDeclaration.getModifier();
			// if access modifier is unspecified, making it public.
			if (!PHPFlags.isPublic(modifier) && !PHPFlags.isProtected(modifier) && !PHPFlags.isPrivate(modifier))
			{
				modifier |= PHPFlags.AccPublic;
			}

			boolean[] mandatories = new boolean[mandatoryParams.size()];
			for (int j = 0; j < mandatoryParams.size(); j++)
			{
				mandatories[j] = mandatoryParams.get(j);
			}
			FunctionPHPEntryValue entryValue = new FunctionPHPEntryValue(modifier, true, parametersMap,
					parameterPositions, mandatories, methodDeclaration.getStart(), currentNamespace);

			if (returnTypes != null)
			{
				Set<Object> returnTypesSet = new HashSet<Object>();
				for (String returnType : returnTypes)
				{
					returnTypesSet.add(returnType);
				}
				entryValue.setReturnTypes(returnTypesSet);
			}

			//String entryPath = ""; //$NON-NLS-1$
			// if (currentClass != null)
			// {
			// entryPath = currentClass.getClassEntry().getEntryPath() +
			// IElementsIndex.DELIMITER;
			// }

			// entryPath += functionName;

			reporter.report(methodDeclaration, new MethodBinding((currentClass != null) ? currentClass.getName() : "", //$NON-NLS-1$
					methodDeclaration.getFunction().getFunctionName().getName(), modifier, module));

			// FIXME currentFunction =
			// reporter.reportEntry(IPHPIndexConstants.FUNCTION_CATEGORY,
			// entryPath, entryValue, module);

			// backuping old scopes
			// backupedScopes = scopes;
			// scopes = new Stack<Scope>();

			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(Variable variable)
		{
			ASTNode parent = variable.getParent();

			// handling variables assigned to some expression
			if (parent instanceof Assignment)
			{
				Assignment assignment = (Assignment) parent;
				if (variable.equals(assignment.getLeftHandSide()))
				{
					boolean staticDeclaration = parent.getParent() instanceof StaticStatement;

					return handleAssigment(variable, assignment, staticDeclaration);
				}
			}
			// handling simple variable definitions
			else if (parent instanceof ExpressionStatement)
			{
				String variableName = getVariableName(variable);
				if (variableName == null || !variable.isDollared())
				{
					return true;
				}
				VariableInfo variableInfo = new VariableInfo(variableName, null, getCurrentScope(), variable.getStart());
				getCurrentScope().addVariable(variableInfo);
			}
			// handling infix expressions, variable might take part in
			else if (parent instanceof InfixExpression)
			{
				String variableName = getVariableName(variable);
				if (variableName == null)
				{
					return true;
				}
				InfixExpression expr = (InfixExpression) parent;
				Set<Object> types = countInfixExpressionTypes(expr);
				VariableInfo variableInfo = new VariableInfo(variableName, types, getCurrentScope(),
						variable.getStart());
				getCurrentScope().addVariable(variableInfo);
			}

			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(StaticFieldAccess fieldAccess)
		{
			visitStaticDispatch(fieldAccess);
			if (currentClass == null)
			{
				return true;
			}

			if (fieldAccess.getParent() == null || !(fieldAccess.getParent() instanceof Assignment))
			{
				return true;
			}

			Expression className = fieldAccess.getClassName();
			if (className == null
					|| (className.getType() != ASTNode.IDENTIFIER && className.getType() != ASTNode.NAMESPACE_NAME)
					|| !SELF.equals(((Identifier) className).getName()))
			{
				return true;
			}

			String fieldName = getVariableName(fieldAccess.getField());
			if (fieldName == null)
			{
				return true;
			}

			Expression value = ((Assignment) fieldAccess.getParent()).getRightHandSide();
			Set<Object> valueTypes = countExpressionTypes(value);
			if (valueTypes != null && valueTypes.size() > 0)
			{
				if (currentClass.hasField(fieldName))
				{
					currentClass.addFieldTypes(fieldName, valueTypes);
				}
				else
				{
					reportField(PHPFlags.AccPublic | PHPFlags.AccStatic, fieldName, valueTypes, fieldAccess.getStart());
				}
			}
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(ConstantDeclaration constantDeclaration)
		{
			if (currentClass == null)
			{
				return true;
			}
			List<Identifier> variableNames = constantDeclaration.names();
			List<Expression> values = constantDeclaration.initializers();

			for (int i = 0; i < variableNames.size(); i++)
			{
				String variableName = variableNames.get(i).getName();

				Expression value = values.get(i);
				Set<Object> valueTypes = countExpressionTypes(value);
				if (valueTypes != null && valueTypes.size() > 0)
				{
					if (currentClass.hasField(variableName))
					{
						currentClass.addFieldTypes(variableName, valueTypes);
					}
					else
					{
						reportField(PHPFlags.AccPublic | PHPFlags.AccStatic | PHPFlags.AccFinal, variableName,
								valueTypes, constantDeclaration.getStart());
					}
				}
			}
			return true;
		}

		@Override
		public boolean visit(MethodInvocation methodInvocation)
		{
			if (methodInvocation == null)
			{
				return true;
			}
			VariableBase leftSide = methodInvocation.getDispatcher();
			if (!(leftSide instanceof Variable))
			{
				return true;
			}
			VariableBase rightSide = methodInvocation.getMember();

			if (rightSide != null)
			{

				ITypeBinding bnd = (ITypeBinding) resolveVariableTypeBinding(getVariableName((Variable) leftSide));
				reporter.report(leftSide, bnd);
				return true;
			}
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(FieldAccess fieldAccess)
		{

			if (fieldAccess == null)
			{
				return false;
			}
			VariableBase rightSide = fieldAccess.getMember();
			if (!(rightSide instanceof Variable))
			{
				return true;
			}
			VariableBase leftSide = fieldAccess.getDispatcher();
			if (!(leftSide instanceof Variable))
			{
				return true;
			}
			String fieldName = getVariableName((Variable) rightSide);
			if (fieldName != null)
			{
				ITypeBinding bnd = (ITypeBinding) resolveVariableTypeBinding(getVariableName((Variable) leftSide));
				reporter.report(leftSide, bnd);
				return true;
			}
			if (currentClass == null)
			{
				return true;
			}

			if (fieldAccess.getParent() == null || !(fieldAccess.getParent() instanceof Assignment))
			{
				return true;
			}

			if (fieldAccess != ((Assignment) fieldAccess.getParent()).getLeftHandSide())
			{
				return true;
			}

			String variableName = getVariableName((Variable) leftSide);
			if (variableName == null || !THIS.equals(variableName))
			{
				return true;
			}

			Expression value = ((Assignment) fieldAccess.getParent()).getRightHandSide();
			Set<Object> valueTypes = countExpressionTypes(value);
			if (valueTypes != null && valueTypes.size() > 0)
			{
				if (currentClass.hasField(fieldName))
				{
					currentClass.addFieldTypes(fieldName, valueTypes);
				}
				else
				{
					reportField(PHPFlags.AccPublic, fieldName, valueTypes, fieldAccess.getStart());
				}
			}
			return true;
		}

		private ITypeBinding resolveVariableTypeBinding(String fieldName)
		{
			Scope currentScope = getCurrentScope();
			VariableInfo variable = currentScope.getVariable(fieldName);
			if (variable != null)
			{
				Set<Object> variableTypes = variable.getVariableTypes();

				Set<String> ts = null;
				Set<String> set = resolvedTypes.get(variableTypes);
				if (set != null)
				{
					ts = set;
				}
				else
				{
					ts = PHPTypeProcessor.processTypes(variableTypes, PHPGlobalIndexer.getInstance().getIndex());
					resolvedTypes.put(variableTypes, ts);
				}
				if (ts.size() == 1)
				{
					String tn = ts.iterator().next();
					if (!binding.containsKey(tn))
					{
						ITypeBinding typeBinding = (ITypeBinding) resolveTypeBinding(tn);
						binding.put(tn, typeBinding);
						return typeBinding;
					}
					ITypeBinding typeBinding = this.binding.get(tn);
					return typeBinding;
				}
			}
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(FieldsDeclaration fieldsDeclaration)
		{
			Variable[] variables = fieldsDeclaration.getVariableNames();
			Expression[] initialValues = fieldsDeclaration.getInitialValues();
			int modifier = fieldsDeclaration.getModifier();

			// if access modifier is unspecified, making it public.
			if (!PHPFlags.isPublic(modifier) && !PHPFlags.isProtected(modifier) && !PHPFlags.isPrivate(modifier))
			{
				modifier |= PHPFlags.AccPublic;
			}

			for (int i = 0; i < variables.length; i++)
			{
				Variable fieldVariable = variables[i];
				Expression initialValue = initialValues[i];

				String fieldName = getVariableName(fieldVariable);
				if (fieldName == null)
				{
					continue;
				}

				Set<Object> fieldTypes = null;
				if (initialValue != null)
				{
					fieldTypes = countExpressionTypes(initialValue);
				}

				reportField(modifier, fieldName, fieldTypes, fieldVariable.getStart());
			}

			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(GlobalStatement globalStatement)
		{
			List<Variable> variables = globalStatement.variables();
			Set<String> imports = new HashSet<String>();
			for (Variable variable : variables)
			{
				String varName = getVariableName(variable);
				if (varName != null)
				{
					imports.add(varName);

					getGlobalScope().addVariable(
							new VariableInfo(varName, null, getGlobalScope(), globalStatement.getStart()));
				}
			}

			getCurrentScope().addGlobalImports(imports);

			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(ReturnStatement returnStatement)
		{
			// ignoring out of function returns
			if (currentFunction == null)
			{
				return true;
			}

			Expression expression = returnStatement.getExpression();
			Set<Object> types = countExpressionTypes(expression);

			if (!(currentFunction.getValue() instanceof FunctionPHPEntryValue))
			{
				return true;
			}

			FunctionPHPEntryValue value = (FunctionPHPEntryValue) currentFunction.getValue();
			if (types != null)
			{
				Set<Object> currentTypes = value.getReturnTypes();
				if (currentTypes == null || currentTypes.size() == 0)
				{
					currentTypes = new HashSet<Object>();
				}
				currentTypes.addAll(types);
				value.setReturnTypes(currentTypes);
			}

			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void endVisit(ClassDeclaration classDeclaration)
		{
			currentClass = null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void endVisit(FunctionDeclaration functionDeclaration)
		{
			currentFunction = null;
			localParametersMap = null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void endVisit(MethodDeclaration methodDeclaration)
		{
			// restoring backuped scopes
			// scopes = backupedScopes;

			currentFunction = null;
			localParametersMap = null;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void endVisit(Program program)
		{
			endVisitScopeNode(program);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(Program program)
		{
			startVisitScopeNode(program);
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(Block block)
		{
			startVisitScopeNode(block.getParent());
			addBlockVariables(block);
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(CatchClause catchClause)
		{
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(DoStatement doStatement)
		{
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(ForEachStatement forEachStatement)
		{
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(ForStatement forStatement)
		{
			startVisitScopeNode(forStatement);
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(IfStatement ifStatement)
		{
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(SwitchStatement switchStatement)
		{
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(TryStatement tryStatement)
		{
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(WhileStatement whileStatement)
		{
			return true;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean visit(Include include)
		{
			Expression expr = include.getExpression();
			if (expr != null && (expr instanceof ParenthesisExpression || expr instanceof Scalar))
			{
				Expression subExpr = null;
				if (expr instanceof Scalar)
				{
					subExpr = expr;
				}
				else
				{
					subExpr = ((ParenthesisExpression) expr).getExpression();
				}
				if (subExpr == null)
				{
					return true;
				}
				if (subExpr instanceof Scalar)
				{
					String includePath = ((Scalar) subExpr).getStringValue();
					if (includePath == null || includePath.length() == 0)
					{
						return true;
					}

					int pathStartOffset = subExpr.getStart();

					if (includePath.startsWith("\"") || includePath.startsWith("'")) //$NON-NLS-1$ //$NON-NLS-2$
					{
						includePath = includePath.substring(1);
						pathStartOffset++;
					}

					if (includePath.endsWith("\"") || includePath.endsWith("'")) //$NON-NLS-1$ //$NON-NLS-2$
					{
						includePath = includePath.substring(0, includePath.length() - 1);
					}

					// int pdtIncludeType = include.getIncludeType();
					// int includeType = -1;
					// switch (pdtIncludeType) {
					// case Include.IT_INCLUDE:
					// includeType = IncludePHPEntryValue.INCLUDE_TYPE;
					// break;
					// case Include.IT_INCLUDE_ONCE:
					// includeType = IncludePHPEntryValue.INCLUDE_ONCE_TYPE;
					// break;
					// case Include.IT_REQUIRE:
					// includeType = IncludePHPEntryValue.REQUIRE_TYPE;
					// break;
					// case Include.IT_REQUIRE_ONCE:
					// includeType = IncludePHPEntryValue.REQUIRE_ONCE_TYPE;
					// break;
					// }
					if (module != null)
					{
						ISourceModule module2 = ((SourceModule) module).getModule(includePath);
						if (module2 != null)
						{
							reporter.report(include, new ModuleBinding(module2));
						}
					}
					//reporter.reportEntry(IPHPIndexConstants.IMPORT_CATEGORY, "", value, module); //$NON-NLS-1$
				}
			}

			return true;
		}

		// ///
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void endVisit(Block block)
		{
			endVisitScopeNode(block);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void endVisit(CatchClause catchClause)
		{
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void endVisit(DoStatement doStatement)
		{
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void endVisit(ForEachStatement forEachStatement)
		{

		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void endVisit(ForStatement forStatement)
		{
			endVisitScopeNode(forStatement);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void endVisit(IfStatement ifStatement)
		{
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void endVisit(SwitchStatement switchStatement)
		{
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void endVisit(TryStatement tryStatement)
		{
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void endVisit(WhileStatement whileStatement)
		{
		}

		/**
		 * Handles variable assignment.
		 * 
		 * @param variable
		 *            - variable.
		 * @param assigment
		 *            - assignment node.
		 * @return
		 */
		private boolean handleAssigment(Variable variable, Assignment assigment, boolean staticDeclaration)
		{
			Expression value = assigment.getRightHandSide();

			String variableName = getVariableName(variable);
			if (variableName == null)
			{
				return true;
			}

			Set<Object> rightSideTypes = countExpressionTypes(value);

			VariableInfo variableInfo = new VariableInfo(variableName, rightSideTypes, getCurrentScope(),
					variable.getStart(), staticDeclaration ? PHPFlags.AccStatic : 0);
			getCurrentScope().addVariable(variableInfo);

			return true;
		}

		/**
		 * Gets variable entry path.
		 * 
		 * @param variable
		 *            - variable.
		 * @return entry path.
		 */
		String getVariableEntryPath(Variable variable)
		{
			String entryPath = ""; //$NON-NLS-1$
			if (currentFunction != null)
			{
				entryPath = currentFunction.getEntryPath() + IElementsIndex.DELIMITER;
			}
			// else if (currentClass != null)
			// {
			// entryPath = currentClass.getClassEntry().getEntryPath() +
			// IElementsIndex.DELIMITER;
			// }

			String varName = getVariableName(variable);
			if (varName == null)
			{
				return null;
			}

			entryPath += varName;

			return entryPath;
		}

		/**
		 * Gets variable name.
		 * 
		 * @param variable
		 *            - variable.
		 * @return variable name.
		 */
		private String getVariableName(Expression variable)
		{
			if (variable != null)
			{
				if (variable.getType() == ASTNode.VARIABLE)
				{
					// this will get the name from the Variable as an identifier
					variable = ((Variable) variable).getName();
				}
				if (variable.getType() == ASTNode.NAMESPACE_NAME || variable.getType() == ASTNode.IDENTIFIER)
				{
					return ((Identifier) variable).getName();
				}
			}
			return null;
		}

		/**
		 * Handles the start of visiting the node that provides variables scope.
		 * 
		 * @param node
		 *            - node.
		 */
		private void startVisitScopeNode(ASTNode node)
		{
			Scope parent = null;
			if (!scopes.isEmpty())
			{
				parent = scopes.peek();
			}

			IElementEntry currentEntry = null;
			if (node instanceof FunctionDeclaration)
			{
				currentEntry = currentFunction;
			}
			// else if (node instanceof ClassDeclaration)
			// {
			// currentEntry = currentClass.getClassEntry();
			// }

			Scope scope = new Scope(node, parent, currentEntry);
			if (localParametersMap != null)
			{
				for (String s : localParametersMap.keySet())
				{
					scope.addVariable(new VariableInfo(s, localParametersMap.get(s), scope, 0));
				}
			}
			scopes.push(scope);
		}

		/**
		 * Handles the end of visiting the node that provides variables scope.
		 * 
		 * @param node
		 *            - node.
		 */
		private void endVisitScopeNode(ASTNode node)
		{
			if (!scopes.isEmpty())
			{

				// if local mode is on and we did not report the local stack
				// yet,
				// backuping the stack.
				if (!globalMode && !localStackReported)
				{
					if (node.getEnd() > currentOffset)
					{
						reportStack(scopes);
						localStackReported = true;
					}
				}

				Scope scope = scopes.pop();
				reportGlobalScopeVariables(scope);
			}
		}

		/**
		 * Gets current scope. Should never be null.
		 * 
		 * @return current scope.
		 */
		private Scope getCurrentScope()
		{
			return scopes.peek();
		}

		/**
		 * Gets current scope. Should never be null.
		 * 
		 * @return current scope.
		 */
		private Scope getGlobalScope()
		{
			return scopes.get(0);
		}

		/**
		 * Reports global scope variables.
		 * 
		 * @param scope
		 *            - scope, which variables to report.
		 */
		private void reportGlobalScopeVariables(Scope scope)
		{
			// if (globalMode)
			// {
			// if (scope.getRoot() instanceof Program)
			// {
			// for (VariableInfo info : scope.getVariables())
			// {
			// VariablePHPEntryValue entryValue = new VariablePHPEntryValue(0, false, false, false,
			// info.getVariableTypes(), info.getNodeStart());
			// String entryPath = info.getName();
			// FIXME
			// reporter.reportEntry(IPHPIndexConstants.VAR_CATEGORY, entryPath, entryValue, module);
			// }
			// }
			// }
		}

		/**
		 * Count variables types by name and scope.
		 * 
		 * @param variableName
		 *            - variable name.
		 * @param currentScope
		 *            - current scope.
		 * @return
		 */
		private Set<Object> countVariableTypes(String variableName, Scope currentScope)
		{
			Set<Object> result = new HashSet<Object>();

			// checking for variable with this name.
			VariableInfo variable = currentScope.getVariable(variableName);
			if (variable != null)
			{
				result.addAll(variable.getVariableTypes());
			}

			// checking for function or method parameter with this name
			IElementEntry entry = findFunctionOrMethodParent(currentScope);
			if (entry != null)
			{
				FunctionPHPEntryValue entryValue = (FunctionPHPEntryValue) entry.getValue();
				Map<String, Set<Object>> parameters = entryValue.getParameters();
				if (parameters != null)
				{
					Set<Object> types = parameters.get(variableName);
					if (types != null)
					{
						result.addAll(types);
					}
				}
			}

			return result;
		}

		/**
		 * Finds function or method parent scope and returns its element entry.
		 * 
		 * @param scope
		 *            - current scope.
		 * @return entry or null if not found.
		 */
		private IElementEntry findFunctionOrMethodParent(Scope scope)
		{
			Scope currentScope = scope;
			while (currentScope != null)
			{
				IElementEntry entry = currentScope.getEntry();
				if (entry != null)
				{
					if (entry.getValue() instanceof FunctionPHPEntryValue)
					{
						return entry;
					}
				}

				currentScope = currentScope.getParent();
			}

			return null;
		}

		/**
		 * Counts infix expression types.
		 * 
		 * @param expr
		 *            - expression.
		 * @return expression type or null if undefined
		 */
		private Set<Object> countInfixExpressionTypes(InfixExpression expr)
		{
			int operator = expr.getOperator();
			String type = null;
			switch (operator)
			{
				case InfixExpression.OP_CONCAT:
					type = IPHPTypeConstants.STRING_TYPE;
					break;
				case InfixExpression.OP_IS_IDENTICAL:
				case InfixExpression.OP_IS_NOT_IDENTICAL:
				case InfixExpression.OP_IS_EQUAL:
				case InfixExpression.OP_IS_NOT_EQUAL:
				case InfixExpression.OP_RGREATER:
				case InfixExpression.OP_IS_SMALLER_OR_EQUAL:
				case InfixExpression.OP_LGREATER:
				case InfixExpression.OP_IS_GREATER_OR_EQUAL:
				case InfixExpression.OP_BOOL_OR:
					type = IPHPTypeConstants.BOOLEAN_TYPE;
					break;
				case InfixExpression.OP_STRING_OR:
				case InfixExpression.OP_STRING_AND:
				case InfixExpression.OP_STRING_XOR:
					type = IPHPTypeConstants.STRING_TYPE;
					break;
				case InfixExpression.OP_PLUS:
				case InfixExpression.OP_MINUS:
				case InfixExpression.OP_MUL:
				case InfixExpression.OP_DIV:
				case InfixExpression.OP_MOD:
					type = IPHPTypeConstants.REAL_TYPE;
					break;
				case InfixExpression.OP_SL:
				case InfixExpression.OP_SR:
					type = IPHPTypeConstants.INTEGER_TYPE;
					break;
			}

			Set<Object> result = new HashSet<Object>(1);
			result.add(type);
			return result;
		}

		/**
		 * Converts static access to the plain path. In example ClassName::$b would be [ClassName,b] path.
		 * 
		 * @param dispatch
		 *            - dispatch.
		 * @return path or null indicating parsing failure
		 */
		private CallPath getPathByStaticDispatch(StaticDispatch dispatch)
		{
			CallPath result = new CallPath();

			// counting first path entry
			Expression classNameIdentifier = dispatch.getClassName();
			// FIXME: Shalom - We might need a better handle here for namespaces
			String className = null;
			if (classNameIdentifier == null
					|| (classNameIdentifier.getType() != ASTNode.IDENTIFIER && classNameIdentifier.getType() != ASTNode.NAMESPACE_NAME))
			{
				IdeLog.logError(PHPEditorPlugin.getDefault(),
						"Expected an identifier or namespace-name", new Exception("Missing identifier")); //$NON-NLS-1$ //$NON-NLS-2$
				return null;
			}
			className = ((Identifier) classNameIdentifier).getName();
			result.setClassEntry(className);

			// counting second entry
			ASTNode member = dispatch.getMember();

			// getting member name and type
			String memberName = null;
			boolean field = true;

			if (member instanceof Variable)
			{
				Variable currentField = (Variable) member;
				memberName = getVariableName(currentField);
				if (memberName == null)
				{
					return null;
				}
				field = true;
			}
			else if (member instanceof FunctionInvocation)
			{
				FunctionInvocation funcInvocation = (FunctionInvocation) member;
				memberName = getFunctionNameByInvocation(funcInvocation);
				if (memberName == null)
				{
					return null;
				}
				field = false;
			}
			else
			{
				return null;
			}

			// inserting member to the path
			if (field)
			{
				result.addVariableEntry(memberName);
			}
			else
			{
				result.addMethodEntry(memberName);
			}

			return result;
		}

		/**
		 * Converts field access to the plain path. In example $a->$b->$method() would be [a,b,method] path.
		 * 
		 * @param dispatch
		 *            - dispatch.
		 * @return path or null indicating parsing failure
		 */
		private CallPath getPathByDispatch(Dispatch dispatch)
		{
			CallPath result = new CallPath();
			Dispatch currentDispatch = dispatch;

			while (currentDispatch != null)
			{
				VariableBase currentDispatcher = currentDispatch.getDispatcher();

				// getting member name and type
				String memberName = null;
				boolean field = true;
				VariableBase member = currentDispatch.getMember();
				if (member instanceof Variable)
				{
					Variable currentField = (Variable) member;
					memberName = getVariableName(currentField);
					if (memberName == null)
					{
						return null;
					}
					field = true;
				}
				else if (member instanceof FunctionInvocation)
				{
					FunctionInvocation funcInvocation = (FunctionInvocation) member;
					memberName = getFunctionNameByInvocation(funcInvocation);
					if (memberName == null)
					{
						return null;
					}
					field = false;
				}
				else
				{
					return null;
				}

				// inserting member to the path
				if (field)
				{
					result.insertVariableEntry(memberName);
				}
				else
				{
					result.insertMethodEntry(memberName);
				}

				// if left side is still the dispatch, continue to parse it
				if (currentDispatcher instanceof Dispatch)
				{
					currentDispatch = (Dispatch) currentDispatcher;
				}
				else if (currentDispatcher instanceof StaticDispatch)
				{
					// getting the path of the static dispatch
					CallPath staticDispatchCallPath = getPathByStaticDispatch((StaticDispatch) currentDispatcher);
					if (staticDispatchCallPath == null)
					{
						return null;
					}

					// adding current path to the static dispatch type
					staticDispatchCallPath.addPath(result);
					return staticDispatchCallPath;
				}
				else if (currentDispatcher instanceof Variable)
				{
					// if left side is variable, inserting it and stopping
					// parsing
					String dispatecherName = getVariableName((Variable) currentDispatcher);
					if (dispatecherName == null)
					{
						return null;
					}

					result.insertVariableEntry(dispatecherName);
					break;
				}
				else if (currentDispatcher instanceof FunctionInvocation)
				{
					// if left side is method, inserting it and stopping parsing
					String dispatecherName = getFunctionNameByInvocation((FunctionInvocation) currentDispatcher);
					if (dispatecherName == null)
					{
						return null;
					}

					result.insertMethodEntry(dispatecherName);
					break;
				}
				else
				{
					// unrecognized construction
					return null;
				}
			}

			return result;
		}

		/**
		 * Counts the all possible types of expression.
		 * 
		 * @param expression
		 *            - expression to count.
		 * @return types set or null if expression types connot be counted.
		 */
		private Set<Object> countExpressionTypes(Expression expression)
		{
			Set<Object> result = null;
			// handling scalar values
			if (expression instanceof Scalar)
			{
				Scalar scalar = (Scalar) expression;
				result = countScalarTypes(scalar);
			}
			// handling variable values
			else if (expression instanceof Variable)
			{
				Variable rightSideVar = (Variable) expression;
				String rightSideVarName = getVariableName(rightSideVar);
				if (rightSideVarName == null)
				{
					return null;
				}

				result = countVariableTypes(rightSideVarName, getCurrentScope());
			}
			// handling instance creation
			else if (expression instanceof ClassInstanceCreation)
			{
				ClassInstanceCreation creation = (ClassInstanceCreation) expression;
				result = countInstanceCreationTypes(creation);
			}
			// handling call paths
			else if (expression instanceof FieldAccess || expression instanceof MethodInvocation)
			{
				Dispatch dispatch = (Dispatch) expression;
				result = countDispatchTypes(dispatch);
			}
			else if (expression instanceof StaticDispatch)
			{
				StaticDispatch staticDispatch = (StaticDispatch) expression;
				result = countStaticDispatchTypes(staticDispatch);
			}
			// handling function invocation
			else if (expression instanceof FunctionInvocation)
			{
				FunctionInvocation invocation = (FunctionInvocation) expression;
				result = countFunctionInvocationTypes(invocation);
			}
			// handling infix expressions
			else if (expression instanceof InfixExpression)
			{
				InfixExpression infix = (InfixExpression) expression;
				result = countInfixExpressionTypes(infix);
			}

			return result;
		}

		/**
		 * Counts function invocation types.
		 * 
		 * @param invocation
		 *            - invocation.
		 * @return set of types or null if cannot count.
		 */
		private Set<Object> countFunctionInvocationTypes(FunctionInvocation invocation)
		{
			String functionName = getFunctionNameByInvocation(invocation);
			if (functionName == null)
			{
				return null;
			}

			FunctionPathReference reference = new FunctionPathReference(functionName, null);

			Set<Object> result = new HashSet<Object>(1);
			result.add(reference);

			return result;
		}

		/**
		 * Counts scalar expression types.
		 * 
		 * @param scalar
		 *            - scalar.
		 * @return set of types or null if cannot count.
		 */
		private Set<Object> countScalarTypes(Scalar scalar)
		{
			Set<Object> result;
			int intType = scalar.getScalarType();
			String type = typeToString(intType);

			result = new HashSet<Object>(1);
			result.add(type);
			return result;
		}

		/**
		 * Counts class instance creation expression types.
		 * 
		 * @param creation
		 *            - instance creation.
		 * @return set of types or null if cannot count.
		 */
		private Set<Object> countInstanceCreationTypes(ClassInstanceCreation creation)
		{
			ClassName className = creation.getClassName();
			if (className == null)
			{
				return null;
			}
			Expression classNameExpr = className.getName();
			String clName = null;
			if (classNameExpr != null
					&& (classNameExpr.getType() == ASTNode.NAMESPACE_NAME || classNameExpr.getType() == ASTNode.IDENTIFIER))
			{
				clName = ((Identifier) classNameExpr).getName();
			}
			if (clName == null)
			{
				return null;
			}

			Set<Object> result = new HashSet<Object>(1);

			if (SELF.equals(clName))
			{
				if (currentClass != null)
				{
					// result.add(currentClass.getClassEntry().getEntryPath());
					return result;
				}

				return null;
			}

			result.add(clName);

			return result;
		}

		/**
		 * Counts dispatch expression types.
		 * 
		 * @param dispatch
		 *            - dispatch.
		 * @return types set or null if cannot count.
		 */
		private Set<Object> countStaticDispatchTypes(StaticDispatch dispatch)
		{
			Set<Object> result = null;

			CallPath path = getPathByStaticDispatch(dispatch);
			if (path == null || path.getSize() < 2)
			{
				return null;
			}

			CallPath remainingPath = path.subPath(1);

			List<CallPath.Entry> pathEntries = path.getEntries();
			CallPath.Entry dispatchEntry = pathEntries.get(0);

			// handling "self::"
			if (dispatchEntry instanceof CallPath.ClassEntry && SELF.equals(dispatchEntry.getName()))
			{
				if (currentClass != null)
				{
					Set<Object> dispatcherTypes = new HashSet<Object>(1);
					// dispatcherTypes.add(ElementsIndexingUtils.
					// getFirstNameInPath(currentClass.getClassEntry()
					// .getEntryPath()));
					StaticPathReference reference = new StaticPathReference(dispatcherTypes, remainingPath);
					result = new HashSet<Object>(1);
					result.add(reference);
				}
				else
				{
					return null;
				}
			}
			// handling "ClassName::"
			else if (dispatchEntry instanceof CallPath.ClassEntry)
			{
				Set<Object> dispatcherTypes = new HashSet<Object>(1);
				dispatcherTypes.add(dispatchEntry.getName());
				StaticPathReference reference = new StaticPathReference(dispatcherTypes, remainingPath);
				result = new HashSet<Object>(1);
				result.add(reference);
			}
			else
			{
				return null;
			}

			return result;
		}

		/**
		 * Counts dispatch expression types.
		 * 
		 * @param dispatch
		 *            - dispatch.
		 * @return types set or null if cannot count.
		 */
		private Set<Object> countDispatchTypes(Dispatch dispatch)
		{
			Set<Object> result = null;

			CallPath path = getPathByDispatch(dispatch);
			if (path == null || path.getSize() < 2)
			{
				return null;
			}

			CallPath remainingPath = path.subPath(1);

			List<CallPath.Entry> pathEntries = path.getEntries();
			CallPath.Entry dispatchEntry = pathEntries.get(0);

			// handling "this->"
			if (dispatchEntry instanceof CallPath.VariableEntry && THIS.equals(dispatchEntry.getName()))
			{
				if (currentClass != null)
				{
					Set<Object> dispatcherTypes = new HashSet<Object>(1);
					// dispatcherTypes.add(ElementsIndexingUtils.
					// getFirstNameInPath(currentClass.getClassEntry()
					// .getEntryPath()));
					VariablePathReference reference = new VariablePathReference(dispatcherTypes, remainingPath);
					result = new HashSet<Object>(1);
					result.add(reference);
				}
				else
				{
					return null;
				}
			}
			// handling "self::"
			else if (dispatchEntry instanceof CallPath.ClassEntry && SELF.equals(dispatchEntry.getName()))
			{
				if (currentClass != null)
				{
					Set<Object> dispatcherTypes = new HashSet<Object>(1);
					// dispatcherTypes.add(ElementsIndexingUtils.
					// getFirstNameInPath(currentClass.getClassEntry()
					// .getEntryPath()));
					StaticPathReference reference = new StaticPathReference(dispatcherTypes, remainingPath);
					result = new HashSet<Object>(1);
					result.add(reference);
				}
				else
				{
					return null;
				}
			}
			// handling "ClassName::"
			else if (dispatchEntry instanceof CallPath.ClassEntry)
			{
				Set<Object> dispatcherTypes = new HashSet<Object>(1);
				dispatcherTypes.add(dispatchEntry.getName());
				StaticPathReference reference = new StaticPathReference(dispatcherTypes, remainingPath);
				result = new HashSet<Object>(1);
				result.add(reference);
			}
			// handling "$var->"
			else if (dispatchEntry instanceof CallPath.VariableEntry)
			{
				Set<Object> dispatcherTypes = countVariableTypes(dispatchEntry.getName(), getCurrentScope());
				if (dispatcherTypes == null)
				{
					return null;
				}

				VariablePathReference reference = new VariablePathReference(dispatcherTypes, remainingPath);
				result = new HashSet<Object>(1);
				result.add(reference);
			}
			// handling "method()->"
			else if (dispatchEntry instanceof CallPath.MethodEntry)
			{
				String methodEntryPath = ((CallPath.MethodEntry) dispatchEntry).getName();

				FunctionPathReference reference = new FunctionPathReference(methodEntryPath, remainingPath);
				result = new HashSet<Object>(1);
				result.add(reference);
			}
			else
			{
				return null;
			}

			return result;
		}

		/**
		 * Gets function name by function invocation.
		 * 
		 * @param invocation
		 *            - function invocation.
		 * @return function name or null if not recognized.
		 */
		private String getFunctionNameByInvocation(FunctionInvocation invocation)
		{
			FunctionName funcName = invocation.getFunctionName();
			if (funcName == null)
			{
				return null;
			}

			Expression funcNameExpression = funcName.getName();
			return getVariableName(funcNameExpression);
		}

		/**
		 * Reports the stack. Needed for local mode.
		 * 
		 * @param stack
		 *            - stack to report.
		 */
		private void reportStack(List<Scope> stack)
		{
			if (stack.isEmpty())
			{
				return;
			}
			// int currentScopeDepth = 0;
			// collecting global imports
			for (int i = 0; i < stack.size(); i++)
			{
				Scope currentScope = stack.get(i);
				Set<String> currentGlobalImports = currentScope.getGlobalImports();
				if (currentGlobalImports != null)
				{
					overallGlobalImports.addAll(currentGlobalImports);
				}
			}

			// searching for a first scope that start earlier then current
			// offset
			Scope currentScope = null;
			for (int i = stack.size() - 1; i >= 0; i--)
			{
				// currentScopeDepth = i;
				currentScope = stack.get(i);
				ASTNode root = currentScope.getRoot();
				if (root == null)
				{
					continue;
				}
				if (root.getStart() < currentOffset)
				{
					break;
				}
			}

			// nothing to report
			if (currentScope == null)
			{
				return;
			}

			isReportedStackGlobal = currentScope.isGlobalScope();

			// String scopePath = getScopePath(stack, i);

			Set<VariableInfo> variables = currentScope.getVariables();

			// boolean localVariables = !currentScope.isGlobalScope();

			// reporting variables
			for (VariableInfo varInfo : variables)
			{
				if (varInfo.getNodeStart() > currentOffset)
				{
					continue;
				}
				// String entryPath = varInfo.getName();
				// VariablePHPEntryValue value = new VariablePHPEntryValue(varInfo
				// .getModifier(), false, localVariables, false, varInfo
				// .getVariableTypes(), varInfo.getNodeStart());
				// FIXME reporter.reportEntry(IPHPIndexConstants.VAR_CATEGORY,
				// entryPath, value, module);
			}

			// reporting function/method parameters if needed, going up in the
			// stack searching for the function
			// for (int i = stack.size() - 1; i >= 0; i--)
			// {
			// Scope scope = stack.get(i);
			// if (scope.getEntry() != null)
			// {
			// IElementEntry entry = scope.getEntry();
			// if (entry.getCategory() == IPHPIndexConstants.FUNCTION_CATEGORY
			// && entry.getValue() instanceof FunctionPHPEntryValue) {
			// FunctionPHPEntryValue val = (FunctionPHPEntryValue) entry
			// .getValue();
			// Map<String, Set<Object>> parameters = val
			// .getParameters();
			// if (parameters != null && !parameters.isEmpty()) {
			// for (Map.Entry<String, Set<Object>> parEntry : parameters
			// .entrySet()) {
			// //String entryPath = parEntry.getKey();
			// // VariablePHPEntryValue value = new VariablePHPEntryValue(
			// // 0, true, false, false, parEntry
			// // .getValue(), val
			// // .getStartOffset());
			// // FIXME
			// // reporter.reportEntry(IPHPIndexConstants.
			// // VAR_CATEGORY, entryPath, value, module);
			// }
			// }
			// }
			// }
			// }
		}

		/**
		 * Gets scope entries path (including the ending delimiter).
		 * 
		 * @param stack
		 *            - scopes stack.
		 * @param pos
		 *            - position of the scope in stack.
		 * @param clazz
		 *            - class active for the scope.
		 * @return scope entries path
		 */
		String getScopePath(List<Scope> stack, int pos)
		{
			StringBuffer result = new StringBuffer();

			for (int i = pos; i >= 0; i--)
			{
				Scope currentScope = stack.get(i);
				ASTNode root = currentScope.getRoot();

				if (root instanceof FunctionDeclaration)
				{
					Identifier funcNameIdentifier = ((FunctionDeclaration) root).getFunctionName();
					if (funcNameIdentifier == null)
					{
						continue;
					}
					String functionName = funcNameIdentifier.getName();
					if (functionName == null)
					{
						continue;
					}
					result.insert(0, functionName + IElementsIndex.DELIMITER);
				}
				else if (root instanceof MethodDeclaration)
				{
					Identifier funcNameIdentifier = ((MethodDeclaration) root).getFunction().getFunctionName();
					if (funcNameIdentifier == null)
					{
						continue;
					}
					String functionName = funcNameIdentifier.getName();
					if (functionName == null)
					{
						continue;
					}
					result.insert(0, functionName + IElementsIndex.DELIMITER);
				}
				else if (root instanceof ClassDeclaration)
				{
					Identifier classNameIdentifier = ((ClassDeclaration) root).getName();
					if (classNameIdentifier == null)
					{
						continue;
					}

					String className = classNameIdentifier.getName();
					if (className == null)
					{
						continue;
					}

					result.insert(0, className + IElementsIndex.DELIMITER);
				}
			}

			return result.toString();
		}

		/**
		 * Parses comment and adds parameter types if available.
		 * 
		 * @param comment
		 * @param parametersMap
		 * @return possible return types.
		 */
		private String[] applyFunctionComment(Comment comment, Map<String, Set<Object>> parametersMap)
		{
			try
			{
				FunctionDocumentation doc = PHPDocUtils.getFunctionDocumentation((IPHPDoc) comment);
				if (doc == null)
				{
					return null;
				}

				TypedDescription[] params = doc.getParams();
				if (params != null && params.length != 0)
				{
					for (TypedDescription param : params)
					{
						String paramName = param.getName();
						if (paramName == null || paramName.length() == 0)
						{
							continue;
						}

						if (paramName.startsWith("$")) //$NON-NLS-1$
						{
							paramName = paramName.substring(1);
						}
						String[] types = param.getTypes();
						Set<Object> toSetParams = (parametersMap != null) ? parametersMap.get(paramName) : Collections
								.emptySet();
						if (toSetParams == null)
						{

							toSetParams = new HashSet<Object>();
							parametersMap.put(paramName, toSetParams);
						}

						if (types != null)
						{
							for (String type : types)
							{
								toSetParams.add(type);
							}
						}
					}
				}

				TypedDescription returnDescr = doc.getReturn();
				if (returnDescr == null)
				{
					return null;
				}

				return returnDescr.getTypes();
			}
			catch (Throwable th)
			{
				IdeLog.logWarning(PHPEditorPlugin.getDefault(),
						"PHP Type-Binding builder - Error while applying a comment to a parameter type (applyComment)", //$NON-NLS-1$
						th, PHPEditorPlugin.DEBUG_SCOPE);
			}

			return null;
		}

		/**
		 * Reports field and adds it to the current class fields list.
		 * 
		 * @param modifier
		 *            - modifier.
		 * @param fieldName
		 *            - field name.
		 * @param fieldTypes
		 *            - field types.
		 * @param pos
		 *            - position.
		 * @return reported entry or null.
		 */
		private IElementEntry reportField(int modifier, String fieldName, Set<Object> fieldTypes, int pos)
		{
			// VariablePHPEntryValue entryValue = new VariablePHPEntryValue(
			// modifier, false, false, true, fieldTypes, pos);
			//
			// if (currentClass != null) {
			// // String entryPath =
			// // currentClass.getClassEntry().getEntryPath() +
			// // IElementsIndex.DELIMITER;
			//
			// // entryPath += fieldName;
			//
			// // IElementEntry result =
			// // reporter.reportEntry(IPHPIndexConstants.VAR_CATEGORY,
			// // entryPath, entryValue,
			// // FIXME module);
			// // currentClass.setField(fieldName, result);
			// // return result;
			// }
			return null;
		}

		/**
		 * Adds block variables for try, for and other non-function expressions that can define local block variables.
		 * 
		 * @param block
		 *            - block.
		 */
		private void addBlockVariables(Block block)
		{
			ASTNode blockParent = block.getParent();
			if (blockParent instanceof CatchClause)
			{
				addCatchClauseVariables((CatchClause) blockParent);
			}
			else if (blockParent instanceof ForStatement)
			{
				addForVariables((ForStatement) blockParent);
			}
			else if (blockParent instanceof ForEachStatement)
			{
				addForEachVariables((ForEachStatement) blockParent);
			}
		}

		/**
		 * Adds "for each" local variables.
		 * 
		 * @param foreachStatement
		 *            - "for each" statement.
		 */
		private void addForEachVariables(ForEachStatement foreachStatement)
		{
			Expression key = foreachStatement.getKey();
			Expression value = foreachStatement.getValue();

			if (key != null && key instanceof Variable)
			{
				String varName = getVariableName((Variable) key);
				if (varName != null)
				{
					VariableInfo info = new VariableInfo(varName, null, getCurrentScope(), key.getStart());
					getCurrentScope().addVariable(info);
				}
			}

			if (value != null && value instanceof Variable)
			{
				String varName = getVariableName((Variable) value);
				if (varName != null)
				{
					VariableInfo info = new VariableInfo(varName, null, getCurrentScope(), value.getStart());
					getCurrentScope().addVariable(info);
				}
			}
		}

		/**
		 * Adds "for" local variables.
		 * 
		 * @param forStatement
		 *            - "for" statement.
		 */
		private void addForVariables(ForStatement forStatement)
		{
			List<Expression> initializations = forStatement.initializers();
			if (initializations == null || initializations.size() == 0)
			{
				return;
			}

			for (Expression initialization : initializations)
			{
				if (initialization instanceof Assignment)
				{
					Assignment assigment = (Assignment) initialization;
					VariableBase var = assigment.getLeftHandSide();
					if (var instanceof Variable)
					{
						String varName = getVariableName((Variable) var);
						if (varName == null)
						{
							continue;
						}

						Set<Object> types = countExpressionTypes(assigment.getRightHandSide());
						if (types != null && types.size() != 0)
						{
							VariableInfo info = new VariableInfo(varName, types, getCurrentScope(), var.getStart());
							getCurrentScope().addVariable(info);
						}
					}
				}
			}
		}

		/**
		 * Adds catch clause local variables.
		 * 
		 * @param clause
		 *            - catch clause.
		 */
		private void addCatchClauseVariables(CatchClause clause)
		{
			Variable var = clause.getVariable();
			String varName = getVariableName(var);
			if (varName != null)
			{
				VariableInfo info = new VariableInfo(varName, "Exception", getCurrentScope(), var.getStart()); //$NON-NLS-1$
				getCurrentScope().addVariable(info);
			}
		}
	}

	/**
	 * types cache;
	 */
	private Map<Set<Object>, Set<String>> resolvedTypes = new HashMap<Set<Object>, Set<String>>();

	/**
	 * Mode.
	 */
	private boolean globalMode = true;

	/**
	 * Current offset.
	 */
	private int currentOffset = 0;

	/**
	 * Comments.
	 */
	private List<Comment> comments;

	/**
	 * Source
	 */
	private String source;

	/**
	 * Overall global imports in the reported stack.
	 */
	private Set<String> overallGlobalImports = new HashSet<String>();

	/**
	 * Whether reported stack is global one.
	 */
	private boolean isReportedStackGlobal = true;

	/**
	 * PDTPHPModuleIndexer constructor. Creates indexer in global mode.
	 */
	public TypeBindingBuilder()
	{
	}

	/**
	 * PDTPHPModuleIndexer constructor. Indexer might be created in one of two modes: global mode and local mode. In
	 * global mode indexer reports includes, classes, functions, methods, fields and only global variables. In local
	 * mode indexer reports includes, classes, functions, methods, fields, global variables and local variables that are
	 * visible from the offset specified.
	 * 
	 * @param globalMode
	 *            - true if global mode is on, false if local mode is on.
	 * @param currentOffset
	 *            - offset used for local mode counting.
	 */
	public TypeBindingBuilder(boolean globalMode, int currentOffset)
	{
		this.globalMode = globalMode;
		this.currentOffset = currentOffset;
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized void indexModule(Program program, String source, IBindingReporter reporter)
	{
		try
		{
			// grab the comments from the AST
			this.comments = program.comments();
			this.source = source;
			// indexing
			PHPASTVisitor visitor = new PHPASTVisitor(reporter, program);
			program.accept(visitor);
		}
		catch (Throwable th)
		{
			IdeLog.logError(PHPEditorPlugin.getDefault(),
					"Unexpected exception while indexing module " + program.toString(), th); //$NON-NLS-1$
		}
	}

	/**
	 * Gets global imports got while parsing in a local mode in the scopes stack.
	 * 
	 * @return global imports.
	 */
	public Set<String> getGlobalImports()
	{
		return overallGlobalImports;
	}

	/**
	 * Gets whether reported scope is global.
	 * 
	 * @return whether reported scope is global.
	 */
	public boolean isReportedScopeGlobal()
	{
		return isReportedStackGlobal;
	}

	/**
	 * Gets whether reported scope is under class or function/method.
	 * 
	 * @return true if whether reported scope is under class or function/method, false otherwise.
	 */
	public boolean isReportedScopeUnderClassOrFunction()
	{
		return isReportedStackGlobal;
	}

	/**
	 * Converts encoded type to its string representation.
	 * 
	 * @param intType
	 *            - integer-encoded type.
	 * @return string representation or null if not found.
	 */
	private static String typeToString(int intType)
	{
		switch (intType)
		{
			case Scalar.TYPE_INT:
				return IPHPTypeConstants.INTEGER_TYPE;
			case Scalar.TYPE_REAL:
				return IPHPTypeConstants.REAL_TYPE;
			case Scalar.TYPE_STRING:
				return IPHPTypeConstants.STRING_TYPE;
			case Scalar.TYPE_SYSTEM:
				return IPHPTypeConstants.SYSTEM_TYPE;
			case Scalar.TYPE_UNKNOWN:
				return IPHPTypeConstants.UNKNOWN_TYPE;
		}

		return null;
	}

	/**
	 * Finds function or method PHPDoc comment above.
	 * 
	 * @param offset
	 *            - offset to start search from.
	 * @return comment contents or null.
	 */
	private Comment findFunctionPHPDocComment(int offset)
	{
		return findComment(offset, Comment.TYPE_PHPDOC);
	}

	/**
	 * @param offset
	 * @param type
	 *            - The comment type
	 * @return
	 */
	private Comment findComment(int offset, int type)
	{
		if (comments == null || comments.isEmpty())
		{
			return null;
		}

		Comment nearestComment = null;
		for (Comment comment : comments)
		{
			if (comment.getStart() > offset)
			{
				break;
			}

			nearestComment = comment;
		}

		if (nearestComment == null)
		{
			return null;
		}

		if (nearestComment.getCommentType() != type)
		{
			return null;
		}
		if (source != null)
		{
			// checking if we have anything but whitespaces between comment end and
			// offset
			for (int i = nearestComment.getEnd() + 1; i < offset - 1; i++)
			{
				char ch = source.charAt(i);
				if (!Character.isWhitespace(ch))
				{
					return null;
				}
			}

			// return _contents.substring(nearestComment.getStart(), nearestComment.getEnd());
			return nearestComment;
		}
		else
		{
			return null;
		}
	}
}
