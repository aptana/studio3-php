/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org2.eclipse.php.internal.debug.core.pathmapper;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.core.runtime.content.IContentTypeManager.ContentTypeChangeEvent;
import org.eclipse.core.runtime.content.IContentTypeManager.IContentTypeChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org2.eclipse.php.debug.core.debugger.pathmapper.PathMapper;
import org2.eclipse.php.internal.core.project.IIncludePathEntry;
import org2.eclipse.php.internal.core.project.options.includepath.IncludePathEntry;
import org2.eclipse.php.internal.core.project.options.includepath.IncludePathVariableManager;
import org2.eclipse.php.internal.core.util.PHPSearchEngine;
import org2.eclipse.php.internal.core.util.PHPSearchEngine.ExternalFileResult;
import org2.eclipse.php.internal.core.util.PHPSearchEngine.IncludedFileResult;
import org2.eclipse.php.internal.core.util.PHPSearchEngine.ResourceResult;
import org2.eclipse.php.internal.core.util.PHPSearchEngine.Result;
import org2.eclipse.php.internal.debug.core.IPHPDebugConstants;
import org2.eclipse.php.internal.debug.core.pathmapper.PathEntry.Type;

import com.aptana.editor.php.internal.core.IPHPConstants;
import com.aptana.php.debug.epl.PHPDebugEPLPlugin;


public class DebugSearchEngine {

	private static PHPFilenameFilter PHP_FILTER = new PHPFilenameFilter();
	private static IPathEntryFilter[] filters;

	/**
	 * Searches for all local resources that match provided remote file, and returns it in best match order.
	 * This method skips internal PHP search mechanism, going straight to the path mapper, so it's good
	 * only for resolving absolute paths.
	 *
	 * @param remoteFile Path of the file on server. This argument must not be <code>null</code>.
	 * @param debugTarget Current debug target
	 * @return path entry or <code>null</code> in case it could not be found
	 */
	public static PathEntry find(String remoteFile, IDebugTarget debugTarget) {
		return find(remoteFile, debugTarget, null, null);
	}

	/**
	 * Searches for all local resources that match provided remote file, and returns it in best match order.
	 * @param remoteFile Path of the file on server. This argument must not be <code>null</code>.
	 * @param debugTarget Current debug target
	 * @param currentWorkingDir Current working directory of PHP process
	 * @param currentScriptDir Directory of current PHP file
	 * @return path entry or <code>null</code> in case it could not be found
	 */
	public static PathEntry find(String remoteFile, IDebugTarget debugTarget, String currentWorkingDir, String currentScriptDir) {
		// TODO - SG: Test the find integration
		if (remoteFile == null) {
			throw new NullPointerException();
		}

		PathEntry pathEntry = null;
		ILaunchConfiguration launchConfiguration = debugTarget.getLaunch().getLaunchConfiguration();

		IProject project = null;
		if (currentScriptDir != null) {
			IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(currentScriptDir);
			if (resource != null) {
				project = resource.getProject();
			}
		}
		if (project == null) {
			String projectName;
			try {
				projectName = launchConfiguration.getAttribute(IPHPDebugConstants.PHP_Project, (String) null);
				if (projectName != null) {
					project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				}
			} catch (CoreException e) {
				PHPDebugEPLPlugin.logError(e);
			}
		}

		// If the given path is not absolute - use internal PHP search mechanism:
		if (!VirtualPath.isAbsolute(remoteFile)) {
			if (project != null && currentWorkingDir != null && currentScriptDir != null) {
				// This is not a full path, search using PHP Search Engine:
				Result<?, ?> result = PHPSearchEngine.find(remoteFile, currentWorkingDir, currentScriptDir, project);
				if (result instanceof ExternalFileResult) {
					ExternalFileResult extFileResult = (ExternalFileResult) result;
					return new PathEntry(extFileResult.getFile().getAbsolutePath(), Type.EXTERNAL, extFileResult.getContainer());
				}
				if (result instanceof IncludedFileResult) {
					IncludedFileResult incFileResult = (IncludedFileResult) result;
					IIncludePathEntry container = incFileResult.getContainer();
					Type type = (container.getEntryKind() == IncludePathEntry.IPE_VARIABLE) ? Type.INCLUDE_VAR : Type.INCLUDE_FOLDER;
					return new PathEntry(incFileResult.getFile().getAbsolutePath(), type, container);
				}
				if (result != null) {
					// workspace file
					ResourceResult resResult = (ResourceResult) result;
					IResource resource = resResult.getFile();
					return new PathEntry(resource.getFullPath().toString(), Type.WORKSPACE, resource.getParent());
				}
			}
			return null;
		}

		PathMapper pathMapper = PathMapperRegistry.getByLaunchConfiguration(launchConfiguration);
		if (pathMapper != null) {
			pathEntry = find(pathMapper, remoteFile, project, debugTarget);
		}
		return pathEntry;
	}

	/**
	 * Searches for all local resources that match provided remote file, and returns it in best match order.
	 * @param pathMapper Path mapper to look at
	 * @param remoteFile Path of the file on server. This argument must not be <code>null</code>.
	 * @param debugTarget Current debug target
	 * @return path entry or <code>null</code> in case it could not be found
	 */
	public static PathEntry find(final PathMapper pathMapper, final String remoteFile, final IProject currentProject, final IDebugTarget debugTarget) {

		final PathEntry[] localFile = new PathEntry[1];

		Job findJob = new Job("Searching for local file...") {
			protected IStatus run(IProgressMonitor monitor) {
				// First, look into the path mapper:
				localFile[0] = pathMapper.getLocalFile(remoteFile);
				if (localFile[0] != null) {
					return Status.OK_STATUS;
				}

				VirtualPath abstractPath = new VirtualPath(remoteFile);

				// Check whether we have an exact mapping for the remote path
				// If so - we shouldn't proceed with search (we should have this file right in the mapped folder)
				VirtualPath testPath = abstractPath.clone();
				testPath.removeLastSegment();
				if (pathMapper.getLocalPathMapping(testPath) != null) {
					return Status.OK_STATUS;
				}

				LinkedList<PathEntry> results = new LinkedList<PathEntry>();

				Object[] includePaths;
				if (currentProject != null) {
					includePaths = PHPSearchEngine.buildIncludePath(currentProject);
				} else {
					// Search in the whole workspace:
					Set<Object> s = new LinkedHashSet<Object>();
					IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
					for (IProject project : projects) {
						if (project.isOpen() && project.isAccessible()) {
							PHPSearchEngine.buildIncludePath(project, s);
						}
					}
					includePaths = s.toArray();
				}

				// Try to find this file in the Workspace:
				try {
					IPath path = Path.fromOSString(remoteFile);
					IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(path);
					if (file != null && file.exists()) {
						for (Object includePath : includePaths) {
							if (includePath instanceof IContainer) {
								IContainer container = (IContainer)includePath;
								if (container.getFullPath().isPrefixOf(file.getFullPath())) {
									localFile[0] = new PathEntry(file.getFullPath().toString(), Type.WORKSPACE, file.getParent());
									pathMapper.addEntry(remoteFile, localFile[0]);
									PathMapperRegistry.storeToPreferences();
									return Status.OK_STATUS;
								}
							}
						}
					}
				} catch (Exception e) {
					// no need to catch - this may be due to IPath creation failure
				}

				// Try to find this file in the Include Path:
				File file = new File(remoteFile);
				if (file.exists()) {
					for (Object includePath : includePaths) {
						if (includePath instanceof IIncludePathEntry) {
							IIncludePathEntry entry = (IIncludePathEntry) includePath;
							IPath entryPath = entry.getPath();
							if (entry.getEntryKind() == IIncludePathEntry.IPE_VARIABLE) {
								entryPath = IncludePathVariableManager.instance().resolveVariablePath(entryPath.toString());
							}
							if (entryPath != null && entryPath.isPrefixOf(Path.fromOSString(remoteFile))) {
								Type type = (entry.getEntryKind() == IIncludePathEntry.IPE_VARIABLE) ? Type.INCLUDE_VAR : Type.INCLUDE_FOLDER;
								localFile[0] = new PathEntry(file.getAbsolutePath(), type, entry);
								pathMapper.addEntry(remoteFile, localFile[0]);
								PathMapperRegistry.storeToPreferences();
								return Status.OK_STATUS;
							}
						}
					}
				}

				// Iterate over all include path, and search for a requested file
				for (Object includePath : includePaths) {
					if (includePath instanceof IContainer) {
						try {
							find((IContainer) includePath, abstractPath, results);
						} catch (InterruptedException e) {
							PHPDebugEPLPlugin.logError(e);
						}
					} else if (includePath instanceof IIncludePathEntry) {
						IIncludePathEntry entry = (IIncludePathEntry) includePath;
						IPath entryPath = entry.getPath();
						if (entry.getEntryKind() == IIncludePathEntry.IPE_LIBRARY) {
							// We don't support lookup in archive
							File entryDir = entryPath.toFile();
							find(entryDir, abstractPath, entry, results);
						} else if (entry.getEntryKind() == IIncludePathEntry.IPE_PROJECT) {
							IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(entry.getPath().lastSegment());
							if (res instanceof IProject) {
								IProject project = (IProject) res;
								if (project.isOpen() && project.isAccessible()) {
									try {
										find(project, abstractPath, results);
									} catch (InterruptedException e) {
										PHPDebugEPLPlugin.logError(e);
									}
								}
							}
						} else if (entry.getEntryKind() == IIncludePathEntry.IPE_VARIABLE) {
							entryPath = IncludePathVariableManager.instance().resolveVariablePath(entryPath.toString());
							if (entryPath != null) {
								File entryDir = entryPath.toFile();
								find(entryDir, abstractPath, entry, results);
							}
						}
					}
				}

				boolean foundInWorkspace = results.size()> 0;

				//search in opened editors
				searchOpenedEditors(results, abstractPath);

				if (!foundInWorkspace && results.size() == 1 && abstractPath.equals(results.getFirst().getAbstractPath())) {
					localFile[0] = results.getFirst();
				}
				else if (results.size() > 0) {
					Collections.sort(results, new BestMatchPathComparator(abstractPath));
					localFile[0] = filterItems(abstractPath, results.toArray(new PathEntry[results.size()]), debugTarget);
					if (localFile[0] != null) {
						pathMapper.addEntry(remoteFile, localFile[0]);
						PathMapperRegistry.storeToPreferences();
					}
				}
				return Status.OK_STATUS;
			}
		};

		findJob.schedule();
		try {
			findJob.join();
		} catch (InterruptedException e) {
		}

		return localFile[0];
	}

	private static void searchOpenedEditors(LinkedList<PathEntry> results, VirtualPath remotePath) {
		// Collect open editor references:
		List<IEditorReference> editors = new ArrayList<IEditorReference>(0);
		IWorkbench workbench= PlatformUI.getWorkbench();
		IWorkbenchWindow[] windows= workbench.getWorkbenchWindows();
		for (IWorkbenchWindow element : windows) {
			IWorkbenchPage[] pages= element.getPages();
			for (IWorkbenchPage element2 : pages) {
				IEditorReference[] references= element2.getEditorReferences();
				editors.addAll(Arrays.asList(references));
			}
		}

		// Collect external files opened in editors:
		for (IEditorReference editor : editors) {
			IEditorInput editorInput = null;
			try {
				editorInput = editor.getEditorInput();
			} catch (PartInitException e) {
				continue;
			}
			if (editorInput instanceof IPathEditorInput) {
				File file = ((IPathEditorInput)editorInput).getPath().toFile();
				if (file.exists() && file.getName().equalsIgnoreCase(remotePath.getLastSegment())) {
					results.add(new PathEntry(file.getAbsolutePath(), PathEntry.Type.EXTERNAL, file.getParentFile()));
				}
			}
			// TODO - SG: Try this for eclipse 3.4 once we migrate
			// if (editorInput instanceof FileStoreEditorInput) {
			// File file = new File(((IURIEditorInput)editorInput).getURI());
			// if (file.exists() && file.getName().equalsIgnoreCase(remotePath.getLastSegment())) {
			// results.add(new PathEntry(file.getAbsolutePath(), PathEntry.Type.EXTERNAL, file.getParentFile()));
			// }
			// }
		}
	}

	private static PathEntry filterItems(VirtualPath remotePath, PathEntry[] entries, IDebugTarget debugTarget) {
		IPathEntryFilter[] filters = initializePathEntryFilters();
		for (int i = 0; i < filters.length; ++i) {
			entries = filters[i].filter(entries, remotePath, debugTarget);
		}
		return entries.length > 0 ? entries[0] : null;
	}

	private static synchronized IPathEntryFilter[] initializePathEntryFilters() {
		if (filters == null) {
			Map<String, IPathEntryFilter> filtersMap = new HashMap<String, IPathEntryFilter>();
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IConfigurationElement[] elements = registry.getConfigurationElementsFor(PHPDebugEPLPlugin.PLUGIN_ID, "pathEntryFilters"); //$NON-NLS-1$
			for (IConfigurationElement element : elements) {
				if ("filter".equals(element.getName())) { //$NON-NLS-1$
					String id = element.getAttribute("id"); //$NON-NLS-1$
					if (!filtersMap.containsKey(id)) {
						String overridesIds = element.getAttribute("overridesId");
						if (overridesIds != null) {
							StringTokenizer st = new StringTokenizer(overridesIds, ", "); //$NON-NLS-1$
							while (st.hasMoreTokens()) {
								filtersMap.put(st.nextToken(), null);
							}
						}
						try {
							filtersMap.put(id, (IPathEntryFilter) element.createExecutableExtension("class")); //$NON-NLS-1$
						} catch (CoreException e) {
							PHPDebugEPLPlugin.logError(e);
						}
					}
				}
			}
			Collection<IPathEntryFilter> l = filtersMap.values();
			while (l.remove(null)); // remove null elements
			filters = l.toArray(new IPathEntryFilter[filtersMap.size()]);
		}
		return filters;
	}

	/**
	 * Searches for the path in the given IO file
	 *
	 * @param file File to start search from
	 * @param path Abstract path of the remote file
	 * @param container Include path entry container
	 * @param results List of results to return
	 * @throws InterruptedException
	 */
	private static void find(final File file, final VirtualPath path, final IIncludePathEntry container, final List<PathEntry> results) {
		if (!file.isDirectory() && file.getName().equals(path.getLastSegment())) {
			Type type = (container.getEntryKind() == IncludePathEntry.IPE_VARIABLE) ? Type.INCLUDE_VAR : Type.INCLUDE_FOLDER;
			PathEntry pathEntry = new PathEntry(file.getAbsolutePath(), type, container);
			results.add(pathEntry);
			return;
		} else {
			File[] files = file.listFiles(PHP_FILTER);
			if (files != null) {
				for (int i = 0; i < files.length; ++i) {
					find(files[i], path, container, results);
				}
			}
		}
	}

	/**
	 * Searches for the path in the given resource
	 * @param resource Resource to start the search from
	 * @param path Abstract path of the remote file
	 * @param results List of results to return
	 * @throws InterruptedException
	 */
	private static void find(final IResource resource, final VirtualPath path, final List<PathEntry> results) throws InterruptedException {
		if (resource == null || !resource.exists() || !resource.isAccessible()) {
			return;
		}
		WorkspaceJob findJob = new WorkspaceJob("") {
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				resource.accept(new IResourceVisitor() {
					public boolean visit(IResource resource) throws CoreException {
						if (!resource.isAccessible()) {
							return false;
						}
						if (resource instanceof IFile && resource.getName().equals(path.getLastSegment())) {
							PathEntry pathEntry = new PathEntry(resource.getFullPath().toString(), Type.WORKSPACE, resource.getParent());
							results.add(pathEntry);
						}
						return true;
					}
				});
				return Status.OK_STATUS;
			}
		};
		findJob.schedule();
		findJob.join();
	}

	private static class PHPFilenameFilter implements FileFilter, IContentTypeChangeListener {
		private Pattern phpFilePattern;

		public PHPFilenameFilter() {
			buildPHPFilePattern();
			Platform.getContentTypeManager().addContentTypeChangeListener(this);
		}

		private void buildPHPFilePattern() {
			IContentType type = Platform.getContentTypeManager().getContentType(IPHPConstants.CONTENT_TYPE_HTML_PHP);
			String[] phpExtensions = type.getFileSpecs(IContentType.FILE_EXTENSION_SPEC);
			StringBuilder buf = new StringBuilder();
			buf.append(".*\\.(");
			for (int i = 0; i < phpExtensions.length; ++i) {
				if (i > 0) {
					buf.append("|");
				}
				buf.append(phpExtensions[i]);
			}
			buf.append(')');
			phpFilePattern = Pattern.compile(buf.toString(), Pattern.CASE_INSENSITIVE);
		}

		public void contentTypeChanged(ContentTypeChangeEvent event) {
			buildPHPFilePattern();
		}

		public boolean accept(File pathname) {
			if (pathname.isDirectory() || phpFilePattern.matcher(pathname.getName()).matches()) {
				return true;
			}
			return false;
		}
	}

}
