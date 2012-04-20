/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org2.eclipse.php.core.compiler.PHPFlags;
import org2.eclipse.php.internal.core.PHPVersion;
import org2.eclipse.php.internal.core.ast.nodes.ASTNode;
import org2.eclipse.php.internal.core.ast.nodes.ClassDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.Expression;
import org2.eclipse.php.internal.core.ast.nodes.Identifier;
import org2.eclipse.php.internal.core.ast.nodes.MethodDeclaration;
import org2.eclipse.php.internal.core.ast.nodes.TypeDeclaration;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.core.model.IField;
import com.aptana.editor.php.core.model.IMethod;
import com.aptana.editor.php.core.model.IModelElement;
import com.aptana.editor.php.core.model.IType;
import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.indexer.IElementsIndex;
import com.aptana.editor.php.indexer.IIndexChangeListener;
import com.aptana.editor.php.indexer.IPHPIndexConstants;
import com.aptana.editor.php.indexer.PHPGlobalIndexer;
import com.aptana.editor.php.internal.builder.BuildPathManager;
import com.aptana.editor.php.internal.builder.FileSystemModule;
import com.aptana.editor.php.internal.builder.LocalModule;
import com.aptana.editor.php.internal.contentAssist.ContentAssistFilters;
import com.aptana.editor.php.internal.contentAssist.mapping.PHPOffsetMapper;
import com.aptana.editor.php.internal.core.builder.IBuildPath;
import com.aptana.editor.php.internal.core.builder.IModule;
import com.aptana.editor.php.internal.indexer.AbstractPHPEntryValue;
import com.aptana.editor.php.internal.indexer.ClassPHPEntryValue;
import com.aptana.editor.php.internal.indexer.FunctionPHPEntryValue;
import com.aptana.editor.php.internal.indexer.TraitPHPEntryValue;
import com.aptana.editor.php.internal.indexer.VariablePHPEntryValue;
import com.aptana.editor.php.internal.model.utils.ModelUtils;
import com.aptana.editor.php.internal.parser.PHPParser;
import com.aptana.editor.php.internal.parser.nodes.IPHPParseNode;
import com.aptana.editor.php.internal.parser.nodes.PHPClassParseNode;
import com.aptana.editor.php.internal.refactoring.RefactoringUtils;
import com.aptana.editor.php.internal.ui.editor.PHPSourceEditor;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.parsing.lexer.Range;

/**
 * @author Pavel Petrochenko, Shalom Gibly
 */
public final class PHPSearchEngine
{

	private static PHPSearchEngine instance;

	private static class ElementNode implements IElementNode
	{

		protected final IElementEntry e;
		protected final AbstractPHPEntryValue value;
		private int kind;

		/**
		 * @param e
		 * @param ea
		 * @param kind
		 */
		private ElementNode(IElementEntry e, AbstractPHPEntryValue ea, int kind)
		{
			super();
			this.e = e;
			this.value = ea;
			this.kind = kind;
		}

		/**
		 * @see com.aptana.editor.php.search.IElementNode#toExternalReference()
		 */
		public ExternalReference toExternalReference()
		{
			IModule module = e.getModule();
			Range range = new Range(value.getStartOffset(), value.getStartOffset());
			return PHPSearchEngine.getInstance().getModuleReference(module, range);
		}

		/**
		 * @see com.aptana.editor.php.search.IElementNode#getModifiers()
		 */
		public int getModifiers()
		{
			return value.getModifiers();
		}

		/**
		 * @see com.aptana.editor.php.search.IElementNode#getName()
		 */
		public String getName()
		{

			return e.getEntryPath();
		}

		/**
		 * @see com.aptana.editor.php.search.IElementNode#getPath()
		 */
		public String getPath()
		{
			IModule module = e.getModule();
			if (module instanceof LocalModule)
			{
				LocalModule mz = (LocalModule) module;
				return mz.getFile().getFullPath().toPortableString().substring(1);
			}
			return module.getFullPath();
		}

		public int getKind()
		{
			return kind;
		}
	}

	private class ClassNode extends ElementNode implements ITypeNode
	{

		protected ClassNode(IElementEntry e, ClassPHPEntryValue ea, int kind)
		{
			super(e, ea, kind);
		}

		private ClassNode(IElementEntry e, ClassPHPEntryValue ea)
		{
			this(e, ea, IElementNode.CLASS);
		}

		public boolean isOnBuildPath(IProject project)
		{
			IBuildPath buildPathByResource = BuildPathManager.getInstance().getBuildPathByResource(project);
			Set<IBuildPath> pa = new HashSet<IBuildPath>();
			pa.add(buildPathByResource);
			pa.addAll(buildPathByResource.getDependencies());
			if (!pa.contains(e.getModule().getBuildPath()))
			{
				return false;
			}
			return true;
		}

		public PHPClassParseNode toParseNode()
		{
			return getClassNode(e);
		}

		public String getIncludePath(IFile from)
		{
			String constructPathFromRoot = RefactoringUtils.constructPathFromRoot(e.getModule().getPath());
			IPath typePath = new Path(constructPathFromRoot);
			IPath fullPath = from.getProjectRelativePath();
			IPath commonRoot = fullPath.removeLastSegments(1);
			int count = 0;
			while (commonRoot != null && !commonRoot.isPrefixOf(typePath))
			{
				commonRoot = commonRoot.removeLastSegments(1);
				count++;
			}
			StringBuilder result = new StringBuilder();
			int pos = count;
			while (count > 0)
			{
				result.append("../"); //$NON-NLS-1$
				count--;
			}
			if (pos > 0)
			{
				IPath removeFirstSegments = typePath.removeFirstSegments(commonRoot.segmentCount());
				result.append(removeFirstSegments.toPortableString());
			}
			else
			{
				result.append("./"); //$NON-NLS-1$
				result.append(typePath.toPortableString());
			}
			return result.toString();
		}
	}

	private class TraitNode extends ClassNode
	{

		protected TraitNode(IElementEntry e, ClassPHPEntryValue ea)
		{
			super(e, ea, ElementNode.TRAIT);
		}
	}

	// private IModule getModule(EditorFileContext fileContext)
	// {
	// String struri = fileContext.getFileContext().getSourceProvider().getSourceURI();
	// URI uri;
	// try
	// {
	// uri = new URI(struri);
	// }
	// catch (URISyntaxException e)
	// {
	// PHPEditorPlugin.logError(e);
	// return null;
	// }
	// IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(uri);
	// if (files == null || files.length == 0)
	// {
	// return null;
	// }
	// return BuildPathManager.getInstance().getModuleByResource(files[0]);
	// }

	/**
	 * Resolves class for a given name and the current module. Does not filter results if current module can not be
	 * determined.
	 * 
	 * @param name
	 *            - class name.
	 * @return parse node
	 */
	public IPHPParseNode resolveClass(String name)
	{
		IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
				.getActiveEditor();
		if (activeEditor instanceof PHPSourceEditor)
		{
			PHPSourceEditor editor = (PHPSourceEditor) activeEditor;
			PHPOffsetMapper mapper = editor.getOffsetMapper();
			String source = editor.getDocumentProvider().getDocument(editor.getEditorInput()).get();
			IElementsIndex index = mapper.getIndex(source, source.length());
			IModule emodule = editor.getModule();
			List<IElementEntry> entries = index.getEntries(IElementsIndex.ANY_CETEGORY, name);
			Set<IElementEntry> filterByModule;
			if (emodule != null)
			{
				filterByModule = ContentAssistFilters.filterByModule(entries, emodule, index);
			}
			else
			{
				filterByModule = new HashSet<IElementEntry>(entries.size());
				filterByModule.addAll(entries);
			}

			for (IElementEntry e : filterByModule)
			{
				if (e.getValue() instanceof ClassPHPEntryValue)
				{
					IModule module = e.getModule();
					try
					{
						// FIXME: Shalom - Perhaps get the real PHP version from the module.
						PHPParser parser = new PHPParser(PHPVersion.getLatest(), false);
						IParseNode parseNode = parser.parse(module.getContents());
						IParseNode findClassNode = findClassNode(parseNode, name);
						return (IPHPParseNode) findClassNode;
					}
					catch (Exception e1)
					{
						return null;
					}
				}
			}
		}
		return null;
	}

	private IParseNode findClassNode(IParseNode populateNodes, String name)
	{
		if (populateNodes instanceof IPHPParseNode)
		{
			IPHPParseNode pn = (IPHPParseNode) populateNodes;
			if (pn.getNodeName().equals(name) && pn instanceof PHPClassParseNode)
			{
				return pn;
			}
			IParseNode[] children = pn.getChildren();
			for (IParseNode n : children)
			{
				IParseNode findClassNode = findClassNode(n, name);
				if (findClassNode != null)
				{
					return findClassNode;
				}
			}
		}
		return null;
	}

	/**
	 * resolves class to reference in the code
	 * 
	 * @param name
	 * @return reference
	 */
	public ExternalReference resolveClassToReference(String name)
	{
		/*
		 * IEditorPart activeEditor = PlatformUI.getWorkbench()
		 * .getActiveWorkbenchWindow().getActivePage().getActiveEditor(); if (activeEditor instanceof PHPSourceEditor) {
		 * PHPSourceEditor editor = (PHPSourceEditor) activeEditor; IFileLanguageService languageService =
		 * editor.getFileContext() .getLanguageService(PHPMimeType.MIME_TYPE); PHPOffsetMapper mapper =
		 * (PHPOffsetMapper) languageService .getOffsetMapper(); String source = editor.getViewer().getDocument().get();
		 * IElementsIndex index = mapper.getIndex(source, source.length()); List<IElementEntry> entries =
		 * index.getEntries( IElementsIndex.ANY_CETEGORY, name); for (IElementEntry e : entries) { Object value =
		 * e.getValue(); if (value instanceof ClassPHPEntryValue) { ClassPHPEntryValue ph = (ClassPHPEntryValue) value;
		 * IModule module = e.getModule(); if (module instanceof LocalModule) { LocalModule lmodule = (LocalModule)
		 * module; FileEditorInput fileEditorInput = new FileEditorInput( lmodule.getFile()); return new
		 * ExternalReference(fileEditorInput, new Range(ph.getStartOffset(), ph .getEndOffset())); } if (module
		 * instanceof FileSystemModule) { FileSystemModule ms = (FileSystemModule) module; IEditorInput
		 * createJavaFileEditorInput = CoreUIUtils .createJavaFileEditorInput(new File(ms .getFullPath())); return new
		 * ExternalReference(createJavaFileEditorInput, new Range(ph.getStartOffset(), ph .getEndOffset())); } } } }
		 */
		// TODO: Shalom Implement me
		IdeLog.logWarning(PHPEditorPlugin.getDefault(),
				"Missing implementation for PHPSearchEngine::resolveClassToReference()", PHPEditorPlugin.DEBUG_SCOPE); //$NON-NLS-1$
		return null;
	}

	/**
	 * @see com.aptana.editor.php.parsing.nodes.ITypeResolver#getAllKnownTypes()
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Collection<ITypeNode> getAllKnownTypes()
	{
		List<IElementEntry> entriesStartingWith = PHPGlobalIndexer.getInstance().getIndex()
				.getEntriesStartingWith(IPHPIndexConstants.CLASS_CATEGORY, ""); //$NON-NLS-1$
		List<IElementNode> nodes = new ArrayList<IElementNode>();
		for (final IElementEntry e : entriesStartingWith)
		{
			processValue(nodes, e);
		}
		return (Collection) nodes;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void processValue(List nodes, final IElementEntry e)
	{
		Object value = e.getValue();
		if (value instanceof TraitPHPEntryValue)
		{
			TraitPHPEntryValue tv = (TraitPHPEntryValue) value;
			TraitNode node = new TraitNode(e, tv);
			nodes.add(node);
		}
		else if (value instanceof ClassPHPEntryValue)
		{
			ClassPHPEntryValue ea = (ClassPHPEntryValue) value;
			ITypeNode node = new ClassNode(e, ea);
			nodes.add(node);
		}
		else if (value instanceof FunctionPHPEntryValue)
		{
			FunctionPHPEntryValue pa = (FunctionPHPEntryValue) value;
			nodes.add(new ElementNode(e, pa, IElementNode.FUNCTION));
		}
		else if (value instanceof VariablePHPEntryValue)
		{
			VariablePHPEntryValue pa = (VariablePHPEntryValue) value;
			nodes.add(new ElementNode(e, pa, IElementNode.CONSTANT));
		}
	}

	PHPClassParseNode getClassNode(IElementEntry e)
	{
		if (e.getValue() instanceof ClassPHPEntryValue)
		{
			IModule module = e.getModule();
			try
			{
				// FIXME: Shalom - Parhaps get the real PHP version from the module.
				PHPParser parser = new PHPParser(PHPVersion.getLatest(), false);
				IParseNode parseNode = parser.parse(module.getContents());
				IParseNode findClassNode = findClassNode(parseNode, e.getEntryPath());
				return (PHPClassParseNode) findClassNode;
			}
			catch (Exception ex)
			{
				IdeLog.logError(PHPEditorPlugin.getDefault(), "Error getting a PHP class node", ex); //$NON-NLS-1$
			}
		}
		return null;
	}

	/**
	 * @see com.aptana.editor.php.parsing.nodes.ITypeResolver#addListener(com.aptana.editor.php.indexer.IIndexChangeListener)
	 */
	public void addListener(IIndexChangeListener listener)
	{
		PHPGlobalIndexer.getInstance().addListener(listener);
	}

	/**
	 * @see com.aptana.editor.php.parsing.nodes.ITypeResolver#removeListener(com.aptana.editor.php.indexer.IIndexChangeListener)
	 */
	public void removeListener(IIndexChangeListener listener)
	{
		PHPGlobalIndexer.getInstance().removeListener(listener);
	}

	/**
	 * @see com.aptana.editor.php.parsing.nodes.ITypeResolver#getTypes(java.lang.String)
	 */
	public Collection<ITypeNode> getTypes(String name)
	{
		List<IElementEntry> entriesStartingWith = PHPGlobalIndexer.getInstance().getIndex()
				.getEntries(IPHPIndexConstants.CLASS_CATEGORY, name);
		List<ITypeNode> nodes = new ArrayList<ITypeNode>();
		for (final IElementEntry e : entriesStartingWith)
		{
			processValue(nodes, e);
		}
		return nodes;
	}

	/**
	 * @see com.aptana.editor.php.parsing.nodes.ITypeResolver#getAllKnownConstants()
	 */
	public Collection<IElementNode> getAllKnownConstants()
	{
		List<IElementEntry> entriesStartingWith = PHPGlobalIndexer.getInstance().getIndex()
				.getEntriesStartingWith(IPHPIndexConstants.CONST_CATEGORY, ""); //$NON-NLS-1$
		List<IElementNode> nodes = new ArrayList<IElementNode>();
		for (final IElementEntry e : entriesStartingWith)
		{
			processValue(nodes, e);
		}
		return nodes;
	}

	/**
	 * @see com.aptana.editor.php.parsing.nodes.ITypeResolver#getAllKnownFunctions()
	 */
	public Collection<IElementNode> getAllKnownFunctions()
	{
		List<IElementEntry> entriesStartingWith = PHPGlobalIndexer.getInstance().getIndex()
				.getEntriesStartingWith(IPHPIndexConstants.FUNCTION_CATEGORY, ""); //$NON-NLS-1$
		List<IElementNode> nodes = new ArrayList<IElementNode>();
		for (final IElementEntry e : entriesStartingWith)
		{
			processValue(nodes, e);
		}
		return nodes;
	}

	/**
	 * @see ITypeResolver#findOverriddenMethod(MethodDeclaration)
	 */
	public IMethodReference findOverriddenMethod(MethodDeclaration node)
	{
		ASTNode parent = node.getParent().getParent();
		String methodname = node.getFunction().getFunctionName().getName();
		if (parent instanceof ClassDeclaration)
		{
			ClassDeclaration decl = (ClassDeclaration) parent;
			Expression superClass = decl.getSuperClass();
			if (superClass != null && superClass.getType() == ASTNode.IDENTIFIER)
			{
				String classname = ((Identifier) superClass).getName();
				Set<String> visited = new HashSet<String>();
				IMethodReference checkType = checkType(classname, methodname, visited);
				if (checkType != null)
				{
					return checkType;
				}
			}
		}
		if (parent instanceof TypeDeclaration)
		{
			TypeDeclaration decl = (TypeDeclaration) parent;
			for (Identifier i : decl.interfaces())
			{
				String name = i.getName();
				IMethodReference checkType = checkType(name, methodname, new HashSet<String>());
				if (checkType != null)
				{
					return checkType;
				}
			}
		}
		return null;
	}

	private IMethodReference checkType(final String classname, final String methodname, Set<String> visited)
	{
		if (!visited.contains(classname))
		{
			visited.add(classname);
			IElementsIndex index = PHPGlobalIndexer.getInstance().getIndex();
			List<IElementEntry> entries = index.getEntries(IPHPIndexConstants.CLASS_CATEGORY, classname);
			for (IElementEntry e : entries)
			{
				final ClassPHPEntryValue value = (ClassPHPEntryValue) e.getValue();
				List<IElementEntry> entries2 = index.getEntries(IPHPIndexConstants.FUNCTION_CATEGORY, classname
						+ IElementsIndex.DELIMITER + methodname);
				if (entries2 != null && !entries2.isEmpty())
				{
					for (final IElementEntry ea : entries2)
					{
						final FunctionPHPEntryValue mvalue = (FunctionPHPEntryValue) ea.getValue();
						return new IMethodReference()
						{

							public String getQualifiedName()
							{
								return classname + "." + methodname; //$NON-NLS-1$
							}

							public String name()
							{
								return methodname;
							}

							public ExternalReference toExternalReference()
							{
								IModule module = ea.getModule();
								Range range = new Range(mvalue.getStartOffset(), mvalue.getStartOffset());
								return getModuleReference(module, range);
							}

							public boolean isAbstract()
							{

								return PHPFlags.isAbstract(mvalue.getModifiers())
										|| PHPFlags.isInterface(value.getModifiers());
							}

						};
					}
				}
				String superClassname = value.getSuperClassname();
				if (superClassname != null && superClassname.length() > 0)
				{
					IMethodReference checkType = checkType(superClassname, methodname, visited);
					if (checkType != null)
					{
						return checkType;
					}
				}
				List<String> interfaces = value.getInterfaces();
				if (interfaces != null)
				{
					for (String s : interfaces)
					{
						IMethodReference checkType = checkType(s, methodname, visited);
						if (checkType != null)
						{
							return checkType;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * @param module
	 * @param range
	 */
	protected ExternalReference getModuleReference(IModule module, Range range)
	{
		if (module instanceof LocalModule)
		{
			LocalModule lmodule = (LocalModule) module;
			FileEditorInput fileEditorInput = new FileEditorInput(lmodule.getFile());
			return new ExternalReference(fileEditorInput, range);
		}
		if (module instanceof FileSystemModule)
		{
			try
			{
				return new ExternalReference(new FileStoreEditorInput(EFS.getStore(((FileSystemModule) module)
						.getExternalFile().getURI())), range);
			}
			catch (CoreException ce)
			{
				IdeLog.logError(PHPEditorPlugin.getDefault(), "Error computing the external reference", ce); //$NON-NLS-1$
			}
		}
		return null;
	}

	private PHPSearchEngine()
	{

	}

	public static synchronized PHPSearchEngine getInstance()
	{
		if (instance == null)
		{
			instance = new PHPSearchEngine();
		}
		return instance;
	}

	public IType[] findTypes(String name, ISearchScope scope)
	{
		List<IElementEntry> entries = PHPGlobalIndexer.getInstance().getIndex()
				.getEntries(IPHPIndexConstants.CLASS_CATEGORY, name);
		if (entries != null)
		{
			List<IType> convertClasses = ModelUtils.convertTypes(entries);
			return convertClasses.toArray(new IType[convertClasses.size()]);
		}
		return null;
	}

	public IMethod[] findMethods(String name, ISearchScope scope)
	{
		List<IElementEntry> entries = PHPGlobalIndexer.getInstance().getIndex()
				.getEntries(IPHPIndexConstants.FUNCTION_CATEGORY, name);
		if (entries != null)
		{
			List<IModelElement> convertClasses = ModelUtils.convertEntries(entries);
			return convertClasses.toArray(new IMethod[convertClasses.size()]);
		}
		return null;
	}

	public IField[] findVariables(String name, ISearchScope scope)
	{
		List<IElementEntry> entries = PHPGlobalIndexer.getInstance()

		.getIndex().getEntries(IPHPIndexConstants.VAR_CATEGORY, name);
		if (entries != null)
		{
			List<IModelElement> convertClasses = ModelUtils.convertEntries(entries);
			return convertClasses.toArray(new IField[convertClasses.size()]);
		}
		return null;
	}

	public IField[] findConstants(String name, ISearchScope scope)
	{
		List<IElementEntry> entries = PHPGlobalIndexer.getInstance().getIndex()
				.getEntries(IPHPIndexConstants.CONST_CATEGORY, name);
		if (entries != null)
		{
			List<IModelElement> convertClasses = ModelUtils.convertEntries(entries);
			return convertClasses.toArray(new IField[convertClasses.size()]);
		}
		return null;
	}
}
