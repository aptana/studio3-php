package com.aptana.editor.php.internal.model.impl;

import com.aptana.editor.php.core.model.IModelElement;
import com.aptana.editor.php.core.model.IModelElementVisitor;
import com.aptana.editor.php.core.model.IParent;
import com.aptana.editor.php.core.model.ISourceModel;
import com.aptana.editor.php.core.model.ISourceProject;
import com.aptana.editor.php.core.model.env.ModelElementInfo;
import com.aptana.editor.php.internal.model.ModelManager;

/**
 * AbstractModelElement
 * 
 * @author Denis Denisenko
 */
public abstract class AbstractModelElement implements IModelElement
{

	/**
	 * {@inheritDoc}
	 */
	public IModelElement getAncestor(int ancestorType)
	{
		IModelElement element = getParent();
		while (element != null)
		{
			if (element.getElementType() == ancestorType)
			{
				return element;
			}

			element = element.getParent();
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public ISourceProject getSourceProject()
	{
		return (ISourceProject) getAncestor(IModelElement.PROJECT);
	}

	/**
	 * {@inheritDoc}
	 */
	public void accept(IModelElementVisitor visitor)
	{
		boolean result = visitor.visit(this);

		if (result && this instanceof IParent)
		{
			for (IModelElement child : ((IParent) this).getChildren())
			{
				child.accept(visitor);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public ISourceModel getModel()
	{
		return ModelManager.getInstance().getModel();
	}

	/**
	 * Gets model element info.
	 * 
	 * @return model element info.
	 */
	public ModelElementInfo getElementInfo()
	{
		// return null by default
		return null;
	}

	/**
	 * Gets element debug string.
	 * 
	 * @return element debug string
	 */
	public String toDebugString()
	{

		StringBuffer buffer = new StringBuffer();
		this.toStringInfo(0, buffer, true/* show resolved info */);
		return buffer.toString();
	}

	/**
	 * @param tab
	 * @param buffer
	 * @param showResolvedInfo
	 */
	protected void toStringInfo(int tab, StringBuffer buffer, boolean showResolvedInfo)
	{
		buffer.append(this.tabString(tab));
		toStringName(buffer);
	}

	/**
	 * Adds element name to a buffer.
	 * 
	 * @param buffer
	 *            - buffer.
	 */
	protected void toStringName(StringBuffer buffer)
	{
		buffer.append(getElementName());
	}

	/**
	 * Gets string consisting from tabs.
	 * 
	 * @param tab
	 *            - number of tabs.
	 * @return string
	 */
	protected String tabString(int tab)
	{
		StringBuffer buffer = new StringBuffer();
		for (int i = tab; i > 0; i--)
			buffer.append("  "); //$NON-NLS-1$
		return buffer.toString();
	}

	/**
	 * Debug.
	 */
	public String toStringWithAncestors()
	{
		return toStringWithAncestors(true/* show resolved info */);
	}

	/**
	 * Debug.
	 */
	public String toStringWithAncestors(boolean showResolvedInfo)
	{
		StringBuffer buffer = new StringBuffer();
		this.toStringInfo(0, buffer, showResolvedInfo);
		this.toStringAncestors(buffer);
		return buffer.toString();
	}

	protected void toStringAncestors(StringBuffer buffer)
	{
		AbstractModelElement parentElement = (AbstractModelElement) this.getParent();
		if (parentElement != null && parentElement.getParent() != null)
		{
			buffer.append(" [in "); //$NON-NLS-1$
			parentElement.toStringInfo(0, buffer, false);
			parentElement.toStringAncestors(buffer);
			buffer.append("]"); //$NON-NLS-1$
		}
	}
}
