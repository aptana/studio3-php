/**
 * Copyright (c) 2005-2006 Aptana, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
 * this entire header must remain intact.
 */
package com.aptana.editor.php.internal.contentAssist;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.InclusivePositionUpdater;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import com.aptana.editor.common.contentassist.CommonCompletionProposal;

/**
 * PHP code assist completion proposal.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>, Pavel Petrochenko
 */
public class PHPCompletionProposal extends CommonCompletionProposal
{
	/** The type of the object. This is an 'enum' of JSCompletionProposalComparator sorting types. */
	private int fObjectType;
	private IRegion fSelectedRegion; // initialized by apply()
	/**
	 * Position updater.
	 */
	private InclusivePositionUpdater fUpdater;

	private IDocumentationResolver resolver;

	/**
	 * Viewer.
	 */
	protected ITextViewer viewer;

	/**
	 * Proposal positions list.
	 */
	private List<Position> positions;

	/**
	 * Caret exit offset.
	 */
	private int exitCaretOffset;

	/**
	 * Creates a new completion proposal. All fields are initialized based on the provided information.
	 * 
	 * @param replacementString
	 *            the actual string to be inserted into the document
	 * @param replacementOffset
	 *            the offset of the text to be replaced
	 * @param replacementLength
	 *            the length of the text to be replaced
	 * @param cursorPosition
	 *            the position of the cursor following the insert relative to replacementOffset
	 * @param image
	 *            the image to display for this proposal
	 * @param displayString
	 *            the string to be displayed for the proposal
	 * @param contextInformation
	 *            the context information associated with this proposal
	 * @param additionalProposalInfo
	 *            the additional information associated with this proposal
	 * @param objectType
	 *            The type of the object. This is an 'enum' of JSCompletionProposalComparator sorting types (used for
	 *            quicker sorting).
	 * @param fileLocation
	 *            The source file location where the CA proposal was found.
	 * @param userAgentImages
	 */
	public PHPCompletionProposal(String replacementString, int replacementOffset, int replacementLength,
			int cursorPosition, Image image, String displayString, IContextInformation contextInformation,
			String additionalProposalInfo, int objectType, String fileLocation, Image[] userAgentImages)
	{
		super(replacementString, replacementOffset, replacementLength, cursorPosition, image, displayString,
				contextInformation, additionalProposalInfo);
		fObjectType = objectType;
		setFileLocation(fileLocation);
		setUserAgentImages(userAgentImages);
	}

	/**
	 * padToColumn
	 * 
	 * @param stringToPad
	 * @param columnWidth
	 * @return String
	 */
	public static String padToColumn(String stringToPad, int columnWidth)
	{
		String blanks = "                             "; //$NON-NLS-1$

		if (stringToPad.length() > columnWidth)
		{
			return stringToPad.substring(0, columnWidth);
		}
		else
		{
			int blankLength = columnWidth - stringToPad.length();
			return stringToPad + blanks.substring(0, blankLength);
		}
	}

	/**
	 * Returns the type of object this proposal contains (class, method, etc).This is used for sorting.
	 * 
	 * @return Returns the type of object this proposal contains (class, method, etc).This is used for sorting.
	 */
	public int getObjectType()
	{
		return fObjectType;
	}

	/**
	 * @see ICompletionProposal#apply(IDocument)
	 */
	public void apply(IDocument document)
	{
		try
		{
			document.replace(_replacementOffset, _replacementLength, _replacementString);

			if (viewer != null)
			{

				LinkedModeModel model = new LinkedModeModel();
				boolean positionsAdded = false;
				ensurePositionCategoryInstalled(document, model);

				if (positions != null && positions.size() != 0)
				{
					for (Position pos : positions)
					{
						try
						{
							document.addPosition(getCategory(), pos);

							LinkedPositionGroup group = new LinkedPositionGroup();
							group.addPosition(new LinkedPosition(document, pos.offset, pos.length));
							model.addGroup(group);
							positionsAdded = true;

						}
						catch (BadPositionCategoryException e)
						{
							ensurePositionCategoryRemoved(document);
							return;
						}
					}
				}

				if (positionsAdded)
				{
					model.forceInstall();
					LinkedModeUI ui = new LinkedModeUI(model, viewer);
					ui.setExitPosition(viewer, exitCaretOffset, 0, Integer.MAX_VALUE);
					ui.enter();
					fSelectedRegion = ui.getSelectedRegion();
				}
				else
				{
					ensurePositionCategoryRemoved(document);
				}
			}
			else
			{
				ensurePositionCategoryRemoved(document);
			}

		}
		catch (BadLocationException x)
		{
			ensurePositionCategoryRemoved(document);
		}
	}

	/**
	 * Sets viewer.
	 * 
	 * @param viewer
	 *            - viewer.
	 */
	public void setViewer(ITextViewer viewer)
	{
		this.viewer = viewer;
	}

	/*
	 * @see ICompletionProposal#getSelection(IDocument)
	 */
	public Point getSelection(IDocument document)
	{
		if (fSelectedRegion == null)
			return new Point(_replacementOffset, 0);

		return new Point(fSelectedRegion.getOffset(), fSelectedRegion.getLength());
	}

	/**
	 * @see ICompletionProposal#getAdditionalProposalInfo()
	 */
	public String getAdditionalProposalInfo()
	{
		if (resolver != null)
		{
			return resolver.resolveDocumentation();
		}
		return super.getAdditionalProposalInfo();
	}

	/**
	 * @return resolver
	 */
	public IDocumentationResolver getResolver()
	{
		return resolver;
	}

	/**
	 * sets resolver
	 * 
	 * @param resolver
	 */
	public void setResolver(IDocumentationResolver resolver)
	{
		this.resolver = resolver;
	}

	/**
	 * Set up proposal position structure.
	 * 
	 * @param positions
	 *            - positions.
	 * @param exitCaretOffset
	 *            - caret exit offset.
	 */
	public void setPositions(List<Position> positions, int exitCaretOffset)
	{
		this.positions = positions;
		this.exitCaretOffset = exitCaretOffset;
	}

	/**
	 * Ensures that position category is installed to the document.
	 * 
	 * @param document
	 *            - document.
	 * @param model
	 *            - linked model.
	 */
	private void ensurePositionCategoryInstalled(final IDocument document, LinkedModeModel model)
	{
		if (!document.containsPositionCategory(getCategory()))
		{
			document.addPositionCategory(getCategory());
			fUpdater = new InclusivePositionUpdater(getCategory());
			document.addPositionUpdater(fUpdater);

			model.addLinkingListener(new ILinkedModeListener()
			{

				/*
				 * @see
				 * org.eclipse.jface.text.link.ILinkedModeListener#left(org.eclipse.jface.text.link.LinkedModeModel,
				 * int)
				 */
				public void left(LinkedModeModel environment, int flags)
				{
					ensurePositionCategoryRemoved(document);
				}

				public void suspend(LinkedModeModel environment)
				{
				}

				public void resume(LinkedModeModel environment, int flags)
				{
				}
			});
		}
	}

	/**
	 * Ensures that position category is removed from the document.
	 * 
	 * @param document
	 *            - document.
	 */
	private void ensurePositionCategoryRemoved(IDocument document)
	{
		if (document.containsPositionCategory(getCategory()))
		{
			try
			{
				document.removePositionCategory(getCategory());
			}
			catch (BadPositionCategoryException e)
			{
				// ignore
			}
			document.removePositionUpdater(fUpdater);
		}
		fSelectedRegion = new Region(_replacementOffset + _replacementString.length(), 0);
	}

	private String getCategory()
	{
		return "PHPProposalCategory_" + toString(); //$NON-NLS-1$
	}

	public int getReplacementLength()
	{
		return _replacementLength;
	}

	public void setReplacementLength(int replacementLength)
	{
		_replacementLength = replacementLength;
	}
}
