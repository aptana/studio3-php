package com.aptana.editor.php.internal.ui.hover;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.internal.text.html.BrowserInformationControl;
import org.eclipse.jface.internal.text.html.BrowserInformationControlInput;
import org.eclipse.jface.internal.text.html.BrowserInput;
import org.eclipse.jface.internal.text.html.HTMLPrinter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IInformationControlExtension4;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.php.internal.core.compiler.ast.nodes.PHPDocBlock;
import org.eclipse.php.internal.ui.PHPUIMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.osgi.framework.Bundle;

import com.aptana.editor.common.contentassist.LexemeProvider;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.epl.PHPEplPlugin;
import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.internal.contentAssist.ContentAssistUtils;
import com.aptana.editor.php.internal.contentAssist.PHPTokenType;
import com.aptana.editor.php.internal.contentAssist.ParsingUtils;
import com.aptana.editor.php.internal.contentAssist.mapping.PHPOffsetMapper;
import com.aptana.editor.php.internal.indexer.AbstractPHPEntryValue;
import com.aptana.editor.php.internal.indexer.PHPDocUtils;
import com.aptana.editor.php.internal.parser.nodes.PHPBaseParseNode;
import com.aptana.editor.php.internal.parser.phpdoc.FunctionDocumentation;
import com.aptana.editor.php.internal.ui.editor.PHPSourceEditor;
import com.aptana.parsing.lexer.Lexeme;

/**
 * Provides PHPDoc as hover info for PHP elements.
 */
@SuppressWarnings("restriction")
public class PHPDocHover extends AbstractPHPTextHover
{

	/**
	 * Action to go back to the previous input in the hover control.
	 */
	private static final class BackAction extends Action
	{
		private final BrowserInformationControl fInfoControl;

		public BackAction(BrowserInformationControl infoControl)
		{
			fInfoControl = infoControl;
			setText(PHPUIMessages.getString("PHPDocHover.back")); //$NON-NLS-1$
			ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
			setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_BACK));
			setDisabledImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_BACK_DISABLED));

			update();
		}

		public void run()
		{
			BrowserInformationControlInput previous = (BrowserInformationControlInput) fInfoControl.getInput()
					.getPrevious();
			if (previous != null)
			{
				fInfoControl.setInput(previous);
			}
		}

		public void update()
		{
			BrowserInformationControlInput current = fInfoControl.getInput();

			if (current != null && current.getPrevious() != null)
			{
				BrowserInput previous = current.getPrevious();
				setToolTipText(MessageFormat.format(
						PHPUIMessages.getString("PHPDocHover.backTo"), previous.getInputName())); //$NON-NLS-1$
				setEnabled(true);
			}
			else
			{
				setToolTipText(PHPUIMessages.getString("PHPDocHover.back")); //$NON-NLS-1$
				setEnabled(false);
			}
		}
	}

	/**
	 * Action to go forward to the next input in the hover control.
	 */
	private static final class ForwardAction extends Action
	{
		private final BrowserInformationControl fInfoControl;

		public ForwardAction(BrowserInformationControl infoControl)
		{
			fInfoControl = infoControl;
			setText(PHPUIMessages.getString("PHPDocHover.forward")); //$NON-NLS-1$
			ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
			setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD));
			setDisabledImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD_DISABLED));

			update();
		}

		public void run()
		{
			BrowserInformationControlInput next = (BrowserInformationControlInput) fInfoControl.getInput().getNext();
			if (next != null)
			{
				fInfoControl.setInput(next);
			}
		}

		public void update()
		{
			BrowserInformationControlInput current = fInfoControl.getInput();

			if (current != null && current.getNext() != null)
			{
				setToolTipText(MessageFormat.format(
						PHPUIMessages.getString("PHPDocHover.forwardTo"), current.getNext().getInputName())); //$NON-NLS-1$
				setEnabled(true);
			}
			else
			{
				setToolTipText(PHPUIMessages.getString("PHPDocHover.forward")); //$NON-NLS-1$
				setEnabled(false);
			}
		}
	}

	// /**
	// * Action that opens the current hover input element.
	// *
	// * @since 3.4
	// */
	// private static final class OpenDeclarationAction extends Action {
	// private final BrowserInformationControl fInfoControl;
	//
	// public OpenDeclarationAction(BrowserInformationControl infoControl) {
	// fInfoControl= infoControl;
	// setText(JavaHoverMessages.JavadocHover_openDeclaration);
	//			JavaPluginImages.setLocalImageDescriptors(this, "goto_input.gif"); //$NON-NLS-1$ //TODO: better images
	// }
	//
	// /*
	// * @see org.eclipse.jface.action.Action#run()
	// */
	// public void run() {
	// JavadocBrowserInformationControlInput infoInput= (JavadocBrowserInformationControlInput) fInfoControl.getInput();
	// //TODO: check cast
	// fInfoControl.notifyDelayedInputChange(null);
	// fInfoControl.dispose(); //FIXME: should have protocol to hide, rather than dispose
	//
	// try {
	// //FIXME: add hover location to editor navigation history?
	// JavaUI.openInEditor(infoInput.getElement());
	// } catch (PartInitException e) {
	// JavaPlugin.log(e);
	// } catch (JavaModelException e) {
	// JavaPlugin.log(e);
	// }
	// }
	// }

	/**
	 * Presenter control creator.
	 */
	public static final class PresenterControlCreator extends AbstractReusableInformationControlCreator
	{

		/*
		 * @see
		 * org.eclipse.jdt.internal.ui.text.java.hover.AbstractReusableInformationControlCreator#doCreateInformationControl
		 * (org.eclipse.swt.widgets.Shell)
		 */
		public IInformationControl doCreateInformationControl(Shell parent)
		{
			if (BrowserInformationControl.isAvailable(parent))
			{
				ToolBarManager tbm = new ToolBarManager(SWT.FLAT);
				// TODO: Set a font, instead of passing a null one.
				BrowserInformationControl iControl = new BrowserInformationControl(parent, null, tbm);

				final BackAction backAction = new BackAction(iControl);
				backAction.setEnabled(false);
				tbm.add(backAction);
				final ForwardAction forwardAction = new ForwardAction(iControl);
				tbm.add(forwardAction);
				forwardAction.setEnabled(false);

				// TODO
				// final ShowInJavadocViewAction showInJavadocViewAction= new ShowInJavadocViewAction(iControl);
				// tbm.add(showInJavadocViewAction);
				// final OpenDeclarationAction openDeclarationAction= new OpenDeclarationAction(iControl);
				// tbm.add(openDeclarationAction);
				return iControl;
			}
			else
			{
				return new DefaultInformationControl(parent, true);
			}
		}
	}

	/**
	 * Hover control creator.
	 */
	public static final class HoverControlCreator extends AbstractReusableInformationControlCreator
	{
		/**
		 * The information presenter control creator.
		 */
		private final IInformationControlCreator fInformationPresenterControlCreator;
		/**
		 * <code>true</code> to use the additional info affordance, <code>false</code> to use the hover affordance.
		 */
		@SuppressWarnings("unused")
		private final boolean fAdditionalInfoAffordance;

		/**
		 * @param informationPresenterControlCreator
		 *            control creator for enriched hover
		 */
		public HoverControlCreator(IInformationControlCreator informationPresenterControlCreator)
		{
			this(informationPresenterControlCreator, false);
		}

		/**
		 * @param informationPresenterControlCreator
		 *            control creator for enriched hover
		 * @param additionalInfoAffordance
		 *            <code>true</code> to use the additional info affordance, <code>false</code> to use the hover
		 *            affordance
		 */
		public HoverControlCreator(IInformationControlCreator informationPresenterControlCreator,
				boolean additionalInfoAffordance)
		{
			fInformationPresenterControlCreator = informationPresenterControlCreator;
			fAdditionalInfoAffordance = additionalInfoAffordance;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * org.eclipse.jface.text.AbstractReusableInformationControlCreator#doCreateInformationControl(org.eclipse.swt
		 * .widgets.Shell)
		 */
		public IInformationControl doCreateInformationControl(Shell parent)
		{
			// String tooltipAffordanceString = fAdditionalInfoAffordance ?
			// JavaPlugin.getAdditionalInfoAffordanceString()
			// : EditorsUI.getTooltipAffordanceString();
			if (BrowserInformationControl.isAvailable(parent))
			{
				// String font = PreferenceConstants.APPEARANCE_JAVADOC_FONT;
				BrowserInformationControl iControl = new BrowserInformationControl(parent, null, EditorsUI
						.getTooltipAffordanceString())
				{
					/*
					 * @see org.eclipse.jface.text.IInformationControlExtension5#getInformationPresenterControlCreator()
					 */
					public IInformationControlCreator getInformationPresenterControlCreator()
					{
						return fInformationPresenterControlCreator;
					}
				};
				// addLinkListener(iControl);
				return iControl;
			}
			else
			{
				return new DefaultInformationControl(parent, EditorsUI.getTooltipAffordanceString());
			}
		}

		/*
		 * (non-Javadoc)
		 * @seeorg.eclipse.jface.text.AbstractReusableInformationControlCreator#canReuse(org.eclipse.jface.text.
		 * IInformationControl)
		 */
		public boolean canReuse(IInformationControl control)
		{
			if (!super.canReuse(control))
				return false;

			if (control instanceof IInformationControlExtension4)
			{
				// String tooltipAffordanceString= fAdditionalInfoAffordance ?
				// JavaPlugin.getAdditionalInfoAffordanceString() : EditorsUI.getTooltipAffordanceString();
				((IInformationControlExtension4) control).setStatusText(EditorsUI.getTooltipAffordanceString());
			}

			return true;
		}
	}

	/**
	 * The style sheet.
	 */
	private static String fgStyleSheet;

	/**
	 * The hover control creator.
	 */
	private IInformationControlCreator fHoverControlCreator;
	/**
	 * The presentation control creator.
	 */
	private IInformationControlCreator fPresenterControlCreator;

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.internal.ui.hover.AbstractPHPTextHover#getInformationPresenterControlCreator()
	 */
	public IInformationControlCreator getInformationPresenterControlCreator()
	{
		if (fPresenterControlCreator == null)
			fPresenterControlCreator = new PresenterControlCreator();
		return fPresenterControlCreator;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.php.internal.ui.hover.AbstractPHPTextHover#getHoverControlCreator()
	 */
	public IInformationControlCreator getHoverControlCreator()
	{
		if (fHoverControlCreator == null)
			fHoverControlCreator = new HoverControlCreator(getInformationPresenterControlCreator());
		return fHoverControlCreator;
	}

	// private static void addLinkListener(final BrowserInformationControl control)
	// {
	// control.addLocationListener(JavaElementLinks.createLocationListener(new JavaElementLinks.ILinkHandler()
	// {
	// /*
	// * (non-Javadoc)
	// * @see
	// * org.eclipse.jdt.internal.ui.viewsupport.JavaElementLinks.ILinkHandler#handleJavadocViewLink(org.eclipse
	// * .jdt.core.IModelElement)
	// */
	// public void handleJavadocViewLink(IModelElement linkTarget)
	// {
	// control.notifyDelayedInputChange(null);
	// control.setVisible(false);
	// control.dispose(); // FIXME: should have protocol to hide, rather than dispose
	// try
	// {
	// JavadocView view = (JavadocView) JavaPlugin.getActivePage().showView(JavaUI.ID_JAVADOC_VIEW);
	// view.setInput(linkTarget);
	// }
	// catch (PartInitException e)
	// {
	// JavaPlugin.log(e);
	// }
	// }
	//
	// /*
	// * (non-Javadoc)
	// * @see
	// * org.eclipse.jdt.internal.ui.viewsupport.JavaElementLinks.ILinkHandler#handleInlineJavadocLink(org.eclipse
	// * .jdt.core.IModelElement)
	// */
	// public void handleInlineJavadocLink(IModelElement linkTarget)
	// {
	// JavadocBrowserInformationControlInput hoverInfo = getHoverInfo(new IModelElement[] { linkTarget }, null,
	// (JavadocBrowserInformationControlInput) control.getInput());
	// if (control.hasDelayedInputChangeListener())
	// control.notifyDelayedInputChange(hoverInfo);
	// else
	// control.setInput(hoverInfo);
	// }
	//
	// /*
	// * (non-Javadoc)
	// * @see
	// * org.eclipse.jdt.internal.ui.viewsupport.JavaElementLinks.ILinkHandler#handleDeclarationLink(org.eclipse
	// * .jdt.core.IModelElement)
	// */
	// public void handleDeclarationLink(IModelElement linkTarget)
	// {
	// control.notifyDelayedInputChange(null);
	// control.dispose(); // FIXME: should have protocol to hide, rather than dispose
	// try
	// {
	// // FIXME: add hover location to editor navigation history?
	// JavaUI.openInEditor(linkTarget);
	// }
	// catch (PartInitException e)
	// {
	// JavaPlugin.log(e);
	// }
	// catch (JavaModelException e)
	// {
	// JavaPlugin.log(e);
	// }
	// }
	//
	// /*
	// * (non-Javadoc)
	// * @see
	// * org.eclipse.jdt.internal.ui.viewsupport.JavaElementLinks.ILinkHandler#handleExternalLink(java.net.URL,
	// * org.eclipse.swt.widgets.Display)
	// */
	// public boolean handleExternalLink(URL url, Display display)
	// {
	// control.notifyDelayedInputChange(null);
	// control.dispose(); // FIXME: should have protocol to hide, rather than dispose
	//
	// // open external links in real browser:
	//				OpenBrowserUtil.open(url, display, ""); //$NON-NLS-1$
	//
	// return true;
	// }
	//
	// public void handleTextSet()
	// {
	// }
	// }));
	// }

	/**
	 * @deprecated see {@link org.eclipse.jface.text.ITextHover#getHoverInfo(ITextViewer, IRegion)}
	 */
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion)
	{
		PHPDocumentationBrowserInformationControlInput info = (PHPDocumentationBrowserInformationControlInput) getHoverInfo2(
				textViewer, hoverRegion);
		return info != null ? info.getHtml() : null;
	}

	/*
	 * @see org.eclipse.jface.text.ITextHoverExtension2#getHoverInfo2(org.eclipse.jface.text.ITextViewer,
	 * org.eclipse.jface.text.IRegion)
	 */
	public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion)
	{
		return internalGetHoverInfo(textViewer, hoverRegion);
	}

	private PHPDocumentationBrowserInformationControlInput internalGetHoverInfo(ITextViewer textViewer,
			IRegion hoverRegion)
	{
		Object[] elements = getPHPElementsAt(textViewer, hoverRegion);
		if (elements == null || elements.length == 0)
			return null;

		String constantValue = null;
		return getHoverInfo(elements, constantValue, null);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.aptana.editor.php.internal.ui.hover.AbstractPHPTextHover#getPHPElementsAt(org.eclipse.jface.text.ITextViewer,
	 * org.eclipse.jface.text.IRegion)
	 */
	protected Object[] getPHPElementsAt(ITextViewer textViewer, IRegion hoverRegion)
	{
		PHPSourceEditor editor = (PHPSourceEditor) getEditor();
		LexemeProvider<PHPTokenType> lexemeProvider = ParsingUtils.createLexemeProvider(editor.getDocumentProvider()
				.getDocument(editor.getEditorInput()), hoverRegion.getOffset());

		Lexeme<PHPTokenType> lexeme = lexemeProvider.getLexemeFromOffset(hoverRegion.getOffset());
		if (lexeme == null)
		{
			return null;
		}
		PHPOffsetMapper offsetMapper = editor.getOffsetMapper();
		Object entry = offsetMapper.findEntry(lexeme, lexemeProvider);
		if (entry == null)
		{
			try
			{
				String name = textViewer.getDocument().get(hoverRegion.getOffset(), hoverRegion.getLength());
				if (!"".equals(name)) { //$NON-NLS-1$
					ArrayList<Object> elements = ContentAssistUtils.selectModelElements(name, true);
					if (elements != null && !elements.isEmpty())
					{
						// return the first element only
						entry = elements.get(0);
					}
				}
			}
			catch (BadLocationException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		if (entry != null)
		{
			return new Object[] { entry };
		}
		return null;
	}

	/**
	 * Computes the hover info.
	 * 
	 * @param elements
	 *            the resolved elements
	 * @param constantValue
	 *            a constant value iff result contains exactly 1 constant field, or <code>null</code>
	 * @param previousInput
	 *            the previous input, or <code>null</code>
	 * @return the HTML hover info for the given element(s) or <code>null</code> if no information is available
	 */
	private static PHPDocumentationBrowserInformationControlInput getHoverInfo(Object[] elements, String constantValue,
			PHPDocumentationBrowserInformationControlInput previousInput)
	{
		int nResults = elements.length;
		StringBuffer buffer = new StringBuffer();
		String base = null;

		int leadingImageWidth = 0;

		if (nResults > 1)
		{
			return null;
		}
		else
		{
			Object element = elements[0];
			if (element != null)
			{
				setHeader(element, buffer);
				setDocumentation(element, buffer);
				if (buffer.length() > 0)
				{
					HTMLPrinter.insertPageProlog(buffer, 0, PHPDocHover.getStyleSheet());
					if (base != null)
					{
						int endHeadIdx = buffer.indexOf("</head>"); //$NON-NLS-1$
						buffer.insert(endHeadIdx, "\n<base href='" + base + "'>\n"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					HTMLPrinter.addPageEpilog(buffer);
					return new PHPDocumentationBrowserInformationControlInput(previousInput, element,
							buffer.toString(), leadingImageWidth);
				}
			}
			else
			{
				return null;
			}
		}
		return null;
	}

	private static void setDocumentation(Object element, StringBuffer buffer)
	{
		if (element instanceof IElementEntry)
		{
			IElementEntry entry = (IElementEntry) element;
			AbstractPHPEntryValue phpValue = (AbstractPHPEntryValue) entry.getValue();
			int startOffset = phpValue.getStartOffset();
			PHPDocBlock comment = PHPDocUtils.findFunctionPHPDocComment(entry, startOffset);
			FunctionDocumentation documentation = PHPDocUtils.getFunctionDocumentation(comment);
			buffer.append(PHPDocUtils.computeDocumentation(documentation, entry.getEntryPath()));
		}
		else if (element instanceof PHPBaseParseNode)
		{
			PHPBaseParseNode node = (PHPBaseParseNode) element;
			buffer.append(ContentAssistUtils.getDocumentation(node, node.getNodeName()));
		}
	}

	private static void setHeader(Object element, StringBuffer buffer)
	{
		// Set the header to display the file location
		if (element instanceof IElementEntry)
		{
			IElementEntry entry = (IElementEntry) element;
			if (entry.getModule() != null)
			{
				buffer.append("<div class=\"header\""); //$NON-NLS-1$
				HTMLPrinter.addSmallHeader(buffer, entry.getModule().getShortName());
				buffer.append("</div>"); //$NON-NLS-1$
			}
		}
		else if (element instanceof PHPBaseParseNode)
		{
			buffer.append("<div class=\"header\""); //$NON-NLS-1$
			HTMLPrinter.addSmallHeader(buffer, "PHP API"); //$NON-NLS-1$
			buffer.append("</div>"); //$NON-NLS-1$
		}
	}

	/**
	 * Returns the PHP hover style sheet
	 */
	private static String getStyleSheet()
	{
		if (fgStyleSheet == null)
		{
			fgStyleSheet = loadStyleSheet();
		}
		String css = fgStyleSheet;
		if (css != null)
		{
			FontData fontData = JFaceResources.getFontRegistry().getFontData("Dialog")[0]; //$NON-NLS-1$
			css = HTMLPrinter.convertTopLevelFont(css, fontData);
		}

		return css;
	}

	/**
	 * Loads the hover style sheet.
	 */
	private static String loadStyleSheet()
	{
		Bundle bundle = Platform.getBundle(PHPEditorPlugin.PLUGIN_ID);
		URL styleSheetURL = bundle.getEntry("/documentationStyle.css"); //$NON-NLS-1$
		if (styleSheetURL != null)
		{
			BufferedReader reader = null;
			try
			{
				reader = new BufferedReader(new InputStreamReader(styleSheetURL.openStream()));
				StringBuffer buffer = new StringBuffer(1500);
				String line = reader.readLine();
				while (line != null)
				{
					buffer.append(line);
					buffer.append('\n');
					line = reader.readLine();
				}
				return buffer.toString();
			}
			catch (IOException ex)
			{
				PHPEplPlugin.logError(ex);
				return ""; //$NON-NLS-1$
			}
			finally
			{
				try
				{
					if (reader != null)
						reader.close();
				}
				catch (IOException e)
				{
				}
			}
		}
		return null;
	}

	public static void addImageAndLabel(StringBuffer buf, String imageName, int imageWidth, int imageHeight,
			int imageLeft, int imageTop, String label, int labelLeft, int labelTop)
	{

		if (imageName != null)
		{
			StringBuffer imageStyle = new StringBuffer("position: absolute; "); //$NON-NLS-1$
			imageStyle.append("width: ").append(imageWidth).append("px; "); //$NON-NLS-1$ //$NON-NLS-2$
			imageStyle.append("height: ").append(imageHeight).append("px; "); //$NON-NLS-1$ //$NON-NLS-2$
			imageStyle.append("top: ").append(imageTop).append("px; "); //$NON-NLS-1$ //$NON-NLS-2$
			imageStyle.append("left: ").append(imageLeft).append("px; "); //$NON-NLS-1$ //$NON-NLS-2$

			buf.append("<!--[if lte IE 6]><![if gte IE 5.5]>\n"); //$NON-NLS-1$
			buf
					.append("<span style=\"").append(imageStyle).append("filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='").append(imageName).append("')\"></span>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			buf.append("<![endif]><![endif]-->\n"); //$NON-NLS-1$

			buf.append("<!--[if !IE]>-->\n"); //$NON-NLS-1$
			buf.append("<img style='").append(imageStyle).append("' src='").append(imageName).append("'/>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			buf.append("<!--<![endif]-->\n"); //$NON-NLS-1$
			buf.append("<!--[if gte IE 7]>\n"); //$NON-NLS-1$
			buf.append("<img style='").append(imageStyle).append("' src='").append(imageName).append("'/>\n"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			buf.append("<![endif]-->\n"); //$NON-NLS-1$
		}

		buf.append("<div style='word-wrap:break-word;"); //$NON-NLS-1$
		if (imageName != null)
		{
			buf.append("margin-left: ").append(labelLeft).append("px; "); //$NON-NLS-1$ //$NON-NLS-2$
			buf.append("margin-top: ").append(labelTop).append("px; "); //$NON-NLS-1$ //$NON-NLS-2$
		}
		buf.append("'>"); //$NON-NLS-1$
		buf.append(label);
		buf.append("</div>"); //$NON-NLS-1$
	}

}
