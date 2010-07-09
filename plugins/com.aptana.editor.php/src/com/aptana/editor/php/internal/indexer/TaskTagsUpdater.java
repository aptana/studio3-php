package com.aptana.editor.php.internal.indexer;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.php.internal.core.preferences.ITaskTagsListener;
import org.eclipse.php.internal.core.preferences.TaskTagsEvent;
import org.eclipse.php.internal.core.preferences.TaskTagsProvider;

import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.indexer.PHPGlobalIndexer;
import com.aptana.editor.php.internal.builder.BuildPathManager;
import com.aptana.editor.php.internal.builder.LocalModule;
import com.aptana.editor.php.internal.core.builder.IBuildPath;
import com.aptana.editor.php.internal.core.builder.IDirectory;
import com.aptana.editor.php.internal.core.builder.IModule;
import com.aptana.editor.php.internal.validation.Task;

public class TaskTagsUpdater
{
	private static final String APTANA_TASK = "aptana_task"; //$NON-NLS-1$

	static
	{
		TaskTagsProvider.getInstance().addTaskTagsListener(new ITaskTagsListener()
		{

			public void taskCaseChanged(TaskTagsEvent event)
			{
				reparse(event);
			}

			private void reparse(TaskTagsEvent event)
			{
				IProject project = event.getProject();
				if (project != null)
				{
					IBuildPath buildPathByResource = BuildPathManager.getInstance().getBuildPathByResource(project);
					if (buildPathByResource != null)
					{
						List<IModule> modules = buildPathByResource.getModules();
						List<IModule> emptyList = Collections.emptyList();
						List<IDirectory> emptyDirList = Collections.emptyList();
						PHPGlobalIndexer.getInstance().processChangedAfter(emptyList, modules, emptyList, 
								emptyDirList, emptyDirList);
					}
				}
				else
				{
					List<IBuildPath> buildPaths = BuildPathManager.getInstance().getBuildPaths();
					ArrayList<IModule> lm = new ArrayList<IModule>();
					for (IBuildPath p : buildPaths)
					{
						lm.addAll(p.getModules());
					}
					List<IModule> emptyList = Collections.emptyList();
					List<IDirectory> emptyDirList = Collections.emptyList();
					
					PHPGlobalIndexer.getInstance().processChangedAfter(emptyList, lm, emptyList, emptyDirList, emptyDirList);
				}
			}

			public void taskPrioritiesChanged(TaskTagsEvent event)
			{
				reparse(event);
			}

			public void taskTagsChanged(TaskTagsEvent event)
			{
				reparse(event);
			}

		}, null);
	}

	/**
	 * TODO: Shalom implement the core functionality of this TaskTagsUpdater
	 * @param reader
	 * @param module
	 */
	void update(Reader reader, IModule module)
	{
		try
		{
			if (module instanceof LocalModule)
			{
//				LocalModule lm = (LocalModule) module;
//				final IFile file = lm.getFile();
//				IProject project = file.getProject();
//				
//				Pattern[] patternsForProject = null;
//				TaskTag[] tags=null;
//				if (project!=null){
//					patternsForProject=TaskPatternsProvider.getInstance().getPatternsForProject(project);
//					tags=TaskTagsProvider.getInstance().getProjectTaskTags(project);
//				}
//				if (patternsForProject == null||tags==null)
//				{
//					patternsForProject = TaskPatternsProvider.getInstance().getPetternsForWorkspace();
//					tags=TaskTagsProvider.getInstance().getWorkspaceTaskTags();
//				}
//				CompletionLexer5 completionLexer5 = new CompletionLexer5(reader);
//				PhpParser5 parser = new PhpParser5(completionLexer5);
//				completionLexer5.setTasksPatterns(patternsForProject);
//				ErrorParserClient errorParserClient = new ErrorParserClient(null);
//				errorParserClient.setTaskTags(tags);
//				// parser.setErrorStrategy(PhpParser5.STACK_ERROR_STRATEGY);
//				completionLexer5.setParserClient(errorParserClient);
//				parser.setParserClient(errorParserClient);
//				try
//				{
//					parser.parse();
//				}
//				catch (Exception e)
//				{
//					PHPEditorPlugin.logError(e);
//				}
//				final Task[] tasks = errorParserClient.getTasks();
//				IWorkspaceRunnable runnable = new IWorkspaceRunnable()
//				{
//					public void run(IProgressMonitor monitor)
//					{
//						doHandleErrorsJob(tasks, file);
//
//					}
//				};
//
//				ResourcesPlugin.getWorkspace().run(runnable, getMarkerRule(file), IWorkspace.AVOID_UPDATE,
//						new NullProgressMonitor());
			}
		}
		catch (Exception e)
		{
			PHPEditorPlugin.logError(e);
		}
	}

	void doHandleErrorsJob(Task[] errors, IFile file)
	{
		synchronized (this) // prevent simultaneous error updates on the same file
		{
			if (ResourcesPlugin.getWorkspace().isTreeLocked())
			{
				PHPEditorPlugin.logWarning("Error updating the document errors. The workspace tree is locked."); //$NON-NLS-1$
			}

			if (file == null || file.exists() == false)
			{
				return;
			}
			int depth = IResource.DEPTH_INFINITE;
			try
			{

				IMarker[] problemMarkers = file.findMarkers(IMarker.TASK, true, depth);
				for (IMarker m : problemMarkers)
				{
					Object attribute2 = m.getAttribute(APTANA_TASK);
					if (attribute2!=null&&attribute2.equals("true")) //$NON-NLS-1$
					{
						m.delete();
					}
				}
				for (Task t : errors)
				{
					IMarker problemMarker = file.createMarker("com.aptana.ide.editor.php.task"); //$NON-NLS-1$
					problemMarker.setAttribute(IMarker.TRANSIENT, false);
					problemMarker.setAttribute(IMarker.SEVERITY, t.getPriority());
					problemMarker.setAttribute(IMarker.PRIORITY, t.getPriority());					
					problemMarker.setAttribute(IMarker.CHAR_START, t.getStart());
					problemMarker.setAttribute(IMarker.CHAR_END, t.getEnd());
					String string = Boolean.TRUE.toString();
					problemMarker.setAttribute(APTANA_TASK, string);
					problemMarker.setAttribute(IMarker.MESSAGE, t.getDescription());
					problemMarker.setAttribute(IMarker.LINE_NUMBER, t.getLineNumber());
				}
			}
			catch (Exception e)
			{
				PHPEditorPlugin.logError(e);
			}
		}
	}

	@SuppressWarnings("unused")
	private static ISchedulingRule getMarkerRule(IResource resource)
	{
		ISchedulingRule rule = null;
		if (resource != null)
		{
			IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
			rule = ruleFactory.markerRule(resource);
		}
		return rule;
	}
}
