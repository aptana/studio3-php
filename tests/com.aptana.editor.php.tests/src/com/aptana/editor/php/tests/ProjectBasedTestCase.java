package com.aptana.editor.php.tests;

import java.io.ByteArrayInputStream;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.php.internal.core.PHPVersion;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import com.aptana.editor.php.core.PHPNature;

public abstract class ProjectBasedTestCase extends TestCase
{

	protected IProject project;
	protected IFile file;
	protected ITextEditor editor;

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		project = createProject();
	}

	@Override
	protected void tearDown() throws Exception
	{
		try
		{
			// Need to force the editor shut!
			if (editor != null)
				editor.close(false);
			// Delete the generated file
			if (file != null)
				file.delete(true, new NullProgressMonitor());
			// Delete the generated project
			project.delete(true, new NullProgressMonitor());
		}
		finally
		{
			editor = null;
			file = null;
			project = null;
			super.tearDown();
		}
	}

	protected IFile createFile(IProject project, String fileName, String contents) throws CoreException
	{
		IFile file = project.getFile(fileName);
		ByteArrayInputStream source = new ByteArrayInputStream(contents.getBytes());
		file.create(source, true, new NullProgressMonitor());
		return file;
	}

	protected IProject createProject() throws CoreException
	{
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject project = workspace.getRoot().getProject(getProjectName());
		IProjectDescription description = workspace.newProjectDescription(project.getName());
		description.setNatureIds(new String[] { PHPNature.NATURE_ID });
		if (!project.exists())
		{
			project.create(description, new NullProgressMonitor());
		}
		if (!project.isOpen())
		{
			project.open(new NullProgressMonitor());
			try
			{
				setProjectOptions(project);
			}
			catch (BackingStoreException e)
			{
				throw new CoreException(new Status(IStatus.ERROR, getPluginPreferenceQualifier(), e.getMessage(), e));
			}
		}
		return project;
	}

	/**
	 * Project name to use for the test we're setting up. Typically one project per test class.
	 * 
	 * @return
	 */
	protected abstract String getProjectName();

	protected abstract PHPVersion getInitialPHPVersion();
	
	protected abstract String getPluginPreferenceQualifier();
	
	protected abstract void setProjectOptions(IProject project) throws BackingStoreException;

	protected void setCaretOffset(int offset) throws PartInitException
	{
		getTextWidget().setCaretOffset(offset);
	}

	protected StyledText getTextWidget() throws PartInitException
	{
		ITextViewer adapter = (ITextViewer) getEditor().getAdapter(ITextOperationTarget.class);
		return adapter.getTextWidget();
	}

	protected ITextEditor getEditor() throws PartInitException
	{
		if (editor == null)
		{
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			editor = (ITextEditor) IDE.openEditor(page, file);
		}
		return editor;
	}

	protected IFile createAndOpenFile(String fileName, String contents) throws CoreException, PartInitException
	{
		if (file == null)
		{
			file = createFile(project, fileName, contents);
			getEditor();
		}
		return file;
	}

	protected void select(int offset, int length) throws PartInitException
	{
		setCaretOffset(offset);
		getEditor().selectAndReveal(offset, length);
	}

	protected void assertContents(String expected) throws PartInitException
	{
		assertEquals(expected, getTextWidget().getText());
	}

	protected Preferences getPreferences(IProject project)
	{
		return new ProjectScope(project).getNode(getPluginPreferenceQualifier());
	}
}
