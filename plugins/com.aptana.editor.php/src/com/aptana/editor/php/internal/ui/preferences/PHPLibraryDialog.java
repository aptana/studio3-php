/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.ui.preferences;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import com.aptana.editor.php.epl.PHPEplPlugin;
import com.aptana.editor.php.internal.builder.IPHPLibrary;
import com.aptana.editor.php.internal.builder.UserLibrary;

/**
 * @author Pavel Petrochenko
 */
public class PHPLibraryDialog extends TitleAreaDialog
{

	private Image titleImage;
	private Text nameText;
	private TableViewer viewer;
	private UserLibrary library;
	private IPHPLibrary[] existing;

	public PHPLibraryDialog(Shell parentShell, UserLibrary library, IPHPLibrary[] existing)
	{
		super(parentShell);
		this.library = library;
		this.existing = existing;
		titleImage = AbstractUIPlugin.imageDescriptorFromPlugin(PHPEplPlugin.PLUGIN_ID,
				"/icons/full/wizban/addlibrary_wiz.png").createImage(); //$NON-NLS-1$

	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite body = new Composite(parent, SWT.NONE);
		body.setLayout(new GridLayout(2, false));
		body.setLayoutData(new GridData(GridData.FILL_BOTH));
		Label label = new Label(body, SWT.NONE);
		label.setText(Messages.PHPLibraryDialog_libraryName);
		nameText = new Text(body, SWT.BORDER);
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		if (library != null)
		{
			nameText.setText(library.getName());
		}
		nameText.addModifyListener(new ModifyListener()
		{

			public void modifyText(ModifyEvent e)
			{
				validate();
			}

		});
		Group pComp = new Group(body, SWT.NONE);
		pComp.setText(Messages.PHPLibraryDialog_libraryContent);
		pComp.setLayout(new GridLayout(2, false));
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.horizontalSpan = 2;
		layoutData.minimumHeight = 200;
		pComp.setLayoutData(layoutData);
		viewer = new TableViewer(pComp, SWT.BORDER);
		viewer.setComparator(new ViewerComparator());
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new LabelProvider()
		{

			public String getText(Object element)
			{
				return element.toString();

			}

			public Image getImage(Object element)
			{
				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
			}
		});
		Composite buttons = new Composite(pComp, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 0;
		if (library != null)
		{
			viewer.setInput(library.getDirectories().toArray());
		}
		else
		{
			viewer.setInput(new String[0]); // $codepro.audit.disable reusableImmutables
		}
		buttons.setLayout(layout);
		Button add = new Button(buttons, SWT.NONE);
		add.setText(Messages.PHPLibraryDialog_addFolder);
		add.addSelectionListener(new SelectionListener()
		{

			public void widgetDefaultSelected(SelectionEvent e)
			{

			}

			public void widgetSelected(SelectionEvent e)
			{
				DirectoryDialog directoryDialog = new DirectoryDialog(Display.getCurrent().getActiveShell());
				directoryDialog.setText(Messages.PHPLibraryDialog_selectFolder);
				String open = directoryDialog.open();
				viewer.add(open);
			}

		});
		add.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Button remove = new Button(buttons, SWT.NONE);
		remove.addSelectionListener(new SelectionListener()
		{

			public void widgetDefaultSelected(SelectionEvent e)
			{

			}

			public void widgetSelected(SelectionEvent e)
			{
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				for (Object o : selection.toArray())
				{
					viewer.remove(o);
				}
			}

		});
		remove.setText(Messages.PHPLibraryDialog_removeSelected);
		remove.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		viewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
		Control createDialogArea = super.createDialogArea(parent);
		setMessage(Messages.PHPLibraryDialog_libraryConfigureMessage);
		setTitleImage(titleImage);
		setTitle(Messages.PHPLibraryDialog_libraryConfigureTitle);
		buttons.setLayoutData(new GridData(GridData.FILL_VERTICAL));

		return createDialogArea;
	}

	@Override
	protected Control createButtonBar(Composite parent)
	{
		Control createButtonBar = super.createButtonBar(parent);
		validate();
		return createButtonBar;
	}

	protected void validate()
	{
		String text = nameText.getText().trim();
		Button button = getButton(Dialog.OK);
		if (text.length() == 0)
		{
			setErrorMessage(Messages.PHPLibraryDialog_emptyLibraryNameError);
			button.setEnabled(false);
			return;
		}
		for (IPHPLibrary l : existing)
		{

			if (l != library && l.getId().equals(text))
			{
				setErrorMessage(Messages.PHPLibraryDialog_uniqueLibraryNameError);
				button.setEnabled(false);
				return;
			}
		}
		setErrorMessage(null);
		button.setEnabled(true);
	}

	public boolean close()
	{
		String[] dirs = new String[viewer.getTable().getItemCount()];
		for (int a = 0; a < dirs.length; a++)
		{
			dirs[a] = (String) viewer.getElementAt(a);
		}
		library = new UserLibrary(nameText.getText(), dirs);
		boolean close = super.close();
		titleImage.dispose();
		return close;
	}

	public UserLibrary getResult()
	{
		return library;
	}
}