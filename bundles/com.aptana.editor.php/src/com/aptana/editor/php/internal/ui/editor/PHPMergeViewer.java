/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.editor.php.internal.ui.editor;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.widgets.Composite;

import com.aptana.editor.common.ExtendedFastPartitioner;
import com.aptana.editor.common.IExtendedPartitioner;
import com.aptana.editor.common.TextUtils;
import com.aptana.editor.common.text.rules.CompositePartitionScanner;
import com.aptana.editor.common.viewer.CommonMergeViewer;
import com.aptana.editor.html.HTMLSourceConfiguration;

/**
 * @author cwilliams
 */
public class PHPMergeViewer extends CommonMergeViewer
{
	public PHPMergeViewer(Composite parent, CompareConfiguration configuration)
	{
		super(parent, configuration);
	}

	@Override
	protected IDocumentPartitioner getDocumentPartitioner()
	{
		CompositePartitionScanner partitionScanner = new CompositePartitionScanner(HTMLSourceConfiguration.getDefault()
				.createSubPartitionScanner(), PHPSourceConfiguration.getDefault().createSubPartitionScanner(),
				PHPPartitionerSwitchStrategy.getDefault());
		IDocumentPartitioner partitioner = new ExtendedFastPartitioner(partitionScanner,
				TextUtils.combine(new String[][] { CompositePartitionScanner.SWITCHING_CONTENT_TYPES,
						HTMLSourceConfiguration.getDefault().getContentTypes(),
						PHPSourceConfiguration.getDefault().getContentTypes() }));
		partitionScanner.setPartitioner((IExtendedPartitioner) partitioner);
		return partitioner;
	}

	@Override
	protected void configureTextViewer(TextViewer textViewer)
	{
		super.configureTextViewer(textViewer);

		if (textViewer instanceof SourceViewer)
		{
			SourceViewer sourceViewer = (SourceViewer) textViewer;
			sourceViewer.unconfigure();
			IPreferenceStore preferences = PHPSourceEditor.getChainedPreferenceStore();
			PHPSourceViewerConfiguration config = new PHPSourceViewerConfiguration(preferences, null);
			sourceViewer.configure(config);
		}
	}
}
