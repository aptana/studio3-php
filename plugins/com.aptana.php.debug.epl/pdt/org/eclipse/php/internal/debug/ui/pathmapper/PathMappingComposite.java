/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zend and IBM - Initial implementation
 *******************************************************************************/
package org.eclipse.php.internal.debug.ui.pathmapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.php.debug.core.debugger.pathmapper.PathMapper;
import org.eclipse.php.debug.core.debugger.pathmapper.PathMapper.Mapping;
import org.eclipse.php.internal.debug.core.pathmapper.PathEntry.Type;
import org.eclipse.php.internal.ui.wizard.field.ListDialogField;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org2.eclipse.php.internal.core.util.ScrolledCompositeImpl;

import com.aptana.editor.php.internal.ui.PHPPluginImages;
import com.aptana.editor.php.internal.ui.wizard.field.IListAdapter;
import com.aptana.ui.util.StatusLineMessageTimerManager;


public class PathMappingComposite extends Composite {

	public static final int IDX_ADD = 0;
	public static final int IDX_EDIT = 1;
	public static final int IDX_REMOVE = 2;
	private static final String[] buttonLabels = { "&Add", "&Edit", "&Remove" };
	private static final String[] columnHeaders = { "Path on server", "Local path" };
	private static final ColumnLayoutData[] columnLayoutDatas = new ColumnLayoutData[] { new ColumnWeightData(50), new ColumnWeightData(50) };
	private List<IPropertyListener> listeners = new ArrayList<IPropertyListener>(3);
	private ListDialogField fMapList;
	private boolean serverIsValid;
	private final boolean isForEdit;

	/**
	 * PathMappingComposite constructor.
	 * By default, the composite will be used for editing as well. 
	 * 
	 * @param parent
	 * @param style
	 */
	public PathMappingComposite(Composite parent, int style) {
		this(parent, style, true);
	}
	
	/**
	 * PathMappingComposite constructor.
	 * 
	 * @param parent
	 * @param style
	 * @param isForEdit Allow or disallow editing for the table's content.
	 */
	public PathMappingComposite(Composite parent, int style, boolean isForEdit) {
		super(parent, style);
		this.isForEdit = isForEdit;
		initializeControls();
	}

	protected void initializeControls() {
		fMapList = new ListDialogField(new ListAdapter(), isForEdit ? buttonLabels : null, new LabelProvider());
		// fMapList.setRemoveButtonIndex(IDX_REMOVE); // SG: Aptana modification - Should not set the index, so that the custom remove method will be called.
		fMapList.setTableColumns(new ListDialogField.ColumnsDescription(columnLayoutDatas, columnHeaders, true));

		GridLayout layout = new GridLayout();
		setLayout(layout);
		setLayoutData(new GridData(GridData.FILL_BOTH));

		PixelConverter conv = new PixelConverter(this);

		ScrolledCompositeImpl scrolledCompositeImpl = new ScrolledCompositeImpl(this, SWT.V_SCROLL | SWT.H_SCROLL);
		scrolledCompositeImpl.setLayout(layout);
		scrolledCompositeImpl.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite composite = new Composite(scrolledCompositeImpl, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;
		composite.setLayout(layout);
		scrolledCompositeImpl.setContent(composite);
		scrolledCompositeImpl.setFont(getFont());

		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = conv.convertWidthInCharsToPixels(50);
		Control listControl = fMapList.getListControl(composite);
		listControl.setLayoutData(data);

		Control buttonsControl = fMapList.getButtonBox(composite);
		buttonsControl.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));

		Point size = composite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		scrolledCompositeImpl.setMinSize(size.x, size.y);

		if (!isForEdit)
		{
			Table table = fMapList.getTableViewer().getTable();
			table.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
			table.setLinesVisible(true);
			table.setForeground(getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
			table.addMouseListener(new MouseAdapter()
			{
				public void mouseDoubleClick(MouseEvent e)
				{
					Display.getDefault().beep();
					StatusLineMessageTimerManager.setErrorMessage("Please click link above to edit the path mapping",
							3000, true);
				}
			});
		}
		updateButtonsEnablement();
	}

	/**
	 * Add a property change listener that will be notified when mappings were added, modified or delete.
	 * The returned id for the properyChange event can be: {@value #IDX_ADD}, {@value #IDX_EDIT} or {@value #IDX_REMOVE}.
	 * 
	 * @param listener
	 */
	public void addPropertyChangeListener(IPropertyListener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}
	
	/**
	 * Remove a property change listener.
	 * @param listener
	 */
	public void removePropertyChangeListener(IPropertyListener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Notifies a propertyChange for all the listener.
	 */
	protected void notifyPropertyChanged(int propertyID) {
		IPropertyListener[] fireAt = listeners.toArray(new IPropertyListener[listeners.size()]);
		for (IPropertyListener listener : fireAt) {
			listener.propertyChanged(this, propertyID);
		}
	}
	
	protected void handleAdd() {
		PathMapperEntryDialog dialog = new PathMapperEntryDialog(getShell());
		if (dialog.open() == Window.OK) {
			Mapping mapping = dialog.getResult();
			fMapList.addElement(mapping);
			notifyPropertyChanged(IDX_ADD);
		}
	}

	@SuppressWarnings("unchecked")
	protected void handleEdit() {
		List l = fMapList.getSelectedElements();
		if (l.size() == 1) {
			Mapping oldElement = (Mapping) l.get(0);
			PathMapperEntryDialog dialog = new PathMapperEntryDialog(getShell(), oldElement);
			if (dialog.open() == Window.OK) {
				Mapping newElement = dialog.getResult();
				if (!oldElement.equals(newElement)) {
					fMapList.replaceElement(oldElement, newElement);
					notifyPropertyChanged(IDX_EDIT);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected void handleRemove() {
		fMapList.removeElements(fMapList.getSelectedElements());
		notifyPropertyChanged(IDX_REMOVE);
	}

	/**
	 * Accepts only Mapping[] type
	 */
	public void setData(Object data) {
		Mapping[] mappings = null;
		if (data == null) {
			mappings = new Mapping[0];
		} else if (!(data instanceof Mapping[])) {
			throw new IllegalArgumentException("Data must be instance of Mapping[]");
		} else {
			mappings = (Mapping[]) data;
		}
		fMapList.setElements(Arrays.asList(mappings));
		updateButtonsEnablement();
	}

	@SuppressWarnings("unchecked")
	public Mapping[] getMappings() {
		List l = fMapList.getElements();
		return (Mapping[]) l.toArray(new Mapping[l.size()]);
	}

	/**
	 * Indicate that the server that we are working on exists and valid. 
	 * In any other case, the composite's buttons will be disabled.
	 * 
	 * @param serverIsValid
	 */
	public void setServerIsValid(boolean serverIsValid) {
		this.serverIsValid = serverIsValid;
	}
	
	protected void updateButtonsEnablement() {
		List<?> selectedElements = fMapList.getSelectedElements();
		// SG: Aptana modification
		// Check that there is a defined server as a Data instance
		fMapList.enableButton(IDX_ADD, serverIsValid);
		fMapList.enableButton(IDX_EDIT, serverIsValid && selectedElements.size() == 1);
		fMapList.enableButton(IDX_REMOVE, serverIsValid && selectedElements.size() > 0);
	}

	class ListAdapter implements IListAdapter {
		public void customButtonPressed(ListDialogField field, int index) {
			switch (index) {
				case IDX_ADD:
					handleAdd();
					break;
				case IDX_EDIT:
					handleEdit();
					break;
				case IDX_REMOVE:
					handleRemove();
					break;
			}
		}

		public void doubleClicked(ListDialogField field) {
			if (isForEdit) { // SG: Aptana mod.
				handleEdit();
			}
		}

		public void selectionChanged(ListDialogField field) {
			updateButtonsEnablement();
		}
	}

	class LabelProvider extends org.eclipse.jface.viewers.LabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			if (columnIndex == 1) { // local path
				PathMapper.Mapping mapping = (PathMapper.Mapping) element;
				if (mapping.type == Type.EXTERNAL) {
					return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
				}
				if (mapping.type == Type.INCLUDE_VAR) {
					return PHPPluginImages.get(PHPPluginImages.IMG_OBJS_ENV_VAR);
				}
				if (mapping.type == Type.INCLUDE_FOLDER) {
					return PHPPluginImages.get(PHPPluginImages.IMG_OBJS_LIBRARY);
				}
				IResource resource = ResourcesPlugin.getWorkspace().getRoot().findMember(mapping.localPath.toString());
				if (resource != null) {
					return WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider().getImage(resource);
				}
			}
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			PathMapper.Mapping mapping = (PathMapper.Mapping) element;
			switch (columnIndex) {
				case 0:
					return mapping.remotePath.toString();
				case 1:
					return mapping.localPath.toString();
			}
			return null;
		}
	}
}
