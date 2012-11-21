package com.aptana.editor.php.tests;

import junit.framework.TestCase;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;
import org2.eclipse.php.internal.core.PHPVersion;

import com.aptana.core.tests.TestProject;
import com.aptana.editor.php.core.PHPNature;

public abstract class ProjectBasedTestCase extends TestCase
{

	protected TestProject testProject;
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
			if (editor != null)
				editor.close(false);

			// Delete the generated project (with any files we have in it)
			testProject.delete();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			editor = null;
			file = null;
			project = null;
			super.tearDown();
		}
	}

	protected IFile createFile(String fileName, String contents) throws CoreException
	{
		return testProject.createFile(fileName, contents, new NullProgressMonitor());
	}

	protected IProject createProject() throws CoreException
	{
		testProject = new TestProject(getProjectName(), new String[] { PHPNature.NATURE_ID }, new NullProgressMonitor());
		project = testProject.getInnerProject();
		try
		{
			setProjectOptions(project);
		}
		catch (BackingStoreException e)
		{
			throw new CoreException(new Status(IStatus.ERROR, getPluginPreferenceQualifier(), e.getMessage(), e));
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
			file = createFile(fileName, contents);
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
