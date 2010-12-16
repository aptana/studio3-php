/*******************************************************************************
 * Copyright (c) 2006 Zend Corporation and IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org.eclipse.php.internal.debug.ui.wizard.exe;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.php.internal.debug.core.interpreter.preferences.DependenciesManager;
import org.eclipse.php.internal.debug.ui.wizard.TextDialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.aptana.debug.php.ui.phpIni.Messages;
import com.aptana.editor.php.epl.PHPEplPlugin;
import com.aptana.editor.php.internal.builder.preferences.PHPExtension;
import com.aptana.ui.util.SWTUtils;

/**
 * @deprecated since Aptana PHP 1.1
 * @author Pavel Petrochenko
 */
public class PHPExtensionsEditor
{

	/**
	 * Name property.
	 */
	private static final String NAME_PROPERTY = Messages.PHPIniEditor_3;

	/**
	 * Value property.
	 */
	private static final String VALUE_PROPERTY = Messages.PHPIniEditor_4;

	private Composite mainComposite;
	private Button addEntryButton;
	private Button removeButton;
	private CheckboxTableViewer viewer;

	/**
	 * Creates editor control.
	 * 
	 * @param parent -
	 *            parent.
	 * @return control.
	 */
	public Control createControl(Composite parent)
	{
		mainComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 0;
		mainComposite.setLayout(layout);
		mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		// creating buttons
		Control buttonsContainer = createButtonsBar(mainComposite);
		GridData buttonsContainerData = new GridData(GridData.FILL_HORIZONTAL);
		buttonsContainer.setLayoutData(buttonsContainerData);

		// creating entries viewer
		Control viewerContainer = createViewer(mainComposite);
		GridData viewerContainerData = new GridData(GridData.FILL_BOTH);
		viewerContainerData.grabExcessHorizontalSpace = true;
		viewerContainerData.grabExcessVerticalSpace = true;
		viewerContainer.setLayoutData(viewerContainerData);

		modifyButtonsStates();

		disable();

		return mainComposite;
	}

	private void modifyButtonsStates()
	{
		if (viewer.getSelection().isEmpty())
		{
			removeButton.setEnabled(false);
		}
		else
		{
			removeButton.setEnabled(true);
		}
	}

	private void disable()
	{

	}

	static class PHPExtensionLabelProvider extends LabelProvider implements ITableLabelProvider
	{

		/**
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		public Image getColumnImage(Object element, int columnIndex)
		{
			return null;
		}

		/**
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText(Object element, int columnIndex)
		{
			PHPExtension ext = (PHPExtension) element;
			if (columnIndex == 0)
			{
				return ext.getName();
			}
			if (columnIndex == 1)
			{
				return ext.getPath();
			}
			// TODO Auto-generated method stub
			return null;
		}

	}

	private Control createViewer(Composite parent)
	{
		// initializing viewer container
		Composite viewerContainer = new Composite(parent, SWT.NONE);
		viewerContainer.setLayout(new FillLayout());

		// initializing viewer
		viewer = new CheckboxTableViewer(new Table(viewerContainer, SWT.FULL_SELECTION | SWT.CHECK | SWT.BORDER));
		viewer.setColumnProperties(new String[] { NAME_PROPERTY, VALUE_PROPERTY });
		viewer.setLabelProvider(new PHPExtensionLabelProvider());
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().addListener(SWT.MeasureItem, new Listener()
		{

			public void handleEvent(Event arg0)
			{
				Object data = arg0.widget.getData("H"); //$NON-NLS-1$
				if (data == null)
				{
					arg0.widget.setData("H", this); //$NON-NLS-1$
					arg0.height = arg0.height+2;
				}
			}

		});
		// creating columns
		TableColumn nameColumn = new TableColumn(viewer.getTable(), SWT.NULL);
		nameColumn.setText(Messages.PHPExtensionsEditor_NAME);

		TableColumn valueColumn = new TableColumn(viewer.getTable(), SWT.NULL);
		valueColumn.setText(Messages.PHPExtensionsEditor_PATH);

		// creating column layout
		TableLayout tableLayout = new TableLayout();
		// TableColumnLayout columnLayout = new TableColumnLayout();
		// viewerContainer.setLayout(columnLayout);

		tableLayout.addColumnData(new ColumnWeightData(40));
		tableLayout.addColumnData(new ColumnWeightData(60));
		// columnLayout.setColumnData(valueColumn, new ColumnWeightData(60));

		// setting cell editor
		TextCellEditor valueEditor = new TextCellEditor(viewer.getTable());
		viewer.setCellEditors(new CellEditor[] { valueEditor, new TextDialogCellEditor(viewer.getTable())
		{

			@Override
			protected Object openDialogBox(Control cellEditorWindow)
			{
				return new FileDialog(cellEditorWindow.getShell(), SWT.OPEN).open();
			}

		} });
		viewer.setCellModifier(new ICellModifier()
		{

			public boolean canModify(Object element, String property)
			{
				return true;
			}

			public Object getValue(Object element, String property)
			{
				PHPExtension ext = (PHPExtension) element;
				if (property.equals(NAME_PROPERTY))
				{
					return ext.getName();
				}
				else if (property.equals(VALUE_PROPERTY))
				{
					return ext.getPath();
				}
				return null;
			}

			public void modify(Object element, String property, Object value)
			{
				PHPExtension ext = (PHPExtension) ((TableItem) element).getData();
				if (property.equals(NAME_PROPERTY))
				{
					ext.setName(value.toString());
				}
				else if (property.equals(VALUE_PROPERTY))
				{
					ext.setPath(value.toString());
				}
				viewer.refresh(ext);
				store();
			}

		});
		viewer.addSelectionChangedListener(new ISelectionChangedListener()
		{

			public void selectionChanged(SelectionChangedEvent event)
			{
				modifyButtonsStates();
			}

		});

		viewer.getTable().setLayout(tableLayout);
		// refreshing
		viewer.getTable().getParent().layout(true, true);
		List<PHPExtension> extensions = DependenciesManager.getExtensions();
		for (PHPExtension e : extensions)
		{
			viewer.add(e);
		}
		return viewerContainer;
	}

	void store()
	{
		ArrayList<PHPExtension> exts = new ArrayList<PHPExtension>();
		for (TableItem e : viewer.getTable().getItems())
		{
			exts.add((PHPExtension) e.getData());
		}
		DependenciesManager.setExtensions(exts);
	}

	private Control createButtonsBar(Composite mainComposite2)
	{
		Composite buttonsComposite = new Composite(mainComposite, SWT.NONE);
		buttonsComposite.setLayout(new GridLayout(2, false));

		addEntryButton = new Button(buttonsComposite, SWT.NONE);
		addEntryButton.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
		addEntryButton.setToolTipText(Messages.PHPIniEditor_7);
		addEntryButton.setImage(SWTUtils.getImage(PHPEplPlugin.getDefault(), "/icons/add.gif")); //$NON-NLS-1$
		addEntryButton.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent e)
			{
			}

			public void widgetSelected(SelectionEvent e)
			{
				addEntry();
				store();
			}
		});

		removeButton = new Button(buttonsComposite, SWT.NONE);
		removeButton.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
		removeButton.setToolTipText(Messages.PHPIniEditor_8);
		removeButton.setImage(SWTUtils.getImage(PHPEplPlugin.getDefault(), "/icons/delete.gif")); //$NON-NLS-1$
		removeButton.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent e)
			{
			}

			public void widgetSelected(SelectionEvent e)
			{
				removeEntry();
				store();
			}
		});
		return buttonsComposite;
	}

	/**
	 * 
	 */
	protected void removeEntry()
	{
		viewer.remove(((StructuredSelection) viewer.getSelection()).toArray());
	}

	/**
	 * 
	 */
	protected void addEntry()
	{
		FileDialog ll = new FileDialog(Display.getCurrent().getActiveShell(), SWT.OPEN);
		String open = ll.open();
		if (open != null)
		{
			PHPExtension extension = new PHPExtension(open);
			viewer.add(extension);
			viewer.setChecked(extension, true);
		}
	}

}
