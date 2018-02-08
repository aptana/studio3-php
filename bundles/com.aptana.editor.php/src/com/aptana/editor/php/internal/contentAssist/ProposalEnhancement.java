package com.aptana.editor.php.internal.contentAssist;

import java.util.List;

import org.eclipse.jface.text.Position;

/**
 * Proposal enhancement.
 * 
 * @author Denis Denisenko
 */
class ProposalEnhancement
{
	/**
	 * Replacement string.
	 */
	public String replaceString;

	/**
	 * Cursor offset modification.
	 */
	public int cursorShift;

	/**
	 * Positions.
	 */
	public List<Position> positions;

	/**
	 * Caret exit offset.
	 */
	public int caretExitOffset;
}