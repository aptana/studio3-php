/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.ui.dialog;

import java.util.Collection;
import java.util.Comparator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SearchPattern;
import org2.eclipse.php.core.compiler.PHPFlags;

import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.indexer.IIndexChangeListener;
import com.aptana.editor.php.internal.search.IElementNode;
import com.aptana.editor.php.internal.search.ITypeNode;
import com.aptana.editor.php.internal.search.PHPSearchEngine;
import com.aptana.editor.php.internal.ui.editor.outline.PHPOutlineLabelProvider;

/**
 * @author Pavel Petrochenko
 */
public class TypeSelectionDialog extends CustomFilteredItemsSelectionDialog
{

	/**
	 * @author Pavel Petrochenko
	 */
	public static final class TypeLabelProvider extends LabelProvider
	{
		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
		 */
		public Image getImage(Object element)
		{
			if (element instanceof ITypeNode)
			{
				ITypeNode node = (ITypeNode) element;
				if (node.getKind() == IElementNode.TRAIT)
				{
					return PHPOutlineLabelProvider.TRAIT_ICON;
				}
				else
				{

					int modifiers = node.getModifiers();
					if (PHPFlags.isInterface(modifiers))
					{
						return PHPOutlineLabelProvider.INTERFACE_ICON;
					}
					return PHPOutlineLabelProvider.CLASS_ICON;
				}
			}
			if (element instanceof IElementNode)
			{
				IElementNode nm = (IElementNode) element;
				if (nm.getKind() == IElementNode.FUNCTION)
				{
					return PHPOutlineLabelProvider.FUNCTION_ICON;
				}
				if (nm.getKind() == IElementNode.CONSTANT)
				{
					return PHPOutlineLabelProvider.LOCALVARIABLE_ICON;
				}
			}
			return super.getImage(element);
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element)
		{
			if (element instanceof IElementNode)
			{
				IElementNode node = (IElementNode) element;
				return node.getName() + " - " + node.getPath(); //$NON-NLS-1$ 
			}
			return super.getText(element);
		}
	}

	private IProject project;

	/**
	 * @return current project
	 */
	public IProject getProject()
	{
		return project;
	}

	/**
	 * @param project
	 */
	public void setProject(IProject project)
	{
		this.project = project;
	}

	private boolean allowInterfaces = true;

	/**
	 * @return allow classes
	 */
	public boolean isAllowInterfaces()
	{
		return allowInterfaces;
	}

	/**
	 * @param allowInterfaces
	 */
	public void setAllowInterfaces(boolean allowInterfaces)
	{
		this.allowInterfaces = allowInterfaces;
	}

	/**
	 * @return allow interfaces
	 */
	public boolean isAllowClasses()
	{
		return allowClasses;
	}

	/**
	 * @param allowClasses
	 */
	public void setAllowClasses(boolean allowClasses)
	{
		this.allowClasses = allowClasses;
	}

	private boolean allowClasses = true;

	/**
	 * Constructs a new TypeSelectionDialog
	 * 
	 * @param shell
	 * @param multi
	 */
	public TypeSelectionDialog(Shell shell, boolean multi)
	{
		super(shell, multi);
		setDetailsLabelProvider(new LabelProvider()
		{

			public Image getImage(Object element)
			{
				if (element instanceof IElementNode)
				{
					return PHPOutlineLabelProvider.BLOCK_ICON;
				}
				return super.getImage(element);
			}

			public String getText(Object element)
			{
				if (element instanceof IElementNode)
				{
					IElementNode node = (IElementNode) element;
					return node.getPath();
				}
				return super.getText(element);
			}
		});
		setListLabelProvider(new TypeLabelProvider());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.aptana.editor.php.internal.ui.dialog.CustomFilteredItemsSelectionDialog#createExtendedContentArea(org.eclipse
	 * .swt.widgets.Composite)
	 */
	protected Control createExtendedContentArea(Composite parent)
	{
		return null;
	}

	private IIndexChangeListener listener = new IIndexChangeListener()
	{

		public void stateChanged(boolean done, String message)
		{
			if (done)
			{
				getShell().getDisplay().asyncExec(new Runnable()
				{

					public void run()
					{
						refreshContent();
					}

				});

			}
		}

		public void changeProcessed()
		{
		}

	};

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.internal.ui.dialog.CustomFilteredItemsSelectionDialog#create()
	 */
	public void create()
	{
		super.create();
		PHPSearchEngine.getInstance().addListener(listener);
		setHelpAvailable(false);
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(getContents(), "com.aptana.editor.php.open_type"); //$NON-NLS-1$		
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.internal.ui.dialog.CustomFilteredItemsSelectionDialog#close()
	 */
	public boolean close()
	{
		PHPSearchEngine.getInstance().removeListener(listener);
		return super.close();
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.internal.ui.dialog.CustomFilteredItemsSelectionDialog#createFilter()
	 */
	protected ItemsFilter createFilter()
	{
		Text txt = (Text) getPatternControl();
		final String text = txt.getText();
		SearchPattern searchPattern = new SearchPattern();
		searchPattern.setPattern(text);
		ItemsFilter itemsFilter = new ItemsFilter(searchPattern)
		{

			public boolean isConsistentItem(Object item)
			{
				return true;
			}

			public boolean matchItem(Object item)
			{
				if (item instanceof ITypeNode)
				{
					ITypeNode node = (ITypeNode) item;
					if (!isAllowClasses())
					{
						if (!PHPFlags.isInterface(node.getModifiers()))
						{
							return false;
						}
					}
					if (!isAllowInterfaces())
					{
						if (PHPFlags.isInterface(node.getModifiers()))
						{
							return false;
						}
					}
					if (project != null)
					{
						if (!node.isOnBuildPath(project))
						{
							return false;
						}
					}
					if (text.length() == 0)
					{
						return true;
					}
				}
				return super.matches(getElementName(item));
			}

		};
		return itemsFilter;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.aptana.editor.php.internal.ui.dialog.CustomFilteredItemsSelectionDialog#fillContentProvider(com.aptana.editor
	 * .php.internal.ui.dialog.CustomFilteredItemsSelectionDialog.AbstractContentProvider,
	 * com.aptana.editor.php.internal.ui.dialog.CustomFilteredItemsSelectionDialog.ItemsFilter,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void fillContentProvider(AbstractContentProvider contentProvider, ItemsFilter itemsFilter,
			IProgressMonitor progressMonitor) throws CoreException
	{
		Collection<?> allKnownTypes = PHPSearchEngine.getInstance().getAllKnownTypes();
		for (Object o : allKnownTypes)
		{
			contentProvider.add(o, itemsFilter);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.internal.ui.dialog.CustomFilteredItemsSelectionDialog#getDialogSettings()
	 */
	protected IDialogSettings getDialogSettings()
	{
		return PHPEditorPlugin.getDefault().getDialogSettings();
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.internal.ui.dialog.CustomFilteredItemsSelectionDialog#getElementName(java.lang.Object)
	 */
	public String getElementName(Object item)
	{
		IElementNode node = (IElementNode) item;
		return node.getName();
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.internal.ui.dialog.CustomFilteredItemsSelectionDialog#getItemsComparator()
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected Comparator getItemsComparator()
	{
		return new Comparator<IElementNode>()
		{

			public int compare(IElementNode o1, IElementNode o2)
			{
				int compareTo = o1.getName().compareTo(o2.getName());
				if (compareTo == 0)
				{
					return o1.getPath().compareTo(o2.getPath());
				}
				return compareTo;
			}

		};
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.FilteredItemsSelectionDialog#validateItem(java.lang.Object)
	 */
	protected IStatus validateItem(Object item)
	{
		return Status.OK_STATUS;
	}
}
