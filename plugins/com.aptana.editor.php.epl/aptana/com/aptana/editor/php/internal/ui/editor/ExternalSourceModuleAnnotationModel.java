package com.aptana.editor.php.internal.ui.editor;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * Annotation model dealing with java marker annotations and temporary problems. Also acts as problem requester for its
 * compilation unit. Initially inactive. Must explicitly be activated.
 */
public class ExternalSourceModuleAnnotationModel extends SourceModuleAnnotationModel
{
	private IPath location;

	public ExternalSourceModuleAnnotationModel(IPath location)
	{
		super(ResourcesPlugin.getWorkspace().getRoot());
		this.location = location;
	}

	/*
	 * @see AbstractMarkerAnnotationModel#retrieveMarkers()
	 */
	protected IMarker[] retrieveMarkers() throws CoreException
	{
		String moduleLocation = location.toPortableString();
		IMarker[] markers = super.retrieveMarkers();
		List<IMarker> locationMarkers = new LinkedList<IMarker>();
		for (int i = 0; i < markers.length; i++)
		{
			IMarker marker = markers[i];
			String markerLocation = (String) marker.getAttribute(IMarker.LOCATION);
			if (moduleLocation.equals(markerLocation))
			{
				locationMarkers.add(marker);
			}
		}
		return locationMarkers.toArray(new IMarker[locationMarkers.size()]);
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
			return;

		String moduleLocation = location.toPortableString();

		for (int i = 0; i < markerDeltas.length; i++)
		{
			IMarkerDelta delta = markerDeltas[i];
			IMarker marker = delta.getMarker();

			if (moduleLocation.equals(marker.getAttribute(IMarker.LOCATION, moduleLocation)))
			{
				switch (delta.getKind())
				{
					case IResourceDelta.ADDED:
						addMarkerAnnotation(marker);
						break;
					case IResourceDelta.REMOVED:
						removeMarkerAnnotation(marker);
						break;
					case IResourceDelta.CHANGED:
						modifyMarkerAnnotation(marker);
						break;
				}
			}
		}

		fireModelChanged();
	}
}