package com.aptana.editor.php.internal.contentAssist;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class AutoActivateContentAssistProposal extends PHPCompletionProposal
{

	public AutoActivateContentAssistProposal(String replacementString, int replacementOffset, int replacementLength,
			int cursorPosition, Image image, String displayString, IContextInformation contextInformation,
			String additionalProposalInfo, int objectType, String fileLocation, Image[] userAgentImages)
	{
		super(replacementString, replacementOffset, replacementLength, cursorPosition, image, displayString,
				contextInformation, additionalProposalInfo, objectType, fileLocation, userAgentImages);
	}

	public void apply(IDocument document)
	{
		super.apply(document);

		if (viewer != null && viewer instanceof SourceViewer)
		{
			Display.getCurrent().asyncExec(new Runnable()
			{
				public void run()
				{
					((SourceViewer) viewer).doOperation(SourceViewer.CONTENTASSIST_PROPOSALS);
				}

			});
		}
	}
}
