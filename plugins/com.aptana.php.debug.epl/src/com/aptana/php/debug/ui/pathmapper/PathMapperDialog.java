/**
 * Aptana Studio
 * Copyright (c) 2005-2012 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.php.debug.ui.pathmapper;

import java.text.MessageFormat;
import java.util.Arrays;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPropertyListener;
import org2.eclipse.php.debug.core.debugger.pathmapper.PathMapper;
import org2.eclipse.php.debug.core.debugger.pathmapper.PathMapper.Mapping;
import org2.eclipse.php.internal.debug.core.pathmapper.PathMapperRegistry;
import org2.eclipse.php.internal.debug.ui.wizard.IControlHandler;
import org2.eclipse.php.internal.debug.ui.wizard.web.PathMapperCompositeFragment;

import com.aptana.webserver.core.IServer;

/**
 * A path mapper dialog that is presented when a path mapping setup is needeed for a single server.
 * 
 * @author Shalom G
 */
public class PathMapperDialog extends TitleAreaDialog implements IControlHandler, IPropertyListener
{
	private IServer server;
	private Image image;
	private PathMapperCompositeFragment pathMapperCompositeFragment;

	/**
	 * @param parent
	 * @param style
	 */
	public PathMapperDialog(Shell shell, IServer server)
	{
		super(shell);
		this.server = server;
		setHelpAvailable(false);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell)
	{
		if (newShell != null)
		{
			if (server != null)
			{
				newShell.setText(MessageFormat.format(Messages.PathMapperDialog_titleTextForServer, server.getName()));
			}
			else
			{
				newShell.setText(Messages.PathMapperDialog_titleText);
			}
		}
		super.configureShell(newShell);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent)
	{
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setFont(parent.getFont());
		// Build the separator line
		Label titleBarSeparator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
		titleBarSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		pathMapperCompositeFragment = new PathMapperCompositeFragment(composite, this, true);
		pathMapperCompositeFragment.setData(server);
		pathMapperCompositeFragment.addPropertyChangeListener(this);
		pathMapperCompositeFragment.setLayoutData(new GridData(GridData.FILL_BOTH));

		parent.addDisposeListener(new DisposeListener()
		{
			public void widgetDisposed(DisposeEvent e)
			{
				if (image != null && !image.isDisposed())
				{
					image.dispose();
				}
			}
		});
		return composite;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed()
	{
		if (pathMapperCompositeFragment != null)
		{
			pathMapperCompositeFragment.performOk();
		}
		super.okPressed();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent)
	{
		Control c = super.createContents(parent);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
		return c;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
	 */
	protected boolean isResizable()
	{
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.ui.wizards.IControlHandler#setDescription(java.lang.String)
	 */
	public void setDescription(String desc)
	{
		super.setTitle(desc);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org2.eclipse.php.internal.ui.wizards.IControlHandler#setImageDescriptor(org.eclipse.jface.resource.ImageDescriptor
	 * )
	 */
	public void setImageDescriptor(ImageDescriptor image)
	{
		if (image != null)
		{
			this.image = image.createImage();
			super.setTitleImage(this.image);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org2.eclipse.php.internal.ui.wizards.IControlHandler#update()
	 */
	public void update()
	{
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IPropertyListener#propertyChanged(java.lang.Object, int)
	 */
	public void propertyChanged(Object source, int propId)
	{
		// Make sure that we enable the ok button only when something is really changed in the path mapping.
		// We sort and compare the old mappings and the new ones before we enable the button.
		PathMapper currentServerMapping = PathMapperRegistry.getByServer(server);
		if (currentServerMapping != null)
		{
			Mapping[] currentMappings = currentServerMapping.getMapping();
			Mapping[] newMappings = pathMapperCompositeFragment.getMappings();
			Arrays.sort(currentMappings);
			Arrays.sort(newMappings);
			if (!Arrays.equals(currentMappings, newMappings))
			{
				getButton(IDialogConstants.OK_ID).setEnabled(true);
				return;
			}
		}
		getButton(IDialogConstants.OK_ID).setEnabled(false);
	}
}
