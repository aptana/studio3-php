/**
 * Copyright (c) 2005-2006 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package com.aptana.editor.php.internal.search;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.php.internal.core.ast.nodes.ASTNode;
import org.eclipse.php.internal.core.ast.nodes.ClassDeclaration;
import org.eclipse.php.internal.core.ast.nodes.Identifier;
import org.eclipse.php.internal.core.ast.nodes.MethodDeclaration;
import org.eclipse.php.internal.core.ast.nodes.TypeDeclaration;
import org.eclipse.php.internal.core.phpModel.parser.php5.CompletionLexer5;
import org.eclipse.php.internal.core.phpModel.parser.php5.PhpParser5;
import org.eclipse.php.internal.core.phpModel.phpElementData.PHPModifier;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;

import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.indexer.IElementsIndex;
import com.aptana.editor.php.indexer.IIndexChangeListener;
import com.aptana.editor.php.indexer.IPHPIndexConstants;
import com.aptana.editor.php.indexer.PHPGlobalIndexer;
import com.aptana.editor.php.internal.builder.BuildPathManager;
import com.aptana.editor.php.internal.builder.FileSystemModule;
import com.aptana.editor.php.internal.builder.IBuildPath;
import com.aptana.editor.php.internal.builder.IModule;
import com.aptana.editor.php.internal.builder.LocalModule;
import com.aptana.editor.php.internal.editor.PHPSourceEditor;
import com.aptana.editor.php.internal.indexer.AbstractPHPEntryValue;
import com.aptana.editor.php.internal.indexer.ClassPHPEntryValue;
import com.aptana.editor.php.internal.indexer.FunctionPHPEntryValue;
import com.aptana.editor.php.internal.indexer.VariablePHPEntryValue;
import com.aptana.editor.php.internal.model.utils.ModelUtils;
import com.aptana.editor.php.internal.parser.PHPMimeType;
import com.aptana.editor.php.internal.parser.nodes.NodeBuilderClient;
import com.aptana.editor.php.internal.parser.nodes.PHPBaseParseNode;
import com.aptana.editor.php.internal.parser.nodes.PHPBlockNode;
import com.aptana.editor.php.internal.parser.nodes.PHPClassParseNode;
import com.aptana.editor.php.model.IField;
import com.aptana.editor.php.model.IMethod;
import com.aptana.editor.php.model.IModelElement;
import com.aptana.editor.php.model.IType;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.parsing.lexer.Range;

/**
 * 
 * @author Pavel Petrochenko
 *
 */
public final class PHPSearchEngine {

	private static PHPSearchEngine instance;
	
	private static class ElementNode implements IElementNode {

		/**
		 * 
		 */
		protected final IElementEntry e;

		/**
		 * 
		 */
		protected final AbstractPHPEntryValue value;

		private int kind;

		/**
		 * @param e
		 * @param ea
		 * @param kind
		 */
		public ElementNode(IElementEntry e, AbstractPHPEntryValue ea, int kind) {
			super();
			this.e = e;
			this.value = ea;
			this.kind = kind;
		}

		/**
		 * @see com.aptana.ide.editor.php.search.IElementNode#toExternalReference()
		 */
		public ExternalReference toExternalReference() {
			IModule module = e.getModule();
			Range range = new Range(value.getStartOffset(), value
					.getStartOffset());
			if (module instanceof LocalModule) {
				LocalModule lmodule = (LocalModule) module;
				FileEditorInput fileEditorInput = new FileEditorInput(lmodule
						.getFile());
				return new ExternalReference(fileEditorInput, range);
			}
			if (module instanceof FileSystemModule) {
				FileSystemModule ms = (FileSystemModule) module;
				
				return new ExternalReference(CoreUIUtils
						.createJavaFileEditorInput(new File(ms.getFullPath())),
						range);
			}
			return null;
		}

		/**
		 * @see com.aptana.ide.editor.php.search.IElementNode#getModifiers()
		 */
		public int getModifiers() {
			return value.getModifiers();
		}

		/**
		 * @see com.aptana.ide.editor.php.search.IElementNode#getName()
		 */
		public String getName() {

			return e.getEntryPath();
		}

		/**
		 * @see com.aptana.ide.editor.php.search.IElementNode#getPath()
		 */
		public String getPath() {
			IModule module = e.getModule();
			if (module instanceof LocalModule) {
				LocalModule mz = (LocalModule) module;
				return mz.getFile().getFullPath().toPortableString().substring(
						1);
			}
			return module.getFullPath();
		}

		public int getKind() {
			return kind;
		}
	}

	private final class ClassNode extends ElementNode implements ITypeNode {

		private ClassNode(IElementEntry e, ClassPHPEntryValue ea) {
			super(e, ea, IElementNode.CLASS);
		}

		public boolean isOnBuildPath(IProject project) {
			IBuildPath buildPathByResource = BuildPathManager.getInstance()
					.getBuildPathByResource(project);
			HashSet<IBuildPath> pa = new HashSet<IBuildPath>();
			pa.add(buildPathByResource);
			pa.addAll(buildPathByResource.getDependencies());
			if (!pa.contains(e.getModule().getBuildPath())) {
				return false;
			}
			return true;
		}

		public PHPClassParseNode toParseNode() {
			return getClassNode(e);
		}

		/**
		 * 
		 */
		public String getIncludePath(IFile from) {
			String constructPathFromRoot = RefactoringUtils
					.constructPathFromRoot(e.getModule().getPath());
			IPath typePath = new Path(constructPathFromRoot);
			IPath fullPath = from.getProjectRelativePath();
			IPath commonRoot = fullPath.removeLastSegments(1);
			int count = 0;
			while (commonRoot != null && !commonRoot.isPrefixOf(typePath)) {
				commonRoot = commonRoot.removeLastSegments(1);
				count++;
			}
			StringBuilder result = new StringBuilder();
			int pos = count;
			while (count > 0) {
				result.append("../"); //$NON-NLS-1$
				count--;
			}
			if (pos > 0) {
				IPath removeFirstSegments = typePath
						.removeFirstSegments(commonRoot.segmentCount());
				result.append(removeFirstSegments.toPortableString());
			} else {
				result.append("./"); //$NON-NLS-1$
				result.append(typePath.toPortableString());
			}
			return result.toString();
		}
	}

	private IModule getModule(EditorFileContext fileContext) {
		String struri = fileContext.getFileContext().getSourceProvider()
				.getSourceURI();
		URI uri;
		try {
			uri = new URI(struri);
		} catch (URISyntaxException e) {
			PHPEditorPlugin.logError(e);
			return null;
		}
		IFile[] files = ResourcesPlugin.getWorkspace().getRoot()
				.findFilesForLocationURI(uri);
		if (files == null || files.length == 0) {
			return null;
		}
		return BuildPathManager.getInstance().getModuleByResource(files[0]);
	}

	/**
	 * Resolves class for a given name and the current module. Does not filter
	 * results if current module can not be determined.
	 * 
	 * @param name
	 *            - class name.
	 * @return parse node
	 */
	public PHPBaseParseNode resolveClass(String name) {
		IEditorPart activeEditor = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (activeEditor instanceof PHPSourceEditor) {
			PHPSourceEditor editor = (PHPSourceEditor) activeEditor;
			IFileLanguageService languageService = editor.getFileContext()
					.getLanguageService(PHPMimeType.MimeType);
			PHPOffsetMapper mapper = (PHPOffsetMapper) languageService
					.getOffsetMapper();
			String source = editor.getViewer().getDocument().get();
			IElementsIndex index = mapper.getIndex(source, source.length());
			IModule emodule = getModule(editor.getFileContext());
			List<IElementEntry> entries = index.getEntries(
					IElementsIndex.ANY_CETEGORY, name);
			Set<IElementEntry> filterByModule;
			if (emodule != null) {
				filterByModule = PHPContentAssistProcessor.filterByModule(
						entries, emodule, index);
			} else {
				filterByModule = new HashSet<IElementEntry>(entries.size());
				filterByModule.addAll(entries);
			}

			for (IElementEntry e : filterByModule) {
				if (e.getValue() instanceof ClassPHPEntryValue) {
					IModule module = e.getModule();
					try {
						// String readContent =
						// StreamUtils.readContent(module.getContents(),
						// Charset.defaultCharset().name());
						PhpParser5 parser = new PhpParser5();
						NodeBuilderClient parserClient = new NodeBuilderClient(
								false);
						CompletionLexer5 completionLexer5 = new CompletionLexer5(
								module.getContents());
						completionLexer5.setParserClient(parserClient);
						completionLexer5.setTasksPatterns(new Pattern[0]);
						parser.setScanner(completionLexer5);
						parser.setParserClient(parserClient);
						try {
							parser.parse();
						} catch (Exception ex) {
							PHPEditorPlugin.logError(ex);
						}
						PHPBlockNode populateNodes = parserClient
								.populateNodes();

						IParseNode findClassNode = findClassNode(populateNodes,
								name);
						return (PHPBaseParseNode) findClassNode;
					} catch (IOException e1) {
						return null;
					}
				}
			}
		}
		return null;
	}

	private IParseNode findClassNode(IParseNode populateNodes, String name) {
		if (populateNodes instanceof PHPBaseParseNode) {
			PHPBaseParseNode pn = (PHPBaseParseNode) populateNodes;
			if (pn.getNodeName().equals(name)
					&& pn instanceof PHPClassParseNode) {
				return pn;
			}
			IParseNode[] children = pn.getChildren();
			for (IParseNode n : children) {
				IParseNode findClassNode = findClassNode(n, name);
				if (findClassNode != null) {
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
	public ExternalReference resolveClassToReference(String name) {
		IEditorPart activeEditor = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (activeEditor instanceof PHPSourceEditor) {
			PHPSourceEditor editor = (PHPSourceEditor) activeEditor;
			IFileLanguageService languageService = editor.getFileContext()
					.getLanguageService(PHPMimeType.MimeType);
			PHPOffsetMapper mapper = (PHPOffsetMapper) languageService
					.getOffsetMapper();
			String source = editor.getViewer().getDocument().get();
			IElementsIndex index = mapper.getIndex(source, source.length());
			List<IElementEntry> entries = index.getEntries(
					IElementsIndex.ANY_CETEGORY, name);
			for (IElementEntry e : entries) {
				Object value = e.getValue();
				if (value instanceof ClassPHPEntryValue) {
					ClassPHPEntryValue ph = (ClassPHPEntryValue) value;
					IModule module = e.getModule();
					if (module instanceof LocalModule) {
						LocalModule lmodule = (LocalModule) module;
						FileEditorInput fileEditorInput = new FileEditorInput(
								lmodule.getFile());
						return new ExternalReference(fileEditorInput,
								new Range(ph.getStartOffset(), ph
										.getEndOffset()));
					}
					if (module instanceof FileSystemModule) {

						FileSystemModule ms = (FileSystemModule) module;
						IEditorInput createJavaFileEditorInput = CoreUIUtils
								.createJavaFileEditorInput(new File(ms
										.getFullPath()));
						return new ExternalReference(createJavaFileEditorInput,
								new Range(ph.getStartOffset(), ph
										.getEndOffset()));

					}
				}
			}
		}
		return null;
	}

	/**
	 * @see com.aptana.ide.editor.php.parsing.nodes.ITypeResolver#getAllKnownTypes()
	 */
	@SuppressWarnings("unchecked")
	public Collection<ITypeNode> getAllKnownTypes() {
		List<IElementEntry> entriesStartingWith = PHPGlobalIndexer
				.getInstance().getIndex().getEntriesStartingWith(
						IPHPIndexConstants.CLASS_CATEGORY, ""); //$NON-NLS-1$
		ArrayList<IElementNode> nodes = new ArrayList<IElementNode>();
		for (final IElementEntry e : entriesStartingWith) {
			processValue(nodes, e);
		}
		return (Collection)nodes;
	}

	@SuppressWarnings("unchecked")
	private void processValue(ArrayList nodes, final IElementEntry e) {
		Object value = e.getValue();
		if (value instanceof ClassPHPEntryValue) {
			final ClassPHPEntryValue ea = (ClassPHPEntryValue) value;
			ITypeNode node = new ClassNode(e, ea);
			nodes.add(node);
		} else if (value instanceof FunctionPHPEntryValue) {
			FunctionPHPEntryValue pa = (FunctionPHPEntryValue) value;
			nodes.add(new ElementNode(e, pa, IElementNode.FUNCTION));
		} else if (value instanceof VariablePHPEntryValue) {
			VariablePHPEntryValue pa = (VariablePHPEntryValue) value;
			nodes.add(new ElementNode(e, pa, IElementNode.CONSTANT));
		}
	}

	PHPClassParseNode getClassNode(IElementEntry e) {
		if (e.getValue() instanceof ClassPHPEntryValue) {
			IModule module = e.getModule();
			try {
				// String readContent =
				// StreamUtils.readContent(module.getContents(),
				// Charset.defaultCharset().name());
				PhpParser5 parser = new PhpParser5();
				NodeBuilderClient parserClient = new NodeBuilderClient(false);
				CompletionLexer5 completionLexer5 = new CompletionLexer5(module
						.getContents());
				completionLexer5.setParserClient(parserClient);
				completionLexer5.setTasksPatterns(new Pattern[0]);
				parser.setScanner(completionLexer5);
				parser.setParserClient(parserClient);
				try {
					parser.parse();
				} catch (Exception ex) {
					PHPEditorPlugin.logError(ex);
				}
				PHPBlockNode populateNodes = parserClient.populateNodes();
				IParseNode findClassNode = findClassNode(populateNodes, e
						.getEntryPath());
				return (PHPClassParseNode) findClassNode;
			} catch (IOException e1) {
				return null;
			}
		}
		return null;
	}

	/**
	 * @see com.aptana.ide.editor.php.parsing.nodes.ITypeResolver#addListener(com.aptana.ide.editor.php.indexer.IIndexChangeListener)
	 */
	public void addListener(IIndexChangeListener listener) {
		PHPGlobalIndexer.getInstance().addListener(listener);
	}

	/**
	 * @see com.aptana.ide.editor.php.parsing.nodes.ITypeResolver#removeListener(com.aptana.ide.editor.php.indexer.IIndexChangeListener)
	 */
	public void removeListener(IIndexChangeListener listener) {
		PHPGlobalIndexer.getInstance().removeListener(listener);
	}

	/**
	 * @see com.aptana.ide.editor.php.parsing.nodes.ITypeResolver#getTypes(java.lang.String)
	 */
	public Collection<ITypeNode> getTypes(String name) {
		List<IElementEntry> entriesStartingWith = PHPGlobalIndexer
				.getInstance().getIndex().getEntries(
						IPHPIndexConstants.CLASS_CATEGORY, name);
		ArrayList<ITypeNode> nodes = new ArrayList<ITypeNode>();
		for (final IElementEntry e : entriesStartingWith) {
			processValue(nodes, e);
		}
		return nodes;
	}

	/**
	 * @see com.aptana.ide.editor.php.parsing.nodes.ITypeResolver#getAllKnownConstants()
	 */
	public Collection<IElementNode> getAllKnownConstants() {
		List<IElementEntry> entriesStartingWith = PHPGlobalIndexer
				.getInstance().getIndex().getEntriesStartingWith(
						IPHPIndexConstants.CONST_CATEGORY, ""); //$NON-NLS-1$
		ArrayList<IElementNode> nodes = new ArrayList<IElementNode>();
		for (final IElementEntry e : entriesStartingWith) {
			processValue(nodes, e);
		}
		return nodes;
	}

	/**
	 * @see com.aptana.ide.editor.php.parsing.nodes.ITypeResolver#getAllKnownFunctions()
	 */
	public Collection<IElementNode> getAllKnownFunctions() {
		List<IElementEntry> entriesStartingWith = PHPGlobalIndexer
				.getInstance().getIndex().getEntriesStartingWith(
						IPHPIndexConstants.FUNCTION_CATEGORY, ""); //$NON-NLS-1$
		ArrayList<IElementNode> nodes = new ArrayList<IElementNode>();
		for (final IElementEntry e : entriesStartingWith) {
			processValue(nodes, e);
		}
		return nodes;
	}

	/**
	 * @see ITypeResolver#findOverriddenMethod(MethodDeclaration)
	 */
	public IMethodReference findOverriddenMethod(MethodDeclaration node) {
		ASTNode parent = node.getParent().getParent();
		String methodname = node.getFunction().getFunctionName().getName();
		if (parent instanceof ClassDeclaration) {
			ClassDeclaration decl = (ClassDeclaration) parent;
			Identifier superClass = decl.getSuperClass();
			if (superClass!=null) {
			String classname = superClass.getName();
			HashSet<String> visited = new HashSet<String>();
			IMethodReference checkType = checkType(classname, methodname, visited);
			if (checkType != null) {
				return checkType;
			}
			}
		}
		if (parent instanceof TypeDeclaration) {
			TypeDeclaration decl = (TypeDeclaration) parent;
			for (Identifier i : decl.getInterfaces()) {
				String name = i.getName();
				IMethodReference checkType = checkType(name, methodname,
						new HashSet<String>());
				if (checkType != null) {
					return checkType;
				}
			}
		}
		return null;
	}

	private IMethodReference checkType(final String classname, final String methodname,
			HashSet<String> visited) {
		if (!visited.contains(classname)) {
			visited.add(classname);
			IElementsIndex index = PHPGlobalIndexer.getInstance().getIndex();
			List<IElementEntry> entries = index.getEntries(
					IPHPIndexConstants.CLASS_CATEGORY, classname);
			for (IElementEntry e : entries) {
				final ClassPHPEntryValue value = (ClassPHPEntryValue) e.getValue();
				List<IElementEntry> entries2 = index.getEntries(
						IPHPIndexConstants.FUNCTION_CATEGORY, classname+IElementsIndex.DELIMITER+
								methodname);
				if (entries2 != null && !entries2.isEmpty()) {
					for (final IElementEntry ea:entries2) {
					final FunctionPHPEntryValue mvalue=(FunctionPHPEntryValue) ea.getValue();
					return new IMethodReference() {

						public String getQualifiedName() {
							return classname+"."+methodname;
						}

						public String name() {
							return methodname;
						}

						public ExternalReference toExternalReference() {
							IModule module = ea.getModule();
							Range range = new Range(mvalue.getStartOffset(), mvalue
									.getStartOffset());
							if (module instanceof LocalModule) {
								LocalModule lmodule = (LocalModule) module;
								FileEditorInput fileEditorInput = new FileEditorInput(lmodule
										.getFile());
								return new ExternalReference(fileEditorInput, range);
							}
							if (module instanceof FileSystemModule) {
								FileSystemModule ms = (FileSystemModule) module;
								return new ExternalReference(CoreUIUtils
										.createJavaFileEditorInput(new File(ms.getFullPath())),
										range);
							}
							return null;
						}

						public boolean isAbstract() {
							
							return PHPModifier.isAbstract(mvalue.getModifiers())||PHPModifier.isInterface(value.getModifiers());
						}

					};
					}
				}
				String superClassname = value.getSuperClassname();
				if (superClassname!=null&&superClassname.length()>0) {
					IMethodReference checkType = checkType(superClassname, methodname, visited);
					if (checkType!=null) {
						return checkType;
					}
				}
				List<String> interfaces = value.getInterfaces();
				if (interfaces!=null) {
					for (String s:interfaces) {
						IMethodReference checkType = checkType(s, methodname, visited);
						if (checkType!=null) {
							return checkType;
						}	
					}
				}
			};
			
		}
		return null;
	}
	
	private PHPSearchEngine(){
		
	}
	
	public static synchronized PHPSearchEngine getInstance(){
		if (instance==null){
			instance=new PHPSearchEngine();
		}
		return instance;
	}
	
	 
	public IType[] findTypes(String name,ISearchScope scope){
		List<IElementEntry> entries = PHPGlobalIndexer.getInstance()
		.getIndex().getEntries(IPHPIndexConstants.CLASS_CATEGORY,
				name);
		if (entries!=null){
			List<IType> convertClasses = ModelUtils.convertTypes(entries);
			return convertClasses.toArray(new IType[convertClasses.size()]);
		}
		return null;		
	}
	
	public IMethod[] findMethods(String name,ISearchScope scope){
		List<IElementEntry> entries = PHPGlobalIndexer.getInstance()
		.getIndex().getEntries(IPHPIndexConstants.FUNCTION_CATEGORY,
				name);
		if (entries!=null){
			List<IModelElement> convertClasses = ModelUtils.convertEntries(entries);
			return convertClasses.toArray(new IMethod[convertClasses.size()]);
		}
		return null;		
	}
	
	public IField[] findVariables(String name,ISearchScope scope){
		List<IElementEntry> entries = PHPGlobalIndexer.getInstance()
		
		.getIndex().getEntries(IPHPIndexConstants.VAR_CATEGORY,
				name);
		if (entries!=null){
			List<IModelElement> convertClasses = ModelUtils.convertEntries(entries);
			return convertClasses.toArray(new IField[convertClasses.size()]);
		}
		return null;		
	}
	
	public IField[] findConstants(String name,ISearchScope scope){
		List<IElementEntry> entries = PHPGlobalIndexer.getInstance()
		.getIndex().getEntries(IPHPIndexConstants.CONST_CATEGORY,
				name);
		if (entries!=null){
			List<IModelElement> convertClasses = ModelUtils.convertEntries(entries);
			return convertClasses.toArray(new IField[convertClasses.size()]);
		}
		return null;		
	}
}
