/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package com.aptana.editor.php.internal.model.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.aptana.editor.php.core.model.IModelElement;
import com.aptana.editor.php.core.model.IModelElementDelta;
import com.aptana.editor.php.core.model.IParent;
import com.aptana.editor.php.core.model.env.MemberElementInfo;
import com.aptana.editor.php.core.model.env.ModelElementInfo;
import com.aptana.editor.php.core.model.env.SourceMethodElementInfo;
import com.aptana.editor.php.internal.model.ModelElementDelta;
import com.aptana.editor.php.internal.model.impl.env.SourceTypeElementInfo;

/**
 * A script element delta biulder creates a script element delta on a script element between the version of the script
 * element at the time the comparator was created and the current version of the script element. It performs this
 * operation by locally caching the contents of the script element when it is created. When the method createDeltas() is
 * called, it creates a delta over the cached contents and the new contents.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class ModelElementDeltaBuilder
{
	/**
	 * The model element handle
	 */
	IModelElement modelElement;

	/**
	 * The maximum depth in the script element children we should look into
	 */
	int maxDepth = Integer.MAX_VALUE;

	/**
	 * The old handle to info relationships
	 */
	Map infos;

	/**
	 * The old position info
	 */
	Map oldPositions;

	/**
	 * The new position info
	 */
	Map newPositions;

	/**
	 * Change delta
	 */
	ModelElementDelta delta;

	/**
	 * List of added elements
	 */
	List added;

	/**
	 * List of removed elements
	 */
	List removed;

	/**
	 * Doubly linked list item
	 */
	static class ListItem
	{
		public IModelElement previous;
		public IModelElement next;

		protected ListItem(IModelElement previous, IModelElement next)
		{
			this.previous = previous;
			this.next = next;
		}
	}

	/**
	 * Creates a script element comparator on a script element looking as deep as necessary.
	 */
	public ModelElementDeltaBuilder(IModelElement modelElement)
	{
		this.modelElement = modelElement;
		this.initialize();
		this.recordElementInfo(modelElement, (SourceModel) this.modelElement.getModel(), 0);
	}

	/**
	 * Creates a script element comparator on a script element looking only 'maxDepth' levels deep.
	 */
	public ModelElementDeltaBuilder(IModelElement modelElement, int maxDepth)
	{
		this.modelElement = modelElement;
		this.maxDepth = maxDepth;
		this.initialize();
		this.recordElementInfo(modelElement, (SourceModel) this.modelElement.getModel(), 0);
	}

	/**
	 * Repairs the positioning information after an element has been added
	 */
	private void added(IModelElement element)
	{
		this.added.add(element);
		ListItem current = this.getNewPosition(element);
		ListItem previous = null, next = null;
		if (current.previous != null)
			previous = this.getNewPosition(current.previous);
		if (current.next != null)
			next = this.getNewPosition(current.next);
		if (previous != null)
			previous.next = current.next;
		if (next != null)
			next.previous = current.previous;
	}

	/**
	 * Builds the script element deltas between the old content of the compilation unit and its new content.
	 * 
	 * @return delta
	 */
	public ModelElementDelta buildDeltas()
	{
		this.delta = new ModelElementDelta(modelElement);

		// if building a delta on a compilation unit or below,
		// it's a fine grained delta
		if (modelElement.getElementType() >= IModelElement.MODULE)
		{
			this.delta.fineGrained();
		}
		this.recordNewPositions(this.modelElement, 0);
		this.findAdditions(this.modelElement, 0);
		this.findDeletions();
		this.findChangesInPositioning(this.modelElement, 0);
		this.trimDelta(this.delta);
		if (this.delta.getAffectedChildren().length == 0)
		{
			// this is a fine grained but not children affected -> mark as
			// content changed
			this.delta.contentChanged();
		}

		return this.delta;
	}

	// private boolean equals(char[][][] first, char[][][] second) {
	// if (first == second)
	// return true;
	// if (first == null || second == null)
	// return false;
	// if (first.length != second.length)
	// return false;
	//
	// for (int i = first.length; --i >= 0;)
	// if (!CharOperation.equals(first[i], second[i]))
	// return false;
	// return true;
	// }

	/**
	 * Finds elements which have been added or changed.
	 */
	private void findAdditions(IModelElement newElement, int depth)
	{
		ModelElementInfo oldInfo = this.getElementInfo(newElement);
		if (oldInfo == null && depth < this.maxDepth)
		{
			this.delta.added(newElement);
			added(newElement);
		}
		else
		{
			this.removeElementInfo(newElement);
		}

		if (depth >= this.maxDepth)
		{
			// mark element as changed
			this.delta.changed(newElement, IModelElementDelta.F_CONTENT);
			return;
		}

		ModelElementInfo newInfo = null;
		newInfo = (ModelElementInfo) ((AbstractModelElement) newElement).getElementInfo();

		this.findContentChange(oldInfo, newInfo, newElement);

		if (oldInfo != null && newElement instanceof IParent)
		{

			IModelElement[] children = newInfo.getChildren();
			if (children != null)
			{
				int length = children.length;
				for (int i = 0; i < length; i++)
				{
					this.findAdditions(children[i], depth + 1);
				}
			}
		}
	}

	/**
	 * Looks for changed positioning of elements.
	 */
	private void findChangesInPositioning(IModelElement element, int depth)
	{
		if (depth >= this.maxDepth || this.added.contains(element) || this.removed.contains(element))
			return;

		if (!isPositionedCorrectly(element))
		{
			this.delta.changed(element, IModelElementDelta.F_REORDER);
		}

		if (element instanceof IParent)
		{
			ModelElementInfo info = null;

			info = (ModelElementInfo) ((AbstractModelElement) element).getElementInfo();

			IModelElement[] children = info.getChildren();
			if (children != null)
			{
				int length = children.length;
				for (int i = 0; i < length; i++)
				{
					this.findChangesInPositioning(children[i], depth + 1);
				}
			}
		}
	}

	/**
	 * The elements are equivalent, but might have content changes.
	 */
	private void findContentChange(ModelElementInfo oldInfo, ModelElementInfo newInfo, IModelElement newElement)
	{
		if (oldInfo instanceof MemberElementInfo && newInfo instanceof MemberElementInfo)
		{
			if (((MemberElementInfo) oldInfo).getModifiers() != ((MemberElementInfo) newInfo).getModifiers())
			{
				this.delta.changed(newElement, IModelElementDelta.F_MODIFIERS);
			}
			else if (oldInfo instanceof SourceMethodElementInfo && newInfo instanceof SourceMethodElementInfo)
			{
				SourceMethodElementInfo oldSourceMethodInfo = (SourceMethodElementInfo) oldInfo;
				SourceMethodElementInfo newSourceMethodInfo = (SourceMethodElementInfo) newInfo;
				if (!equals(oldSourceMethodInfo.getArgumentNames(), newSourceMethodInfo.getArgumentNames())
						|| !equals(oldSourceMethodInfo.getArgumentInitializers(),
								newSourceMethodInfo.getArgumentInitializers()))
				{
					this.delta.changed(newElement, IModelElementDelta.F_CONTENT);
				}
			}
		}
		if (oldInfo instanceof SourceTypeElementInfo && newInfo instanceof SourceTypeElementInfo)
		{
			SourceTypeElementInfo oldSourceTypeInfo = (SourceTypeElementInfo) oldInfo;
			SourceTypeElementInfo newSourceTypeInfo = (SourceTypeElementInfo) newInfo;
			if (!equals(oldSourceTypeInfo.getSuperclassNames(), newSourceTypeInfo.getSuperclassNames()))
			{
				this.delta.changed(newElement, IModelElementDelta.F_SUPER_TYPES);
			}
		}
	}

	/**
	 * Adds removed deltas for any handles left in the table
	 */
	private void findDeletions()
	{
		Iterator iter = this.infos.keySet().iterator();
		while (iter.hasNext())
		{
			IModelElement element = (IModelElement) iter.next();
			this.delta.removed(element);
			this.removed(element);
		}
	}

	private ModelElementInfo getElementInfo(IModelElement element)
	{
		return (ModelElementInfo) this.infos.get(element);
	}

	private ListItem getNewPosition(IModelElement element)
	{
		return (ListItem) this.newPositions.get(element);
	}

	private ListItem getOldPosition(IModelElement element)
	{
		return (ListItem) this.oldPositions.get(element);
	}

	private void initialize()
	{
		this.infos = new HashMap(20);
		this.oldPositions = new HashMap(20);
		this.newPositions = new HashMap(20);
		this.putOldPosition(this.modelElement, new ListItem(null, null));
		this.putNewPosition(this.modelElement, new ListItem(null, null));

		this.added = new ArrayList(5);
		this.removed = new ArrayList(5);
	}

	/**
	 * Inserts position information for the elements into the new or old positions table
	 */
	private void insertPositions(IModelElement[] elements, boolean isNew)
	{
		int length = elements.length;
		IModelElement previous = null, current = null, next = (length > 0) ? elements[0] : null;
		for (int i = 0; i < length; i++)
		{
			previous = current;
			current = next;
			next = (i + 1 < length) ? elements[i + 1] : null;
			if (isNew)
			{
				this.putNewPosition(current, new ListItem(previous, next));
			}
			else
			{
				this.putOldPosition(current, new ListItem(previous, next));
			}
		}
	}

	/**
	 * Returns whether the elements position has not changed.
	 */
	private boolean isPositionedCorrectly(IModelElement element)
	{
		ListItem oldListItem = this.getOldPosition(element);
		if (oldListItem == null)
			return false;

		ListItem newListItem = this.getNewPosition(element);
		if (newListItem == null)
			return false;

		IModelElement oldPrevious = oldListItem.previous;
		IModelElement newPrevious = newListItem.previous;
		if (oldPrevious == null)
		{
			return newPrevious == null;
		}
		else
		{
			return oldPrevious.equals(newPrevious);
		}
	}

	private void putElementInfo(IModelElement element, ModelElementInfo info)
	{
		this.infos.put(element, info);
	}

	private void putNewPosition(IModelElement element, ListItem position)
	{
		this.newPositions.put(element, position);
	}

	private void putOldPosition(IModelElement element, ListItem position)
	{
		this.oldPositions.put(element, position);
	}

	/**
	 * Records this elements info, and attempts to record the info for the children.
	 */
	private void recordElementInfo(IModelElement element, SourceModel model, int depth)
	{
		if (depth >= this.maxDepth)
		{
			return;
		}
		ModelElementInfo info = null;
		if (element instanceof AbstractModelElement)
		{
			info = ((AbstractModelElement) element).getElementInfo();
		}
		if (info == null) // no longer in the model.
			return;
		int sizeBeforeInsert = this.infos.size();
		this.putElementInfo(element, info);
		if (sizeBeforeInsert == this.infos.size())
		{
			return;
		}
		if (element instanceof IParent)
		{
			IModelElement[] children = info.getChildren();
			if (children != null)
			{
				insertPositions(children, false);
				for (int i = 0, length = children.length; i < length; i++)
					recordElementInfo(children[i], model, depth + 1);
			}
		}
	}

	/**
	 * Fills the newPositions hashtable with the new position information
	 */
	private void recordNewPositions(IModelElement newElement, int depth)
	{
		if (depth < this.maxDepth && newElement instanceof IParent)
		{
			ModelElementInfo info = null;

			info = (ModelElementInfo) ((AbstractModelElement) newElement).getElementInfo();

			IModelElement[] children = info.getChildren();
			if (children != null)
			{
				int sizeBeforeInsertion = this.newPositions.size();
				insertPositions(children, true);
				if (sizeBeforeInsertion == this.newPositions.size())
				{
					return;
				}
				for (int i = 0, length = children.length; i < length; i++)
				{
					recordNewPositions(children[i], depth + 1);
				}
			}
		}
	}

	/**
	 * Repairs the positioning information after an element has been removed
	 */
	private void removed(IModelElement element)
	{
		this.removed.add(element);
		ListItem current = this.getOldPosition(element);
		ListItem previous = null, next = null;
		if (current.previous != null)
			previous = this.getOldPosition(current.previous);
		if (current.next != null)
			next = this.getOldPosition(current.next);
		if (previous != null)
			previous.next = current.next;
		if (next != null)
			next.previous = current.previous;

	}

	private void removeElementInfo(IModelElement element)
	{
		this.infos.remove(element);
	}

	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("Built delta:\n"); //$NON-NLS-1$ // $codepro.audit.disable platformSpecificLineSeparator
		buffer.append(this.delta.toString());
		return buffer.toString();
	}

	/**
	 * Trims deletion deltas to only report the highest level of deletion
	 */
	private void trimDelta(ModelElementDelta elementDelta)
	{
		if (elementDelta.getKind() == IModelElementDelta.REMOVED)
		{
			IModelElementDelta[] children = elementDelta.getAffectedChildren();
			for (int i = 0, length = children.length; i < length; i++)
			{
				elementDelta.removeAffectedChild((ModelElementDelta) children[i]);
			}
		}
		else
		{
			IModelElementDelta[] children = elementDelta.getAffectedChildren();
			for (int i = 0, length = children.length; i < length; i++)
			{
				trimDelta((ModelElementDelta) children[i]);
			}
		}
	}

	private static final boolean equals(String[] first, String[] second)
	{
		if (first == second)
			return true;
		if (first == null || second == null)
			return false;
		if (first.length != second.length)
			return false;

		for (int i = first.length - 1; i >= 0; i--)
		{
			if (first[i] == null && second[i] != null)
			{
				return false;
			}
			if (first[i] != null && second[i] == null)
			{
				return false;
			}
			if (first[i] != null && !first[i].equals(second[i]))
				return false;
		}
		return true;
	}
}
