package com.aptana.editor.php.internal.parser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.php.internal.core.PHPVersion;
import org.eclipse.php.internal.core.ast.nodes.ASTParser;
import org.eclipse.php.internal.core.ast.nodes.Program;

import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.core.PHPVersionProvider;
import com.aptana.editor.php.core.model.ISourceModule;
import com.aptana.editor.php.epl.PHPEplPlugin;
import com.aptana.editor.php.indexer.PHPGlobalIndexer;
import com.aptana.editor.php.internal.core.builder.IModule;
import com.aptana.editor.php.internal.model.utils.ModelUtils;
import com.aptana.editor.php.internal.parser.nodes.NodeBuilder;
import com.aptana.editor.php.internal.parser.nodes.NodeBuildingVisitor;
import com.aptana.editor.php.internal.parser.nodes.PHPBlockNode;
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

	private PHPVersion phpVersion;
	private IModule module;
	private ISourceModule sourceModule;

	/**
	 * Constructs a new PHPParser
	 */
	public PHPParser()
	{
	}

	/**
	 * Constructs a new PHPParser with a preset PHPVersion
	 * 
	 * @param phpVersion
	 */
	public PHPParser(PHPVersion phpVersion)
	{
		this.phpVersion = phpVersion;
	}

	/**
	 * Override the default implementation to provide support for PHP nodes inside JavaScript.
	 */
	public IParseRootNode parse(IParseState parseState) throws java.lang.Exception
	{
		String source = new String(parseState.getSource());
		int startingOffset = parseState.getStartingOffset();
		IParseRootNode root = new ParseRootNode(PHPMimeType.MimeType, new ParseNode[0], startingOffset, startingOffset
				+ source.length());
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
			}
			aboutToBeReconciled();
		}
		try
		{
			PHPVersion version = (phpVersion == null) ? PHPVersionProvider.getDefaultPHPVersion() : phpVersion;
			ASTParser parser = ASTParser.newParser(new StringReader(source), version, true, sourceModule);
			program = parser.createAST(null);
		}
		catch (Exception e)
		{
			// TODO: handle exception
			PHPEditorPlugin.logError(e);
		}
		if (program != null)
		{
			processChildren(program, root, source);
		}
		parseState.setParseResult(root);
		if (program != null)
		{
			try
			{
				program.setSourceModule(ModelUtils.convertModule(module));
				// TODO: Shalom - check for Program errors?
				// if (!ast.hasSyntaxErrors() && module != null) {
				program.getAST().flushErrors();
				if (module != null)
				{
					PHPGlobalIndexer.getInstance().processUnsavedModuleUpdate(program, module);
				}
				// Recalculate the type bindings
				TypeBindingBuilder.buildBindings(program);
			}
			catch (Throwable t)
			{
				PHPEditorPlugin.logError(t);
			}
			reconciled(program, false, new NullProgressMonitor());
		} else {
			reconciled(null, false, new NullProgressMonitor());
		}
		return root;
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
	public IParseNode parse(InputStream source) throws java.lang.Exception
	{
		Program ast = null;
		try
		{
			PHPVersion version = (phpVersion == null) ? PHPVersionProvider.getDefaultPHPVersion() : phpVersion;
			// TODO: Shalom - Have this parser in a PHP parsers pool?

			ASTParser parser = ASTParser.newParser(new InputStreamReader(source), version);
			ast = parser.createAST(null);
		}
		catch (Exception e)
		{
			// TODO: handle exception
			PHPEditorPlugin.logError(e);
		}
		if (ast != null)
		{
			IParseNode root = new ParseRootNode(PHPMimeType.MimeType, new ParseNode[0], ast.getStart(), ast
					.getEnd());
			processChildren(ast, root, null);
			return root;
		}
		return new ParseRootNode(PHPMimeType.MimeType, new ParseNode[0], 0, 0);
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
	private void processChildren(Program ast, IParseNode root, String source)
	{
		/*
		 * kept here for Debug purposes ApplyAll astPrinter = new ApplyAll() {
		 * @Override public boolean apply(ASTNode node) { System.out.println(node.toString()); return true; } };
		 * ast.accept(astPrinter);
		 */
		NodeBuilder builderClient = new NodeBuilder();
		ast.accept(new NodeBuildingVisitor(builderClient, source));
		PHPBlockNode nodes = builderClient.populateNodes();
		for (IParseNode child : nodes.getChildren())
		{
			root.addChild(child);
		}
	}
}
