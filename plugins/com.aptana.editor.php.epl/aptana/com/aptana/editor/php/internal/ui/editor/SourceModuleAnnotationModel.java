package com.aptana.editor.php.internal.ui.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationModelEvent;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.eclipse.ui.texteditor.ResourceMarkerAnnotationModel;
import org2.eclipse.dltk.compiler.problem.IProblem;

import com.aptana.editor.php.core.model.ISourceModule;

/**
 * Annotation model dealing with PHP marker annotations and temporary
 * problems.
 * Initially inactive. Must explicitly be activated.
 */
public class SourceModuleAnnotationModel extends
		ResourceMarkerAnnotationModel  {

	private static class ProblemRequestorState {
		boolean fInsideReportingSequence = false;
		List<IProblem> fReportedProblems;
	}

	private ThreadLocal<ProblemRequestorState> fProblemRequestorState = new ThreadLocal<ProblemRequestorState>();
	private int fStateCount = 0;

	private ISourceModule fSourceModule;
	private List<ProblemAnnotation> fGeneratedAnnotations = new ArrayList<ProblemAnnotation>();
	private IProgressMonitor fProgressMonitor;
	private boolean fIsActive = false;
	private boolean fIsHandlingTemporaryProblems;

	private ReverseMap fReverseMap = new ReverseMap();
	private List<ScriptMarkerAnnotation> fPreviouslyOverlaid = null;
	private List<ScriptMarkerAnnotation> fCurrentlyOverlaid = new ArrayList<ScriptMarkerAnnotation>();

	public SourceModuleAnnotationModel(IResource resource) {
		super(resource);
	}

	public void setSourceModule(ISourceModule unit) {
		fSourceModule = unit;
	}

	protected MarkerAnnotation createMarkerAnnotation(IMarker marker) {
		String markerType = MarkerUtilities.getMarkerType(marker);
		if (markerType != null
				&& markerType
						.startsWith(ScriptMarkerAnnotation.DLTK_MARKER_TYPE_PREFIX))
			return new ScriptMarkerAnnotation(marker);
		return super.createMarkerAnnotation(marker);
	}

	/*
	 * @see
	 * org.eclipse.jface.text.source.AnnotationModel#createAnnotationModelEvent
	 * ()
	 */
	protected AnnotationModelEvent createAnnotationModelEvent() {
		return new SourceModuleAnnotationModelEvent(this, getResource());
	}

	protected Position createPositionFromProblem(IProblem problem) {
		int start = problem.getSourceStart();
		if (start < 0)
			return null;
		int end = problem.getSourceEnd();
		if (end == 0 && start == 0) {
			return new Position(0, 0);
		}
		int length = end - start;
		if (length < 0)
			return null;

		return new Position(start, length);
	}

	/*
	 * @see IProblemRequestor#beginReporting()
	 */
	public void beginReporting() {
		ProblemRequestorState state = fProblemRequestorState.get();
		if (state == null)
			internalBeginReporting(false);
	}

	/*
	 * @see
	 * org.eclipse.jdt.internal.ui.text.java.IProblemRequestorExtension#
	 * beginReportingSequence()
	 */
	public void beginReportingSequence() {
		ProblemRequestorState state = fProblemRequestorState.get();
		if (state == null)
			internalBeginReporting(true);
	}

	/**
	 * Sets up the infrastructure necessary for problem reporting.
	 * 
	 * @param insideReportingSequence
	 *            <code>true</code> if this method call is issued from
	 *            inside a reporting sequence
	 */
	private void internalBeginReporting(boolean insideReportingSequence) {

		// the same behavior as in
		// AbstractSourceModule.getAccumulatingProblemReporter
		// It's possible that there is no script nature set, but the project
		// is ExtenalScriptProject,
		// that determined by it's name
		if (fSourceModule != null
				/*XXX - Aptana Mod: For now we just let everything go through
				&& (ExternalScriptProject.EXTERNAL_PROJECT_NAME
						.equals(fSourceModule.getScriptProject()
								.getElementName()) || fSourceModule
						.getScriptProject().isOnBuildpath(fSourceModule))*/) {
			ProblemRequestorState state = new ProblemRequestorState();
			state.fInsideReportingSequence = insideReportingSequence;
			state.fReportedProblems = new ArrayList<IProblem>();
			synchronized (getLockObject()) {
				fProblemRequestorState.set(state);
				++fStateCount;
			}
		}
	}

	/*
	 * @see IProblemRequestor#acceptProblem(IProblem)
	 */
	public void acceptProblem(IProblem problem) {
		if (fIsHandlingTemporaryProblems) {
			/*
			 * || problem.getID() == JavaSpellingReconcileStrategy
			 * .SPELLING_PROBLEM_ID
			 */
			ProblemRequestorState state = fProblemRequestorState.get();
			if (state != null)
				state.fReportedProblems.add(problem);
		}
	}

	/*
	 * @see IProblemRequestor#endReporting()
	 */
	public void endReporting() {
		ProblemRequestorState state = fProblemRequestorState.get();
		if (state != null && !state.fInsideReportingSequence)
			internalEndReporting(state);
	}

	/*
	 * @see
	 * org.eclipse.jdt.internal.ui.text.java.IProblemRequestorExtension#
	 * endReportingSequence()
	 */
	public void endReportingSequence() {
		ProblemRequestorState state = fProblemRequestorState.get();
		if (state != null && state.fInsideReportingSequence)
			internalEndReporting(state);
	}

	private void internalEndReporting(ProblemRequestorState state) {
		int stateCount = 0;
		synchronized (getLockObject()) {
			--fStateCount;
			stateCount = fStateCount;
			fProblemRequestorState.set(null);
		}

		if (stateCount == 0)
			reportProblems(state.fReportedProblems);
	}

	/**
	 * Signals the end of problem reporting.
	 */
	private void reportProblems(List<IProblem> reportedProblems) {
		if (fProgressMonitor != null && fProgressMonitor.isCanceled())
			return;

		boolean temporaryProblemsChanged = false;

		synchronized (getLockObject()) {

			boolean isCanceled = false;

			fPreviouslyOverlaid = fCurrentlyOverlaid;
			fCurrentlyOverlaid = new ArrayList<ScriptMarkerAnnotation>();

			if (fGeneratedAnnotations.size() > 0) {
				temporaryProblemsChanged = true;
				removeAnnotations(fGeneratedAnnotations, false, true);
				fGeneratedAnnotations.clear();
			}

			if (reportedProblems != null && reportedProblems.size() > 0) {

				Iterator<IProblem> e = reportedProblems.iterator();
				while (e.hasNext()) {

					if (fProgressMonitor != null
							&& fProgressMonitor.isCanceled()) {
						isCanceled = true;
						break;
					}

					IProblem problem = e.next();
					Position position = createPositionFromProblem(problem);
					if (position != null) {

						try {
							ProblemAnnotation annotation = new ProblemAnnotation(
									problem, fSourceModule);
							overlayMarkers(position, annotation);
							addAnnotation(annotation, position, false);
							fGeneratedAnnotations.add(annotation);

							temporaryProblemsChanged = true;
						} catch (BadLocationException x) {
							// ignore invalid position
						}
					}
				}
			}

			removeMarkerOverlays(isCanceled);
			fPreviouslyOverlaid = null;
		}

		if (temporaryProblemsChanged)
			fireModelChanged();
	}

	private void removeMarkerOverlays(boolean isCanceled) {
		if (isCanceled) {
			fCurrentlyOverlaid.addAll(fPreviouslyOverlaid);
		} else if (fPreviouslyOverlaid != null) {
			for (ScriptMarkerAnnotation annotation : fPreviouslyOverlaid) {
				annotation.setOverlay(null);
			}
		}
	}

	/**
	 * Overlays value with problem annotation.
	 * 
	 * @param problemAnnotation
	 */
	private void setOverlay(Object value,
			ProblemAnnotation problemAnnotation) {
		if (value instanceof ScriptMarkerAnnotation) {
			ScriptMarkerAnnotation annotation = (ScriptMarkerAnnotation) value;
			if (annotation.isProblem()) {
				annotation.setOverlay(problemAnnotation);
				fPreviouslyOverlaid.remove(annotation);
				fCurrentlyOverlaid.add(annotation);
			}
		} else {
		}
	}

	private void overlayMarkers(Position position,
			ProblemAnnotation problemAnnotation) {
		Object value = getAnnotations(position);
		if (value instanceof List<?>) {
			List<?> list = (List<?>) value;
			for (Iterator<?> e = list.iterator(); e.hasNext();)
				setOverlay(e.next(), problemAnnotation);
		} else {
			setOverlay(value, problemAnnotation);
		}
	}

	/**
	 * Tells this annotation model to collect temporary problems from now
	 * on.
	 */
	private void startCollectingProblems() {
		fGeneratedAnnotations.clear();
	}

	/**
	 * Tells this annotation model to no longer collect temporary problems.
	 */
	private void stopCollectingProblems() {
		if (fGeneratedAnnotations != null)
			removeAnnotations(fGeneratedAnnotations, true, true);
		fGeneratedAnnotations.clear();
	}

	/*
	 * @see IProblemRequestor#isActive()
	 */
	public boolean isActive() {
		return fIsActive;
	}

	/*
	 * @see IProblemRequestorExtension#setProgressMonitor(IProgressMonitor)
	 */
	public void setProgressMonitor(IProgressMonitor monitor) {
		fProgressMonitor = monitor;
	}

	/*
	 * @see IProblemRequestorExtension#setIsActive(boolean)
	 */
	public void setIsActive(boolean isActive) {
		fIsActive = isActive;
	}

	/*
	 * @see
	 * IProblemRequestorExtension#setIsHandlingTemporaryProblems(boolean)
	 * 
	 * @since 3.1
	 */
	public void setIsHandlingTemporaryProblems(boolean enable) {
		if (fIsHandlingTemporaryProblems != enable) {
			fIsHandlingTemporaryProblems = enable;
			if (fIsHandlingTemporaryProblems)
				startCollectingProblems();
			else
				stopCollectingProblems();
		}

	}

	private Object getAnnotations(Position position) {
		synchronized (getLockObject()) {
			return fReverseMap.get(position);
		}
	}

	/*
	 * @see AnnotationModel#addAnnotation(Annotation, Position, boolean)
	 */
	protected void addAnnotation(Annotation annotation, Position position,
			boolean fireModelChanged) throws BadLocationException {
		super.addAnnotation(annotation, position, fireModelChanged);

		synchronized (getLockObject()) {
			Object cached = fReverseMap.get(position);
			if (cached == null)
				fReverseMap.put(position, annotation);
			else if (cached instanceof List<?>) {
				@SuppressWarnings("unchecked")
				List<Annotation> list = (List<Annotation>) cached;
				list.add(annotation);
			} else if (cached instanceof Annotation) {
				List<Annotation> list = new ArrayList<Annotation>(2);
				list.add((Annotation) cached);
				list.add(annotation);
				fReverseMap.put(position, list);
			}
		}
	}

	/*
	 * @see AnnotationModel#removeAllAnnotations(boolean)
	 */
	protected void removeAllAnnotations(boolean fireModelChanged) {
		super.removeAllAnnotations(fireModelChanged);
		synchronized (getLockObject()) {
			fReverseMap.clear();
		}
	}

	/*
	 * @see AnnotationModel#removeAnnotation(Annotation, boolean)
	 */
	protected void removeAnnotation(Annotation annotation,
			boolean fireModelChanged) {
		Position position = getPosition(annotation);
		synchronized (getLockObject()) {
			Object cached = fReverseMap.get(position);
			if (cached instanceof List<?>) {
				List<?> list = (List<?>) cached;
				list.remove(annotation);
				if (list.size() == 1) {
					fReverseMap.put(position, list.get(0));
					list.clear();
				}
			} else if (cached instanceof Annotation) {
				fReverseMap.remove(position);
			}
		}
		super.removeAnnotation(annotation, fireModelChanged);
	}
	
	/**
	 * Internal structure for mapping positions to some value. The reason for
	 * this specific structure is that positions can change over time. Thus a
	 * lookup is based on value and not on hash value.
	 */
	protected static class ReverseMap {

		static class Entry {
			Position fPosition;
			Object fValue;
		}

		private List<Entry> fList = new ArrayList<Entry>(2);
		private int fAnchor = 0;

		public ReverseMap() {
		}

		public Object get(Position position) {

			Entry entry;

			// behind anchor
			int length = fList.size();
			for (int i = fAnchor; i < length; i++) {
				entry = fList.get(i);
				if (entry.fPosition.equals(position)) {
					fAnchor = i;
					return entry.fValue;
				}
			}

			// before anchor
			for (int i = 0; i < fAnchor; i++) {
				entry = fList.get(i);
				if (entry.fPosition.equals(position)) {
					fAnchor = i;
					return entry.fValue;
				}
			}

			return null;
		}

		private int getIndex(Position position) {
			Entry entry;
			int length = fList.size();
			for (int i = 0; i < length; i++) {
				entry = fList.get(i);
				if (entry.fPosition.equals(position))
					return i;
			}
			return -1;
		}

		public void put(Position position, Object value) {
			int index = getIndex(position);
			if (index == -1) {
				Entry entry = new Entry();
				entry.fPosition = position;
				entry.fValue = value;
				fList.add(entry);
			} else {
				Entry entry = fList.get(index);
				entry.fValue = value;
			}
		}

		public void remove(Position position) {
			int index = getIndex(position);
			if (index > -1)
				fList.remove(index);
		}

		public void clear() {
			fList.clear();
		}
	}
}
