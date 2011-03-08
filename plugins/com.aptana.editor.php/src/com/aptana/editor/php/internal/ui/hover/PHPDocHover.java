package com.aptana.editor.php.internal.ui.hover;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.internal.text.html.BrowserInformationControl;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.EditorsUI;
import org.osgi.framework.Bundle;

import com.aptana.core.util.IOUtil;
import com.aptana.core.util.StringUtil;
import com.aptana.editor.common.contentassist.LexemeProvider;
import com.aptana.editor.php.PHPEditorPlugin;
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
	private static final String DOCUMENTATION_STYLE_CSS = "/documentationStyle.css"; //$NON-NLS-1$

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
				BrowserInformationControl iControl = new BrowserInformationControl(parent, null, tbm);
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
		private final IInformationControlCreator informationPresenterControlCreator;
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
			this.informationPresenterControlCreator = informationPresenterControlCreator;
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
			if (BrowserInformationControl.isAvailable(parent))
			{
				BrowserInformationControl iControl = new BrowserInformationControl(parent, null, EditorsUI
						.getTooltipAffordanceString())
				{
					public IInformationControlCreator getInformationPresenterControlCreator()
					{
						return informationPresenterControlCreator;
					}
				};
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
				((IInformationControlExtension4) control).setStatusText(EditorsUI.getTooltipAffordanceString());
			}

			return true;
		}
	}

	/**
	 * The style sheet.
	 */
	private static String styleSheet;

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
	 * (non-Javadoc)
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
				if (!StringUtil.EMPTY.equals(name))
				{
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
				PHPEditorPlugin.logError(e);
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
	@SuppressWarnings("unused")
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
		if (styleSheet == null)
		{
			styleSheet = loadStyleSheet();
		}
		String css = styleSheet;
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
		URL styleSheetURL = bundle.getEntry(DOCUMENTATION_STYLE_CSS);
		if (styleSheetURL != null)
		{
			try
			{
				return IOUtil.read(styleSheetURL.openStream());
			}
			catch (IOException ex)
			{
				PHPEditorPlugin.logError(ex);
				return StringUtil.EMPTY;
			}
		}
		return null;
	}

	@SuppressWarnings("nls")
	public static void addImageAndLabel(StringBuffer buf, String imageName, int imageWidth, int imageHeight,
			int imageLeft, int imageTop, String label, int labelLeft, int labelTop)
	{

		if (imageName != null)
		{
			StringBuffer imageStyle = new StringBuffer("position: absolute; ");
			imageStyle.append("width: ");
			imageStyle.append(imageWidth);
			imageStyle.append("px; ");
			imageStyle.append("height: ");
			imageStyle.append(imageHeight);
			imageStyle.append("px; ");
			imageStyle.append("top: ");
			imageStyle.append(imageTop);
			imageStyle.append("px; ");
			imageStyle.append("left: ");
			imageStyle.append(imageLeft);
			imageStyle.append("px; ");

			buf.append("<!--[if lte IE 6]><![if gte IE 5.5]>\n");
			buf.append("<span style=\"");
			buf.append(imageStyle);
			buf.append("filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(src='");
			buf.append(imageName);
			buf.append("')\"></span>\n");
			buf.append("<![endif]><![endif]-->\n");

			buf.append("<!--[if !IE]>-->\n");
			buf.append("<img style='");
			buf.append(imageStyle);
			buf.append("' src='");
			buf.append(imageName);
			buf.append("'/>\n");
			buf.append("<!--<![endif]-->\n");
			buf.append("<!--[if gte IE 7]>\n");
			buf.append("<img style='");
			buf.append(imageStyle);
			buf.append("' src='");
			buf.append(imageName);
			buf.append("'/>\n");
			buf.append("<![endif]-->\n");
		}

		buf.append("<div style='word-wrap:break-word;");
		if (imageName != null)
		{
			buf.append("margin-left: ").append(labelLeft).append("px; ");
			buf.append("margin-top: ").append(labelTop).append("px; ");
		}
		buf.append("'>");
		buf.append(label);
		buf.append("</div>");
	}
}
