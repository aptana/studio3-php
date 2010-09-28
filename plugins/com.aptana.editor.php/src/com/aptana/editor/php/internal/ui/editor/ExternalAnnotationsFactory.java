package com.aptana.editor.php.internal.ui.editor;

import org.eclipse.core.filebuffers.IAnnotationModelFactory;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.source.IAnnotationModel;

/**
 * Annotation model factory that is called when an external recourse with PHP content type is opened.<br>
 * This factory creates {@link ExternalSourceModuleAnnotationModel} when the createAnnotationModel is called.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class ExternalAnnotationsFactory implements IAnnotationModelFactory
{

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.filebuffers.IAnnotationModelFactory#createAnnotationModel(org.eclipse.core.runtime.IPath)
	 */
	public IAnnotationModel createAnnotationModel(IPath location)
	{
		return new ExternalSourceModuleAnnotationModel(location);
	}
}
