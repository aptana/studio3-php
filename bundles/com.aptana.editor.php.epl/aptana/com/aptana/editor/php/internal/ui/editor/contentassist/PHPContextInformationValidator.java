package com.aptana.editor.php.internal.ui.editor.contentassist;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationExtension;
import org.eclipse.jface.text.contentassist.IContextInformationPresenter;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;

/**
 * PHP content assist validator. <br>
 * This validator checks whether the editor should display argument assistance when typing in arguments into a function
 * or a constructor.
 */
public class PHPContextInformationValidator implements IContextInformationValidator, IContextInformationPresenter
{

	private IContextInformation fContextInformation;
	private int fInitialOffset;
	private ITextViewer fViewer;
	private int fCurrentParameter;

	/**
	 * 
	 */
	public PHPContextInformationValidator()
	{
	}

	/**
	 * @see org.eclipse.jface.text.contentassist.IContextInformationValidator#install(org.eclipse.jface.text.contentassist.IContextInformation,
	 *      org.eclipse.jface.text.ITextViewer, int)
	 */
	public void install(IContextInformation info, ITextViewer viewer, int offset)
	{
		fContextInformation = info;
		fViewer = viewer;
		if (info instanceof IContextInformationExtension)
		{
			fInitialOffset = ((IContextInformationExtension) info).getContextInformationPosition();
		}
		else
		{
			fInitialOffset = offset - 1;
		}
		fInitialOffset++;
		fCurrentParameter = -1;
	}

	/**
	 * @see IContextInformationValidator#isContextInformationValid(int)
	 */
	public boolean isContextInformationValid(int offset)
	{

		try
		{
			if (offset < fInitialOffset)
				return false;

			IDocument document = fViewer.getDocument();
			IRegion line = document.getLineInformationOfOffset(fInitialOffset);

			if (offset < line.getOffset() || offset >= document.getLength())
				return false;

			return getCharCount(document, fInitialOffset, offset, "(", ")", false) >= 0; //$NON-NLS-1$ //$NON-NLS-2$

		}
		catch (BadLocationException x)
		{
			return false;
		}
	}

	/**
	 * @see org.eclipse.jface.text.contentassist.IContextInformationPresenter#updatePresentation(int,
	 *      org.eclipse.jface.text.TextPresentation)
	 */
	public boolean updatePresentation(int offset, TextPresentation presentation)
	{
		int currentParameter = -1;

		try
		{
			currentParameter = getCharCount(fViewer.getDocument(), fInitialOffset, offset, ",", "", true); //$NON-NLS-1$//$NON-NLS-2$
		}
		catch (BadLocationException x)
		{
			return false;
		}

		if (fCurrentParameter != -1)
		{
			if (currentParameter == fCurrentParameter)
				return false;
		}

		presentation.clear();
		fCurrentParameter = currentParameter;

		String s = fContextInformation.getInformationDisplayString();
		int[] commas = computeCommaPositions(s);

		if (commas.length - 2 < fCurrentParameter)
		{
			presentation.addStyleRange(new StyleRange(0, s.length(), null, null, SWT.NORMAL));
			return true;
		}

		int start = commas[fCurrentParameter] + 1;
		int end = commas[fCurrentParameter + 1];
		if (start > 0)
			presentation.addStyleRange(new StyleRange(0, start, null, null, SWT.NORMAL));

		if (end > start)
			presentation.addStyleRange(new StyleRange(start, end - start, null, null, SWT.BOLD));

		if (end < s.length())
			presentation.addStyleRange(new StyleRange(end, s.length() - end, null, null, SWT.NORMAL));

		return true;
	}

	private int getCharCount(IDocument document, int start, final int end, String increments, String decrements,
			boolean considerNesting) throws BadLocationException
	{

		Assert.isTrue((increments.length() != 0 || decrements.length() != 0) && !increments.equals(decrements));

		// The given offset that we get here for PHP is for the function name end. We'll need to
		// scan for the open bracket and fix the offset to be inside the function arguments.
		while (start < end)
		{
			char c = document.getChar(start);
			if (Character.isWhitespace(c))
			{
				start++;
			}
			else if (c == '(')
			{
				start++;
				break;
			} else {
				// Avoid an infinite loop
				break;
			}
		}

		final int NONE = 0;
		final int BRACKET = 1;
		final int BRACE = 2;
		final int PAREN = 3;
		final int ANGLE = 4;

		int nestingMode = NONE;
		int nestingLevel = 0;

		int charCount = 0;
		int offset = start;
		while (offset < end)
		{
			char curr = document.getChar(offset++);
			switch (curr)
			{
				case '/':
					if (offset < end)
					{
						char next = document.getChar(offset);
						if (next == '*')
						{
							// a comment starts, advance to the comment end
							offset = getCommentEnd(document, offset + 1, end);
						}
						else if (next == '/')
						{
							// '//'-comment: nothing to do anymore on this line
							int nextLine = document.getLineOfOffset(offset) + 1;
							if (nextLine == document.getNumberOfLines())
								offset = end;
							else
								offset = document.getLineOffset(nextLine);
						}
					}
					break;
				case '*':
					if (offset < end)
					{
						char next = document.getChar(offset);
						if (next == '/')
						{
							// we have been in a comment: forget what we read before
							charCount = 0;
							++offset;
						}
					}
					break;
				case '"':
				case '\'':
					offset = getStringEnd(document, offset, end, curr);
					break;
				case '[':
					if (considerNesting)
					{
						if (nestingMode == BRACKET || nestingMode == NONE)
						{
							nestingMode = BRACKET;
							nestingLevel++;
						}
						break;
					}
					//$FALL-THROUGH$
				case ']':
					if (considerNesting)
					{
						if (nestingMode == BRACKET)
							if (--nestingLevel == 0)
								nestingMode = NONE;
						break;
					}
					//$FALL-THROUGH$
				case '(':
					if (considerNesting)
					{
						if (nestingMode == ANGLE)
						{
							// generics heuristic failed
							nestingMode = PAREN;
							nestingLevel = 1;
						}
						if (nestingMode == PAREN || nestingMode == NONE)
						{
							nestingMode = PAREN;
							nestingLevel++;
						}
						break;
					}
					//$FALL-THROUGH$
				case ')':
					if (considerNesting)
					{
						if (nestingMode == PAREN)
							if (--nestingLevel == 0)
								nestingMode = NONE;
						break;
					}
					//$FALL-THROUGH$
				case '{':
					if (considerNesting)
					{
						if (nestingMode == ANGLE)
						{
							nestingMode = BRACE;
							nestingLevel = 1;
						}
						if (nestingMode == BRACE || nestingMode == NONE)
						{
							nestingMode = BRACE;
							nestingLevel++;
						}
						break;
					}
					//$FALL-THROUGH$
				case '}':
					if (considerNesting)
					{
						if (nestingMode == BRACE)
							if (--nestingLevel == 0)
								nestingMode = NONE;
						break;
					}
					//$FALL-THROUGH$
				default:
					if (nestingLevel != 0)
						continue;

					if (increments.indexOf(curr) >= 0)
					{
						++charCount;
					}

					if (decrements.indexOf(curr) >= 0)
					{
						--charCount;
					}
			}
		}

		return charCount;
	}

	private int[] computeCommaPositions(String code)
	{
		final int length = code.length();
		int pos = 0;
		List<Integer> positions = new ArrayList<Integer>();
		positions.add(new Integer(-1));
		while (pos < length && pos != -1)
		{
			char ch = code.charAt(pos);
			switch (ch)
			{
				case ',':
					positions.add(new Integer(pos));
					break;
				case '[':
					pos = code.indexOf(']', pos);
					break;
				default:
					break;
			}
			if (pos != -1)
				pos++;
		}
		positions.add(new Integer(length));

		int[] fields = new int[positions.size()];
		for (int i = 0; i < fields.length; i++)
			fields[i] = ((Integer) positions.get(i)).intValue();
		return fields;
	}

	private int getCommentEnd(IDocument d, int pos, int end) throws BadLocationException
	{
		while (pos < end)
		{
			char curr = d.getChar(pos);
			pos++;
			if (curr == '*')
			{
				if (pos < end && d.getChar(pos) == '/')
				{
					return pos + 1;
				}
			}
		}
		return end;
	}

	private int getStringEnd(IDocument d, int pos, int end, char ch) throws BadLocationException
	{
		while (pos < end)
		{
			char curr = d.getChar(pos);
			pos++;
			if (curr == '\\')
			{
				// ignore escaped characters
				pos++;
			}
			else if (curr == ch)
			{
				return pos;
			}
		}
		return end;
	}
}
