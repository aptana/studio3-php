package com.aptana.editor.php.internal.core.model;

import com.aptana.editor.php.core.model.ISourceModule;

/**
 * An interface for any class that can provide an {@link ISourceModule}. Note - Only the PHP Editor is allowed to
 * implement this interface.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 * @since Aptana PHP 3.0
 */
public interface ISourceModuleProviderEditor
{
	/**
	 * Returns an {@link ISourceModule}.
	 * 
	 * @return {@link ISourceModule}
	 */
	public ISourceModule getSourceModule();

	/**
	 * Return the source module's editor ID.
	 * 
	 * @return The ID of the editor that implements this source module provider.
	 */
	public String getEditorID();
}
