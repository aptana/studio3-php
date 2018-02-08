package org2.eclipse.php.internal.core.ast.scanner;

import java.text.MessageFormat;
import java.util.List;

import java_cup.runtime.Scanner;
import java_cup.runtime.Symbol;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Platform;
import org2.eclipse.dltk.compiler.problem.DefaultProblem;
import org2.eclipse.dltk.compiler.problem.IProblem;
import org2.eclipse.dltk.compiler.problem.IProblemReporter;
import org2.eclipse.dltk.compiler.problem.ProblemSeverities;
import org2.eclipse.php.internal.core.ast.nodes.AST;
import org2.eclipse.php.internal.core.ast.nodes.ASTError;

import com.aptana.core.resources.IUniformResource;
import com.aptana.editor.common.CommonEditorPlugin;
import com.aptana.editor.common.preferences.IPreferenceConstants;
import com.aptana.editor.php.internal.core.IPHPConstants;

/**
 * Base class for every PhpAstParser that also handles error reporting.<br>
 * Note: his class is an Aptana Mod. The PDT's PhpAstParser extends directly java_cup.runtime.lr_parser.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public abstract class AbstractASTParser extends java_cup.runtime.lr_parser
{
	protected IProblemReporter problemReporter;
	protected Scanner scanner;
	public AST ast;

	public AbstractASTParser(Scanner scanner)
	{
		this.scanner = scanner;
	}

	public AbstractASTParser()
	{
	}

	public final void setAST(AST ast)
	{
		this.ast = ast;
	}

	public void setProblemReporter(IProblemReporter problemReporter)
	{
		this.problemReporter = problemReporter;
	}

	public IProblemReporter getProblemReporter()
	{
		return problemReporter;
	}

	protected List<ASTError> getErrors()
	{
		return ast.getErrors();
	}

	/**
	 * Report an error (or warning).<br>
	 * We also catch any un-recovered errors in this location.
	 * 
	 * @param message
	 *            an error message.
	 * @param info
	 *            an extra object reserved for use by specialized subclasses.
	 */
	public final void report_error(String message, Object info)
	{
		if (!Platform.getPreferencesService().getBoolean(
				CommonEditorPlugin.PLUGIN_ID,
				MessageFormat.format("{0}:{1}", IPHPConstants.CONTENT_TYPE_HTML_PHP, //$NON-NLS-1$
						IPreferenceConstants.PARSE_ERROR_ENABLED), true, null))
		{
			return;
		}
		if (info instanceof Symbol)
		{
			Symbol s = (Symbol) info;
			if (s.left >= 0 && s.right >= s.left)
			{
				reportError(new ASTError(s.left, s.right, ast), message);
			}
		}
	}

	/**
	 * Reports a fatal error.
	 * 
	 * @param message
	 * @param info
	 *            A {@link Symbol}
	 * @see #report_error(String, Object)
	 */
	public final void report_fatal_error(String message, Object info) throws java.lang.Exception
	{
		/* stop parsing (not really necessary since we throw an exception, but) */
		done_parsing();

		// We don't report it, as we already get another event at report_error for this one.
		// report_error(message, info);
	}

	/**
	 * Reports an error to a given {@link IProblemReporter}
	 * 
	 * @param problemReporter
	 * @param resource
	 * @param start
	 * @param end
	 * @param lineNumber
	 * @param message
	 */
	protected void reportError(IProblemReporter problemReporter, Object resource, int start, int end, int lineNumber,
			String message)
	{
		String location = null;
		if (resource instanceof IResource)
		{
			location = ((IResource) resource).getLocation().toString();
		}
		else if (resource instanceof IUniformResource)
		{
			location = ((IUniformResource) resource).getURI().toString();
		}
		DefaultProblem problem = new DefaultProblem(location, message, IProblem.Syntax, new String[0],
				ProblemSeverities.Error, start, end, lineNumber);
		problemReporter.reportProblem(problem);
	}

	/**
	 * Reporting an error that cannot be added as a statement and has to be in a separated list.
	 * 
	 * @param error
	 */
	public void reportError(ASTError error, String message)
	{
		getErrors().add(error);
		if (message != null && problemReporter != null && ast.getResource() != null)
		{
			int lineNumber = ((AstLexer) getScanner()).getCurrentLine();
			reportError(problemReporter, ast.getResource(), error.getStart(), error.getEnd(), lineNumber, message);
		}
	}

}
