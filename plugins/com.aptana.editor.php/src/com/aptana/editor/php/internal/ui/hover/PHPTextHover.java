/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.ui.hover;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.internal.text.html.BrowserInformationControlInput;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInputChangedListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.FileEditorInput;
import org2.eclipse.php.internal.core.compiler.ast.nodes.PHPDocBlock;

import com.aptana.core.logging.IdeLog;
import com.aptana.core.util.ArrayUtil;
import com.aptana.core.util.CollectionsUtil;
import com.aptana.core.util.StringUtil;
import com.aptana.editor.common.contentassist.ILexemeProvider;
import com.aptana.editor.common.hover.CustomBrowserInformationControl;
import com.aptana.editor.common.hover.DocumentationBrowserInformationControlInput;
import com.aptana.editor.php.PHPEditorPlugin;
import com.aptana.editor.php.indexer.IElementEntry;
import com.aptana.editor.php.internal.builder.LocalModule;
import com.aptana.editor.php.internal.contentAssist.ContentAssistUtils;
import com.aptana.editor.php.internal.contentAssist.PHPTokenType;
import com.aptana.editor.php.internal.contentAssist.ParsingUtils;
import com.aptana.editor.php.internal.contentAssist.mapping.PHPOffsetMapper;
import com.aptana.editor.php.internal.core.builder.IModule;
import com.aptana.editor.php.internal.indexer.AbstractPHPEntryValue;
import com.aptana.editor.php.internal.indexer.PHPDocUtils;
import com.aptana.editor.php.internal.parser.nodes.PHPBaseParseNode;
import com.aptana.editor.php.internal.parser.phpdoc.FunctionDocumentation;
import com.aptana.editor.php.internal.ui.editor.PHPSourceEditor;
import com.aptana.editor.php.internal.ui.editor.hyperlink.PHPHyperlinkDetector;
import com.aptana.ide.ui.io.internal.UniformFileStoreEditorInput;
import com.aptana.parsing.lexer.Lexeme;
import com.aptana.ui.epl.UIEplPlugin;
import com.aptana.ui.util.UIUtils;

/**
 * Provides PHPDoc as hover info for PHP elements.
 */
@SuppressWarnings("restriction")
public class PHPTextHover extends AbstractPHPTextHover
{
	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.hover.AbstractDocumentationHover#getHeader(java.lang.Object,
	 * org.eclipse.ui.IEditorPart, org.eclipse.jface.text.IRegion)
	 */
	@Override
	public String getHeader(Object element, IEditorPart editorPart, IRegion hoverRegion)
	{
		// Set the header to display the file location
		if (element instanceof IElementEntry)
		{
			IElementEntry entry = (IElementEntry) element;
			if (entry.getModule() != null)
			{
				String moduleName = entry.getModule().getShortName();
				// In case the module is from a remote location, we would like to display the file name as is, and not
				// the temp file name that was created.
				if (editorPart != null && editorPart.getEditorInput() instanceof UniformFileStoreEditorInput)
				{
					UniformFileStoreEditorInput uniformInput = (UniformFileStoreEditorInput) editorPart
							.getEditorInput();
					if (uniformInput.isRemote())
					{
						moduleName = editorPart.getTitle();
					}
				}
				return moduleName;
			}
		}
		else if (element instanceof PHPBaseParseNode)
		{
			return Messages.PHPTextHover_phpAPIHeader;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.hover.AbstractDocumentationHover#getDocumentation(java.lang.Object,
	 * org.eclipse.ui.IEditorPart, org.eclipse.jface.text.IRegion)
	 */
	@Override
	public String getDocumentation(Object element, IEditorPart editorPart, IRegion hoverRegion)
	{
		String computedDocumentation = null;
		if (element instanceof IElementEntry)
		{
			IElementEntry entry = (IElementEntry) element;
			AbstractPHPEntryValue phpValue = (AbstractPHPEntryValue) entry.getValue();
			int startOffset = phpValue.getStartOffset();
			// Locate the IDocument and pass it along to the PHPDocUtils
			IDocument document = null;
			if (editorPart != null)
			{
				ISourceViewer sourceViewer = (ISourceViewer) editorPart.getAdapter(ISourceViewer.class);
				if (sourceViewer != null)
				{
					document = sourceViewer.getDocument();
				}
			}
			PHPDocBlock comment = PHPDocUtils.findFunctionPHPDocComment(entry, document, startOffset);
			FunctionDocumentation documentation = PHPDocUtils.getFunctionDocumentation(comment);
			computedDocumentation = PHPDocUtils.computeDocumentation(documentation, document, entry.getEntryPath());
		}
		else if (element instanceof PHPBaseParseNode)
		{
			PHPBaseParseNode node = (PHPBaseParseNode) element;
			computedDocumentation = ContentAssistUtils.getDocumentation(node, node.getNodeName());
		}
		return computedDocumentation;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.hover.AbstractDocumentationHover#populateToolbarActions(org.eclipse.jface.action.
	 * ToolBarManager, com.aptana.editor.common.hover.CustomBrowserInformationControl)
	 */
	@Override
	public void populateToolbarActions(ToolBarManager tbm, CustomBrowserInformationControl iControl)
	{
		final OpenDeclarationAction openDeclarationAction = new OpenDeclarationAction(iControl);
		tbm.add(openDeclarationAction);
		IInputChangedListener inputChangeListener = new IInputChangedListener()
		{
			public void inputChanged(Object newInput)
			{
				if (newInput instanceof BrowserInformationControlInput)
				{
					openDeclarationAction.update();
				}
			}
		};
		iControl.addInputChangeListener(inputChangeListener);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.text.ITextHoverExtension2#getHoverInfo2(org.eclipse.jface.text.ITextViewer,
	 * org.eclipse.jface.text.IRegion)
	 */
	public Object getHoverInfo2(ITextViewer textViewer, IRegion hoverRegion)
	{
		if (isHoverEnabled())
		{
			Object[] elements = getPHPElementsAt(textViewer, hoverRegion);
			if (ArrayUtil.isEmpty(elements) || elements[0] == null)
			{
				return null;
			}
			IEditorPart editor = getEditor();
			if (editor != null)
			{
				// We need to check if the current editor is matching the resource that the PHP module refers to.
				// In case it's not, we try to locate the editor that does match. If it's open, we'll pass it along.
				// If not, the hover will use the cached value for the documentation.
				IEditorInput editorInput = editor.getEditorInput();
				IFile resource = (IFile) editorInput.getAdapter(IFile.class);
				if (resource != null)
				{
					final IFile moduleFile = getModuleFile(elements[0]);
					if (moduleFile != null && !resource.equals(moduleFile))
					{
						final IEditorPart[] moduleEditorPart = new IEditorPart[1];
						// The module points to a different file. Try to grab the matching editor.
						UIUtils.getDisplay().syncExec(new Runnable()
						{
							public void run()
							{
								IWorkbenchPage activePage = UIUtils.getActivePage();
								if (activePage != null)
								{
									// locate an open editor.
									moduleEditorPart[0] = activePage.findEditor(new FileEditorInput(moduleFile));
								}
							}
						});
						// Assign the editor to the one we found. It can still be null in case the editor is not opened.
						editor = moduleEditorPart[0];
					}
				}
			}
			return getHoverInfo(elements[0], isBrowserControlAvailable(textViewer), null, editor, hoverRegion);
		}
		return null;
	}

	/**
	 * Returns an IFile associated with an IModule.
	 * 
	 * @param element
	 *            An instance of IModule
	 * @return An {@link IFile}; <code>null</code> if the given element is not a {@link LocalModule}.
	 */
	private IFile getModuleFile(Object element)
	{
		if (element instanceof IElementEntry)
		{
			IModule module = ((IElementEntry) element).getModule();
			if (module instanceof LocalModule)
			{
				return ((LocalModule) module).getFile();
			}
		}
		return null;
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
		if (editor == null)
		{
			return null;
		}
		ILexemeProvider<PHPTokenType> lexemeProvider = ParsingUtils.createLexemeProvider(editor.getDocumentProvider()
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
				if (!StringUtil.isEmpty(name))
				{
					List<Object> elements = ContentAssistUtils.selectModelElements(name, true);
					if (!CollectionsUtil.isEmpty(elements))
					{
						// return the first element only
						entry = elements.get(0);
					}
				}
			}
			catch (BadLocationException e)
			{
				IdeLog.logError(PHPEditorPlugin.getDefault(),
						"PHP documentation hover - error getting an element at offset - " + hoverRegion.getOffset(), e); //$NON-NLS-1$
			}

		}
		if (entry != null)
		{
			return new Object[] { entry };
		}
		return null;
	}

	/**
	 * Open declaration action.
	 */
	public class OpenDeclarationAction extends Action
	{
		private static final String IMG_OPEN_DECLARATION = "icons/full/elcl16/goto_input.gif"; //$NON-NLS-1$
		private static final String IMG_OPEN_DECLARATION_DISABLED = "icons/full/dlcl16/goto_input.gif"; //$NON-NLS-1$
		private CustomBrowserInformationControl iControl;
		private IHyperlink[] hyperlinks;

		/**
		 * @param iControl
		 */
		public OpenDeclarationAction(CustomBrowserInformationControl iControl)
		{
			setText(Messages.PHPTextHover_openDeclarationTooltip);
			setImageDescriptor(UIEplPlugin.imageDescriptorFromPlugin(UIEplPlugin.PLUGIN_ID, IMG_OPEN_DECLARATION));
			setDisabledImageDescriptor(UIEplPlugin.imageDescriptorFromPlugin(UIEplPlugin.PLUGIN_ID,
					IMG_OPEN_DECLARATION_DISABLED));
			this.iControl = iControl;
		}

		/**
		 * Update the action
		 */
		void update()
		{
			BrowserInformationControlInput input = iControl.getInput();
			if (input instanceof DocumentationBrowserInformationControlInput)
			{
				PHPHyperlinkDetector detector = new PHPHyperlinkDetector();
				IRegion hoverRegion = ((DocumentationBrowserInformationControlInput) input).getHoverRegion();
				if (hoverRegion != null)
				{
					hyperlinks = detector.detectHyperlinks((PHPSourceEditor) getEditor(), hoverRegion, false);
					setEnabled(!ArrayUtil.isEmpty(hyperlinks) && hyperlinks[0] != null);
					return;
				}

			}
			setEnabled(false);
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.action.Action#run()
		 */
		@Override
		public void run()
		{
			// We already know that this hyperlink is valid. A check was made at the update call.
			iControl.dispose();
			hyperlinks[0].open();
		}
	}
}
