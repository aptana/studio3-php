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
package org2.eclipse.php.internal.core.documentModel.phpElementData;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;

public abstract class AbstractCodeData implements CodeData {

	private static final long serialVersionUID = -1584581365974401435L;
	protected String name;
	private String description;
	private boolean isUserCode;
	protected IUserData userData;

	/**
	 * Construct a new AbstractCodeData that is not user data.
	 *
	 * @param name        The name of the Code Data.
	 * @param description The description of the Code Data.
	 */
	public AbstractCodeData(String name, String description) {
		this(name, description, null);
	}

	/**
	 * Construct a new AbstractCodeData.
	 *
	 * @param name        The name of the Code Data.
	 * @param description The description of the Code Data.
	 * @param userData
	 */
	public AbstractCodeData(String name, String description, IUserData userData) {
		this.name = name;
		this.description = description;
		this.userData = userData;
		this.isUserCode = userData != null;
	}

	/**
	 * Returns the name of the CodeData.
	 *
	 * @return The name of the CodeData.
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Returns a description of the CodeData.
	 *
	 * @return Description of the CodeData.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * return true if this CodeData is user code
	 */
	public final boolean isUserCode() {
		return isUserCode;
	}

	/**
	 * Returns the user data
	 *
	 * @return the user data
	 */
	public final IUserData getUserData() {
		return userData;
	}

	/**
	 * Compare this object to anther object.
	 * If the other object is not instanceof CodeData return -1;
	 * Compares the name of this CodeData to the name of the other Code data, if the result is not 0 return the result.
	 *
	 * @param o the object that we compare to.
	 * @return
	 */
	public int compareTo(Object o) {
		if (!(o instanceof CodeData)) {
			return -1;
		}
		CodeData other = ((CodeData) o);
		int rv = name.compareToIgnoreCase(other.getName());
		if (rv != 0) {
			return rv;
		}
		boolean otherIsUserCode = other.isUserCode();
		if (!isUserCode) {
			if (!otherIsUserCode) {
				return 0;
			}
			return -1;
		}
		if (!otherIsUserCode) {
			return 1;
		}
		return userData.getFileName().compareTo(other.getUserData().getFileName());
	}

	public String toString() {
		if (getUserData() == null) {
			return name;
		}
		return name + " (in " + getUserData().getFileName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Returns an object which is an instance of the given class
	 * associated with this object. Returns <code>null</code> if
	 * no such object can be found.
	 * <p>
	 * This implementation of the method declared by <code>IAdaptable</code>
	 * passes the request along to the platform's adapter manager; roughly
	 * <code>Platform.getAdapterManager().getAdapter(this, adapter)</code>.
	 * Subclasses may override this method (however, if they do so, they
	 * should invoke the method on their superclass to ensure that the
	 * Platform's adapter manager is consulted).
	 * </p>
	 *
	 * @param adapter the class to adapt to
	 * @return the adapted object or <code>null</code>
	 * @see IAdaptable#getAdapter(Class)
	 * @see Platform#getAdapterManager()
	 */
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}
	
}