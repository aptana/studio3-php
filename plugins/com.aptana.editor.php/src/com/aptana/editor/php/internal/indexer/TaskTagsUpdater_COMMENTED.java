package com.aptana.editor.php.internal.indexer;

///**
// * This file Copyright (c) 2005-2008 Aptana, Inc. This program is
// * dual-licensed under both the Aptana Public License and the GNU General
// * Public license. You may elect to use one or the other of these licenses.
// * 
// * This program is distributed in the hope that it will be useful, but
// * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
// * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
// * the GPL or APL you select, is prohibited.
// *
// * 1. For the GPL license (GPL), you can redistribute and/or modify this
// * program under the terms of the GNU General Public License,
// * Version 3, as published by the Free Software Foundation.  You should
// * have received a copy of the GNU General Public License, Version 3 along
// * with this program; if not, write to the Free Software Foundation, Inc., 51
// * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
// * 
// * Aptana provides a special exception to allow redistribution of this file
// * with certain other free and open source software ("FOSS") code and certain additional terms
// * pursuant to Section 7 of the GPL. You may view the exception and these
// * terms on the web at http://www.aptana.com/legal/gpl/.
// * 
// * 2. For the Aptana Public License (APL), this program and the
// * accompanying materials are made available under the terms of the APL
// * v1.0 which accompanies this distribution, and is available at
// * http://www.aptana.com/legal/apl/.
// * 
// * You may view the GPL, Aptana's exception and additional terms, and the
// * APL in the file titled license.html at the root of the corresponding
// * plugin containing this source file.
// * 
// * Any modifications to this file must keep this entire header intact.
// */
//package com.aptana.editor.php.internal.indexer;
//
//import java.io.Reader;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.regex.Pattern;
//
//import org.eclipse.core.resources.IFile;
//import org.eclipse.core.resources.IMarker;
//import org.eclipse.core.resources.IProject;
//import org.eclipse.core.resources.IResource;
//import org.eclipse.core.resources.IResourceRuleFactory;
//import org.eclipse.core.resources.IWorkspace;
//import org.eclipse.core.resources.IWorkspaceRunnable;
//import org.eclipse.core.resources.ResourcesPlugin;
//import org.eclipse.core.runtime.IProgressMonitor;
//import org.eclipse.core.runtime.NullProgressMonitor;
//import org.eclipse.core.runtime.jobs.ISchedulingRule;
//import org.eclipse.php.internal.core.phpModel.parser.php5.CompletionLexer5;
//import org.eclipse.php.internal.core.phpModel.parser.php5.PhpParser5;
//
//import com.aptana.editor.php.builder.IBuildPath;
//import com.aptana.editor.php.builder.IDirectory;
//import com.aptana.editor.php.builder.IModule;
//import com.aptana.editor.php.indexer.PHPGlobalIndexer;
//import com.sun.jmx.snmp.tasks.Task;
//
//public class TaskTagsUpdater
//{
//	private static final String APTANA_TASK = "aptana_task";
//
//	static
//	{
//		TaskTagsProvider.getInstance().addTaskTagsListener(new ITaskTagsListener()
//		{
//
//			public void taskCaseChanged(TaskTagsEvent event)
//			{
//				reparse(event);
//			}
//
//			private void reparse(TaskTagsEvent event)
//			{
//				IProject project = event.getProject();
//				if (project != null)
//				{
//					IBuildPath buildPathByResource = BuildPathManager.getInstance().getBuildPathByResource(project);
//					if (buildPathByResource != null)
//					{
//						List<IModule> modules = buildPathByResource.getModules();
//						List<IModule> emptyList = Collections.emptyList();
//						List<IDirectory> emptyDirList = Collections.emptyList();
//						PHPGlobalIndexer.getInstance().processChangedAfter(emptyList, modules, emptyList, 
//								emptyDirList, emptyDirList);
//					}
//				}
//				else
//				{
//					List<IBuildPath> buildPaths = BuildPathManager.getInstance().getBuildPaths();
//					ArrayList<IModule> lm = new ArrayList<IModule>();
//					for (IBuildPath p : buildPaths)
//					{
//						lm.addAll(p.getModules());
//					}
//					List<IModule> emptyList = Collections.emptyList();
//					List<IDirectory> emptyDirList = Collections.emptyList();
//					
//					PHPGlobalIndexer.getInstance().processChangedAfter(emptyList, lm, emptyList, emptyDirList, emptyDirList);
//				}
//			}
//
//			public void taskPrioritiesChanged(TaskTagsEvent event)
//			{
//				reparse(event);
//			}
//
//			public void taskTagsChanged(TaskTagsEvent event)
//			{
//				reparse(event);
//			}
//
//		}, null);
//	}
//
//	void update(Reader reader, IModule module)
//	{
//		try
//		{
//			if (module instanceof LocalModule)
//			{
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
//					PHPPlugin.log(e);
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
//			}
//		}
//		catch (Exception e)
//		{
//			IdeLog.logError(PHPPlugin.getDefault(), e.getMessage(), e);
//		}
//	}
//
//	void doHandleErrorsJob(Task[] errors, IFile file)
//	{
//		synchronized (this) // prevent simultaneous error updates on the same file
//		{
//			if (ResourcesPlugin.getWorkspace().isTreeLocked())
//			{
//				// Note from Spike (1/26/2005): if this occurs, we will have problems getting the problem markers
//				// updated in the file.
//				// Robin had put in a fix to try alleviate this that we no longer think is necessary now that errors are
//				// handled as
//				// an atomic update via a IWorkspaceRunnable. If we see this error, we should consider putting that fix
//				// back in.
//				// If this exception is never seen again, we can remove this check.
//				IdeLog.logError(UnifiedEditorsPlugin.getDefault(),
//						Messages.ProjectFileErrorListener_ErrorUpdatingErrors, new IllegalStateException(
//								Messages.ProjectFileErrorListener_TreeLocked)); //
//			}
//
//			if (file == null || file.exists() == false)
//			{
//				return;
//			}
//			int depth = IResource.DEPTH_INFINITE;
//			try
//			{
//
//				IMarker[] problemMarkers = file.findMarkers(IMarker.TASK, true, depth);
//				for (IMarker m : problemMarkers)
//				{
//					Object attribute2 = m.getAttribute(APTANA_TASK);
//					if (attribute2!=null&&attribute2.equals("true"))
//					{
//						m.delete();
//					}
//				}
//				for (Task t : errors)
//				{
//					IMarker problemMarker = file.createMarker("com.aptana.ide.editor.php.task"); //$NON-NLS-1$
//					problemMarker.setAttribute(IMarker.TRANSIENT, false);
//					problemMarker.setAttribute(IMarker.SEVERITY, t.getPriority());
//					problemMarker.setAttribute(IMarker.PRIORITY, t.getPriority());					
//					problemMarker.setAttribute(IMarker.CHAR_START, t.getStart());
//					problemMarker.setAttribute(IMarker.CHAR_END, t.getEnd());
//					String string = Boolean.TRUE.toString();
//					problemMarker.setAttribute(APTANA_TASK, string);
//					problemMarker.setAttribute(IMarker.MESSAGE, t.getDescription());
//					problemMarker.setAttribute(IMarker.LINE_NUMBER, t.getLineNumber());
//				}
//			}
//			catch (Exception e)
//			{
//				IdeLog.logError(UnifiedEditorsPlugin.getDefault(), Messages.ProjectFileErrorListener_ErrorHere, e);
//			}
//		}
//	}
//
//	private static ISchedulingRule getMarkerRule(IResource resource)
//	{
//		ISchedulingRule rule = null;
//		if (resource != null)
//		{
//			IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
//			rule = ruleFactory.markerRule(resource);
//		}
//		return rule;
//	}
// }
