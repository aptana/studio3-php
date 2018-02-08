package com.aptana.editor.php.internal.ui.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.quickassist.IQuickFixableAnnotation;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationAccessExtension;
import org.eclipse.jface.text.source.IAnnotationPresentation;
import org.eclipse.jface.text.source.ImageUtilities;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.AnnotationPreferenceLookup;
import org2.eclipse.dltk.compiler.problem.CategorizedProblem;
import org2.eclipse.dltk.compiler.problem.IProblem;

import com.aptana.editor.php.core.model.ISourceModule;
import com.aptana.editor.php.internal.ui.PHPPluginImages;

/**
 * Annotation representing an <code>IProblem</code>.
 */
public class ProblemAnnotation extends Annotation implements
		IScriptAnnotation, IAnnotationPresentation, IQuickFixableAnnotation {

	public static final String SPELLING_ANNOTATION_TYPE = "org.eclipse.ui.workbench.texteditor.spelling"; //$NON-NLS-1$

	// XXX: To be fully correct these constants should be non-static
	/**
	 * The layer in which task problem annotations are located.
	 */
	private static final int TASK_LAYER;
	/**
	 * The layer in which info problem annotations are located.
	 */
	private static final int INFO_LAYER;
	/**
	 * The layer in which warning problem annotations representing are
	 * located.
	 */
	private static final int WARNING_LAYER;
	/**
	 * The layer in which error problem annotations representing are
	 * located.
	 */
	private static final int ERROR_LAYER;

	static {
		final AnnotationPreferenceLookup lookup = EditorsUI
				.getAnnotationPreferenceLookup();
		TASK_LAYER = computeLayer(
				ScriptMarkerAnnotation.TASK_ANNOTATION_TYPE, lookup);
		INFO_LAYER = computeLayer(
				ScriptMarkerAnnotation.INFO_ANNOTATION_TYPE, lookup);
		WARNING_LAYER = computeLayer(
				ScriptMarkerAnnotation.WARNING_ANNOTATION_TYPE, lookup);
		ERROR_LAYER = computeLayer(
				ScriptMarkerAnnotation.ERROR_ANNOTATION_TYPE, lookup);
	}

	private static int computeLayer(String annotationType,
			AnnotationPreferenceLookup lookup) {
		Annotation annotation = new Annotation(annotationType, false, null);
		AnnotationPreference preference = lookup
				.getAnnotationPreference(annotation);
		if (preference != null)
			return preference.getPresentationLayer() + 1;
		else
			return IAnnotationAccessExtension.DEFAULT_LAYER + 1;
	}

	private static Image fgQuickFixImage;
	private static Image fgQuickFixErrorImage;

	private static Image fgTaskImage;
	private static Image fgInfoImage;
	private static Image fgWarningImage;
	private static Image fgErrorImage;
	private static boolean fgImagesInitialized = false;

	private final ISourceModule fSourceModule;
	private List<IScriptAnnotation> fOverlaids;
	private final IProblem fProblem;
	private Image fImage;
	private boolean fImageInitialized = false;
	private int fLayer = IAnnotationAccessExtension.DEFAULT_LAYER;
	private boolean fIsQuickFixable;
	private boolean fIsQuickFixableStateSet = false;

	public ProblemAnnotation(IProblem problem, ISourceModule cu) {

		fProblem = problem;
		fSourceModule = cu;

		/*
		 * if (JavaSpellingReconcileStrategy.SPELLING_PROBLEM_ID ==
		 * fProblem.getID()) { setType(SPELLING_ANNOTATION_TYPE); fLayer=
		 * WARNING_LAYER; } else
		 */
		if (IProblem.Task == fProblem.getID()) {
			setType(ScriptMarkerAnnotation.TASK_ANNOTATION_TYPE);
			fLayer = TASK_LAYER;
		} else if (fProblem.isWarning()) {
			setType(ScriptMarkerAnnotation.WARNING_ANNOTATION_TYPE);
			fLayer = WARNING_LAYER;
		} else if (fProblem.isError()) {
			setType(ScriptMarkerAnnotation.ERROR_ANNOTATION_TYPE);
			fLayer = ERROR_LAYER;
		} else {
			setType(ScriptMarkerAnnotation.INFO_ANNOTATION_TYPE);
			fLayer = INFO_LAYER;
		}
	}

	/*
	 * @see org.eclipse.jface.text.source.IAnnotationPresentation#getLayer()
	 */
	public int getLayer() {
		return fLayer;
	}

	/**
	 * delayed image loading - to be sure it is called on the UI thread
	 */
	private void initializeImage() {
		if (!fImageInitialized) {
			initializeImages();
			// if (!isQuickFixableStateSet()) {
			// setQuickFixable(isProblem()
			// && ScriptAnnotationUtils.hasCorrections(this));
			// }
			if (isQuickFixable()) {
				if (ScriptMarkerAnnotation.ERROR_ANNOTATION_TYPE
						.equals(getType()))
					fImage = fgQuickFixErrorImage;
				else
					fImage = fgQuickFixImage;
			} else {
				final String type = getType();
				if (ScriptMarkerAnnotation.TASK_ANNOTATION_TYPE
						.equals(type))
					fImage = fgTaskImage;
				else if (ScriptMarkerAnnotation.INFO_ANNOTATION_TYPE
						.equals(type))
					fImage = fgInfoImage;
				else if (ScriptMarkerAnnotation.WARNING_ANNOTATION_TYPE
						.equals(type))
					fImage = fgWarningImage;
				else if (ScriptMarkerAnnotation.ERROR_ANNOTATION_TYPE
						.equals(type))
					fImage = fgErrorImage;
			}
			fImageInitialized = true;
		}
	}

	private static void initializeImages() {
		if (fgImagesInitialized)
			return;

		fgQuickFixImage = PHPPluginImages
				.get(PHPPluginImages.IMG_OBJS_FIXABLE_PROBLEM);
		fgQuickFixErrorImage = PHPPluginImages
				.get(PHPPluginImages.IMG_OBJS_FIXABLE_ERROR);

		final ISharedImages sharedImages = PlatformUI.getWorkbench()
				.getSharedImages();
		fgTaskImage = sharedImages.getImage(IDE.SharedImages.IMG_OBJS_TASK_TSK);
		fgInfoImage = sharedImages
				.getImage(ISharedImages.IMG_OBJS_INFO_TSK);
		fgWarningImage = sharedImages
				.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
		fgErrorImage = sharedImages
				.getImage(ISharedImages.IMG_OBJS_ERROR_TSK);

		fgImagesInitialized = true;
	}

	/*
	 * @see Annotation#paint
	 */
	public void paint(GC gc, Canvas canvas, Rectangle r) {
		initializeImage();
		if (fImage != null)
			ImageUtilities.drawImage(fImage, gc, canvas, r, SWT.CENTER,
					SWT.TOP);
	}

	/*
	 * @see IJavaAnnotation#getImage(Display)
	 */
	public Image getImage(Display display) {
		initializeImage();
		return fImage;
	}

	/*
	 * @see IJavaAnnotation#getMessage()
	 */
	public String getText() {
		String[] arguments = getArguments();
		if (arguments != null) {
			for (int i = 0; i < arguments.length; i++) {
				String ar = arguments[i];
				if (ar.startsWith(IProblem.DESCRIPTION_ARGUMENT_PREFIX)) {
					return fProblem.getMessage()
							+ '\n'
							+ ar
									.substring(IProblem.DESCRIPTION_ARGUMENT_PREFIX
											.length());
				}
			}
		}
		return fProblem.getMessage();
	}

	/*
	 * @see IJavaAnnotation#getArguments()
	 */
	public String[] getArguments() {
		return isProblem() ? fProblem.getArguments() : null;
	}

	/*
	 * @see IJavaAnnotation#getId()
	 */
	public int getId() {
		return fProblem.getID();
	}

	/*
	 * @see IJavaAnnotation#isProblem()
	 */
	public boolean isProblem() {
		String type = getType();
		return ScriptMarkerAnnotation.WARNING_ANNOTATION_TYPE.equals(type)
				|| ScriptMarkerAnnotation.ERROR_ANNOTATION_TYPE
						.equals(type)
				|| SPELLING_ANNOTATION_TYPE.equals(type);
	}

	/*
	 * @see IJavaAnnotation#hasOverlay()
	 */
	public boolean hasOverlay() {
		return false;
	}

	/*
	 * @see
	 * org.eclipse.jdt.internal.ui.javaeditor.IJavaAnnotation#getOverlay()
	 */
	public IScriptAnnotation getOverlay() {
		return null;
	}

	/*
	 * @see IJavaAnnotation#addOverlaid(IJavaAnnotation)
	 */
	public void addOverlaid(IScriptAnnotation annotation) {
		if (fOverlaids == null)
			fOverlaids = new ArrayList<IScriptAnnotation>(1);
		fOverlaids.add(annotation);
	}

	/*
	 * @see IJavaAnnotation#removeOverlaid(IJavaAnnotation)
	 */
	public void removeOverlaid(IScriptAnnotation annotation) {
		if (fOverlaids != null) {
			fOverlaids.remove(annotation);
			if (fOverlaids.size() == 0)
				fOverlaids = null;
		}
	}

	/*
	 * @see IJavaAnnotation#getOverlaidIterator()
	 */
	@SuppressWarnings("rawtypes")
	public Iterator getOverlaidIterator() {
		if (fOverlaids != null)
			return fOverlaids.iterator();
		return null;
	}

	/*
	 * @see
	 * org.eclipse.jdt.internal.ui.javaeditor.IJavaAnnotation#getCompilationUnit
	 * ()
	 */
	public ISourceModule getSourceModule() {
		return fSourceModule;
	}

	public IProblem getProblem() {
		return fProblem;
	}

	/*
	 * @see
	 * org.eclipse.jdt.internal.ui.javaeditor.IJavaAnnotation#getMarkerType
	 * ()
	 */
	public String getMarkerType() {
		if (fProblem instanceof CategorizedProblem)
			return ((CategorizedProblem) fProblem).getMarkerType();
		return null;
	}

	/*
	 * @seeorg.eclipse.jface.text.quickassist.IQuickFixableAnnotation#
	 * setQuickFixable(boolean)
	 * 
	 * @since 3.2
	 */
	public void setQuickFixable(boolean state) {
		fIsQuickFixable = state;
		fIsQuickFixableStateSet = true;
	}

	/*
	 * @seeorg.eclipse.jface.text.quickassist.IQuickFixableAnnotation#
	 * isQuickFixableStateSet()
	 * 
	 * @since 3.2
	 */
	public boolean isQuickFixableStateSet() {
		return fIsQuickFixableStateSet;
	}

	/*
	 * @see
	 * org.eclipse.jface.text.quickassist.IQuickFixableAnnotation#isQuickFixable
	 * ()
	 * 
	 * @since 3.2
	 */
	public boolean isQuickFixable() {
		Assert.isTrue(isQuickFixableStateSet());
		return fIsQuickFixable;
	}
}
