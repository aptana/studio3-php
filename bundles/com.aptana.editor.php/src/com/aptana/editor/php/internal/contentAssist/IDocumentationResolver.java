package com.aptana.editor.php.internal.contentAssist;

/**
 * Document resolver
 */
public interface IDocumentationResolver
{
	public String resolveDocumentation();

	/**
	 * Returns the original proposal content. This can differ from the display string by not returning any additional
	 * text that is appended to the display string when creating the proposal. For example, a '-(local)' string that is
	 * appended to a proposal display string will not be returned with this proposal content.
	 * 
	 * @return
	 */
	public String getProposalContent();
}
