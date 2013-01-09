package com.aptana.editor.php.internal.indexer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.text.Document;
import org2.eclipse.php.internal.core.ast.nodes.Comment;
import org2.eclipse.php.internal.core.ast.nodes.Program;
import org2.eclipse.php.internal.core.ast.util.Util;
import org2.eclipse.php.internal.core.preferences.ITaskTagsListener;
import org2.eclipse.php.internal.core.preferences.TaskTagsEvent;
import org2.eclipse.php.internal.core.preferences.TaskTagsProvider;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.resources.TaskTag;
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
	private static final Pattern NEWLINE_SPLIT = Pattern.compile("\r\n|\r|\n"); //$NON-NLS-1$ // $codepro.audit.disable platformSpecificLineSeparator

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
						PHPGlobalIndexer.getInstance().processChangedAfter(emptyList, modules, emptyList, emptyDirList,
								emptyDirList);
					}
				}
				else
				{
					List<IBuildPath> buildPaths = BuildPathManager.getInstance().getBuildPaths();
					List<IModule> lm = new ArrayList<IModule>();
					for (IBuildPath p : buildPaths)
					{
						lm.addAll(p.getModules());
					}
					List<IModule> emptyList = Collections.emptyList();
					List<IDirectory> emptyDirList = Collections.emptyList();

					PHPGlobalIndexer.getInstance().processChangedAfter(emptyList, lm, emptyList, emptyDirList,
							emptyDirList);
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

	void doHandleErrorsJob(Task[] errors, IFile file)
	{
		synchronized (this) // prevent simultaneous error updates on the same file
		{
			if (ResourcesPlugin.getWorkspace().isTreeLocked())
			{
				IdeLog.logWarning(
						PHPEditorPlugin.getDefault(),
						"Error updating the document errors. The workspace tree is locked.", PHPEditorPlugin.DEBUG_SCOPE); //$NON-NLS-1$
			}

			if (file == null || !file.exists())
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
					if (attribute2 != null && attribute2.equals("true")) //$NON-NLS-1$
					{
						m.delete();
					}
				}
				for (Task t : errors)
				{
					IMarker problemMarker = file.createMarker(IMarker.TASK);
					problemMarker.setAttribute(IMarker.PRIORITY, t.getPriority());
					problemMarker.setAttribute(IMarker.CHAR_START, t.getStart());
					problemMarker.setAttribute(IMarker.CHAR_END, t.getEnd());
					problemMarker.setAttribute(APTANA_TASK, Boolean.TRUE.toString());
					problemMarker.setAttribute(IMarker.MESSAGE, t.getDescription());
					problemMarker.setAttribute(IMarker.LINE_NUMBER, t.getLineNumber());
				}
			}
			catch (Exception e)
			{
				IdeLog.logWarning(PHPEditorPlugin.getDefault(),
						"Error updating the PHP task-tags.", e, PHPEditorPlugin.DEBUG_SCOPE); //$NON-NLS-1$
			}
		}
	}

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

	public void updateTaskTags(final String contents, final Program program, final List<Comment> comments,
			IModule module)
	{
		try
		{
			if (module instanceof LocalModule)
			{
				LocalModule lm = (LocalModule) module;
				final IFile file = lm.getFile();
				// TODO get project level task tags/prefs when we support them.
				Collection<TaskTag> tags = TaskTag.getTaskTags();
				boolean isCaseSensitive = TaskTag.isCaseSensitive();
				program.setLineEndTable(Util.lineEndTable(new Document(contents)));

				// visit the comment nodes and parse for tasks!
				Collection<Task> tasks = new ArrayList<Task>();
				for (Comment comment : comments)
				{
					tasks.addAll(processCommentNode(program, contents, isCaseSensitive, tags, comment));
				}
				final Task[] finalTasks = tasks.toArray(new Task[tasks.size()]);
				IWorkspaceRunnable runnable = new IWorkspaceRunnable()
				{
					public void run(IProgressMonitor monitor)
					{
						doHandleErrorsJob(finalTasks, file);
					}
				};

				ResourcesPlugin.getWorkspace().run(runnable, getMarkerRule(file), IWorkspace.AVOID_UPDATE,
						new NullProgressMonitor());
			}
		}
		catch (Exception e)
		{
			IdeLog.logWarning(PHPEditorPlugin.getDefault(),
					"Error updating the PHP task-tags.", e, PHPEditorPlugin.DEBUG_SCOPE); //$NON-NLS-1$
		}
	}

	private Collection<Task> processCommentNode(final Program program, final String source,
			final boolean isCaseSensitive, final Collection<TaskTag> tags, final Comment commentNode)
	{
		Collection<Task> tasks = new ArrayList<Task>();
		// Explicitly make copies of substrings here and with message to avoid holding ref to underlying char[] for
		// entire source string (or full line for message)!
		String text = new String(source.substring(commentNode.getStart(),
				Math.min(source.length(), commentNode.getEnd() + 1)));
		if (!isCaseSensitive)
		{
			text = text.toLowerCase();
		}
		int offset = 0;
		String[] lines = NEWLINE_SPLIT.split(text);
		for (String line : lines)
		{
			for (TaskTag entry : tags)
			{
				String tag = entry.getName();
				if (!isCaseSensitive)
				{
					tag = tag.toLowerCase();
				}
				int index = line.indexOf(tag);
				if (index == -1)
				{
					continue;
				}

				String message = line.substring(index).trim();
				// Remove "**/" from the end of the line!
				if (message.endsWith("**/")) //$NON-NLS-1$
				{
					message = message.substring(0, message.length() - 3).trim();
				}
				// Remove "*/" from the end of the line!
				if (message.endsWith("*/")) //$NON-NLS-1$
				{
					message = message.substring(0, message.length() - 2).trim();
				}
				int start = commentNode.getStart() + offset + index;
				tasks.add(new Task(entry.getName(), new String(message), entry.getPriority(), start, start
						+ line.length() - index, program.getLineNumber(start)));
			}
			// FIXME If newline is \r\n, this means we're one off per line in our offsets...
			offset += line.length() + 1;
		}
		return tasks;
	}
}
