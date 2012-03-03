/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.ui.phpIni;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.forms.widgets.FormText;

import com.aptana.editor.php.epl.PHPEplPlugin;
import com.aptana.php.debug.PHPDebugPlugin;
import com.aptana.ui.util.SWTUtils;
import com.jcraft.jsch.IO;

/**
 * PHP.ini editor.
 * 
 * @author Denis Denisenko, Shalom Gibly
 */
public class PHPIniEditor
{

	/**
	 * Comment icon.
	 */
	private static final Image COMMENT_ICON = SWTUtils.getImage(PHPEplPlugin.getDefault(), "/icons/comment.gif"); //$NON-NLS-1$

	/**
	 * Uncomment icon.
	 */
	private static final Image UNCOMMENT_ICON = SWTUtils.getImage(PHPEplPlugin.getDefault(), "/icons/uncomment.gif"); //$NON-NLS-1$

	/**
	 * Extensions validation icon.
	 */
	private static final Image VALIDATE_ICON = SWTUtils.getImage(PHPEplPlugin.getDefault(), "/icons/validate.gif"); //$NON-NLS-1$

	/*
	 * Validation tool-tips constants
	 */
	private static final String VALIDATION_OK_TOOLTIP = "<form><p><span font=\"header\">{0}:</span></p><BR/>" //$NON-NLS-1$
			+ "<li style=\"text\" bindent=\"5\" indent=\"5\">The extension is functional</li></form>"; //$NON-NLS-1$
	private static final String VALIDATION_ERROR_TOOLTIP = "<form><p><span font=\"header\">{0}:</span></p><BR/>" //$NON-NLS-1$
			+ "<li style=\"text\" bindent=\"5\" indent=\"5\">The extension is non functional<BR/><BR/>PHP reports:<BR/>{1}</li></form>"; //$NON-NLS-1$
	private static final String VALIDATION_WARNING_TOOLTIP = "<form><p><span font=\"header\">{0}:</span></p><BR/>" //$NON-NLS-1$
			+ "<li style=\"text\" bindent=\"5\" indent=\"5\">The extension is functional but with a warning<BR/><BR/>PHP reports:<BR/>{1}</li></form>"; //$NON-NLS-1$
	private static final String VALIDATION_UNKNOWN_TOOLTIP = "<form><p><span font=\"header\">{0}:</span></p><BR/>" //$NON-NLS-1$
			+ "<li style=\"text\" bindent=\"5\" indent=\"5\">The extension state is unknown.<br/><br/>" //$NON-NLS-1$
			+ "Please click the 'Validate Extensions' button to resolve its state</li></form>"; //$NON-NLS-1$

	/**
	 * Entry name column index.
	 */
	private static final int ENTRY_NAME_INDEX = 0;

	/**
	 * Entry value column index.
	 */
	private static final int ENTRY_VALUE_INDEX = 1;

	/**
	 * Entry value column index.
	 */
	private static final int ENTRY_VALID_INDEX = 2;

	private final static Image TREE_ICON = SWTUtils.getImage(PHPEplPlugin.getDefault(), "/icons/tree_mode.gif"); //$NON-NLS-1$

	private final static Image VALIDATION_OK_ICON = SWTUtils.getImage(PHPEplPlugin.getDefault(),
			"/icons/validation_ok.gif"); //$NON-NLS-1$

	private final static Image VALIDATION_ERROR_ICON = SWTUtils.getImage(PHPEplPlugin.getDefault(),
			"/icons/validation_err.gif"); //$NON-NLS-1$

	private final static Image VALIDATION_WARNING_ICON = SWTUtils.getImage(PHPEplPlugin.getDefault(),
			"/icons/validation_warn.gif"); //$NON-NLS-1$

	private final static Image VALIDATION_UNKNOWN_ICON = SWTUtils.getImage(PHPEplPlugin.getDefault(),
			"/icons/validation_unknown.gif"); //$NON-NLS-1$

	/**
	 * Comment foreground color.
	 */
	private final Color COMMENT_FOREGROUND;

	/**
	 * Section background color.
	 */
	private final Color SECTION_BACKGROUND;

	/**
	 * Comment font.
	 */
	private final Font COMMENT_FONT;

	/**
	 * Name property.
	 */
	private static final String NAME_PROPERTY = Messages.PHPIniEditor_3;

	/**
	 * Value property.
	 */
	private static final String VALUE_PROPERTY = Messages.PHPIniEditor_4;

	/**
	 * Extension valid property.
	 */
	private static final String VALID_EXTENSION_PROPERTY = Messages.PHPIniEditor_20;

	/**
	 * PHP Ini label provider.
	 * 
	 * @author Denis Denisenko
	 */
	class PHPIniLabelProvider implements ITableLabelProvider, IColorProvider, IFontProvider
	{

		/**
		 * {@inheritDoc}
		 */
		public Image getColumnImage(Object element, int columnIndex)
		{
			if (columnIndex == ENTRY_NAME_INDEX && element instanceof INIFileSection)
			{
				return TREE_ICON;
			}
			if (columnIndex == ENTRY_VALID_INDEX && element instanceof PHPIniEntry)
			{
				return getEntryValidationColumnImage((PHPIniEntry) element);
			}
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public String getColumnText(Object element, int columnIndex)
		{
			if (element instanceof PHPIniEntry)
			{
				PHPIniEntry entry = (PHPIniEntry) element;

				if (columnIndex == ENTRY_NAME_INDEX)
				{
					return entry.getKey();
				}
				else if (columnIndex == ENTRY_VALUE_INDEX)
				{
					return entry.getValue();
				}
			}
			else if (element instanceof INIFileSection && columnIndex == ENTRY_NAME_INDEX)
			{
				return ((INIFileSection) element).getName();
			}

			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public void addListener(ILabelProviderListener listener)
		{
		}

		/**
		 * {@inheritDoc}
		 */
		public void dispose()
		{
		}

		/**
		 * {@inheritDoc}
		 */
		public boolean isLabelProperty(Object element, String property)
		{
			return false;
		}

		/**
		 * {@inheritDoc}
		 */
		public void removeListener(ILabelProviderListener listener)
		{
		}

		/**
		 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
		 */
		public Color getBackground(Object element)
		{
			return null;
		}

		/**
		 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
		 */
		public Color getForeground(Object element)
		{
			if (element instanceof PHPIniEntry)
			{
				if (((PHPIniEntry) element).getCommented())
				{
					return COMMENT_FOREGROUND;
				}
			}
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public Font getFont(Object element)
		{
			if (element instanceof PHPIniEntry)
			{
				if (((PHPIniEntry) element).getCommented())
				{
					return COMMENT_FONT;
				}
			}

			return null;
		}
	}

	/*
	 * Returns the image for the entry.
	 */
	private Image getEntryValidationColumnImage(PHPIniEntry entry)
	{
		if (entry.isExtensionEntry())
		{
			switch (entry.getValidationState())
			{
				case PHPIniEntry.PHP_EXTENSION_VALIDATION_OK:
					return VALIDATION_OK_ICON;
				case PHPIniEntry.PHP_EXTENSION_VALIDATION_ERROR:
					return VALIDATION_ERROR_ICON;
				case PHPIniEntry.PHP_EXTENSION_VALIDATION_WARNING:
					return VALIDATION_WARNING_ICON;
				default:
					return VALIDATION_UNKNOWN_ICON;
			}
		}
		return null;
	}

	/*
	 * Returns the image for the entry.
	 */
	private Image getEntryValidationTooltipImage(PHPIniEntry entry)
	{
		if (entry.isExtensionEntry())
		{
			switch (entry.getValidationState())
			{
				case PHPIniEntry.PHP_EXTENSION_VALIDATION_OK:
					return getSWTImage(SWT.ICON_INFORMATION);
				case PHPIniEntry.PHP_EXTENSION_VALIDATION_ERROR:
					return getSWTImage(SWT.ICON_ERROR);
				case PHPIniEntry.PHP_EXTENSION_VALIDATION_WARNING:
					return getSWTImage(SWT.ICON_WARNING);
				default:
					return getSWTImage(SWT.ICON_QUESTION);
			}
		}
		return null;
	}

	/**
	 * Get an <code>Image</code> from the provide SWT image constant.
	 * 
	 * @param imageID
	 *            the SWT image constant
	 * @return image the image
	 */
	private Image getSWTImage(final int imageID)
	{
		Shell shell = viewer.getControl().getShell();
		final Display display;
		if (shell == null)
		{
			display = Display.getCurrent();
		}
		else
		{
			display = shell.getDisplay();
		}

		final Image[] image = new Image[1];
		display.syncExec(new Runnable()
		{
			public void run()
			{
				image[0] = display.getSystemImage(imageID);
			}
		});

		return image[0];

	}

	class PHPIniCellModifier implements ICellModifier
	{

		/**
		 * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
		 */
		public boolean canModify(Object element, String property)
		{
			if (element != null && element instanceof PHPIniEntry && VALUE_PROPERTY.equals(property))
			{
				PHPIniEntry entry = (PHPIniEntry) element;
				if (!entry.getCommented())
				{
					return true;
				}
			}

			return false;
		}

		public Object getValue(Object element, String property)
		{
			if (element != null && element instanceof PHPIniEntry && VALUE_PROPERTY.equals(property))
			{
				PHPIniEntry entry = (PHPIniEntry) element;
				if (!entry.getCommented())
				{
					return entry.getValue();
				}
			}

			return null;
		}

		public void modify(Object element, String property, Object value)
		{
			if (element != null && element instanceof TreeItem && ((TreeItem) element).getData() != null
					&& ((TreeItem) element).getData() instanceof PHPIniEntry && VALUE_PROPERTY.equals(property))
			{
				PHPIniEntry entry = (PHPIniEntry) (((TreeItem) element).getData());
				if (!entry.getCommented() && !value.equals(entry.getValue()))
				{
					modifyEntry(entry, value.toString());
					// entry.setValue(value.toString());
					if (viewer != null)
					{
						viewer.refresh(true);
						viewer.getTree().getParent().layout(true, true);
					}
				}
			}
		}
	}

	/**
	 * Viewer.
	 */
	private TreeViewer viewer;

	/**
	 * Current content provider.
	 */
	private PHPIniContentProvider provider;

	/**
	 * Add entry button.
	 */
	private Button addEntryButton;

	/**
	 * Remove button.
	 */
	private Button removeButton;

	/**
	 * Main composite.
	 */
	private Composite mainComposite;

	/**
	 * Whether editor is enabled.
	 */
	private boolean isEnabled;

	/**
	 * Add section button.
	 */
	private Button addSectionButton;

	private Button commentButton;

	private Button validateButton;

	private String phpExePath;

	private boolean isShowingExtensionsOnly;

	private ViewerFilter extensionsOnlyFilter;

	private String debuggerID;

	public PHPIniEditor()
	{
		COMMENT_FOREGROUND = new Color(Display.getCurrent(), 0, 128, 0);
		SECTION_BACKGROUND = new Color(Display.getCurrent(), 203, 243, 243);
		Font defaultFont = JFaceResources.getDefaultFont();
		FontData data = new FontData(defaultFont.getFontData()[0].getName(), defaultFont.getFontData()[0].getHeight(),
				SWT.ITALIC);
		COMMENT_FONT = new Font(Display.getDefault(), data);
		extensionsOnlyFilter = new ExtensionsOnlyFilter();
	}

	/**
	 * Set the PHP executable path. (The executable is needed for the extensions validation process)
	 * 
	 * @param path
	 */
	public void setPHPExe(String path)
	{
		this.phpExePath = path;
	}

	/**
	 * Set the debugger id.
	 * 
	 * @param debuggerID
	 */
	public void setDebuggerID(String debuggerID)
	{
		this.debuggerID = debuggerID;
	}

	/**
	 * Opens ini file.
	 * 
	 * @param fileName
	 *            - file name.
	 * @throws IOException
	 *             IF IO error occurs.
	 */
	public void openFile(String fileName) throws IOException
	{
		provider = new PHPIniContentProvider(fileName);
		viewer.setContentProvider(provider);
		viewer.setInput(new Object());
		viewer.refresh();
		viewer.getTree().getParent().layout(true, true);
		enable();
		modifyButtonsStates();
	}

	/**
	 * Saves current ini file.
	 * 
	 * @throws IO
	 *             exception IF IO error occurs
	 */
	public void save() throws IOException
	{
		if (isEnabled() && provider != null)
		{
			provider.save();
		}
	}

	/**
	 * Gets current file name.
	 * 
	 * @return current file name.
	 */
	public String getFileName()
	{
		if (provider != null)
		{
			return provider.getFileName();
		}

		return null;
	}

	public void showExtensionsOnly(boolean show)
	{
		if (isShowingExtensionsOnly != show)
		{
			isShowingExtensionsOnly = show;
			if (viewer != null)
			{
				if (isShowingExtensionsOnly)
				{
					viewer.addFilter(extensionsOnlyFilter);
					viewer.expandAll();
				}
				else
				{
					viewer.removeFilter(extensionsOnlyFilter);
				}
			}
		}
	}

	/**
	 * Creates editor control.
	 * 
	 * @param parent
	 *            - parent.
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

		// Add the extensions filter check-box
		final Button onlyExtensionsCheck = new Button(mainComposite, SWT.CHECK);
		onlyExtensionsCheck.setText(Messages.PHPIniEditor_showExtensionOnlyButton);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalIndent = 5;
		onlyExtensionsCheck.setLayoutData(layoutData);
		onlyExtensionsCheck.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent e)
			{
				showExtensionsOnly(onlyExtensionsCheck.getSelection());
			}
		});

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

	/**
	 * Creates entries viewer.
	 * 
	 * @param parent
	 *            - parent.
	 * @return viewer parent control.
	 */
	private Control createViewer(Composite parent)
	{
		// initializing viewer container
		Composite viewerContainer = new Composite(parent, SWT.NONE);
		viewerContainer.setLayout(new FillLayout());

		// initializing viewer
		viewer = new TreeViewer(viewerContainer, SWT.FULL_SELECTION | SWT.BORDER);
		viewer.setColumnProperties(new String[] { NAME_PROPERTY, VALUE_PROPERTY });
		viewer.setLabelProvider(new PHPIniLabelProvider());
		viewer.getTree().setHeaderVisible(true);
		viewer.setUseHashlookup(true);

		// creating columns
		TreeColumn nameColumn = new TreeColumn(viewer.getTree(), SWT.NULL);
		nameColumn.setText(Messages.PHPIniEditor_5);

		TreeColumn valueColumn = new TreeColumn(viewer.getTree(), SWT.NULL);
		valueColumn.setText(Messages.PHPIniEditor_6);

		TreeColumn validityColumn = new TreeColumn(viewer.getTree(), SWT.NULL);
		validityColumn.setText(Messages.PHPIniEditor_21);

		// creating column layout
		TableLayout columnLayout = new TableLayout();
		// viewerContainer.setLayout(columnLayout);

		columnLayout.addColumnData(new ColumnWeightData(40));
		columnLayout.addColumnData(new ColumnWeightData(50));
		columnLayout.addColumnData(new ColumnWeightData(10));

		// setting cell editor
		TextCellEditor valueEditor = new TextCellEditor(viewer.getTree());
		viewer.setCellEditors(new CellEditor[] { null, valueEditor, null });

		// setting cell modifier
		viewer.setCellModifier(new PHPIniCellModifier());

		// setting selection listener
		viewer.addSelectionChangedListener(new ISelectionChangedListener()
		{

			public void selectionChanged(SelectionChangedEvent event)
			{
				modifyButtonsStates();
			}

		});

		viewer.getTree().addControlListener(new ControlListener()
		{
			int times;

			public void controlMoved(ControlEvent e)
			{
			}

			public void controlResized(ControlEvent e)
			{
				// found no way to make correct layouting other then this
				times++;
				if (times <= 2)
				{
					TableLayout columnLayout = new TableLayout();
					columnLayout.addColumnData(new ColumnWeightData(40));
					columnLayout.addColumnData(new ColumnWeightData(50));
					columnLayout.addColumnData(new ColumnWeightData(10));
					viewer.getTree().setLayout(columnLayout);
					viewer.getTree().layout(true);
				}
			}

		});

		// Set the viewer filter, if needed
		if (isShowingExtensionsOnly)
		{
			viewer.addFilter(extensionsOnlyFilter);
		}

		// refreshing
		viewer.getTree().setLayout(columnLayout);
		viewer.getTree().getParent().layout(true, true);

		// Add tool-tip support
		new PHPExtensionsTooltip(viewer.getTree());

		return viewerContainer;
	}

	private class PHPExtensionsTooltip extends ToolTip
	{

		public PHPExtensionsTooltip(Control control)
		{
			super(control);
		}

		/**
		 * Create a tooltip for entries that describes active extensions in the ini.
		 */
		protected boolean shouldCreateToolTip(Event event)
		{
			if (super.shouldCreateToolTip(event))
			{
				TreeItem treeItem = viewer.getTree().getItem(new Point(event.x, event.y));
				return treeItem != null && treeItem.getData() instanceof PHPIniEntry
						&& ((PHPIniEntry) treeItem.getData()).isExtensionEntry();
			}
			return false;
		}

		protected Composite createToolTipContentArea(Event event, Composite parent)
		{
			TreeItem treeItem = viewer.getTree().getItem(new Point(event.x, event.y));
			PHPIniEntry entry = (PHPIniEntry) treeItem.getData();
			if (!entry.isExtensionEntry())
			{
				return null;
			}
			Composite sm = new Composite(parent, SWT.NONE);
			sm.setLayout(GridLayoutFactory.fillDefaults().margins(10, 10).create());
			FormText ts = new FormText(sm, SWT.NONE);
			// ts.setImage("entry", getEntryValidationTooltipImage(entry)); //$NON-NLS-1$  
			ts.setWhitespaceNormalized(true);
			ts.setFont("header", JFaceResources.getHeaderFont()); //$NON-NLS-1$
			ts.setText(getEntryTooltipInfo(entry), true, false);
			Point size = sm.computeSize(-1, -1);
			if (size != null && size.x > 0 && size.y > 0)
			{
				ts.setLayoutData(new GridData(Math.min(500, size.x), size.y + 5));
			}
			else
			{
				ts.setLayoutData(new GridData(200, 40));
			}
			return sm;
		}
	}

	/*
	 * Returns the tool-tip text for the given PHPIniEntry.
	 */
	private String getEntryTooltipInfo(PHPIniEntry entry)
	{
		switch (entry.getValidationState())
		{
			case PHPIniEntry.PHP_EXTENSION_VALIDATION_OK:
				return MessageFormat.format(VALIDATION_OK_TOOLTIP, entry.getValue());
			case PHPIniEntry.PHP_EXTENSION_VALIDATION_ERROR:
				return MessageFormat.format(VALIDATION_ERROR_TOOLTIP, entry.getValue(), entry.getValidationNote());
			case PHPIniEntry.PHP_EXTENSION_VALIDATION_WARNING:
				return MessageFormat.format(VALIDATION_WARNING_TOOLTIP, entry.getValue(), entry.getValidationNote());
			default:
				return MessageFormat.format(VALIDATION_UNKNOWN_TOOLTIP, entry.getValue());
		}
	}

	/**
	 * Creates buttons bar.
	 * 
	 * @param mainComposite
	 * @return buttons parent control.
	 */
	private Control createButtonsBar(Composite mainComposite)
	{
		Composite buttonsComposite = new Composite(mainComposite, SWT.NONE);
		buttonsComposite.setLayout(new GridLayout(5, true));

		addEntryButton = new Button(buttonsComposite, SWT.NONE);
		addEntryButton.setText(Messages.PHPIniEditor_Entry);
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
			}
		});

		removeButton = new Button(buttonsComposite, SWT.NONE);
		removeButton.setText(Messages.PHPIniEditor_Delete);
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
			}
		});

		addSectionButton = new Button(buttonsComposite, SWT.NONE);
		addSectionButton.setText(Messages.PHPIniEditor_Section);
		addSectionButton.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
		addSectionButton.setToolTipText(Messages.PHPIniEditor_9);
		addSectionButton.setImage(SWTUtils.getImage(PHPEplPlugin.getDefault(), "/icons/add_section.gif")); //$NON-NLS-1$
		addSectionButton.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent e)
			{
			}

			public void widgetSelected(SelectionEvent e)
			{
				addSection();
			}
		});

		commentButton = new Button(buttonsComposite, SWT.NONE);
		commentButton.setText(Messages.PHPIniEditor_Comment);
		commentButton.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
		commentButton.setToolTipText(Messages.PHPIniEditor_10);
		commentButton.setImage(COMMENT_ICON);
		commentButton.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent e)
			{
			}

			public void widgetSelected(SelectionEvent e)
			{
				comment();
			}

		});

		validateButton = new Button(buttonsComposite, SWT.NONE);
		validateButton.setText(Messages.PHPIniEditor_Validate);
		validateButton.setLayoutData(new GridData(GridData.FILL, GridData.FILL, false, false));
		validateButton.setToolTipText(Messages.PHPIniEditor_22);
		validateButton.setImage(VALIDATE_ICON);
		validateButton.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent e)
			{
			}

			public void widgetSelected(SelectionEvent e)
			{
				validateExtensions();
			}

		});

		return buttonsComposite;
	}

	/**
	 * Disposes the editor.
	 */
	public void dispose()
	{
		if (provider != null)
		{
			provider.dispose();
		}

		COMMENT_FOREGROUND.dispose();
		SECTION_BACKGROUND.dispose();
		COMMENT_FONT.dispose();
	}

	public void modifyEntry(PHPIniEntry entry, String value)
	{
		if (provider != null)
		{
			provider.modifyEntry(entry, value);
		}
	}

	/**
	 * Enables editor.
	 */
	public void enable()
	{
		if (mainComposite != null)
		{
			if (provider != null)
			{
				isEnabled = true;
				enableAll(mainComposite);
			}
		}
	}

	/**
	 * Disables editor.
	 */
	public void disable()
	{
		if (mainComposite != null)
		{
			isEnabled = false;
			disableAll(mainComposite);
		}
	}

	/**
	 * Gets whether editor is enabled.
	 * 
	 * @return whether editor is enabled.
	 */
	public boolean isEnabled()
	{
		return isEnabled;
	}

	private void addEntry()
	{
		ISelection selection = viewer.getSelection();
		if (selection instanceof IStructuredSelection)
		{
			Object element = ((IStructuredSelection) selection).getFirstElement();
			INIFileSection section = null;
			if (element instanceof PHPIniEntry || element instanceof INIFileSection)
			{
				NewPHPIniEntryDialog dialog = new NewPHPIniEntryDialog(viewer.getTree().getShell());
				if (dialog.open() == Window.OK)
				{
					String name = dialog.getName();
					String value = dialog.getValue();
					if (element instanceof INIFileSection)
					{
						section = (INIFileSection) element;
						provider.insertEntryToSectionBeginning(section, name, value);
						viewer.reveal(section);
						viewer.expandToLevel(section, 1);
					}
					else if (element instanceof PHPIniEntry)
					{
						provider.insertEntry((PHPIniEntry) element, name, value);
					}

					viewer.refresh(true);
					viewer.getTree().getParent().layout(true, true);
				}
			}
			else
			{
				NewPHPIniEntryDialog dialog = new NewPHPIniEntryDialog(viewer.getTree().getShell());
				if (dialog.open() == Window.OK)
				{
					String name = dialog.getName();
					String value = dialog.getValue();

					section = provider.getGlobalSection();
					provider.insertEntryToSectionBeginning(section, name, value);
					viewer.reveal(section);
					viewer.refresh(true);
					viewer.expandToLevel(section, 1);
					viewer.getTree().getParent().layout(true, true);
				}
			}

		}
	}

	/**
	 * Adds new section.
	 */
	private void addSection()
	{
		NewPHPIniSectionDialog dialog = new NewPHPIniSectionDialog(viewer.getTree().getShell());
		List<String> sectionsNames = new ArrayList<String>();
		for (INIFileSection section : provider.getSections())
		{
			sectionsNames.add(section.getName());
		}
		dialog.setForbiddenNames(sectionsNames);

		if (dialog.open() == Window.OK)
		{
			String name = dialog.getName();
			INIFileSection section = provider.addSection(name);
			viewer.refresh(true);
			viewer.reveal(section);
			viewer.getTree().getParent().layout(true, true);
		}
	}

	/**
	 * Removes entry.
	 */
	private void removeEntry()
	{
		ISelection selection = viewer.getSelection();
		if (selection instanceof IStructuredSelection)
		{
			Object element = ((IStructuredSelection) selection).getFirstElement();

			if (element instanceof PHPIniEntry)
			{
				provider.removeEntry(((PHPIniEntry) element));
				viewer.refresh(true);
				viewer.getTree().getParent().layout(true, true);
			}
			else if (element instanceof INIFileSection)
			{
				INIFileSection section = (INIFileSection) element;
				if (section.equals(provider.getGlobalSection()))
				{
					MessageDialog.openInformation(viewer.getTree().getShell(), Messages.PHPIniEditor_11,
							Messages.PHPIniEditor_12);
					return;
				}
				boolean result = MessageDialog.openQuestion(viewer.getTree().getShell(), Messages.PHPIniEditor_13,
						Messages.PHPIniEditor_14 + section.getName() + Messages.PHPIniEditor_15);
				if (result)
				{
					provider.removeSection(section);
					viewer.refresh(true);
					viewer.getTree().getParent().layout(true, true);
				}
			}
			else
			{
				return;
			}
		}
	}

	/**
	 * Comments or uncomments.
	 */
	private void comment()
	{
		ISelection selection = viewer.getSelection();
		if (selection instanceof IStructuredSelection)
		{
			Object element = ((IStructuredSelection) selection).getFirstElement();

			if (element instanceof PHPIniEntry)
			{
				PHPIniEntry entry = ((PHPIniEntry) element);
				if (entry.getCommented())
				{
					provider.uncommentEntry(entry);
				}
				else
				{
					provider.commentEntry(entry);
				}

				viewer.refresh(true);
				viewer.reveal(entry);
				viewer.getTree().getParent().layout(true, true);
			}
			else if (element instanceof INIFileSection)
			{
				INIFileSection section = (INIFileSection) element;
				if (allEntriesAreCommented(section))
				{
					for (PHPIniEntry entry : section.getEntries())
					{
						provider.uncommentEntry(entry);
					}
				}
				else if (allEntriesAreUnCommented(section))
				{
					for (PHPIniEntry entry : section.getEntries())
					{
						provider.commentEntry(entry);
					}
				}

				viewer.refresh(true);
				viewer.expandToLevel(section, 1);
				viewer.getTree().getParent().layout(true, true);
			}
		}
	}

	/**
	 * Validate the PHP extensions.
	 * 
	 * @return True, if a validation process was initiated; False otherwise. A validation will not be initiated unless
	 *         the {@link #openFile(String)} was called before.
	 */
	public boolean validateExtensions()
	{
		if (provider == null)
			return false;
		if (provider.isDirtyINI())
		{
			// Inform the user that the ini should be saved before the validation starts
			if (!MessageDialog.openQuestion(viewer.getTree().getShell(), Messages.PHPIniEditor_extensionValidatorTitle,
					Messages.PHPIniEditor_extensionValidatorQuestion))
			{
				return false;
			}
			else
			{
				try
				{
					provider.save();
				}
				catch (IOException e)
				{
					MessageDialog.openError(viewer.getTree().getShell(), Messages.PHPIniEditor_errorTitle,
							Messages.PHPIniEditor_errorSavingIniMessage);
					PHPDebugPlugin.logError("Error saving the php.ini", e); //$NON-NLS-1$

				}
			}
		}
		// Expand the tree
		Display.getDefault().asyncExec(new Runnable()
		{
			public void run()
			{
				viewer.expandAll();
			}
		});
		// Validate the extensions
		(new PHPIniValidator(provider, phpExePath, debuggerID)).validate();

		// Refresh
		viewer.refresh(true);
		viewer.getTree().getParent().layout(true, true);
		return true;
	}

	/**
	 * Disables the control and all it's children recursively.
	 * 
	 * @param control
	 *            - control to disable.
	 */
	private void disableAll(Control control)
	{
		if (control instanceof Composite)
		{
			Control[] children = ((Composite) control).getChildren();
			if (children != null && children.length > 0)
			{
				for (Control child : children)
				{
					disableAll(child);
				}
			}
		}

		control.setEnabled(false);
	}

	/**
	 * Enables the control and all it's children recursively.
	 * 
	 * @param control
	 *            - control to disable.
	 */
	private void enableAll(Control control)
	{
		if (control instanceof Composite)
		{
			Control[] children = ((Composite) control).getChildren();
			if (children != null && children.length > 0)
			{
				for (Control child : children)
				{
					enableAll(child);
				}
			}
		}

		control.setEnabled(true);
	}

	/**
	 * Modifies buttons states.
	 */
	private void modifyButtonsStates()
	{
		addEntryButton.setEnabled(true);
		addSectionButton.setEnabled(true);

		ISelection selection = viewer.getSelection();
		if (selection != null && selection instanceof IStructuredSelection)
		{
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element != null)
			{
				removeButton.setEnabled(true);
				if (element instanceof PHPIniEntry)
				{
					if (((PHPIniEntry) element).getCommented())
					{
						commentButton.setImage(UNCOMMENT_ICON);
						commentButton.setToolTipText(Messages.PHPIniEditor_16);
					}
					else
					{
						commentButton.setImage(COMMENT_ICON);
						commentButton.setToolTipText(Messages.PHPIniEditor_17);
					}
					commentButton.setEnabled(true);
				}
				else if (element instanceof INIFileSection)
				{
					INIFileSection section = (INIFileSection) element;
					if (allEntriesAreCommented(section))
					{
						commentButton.setImage(UNCOMMENT_ICON);
						commentButton.setToolTipText(Messages.PHPIniEditor_18);
						commentButton.setEnabled(true);
					}
					else if (allEntriesAreUnCommented(section))
					{
						commentButton.setImage(COMMENT_ICON);
						commentButton.setToolTipText(Messages.PHPIniEditor_19);
						commentButton.setEnabled(true);
					}
				}
				else
				{

				}
				return;
			}
		}

		removeButton.setEnabled(false);
		commentButton.setEnabled(false);
	}

	/**
	 * Checks whether all entries in this section are commented.
	 * 
	 * @param section
	 *            - section to check.
	 * @return true if all entries are commentedm false otherwise.
	 */
	private boolean allEntriesAreCommented(INIFileSection section)
	{
		if (section.getEntries().size() == 0)
		{
			return false;
		}

		for (PHPIniEntry entry : section.getEntries())
		{
			if (!entry.getCommented())
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * Checks whether all entries in this section are uncommented.
	 * 
	 * @param section
	 *            - section to check.
	 * @return true if all entries are commented false otherwise.
	 */
	private boolean allEntriesAreUnCommented(INIFileSection section)
	{
		if (section.getEntries().size() == 0)
		{
			return false;
		}

		for (PHPIniEntry entry : section.getEntries())
		{
			if (entry.getCommented())
			{
				return false;
			}
		}

		return true;
	}

	/**
	 * Extract only the elements that are related to the loaded PHP extensions. This includes: extension_dir, extension,
	 * zend_extension, and zend_extension_ts
	 * 
	 * @author Shalom G
	 * @since Aptana PHP 1.1
	 */
	private static class ExtensionsOnlyFilter extends ViewerFilter
	{
		public boolean select(Viewer viewer, Object parentElement, Object element)
		{
			if (element instanceof INIFileSection)
			{
				return containsExtensionData((INIFileSection) element);
			}
			if (element instanceof PHPIniEntry)
			{
				return containsExtensionData((PHPIniEntry) element);
			}
			return false;
		}

		private boolean containsExtensionData(PHPIniEntry entry)
		{
			return "extension".equals(entry.getKey()) || "extension_dir".equals(entry.getKey()) //$NON-NLS-1$ //$NON-NLS-2$
					|| "zend_extension".equals(entry.getKey()) || "zend_extension_ts".equals(entry.getKey());//$NON-NLS-1$ //$NON-NLS-2$
		}

		private boolean containsExtensionData(INIFileSection section)
		{
			List<PHPIniEntry> entries = section.getEntries();
			for (PHPIniEntry entry : entries)
			{
				if (containsExtensionData(entry))
				{
					return true;
				}
			}
			return false;
		}
	}
}
