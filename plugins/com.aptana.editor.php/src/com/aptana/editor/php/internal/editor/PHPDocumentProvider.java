package com.aptana.editor.php.internal.editor;

import com.aptana.editor.common.CompositeDocumentProvider;
import com.aptana.editor.html.HTMLSourceConfiguration;
import com.aptana.editor.php.internal.IPHPConstants;

/**
 * PHP document provider.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPDocumentProvider extends CompositeDocumentProvider
{

	protected PHPDocumentProvider()
	{
		super(IPHPConstants.CONTENT_TYPE_PHP, 
				HTMLSourceConfiguration.getDefault(),
				PHPSourceConfiguration.getDefault(), 
				PHPPartitionerSwitchStrategy.getDefault());
	}
}