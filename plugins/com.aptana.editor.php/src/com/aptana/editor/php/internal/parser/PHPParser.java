package com.aptana.editor.php.internal.parser;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org2.eclipse.php.internal.core.PHPVersion;
import org2.eclipse.php.internal.core.ast.nodes.AST;
import org2.eclipse.php.internal.core.ast.nodes.ASTParser;
import org2.eclipse.php.internal.core.ast.nodes.Comment;
import org2.eclipse.php.internal.core.ast.nodes.Program;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.IOUtil;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.core.PHPVersionProvider;
import com.aptana.editor.php.core.model.ISourceModule;
import com.aptana.editor.php.epl.PHPEplPlugin;
import com.aptana.editor.php.indexer.PHPGlobalIndexer;
import com.aptana.editor.php.internal.core.IPHPConstants;
import com.aptana.editor.php.internal.core.builder.IModule;
import com.aptana.editor.php.internal.model.utils.ModelUtils;
import com.aptana.editor.php.internal.parser.nodes.NodeBuilder;
import com.aptana.editor.php.internal.parser.nodes.NodeBuildingVisitor;
import com.aptana.editor.php.internal.parser.nodes.PHPBlockNode;
import com.aptana.editor.php.internal.parser.nodes.PHPCommentNode;
import com.aptana.editor.php.internal.typebinding.TypeBindingBuilder;
import com.aptana.parsing.IParseState;
import com.aptana.parsing.IParser;
import com.aptana.parsing.ast.IParseNode;
import com.aptana.parsing.ast.IParseRootNode;
import com.aptana.parsing.ast.ParseNode;
import com.aptana.parsing.ast.ParseRootNode;

/**
 * PHP parser.<br>
 * This parser will only deal with PHP elements. Any other HTML elements are ignored (for now).
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 * @since Aptana PHP 3.0
 */
public class PHPParser implements IParser
{
	protected static final ParseNode[] NO_CHILDREN = new ParseNode[0];
	private PHPVersion phpVersion;
	private IModule module;
	private ISourceModule sourceModule;
	private boolean parseHTML;
	private IParseRootNode latestValidNode;

	/**
	 * Constructs a new PHPParser.<br>
	 * By default, the node building that is done by the parser will involve HTML parsing as well.
	 * 
	 * @see #PHPParser(PHPVersion, boolean)
	 */
	public PHPParser()
	{
		this(null);
	}

	/**
	 * Constructs a new PHPParser with a preset PHPVersion.<br>
	 * By default, the node building that is done by the parser will involve HTML parsing as well.
	 * 
	 * @param phpVersion
	 * @see #PHPParser(PHPVersion, boolean)
	 */
	public PHPParser(PHPVersion phpVersion)
	{
		this(phpVersion, true);
	}

	/**
	 * Constructs a new PHPParser with a preset PHPVersion
	 * 
	 * @param phpVersion
	 * @param parseHTML
	 *            - indicate whether the node building that is done by the parser should involve HTML parsing as well.
	 */
	public PHPParser(PHPVersion phpVersion, boolean parseHTML)
	{
		this.phpVersion = phpVersion;
		this.parseHTML = parseHTML;
	}

	/**
	 * Override the default implementation to provide support for PHP nodes inside JavaScript.
	 */
	public IParseRootNode parse(IParseState parseState) // $codepro.audit.disable declaredExceptions
	{
		String source = parseState.getSource();
		int startingOffset = parseState.getStartingOffset();
		ParseRootNode root = new ParseRootNode(IPHPConstants.CONTENT_TYPE_PHP, NO_CHILDREN, startingOffset,
				startingOffset + source.length() - 1);
		Program program = null;
		if (parseState instanceof IPHPParseState)
		{
			IPHPParseState phpParseState = (IPHPParseState) parseState;
			phpVersion = phpParseState.getPHPVersion();

			IModule newModule = phpParseState.getModule();
			if (module != newModule)
			{
				module = newModule;
				sourceModule = phpParseState.getSourceModule();
				latestValidNode = null;
			}
			aboutToBeReconciled();
		}
		ASTParser parser = null;
		try
		{
			PHPVersion version = (phpVersion == null) ? PHPVersionProvider.getDefaultPHPVersion() : phpVersion;
			parser = ASTParser.newParser(new StringReader(source), version, true, sourceModule); // $codepro.audit.disable
																									// closeWhereCreated
			program = parser.createAST(null);
		}
		catch (Exception e)
		{
			// TODO: handle exception
			IdeLog.logError(PHPEditorPlugin.getDefault(), "PHP parser error", e); //$NON-NLS-1$
		}
		if (program != null)
		{
			processChildren(program, root, source);
		}
		// we maintain this value since it's being reset when the errors are flushed.
		boolean astHasErrors = false;
		if (program != null)
		{
			parseState.setParseResult(root);
			try
			{
				program.setSourceModule(ModelUtils.convertModule(module));
				// TODO: Shalom - check for Program errors?
				// if (!ast.hasSyntaxErrors() && module != null) {
				AST ast = program.getAST();
				astHasErrors = ast.hasErrors();
				if (astHasErrors)
				{
					parseState.setParseResult(null);
				}
				if (module != null)
				{
					PHPGlobalIndexer.getInstance().processUnsavedModuleUpdate(program, module);
				}
				// Recalculate the type bindings
				TypeBindingBuilder.buildBindings(program, source);
				ast.flushErrors();
			}
			catch (Throwable t)
			{
				IdeLog.logError(PHPEditorPlugin.getDefault(), "PHP parser error", t); //$NON-NLS-1$
			}
			reconciled(program, false, new NullProgressMonitor());
		}
		else
		{
			if (parser != null)
			{
				AST ast = parser.getAST();
				if (ast != null)
				{
					astHasErrors = ast.hasErrors();
					ast.flushErrors();
				}
			}
			reconciled(null, false, new NullProgressMonitor());
		}
		if (astHasErrors)
		{
			return latestValidNode;
		}
		latestValidNode = root;
		return latestValidNode;
	}

	/**
	 * Parse the PHP content, given as an input stream, and return a parse node that contains the children nodes that
	 * were parsed. Note that this method does not use the parse state and does not update anything.
	 * 
	 * @param source
	 * @return
	 * @throws java.lang.Exception
	 * @see {@link #parse(IParseState)}
	 */
	public IParseRootNode parse(InputStream source)
	{
		String input = IOUtil.read(source);
		Program ast = parseAST(new StringReader(input)); // $codepro.audit.disable closeWhereCreated
		if (ast != null)
		{

			ParseRootNode root = new ParseRootNode(IPHPConstants.CONTENT_TYPE_PHP, NO_CHILDREN, ast.getStart(),
					ast.getEnd());
			// We have to pass in the source itself to support accurate PHPDoc display.
			processChildren(ast, root, input);
			return root;
		}
		return new ParseRootNode(IPHPConstants.CONTENT_TYPE_PHP, NO_CHILDREN, 0, 0);
	}

	/**
	 * Parse an return a {@link Program} (AST) for the given PHP source.<br>
	 * This method is only parsing and does not update any state.
	 * 
	 * @param reader
	 * @return A {@link Program} AST; Null, in case an error occurred.
	 */
	public Program parseAST(Reader reader)
	{
		Program ast = null;
		try
		{
			PHPVersion version = (phpVersion == null) ? PHPVersionProvider.getDefaultPHPVersion() : phpVersion;
			// TODO - Perhaps we'll need to pass a preference value for the 'short-tags' instead of passing 'true' by
			// default.
			ASTParser parser = ASTParser.newParser(reader, version, true);
			ast = parser.createAST(null);
		}
		catch (Exception e)
		{
			IdeLog.logError(PHPEditorPlugin.getDefault(), "PHP parser error", e); //$NON-NLS-1$
		}
		return ast;
	}

	/**
	 * Notify the shared AST provider that the module is about to be reconciled.
	 */
	private void aboutToBeReconciled()
	{
		// Notify the shared AST provider
		PHPEplPlugin.getDefault().getASTProvider().aboutToBeReconciled(sourceModule);
	}

	/**
	 * Notify the shared AST provider that the module was reconciled.
	 */
	private void reconciled(Program program, boolean forced, IProgressMonitor progressMonitor)
	{
		PHPEplPlugin.getDefault().getASTProvider().reconciled(program, sourceModule, progressMonitor);
	}

	/*
	 * Process the AST and update the given IParseNode
	 */
	private void processChildren(Program ast, ParseRootNode root, String source)
	{
		/*
		 * kept here for Debug purposes ApplyAll astPrinter = new ApplyAll() {
		 * @Override public boolean apply(ASTNode node) { System.out.println(node.toString()); return true; } };
		 * ast.accept(astPrinter);
		 */
		List<IParseNode> commentNodes = new ArrayList<IParseNode>();
		for (Comment c : ast.comments())
		{
			commentNodes.add(new PHPCommentNode(c));
		}
		root.setCommentNodes(commentNodes.toArray(new IParseNode[commentNodes.size()]));
		NodeBuilder builderClient = new NodeBuilder(source, false, parseHTML);
		ast.accept(new NodeBuildingVisitor(builderClient, source));
		PHPBlockNode nodes = builderClient.populateNodes();
		for (IParseNode child : nodes.getChildren())
		{
			root.addChild(child);
		}
	}
}
