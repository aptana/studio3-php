package com.aptana.editor.php.internal.ui.editor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IAnnotationImageProvider;

import com.aptana.editor.php.internal.ui.PHPPluginImages;

/**
 * Image provider for annotations (based on JDT code).
 * 
 * @since 3.0
 */
public class ScriptAnnotationImageProvider implements IAnnotationImageProvider
{

	private final static int NO_IMAGE = 0;
	private final static int GRAY_IMAGE = 1;
	private final static int OVERLAY_IMAGE = 2;
	private final static int QUICKFIX_IMAGE = 3;
	private final static int QUICKFIX_ERROR_IMAGE = 4;

	private static Image fgQuickFixImage;
	private static Image fgQuickFixErrorImage;
	private static ImageRegistry fgImageRegistry;

	private int fCachedImageType;
	private Image fCachedImage;

	public ScriptAnnotationImageProvider()
	{
	}

	/*
	 * @see IAnnotationImageProvider#getManagedImage(Annotation)
	 */
	public Image getManagedImage(Annotation annotation)
	{
		if (annotation instanceof IScriptAnnotation)
		{
			IScriptAnnotation javaAnnotation = (IScriptAnnotation) annotation;
			int imageType = getImageType(javaAnnotation);
			return getImage(javaAnnotation, imageType, Display.getCurrent());
		}
		return null;
	}

	/*
	 * @see IAnnotationImageProvider#getImageDescriptorId(Annotation)
	 */
	public String getImageDescriptorId(Annotation annotation)
	{
		// unmanaged images are not supported
		return null;
	}

	/*
	 * @see IAnnotationImageProvider#getImageDescriptor(String)
	 */
	public ImageDescriptor getImageDescriptor(String symbolicName)
	{
		// unmanaged images are not supported
		return null;
	}

	private boolean showQuickFix(IScriptAnnotation annotation)
	{
		return false;
		/*
		 * return annotation.isProblem() && ScriptAnnotationUtils.hasCorrections(annotation);
		 */
	}

	private Image getQuickFixImage()
	{
		if (fgQuickFixImage == null)
			fgQuickFixImage = PHPPluginImages.get(PHPPluginImages.IMG_OBJS_FIXABLE_PROBLEM);
		return fgQuickFixImage;
	}

	private Image getQuickFixErrorImage()
	{
		if (fgQuickFixErrorImage == null)
			fgQuickFixErrorImage = PHPPluginImages.get(PHPPluginImages.IMG_OBJS_FIXABLE_ERROR);
		return fgQuickFixErrorImage;
	}

	private ImageRegistry getImageRegistry(Display display)
	{
		if (fgImageRegistry == null)
			fgImageRegistry = new ImageRegistry(display);
		return fgImageRegistry;
	}

	private int getImageType(IScriptAnnotation annotation)
	{
		int imageType = NO_IMAGE;
		if (annotation.hasOverlay())
			imageType = OVERLAY_IMAGE;
		else if (!annotation.isMarkedDeleted())
		{
			if (showQuickFix(annotation))
				imageType = ScriptMarkerAnnotation.ERROR_ANNOTATION_TYPE.equals(annotation.getType()) ? QUICKFIX_ERROR_IMAGE
						: QUICKFIX_IMAGE;
		}
		else
		{
			imageType = GRAY_IMAGE;
		}
		return imageType;
	}

	private Image getImage(IScriptAnnotation annotation, int imageType, Display display)
	{
		if ((imageType == QUICKFIX_IMAGE || imageType == QUICKFIX_ERROR_IMAGE) && fCachedImageType == imageType)
			return fCachedImage;

		Image image = null;
		switch (imageType)
		{
			case OVERLAY_IMAGE:
				IScriptAnnotation overlay = annotation.getOverlay();
				image = getManagedImage((Annotation) overlay);
				fCachedImageType = -1;
				break;
			case QUICKFIX_IMAGE:
				image = getQuickFixImage();
				fCachedImageType = imageType;
				fCachedImage = image;
				break;
			case QUICKFIX_ERROR_IMAGE:
				image = getQuickFixErrorImage();
				fCachedImageType = imageType;
				fCachedImage = image;
				break;
			case GRAY_IMAGE:
			{
				ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
				String annotationType = annotation.getType();
				if (ScriptMarkerAnnotation.ERROR_ANNOTATION_TYPE.equals(annotationType))
				{
					image = sharedImages.getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
				}
				else if (ScriptMarkerAnnotation.WARNING_ANNOTATION_TYPE.equals(annotationType))
				{
					image = sharedImages.getImage(ISharedImages.IMG_OBJS_WARN_TSK);
				}
				else if (ScriptMarkerAnnotation.INFO_ANNOTATION_TYPE.equals(annotationType))
				{
					image = sharedImages.getImage(ISharedImages.IMG_OBJS_INFO_TSK);
				}
				if (image != null)
				{
					ImageRegistry registry = getImageRegistry(display);
					String key = Integer.toString(image.hashCode());
					Image grayImage = registry.get(key);
					if (grayImage == null)
					{
						grayImage = new Image(display, image, SWT.IMAGE_GRAY);
						registry.put(key, grayImage);
					}
					image = grayImage;
				}
				fCachedImageType = -1;
				break;
			}
		}

		return image;
	}
}
