/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.ui.dialog;

import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

import com.aptana.editor.php.internal.search.IElementNode;
import com.aptana.editor.php.internal.search.ITypeNode;
import com.aptana.editor.php.internal.search.PHPSearchEngine;

/**
 * @author Pavel Petrochenko
 */
public class ElementSelectionDialog extends TypeSelectionDialog
{

	private static final String CONSTANTS = "constants"; //$NON-NLS-1$
	private static final String FUNCTIONS = "functions"; //$NON-NLS-1$
	private static final String CLASSES = "classes"; //$NON-NLS-1$
	private static final String TRAITS = "traits"; //$NON-NLS-1$
	private boolean addTraits;
	private boolean addClasses;
	private boolean addFunctions;
	private boolean addConstants;

	/**
	 * @see com.aptana.ide.editor.php.dialogs.FilteredItemsSelectionDialog#storeDialog(org.eclipse.jface.dialogs.IDialogSettings)
	 */
	protected void storeDialog(IDialogSettings settings)
	{
		settings.put(TRAITS, addTraits);
		settings.put(CLASSES, addClasses);
		settings.put(FUNCTIONS, addFunctions);
		settings.put(CONSTANTS, addConstants);
		super.storeDialog(settings);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.internal.ui.dialog.TypeSelectionDialog#fillContentProvider(org.eclipse.ui.dialogs.
	 * CustomFilteredItemsSelectionDialog.AbstractContentProvider,
	 * org.eclipse.ui.dialogs.FilteredItemsSelectionDialog.ItemsFilter, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void fillContentProvider(AbstractContentProvider contentProvider, ItemsFilter itemsFilter,
			IProgressMonitor progressMonitor) throws CoreException
	{
		if (addClasses || addTraits)
		{
			Collection<ITypeNode> allKnownTypes = PHPSearchEngine.getInstance().getAllKnownTypes();
			for (ITypeNode typeNode : allKnownTypes)
			{
				if ((addTraits && typeNode.getKind() == IElementNode.TRAIT)
						|| (addClasses && typeNode.getKind() == IElementNode.CLASS))
				{
					contentProvider.add(typeNode, itemsFilter);
				}
			}
		}
		if (addFunctions)
		{
			Collection<?> allKnownTypes = PHPSearchEngine.getInstance().getAllKnownFunctions();
			for (Object o : allKnownTypes)
			{
				contentProvider.add(o, itemsFilter);
			}
		}
		if (addConstants)
		{
			Collection<?> allKnownTypes = PHPSearchEngine.getInstance().getAllKnownConstants();
			for (Object o : allKnownTypes)
			{
				contentProvider.add(o, itemsFilter);
			}
		}
	}

	/**
	 * @param shell
	 * @param multi
	 */
	public ElementSelectionDialog(Shell shell, boolean multi)
	{
		super(shell, multi);
	}

	/**
	 * @see com.aptana.ide.editor.php.dialogs.FilteredItemsSelectionDialog#createExtras(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void createExtras(Composite content)
	{
		IDialogSettings dialogSettings = getDialogSettings();
		String string = dialogSettings.get(TRAITS);
		if (string != null)
		{
			addTraits = Boolean.parseBoolean(string);
		}
		else
		{
			addTraits = true;
		}
		string = dialogSettings.get(CLASSES);
		if (string != null)
		{
			addClasses = Boolean.parseBoolean(string);
		}
		else
		{
			addClasses = true;
		}
		string = dialogSettings.get(FUNCTIONS);
		if (string != null)
		{
			addFunctions = Boolean.parseBoolean(string);
		}
		else
		{
			addFunctions = true;
		}
		string = dialogSettings.get(CONSTANTS);
		if (string != null)
		{
			addConstants = Boolean.parseBoolean(string);
		}
		else
		{
			addConstants = true;
		}
		Group extraBar = new Group(content, SWT.NONE);
		extraBar.setLayout(new GridLayout(4, false));
		final Button traits = new Button(extraBar, SWT.CHECK);
		final Button classes = new Button(extraBar, SWT.CHECK);
		final Button functions = new Button(extraBar, SWT.CHECK);
		final Button constants = new Button(extraBar, SWT.CHECK);
		extraBar.setText(Messages.ElementSelectionDialog_extraBarText);
		traits.setText(Messages.ElementSelectionDialog_traits);
		traits.setSelection(addTraits);
		classes.setText(Messages.ElementSelectionDialog_classes);
		classes.setSelection(addClasses);
		functions.setText(Messages.ElementSelectionDialog_functions);
		functions.setSelection(addFunctions);
		constants.setText(Messages.ElementSelectionDialog_constants);
		constants.setSelection(addConstants);
		SelectionListener selectionListener = new SelectionListener()
		{

			public void widgetDefaultSelected(SelectionEvent e)
			{

			}

			public void widgetSelected(SelectionEvent e)
			{
				addTraits = traits.getSelection();
				addClasses = classes.getSelection();
				addFunctions = functions.getSelection();
				addConstants = constants.getSelection();
				refreshContent();
			}

		};
		traits.addSelectionListener(selectionListener);
		classes.addSelectionListener(selectionListener);
		functions.addSelectionListener(selectionListener);
		constants.addSelectionListener(selectionListener);
		extraBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		super.createExtras(content);
	}

}
