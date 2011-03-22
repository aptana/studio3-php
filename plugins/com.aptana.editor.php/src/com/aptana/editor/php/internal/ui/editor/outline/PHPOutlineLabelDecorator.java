package com.aptana.editor.php.internal.ui.editor.outline;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org2.eclipse.php.core.compiler.PHPFlags;

import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.internal.parser.nodes.PHPBaseParseNode;
import com.aptana.parsing.ast.IParseNode;

/**
 * PHPOutlineLabelDecorator
 * 
 * @author Denis Denisenko
 */
public class PHPOutlineLabelDecorator extends AbstractComposingLabelDecorator
{

	private static final ImageDescriptor STATIC_IMAGE_DESCRIPTOR = PHPEditorPlugin
			.getImageDescriptor("icons/full/ovr16/static_co.png"); //$NON-NLS-1$

	private static final ImageDescriptor FINAL_IMAGE_DESCRIPTOR = PHPEditorPlugin
			.getImageDescriptor("icons/full/ovr16/final_co.png"); //$NON-NLS-1$

	private static final ImageDescriptor ABSTRACT_IMAGE_DESCRIPTOR = PHPEditorPlugin
			.getImageDescriptor("icons/full/ovr16/abstract_co.png"); //$NON-NLS-1$

	/**
	 * PHPOutlineLabelDecorator constructor.
	 * 
	 * @param width
	 *            - result width.
	 * @param height
	 *            - result height.
	 */
	public PHPOutlineLabelDecorator(int width, int height)
	{
		super(width, height);
	}

	/**
	 * Returns a unique key for the given image and the element's modifiers.
	 * 
	 * @param image
	 *            The original image that we want to decorate.
	 * @param element
	 *            a {@link PHPOutlineItem} that contains a {@link PHPBaseParseNode} element.
	 */
	@Override
	protected Object getCompositionKey(Image image, Object element)
	{
		if (image == null || element == null)
		{
			return null;
		}
		PHPBaseParseNode node = getPHPNode(element);
		if (node == null)
		{
			return null;
		}
		int imageHashCode = image.hashCode();
		int modifiers = node.getModifiers();

		long key = (((long) imageHashCode) << 32) + (long) modifiers;
		return key;
	}

	/**
	 * Returns the inner referenced node from the given element. We expect the given element to be a
	 * {@link PHPOutlineItem} which contains a reference node of {@link PHPBaseParseNode}. In any other case the method
	 * will return null.
	 * 
	 * @param element
	 *            A {@link PHPOutlineItem}
	 * @return A {@link PHPBaseParseNode} instance, or null.
	 */
	protected PHPBaseParseNode getPHPNode(Object element)
	{
		if (!(element instanceof PHPOutlineItem))
		{
			return null;
		}
		PHPOutlineItem outlineItem = (PHPOutlineItem) element;
		IParseNode referenceNode = outlineItem.getReferenceNode();
		if (referenceNode instanceof PHPBaseParseNode)
			return (PHPBaseParseNode) referenceNode;
		return null;
	}

	/**
	 * Generates a decorated image with regards to the abstract, static and final modifiers.
	 */
	@Override
	public Image compose(Image mainImage, Object element)
	{
		ImageDescriptor[] descriptors = new ImageDescriptor[5];
		PHPBaseParseNode phpNode = getPHPNode(element);
		if (phpNode == null)
		{
			return mainImage;
		}
		int modifiers = phpNode.getModifiers();
		if (PHPFlags.isAbstract(modifiers))
		{
			descriptors[IDecoration.TOP_RIGHT] = ABSTRACT_IMAGE_DESCRIPTOR;
			if (PHPFlags.isStatic(modifiers))
			{
				descriptors[IDecoration.TOP_LEFT] = STATIC_IMAGE_DESCRIPTOR;
			}
		}
		else if (PHPFlags.isFinal(modifiers))
		{
			descriptors[IDecoration.TOP_RIGHT] = FINAL_IMAGE_DESCRIPTOR;
			if (PHPFlags.isStatic(modifiers))
			{
				descriptors[IDecoration.TOP_LEFT] = STATIC_IMAGE_DESCRIPTOR;
			}
		}
		else if (PHPFlags.isStatic(modifiers))
		{
			descriptors[IDecoration.TOP_RIGHT] = STATIC_IMAGE_DESCRIPTOR;
		}
		if (descriptors[IDecoration.TOP_RIGHT] != null)
		{
			return new DecorationOverlayIcon(mainImage, descriptors, new Point(width, height)).createImage();
		}
		return mainImage;
	}
}
