package com.aptana.editor.php.internal.ui.wizard;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.internal.resources.ResourceStatus;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.dialogs.WizardNewProjectReferencePage;
import org.eclipse.ui.ide.undo.CreateProjectOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.statushandlers.IStatusAdapterConstants;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.core.PHPNature;
import com.aptana.editor.php.internal.ui.preferences.IPhpPreferenceConstants;

/**
 * @author Shalom Gibly <sgibly@aptana.com>
 */
@SuppressWarnings("restriction")
public class NewPHPProjectWizard extends BasicNewResourceWizard implements IExecutableExtension
{

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	public static final String ID = "com.aptana.editor.php.NewPHPProjectWizard"; //$NON-NLS-1$
	private static final String PHP_PROJ_IMAGE_PATH = "/icons/full/wizban/new_project.gif"; //$NON-NLS-1$

	private IFile file;
	private IFile initialPhpFile;
	private WizardNewProjectReferencePage referencePage;
	private PHPWizardNewProjectCreationPage projectPage;
	private IProject newProject;
	private IConfigurationElement configElement;

	public NewPHPProjectWizard()
	{
		IDialogSettings workbenchSettings = PHPEditorPlugin.getDefault().getDialogSettings();
		IDialogSettings section = workbenchSettings.getSection("BasicNewProjectResourceWizard");//$NON-NLS-1$
		if (section == null)
		{
			section = workbenchSettings.addNewSection("BasicNewProjectResourceWizard");//$NON-NLS-1$
		}
		setDialogSettings(section);
	}

	/**
	 * @see org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard#addPages()
	 */
	public void addPages()
	{
		projectPage = new PHPWizardNewProjectCreationPage("phpProjectPage"); //$NON-NLS-1$
		projectPage.setTitle(Messages.NewPHPProjectWizard_projectWizardTitle);
		projectPage.setDescription(Messages.NewPHPProjectWizard_projectWizardDescription);
		projectPage.setWizard(this);
		projectPage.setPageComplete(false);
		this.addPage(projectPage);
		addExtensionPages();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.wizards.newresource.BasicNewResourceWizard#initializeDefaultPageImageDescriptor()
	 */
	protected void initializeDefaultPageImageDescriptor()
	{
		ImageDescriptor desc = PHPEditorPlugin.getImageDescriptor(PHP_PROJ_IMAGE_PATH);
		setDefaultPageImageDescriptor(desc);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.ide.core.ui.wizards.BaseWizard#addExtensionPages()
	 */
	protected void addExtensionPages()
	{
		// only add page if there are already projects in the workspace
		/*
		 * TODO: Shalom - Uncomment this block once we have the PHP builder in place. if
		 * (ResourcesPlugin.getWorkspace().getRoot().getProjects().length > 0) { referencePage = new
		 * WizardNewProjectReferencePage("basicReferenceProjectPage");//$NON-NLS-1$ referencePage.setTitle("");
		 * //$NON-NLS-1$ referencePage.setDescription(""); //$NON-NLS-1$ this.addPage(referencePage); }
		 */
	}

	/**
	 * We will initialize file contents with a sample text.
	 */
	private InputStream openContentStream()
	{
		String contents = getInitialFileContents();
		if (contents == null)
		{
			contents = EMPTY_STRING;
		}
		return new ByteArrayInputStream(contents.getBytes());
	}

	/**
	 * @return String
	 */
	protected String getInitialFileContents()
	{
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		IPreferenceStore store = PHPEditorPlugin.getDefault().getPreferenceStore();
		String contents = store.getString(IPhpPreferenceConstants.PHPEDITOR_INITIAL_CONTENTS);
		pw.println(contents);
		pw.close();
		return sw.toString();
	}

	/**
	 * Creates a new project resource with the selected name.
	 * <p>
	 * In normal usage, this method is invoked after the user has pressed Finish on the wizard; the enablement of the
	 * Finish button implies that all controls on the pages currently contain valid values.
	 * </p>
	 * <p>
	 * Note that this wizard caches the new project once it has been successfully created; subsequent invocations of
	 * this method will answer the same project resource without attempting to create it again.
	 * </p>
	 * 
	 * @return the created project resource, or <code>null</code> if the project was not created
	 */
	private IProject createNewProject()
	{
		// HACK I have to query for this here, because otherwise when we generate the project somehow the fields get
		// focus and that auto changes the radio selection value for generation
		if (newProject != null)
		{
			return newProject;
		}

		// get a project handle
		final IProject newProjectHandle = projectPage.getProjectHandle();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProjectDescription description = workspace.newProjectDescription(newProjectHandle.getName());
		description.setNatureIds(new String[] { PHPNature.NATURE_ID });

		if (!doCreateProject(description, newProjectHandle))
		{
			return null;
		}
		newProject = newProjectHandle;

		return newProject;
	}

	private boolean doCreateProject(final IProjectDescription description, final IProject newProjectHandle)
	{
		// create the new project operation
		IRunnableWithProgress op = new IRunnableWithProgress()
		{
			public void run(IProgressMonitor monitor) throws InvocationTargetException
			{
				CreateProjectOperation op = new CreateProjectOperation(description,
						Messages.NewPHPProjectWizard_projectWizardTitle);
				try
				{
					// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=219901
					// directly execute the operation so that the undo state is
					// not preserved. Making this undoable resulted in too many
					// accidental file deletions.
					op.execute(monitor, WorkspaceUndoUtil.getUIInfoAdapter(getShell()));
				}
				catch (ExecutionException e)
				{
					throw new InvocationTargetException(e);
				}
			}
		};

		// run the new project creation operation
		try
		{
			getContainer().run(true, true, op);
		}
		catch (InterruptedException e)
		{
			return false;
		}
		catch (InvocationTargetException e)
		{
			Throwable t = e.getTargetException();
			if (t instanceof ExecutionException && t.getCause() instanceof CoreException)
			{
				CoreException cause = (CoreException) t.getCause();
				StatusAdapter status;
				if (cause.getStatus().getCode() == IResourceStatus.CASE_VARIANT_EXISTS)
				{
					status = new StatusAdapter(new Status(IStatus.WARNING, PHPEditorPlugin.PLUGIN_ID, NLS.bind(
							Messages.NewPHPProjectWizard_conflictDirectory, newProjectHandle.getName()), cause));
				}
				else
				{
					status = new StatusAdapter(new Status(cause.getStatus().getSeverity(), PHPEditorPlugin.PLUGIN_ID,
							Messages.NewPHPProjectWizard_creationProblem, cause));
				}
				status
						.setProperty(IStatusAdapterConstants.TITLE_PROPERTY,
								Messages.NewPHPProjectWizard_creationProblem);
				StatusManager.getManager().handle(status, StatusManager.BLOCK);
			}
			else
			{
				StatusAdapter status = new StatusAdapter(new Status(IStatus.WARNING, PHPEditorPlugin.PLUGIN_ID, 0, NLS
						.bind(Messages.NewPHPProjectWizard_internalError, t.getMessage()), t));
				status
						.setProperty(IStatusAdapterConstants.TITLE_PROPERTY,
								Messages.NewPHPProjectWizard_creationProblem);
				StatusManager.getManager().handle(status, StatusManager.LOG | StatusManager.BLOCK);
			}
			return false;
		}
		return true;

	}

	public void finishProjectCreation()
	{
		final IProject project = createNewProject();
		IPreferenceStore store = PHPEditorPlugin.getDefault().getPreferenceStore();
		boolean shouldCreateFile = store.getBoolean(IPhpPreferenceConstants.PHPEDITOR_INITIAL_PROJECT_FILE_CREATE);
		String fileName = null;
		if (shouldCreateFile)
		{
			fileName = store.getString(IPhpPreferenceConstants.PHPEDITOR_INITIAL_PROJECT_FILE_NAME);
			if (fileName != null)
			{
				if (fileName.indexOf('.') == -1)
				{
					fileName = new StringBuffer().append(fileName).append(".php").toString(); //$NON-NLS-1$
				}
				initialPhpFile = project.getFile(fileName);
			}
		}
		else
		{
			initialPhpFile = null;
		}
		if (initialPhpFile != null && !initialPhpFile.exists())
		{
			final InputStream stream = openContentStream();
			try
			{
				IContainer container = (IContainer) project;
				file = container.getFile(new Path(fileName));
				file.refreshLocal(IResource.DEPTH_INFINITE, null);
				if (!file.exists())
				{
					file.create(stream, true, null);
				}
			}
			catch (CoreException ce)
			{
				if (stream != null)
				{
					try
					{
						stream.close();
					}
					catch (IOException e)
					{
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.ide.core.ui.wizards.BaseWizard#getFileNamesToSelect()
	 */
	public String[] getFileNamesToSelect()
	{
		IPreferenceStore store = PHPEditorPlugin.getDefault().getPreferenceStore();
		String fileName = store.getString(IPhpPreferenceConstants.PHPEDITOR_INITIAL_PROJECT_FILE_NAME);
		if (fileName != null)
		{
			if (fileName.indexOf('.') == -1)
			{
				fileName = new StringBuffer().append(fileName).append(".php").toString(); //$NON-NLS-1$
			}
			return new String[] { fileName, "index.php", "index.html", "index.htm" }; //$NON-NLS-1$ //$NON-NLS-2$; //$NON-NLS-3$
		}
		return new String[] { "index.php", "index.html", "index.htm" }; //$NON-NLS-1$ //$NON-NLS-2$; //$NON-NLS-3$
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.ide.core.ui.wizards.BaseWizard#getFileToOpenOnFinish()
	 */
	public IFile getFileToOpenOnFinish()
	{
		if (initialPhpFile != null && initialPhpFile.exists())
		{
			return initialPhpFile;
		}
		return null;
	}

	/**
	 * Returns the wizard's ID (com.aptana.ide.editor.php.wizards.PHPNewProjectWizard)
	 * 
	 * @return com.aptana.ide.editor.php.wizards.PHPNewProjectWizard
	 */
	public String getID()
	{
		return ID;
	}

	/**
	 * @see com.aptana.ide.core.ui.wizards.BaseWizard#createProjectDescription(java.lang.String,
	 *      org.eclipse.core.runtime.IPath)
	 */
	protected IProjectDescription createProjectDescription(String name, IPath path)
	{
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProjectDescription description = workspace.newProjectDescription(name);
		description.setLocation(path);
		description.setNatureIds(new String[] { PHPNature.NATURE_ID });
		// update the referenced project if provided
		if (referencePage != null)
		{
			IProject[] refProjects = referencePage.getReferencedProjects();
			if (refProjects.length > 0)
			{
				description.setReferencedProjects(refProjects);
			}
		}
		return description;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.ide.core.ui.wizards.BaseWizard#performFinish()
	 */
	public boolean performFinish()
	{
		createNewProject();
		if (newProject == null)
		{
			return false;
		}
		MultiStatus status = new MultiStatus(PHPEditorPlugin.PLUGIN_ID, 0,
				com.aptana.editor.php.internal.ui.wizard.Messages.NewPHPProjectWizard_natureConfigurationError, null);
		if (newProject != null && newProject.isAccessible())
		{

			configureProject(status);
			PHPWizardNewProjectCreationPage p = (PHPWizardNewProjectCreationPage) getPages()[0];
			p.setPhpLangOptions(newProject);
		}
		if (status.getChildren().length > 0)
		{
			for (IStatus error : status.getChildren())
			{
				PHPEditorPlugin.logError(error.getMessage(), error.getException());
				if (PHPEditorPlugin.DEBUG)
				{
					System.err.println("Error configuring the nature/builder"); //$NON-NLS-1$
					if (error.getMessage() != null)
					{
						System.err.println(error.getMessage());
					}
					if (error.getException() != null)
					{
						error.getException().printStackTrace();
					}
				}
			}
		}
		updatePerspective();
		selectAndReveal(newProject);
		return true;
	}

	/**
	 * Configure the project to include the nature and the builder. The Nature suppose to be set already, so the actual
	 * change would usually be a builder configuration.
	 * 
	 * @param errors
	 *            A multi-status instance that will hold any errors that occur while configuring the project.
	 */
	protected void configureProject(final MultiStatus errors)
	{
		ISafeRunnable code = new ISafeRunnable()
		{
			public void run() throws Exception
			{
				IProjectNature nature = newProject.getNature(PHPNature.NATURE_ID);
				if (nature != null)
				{
					nature.configure();
				}

			}

			public void handleException(Throwable exception)
			{
				if (exception instanceof CoreException)
					errors.add(((CoreException) exception).getStatus());
				else
					errors.add(new ResourceStatus(IResourceStatus.INTERNAL_ERROR, newProject.getFullPath(), NLS.bind(
							"Error configuring nature ''{0}''", PHPNature.NATURE_ID), exception)); //$NON-NLS-1$
			}
		};
		if (PHPEditorPlugin.DEBUG)
		{
			System.out.println("Configuring nature: " + PHPNature.NATURE_ID + " on project: " + newProject.getName()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		SafeRunner.run(code);
	}

	protected void updatePerspective()
	{
		BasicNewProjectResourceWizard.updatePerspective(configElement);
	}

	@Override
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException
	{
		configElement = config;
	}
}
