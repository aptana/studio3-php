/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the Eclipse Public License (EPL).
 * Please see the license-epl.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.ui.editor;

import java.util.Map;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.source.ISourceViewer;

import com.aptana.core.util.StringUtil;
import com.aptana.editor.common.AbstractThemeableEditor;
import com.aptana.editor.common.CompositeSourceViewerConfiguration;
import com.aptana.editor.common.IPartitionerSwitchStrategy;
import com.aptana.editor.common.contentassist.ContentAssistant;
import com.aptana.editor.common.text.RubyRegexpAutoIndentStrategy;
import com.aptana.editor.html.HTMLPlugin;
import com.aptana.editor.html.HTMLSourceConfiguration;
import com.aptana.editor.php.core.PHPDoubleClickStrategy;
import com.aptana.editor.php.internal.core.IPHPConstants;
import com.aptana.editor.php.internal.ui.editor.formatting.PHPAutoIndentStrategy;

public class PHPSourceViewerConfiguration extends CompositeSourceViewerConfiguration
{
	private PHPDoubleClickStrategy fDoubleClickStrategy;

	public PHPSourceViewerConfiguration(IPreferenceStore preferences, AbstractThemeableEditor editor)
	{
		super(HTMLSourceConfiguration.getDefault(), PHPSourceConfiguration.getDefault(), preferences, editor);
	}

	@Override
	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType)
	{
		if (fDoubleClickStrategy == null)
		{
			fDoubleClickStrategy = new PHPDoubleClickStrategy();
		}
		return fDoubleClickStrategy;
	}

	@Override
	protected IPartitionerSwitchStrategy getPartitionerSwitchStrategy()
	{
		return PHPPartitionerSwitchStrategy.getDefault();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.jface.text.source.SourceViewerConfiguration#getPresentationReconciler(org.eclipse.jface.text.source
	 * .ISourceViewer)
	 */
	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer)
	{
		PresentationReconciler reconciler = (PresentationReconciler) super.getPresentationReconciler(sourceViewer);
		PHPSourceConfiguration.getDefault().setupPresentationReconciler(reconciler, sourceViewer);
		return reconciler;
	}

	@Override
	protected String getStartEndTokenType()
	{
		// We already handle the full scope in PHPSourceConfiguration
		return StringUtil.EMPTY;
	}

	@Override
	protected String getTopContentType()
	{
		return IPHPConstants.CONTENT_TYPE_HTML_PHP;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.aptana.editor.common.CommonSourceViewerConfiguration#getAutoEditStrategies(org.eclipse.jface.text.source.
	 * ISourceViewer, java.lang.String)
	 */
	@Override
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType)
	{
		if (contentType.startsWith(IPHPConstants.PREFIX))
		{
			return new IAutoEditStrategy[] { new PHPAutoIndentStrategy(contentType, this, sourceViewer) };
		}
		return new IAutoEditStrategy[] { new RubyRegexpAutoIndentStrategy(contentType, this, sourceViewer, HTMLPlugin
				.getDefault().getPreferenceStore()) };
	}

	@Override
	protected IContentAssistProcessor getContentAssistProcessor(ISourceViewer sourceViewer, String contentType)
	{
		if (getEditor() == null)
		{
			return null;
		}
		return PHPSourceConfiguration.getDefault().getContentAssistProcessor(getEditor(), contentType);
	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer)
	{
		IContentAssistant assistant = super.getContentAssistant(sourceViewer);
		if (assistant instanceof ContentAssistant)
		{
			// Turn on auto insert if only one proposal
			ContentAssistant contentAssistant = (ContentAssistant) assistant;
			contentAssistant.enableAutoInsert(true);
			// This one is a little buggy, as it does not update the proposal replacement string,
			// so for now it's off.
			// contentAssistant.enablePrefixCompletion(true);
		}
		return assistant;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Map getHyperlinkDetectorTargets(ISourceViewer sourceViewer)
	{
		Map targets = super.getHyperlinkDetectorTargets(sourceViewer);
		targets.put("com.aptana.editor.php", getEditor()); //$NON-NLS-1$
		return targets;
	}
}
