package com.aptana.editor.php.internal.ui.editor.outline;

import org.eclipse.jface.viewers.DecoratingLabelProvider;

/**
 * Decorated PHP label provider.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPDecoratingLabelProvider extends DecoratingLabelProvider
{

	/**
	 * Constructs a new decorating PHP label provider with a given parse state
	 * 
	 * @param parseState
	 */
	public PHPDecoratingLabelProvider()
	{
		super(new PHTMLOutlineLabelProvider(), new PHPOutlineLabelDecorator(16, 16));
	}
}
