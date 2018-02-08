package com.aptana.editor.php.internal.ui.editor.outline;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

/**
 * Abstract composing label decorator.
 * 
 * @author Denis Denisenko
 */
public abstract class AbstractComposingLabelDecorator implements ILabelDecorator
{
	/**
	 * Compositions cache.
	 */
	private Map<Object, Image> compositions = new HashMap<Object, Image>();

	/**
	 * Result width.
	 */
	protected int width;

	/**
	 * Result height.
	 */
	protected int height;

	/**
	 * AbstractComposingLabelDecorator constructor.
	 * 
	 * @param width
	 *            - result width.
	 * @param height
	 *            - result height.
	 */
	protected AbstractComposingLabelDecorator(int width, int height)
	{
		this.width = width;
		this.height = height;
	}

	/**
	 * Gets the unique identifier of the whole composition of the main image and additional decorations.
	 * 
	 * @param image
	 *            - main image.
	 * @param element
	 *            - element.
	 * @return unique identifier or null if element can not be decorated.
	 */
	protected abstract Object getCompositionKey(Image image, Object element);

	/**
	 * Compose an image using the given main image and an element.<br>
	 * It's recommended that the implementor of this method use {@link DecorationOverlayIcon} to generate a decorated
	 * image.
	 * 
	 * @param mainImage
	 *            - main image.
	 * @param element
	 *            - element.
	 * @return composed image.
	 */
	public abstract Image compose(Image mainImage, Object element);

	/**
	 * {@inheritDoc}
	 */
	public Image decorateImage(Image image, Object element)
	{
		Object compositionKey = getCompositionKey(image, element);
		if (compositionKey == null)
		{
			return image;
		}

		Image result = compositions.get(compositionKey);

		if (result == null || result.isDisposed())
		{
			result = compose(image, element);
			if (result != image)
			{
				compositions.put(compositionKey, result);
			}
		}

		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public String decorateText(String text, Object element)
	{
		return text;
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
		// disposing compositions.
		for (Image image : compositions.values())
		{
			image.dispose();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isLabelProperty(Object element, String property)
	{
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeListener(ILabelProviderListener listener)
	{
	}
}
