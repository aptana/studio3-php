/**
 * Copyright (c) 2005-2008 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package com.aptana.editor.php.internal.parser2;

import java.io.CharArrayReader;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.php.internal.core.ast.nodes.Program;
import org.eclipse.php.internal.core.ast.parser.ASTParser;
import org.eclipse.php.internal.core.ast.parser.PhpAstLexer53;
import org.eclipse.php.internal.core.ast.parser.PhpAstParser53;
import org.eclipse.php.internal.core.phpModel.javacup.runtime.Symbol;
import org.eclipse.php.internal.core.phpModel.parser.php5.CompletionLexer5;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.PageBookView;

import com.aptana.editor.common.parsing.FileService;
import com.aptana.editor.php.core.PHPNature;
import com.aptana.editor.php.internal.builder.BuildPathManager;
import com.aptana.editor.php.internal.builder.FileSystemModule;
import com.aptana.editor.php.internal.builder.IModule;
import com.aptana.editor.php.internal.parser.PHPMimeType;
import com.aptana.editor.php.internal.parser.nodes.NodeBuildingVisitor;
import com.aptana.editor.php.internal.parser2.nodes.NodeBuilderClient;
import com.aptana.editor.php.utils.PHPASTVisitorProxy;
import com.aptana.editor.php.utils.PHPASTVisitorStub;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.parsing.lexer.Range;

public class PHPBackgroundParser extends PHPParser {

	private boolean returnImmediatly;
	private long lastChange;
	private WeakHashMap<Object, NodeBuilderClient> parserClients = new WeakHashMap<Object, NodeBuilderClient>();
	private volatile boolean isSheduled;
	private int posStart;
	private int posEnd;

	private LexemeList lexemeCache;
	private MatcherLexer lexer;

	private HashSet<IBackgroundParserListener> listeners = new HashSet<IBackgroundParserListener>();

	public synchronized void addBackgroundParserListener(
			IBackgroundParserListener listener) {
		listeners.add(listener);
	}

	public synchronized void removeBackgroundParserListener(
			IBackgroundParserListener listener) {
		listeners.remove(listener);
	}

	/**
	 * getCachedLexeme
	 * 
	 * @return Lexeme
	 */
	private Lexeme getCachedLexeme() {
		Lexeme result = null;
		int currentOffset = lexer.getCurrentOffset();

		if (this.lexemeCache != null) {
			// search for lexeme at the current offset in our lexeme cache
			if (lastPosition != -1 && false) {
				int p = currentOffset;

				while (p < sourceUnsafe.length
						&& (sourceUnsafe[p] == ' ' || sourceUnsafe[p] == '\t'
								|| sourceUnsafe[p] == '\r' || sourceUnsafe[p] == '\n')) {
					p++;
				}
				Lexeme lexeme = lexemeCache.get(lastPosition + 1);
				if (lexeme != null && lexeme.offset <= p
						&& lexeme.offset + lexeme.length >= p) {
					lexer.setCurrentOffset(p + lexeme.length);
					lastPosition = lastPosition + 1;
					return lexeme;
				}
			}
			int index = this.lexemeCache.getLexemeIndex(currentOffset);
			if (index >= 0) {
				result = this.lexemeCache.get(index);
				// update our current position
				lexer.setCurrentOffset(currentOffset + result.length);
				lastPosition = index;
			} else {
				Range range = this.lexemeCache.getAffectedRegion();
				// make sure we're not in the affected region
				if (range.containsOffset(currentOffset) == false) {
					// we're aren't in the affected region, so adjust
					// the index to the next item in our cache
					index = -(index + 1);

					// make sure our index is not off the end of the
					// cache list
					if (index < this.lexemeCache.size()) {
						// get the starting offset of the affected
						// region
						int startingOffset = range.getStartingOffset();

						// get our candidate lexeme from the cache
						Lexeme candidate = this.lexemeCache.get(index);

						// make sure we're either already past the
						// affected region OR that candidate in the
						// cache does not cross through the affected
						// region
						if (currentOffset >= range.getEndingOffset()
								|| (currentOffset < startingOffset && candidate
										.getEndingOffset() <= startingOffset)) {
							result = candidate;
							lastPosition = index;
							lexer.setCurrentOffset(result.getEndingOffset());
						}
					}
				}
			}
		}
		if (result == null) {
			lastPosition = -1;
		}
		return result;
	}

	private final class UpdaterThread extends Thread {
		private final MatcherLexer mlexer;
		private final ILexer lexer;
		private final Object service;
		boolean breakFrom;

		private UpdaterThread(MatcherLexer mlexer, ILexer lexer, Object service) {
			this.mlexer = mlexer;
			this.lexer = lexer;
			this.service = service;
		}

		public void run() {
			try {
				synchronized (mlexer) {
					while (!breakFrom) {
						long currentTimeMillis = System.currentTimeMillis();
						while (!(currentTimeMillis - lastChange > 300)) {
							Thread.sleep(300);
							currentTimeMillis = System.currentTimeMillis();
						}
						final char[] sourceUnsafe2 = lexer.getSourceUnsafe();
						try {
							NodeBuilderClient doParse = doParse(sourceUnsafe2,
									service);
							if (doParse != null) {
								updateFileContext(lexer, service,
										sourceUnsafe2, doParse);
							} else {
								isSheduled = false;
								return;
							}
						} catch (Throwable e) {
							isSheduled = false;
							return;
						}
					}

				}
			} catch (InterruptedException e1) {
				PHPPlugin.log(e1);
			}
		}

		private void updateFileContext(final ILexer lexer,
				final Object service, final char[] sourceUnsafe2,
				final NodeBuilderClient doParse) {
			Display.getDefault().syncExec(new Runnable() {

				public void run() {

					char[] sourceUnsafe3 = lexer.getSourceUnsafe();
					if (sourceUnsafe3 == sourceUnsafe2) {
						breakFrom = true;
						lastSource = sourceUnsafe3;
					} else {
						return;
					}
					setParserClient(service, doParse);
					returnImmediatly = true;
					isSheduled = false;
					try {
						// /System.out.println("Updating");
						TreeViewer outline = getOutline();
						TreePath[] pathes = outline != null ? outline
								.getExpandedTreePaths() : null;
						IUnifiedEditor editor = (IUnifiedEditor) service;
						FileService serv = (FileService) editor
								.getFileContext().getFileContext();
						serv.updateContent("", 0, 0);
						if (outline != null) {
							outline.refresh(true);
							outline.setExpandedTreePaths(pathes);
						}
					} finally {
						returnImmediatly = false;
						// System.out.println("Finished Background update");
					}
				}

			});
		}
	}

	private NodeBuilderClient doParse(final char[] sourceUnsafe, Object service) {

		final NodeBuilderClient parserClient = new NodeBuilderClient();

		boolean isPHP53 = true;
		if (isPHP53) {
			PhpAstLexer53 ls = new PhpAstLexer53(new CharArrayReader(
					sourceUnsafe));
			PhpAstParser53 astParser = new PhpAstParser53(ls);
			try {
				Symbol parse = astParser.parse();
				Program pr = (Program) parse.value;
				pr.setHasSyntaxErrors(astParser.hasSyntaxErrors());
				ArrayList<IBackgroundParserListener> arrayList = null;
				synchronized (this) {
					arrayList = new ArrayList<IBackgroundParserListener>(
							listeners);
				}
				PHPASTVisitorStub phpastVisitorStub = new NodeBuildingVisitor(
						parserClient);
				PHPASTVisitorProxy proxy = new PHPASTVisitorProxy(
						phpastVisitorStub);

				proxy.visit(pr);
				for (IBackgroundParserListener l : arrayList) {
					try {
						l.reconciled(pr, getModule(service));
					} catch (Throwable e) {
						IdeLog.logError(PHPPlugin.getDefault(), e.getMessage(),
								e);
					}
				}
				return parserClient;
			} catch (Exception e) {
				PHPPlugin.log(e);
				return parserClient;
			}
		}

		// lastSource = sourceUnsafe;
		CompletionLexer5 completionLexer5 = new CompletionLexer5(
				new CharArrayReader(sourceUnsafe.clone(), 0,
						sourceUnsafe.length));
		completionLexer5.setParserClient(parserClient);

		completionLexer5.setTasksPatterns(TaskPatternsProvider.getInstance()
				.getPetternsForWorkspace());
		parser.setScanner(completionLexer5);
		parser.setParserClient(parserClient);
		try {
			parser.parse();
		} catch (Throwable e) {
			IdeLog.logError(PHPPlugin.getDefault(), e.getMessage(), e);
		}
		try {
			Program parse = ASTParser.parse(new CharArrayReader(sourceUnsafe));
			parse.setHasSyntaxErrors(parserClient.hasSyntaxErrors());
			ArrayList<IBackgroundParserListener> arrayList = null;
			synchronized (this) {
				arrayList = new ArrayList<IBackgroundParserListener>(listeners);
			}
			for (IBackgroundParserListener l : arrayList) {
				try {
					l.reconciled(parse, getModule(service));
				} catch (Throwable e) {
					IdeLog.logError(PHPPlugin.getDefault(), e.getMessage(), e);
				}
			}
		} catch (Throwable e) {

			// we should not report parsing exception as such kind of exception
			// is viable here
			return null;
			// IdeLog.logError(PHPPlugin.getDefault(),e.getMessage(),e);
		}
		return parserClient;
	}

	private Object getOwner() {

		PHPParseState parseState = (PHPParseState) getParseState()
				.getParseState(PHPMimeType.MimeType);
		IUnifiedEditor ef = (IUnifiedEditor) parseState.getOwner();
		return ef;
	}

	public synchronized void parseAll(IParseNode parentNode)
			throws ParseException, LexerException {
		lastPosition = -1;
		lexer = (MatcherLexer) getLexer();
		sourceUnsafe = lexer.getSourceUnsafe();
		LexemeList lexemeList = getLexemeList();
		this.lexemeCache = lexemeList;
		Range affectedRegion = lexemeList.getAffectedRegion();
		int startingOffset = affectedRegion.getStartingOffset();
		int endingOffset = affectedRegion.getEndingOffset();
		int floorLexemeIndex = lexemeList.getLexemeFloorIndex(startingOffset);
		if (floorLexemeIndex == -1) {
			floorLexemeIndex = 0;
		}
		int endingLexemeIndex = lexemeList.getLexemeCeilingIndex(endingOffset);
		if (endingLexemeIndex == -1) {
			endingLexemeIndex = lexemeList.size() - 1;
		}
		posStart = floorLexemeIndex;
		posEnd = floorLexemeIndex;
		for (int a = floorLexemeIndex; a >= 0; a--) {
			Lexeme lexeme = lexemeList.get(a);
			if (!lexeme.getLanguage().equals(PHPMimeType.MimeType)) {
				posStart = lexeme.getStartingOffset();
			} else {
				break;
			}
		}
		for (int a = endingLexemeIndex; a < lexemeList.size(); a++) {
			Lexeme lexeme = lexemeList.get(a);
			if (!lexeme.getLanguage().equals(PHPMimeType.MimeType)) {
				posEnd = lexeme.getEndingOffset();
			} else {
				break;
			}
		}
		super.parseAll(parentNode);
	}

	/**
	 * @see com.aptana.ide.parsing.AbstractParser#getNextLexemeInLanguage()
	 */
	protected Lexeme getNextLexemeInLanguage() throws LexerException {

		Lexeme result = internalNextLexemeInLanguage();

		// always need to look for language change
		result = parsePossibleComment(result);

		return result;
	}

	int lastPosition;
	private char[] sourceUnsafe;

	protected Lexeme internalNextLexemeInLanguage() throws LexerException {

		Lexeme result = null;

		while (result == null && lexer.isEOS() == false) {

			result = getCachedLexeme();
			if (result == null) {
				result = lexer.getNextLexeme();
			}

			// if this is a stale lexeme (from a different language) back up,
			// damage the whole partition, and re-parse.
			if (result != null && result != EOS
					&& !result.getLanguage().equals(this.getLanguage())) {
				result = processLexemeFromOtherLanguage(lexer, result);
			}

			if (result == null && lexer.isEOS() == false) {
				// if we're already in the error group, then abort
				if ("error".equals(lexer.getGroup())) //$NON-NLS-1$
				{
					break;
				}

				// Switch to error group.
				lexer.setGroup("error"); //$NON-NLS-1$

				// get error lexeme
				result = lexer.getNextLexeme();

				// if we failed to get a new lexeme and we're still in the error
				// state,
				// then we need to abort to prevent an infinite loop
				if (result == null && "error".equals(lexer.getGroup())) //$NON-NLS-1$
				{
					break;
				}
			}
		}

		return result;
	}

	protected Lexeme processLexemeFromOtherLanguage(ILexer lexer, Lexeme result) {
		if (result.getEndingOffset() < posStart
				|| result.getStartingOffset() > posEnd
				&& result.getLanguage().equals(PHPMimeType.MimeType)) {
			if (result.getLanguage().equals(PHPDocMimeType.MimeType)) {
				return result;
			}
		}
		LexemeList lexemes = this.getLexemeList();
		lexemes.getAffectedRegion().includeInRange(result);
		this.removeLexeme(result);
		lexer.setCurrentOffset(result.offset);
		result = lexer.getNextLexeme();
		return result;
	}

	protected void parseStructure(final ILexer lexer, IParseNode parentNode) {

		final MatcherLexer mlexer = (MatcherLexer) lexer;
		char[] sourceUnsafe = mlexer.getSourceUnsafe();
		final Object service = getOwner();
		lastChange = System.currentTimeMillis();
		final NodeBuilderClient parserClient = getParseClient(service);
		if (parserClient != null && service != null && !isSheduled) {
			synchronized (this) {
				parserClient.populateNodes(parentNode, mlexer
						.getCurrentOffset(), sourceUnsafe);
				if (returnImmediatly || sourceUnsafe == lastSource) {
					return;
				}
			}
		}
		if (lastSource == sourceUnsafe) {
			parserClient.populateNodes(parentNode, mlexer.getCurrentOffset(),
					sourceUnsafe);
			return;
		}
		lastSource = sourceUnsafe;
		{

			synchronized (this) {
				if (!isSheduled && service != null) {
					Thread updater = new UpdaterThread(mlexer, lexer, service);
					updater.setPriority((Thread.NORM_PRIORITY
							+ Thread.MIN_PRIORITY - 1) / 2);
					updater.setName("PHP Background AST Updater");
					updater.setDaemon(true);
					isSheduled = true;
					updater.start();
					// System.out.println("Starting thread");

					return;
				}
			}
			if (isSheduled && parserClient != null) {
				parserClient.populateNodes(parentNode, mlexer
						.getCurrentOffset(), sourceUnsafe);
				return;
			}
			NodeBuilderClient doParse = doParse(sourceUnsafe, service);
			if (doParse != null) {
				setParserClient(service, doParse);
				// System.out.println("Direct parsing");
				doParse.populateNodes(parentNode, mlexer.getCurrentOffset(),
						sourceUnsafe);
			}
		}
	}

	private void setParserClient(Object service, NodeBuilderClient doParse) {
		parserClients.put(service, doParse);
	}

	private NodeBuilderClient getParseClient(Object service) {
		NodeBuilderClient nodeBuilderClient = parserClients.get(service);
		if (nodeBuilderClient == null) {
			nodeBuilderClient = new NodeBuilderClient();
			parserClients.put(service, nodeBuilderClient);
		}
		return nodeBuilderClient;
	}

	public PHPBackgroundParser() throws ParserInitializationException {
		super();
	}

	private TreeViewer getOutline() {
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (activeWorkbenchWindow != null) {
			IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
			PageBookView findView = (PageBookView) activePage
					.findView("org.eclipse.ui.views.ContentOutline");
			if (findView != null) {
				UnifiedOutlinePage page = (UnifiedOutlinePage) findView
						.getCurrentPage();
				TreeViewer treeViewer = page.getTreeViewer();

				return treeViewer;
			}
		}
		return null;
	}

	private IModule getModule(Object service) {

		if (service == null || !(service instanceof IUnifiedEditor)) {
			return null;
		}

		IModule _module = null;
		if (((IUnifiedEditor) service).getFileContext().getSourceProvider() == null) {
			return null;
		}

		String struri = ((IUnifiedEditor) service).getFileContext()
				.getSourceProvider().getSourceURI();
		URI uri;
		try {
			uri = new URI(struri);
		} catch (URISyntaxException e) {
			IdeLog.logError(PHPPlugin.getDefault(), "Unexpected exception", e); //$NON-NLS-1$
			return null;
		}
		if (!uri.isAbsolute()) {
			return null;
		}
		IFile[] files = ResourcesPlugin.getWorkspace().getRoot()
				.findFilesForLocationURI(uri);
		if (files == null || files.length == 0) {
			return createLocalFileModule(uri);
		}

		if (_module == null) {
			try {
				if (files[0].getProject().getNature(PHPNature.NATURE_ID) == null) {
					// we are outside of PHP project (probably web project or
					// other)
					return createLocalFileModule(uri);
				}
			} catch (CoreException e) {
				// ignore
			}
		}

		_module = BuildPathManager.getInstance().getModuleByResource(files[0]);

		return _module;
	}

	private IModule createLocalFileModule(URI uri) {
		File file = new File(uri.getPath());
		FileSystemModule fileSystemModule = new FileSystemModule(file,
				new SingleFileBuildPath(file));
		return fileSystemModule;
	}

}