/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.ui.preferences;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.aptana.editor.php.internal.builder.IPHPLibrary;
import com.aptana.editor.php.internal.builder.LibraryManager;
import com.aptana.editor.php.internal.builder.PHPLibrary;
import com.aptana.editor.php.internal.builder.UserLibrary;

/**
 * @author Pavel Petrochenko
 */
public class PHPLibrariesPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{

	private final class SelectAction implements SelectionListener
	{

		boolean select;

		private SelectAction(boolean doSelect)
		{
			this.select = doSelect;
		}

		public void widgetDefaultSelected(SelectionEvent e)
		{

		}

		public void widgetSelected(SelectionEvent e)
		{
			newCheckList.setAllChecked(select);
		}
	}

	private CheckboxTableViewer newCheckList;

	@Override
	protected Control createContents(Composite parent)
	{
		Composite body = new Composite(parent, SWT.NONE);

		body.setLayout(new GridLayout(1, false));
		Label label = new Label(body, SWT.NONE | SWT.WRAP);
		label.setText(Messages.PHPLibrariesPreferencePage_librariesTitle);
		final Map<URL,Image> images = new HashMap<URL, Image>();
		Composite tableAndButton = new Composite(body, SWT.NONE);
		tableAndButton.setLayout(new GridLayout(2, false));
		newCheckList = CheckboxTableViewer.newCheckList(tableAndButton, SWT.BORDER);
		newCheckList.setContentProvider(new ArrayContentProvider());
		newCheckList.setInput(LibraryManager.getInstance().getAllLibraries());
		Composite buttons = new Composite(tableAndButton, SWT.NONE);
		buttons.setLayout(new GridLayout(1, false));
		newCheckList.setComparator(new ViewerComparator());
		newCheckList.setLabelProvider(new LibraryLabelProvider(images));
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.minimumHeight = 400;
		newCheckList.getControl().setLayoutData(layoutData);
		body.addDisposeListener(new DisposeListener()
		{

			public void widgetDisposed(DisposeEvent e)
			{
				for (Image m : images.values())
				{
					m.dispose();
				}
			}

		});
		layoutData = new GridData();
		layoutData.heightHint = 400;
		body.setLayoutData(layoutData);
		for (IPHPLibrary l : LibraryManager.getInstance().getAllLibraries())
		{
			newCheckList.setChecked(l, l.isTurnedOn());
		}

		buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		Button add = new Button(buttons, SWT.PUSH);
		add.setText(Messages.PHPLibrariesPreferencePage_newUserLibrary);
		add.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		add.addSelectionListener(new SelectionListener()
		{

			public void widgetDefaultSelected(SelectionEvent e)
			{

			}

			public void widgetSelected(SelectionEvent e)
			{
				PHPLibraryDialog libraryDialog = new PHPLibraryDialog(Display.getCurrent().getActiveShell(), null,
						getContent());
				if (libraryDialog.open() == Dialog.OK)
				{
					UserLibrary result = libraryDialog.getResult();
					newCheckList.add(result);
					newCheckList.setChecked(result, true);
				}
			}

		});
		final Button edit = new Button(buttons, SWT.PUSH);
		edit.addSelectionListener(new SelectionListener()
		{

			public void widgetDefaultSelected(SelectionEvent e)
			{
				// empty
			}

			public void widgetSelected(SelectionEvent e)
			{
				IStructuredSelection ss = (IStructuredSelection) newCheckList.getSelection();
				UserLibrary firstElement = (UserLibrary) ss.getFirstElement();
				PHPLibraryDialog libraryDialog = new PHPLibraryDialog(Display.getCurrent().getActiveShell(),
						firstElement, getContent());
				if (libraryDialog.open() == Dialog.OK)
				{
					newCheckList.remove(firstElement);
					newCheckList.add(libraryDialog.getResult());
				}
			}

		});
		edit.setText(Messages.PHPLibrariesPreferencePage_editLibrary);
		edit.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		final Button remove = new Button(buttons, SWT.PUSH);
		remove.addSelectionListener(new SelectionListener()
		{

			public void widgetDefaultSelected(SelectionEvent e)
			{

			}

			public void widgetSelected(SelectionEvent e)
			{
				IStructuredSelection ss = (IStructuredSelection) newCheckList.getSelection();
				for (Object o : ss.toArray())
				{
					newCheckList.remove(o);
				}
			}

		});
		remove.setText(Messages.PHPLibrariesPreferencePage_removeLibrary);
		tableAndButton.setLayoutData(new GridData(GridData.FILL_BOTH));
		remove.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		newCheckList.addSelectionChangedListener(new ISelectionChangedListener()
		{

			public void selectionChanged(SelectionChangedEvent event)
			{
				IStructuredSelection ss = (IStructuredSelection) event.getSelection();
				if (ss.isEmpty() || ss.getFirstElement() instanceof PHPLibrary)
				{
					edit.setEnabled(false);
					remove.setEnabled(false);
					return;
				}
				edit.setEnabled(true);
				remove.setEnabled(true);
			}

		});
		Button selectAll = new Button(buttons, SWT.PUSH);
		selectAll.setText(Messages.LibrariesPage_selectAll);
		selectAll.addSelectionListener(new SelectAction(true));
		Button deselectAll = new Button(buttons, SWT.PUSH);
		deselectAll.setText(Messages.LibrariesPage_deselectAll);
		selectAll.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		deselectAll.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		deselectAll.addSelectionListener(new SelectAction(false));
		edit.setEnabled(false);
		remove.setEnabled(false);
		return body;
	}

	IPHPLibrary[] getContent()
	{
		int count = newCheckList.getTable().getItemCount();
		List<IPHPLibrary> ul = new ArrayList<IPHPLibrary>();
		for (int a = 0; a < count; a++)
		{
			Object elementAt = newCheckList.getElementAt(a);
			ul.add((IPHPLibrary) elementAt);
		}
		return ul.toArray(new IPHPLibrary[ul.size()]);
	}

	@Override
	protected void performDefaults()
	{
		newCheckList.setAllChecked(true);
	}

	@Override
	public boolean performOk()
	{
		Set<IPHPLibrary> turnedOff = new HashSet<IPHPLibrary>();
		for (IPHPLibrary l : LibraryManager.getInstance().getAllLibraries())
		{
			boolean checked = newCheckList.getChecked(l);
			if (!checked)
			{
				turnedOff.add(l);
			}
		}
		int count = newCheckList.getTable().getItemCount();
		ArrayList<UserLibrary> ul = new ArrayList<UserLibrary>();
		for (int a = 0; a < count; a++)
		{
			Object elementAt = newCheckList.getElementAt(a);
			if (elementAt instanceof UserLibrary)
			{
				ul.add((UserLibrary) elementAt);
			}
		}
		LibraryManager.getInstance().setUserLibraries(ul.toArray(new UserLibrary[ul.size()]));
		LibraryManager.getInstance().setTurnedOff(turnedOff);
		return true;
	}

	public void init(IWorkbench workbench)
	{

	}
}
