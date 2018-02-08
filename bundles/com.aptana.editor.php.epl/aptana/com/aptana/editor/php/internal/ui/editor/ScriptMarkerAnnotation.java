package com.aptana.editor.php.internal.ui.editor;

import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.MarkerUtilities;

public class ScriptMarkerAnnotation extends MarkerAnnotation implements IScriptAnnotation
{

	public static final String DLTK_MARKER_TYPE_PREFIX = "com.aptana.editor.php.epl"; //$NON-NLS-1$
	public static final String ERROR_ANNOTATION_TYPE = "com.aptana.editor.php.epl.error"; //$NON-NLS-1$
	public static final String WARNING_ANNOTATION_TYPE = "com.aptana.editor.php.epl.warning"; //$NON-NLS-1$
	public static final String INFO_ANNOTATION_TYPE = "com.aptana.editor.php.epl.info"; //$NON-NLS-1$
	public static final String TASK_ANNOTATION_TYPE = "org.eclipse.ui.workbench.texteditor.task"; //$NON-NLS-1$
	private static final String MARKER_ID = "id"; //$NON-NLS-1$

	private IScriptAnnotation fOverlay;

	public ScriptMarkerAnnotation(IMarker marker)
	{
		super(marker);
	}

	/*
	 * @see IJavaAnnotation#getArguments()
	 */
	public String[] getArguments()
	{
		// IMarker marker= getMarker();
		// if (marker != null && marker.exists() && isProblem())
		// return CorrectionEngine.getProblemArguments(marker);
		return null;
	}

	/*
	 * @see IJavaAnnotation#getId()
	 */
	public int getId()
	{
		IMarker marker = getMarker();
		if (marker == null || !marker.exists())
			return -1;

		if (isProblem())
			return marker.getAttribute(MARKER_ID, -1);

		// if (TASK_ANNOTATION_TYPE.equals(getAnnotationType())) {
		// try {
		// if (marker.isSubtypeOf(IScriptModelMarker.TASK_MARKER)) {
		// return IProblem.Task;
		// }
		// } catch (CoreException e) {
		// DLTKUIPlugin.log(e); // should no happen, we test for marker.exists
		// }
		// }

		return -1;
	}

	/*
	 * @see IJavaAnnotation#isProblem()
	 */
	public boolean isProblem()
	{
		String type = getType();
		return WARNING_ANNOTATION_TYPE.equals(type) || ERROR_ANNOTATION_TYPE.equals(type);
	}

	/**
	 * Overlays this annotation with the given javaAnnotation.
	 * 
	 * @param javaAnnotation
	 *            annotation that is overlaid by this annotation
	 */
	public void setOverlay(IScriptAnnotation javaAnnotation)
	{
		if (fOverlay != null)
			fOverlay.removeOverlaid(this);

		fOverlay = javaAnnotation;
		if (!isMarkedDeleted())
			markDeleted(fOverlay != null);

		if (fOverlay != null)
			fOverlay.addOverlaid(this);
	}

	/*
	 * @see IJavaAnnotation#hasOverlay()
	 */
	public boolean hasOverlay()
	{
		return fOverlay != null;
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.javaeditor.IJavaAnnotation#getOverlay()
	 */
	public IScriptAnnotation getOverlay()
	{
		return fOverlay;
	}

	/*
	 * @see IJavaAnnotation#addOverlaid(IJavaAnnotation)
	 */
	public void addOverlaid(IScriptAnnotation annotation)
	{
		// not supported
	}

	/*
	 * @see IJavaAnnotation#removeOverlaid(IJavaAnnotation)
	 */
	public void removeOverlaid(IScriptAnnotation annotation)
	{
		// not supported
	}

	/*
	 * @see IJavaAnnotation#getOverlaidIterator()
	 */
	@SuppressWarnings("rawtypes")
	public Iterator getOverlaidIterator()
	{
		// not supported
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.javaeditor.IJavaAnnotation#getMarkerType()
	 */
	public String getMarkerType()
	{
		IMarker marker = getMarker();
		if (marker == null || !marker.exists())
			return null;

		return MarkerUtilities.getMarkerType(getMarker());
	}
}
