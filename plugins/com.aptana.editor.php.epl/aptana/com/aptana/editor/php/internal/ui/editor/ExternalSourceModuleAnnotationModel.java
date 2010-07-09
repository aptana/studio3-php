package com.aptana.editor.php.internal.ui.editor;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dltk.compiler.problem.DefaultProblem;

import com.aptana.core.resources.IUniformResourceChangeEvent;
import com.aptana.core.resources.IUniformResourceChangeListener;
import com.aptana.core.resources.IUniformResourceMarker;
import com.aptana.core.resources.MarkerUtils;
import com.aptana.editor.php.epl.PHPEplPlugin;
import com.aptana.editor.php.internal.core.builder.PHPUniformResource;

/**
 * Annotation model dealing with java marker annotations and temporary problems. Also acts as problem requester for its
 * compilation unit. Initially inactive. Must explicitly be activated.
 */
public class ExternalSourceModuleAnnotationModel extends SourceModuleAnnotationModel
{
	private PHPUniformResource resource;
	private IUniformResourceChangeListener resourceChangeListener;

	/**
	 * ResourceChangeListener
	 */
	private class ResourceChangeListener implements IUniformResourceChangeListener
	{
		/**
		 * @see com.aptana.ide.core.resources.IUniformResourceChangeListener#resourceChanged(com.aptana.ide.core.resources.IUniformResourceChangeEvent)
		 */
		public void resourceChanged(IUniformResourceChangeEvent event)
		{
			if (resource.equals(event.getResource()))
			{
				update(event.getMarkerDeltas());
			}
		}
	};

	/**
	 * Constructs a new ExternalSourceModuleAnnotationModel
	 * 
	 * @param location
	 *            An IPath location
	 */
	public ExternalSourceModuleAnnotationModel(IPath location)
	{
		super(ResourcesPlugin.getWorkspace().getRoot());
		this.resource = new PHPUniformResource(location);
		this.resourceChangeListener = new ResourceChangeListener();
	}

	/*
	 * @see AbstractMarkerAnnotationModel#retrieveMarkers()
	 */
	protected IMarker[] retrieveMarkers() throws CoreException
	{
		IMarker[] markers = MarkerUtils.findMarkers(resource, DefaultProblem.MARKER_TYPE_PROBLEM, true);
		return markers;
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel#deleteMarkers(org.eclipse.core.resources.IMarker[])
	 */
	protected void deleteMarkers(final IMarker[] markers) throws CoreException
	{
		try
		{
			ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable()
			{
				public void run(IProgressMonitor monitor) throws CoreException
				{
					for (int i = 0; i < markers.length; ++i)
					{
						markers[i].delete();
					}
				}
			}, null, IWorkspace.AVOID_UPDATE, null);
		}
		catch (CoreException e)
		{
			PHPEplPlugin.logInfo("Problem while deleting external markers", e); //$NON-NLS-1$
		}
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel#listenToMarkerChanges(boolean)
	 */
	protected void listenToMarkerChanges(boolean listen)
	{
		if (listen)
		{
			MarkerUtils.addResourceChangeListener(resourceChangeListener);
		}
		else
		{
			MarkerUtils.removeResourceChangeListener(resourceChangeListener);
		}
	}

	/**
	 * @see org.eclipse.ui.texteditor.AbstractMarkerAnnotationModel#isAcceptable(org.eclipse.core.resources.IMarker)
	 */
	protected boolean isAcceptable(IMarker marker)
	{
		return marker instanceof IUniformResourceMarker
				&& resource.equals(((IUniformResourceMarker) marker).getUniformResource());
	}

	/**
	 * Updates this model to the given marker deltas.
	 * 
	 * @param markerDeltas
	 *            the array of marker deltas
	 */
	protected void update(IMarkerDelta[] markerDeltas)
	{
		if (markerDeltas.length == 0)
		{
			return;
		}
		for (int i = 0; i < markerDeltas.length; i++)
		{
			IMarkerDelta delta = markerDeltas[i];

			switch (delta.getKind())
			{
				case IResourceDelta.ADDED:
					addMarkerAnnotation(delta.getMarker());
					break;

				case IResourceDelta.REMOVED:
					removeMarkerAnnotation(delta.getMarker());
					break;

				case IResourceDelta.CHANGED:
					modifyMarkerAnnotation(delta.getMarker());
					break;

				default:
					break;
			}
		}
		fireModelChanged();
	}
}