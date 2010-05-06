package com.aptana.editor.php.internal.editor.scanner.tokenMap;

import org.eclipse.jface.text.rules.IToken;
import org.eclipse.php.internal.core.ast.parser.ParserConstants5;

import com.aptana.editor.php.internal.editor.scanner.PHPCodeScanner;

public class PHP5TokenMapper implements IPHPTokenMapper, ParserConstants5
{

	@Override
	public IToken mapToken(int sym, PHPCodeScanner scanner)
	{
		switch (sym)
		{
			case T_ABSTRACT:
			case T_IMPLEMENTS:
			case T_INTERFACE:
			case T_FINAL:
			case T_VAR:
			case T_USE:
			case T_CLONE:
			case T_STATIC:
			case T_GLOBAL:
			case T_INSTANCEOF:
			case T_DECLARE:
			case T_ENDDECLARE:
			case T_ARRAY:
			case T_THROW:
			case T_TRY:
			case T_CATCH:
			case T_ECHO:
			case T_EMPTY:
			case T_EVAL:
			case T_EXTENDS:
			case T_HALT_COMPILER:
			case T_LIST:
			case T_NEW:
			case T_PRINT:
			case T_PRIVATE:
			case T_PUBLIC:
			case T_PROTECTED:
			case T_UNSET:
			case T_ISSET:
			// TODO - Shalom: Maybe move these to their own style
			case T_INCLUDE:
			case T_INCLUDE_ONCE:
			case T_REQUIRE:
			case T_REQUIRE_ONCE:
			case T_FUNCTION:
			case T_FUNC_C:
			case T_CLASS:
			case T_CLASS_C:
			case T_START_HEREDOC:
			case T_END_HEREDOC:
			//case T_NAMESPACE:
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
			case T_AT:
			case T_AS:
			case T_LOGICAL_AND:
			case T_LOGICAL_OR:
			case T_LOGICAL_XOR:
				return scanner.getToken("keyword.operator.php"); //$NON-NLS-1$
			case T_VARIABLE:
				return scanner.getToken("variable.other.php"); //$NON-NLS-1$
			case T_CONST:
				return scanner.getToken("constant.php"); //$NON-NLS-1$
			default:
				return scanner.getToken("default.php"); //$NON-NLS-1$
		}
	}

	/*
	 * if (lookForBlock) { if (!inPipe && data.intValue() != Tokens.tPIPE) lookForBlock = false; } if (nextAreArgs &&
	 * (data.intValue() == PHPTokenScanner.PHPTokenScanner || data.intValue() == PHPTokenScanner.PHPTokenScannerOLD)) {
	 * nextAreArgs = false; }
	 */
	// Convert the integer tokens into tokens containing color information!
	/*
	 * if (isKeyword(data.intValue())) { switch (data.intValue()) { case Tokens.k__FILE__: case Tokens.k__LINE__: case
	 * Tokens.kSELF: if (nextIsClassName) { nextIsClassName = false; return getToken("entity.name.type.class.ruby");
	 * //$NON-NLS-1$ } return getToken("variable.language.ruby"); //$NON-NLS-1$ case Tokens.kNIL: case Tokens.kTRUE:
	 * case Tokens.kFALSE: return getToken("constant.language.ruby"); //$NON-NLS-1$ case Tokens.kAND: case Tokens.kNOT:
	 * case Tokens.kOR: return getToken("keyword.operator.logical.ruby"); //$NON-NLS-1$ case Tokens.kDO_BLOCK: case
	 * Tokens.kDO: lookForBlock = true; return getToken("keyword.control.start-block.ruby"); //$NON-NLS-1$ case
	 * Tokens.kCLASS: nextIsClassName = true; return getToken("keyword.control.class.ruby"); //$NON-NLS-1$ case
	 * Tokens.kMODULE: nextIsModuleName = true; return getToken("keyword.control.module.ruby"); //$NON-NLS-1$ case
	 * Tokens.kDEF: nextIsMethodName = true; return getToken("keyword.control.def.ruby"); //$NON-NLS-1$ default: return
	 * getToken("keyword.control.php"); //$NON-NLS-1$ } }
	 */
	// return getToken("entity.name.type.class.php"); //$NON-NLS-1$
	/*
	 * switch (data.intValue()) { case PHPTokenScanner.ASSIGNMENT: return getToken("keyword.operator.assignment.php");
	 * //$NON-NLS-1$ case Tokens.tCMP: <=> case Tokens.tMATCH: =~ case Tokens.tNMATCH: !~ case Tokens.tEQ: == case
	 * Tokens.tEQQ: === case Tokens.tNEQ: != case Tokens.tGEQ: >= case Tokens.tLEQ: case Tokens.tLT: case Tokens.tGT:
	 * return getToken("keyword.operator.comparison.ruby"); //$NON-NLS-1$ case Tokens.tAMPER: case Tokens.tPERCENT: case
	 * Tokens.tPOW: case Tokens.tSTAR: case Tokens.tPLUS: case Tokens.tMINUS: case Tokens.tDIVIDE: return
	 * getToken("keyword.operator.arithmetic.ruby"); //$NON-NLS-1$ case Tokens.tANDOP: case Tokens.tBANG: case
	 * Tokens.tOROP: case Tokens.tCARET: case PHPTokenScanner.QUESTION: return
	 * getToken("keyword.operator.logical.ruby"); //$NON-NLS-1$ case Tokens.tPIPE: if (lookForBlock) { inPipe = !inPipe;
	 * if (!inPipe) lookForBlock = false; return getToken("default.ruby"); //$NON-NLS-1$ } return
	 * getToken("keyword.operator.logical.ruby"); //$NON-NLS-1$ case Tokens.tLBRACE: lookForBlock = true; return
	 * getToken("default.ruby"); //$NON-NLS-1$ case Tokens.tRPAREN: nextAreArgs = false; return
	 * getToken("default.ruby"); //$NON-NLS-1$ case Tokens.tLSHFT: if (nextIsClassName) { return
	 * getToken("entity.name.type.class.ruby"); //$NON-NLS-1$ } return
	 * getToken("keyword.operator.assignment.augmented.ruby"); //$NON-NLS-1$ case Tokens.tOP_ASGN: return
	 * getToken("keyword.operator.assignment.augmented.ruby"); //$NON-NLS-1$ case Tokens.tASSOC: return
	 * getToken("punctuation.separator.key-value"); //$NON-NLS-1$ case PHPTokenScanner.CHARACTER: return
	 * getToken("character.ruby"); //$NON-NLS-1$ case Tokens.tCOLON2: case Tokens.tCOLON3: return
	 * getToken("punctuation.separator.inheritance.ruby"); //$NON-NLS-1$ case Tokens.tFLOAT: case Tokens.tINTEGER:
	 * return getToken("constant.numeric.ruby"); //$NON-NLS-1$ case Tokens.tSYMBEG: return
	 * getToken("constant.other.symbol.ruby"); //$NON-NLS-1$ case Tokens.tGVAR: return
	 * getToken("variable.other.readwrite.global.ruby"); //$NON-NLS-1$ case Tokens.tIVAR: return
	 * getToken("variable.other.readwrite.instance.ruby"); //$NON-NLS-1$ case Tokens.tCVAR: return
	 * getToken("variable.other.readwrite.class.ruby"); //$NON-NLS-1$ case Tokens.tCONSTANT: if (nextIsModuleName) {
	 * nextIsModuleName = false; return getToken("entity.name.type.module.ruby"); //$NON-NLS-1$ } if (nextIsClassName) {
	 * nextIsClassName = false; return getToken("entity.name.type.class.ruby"); //$NON-NLS-1$ } int nextToken = peek();
	 * if (nextToken == Tokens.tCOLON2 || nextToken == Tokens.tDOT) { return getToken("support.class.ruby");
	 * //$NON-NLS-1$ } return getToken("variable.other.constant.ruby"); //$NON-NLS-1$ case Tokens.yyErrorCode: return
	 * getToken("error.ruby"); //$NON-NLS-1$ case Tokens.tIDENTIFIER: case Tokens.tFID: if (nextAreArgs) { return
	 * getToken("variable.parameter.ruby"); //$NON-NLS-1$ } if (nextIsMethodName) { nextIsMethodName = false;
	 * nextAreArgs = true; return getToken("entity.name.function.ruby"); //$NON-NLS-1$ } if (lookForBlock && inPipe)
	 * return getToken("variable.other.block.ruby"); //$NON-NLS-1$ // intentionally fall through default: return
	 * getToken("default.php"); //$NON-NLS-1$ }
	 */
}
