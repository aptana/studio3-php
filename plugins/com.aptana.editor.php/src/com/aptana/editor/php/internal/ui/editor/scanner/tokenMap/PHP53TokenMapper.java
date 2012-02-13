package com.aptana.editor.php.internal.ui.editor.scanner.tokenMap;

import java_cup.runtime.Symbol;

import org.eclipse.jface.text.rules.IToken;
import org2.eclipse.php.internal.core.ast.scanner.php53.ParserConstants;

import com.aptana.editor.php.internal.indexer.language.PHPBuiltins;
import com.aptana.editor.php.internal.parser.PHPTokenType;
import com.aptana.editor.php.internal.ui.editor.scanner.PHPCodeScanner;

public class PHP53TokenMapper implements IPHPTokenMapper, ParserConstants
{

	public IToken mapToken(Symbol sym, PHPCodeScanner scanner)
	{
		switch (sym.sym)
		{
			case T_ECHO:
			case T_EVAL:
				return scanner.getToken(PHPTokenType.SUPPORT_FUNCTION_CONSTRUCT);
			case T_DEFINE:
				return scanner.getToken(PHPTokenType.SUPPORT_FUNCTION_BUILTIN);
			case T_USE:
			case T_CLONE:
			case T_DECLARE:
			case T_ENDDECLARE:
			case T_ARRAY:
			case T_THROW:
			case T_TRY:
			case T_CATCH:
			case T_EMPTY:
			case T_HALT_COMPILER:
			case T_LIST:
			case T_NEW:
			case T_PRINT:
			case T_UNSET:
			case T_ISSET:
				// TODO - Shalom: Maybe move these to their own style
			case T_START_HEREDOC:
			case T_END_HEREDOC:
				return scanner.getToken(PHPTokenType.KEYWORD);
			case T_NAMESPACE:
				return scanner.getToken(PHPTokenType.KEYWORD_NAMESPACE);
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
				return scanner.getToken(PHPTokenType.KEYWORD_CONTROL);
			case T_FINAL:
				return scanner.getToken(PHPTokenType.STORAGE_MODIFIER_FINAL);
			case T_STATIC:
				return scanner.getToken(PHPTokenType.STORAGE_MODIFIER_STATIC);
			case T_PRIVATE:
				return scanner.getToken(PHPTokenType.STORAGE_MODIFIER_PRIVATE);
			case T_PUBLIC:
				return scanner.getToken(PHPTokenType.STORAGE_MODIFIER_PUBLIC);
			case T_PROTECTED:
				return scanner.getToken(PHPTokenType.STORAGE_MODIFIER_PROTECTED);
			case T_ABSTRACT:
				return scanner.getToken(PHPTokenType.STORAGE_MODIFIER_ABSTRACT);
			case T_FUNCTION:
				return scanner.getToken(PHPTokenType.STORAGE_TYPE_FUNCTION);
			case T_CLASS:
				return scanner.getToken(PHPTokenType.STORAGE_TYPE_CLASS);
			case T_VAR:
			case T_GLOBAL:
			case T_INTERFACE:
				return scanner.getToken(PHPTokenType.STORAGE_TYPE);
			case T_INSTANCEOF:
			case T_EXTENDS:
			case T_IMPLEMENTS:
				return scanner.getToken(PHPTokenType.KEYWORD_OTHER_CLASS);
			case T_INCLUDE:
			case T_INCLUDE_ONCE:
			case T_REQUIRE:
			case T_REQUIRE_ONCE:
				return scanner.getToken(PHPTokenType.KEYWORD_CONTROL_IMPORT);
			case T_OBJECT_OPERATOR:
				return scanner.getToken(PHPTokenType.KEYWORD_OP_CLASS);
			case T_PAAMAYIM_NEKUDOTAYIM:
				return scanner.getToken(PHPTokenType.META_FUNCTION_CALL_STATIC);
			case T_AT:
			case T_AS:
			case T_LOGICAL_AND:
			case T_LOGICAL_OR:
			case T_LOGICAL_XOR:
				return scanner.getToken(PHPTokenType.KEYWORD_OP_LOGICAL);
			case T_VARIABLE:
				String value = scanner.getSymbolValue(sym);
				if (THIS.equals(value))
				{
					return scanner.getToken(PHPTokenType.VARIABLE_LANGUAGE);
				}
				if (PHPTokenMapperFactory.GLOBALS.contains(value))
				{
					return scanner.getToken(PHPTokenType.VARIABLE_OTHER_GLOBAL);
				}
				if (PHPTokenMapperFactory.SAFER_GLOBALS.contains(value))
				{
					return scanner.getToken(PHPTokenType.VARIABLE_OTHER_GLOBAL_SAFER);
				}
				return scanner.getToken(PHPTokenType.VARIABLE_OTHER);
			case T_LINE:
			case T_FILE:
			case T_METHOD_C:
			case T_FUNC_C:
			case T_CLASS_C:
				return scanner.getToken(PHPTokenType.CONSTANT_LANGUAGE);
			case T_CONST:
				return scanner.getToken(PHPTokenType.CONSTANT);
			case T_LNUMBER:
			case T_DNUMBER:
				return scanner.getToken(PHPTokenType.CONSTANT_NUMERIC);
			case T_CONSTANT_ENCAPSED_STRING:
				return scanner.getToken(PHPTokenType.STRING_QUOTED);
			case T_STRING:
				String tokenContent = scanner.getSymbolValue(sym);
				if (SELF.equals(tokenContent) || PARENT.equals(tokenContent))
				{
					return scanner.getToken(PHPTokenType.VARIABLE_LANGUAGE);
				}
				if (TRUE.equalsIgnoreCase(tokenContent) || FALSE.equalsIgnoreCase(tokenContent)
						|| NULL.equalsIgnoreCase(tokenContent) || ON.equalsIgnoreCase(tokenContent)
						|| OFF.equalsIgnoreCase(tokenContent) || YES.equalsIgnoreCase(tokenContent)
						|| NO.equalsIgnoreCase(tokenContent) || NL.equalsIgnoreCase(tokenContent)
						|| BR.equalsIgnoreCase(tokenContent) || TAB.equalsIgnoreCase(tokenContent))
				{
					return scanner.getToken(PHPTokenType.CONSTANT_LANGUAGE_OTHER);
				}
				PHPBuiltins builtins = PHPBuiltins.getInstance();
				if (builtins != null)
				{
					if (builtins.isBuiltinFunction(tokenContent))
					{
						return scanner.getToken(PHPTokenType.SUPPORT_FUNCTION);
					}
					else if (builtins.isBuiltinClass(tokenContent))
					{
						return scanner.getToken(PHPTokenType.SUPPORT_CLASS);
					}
					else if (builtins.isBuiltinConstant(tokenContent))
					{
						return scanner.getToken(PHPTokenType.SUPPORT_CONSTANT);
					}
				}
			default: // $codepro.audit.disable nonTerminatedCaseClause
				return PHPTokenMapperFactory.mapDefaultToken(scanner, sym);
		}
	}
}