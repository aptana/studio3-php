package com.aptana.editor.php.internal.ui.editor.scanner.tokenMap;

import java_cup.runtime.Symbol;

import org.eclipse.jface.text.rules.IToken;
import org2.eclipse.php.internal.core.ast.scanner.php4.ParserConstants;

import com.aptana.editor.php.internal.indexer.language.PHPBuiltins;
import com.aptana.editor.php.internal.ui.editor.scanner.PHPCodeScanner;

public class PHP4TokenMapper implements IPHPTokenMapper, ParserConstants
{

	public IToken mapToken(Symbol sym, PHPCodeScanner scanner)
	{
		switch (sym.sym)
		{
			case T_ECHO:
			case T_EVAL:
				return scanner.getToken("support.function.construct.php"); //$NON-NLS-1$
			case T_DEFINE:
				return scanner.getToken("support.function.builtin_functions.php"); //$NON-NLS-1$
			case T_USE:
			case T_DECLARE:
			case T_ENDDECLARE:
			case T_ARRAY:
			case T_EMPTY:
			case T_LIST:
			case T_NEW:
			case T_PRINT:
			case T_UNSET:
			case T_ISSET:
				// TODO - Shalom: Maybe move these to their own style
			case T_START_HEREDOC:
			case T_END_HEREDOC:
				// case T_NAMESPACE:
				return scanner.getToken("keyword.php"); //$NON-NLS-1$
			case T_WHILE:
			case T_ENDWHILE:
			case T_DO:
			case T_RETURN:
			case T_EXIT:
			case T_CONTINUE:
			case T_BREAK:
			case T_FOR:
			case T_ENDFOR:
			case T_FOREACH:
			case T_ENDFOREACH:
			case T_SWITCH:
			case T_ENDSWITCH:
			case T_CASE:
			case T_DEFAULT:
			case T_IF:
			case T_ELSE:
			case T_ELSEIF:
			case T_ENDIF:
				// TODO - Shalom: Missing DIE, TRUE, FALSE
				return scanner.getToken("keyword.control.php"); //$NON-NLS-1$
			case T_STATIC:
				return scanner.getToken("storage.modifier.static.php"); //$NON-NLS-1$
			case T_FUNCTION:
				return scanner.getToken("storage.type.function.php"); //$NON-NLS-1$
			case T_CLASS:
				return scanner.getToken("storage.type.class.php"); //$NON-NLS-1$
			case T_VAR:
			case T_GLOBAL:
				return scanner.getToken("storage.type.php"); //$NON-NLS-1$
			case T_EXTENDS:
				return scanner.getToken("keyword.other.class.php"); //$NON-NLS-1$
			case T_INCLUDE:
			case T_INCLUDE_ONCE:
			case T_REQUIRE:
			case T_REQUIRE_ONCE:
				return scanner.getToken("keyword.control.import.php"); //$NON-NLS-1$
			case T_OBJECT_OPERATOR:
				return scanner.getToken("keyword.operator.class.php"); //$NON-NLS-1$
			case T_PAAMAYIM_NEKUDOTAYIM:
				return scanner.getToken("meta.function-call.static.php"); //$NON-NLS-1$
			case T_AT:
			case T_AS:
			case T_LOGICAL_AND:
			case T_LOGICAL_OR:
			case T_LOGICAL_XOR:
				return scanner.getToken("keyword.operator.logical.php"); //$NON-NLS-1$
			case T_LINE:
			case T_FILE:
			case T_FUNC_C:
			case T_CLASS_C:
				return scanner.getToken("constant.language.php"); //$NON-NLS-1$
			case T_VARIABLE:
				if (THIS.equals(scanner.getSymbolValue(sym)))
				{
					return scanner.getToken("variable.language.php"); //$NON-NLS-1$ 
				}
				return scanner.getToken("variable.other.php"); //$NON-NLS-1$
			case T_CONST:
				return scanner.getToken("constant.php"); //$NON-NLS-1$
			case T_LNUMBER:
			case T_DNUMBER:
				return scanner.getToken("constant.numeric.php"); //$NON-NLS-1$
			case T_CONSTANT_ENCAPSED_STRING:
				return scanner.getToken("string.quoted.php"); //$NON-NLS-1$
			case T_STRING:
				String tokenContent = scanner.getSymbolValue(sym);
				if (SELF.equals(tokenContent) || PARENT.equals(tokenContent))
				{
					return scanner.getToken("variable.language.php"); //$NON-NLS-1$
				}
				if (TRUE.equalsIgnoreCase(tokenContent) || FALSE.equalsIgnoreCase(tokenContent)
						|| NULL.equalsIgnoreCase(tokenContent) || ON.equalsIgnoreCase(tokenContent)
						|| OFF.equalsIgnoreCase(tokenContent) || YES.equalsIgnoreCase(tokenContent)
						|| NO.equalsIgnoreCase(tokenContent) || NL.equalsIgnoreCase(tokenContent)
						|| BR.equalsIgnoreCase(tokenContent) || TAB.equalsIgnoreCase(tokenContent))
				{
					return scanner.getToken("constant.language.other.php"); //$NON-NLS-1$
				}
				PHPBuiltins builtins = PHPBuiltins.getInstance();
				if (builtins != null)
				{
					if (builtins.isBuiltinFunction(tokenContent))
					{
						return scanner.getToken("support.function.php"); //$NON-NLS-1$
					}
					else if (builtins.isBuiltinClass(tokenContent))
					{
						return scanner.getToken("support.class.php"); //$NON-NLS-1$
					}
					else if (builtins.isBuiltinConstant(tokenContent))
					{
						return scanner.getToken("support.constant.php"); //$NON-NLS-1$
					}
				}
			default: // $codepro.audit.disable nonTerminatedCaseClause
				return PHPTokenMapperFactory.mapDefaultToken(scanner, sym);
		}
	}
}