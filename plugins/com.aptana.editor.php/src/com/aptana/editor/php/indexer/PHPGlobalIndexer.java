/**
 * This file Copyright (c) 2005-2008 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain other free and open source software ("FOSS") code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.indexer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.php.internal.core.ast.nodes.Program;

import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.core.CorePreferenceConstants.Keys;
import com.aptana.editor.php.internal.builder.BuildPathManager;
import com.aptana.editor.php.internal.builder.FileSystemBuildPath;
import com.aptana.editor.php.internal.builder.IBuildPath;
import com.aptana.editor.php.internal.builder.IBuildPathChangeListener;
import com.aptana.editor.php.internal.builder.IBuildPathsListener;
import com.aptana.editor.php.internal.builder.IDirectory;
import com.aptana.editor.php.internal.builder.IModule;
import com.aptana.editor.php.internal.indexer.ComplexIndex;
import com.aptana.editor.php.internal.indexer.IndexPersistence;
import com.aptana.editor.php.internal.indexer.UnpackedElementIndex;

/**
 * PHP global indexer.
 * 
 * @author Denis Denisenko
 */
public final class PHPGlobalIndexer {

	private static class Mutex {};
	private static Mutex mutex = new Mutex();
	private final class WrapIndexer implements IModuleIndexer,IProgramIndexer {
		private final IConfigurationElement element;
		private IModuleIndexer indexer;

		private WrapIndexer(IConfigurationElement element) {
			this.element = element;
		}

		public void indexModule(IModule module,
				IIndexReporter reporter) {
			try {
				initIfNeeded();
				indexer.indexModule(module, reporter);
			} catch (CoreException e) {
				PHPEditorPlugin.logError(e);
			}

		}

		private void initIfNeeded() throws CoreException {
			if (indexer==null){
			indexer = (IModuleIndexer) element
					.createExecutableExtension(CLASS_ATTRIBUTE_NAME);
			}
		}

		public void indexModule(Program program, IModule module,
				IIndexReporter reporter) {
			if (indexer instanceof IProgramIndexer){
				IProgramIndexer pi=(IProgramIndexer) indexer;
				pi.indexModule(program, module, reporter);
			}
		}
	}

	private static final int SAVING_INTERVAL = 10000;

	Thread saverThread = new Thread() {
		public void run() {
			while (true) {
				try {
					Thread.sleep(SAVING_INTERVAL);
					try {
						doSave();
					} catch (Exception e) {
						PHPEditorPlugin.logError(e);
						e.printStackTrace();
					}
				} catch (InterruptedException e) {
					PHPEditorPlugin.logError(e);
					return;
				}
			}
		}
	};

	/**
	 * Extension point name.
	 */
	private static final String EXTENSION_POINT_NAME = "com.aptana.ide.editor.php.indexer"; //$NON-NLS-1$

	/**
	 * Indexer element name.
	 */
	private static final String INDEXER_ELEMENT_NAME = "indexer"; //$NON-NLS-1$

	/**
	 * Class attribute name.
	 */
	private static final String CLASS_ATTRIBUTE_NAME = "class"; //$NON-NLS-1$

	/**
	 * Indexer instance.
	 */
	private static PHPGlobalIndexer instance;

	/**
	 * Main index.
	 */
	private ComplexIndex mainIndex;

	/**
	 * Module indexers.
	 */
	private List<IModuleIndexer> moduleIndexers;

	/**
	 * Build path change listener.
	 */
	private IBuildPathChangeListener buildPathChangeListener;

	private HashSet<IIndexChangeListener> listeners = new HashSet<IIndexChangeListener>();

	/**
	 * Module index listeners.
	 */
	private HashSet<IModuleIndexListener> moduleIndexListeners = new HashSet<IModuleIndexListener>();

	/**
	 * Gets indexer instance.
	 * 
	 * @return indexer instance.
	 */
	public static PHPGlobalIndexer getInstance()
	{
		synchronized (mutex)
		{
			if (instance == null)
			{
				instance = new PHPGlobalIndexer();
			}
			return instance;
		}
	}

	/**
	 * Gets index.
	 * 
	 * @return index.
	 */
	public IElementsIndex getIndex() {
		return mainIndex;
	}

	/**
	 * PHPElementsIndexer private constructor.
	 */
	private PHPGlobalIndexer() {
		createMainIndex();
		PHPEditorPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener(){

			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(Keys.PHP_VERSION))
				PHPGlobalIndexer.getInstance().cleanLibraries();
			}
			
		});
		collectModuleIndexersInfo();
		initPersistence();
		buildPathChangeListener = new IBuildPathChangeListener()
		{
			public void changedBefore(List<IModule> changed,
					List<IModule> removed, List<IDirectory> removedDirectories)
			{
				processChangedBefore(changed, removed, removedDirectories);
			}

			public void changedAfter(List<IModule> added,
					List<IModule> changed, List<IModule> removed,
					List<IDirectory> addedDirectories, List<IDirectory> removedDirectories)
			{
				processChangedAfter(added, changed, removed, addedDirectories, removedDirectories);
			}
		};

		indexLocalModules();

		BuildPathManager.getInstance().addBuildPathChangeListener(new IBuildPathsListener()
		{
			public void changed(List<IBuildPath> added, List<IBuildPath> removed)
			{
				for (IBuildPath path : removed)
				{
					path.removeBuildPathChangeListener(buildPathChangeListener);
					mainIndex.removeIndex(path);
					// if build path is passive, we need to initiate
					// modules indexing manually
					if (path.isPassive())
					{
						Job job = handleModulesRemoved(path.getModules());
						job.setPriority(Job.BUILD);
						job.schedule();
					}
				}

				for (IBuildPath path : added)
				{
					mainIndex.addIndex(path, new UnpackedElementIndex());
					path.addBuildPathChangeListener(buildPathChangeListener);

					// if build path is passive, we need to initiate
					// modules indexing manually
					if (path.isPassive())
					{
						Job job = handleModulesAdded(path.getModules());
						job.setPriority(Job.BUILD);
						job.schedule();
					}
				}
			}
		});
		
		// Listen to the workspace and remove the index files for deleted projects
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(new IResourceChangeListener(){
			public void resourceChanged(IResourceChangeEvent event)
			{
				IResource resource = event.getResource();
				if (resource instanceof IProject)
				{
					IBuildPath buildPath = BuildPathManager.getInstance().getBuildPathByResource(resource);
					if (buildPath != null)
					{
						File indexFile = getIndexFile(buildPath);
						if (indexFile != null && indexFile.exists())
						{
							// We only delete the index file.
							// The content of the indexMapping file will be updated only on the next loading of the Studio.
							indexFile.delete();
						}
					}
				}
			}
		}, IResourceChangeEvent.PRE_DELETE);
	}

	private void initPersistence() {
		saverThread.setDaemon(true);
		saverThread.start();
	}

	/**
	 * Creates main index.
	 */
	private void createMainIndex() {
		mainIndex = new ComplexIndex();
	}

	/**
	 * Handles removed modules.
	 * 
	 * @param modules
	 *            - modules.
	 * @return job that is able to handle the removed modules.
	 */
	private Job handleModulesRemoved(final List<IModule> modules) {
		Job job = new Job(Messages.PHPGlobalIndexer_RemovingModuleIndex2) {
			public IStatus run(IProgressMonitor monitor) {
				long start = System.currentTimeMillis();
				monitor.beginTask(
						Messages.PHPGlobalIndexer_RemovingModuleIndex2, modules
								.size());
				fireChanged(modules.size());
				for (int i = 0; i < modules.size(); i++) {
					if (monitor.isCanceled())
					{
						break;
					}
					IModule module = modules.get(i);
					mainIndex
							.removeModuleEntries(module, module.getBuildPath());
					monitor.worked(1);
					UnpackedElementIndex elementIndex = (UnpackedElementIndex) mainIndex
							.getElementIndex(module.getBuildPath());
					if (elementIndex != null) {
						elementIndex.removeTimeStamp(module);
					}
				}
				monitor.done();
				markDirtyPathes(modules);
				fireChanged(0);
				if (PHPEditorPlugin.INDEXER_DEBUG)
				{
					System.out.println("Indexer handleModulesRemoved [took " + (System.currentTimeMillis() - start) //$NON-NLS-1$
							+ "ms)"); //$NON-NLS-1$
				}
				return Status.OK_STATUS;
			}
		};

		return job;
	}

	private void markDirtyPathes(final List<IModule> modules) {
		synchronized (needSaving) {
			for (IModule m : modules) {
				needSaving.add(m.getBuildPath());
			}
		}
	}

	/**
	 * Handles changed modules.
	 * 
	 * @param modules
	 *            - modules.
	 * @param resource
	 *            - resource.
	 * @return job that is able to handle the changed modules.
	 */
	private Job handleModulesChanged(final List<IModule> modules) {

		Job job = new Job(Messages.PHPGlobalIndexer_IndexChanged) {
			public IStatus run(IProgressMonitor monitor) {
				long start = System.currentTimeMillis();
				monitor.beginTask(Messages.PHPGlobalIndexer_IndexChanged2,
						modules.size());
				fireChanged(modules.size());
				for (int i = 0; i < modules.size(); i++) {
					IModule module = modules.get(i);
					long l = module.getTimeStamp();
					mainIndex.removeModuleEntries(module, module.getBuildPath());

					for (IModuleIndexer indexer : moduleIndexers) {
						if (monitor.isCanceled())
						{
							break;
						}
						indexer.indexModule(module, new IIndexReporter() {
							public IElementEntry reportEntry(int category,
									String entryPath, IReportable value,
									IModule module) {
								return mainIndex.addEntry(category, entryPath,
										value, module, module.getBuildPath());
							}
						});
					}
					UnpackedElementIndex elementIndex = (UnpackedElementIndex) mainIndex
							.getElementIndex(module.getBuildPath());
					if (elementIndex != null) {
						elementIndex.recordTimeStamp(module, l);
					}
					if (monitor.isCanceled())
					{
						break;
					}
					monitor.worked(1);
				}
				fireChanged(0);
				markDirtyPathes(modules);
				monitor.done();
				if (PHPEditorPlugin.INDEXER_DEBUG)
				{
					System.out.println("Indexer handleModulesChanged [took " + (System.currentTimeMillis() - start) //$NON-NLS-1$
							+ "ms)"); //$NON-NLS-1$
				}
				return Status.OK_STATUS;
			}
		};

		return job;
	}

	/**
	 * Handles added modules.
	 * 
	 * @param modules
	 *            - modules.
	 * @param resource
	 *            - resource.
	 * @return job that is able to handle the added modules.
	 */
	private Job handleModulesAdded(final List<IModule> modules) {

		Job job = new Job(Messages.PHPGlobalIndexer_IndexNew) {

			public IStatus run(IProgressMonitor monitor) {
				long start = System.currentTimeMillis();
				fireChanged(modules.size());
				monitor.beginTask(Messages.PHPGlobalIndexer_IndexNew, modules
						.size());

				for (int i = 0; i < modules.size(); i++) {
					if (monitor.isCanceled())
					{
						break;
					}
					IModule module = modules.get(i);
					monitor.setTaskName(Messages.PHPGlobalIndexer_IndexNew + " - ../" + module.getShortName()); //$NON-NLS-1$

					long l = module.getTimeStamp();
					for (IModuleIndexer indexer : moduleIndexers) {
						indexer.indexModule(module, new IIndexReporter() {
							public IElementEntry reportEntry(int category,
									String entryPath, IReportable value,
									IModule module) {
								return mainIndex.addEntry(category, entryPath,
										value, module, module.getBuildPath());
							}
						});
					}
					UnpackedElementIndex elementIndex = (UnpackedElementIndex) mainIndex
							.getElementIndex(module.getBuildPath());
					if (elementIndex != null) {
						elementIndex.recordTimeStamp(module, l);
					}
					monitor.worked(1);
				}
				monitor.done();
				markDirtyPathes(modules);
				fireChanged(0);
				if (PHPEditorPlugin.INDEXER_DEBUG)
				{
					System.out.println("Indexer handleModulesAdded [took " + (System.currentTimeMillis() - start) //$NON-NLS-1$
							+ "ms)"); //$NON-NLS-1$
				}
				return Status.OK_STATUS;
			}

		};
		
		return job;
	}

	/**
	 * Collects module indexers from extensions.
	 */
	private void collectModuleIndexersInfo() {
		moduleIndexers = new ArrayList<IModuleIndexer>();

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint ep = registry.getExtensionPoint(EXTENSION_POINT_NAME);

		if (ep != null) {
			IExtension[] extensions = ep.getExtensions();

			for (int i = 0; i < extensions.length; i++) {
				IExtension extension = extensions[i];
				IConfigurationElement[] elements = extension
						.getConfigurationElements();

				for (int j = 0; j < elements.length; j++) {
					final IConfigurationElement element = elements[j];
					String elementName = element.getName();

					if (elementName.equals(INDEXER_ELEMENT_NAME)) {
						try {

							moduleIndexers.add(new WrapIndexer(element));
						} catch (Throwable th) {
							PHPEditorPlugin.logError(
											Messages.PHPGlobalIndexer_UnableLoad
													+ elementName
													+ Messages.PHPGlobalIndexer_ProviderDecl, th);
						}
					}
				}
			}
		}
	}

	static HashSet<IBuildPath> needSaving = new HashSet<IBuildPath>();

	/**
	 * Save the index, if needed.
	 */
	public void save()
	{
		doSave();
	}
	
	private void doSave() {
		HashSet<IBuildPath> bp = new HashSet<IBuildPath>();
		synchronized (needSaving) {
			bp.addAll(needSaving);
			needSaving.clear();
		}
		for (IBuildPath p : bp) {
			File indexFile = getIndexFile(p);
			if (indexFile != null) {
				//long l0 = System.currentTimeMillis();
				BufferedOutputStream stream = null;
				try {
					stream = new BufferedOutputStream(new FileOutputStream(
							indexFile));
					UnpackedElementIndex elementIndex = (UnpackedElementIndex) mainIndex
							.getElementIndex(p);
					if (elementIndex != null)
					{
						IndexPersistence.store(elementIndex, new DataOutputStream(stream),
								p);
					}
				} catch (IOException e) {
					PHPEditorPlugin.logError(e);
				} finally {
					//long l1 = System.currentTimeMillis();
					//System.out.println("Saving:" + (l1 - l0));
					if (stream != null) {
						try {
							stream.close();
						} catch (IOException e) {
							PHPEditorPlugin.logError(e);
						}
					}
				}
			}
		}
	}

	private File getIndexFile(IBuildPath p) {
		if (pathes == null) {
			loadPathMappings();
		}
		String handleIdentifier = p.getHandleIdentifier();
		String string = pathes.get(handleIdentifier);
		if (string != null) {
			// In case the indexMappings got deleted during the session, recreate it.
			File parent = PHPEditorPlugin.getDefault().getStateLocation().toFile();
			File pathesFile = new File(parent, "indexMappings"); //$NON-NLS-1$
			if (!pathesFile.exists())
			{
				savePathMappings();
			}
			return new File(string);
		} else {
			int code = handleIdentifier.hashCode();
			File parent = PHPEditorPlugin.getDefault().getStateLocation().toFile();
			String sm = "" + code; //$NON-NLS-1$
			while (pathes.get(new File(parent, sm).getAbsolutePath()) != null) {
				sm += 'l';
			}
			File result = new File(parent, sm);
			pathes.put(handleIdentifier, result.getAbsolutePath());
			savePathMappings();
			return result;
		}
	}

	private synchronized void loadPathMappings() {
		pathes = new HashMap<String, String>();
		File parent = PHPEditorPlugin.getDefault().getStateLocation().toFile();
		File pathesFile = new File(parent, "indexMappings"); //$NON-NLS-1$
		if (pathesFile.exists()) {
			try {
				BufferedInputStream stream = new BufferedInputStream(
						new FileInputStream(pathesFile));
				DataInputStream dataInputStream = new DataInputStream(stream);
				int readInt = dataInputStream.readInt();
				for (int a = 0; a < readInt; a++) {
					String handle = dataInputStream.readUTF();
					String path = dataInputStream.readUTF();
					// Test the path and remove any non-existing index files from the indexMappings file
					if (path != null && new File(path).exists())
					{
						pathes.put(handle, path);
					}
				}
				dataInputStream.close();
			} catch (IOException e) {
				PHPEditorPlugin.logError(e);
			}
		}
	}

	private synchronized void savePathMappings() {
		File parent = PHPEditorPlugin.getDefault().getStateLocation().toFile();
		File pathesFile = new File(parent, "indexMappings"); //$NON-NLS-1$

		try {
			BufferedOutputStream stream = new BufferedOutputStream(
					new FileOutputStream(pathesFile));
			DataOutputStream dataOutputStream = new DataOutputStream(stream);
			dataOutputStream.writeInt(pathes.size());
			for (String s : pathes.keySet()) {
				dataOutputStream.writeUTF(s);
				dataOutputStream.writeUTF(pathes.get(s));
			}
			dataOutputStream.close();
		} catch (IOException e) {
			PHPEditorPlugin.logError(e);
		}
	}

	private HashMap<String, String> pathes;

	int modulesNum = 0;

	/**
	 * Indexes local modules.
	 */
	public void indexLocalModules() {
		Job initializator = new Job(Messages.PHPGlobalIndexer_initializinIndex) {
			protected IStatus run(IProgressMonitor monitor) {
				
				List<IBuildPath> paths = BuildPathManager.getInstance()
						.getBuildPaths();
				for (IBuildPath path : paths) {
					if (monitor.isCanceled())
					{
						break;
					}
					final ArrayList<IModule> modules = new ArrayList<IModule>();
					UnpackedElementIndex index = new UnpackedElementIndex();
					boolean loaded = false;
					File indexFile = getIndexFile(path);
					if (indexFile.exists()) {
						DataInputStream di = null;
						try {

							di = new DataInputStream(new BufferedInputStream(
									new FileInputStream(indexFile)));
							try {
								IndexPersistence.load(index, di, path);
								loaded = true;
							} catch (Exception e) {								
								indexFile.delete();
								if (!(e instanceof IOException)){
									e.printStackTrace();									
								}

							}
							di.close();
						} catch (Exception e1) {
							PHPEditorPlugin.logError(e1);
							e1.printStackTrace();
						}
					}
					if (monitor.isCanceled())
					{
						break;
					}
					path.addBuildPathChangeListener(buildPathChangeListener);
					modules.addAll(path.getModules());
					if (!loaded) {
						mainIndex.addIndex(path, new UnpackedElementIndex());
						Job addedJob = handleModulesAdded(modules);
						addedJob.schedule();
					} else {
						mainIndex.addIndex(path, index);
						ArrayList<IModule> changed = new ArrayList<IModule>();
						List<IModule> asList = Arrays.asList(index
								.getAllModules());
						HashSet<IModule> all = new HashSet<IModule>(asList);
						for (IModule m : modules) {
							if (monitor.isCanceled())
							{
								break;
							}
							if (all.contains(m)) {
								long timeStamp = index.getTimeStamp(m);
								long timeStamp2 = m.getTimeStamp();
								if (timeStamp != timeStamp2) {
									changed.add(m);
								}
							}
						}
						if (!changed.isEmpty()) {
							Job changedJob = handleModulesChanged(changed);
							changedJob.setPriority(Job.BUILD);
							changedJob.schedule();
						}
						all.removeAll(modules);
						if (!all.isEmpty()) {
							Job removedJob = handleModulesRemoved(new ArrayList<IModule>(
									all));
							removedJob.setPriority(Job.BUILD);
							removedJob.schedule();
						}
						all = new HashSet<IModule>(modules);
						all.removeAll(asList);
						
						if (!all.isEmpty()) {
							Job addedJob = handleModulesAdded(new ArrayList<IModule>(
									all));
							addedJob.setPriority(Job.BUILD);
							addedJob.schedule();
						}
					}

				}
				return Status.OK_STATUS;
			}

		};
		initializator.setPriority(Job.BUILD);
		initializator.setRule(ResourcesPlugin.getWorkspace().getRoot());
		initializator.schedule();
	}

	/**
	 * @param added
	 * @param changed
	 * @param removed
	 */
	public void processChangedBefore(final List<IModule> changed, 
			final List<IModule> removed,
			final List<IDirectory> removedDirectories)
	{
		fireBeforeIndexing(changed, removed, removedDirectories);
	}

	/**
	 * @param added
	 * @param changed
	 * @param removed
	 */
	public void processChangedAfter(final List<IModule> added, final List<IModule> changed,
			List<IModule> removed,
			final List<IDirectory> addedDirectories, final List<IDirectory> removedDirectories)
	{
		
		final List<Job> jobs = new ArrayList<Job>();
		final List<Integer> sizes = new ArrayList<Integer>();

		if (changed.size() != 0)
		{
			jobs.add(handleModulesChanged(changed));
			sizes.add(changed.size());
		}

		if (added.size() != 0)
		{
			jobs.add(handleModulesAdded(added));
			sizes.add(added.size());
		}
		
		if (removed.size() != 0)
		{
			jobs.add(handleModulesRemoved(removed));
			sizes.add(removed.size());
		}

		int size = 0;
		for (int i = 0; i < sizes.size(); i++)
		{
			size += sizes.get(i);
		}
		final int summSize = size;
		if (size != 0)
		{
			Job complexJob = new Job(Messages.PHPGlobalIndexer_PHP_Index)
			{

				@Override
				protected IStatus run(IProgressMonitor monitor)
				{
					monitor.beginTask(Messages.PHPGlobalIndexer_PHP_Index2, summSize);

					IProgressMonitor pgMonitor = getJobManager().createProgressGroup();
					pgMonitor.setTaskName(Messages.PHPGlobalIndexer_PHP_Index);
					for (int i = 0; i < jobs.size(); i++)
					{
						if (monitor.isCanceled() || pgMonitor.isCanceled())
						{
							break;
						}
						Job job = jobs.get(i);
						job.setProgressGroup(pgMonitor, sizes.get(i));
						job.schedule();
						try
						{
							job.join();
						}
						catch (InterruptedException e)
						{
						}
					}
					monitor.done();
					pgMonitor.done();
					fireChangeProcessed();
					fireAfterIndexing(added, changed, addedDirectories);
					return Status.OK_STATUS;
				}

			};
			complexJob.setPriority(Job.BUILD);
			complexJob.schedule();
		}
		else
		{
			//notifying listeners about added directories
			fireAfterIndexing(added, changed, addedDirectories);
		}
	}

	public void processUnsavedModuleUpdate(Program program, IModule module) {
		
		mainIndex.removeModuleEntries(module, module.getBuildPath());
		UnpackedElementIndex elementIndex = (UnpackedElementIndex) mainIndex
		.getElementIndex(module.getBuildPath());		
		for (IModuleIndexer indexer : moduleIndexers) {
			if (indexer instanceof IProgramIndexer){
			((IProgramIndexer)indexer).indexModule(program,module, new IIndexReporter() {
				public IElementEntry reportEntry(int category,
						String entryPath, IReportable value, IModule module) {
					return mainIndex.addEntry(category, entryPath, value,
							module, module.getBuildPath());
				}
			});
			}
		}
		
		if (elementIndex != null) {
			//reindex it later
			elementIndex.recordTimeStamp(module, -1);
		}
		fireChanged(0);
		fireChangeProcessed();
	}

	/**
	 * @param modulesLeft
	 */
	protected synchronized void fireChanged(int modulesLeft) {
		for (IIndexChangeListener l : listeners) {
			l.stateChanged(modulesLeft == 0, MessageFormat.format(
					Messages.PHPGlobalIndexer_ModulesLeft, modulesLeft));
		}
	}

	/**
	 * @param modulesLeft
	 */
	protected synchronized void fireChangeProcessed() {
		for (IIndexChangeListener l : listeners) {
			l.changeProcessed();
		}
	}

	/**
	 * Fires changes.
	 * @param changed - changed modules.
	 * @param removed - removed modules.
	 */
	protected void fireBeforeIndexing(List<IModule> changed, List<IModule> removed, 
			List<IDirectory> removedDirectories)
	{
		List<IModuleIndexListener> listeners = new ArrayList<IModuleIndexListener>();
		synchronized (moduleIndexListeners)
		{
			listeners.addAll(moduleIndexListeners);
		}
		
		for (IModuleIndexListener listener : listeners)
		{
			listener.beforeIndexChange(changed, removed, removedDirectories);
		}
	}
	
	/**
	 * Fires changes.
	 * @param added - added.
	 * @param changed - changed.
	 */
	protected void fireAfterIndexing(List<IModule> added, List<IModule> changed, 
			List<IDirectory> addedDirectories)
	{
		List<IModuleIndexListener> toNotify = new ArrayList<IModuleIndexListener>();
		synchronized (moduleIndexListeners)
		{
			toNotify.addAll(moduleIndexListeners);
		}
		
		for (IModuleIndexListener listener : toNotify)
		{
			listener.afterIndexChange(added, changed, addedDirectories);
		}
	}

	/**
	 * @param listener
	 */
	public synchronized void addListener(IIndexChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * @param listener
	 */
	public synchronized void removeListener(IIndexChangeListener listener) {
		listeners.remove(listener);
	}

	/**
	 * @param listener
	 */
	public void addListener(IModuleIndexListener listener) {
		synchronized (moduleIndexListeners) {
			moduleIndexListeners.add(listener);
		}
	}

	/**
	 * @param listener
	 */
	public synchronized void removeListener(IModuleIndexListener listener) {
		synchronized (moduleIndexListeners) {
			moduleIndexListeners.remove(listener);
		}
	}

	/**
	 * Clean the index for the contained project in the given builder.
	 * 
	 * @param project
	 *            The project to clean
	 */
	public void clean(IProject project)
	{
		if (project != null)
		{
			BuildPathManager buildPathManager = BuildPathManager.getInstance();
			IBuildPath buildPath = buildPathManager.getBuildPathByResource(project);
			if (buildPath != null)
			{
				File indexFile = getIndexFile(buildPath);
				if (indexFile != null && indexFile.exists())
				{
					// We only delete the index file.
					// The content of the indexMapping file will be updated only on the next loading of the Studio.
					indexFile.delete();
				}
				List<IProject> targetProject = new ArrayList<IProject>(1);
				List<IProject> empty = new ArrayList<IProject>(0);
				// TODO - SG Check the threading issue that might happen with the global indexer in the buildpathmanager
				// call
				mainIndex.removeIndex(buildPath);
				buildPathManager.handleChanged(empty, targetProject);
				buildPathManager.handleChanged(targetProject, empty);

				final IBuildPath newBuildPath = buildPathManager.getBuildPathByResource(project);
				mainIndex.addIndex(newBuildPath, new UnpackedElementIndex());
				Job job = handleModulesAdded(newBuildPath.getModules());
				job.setPriority(Job.BUILD);
				job.schedule();
				try
				{
					job.join();
				}
				catch (InterruptedException e)
				{
				}
				Job savingJob = new Job(Messages.PHPGlobalIndexer_savingIndex)
				{
					protected IStatus run(IProgressMonitor monitor)
					{
						needSaving.add(newBuildPath);
						doSave();
						return Status.OK_STATUS;
					}
				};
				savingJob.setSystem(true);
				savingJob.setPriority(Job.BUILD);
			}
		}
	}

	/**
	 * Clean all the projects in the workspace.
	 */
	public void cleanAllProjects()
	{
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (IProject p : projects)
		{
			if (p.isAccessible())
			{
				clean(p);
			}
		}
	}

	/**
	 * Perform a clean on the external libraries attached to the system.
	 */
	public void cleanLibraries()
	{
		BuildPathManager buildPathManager = BuildPathManager.getInstance();
		List<IBuildPath> buildPaths = buildPathManager.getBuildPaths();
		for (IBuildPath buildPath : buildPaths)
		{
			if (buildPath instanceof FileSystemBuildPath)
			{
				File indexFile = getIndexFile(buildPath);
				if (indexFile != null && indexFile.exists())
				{
					// We only delete the index file.
					// The content of the indexMapping file will be updated only on the next loading of the Studio.
					indexFile.delete();
				}
				buildPathManager.removeBuildPath(((FileSystemBuildPath) buildPath).getFile());
			}
		}
		buildPathManager.indexExternalLibraries();
	}
}
