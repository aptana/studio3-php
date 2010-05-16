package com.aptana.editor.php.internal.ui.editor;

import com.aptana.editor.common.PartitionerSwitchStrategy;

/**
 * @author Max Stepanov
 */
public class PHPPartitionerSwitchStrategy extends PartitionerSwitchStrategy
{

	private static PHPPartitionerSwitchStrategy instance;

	private static final String[][] PHP_PAIRS = new String[][] { { "<?php", "?>" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "<?=", "?>" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "<?", "?>" }, //$NON-NLS-1$ //$NON-NLS-2$
			{ "<%", "%>" } //$NON-NLS-1$ //$NON-NLS-2$
	};

	private static final String[][] ESCAPE_PAIRS = new String[][] {
	// Escape from the beginning of a single line comment to the end of the line.
	// Closing php tags will be ignored.
	{ "//", null }, //$NON-NLS-1$
	};

	/**
	 * 
	 */
	private PHPPartitionerSwitchStrategy()
	{
		super(PHP_PAIRS, ESCAPE_PAIRS);
	}

	public static PHPPartitionerSwitchStrategy getDefault()
	{
		if (instance == null)
		{
			instance = new PHPPartitionerSwitchStrategy();
		}
		return instance;
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.editor.common.IPartitionerSwitchStrategy#getSwitchTagPairs()
	 */
	public String[][] getSwitchTagPairs()
	{
		return PHP_PAIRS;
	}

}
