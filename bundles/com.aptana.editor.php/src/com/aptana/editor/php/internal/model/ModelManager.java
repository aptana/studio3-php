/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aptana.editor.php.core.model.IModelElementDelta;
import com.aptana.editor.php.core.model.ISourceModel;
import com.aptana.editor.php.core.model.ISourceModule;
import com.aptana.editor.php.indexer.IModuleIndexListener;
import com.aptana.editor.php.indexer.PHPGlobalIndexer;
import com.aptana.editor.php.internal.core.builder.IDirectory;
import com.aptana.editor.php.internal.core.builder.IModule;
import com.aptana.editor.php.internal.model.impl.ModelElementDeltaBuilder;
import com.aptana.editor.php.internal.model.impl.SourceModel;
import com.aptana.editor.php.internal.model.utils.ModelUtils;

/**
 * ModelManager
 * 
 * @author Denis Denisenko
 */
public class ModelManager
{
	/**
	 * Instance.
	 */
	private static ModelManager instance = new ModelManager();

	/**
	 * Module index listener.
	 */
	private IModuleIndexListener moduleIndexListener;

	/**
	 * Source model.
	 */
	private ISourceModel model = new SourceModel();

	/**
	 * Listeners.
	 */
	private Set<IModelDeltaListener> listeners = new HashSet<IModelDeltaListener>();

	/**
	 * Gets instance.
	 * 
	 * @return model manager instance.
	 */
	public static ModelManager getInstance()
	{
		return instance;
	}

	/**
	 * Gets source model.
	 * 
	 * @return source model.
	 */
	public ISourceModel getModel()
	{
		return model;
	}

	/**
	 * Adds delta listener.
	 * 
	 * @param listener
	 *            - listener.
	 */
	public void addListener(IModelDeltaListener listener)
	{
		synchronized (listeners)
		{
			listeners.add(listener);
		}
	}

	/**
	 * Removes delta listener.
	 * 
	 * @param listener
	 *            - listener.
	 */
	public void removeListener(IModelDeltaListener listener)
	{
		synchronized (listeners)
		{
			listeners.remove(listener);
		}
	}

	/**
	 * ModelManager constructor.
	 */
	private ModelManager()
	{
		bindListeners();
	}

	/**
	 * Creates and binds listeners.
	 */
	private void bindListeners()
	{
		moduleIndexListener = new IModuleIndexListener()
		{
			/**
			 * Module delta builders.
			 */
			private Map<ISourceModule, ModelElementDeltaBuilder> deltaBuilders = new HashMap<ISourceModule, ModelElementDeltaBuilder>();

			public void afterIndexChange(List<IModule> added, List<IModule> changed, List<IDirectory> addedDirectories)
			{
				ModelElementDelta delta = null;

				try
				{
					if ((changed == null || changed.size() == 0) && (added == null || added.size() == 0)
							&& (addedDirectories == null || addedDirectories.size() == 0))
					{
						return;
					}

					// System.out.println("----------------After index change---------------");
					//					
					// if (changed != null && changed.size() != 0)
					// {
					// System.out.println("----Changed modules:");
					// for (IModule module : changed)
					// {
					// System.out.println(module);
					// }
					// }
					//					
					// if (added != null && added.size() != 0)
					// {
					// System.out.println("----Added modules:");
					// for (IModule module : added)
					// {
					// System.out.println(module);
					// }
					// }
					//					
					// if (addedDirectories != null && addedDirectories.size() != 0)
					// {
					// System.out.println("----Added directories:");
					// for (IDirectory dir : addedDirectories)
					// {
					// System.out.println(dir);
					// }
					// }

					// creating delta.
					delta = new ModelElementDelta(ModelManager.getInstance().getModel());

					// adding "added" folders
					for (IDirectory dir : addedDirectories)
					{
						ISourceFolder fld = ModelUtils.convertFolder(dir);
						if (fld != null)
						{
							delta.added(fld);
						}
					}

					// adding "added" modules
					for (IModule module : added)
					{
						ISourceModule sourceModule = ModelUtils.convertModule(module);
						if (sourceModule != null)
						{
							delta.added(sourceModule);
						}
					}

					// creating deltas for changed modules using precreated delta builders
					for (IModule changedModule : changed)
					{
						ISourceModule sourceModule = ModelUtils.convertModule(changedModule);
						if (sourceModule != null)
						{
							ModelElementDeltaBuilder builder = this.deltaBuilders.get(sourceModule);
							if (builder != null)
							{
								ModelElementDelta moduleDelta = builder.buildDeltas();
								if (moduleDelta != null)
								{
									delta.insertDeltaTree(sourceModule, moduleDelta);
								}
							}
						}
					}
				}
				finally
				{
					deltaBuilders.clear();
				}

				if (delta != null)
				{
					// System.out.println("After index change delta: " + delta);
					notifyDelta(delta);
				}
			}

			public void beforeIndexChange(List<IModule> changed, List<IModule> removed,
					List<IDirectory> removedDirectories)
			{
				if ((changed == null || changed.size() == 0) && (removed == null || removed.size() == 0)
						&& (removedDirectories == null || removedDirectories.size() == 0))
				{
					return;
				}
				// System.out.println("----------------Before index change---------------");
				// if (changed != null && changed.size() != 0)
				// {
				// System.out.println("----Changed modules:");
				// for (IModule module : changed)
				// {
				// System.out.println(module);
				// }
				// }
				//				
				// if (removed != null && removed.size() != 0)
				// {
				// System.out.println("----Removed modules:");
				// for (IModule module : removed)
				// {
				// System.out.println(module);
				// }
				// }
				//				
				// if (removedDirectories != null && removedDirectories.size() != 0)
				// {
				// System.out.println("----Removed directories:");
				// for (IDirectory dir : removedDirectories)
				// {
				// System.out.println(dir);
				// }
				// }

				// creating delta.
				ModelElementDelta delta = new ModelElementDelta(ModelManager.getInstance().getModel());

				// adding removed folders
				for (IDirectory dir : removedDirectories)
				{
					ISourceFolder fld = ModelUtils.convertFolder(dir);
					if (fld != null)
					{
						delta.removed(fld);
					}
				}

				// adding removed modules
				for (IModule module : removed)
				{
					ISourceModule sourceModule = ModelUtils.convertModule(module);
					if (sourceModule != null)
					{
						delta.removed(sourceModule);
					}
				}

				// creating delta builders for changed modules
				for (IModule changedModule : changed)
				{
					ISourceModule sourceModule = ModelUtils.convertModule(changedModule);
					if (sourceModule != null)
					{
						ModelElementDeltaBuilder builder = new ModelElementDeltaBuilder(sourceModule);
						this.deltaBuilders.put(sourceModule, builder);
					}
				}

				if ((removed != null && removed.size() != 0)
						|| (removedDirectories != null && removedDirectories.size() != 0))
				{
					notifyDelta(delta);
				}
			}

		};

		PHPGlobalIndexer.getInstance().addListener(moduleIndexListener);
	}

	/**
	 * Notifies delta listeners.
	 * 
	 * @param delta
	 *            - delta to notify with.
	 */
	private void notifyDelta(IModelElementDelta delta)
	{

		Set<IModelDeltaListener> toNotify = null;
		synchronized (listeners)
		{
			toNotify = new HashSet<IModelDeltaListener>(listeners.size());
			toNotify.addAll(listeners);
		}

		for (IModelDeltaListener listener : toNotify)
		{
			listener.notify(delta);
		}
	}
}
