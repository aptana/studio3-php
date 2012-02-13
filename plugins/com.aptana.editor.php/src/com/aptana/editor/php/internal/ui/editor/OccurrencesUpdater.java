package com.aptana.editor.php.internal.ui.editor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ISelectionValidator;
import org.eclipse.jface.text.ISynchronizable;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org2.eclipse.dltk.internal.ui.text.ScriptWordFinder;
import org2.eclipse.php.internal.core.ast.locator.PhpElementConciliator;
import org2.eclipse.php.internal.core.ast.nodes.ASTNode;
import org2.eclipse.php.internal.core.ast.nodes.Expression;
import org2.eclipse.php.internal.core.ast.nodes.Identifier;
import org2.eclipse.php.internal.core.ast.nodes.Program;
import org2.eclipse.php.internal.core.ast.nodes.Variable;
import org2.eclipse.php.internal.core.corext.NodeFinder;
import org2.eclipse.php.internal.core.search.IOccurrencesFinder;
import org2.eclipse.php.internal.core.search.IOccurrencesFinder.OccurrenceLocation;
import org2.eclipse.php.internal.core.search.OccurrencesFinderFactory;
import org2.eclipse.php.internal.ui.preferences.PreferenceConstants;
import org2.eclipse.php.internal.ui.viewsupport.ISelectionListenerWithAST;
import org2.eclipse.php.internal.ui.viewsupport.SelectionListenerWithASTManager;
import org2.eclipse.php.ui.editor.SharedASTProvider;

import com.aptana.core.logging.IdeLog;
import com.aptana.editor.php.Messages;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.core.model.IModelElement;
import com.aptana.editor.php.core.model.ISourceModule;
import com.aptana.editor.php.internal.typebinding.TypeBindingBuilder;
import com.aptana.parsing.IParseState;

/**
 * This class works closely with the {@link PHPSourceEditor} to update the PHP elements occurrences annotations.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
class OccurrencesUpdater implements IPropertyChangeListener
{
	private PHPSourceEditor editor;

	// Occurrences
	private boolean fMarkOccurrenceAnnotations;
	private boolean fStickyOccurrenceAnnotations;
	private boolean fMarkTypeOccurrences;
	private boolean fMarkMethodOccurrences;
	private boolean fMarkFunctionOccurrences;
	private boolean fMarkConstantOccurrences;
	private boolean fMarkGlobalVariableOccurrences;
	private boolean fMarkLocalVariableOccurrences;
	private boolean fMarkImplementors;
	private boolean fMarkMethodExitPoints;
	private boolean fMarkBreakContinueTargets;
	private boolean fMarkExceptions; // TODO - not yet supported
	private ISelection fForcedMarkOccurrencesSelection;
	private Annotation[] fOccurrenceAnnotations;
	private ActivationListener fActivationListener;

	private OccurrencesFinderJob fOccurrencesFinderJob;
	private IRegion fMarkOccurrenceTargetRegion;
	private long fMarkOccurrenceModificationStamp;

	private OccurrencesFinderJobCanceler fOccurrencesFinderJobCanceler;
	private ISelectionListenerWithAST fPostSelectionListenerWithAST;

	/**
	 * Creates a new {@link OccurrencesUpdater} with a given {@link PHPSourceEditor}.
	 * 
	 * @param editor
	 *            A {@link PHPSourceEditor}
	 */
	protected OccurrencesUpdater(PHPSourceEditor editor)
	{
		this.editor = editor;
	}

	protected void initialize(IPreferenceStore store)
	{
		// Setup the Mark Occurrences
		fMarkOccurrenceAnnotations = store
				.getBoolean(com.aptana.editor.common.preferences.IPreferenceConstants.EDITOR_MARK_OCCURRENCES);
		fStickyOccurrenceAnnotations = store.getBoolean(PreferenceConstants.EDITOR_STICKY_OCCURRENCES);
		fMarkTypeOccurrences = store.getBoolean(PreferenceConstants.EDITOR_MARK_TYPE_OCCURRENCES);
		fMarkMethodOccurrences = store.getBoolean(PreferenceConstants.EDITOR_MARK_METHOD_OCCURRENCES);
		fMarkFunctionOccurrences = store.getBoolean(PreferenceConstants.EDITOR_MARK_FUNCTION_OCCURRENCES);
		fMarkConstantOccurrences = store.getBoolean(PreferenceConstants.EDITOR_MARK_CONSTANT_OCCURRENCES);
		fMarkGlobalVariableOccurrences = store.getBoolean(PreferenceConstants.EDITOR_MARK_GLOBAL_VARIABLE_OCCURRENCES);
		fMarkLocalVariableOccurrences = store.getBoolean(PreferenceConstants.EDITOR_MARK_LOCAL_VARIABLE_OCCURRENCES);
		fMarkImplementors = store.getBoolean(PreferenceConstants.EDITOR_MARK_IMPLEMENTORS);
		fMarkMethodExitPoints = store.getBoolean(PreferenceConstants.EDITOR_MARK_METHOD_EXIT_POINTS);
		fMarkBreakContinueTargets = store.getBoolean(PreferenceConstants.EDITOR_MARK_BREAK_CONTINUE_TARGETS);

		fActivationListener = new ActivationListener();
		PlatformUI.getWorkbench().addWindowListener(fActivationListener);

		if (editor.isMarkingOccurrences())
		{
			installOccurrencesFinder(true);
		}

		store.addPropertyChangeListener(this);
	}

	protected void installOccurrencesFinder(boolean forceUpdate)
	{
		fMarkOccurrenceAnnotations = true;

		fPostSelectionListenerWithAST = new ISelectionListenerWithAST()
		{
			public void selectionChanged(IEditorPart part, ITextSelection selection, Program astRoot)
			{
				updateOccurrenceAnnotations(selection, astRoot);
			}
		};
		SelectionListenerWithASTManager.getDefault().addListener(editor, fPostSelectionListenerWithAST);
		if (forceUpdate && editor.getSelectionProvider() != null)
		{
			fForcedMarkOccurrencesSelection = editor.getSelectionProvider().getSelection();
			IModelElement source = editor.getSourceModule();
			if (source != null)
			{
				try
				{
					final Program ast = SharedASTProvider.getAST((ISourceModule) source, SharedASTProvider.WAIT_NO,
							editor.getProgressMonitor());
					updateOccurrenceAnnotations((ITextSelection) fForcedMarkOccurrencesSelection, ast);
				}
				catch (Exception e)
				{
					IdeLog.logError(PHPEditorPlugin.getDefault(), "Error installing the PHP occurrences finder", e); //$NON-NLS-1$
				}
			}
		}

		if (fOccurrencesFinderJobCanceler == null)
		{
			fOccurrencesFinderJobCanceler = new OccurrencesFinderJobCanceler();
			fOccurrencesFinderJobCanceler.install();
		}

		// TODO Do we need some way to hook into reconciling to force an update? Won't typing changed the "selection"
		// anyhow?
	}

	protected void uninstallOccurrencesFinder()
	{
		fMarkOccurrenceAnnotations = false;

		if (fOccurrencesFinderJob != null)
		{
			fOccurrencesFinderJob.cancel();
			fOccurrencesFinderJob = null;
		}

		if (fOccurrencesFinderJobCanceler != null)
		{
			fOccurrencesFinderJobCanceler.uninstall();
			fOccurrencesFinderJobCanceler = null;
		}

		if (fPostSelectionListenerWithAST != null)
		{
			SelectionListenerWithASTManager.getDefault().removeListener(editor, fPostSelectionListenerWithAST);
			fPostSelectionListenerWithAST = null;
		}

		removeOccurrenceAnnotations();
	}

	/**
	 * Dispose this occurrences updater. This should be called when the PHP editor is disposed.
	 */
	protected void dispose()
	{
		if (fActivationListener != null)
		{
			PlatformUI.getWorkbench().removeWindowListener(fActivationListener);
			fActivationListener = null;
		}
		uninstallOccurrencesFinder();
	}

	/**
	 * Updates the occurrences annotations based on the current selection.
	 * 
	 * @param selection
	 *            The text selection
	 * @param ast
	 *            An AST
	 */
	protected void updateOccurrenceAnnotations(ITextSelection selection, Program ast)
	{
		if (fOccurrencesFinderJob != null)
			fOccurrencesFinderJob.cancel();

		if (!fMarkOccurrenceAnnotations)
			return;

		if (ast == null || selection == null)
			return;
		if (!ast.isBindingCompleted())
		{
			TypeBindingBuilder.buildBindings(ast);
		}
		IDocument document = editor.getISourceViewer().getDocument();
		if (document == null)
			return;

		// TODO: Shalom - Replace this to do a real check whether this script was already reconciled
		if (document.getLength() != ast.getEnd())
		{
			return;
		}

		boolean hasChanged = false;
		if (document instanceof IDocumentExtension4)
		{
			int offset = selection.getOffset();
			long currentModificationStamp = ((IDocumentExtension4) document).getModificationStamp();
			IRegion markOccurrenceTargetRegion = fMarkOccurrenceTargetRegion;
			hasChanged = currentModificationStamp != fMarkOccurrenceModificationStamp;
			if (markOccurrenceTargetRegion != null && !hasChanged)
			{
				if (markOccurrenceTargetRegion.getOffset() <= offset
						&& offset <= markOccurrenceTargetRegion.getOffset() + markOccurrenceTargetRegion.getLength())
					return;
			}
			fMarkOccurrenceTargetRegion = ScriptWordFinder.findWord(document, offset);
			fMarkOccurrenceModificationStamp = currentModificationStamp;
		}

		OccurrenceLocation[] locations = null;

		ASTNode selectedNode = NodeFinder.perform(ast, selection.getOffset(), selection.getLength());

		// if (locations == null && fMarkExceptions)
		// {
		// TODO: Shalom - Implement
		// }

		if (locations == null && fMarkMethodExitPoints)
		{
			IOccurrencesFinder finder = OccurrencesFinderFactory.createMethodExitsFinder();
			if (finder.initialize(ast, selectedNode) == null)
			{
				locations = finder.getOccurrences();
			}
		}

		if (locations == null && fMarkImplementors)
		{
			IOccurrencesFinder finder = OccurrencesFinderFactory.createIncludeFinder();
			if (finder.initialize(ast, selectedNode) == null)
			{
				locations = finder.getOccurrences();
			}
		}

		if (locations == null && fMarkBreakContinueTargets)
		{
			IOccurrencesFinder finder = OccurrencesFinderFactory.createBreakContinueTargetFinder();
			if (finder.initialize(ast, selectedNode) == null)
			{
				locations = finder.getOccurrences();
			}
		}

		if (locations == null && fMarkImplementors)
		{
			IOccurrencesFinder finder = OccurrencesFinderFactory.createImplementorsOccurrencesFinder();
			if (finder.initialize(ast, selectedNode) == null)
			{
				locations = finder.getOccurrences();
			}
		}

		if (selectedNode != null && selectedNode.getType() == ASTNode.VARIABLE)
		{
			final Expression name = ((Variable) selectedNode).getName();
			if (name instanceof Identifier)
			{
				selectedNode = name;
			}
		}

		if (locations == null && selectedNode != null
				&& (selectedNode instanceof Identifier || (isNonStringScalar(selectedNode))))
		{
			int type = PhpElementConciliator.concile(selectedNode);
			if (isMarkingOccurrencesFor(type))
			{
				IOccurrencesFinder finder = OccurrencesFinderFactory.getOccurrencesFinder(type);
				if (finder != null)
				{
					if (finder.initialize(ast, selectedNode) == null)
					{
						locations = finder.getOccurrences();
					}
				}
			}
		}

		if (locations == null)
		{
			if (!fStickyOccurrenceAnnotations)
			{
				removeOccurrenceAnnotations();
			}
			else if (hasChanged) // check consistency of current annotations
			{
				removeOccurrenceAnnotations();
			}
			return;
		}

		fOccurrencesFinderJob = new OccurrencesFinderJob(document, locations, selection);
		fOccurrencesFinderJob.setPriority(Job.DECORATE);
		fOccurrencesFinderJob.schedule();
	}

	/*
	 * Returns the lock object or <code>null</code> if there is none. Clients should use the lock object in order to
	 * synchronize concurrent access to the implementer.
	 * @return the lock object or the annotationModel itself
	 * @see ISynchronizable#getLockObject()
	 */
	private Object getAnnotationModelLock(IAnnotationModel annotationModel)
	{
		if (annotationModel instanceof ISynchronizable)
		{
			Object lock = ((ISynchronizable) annotationModel).getLockObject();
			if (lock != null)
				return lock;
		}
		return annotationModel;
	}

	/**
	 * Returns true iff the given node is a scalar that is not inside a string.
	 * 
	 * @param node
	 *            {@link ASTNode}
	 */
	private boolean isNonStringScalar(ASTNode node)
	{
		return (node.getType() == ASTNode.SCALAR) && (node.getParent().getType() != ASTNode.QUOTE);
	}

	/**
	 * Returns true if the given type of occurrence should be marked; False, otherwise.
	 * 
	 * @param type
	 *            A {@link PhpElementConciliator} type.
	 * @return True if the given type of occurrence should be marked; False, otherwise.
	 */
	private boolean isMarkingOccurrencesFor(int type)
	{
		switch (type)
		{
			case PhpElementConciliator.CONCILIATOR_GLOBAL_VARIABLE:
				return fMarkGlobalVariableOccurrences;
			case PhpElementConciliator.CONCILIATOR_LOCAL_VARIABLE:
				return fMarkLocalVariableOccurrences;
			case PhpElementConciliator.CONCILIATOR_FUNCTION:
				return fMarkFunctionOccurrences;
			case PhpElementConciliator.CONCILIATOR_CLASSNAME:
				return fMarkTypeOccurrences;
			case PhpElementConciliator.CONCILIATOR_CONSTANT:
				return fMarkConstantOccurrences;
			case PhpElementConciliator.CONCILIATOR_CLASS_MEMBER:
				return fMarkMethodOccurrences;
			case PhpElementConciliator.CONCILIATOR_UNKNOWN:
			case PhpElementConciliator.CONCILIATOR_PROGRAM:
			default:
				return false;
		}
	}

	void removeOccurrenceAnnotations()
	{
		fMarkOccurrenceModificationStamp = IDocumentExtension4.UNKNOWN_MODIFICATION_STAMP;
		fMarkOccurrenceTargetRegion = null;

		IDocumentProvider documentProvider = editor.getDocumentProvider();
		if (documentProvider == null)
			return;

		IAnnotationModel annotationModel = documentProvider.getAnnotationModel(editor.getEditorInput());
		if (annotationModel == null || fOccurrenceAnnotations == null)
			return;

		synchronized (getAnnotationModelLock(annotationModel))
		{
			if (annotationModel instanceof IAnnotationModelExtension)
			{
				((IAnnotationModelExtension) annotationModel).replaceAnnotations(fOccurrenceAnnotations, null);
			}
			else
			{
				for (int i = 0, length = fOccurrenceAnnotations.length; i < length; i++)
					annotationModel.removeAnnotation(fOccurrenceAnnotations[i]);
			}
			fOccurrenceAnnotations = null;
		}
	}

	/**
	 * Reacts to any changes in the properties for the Mark Occurrences
	 */
	public void propertyChange(PropertyChangeEvent event)
	{
		final String property = event.getProperty();
		boolean newBooleanValue = false;
		Object newValue = event.getNewValue();
		if (newValue != null)
		{
			newBooleanValue = Boolean.valueOf(newValue.toString()).booleanValue();
		}
		if (com.aptana.editor.common.preferences.IPreferenceConstants.EDITOR_MARK_OCCURRENCES.equals(property))
		{
			if (newBooleanValue != fMarkOccurrenceAnnotations)
			{
				fMarkOccurrenceAnnotations = newBooleanValue;
				if (!fMarkOccurrenceAnnotations)
				{
					uninstallOccurrencesFinder();
				}
				else
				{
					installOccurrencesFinder(true);
				}
			}
			return;
		}
		if (PreferenceConstants.EDITOR_MARK_TYPE_OCCURRENCES.equals(property))
		{
			fMarkTypeOccurrences = newBooleanValue;
			return;
		}
		if (PreferenceConstants.EDITOR_MARK_METHOD_OCCURRENCES.equals(property))
		{
			fMarkMethodOccurrences = newBooleanValue;
			return;
		}
		if (PreferenceConstants.EDITOR_MARK_FUNCTION_OCCURRENCES.equals(property))
		{
			fMarkFunctionOccurrences = newBooleanValue;
			return;
		}
		if (PreferenceConstants.EDITOR_MARK_CONSTANT_OCCURRENCES.equals(property))
		{
			fMarkConstantOccurrences = newBooleanValue;
			return;
		}
		if (PreferenceConstants.EDITOR_MARK_GLOBAL_VARIABLE_OCCURRENCES.equals(property))
		{
			fMarkGlobalVariableOccurrences = newBooleanValue;
			return;
		}
		if (PreferenceConstants.EDITOR_MARK_LOCAL_VARIABLE_OCCURRENCES.equals(property))
		{
			fMarkLocalVariableOccurrences = newBooleanValue;
			return;
		}
		if (PreferenceConstants.EDITOR_MARK_METHOD_EXIT_POINTS.equals(property))
		{
			fMarkMethodExitPoints = newBooleanValue;
			return;
		}
		if (PreferenceConstants.EDITOR_MARK_BREAK_CONTINUE_TARGETS.equals(property))
		{
			fMarkBreakContinueTargets = newBooleanValue;
			return;
		}
		if (PreferenceConstants.EDITOR_MARK_IMPLEMENTORS.equals(property))
		{
			fMarkImplementors = newBooleanValue;
			return;
		}
		if (PreferenceConstants.EDITOR_STICKY_OCCURRENCES.equals(property))
		{
			fStickyOccurrenceAnnotations = newBooleanValue;
			return;
		}
	}

	/**
	 * Internal activation listener.
	 * 
	 * @since 3.0
	 */
	private class ActivationListener implements IWindowListener
	{

		/*
		 * @seeorg.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui. IWorkbenchWindow)
		 * @since 3.1
		 */
		public void windowActivated(IWorkbenchWindow window)
		{
			if (window == editor.getEditorSite().getWorkbenchWindow() && fMarkOccurrenceAnnotations
					&& editor.isActiveEditor())
			{
				fForcedMarkOccurrencesSelection = editor.getSelectionProvider().getSelection();
				IModelElement sourceModule = editor.getSourceModule();
				if (sourceModule != null && sourceModule.getElementType() == IModelElement.MODULE)
				{
					try
					{
						updateOccurrenceAnnotations((ITextSelection) fForcedMarkOccurrencesSelection,
								SharedASTProvider.getAST((ISourceModule) sourceModule, SharedASTProvider.WAIT_NO,
										editor.getProgressMonitor()));
					}
					catch (Exception e)
					{
						IdeLog.logError(PHPEditorPlugin.getDefault(), "PHP code-scanner - Update error", e); //$NON-NLS-1$
					}
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.IWorkbenchWindow)
		 */
		public void windowDeactivated(IWorkbenchWindow window)
		{
			if (window == editor.getEditorSite().getWorkbenchWindow() && fMarkOccurrenceAnnotations
					&& editor.isActiveEditor())
			{
				removeOccurrenceAnnotations();
			}
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow)
		 */
		public void windowClosed(IWorkbenchWindow window)
		{
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
		 */
		public void windowOpened(IWorkbenchWindow window)
		{
		}
	}

	/**
	 * Cancels the occurrences finder job upon document changes.
	 * 
	 * @since 3.0
	 */
	class OccurrencesFinderJobCanceler implements IDocumentListener, ITextInputListener
	{

		public void install()
		{
			ISourceViewer sourceViewer = editor.getISourceViewer();
			if (sourceViewer == null)
				return;

			StyledText text = sourceViewer.getTextWidget();
			if (text == null || text.isDisposed())
				return;

			sourceViewer.addTextInputListener(this);

			IDocument document = sourceViewer.getDocument();
			if (document != null)
				document.addDocumentListener(this);
		}

		public void uninstall()
		{
			ISourceViewer sourceViewer = editor.getISourceViewer();
			if (sourceViewer != null)
				sourceViewer.removeTextInputListener(this);

			IDocumentProvider documentProvider = editor.getDocumentProvider();
			if (documentProvider != null)
			{
				IDocument document = documentProvider.getDocument(editor.getEditorInput());
				if (document != null)
					document.removeDocumentListener(this);
			}
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
		 */
		public void documentAboutToBeChanged(DocumentEvent event)
		{
			if (fOccurrencesFinderJob != null)
				fOccurrencesFinderJob.doCancel();
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
		 */
		public void documentChanged(DocumentEvent event)
		{
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.eclipse.jface.text.ITextInputListener#inputDocumentAboutToBeChanged(org.eclipse.jface.text.IDocument,
		 * org.eclipse.jface.text.IDocument)
		 */
		public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput)
		{
			if (oldInput == null)
				return;

			oldInput.removeDocumentListener(this);
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.text.ITextInputListener#inputDocumentChanged(org.eclipse.jface.text.IDocument,
		 * org.eclipse.jface.text.IDocument)
		 */
		public void inputDocumentChanged(IDocument oldInput, IDocument newInput)
		{
			if (newInput == null)
				return;
			newInput.addDocumentListener(this);
		}
	}

	/**
	 * Finds and marks occurrence annotations.
	 * 
	 * @since 3.0
	 */
	class OccurrencesFinderJob extends Job
	{

		protected static final String WRITE_OCCURRENCE_ID = "com.aptana.php.ui.occurrences.write"; //$NON-NLS-1$
		protected static final String READ_OCCURRENCE_ID = "com.aptana.php.ui.occurrences"; //$NON-NLS-1$

		private final IDocument fDocument;
		private final ISelection fSelection;
		private final ISelectionValidator fPostSelectionValidator;
		private boolean fCanceled = false;
		private final OccurrenceLocation[] fLocations;

		protected OccurrencesFinderJob(IDocument document, OccurrenceLocation[] locations, ISelection selection)
		{
			super(Messages.PHPSourceEditor_markOccurrencesJob_name);
			fDocument = document;
			fSelection = selection;
			fLocations = locations;

			ISelectionProvider selectionProvider = editor.getSelectionProvider();
			if (selectionProvider instanceof ISelectionValidator)
			{
				fPostSelectionValidator = (ISelectionValidator) selectionProvider;
			}
			else
			{
				fPostSelectionValidator = null;
			}
		}

		void doCancel()
		{
			fCanceled = true;
			cancel();
		}

		private boolean isCanceled(IProgressMonitor progressMonitor)
		{
			return fCanceled || progressMonitor.isCanceled() || fPostSelectionValidator != null
					&& !(fPostSelectionValidator.isValid(fSelection) || fForcedMarkOccurrencesSelection == fSelection)
					|| LinkedModeModel.hasInstalledModel(fDocument);
		}

		/*
		 * @see Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		public IStatus run(IProgressMonitor progressMonitor)
		{
			if (isCanceled(progressMonitor))
			{
				return Status.CANCEL_STATUS;
			}

			ITextViewer textViewer = editor.getISourceViewer();
			if (textViewer == null)
			{
				return Status.CANCEL_STATUS;
			}

			IDocument document = textViewer.getDocument();
			if (document == null)
			{
				return Status.CANCEL_STATUS;
			}

			IDocumentProvider documentProvider = editor.getDocumentProvider();
			if (documentProvider == null)
			{
				return Status.CANCEL_STATUS;
			}

			IAnnotationModel annotationModel = documentProvider.getAnnotationModel(editor.getEditorInput());
			if (annotationModel == null)
			{
				return Status.CANCEL_STATUS;
			}

			int length = fLocations.length;
			Map<Annotation, Position> annotationMap = new HashMap<Annotation, Position>(length);
			for (int i = 0; i < length; i++)
			{

				if (isCanceled(progressMonitor))
				{
					return Status.CANCEL_STATUS;
				}

				OccurrenceLocation occurrence = fLocations[i];
				Position position = new Position(occurrence.getOffset(), occurrence.getLength());

				String description = occurrence.getDescription();
				String annotationType = (occurrence.getFlags() == IOccurrencesFinder.F_WRITE_OCCURRENCE) ? WRITE_OCCURRENCE_ID
						: READ_OCCURRENCE_ID;

				/*
				 * // create an annotation to mark the occurrence ReconcileAnnotationKey reconcileAnnotationKey = new
				 * ReconcileAnnotationKey(null, PHPPartitionTypes.PHP_DEFAULT, ReconcileAnnotationKey.TOTAL);
				 * TemporaryAnnotation annotation = new TemporaryAnnotation(position, annotationType, description,
				 * reconcileAnnotationKey) {
				 *//**
				 * Paint the ruler annotation.
				 * 
				 * @param gc
				 * @param canvas
				 * @param r
				 */
				/*
				 * @Override public void paint(GC gc, Canvas canvas, Rectangle r) { // TODO - Shalom: Change this image
				 * location ImageUtilities.drawImage(PHPUiPlugin.getImageDescriptorRegistry().get(
				 * PHPPluginImages.DESC_OBJS_OCCURRENCES), gc, canvas, r, SWT.CENTER, SWT.TOP); } };
				 */
				Annotation annotation = new Annotation(annotationType, false, description);
				annotationMap.put(annotation, position);
			}

			if (isCanceled(progressMonitor))
				return Status.CANCEL_STATUS;

			synchronized (getAnnotationModelLock(annotationModel))
			{
				if (annotationModel instanceof IAnnotationModelExtension)
				{
					((IAnnotationModelExtension) annotationModel).replaceAnnotations(fOccurrenceAnnotations,
							annotationMap);
				}
				else
				{
					removeOccurrenceAnnotations();
					for (Map.Entry<Annotation, Position> entry : annotationMap.entrySet())
					{
						annotationModel.addAnnotation(entry.getKey(), entry.getValue());
					}
				}
				fOccurrenceAnnotations = annotationMap.keySet().toArray(new Annotation[annotationMap.keySet().size()]);
			}

			return Status.OK_STATUS;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.outline.IParseListener#parseAboutToStart(com.aptana.parsing.IParseState)
	 */
	public void beforeParse(IParseState parseState)
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.outline.IParseListener#afterParse(com.aptana.parsing.IParseState)
	 */
	public void afterParse(IParseState parseState)
	{
		// TODO Auto-generated method stub

	}
}
